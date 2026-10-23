package dev.daamky;

import net.minecraft.util.math.Vec3d;

record TrailPoint(Vec3d position, double timestampSeconds) {
    private static final String p57e09158 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   TrailPoint(Vec3d position, double timestampSeconds) {
      this.position = position;
      this.timestampSeconds = timestampSeconds;
   }
// https://t.me/pasterplace

   TrailPoint II1Il11l111II11IIl(TrailPoint nextPoint, double interpolationProgress) {
      return new TrailPoint(
         this.position.lerp(nextPoint.position, interpolationProgress), this.timestampSeconds + (nextPoint.timestampSeconds - this.timestampSeconds) * interpolationProgress
      );
   }
}

