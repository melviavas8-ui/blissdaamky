package dev.daamky;

public final class NotificationEntry {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final String title;
   private final String message;
   private boolean visible;
   private float animationProgress;
// https://t.me/pasterplace

   public NotificationEntry(String title, String message, boolean visible) {
      this.title = title;
      this.message = message;
      this.visible = visible;
      this.animationProgress = visible ? 1.0F : 0.0F;
   }
// https://t.me/pasterplace

   public String getTitle() {
      return this.title;
   }
// https://t.me/pasterplace

   public String getMessage() {
      return this.message;
   }
// https://t.me/pasterplace

   public boolean isVisible() {
      return this.visible;
   }
// https://t.me/pasterplace

   public void toggleVisibility() {
      this.visible = !this.visible;
   }
// https://t.me/pasterplace

   public void updateAnimation(float deltaSeconds) {
      float targetProgress = this.visible ? 1.0F : 0.0F;
      this.animationProgress = this.animationProgress + (targetProgress - this.animationProgress) * (1.0F - (float)Math.exp(-18.0F * deltaSeconds));
      if (this.visible && this.animationProgress > 0.999F) {
         this.animationProgress = 1.0F;
      } else if (!this.visible && this.animationProgress < 0.001F) {
         this.animationProgress = 0.0F;
      }
   }
// https://t.me/pasterplace

   public float getAnimationProgress() {
      return this.animationProgress;
   }
}

