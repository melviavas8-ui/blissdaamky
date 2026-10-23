package dev.daamky;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.hud.ChatHud.ChatState;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;

final class BotSessionSwitcher {
    private static final String p3b370f19 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final long SWITCH_NOTICE_DURATION_MILLIS = 900L;
   private volatile BotSession activeDisconnect;
   private volatile BotSession queuedDisconnect;
   private ClientStateSnapshot clientStateBeforeSwitch;
   private Input playerInputBeforeSwitch;
   private KeyBindingStateSnapshot keyBindingStateBeforeSwitch;
   private int previousViewDistance;
   private boolean switchInProgress;
   private volatile long lastSwitchNoticeTimeMillis;
   private ChatState previousChatState;
   private BotSession chatStateDisconnect;
// https://t.me/pasterplace

   private void updateSwitchNoticeTimestamp() {
      this.lastSwitchNoticeTimeMillis = System.currentTimeMillis();
   }
// https://t.me/pasterplace

   float getSwitchNoticeAlpha() {
      long lastSwitchNoticeTimeMillis = this.lastSwitchNoticeTimeMillis;
      if (lastSwitchNoticeTimeMillis == 0L) {
         return 0.0F;
      }

      long elapsedMillis = System.currentTimeMillis() - lastSwitchNoticeTimeMillis;
      return elapsedMillis >= 900L ? 0.0F : 1.0F - (float)elapsedMillis / 900.0F;
   }
// https://t.me/pasterplace

   void switchToDisconnect(BotSession disconnect) {
      MinecraftClient client = MinecraftClient.getInstance();
      KeyBindingStateSnapshot previousKeyBindingState = dev.daamky.KeyBindingStateSnapshot.capture(client);
      ModuleToggleProfileManager.switchActiveProfile(disconnect.getUsername());
      if (!this.switchInProgress) {
         this.clientStateBeforeSwitch = ClientStateSnapshot.capture(client);
         this.playerInputBeforeSwitch = client.player != null ? client.player.input : null;
         this.keyBindingStateBeforeSwitch = dev.daamky.KeyBindingStateSnapshot.capture(client);
         this.previousViewDistance = client.options.getClampedViewDistance();
         this.switchInProgress = true;
      } else if (this.activeDisconnect != null) {
         this.activeDisconnect.getKeyBindingStateSnapshot().clear();
         this.activeDisconnect.resetPlayerLoadedState();
         this.activeDisconnect = null;
      }

      if (disconnect.hasPlayerControlContext()) {
         disconnect.getKeyBindingStateSnapshot().copyFrom(previousKeyBindingState);
         disconnect.getKeyBindingStateSnapshot().restoreTo(client);
         this.activeDisconnect = disconnect;
         this.updateSwitchNoticeTimestamp();
         this.updateChatForDisconnect(disconnect);
      } else {
         this.restoreClientState();
         ModuleToggleProfileManager.resetToDefaultProfile();
      }
   }
// https://t.me/pasterplace

   void restorePreviousSession() {
      ModuleToggleProfileManager.resetToDefaultProfile();
      boolean hadActiveDisconnect = this.activeDisconnect != null;
      MinecraftClient client = MinecraftClient.getInstance();
      if (this.activeDisconnect != null) {
         this.keyBindingStateBeforeSwitch = dev.daamky.KeyBindingStateSnapshot.capture(client);
         this.activeDisconnect.getKeyBindingStateSnapshot().clear();
         this.activeDisconnect.resetPlayerLoadedState();
      }

      this.restoreClientState();
      this.activeDisconnect = null;
      this.queuedDisconnect = null;
      if (hadActiveDisconnect) {
         this.updateSwitchNoticeTimestamp();
      }
// https://t.me/pasterplace

      this.updateChatForDisconnect(null);
   }
// https://t.me/pasterplace

   private void updateChatForDisconnect(BotSession disconnect) {
      if (this.chatStateDisconnect != disconnect) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.inGameHud == null) {
            this.chatStateDisconnect = disconnect;
         } else {
            ChatHud chatHud = client.inGameHud.getChatHud();
            if (this.chatStateDisconnect == null) {
               this.previousChatState = chatHud.toChatState();
            }

            if (disconnect == null) {
               if (this.previousChatState != null) {
                  chatHud.restoreChatState(this.previousChatState);
               } else {
                  chatHud.clear(false);
               }
            } else {
               chatHud.restoreChatState(this.createChatStateForDisconnect(client, chatHud, disconnect));
            }

            this.chatStateDisconnect = disconnect;
         }
      }
   }
// https://t.me/pasterplace

   private ChatState createChatStateForDisconnect(MinecraftClient client, ChatHud chatHud, BotSession disconnect) {
      int currentTick = client.inGameHud.getTicks();
      List items = disconnect.getChatMessages();
      ArrayList arrayList = new ArrayList(items.size());

      for (int messageIndex = items.size() - 1; messageIndex >= 0; messageIndex--) {
         arrayList.add(new ChatHudLine(currentTick, (Text)items.get(messageIndex), null, MessageIndicator.system()));
      }

      return new ChatState(arrayList, new ArrayList(chatHud.getMessageHistory()), new ArrayList());
   }
// https://t.me/pasterplace

   BotSession activeDisconnect() {
      return this.activeDisconnect;
   }
// https://t.me/pasterplace

   boolean isActiveDisconnectReady() {
      return this.activeDisconnect != null && this.activeDisconnect.isOnline();
   }
// https://t.me/pasterplace

   boolean owns(Object sessionObject) {
      return this.activeDisconnect != null && this.activeDisconnect.ownsSessionObject(sessionObject);
   }
// https://t.me/pasterplace

   boolean isSwitchingTo(BotSession disconnect) {
      return this.switchInProgress && this.activeDisconnect == disconnect;
   }
// https://t.me/pasterplace

   void restoreIfActive(BotSession disconnect) {
      if (this.activeDisconnect == disconnect) {
         this.restorePreviousSession();
      }
   }
// https://t.me/pasterplace

   void clearQueuedDisconnect(BotSession disconnect) {
      if (this.queuedDisconnect == disconnect) {
         this.queuedDisconnect = null;
      }
   }
// https://t.me/pasterplace

   void queueDisconnect(BotSession disconnect) {
      if (this.activeDisconnect == disconnect) {
         this.restorePreviousSession();
         this.queuedDisconnect = disconnect;
      }
   }
// https://t.me/pasterplace

   void processQueuedDisconnect(BotSession disconnect) {
      if (this.queuedDisconnect == disconnect) {
         this.switchToDisconnect(disconnect);
         if (this.activeDisconnect == disconnect) {
            this.queuedDisconnect = null;
         }
      }
   }
// https://t.me/pasterplace

   void tickQueuedDisconnect() {
      if (this.queuedDisconnect != null && this.queuedDisconnect.isOnline() && !this.switchInProgress) {
         this.processQueuedDisconnect(this.queuedDisconnect);
      }
   }
// https://t.me/pasterplace

   void runWithClientStateIsolation(Runnable runnable) {
      if (this.switchInProgress && this.clientStateBeforeSwitch != null) {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientStateSnapshot previousClientState = ClientStateSnapshot.capture(client);
         this.clientStateBeforeSwitch.restore(client, false);

         try {
            runnable.run();
            this.clientStateBeforeSwitch = ClientStateSnapshot.capture(client);
         } finally {
            previousClientState.restore(client, false);
         }
      } else {
         runnable.run();
      }
   }
// https://t.me/pasterplace

   void tickSavedConnection() {
      if (this.switchInProgress && this.clientStateBeforeSwitch != null) {
         ClientPlayerEntity clientPlayerEntity = this.clientStateBeforeSwitch.getPlayer();
         if (clientPlayerEntity != null) {
            ClientConnection clientConnection = clientPlayerEntity.networkHandler.getConnection();
            if (clientConnection != null && clientConnection.isOpen()) {
               this.runWithClientStateIsolation(clientConnection::tick);
            }
         }
      }
   }
// https://t.me/pasterplace

   private void restoreClientState() {
      if (this.switchInProgress) {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientStateSnapshot previousClientState = this.clientStateBeforeSwitch;
         ClientWorld clientWorld = previousClientState.getWorld();
         if (client.world != clientWorld) {
            client.world = clientWorld;
            if (clientWorld != null) {
               client.particleManager.setWorld(clientWorld);
               client.worldRenderer.setWorld(clientWorld);
               client.gameRenderer.setWorld(clientWorld);
            }
         }

         ClientPlayerEntity clientPlayerEntity = previousClientState.getPlayer();
         client.player = clientPlayerEntity;
         if (clientPlayerEntity != null) {
            clientPlayerEntity.input = (Input)(this.playerInputBeforeSwitch != null ? this.playerInputBeforeSwitch : new KeyboardInput(client.options));
         }

         client.interactionManager = previousClientState.getInteractionManager();
         client.setCameraEntity((Entity)(previousClientState.getCameraEntity() != null ? previousClientState.getCameraEntity() : clientPlayerEntity));
         client.currentScreen = previousClientState.getCurrentScreen();
         client.options.setServerViewDistance(this.previousViewDistance);
         this.clientStateBeforeSwitch = null;
         this.playerInputBeforeSwitch = null;
         if (this.keyBindingStateBeforeSwitch != null) {
            this.keyBindingStateBeforeSwitch.restoreTo(client);
            this.keyBindingStateBeforeSwitch = null;
         }

         this.previousViewDistance = 0;
         this.switchInProgress = false;
      }
   }
}

