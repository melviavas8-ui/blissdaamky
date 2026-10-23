package dev.daamky;

import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public final class ClickThrough extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final BooleanSetting signsSetting = new BooleanSetting("Таблички", true);
   private final BooleanSetting bannersSetting = new BooleanSetting("Баннеры", true);
   private final BooleanSetting itemFramesSetting = new BooleanSetting("Рамки", true);
// https://t.me/pasterplace

   public ClickThrough() {
      super("ClickThrough", "ПКМ сквозь таблички, рамки и баннеры открывает контейнер за ними", ModuleCategory.PLAYER);
      this.addSettings(this.signsSetting, this.bannersSetting, this.itemFramesSetting);
   }
// https://t.me/pasterplace

   public static void updateCrosshairTarget() {
      ClickThrough moduleStructure = (ClickThrough)ModuleRegistry.getInstance().getModuleByClass(ClickThrough.class);
      if (moduleStructure != null && moduleStructure.isEnabled()) {
         moduleStructure.updateTargetedBlock();
      }
   }
// https://t.me/pasterplace

   private void updateTargetedBlock() {
      if (this.hasActiveClientWorld() && CLIENT.world != null && !CLIENT.player.isSneaking()) {
         BlockPos targetedBlockPos;
         label71: {
            HitResult hitResult = CLIENT.crosshairTarget;
            targetedBlockPos = null;
            if (hitResult instanceof BlockHitResult) {
               BlockHitResult hitResult2 = (BlockHitResult)hitResult;
               if (hitResult.getType() == Type.BLOCK) {
                  if (!this.isTransparentBlock(CLIENT.world.getBlockState(hitResult2.getBlockPos()))) {
                     return;
                  }

                  targetedBlockPos = hitResult2.getBlockPos();
                  break label71;
               }
            }

            if (!this.itemFramesSetting.getValue() || !(hitResult instanceof EntityHitResult)) {
               return;
            }

            EntityHitResult hitResult3 = (EntityHitResult)hitResult;
            if (!(hitResult3.getEntity() instanceof ItemFrameEntity)) {
               return;
            }
         }
// https://t.me/pasterplace

         Vec3d eyePosition = CLIENT.player.getEyePos();
         Vec3d lookDirection = CLIENT.player.getRotationVec(1.0F);
         double interactionRange = CLIENT.player.getBlockInteractionRange();
         Vec3d rayEnd = eyePosition.add(lookDirection.multiply(interactionRange));
         BlockPos previousBlockPos = null;

         for (double distanceAlongRay = 0.0; distanceAlongRay <= interactionRange; distanceAlongRay += 0.05) {
            BlockPos candidateBlockPos = BlockPos.ofFloored(eyePosition.add(lookDirection.multiply(distanceAlongRay)));
            if (!candidateBlockPos.equals(previousBlockPos) && !candidateBlockPos.equals(targetedBlockPos)) {
               previousBlockPos = candidateBlockPos;
               BlockState blockState = CLIENT.world.getBlockState(candidateBlockPos);
               if (!blockState.isAir() && !this.isTransparentBlock(blockState)) {
                  VoxelShape voxelShape = blockState.getOutlineShape(CLIENT.world, candidateBlockPos);
                  BlockHitResult blockHitResult = voxelShape.isEmpty() ? null : voxelShape.raycast(eyePosition, rayEnd, candidateBlockPos);
                  if (blockHitResult != null) {
                     if (this.isContainerBlock(candidateBlockPos, blockState) && CLIENT.player.canInteractWithBlockAt(candidateBlockPos, 0.0)) {
                        CLIENT.crosshairTarget = blockHitResult;
                     }

                     return;
                  }
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private boolean isTransparentBlock(BlockState blockState) {
      return this.signsSetting.getValue() && blockState.isIn(BlockTags.ALL_SIGNS)
         || this.bannersSetting.getValue() && blockState.isIn(BlockTags.BANNERS);
   }
// https://t.me/pasterplace

   private boolean isContainerBlock(BlockPos blockPosition, BlockState blockState) {
      return blockState.getBlock() instanceof EnderChestBlock
         || blockState.createScreenHandlerFactory(CLIENT.world, blockPosition) != null
         || CLIENT.world.getBlockEntity(blockPosition) instanceof NamedScreenHandlerFactory;
   }
}

