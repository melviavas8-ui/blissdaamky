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
   @Inject(method = "renderPreparedDraws", at = @At("HEAD"))
   private void modid$processBlur(CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.isFinishedLoading() && client.getOverlay() == null) {
         InSampler.processPendingBlurPasses();
      } else {
         InSampler.clearPendingBlurPasses();
      }
   }

   @Inject(method = "close", at = @At("HEAD"))
   private void modid$closeBlur(CallbackInfo callbackInfo) {
      InSampler.close();
   }
}

