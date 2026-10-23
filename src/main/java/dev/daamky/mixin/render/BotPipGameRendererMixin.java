package dev.daamky.mixin.render;

import dev.daamky.BotManagementScreen;
import dev.daamky.BotPictureInPictureRenderer;
import dev.daamky.BotSessionInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class BotPipGameRendererMixin {
    private static final String p52afb0dc = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   @Final
   private MinecraftClient client;
// https://t.me/pasterplace

   @Inject(method = "renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At("RETURN"))
   private void daamky$renderBotPip(RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
      if (!BotPictureInPictureRenderer.renderingBotWorld && this.client.currentScreen instanceof BotManagementScreen botManagementScreen) {
         BotSessionInfo selectedSessionInfo = botManagementScreen.getSelectedSessionInfo();
         if (selectedSessionInfo != null && selectedSessionInfo.hasActivePlayerSession()) {
            BotPictureInPictureRenderer.INSTANCE.renderBotWorldPreview(selectedSessionInfo);
         }
      }
   }
}

