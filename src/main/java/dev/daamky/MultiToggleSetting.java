package dev.daamky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MultiToggleSetting extends Setting<Boolean> {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final List<BooleanSetting> options = new ArrayList<>();
   private float toggleProgress;
// https://t.me/pasterplace

   public MultiToggleSetting(String name, BooleanSetting... initialOptions) {
      super(name, false);
      Collections.addAll(this.options, initialOptions);
   }
// https://t.me/pasterplace

   public MultiToggleSetting addOption(String optionName, boolean initiallyEnabled) {
      this.options.add(new BooleanSetting(optionName, initiallyEnabled));
      return this;
   }
// https://t.me/pasterplace

   public List<BooleanSetting> getOptions() {
      return Collections.unmodifiableList(this.options);
   }
// https://t.me/pasterplace

   public BooleanSetting getOption(String optionName) {
      for (Object optionSetting : this.options) {
         if (((BooleanSetting)optionSetting).getName().equalsIgnoreCase(optionName)) {
            return (BooleanSetting)optionSetting;
         }
      }
// https://t.me/pasterplace

      return null;
   }
// https://t.me/pasterplace

   public boolean isOptionEnabled(String optionName) {
      BooleanSetting optionSetting = this.getOption(optionName);
      return optionSetting != null && optionSetting.getValue();
   }
// https://t.me/pasterplace

   public int getEnabledCount() {
      int enabledCount = 0;

      for (Object optionSetting : this.options) {
         if (((BooleanSetting)optionSetting).getValue()) {
            enabledCount++;
         }
      }

      return enabledCount;
   }
// https://t.me/pasterplace

   public boolean isEnabled() {
      return this.getValue();
   }
// https://t.me/pasterplace

   public void toggle() {
      this.setValue(!this.getValue());
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

   public float getToggleProgress() {
      return this.toggleProgress;
   }
}

