package dev.daamky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MultiToggleSetting extends Setting<Boolean> {
   private final List<BooleanSetting> options = new ArrayList<>();
   private float toggleProgress;

   public MultiToggleSetting(String name, BooleanSetting... initialOptions) {
      super(name, false);
      Collections.addAll(this.options, initialOptions);
   }

   public MultiToggleSetting addOption(String optionName, boolean initiallyEnabled) {
      this.options.add(new BooleanSetting(optionName, initiallyEnabled));
      return this;
   }

   public List<BooleanSetting> getOptions() {
      return Collections.unmodifiableList(this.options);
   }

   public BooleanSetting getOption(String optionName) {
      for (Object optionSetting : this.options) {
         if (((BooleanSetting)optionSetting).getName().equalsIgnoreCase(optionName)) {
            return (BooleanSetting)optionSetting;
         }
      }

      return null;
   }

   public boolean isOptionEnabled(String optionName) {
      BooleanSetting optionSetting = this.getOption(optionName);
      return optionSetting != null && optionSetting.getValue();
   }

   public int getEnabledCount() {
      int enabledCount = 0;

      for (Object optionSetting : this.options) {
         if (((BooleanSetting)optionSetting).getValue()) {
            enabledCount++;
         }
      }

      return enabledCount;
   }

   public boolean isEnabled() {
      return this.getValue();
   }

   public void toggle() {
      this.setValue(!this.getValue());
   }

   public void updateAnimation(float frameDelta) {
      float targetProgress = this.isEnabled() ? 1.0F : 0.0F;
      this.toggleProgress = this.toggleProgress + (targetProgress - this.toggleProgress) * (1.0F - (float)Math.exp(-7.0F * frameDelta));
      if (Math.abs(targetProgress - this.toggleProgress) < 0.001F) {
         this.toggleProgress = targetProgress;
      }

      for (Object optionSetting : this.options) {
         ((BooleanSetting)optionSetting).updateToggleProgress(frameDelta);
      }
   }

   public float getToggleProgress() {
      return this.toggleProgress;
   }
}

