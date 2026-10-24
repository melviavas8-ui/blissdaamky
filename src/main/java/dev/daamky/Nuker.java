package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.block.BlockState;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Nuker extends ModuleStructure {
   private final FloatSetting radiusSetting = new FloatSetting("Радиус", 4.0F, 1.0F, 5.5F, 0.5F, "бл.");
   private final BooleanSetting avoidBreakingBelowPlayerSetting = new BooleanSetting("Не ломать под ногами", true);
   private final BooleanSetting rotationSetting = new BooleanSetting("Ротация", true);
   private BlockPos targetBlockPos;

   public Nuker() {
      super("Nuker", "Ломает блоки вокруг игрока", ModuleCategory.PLAYER);
      this.addSettings(this.radiusSetting, this.avoidBreakingBelowPlayerSetting, this.rotationSetting);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (this.targetBlockPos == null || !this.canBreakBlock(this.targetBlockPos)) {
            if (this.targetBlockPos != null && CLIENT.interactionManager.isBreakingBlock()) {
               CLIENT.interactionManager.cancelBlockBreaking();
            }

            this.targetBlockPos = this.findTargetBlock();
         }

         if (this.targetBlockPos != null && this.rotationSetting.getValue()) {
            RotationPriorityManager.getInstance()
               .requestRotation(
                  RotationUtils.lookAt(CLIENT.player.getEyePos(), Vec3d.ofCenter(this.targetBlockPos)),
                  FocusMode.FREE,
                  60.0F,
                  3,
                  false,
                  this
               );
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onMotion(EventMotion eventMotion) {
      if (eventMotion.isPost() && this.targetBlockPos != null && this.hasActiveClientWorld()) {
         Vec3d eyeToTargetDirection = CLIENT.player.getEyePos().subtract(Vec3d.ofCenter(this.targetBlockPos));
         Direction direction = Direction.getFacing(eyeToTargetDirection.x, eyeToTargetDirection.y, eyeToTargetDirection.z);
         CLIENT.interactionManager.updateBlockBreakingProgress(this.targetBlockPos, direction);
         CLIENT.player.swingHand(Hand.MAIN_HAND);
         if (CLIENT.world.getBlockState(this.targetBlockPos).isAir()) {
            this.targetBlockPos = null;
         }
      }
   }

   private BlockPos findTargetBlock() {
      BlockPos playerBlockPos = CLIENT.player.getBlockPos();
      int searchRadius = (int)Math.ceil(this.radiusSetting.getValue().floatValue());
      BlockPos nearestBlockPos = null;
      double nearestDistanceSquared = Double.MAX_VALUE;

      for (int offsetX = -searchRadius; offsetX <= searchRadius; offsetX++) {
         for (int offsetY = -searchRadius; offsetY <= searchRadius; offsetY++) {
            for (int offsetZ = -searchRadius; offsetZ <= searchRadius; offsetZ++) {
               BlockPos candidateBlockPos = playerBlockPos.add(offsetX, offsetY, offsetZ);
               if (this.canBreakBlock(candidateBlockPos)) {
                  double distanceSquared = CLIENT.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(candidateBlockPos));
                  if (distanceSquared < nearestDistanceSquared) {
                     nearestDistanceSquared = distanceSquared;
                     nearestBlockPos = candidateBlockPos;
                  }
               }
            }
         }
      }

      return nearestBlockPos;
   }

   private boolean canBreakBlock(BlockPos targetBlockPos) {
      BlockState blockState = CLIENT.world.getBlockState(targetBlockPos);
      if (blockState.isAir() || blockState.isLiquid() || blockState.getHardness(CLIENT.world, targetBlockPos) < 0.0F) {
         return false;
      } else {
         return this.avoidBreakingBelowPlayerSetting.getValue() && targetBlockPos.getY() < CLIENT.player.getBlockY()
            ? false
            : CLIENT.player.getEyePos().distanceTo(Vec3d.ofCenter(targetBlockPos)) <= this.radiusSetting.getValue().floatValue();
      }
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      if (CLIENT.interactionManager != null && CLIENT.interactionManager.isBreakingBlock()) {
         CLIENT.interactionManager.cancelBlockBreaking();
      }

      this.targetBlockPos = null;
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }
}

