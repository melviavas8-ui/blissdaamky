package dev.daamky.mixin.client;

import dev.daamky.NoSlow;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class LocalPlayerSlowMixin {
   @Inject(method = "getActiveItemSpeedMultiplier()F", at = @At("RETURN"), cancellable = true)
   private void daamky$noSlowMultiplier(CallbackInfoReturnable<Float> callbackInfo) {
      float movementMultiplier = NoSlow.IllI1l1l1((Float)callbackInfo.getReturnValue());
      if (movementMultiplier != (Float)callbackInfo.getReturnValue()) {
         callbackInfo.setReturnValue(movementMultiplier);
      }
   }

   @Inject(method = "isBlockedFromSprinting()Z", at = @At("RETURN"), cancellable = true)
   private void daamky$noSlowSprint(CallbackInfoReturnable<Boolean> callbackInfo) {
      if ((Boolean)callbackInfo.getReturnValue() && NoSlow.l1I1IIl111Il1l1l()) {
         callbackInfo.setReturnValue(false);
      }
   }
}

