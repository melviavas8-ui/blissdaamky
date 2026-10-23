package dev.daamky.mixin.entity;

import dev.daamky.ElytraBoost;
import dev.daamky.ElytraTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin {
    private static final String p2e504695 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   private LivingEntity shooter;
// https://t.me/pasterplace

   @Redirect(
      method = "tick",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;", ordinal = 0)
   )
   private Vec3d daamky$boost(Vec3d currentVelocity, double velocityX, double velocityY, double velocityZ) {
      if (this.shooter != null && this.shooter == MinecraftClient.getInstance().player) {
         Vec3d configuredBoostDirection = ElytraBoost.getConfiguredBoostDirection();
         if (configuredBoostDirection == null) {
            return currentVelocity.add(velocityX, velocityY, velocityZ);
         }
// https://t.me/pasterplace

         Vec3d rotationDirection = ElytraBoost.getRotationDirectionVector();
         return currentVelocity.add(
            rotationDirection.x * 0.1 + (rotationDirection.x * configuredBoostDirection.x - currentVelocity.x) * 0.5,
            rotationDirection.y * 0.1 + (rotationDirection.y * configuredBoostDirection.y - currentVelocity.y) * 0.5,
            rotationDirection.z * 0.1 + (rotationDirection.z * configuredBoostDirection.z - currentVelocity.z) * 0.5
         );
      } else {
         return currentVelocity.add(velocityX, velocityY, velocityZ);
      }
   }
// https://t.me/pasterplace

   @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
   private Vec3d daamky$boostLook(LivingEntity livingEntity) {
      Vec3d rotationVector = livingEntity.getRotationVector();
      return livingEntity == MinecraftClient.getInstance().player ? ElytraTarget.getSilentRotationDirection(rotationVector) : rotationVector;
   }
}

