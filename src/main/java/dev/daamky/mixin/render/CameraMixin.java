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
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   private boolean thirdPerson;
   @Unique
   private static boolean daamky$smartCullSuppressed;
// https://t.me/pasterplace

   @Shadow
   protected abstract void setRotation(float yaw, float pitch);
// https://t.me/pasterplace

   @Shadow
   protected abstract void setPos(Vec3d position);
// https://t.me/pasterplace

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
// https://t.me/pasterplace

         this.setRotation(freeCamera.getYaw(), freeCamera.getPitch());
         this.setPos(freeCamera.getCameraPosition(tickDelta));
         this.thirdPerson = true;
      }
   }
}

