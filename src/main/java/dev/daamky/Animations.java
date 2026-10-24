package dev.daamky;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public final class Animations {
   private final Map<String, Animations.AxisKeyframes> keyframesByBoneName = new HashMap<>();
   private final boolean looping;
   private final float animationLengthSeconds;

   private Animations(boolean looping, float animationLengthSeconds) {
      this.looping = looping;
      this.animationLengthSeconds = animationLengthSeconds;
   }

   public static Animations fromJson(JsonObject rootJson) {
      if (rootJson != null && rootJson.has("animations")) {
         JsonObject animationsObject = rootJson.getAsJsonObject("animations");
         if (animationsObject.isEmpty()) {
            return null;
         }

         JsonObject animationData = selectAnimationData(animationsObject);
         if (animationData == null) {
            return null;
         }

         boolean looping = animationData.has("loop")
            && animationData.get("loop").isJsonPrimitive()
            && animationData.get("loop").getAsJsonPrimitive().isBoolean()
            && animationData.get("loop").getAsBoolean();
         float animationLengthSeconds = animationData.has("animation_length") ? animationData.get("animation_length").getAsFloat() : 1.0F;
         Animations animation = new Animations(looping, animationLengthSeconds);
         if (animationData.has("bones")) {
            for (Map.Entry<String, JsonElement> boneEntry : animationData.getAsJsonObject("bones").entrySet()) {
               JsonObject boneAnimation = boneEntry.getValue().getAsJsonObject();
               animation.keyframesByBoneName
                  .put(
                     boneEntry.getKey(),
                     new Animations.AxisKeyframes(
                        parseKeyframes(boneAnimation.get("rotation")),
                        parseKeyframes(boneAnimation.get("position")),
                        parseKeyframes(boneAnimation.get("scale"))
                     )
                  );
            }
         }

         return animation;
      } else {
         return null;
      }
   }

   private static JsonObject selectAnimationData(JsonObject animationClips) {
      JsonObject fallbackAnimation = null;

      for (Map.Entry<String, JsonElement> clipEntry : animationClips.entrySet()) {
         if (clipEntry.getValue().isJsonObject()) {
            String clipName = clipEntry.getKey().toLowerCase();
            if (clipName.endsWith(".idle") || clipName.equals("idle")) {
               return clipEntry.getValue().getAsJsonObject();
            }

            if (fallbackAnimation == null && !clipName.contains("gui")) {
               fallbackAnimation = clipEntry.getValue().getAsJsonObject();
            }
         }
      }

      return fallbackAnimation;
   }

   private static TreeMap<Float, float[]> parseKeyframes(JsonElement keyframeJson) {
      TreeMap keyframesByTime = new TreeMap();
      if (keyframeJson == null) {
         return keyframesByTime;
      }

      if (keyframeJson.isJsonArray()) {
         float[] vectorValues = parseVector(keyframeJson);
         if (vectorValues != null) {
            keyframesByTime.put(0.0F, vectorValues);
         }

         return keyframesByTime;
      } else {
         if (!keyframeJson.isJsonObject()) {
            return keyframesByTime;
         }

         for (Map.Entry<String, JsonElement> keyframeEntry : keyframeJson.getAsJsonObject().entrySet()) {
            try {
               float[] vectorValues = parseVector(keyframeEntry.getValue());
               if (vectorValues != null) {
                  keyframesByTime.put(Float.parseFloat(keyframeEntry.getKey()), vectorValues);
               }
            } catch (NumberFormatException invalidKeyframeTime) {
            }
         }

         return keyframesByTime;
      }
   }

   private static float[] parseVector(JsonElement vectorJson) {
      JsonArray vectorComponents = null;
      if (vectorJson.isJsonArray()) {
         vectorComponents = vectorJson.getAsJsonArray();
      } else if (vectorJson.isJsonObject() && vectorJson.getAsJsonObject().has("vector")) {
         vectorComponents = vectorJson.getAsJsonObject().getAsJsonArray("vector");
      }

      if (vectorComponents != null && vectorComponents.size() >= 3) {
         try {
            return new float[]{vectorComponents.get(0).getAsFloat(), vectorComponents.get(1).getAsFloat(), vectorComponents.get(2).getAsFloat()};
         } catch (RuntimeException invalidVectorException) {
            return null;
         }
      } else {
         return null;
      }
   }

   public void apply(ModelGeometry modelGeometry, float elapsedSeconds) {
      float animationTime = this.looping && this.animationLengthSeconds > 0.0F ? elapsedSeconds % this.animationLengthSeconds : Math.min(elapsedSeconds, this.animationLengthSeconds);
      modelGeometry.restoreInitialPose();

      for (Map.Entry<String, Animations.AxisKeyframes> boneEntry : this.keyframesByBoneName.entrySet()) {
         ModelBone bone = modelGeometry.bonesByName.get(boneEntry.getKey());
         float[] initialPose = modelGeometry.getInitialPose(boneEntry.getKey());
         if (bone != null && initialPose != null) {
            Animations.AxisKeyframes boneKeyframes = boneEntry.getValue();
            if (!boneKeyframes.rotationKeyframes.isEmpty()) {
               float[] rotationValues = sampleKeyframes(boneKeyframes.rotationKeyframes, animationTime);
               ((ModelBone)bone).I1lIIII1III1lIlIl1l = initialPose[0] + (float)Math.toRadians(-rotationValues[0]);
               ((ModelBone)bone).l11lIlIIll1III1 = initialPose[1] + (float)Math.toRadians(-rotationValues[1]);
               ((ModelBone)bone).floatValue = initialPose[2] + (float)Math.toRadians(rotationValues[2]);
            }

            if (!boneKeyframes.positionKeyframes.isEmpty()) {
               float[] positionValues = sampleKeyframes(boneKeyframes.positionKeyframes, animationTime);
               ((ModelBone)bone).Ill111IlI11l = initialPose[3] + positionValues[0];
               ((ModelBone)bone).I1l11IIlIIIll1l1lI1II = initialPose[4] + positionValues[1];
               ((ModelBone)bone).I1I1lI111lII1 = initialPose[5] + positionValues[2];
            }

            if (!boneKeyframes.scaleKeyframes.isEmpty()) {
               float[] scaleValues = sampleKeyframes(boneKeyframes.scaleKeyframes, animationTime);
               ((ModelBone)bone).lIll1Il1lll111l11l1I1llllI = initialPose[6] * scaleValues[0];
               ((ModelBone)bone).lIIIlI1III1 = initialPose[7] * scaleValues[1];
               ((ModelBone)bone).IIl1IlII1III1IIl1IIl = initialPose[8] * scaleValues[2];
            }
         }
      }
   }

   private static float[] sampleKeyframes(TreeMap<Float, float[]> keyframesByTime, float animationTime) {
      Entry<Float, float[]> lowerKeyframe = keyframesByTime.floorEntry(animationTime);
      Entry<Float, float[]> upperKeyframe = keyframesByTime.ceilingEntry(animationTime);
      if (lowerKeyframe == null) {
         return (float[])upperKeyframe.getValue();
      } else if (upperKeyframe != null && !((Float)upperKeyframe.getKey()).equals(lowerKeyframe.getKey())) {
         float interpolationProgress = (animationTime - (Float)lowerKeyframe.getKey()) / ((Float)upperKeyframe.getKey() - (Float)lowerKeyframe.getKey());
         float[] lowerValues = lowerKeyframe.getValue();
         float[] upperValues = upperKeyframe.getValue();
         return new float[]{
            lowerValues[0] + (upperValues[0] - lowerValues[0]) * interpolationProgress,
            lowerValues[1] + (upperValues[1] - lowerValues[1]) * interpolationProgress,
            lowerValues[2] + (upperValues[2] - lowerValues[2]) * interpolationProgress
         };
      } else {
         return (float[])lowerKeyframe.getValue();
      }
   }

   private record AxisKeyframes(TreeMap<Float, float[]> rotationKeyframes, TreeMap<Float, float[]> positionKeyframes, TreeMap<Float, float[]> scaleKeyframes) {

      private AxisKeyframes(TreeMap<Float, float[]> rotationKeyframes, TreeMap<Float, float[]> positionKeyframes, TreeMap<Float, float[]> scaleKeyframes) {
         this.rotationKeyframes = rotationKeyframes;
         this.positionKeyframes = positionKeyframes;
         this.scaleKeyframes = scaleKeyframes;
      }
   }
}

