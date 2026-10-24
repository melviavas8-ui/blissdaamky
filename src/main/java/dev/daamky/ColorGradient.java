package dev.daamky;

import java.util.Arrays;

public final class ColorGradient {
   private final int[] colors;
   private final float[] stopPositions;
   private final float angleDegrees;
   private final float offset;
   private final ColorGradient.GradientMode mode;
   private final float directionX;
   private final float directionY;
   private final float directionScale;

   private ColorGradient(int[] colors, float[] stopPositions, float angleDegrees, float offset, ColorGradient.GradientMode mode) {
      this.colors = colors;
      this.stopPositions = stopPositions;
      this.angleDegrees = angleDegrees;
      this.offset = offset;
      this.mode = mode;
      double angleRadians = Math.toRadians(angleDegrees);
      this.directionX = (float)Math.cos(angleRadians);
      this.directionY = (float)Math.sin(angleRadians);
      float directionLength = Math.abs(this.directionX) + Math.abs(this.directionY);
      this.directionScale = directionLength > 1.0E-6F ? 1.0F / directionLength : 1.0F;
   }

   public static ColorGradient fromColors(int... colors) {
      if (colors.length == 0) {
         throw new IllegalArgumentException("Gradient requires at least one color");
      }

      float[] stopPositions = new float[colors.length];

      for (int stopIndex = 0; stopIndex < stopPositions.length; stopIndex++) {
         stopPositions[stopIndex] = colors.length == 1 ? 0.0F : (float)stopIndex / (colors.length - 1);
      }

      return new ColorGradient((int[])colors.clone(), stopPositions, 0.0F, 0.0F, ColorGradient.GradientMode.CLAMP);
   }

   public static ColorGradient fromColors(RgbaColor... inputColors) {
      return fromColors(Arrays.stream(inputColors).mapToInt(RgbaColor::toArgb).toArray());
   }

   public static ColorGradient fromStops(float[] stopPositions, int[] colors) {
      if (colors.length != 0 && stopPositions.length == colors.length) {
         for (int stopIndex = 1; stopIndex < stopPositions.length; stopIndex++) {
            if (stopPositions[stopIndex] < stopPositions[stopIndex - 1]) {
               throw new IllegalArgumentException("Gradient stops must be ascending");
            }
         }

         return new ColorGradient((int[])colors.clone(), (float[])stopPositions.clone(), 0.0F, 0.0F, ColorGradient.GradientMode.CLAMP);
      } else {
         throw new IllegalArgumentException("Gradient stops and colors must have the same non-zero length");
      }
   }

   public static ColorGradient fromStops(float[] stopPositions, RgbaColor[] inputColors) {
      return fromStops(stopPositions, Arrays.stream(inputColors).mapToInt(RgbaColor::toArgb).toArray());
   }

   public ColorGradient withAngle(float angleDegrees) {
      return new ColorGradient(this.colors, this.stopPositions, angleDegrees, this.offset, this.mode);
   }

   public ColorGradient withOffset(float offset) {
      return new ColorGradient(this.colors, this.stopPositions, this.angleDegrees, offset, this.mode);
   }

   public ColorGradient withMode(ColorGradient.GradientMode mode) {
      return new ColorGradient(this.colors, this.stopPositions, this.angleDegrees, this.offset, mode);
   }

   public ColorGradient repeat() {
      return this.withMode(ColorGradient.GradientMode.REPEAT);
   }

   public ColorGradient reflect() {
      return this.withMode(ColorGradient.GradientMode.REFLECT);
   }

   public int sampleColor(float position) {
      float wrappedPosition = this.wrapPosition(position + this.offset);
      int lastStopIndex = this.colors.length - 1;
      if (lastStopIndex == 0 || wrappedPosition <= this.stopPositions[0]) {
         return this.colors[0];
      }

      if (wrappedPosition >= this.stopPositions[lastStopIndex]) {
         return this.colors[lastStopIndex];
      }

      int nextStopIndex = 1;

      while (this.stopPositions[nextStopIndex] < wrappedPosition) {
         nextStopIndex++;
      }

      float stopRange = this.stopPositions[nextStopIndex] - this.stopPositions[nextStopIndex - 1];
      return ColorUtils.interpolateColor(
         this.colors[nextStopIndex - 1], this.colors[nextStopIndex], stopRange <= 0.0F ? 1.0F : (wrappedPosition - this.stopPositions[nextStopIndex - 1]) / stopRange
      );
   }

   public int sampleColor(float x, float y) {
      return this.sampleColor(((x - 0.5F) * this.directionX + (y - 0.5F) * this.directionY) * this.directionScale + 0.5F);
   }

   private float wrapPosition(float position) {
      return switch (this.mode) {
         case CLAMP -> Math.clamp(position, 0.0F, 1.0F);
         case REPEAT -> position - (float)Math.floor(position);
         case REFLECT -> {
            float wrappedPosition = position - 2.0F * (float)Math.floor(position * 0.5F);
            yield wrappedPosition > 1.0F ? 2.0F - wrappedPosition : wrappedPosition;
         }
      };
   }

   public enum GradientMode {
      CLAMP,
      REPEAT,
      REFLECT;

      // $VF: synthetic method
      private static ColorGradient.GradientMode[] enumValues() {
         return new ColorGradient.GradientMode[]{CLAMP, REPEAT, REFLECT};
      }
   }
}

