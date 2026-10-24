package dev.daamky.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.daamky.NoPush;
import dev.daamky.RotationPriorityManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin {
   @Inject(method = "isPushedByFluids()Z", at = @At("HEAD"), cancellable = true)
   private void daamky$noFluidPush(CallbackInfoReturnable<Boolean> callbackInfo) {
      if ((PlayerEntity)(Object)this == MinecraftClient.getInstance().player && NoPush.IIIlll111l111()) {
         callbackInfo.setReturnValue(false);
      }
   }

   @ModifyExpressionValue(
      method = "travel",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;")
   )
   private Vec3d daamky$swimLook(Vec3d lookDirection) {
      return (PlayerEntity)(Object)this == MinecraftClient.getInstance().player ? RotationPriorityManager.getInstance().resolveLookVector(lookDirection) : lookDirection;
   }
}

