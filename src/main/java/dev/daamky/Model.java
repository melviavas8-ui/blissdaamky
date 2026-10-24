package dev.daamky;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.util.Identifier;

public final class Model {
   private final int modelId;
   private final String displayName;
   private final Wings cosmeticType;
   private final Identifier textureIdentifier;
   private float offsetX;
   private float offsetY;
   private float offsetZ;
   private float yaw;
   private float pitch;
   private float roll;
   private float scale = 1.0F;
   private boolean geometryLoaded;
   private ModelGeometry geometry;
   private Animations animations;

   Model(int modelId, String modelName, Wings wings) {
      this.modelId = modelId;
      this.displayName = modelName;
      this.cosmeticType = wings;
      this.textureIdentifier = Identifier.of("daamky", "textures/cosmetics/cosmetic_" + modelId + ".png");
   }

   public int getModelId() {
      return this.modelId;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public Wings getCosmeticType() {
      return this.cosmeticType;
   }

   public Identifier getTextureIdentifier() {
      return this.textureIdentifier;
   }

   public float getOffsetX() {
      return this.offsetX;
   }

   public float getOffsetY() {
      return this.offsetY;
   }

   public float getOffsetZ() {
      return this.offsetZ;
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getRoll() {
      return this.roll;
   }

   public float getScale() {
      return this.scale;
   }

   public Animations getAnimations() {
      return this.animations;
   }

   public ModelGeometry getGeometry() {
      if (!this.geometryLoaded) {
         this.geometryLoaded = true;
         this.loadModelData();
      }

      return this.geometry;
   }

   private void loadModelData() {
      try (InputStream resourceAsStream = Model.class.getResourceAsStream(Cape.getModelResourcePath(this.modelId))) {
         if (resourceAsStream != null) {
            JsonObject asJsonObject = JsonParser.parseString(new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            if (asJsonObject.has("model")) {
               this.offsetX = getFloatProperty(asJsonObject, "x", 0.0F);
               this.offsetY = getFloatProperty(asJsonObject, "y", 0.0F);
               this.offsetZ = getFloatProperty(asJsonObject, "z", 0.0F);
               this.yaw = getFloatProperty(asJsonObject, "yaw", 0.0F);
               this.pitch = getFloatProperty(asJsonObject, "pitch", 0.0F);
               this.roll = getFloatProperty(asJsonObject, "roll", 0.0F);
               this.scale = getFloatProperty(asJsonObject, "scale", 1.0F);
               this.geometry = Description.parseGeometry(asJsonObject.getAsJsonObject("model"));
               if (asJsonObject.has("animation") && asJsonObject.get("animation").isJsonObject()) {
                  this.animations = Animations.fromJson(asJsonObject.getAsJsonObject("animation"));
               }
            }
         }
      } catch (Exception modelParseFailure) {
         this.geometry = null;
         this.animations = null;
      }
   }

   private static float getFloatProperty(JsonObject jsonObject, String propertyName, float defaultValue) {
      return jsonObject.has(propertyName) && jsonObject.get(propertyName).isJsonPrimitive() ? jsonObject.get(propertyName).getAsFloat() : defaultValue;
   }
}

