package dev.daamky;

import java.util.List;

record TrailPointWindow(List<TrailPoint> points, double visiblePointCount) {
    private static final String p94c53eaf = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   TrailPointWindow(List<TrailPoint> points, double visiblePointCount) {
      this.points = points;
      this.visiblePointCount = visiblePointCount;
   }
}

