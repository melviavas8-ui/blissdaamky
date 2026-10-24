package dev.daamky;

import dev.daamky.core.event.EventBus;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.mixin.entity.ItemFrameAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPosition;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState.UpdateData;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.AcknowledgeReconfigurationC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientTickEndC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerLoadedC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.EnterReconfigurationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRotationS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class Joined extends BotPacketListenerInvocationHandler {
   private final ClientConnectionState clientConnectionState;
   private final BotSessionCallback sessionCallback;
   private final BotClientPlayHandler playHandler;
   private final BotInputController inputController;
   private final ClientPlayPacketListener playPacketListener;
   private boolean playerLoadedPacketSent;
   private long gameJoinReceivedAtMillis;

   Joined(MinecraftClient client, ClientConnection clientConnection, ClientConnectionState clientConnectionState, BotSession disconnect, BotSessionProfile l1lll11l1l) {
      super(client, clientConnection, disconnect, l1lll11l1l, clientConnectionState.serverCookies());
      this.clientConnectionState = clientConnectionState;
      this.serverBrand = clientConnectionState.serverBrand();
      this.customReportDetails = clientConnectionState.customReportDetails();
      this.serverLinks = clientConnectionState.serverLinks();
      this.sessionCallback = l1lll11l1l.callbackFor(disconnect);
      this.playHandler = new BotClientPlayHandler(client, clientConnection, clientConnectionState, disconnect, ModuleToggleProfileManager.getContext());
      this.inputController = new BotInputController(client, disconnect.getKeyBindingStateSnapshot());
      this.playPacketListener = this.createProxy(ClientPlayPacketListener.class, TickablePacketListener.class);
   }

   ClientPlayPacketListener getPlayPacketListener() {
      return this.playPacketListener;
   }

   @Override
   protected boolean requiresClientThreadDispatch() {
      return true;
   }

   @Override
   public void tickConnection() {
      this.playHandler.tick();
      this.sendPlayerLoadedPacketWhenReady();
      if (this.playerLoadedPacketSent) {
         this.playHandler.markClientLoaded();
      }

      if (!SavedBots.isSwitchingToSession(this.disconnect) && this.disconnect.isOnline()) {
         this.playHandler.initializePlaySession();
         if (this.playHandler.hasWorldAndPlayer()) {
            this.inputController.setMovementKeysPressed(SavedBots.isManuallyDisconnected(this.disconnect));
            this.inputController
               .runWithKeyBindingsRestored(
                  () -> this.playHandler
                     .tickClientWorld(
                        this.inputController.keyboardInput(),
                        () -> BotPictureInPictureRenderer.INSTANCE.hasPictureInPicture(this.disconnect),
                        this.inputController::updateCrosshairAndAttack
                     )
               );
         }

         this.sendPacketIfOpen(ClientTickEndC2SPacket.INSTANCE);
      }

      if (this.disconnect.isOnline() && this.playHandler.hasWorldAndPlayer()) {
         this.playHandler
            .runWithPlaySession(() -> this.disconnect.handleLobbySelection(this.playHandler.getClientPlayer(), this.playHandler.getInteractionManager()));
      }
   }

   @Override
   protected boolean handleLoginOrConfigurationPacket(Packet<?> packet) {
      if (packet instanceof GameJoinS2CPacket gameJoinPacket) {
         this.handleGameJoinPacket(gameJoinPacket);
      } else if (packet instanceof PlayerPositionLookS2CPacket playerPositionLookPacket) {
         this.handlePlayerPositionLookPacket(playerPositionLookPacket);
      } else if (packet instanceof PlayerRotationS2CPacket playerRotationPacket) {
         this.handlePlayerRotationPacket(playerRotationPacket);
      } else if (packet instanceof GameMessageS2CPacket gameMessagePacket) {
         this.handleGameMessagePacket(gameMessagePacket);
      } else if (packet instanceof ProfilelessChatMessageS2CPacket profilelessChatMessagePacket) {
         this.handleProfilelessChatMessagePacket(profilelessChatMessagePacket);
      } else if (packet instanceof ChatMessageS2CPacket chatMessagePacket) {
         this.handleChatMessagePacket(chatMessagePacket);
      } else if (packet instanceof EntitySpawnS2CPacket entitySpawnPacket) {
         this.handleEntitySpawnPacket(entitySpawnPacket);
      } else if (packet instanceof MapUpdateS2CPacket mapUpdatePacket) {
         this.handleMapUpdatePacket(mapUpdatePacket);
      } else if (packet instanceof EntityTrackerUpdateS2CPacket entityTrackerUpdatePacket) {
         this.handleEntityTrackerUpdatePacket(entityTrackerUpdatePacket);
      } else if (packet instanceof BundleS2CPacket bundlePacket) {
         this.applyBundlePacket(bundlePacket);
      } else if (packet instanceof EnterReconfigurationS2CPacket) {
         this.handleReconfigurationPacket();
      } else if (packet instanceof ChunkLoadDistanceS2CPacket chunkLoadDistancePacket) {
         this.playHandler.setViewDistance(chunkLoadDistancePacket.getDistance());
         this.dispatchPacketOrQueue(packet);
      } else if (packet instanceof OverlayMessageS2CPacket overlayMessagePacket) {
         this.sessionCallback.onServerMessage(overlayMessagePacket.text().getString());
         this.dispatchPacketOrQueue(packet);
      } else if (packet instanceof SubtitleS2CPacket subtitlePacket) {
         this.sessionCallback.onServerMessage(subtitlePacket.text().getString());
         this.dispatchPacketOrQueue(packet);
      } else if (packet instanceof TitleS2CPacket titlePacket) {
         this.sessionCallback.onServerMessage(titlePacket.text().getString());
         this.dispatchPacketOrQueue(packet);
      } else if (packet instanceof PlayerRespawnS2CPacket) {
         this.dispatchPacketOrQueue(packet, true);
         this.disconnect.updateLobbyNavigationState();
      } else if (packet instanceof ChunkDataS2CPacket chunkDataPacket) {
         this.handleChunkDataPacket(chunkDataPacket);
      } else if (packet instanceof OpenScreenS2CPacket openScreenPacket) {
         this.playHandler.setScreenTitle(openScreenPacket.getName().getString());
         this.dispatchPacketOrQueue(packet);
      } else {
         this.dispatchPacketOrQueue(packet);
      }

      return true;
   }

   private void handleGameJoinPacket(GameJoinS2CPacket gameJoinS2CPacket) {
      if (SavedBots.getActiveSession() == this.disconnect) {
         SavedBots.queueSessionDisconnect(this.disconnect);
      }

      this.playHandler.reset();
      ModuleToggleProfileManager.clearProfileCallbackState(this.disconnect.getUsername());
      this.playHandler.setViewDistance(gameJoinS2CPacket.playerEntityId());
      this.playHandler.setGameJoinPacket(gameJoinS2CPacket);
      this.disconnect.setConnectionStatus(BotSession.BotConnectionStatus.ONLINE);
      this.disconnect.appendSessionHistory("joined");
      this.sessionCallback.onSessionReset();
      this.playerLoadedPacketSent = false;
      this.gameJoinReceivedAtMillis = System.currentTimeMillis();
      this.playHandler.initializePlaySession();
      this.disconnect.onGameJoinOrRespawn();
   }

   private void sendPlayerLoadedPacketWhenReady() {
      if (!this.playerLoadedPacketSent && this.disconnect.isOnline() && this.playHandler.hasEntityPosition()) {
         if (System.currentTimeMillis() - this.gameJoinReceivedAtMillis >= 500L) {
            if (!this.playHandler.isPlayNetworkHandlerLoaded()) {
               this.sendPacketIfOpen(new PlayerLoadedC2SPacket());
            }

            this.playerLoadedPacketSent = true;
         }
      }
   }

   private void handlePlayerPositionLookPacket(PlayerPositionLookS2CPacket playerPositionLookS2CPacket) {
      Vec3d serverPosition = playerPositionLookS2CPacket.change().position();
      this.sessionCallback.onPlayerLookUpdate(serverPosition.x, serverPosition.y, serverPosition.z, playerPositionLookS2CPacket.change().yaw());
      if (!this.playHandler.hasPlayNetworkHandler()) {
         EntityPosition entityPosition = this.playHandler.applyPlayerPosition(playerPositionLookS2CPacket);
         Vec3d resolvedPosition = entityPosition.position();
         this.sendPacketIfOpen(new TeleportConfirmC2SPacket(playerPositionLookS2CPacket.teleportId()));
         this.sendPacketIfOpen(new Full(resolvedPosition.x, resolvedPosition.y, resolvedPosition.z, entityPosition.yaw(), entityPosition.pitch(), false, false));
      } else {
         this.dispatchPacketOrQueue(playerPositionLookS2CPacket);
      }
   }

   private void handlePlayerRotationPacket(PlayerRotationS2CPacket playerRotationS2CPacket) {
      if (!this.playHandler.hasPlayNetworkHandler()) {
         this.playHandler.applyPlayerRotation(playerRotationS2CPacket);
      } else {
         this.dispatchPacketOrQueue(playerRotationS2CPacket);
      }
   }

   private void handleGameMessagePacket(GameMessageS2CPacket gameMessageS2CPacket) {
      if (gameMessageS2CPacket.overlay()) {
         this.sessionCallback.onServerMessage(gameMessageS2CPacket.content().getString());
         this.dispatchPacketOrQueue(gameMessageS2CPacket);
      } else {
         this.recordChatMessage(gameMessageS2CPacket.content());
         if (SavedBots.getActiveSession() == this.disconnect) {
            this.dispatchPacketOrQueue(gameMessageS2CPacket);
         } else {
            this.publishReceivedPacketEvent(gameMessageS2CPacket);
         }
      }
   }

   private void handleProfilelessChatMessagePacket(ProfilelessChatMessageS2CPacket profilelessChatMessageS2CPacket) {
      this.recordChatMessage(profilelessChatMessageS2CPacket.message());
      if (SavedBots.getActiveSession() == this.disconnect) {
         this.dispatchPacketOrQueue(profilelessChatMessageS2CPacket);
      } else {
         this.publishReceivedPacketEvent(profilelessChatMessageS2CPacket);
      }
   }

   private void handleChatMessagePacket(ChatMessageS2CPacket chatMessageS2CPacket) {
      Text chatMessage = chatMessageS2CPacket.unsignedContent() != null
         ? chatMessageS2CPacket.unsignedContent()
         : Text.literal(chatMessageS2CPacket.body().content());
      this.disconnect.appendSessionHistory(chatMessage.getString());
      this.disconnect.appendChatMessage((Text)chatMessage);
      this.disconnect.handleServerMessage(chatMessage.getString());
      if (SavedBots.getActiveSession() == this.disconnect) {
         this.dispatchPacketOrQueue(chatMessageS2CPacket);
      }
   }

   private void recordChatMessage(Text chatText) {
      String messageText = chatText.getString();
      this.disconnect.appendSessionHistory(messageText);
      this.disconnect.appendChatMessage(chatText);
      this.disconnect.handleServerMessage(messageText);
      this.sessionCallback.onServerMessage(messageText);
   }

   private void publishReceivedPacketEvent(Packet<?> packet) {
      EventBus eventBus = ClientInitializer.getEventBus();
      if (eventBus != null) {
         this.runWithModuleProfile(() -> eventBus.post(new EventPacket(packet, EventPacket.Direction.RECEIVE)));
      }
   }

   private void handleEntitySpawnPacket(EntitySpawnS2CPacket entitySpawnS2CPacket) {
      if (entitySpawnS2CPacket.getEntityType() == EntityType.ITEM_FRAME || entitySpawnS2CPacket.getEntityType() == EntityType.GLOW_ITEM_FRAME) {
         this.sessionCallback
            .onMapDecoration(
               entitySpawnS2CPacket.getEntityId(),
               entitySpawnS2CPacket.getX(),
               entitySpawnS2CPacket.getY(),
               entitySpawnS2CPacket.getZ(),
               entitySpawnS2CPacket.getEntityData()
            );
      }

      this.dispatchPacketOrQueue(entitySpawnS2CPacket);
   }

   private void handleMapUpdatePacket(MapUpdateS2CPacket mapUpdateS2CPacket) {
      MapIdComponent mapIdComponent = mapUpdateS2CPacket.mapId();
      this.sessionCallback.onMapUpdate(mapIdComponent.id(), (UpdateData)mapUpdateS2CPacket.updateData().orElse(null));
      this.dispatchPacketOrQueue(mapUpdateS2CPacket);
   }

   private void handleEntityTrackerUpdatePacket(EntityTrackerUpdateS2CPacket entityTrackerUpdateS2CPacket) {
      for (DataTracker.SerializedEntry<?> trackedEntry : entityTrackerUpdateS2CPacket.trackedValues()) {
         Object trackedValue = trackedEntry.value();
         if (trackedValue instanceof ItemStack) {
            ItemStack mapItemStack = (ItemStack)trackedValue;
            trackedValue = mapItemStack.get(DataComponentTypes.MAP_ID);
            if (trackedValue != null) {
               this.sessionCallback.onMapColorUpdate(entityTrackerUpdateS2CPacket.id(), ((MapIdComponent)trackedValue).id());
            }
         } else if (trackedEntry.id() == ItemFrameAccessor.daamky$getRotationData().id()) {
            trackedValue = trackedEntry.value();
            if (trackedValue instanceof Integer) {
               int rotation = (Integer)trackedValue;
               this.sessionCallback.onMapCursorUpdate(entityTrackerUpdateS2CPacket.id(), rotation);
            }
         }
      }

      this.dispatchPacketOrQueue(entityTrackerUpdateS2CPacket);
   }

   private void applyBundlePacket(BundleS2CPacket bundleS2CPacket) {
      for (Packet<? super ClientPlayPacketListener> packet : bundleS2CPacket.getPackets()) {
         packet.apply(this.playPacketListener);
      }
   }

   private void handleChunkDataPacket(ChunkDataS2CPacket chunkDataS2CPacket) {
      this.dispatchPacketOrQueue(chunkDataS2CPacket);
      ClientWorld clientWorld = this.playHandler.getClientWorld();
      BotCaptchaLog.II1Il11l111II11IIl(
         "["
            + this.disconnect.getUsername()
            + "] chunk "
            + chunkDataS2CPacket.getChunkX()
            + ","
            + chunkDataS2CPacket.getChunkZ()
            + " applied, loaded="
            + (clientWorld == null ? "no-world" : clientWorld.getChunkManager().getLoadedChunkCount())
      );
   }

   private void handleReconfigurationPacket() {
      if (SavedBots.getActiveSession() == this.disconnect) {
         SavedBots.queueSessionDisconnect(this.disconnect);
      }

      this.playHandler.reset();
      this.disconnect.setConnectionStatus(BotSession.BotConnectionStatus.LOADING);
      this.connection
         .transitionInbound(
            ConfigurationStates.S2C,
            BotConfigurationPacketHandler.createListener(
               this.client, this.connection, this.clientConnectionState.localGameProfile(), this.serverCookies, this.disconnect, this.sessionProfile
            )
         );
      this.connection.send(AcknowledgeReconfigurationC2SPacket.INSTANCE);
      this.connection.transitionOutbound(ConfigurationStates.C2S);
   }

   boolean enterClientWorld() {
      return this.playHandler.enterClientView();
   }

   void resetClientWorldState() {
      this.playHandler.leaveClientView();
   }

   ClientWorld getClientWorld() {
      return this.playHandler.getClientWorld();
   }

   ClientPlayerEntity getClientPlayer() {
      return this.playHandler.getClientPlayer();
   }

   Entity getCameraEntity() {
      return this.playHandler.getCameraEntity();
   }

   ClientPlayerInteractionManager getInteractionManager() {
      return this.playHandler.getInteractionManager();
   }

   Screen getCurrentScreen() {
      return this.playHandler.getClientScreen();
   }

   String getCurrentScreenTitle() {
      Screen screen = this.playHandler.getClientScreen();
      if (screen instanceof HandledScreen) {
         Screen screen2 = (HandledScreen)screen;
         return screen2.getTitle().getString();
      } else {
         return this.playHandler.getScreenTitle();
      }
   }

   boolean runWithScreenRestored(Runnable runnable) {
      return this.playHandler.runWithScreenRestored(runnable);
   }

   boolean isCameraEntity(Object cameraEntity) {
      return this.playHandler.isPlayNetworkHandler(cameraEntity);
   }

   private void dispatchPacketOrQueue(Packet<?> packet) {
      this.dispatchPacketOrQueue(packet, false);
   }

   private void dispatchPacketOrQueue(Packet<?> packet, boolean updateClientRenderState) {
      Packet packetToDispatch = packet;
      if (this.playHandler.hasPlayNetworkHandler() && this.playHandler.hasWorldAndPlayer()) {
         this.playHandler.applyPacket(packetToDispatch, updateClientRenderState);
      } else {
         this.playHandler.queuePacket(packetToDispatch);
      }
   }

   @Override
   protected void runWithModuleProfile(Runnable runnable) {
      if (!this.playHandler.runWithProfile(this.disconnect.getUsername(), runnable)) {
         super.runWithModuleProfile(runnable);
      }
   }
}

