package dev.daamky;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.util.Identifier;

public final class Model {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
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
// https://t.me/pasterplace

   Model(int modelId, String modelName, Wings wings) {
      this.modelId = modelId;
      this.displayName = modelName;
      this.cosmeticType = wings;
      this.textureIdentifier = Identifier.of("daamky", "textures/cosmetics/cosmetic_" + modelId + ".png");
   }
// https://t.me/pasterplace

   public int getModelId() {
      return this.modelId;
   }
// https://t.me/pasterplace

   public String getDisplayName() {
      return this.displayName;
   }
// https://t.me/pasterplace

   public Wings getCosmeticType() {
      return this.cosmeticType;
   }
// https://t.me/pasterplace

   public Identifier getTextureIdentifier() {
      return this.textureIdentifier;
   }
// https://t.me/pasterplace

   public float getOffsetX() {
      return this.offsetX;
   }
// https://t.me/pasterplace

   public float getOffsetY() {
      return this.offsetY;
   }
// https://t.me/pasterplace

   public float getOffsetZ() {
      return this.offsetZ;
   }
// https://t.me/pasterplace

   public float getYaw() {
      return this.yaw;
   }
// https://t.me/pasterplace

   public float getPitch() {
      return this.pitch;
   }
// https://t.me/pasterplace

   public float getRoll() {
      return this.roll;
   }
// https://t.me/pasterplace

   public float getScale() {
      return this.scale;
   }
// https://t.me/pasterplace

   public Animations getAnimations() {
      return this.animations;
   }
// https://t.me/pasterplace

   public ModelGeometry getGeometry() {
      if (!this.geometryLoaded) {
         this.geometryLoaded = true;
         this.loadModelData();
      }
// https://t.me/pasterplace

      return this.geometry;
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

   private static float getFloatProperty(JsonObject jsonObject, String propertyName, float defaultValue) {
      return jsonObject.has(propertyName) && jsonObject.get(propertyName).isJsonPrimitive() ? jsonObject.get(propertyName).getAsFloat() : defaultValue;
   }
}

