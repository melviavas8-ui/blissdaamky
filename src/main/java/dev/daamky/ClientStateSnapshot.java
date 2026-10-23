package dev.daamky;

import dev.daamky.mixin.client.MinecraftAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;

public final class ClientStateSnapshot {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final ClientWorld world;
   private final ClientPlayerEntity player;
   private final ClientPlayerInteractionManager interactionManager;
   private final Entity cameraEntity;
   private final Screen currentScreen;
   private final HitResult crosshairTarget;
   private final Entity targetedEntity;
// https://t.me/pasterplace

   private ClientStateSnapshot(
      ClientWorld clientWorld,
      ClientPlayerEntity clientPlayerEntity,
      ClientPlayerInteractionManager clientPlayerInteractionManager,
      Entity entity,
      Screen screen,
      HitResult hitResult,
      Entity entity2
   ) {
      this.world = clientWorld;
      this.player = clientPlayerEntity;
      this.interactionManager = clientPlayerInteractionManager;
      this.cameraEntity = entity;
      this.currentScreen = screen;
      this.crosshairTarget = hitResult;
      this.targetedEntity = entity2;
   }
// https://t.me/pasterplace

   public static ClientStateSnapshot capture(MinecraftClient client) {
      return new ClientStateSnapshot(
         client.world, client.player, client.interactionManager, client.getCameraEntity(), client.currentScreen, client.crosshairTarget, client.targetedEntity
      );
   }
// https://t.me/pasterplace

   public void restore(MinecraftClient client, boolean updateWorldRenderer) {
      if (client.world != this.world) {
         client.world = this.world;
         if (this.world != null) {
            client.particleManager.setWorld(this.world);
         }

         if (updateWorldRenderer) {
            client.worldRenderer.setWorld(this.world);
            client.gameRenderer.setWorld(this.world);
         }
      }
// https://t.me/pasterplace

      client.player = this.player;
      client.interactionManager = this.interactionManager;
      ((MinecraftAccessor)client).daamky$setCameraEntity((Entity)(this.cameraEntity != null ? this.cameraEntity : this.player));
      client.currentScreen = this.currentScreen;
      client.crosshairTarget = this.crosshairTarget;
      client.targetedEntity = this.targetedEntity;
   }
// https://t.me/pasterplace

   public ClientWorld getWorld() {
      return this.world;
   }
// https://t.me/pasterplace

   public ClientPlayerEntity getPlayer() {
      return this.player;
   }
// https://t.me/pasterplace

   public ClientPlayerInteractionManager getInteractionManager() {
      return this.interactionManager;
   }
// https://t.me/pasterplace

   public Entity getCameraEntity() {
      return this.cameraEntity;
   }
// https://t.me/pasterplace

   public Screen getCurrentScreen() {
      return this.currentScreen;
   }
}

