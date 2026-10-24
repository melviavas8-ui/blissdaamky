package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

final class RoundedTextureRenderState implements SimpleGuiElementRenderState {
   private static final float EDGE_PADDING = 1.0F;
   private static final float MAX_OVERLAY_COORDINATE = 32767.0F;
   private final Matrix3x2f transformationMatrix;
   private final float x;
   private final float y;
   private final float width;
   private final float height;
   private final int blockLight;
   private final int skyLight;
   private final int color;
   private final Identifier textureId;
   private final float textureUStart;
   private final float textureVStart;
   private final float textureUWidth;
   private final float textureVHeight;
   private final @Nullable ScreenRect scissorArea;
   private final ScreenRect bounds;
   private @Nullable TextureSetup cachedTextureSetup;

   RoundedTextureRenderState(
      Matrix3x2f transformationMatrix,
      float x,
      float y,
      float width,
      float height,
      int blockLight,
      int skyLight,
      int color,
      Identifier textureId,
      float textureUStart,
      float textureVStart,
      float textureUWidth,
      float textureVHeight,
      @Nullable ScreenRect scissorArea
   ) {
      this.transformationMatrix = transformationMatrix;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.blockLight = blockLight;
      this.skyLight = skyLight;
      this.color = color;
      this.textureId = textureId;
      this.textureUStart = textureUStart;
      this.textureVStart = textureVStart;
      this.textureUWidth = textureUWidth;
      this.textureVHeight = textureVHeight;
      this.scissorArea = scissorArea;
      ScreenRect transformedBounds = new ScreenRect(
            (int)Math.floor(x - 1.0F), (int)Math.floor(y - 1.0F), (int)Math.ceil(width + 2.0F) + 1, (int)Math.ceil(height + 2.0F) + 1
         )
         .transform(transformationMatrix);
      ScreenRect clippedBounds = scissorArea != null ? transformedBounds.intersection(scissorArea) : transformedBounds;
      this.bounds = clippedBounds != null ? clippedBounds : ScreenRect.empty();
   }

   public void setupVertices(VertexConsumer vertices) {
      float uExtent = this.width * 0.5F + 1.0F;
      float vExtent = this.height * 0.5F + 1.0F;
      float left = this.x - 1.0F;
      float top = this.y - 1.0F;
      float right = this.x + this.width + 1.0F;
      float bottom = this.y + this.height + 1.0F;
      this.emitVertex(vertices, left, top, -uExtent, -vExtent);
      this.emitVertex(vertices, left, bottom, -uExtent, vExtent);
      this.emitVertex(vertices, right, bottom, uExtent, vExtent);
      this.emitVertex(vertices, right, top, uExtent, -vExtent);
   }

   private void emitVertex(VertexConsumer vertices, float x, float y, float u, float v) {
      int overlayU = Math.round(Math.clamp((x - this.textureUStart) / this.textureUWidth, 0.0F, 1.0F) * 32767.0F);
      int overlayV = Math.round(Math.clamp((y - this.textureVStart) / this.textureVHeight, 0.0F, 1.0F) * 32767.0F);
      vertices.vertex(this.transformationMatrix, x, y)
         .color(this.color)
         .texture(u, v)
         .overlay(overlayU, overlayV)
         .light(this.blockLight, this.skyLight)
         .normal(0.0F, 0.0F, 1.0F);
   }

   public RenderPipeline pipeline() {
      return RegionImageRenderPipeline.II1Il11l111II11IIl;
   }

   public TextureSetup textureSetup() {
      if (this.cachedTextureSetup == null) {
         GpuTextureView textureView = null;

         try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.getTextureManager() != null) {
               AbstractTexture abstractTexture = client.getTextureManager().getTexture(this.textureId);
               if (abstractTexture != null) {
                  textureView = abstractTexture.getGlTextureView();
               }
            }
         } catch (Throwable ignoredTextureLookupFailure) {
         }

         if (textureView == null) {
            return TextureSetup.empty();
         }

         this.cachedTextureSetup = TextureSetup.of(textureView, RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
      }

      return this.cachedTextureSetup;
   }

   public @Nullable ScreenRect scissorArea() {
      return this.scissorArea;
   }

   public @Nullable ScreenRect bounds() {
      return this.bounds;
   }
}

