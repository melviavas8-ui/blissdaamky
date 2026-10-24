package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public final class RoundedOutlineRenderState implements SimpleGuiElementRenderState {
   private final Matrix3x2f matrices;
   private final float x;
   private final float y;
   private final float width;
   private final float height;
   private final float outlinePadding;
   private final int packedTopCornerData;
   private final int packedBottomCornerData;
   private final int topLeftColor;
   private final int topRightColor;
   private final int bottomRightColor;
   private final int bottomLeftColor;
   private final @Nullable ScreenRect scissorArea;
   private final ScreenRect transformedBounds;

   RoundedOutlineRenderState(
      Matrix3x2f matrices,
      float x,
      float y,
      float width,
      float height,
      float outlineRadius,
      int packedTopCornerData,
      int packedBottomCornerData,
      int topLeftColor,
      int topRightColor,
      int bottomRightColor,
      int bottomLeftColor,
      @Nullable ScreenRect scissorArea
   ) {
      this.matrices = matrices;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.outlinePadding = outlineRadius + 1.0F;
      this.packedTopCornerData = packedTopCornerData;
      this.packedBottomCornerData = packedBottomCornerData;
      this.topLeftColor = topLeftColor;
      this.topRightColor = topRightColor;
      this.bottomRightColor = bottomRightColor;
      this.bottomLeftColor = bottomLeftColor;
      this.scissorArea = scissorArea;
      ScreenRect transformedBounds = new ScreenRect(
            (int)Math.floor(x - this.outlinePadding),
            (int)Math.floor(y - this.outlinePadding),
            (int)Math.ceil(width + this.outlinePadding * 2.0F) + 1,
            (int)Math.ceil(height + this.outlinePadding * 2.0F) + 1
         )
         .transform(matrices);
      ScreenRect clippedBounds = scissorArea != null ? transformedBounds.intersection(scissorArea) : transformedBounds;
      this.transformedBounds = clippedBounds != null ? clippedBounds : ScreenRect.empty();
   }

   public void setupVertices(VertexConsumer vertices) {
      float halfWidthUvRadius = this.width * 0.5F + this.outlinePadding;
      float halfHeightUvRadius = this.height * 0.5F + this.outlinePadding;
      float leftX = this.x - this.outlinePadding;
      float topY = this.y - this.outlinePadding;
      float rightX = this.x + this.width + this.outlinePadding;
      float bottomY = this.y + this.height + this.outlinePadding;
      this.emitVertex(vertices, leftX, topY, -halfWidthUvRadius, -halfHeightUvRadius, this.topLeftColor);
      this.emitVertex(vertices, leftX, bottomY, -halfWidthUvRadius, halfHeightUvRadius, this.bottomLeftColor);
      this.emitVertex(vertices, rightX, bottomY, halfWidthUvRadius, halfHeightUvRadius, this.bottomRightColor);
      this.emitVertex(vertices, rightX, topY, halfWidthUvRadius, -halfHeightUvRadius, this.topRightColor);
   }

   private void emitVertex(VertexConsumer vertices, float x, float y, float u, float v, int argbColor) {
      vertices.vertex(this.matrices, x, y)
         .texture(u, v)
         .light(this.packedTopCornerData, this.packedBottomCornerData)
         .color(argbColor);
   }

   public RenderPipeline pipeline() {
      return ShadowRenderPipeline.l1I1IlllIlI;
   }

   public TextureSetup textureSetup() {
      return TextureSetup.empty();
   }

   public @Nullable ScreenRect scissorArea() {
      return this.scissorArea;
   }

   public @Nullable ScreenRect bounds() {
      return this.transformedBounds;
   }
}

