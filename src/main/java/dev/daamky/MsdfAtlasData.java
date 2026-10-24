package dev.daamky;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import org.jspecify.annotations.Nullable;

record MsdfAtlasData(MsdfAtlasMetadata metadata, MsdfFontMetrics metrics, Int2ObjectMap<GlyphMetrics> glyphsByCodePoint, Long2FloatMap kerningAdjustments, @Nullable GlyphMetrics replacementGlyph) {

   MsdfAtlasData(MsdfAtlasMetadata metadata, MsdfFontMetrics metrics, Int2ObjectMap<GlyphMetrics> glyphsByCodePoint, Long2FloatMap kerningAdjustments, @Nullable GlyphMetrics replacementGlyph) {
      this.metadata = metadata;
      this.metrics = metrics;
      this.glyphsByCodePoint = glyphsByCodePoint;
      this.kerningAdjustments = kerningAdjustments;
      this.replacementGlyph = replacementGlyph;
   }
}

