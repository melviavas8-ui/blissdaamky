package dev.daamky;

public final class ColorUtils {
   private ColorUtils() {
   }

   public static int interpolateColor(int startColor, int endColor, float progress) {
      if (progress <= 0.0F || startColor == endColor) {
         return startColor;
      }

      if (progress >= 1.0F) {
         return endColor;
      }

      int alpha = interpolateChannel(startColor >>> 24, endColor >>> 24, progress);
      int red = interpolateChannel(startColor >> 16 & 0xFF, endColor >> 16 & 0xFF, progress);
      int green = interpolateChannel(startColor >> 8 & 0xFF, endColor >> 8 & 0xFF, progress);
      int blue = interpolateChannel(startColor & 0xFF, endColor & 0xFF, progress);
      return alpha << 24 | red << 16 | green << 8 | blue;
   }

   public static int interpolateColorBilinear(int topLeftColor, int bottomLeftColor, int topRightColor, int bottomRightColor, float horizontalProgress, float verticalProgress) {
      return interpolateColor(interpolateColor(topLeftColor, bottomLeftColor, horizontalProgress), interpolateColor(bottomRightColor, topRightColor, horizontalProgress), verticalProgress);
   }

   public static int scaleAlpha(int color, float alphaMultiplier) {
      if (alphaMultiplier >= 1.0F) {
         return color;
      }

      int alpha = Math.round((color >>> 24) * Math.clamp(alphaMultiplier, 0.0F, 1.0F));
      return alpha << 24 | color & 16777215;
   }

   private static int interpolateChannel(int startChannel, int endChannel, float progress) {
      return Math.round(startChannel + (endChannel - startChannel) * progress);
   }
}

