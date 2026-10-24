package dev.daamky;

public final class NotificationEntry {
   private final String title;
   private final String message;
   private boolean visible;
   private float animationProgress;

   public NotificationEntry(String title, String message, boolean visible) {
      this.title = title;
      this.message = message;
      this.visible = visible;
      this.animationProgress = visible ? 1.0F : 0.0F;
   }

   public String getTitle() {
      return this.title;
   }

   public String getMessage() {
      return this.message;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void toggleVisibility() {
      this.visible = !this.visible;
   }

   public void updateAnimation(float deltaSeconds) {
      float targetProgress = this.visible ? 1.0F : 0.0F;
      this.animationProgress = this.animationProgress + (targetProgress - this.animationProgress) * (1.0F - (float)Math.exp(-18.0F * deltaSeconds));
      if (this.visible && this.animationProgress > 0.999F) {
         this.animationProgress = 1.0F;
      } else if (!this.visible && this.animationProgress < 0.001F) {
         this.animationProgress = 0.0F;
      }
   }

   public float getAnimationProgress() {
      return this.animationProgress;
   }
}

