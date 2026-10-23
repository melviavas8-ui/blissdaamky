package dev.daamky.mixin.render;

import dev.daamky.InSampler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    private static final String pa455f1b5 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "renderPreparedDraws", at = @At("HEAD"))
   private void modid$processBlur(CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.isFinishedLoading() && client.getOverlay() == null) {
         InSampler.processPendingBlurPasses();
      } else {
         InSampler.clearPendingBlurPasses();
      }
   }
// https://t.me/pasterplace

   @Inject(method = "close", at = @At("HEAD"))
   private void modid$closeBlur(CallbackInfo callbackInfo) {
      InSampler.close();
   }
}

