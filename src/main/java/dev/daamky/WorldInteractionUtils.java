package dev.daamky;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class WorldInteractionUtils {
   private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
   private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);

   private WorldInteractionUtils() {
   }

   public static Vec3d directionVector(float yawDegrees, float pitchDegrees) {
      float negativeYawRadians = -yawDegrees * (float) (Math.PI / 180.0);
      float negativePitchRadians = -pitchDegrees * (float) (Math.PI / 180.0);
      float pitchCosine = (float)Math.cos(negativePitchRadians);
      return new Vec3d(Math.sin(negativeYawRadians) * pitchCosine, Math.sin(negativePitchRadians), Math.cos(negativeYawRadians) * pitchCosine);
   }

   private static double distanceSquaredToSegment(Vec3d point, Vec3d segmentStart, Vec3d segmentEnd) {
      Vec3d segmentVector = segmentEnd.subtract(segmentStart);
      double segmentLengthSquared = segmentVector.lengthSquared();
      if (segmentLengthSquared == 0.0) {
         return point.squaredDistanceTo(segmentStart);
      }

      double projectionFactor = Math.max(0.0, Math.min(1.0, point.subtract(segmentStart).dotProduct(segmentVector) / segmentLengthSquared));
      return point.squaredDistanceTo(segmentStart.add(segmentVector.multiply(projectionFactor)));
   }

   public static boolean hasClearRayToBox(Box targetBox, float yawDegrees, float pitchDegrees, double reachDistance, boolean checkBlockOcclusion) {
      if (CLIENT.player != null && CLIENT.world != null) {
         Vec3d eyePosition = CLIENT.player.getEyePos();
         if (targetBox.contains(eyePosition)) {
            return true;
         }

         Vec3d boxIntersection = eyePosition.add(directionVector(yawDegrees, pitchDegrees).multiply(reachDistance));
         Optional intersection = targetBox.raycast(eyePosition, boxIntersection);
         if (intersection.isEmpty()) {
            return false;
         }

         if (!checkBlockOcclusion) {
            return true;
         }

         BlockHitResult blockOcclusionHit = CLIENT.world
            .raycast(new RaycastContext(eyePosition, (Vec3d)intersection.get(), ShapeType.OUTLINE, FluidHandling.NONE, CLIENT.player));
         return blockOcclusionHit.getType() != Type.BLOCK
            || eyePosition.squaredDistanceTo(blockOcclusionHit.getPos()) >= eyePosition.squaredDistanceTo((Vec3d)intersection.get());
      } else {
         return false;
      }
   }

   public static boolean isPointNearViewRay(Vec3d targetPosition, float yawDegrees, float pitchDegrees, double reachDistance, double viewRayRadius) {
      Vec3d eyePosition = CLIENT.player.getEyePos();
      Vec3d viewRayEnd = eyePosition.add(directionVector(yawDegrees, pitchDegrees).multiply(reachDistance));
      return distanceSquaredToSegment(targetPosition, eyePosition, viewRayEnd) <= viewRayRadius * viewRayRadius;
   }

   public static boolean isEntityNearViewRay(Entity targetEntity, float yawDegrees, float pitchDegrees, double reachDistance, boolean checkBlockOcclusion) {
      return hasClearRayToBox(targetEntity.getBoundingBox(), yawDegrees, pitchDegrees, reachDistance, checkBlockOcclusion);
   }

   public static float getBlockBreakingSpeed(BlockState blockState, ItemStack toolStack) {
      float miningSpeed = toolStack.getMiningSpeedMultiplier(blockState);
      if (miningSpeed > 1.0F) {
         miningSpeed += (float)CLIENT.player.getAttributeValue(EntityAttributes.MINING_EFFICIENCY);
      }

      if (StatusEffectUtil.hasHaste(CLIENT.player)) {
         miningSpeed *= 1.0F + (StatusEffectUtil.getHasteAmplifier(CLIENT.player) + 1) * 0.2F;
      }

      if (CLIENT.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
         miningSpeed *= switch (CLIENT.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
            case 0 -> 0.3F;
            case 1 -> 0.09F;
            case 2 -> 0.0027F;
            default -> 8.1E-4F;
         };
      }

      miningSpeed *= (float)CLIENT.player.getAttributeValue(EntityAttributes.BLOCK_BREAK_SPEED);
      if (CLIENT.player.isSubmergedIn(FluidTags.WATER)) {
         miningSpeed *= (float)CLIENT.player.getAttributeInstance(EntityAttributes.SUBMERGED_MINING_SPEED).getValue();
      }

      if (!CLIENT.player.isOnGround()) {
         miningSpeed /= 5.0F;
      }

      return miningSpeed;
   }
}

