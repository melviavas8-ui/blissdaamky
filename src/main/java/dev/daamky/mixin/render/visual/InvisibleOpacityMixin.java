package dev.daamky.mixin.render.visual;

import dev.daamky.InvisibleOpacity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class InvisibleOpacityMixin {
    private static final String pc1ccf675 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
   private void daamky$showInvisible(LivingEntity entity, LivingEntityRenderState renderState, float tickDelta, CallbackInfo callbackInfo) {
      if (renderState.invisible && entity instanceof PlayerEntity && InvisibleOpacity.l1I1IIl111Il1l1l()) {
         renderState.invisibleToPlayer = false;
      }
   }
// https://t.me/pasterplace

   @ModifyConstant(
      method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
      constant = @Constant(intValue = 654311423)
   )
   private int daamky$invisibleAlpha(int alpha) {
      return InvisibleOpacity.l1I1IlllIlI(alpha);
   }
}

