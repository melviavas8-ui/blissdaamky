package dev.daamky;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import org.jspecify.annotations.Nullable;

record MsdfAtlasData(MsdfAtlasMetadata metadata, MsdfFontMetrics metrics, Int2ObjectMap<GlyphMetrics> glyphsByCodePoint, Long2FloatMap kerningAdjustments, @Nullable GlyphMetrics replacementGlyph) {
    private static final String p54e94351 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   MsdfAtlasData(MsdfAtlasMetadata metadata, MsdfFontMetrics metrics, Int2ObjectMap<GlyphMetrics> glyphsByCodePoint, Long2FloatMap kerningAdjustments, @Nullable GlyphMetrics replacementGlyph) {
      this.metadata = metadata;
      this.metrics = metrics;
      this.glyphsByCodePoint = glyphsByCodePoint;
      this.kerningAdjustments = kerningAdjustments;
      this.replacementGlyph = replacementGlyph;
   }
}

