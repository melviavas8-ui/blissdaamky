package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class RotationUtils {
   private RotationUtils() {
   }

   public static Rotation lookAt(Vec3d origin, Vec3d target) {
      double deltaX = target.x - origin.x;
      double deltaY = target.y - origin.y;
      double deltaZ = target.z - origin.z;
      double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance)));
      return new Rotation(MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0F, 90.0F));
   }

   public static Rotation lookAt(Vec3d targetPosition) {
      MinecraftClient client = MinecraftClient.getInstance();
      return lookAt(client.player.getEyePos(), targetPosition);
   }

   public static double mouseSensitivityStep() {
      MinecraftClient client = MinecraftClient.getInstance();
      double sensitivityFactor = (Double)client.options.getMouseSensitivity().getValue() * 0.6F + 0.2F;
      return sensitivityFactor * sensitivityFactor * sensitivityFactor * 8.0 * 0.15;
   }

   public static Rotation snapToSensitivityGrid(
      Rotation currentRotation, Rotation targetRotation
   ) {
      double mouseStep = mouseSensitivityStep();
      float yawDelta = MathHelper.wrapDegrees(targetRotation.getYaw() - currentRotation.getYaw());
      float pitchDelta = targetRotation.getPitch() - currentRotation.getPitch();
      yawDelta = (float)(Math.round(yawDelta / mouseStep) * mouseStep);
      pitchDelta = (float)(Math.round(pitchDelta / mouseStep) * mouseStep);
      return new Rotation(
         currentRotation.getYaw() + yawDelta, MathHelper.clamp(currentRotation.getPitch() + pitchDelta, -90.0F, 90.0F)
      );
   }

   public static Rotation limitRotationChange(
      Rotation currentRotation, Rotation targetRotation, float maxYawStep, float maxPitchStep
   ) {
      float yawDelta = MathHelper.wrapDegrees(targetRotation.getYaw() - currentRotation.getYaw());
      float pitchDelta = targetRotation.getPitch() - currentRotation.getPitch();
      float clampedYawDelta = MathHelper.clamp(yawDelta, -maxYawStep, maxYawStep);
      float clampedPitchDelta = MathHelper.clamp(pitchDelta, -maxPitchStep, maxPitchStep);
      return new Rotation(
         currentRotation.getYaw() + clampedYawDelta, MathHelper.clamp(currentRotation.getPitch() + clampedPitchDelta, -90.0F, 90.0F)
      );
   }

   public static Vec3d closestPointOnBox(Vec3d point, Box bounds) {
      double clampedX = MathHelper.clamp(point.x, bounds.minX, bounds.maxX);
      double clampedY = MathHelper.clamp(point.y, bounds.minY, bounds.maxY);
      double clampedZ = MathHelper.clamp(point.z, bounds.minZ, bounds.maxZ);
      return new Vec3d(clampedX, clampedY, clampedZ);
   }

   public static double distanceToBox(Vec3d point, Box bounds) {
      return point.distanceTo(closestPointOnBox(point, bounds));
   }

   public static float angularDistance(Rotation currentRotation, Vec3d origin, Vec3d target) {
      Rotation targetRotation = lookAt(origin, target);
      return Math.abs(currentRotation.yawDeltaTo(targetRotation))
         + Math.abs(currentRotation.pitchDeltaTo(targetRotation)) * 0.5F;
   }

   public static boolean hasLineOfSight(Vec3d origin, Vec3d target) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.world != null && client.player != null) {
         BlockHitResult blockHitResult = client.world
            .raycast(new RaycastContext(origin, target, ShapeType.COLLIDER, FluidHandling.NONE, client.player));
         return blockHitResult.getType() == Type.MISS;
      } else {
         return false;
      }
   }

   public static Vec3d getAimPoint(Entity entity, Vec3d observerPosition, boolean allowThroughWalls) {
      Box targetBounds = entity.getBoundingBox();
      Vec3d closestPointOnBounds = closestPointOnBox(observerPosition, targetBounds.contract(0.05));
      double targetHeight = targetBounds.maxY - targetBounds.minY;
      Vec3d centerAimPoint = new Vec3d(targetBounds.getCenter().x, targetBounds.minY + targetHeight * 0.7, targetBounds.getCenter().z);
      Vec3d initialAimPoint = new Vec3d(
         centerAimPoint.x + (closestPointOnBounds.x - centerAimPoint.x) * 0.6,
         MathHelper.clamp(observerPosition.y, targetBounds.minY + targetHeight * 0.25, targetBounds.maxY - targetHeight * 0.12),
         centerAimPoint.z + (closestPointOnBounds.z - centerAimPoint.z) * 0.6
      );
      if (!allowThroughWalls && !hasLineOfSight(observerPosition, initialAimPoint)) {
         Vec3d bestVisiblePoint = null;
         double bestDistanceSquared = Double.MAX_VALUE;

         for (int xSampleIndex = 0; xSampleIndex <= 2; xSampleIndex++) {
            for (int ySampleIndex = 0; ySampleIndex <= 4; ySampleIndex++) {
               for (int zSampleIndex = 0; zSampleIndex <= 2; zSampleIndex++) {
                  Vec3d candidatePoint = new Vec3d(
                     MathHelper.lerp(0.1 + xSampleIndex * 0.4, targetBounds.minX, targetBounds.maxX),
                     MathHelper.lerp(0.05 + ySampleIndex * 0.225, targetBounds.minY, targetBounds.maxY),
                     MathHelper.lerp(0.1 + zSampleIndex * 0.4, targetBounds.minZ, targetBounds.maxZ)
                  );
                  if (hasLineOfSight(observerPosition, candidatePoint)) {
                     double candidateDistanceSquared = candidatePoint.squaredDistanceTo(initialAimPoint);
                     if (candidateDistanceSquared < bestDistanceSquared) {
                        bestDistanceSquared = candidateDistanceSquared;
                        bestVisiblePoint = candidatePoint;
                     }
                  }
               }
            }
         }

         return bestVisiblePoint != null ? bestVisiblePoint : initialAimPoint;
      } else {
         return initialAimPoint;
      }
   }

   public static boolean isLookingAtEntity(Rotation rotation, Entity entity, double reach) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null) {
         return false;
      }

      Vec3d eyePosition = client.player.getEyePos();
      Vec3d raycastEnd = eyePosition.add(rotation.directionVector().multiply(reach));
      Box box = entity.getBoundingBox().expand(entity.getTargetingMargin());
      return box.contains(eyePosition) ? true : box.raycast(eyePosition, raycastEnd).isPresent();
   }
}

