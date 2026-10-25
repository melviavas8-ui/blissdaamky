package dev.daamky.mixin.client;

import dev.daamky.FreezeWatchdog;
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
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   GuiRenderState guiState;

   @Inject(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/hud/InGameHud;render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"
      )
   )
   private void daamky$hudUnderVanilla(RenderTickCounter renderTickCounter, boolean renderOverlay, CallbackInfo callbackInfo) {
      FreezeWatchdog.beat();
      GuiEventRenderer.dispatchHudLayerEvent(this.client, this.guiState);
   }
}

