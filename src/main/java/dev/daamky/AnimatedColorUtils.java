package dev.daamky;

public final class AnimatedColorUtils {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private AnimatedColorUtils() {
   }
// https://t.me/pasterplace

   public static float animationTime() {
      return (float)(System.nanoTime() / 1000000L % 1000000L) / 1000.0F;
   }
// https://t.me/pasterplace

   public static RgbaColor getAccentColor(int alpha) {
      return Accent.getAccentColorWithAlpha(alpha);
   }
// https://t.me/pasterplace

   public static RgbaColor getAnimatedAccentColor(float phaseOffset, int alpha) {
      float animationProgress = (float)(Math.sin(animationTime() * 2.2F + phaseOffset) * 0.5 + 0.5);
      return Accent.getAccentColorWithAlpha(255).mix(Accent.getLighterAccentColor(255), animationProgress).withAlpha(clampAlpha(alpha));
   }
// https://t.me/pasterplace

   public static RgbaColor getAnimatedThemeColor(float phaseOffset, int alpha) {
      float animationProgress = (float)(Math.sin(animationTime() * 2.2F + phaseOffset) * 0.5 + 0.5);
      return Accent.getDarkerAccentColor(255).mix(Accent.getLighterAccentColor(255), animationProgress).withAlpha(clampAlpha(alpha));
   }
// https://t.me/pasterplace

   public static RgbaColor getHealthColor(float healthRatio, int alpha) {
      float clampedHealthRatio = Math.clamp(healthRatio, 0.0F, 1.0F);
      return RgbaColor.rgb(255, 70, 70)
         .mix(RgbaColor.rgb(90, 255, 120), clampedHealthRatio)
         .withAlpha(clampAlpha(alpha));
   }
// https://t.me/pasterplace

   public static int clampAlpha(int alpha) {
      return Math.clamp(alpha, 0, 255);
   }
// https://t.me/pasterplace

   public static int scaleAlpha(float alpha, float alphaMultiplier) {
      return clampAlpha((int)(alpha * alphaMultiplier));
   }
}

