package dev.daamky;

public final class BooleanSetting extends Setting<Boolean> {
    private static final String pa4d2b434 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private float animationProgress;
// https://t.me/pasterplace

   public BooleanSetting(String name, boolean initialValue) {
      super(name, initialValue);
      this.animationProgress = initialValue ? 1.0F : 0.0F;
   }
// https://t.me/pasterplace

   public void toggle() {
      this.setValue(!this.getValue());
   }
// https://t.me/pasterplace

   public void updateToggleProgress(float delta) {
      float targetProgress = this.getValue() ? 1.0F : 0.0F;
      this.animationProgress = this.animationProgress + (targetProgress - this.animationProgress) * (1.0F - (float)Math.exp(-5.5F * delta));
      if (this.getValue() && this.animationProgress > 0.999F) {
         this.animationProgress = 1.0F;
      } else if (!this.getValue() && this.animationProgress < 0.001F) {
         this.animationProgress = 0.0F;
      }
   }
// https://t.me/pasterplace

   public float getToggleProgress() {
      return this.animationProgress;
   }
}

