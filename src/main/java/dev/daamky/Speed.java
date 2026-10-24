package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventInput;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Speed extends ModuleStructure {
   private final MultiOptionSetting movementMode = new MultiOptionSetting("Режим", "Collision");
   private final FloatSetting vanillaSpeed = new FloatSetting("Скорость", 1.2F, 0.3F, 3.0F, 0.05F);
   private final FloatSetting reallyWorld2Boost = new FloatSetting("Сила буста", 0.08F, 0.01F, 0.12F, 0.005F);
   private final BooleanSetting autoJump = new BooleanSetting("Авто-прыжок", true);
   private final FloatSetting collisionSpeed = new FloatSetting("Скорость", 0.45F, 0.05F, 1.5F, 0.05F);
   private final FloatSetting targetSearchRadius = new FloatSetting("Радиус", 0.25F, 0.0F, 1.5F, 0.05F);
   private final FloatSetting velocitySmoothing = new FloatSetting("Плавность", 0.15F, 0.01F, 1.0F, 0.01F);
   private final FloatSetting predictionMultiplier = new FloatSetting("Предикт", 1.0F, 0.0F, 5.0F, 0.5F);
   private final BooleanSetting playersOnly = new BooleanSetting("Только игроки", true);
   private final BooleanSetting requireElytra = new BooleanSetting("Только на элитре", false);
   private int airborneTicks;
   private int reallyWorldTicks;
   private int verticalCollisionTicks;
   private double smoothedVelocityX;
   private double smoothedVelocityZ;

   public Speed() {
      super("Speed", "Ускоряет передвижение", ModuleCategory.MOVEMENT);
      this.vanillaSpeed.visibleWhen(() -> this.movementMode.isSelected("Ванилла"));
      this.reallyWorld2Boost.visibleWhen(() -> this.movementMode.isSelected("ReallyWorld 2"));
      this.autoJump.visibleWhen(() -> this.movementMode.isSelected("Strafe") || this.movementMode.isSelected("Matrix"));
      this.collisionSpeed.visibleWhen(() -> this.movementMode.isSelected("Collision"));
      this.targetSearchRadius.visibleWhen(() -> this.movementMode.isSelected("Collision"));
      this.velocitySmoothing.visibleWhen(() -> this.movementMode.isSelected("Collision"));
      this.predictionMultiplier.visibleWhen(() -> this.movementMode.isSelected("Collision"));
      this.playersOnly.visibleWhen(() -> this.movementMode.isSelected("Collision"));
      this.requireElytra.visibleWhen(() -> this.movementMode.isSelected("Collision"));
      this.addSettings(
         this.movementMode,
         this.vanillaSpeed,
         this.reallyWorld2Boost,
         this.autoJump,
         this.collisionSpeed,
         this.targetSearchRadius,
         this.velocitySmoothing,
         this.predictionMultiplier,
         this.playersOnly,
         this.requireElytra
      );
   }

   @Override
   public String getDisplayInfo() {
      return this.movementMode.getValue();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void handleInput(EventInput eventInput) {
      if (this.hasActiveClientWorld() && this.movementMode.isSelected("ReallyWorld")) {
         this.verticalCollisionTicks = !CLIENT.player.verticalCollision ? 0 : this.verticalCollisionTicks + 1;
         if (this.verticalCollisionTicks >= 1 && MovementUtils.isMoving()) {
            eventInput.setJump(true);
         }
      } else if (this.hasActiveClientWorld()
         && this.autoJump.getValue()
         && (this.movementMode.isSelected("Strafe") || this.movementMode.isSelected("Matrix"))) {
         if (MovementUtils.isMoving() && CLIENT.player.isOnGround() && !CLIENT.player.isTouchingWater()) {
            eventInput.setJump(true);
         }
      }
   }

   @EventTarget
   public void handleMovementUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && this.movementMode.isSelected("Collision")) {
         this.applyCollisionSpeed();
      } else if (this.hasActiveClientWorld()
         && !CLIENT.player.isGliding()
         && !CLIENT.player.getAbilities().flying
         && !CLIENT.player.hasVehicle()) {
         this.airborneTicks = CLIENT.player.isOnGround() ? 0 : this.airborneTicks + 1;
         if (!MovementUtils.isMoving()) {
            this.reallyWorldTicks = 0;
         } else {
            switch ((String)this.movementMode.getValue()) {
               case "ReallyWorld 2":
                  this.applyReallyWorld2Boost();
                  break;
               case "ReallyWorld":
                  if (this.reallyWorldTicks > 3) {
                     double verticalBoost = 0.03;
                     if (this.reallyWorldTicks % 2 == 0) {
                        CLIENT.player.addVelocity(0.0, 0.03, 0.0);
                        verticalBoost = CLIENT.player.isOnGround() ? 0.085 : 0.03;
                     }

                     float movementYaw = MovementUtils.getMovementDirectionRadians();
                     CLIENT.player.addVelocity(-MathHelper.sin(movementYaw) * verticalBoost, 0.0, MathHelper.cos(movementYaw) * verticalBoost);
                  }

                  this.reallyWorldTicks++;
                  break;
               case "Strafe":
                  if (!CLIENT.player.isOnGround()) {
                     MovementUtils.setHorizontalSpeed(Math.max(MovementUtils.getHorizontalSpeed(), MovementUtils.getBaseMovementSpeed() * 0.94));
                  }
                  break;
               case "Matrix":
                  if (!CLIENT.player.isOnGround() && this.airborneTicks == 1) {
                     Vec3d playerVelocity = CLIENT.player.getVelocity();
                     CLIENT.player.setVelocity(playerVelocity.x * 1.0065, playerVelocity.y, playerVelocity.z * 1.0065);
                  }

                  if (CLIENT.player.getVelocity().y < 0.0 && this.airborneTicks > 3) {
                     MovementUtils.setHorizontalSpeed(MovementUtils.getHorizontalSpeed());
                  }
                  break;
               case "Ванилла":
                  MovementUtils.setHorizontalSpeed(this.vanillaSpeed.getValue().floatValue() * MovementUtils.getBaseMovementSpeed());
            }
         }
      }
   }

   public static float getReallyWorldSpeedMultiplier() {
      Speed speedModule = (Speed)ModuleRegistry.getInstance().getModuleByClass(Speed.class);
      return speedModule != null
            && speedModule.isEnabled()
            && speedModule.movementMode.isSelected("ReallyWorld")
            && MovementUtils.isMoving()
         ? 1.7F
         : 1.0F;
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.reallyWorldTicks = 0;
      this.verticalCollisionTicks = 0;
      this.smoothedVelocityX = 0.0;
      this.smoothedVelocityZ = 0.0;
   }

   private void applyCollisionSpeed() {
      if (!CLIENT.player.getAbilities().flying
         && !CLIENT.player.hasVehicle()
         && (!this.requireElytra.getValue() || CLIENT.player.isGliding())) {
         LivingEntity collisionTarget = this.findCollisionTarget();
         if (collisionTarget == null) {
            this.resetCollisionVelocity();
         } else {
            double predictionMultiplier = this.predictionMultiplier.getValue().floatValue();
            Vec3d predictedTargetPosition = collisionTarget.getEntityPos()
               .add((collisionTarget.getX() - collisionTarget.lastX) * predictionMultiplier, 0.0, (collisionTarget.getZ() - collisionTarget.lastZ) * predictionMultiplier);
            double targetDeltaX = predictedTargetPosition.x - CLIENT.player.getX();
            double targetDeltaZ = predictedTargetPosition.z - CLIENT.player.getZ();
            if (targetDeltaX * targetDeltaX + targetDeltaZ * targetDeltaZ < 1.0E-6) {
               float movementYaw = MovementUtils.getMovementDirectionRadians();
               targetDeltaX = -MathHelper.sin(movementYaw);
               targetDeltaZ = MathHelper.cos(movementYaw);
            }

            double targetDirectionAngle = Math.atan2(targetDeltaZ, targetDeltaX) - (Math.PI / 2);
            double targetSpeed = 0.072 * this.collisionSpeed.getValue().floatValue();
            double targetVelocityX = -Math.sin(targetDirectionAngle) * targetSpeed;
            double targetVelocityZ = Math.cos(targetDirectionAngle) * targetSpeed;
            float smoothingFactor = this.velocitySmoothing.getValue();
            this.smoothedVelocityX = this.smoothedVelocityX + (targetVelocityX - this.smoothedVelocityX) * smoothingFactor;
            this.smoothedVelocityZ = this.smoothedVelocityZ + (targetVelocityZ - this.smoothedVelocityZ) * smoothingFactor;
            Vec3d playerVelocity = CLIENT.player.getVelocity();
            CLIENT.player
               .setVelocity(playerVelocity.x + this.smoothedVelocityX, playerVelocity.y, playerVelocity.z + this.smoothedVelocityZ);
         }
      } else {
         this.resetCollisionVelocity();
      }
   }

   private LivingEntity findCollisionTarget() {
      Box searchBounds = CLIENT.player.getBoundingBox().expand(this.targetSearchRadius.getValue().floatValue());
      LivingEntity nearestTarget = null;
      double nearestDistanceSquared = Double.MAX_VALUE;

      for (Entity candidateEntity : CLIENT.world
         .getOtherEntities(CLIENT.player, searchBounds.expand(CLIENT.player.isGliding() ? 2.5 : 0.0))) {
         if (candidateEntity instanceof LivingEntity candidateLivingEntity) {
            if (candidateLivingEntity.isAlive() && !(candidateLivingEntity instanceof ArmorStandEntity) && (!this.playersOnly.getValue() || candidateLivingEntity instanceof PlayerEntity)) {
               boolean intersectsSearchBounds = candidateLivingEntity.getBoundingBox().intersects(searchBounds);
               if (intersectsSearchBounds
                  || CLIENT.player.isGliding()
                     && CLIENT.player.getEyePos().distanceTo(candidateLivingEntity.getBoundingBox().getCenter()) <= 2.5) {
                  double distanceSquared = CLIENT.player.squaredDistanceTo((Entity)candidateLivingEntity);
                  if (distanceSquared < nearestDistanceSquared) {
                     nearestDistanceSquared = distanceSquared;
                     nearestTarget = candidateLivingEntity;
                  }
               }
            }
         }
      }

      return nearestTarget;
   }

   private void resetCollisionVelocity() {
      this.smoothedVelocityX = 0.0;
      this.smoothedVelocityZ = 0.0;
   }

   private void applyReallyWorld2Boost() {
      Box searchBounds = CLIENT.player.getBoundingBox().expand(1.0);
      int nearbyEntityCount = 0;

      for (Entity candidateEntity : CLIENT.world.getOtherEntities(CLIENT.player, searchBounds)) {
         if (candidateEntity instanceof LivingEntity && !(candidateEntity instanceof ArmorStandEntity) && candidateEntity.isAlive()) {
            nearbyEntityCount++;
         }
      }

      if (nearbyEntityCount != 0) {
         float movementYaw = MovementUtils.getMovementDirectionRadians();
         double boostStrength = this.reallyWorld2Boost.getValue() * Math.min(nearbyEntityCount, 3);
         CLIENT.player.addVelocity(-MathHelper.sin(movementYaw) * boostStrength, 0.0, MathHelper.cos(movementYaw) * boostStrength);
      }
   }
}

