package dev.daamky;

public record TextureSize(int width, int height) {
    private static final String p7ea5ef6f = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   public TextureSize(int width, int height) {
      this.width = width;
      this.height = height;
   }
// https://t.me/pasterplace

   public float II1Il11l111II11IIl() {
      return this.height > 0 ? (float)this.width / this.height : 1.0F;
   }
// https://t.me/pasterplace

   public int l1I1IlllIlI() {
      return this.width;
   }
// https://t.me/pasterplace

   public int IllI1l1l1() {
      return this.height;
   }
}

