package dev.daamky;

import dev.daamky.core.event.EventBus;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.mixin.client.BotClientPacketListenerAccessor;
import dev.daamky.mixin.client.MinecraftAccessor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPosition;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRotationS2CPacket;
import net.minecraft.util.math.Vec3d;

public final class BotClientPlayHandler {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final int MAX_PENDING_PACKETS = 4096;
   private static final EntityPosition DEFAULT_ENTITY_POSITION = new EntityPosition(Vec3d.ZERO, Vec3d.ZERO, 0.0F, 0.0F);
   private static int CLIENT_STATE_ISOLATION_DEPTH;
   private static int CLIENT_VIEW_ISOLATION_DEPTH;
   private final MinecraftClient client;
   private final ClientConnection connection;
   private final ClientConnectionState connectionState;
   private final BotSession botSession;
   private final ModuleToggleProfileContext profileContext;
   private final Deque<Packet<? super ClientPlayPacketListener>> pendingPackets = new ArrayDeque<>();
   private GameJoinS2CPacket pendingGameJoinPacket;
   private ClientWorld world;
   private ClientPlayerEntity player;
   private ClientPlayerInteractionManager interactionManager;
   private ClientPlayNetworkHandler playNetworkHandler;
   private Entity cameraEntity;
   private Screen savedScreen;
   private String screenTitle = "";
   private EntityPosition entityPosition = DEFAULT_ENTITY_POSITION;
   private int viewDistance = 3;
   private boolean hasEntityPosition;
   private boolean clientViewActive;
   private long nextJoinRetryAtMillis;
// https://t.me/pasterplace

   BotClientPlayHandler(
      MinecraftClient client,
      ClientConnection clientConnection,
      ClientConnectionState clientConnectionState,
      BotSession disconnect,
      ModuleToggleProfileContext i1lIIII1III1lIlIl1l3
   ) {
      this.client = client;
      this.connection = clientConnection;
      this.connectionState = clientConnectionState;
      this.botSession = disconnect;
      this.profileContext = i1lIIII1III1lIlIl1l3;
   }
// https://t.me/pasterplace

   public static boolean isClientStateIsolationActive() {
      return CLIENT_STATE_ISOLATION_DEPTH > 0;
   }
// https://t.me/pasterplace

   public static boolean isClientViewStateIsolationActive() {
      return CLIENT_VIEW_ISOLATION_DEPTH > 0;
   }
// https://t.me/pasterplace

   boolean hasPlayNetworkHandler() {
      return this.playNetworkHandler != null;
   }
// https://t.me/pasterplace

   boolean hasWorldAndPlayer() {
      return this.player != null && this.world != null;
   }
// https://t.me/pasterplace

   boolean hasEntityPosition() {
      return this.hasEntityPosition;
   }
// https://t.me/pasterplace

   boolean isPlayNetworkHandlerLoaded() {
      return this.playNetworkHandler != null && this.playNetworkHandler.isLoaded();
   }
// https://t.me/pasterplace

   void setGameJoinPacket(GameJoinS2CPacket gameJoinS2CPacket) {
      this.pendingGameJoinPacket = gameJoinS2CPacket;
   }
// https://t.me/pasterplace

   void setViewDistance(int viewDistance) {
      this.viewDistance = viewDistance;
   }
// https://t.me/pasterplace

   void setScreenTitle(String screenTitle) {
      this.screenTitle = screenTitle == null ? "" : screenTitle;
   }
// https://t.me/pasterplace

   String getScreenTitle() {
      return this.screenTitle;
   }
// https://t.me/pasterplace

   void queuePacket(Packet<? super ClientPlayPacketListener> packet) {
      if (this.pendingPackets.size() >= 4096) {
         this.pendingPackets.pollFirst();
      }

      this.pendingPackets.add(packet);
   }
// https://t.me/pasterplace

   void applyPacket(Packet<? super ClientPlayPacketListener> packet, boolean updateRenderState) {
      this.runWithClientState(() -> {
         if (this.player != null && this.world != null) {
            this.profileContext.runWithProfile(this.botSession.getUsername(), () -> packet.apply(this.playNetworkHandler));
         } else {
            packet.apply(this.playNetworkHandler);
         }
      }, updateRenderState);
   }
// https://t.me/pasterplace

   boolean initializePlaySession() {
      if (this.playNetworkHandler != null) {
         return true;
      }

      if (this.pendingGameJoinPacket == null) {
         return false;
      }

      if (System.currentTimeMillis() < this.nextJoinRetryAtMillis) {
         return false;
      }
// https://t.me/pasterplace

      EntityPosition entityPosition = this.entityPosition;
      boolean hadSavedPosition = this.hasEntityPosition;
      this.playNetworkHandler = new ClientPlayNetworkHandler(this.client, this.connection, this.connectionState);

      try {
         this.applyPacket(this.pendingGameJoinPacket, true);
      } catch (Exception joinFailure) {
         this.playNetworkHandler = null;
         this.nextJoinRetryAtMillis = System.currentTimeMillis() + 2000L;
         BotCaptchaLog.II1Il11l111II11IIl("[" + this.botSession.getUsername() + "] visual join failed: " + joinFailure);
         return false;
      }

      if (this.world != null && this.player != null) {
         this.entityPosition = entityPosition;
         this.hasEntityPosition = hadSavedPosition;
         this.syncPlayerPosition();

         for (Object queuedPacket : List.copyOf(this.pendingPackets)) {
            try {
               this.applyPacket((Packet<? super ClientPlayPacketListener>)queuedPacket, false);
            } catch (Exception replayFailure) {
               BotCaptchaLog.II1Il11l111II11IIl(
                  "[" + this.botSession.getUsername() + "] visual replay failed: " + queuedPacket.getClass().getSimpleName() + ": " + replayFailure
               );
            }
         }

         this.pendingPackets.clear();
         return this.world != null && this.player != null && this.interactionManager != null;
      } else {
         this.playNetworkHandler = null;
         this.nextJoinRetryAtMillis = System.currentTimeMillis() + 2000L;
         BotCaptchaLog.II1Il11l111II11IIl(
            "["
               + this.botSession.getUsername()
               + "] visual join incomplete: world="
               + (this.world != null)
               + " player="
               + (this.player != null)
         );
         return false;
      }
   }
// https://t.me/pasterplace

   void markClientLoaded() {
      if (this.playNetworkHandler != null && !this.playNetworkHandler.isLoaded()) {
         BotClientPacketListenerAccessor listenerAccessor = (BotClientPacketListenerAccessor)this.playNetworkHandler;
         listenerAccessor.daamky$setClientLoaded(true);
         listenerAccessor.daamky$setLevelLoadTracker(null);
      }
   }
// https://t.me/pasterplace

   void tick() {
      if (this.playNetworkHandler != null && this.world != null) {
         this.runInIsolatedClientState(this.playNetworkHandler::tick);
      }
   }
// https://t.me/pasterplace

   void tickClientWorld(Input input, BooleanSupplier booleanSupplier, Runnable runnable) {
      this.runInIsolatedClientState(() -> this.profileContext.runWithProfile(this.botSession.getUsername(), () -> {
         this.player.input = input;
         EventBus eventBus = ClientInitializer.getEventBus();
         if (eventBus != null) {
            eventBus.post(EventTick.get());
         }

         if (booleanSupplier.getAsBoolean()) {
            this.world.tickEntities();
         } else {
            this.world.tickEntity(this.world::tickEntity, this.player);
         }

         runnable.run();
      }));
   }
// https://t.me/pasterplace

   void runWithPlaySession(Runnable runnable) {
      if (this.player != null && this.world != null) {
         if (this.isClientStateCurrent()) {
            runnable.run();
         } else {
            this.runInIsolatedClientState(runnable);
         }
      }
   }
// https://t.me/pasterplace

   boolean runWithProfile(String username, Runnable runnable) {
      if (this.player != null && this.world != null) {
         if (this.isClientStateCurrent()) {
            this.profileContext.runWithProfile(username, runnable);
         } else {
            this.runInIsolatedClientState(() -> this.profileContext.runWithProfile(username, runnable));
         }
// https://t.me/pasterplace

         return true;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   boolean enterClientView() {
      if (!this.initializePlaySession()) {
         return false;
      } else if (this.world != null && this.player != null && this.interactionManager != null) {
         this.client.world = this.world;
         this.client.particleManager.setWorld(this.world);
         this.client.worldRenderer.setWorld(this.world);
         this.client.gameRenderer.setWorld(this.world);
         this.client.player = this.player;
         this.client.interactionManager = this.interactionManager;
         this.client.setCameraEntity((Entity)(this.cameraEntity != null ? this.cameraEntity : this.player));
         this.player.input = new KeyboardInput(this.client.options);
         this.client.currentScreen = this.getVisibleScreen();
         this.client.options.setServerViewDistance(this.viewDistance);
         this.clientViewActive = true;
         BotCaptchaLog.II1Il11l111II11IIl(
            "["
               + this.botSession.getUsername()
               + "] enterView pos="
               + (int)this.player.getX()
               + ","
               + (int)this.player.getY()
               + ","
               + (int)this.player.getZ()
               + " chunks="
               + this.world.getChunkManager().getLoadedChunkCount()
               + " entities="
               + this.world.getRegularEntityCount()
               + " viewDist="
               + this.viewDistance
         );
         return true;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   void leaveClientView() {
      this.clientViewActive = false;
   }
// https://t.me/pasterplace

   ClientWorld getClientWorld() {
      return this.world;
   }
// https://t.me/pasterplace

   ClientPlayerEntity getClientPlayer() {
      return this.player;
   }
// https://t.me/pasterplace

   Entity getCameraEntity() {
      return (Entity)(this.cameraEntity != null ? this.cameraEntity : this.player);
   }
// https://t.me/pasterplace

   ClientPlayerInteractionManager getInteractionManager() {
      return this.interactionManager;
   }
// https://t.me/pasterplace

   Screen getClientScreen() {
      return this.getVisibleScreen();
   }
// https://t.me/pasterplace

   boolean runWithScreenRestored(Runnable runnable) {
      if (this.initializePlaySession() && this.world != null && this.player != null) {
         this.runWithClientState(() -> {
            this.client.currentScreen = this.getVisibleScreen();
            runnable.run();
            this.savedScreen = filterScreen(this.client.currentScreen);
         }, false);
         return true;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   boolean isPlayNetworkHandler(Object packetListener) {
      return packetListener != null && packetListener == this.playNetworkHandler;
   }
// https://t.me/pasterplace

   EntityPosition applyPlayerPosition(PlayerPositionLookS2CPacket playerPositionLookS2CPacket) {
      this.entityPosition = EntityPosition.apply(
         this.entityPosition, playerPositionLookS2CPacket.change(), playerPositionLookS2CPacket.relatives()
      );
      this.hasEntityPosition = true;
      return this.entityPosition;
   }
// https://t.me/pasterplace

   void applyPlayerRotation(PlayerRotationS2CPacket playerRotationS2CPacket) {
      float yaw = playerRotationS2CPacket.relativeYaw()
         ? this.entityPosition.yaw() + playerRotationS2CPacket.yaw()
         : playerRotationS2CPacket.yaw();
      float pitch = playerRotationS2CPacket.relativePitch()
         ? this.entityPosition.pitch() + playerRotationS2CPacket.pitch()
         : playerRotationS2CPacket.pitch();
      this.entityPosition = new EntityPosition(this.entityPosition.position(), this.entityPosition.deltaMovement(), yaw, pitch);
   }
// https://t.me/pasterplace

   private void runInIsolatedClientState(Runnable runnable) {
      this.runWithClientState(runnable, false);
   }
// https://t.me/pasterplace

   private void runWithClientState(Runnable runnable, boolean updateRenderState) {
      if (this.isClientStateCurrent()) {
         this.captureClientState();
      }

      ClientStateSnapshot clientStateSnapshot = ClientStateSnapshot.capture(this.client);
      boolean wasClientViewActive = this.clientViewActive;
      CLIENT_STATE_ISOLATION_DEPTH++;
      if (!wasClientViewActive) {
         CLIENT_VIEW_ISOLATION_DEPTH++;
      }

      this.setClientState(wasClientViewActive);

      try {
         runnable.run();
         this.captureClientState();
      } finally {
         CLIENT_STATE_ISOLATION_DEPTH--;
         if (!wasClientViewActive) {
            CLIENT_VIEW_ISOLATION_DEPTH--;
         }

         if (!wasClientViewActive && !this.clientViewActive) {
            clientStateSnapshot.restore(this.client, updateRenderState && this.clientViewActive);
         } else {
            this.setClientState(true);
         }
      }
   }
// https://t.me/pasterplace

   private void setClientState(boolean updateRenderState) {
      if (this.client.world != this.world) {
         this.client.world = this.world;
         if (this.world != null) {
            this.client.particleManager.setWorld(this.world);
         }

         if (updateRenderState) {
            this.client.worldRenderer.setWorld(this.world);
            this.client.gameRenderer.setWorld(this.world);
         }
      }

      this.client.player = this.player;
      this.client.interactionManager = this.interactionManager;
      ((MinecraftAccessor)this.client)
         .daamky$setCameraEntity((Entity)(this.cameraEntity != null ? this.cameraEntity : this.player));
      this.client.currentScreen = this.clientViewActive ? this.getVisibleScreen() : null;
   }
// https://t.me/pasterplace

   private void captureClientState() {
      this.world = this.client.world;
      this.player = this.client.player;
      this.interactionManager = this.client.interactionManager;
      this.cameraEntity = this.client.getCameraEntity();
      this.savedScreen = filterScreen(this.client.currentScreen);
      if (this.player != null) {
         if (!(this.player.input instanceof KeyboardInput)) {
            this.player.input = new KeyboardInput(this.client.options);
         }

         this.entityPosition = EntityPosition.fromEntity(this.player);
         this.hasEntityPosition = true;
      }
   }
// https://t.me/pasterplace

   private void syncPlayerPosition() {
      if (this.hasEntityPosition && this.player != null) {
         Vec3d playerPosition = this.entityPosition.position();
         this.player
            .refreshPositionAndAngles(playerPosition.x, playerPosition.y, playerPosition.z, this.entityPosition.yaw(), this.entityPosition.pitch());
         this.player.setVelocity(this.entityPosition.deltaMovement());
         this.player.updateLastAngles();
      }
   }
// https://t.me/pasterplace

   private Screen getVisibleScreen() {
      return filterScreen(this.savedScreen);
   }
// https://t.me/pasterplace

   private static Screen filterScreen(Screen screen) {
      return screen instanceof LevelLoadingScreen ? null : screen;
   }
// https://t.me/pasterplace

   private boolean isClientStateCurrent() {
      return this.client.world == this.world
         && this.client.player == this.player
         && this.client.interactionManager == this.interactionManager;
   }
// https://t.me/pasterplace

   void reset() {
      this.clientViewActive = false;
      this.world = null;
      this.player = null;
      this.interactionManager = null;
      this.playNetworkHandler = null;
      this.pendingGameJoinPacket = null;
      this.pendingPackets.clear();
      this.cameraEntity = null;
      this.savedScreen = null;
      this.resetTrackedPosition();
   }
// https://t.me/pasterplace

   private void resetTrackedPosition() {
      this.entityPosition = DEFAULT_ENTITY_POSITION;
      this.hasEntityPosition = false;
   }
}

