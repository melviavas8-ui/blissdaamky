package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.attribute.EnvironmentAttributes;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoAnchor extends ModuleStructure {
   private final FloatSetting targetSearchRange = new FloatSetting("Дистанция до цели", 8.0F, 3.0F, 12.0F, 0.5F, "бл.");
   private final FloatSetting anchorInteractionRange = new FloatSetting("Дистанция установки", 4.5F, 2.0F, 6.0F, 0.1F, "бл.");
   private final FloatSetting minimumPlayerDistance = new FloatSetting("Мин. дистанция до себя", 3.0F, 0.0F, 6.0F, 0.5F, "бл.");
   private final FloatSetting minimumHealth = new FloatSetting("Мин. здоровье", 8.0F, 0.0F, 20.0F, 0.5F, "HP");
   private final FloatSetting actionDelayMillis = new FloatSetting("Задержка", 150.0F, 0.0F, 1000.0F, 10.0F, "мс");
   private final BooleanSetting placeAnchorEnabled = new BooleanSetting("Ставить", true);
   private final BooleanSetting restoreHotbarSlot = new BooleanSetting("Возвращать слот", true);
   private final Stopwatch actionTimer = new Stopwatch();
   private BlockPos existingAnchorPosition;
   private BlockPos placementTargetPosition;
   private Direction placementFace;

   public AutoAnchor() {
      super("AutoAnchor", "Ставит, заряжает и взрывает якорь возрождения рядом с целью", ModuleCategory.COMBAT);
      this.addSettings(
         this.targetSearchRange, this.anchorInteractionRange, this.minimumPlayerDistance, this.minimumHealth, this.actionDelayMillis, this.placeAnchorEnabled, this.restoreHotbarSlot
      );
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate event) {
      this.existingAnchorPosition = null;
      this.placementTargetPosition = null;
      if (this.hasActiveClientWorld() && !(Boolean)CLIENT.world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS_GAMEPLAY)) {
         if (!(CombatUtils.getEffectiveHealth((LivingEntity)CLIENT.player) <= this.minimumHealth.getValue())) {
            PlayerEntity targetPlayer = this.findNearestTargetPlayer();
            if (targetPlayer != null) {
               this.existingAnchorPosition = this.findExistingAnchorNearTarget(targetPlayer);
               if (this.existingAnchorPosition == null
                  && this.placeAnchorEnabled.getValue()
                  && InventoryUtils.findHotbarSlot(candidateStack -> candidateStack.isOf(Items.RESPAWN_ANCHOR)) != -1) {
                  this.findAnchorPlacementNearTarget(targetPlayer);
               }

               Vec3d rotationTargetPosition = this.existingAnchorPosition == null
                  ? (
                     this.placementTargetPosition == null
                        ? null
                        : this.getFaceHitPosition(this.placementTargetPosition.offset(this.placementFace.getOpposite()), this.placementFace)
                  )
                  : Vec3d.ofCenter(this.existingAnchorPosition);
               if (rotationTargetPosition != null) {
                  Rotation targetRotation = RotationUtils.lookAt(
                     CLIENT.player.getEyePos(), rotationTargetPosition
                  );
                  RotationPriorityManager.getInstance().requestRotation(targetRotation, FocusMode.FREE, 60.0F, 4, false, this);
               }
            }
         }
      }
   }

   @EventTarget
   public void onMotion(EventMotion event) {
      if (event.isPost() && this.hasActiveClientWorld() && this.actionTimer.hasElapsed(this.actionDelayMillis.getValue().longValue())) {
         int previousHotbarSlot = InventoryUtils.getHotbarSlotToRestore();
         if (this.existingAnchorPosition != null) {
            BlockState anchorState = CLIENT.world.getBlockState(this.existingAnchorPosition);
            int chargeCount = (Integer)anchorState.get(RespawnAnchorBlock.CHARGES);
            int hotbarSlot;
            if (chargeCount == 0) {
               hotbarSlot = InventoryUtils.findHotbarSlot(candidateStack -> candidateStack.isOf(Items.GLOWSTONE));
            } else {
               if (CLIENT.player.getOffHandStack().isOf(Items.GLOWSTONE) || CLIENT.player.shouldCancelInteraction()) {
                  return;
               }

               hotbarSlot = InventoryUtils.findHotbarSlot(
                  candidateStack -> !candidateStack.isOf(Items.GLOWSTONE) && !(candidateStack.getItem() instanceof BlockItem)
               );
               if (hotbarSlot == -1) {
                  hotbarSlot = InventoryUtils.findHotbarSlot(candidateStack -> !candidateStack.isOf(Items.GLOWSTONE));
               }
            }

            if (hotbarSlot == -1) {
               return;
            }

            InventoryUtils.setSelectedHotbarSlot(hotbarSlot);
            this.interactWithBlockFace(this.existingAnchorPosition, Direction.UP);
         } else {
            if (this.placementTargetPosition == null) {
               return;
            }

            int anchorHotbarSlot = InventoryUtils.findHotbarSlot(candidateStack -> candidateStack.isOf(Items.RESPAWN_ANCHOR));
            if (anchorHotbarSlot == -1) {
               return;
            }

            InventoryUtils.setSelectedHotbarSlot(anchorHotbarSlot);
            this.interactWithBlockFace(this.placementTargetPosition.offset(this.placementFace.getOpposite()), this.placementFace);
         }

         if (this.restoreHotbarSlot.getValue()) {
            InventoryUtils.rememberHotbarSlotForRestore(previousHotbarSlot);
         }

         this.actionTimer.reset();
      }
   }

   private void interactWithBlockFace(BlockPos targetBlockPosition, Direction clickedFace) {
      CLIENT.interactionManager
         .interactBlock(CLIENT.player, Hand.MAIN_HAND, new BlockHitResult(this.getFaceHitPosition(targetBlockPosition, clickedFace), clickedFace, targetBlockPosition, false));
      CLIENT.player.swingHand(Hand.MAIN_HAND);
   }

   private Vec3d getFaceHitPosition(BlockPos targetBlockPosition, Direction face) {
      return Vec3d.ofCenter(targetBlockPosition).add(Vec3d.of(face.getVector()).multiply(0.5));
   }

   private boolean isValidAnchorPosition(BlockPos anchorPosition, PlayerEntity targetPlayer) {
      Vec3d anchorCenter = Vec3d.ofCenter(anchorPosition);
      return CLIENT.player.getEyePos().distanceTo(anchorCenter) <= this.anchorInteractionRange.getValue().floatValue()
         && CLIENT.player.getEntityPos().distanceTo(anchorCenter) >= this.minimumPlayerDistance.getValue().floatValue()
         && targetPlayer.getEntityPos().distanceTo(anchorCenter) <= 3.0;
   }

   private BlockPos findExistingAnchorNearTarget(PlayerEntity targetPlayer) {
      BlockPos targetBlockPosition = targetPlayer.getBlockPos();

      for (int xOffset = -2; xOffset <= 2; xOffset++) {
         for (int yOffset = -1; yOffset <= 2; yOffset++) {
            for (int zOffset = -2; zOffset <= 2; zOffset++) {
               BlockPos candidateAnchorPosition = targetBlockPosition.add(xOffset, yOffset, zOffset);
               if (CLIENT.world.getBlockState(candidateAnchorPosition).isOf(Blocks.RESPAWN_ANCHOR) && this.isValidAnchorPosition(candidateAnchorPosition, targetPlayer)) {
                  return candidateAnchorPosition;
               }
            }
         }
      }

      return null;
   }

   private void findAnchorPlacementNearTarget(PlayerEntity targetPlayer) {
      BlockPos targetBlockPosition = targetPlayer.getBlockPos();
      double closestDistance = Double.MAX_VALUE;

      for (int xOffset = -2; xOffset <= 2; xOffset++) {
         for (int yOffset = -1; yOffset <= 2; yOffset++) {
            for (int zOffset = -2; zOffset <= 2; zOffset++) {
               BlockPos candidatePosition = targetBlockPosition.add(xOffset, yOffset, zOffset);
               if (CLIENT.world.getBlockState(candidatePosition).isReplaceable()
                  && this.isValidAnchorPosition(candidatePosition, targetPlayer)
                  && !targetPlayer.getBoundingBox().intersects(new Box(candidatePosition))
                  && !CLIENT.player.getBoundingBox().intersects(new Box(candidatePosition))) {
                  double playerToCandidateDistance = targetPlayer.getEntityPos().distanceTo(Vec3d.ofCenter(candidatePosition));
                  if (!(playerToCandidateDistance >= closestDistance)) {
                     for (Direction face : Direction.values()) {
                        if (!CLIENT.world.getBlockState(candidatePosition.offset(face)).isReplaceable()) {
                           closestDistance = playerToCandidateDistance;
                           this.placementTargetPosition = candidatePosition;
                           this.placementFace = face.getOpposite();
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private PlayerEntity findNearestTargetPlayer() {
      PlayerEntity closestPlayer = null;
      double searchRange = this.targetSearchRange.getValue().floatValue();

      for (PlayerEntity candidatePlayer : CLIENT.world.getPlayers()) {
         if (candidatePlayer != CLIENT.player
            && candidatePlayer.isAlive()
            && !candidatePlayer.isSpectator()
            && !candidatePlayer.isCreative()
            && !FriendManager.getInstance().isFriend(candidatePlayer)
            && !AntiBot.II1Il11l111II11IIl(candidatePlayer)) {
            double playerDistance = candidatePlayer.distanceTo(CLIENT.player);
            if (playerDistance < searchRange) {
               searchRange = playerDistance;
               closestPlayer = candidatePlayer;
            }
         }
      }

      return closestPlayer;
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.existingAnchorPosition = null;
      this.placementTargetPosition = null;
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }
}

