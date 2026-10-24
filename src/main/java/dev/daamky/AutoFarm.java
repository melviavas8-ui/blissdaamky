package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoFarm extends ModuleStructure {
   private final MultiToggleSetting enabledCrops = new MultiToggleSetting("Культуры")
      .addOption("Пшеница", true)
      .addOption("Морковь", true)
      .addOption("Картофель", true)
      .addOption("Свёкла", true)
      .addOption("Адский нарост", true)
      .addOption("Тростник", false);
   private final BooleanSetting replantCrops = new BooleanSetting("Пересаживать", true);
   private final BooleanSetting rotateToTarget = new BooleanSetting("Поворачиваться", true);
   private final FloatSetting targetRadius = new FloatSetting("Радиус", 4.5F, 2.0F, 6.0F, 0.1F, "бл.");
   private final FloatSetting actionDelay = new FloatSetting("Задержка", 80.0F, 0.0F, 500.0F, 10.0F, "мс");
   private final Stopwatch actionStopwatch = new Stopwatch();
   private BlockPos targetBlockPosition;
   private boolean targetRequiresReplanting;

   public AutoFarm() {
      super("AutoFarm", "Собирает созревшие культуры вокруг и пересаживает их", ModuleCategory.PLAYER);
      this.addSettings(this.enabledCrops, this.replantCrops, this.rotateToTarget, this.targetRadius, this.actionDelay);
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.targetBlockPosition = null;
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      this.targetBlockPosition = null;
      if (this.hasActiveClientWorld() && CLIENT.currentScreen == null && this.actionStopwatch.hasElapsed(this.actionDelay.getValue().longValue())
         )
       {
         this.findNearestCropTarget();
         if (this.targetBlockPosition != null) {
            if (this.rotateToTarget.getValue()) {
               RotationPriorityManager.getInstance()
                  .requestRotation(
                     RotationUtils.lookAt(CLIENT.player.getEyePos(), Vec3d.ofCenter(this.targetBlockPosition)),
                     FocusMode.FREE,
                     60.0F,
                     2,
                     false,
                     this
                  );
            }
         } else {
            RotationPriorityManager.getInstance().clearRotationRequest(this);
         }
      } else {
         RotationPriorityManager.getInstance().clearRotationRequest(this);
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onMotion(EventMotion eventMotion) {
      if (eventMotion.isPost() && this.targetBlockPosition != null && this.hasActiveClientWorld()) {
         if (!this.rotateToTarget.getValue() || RotationPriorityManager.getInstance().isFocusedBy(this)) {
            if (!this.targetRequiresReplanting) {
               CLIENT.interactionManager.attackBlock(this.targetBlockPosition, Direction.UP);
               CLIENT.player.swingHand(Hand.MAIN_HAND);
            } else {
               this.replantCrop(this.targetBlockPosition);
            }

            this.actionStopwatch.reset();
            this.targetBlockPosition = null;
         }
      }
   }

   private void findNearestCropTarget() {
      Vec3d playerEyePosition = CLIENT.player.getEyePos();
      double radiusSquared = this.targetRadius.getValue() * this.targetRadius.getValue();
      int searchRadiusInBlocks = (int)Math.ceil(this.targetRadius.getValue().floatValue());
      BlockPos playerBlockPosition = CLIENT.player.getBlockPos();
      double nearestTargetDistanceSquared = radiusSquared;

      for (BlockPos candidateBlockPosition : BlockPos.iterate(playerBlockPosition.add(-searchRadiusInBlocks, -2, -searchRadiusInBlocks), playerBlockPosition.add(searchRadiusInBlocks, 2, searchRadiusInBlocks))) {
         double candidateDistanceSquared = Vec3d.ofCenter(candidateBlockPosition).squaredDistanceTo(playerEyePosition);
         if (!(candidateDistanceSquared >= nearestTargetDistanceSquared)) {
            BlockState blockState = CLIENT.world.getBlockState(candidateBlockPosition);
            if (this.isHarvestableSelectedCrop(candidateBlockPosition, blockState)) {
               nearestTargetDistanceSquared = candidateDistanceSquared;
               this.targetBlockPosition = candidateBlockPosition.toImmutable();
               this.targetRequiresReplanting = false;
            } else if (this.replantCrops.getValue()
               && blockState.isAir()
               && this.getReplantItem(CLIENT.world.getBlockState(candidateBlockPosition.down()).getBlock()) != null
               && InventoryUtils.findHotbarSlot(
                     replantSeedStack -> replantSeedStack.isOf(this.getReplantItem(CLIENT.world.getBlockState(candidateBlockPosition.down()).getBlock()))
                  )
                  != -1) {
               nearestTargetDistanceSquared = candidateDistanceSquared;
               this.targetBlockPosition = candidateBlockPosition.toImmutable();
               this.targetRequiresReplanting = true;
            }
         }
      }
   }

   private boolean isHarvestableSelectedCrop(BlockPos cropPosition, BlockState blockState) {
      Block block = blockState.getBlock();
      if (block instanceof CropBlock) {
         CropBlock block2 = (CropBlock)block;
         return block2.isMature(blockState) && this.isSelectedCrop(block);
      } else if (block instanceof NetherWartBlock) {
         return (Integer)blockState.get(NetherWartBlock.AGE) >= 3 && this.enabledCrops.isOptionEnabled("Адский нарост");
      } else {
         return block != Blocks.SUGAR_CANE
            ? false
            : this.enabledCrops.isOptionEnabled("Тростник")
               && CLIENT.world.getBlockState(cropPosition.down()).isOf(Blocks.SUGAR_CANE)
               && !CLIENT.world.getBlockState(cropPosition.down(2)).isOf(Blocks.SUGAR_CANE);
      }
   }

   private boolean isSelectedCrop(Block block) {
      if (block == Blocks.WHEAT) {
         return this.enabledCrops.isOptionEnabled("Пшеница");
      } else if (block == Blocks.CARROTS) {
         return this.enabledCrops.isOptionEnabled("Морковь");
      } else if (block == Blocks.POTATOES) {
         return this.enabledCrops.isOptionEnabled("Картофель");
      } else {
         return block == Blocks.BEETROOTS ? this.enabledCrops.isOptionEnabled("Свёкла") : false;
      }
   }

   private Item getReplantItem(Block block) {
      if (block == Blocks.SOUL_SAND) {
         return this.enabledCrops.isOptionEnabled("Адский нарост") ? Items.NETHER_WART : null;
      } else if (block != Blocks.FARMLAND) {
         return null;
      } else if (this.enabledCrops.isOptionEnabled("Пшеница") && InventoryUtils.findHotbarSlot(seedStack -> seedStack.isOf(Items.WHEAT_SEEDS)) != -1) {
         return Items.WHEAT_SEEDS;
      } else if (this.enabledCrops.isOptionEnabled("Морковь") && InventoryUtils.findHotbarSlot(seedStack -> seedStack.isOf(Items.CARROT)) != -1) {
         return Items.CARROT;
      } else if (this.enabledCrops.isOptionEnabled("Картофель") && InventoryUtils.findHotbarSlot(seedStack -> seedStack.isOf(Items.POTATO)) != -1) {
         return Items.POTATO;
      } else {
         return this.enabledCrops.isOptionEnabled("Свёкла") && InventoryUtils.findHotbarSlot(seedStack -> seedStack.isOf(Items.BEETROOT_SEEDS)) != -1
            ? Items.BEETROOT_SEEDS
            : null;
      }
   }

   private void replantCrop(BlockPos cropPosition) {
      BlockPos plantingSoilPosition = cropPosition.down();
      Item item = this.getReplantItem(CLIENT.world.getBlockState(plantingSoilPosition).getBlock());
      if (item != null) {
         int seedSlot = InventoryUtils.findHotbarSlot(seedStack -> seedStack.isOf(item));
         if (seedSlot != -1) {
            int previousHotbarSlot = InventoryUtils.getHotbarSlotToRestore();
            InventoryUtils.setSelectedHotbarSlot(seedSlot);
            CLIENT.interactionManager
               .interactBlock(
                  CLIENT.player,
                  Hand.MAIN_HAND,
                  new BlockHitResult(Vec3d.ofCenter(plantingSoilPosition).add(0.0, 0.5, 0.0), Direction.UP, plantingSoilPosition, false)
               );
            CLIENT.player.swingHand(Hand.MAIN_HAND);
            InventoryUtils.rememberHotbarSlotForRestore(previousHotbarSlot);
         }
      }
   }
}

