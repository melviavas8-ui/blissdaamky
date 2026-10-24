package dev.daamky.mixin.client;

import dev.daamky.ClickThrough;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClickThroughMixin {
   @Inject(method = "doItemUse()V", at = @At("HEAD"))
   private void daamky$clickThrough(CallbackInfo callbackInfo) {
      ClickThrough.updateCrosshairTarget();
   }
}

