package dev.daamky;

import java.util.List;

public class MultiOptionSetting extends Setting<String> {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final List<String> options;
   private final float[] optionProgress;
   private final float[] hoverProgress;
// https://t.me/pasterplace

   public MultiOptionSetting(String name, String defaultOption, String... textArray) {
      super(name, defaultOption);
      this.options = List.of(textArray);
      this.optionProgress = new float[this.options.size()];
      this.hoverProgress = new float[this.options.size()];
      int defaultOptionIndex = this.options.indexOf(defaultOption);
      if (defaultOptionIndex >= 0) {
         this.optionProgress[defaultOptionIndex] = 1.0F;
      }
   }
// https://t.me/pasterplace

   public List<String> getOptions() {
      return this.options;
   }
// https://t.me/pasterplace

   public boolean isSelected(String option) {
      return this.getValue().equals(option);
   }
// https://t.me/pasterplace

   public int getSelectedIndex() {
      int selectedOptionIndex = this.options.indexOf(this.getValue());
      return selectedOptionIndex >= 0 ? selectedOptionIndex : 0;
   }
// https://t.me/pasterplace

   public void setSelectedIndex(int selectedOptionIndex) {
      if (selectedOptionIndex >= 0 && selectedOptionIndex < this.options.size()) {
         this.setValue(this.options.get(selectedOptionIndex));
      }
   }
// https://t.me/pasterplace

   public void updateAnimation(float frameDelta) {
      int selectedOptionIndex = this.getSelectedIndex();
// https://t.me/pasterplace

      for (int optionIndex = 0; optionIndex < this.optionProgress.length; optionIndex++) {
         float targetProgress = optionIndex == selectedOptionIndex ? 1.0F : 0.0F;
         this.optionProgress[optionIndex] = this.optionProgress[optionIndex] + (targetProgress - this.optionProgress[optionIndex]) * (1.0F - (float)Math.exp(-5.5F * frameDelta));
         if (Math.abs(targetProgress - this.optionProgress[optionIndex]) < 0.001F) {
            this.optionProgress[optionIndex] = targetProgress;
         }
      }
   }
// https://t.me/pasterplace

   public float getOptionProgress(int optionIndex) {
      return optionIndex >= 0 && optionIndex < this.optionProgress.length ? this.optionProgress[optionIndex] : 0.0F;
   }
// https://t.me/pasterplace

   public float getOptionHoverProgress(int optionIndex) {
      return optionIndex >= 0 && optionIndex < this.hoverProgress.length ? this.hoverProgress[optionIndex] : 0.0F;
   }
// https://t.me/pasterplace

   public void setOptionHoverProgress(int optionIndex, float hoverProgress) {
      if (optionIndex >= 0 && optionIndex < this.hoverProgress.length) {
         this.hoverProgress[optionIndex] = hoverProgress;
      }
   }
}

