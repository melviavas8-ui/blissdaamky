package dev.daamky.mixin.client;

import dev.daamky.Timer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftExtrasMixin {
   @Inject(method = "getTargetMillisPerTick(F)F", at = @At("RETURN"), cancellable = true)
   private void daamky$timer(float tickDelta, CallbackInfoReturnable<Float> callbackInfo) {
      float timerMultiplier = Timer.l1I1IIl111Il1l1l();
      if (timerMultiplier > 0.0F && Float.isFinite(timerMultiplier) && timerMultiplier != 1.0F) {
         callbackInfo.setReturnValue((Float)callbackInfo.getReturnValue() / timerMultiplier);
      }
   }
}

