package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class TargetPearl extends ModuleStructure {
   private static final double PEARL_LAUNCH_SPEED = 1.5;
   private static final double PEARL_DRAG_FACTOR = 0.99;
   private static final double PEARL_GRAVITY_PER_TICK = 0.03;
   private static final int MAX_TRAJECTORY_TICKS = 200;
   private final BooleanSetting targetAuraOnly = new BooleanSetting("Только цель ауры", true);
   private final FloatSetting maxTargetDistance = new FloatSetting("Дистанция", 12.0F, 3.0F, 30.0F, 0.5F, "бл");
   private final FloatSetting minimumFlightDistance = new FloatSetting("Мин. дистанция полёта", 6.0F, 0.0F, 30.0F, 0.5F, "бл");
   private final FloatSetting trajectoryAccuracy = new FloatSetting("Точность", 3.0F, 0.5F, 8.0F, 0.5F, "бл");
   private final ReallyWorld pearlSwapMode = new ReallyWorld();
   private final Set<UUID> trackedPearlIds = new HashSet<>();
   private final Stopwatch predictionCooldown = new Stopwatch();
   private Rotation aimRotation;
   private Vec3d predictedHitPosition;
   private int aimUpdateTicks;
   private int predictedFlightTicks;
   private int pearlHotbarSlot = -1;
   private int originalHotbarSlot = -1;
   private int restoreSlotDelayTicks;
   private LivingEntity lastAuraTarget;
   private long lastAuraTargetTimeMillis;

   public TargetPearl() {
      super("TargetPearl", "Бросает эндер-жемчуг следом за убегающей целью", ModuleCategory.PLAYER);
      this.addSettings(this.targetAuraOnly, this.maxTargetDistance, this.minimumFlightDistance, this.trajectoryAccuracy, this.pearlSwapMode);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.trackedPearlIds.clear();
      this.aimRotation = null;
      this.predictedHitPosition = null;
      this.pearlHotbarSlot = -1;
      this.restoreSlotDelayTicks = 0;
   }

   private static boolean isEnderPearl(ItemStack stack) {
      return stack.isOf(Items.ENDER_PEARL);
   }

   private boolean hasPearlAvailable() {
      return isEnderPearl(CLIENT.player.getOffHandStack()) || InventoryUtils.findHotbarSlot(TargetPearl::isEnderPearl) != -1;
   }

   private void cancelAimAndScheduleRestore() {
      this.aimRotation = null;
      this.predictedHitPosition = null;
      if (this.pearlHotbarSlot != -1 && this.restoreSlotDelayTicks == 0) {
         this.restoreSlotDelayTicks = 2;
      }
   }

   private void restoreHotbarSlot() {
      if (this.pearlHotbarSlot != -1 && InventoryUtils.canSwapInventorySlots()) {
         InventoryUtils.swapInventorySlotWithHotbar(this.pearlHotbarSlot, this.originalHotbarSlot, this.pearlSwapMode.getInventorySwapCategory());
      }

      this.pearlHotbarSlot = -1;
      this.originalHotbarSlot = -1;
      this.restoreSlotDelayTicks = 0;
   }

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      if (!this.hasActiveClientWorld()) {
         this.aimRotation = null;
         this.predictedHitPosition = null;
      } else if (CLIENT.currentScreen != null) {
         this.cancelAimAndScheduleRestore();
      } else {
         if (this.restoreSlotDelayTicks > 0 && --this.restoreSlotDelayTicks == 0) {
            this.restoreHotbarSlot();
         }

         Aura aura = Aura.getInstance();
         if (aura != null && aura.isEnabled() && aura.getTarget() != null) {
            this.lastAuraTarget = aura.getTarget();
            this.lastAuraTargetTimeMillis = System.currentTimeMillis();
         }

         if (this.aimRotation == null && this.pearlHotbarSlot == -1) {
            this.findTargetPearl();
         }

         if (this.aimRotation != null) {
            if (!this.hasPearlAvailable() && this.pearlHotbarSlot == -1) {
               int lIll1Il1lll111l11l1I1llllI2 = InventoryUtils.findInventorySlot(TargetPearl::isEnderPearl);
               if (lIll1Il1lll111l11l1I1llllI2 == -1 || !InventoryUtils.canSwapInventorySlots()) {
                  this.aimRotation = null;
                  this.predictedHitPosition = null;
                  return;
               }

               this.pearlHotbarSlot = lIll1Il1lll111l11l1I1llllI2;
               this.originalHotbarSlot = InventoryUtils.getSelectedHotbarSlot();
               InventoryUtils.swapInventorySlotWithHotbar(lIll1Il1lll111l11l1I1llllI2, this.originalHotbarSlot, this.pearlSwapMode.getInventorySwapCategory());
            }

            if (++this.aimUpdateTicks > (this.pearlHotbarSlot != -1 ? 16 : 8)) {
               this.cancelAimAndScheduleRestore();
            } else {
               Rotation lIll1Il1lll111l11l1I1llllI2 = this.optimizeAimRotation(
                  CLIENT.player.getEyePos(),
                  this.predictedHitPosition,
                  this.aimRotation.getYaw(),
                  this.aimRotation.getPitch(),
                  this.trajectoryAccuracy.getValue().floatValue()
               );
               if (lIll1Il1lll111l11l1I1llllI2 == null) {
                  this.cancelAimAndScheduleRestore();
               } else {
                  this.aimRotation = lIll1Il1lll111l11l1I1llllI2;
                  RotationPriorityManager.getInstance().requestRotation(this.aimRotation, FocusMode.FREE, 90.0F, 6, false, this);
               }
            }
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onMotion(EventMotion eventMotion) {
      if (eventMotion.isPost() && this.aimRotation != null && this.hasActiveClientWorld()) {
         if (!(Math.abs(MathHelper.wrapDegrees(eventMotion.yaw() - this.aimRotation.getYaw())) > 2.0F)
            && !(Math.abs(eventMotion.pitch() - this.aimRotation.getPitch()) > 2.0F)) {
            if (CLIENT.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL))) {
               this.cancelAimAndScheduleRestore();
            } else if (this.hasPearlAvailable() && !Priority.getInstance().hasQueuedActions()) {
               float originalYaw = CLIENT.player.getYaw();
               float originalPitch = CLIENT.player.getPitch();
               CLIENT.player.setYaw(eventMotion.yaw());
               CLIENT.player.setPitch(eventMotion.pitch());

               try {
                  InventoryUtils.useItemFromInventory(stack -> stack.isOf(Items.ENDER_PEARL), this.pearlSwapMode.getInventorySwapCategory());
               } finally {
                  CLIENT.player.setYaw(originalYaw);
                  CLIENT.player.setPitch(originalPitch);
               }

               this.aimRotation = null;
               this.predictedHitPosition = null;
               this.predictionCooldown.reset();
               if (this.pearlHotbarSlot != -1) {
                  this.restoreSlotDelayTicks = 3;
               }
            }
         }
      }
   }

   private void findTargetPearl() {
      if (this.predictionCooldown.hasElapsed(500L)
         && InventoryUtils.findItemSlot(stack -> stack.isOf(Items.ENDER_PEARL)) != -1
         && !CLIENT.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL))) {
         LivingEntity recentAuraTarget = this.lastAuraTarget != null && System.currentTimeMillis() - this.lastAuraTargetTimeMillis < 3000L ? this.lastAuraTarget : null;

         for (Entity worldEntity : CLIENT.world.getEntities()) {
            if (worldEntity instanceof EnderPearlEntity enderPearl) {
               if (!this.trackedPearlIds.contains(enderPearl.getUuid())) {
                  Entity pearlOwner = enderPearl.getOwner();
                  if (pearlOwner != null || enderPearl.age >= 3) {
                     this.trackedPearlIds.add(enderPearl.getUuid());
                     if (pearlOwner instanceof PlayerEntity) {
                        Entity throwingPlayer = (PlayerEntity)pearlOwner;
                        if (throwingPlayer != CLIENT.player
                           && (!this.targetAuraOnly.getValue() || pearlOwner == recentAuraTarget)
                           && !(CLIENT.player.distanceTo(pearlOwner) > this.maxTargetDistance.getValue())) {
                           Vec3d predictedImpactPosition = this.simulatePearlTrajectory(enderPearl.getEntityPos(), enderPearl.getVelocity());
                           if (predictedImpactPosition != null
                              && !(predictedImpactPosition.distanceTo(CLIENT.player.getEntityPos()) < this.minimumFlightDistance.getValue().floatValue())) {
                              Rotation lIll1Il1lll111l11l1I1llllI2 = this.calculateAimRotation(predictedImpactPosition);
                              if (lIll1Il1lll111l11l1I1llllI2 != null) {
                                 this.aimRotation = lIll1Il1lll111l11l1I1llllI2;
                                 this.predictedHitPosition = predictedImpactPosition;
                                 this.aimUpdateTicks = 0;
                                 return;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private Rotation calculateAimRotation(Vec3d predictedHitPosition) {
      Vec3d playerEyePosition = CLIENT.player.getEyePos();
      double horizontalDeltaX = predictedHitPosition.x - playerEyePosition.x;
      double horizontalDeltaZ = predictedHitPosition.z - playerEyePosition.z;
      float targetYaw = (float)(MathHelper.atan2(horizontalDeltaZ, horizontalDeltaX) * 180.0F / (float)Math.PI) - 90.0F;
      float bestPitch = Float.NaN;
      double bestTrajectoryError = Double.MAX_VALUE;
      float shortFlightPitch = Float.NaN;
      int shortestFlightTicks = Integer.MAX_VALUE;

      for (float candidatePitch = -89.0F; candidatePitch <= 89.0F; candidatePitch++) {
         double trajectoryError = this.calculateTrajectoryError(playerEyePosition, predictedHitPosition, targetYaw, candidatePitch);
         if (trajectoryError < bestTrajectoryError) {
            bestTrajectoryError = trajectoryError;
            bestPitch = candidatePitch;
         }

         if (trajectoryError <= 2.0 && this.predictedFlightTicks < shortestFlightTicks) {
            shortestFlightTicks = this.predictedFlightTicks;
            shortFlightPitch = candidatePitch;
         }
      }

      if (!Float.isNaN(shortFlightPitch)) {
         bestPitch = shortFlightPitch;
      }

      return Float.isNaN(bestPitch) ? null : this.optimizeAimRotation(playerEyePosition, predictedHitPosition, targetYaw, bestPitch, this.trajectoryAccuracy.getValue().floatValue());
   }

   private Rotation optimizeAimRotation(Vec3d startPosition, Vec3d targetPosition, float targetYaw, float initialPitch, double maximumError) {
      float bestYaw = targetYaw;
      float bestPitch = initialPitch;
      double bestError = this.calculateTrajectoryError(startPosition, targetPosition, bestYaw, bestPitch);

      for (float searchStep = 1.0F; searchStep >= 0.05F; searchStep *= 0.5F) {
         boolean foundImprovement = true;
         int iterationCount = 0;

         while (foundImprovement && iterationCount++ < 20) {
            foundImprovement = false;
            float[][] neighboringAngles = new float[][]{{bestYaw, bestPitch - searchStep}, {bestYaw, bestPitch + searchStep}, {bestYaw - searchStep, bestPitch}, {bestYaw + searchStep, bestPitch}};

            for (float[] candidateAngles : neighboringAngles) {
               if (!(candidateAngles[1] < -90.0F) && !(candidateAngles[1] > 90.0F)) {
                  double candidateError = this.calculateTrajectoryError(startPosition, targetPosition, candidateAngles[0], candidateAngles[1]);
                  if (candidateError < bestError) {
                     bestError = candidateError;
                     bestYaw = candidateAngles[0];
                     bestPitch = candidateAngles[1];
                     foundImprovement = true;
                  }
               }
            }
         }
      }

      return bestError <= maximumError ? new Rotation(MathHelper.wrapDegrees(bestYaw), bestPitch) : null;
   }

   private double calculateTrajectoryError(Vec3d startPosition, Vec3d targetPosition, float yaw, float pitch) {
      Vec3d playerVelocity = CLIENT.player.getVelocity();
      Vec3d pearlVelocity = Vec3d.fromPolar(pitch, yaw)
         .multiply(1.5)
         .add(playerVelocity.x, CLIENT.player.isOnGround() ? 0.0 : playerVelocity.y, playerVelocity.z);
      Vec3d predictedImpactPosition = this.simulatePearlTrajectory(startPosition.subtract(0.0, 0.1, 0.0), pearlVelocity);
      return predictedImpactPosition == null ? Double.MAX_VALUE : predictedImpactPosition.distanceTo(targetPosition);
   }

   private Vec3d simulatePearlTrajectory(Vec3d initialPosition, Vec3d initialVelocity) {
      Vec3d currentPosition = initialPosition;
      Vec3d currentVelocity = initialVelocity;

      for (int tick = 0; tick < 200; tick++) {
         Vec3d nextPosition = currentPosition.add(currentVelocity);
         BlockHitResult blockHitResult = CLIENT.world
            .raycast(new RaycastContext(currentPosition, nextPosition, ShapeType.COLLIDER, FluidHandling.NONE, CLIENT.player));
         if (blockHitResult.getType() != Type.MISS) {
            this.predictedFlightTicks = tick + 1;
            return blockHitResult.getPos();
         }

         currentPosition = nextPosition;
         currentVelocity = currentVelocity.multiply(0.99).subtract(0.0, 0.03, 0.0);
         if (currentPosition.y < CLIENT.world.getBottomY()) {
            return null;
         }
      }

      return null;
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.aimRotation = null;
      this.predictedHitPosition = null;
      this.trackedPearlIds.clear();
      this.lastAuraTarget = null;
      this.restoreHotbarSlot();
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }
}

