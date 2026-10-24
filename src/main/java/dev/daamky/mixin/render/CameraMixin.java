package dev.daamky.mixin.render;

import dev.daamky.FreeCamera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
   @Shadow
   private boolean thirdPerson;
   @Unique
   private static boolean daamky$smartCullSuppressed;

   @Shadow
   protected abstract void setRotation(float yaw, float pitch);

   @Shadow
   protected abstract void setPos(Vec3d position);

   @Inject(method = "update", at = @At("TAIL"))
   private void daamky$freeCamera(World world, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo callbackInfo) {
      FreeCamera freeCamera = FreeCamera.getEnabledInstance();
      MinecraftClient client = MinecraftClient.getInstance();
      if (freeCamera == null) {
         if (daamky$smartCullSuppressed) {
            client.chunkCullingEnabled = true;
            daamky$smartCullSuppressed = false;
         }
      } else {
         if (client.chunkCullingEnabled) {
            client.chunkCullingEnabled = false;
            daamky$smartCullSuppressed = true;
         }

         this.setRotation(freeCamera.getYaw(), freeCamera.getPitch());
         this.setPos(freeCamera.getCameraPosition(tickDelta));
         this.thirdPerson = true;
      }
   }
}

