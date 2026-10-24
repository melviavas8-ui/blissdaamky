package dev.daamky;

import java.util.List;

record TrailPointWindow(List<TrailPoint> points, double visiblePointCount) {

   TrailPointWindow(List<TrailPoint> points, double visiblePointCount) {
      this.points = points;
      this.visiblePointCount = visiblePointCount;
   }
}

