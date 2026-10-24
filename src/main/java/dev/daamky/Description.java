package dev.daamky;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public final class Description {
   private Description() {
   }

   public static ModelGeometry parseGeometry(JsonObject geometryJson) {
      JsonArray geometryVariants = geometryJson.getAsJsonArray("minecraft:geometry");
      if (geometryVariants != null && !geometryVariants.isEmpty()) {
         JsonObject geometryRoot = geometryVariants.get(0).getAsJsonObject();
         JsonObject geometryDescription = geometryRoot.getAsJsonObject("description");
         int textureWidth = geometryDescription != null && geometryDescription.has("texture_width") ? geometryDescription.get("texture_width").getAsInt() : 64;
         int textureHeight = geometryDescription != null && geometryDescription.has("texture_height") ? geometryDescription.get("texture_height").getAsInt() : 64;
         ModelGeometry geometry = new ModelGeometry();
         JsonArray boneDefinitions = geometryRoot.getAsJsonArray("bones");
         if (boneDefinitions != null) {
            for (JsonElement boneDefinitionElement : boneDefinitions) {
               ModelBone modelBone = parseBone(boneDefinitionElement.getAsJsonObject(), textureWidth, textureHeight);
               geometry.bonesByName.put(modelBone.II1Il11l111II11IIl, modelBone);
            }

            for (JsonElement boneDefinitionElement : boneDefinitions) {
               JsonObject boneDefinitionJson = boneDefinitionElement.getAsJsonObject();
               ModelBone modelBone = geometry.bonesByName.get(boneDefinitionJson.get("name").getAsString());
               ModelBone parentBone = boneDefinitionJson.has("parent")
                  ? geometry.bonesByName.get(boneDefinitionJson.get("parent").getAsString())
                  : null;
               if (parentBone != null) {
                  parentBone.l1I1IlllIlI.add(modelBone);
               } else if (!boneDefinitionJson.has("parent")) {
                  geometry.rootBones.add(modelBone);
               }
            }
         }

         geometry.captureInitialPose();
         return geometry;
      } else {
         return null;
      }
   }

   private static ModelBone parseBone(JsonObject boneJson, int textureWidth, int textureHeight) {
      ModelBone bone = new ModelBone(boneJson.get("name").getAsString());
      if (boneJson.has("pivot")) {
         JsonArray pivotCoordinates = boneJson.getAsJsonArray("pivot");
         bone.l1lll11l1l = -pivotCoordinates.get(0).getAsFloat();
         bone.lIIl11l111lIIl1ll = pivotCoordinates.get(1).getAsFloat();
         bone.l1IIl11lI = pivotCoordinates.get(2).getAsFloat();
      }

      if (boneJson.has("rotation")) {
         JsonArray rotationDegrees = boneJson.getAsJsonArray("rotation");
         bone.I1lIIII1III1lIlIl1l = (float)Math.toRadians(-rotationDegrees.get(0).getAsFloat());
         bone.l11lIlIIll1III1 = (float)Math.toRadians(-rotationDegrees.get(1).getAsFloat());
         bone.floatValue = (float)Math.toRadians(rotationDegrees.get(2).getAsFloat());
      }

      if (boneJson.has("cubes")) {
         for (JsonElement cubeDefinitionElement : boneJson.getAsJsonArray("cubes")) {
            bone.IllI1l1l1.add(parseCube(cubeDefinitionElement.getAsJsonObject(), textureWidth, textureHeight));
         }
      }

      return bone;
   }

   private static ModelBonePose parseCube(JsonObject cubeJson, int textureWidth, int textureHeight) {
      float[] cubeOrigin = getFloatArray(cubeJson, "origin", new float[]{0.0F, 0.0F, 0.0F});
      float[] cubeSize = getFloatArray(cubeJson, "size", new float[]{1.0F, 1.0F, 1.0F});
      float[] cubePivot = getFloatArray(cubeJson, "pivot", (float[])cubeOrigin.clone());
      float[] cubeRotationDegrees = getFloatArray(cubeJson, "rotation", new float[]{0.0F, 0.0F, 0.0F});
      float inflateAmount = cubeJson.has("inflate") ? cubeJson.get("inflate").getAsFloat() : 0.0F;
      ModelBonePose cubePose = new ModelBonePose(cubeSize[0], cubeSize[1], cubeSize[2]);
      cubePose.lIIl11l111lIIl1ll = -cubePivot[0];
      cubePose.l1IIl11lI = cubePivot[1];
      cubePose.I1lIIII1III1lIlIl1l = cubePivot[2];
      cubePose.l11lIlIIll1III1 = (float)Math.toRadians(-cubeRotationDegrees[0]);
      cubePose.floatValue = (float)Math.toRadians(-cubeRotationDegrees[1]);
      cubePose.Ill111IlI11l = (float)Math.toRadians(cubeRotationDegrees[2]);
      float inflatedOriginX = cubeOrigin[0] - inflateAmount;
      float inflatedOriginY = cubeOrigin[1] - inflateAmount;
      float inflatedOriginZ = cubeOrigin[2] - inflateAmount;
      float inflatedSizeX = cubeSize[0] + inflateAmount * 2.0F;
      float inflatedSizeY = cubeSize[1] + inflateAmount * 2.0F;
      float inflatedSizeZ = cubeSize[2] + inflateAmount * 2.0F;
      float minModelX = -(inflatedOriginX + inflatedSizeX) / 16.0F;
      float minModelY = inflatedOriginY / 16.0F;
      float minModelZ = inflatedOriginZ / 16.0F;
      float maxModelX = -inflatedOriginX / 16.0F;
      float maxModelY = (inflatedOriginY + inflatedSizeY) / 16.0F;
      float maxModelZ = (inflatedOriginZ + inflatedSizeZ) / 16.0F;
      JsonElement uvDefinitionElement = cubeJson.get("uv");
      if (uvDefinitionElement != null && uvDefinitionElement.isJsonObject()) {
         JsonObject faceUvDefinitions = uvDefinitionElement.getAsJsonObject();
         cubePose.II1Il11l111II11IIl[0] = readJsonFace(
            faceUvDefinitions, "west", minModelX, minModelY, minModelZ, minModelX, maxModelY, maxModelZ, -1.0F, 0.0F, 0.0F, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[1] = readJsonFace(
            faceUvDefinitions, "east", maxModelX, minModelY, minModelZ, maxModelX, maxModelY, maxModelZ, 1.0F, 0.0F, 0.0F, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[2] = readJsonFace(
            faceUvDefinitions, "down", minModelX, minModelY, minModelZ, maxModelX, minModelY, maxModelZ, 0.0F, -1.0F, 0.0F, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[3] = readJsonFace(
            faceUvDefinitions, "up", minModelX, maxModelY, minModelZ, maxModelX, maxModelY, maxModelZ, 0.0F, 1.0F, 0.0F, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[4] = readJsonFace(
            faceUvDefinitions, "north", minModelX, minModelY, minModelZ, maxModelX, maxModelY, minModelZ, 0.0F, 0.0F, -1.0F, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[5] = readJsonFace(
            faceUvDefinitions, "south", minModelX, minModelY, maxModelZ, maxModelX, maxModelY, maxModelZ, 0.0F, 0.0F, 1.0F, textureWidth, textureHeight
         );
      } else {
         float baseUvU = 0.0F;
         float baseUvV = 0.0F;
         if (uvDefinitionElement != null && uvDefinitionElement.isJsonArray()) {
            baseUvU = uvDefinitionElement.getAsJsonArray().get(0).getAsFloat();
            baseUvV = uvDefinitionElement.getAsJsonArray().get(1).getAsFloat();
         }

         cubePose.II1Il11l111II11IIl[0] = createCubeFaceQuadWithAtlasUv(
            "west", minModelX, minModelY, minModelZ, minModelX, maxModelY, maxModelZ, -1.0F, 0.0F, 0.0F, baseUvU, baseUvV, inflatedSizeX, inflatedSizeY, inflatedSizeZ, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[1] = createCubeFaceQuadWithAtlasUv(
            "east", maxModelX, minModelY, minModelZ, maxModelX, maxModelY, maxModelZ, 1.0F, 0.0F, 0.0F, baseUvU, baseUvV, inflatedSizeX, inflatedSizeY, inflatedSizeZ, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[2] = createCubeFaceQuadWithAtlasUv(
            "down", minModelX, minModelY, minModelZ, maxModelX, minModelY, maxModelZ, 0.0F, -1.0F, 0.0F, baseUvU, baseUvV, inflatedSizeX, inflatedSizeY, inflatedSizeZ, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[3] = createCubeFaceQuadWithAtlasUv(
            "up", minModelX, maxModelY, minModelZ, maxModelX, maxModelY, maxModelZ, 0.0F, 1.0F, 0.0F, baseUvU, baseUvV, inflatedSizeX, inflatedSizeY, inflatedSizeZ, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[4] = createCubeFaceQuadWithAtlasUv(
            "north", minModelX, minModelY, minModelZ, maxModelX, maxModelY, minModelZ, 0.0F, 0.0F, -1.0F, baseUvU, baseUvV, inflatedSizeX, inflatedSizeY, inflatedSizeZ, textureWidth, textureHeight
         );
         cubePose.II1Il11l111II11IIl[5] = createCubeFaceQuadWithAtlasUv(
            "south", minModelX, minModelY, maxModelZ, maxModelX, maxModelY, maxModelZ, 0.0F, 0.0F, 1.0F, baseUvU, baseUvV, inflatedSizeX, inflatedSizeY, inflatedSizeZ, textureWidth, textureHeight
         );
      }

      return cubePose;
   }

   private static ModelQuad readJsonFace(
      JsonObject faceUvDefinitions,
      String faceName,
      float vertexStartX,
      float vertexStartY,
      float vertexStartZ,
      float vertexEndX,
      float vertexEndY,
      float vertexEndZ,
      float faceNormalX,
      float faceNormalY,
      float faceNormalZ,
      int textureWidth,
      int textureHeight
   ) {
      if (!faceUvDefinitions.has(faceName)) {
         return null;
      }

      JsonObject faceUvJson = faceUvDefinitions.getAsJsonObject(faceName);
      JsonArray uvOrigin = faceUvJson.getAsJsonArray("uv");
      JsonArray uvSize = faceUvJson.getAsJsonArray("uv_size");
      return createCubeFaceQuad(
         faceName,
         vertexStartX,
         vertexStartY,
         vertexStartZ,
         vertexEndX,
         vertexEndY,
         vertexEndZ,
         faceNormalX,
         faceNormalY,
         faceNormalZ,
         uvOrigin.get(0).getAsFloat() / textureWidth,
         uvOrigin.get(1).getAsFloat() / textureHeight,
         uvSize.get(0).getAsFloat() / textureWidth,
         uvSize.get(1).getAsFloat() / textureHeight
      );
   }

   private static ModelQuad createCubeFaceQuadWithAtlasUv(
      String faceName,
      float vertexStartX,
      float vertexStartY,
      float vertexStartZ,
      float vertexEndX,
      float vertexEndY,
      float vertexEndZ,
      float faceNormalX,
      float faceNormalY,
      float faceNormalZ,
      float baseUvU,
      float baseUvV,
      float cubeWidth,
      float cubeHeight,
      float cubeDepth,
      float textureWidth,
      float textureHeight
   ) {
      float faceUvU;
      float faceUvV;
      float faceUvWidth;
      float faceUvHeight;
      switch (faceName) {
         case "north":
            faceUvU = baseUvU + cubeDepth + cubeWidth;
            faceUvV = baseUvV + cubeDepth;
            faceUvWidth = cubeWidth;
            faceUvHeight = cubeHeight;
            break;
         case "south":
            faceUvU = baseUvU + cubeDepth + cubeWidth + cubeDepth;
            faceUvV = baseUvV + cubeDepth;
            faceUvWidth = cubeWidth;
            faceUvHeight = cubeHeight;
            break;
         case "east":
            faceUvU = baseUvU;
            faceUvV = baseUvV + cubeDepth;
            faceUvWidth = cubeDepth;
            faceUvHeight = cubeHeight;
            break;
         case "west":
            faceUvU = baseUvU + cubeDepth + cubeWidth;
            faceUvV = baseUvV + cubeDepth;
            faceUvWidth = cubeDepth;
            faceUvHeight = cubeHeight;
            break;
         case "up":
            faceUvU = baseUvU + cubeDepth;
            faceUvV = baseUvV;
            faceUvWidth = cubeWidth;
            faceUvHeight = cubeDepth;
            break;
         default:
            faceUvU = baseUvU + cubeDepth + cubeWidth;
            faceUvV = baseUvV;
            faceUvWidth = cubeWidth;
            faceUvHeight = cubeDepth;
      }

      return createCubeFaceQuad(
         faceName,
         vertexStartX,
         vertexStartY,
         vertexStartZ,
         vertexEndX,
         vertexEndY,
         vertexEndZ,
         faceNormalX,
         faceNormalY,
         faceNormalZ,
         faceUvU / textureWidth,
         faceUvV / textureHeight,
         faceUvWidth / textureWidth,
         faceUvHeight / textureHeight
      );
   }

   private static ModelQuad createCubeFaceQuad(
      String faceName,
      float vertexStartX,
      float vertexStartY,
      float vertexStartZ,
      float vertexEndX,
      float vertexEndY,
      float vertexEndZ,
      float faceNormalX,
      float faceNormalY,
      float faceNormalZ,
      float uvStartU,
      float uvStartV,
      float uvSizeU,
      float uvSizeV
   ) {
      float uvEndU = uvStartU + uvSizeU;
      float uvEndV = uvStartV + uvSizeV;
      float[] vertexPositions;
      float[] vertexUvs;
      switch (faceName) {
         case "north":
            vertexPositions = new float[]{vertexEndX, vertexEndY, vertexStartZ, vertexStartX, vertexEndY, vertexStartZ, vertexStartX, vertexStartY, vertexStartZ, vertexEndX, vertexStartY, vertexStartZ};
            vertexUvs = new float[]{uvStartU, uvStartV, uvEndU, uvStartV, uvEndU, uvEndV, uvStartU, uvEndV};
            break;
         case "south":
            vertexPositions = new float[]{vertexStartX, vertexEndY, vertexEndZ, vertexEndX, vertexEndY, vertexEndZ, vertexEndX, vertexStartY, vertexEndZ, vertexStartX, vertexStartY, vertexEndZ};
            vertexUvs = new float[]{uvStartU, uvStartV, uvEndU, uvStartV, uvEndU, uvEndV, uvStartU, uvEndV};
            break;
         case "east":
            vertexPositions = new float[]{vertexEndX, vertexEndY, vertexEndZ, vertexEndX, vertexEndY, vertexStartZ, vertexEndX, vertexStartY, vertexStartZ, vertexEndX, vertexStartY, vertexEndZ};
            vertexUvs = new float[]{uvStartU, uvStartV, uvEndU, uvStartV, uvEndU, uvEndV, uvStartU, uvEndV};
            break;
         case "west":
            vertexPositions = new float[]{vertexStartX, vertexEndY, vertexStartZ, vertexStartX, vertexEndY, vertexEndZ, vertexStartX, vertexStartY, vertexEndZ, vertexStartX, vertexStartY, vertexStartZ};
            vertexUvs = new float[]{uvStartU, uvStartV, uvEndU, uvStartV, uvEndU, uvEndV, uvStartU, uvEndV};
            break;
         case "up":
            vertexPositions = new float[]{vertexStartX, vertexEndY, vertexStartZ, vertexStartX, vertexEndY, vertexEndZ, vertexEndX, vertexEndY, vertexEndZ, vertexEndX, vertexEndY, vertexStartZ};
            vertexUvs = new float[]{uvStartU, uvStartV, uvStartU, uvEndV, uvEndU, uvEndV, uvEndU, uvStartV};
            break;
         default:
            vertexPositions = new float[]{vertexEndX, vertexStartY, vertexStartZ, vertexEndX, vertexStartY, vertexEndZ, vertexStartX, vertexStartY, vertexEndZ, vertexStartX, vertexStartY, vertexStartZ};
            vertexUvs = new float[]{uvStartU, uvStartV, uvStartU, uvEndV, uvEndU, uvEndV, uvEndU, uvStartV};
      }

      return new ModelQuad(vertexPositions, vertexUvs, faceNormalX, faceNormalY, faceNormalZ);
   }

   private static float[] getFloatArray(JsonObject definitionJson, String propertyName, float[] defaultValues) {
      if (!definitionJson.has(propertyName)) {
         return defaultValues;
      }

      JsonArray jsonValues = definitionJson.getAsJsonArray(propertyName);
      float[] values = new float[jsonValues.size()];

      for (int valueIndex = 0; valueIndex < jsonValues.size(); valueIndex++) {
         values[valueIndex] = jsonValues.get(valueIndex).getAsFloat();
      }

      return values;
   }
}

