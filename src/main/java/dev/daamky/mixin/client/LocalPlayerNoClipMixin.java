package dev.daamky.mixin.client;

import dev.daamky.NoClip;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class LocalPlayerNoClipMixin {
    private static final String p77e9f54 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "pushOutOfBlocks(DD)V", at = @At("HEAD"), cancellable = true)
   private void daamky$noClipPush(double pushX, double pushZ, CallbackInfo callbackInfo) {
      if (NoClip.l1I1IIl111Il1l1l()) {
         callbackInfo.cancel();
      }
   }
}

