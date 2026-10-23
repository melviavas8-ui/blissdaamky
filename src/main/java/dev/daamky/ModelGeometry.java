package dev.daamky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModelGeometry {
    public final List<ModelBone> rootBones = new ArrayList<>();
    public final Map<String, ModelBone> bonesByName = new HashMap<>();
    private final Map<String, float[]> initialPoseValues = new HashMap<>();

    public void captureInitialPose() {
        for (ModelBone bone : this.bonesByName.values()) {
            this.initialPoseValues.put(bone.II1Il11l111II11IIl, bone.II1Il11l111II11IIl());
        }
    }

    public float[] getInitialPose(String boneName) {
        return this.initialPoseValues.get(boneName);
    }

    public void restoreInitialPose() {
        for (ModelBone bone : this.bonesByName.values()) {
            float[] initialPose = this.initialPoseValues.get(bone.II1Il11l111II11IIl);
            if (initialPose != null) {
                bone.II1Il11l111II11IIl(initialPose);
            }
        }
    }
}
