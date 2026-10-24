package dev.daamky;

public final class BooleanSetting extends Setting<Boolean> {
   private float animationProgress;

   public BooleanSetting(String name, boolean initialValue) {
      super(name, initialValue);
      this.animationProgress = initialValue ? 1.0F : 0.0F;
   }

   public void toggle() {
      this.setValue(!this.getValue());
   }

   public void updateToggleProgress(float delta) {
      float targetProgress = this.getValue() ? 1.0F : 0.0F;
      this.animationProgress = this.animationProgress + (targetProgress - this.animationProgress) * (1.0F - (float)Math.exp(-5.5F * delta));
      if (this.getValue() && this.animationProgress > 0.999F) {
         this.animationProgress = 1.0F;
      } else if (!this.getValue() && this.animationProgress < 0.001F) {
         this.animationProgress = 0.0F;
      }
   }

   public float getToggleProgress() {
      return this.animationProgress;
   }
}

