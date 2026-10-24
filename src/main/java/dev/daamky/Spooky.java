package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public final class Spooky {
   private static final Spooky INSTANCE = new Spooky();
   private static final long RECONNECT_TIMEOUT_MS = 25000L;
   private Spooky.ReconnectState reconnectState = Spooky.ReconnectState.IDLE;
   private int targetAnarchy;
   private long reconnectStartedAtMs;
   private int phaseTicks;
   private int lastMenuSyncId = -1;

   private Spooky() {
   }

   public static Spooky getInstance() {
      return INSTANCE;
   }

   public boolean isReconnecting() {
      return this.reconnectState != Spooky.ReconnectState.IDLE;
   }

   public void startAnarchyReconnect(String[] textArray) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.getNetworkHandler() != null) {
         if (textArray.length == 0) {
            ClientChatLogger.l1I1IlllIlI("Specify anarchy number: .rct <number>");
         } else {
            try {
               this.targetAnarchy = Integer.parseInt(textArray[0]);
            } catch (NumberFormatException numberFormatException) {
               ClientChatLogger.l1I1IlllIlI("Anarchy number must be a number");
               return;
            }

            if (this.targetAnarchy <= 0) {
               ClientChatLogger.l1I1IlllIlI("Anarchy number must be greater than zero");
            } else {
               this.reconnectStartedAtMs = System.currentTimeMillis();
               this.lastMenuSyncId = -1;
               if (!serverAddressContains("funtime") && !serverAddressContains("spooky")) {
                  client.getNetworkHandler().sendChatCommand("hub");
                  this.reconnectState = Spooky.ReconnectState.WAITING_FOR_HUB;
                  this.phaseTicks = 0;
                  ClientChatLogger.II1Il11l111II11IIl("Switching to hub, then anarchy #" + this.targetAnarchy);
               } else {
                  client.getNetworkHandler().sendChatCommand("an" + this.targetAnarchy);
                  ClientChatLogger.II1Il11l111II11IIl("Reconnecting to anarchy #" + this.targetAnarchy);
                  this.reconnectState = Spooky.ReconnectState.IDLE;
               }
            }
         }
      }
   }

   private static boolean serverAddressContains(String serverFragment) {
      ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
      return serverInfo != null && serverInfo.address != null && serverInfo.address.toLowerCase(Locale.ROOT).contains(serverFragment);
   }

   @EventTarget
   public void onWorldChange(EventWorldChange eventWorldChange) {
      if (this.reconnectState == Spooky.ReconnectState.WAITING_FOR_HUB) {
         this.reconnectState = Spooky.ReconnectState.WAITING_FOR_SERVER_MENU;
         this.phaseTicks = 0;
      }
   }

   @EventTarget
   public void tickReconnect(EventTick eventTick) {
      if (this.reconnectState != Spooky.ReconnectState.IDLE) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (System.currentTimeMillis() - this.reconnectStartedAtMs > 25000L) {
            this.reconnectState = Spooky.ReconnectState.IDLE;
            if (client.player != null) {
               ClientChatLogger.l1I1IlllIlI("Failed to reconnect: timed out");
            }
         } else if (client.player != null && client.interactionManager != null) {
            this.phaseTicks++;
            switch (this.reconnectState) {
               case WAITING_FOR_HUB:
                  if (this.phaseTicks > 60) {
                     this.reconnectState = Spooky.ReconnectState.WAITING_FOR_SERVER_MENU;
                     this.phaseTicks = 0;
                  }
                  break;
               case WAITING_FOR_SERVER_MENU:
                  if (this.phaseTicks < 30 || this.phaseTicks % 20 != 0) {
                     return;
                  }

                  if (client.currentScreen instanceof GenericContainerScreen) {
                     this.reconnectState = Spooky.ReconnectState.SELECTING_ANARCHY;
                     this.phaseTicks = 0;
                     return;
                  }

                  int compassSlot = InventoryUtils.findHotbarSlot(compassStack -> compassStack.isOf(Items.COMPASS));
                  if (compassSlot != -1) {
                     InventoryUtils.useHotbarSlot(compassSlot, client.player.getYaw(), client.player.getPitch());
                  }
                  break;
               case SELECTING_ANARCHY:
                  if (this.phaseTicks % 6 != 0) {
                     return;
                  }

                  Screen screen = client.currentScreen;
                  if (!(screen instanceof GenericContainerScreen)) {
                     return;
                  }

                  GenericContainerScreen screen2 = (GenericContainerScreen)screen;
                  GenericContainerScreenHandler containerHandler = (GenericContainerScreenHandler)screen2.getScreenHandler();
                  if (containerHandler.syncId == this.lastMenuSyncId) {
                     return;
                  }

                  String containerTitle = screen2.getTitle().getString().toLowerCase(Locale.ROOT);
                  int menuSlotCount = containerHandler.getRows() * 9;
                  if (containerTitle.contains("выбор сервера") || containerTitle.contains("выберите сервер")) {
                     int serverSelectionSlot = findSlotByName(containerHandler, menuSlotCount, textx -> textx.contains("гриф") || textx.contains("анарх"));
                     if (serverSelectionSlot != -1) {
                        clickMenuSlot(containerHandler, serverSelectionSlot);
                        this.lastMenuSyncId = containerHandler.syncId;
                     }
                  } else if (containerTitle.contains("выбор мира") || containerTitle.contains("гриф") || containerTitle.contains("анарх")) {
                     Pattern pattern = Pattern.compile("(^|\\D)" + this.targetAnarchy + "(\\D|$)");
                     int anarchySelectionSlot = findSlotByName(containerHandler, menuSlotCount, textx -> pattern.matcher(textx).find());
                     if (anarchySelectionSlot != -1) {
                        clickMenuSlot(containerHandler, anarchySelectionSlot);
                        ClientChatLogger.II1Il11l111II11IIl("Selected anarchy #" + this.targetAnarchy);
                        this.reconnectState = Spooky.ReconnectState.IDLE;
                     }
                  }
            }
         }
      }
   }

   private static int findSlotByName(GenericContainerScreenHandler container, int slotLimit, Predicate<String> namePredicate) {
      for (int slotIndex = 0; slotIndex < slotLimit && slotIndex < container.slots.size(); slotIndex++) {
         ItemStack candidateStack = ((Slot)container.slots.get(slotIndex)).getStack();
         if (!candidateStack.isEmpty() && namePredicate.test(candidateStack.getName().getString().toLowerCase(Locale.ROOT))) {
            return slotIndex;
         }
      }

      return -1;
   }

   private static void clickMenuSlot(GenericContainerScreenHandler container, int slotIndex) {
      MinecraftClient client = MinecraftClient.getInstance();
      client.interactionManager.clickSlot(container.syncId, slotIndex, 0, SlotActionType.PICKUP, client.player);
   }

   private enum ReconnectState {
      IDLE,
      WAITING_FOR_HUB,
      WAITING_FOR_SERVER_MENU,
      SELECTING_ANARCHY;

      // $VF: synthetic method
      private static Spooky.ReconnectState[] II1Il11l111II11IIl() {
         return new Spooky.ReconnectState[]{IDLE, WAITING_FOR_HUB, WAITING_FOR_SERVER_MENU, SELECTING_ANARCHY};
      }
   }
}

