package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventTick;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoDuel extends ModuleStructure {
   private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
   private final MultiToggleSetting enabledDuelKits = new MultiToggleSetting("Наборы")
      .addOption("Щит", true)
      .addOption("Шипы", false)
      .addOption("Лук", false)
      .addOption("Тотемы", false)
      .addOption("Хил", false)
      .addOption("Шары", false)
      .addOption("Классика", true)
      .addOption("Читеры", false)
      .addOption("Незер", false);
   private final BooleanSetting wagerEnabled = new BooleanSetting("На деньги", false);
   private final FloatSetting wagerAmount = new FloatSetting("Ставка", 50.0F, 0.0F, 500.0F, 50.0F);
   private final FloatSetting challengeCooldown = new FloatSetting("Задержка запросов", 1500.0F, 500.0F, 10000.0F, 100.0F, "мс");
   private final FloatSetting menuClickDelay = new FloatSetting("Задержка кликов", 150.0F, 20.0F, 400.0F, 10.0F, "мс");
   private final Stopwatch challengeStopwatch = new Stopwatch();
   private final Stopwatch menuClickStopwatch = new Stopwatch();
   private final List<Integer> processedMenuSlotIds = new ArrayList<>();
   private final List<String> challengedPlayerNames = new ArrayList<>();
   private int lastScreenHandlerId = -1;
   private volatile boolean stopAfterDuelResponse;

   public AutoDuel() {
      super("AutoDuel", "Автоматически вызывает игроков на дуэль и выбирает набор", ModuleCategory.WORLD);
      this.wagerAmount.visibleWhen(this.wagerEnabled::getValue);
      this.addSettings(this.enabledDuelKits, this.wagerEnabled, this.wagerAmount, this.challengeCooldown, this.menuClickDelay);
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.stopAfterDuelResponse = false;
      this.processedMenuSlotIds.clear();
      this.challengedPlayerNames.clear();
      this.lastScreenHandlerId = -1;
      this.challengeStopwatch.reset();
   }

   @EventTarget
   public void onTick(EventTick eventTick) {
      if (CLIENT.player != null && CLIENT.getNetworkHandler() != null && !this.stopAfterDuelResponse) {
         Screen screen = CLIENT.currentScreen;
         if (screen instanceof GenericContainerScreen) {
            GenericContainerScreen screen2 = (GenericContainerScreen)screen;
            this.handleDuelMenu(screen2);
            this.challengeStopwatch.reset();
         } else if (CLIENT.currentScreen == null && this.challengeStopwatch.hasElapsed(this.challengeCooldown.getValue().longValue())) {
            ArrayList arrayList = new ArrayList();

            for (PlayerListEntry playerListEntry : CLIENT.getNetworkHandler().getPlayerList()) {
               String playerName = playerListEntry.getProfile().name();
               if (playerName != null
                  && PLAYER_NAME_PATTERN.matcher(playerName).matches()
                  && !playerName.equals(CLIENT.player.getGameProfile().name())
                  && !FriendManager.getInstance().isFriend(playerName)
                  && !this.challengedPlayerNames.contains(playerName)) {
                  arrayList.add(playerName);
               }
            }

            if (arrayList.isEmpty()) {
               if (!this.challengedPlayerNames.isEmpty()) {
                  this.challengedPlayerNames.clear();
               }
            } else {
               Object targetPlayerName = (String)arrayList.get(ThreadLocalRandom.current().nextInt(arrayList.size()));
               String duelCommand = "duel " + targetPlayerName + (this.wagerEnabled.getValue() ? " " + this.wagerAmount.getValue().intValue() : "");
               CLIENT.getNetworkHandler().sendChatCommand(duelCommand);
               this.challengedPlayerNames.add((String)targetPlayerName);
               if (this.challengedPlayerNames.size() > 10) {
                  this.challengedPlayerNames.remove(0);
               }

               this.challengeStopwatch.reset();
            }
         }
      }
   }

   private void handleDuelMenu(GenericContainerScreen genericContainerScreen) {
      String lowercaseScreenTitle = genericContainerScreen.getTitle().getString().toLowerCase(Locale.ROOT);
      boolean isKitSelectionScreen = lowercaseScreenTitle.contains("выбор набора");
      boolean isDuelSettingsScreen = lowercaseScreenTitle.contains("настройка поединка");
      if ((isKitSelectionScreen || isDuelSettingsScreen) && this.menuClickStopwatch.hasElapsed(this.menuClickDelay.getValue().longValue())) {
         GenericContainerScreenHandler containerHandler = (GenericContainerScreenHandler)genericContainerScreen.getScreenHandler();
         if (containerHandler.syncId != this.lastScreenHandlerId) {
            this.lastScreenHandlerId = containerHandler.syncId;
            this.processedMenuSlotIds.clear();
         }

         if (this.processedMenuSlotIds.isEmpty()) {
            if (isDuelSettingsScreen) {
               this.clickMenuSlot(containerHandler, 0);
            } else {
               ArrayList arrayList = new ArrayList();
               List items = this.enabledDuelKits.getOptions();

               for (int kitOptionIndex = 0; kitOptionIndex < items.size(); kitOptionIndex++) {
                  if (((BooleanSetting)items.get(kitOptionIndex)).getValue()) {
                     arrayList.add(kitOptionIndex);
                  }
               }

               if (!arrayList.isEmpty()) {
                  this.clickMenuSlot(containerHandler, (Integer)arrayList.get(ThreadLocalRandom.current().nextInt(arrayList.size())));
               }
            }
         }
      }
   }

   private void clickMenuSlot(GenericContainerScreenHandler genericContainerScreenHandler, int slotIndex) {
      CLIENT.interactionManager.clickSlot(genericContainerScreenHandler.syncId, slotIndex, 0, SlotActionType.PICKUP, CLIENT.player);
      this.processedMenuSlotIds.add(slotIndex);
      this.menuClickStopwatch.reset();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onPacket(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof GameMessageS2CPacket) {
            GameMessageS2CPacket packet2 = (GameMessageS2CPacket)packet;
             String chatMessage = packet2.content().getString().toLowerCase(Locale.ROOT);
            if (chatMessage.contains("начало") || chatMessage.contains("через") && chatMessage.contains("секунд") || chatMessage.contains("принял") && !chatMessage.contains("не принял")) {
               this.stopAfterDuelResponse = true;
               CLIENT.execute(() -> this.setEnabled(false));
            }

            return;
         }
      }
   }
}

