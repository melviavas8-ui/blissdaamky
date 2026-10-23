package dev.daamky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Stores and restores the initial transform of each named model bone. */
public final class ModelPoseCache {
   public final List<ModelBone> bones = new ArrayList<>();
   public final Map<String, ModelBone> initialPoseByBone = new HashMap<>();
   private final Map<String, float[]> initialPoseValues = new HashMap<>();

   public void captureInitialPose() {
      for (ModelBone bone : this.initialPoseByBone.values()) {
         this.initialPoseValues.put(bone.II1Il11l111II11IIl, bone.II1Il11l111II11IIl());
      }
   }

   public float[] getInitialPose(String boneName) {
      return this.initialPoseValues.get(boneName);
   }

   public void restoreInitialPose() {
      for (ModelBone bone : this.initialPoseByBone.values()) {
         float[] pose = this.initialPoseValues.get(bone.II1Il11l111II11IIl);
         if (pose != null) {
            bone.II1Il11l111II11IIl(pose);
         }
      }
   }
}
