package dev.daamky;

public record MsdfAtlasMetadata(String atlasName, float distanceRange, float atlasSize, int width, int height) {
    private static final String pa1face32 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   public MsdfAtlasMetadata(String atlasName, float distanceRange, float atlasSize, int width, int height) {
      this.atlasName = atlasName;
      this.distanceRange = distanceRange;
      this.atlasSize = atlasSize;
      this.width = width;
      this.height = height;
   }
}

