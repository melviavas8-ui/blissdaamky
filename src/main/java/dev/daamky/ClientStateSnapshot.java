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
   private final ClientWorld world;
   private final ClientPlayerEntity player;
   private final ClientPlayerInteractionManager interactionManager;
   private final Entity cameraEntity;
   private final Screen currentScreen;
   private final HitResult crosshairTarget;
   private final Entity targetedEntity;

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

   public static ClientStateSnapshot capture(MinecraftClient client) {
      return new ClientStateSnapshot(
         client.world, client.player, client.interactionManager, client.getCameraEntity(), client.currentScreen, client.crosshairTarget, client.targetedEntity
      );
   }

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

      client.player = this.player;
      client.interactionManager = this.interactionManager;
      ((MinecraftAccessor)client).daamky$setCameraEntity((Entity)(this.cameraEntity != null ? this.cameraEntity : this.player));
      client.currentScreen = this.currentScreen;
      client.crosshairTarget = this.crosshairTarget;
      client.targetedEntity = this.targetedEntity;
   }

   public ClientWorld getWorld() {
      return this.world;
   }

   public ClientPlayerEntity getPlayer() {
      return this.player;
   }

   public ClientPlayerInteractionManager getInteractionManager() {
      return this.interactionManager;
   }

   public Entity getCameraEntity() {
      return this.cameraEntity;
   }

   public Screen getCurrentScreen() {
      return this.currentScreen;
   }
}

