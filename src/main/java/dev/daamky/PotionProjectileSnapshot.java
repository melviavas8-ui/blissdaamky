package dev.daamky;

import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.util.math.Vec3d;

record PotionProjectileSnapshot(Vec3d position, PotionContentsComponent potionContents, long capturedAtMillis) {

   PotionProjectileSnapshot(Vec3d position, PotionContentsComponent potionContents, long capturedAtMillis) {
      this.position = position;
      this.potionContents = potionContents;
      this.capturedAtMillis = capturedAtMillis;
   }
}

