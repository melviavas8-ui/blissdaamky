package dev.daamky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.MinecraftClient;

public abstract class ModuleStructure {
    private static final String TELEGRAM_CHANNEL_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   protected static final MinecraftClient CLIENT = MinecraftClient.getInstance();
   private final String moduleName;
   private final String description;
   private final ModuleCategory category;
   private boolean enabled;
   private final IntSetting keybindSetting = new IntSetting("Bind", 0);
   private float animationProgress;
   private float layoutOffset;
   private final List<Setting<?>> settings = new ArrayList<>();
// https://t.me/pasterplace

   public ModuleStructure(String moduleName, String moduleDescription, ModuleCategory moduleCategory) {
      this.moduleName = moduleName;
      this.description = moduleDescription;
      this.category = moduleCategory;
      this.enabled = false;
      this.animationProgress = 0.0F;
   }
// https://t.me/pasterplace

   public String getName() {
      return this.moduleName;
   }
// https://t.me/pasterplace

   public String getDescription() {
      return this.description;
   }
// https://t.me/pasterplace

   public ModuleCategory getCategory() {
      return this.category;
   }
// https://t.me/pasterplace

   public boolean isEnabled() {
      return this.enabled;
   }
// https://t.me/pasterplace

   public void setEnabled(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         if (enabled) {
            if (ClientInitializer.getEventBus() != null) {
               ClientInitializer.getEventBus().register(this);
            }

            this.onEnable();
         } else {
            if (ClientInitializer.getEventBus() != null) {
               ClientInitializer.getEventBus().unregister(this);
            }

            this.onDisable();
         }
      }
   }
// https://t.me/pasterplace

   public boolean getEnabledState() {
      return this.enabled;
   }
// https://t.me/pasterplace

   public void setEnabledWithoutCallbacks(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         if (ClientInitializer.getEventBus() != null) {
            if (enabled) {
               ClientInitializer.getEventBus().register(this);
            } else {
               ClientInitializer.getEventBus().unregister(this);
            }
         }
      }
   }
// https://t.me/pasterplace

   public void invokeEnableCallback() {
      this.onEnable();
   }
// https://t.me/pasterplace

   public void toggle() {
      this.setEnabled(!this.enabled);
      ClientSounds.playModuleToggleSound(this);
   }
// https://t.me/pasterplace

   protected void onEnable() {
   }
// https://t.me/pasterplace

   protected void onDisable() {
   }
// https://t.me/pasterplace

   protected boolean hasActiveClientWorld() {
      return CLIENT.player != null && CLIENT.world != null && CLIENT.interactionManager != null;
   }
// https://t.me/pasterplace

   public String getDisplayInfo() {
      return null;
   }
// https://t.me/pasterplace

   public void addSetting(Setting<?> setting) {
      this.settings.add(setting);
   }
// https://t.me/pasterplace

   public void addSettings(Setting<?>... settings) {
      Collections.addAll(this.settings, settings);
   }
// https://t.me/pasterplace

   public List<Setting<?>> getSettings() {
      return Collections.unmodifiableList(this.settings);
   }
// https://t.me/pasterplace

   public int getKeybind() {
      return this.keybindSetting.getValue();
   }
// https://t.me/pasterplace

   public void setKeybind(int keybind) {
      this.keybindSetting.setValue(Integer.valueOf(keybind));
   }
// https://t.me/pasterplace

   public IntSetting getKeybindSetting() {
      return this.keybindSetting;
   }
// https://t.me/pasterplace

   public void updateAnimation(float deltaSeconds) {
      float targetProgress = this.enabled ? 1.0F : 0.0F;
      this.animationProgress = this.animationProgress + (targetProgress - this.animationProgress) * (1.0F - (float)Math.exp(-5.5F * deltaSeconds));
      if (this.enabled && this.animationProgress > 0.999F) {
         this.animationProgress = 1.0F;
      } else if (!this.enabled && this.animationProgress < 0.001F) {
         this.animationProgress = 0.0F;
      }
// https://t.me/pasterplace

      for (Setting<?> setting : this.settings) {
         if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.updateToggleProgress(deltaSeconds);
         } else if (setting instanceof FloatSetting floatSetting) {
            floatSetting.updateAnimation(deltaSeconds);
         } else if (setting instanceof MultiOptionSetting multiOptionSetting) {
            multiOptionSetting.updateAnimation(deltaSeconds);
         } else if (setting instanceof MultiToggleSetting multiToggleSetting) {
            multiToggleSetting.updateAnimation(deltaSeconds);
         } else if (setting instanceof ColorSetting colorSetting) {
            colorSetting.updatePickerAnimation(deltaSeconds);
         }
      }
   }
// https://t.me/pasterplace

   public float getAnimationProgress() {
      return this.animationProgress;
   }
// https://t.me/pasterplace

   public float getLayoutOffset() {
      return this.layoutOffset;
   }
// https://t.me/pasterplace

   public void setLayoutOffset(float offset) {
      this.layoutOffset = offset;
   }
}

