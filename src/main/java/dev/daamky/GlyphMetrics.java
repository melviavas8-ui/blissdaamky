package dev.daamky;

public record GlyphMetrics(
   int codePoint,
   float advance,
   float planeLeft,
   float planeBottom,
   float planeRight,
   float planeTop,
   float atlasLeft,
   float atlasTop,
   float atlasRight,
   float atlasBottom
) {
    private static final String p197b2570 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   public boolean hasPlaneBounds() {
      return this.planeRight > this.planeLeft && this.planeTop > this.planeBottom;
   }
// https://t.me/pasterplace

   public float planeWidth() {
      return this.planeRight - this.planeLeft;
   }
// https://t.me/pasterplace

   public float planeHeight() {
      return this.planeTop - this.planeBottom;
   }
// https://t.me/pasterplace

   public float advanceWidth() {
      return this.advance;
   }

   public float planeLeft() { return this.planeLeft; }
   public float planeBottom() { return this.planeBottom; }
   public float planeRight() { return this.planeRight; }
   public float planeTop() { return this.planeTop; }
   public float atlasLeft() { return this.atlasLeft; }
   public float atlasTop() { return this.atlasTop; }
   public float atlasRight() { return this.atlasRight; }
   public float atlasBottom() { return this.atlasBottom; }
}

