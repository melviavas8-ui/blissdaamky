package dev.daamky;

import net.minecraft.util.math.Vec3d;

record TrailPoint(Vec3d position, double timestampSeconds) {

   TrailPoint(Vec3d position, double timestampSeconds) {
      this.position = position;
      this.timestampSeconds = timestampSeconds;
   }

   TrailPoint II1Il11l111II11IIl(TrailPoint nextPoint, double interpolationProgress) {
      return new TrailPoint(
         this.position.lerp(nextPoint.position, interpolationProgress), this.timestampSeconds + (nextPoint.timestampSeconds - this.timestampSeconds) * interpolationProgress
      );
   }
}

