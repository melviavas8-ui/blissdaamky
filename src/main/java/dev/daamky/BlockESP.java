package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventRender3D;
import dev.daamky.core.event.impl.EventUpdate;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.chunk.WorldChunk;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class BlockESP extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiToggleSetting blockSettings = new MultiToggleSetting("Блоки")
      .addOption("Алмазы", true)
      .addOption("Древние обломки", true)
      .addOption("Изумруды", false)
      .addOption("Золото", false)
      .addOption("Железо", false)
      .addOption("Порталы", false)
      .addOption("Динамит", false)
      .addOption("Паутина", false)
      .addOption("Якоря", false)
      .addOption("Безопасные ямы", false);
   private final MultiToggleSetting storageSettings = new MultiToggleSetting("Хранилища")
      .addOption("Сундуки", true)
      .addOption("Ловушки", true)
      .addOption("Эндер-сундуки", true)
      .addOption("Шалкеры", true)
      .addOption("Бочки", true)
      .addOption("Спавнеры", true)
      .addOption("Печи", false)
      .addOption("Воронки", false)
      .addOption("Раздатчики", false)
      .addOption("Вагонетки", true);
   private final MultiOptionSetting storageColorMode = new MultiOptionSetting("Цвет хранилищ", "По типу", "По типу", "Тема");
   private final BooleanSetting storageTracers = new BooleanSetting("Трейсеры к хранилищам", false);
   private final FloatSetting storageRange = new FloatSetting("Дистанция хранилищ", 96.0F, 16.0F, 256.0F, 8.0F, " бл");
   private final BooleanSetting filledBoxes = new BooleanSetting("Заливка", true);
   private final BooleanSetting renderThroughWalls = new BooleanSetting("Сквозь стены", true);
   private final FloatSetting outlineThickness = new FloatSetting("Толщина", 1.5F, 0.5F, 4.0F, 0.1F);
   private final FloatSetting safeHoleRadius = new FloatSetting("Радиус", 24.0F, 8.0F, 64.0F, 4.0F, " бл");
   private final FloatSetting scanBudgetPerTick = new FloatSetting("Скорость поиска", 96.0F, 16.0F, 512.0F, 16.0F);
   private Map<BlockPos, RgbaColor> blockHighlightColors = new HashMap<>();
   private Map<BlockPos, RgbaColor> pendingBlockHighlightColors = new HashMap<>();
   private int scanCursor;
   private BlockPos scanCenter = BlockPos.ORIGIN;
   private int currentScanRadius;
// https://t.me/pasterplace

   public BlockESP() {
      super("BlockESP", "Подсвечивает руды, порталы, безопасные ямы и хранилища", ModuleCategory.RENDER);
      this.addSettings(
         this.blockSettings,
         this.storageSettings,
         this.storageColorMode,
         this.storageTracers,
         this.filledBoxes,
         this.renderThroughWalls,
         this.outlineThickness,
         this.safeHoleRadius,
         this.scanBudgetPerTick,
         this.storageRange
      );
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.blockHighlightColors = new HashMap<>();
      this.pendingBlockHighlightColors = new HashMap<>();
      this.scanCursor = -1;
   }
// https://t.me/pasterplace

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      if (CLIENT.world != null && CLIENT.player != null) {
         if (this.scanCursor < 0) {
            this.scanCenter = CLIENT.player.getBlockPos();
            this.currentScanRadius = this.safeHoleRadius.getValue().intValue();
            this.scanCursor = 0;
            this.pendingBlockHighlightColors = new HashMap<>();
         }

         int horizontalSideLength = this.currentScanRadius * 2 + 1;
         int horizontalColumnCount = horizontalSideLength * horizontalSideLength;
         int scanMinY = Math.max(CLIENT.world.getBottomY(), this.scanCenter.getY() - this.currentScanRadius);
         int scanMaxY = Math.min(CLIENT.world.getTopYInclusive(), this.scanCenter.getY() + this.currentScanRadius);
         int blocksRemainingThisTick = this.scanBudgetPerTick.getValue().intValue();
         Mutable mutable = new Mutable();
// https://t.me/pasterplace

         while (blocksRemainingThisTick-- > 0 && this.scanCursor < horizontalColumnCount) {
            int scanX = this.scanCenter.getX() - this.currentScanRadius + this.scanCursor % horizontalSideLength;
            int scanZ = this.scanCenter.getZ() - this.currentScanRadius + this.scanCursor / horizontalSideLength;
            this.scanCursor++;

            for (int scanY = scanMinY; scanY <= scanMaxY; scanY++) {
               mutable.set(scanX, scanY, scanZ);
               RgbaColor blockColor = this.getBlockHighlightColor(mutable);
               if (blockColor != null) {
                  this.pendingBlockHighlightColors.put(mutable.toImmutable(), blockColor);
               }
            }
         }

         if (this.scanCursor >= horizontalColumnCount) {
            this.blockHighlightColors = this.pendingBlockHighlightColors;
            this.scanCursor = -1;
         }
      }
   }
// https://t.me/pasterplace

   private RgbaColor getBlockHighlightColor(BlockPos position) {
      BlockState blockState = CLIENT.world.getBlockState(position);
      if (blockState.isAir()) {
         return this.blockSettings.isOptionEnabled("Безопасные ямы") ? this.getSafeHoleColor(position) : null;
      }

      Block block = blockState.getBlock();
      if (!this.blockSettings.isOptionEnabled("Алмазы") || block != Blocks.DIAMOND_ORE && block != Blocks.DEEPSLATE_DIAMOND_ORE) {
         if (this.blockSettings.isOptionEnabled("Древние обломки") && block == Blocks.ANCIENT_DEBRIS) {
            return RgbaColor.rgb(170, 110, 80);
         }

         if (!this.blockSettings.isOptionEnabled("Изумруды") || block != Blocks.EMERALD_ORE && block != Blocks.DEEPSLATE_EMERALD_ORE) {
            if (!this.blockSettings.isOptionEnabled("Золото") || block != Blocks.GOLD_ORE && block != Blocks.DEEPSLATE_GOLD_ORE && block != Blocks.NETHER_GOLD_ORE) {
               if (!this.blockSettings.isOptionEnabled("Железо") || block != Blocks.IRON_ORE && block != Blocks.DEEPSLATE_IRON_ORE) {
                  if (!this.blockSettings.isOptionEnabled("Порталы") || block != Blocks.NETHER_PORTAL && block != Blocks.END_PORTAL_FRAME && block != Blocks.END_PORTAL) {
                     if (this.blockSettings.isOptionEnabled("Динамит") && block == Blocks.TNT) {
                        return RgbaColor.rgb(255, 70, 70);
                     } else if (this.blockSettings.isOptionEnabled("Паутина") && block == Blocks.COBWEB) {
                        return RgbaColor.rgb(235, 235, 235);
                     } else {
                        return this.blockSettings.isOptionEnabled("Якоря") && block == Blocks.RESPAWN_ANCHOR ? RgbaColor.rgb(255, 120, 40) : null;
                     }
                  } else {
                     return RgbaColor.rgb(170, 80, 255);
                  }
               } else {
                  return RgbaColor.rgb(230, 190, 160);
               }
            } else {
               return RgbaColor.rgb(255, 215, 70);
            }
         } else {
            return RgbaColor.rgb(60, 255, 120);
         }
      } else {
         return RgbaColor.rgb(90, 230, 255);
      }
   }
// https://t.me/pasterplace

   private RgbaColor getSafeHoleColor(BlockPos position) {
      if (CLIENT.world.getBlockState(position.up()).isAir()
         && CLIENT.world.getBlockState(position.up(2)).isAir()) {
         int allAdjacentBlocksAreBedrock = 1;
         BlockState blockState = CLIENT.world.getBlockState(position.down());
         if (!this.isSafeHoleMaterial(blockState)) {
            return null;
         }

         allAdjacentBlocksAreBedrock = 1 & (blockState.getBlock() == Blocks.BEDROCK ? 1 : 0);

         for (Object horizontalDirection : Type.HORIZONTAL) {
            BlockState blockState2 = CLIENT.world.getBlockState(position.offset((Direction)horizontalDirection));
            if (!this.isSafeHoleMaterial(blockState2)) {
               return null;
            }

            allAdjacentBlocksAreBedrock &= blockState2.getBlock() == Blocks.BEDROCK ? 1 : 0;
         }

         return allAdjacentBlocksAreBedrock != 0 ? RgbaColor.rgb(80, 255, 140) : RgbaColor.rgb(255, 200, 70);
      } else {
         return null;
      }
   }
// https://t.me/pasterplace

   private boolean isSafeHoleMaterial(BlockState blockState) {
      Block block = blockState.getBlock();
      return block == Blocks.BEDROCK
         || block == Blocks.OBSIDIAN
         || block == Blocks.CRYING_OBSIDIAN
         || block == Blocks.RESPAWN_ANCHOR
         || block == Blocks.ENDER_CHEST
         || block == Blocks.NETHERITE_BLOCK;
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onRender3D(EventRender3D eventRender3D) {
      if (CLIENT.world != null) {
         this.updateStorageHighlights();
         if (!this.blockHighlightColors.isEmpty()) {
            boolean throughWalls = this.renderThroughWalls.getValue();
            float pulsePhase = (float)(Math.sin(AnimatedColorUtils.animationTime() * 3.0F) * 0.5 + 0.5);

            for (Map.Entry<BlockPos, RgbaColor> highlightedBlockEntry : this.blockHighlightColors.entrySet()) {
               BlockPos key = highlightedBlockEntry.getKey();
               RgbaColor value = highlightedBlockEntry.getValue();
               if (!CLIENT.world.getBlockState((BlockPos)key).isAir()) {
                  Box box3 = new Box((BlockPos)key);
                  if (this.filledBoxes.getValue()) {
                     DaamkyWorldRender.drawFilledBox(box3, value.withAlpha((int)(30.0F + 30.0F * pulsePhase)), throughWalls);
                  }

                  DaamkyWorldRender.drawBoxOutline(box3, value.withAlpha(220), this.outlineThickness.getValue(), throughWalls);
               } else {
                  Box box = new Box(
                     key.getX(), key.getY(), key.getZ(), key.getX() + 1, key.getY() + 0.02, key.getZ() + 1
                  );
                  Box box2 = new Box(
                     key.getX(), key.getY(), key.getZ(), key.getX() + 1, key.getY() + 1, key.getZ() + 1
                  );
                  DaamkyWorldRender.drawGradientBox(
                     box2, value.withAlpha((int)(50.0F + 30.0F * pulsePhase)), value.withAlpha(0), throughWalls
                  );
                  DaamkyWorldRender.drawBoxOutline(box, value.withAlpha(230), this.outlineThickness.getValue(), throughWalls);
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private void updateStorageHighlights() {
      if (CLIENT.player != null && this.storageSettings.getEnabledCount() != 0) {
         double storageRangeSquared = this.storageRange.getValue() * this.storageRange.getValue();
         int chunkRadius = (int)Math.ceil(this.storageRange.getValue() / 16.0F);
         int playerChunkX = CLIENT.player.getBlockPos().getX() >> 4;
         int playerChunkZ = CLIENT.player.getBlockPos().getZ() >> 4;
         float pulsePhase = (float)(Math.sin(AnimatedColorUtils.animationTime() * 3.0F) * 0.5 + 0.5);
// https://t.me/pasterplace

         for (int chunkX = playerChunkX - chunkRadius; chunkX <= playerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = playerChunkZ - chunkRadius; chunkZ <= playerChunkZ + chunkRadius; chunkZ++) {
               if (CLIENT.world.isChunkLoaded(chunkX, chunkZ)) {
                  WorldChunk worldChunk = CLIENT.world.getChunk(chunkX, chunkZ);

                  for (BlockEntity blockEntity : worldChunk.getBlockEntities().values()) {
                     RgbaColor storageColor = this.getBlockEntityHighlightColor(blockEntity);
                     if (storageColor != null) {
                        BlockPos storageBlockPos = blockEntity.getPos();
                        if (!(
                           CLIENT.player
                                 .squaredDistanceTo(storageBlockPos.getX() + 0.5, storageBlockPos.getY() + 0.5, storageBlockPos.getZ() + 0.5)
                              > storageRangeSquared
                        )) {
                           this.renderStorageHighlight(this.getStorageBounds(storageBlockPos), storageColor, pulsePhase);
                        }
                     }
                  }
               }
            }
         }

         if (this.storageSettings.isOptionEnabled("Вагонетки")) {
            for (Entity chestMinecart : CLIENT.world.getEntities()) {
               if (chestMinecart instanceof ChestMinecartEntity && chestMinecart.squaredDistanceTo(CLIENT.player) <= storageRangeSquared) {
                  this.renderStorageHighlight(
                     DaamkyWorldRender.getInterpolatedBoundingBox((Entity)chestMinecart), this.applyStorageColorMode(RgbaColor.rgb(255, 200, 100)), pulsePhase
                  );
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private Box getStorageBounds(BlockPos position) {
      VoxelShape voxelShape = CLIENT.world.getBlockState(position).getOutlineShape(CLIENT.world, position);
      return voxelShape.isEmpty() ? new Box(position) : voxelShape.getBoundingBox().offset(position);
   }
// https://t.me/pasterplace

   private void renderStorageHighlight(Box box, RgbaColor color, float pulsePhase) {
      boolean throughWalls = this.renderThroughWalls.getValue();
      if (this.filledBoxes.getValue()) {
         DaamkyWorldRender.drawGradientBox(box, color.withAlpha((int)(25.0F + 30.0F * pulsePhase)), color.withAlpha(60), throughWalls);
      }

      DaamkyWorldRender.drawBoxOutline(box, color.withAlpha(220), this.outlineThickness.getValue(), throughWalls);
      if (this.storageTracers.getValue()) {
         DaamkyWorldRender.drawCameraToPointGradientLine(box.getCenter(), color.withAlpha(30), color.withAlpha(200), 1.2F);
      }
   }
// https://t.me/pasterplace

   private RgbaColor applyStorageColorMode(RgbaColor inputColor) {
      return this.storageColorMode.isSelected("Тема") ? AnimatedColorUtils.getAnimatedAccentColor(0.0F, 255) : inputColor;
   }
// https://t.me/pasterplace

   private RgbaColor getBlockEntityHighlightColor(BlockEntity blockEntity) {
      if (blockEntity instanceof TrappedChestBlockEntity) {
         return this.storageSettings.isOptionEnabled("Ловушки") ? this.applyStorageColorMode(RgbaColor.rgb(255, 90, 60)) : null;
      } else if (blockEntity instanceof ChestBlockEntity) {
         return this.storageSettings.isOptionEnabled("Сундуки") ? this.applyStorageColorMode(RgbaColor.rgb(255, 170, 60)) : null;
      } else if (blockEntity instanceof EnderChestBlockEntity) {
         return this.storageSettings.isOptionEnabled("Эндер-сундуки") ? this.applyStorageColorMode(RgbaColor.rgb(140, 70, 255)) : null;
      } else if (blockEntity instanceof ShulkerBoxBlockEntity) {
         return this.storageSettings.isOptionEnabled("Шалкеры") ? this.applyStorageColorMode(RgbaColor.rgb(200, 90, 255)) : null;
      } else if (blockEntity instanceof BarrelBlockEntity) {
         return this.storageSettings.isOptionEnabled("Бочки") ? this.applyStorageColorMode(RgbaColor.rgb(200, 140, 80)) : null;
      } else if (blockEntity instanceof MobSpawnerBlockEntity) {
         return this.storageSettings.isOptionEnabled("Спавнеры") ? this.applyStorageColorMode(RgbaColor.rgb(255, 60, 120)) : null;
      } else if (blockEntity instanceof AbstractFurnaceBlockEntity) {
         return this.storageSettings.isOptionEnabled("Печи") ? this.applyStorageColorMode(RgbaColor.rgb(150, 150, 150)) : null;
      } else if (blockEntity instanceof HopperBlockEntity) {
         return this.storageSettings.isOptionEnabled("Воронки") ? this.applyStorageColorMode(RgbaColor.rgb(110, 110, 120)) : null;
      } else if (blockEntity instanceof DispenserBlockEntity) {
         return this.storageSettings.isOptionEnabled("Раздатчики") ? this.applyStorageColorMode(RgbaColor.rgb(120, 120, 120)) : null;
      } else {
         return null;
      }
   }
}

