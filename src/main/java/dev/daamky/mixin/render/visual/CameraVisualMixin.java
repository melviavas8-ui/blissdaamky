package dev.daamky.mixin.render.visual;

import dev.daamky.NoRender;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraVisualMixin {
   @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
   private void daamky$cameraClip(float originalClipDistance, CallbackInfoReturnable<Float> callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Камера клип")) {
         callbackInfo.setReturnValue(originalClipDistance);
      }
   }
}

