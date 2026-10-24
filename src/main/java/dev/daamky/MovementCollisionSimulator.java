package dev.daamky;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class MovementCollisionSimulator {
   private MovementCollisionSimulator() {
   }

   public static MovementCollisionSimulator.SimulatedMovementResult simulateMovement(LivingEntity livingEntity, int simulationSteps) {
      Box simulatedBoundingBox = livingEntity.getBoundingBox();
      Vec3d motion = livingEntity instanceof PlayerEntity
         ? new Vec3d(
            livingEntity.getX() - livingEntity.lastX,
            livingEntity.getY() - livingEntity.lastY,
            livingEntity.getZ() - livingEntity.lastZ
         )
         : livingEntity.getVelocity();
      boolean onGround = livingEntity.isOnGround();

      for (int stepIndex = 0; stepIndex < simulationSteps; stepIndex++) {
         Vec3d adjustedMovement = Entity.adjustMovementForCollisions(livingEntity, motion, simulatedBoundingBox, livingEntity.getEntityWorld(), List.of());
         simulatedBoundingBox = simulatedBoundingBox.offset(adjustedMovement);
         boolean horizontalCollision = motion.x != adjustedMovement.x || motion.z != adjustedMovement.z;
         boolean verticalCollision = motion.y != adjustedMovement.y;
         onGround = verticalCollision && motion.y < 0.0;
         double motionX = motion.x != adjustedMovement.x ? 0.0 : adjustedMovement.x;
         double motionZ = motion.z != adjustedMovement.z ? 0.0 : adjustedMovement.z;
         double motionY = onGround ? 0.0 : adjustedMovement.y;
         boolean inWater = intersectsFluid(livingEntity, simulatedBoundingBox, true);
         boolean inLava = intersectsFluid(livingEntity, simulatedBoundingBox, false);
         if (livingEntity.isGliding()) {
            motion = new Vec3d(motionX * 0.99, motionY * 0.98, motionZ * 0.99);
         } else if (inWater) {
            motion = new Vec3d(motionX * 0.8, (motionY - 0.08) * 0.8, motionZ * 0.8);
            if (horizontalCollision && canMoveWithoutCollision(livingEntity, simulatedBoundingBox, motion.x, 0.6, motion.z)) {
               motion = new Vec3d(motion.x, 0.3, motion.z);
            }
         } else if (inLava) {
            motion = new Vec3d(motionX * 0.5, (motionY - 0.02) * 0.5, motionZ * 0.5);
            if (horizontalCollision && canMoveWithoutCollision(livingEntity, simulatedBoundingBox, motion.x, 0.6, motion.z)) {
               motion = new Vec3d(motion.x, 0.3, motion.z);
            }
         } else {
            BlockPos feetBlockPosition = BlockPos.ofFloored((simulatedBoundingBox.minX + simulatedBoundingBox.maxX) * 0.5, simulatedBoundingBox.minY, (simulatedBoundingBox.minZ + simulatedBoundingBox.maxZ) * 0.5);
            if (livingEntity.getEntityWorld().getBlockState(feetBlockPosition).isIn(BlockTags.CLIMBABLE)) {
               motionX = Math.max(-0.15, Math.min(0.15, motionX));
               motionZ = Math.max(-0.15, Math.min(0.15, motionZ));
               motionY = Math.max(motionY, -0.15);
               if (horizontalCollision) {
                  motionY = 0.2;
               }
            }

            double gravity = livingEntity.hasStatusEffect(StatusEffects.SLOW_FALLING) && motionY <= 0.0 ? 0.01 : 0.08;
            StatusEffectInstance levitationEffect = livingEntity.getStatusEffect(StatusEffects.LEVITATION);
            if (levitationEffect != null) {
               motionY += (0.05 * (levitationEffect.getAmplifier() + 1) - motionY) * 0.2;
            } else if (!livingEntity.hasNoGravity()) {
               motionY -= gravity;
            }

            BlockPos supportBlockPosition = BlockPos.ofFloored(
               (simulatedBoundingBox.minX + simulatedBoundingBox.maxX) * 0.5, simulatedBoundingBox.minY - 0.5000001, (simulatedBoundingBox.minZ + simulatedBoundingBox.maxZ) * 0.5
            );
            BlockState supportBlockState = livingEntity.getEntityWorld().getBlockState(supportBlockPosition);
            float groundFriction = onGround ? supportBlockState.getBlock().getSlipperiness() * 0.91F : 0.91F;
            motion = new Vec3d(motionX * groundFriction, motionY * 0.98, motionZ * groundFriction);
         }
      }

      return new MovementCollisionSimulator.SimulatedMovementResult(
         simulatedBoundingBox, new Vec3d((simulatedBoundingBox.minX + simulatedBoundingBox.maxX) * 0.5, simulatedBoundingBox.minY, (simulatedBoundingBox.minZ + simulatedBoundingBox.maxZ) * 0.5)
      );
   }

   private static boolean canMoveWithoutCollision(LivingEntity livingEntity, Box currentBoundingBox, double movementX, double movementY, double movementZ) {
      return Entity.adjustMovementForCollisions(livingEntity, new Vec3d(movementX, movementY, movementZ), currentBoundingBox, livingEntity.getEntityWorld(), List.of())
         .equals(new Vec3d(movementX, movementY, movementZ));
   }

   private static boolean intersectsFluid(LivingEntity livingEntity, Box boundingBox, boolean checkWater) {
      int minBlockX = (int)Math.floor(boundingBox.minX);
      int minBlockY = (int)Math.floor(boundingBox.minY);
      int minBlockZ = (int)Math.floor(boundingBox.minZ);
      int maxBlockX = (int)Math.floor(boundingBox.maxX - 1.0E-7);
      int maxBlockY = (int)Math.floor(boundingBox.maxY - 1.0E-7);
      int maxBlockZ = (int)Math.floor(boundingBox.maxZ - 1.0E-7);

      for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
         for (int blockY = minBlockY; blockY <= maxBlockY; blockY++) {
            for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ++) {
               FluidState fluidState = livingEntity.getEntityWorld().getFluidState(new BlockPos(blockX, blockY, blockZ));
               if (checkWater ? fluidState.isIn(FluidTags.WATER) : fluidState.isIn(FluidTags.LAVA)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public record SimulatedMovementResult(Box boundingBox, Vec3d feetPosition) {

      public SimulatedMovementResult(Box boundingBox, Vec3d feetPosition) {
         this.boundingBox = boundingBox;
         this.feetPosition = feetPosition;
      }
   }
}

