package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventRender3D;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class BlockOutline extends ModuleStructure {
   private final MultiOptionSetting renderMode = new MultiOptionSetting("Режим", "Полный", "Полный", "Контур", "Волна", "Углы");
   private final BooleanSetting smoothMovement = new BooleanSetting("Плавное движение", true);
   private final BooleanSetting renderThroughWalls = new BooleanSetting("Сквозь стены", false);
   private final FloatSetting lineWidth = new FloatSetting("Толщина", 2.0F, 0.5F, 5.0F, 0.1F);
   private final FloatSetting fillOpacity = new FloatSetting("Прозрачность заливки", 50.0F, 10.0F, 160.0F, 5.0F);
   private final FloatSetting animationSpeed = new FloatSetting("Скорость анимации", 1.0F, 0.3F, 3.0F, 0.1F);
   private final BooleanSetting glowEnabled = new BooleanSetting("Glow", false);
   private final FloatSetting glowRadius = new FloatSetting("Радиус свечения", 2.0F, 0.5F, 5.0F, 0.1F);
   private final FloatSetting glowStrength = new FloatSetting("Сила свечения", 1.5F, 0.2F, 4.0F, 0.1F);
   private double animatedMinX;
   private double animatedMinY;
   private double animatedMinZ;
   private double animatedMaxX;
   private double animatedMaxY;
   private double animatedMaxZ;
   private float visibilityAlpha;
   private boolean hasAnimatedBounds;

   public BlockOutline() {
      super("BlockOutline", "Красивое выделение блока под прицелом", ModuleCategory.RENDER);
      this.fillOpacity.visibleWhen(() -> !this.renderMode.isSelected("Контур") && !this.renderMode.isSelected("Углы"));
      this.glowRadius.visibleWhen(this.glowEnabled::getValue);
      this.glowStrength.visibleWhen(this.glowEnabled::getValue);
      this.addSettings(
         this.renderMode,
         this.smoothMovement,
         this.renderThroughWalls,
         this.lineWidth,
         this.fillOpacity,
         this.animationSpeed,
         this.glowEnabled,
         this.glowRadius,
         this.glowStrength
      );
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.hasAnimatedBounds = false;
      this.visibilityAlpha = 0.0F;
   }

   @EventTarget
   public void onRender3D(EventRender3D eventRender3D) {
      if (CLIENT.world != null && CLIENT.player != null) {
         float deltaSeconds = eventRender3D.delta();
         Box targetBlockBox = null;
         HitResult crosshairHit = CLIENT.crosshairTarget;
         if (crosshairHit instanceof BlockHitResult) {
             BlockHitResult blockHit = (BlockHitResult)crosshairHit;
            if (crosshairHit.getType() == Type.BLOCK) {
               BlockPos targetBlockPosition = blockHit.getBlockPos();
               BlockState targetBlockState = CLIENT.world.getBlockState(targetBlockPosition);
               if (!targetBlockState.isAir() && CLIENT.world.getWorldBorder().contains(targetBlockPosition)) {
                  VoxelShape targetOutlineShape = targetBlockState.getOutlineShape(CLIENT.world, targetBlockPosition);
                  if (!targetOutlineShape.isEmpty()) {
                     targetBlockBox = targetOutlineShape.getBoundingBox().offset(targetBlockPosition);
                  }
               }
            }
         }

         float targetVisibility = targetBlockBox != null ? 1.0F : 0.0F;
         this.visibilityAlpha = this.visibilityAlpha + (targetVisibility - this.visibilityAlpha) * (1.0F - (float)Math.exp(-10.0F * deltaSeconds));
         if (targetBlockBox != null) {
            if (this.hasAnimatedBounds && this.smoothMovement.getValue()) {
               float smoothingFactor = 1.0F - (float)Math.exp(-18.0F * this.animationSpeed.getValue() * deltaSeconds);
               this.animatedMinX = this.animatedMinX + (targetBlockBox.minX - this.animatedMinX) * smoothingFactor;
               this.animatedMinY = this.animatedMinY + (targetBlockBox.minY - this.animatedMinY) * smoothingFactor;
               this.animatedMinZ = this.animatedMinZ + (targetBlockBox.minZ - this.animatedMinZ) * smoothingFactor;
               this.animatedMaxX = this.animatedMaxX + (targetBlockBox.maxX - this.animatedMaxX) * smoothingFactor;
               this.animatedMaxY = this.animatedMaxY + (targetBlockBox.maxY - this.animatedMaxY) * smoothingFactor;
               this.animatedMaxZ = this.animatedMaxZ + (targetBlockBox.maxZ - this.animatedMaxZ) * smoothingFactor;
            } else {
               this.updateAnimatedBounds(targetBlockBox);
               this.hasAnimatedBounds = true;
            }
         }

         if (!(this.visibilityAlpha < 0.01F) && this.hasAnimatedBounds) {
            Box animatedOutlineBounds = new Box(
                  this.animatedMinX,
                  this.animatedMinY,
                  this.animatedMinZ,
                  this.animatedMaxX,
                  this.animatedMaxY,
                  this.animatedMaxZ
               )
               .expand(0.002);
            this.renderBlockOutline(animatedOutlineBounds);
            if (this.glowEnabled.getValue()) {
               DaamkyWorldRender.beginTexturedQuadBuffer(this.glowRadius.getValue(), this.glowStrength.getValue(), this.renderThroughWalls.getValue());
               this.renderBlockOutline(animatedOutlineBounds);
               DaamkyWorldRender.flushTexturedQuadBuffer();
            }
         }
      }
   }

   private void updateAnimatedBounds(Box targetBounds) {
      this.animatedMinX = targetBounds.minX;
      this.animatedMinY = targetBounds.minY;
      this.animatedMinZ = targetBounds.minZ;
      this.animatedMaxX = targetBounds.maxX;
      this.animatedMaxY = targetBounds.maxY;
      this.animatedMaxZ = targetBounds.maxZ;
   }

   private void renderBlockOutline(Box outlineBounds) {
      boolean renderThroughWalls = this.renderThroughWalls.getValue();
      float lineWidth = this.lineWidth.getValue();
      RgbaColor accentColor = AnimatedColorUtils.getAnimatedAccentColor(0.0F, AnimatedColorUtils.scaleAlpha(240.0F, this.visibilityAlpha));
      RgbaColor themeColor = AnimatedColorUtils.getAnimatedThemeColor(1.5F, AnimatedColorUtils.scaleAlpha(240.0F, this.visibilityAlpha));
      int fillAlpha = AnimatedColorUtils.scaleAlpha(this.fillOpacity.getValue(), this.visibilityAlpha);
      switch ((String)this.renderMode.getValue()) {
         case "Контур":
            DaamkyWorldRender.drawBoxOutline(outlineBounds, accentColor, lineWidth, renderThroughWalls);
            break;
         case "Волна":
            float waveProgress = (float)(AnimatedColorUtils.animationTime() * 0.8F * this.animationSpeed.getValue() % 1.0);
            double waveHeight = outlineBounds.minY + (outlineBounds.maxY - outlineBounds.minY) * waveProgress;
            Box waveBounds = new Box(outlineBounds.minX, outlineBounds.minY, outlineBounds.minZ, outlineBounds.maxX, waveHeight, outlineBounds.maxZ);
            DaamkyWorldRender.drawGradientBox(waveBounds, themeColor.withAlpha(fillAlpha / 3), accentColor.withAlpha(fillAlpha), renderThroughWalls);
            DaamkyWorldRender.drawBoxOutline(outlineBounds, accentColor, lineWidth, renderThroughWalls);
            DaamkyWorldRender.drawLine(
               outlineBounds.minX, waveHeight, outlineBounds.minZ, outlineBounds.maxX, waveHeight, outlineBounds.minZ, accentColor.toArgb(), accentColor.toArgb(), lineWidth, renderThroughWalls
            );
            DaamkyWorldRender.drawLine(
               outlineBounds.maxX, waveHeight, outlineBounds.minZ, outlineBounds.maxX, waveHeight, outlineBounds.maxZ, accentColor.toArgb(), accentColor.toArgb(), lineWidth, renderThroughWalls
            );
            DaamkyWorldRender.drawLine(
               outlineBounds.maxX, waveHeight, outlineBounds.maxZ, outlineBounds.minX, waveHeight, outlineBounds.maxZ, accentColor.toArgb(), accentColor.toArgb(), lineWidth, renderThroughWalls
            );
            DaamkyWorldRender.drawLine(
               outlineBounds.minX, waveHeight, outlineBounds.maxZ, outlineBounds.minX, waveHeight, outlineBounds.minZ, accentColor.toArgb(), accentColor.toArgb(), lineWidth, renderThroughWalls
            );
            break;
         case "Углы":
            this.drawCornerMarkers(outlineBounds, accentColor, themeColor, lineWidth, renderThroughWalls);
            break;
         default:
            DaamkyWorldRender.drawGradientBox(outlineBounds, themeColor.withAlpha(fillAlpha / 2), accentColor.withAlpha(fillAlpha), renderThroughWalls);
            DaamkyWorldRender.drawBoxOutline(outlineBounds, accentColor, lineWidth, renderThroughWalls);
      }
   }

   private void drawCornerMarkers(Box outlineBounds, RgbaColor accentColor, RgbaColor themeColor, float lineWidth, boolean renderThroughWalls) {
      double cornerWidth = (outlineBounds.maxX - outlineBounds.minX) * 0.25;
      double cornerHeight = (outlineBounds.maxY - outlineBounds.minY) * 0.25;
      double cornerDepth = (outlineBounds.maxZ - outlineBounds.minZ) * 0.25;
      double[] xCoordinates = new double[]{outlineBounds.minX, outlineBounds.maxX};
      double[] yCoordinates = new double[]{outlineBounds.minY, outlineBounds.maxY};
      double[] zCoordinates = new double[]{outlineBounds.minZ, outlineBounds.maxZ};

      for (int xSideIndex = 0; xSideIndex < 2; xSideIndex++) {
         for (int ySideIndex = 0; ySideIndex < 2; ySideIndex++) {
            for (int zSideIndex = 0; zSideIndex < 2; zSideIndex++) {
               double x = xCoordinates[xSideIndex];
               double y = yCoordinates[ySideIndex];
               double z = zCoordinates[zSideIndex];
               int cornerColor = (ySideIndex == 1 ? accentColor : themeColor).toArgb();
               DaamkyWorldRender.drawLine(x, y, z, x + (xSideIndex == 0 ? cornerWidth : -cornerWidth), y, z, cornerColor, cornerColor, lineWidth, renderThroughWalls);
               DaamkyWorldRender.drawLine(x, y, z, x, y + (ySideIndex == 0 ? cornerHeight : -cornerHeight), z, cornerColor, cornerColor, lineWidth, renderThroughWalls);
               DaamkyWorldRender.drawLine(x, y, z, x, y, z + (zSideIndex == 0 ? cornerDepth : -cornerDepth), cornerColor, cornerColor, lineWidth, renderThroughWalls);
            }
         }
      }
   }
}

