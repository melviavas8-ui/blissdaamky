package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventClickBlockRight;
import dev.daamky.core.event.impl.EventPlaceBlock;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.mixin.client.MultiPlayerGameModeAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public class AutoExplosion extends ModuleStructure {
   private BlockPos pendingExplosionPosition;
   private int activeExplosionItemSlot;
   private boolean isRespawnAnchorBoost;
   private int previousSelectedSlot;
   private boolean restoreSelectedSlotNextTick;
   private Box crystalSearchBounds;
   private BlockPos pendingCrystalBasePosition;
   private boolean deferExplosionUpdate;
   private final BooleanSetting avoidSelfDamage = new BooleanSetting("Не взрывать себя", true);
   private final BooleanSetting preserveValuableDrops = new BooleanSetting("Не взрывать ресурсы", true);

   public AutoExplosion() {
      super("Auto Explosion", "Автоматичeски взрывает кристаллы", ModuleCategory.COMBAT);
      this.addSettings(this.avoidSelfDamage, this.preserveValuableDrops);
   }

   public int findItemSlot(Item targetItem, boolean searchHotbarOnly) {
      int slotLimit = searchHotbarOnly ? 9 : 36;
      int foundSlot = -1;

      for (int slotIndex = 0; slotIndex < slotLimit; slotIndex++) {
         if (CLIENT.player.getInventory().getStack(slotIndex).getItem() == targetItem) {
            foundSlot = slotIndex;
         }
      }

      return foundSlot;
   }

   @EventTarget
   public void updateExplosionAction(EventUpdate eventUpdate) {
      if (CLIENT.player != null && CLIENT.world != null && CLIENT.interactionManager != null) {
         if (this.restoreSelectedSlotNextTick) {
            this.restoreSelectedSlotNextTick = false;
            CLIENT.player.getInventory().setSelectedSlot(this.previousSelectedSlot);
            this.syncSelectedSlot();
         } else if (this.pendingExplosionPosition != null) {
            if (this.isReservedBlock(this.pendingExplosionPosition) || CLIENT.world.getBlockState(this.pendingExplosionPosition).isAir()) {
               this.clearExplosionTarget();
            } else if (this.deferExplosionUpdate) {
               this.deferExplosionUpdate = false;
            } else {
               Vec3d playerCameraPosition = CLIENT.player.getCameraPosVec(CLIENT.getRenderTickCounter().getTickProgress(true));
               Vec3d closestBlockPoint = RotationUtils.closestPointOnBox(playerCameraPosition, new Box(this.pendingExplosionPosition));
               Vec3d lookDirection = closestBlockPoint.subtract(playerCameraPosition);
               float rotationYaw = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(lookDirection.z, lookDirection.x)) - 90.0);
               float rotationPitch = (float)(-Math.toDegrees(Math.atan2(lookDirection.y, Math.hypot(lookDirection.x, lookDirection.z))));
               Priority4.requestRotation(new Rotation(rotationYaw, rotationPitch), 180.0F, 1, 6);
               this.previousSelectedSlot = CLIENT.player.getInventory().getSelectedSlot();
               CLIENT.player.getInventory().setSelectedSlot(this.activeExplosionItemSlot);
               this.syncSelectedSlot();
               lookDirection = lookDirection.negate();
               CLIENT.interactionManager
                  .interactBlock(
                     CLIENT.player,
                     Hand.MAIN_HAND,
                     new BlockHitResult(
                        closestBlockPoint, Direction.getFacing((float)lookDirection.x, (float)lookDirection.y, (float)lookDirection.z), this.pendingExplosionPosition, false
                     )
                  );
               CLIENT.player.swingHand(Hand.MAIN_HAND);
               this.restoreSelectedSlotNextTick = true;
               if (this.isRespawnAnchorBoost) {
                  this.activeExplosionItemSlot = this.previousSelectedSlot;
                  this.isRespawnAnchorBoost = false;
               } else {
                  this.clearExplosionTarget();
               }
            }
         }
      }
   }

   private void clearCrystalTracking() {
      this.crystalSearchBounds = null;
      this.pendingCrystalBasePosition = null;
   }

   @EventTarget
   public void attackPlacedCrystal(EventUpdate eventUpdate) {
      if (this.crystalSearchBounds != null
         && CLIENT.player != null
         && CLIENT.world != null
         && CLIENT.interactionManager != null) {
         if (this.pendingCrystalBasePosition != null && this.isReservedBlock(this.pendingCrystalBasePosition)) {
            this.clearCrystalTracking();
         } else {
             for (Entity entityCandidate : CLIENT.world.getEntities()) {
                if (entityCandidate instanceof EndCrystalEntity crystalCandidate) {
                   if (this.crystalSearchBounds.contains(crystalCandidate.getEntityPos())) {
                     BlockPos crystalBasePosition = BlockPos.ofFloored(crystalCandidate.getX(), crystalCandidate.getY() - 1.0, crystalCandidate.getZ());
                     if (this.isReservedBlock(crystalBasePosition)) {
                        this.clearCrystalTracking();
                        return;
                     }

                     if (this.avoidSelfDamage.getValue() && Math.abs(CLIENT.player.getY() - crystalCandidate.getY()) < 1.0) {
                        this.clearCrystalTracking();
                        return;
                     }

                     if (this.containsValuableDrops((EndCrystalEntity)crystalCandidate)) {
                        this.clearCrystalTracking();
                        return;
                     }

                     Vec3d playerCameraPosition = CLIENT.player.getCameraPosVec(CLIENT.getRenderTickCounter().getTickProgress(true));
                     if (!crystalCandidate.getBoundingBox().contains(playerCameraPosition)) {
                        Vec3d directionToCrystal = RotationUtils.closestPointOnBox(playerCameraPosition, crystalCandidate.getBoundingBox()).subtract(playerCameraPosition);
                        float rotationYaw = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(directionToCrystal.z, directionToCrystal.x)) - 90.0);
                        float rotationPitch = (float)(-Math.toDegrees(Math.atan2(directionToCrystal.y, Math.hypot(directionToCrystal.x, directionToCrystal.z))));
                        Priority4.requestRotation(new Rotation(rotationYaw, rotationPitch), 180.0F, 1, 6);
                     }

                     CLIENT.interactionManager.attackEntity(CLIENT.player, (Entity)crystalCandidate);
                     CLIENT.player.swingHand(Hand.MAIN_HAND);
                     this.clearCrystalTracking();
                     return;
                  }
               }
            }
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onRightClickBlock(EventClickBlockRight eventClickBlockRight) {
      BlockPos clickedBlockPos = eventClickBlockRight.hitResult().getBlockPos();
      if (!this.isReservedBlock(clickedBlockPos)) {
         if (!CLIENT.player.getItemCooldownManager().isCoolingDown(Items.END_CRYSTAL.getDefaultStack())) {
            Block block = eventClickBlockRight.level().getBlockState(clickedBlockPos).getBlock();
            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
               this.pendingCrystalBasePosition = clickedBlockPos.toImmutable();
               this.crystalSearchBounds = new Box(clickedBlockPos.up()).expand(0.1);
            }
         }
      } else {
         if (clickedBlockPos.equals(this.pendingCrystalBasePosition)) {
            this.clearCrystalTracking();
         }
      }
   }

   private boolean containsValuableDrops(EndCrystalEntity crystal) {
      if (this.preserveValuableDrops.getValue() && CLIENT.world != null) {
         Vec3d crystalPosition = new Vec3d(crystal.getX(), crystal.getY(), crystal.getZ());
         Box nearbyItemSearchBounds = new Box(crystalPosition, crystalPosition).expand(6.0);

          for (ItemEntity nearbyItem : CLIENT.world.getEntitiesByClass(ItemEntity.class, nearbyItemSearchBounds, Entity::isAlive)) {
            if (this.isValuableItem(nearbyItem.getStack().getItem())) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean isReservedBlock(BlockPos blockPosition) {
      BlockPosReservationManager reservationManager = BlockPosReservationManager.getInstance();
      return reservationManager.isReservedByOther(this, blockPosition) || reservationManager.isReservedByOther(this, blockPosition.up());
   }

   private void clearExplosionTarget() {
      this.pendingExplosionPosition = null;
      this.isRespawnAnchorBoost = false;
      this.deferExplosionUpdate = false;
   }

   private boolean isValuableItem(Item item) {
      if (item != Items.GOLDEN_APPLE && item != Items.ENCHANTED_GOLDEN_APPLE && item != Items.ELYTRA && item != Items.PLAYER_HEAD && item != Items.END_CRYSTAL) {
         String translationKey = item.getTranslationKey();
         return translationKey != null && translationKey.contains("netherite");
      } else {
         return true;
      }
   }

   @EventTarget
   public void onBlockPlaced(EventPlaceBlock eventPlaceBlock) {
      if (this.isReservedBlock(eventPlaceBlock.pos())) {
         if (eventPlaceBlock.pos().equals(this.pendingExplosionPosition)) {
            this.clearExplosionTarget();
         }

         if (eventPlaceBlock.pos().equals(this.pendingCrystalBasePosition)) {
            this.clearCrystalTracking();
         }
      } else {
         if (eventPlaceBlock.block() == Blocks.OBSIDIAN) {
            if (CLIENT.player.getItemCooldownManager().isCoolingDown(Items.END_CRYSTAL.getDefaultStack())) {
               return;
            }

            int crystalSlot = this.findItemSlot(Items.END_CRYSTAL, true);
            if (crystalSlot == -1) {
               return;
            }

            this.pendingExplosionPosition = eventPlaceBlock.pos().toImmutable();
            this.activeExplosionItemSlot = crystalSlot;
         } else if (eventPlaceBlock.block() instanceof RespawnAnchorBlock) {
            int glowstoneSlot = this.findItemSlot(Items.GLOWSTONE, true);
            if (glowstoneSlot == -1) {
               return;
            }

            this.pendingExplosionPosition = eventPlaceBlock.pos().toImmutable();
            this.activeExplosionItemSlot = glowstoneSlot;
            this.isRespawnAnchorBoost = true;
         }

         this.deferExplosionUpdate = true;
      }
   }

   private void syncSelectedSlot() {
      ((MultiPlayerGameModeAccessor)CLIENT.interactionManager).daamky$syncSelectedSlot();
   }
}

