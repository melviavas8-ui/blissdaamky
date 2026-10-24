package dev.daamky;

import java.util.List;

public class MultiOptionSetting extends Setting<String> {
   private final List<String> options;
   private final float[] optionProgress;
   private final float[] hoverProgress;

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

   public List<String> getOptions() {
      return this.options;
   }

   public boolean isSelected(String option) {
      return this.getValue().equals(option);
   }

   public int getSelectedIndex() {
      int selectedOptionIndex = this.options.indexOf(this.getValue());
      return selectedOptionIndex >= 0 ? selectedOptionIndex : 0;
   }

   public void setSelectedIndex(int selectedOptionIndex) {
      if (selectedOptionIndex >= 0 && selectedOptionIndex < this.options.size()) {
         this.setValue(this.options.get(selectedOptionIndex));
      }
   }

   public void updateAnimation(float frameDelta) {
      int selectedOptionIndex = this.getSelectedIndex();

      for (int optionIndex = 0; optionIndex < this.optionProgress.length; optionIndex++) {
         float targetProgress = optionIndex == selectedOptionIndex ? 1.0F : 0.0F;
         this.optionProgress[optionIndex] = this.optionProgress[optionIndex] + (targetProgress - this.optionProgress[optionIndex]) * (1.0F - (float)Math.exp(-5.5F * frameDelta));
         if (Math.abs(targetProgress - this.optionProgress[optionIndex]) < 0.001F) {
            this.optionProgress[optionIndex] = targetProgress;
         }
      }
   }

   public float getOptionProgress(int optionIndex) {
      return optionIndex >= 0 && optionIndex < this.optionProgress.length ? this.optionProgress[optionIndex] : 0.0F;
   }

   public float getOptionHoverProgress(int optionIndex) {
      return optionIndex >= 0 && optionIndex < this.hoverProgress.length ? this.hoverProgress[optionIndex] : 0.0F;
   }

   public void setOptionHoverProgress(int optionIndex, float hoverProgress) {
      if (optionIndex >= 0 && optionIndex < this.hoverProgress.length) {
         this.hoverProgress[optionIndex] = hoverProgress;
      }
   }
}

