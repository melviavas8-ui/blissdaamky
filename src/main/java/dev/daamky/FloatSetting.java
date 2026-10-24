package dev.daamky;

import java.util.Locale;

public final class FloatSetting extends Setting<Float> {
   private final float minimum;
   private final float maximum;
   private final float step;
   private final String unit;
   private float sliderProgress;

   public FloatSetting(String name, float defaultValue, float minimum, float maximum, float stepSize, String unit) {
      super(name, defaultValue);
      this.minimum = minimum;
      this.maximum = maximum;
      this.step = stepSize;
      this.unit = unit;
      this.sliderProgress = this.normalizeValue(defaultValue);
   }

   public FloatSetting(String name, float defaultValue, float minimum, float maximum, float stepSize) {
      this(name, defaultValue, minimum, maximum, stepSize, "");
   }

   private float normalizeValue(float value) {
      return this.maximum <= this.minimum
         ? 0.0F
         : Math.clamp((value - this.minimum) / (this.maximum - this.minimum), 0.0F, 1.0F);
   }

   public float minimum() {
      return this.minimum;
   }

   public float maximum() {
      return this.maximum;
   }

   public float step() {
      return this.step;
   }

   public String unit() {
      return this.unit;
   }

   public void setSliderProgress(float progress) {
      progress = Math.clamp(progress, 0.0F, 1.0F);
      float value = this.minimum + progress * (this.maximum - this.minimum);
      if (this.step > 0.0F) {
         value = this.minimum + Math.round((value - this.minimum) / this.step) * this.step;
         value = Math.round(value * 1000.0F) / 1000.0F;
      }

      value = Math.clamp(value, this.minimum, this.maximum);
      this.setValue(Float.valueOf(value));
   }

   public void updateAnimation(float frameDelta) {
      float targetProgress = this.normalizeValue(this.getValue());
      this.sliderProgress = this.sliderProgress + (targetProgress - this.sliderProgress) * (1.0F - (float)Math.exp(-6.0F * frameDelta));
   }

   public float sliderProgress() {
      return this.sliderProgress;
   }

   public String formatValue() {
      float value = this.getValue();
      String formattedValue;
      if (this.step >= 1.0F && value == (float)((long)value)) {
         formattedValue = String.format("%d", (long)value);
      } else if (this.step != 0.01F && this.step != 0.05F) {
         formattedValue = value == (float)((long)value) ? String.format("%d", (long)value) : String.format(Locale.ROOT, "%.1f", value);
      } else {
         formattedValue = String.format(Locale.ROOT, "%.2f", value);
      }

      return this.unit != null && !this.unit.isEmpty() ? formattedValue + " " + this.unit : formattedValue;
   }
}

