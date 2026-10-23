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
    private static final String pf5216dd8 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "renderWorld", at = @At("HEAD"))
   private void daamky$beginProjectionFrame(CallbackInfo callbackInfo) {
      if (!BotPictureInPictureRenderer.renderingBotWorld) {
         WorldToScreenProjection.markProjectionMatrixUpdated();
      }
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

      return projectionMatrix;
   }
}

