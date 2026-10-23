package dev.daamky;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public final class MsdfFont {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final int TAB_WIDTH_SPACES = 4;
   private final String fontName;
   private final Identifier atlasDataIdentifier;
   private final Identifier textureIdentifier;
   private @Nullable MsdfAtlasData cachedAtlasData;
// https://t.me/pasterplace

   MsdfFont(String fontName, Identifier atlasDataIdentifier, Identifier textureIdentifier) {
      this.fontName = fontName;
      this.atlasDataIdentifier = atlasDataIdentifier;
      this.textureIdentifier = textureIdentifier;
   }
// https://t.me/pasterplace

   public String name() {
      return this.fontName;
   }
// https://t.me/pasterplace

   public Identifier textureIdentifier() {
      return this.textureIdentifier;
   }
// https://t.me/pasterplace

   public MsdfAtlasMetadata metadata() {
      return this.atlasData().metadata();
   }
// https://t.me/pasterplace

   public MsdfFontMetrics metrics() {
      return this.atlasData().metrics();
   }
// https://t.me/pasterplace

   public @Nullable GpuTextureView textureView() {
      try {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client != null && client.getTextureManager() != null) {
            AbstractTexture abstractTexture = client.getTextureManager().getTexture(this.textureIdentifier);
            return abstractTexture != null ? abstractTexture.getGlTextureView() : null;
         } else {
            return null;
         }
      } catch (Throwable ignoredTextureLookupFailure) {
         return null;
      }
   }
// https://t.me/pasterplace

   public @Nullable GlyphMetrics glyphMetrics(int codePoint) {
      if (codePoint < 32) {
         return null;
      }

      MsdfAtlasData atlasData = this.atlasData();
      Object glyphValue = (GlyphMetrics)atlasData.glyphsByCodePoint().get(codePoint);
      return (GlyphMetrics)(glyphValue != null ? glyphValue : atlasData.replacementGlyph());
   }
// https://t.me/pasterplace

   public boolean hasGlyph(int codePoint) {
      return this.atlasData().glyphsByCodePoint().containsKey(codePoint);
   }
// https://t.me/pasterplace

   public float advanceWidth(int codePoint) {
      if (codePoint == 9) {
         return this.advanceWidth(32) * 4.0F;
      }
// https://t.me/pasterplace

      GlyphMetrics glyphMetrics = this.glyphMetrics(codePoint);
      return glyphMetrics != null ? glyphMetrics.advanceWidth() : 0.0F;
   }
// https://t.me/pasterplace

   public float kerning(int leftCodePoint, int rightCodePoint) {
      return this.atlasData().kerningAdjustments().get(Atlas.packCodePointPair(leftCodePoint, rightCodePoint));
   }
// https://t.me/pasterplace

   public float measureWidth(CharSequence textSequence, float scale) {
      return this.measureWidth(textSequence, scale, 0.0F);
   }
// https://t.me/pasterplace

   public float measureWidth(CharSequence textSequence, float scale, float letterSpacing) {
      float normalizedLetterSpacing = scale > 0.0F ? letterSpacing / scale : 0.0F;
      float normalizedTextWidth = 0.0F;
      int glyphCount = 0;
      int previousCodePoint = -1;
      int charIndex = 0;

      while (charIndex < textSequence.length()) {
         int codePoint = Character.codePointAt(textSequence, charIndex);
         charIndex += Character.charCount(codePoint);
         if (codePoint >= 32 || codePoint == 9) {
            if (previousCodePoint >= 0) {
               normalizedTextWidth += this.kerning(previousCodePoint, codePoint);
            }

            normalizedTextWidth += this.advanceWidth(codePoint) + normalizedLetterSpacing;
            previousCodePoint = codePoint;
            glyphCount++;
         }
      }

      return glyphCount > 0 ? (normalizedTextWidth - normalizedLetterSpacing) * scale : 0.0F;
   }
// https://t.me/pasterplace

   public float lineHeight(float scale) {
      return this.metrics().lineHeight() * scale;
   }
// https://t.me/pasterplace

   public float ascender(float scale) {
      return this.metrics().ascender() * scale;
   }
// https://t.me/pasterplace

   public float descender(float scale) {
      return -this.metrics().descender() * scale;
   }
// https://t.me/pasterplace

   public String truncateToWidth(String sourceText, float scale, float maxWidth) {
      if (this.measureWidth(sourceText, scale) <= maxWidth) {
         return sourceText;
      }

      int truncatedEndIndex = 0;
      int charIndex = 0;

      while (charIndex < sourceText.length()) {
         int nextCharIndex = charIndex + Character.charCount(sourceText.codePointAt(charIndex));
         if (this.measureWidth(sourceText.substring(0, nextCharIndex), scale) > maxWidth) {
            break;
         }

         truncatedEndIndex = nextCharIndex;
         charIndex = nextCharIndex;
      }

      return sourceText.substring(0, truncatedEndIndex);
   }
// https://t.me/pasterplace

   public GuiTextBuilder text(String content) {
      return GuiTextBuilder.create(this, content);
   }
// https://t.me/pasterplace

   void clearCachedAtlasData() {
      this.cachedAtlasData = null;
   }
// https://t.me/pasterplace

   private MsdfAtlasData atlasData() {
      MsdfAtlasData atlasData = this.cachedAtlasData;
      if (atlasData == null) {
         atlasData = Atlas.loadAtlasData(this.atlasDataIdentifier);
         this.cachedAtlasData = atlasData;
      }

      return atlasData;
   }
}

