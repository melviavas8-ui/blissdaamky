package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

public final class GlyphRenderState implements SimpleGuiElementRenderState {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float GLYPH_BOUNDS_PADDING = 2.0F;
   private final GlyphAtlasEntry glyphEntry;
   private final MsdfFont font;
   private final Matrix3x2f matrices;
   private final float x;
   private final float y;
   private final float fontSize;
   private final int baseColor;
   private final @Nullable GlyphColorProvider colorProvider;
   private final float alpha;
   private final int distanceRangeLight;
   private final @Nullable ScreenRect scissorArea;
   private final ScreenRect transformedBounds;
   private @Nullable TextureSetup textureSetup;
// https://t.me/pasterplace

   GlyphRenderState(
      GlyphAtlasEntry glyphEntry,
      Matrix3x2f matrices,
      float x,
      float y,
      float fontSize,
      int baseColor,
      @Nullable GlyphColorProvider colorProvider,
      float alpha,
      @Nullable ScreenRect scissorArea
   ) {
      this.glyphEntry = glyphEntry;
      this.font = glyphEntry.font();
      this.matrices = matrices;
      this.x = x;
      this.y = y;
      this.fontSize = fontSize;
      this.baseColor = baseColor;
      this.colorProvider = colorProvider;
      this.alpha = alpha;
      this.distanceRangeLight = Math.clamp(Math.round(this.font.metadata().distanceRange() * 2.0F), 1, 255);
      this.scissorArea = scissorArea;
      this.transformedBounds = computeBounds(
         matrices, x - 2.0F, y - 2.0F, glyphEntry.advanceWidth() * fontSize + 4.0F, glyphEntry.lineHeight() * fontSize + 4.0F, scissorArea
      );
   }
// https://t.me/pasterplace

   public void setupVertices(VertexConsumer vertices) {
      float[] glyphQuadData = this.glyphEntry.glyphVertexData();
      int[] glyphTextIndices = this.glyphEntry.visibleGlyphIndices();
      float inverseAtlasWidth = this.glyphEntry.advanceWidth() > 0.0F ? 1.0F / this.glyphEntry.advanceWidth() : 0.0F;
      float inverseAtlasHeight = this.glyphEntry.lineHeight() > 0.0F ? 1.0F / this.glyphEntry.lineHeight() : 0.0F;

      for (int glyphIndex = 0; glyphIndex < this.glyphEntry.glyphCount(); glyphIndex++) {
         int glyphDataOffset = glyphIndex * 8;
         float leftX = glyphQuadData[glyphDataOffset];
         float topY = glyphQuadData[glyphDataOffset + 1];
         float rightX = glyphQuadData[glyphDataOffset + 2];
         float bottomY = glyphQuadData[glyphDataOffset + 3];
         float atlasLeftU = glyphQuadData[glyphDataOffset + 4];
         float atlasTopV = glyphQuadData[glyphDataOffset + 5];
         float atlasRightU = glyphQuadData[glyphDataOffset + 6];
         float atlasBottomV = glyphQuadData[glyphDataOffset + 7];
         int glyphTextIndex = glyphTextIndices[glyphIndex];
         this.emitVertex(vertices, leftX, topY, atlasLeftU, atlasTopV, this.resolveVertexColor(glyphTextIndex, leftX * inverseAtlasWidth, topY * inverseAtlasHeight));
         this.emitVertex(vertices, leftX, bottomY, atlasLeftU, atlasBottomV, this.resolveVertexColor(glyphTextIndex, leftX * inverseAtlasWidth, bottomY * inverseAtlasHeight));
         this.emitVertex(vertices, rightX, bottomY, atlasRightU, atlasBottomV, this.resolveVertexColor(glyphTextIndex, rightX * inverseAtlasWidth, bottomY * inverseAtlasHeight));
         this.emitVertex(vertices, rightX, topY, atlasRightU, atlasTopV, this.resolveVertexColor(glyphTextIndex, rightX * inverseAtlasWidth, topY * inverseAtlasHeight));
      }
   }
// https://t.me/pasterplace

   private int resolveVertexColor(int glyphTextIndex, float textureU, float textureV) {
      return this.colorProvider == null
         ? ColorUtils.scaleAlpha(this.baseColor, this.alpha)
         : ColorUtils.scaleAlpha(this.colorProvider.color(glyphTextIndex, Math.clamp(textureU, 0.0F, 1.0F), Math.clamp(textureV, 0.0F, 1.0F)), this.alpha);
   }
// https://t.me/pasterplace

   private void emitVertex(VertexConsumer vertices, float x, float y, float textureU, float textureV, int argbColor) {
      vertices.vertex(this.matrices, this.x + x * this.fontSize, this.y + y * this.fontSize)
         .texture(textureU, textureV)
         .light(this.distanceRangeLight, 0)
         .color(argbColor);
   }
// https://t.me/pasterplace

   private static ScreenRect computeBounds(Matrix3x2f matrices, float x, float y, float width, float height, @Nullable ScreenRect scissorArea) {
      Vector2f transformedCorner = new Vector2f();
      float minX = Float.MAX_VALUE;
      float minY = Float.MAX_VALUE;
      float maxX = -Float.MAX_VALUE;
      float maxY = -Float.MAX_VALUE;

      for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
         matrices.transformPosition(x + (cornerIndex & 1) * width, y + (cornerIndex >> 1) * height, transformedCorner);
         minX = Math.min(minX, transformedCorner.x);
         minY = Math.min(minY, transformedCorner.y);
         maxX = Math.max(maxX, transformedCorner.x);
         maxY = Math.max(maxY, transformedCorner.y);
      }
// https://t.me/pasterplace

      ScreenRect transformedBounds = new ScreenRect((int)Math.floor(minX), (int)Math.floor(minY), (int)Math.ceil(maxX - minX) + 1, (int)Math.ceil(maxY - minY) + 1);
      ScreenRect clippedBounds = scissorArea != null ? transformedBounds.intersection(scissorArea) : transformedBounds;
      return clippedBounds != null ? clippedBounds : ScreenRect.empty();
   }
// https://t.me/pasterplace

   public RenderPipeline pipeline() {
      return TextRenderPipeline.II1Il11l111II11IIl;
   }
// https://t.me/pasterplace

   public TextureSetup textureSetup() {
      if (this.textureSetup == null) {
         GpuTextureView gpuTextureView = this.font.textureView();
         if (gpuTextureView == null) {
            return TextureSetup.empty();
         }

         this.textureSetup = TextureSetup.of(gpuTextureView, RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
      }

      return this.textureSetup;
   }
// https://t.me/pasterplace

   public @Nullable ScreenRect scissorArea() {
      return this.scissorArea;
   }
// https://t.me/pasterplace

   public @Nullable ScreenRect bounds() {
      return this.transformedBounds;
   }
}

