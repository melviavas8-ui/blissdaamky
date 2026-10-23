package dev.daamky;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class GlyphAtlasEntry {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public static final int FLOATS_PER_GLYPH = 8;
   private static final int CACHE_CAPACITY = 1024;
   private static final Map<GlyphAtlasEntry.FontGlyphOffset, GlyphAtlasEntry> ENTRY_CACHE = new LinkedHashMap<GlyphAtlasEntry.FontGlyphOffset, GlyphAtlasEntry>(
      256, 0.75F, true
   ) {
      @Override
      protected boolean removeEldestEntry(Entry<GlyphAtlasEntry.FontGlyphOffset, GlyphAtlasEntry> eldestEntry) {
         return this.size() > 1024;
      }
   };
   private final MsdfFont font;
   private final float[] glyphVertexData;
   private final int[] visibleGlyphIndices;
   private final int glyphCount;
   private final float advanceWidth;
   private final float lineHeight;
   private final float ascender;
// https://t.me/pasterplace

   private GlyphAtlasEntry(MsdfFont font, float[] glyphVertexData, int[] visibleGlyphIndices, int glyphCount, float advanceWidth, float lineHeight, float ascender) {
      this.font = font;
      this.glyphVertexData = glyphVertexData;
      this.visibleGlyphIndices = visibleGlyphIndices;
      this.glyphCount = glyphCount;
      this.advanceWidth = advanceWidth;
      this.lineHeight = lineHeight;
      this.ascender = ascender;
   }
// https://t.me/pasterplace

   public static GlyphAtlasEntry getOrCreate(MsdfFont font, String glyphText, float fontScale) {
      return ENTRY_CACHE.computeIfAbsent(new GlyphAtlasEntry.FontGlyphOffset(font, glyphText, fontScale), GlyphAtlasEntry::build);
   }
// https://t.me/pasterplace

   public static void clearCache() {
      ENTRY_CACHE.clear();
   }
// https://t.me/pasterplace

   public MsdfFont font() {
      return this.font;
   }
// https://t.me/pasterplace

   public float[] glyphVertexData() {
      return this.glyphVertexData;
   }
// https://t.me/pasterplace

   public int[] visibleGlyphIndices() {
      return this.visibleGlyphIndices;
   }
// https://t.me/pasterplace

   public int glyphCount() {
      return this.glyphCount;
   }
// https://t.me/pasterplace

   public float advanceWidth() {
      return this.advanceWidth;
   }
// https://t.me/pasterplace

   public float lineHeight() {
      return this.lineHeight;
   }
// https://t.me/pasterplace

   public float ascender() {
      return this.ascender;
   }
// https://t.me/pasterplace

   private static GlyphAtlasEntry build(GlyphAtlasEntry.FontGlyphOffset cacheKey) {
      MsdfFont font = cacheKey.font();
      int[] codePoints = cacheKey.glyphText().codePoints().toArray();
      float fontScale = cacheKey.fontScale();
      float ascender = font.metrics().ascender();
      float[] glyphVertexData = new float[codePoints.length * 8];
      int[] visibleGlyphIndices = new int[codePoints.length];
      int visibleGlyphCount = 0;
      int advanceGlyphCount = 0;
      float totalAdvance = 0.0F;
      int previousCodePoint = -1;

      for (int sourceGlyphIndex = 0; sourceGlyphIndex < codePoints.length; sourceGlyphIndex++) {
         int codePoint = codePoints[sourceGlyphIndex];
         if (codePoint >= 32 || codePoint == 9) {
            if (previousCodePoint >= 0) {
               totalAdvance += font.kerning(previousCodePoint, codePoint);
            }

            GlyphMetrics glyphMetrics = font.glyphMetrics(codePoint);
            if (glyphMetrics != null && glyphMetrics.hasPlaneBounds()) {
               int vertexDataOffset = visibleGlyphCount * 8;
               glyphVertexData[vertexDataOffset] = totalAdvance + glyphMetrics.planeLeft();
               glyphVertexData[vertexDataOffset + 1] = ascender - glyphMetrics.planeTop();
               glyphVertexData[vertexDataOffset + 2] = totalAdvance + glyphMetrics.planeRight();
               glyphVertexData[vertexDataOffset + 3] = ascender - glyphMetrics.planeBottom();
               glyphVertexData[vertexDataOffset + 4] = glyphMetrics.atlasLeft();
               glyphVertexData[vertexDataOffset + 5] = glyphMetrics.atlasTop();
               glyphVertexData[vertexDataOffset + 6] = glyphMetrics.atlasRight();
               glyphVertexData[vertexDataOffset + 7] = glyphMetrics.atlasBottom();
               visibleGlyphIndices[visibleGlyphCount++] = sourceGlyphIndex;
            }

            totalAdvance += font.advanceWidth(codePoint) + fontScale;
            previousCodePoint = codePoint;
            advanceGlyphCount++;
         }
      }

      float textWidth = advanceGlyphCount > 0 ? totalAdvance - fontScale : 0.0F;
      return new GlyphAtlasEntry(
         font, Arrays.copyOf(glyphVertexData, visibleGlyphCount * 8), Arrays.copyOf(visibleGlyphIndices, visibleGlyphCount), visibleGlyphCount, textWidth, font.metrics().lineHeight(), ascender
      );
   }
// https://t.me/pasterplace

   private record FontGlyphOffset(MsdfFont font, String glyphText, float fontScale) {
// https://t.me/pasterplace

      private FontGlyphOffset(MsdfFont font, String glyphText, float fontScale) {
         this.font = font;
         this.glyphText = glyphText;
         this.fontScale = fontScale;
      }
   }
}

