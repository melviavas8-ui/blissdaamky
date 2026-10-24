package dev.daamky.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.daamky.ClientInitializer;
import dev.daamky.ElytraBoost;
import dev.daamky.ElytraTarget;
import dev.daamky.JumpCircle;
import dev.daamky.NoPush;
import dev.daamky.RotationPriorityManager;
import dev.daamky.core.event.impl.EventElytraVelocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
   @Inject(method = "jump()V", at = @At("HEAD"))
   private void daamky$onJumpFromGround(CallbackInfo callbackInfo) {
      JumpCircle.addCircleForLocalPlayer((LivingEntity)(Object)this);
   }

   @ModifyExpressionValue(method = "jump()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
   private float daamky$jumpYaw(float yaw) {
      return (Object)this == MinecraftClient.getInstance().player ? RotationPriorityManager.getInstance().resolveYaw(yaw) : yaw;
   }

   @Inject(method = "tickCramming()V", at = @At("HEAD"), cancellable = true)
   private void daamky$noPush(CallbackInfo callbackInfo) {
      if ((Object)this == MinecraftClient.getInstance().player && NoPush.l1I1IIl111Il1l1l()) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "calcGlidingVelocity", at = @At("RETURN"), cancellable = true)
   private void daamky$elytraVelocity(Vec3d originalVelocity, CallbackInfoReturnable<Vec3d> callbackInfo) {
      if ((Object)this == MinecraftClient.getInstance().player && ClientInitializer.getEventBus() != null) {
         EventElytraVelocity velocityEvent = ClientInitializer.getEventBus().post(new EventElytraVelocity(originalVelocity, (Vec3d)callbackInfo.getReturnValue()));
         if (velocityEvent.velocity() != callbackInfo.getReturnValue()) {
            callbackInfo.setReturnValue(velocityEvent.velocity());
         }
      }
   }

   @ModifyExpressionValue(
      method = "calcGlidingVelocity",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;")
   )
   private Vec3d daamky$glideLook(Vec3d rotationVector) {
      return (Object)this == MinecraftClient.getInstance().player ? ElytraBoost.adjustDynamicBoostDirection(ElytraTarget.getSilentRotationDirection(rotationVector)) : rotationVector;
   }

   @ModifyExpressionValue(method = "calcGlidingVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPitch()F"))
   private float daamky$glidePitch(float pitch) {
      return (Object)this == MinecraftClient.getInstance().player ? ElytraTarget.getSilentRotationPitch(pitch) : pitch;
   }
}

