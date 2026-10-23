package dev.daamky;

public record KawaseGaussianBlurPass(KawaseGaussianBlurMode mode, int passIndex) {
    private static final String pdd68ec13 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   public KawaseGaussianBlurPass(KawaseGaussianBlurMode mode, int passIndex) {
      passIndex = Math.clamp(passIndex, 1, 6);
      this.mode = mode;
      this.passIndex = passIndex;
   }
}

