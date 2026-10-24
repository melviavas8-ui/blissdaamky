package dev.daamky;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class Cape {
   private static final int MAX_COSMETIC_MODELS = 128;
   private static final Cape INSTANCE = new Cape();
   private final List<Model> allModels = new ArrayList<>();
   private final Map<Wings, Model> equippedModelsByCategory = new EnumMap<>(Wings.class);

   private Cape() {
      for (int cosmeticId = 0; cosmeticId < 128; cosmeticId++) {
         try (InputStream resourceAsStream = Cape.class.getResourceAsStream(getModelResourcePath(cosmeticId))) {
            if (resourceAsStream != null) {
               JsonObject asJsonObject = JsonParser.parseString(new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
               String modelName = asJsonObject.has("name") ? asJsonObject.get("name").getAsString() : "Cosmetic " + cosmeticId;
               String cosmeticType = asJsonObject.has("type") ? asJsonObject.get("type").getAsString() : "";
               Wings wings = classifyCosmetic(cosmeticId, modelName, cosmeticType);
               if (wings != null) {
                  this.allModels.add(new Model(cosmeticId, formatCosmeticName(modelName), wings));
               }
            }
         } catch (Exception resourceLoadException) {
         }
      }
   }

   public static Cape getInstance() {
      return INSTANCE;
   }

   static String getModelResourcePath(int cosmeticId) {
      return "/assets/daamky/cosmetics/models/cosmetic_" + cosmeticId + ".json";
   }

   public List<Model> getAllModels() {
      return this.allModels;
   }

   public List<Model> getModelsByCategory(Wings wings) {
      if (wings == null) {
         return this.allModels;
      }

      ArrayList arrayList = new ArrayList();

      for (Object model : this.allModels) {
         if (((Model)model).getCosmeticType() == wings) {
            arrayList.add(model);
         }
      }

      return arrayList;
   }

   public Collection<Model> getEquippedModels() {
      return this.equippedModelsByCategory.values();
   }

   public int getEquippedModelCount() {
      return this.equippedModelsByCategory.size();
   }

   public Model getEquippedModel(Wings wings) {
      return this.equippedModelsByCategory.get(wings);
   }

   public boolean isEquipped(Model model) {
      return this.equippedModelsByCategory.get(model.getCosmeticType()) == model;
   }

   public void toggleEquippedModel(Model model) {
      if (this.isEquipped(model)) {
         this.equippedModelsByCategory.remove(model.getCosmeticType());
      } else {
         this.equippedModelsByCategory.put(model.getCosmeticType(), model);
         model.getGeometry();
      }
   }

   public void clearEquippedModels() {
      this.equippedModelsByCategory.clear();
   }

   public JsonObject serializeSelection() {
      JsonObject jsonObject = new JsonObject();

      for (java.util.Map.Entry<?, ?> equippedModelEntry : this.equippedModelsByCategory.entrySet()) {
         jsonObject.addProperty(((Wings)equippedModelEntry.getKey()).getIdentifier(), ((Model)equippedModelEntry.getValue()).getModelId());
      }

      return jsonObject;
   }

   public Map<Wings, Model> parseSelection(JsonObject jsonObject) {
      EnumMap enumMap = new EnumMap<>(Wings.class);
      if (jsonObject == null) {
         return enumMap;
      }

      for (java.util.Map.Entry<?, ?> selectionEntry : jsonObject.entrySet()) {
         Wings wings = Wings.fromIdentifier((String)selectionEntry.getKey());
         if (wings != null && ((JsonElement)selectionEntry.getValue()).isJsonPrimitive()) {
            int asInt = ((JsonElement)selectionEntry.getValue()).getAsInt();

            for (Object model : this.allModels) {
               if (((Model)model).getModelId() == asInt && ((Model)model).getCosmeticType() == wings) {
                  enumMap.put(wings, model);
                  break;
               }
            }
         }
      }

      return enumMap;
   }

   public void loadSelection(JsonObject jsonObject) {
      this.equippedModelsByCategory.clear();

      for (java.util.Map.Entry<?, ?> selectionEntry : jsonObject.entrySet()) {
         Wings wings = Wings.fromIdentifier((String)selectionEntry.getKey());
         if (wings != null && ((JsonElement)selectionEntry.getValue()).isJsonPrimitive()) {
            int asInt = ((JsonElement)selectionEntry.getValue()).getAsInt();

            for (Object model : this.allModels) {
               if (((Model)model).getModelId() == asInt && ((Model)model).getCosmeticType() == wings) {
                  this.equippedModelsByCategory.put(wings, (Model)model);
                  break;
               }
            }
         }
      }
   }

   private static String formatCosmeticName(String modelFileName) {
      String displayName = modelFileName.replaceFirst("^[a-z0-9]+_", "").replace('_', ' ').trim();
      return displayName.isEmpty() ? modelFileName : displayName;
   }

   private static Wings classifyCosmetic(int cosmeticId, String displayName, String cosmeticType) {
      String lowercaseName = displayName.toLowerCase();
      String lowercaseType = cosmeticType.toLowerCase();
      if (lowercaseType.equals("watch")) {
         return Wings.WATCHES;
      } else if (lowercaseType.equals("cape") || lowercaseName.contains("cape")) {
         return null;
      } else if (lowercaseType.equals("wings") || lowercaseName.contains("wing")) {
         return Wings.WINGS;
      } else if (lowercaseType.equals("pet") || lowercaseName.contains("pet") || lowercaseName.contains("bee") || lowercaseName.contains("radish")) {
         return Wings.PETS;
      } else if (lowercaseType.equals("hat")
         || lowercaseName.contains("hat")
         || lowercaseName.contains("nimb")
         || lowercaseName.contains("crown")
         || lowercaseName.contains("horn")
         || lowercaseName.contains("helm")
         || lowercaseName.contains("mask")) {
         return Wings.HATS;
      } else if (lowercaseType.equals("bodywear") || lowercaseName.contains("back") || lowercaseName.contains("katana") || lowercaseName.contains("bag")) {
         return Wings.BACK;
      } else if (cosmeticId >= 50) {
         return Wings.HATS;
      } else {
         return cosmeticId >= 38 ? Wings.PETS : Wings.BACK;
      }
   }
}

