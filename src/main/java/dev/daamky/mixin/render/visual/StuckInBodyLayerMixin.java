package dev.daamky.mixin.render.visual;

import dev.daamky.NoRender;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StuckObjectsFeatureRenderer.class)
public abstract class StuckInBodyLayerMixin {
   @Inject(
      method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void daamky$noStuck(
      MatrixStack matrices, OrderedRenderCommandQueue commandQueue, int light, PlayerEntityRenderState renderState, float tickDelta, float animationProgress, CallbackInfo callbackInfo
   ) {
      if (NoRender.II1Il11l111II11IIl("Стрелы в игроке")) {
         callbackInfo.cancel();
      }
   }
}

