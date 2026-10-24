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

   public boolean hasPlaneBounds() {
      return this.planeRight > this.planeLeft && this.planeTop > this.planeBottom;
   }

   public float planeWidth() {
      return this.planeRight - this.planeLeft;
   }

   public float planeHeight() {
      return this.planeTop - this.planeBottom;
   }

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

