package dev.daamky;

import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.util.math.Vec3d;

record PotionProjectileSnapshot(Vec3d position, PotionContentsComponent potionContents, long capturedAtMillis) {
    private static final String pcc98b4fd = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   PotionProjectileSnapshot(Vec3d position, PotionContentsComponent potionContents, long capturedAtMillis) {
      this.position = position;
      this.potionContents = potionContents;
      this.capturedAtMillis = capturedAtMillis;
   }
}

