package dev.daamky.mixin.client;

import dev.daamky.NoClip;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class LocalPlayerNoClipMixin {
   @Inject(method = "pushOutOfBlocks(DD)V", at = @At("HEAD"), cancellable = true)
   private void daamky$noClipPush(double pushX, double pushZ, CallbackInfo callbackInfo) {
      if (NoClip.l1I1IIl111Il1l1l()) {
         callbackInfo.cancel();
      }
   }
}

