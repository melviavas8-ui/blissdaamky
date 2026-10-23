package dev.daamky.mixin.render.visual;

import dev.daamky.SwingAnimation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {
    private static final String p20017a09 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "getHandSwingDuration", at = @At("RETURN"), cancellable = true)
   private void daamky$swingSpeed(CallbackInfoReturnable<Integer> callbackInfo) {
      if ((LivingEntity)(Object)this == MinecraftClient.getInstance().player) {
         int swingDuration = SwingAnimation.adjustSwingDuration(callbackInfo.getReturnValueI());
         if (swingDuration != callbackInfo.getReturnValueI()) {
            callbackInfo.setReturnValue(swingDuration);
         }
      }
   }
}

