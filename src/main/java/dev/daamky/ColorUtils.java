package dev.daamky;

public final class ColorUtils {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private ColorUtils() {
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

   public static int interpolateColorBilinear(int topLeftColor, int bottomLeftColor, int topRightColor, int bottomRightColor, float horizontalProgress, float verticalProgress) {
      return interpolateColor(interpolateColor(topLeftColor, bottomLeftColor, horizontalProgress), interpolateColor(bottomRightColor, topRightColor, horizontalProgress), verticalProgress);
   }
// https://t.me/pasterplace

   public static int scaleAlpha(int color, float alphaMultiplier) {
      if (alphaMultiplier >= 1.0F) {
         return color;
      }
// https://t.me/pasterplace

      int alpha = Math.round((color >>> 24) * Math.clamp(alphaMultiplier, 0.0F, 1.0F));
      return alpha << 24 | color & 16777215;
   }
// https://t.me/pasterplace

   private static int interpolateChannel(int startChannel, int endChannel, float progress) {
      return Math.round(startChannel + (endChannel - startChannel) * progress);
   }
}

