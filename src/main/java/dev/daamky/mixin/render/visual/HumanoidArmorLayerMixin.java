package dev.daamky.mixin.render.visual;

import dev.daamky.NoRender;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public abstract class HumanoidArmorLayerMixin {
    private static final String p7ee210c6 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(
      method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void daamky$noArmor(
      MatrixStack matrices, OrderedRenderCommandQueue commandQueue, int light, BipedEntityRenderState renderState, float tickDelta, float animationProgress, CallbackInfo callbackInfo
   ) {
      if (NoRender.II1Il11l111II11IIl("Броня")) {
         callbackInfo.cancel();
      }
   }
}

