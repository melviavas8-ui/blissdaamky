package dev.daamky;

public final class AnimatedColorUtils {
   private AnimatedColorUtils() {
   }

   public static float animationTime() {
      return (float)(System.nanoTime() / 1000000L % 1000000L) / 1000.0F;
   }

   public static RgbaColor getAccentColor(int alpha) {
      return Accent.getAccentColorWithAlpha(alpha);
   }

   public static RgbaColor getAnimatedAccentColor(float phaseOffset, int alpha) {
      float animationProgress = (float)(Math.sin(animationTime() * 2.2F + phaseOffset) * 0.5 + 0.5);
      return Accent.getAccentColorWithAlpha(255).mix(Accent.getLighterAccentColor(255), animationProgress).withAlpha(clampAlpha(alpha));
   }

   public static RgbaColor getAnimatedThemeColor(float phaseOffset, int alpha) {
      float animationProgress = (float)(Math.sin(animationTime() * 2.2F + phaseOffset) * 0.5 + 0.5);
      return Accent.getDarkerAccentColor(255).mix(Accent.getLighterAccentColor(255), animationProgress).withAlpha(clampAlpha(alpha));
   }

   public static RgbaColor getHealthColor(float healthRatio, int alpha) {
      float clampedHealthRatio = Math.clamp(healthRatio, 0.0F, 1.0F);
      return RgbaColor.rgb(255, 70, 70)
         .mix(RgbaColor.rgb(90, 255, 120), clampedHealthRatio)
         .withAlpha(clampAlpha(alpha));
   }

   public static int clampAlpha(int alpha) {
      return Math.clamp(alpha, 0, 255);
   }

   public static int scaleAlpha(float alpha, float alphaMultiplier) {
      return clampAlpha((int)(alpha * alphaMultiplier));
   }
}

