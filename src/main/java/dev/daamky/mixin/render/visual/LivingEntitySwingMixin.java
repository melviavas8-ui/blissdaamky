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

