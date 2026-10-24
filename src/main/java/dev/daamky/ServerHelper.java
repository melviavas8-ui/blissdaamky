package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ServerHelper extends ModuleStructure {
   private static final String REALLY_WORLD_NAME = "ReallyWorld";
   private static final String FUN_TIME_NAME = "FunTime";
   private static final String HOLY_WORLD_NAME = "HolyWorld";
   private static final String LONY_GRIEF_NAME = "LonyGrief";
   private static final String ANTI_FLY_ITEM_NAME = "Анти Полет";
   private static final String REALLY_WORLD_BLOCKING_SCREEN_TITLE = "ꈁꀀꈂꌁꈂꀁ§0ꈃꄀ";
   private static final List<Pattern> BLOCKED_CLIENT_NAME_PATTERNS = List.of(
         "акриен(а|у|ом|е|чик)?",
         "рич(а|у|ом|ей|е)?",
         "ньюкод(ом|а|у|ами|ик|е)?",
         "экспенсив(ом|а|у|ами|е)?",
         "импакт(ом|а|у|ами|ик|е)?",
         "экселл?ент(ом|а|у|ами|ик|е)?",
         "катл[ао]ван(ом|а|у|ами|чик)?",
         "целестиал(ом|а|у|ами|е)?",
         "целк(ой|а|у|ами|очка|е)?",
         "матикс(ом|а|у|ами|е)?",
         "инерти(я|ей|ю|ями|е)?",
         "эксп(а|ой|ою|у|уличка|е)?",
         "флюгер(ом|а|у|ами)?",
         "рикер(а|у|ом|очек)?",
         "фанпе(й|ю|я|ем|е|йчик)?",
         "вексайд(ом|а|у|ами|ик|е)?",
         "нурсултан(а|у|е|ом|чик)?",
         "нурик(а|у|ом|е)?",
         "нурлан(а|у|ом|чик|е)?",
         "векс(ом|у|а|ами|ик|е)?",
         "релейк(ом|у|а|ами|е)?",
         "арбуз(ом|а|у|ами|ик|е)?",
         "вилд(ом|у|а|ами|ик|е)?",
         "фантайм(е|а|у)?",
         "холик(е|а|у)?",
         "холиворлд(а|у|е)?",
         "рокстар(ом|а|у|ами|чик|е)?",
         "рогалик(а|у|ом|е)?",
         "тандерхак(ом|у|и|ами|а|е)?",
         "ликвидбаунс(а|у|ами|е)?",
         "expensive",
         "celestial",
         "newcode",
         "arbuz",
         "akrien",
         "nursultan",
         "relake",
         "wild",
         "wurst",
         "catl[ao]van",
         "excellent",
         "rockstar",
         "impact",
         "matix",
         "inertia",
         "wex",
         "wexside",
         "nurik",
         "nurlan",
         "rich",
         "funpay",
         "fluger",
         "riker",
         "funtime",
         "holyworld",
         "wwe",
         "hvh",
         "rogalik",
         "thunderhack",
         "liquidbounce"
      )
      .stream()
      .map(Pattern::compile)
      .toList();
   private final MultiOptionSetting serverMode = new MultiOptionSetting(
      "Сервер", "ReallyWorld", "ReallyWorld", "FunTime", "HolyWorld", "LonyGrief"
   );
   private final BooleanSetting detectServerFromAddress = new BooleanSetting("Определять сервер по IP", true);
   private final ReallyWorld reallyWorldSettings = new ReallyWorld();
   private final IntSetting antiFlyKey = new IntSetting("Анти-полёт", 0);
   private final BooleanSetting closeMenuOnJoin = new BooleanSetting("Закрывать меню при входе", false);
   private final BooleanSetting chatFilterEnabled = new BooleanSetting("Фильтр чата", true);
   private final List<ServerHelper.ItemFilterRule> itemFilterRules = new ArrayList<>();
   private int antiFlySwapTicksRemaining;
   private int antiFlyPreviousSlot = -1;

   public ServerHelper() {
      super("ServerHelper", "Помощник серверов: бинды предметов ReallyWorld, FunTime, HolyWorld, LonyGrief", ModuleCategory.WORLD);
      this.antiFlyKey.visibleWhen(() -> this.serverMode.isSelected("ReallyWorld"));
      this.closeMenuOnJoin.visibleWhen(() -> this.serverMode.isSelected("ReallyWorld"));
      this.chatFilterEnabled.visibleWhen(() -> this.serverMode.isSelected("ReallyWorld"));
      this.addSettings(
         this.serverMode, this.detectServerFromAddress, this.reallyWorldSettings, this.antiFlyKey, this.closeMenuOnJoin, this.chatFilterEnabled
      );
      this.registerItemFilterRule("Ловушка", stack -> stack.isOf(Items.HEART_OF_THE_SEA), "ReallyWorld");
      this.registerItemFilterRule("Эндер Ловушка", stack -> InventoryUtils.itemNameContains(stack, "Эндер Ловушка"), "ReallyWorld");
      this.registerItemFilterRule(
         "Трапка",
         stack -> this.serverMode.isSelected("HolyWorld")
            ? stack.isOf(Items.PRISMARINE_SHARD)
            : stack.isOf(Items.NETHERITE_SCRAP) && InventoryUtils.itemNameContains(stack, "трапка"),
         "FunTime",
         "HolyWorld"
      );
      this.registerItemFilterRule(
         "Пласт", stack -> stack.isOf(Items.DRIED_KELP) && InventoryUtils.itemNameContains(stack, "пласт"), "FunTime"
      );
      this.registerItemFilterRule(
         "Явная пыль", stack -> stack.isOf(Items.SUGAR) && InventoryUtils.itemNameContains(stack, "явная пыль"), "FunTime"
      );
      this.registerItemFilterRule(
         "Дезориентация", stack -> stack.isOf(Items.ENDER_EYE) && InventoryUtils.itemNameContains(stack, "дезориентация"), "FunTime"
      );
      this.registerItemFilterRule("Огненный смерч", stack -> stack.isOf(Items.FIRE_CHARGE), "FunTime");
      this.registerItemFilterRule("Снежок заморозка", stack -> stack.isOf(Items.SNOWBALL), "FunTime");
      this.registerItemFilterRule("Божья аура", stack -> stack.isOf(Items.PHANTOM_MEMBRANE), "FunTime");
      this.registerPotionItemRule("Хлопушка", "FunTime");
      this.registerPotionItemRule("Святая вода", "FunTime");
      this.registerPotionItemRule("Зелье Гнева", "FunTime");
      this.registerPotionItemRule("Зелье Палладина", "FunTime");
      this.registerPotionItemRule("Зелье Ассасина", "FunTime");
      this.registerPotionItemRule("Зелье Радиации", "FunTime");
      this.registerPotionItemRule("Снотворное", "FunTime");
      this.registerItemFilterRule("Снежок", stack -> stack.isOf(Items.SNOWBALL), "HolyWorld");
      this.registerItemFilterRule("Прощальный гул", stack -> stack.isOf(Items.FIREWORK_STAR), "HolyWorld");
      this.registerItemFilterRule("Стан", stack -> stack.isOf(Items.NETHER_STAR), "HolyWorld");
      this.registerItemFilterRule("Обычная ливалка", stack -> stack.isOf(Items.CLAY_BALL), "LonyGrief");
      this.registerItemFilterRule("Ливалка с платформой", stack -> stack.isOf(Items.MAGMA_CREAM), "LonyGrief");
      this.registerItemFilterRule("Уникальная трапка", stack -> stack.isOf(Items.CRYING_OBSIDIAN), "LonyGrief");
      this.registerItemFilterRule("Уникальное перо", stack -> stack.isOf(Items.FEATHER), "LonyGrief");
      this.setEnabled(true);
   }

   private void registerPotionItemRule(String itemName, String... serverModes) {
      this.registerItemFilterRule(itemName, candidateStack -> candidateStack.isOf(Items.SPLASH_POTION) && InventoryUtils.itemNameContains(candidateStack, itemName), serverModes);
   }

   private void registerItemFilterRule(String itemName, Predicate<ItemStack> itemPredicate, String... serverModes) {
      Set allowedServerModes = Set.of(serverModes);
      IntSetting keyBindingSetting = new IntSetting(itemName, 0);
      keyBindingSetting.visibleWhen(() -> allowedServerModes.contains(this.serverMode.getValue()));
      this.itemFilterRules.add(new ServerHelper.ItemFilterRule(itemName, keyBindingSetting, itemPredicate, allowedServerModes));
      this.addSetting(keyBindingSetting);
   }

   @Override
   public String getDisplayInfo() {
      return this.serverMode.getValue();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.antiFlySwapTicksRemaining = 0;
      this.antiFlyPreviousSlot = -1;
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.antiFlySwapTicksRemaining = 0;
      this.antiFlyPreviousSlot = -1;
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onKey(EventKey eventKey) {
      if (this.hasActiveClientWorld()) {
         int keyCode = dev.daamky.KeybindManager.encodeKey(eventKey.key(), eventKey.isMouse());

         for (Object itemFilterRule : this.itemFilterRules) {
            if (((ServerHelper.ItemFilterRule)itemFilterRule).modes().contains(this.serverMode.getValue())
               && ((ServerHelper.ItemFilterRule)itemFilterRule).keySetting().matchesKeyCode(keyCode)
               && !InventoryUtils.useItemFromInventory(((ServerHelper.ItemFilterRule)itemFilterRule).itemPredicate(), this.reallyWorldSettings.getInventorySwapCategory())) {
               ClientChatLogger.l1I1IlllIlI("Нет предмета: " + ((ServerHelper.ItemFilterRule)itemFilterRule).name());
            }
         }

         int antiFlyItemSlot;
         if (this.serverMode.isSelected("ReallyWorld")
            && this.antiFlyKey.matchesKeyCode(keyCode)
            && this.antiFlySwapTicksRemaining == 0
            && InventoryUtils.canSwapInventorySlots()
            && (antiFlyItemSlot = InventoryUtils.findItemSlot(antiFlyItemStack -> InventoryUtils.itemNameContains(antiFlyItemStack, "Анти Полет"))) != -1) {
            this.antiFlyPreviousSlot = antiFlyItemSlot;
            InventoryUtils.swapInventorySlotWithOffhand(antiFlyItemSlot, this.reallyWorldSettings.getInventorySwapCategory());
            this.antiFlySwapTicksRemaining = 20;
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.detectServerFromAddress.getValue()) {
         String detectedServerMode = this.detectServerModeFromAddress();
         if (detectedServerMode != null && !this.serverMode.isSelected(detectedServerMode)) {
            this.serverMode.setValue(detectedServerMode);
         }
      }

      if (this.antiFlySwapTicksRemaining <= 1) {
         if (this.antiFlySwapTicksRemaining == 1
            && this.antiFlyPreviousSlot != -1
            && this.hasActiveClientWorld()
            && InventoryUtils.canSwapInventorySlots()
            && CLIENT.player.currentScreenHandler.getCursorStack().isEmpty()) {
            this.antiFlySwapTicksRemaining = 0;
            InventoryUtils.swapInventorySlotWithOffhand(this.antiFlyPreviousSlot, this.reallyWorldSettings.getInventorySwapCategory());
            this.antiFlyPreviousSlot = -1;
         }
      } else {
         this.antiFlySwapTicksRemaining--;
      }
   }

   private String detectServerModeFromAddress() {
      ServerInfo serverInfo = CLIENT.getCurrentServerEntry();
      if (serverInfo != null && serverInfo.address != null) {
         String normalizedServerAddress = serverInfo.address.toLowerCase(Locale.ROOT);
         if (normalizedServerAddress.contains("reallyworld")) {
            return "ReallyWorld";
         } else if (normalizedServerAddress.contains("funtime")) {
            return "FunTime";
         } else if (normalizedServerAddress.contains("holyworld")) {
            return "HolyWorld";
         } else {
            return normalizedServerAddress.contains("lonygrief") ? "LonyGrief" : null;
         }
      } else {
         return null;
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onPacket(EventPacket eventPacket) {
      if (CLIENT.player != null && this.serverMode.isSelected("ReallyWorld")) {
         if (eventPacket.isReceive() && this.closeMenuOnJoin.getValue()) {
            Packet packet = eventPacket.packet();
            if (packet instanceof OpenScreenS2CPacket) {
            OpenScreenS2CPacket packet2 = (OpenScreenS2CPacket)packet;
               if (packet2.getName().getString().contains("ꈁꀀꈂꌁꈂꀁ§0ꈃꄀ") && CLIENT.player.age < 100) {
                  eventPacket.cancel();
                  PacketSender.II1Il11l111II11IIl(new CloseHandledScreenC2SPacket(packet2.getSyncId()));
                  return;
               }
            }
         }
      }
   }

   public static boolean shouldFilterChatMessage(String message) {
      ServerHelper moduleStructure = (ServerHelper)ModuleRegistry.getInstance().getModuleByClass(ServerHelper.class);
      if (moduleStructure != null
         && moduleStructure.isEnabled()
         && moduleStructure.serverMode.isSelected("ReallyWorld")
         && moduleStructure.chatFilterEnabled.getValue()) {
         String normalizedMessage = message.toLowerCase(Locale.ROOT);

         for (Pattern blockedClientPattern : BLOCKED_CLIENT_NAME_PATTERNS) {
            if (blockedClientPattern.matcher(normalizedMessage).find()) {
               ClientChatLogger.l1I1IlllIlI("Найдено запретное слово, сообщение отменено");
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private record ItemFilterRule(String name, IntSetting keySetting, Predicate<ItemStack> itemPredicate, Set<String> modes) {

      private ItemFilterRule(String name, IntSetting keySetting, Predicate<ItemStack> itemPredicate, Set<String> modes) {
         this.name = name;
         this.keySetting = keySetting;
         this.itemPredicate = itemPredicate;
         this.modes = modes;
      }
   }
}

