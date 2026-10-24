package dev.daamky.mixin.render;

import dev.daamky.BotPictureInPictureRenderer;
import dev.daamky.WorldToScreenProjection;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class WorldProjectionMixin {
   @Inject(method = "renderWorld", at = @At("HEAD"))
   private void daamky$beginProjectionFrame(CallbackInfo callbackInfo) {
      if (!BotPictureInPictureRenderer.renderingBotWorld) {
         WorldToScreenProjection.markProjectionMatrixUpdated();
      }
   }

   @ModifyArg(
      method = "renderWorld",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/RawProjectionMatrix;set(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
      ),
      index = 0
   )
   private Matrix4f daamky$captureProjection(Matrix4f projectionMatrix) {
      if (!BotPictureInPictureRenderer.renderingBotWorld) {
         WorldToScreenProjection.setProjectionMatrix(projectionMatrix);
      }

      return projectionMatrix;
   }
}

