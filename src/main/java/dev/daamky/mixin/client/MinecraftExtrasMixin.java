package dev.daamky.mixin.client;

import dev.daamky.Timer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftExtrasMixin {
    private static final String pe6192b87 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "getTargetMillisPerTick(F)F", at = @At("RETURN"), cancellable = true)
   private void daamky$timer(float tickDelta, CallbackInfoReturnable<Float> callbackInfo) {
      float timerMultiplier = Timer.l1I1IIl111Il1l1l();
      if (timerMultiplier > 0.0F && Float.isFinite(timerMultiplier) && timerMultiplier != 1.0F) {
         callbackInfo.setReturnValue((Float)callbackInfo.getReturnValue() / timerMultiplier);
      }
   }
}

