package dev.daamky.mixin.client;

import dev.daamky.NoSlow;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class LocalPlayerSlowMixin {
    private static final String p906c6ba4 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "getActiveItemSpeedMultiplier()F", at = @At("RETURN"), cancellable = true)
   private void daamky$noSlowMultiplier(CallbackInfoReturnable<Float> callbackInfo) {
      float movementMultiplier = NoSlow.IllI1l1l1((Float)callbackInfo.getReturnValue());
      if (movementMultiplier != (Float)callbackInfo.getReturnValue()) {
         callbackInfo.setReturnValue(movementMultiplier);
      }
   }
// https://t.me/pasterplace

   @Inject(method = "isBlockedFromSprinting()Z", at = @At("RETURN"), cancellable = true)
   private void daamky$noSlowSprint(CallbackInfoReturnable<Boolean> callbackInfo) {
      if ((Boolean)callbackInfo.getReturnValue() && NoSlow.l1I1IIl111Il1l1l()) {
         callbackInfo.setReturnValue(false);
      }
   }
}

