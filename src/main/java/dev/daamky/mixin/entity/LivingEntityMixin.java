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
    private static final String p5b4a2443 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "jump()V", at = @At("HEAD"))
   private void daamky$onJumpFromGround(CallbackInfo callbackInfo) {
      JumpCircle.addCircleForLocalPlayer((LivingEntity)(Object)this);
   }
// https://t.me/pasterplace

   @ModifyExpressionValue(method = "jump()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getYaw()F"))
   private float daamky$jumpYaw(float yaw) {
      return (Object)this == MinecraftClient.getInstance().player ? RotationPriorityManager.getInstance().resolveYaw(yaw) : yaw;
   }
// https://t.me/pasterplace

   @Inject(method = "tickCramming()V", at = @At("HEAD"), cancellable = true)
   private void daamky$noPush(CallbackInfo callbackInfo) {
      if ((Object)this == MinecraftClient.getInstance().player && NoPush.l1I1IIl111Il1l1l()) {
         callbackInfo.cancel();
      }
   }
// https://t.me/pasterplace

   @Inject(method = "calcGlidingVelocity", at = @At("RETURN"), cancellable = true)
   private void daamky$elytraVelocity(Vec3d originalVelocity, CallbackInfoReturnable<Vec3d> callbackInfo) {
      if ((Object)this == MinecraftClient.getInstance().player && ClientInitializer.getEventBus() != null) {
         EventElytraVelocity velocityEvent = ClientInitializer.getEventBus().post(new EventElytraVelocity(originalVelocity, (Vec3d)callbackInfo.getReturnValue()));
         if (velocityEvent.velocity() != callbackInfo.getReturnValue()) {
            callbackInfo.setReturnValue(velocityEvent.velocity());
         }
      }
   }
// https://t.me/pasterplace

   @ModifyExpressionValue(
      method = "calcGlidingVelocity",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;")
   )
   private Vec3d daamky$glideLook(Vec3d rotationVector) {
      return (Object)this == MinecraftClient.getInstance().player ? ElytraBoost.adjustDynamicBoostDirection(ElytraTarget.getSilentRotationDirection(rotationVector)) : rotationVector;
   }
// https://t.me/pasterplace

   @ModifyExpressionValue(method = "calcGlidingVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPitch()F"))
   private float daamky$glidePitch(float pitch) {
      return (Object)this == MinecraftClient.getInstance().player ? ElytraTarget.getSilentRotationPitch(pitch) : pitch;
   }
}

