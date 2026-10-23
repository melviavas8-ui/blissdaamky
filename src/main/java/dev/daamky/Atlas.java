package dev.daamky;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

final class Atlas {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private Atlas() {
   }
// https://t.me/pasterplace

   static MsdfAtlasData loadAtlasData(Identifier atlasDataIdentifier) {
      try (BufferedReader bufferedReader = MinecraftClient.getInstance().getResourceManager().getResourceOrThrow(atlasDataIdentifier).getReader()) {
         return parseAtlasData(JsonParser.parseReader(bufferedReader).getAsJsonObject());
      } catch (IOException ioException) {
         throw new UncheckedIOException("Failed to load font " + atlasDataIdentifier, ioException);
      }
   }
// https://t.me/pasterplace

   static long packCodePointPair(int leftCodePoint, int rightCodePoint) {
      return (long)leftCodePoint << 32 | rightCodePoint & 4294967295L;
   }
// https://t.me/pasterplace

   private static MsdfAtlasData parseAtlasData(JsonObject fontJson) {
      JsonObject atlasJson = fontJson.getAsJsonObject("atlas");
      String atlasType = atlasJson.has("type") ? atlasJson.get("type").getAsString() : "msdf";
      boolean topOrigin = atlasJson.has("yOrigin") && atlasJson.get("yOrigin").getAsString().equalsIgnoreCase("top");
      float distanceRange = atlasJson.has("distanceRange")
         ? getFloatProperty(atlasJson, "distanceRange")
         : (
            atlasJson.has("pxRange")
               ? getFloatProperty(atlasJson, "pxRange")
               : (atlasJson.has("range") ? getFloatProperty(atlasJson, "range") : 4.0F)
         );
      float atlasSize = atlasJson.has("size")
         ? getFloatProperty(atlasJson, "size")
         : (atlasJson.has("glyphSize") ? getFloatProperty(atlasJson, "glyphSize") : 32.0F);
      MsdfAtlasMetadata atlasMetadata = new MsdfAtlasMetadata(
         atlasType, distanceRange, atlasSize, atlasJson.get("width").getAsInt(), atlasJson.get("height").getAsInt()
      );
      JsonObject fontMetricsJson = fontJson.getAsJsonObject("metrics");
      float emSize = fontMetricsJson.has("emSize") ? getFloatProperty(fontMetricsJson, "emSize") : 1.0F;
      MsdfFontMetrics fontMetrics = new MsdfFontMetrics(
         getFloatProperty(fontMetricsJson, "lineHeight") / emSize,
         getFloatProperty(fontMetricsJson, "ascender") / emSize,
         getFloatProperty(fontMetricsJson, "descender") / emSize,
         fontMetricsJson.has("underlineY") ? getFloatProperty(fontMetricsJson, "underlineY") / emSize : 0.0F,
         fontMetricsJson.has("underlineThickness") ? getFloatProperty(fontMetricsJson, "underlineThickness") / emSize : 0.0F
      );
      float glyphScale = emSize;
      if (emSize > 10.0F && fontJson.has("glyphs")) {
         for (JsonElement probeGlyphElement : fontJson.getAsJsonArray("glyphs")) {
            JsonObject probeGlyphJson = probeGlyphElement.getAsJsonObject();
            if (probeGlyphJson.has("planeBounds")) {
               JsonObject planeBoundsJson = probeGlyphJson.getAsJsonObject("planeBounds");
               float planeBoundsWidth = Math.abs(getFloatProperty(planeBoundsJson, "right") - getFloatProperty(planeBoundsJson, "left"));
               if (planeBoundsWidth <= 2.0F) {
                  glyphScale = 1.0F;
               }
               break;
            }
         }
      }

      Int2ObjectMap<GlyphMetrics> glyphMetricsByCodePoint = new Int2ObjectOpenHashMap<>();
// https://t.me/pasterplace

      for (JsonElement glyphElement : fontJson.getAsJsonArray("glyphs")) {
         JsonObject glyphJson = glyphElement.getAsJsonObject();
         if (glyphJson.has("unicode")) {
            int codePoint = glyphJson.get("unicode").getAsInt();
            float advanceWidth = getFloatProperty(glyphJson, "advance") / glyphScale;
            glyphMetricsByCodePoint.put(
               codePoint,
               glyphJson.has("planeBounds") && glyphJson.has("atlasBounds")
                  ? createGlyphMetrics(
                     codePoint, advanceWidth, glyphScale, atlasMetadata, topOrigin, glyphJson.getAsJsonObject("planeBounds"), glyphJson.getAsJsonObject("atlasBounds")
                  )
                  : new GlyphMetrics(codePoint, advanceWidth, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
            );
         }
      }

      addFallbackGlyph(glyphMetricsByCodePoint, 183, 46);
      addFallbackGlyph(glyphMetricsByCodePoint, 8226, 46);
      addFallbackGlyph(glyphMetricsByCodePoint, 8212, 45);
      addFallbackGlyph(glyphMetricsByCodePoint, 8211, 45);
      addFallbackGlyph(glyphMetricsByCodePoint, 8594, 62);
      addFallbackGlyph(glyphMetricsByCodePoint, 8592, 60);
      addFallbackGlyph(glyphMetricsByCodePoint, 8230, 46);
      addFallbackGlyph(glyphMetricsByCodePoint, 171, 34);
      addFallbackGlyph(glyphMetricsByCodePoint, 187, 34);
      addFallbackGlyph(glyphMetricsByCodePoint, 8470, 78);
      Long2FloatOpenHashMap kerningAdjustments = new Long2FloatOpenHashMap();
      if (fontJson.has("kerning") && fontJson.get("kerning").isJsonArray()) {
         for (JsonElement kerningElement : fontJson.getAsJsonArray("kerning")) {
            JsonObject kerningJson = kerningElement.getAsJsonObject();
            kerningAdjustments.put(
               packCodePointPair(kerningJson.get("unicode1").getAsInt(), kerningJson.get("unicode2").getAsInt()),
               getFloatProperty(kerningJson, "advance") / emSize
            );
         }
      }

      return new MsdfAtlasData(
         atlasMetadata, fontMetrics, glyphMetricsByCodePoint, kerningAdjustments, (GlyphMetrics)glyphMetricsByCodePoint.get(63)
      );
   }
// https://t.me/pasterplace

   private static void addFallbackGlyph(Int2ObjectMap<GlyphMetrics> glyphMetricsByCodePoint, int aliasCodePoint, int sourceCodePoint) {
      GlyphMetrics glyph = glyphMetricsByCodePoint.get(sourceCodePoint);
      if (!glyphMetricsByCodePoint.containsKey(aliasCodePoint) && glyph != null) {
         glyphMetricsByCodePoint.put(aliasCodePoint, glyph);
      }
   }
// https://t.me/pasterplace

   private static GlyphMetrics createGlyphMetrics(
      int codePoint, float advanceWidth, float normalizationScale, MsdfAtlasMetadata atlasMetadata, boolean topOrigin, JsonObject planeBoundsJson, JsonObject atlasBoundsJson
   ) {
      float atlasLeft = getFloatProperty(atlasBoundsJson, "left");
      float atlasRight = getFloatProperty(atlasBoundsJson, "right");
      float atlasLowerEdge = Math.min(getFloatProperty(atlasBoundsJson, "bottom"), getFloatProperty(atlasBoundsJson, "top"));
      float atlasUpperEdge = Math.max(getFloatProperty(atlasBoundsJson, "bottom"), getFloatProperty(atlasBoundsJson, "top"));
      float normalizedAtlasTop = topOrigin ? atlasLowerEdge / atlasMetadata.height() : 1.0F - atlasUpperEdge / atlasMetadata.height();
      float normalizedAtlasBottom = topOrigin ? atlasUpperEdge / atlasMetadata.height() : 1.0F - atlasLowerEdge / atlasMetadata.height();
      return new GlyphMetrics(
         codePoint,
         advanceWidth,
         getFloatProperty(planeBoundsJson, "left") / normalizationScale,
         getFloatProperty(planeBoundsJson, "bottom") / normalizationScale,
         getFloatProperty(planeBoundsJson, "right") / normalizationScale,
         getFloatProperty(planeBoundsJson, "top") / normalizationScale,
         atlasLeft / atlasMetadata.width(),
         normalizedAtlasTop,
         atlasRight / atlasMetadata.width(),
         normalizedAtlasBottom
      );
   }
// https://t.me/pasterplace

   private static float getFloatProperty(JsonObject jsonObject, String propertyName) {
      return jsonObject != null && jsonObject.has(propertyName) && !jsonObject.get(propertyName).isJsonNull() ? jsonObject.get(propertyName).getAsFloat() : 0.0F;
   }
}

