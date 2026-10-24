package dev.daamky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.MinecraftClient;

public abstract class ModuleStructure {
   protected static final MinecraftClient CLIENT = MinecraftClient.getInstance();
   private final String moduleName;
   private final String description;
   private final ModuleCategory category;
   private boolean enabled;
   private final IntSetting keybindSetting = new IntSetting("Bind", 0);
   private float animationProgress;
   private float layoutOffset;
   private final List<Setting<?>> settings = new ArrayList<>();

   public ModuleStructure(String moduleName, String moduleDescription, ModuleCategory moduleCategory) {
      this.moduleName = moduleName;
      this.description = moduleDescription;
      this.category = moduleCategory;
      this.enabled = false;
      this.animationProgress = 0.0F;
   }

   public String getName() {
      return this.moduleName;
   }

   public String getDescription() {
      return this.description;
   }

   public ModuleCategory getCategory() {
      return this.category;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

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

   public boolean getEnabledState() {
      return this.enabled;
   }

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

   public void invokeEnableCallback() {
      this.onEnable();
   }

   public void toggle() {
      this.setEnabled(!this.enabled);
      ClientSounds.playModuleToggleSound(this);
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   protected boolean hasActiveClientWorld() {
      return CLIENT.player != null && CLIENT.world != null && CLIENT.interactionManager != null;
   }

   public String getDisplayInfo() {
      return null;
   }

   public void addSetting(Setting<?> setting) {
      this.settings.add(setting);
   }

   public void addSettings(Setting<?>... settings) {
      Collections.addAll(this.settings, settings);
   }

   public List<Setting<?>> getSettings() {
      return Collections.unmodifiableList(this.settings);
   }

   public int getKeybind() {
      return this.keybindSetting.getValue();
   }

   public void setKeybind(int keybind) {
      this.keybindSetting.setValue(Integer.valueOf(keybind));
   }

   public IntSetting getKeybindSetting() {
      return this.keybindSetting;
   }

   public void updateAnimation(float deltaSeconds) {
      float targetProgress = this.enabled ? 1.0F : 0.0F;
      this.animationProgress = this.animationProgress + (targetProgress - this.animationProgress) * (1.0F - (float)Math.exp(-5.5F * deltaSeconds));
      if (this.enabled && this.animationProgress > 0.999F) {
         this.animationProgress = 1.0F;
      } else if (!this.enabled && this.animationProgress < 0.001F) {
         this.animationProgress = 0.0F;
      }

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

   public float getAnimationProgress() {
      return this.animationProgress;
   }

   public float getLayoutOffset() {
      return this.layoutOffset;
   }

   public void setLayoutOffset(float offset) {
      this.layoutOffset = offset;
   }
}

