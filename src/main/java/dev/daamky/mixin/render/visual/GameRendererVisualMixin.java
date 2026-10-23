package dev.daamky.mixin.render.visual;

import dev.daamky.AspectRatio;
import dev.daamky.HandsCapture;
import dev.daamky.NoRender;
import dev.daamky.Tracers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GameRenderer.class)
public abstract class GameRendererVisualMixin {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   private RenderDispatcher entityRenderDispatcher;
   @Shadow
   @Final
   private BufferBuilderStorage buffers;
// https://t.me/pasterplace

   @Shadow
   private void bobView(MatrixStack matrices, float tickDelta) {
   }
// https://t.me/pasterplace

   @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
   private void daamky$noHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Тряска камеры")) {
         callbackInfo.cancel();
      }
   }
// https://t.me/pasterplace

   @Redirect(
      method = "renderWorld",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V")
   )
   private void daamky$worldBob(GameRenderer renderer, MatrixStack matrices, float tickDelta) {
      if (!Tracers.isCameraShakeSuppressed()) {
         this.bobView(matrices, tickDelta);
      }
   }
// https://t.me/pasterplace

   @ModifyArg(
      method = "getBasicProjectionMatrix",
      at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFF)Lorg/joml/Matrix4f;", remap = false),
      index = 1
   )
   private float daamky$aspectRatio(float aspectRatio) {
      float framebufferWidth = this.client.getWindow().getFramebufferWidth();
      float framebufferHeight = this.client.getWindow().getFramebufferHeight();
      return framebufferHeight <= 0.0F ? aspectRatio : AspectRatio.II1Il11l111II11IIl(framebufferWidth, framebufferHeight) / framebufferHeight;
   }
// https://t.me/pasterplace

   @ModifyArgs(
      method = "renderWorld",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/ProjectionMatrix3;set(IIF)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;")
   )
   private void daamky$handAspectRatio(Args arguments) {
      int framebufferWidth = (Integer)arguments.get(0);
      int framebufferHeight = (Integer)arguments.get(1);
      arguments.set(0, Math.max(1, Math.round(AspectRatio.l1I1IlllIlI(framebufferWidth, framebufferHeight))));
   }
// https://t.me/pasterplace

   @Inject(
      method = "renderHand",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw()V", shift = Shift.AFTER)
   )
   private void daamky$handsCaptureBegin(float tickDelta, boolean renderHands, Matrix4f modelViewMatrix, CallbackInfo callbackInfo) {
      HandsCapture.prepareFramebuffer();
      if (HandsCapture.isCaptureAvailable()) {
         HandsCapture.setOutputOverride(true);
      }
   }
// https://t.me/pasterplace

   @Inject(method = "renderHand", at = @At("RETURN"))
   private void daamky$handsCaptureEnd(float tickDelta, boolean renderHands, Matrix4f modelViewMatrix, CallbackInfo callbackInfo) {
      if (HandsCapture.isCaptureAvailable()) {
         this.entityRenderDispatcher.render();
         this.buffers.getEntityVertexConsumers().draw();
         HandsCapture.setOutputOverride(false);
      }
// https://t.me/pasterplace

      HandsCapture.renderCapturedTrails();
   }
}

