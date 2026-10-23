package dev.daamky.mixin.render.visual;

import dev.daamky.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class ScreenEffectRendererMixin {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
   private static void daamky$noFire(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Sprite sprite, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Огонь")) {
         callbackInfo.cancel();
      }
   }
// https://t.me/pasterplace

   @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
   private static void daamky$noWater(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Вода на экране")) {
         callbackInfo.cancel();
      }
   }
// https://t.me/pasterplace

   @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
   private static void daamky$noBlock(Sprite sprite, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Блоки на экране")) {
         callbackInfo.cancel();
      }
   }
// https://t.me/pasterplace

   @Inject(method = "renderFloatingItem", at = @At("HEAD"), cancellable = true)
   private void daamky$noTotem(MatrixStack matrices, float tickDelta, OrderedRenderCommandQueue renderCommandQueue, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Анимация тотема")) {
         callbackInfo.cancel();
      }
   }
}

