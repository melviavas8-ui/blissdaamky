package dev.daamky.mixin.client;

import dev.daamky.GuiEventRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GuiScreenLayerMixin {
    private static final String p4700fde1 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   GuiRenderState guiState;
// https://t.me/pasterplace

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"
      )
   )
   private void daamky$hudUnderVanilla(RenderTickCounter renderTickCounter, boolean renderOverlay, CallbackInfo callbackInfo) {
      GuiEventRenderer.dispatchHudLayerEvent(this.client, this.guiState);
   }
}

