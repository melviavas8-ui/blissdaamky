package dev.daamky;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientConfigManager {
   private static final ClientConfigManager INSTANCE = new ClientConfigManager();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Path CONFIG_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("daamky");
   private final Path CONFIG_FILE = this.CONFIG_DIRECTORY.resolve("config.json");

   private ClientConfigManager() {
   }

   public static ClientConfigManager getInstance() {
      return INSTANCE;
   }

   public synchronized void saveConfig() {
      JsonObject configData = this.createConfigJson();

      try {
         Files.createDirectories(this.CONFIG_DIRECTORY);
         Path temporaryConfigPath = this.CONFIG_DIRECTORY.resolve("config.json.tmp");
         Files.writeString(temporaryConfigPath, GSON.toJson(configData), StandardCharsets.UTF_8);

         try {
            Files.move(temporaryConfigPath, this.CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException | AccessDeniedException atomicMoveException) {
            Files.move(temporaryConfigPath, this.CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (IOException configWriteException) {
         configWriteException.printStackTrace();
      }
   }

   public synchronized JsonObject createConfigJson() {
      JsonObject rootConfig = new JsonObject();
      JsonObject moduleConfigs = new JsonObject();

      for (Object module : ModuleRegistry.getInstance().getModules()) {
         JsonObject moduleConfig = new JsonObject();
         moduleConfig.addProperty("enabled", ((ModuleStructure)module).isEnabled());
         moduleConfig.addProperty("bind", ((ModuleStructure)module).getKeybind());
         JsonObject moduleSettings = new JsonObject();

         for (Object setting : ((ModuleStructure)module).getSettings()) {
            serializeSetting(moduleSettings, (Setting<?>)setting);
         }

         moduleConfig.add("settings", moduleSettings);
         moduleConfigs.add(((ModuleStructure)module).getName(), moduleConfig);
      }

      rootConfig.add("modules", moduleConfigs);
      rootConfig.add("theme", Accent.getInstance().toJson());
      rootConfig.add("cosmetics", Cape.getInstance().serializeSelection());
      JsonArray friendNames = new JsonArray();
      FriendManager.getInstance().getFriendNames().forEach(friendNames::add);
      rootConfig.add("friends", friendNames);
      rootConfig.add("bots", SavedBots.serializeSettings());
      rootConfig.add("macros", Key.getInstance().toJson());
      return rootConfig;
   }

   public synchronized void loadConfig() {
      if (Files.isRegularFile(this.CONFIG_FILE)) {
         JsonObject loadedConfig;
         try {
            loadedConfig = JsonParser.parseString(Files.readString(this.CONFIG_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
         } catch (IOException | RuntimeException configReadException) {
            configReadException.printStackTrace();

            try {
               Files.copy(this.CONFIG_FILE, this.CONFIG_DIRECTORY.resolve("config.json.broken"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException configBackupException) {
            }

            return;
         }

         this.applyConfig(loadedConfig);
      }
   }

   public synchronized void applyConfig(JsonObject rootConfig) {
      if (rootConfig.has("friends") && rootConfig.get("friends").isJsonArray()) {
         FriendManager.getInstance().clearFriends();

          for (JsonElement friendEntry : rootConfig.getAsJsonArray("friends")) {
             if (friendEntry.isJsonPrimitive() && !friendEntry.getAsString().isBlank()) {
                FriendManager.getInstance().addFriend(friendEntry.getAsString().trim());
            }
         }
      }

      if (rootConfig.has("macros") && rootConfig.get("macros").isJsonArray()) {
         Key.getInstance().loadFromJson(rootConfig.getAsJsonArray("macros"));
      }

      if (rootConfig.has("bots") && rootConfig.get("bots").isJsonObject()) {
         SavedBots.loadSettings(rootConfig.getAsJsonObject("bots"));
      }

      if (rootConfig.has("theme") && rootConfig.get("theme").isJsonObject()) {
         Accent.getInstance().fromJson(rootConfig.getAsJsonObject("theme"));
      }

      if (rootConfig.has("cosmetics") && rootConfig.get("cosmetics").isJsonObject()) {
         Cape.getInstance().loadSelection(rootConfig.getAsJsonObject("cosmetics"));
      }

      if (rootConfig.has("modules") && rootConfig.get("modules").isJsonObject()) {
         JsonObject moduleConfigs = rootConfig.getAsJsonObject("modules");
         migrateLegacyServerHelperSettings(moduleConfigs);

         for (Object module : ModuleRegistry.getInstance().getModules()) {
            if (moduleConfigs.has(((ModuleStructure)module).getName())
               && moduleConfigs.get(((ModuleStructure)module).getName()).isJsonObject()) {
               JsonObject moduleConfig = moduleConfigs.getAsJsonObject(((ModuleStructure)module).getName());
               if (moduleConfig.has("bind")) {
                  ((ModuleStructure)module).setKeybind(moduleConfig.get("bind").getAsInt());
               } else if (moduleConfig.has("key")) {
                  ((ModuleStructure)module).setKeybind(moduleConfig.get("key").getAsInt());
               } else if (moduleConfig.has("settings")
                  && moduleConfig.get("settings").isJsonObject()
                  && (moduleConfig.getAsJsonObject("settings").has("Bind") || moduleConfig.getAsJsonObject("settings").has("Бинд"))) {
                  JsonObject legacyKeybindSettings = moduleConfig.getAsJsonObject("settings");
                  ((ModuleStructure)module).setKeybind((legacyKeybindSettings.has("Bind") ? legacyKeybindSettings.get("Bind") : legacyKeybindSettings.get("Бинд")).getAsInt());
               }

               if (moduleConfig.has("settings") && moduleConfig.get("settings").isJsonObject()) {
                  JsonObject moduleSettings = moduleConfig.getAsJsonObject("settings");

                  for (Object setting : ((ModuleStructure)module).getSettings()) {
                     applySetting(moduleSettings, (Setting<?>)setting);
                  }
               }

               if (moduleConfig.has("enabled")) {
                  try {
                     ((ModuleStructure)module).setEnabled(moduleConfig.get("enabled").getAsBoolean());
                  } catch (RuntimeException enabledValueException) {
                     enabledValueException.printStackTrace();
                  }
               }
            }
         }
      }
   }

   private static void serializeSetting(JsonObject settingsJson, Setting<?> setting) {
      if (setting instanceof MultiToggleSetting multiToggleSetting) {
         JsonObject optionValuesJson = new JsonObject();

         for (Object optionSetting : multiToggleSetting.getOptions()) {
            optionValuesJson.addProperty(((BooleanSetting)optionSetting).getName(), ((BooleanSetting)optionSetting).getValue());
         }

         settingsJson.add(setting.getName(), optionValuesJson);
      } else if (setting instanceof BooleanSetting booleanSetting) {
         settingsJson.addProperty(setting.getName(), booleanSetting.getValue());
      } else if (setting instanceof FloatSetting floatSetting) {
         settingsJson.addProperty(setting.getName(), floatSetting.getValue());
      } else if (setting instanceof MultiOptionSetting optionSetting) {
         settingsJson.addProperty(setting.getName(), optionSetting.getValue());
      } else if (setting instanceof IntSetting integerSetting) {
         settingsJson.addProperty(setting.getName(), integerSetting.getValue());
      } else if (setting instanceof ColorSetting colorSetting) {
         settingsJson.addProperty(setting.getName(), colorSetting.getValue());
      }
   }

   private static void applySetting(JsonObject settingsJson, Setting<?> setting) {
      if (settingsJson.has(setting.getName())) {
         JsonElement settingValue = settingsJson.get(setting.getName());

         try {
            if (setting instanceof MultiToggleSetting multiToggleSetting && settingValue.isJsonObject()) {
               JsonObject optionValues = settingValue.getAsJsonObject();

               for (Object optionToggle : multiToggleSetting.getOptions()) {
                  if (optionValues.has(((BooleanSetting)optionToggle).getName())) {
                     ((BooleanSetting)optionToggle).setValue(optionValues.get(((BooleanSetting)optionToggle).getName()).getAsBoolean());
                  }
               }
            } else if (setting instanceof BooleanSetting booleanSetting) {
               booleanSetting.setValue(settingValue.getAsBoolean());
            } else if (setting instanceof FloatSetting floatSetting && Float.isFinite(settingValue.getAsFloat())) {
               floatSetting.setValue(Float.valueOf(Math.clamp(settingValue.getAsFloat(), floatSetting.minimum(), floatSetting.maximum())));
            } else if (setting instanceof MultiOptionSetting optionSetting) {
               String optionName = settingValue.getAsString();
               if (optionSetting.getOptions().contains(optionName)) {
                  optionSetting.setValue(optionName);
               }
            } else if (setting instanceof IntSetting integerSetting) {
               integerSetting.setValue(Integer.valueOf(settingValue.getAsInt()));
            } else if (setting instanceof ColorSetting colorSetting) {
               String colorHexValue = settingValue.getAsString();
               colorSetting.updatePickerAnimation(Integer.parseInt(colorHexValue.startsWith("#") ? colorHexValue.substring(1) : colorHexValue, 16));
            }
         } catch (RuntimeException invalidSettingValue) {
            invalidSettingValue.printStackTrace();
         }
      }
   }

   private static void migrateLegacyServerHelperSettings(JsonObject moduleConfigs) {
      if (!moduleConfigs.has("ServerHelper")) {
         JsonObject migratedSettings = new JsonObject();
         String[][] legacySettingMappings = new String[][]{
            {"ReallyWorldHelper", "Бинд ловушки", "Ловушка"},
            {"ReallyWorldHelper", "Бинд эндер-ловушки", "Эндер Ловушка"},
            {"ReallyWorldHelper", "Бинд анти-полёта", "Анти-полёт"},
            {"ReallyWorldHelper", "Закрывать меню при входе", "Закрывать меню при входе"},
            {"ReallyWorldHelper", "Фильтр чата", "Фильтр чата"}
         };

         for (String[] legacySettingMapping : legacySettingMappings) {
            JsonObject legacyModuleSettings = getModuleSettings(moduleConfigs, legacySettingMapping[0]);
            if (legacyModuleSettings != null && legacyModuleSettings.has(legacySettingMapping[1])) {
               migratedSettings.add(legacySettingMapping[2], legacyModuleSettings.get(legacySettingMapping[1]));
            }
         }

         for (String sourceModuleName : new String[]{"FunTimeHelper", "HolyWorldHelper", "LonyGriefHelper"}) {
            JsonObject sourceSettings = getModuleSettings(moduleConfigs, sourceModuleName);
            if (sourceSettings != null) {
               for (Object settingName : sourceSettings.keySet()) {
                  if (!settingName.equals("Бинд") && !migratedSettings.has((String)settingName)) {
                     migratedSettings.add((String)settingName, sourceSettings.get((String)settingName));
                  }
               }
            }
         }

         if (migratedSettings.size() != 0) {
            JsonObject serverHelperConfig = new JsonObject();
            serverHelperConfig.addProperty("enabled", true);
            serverHelperConfig.add("settings", migratedSettings);
            moduleConfigs.add("ServerHelper", serverHelperConfig);
         }
      }
   }

   private static JsonObject getModuleSettings(JsonObject rootConfig, String moduleName) {
      if (rootConfig.has(moduleName) && rootConfig.get(moduleName).isJsonObject()) {
         JsonObject moduleConfig = rootConfig.getAsJsonObject(moduleName);
         return moduleConfig.has("settings") && moduleConfig.get("settings").isJsonObject() ? moduleConfig.getAsJsonObject("settings") : null;
      } else {
         return null;
      }
   }
}

