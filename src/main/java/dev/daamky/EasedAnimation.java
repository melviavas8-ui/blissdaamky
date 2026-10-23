package dev.daamky;

public final class EasedAnimation {
    private static final String pb2134adb = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private float progress = 0.0F;
   private boolean running = false;
   private float frameDeltaSeconds = 0.016F;
   private long lastUpdateNanos = System.nanoTime();
// https://t.me/pasterplace

   public void start() {
      this.running = true;
   }
// https://t.me/pasterplace

   public void stop() {
      this.running = false;
   }
// https://t.me/pasterplace

   public boolean isRunning() {
      return this.running;
   }
// https://t.me/pasterplace

   public boolean isFinished() {
      return !this.running && this.progress <= 0.001F;
   }
// https://t.me/pasterplace

   public void update() {
      long currentTimeNanos = System.nanoTime();
      float deltaSeconds = (float)(currentTimeNanos - this.lastUpdateNanos) / 1.0E9F;
      this.lastUpdateNanos = currentTimeNanos;
      deltaSeconds = Math.clamp(deltaSeconds, 0.001F, 0.05F);
      this.frameDeltaSeconds = deltaSeconds;
      float targetProgress = this.running ? 1.0F : 0.0F;
      float animationSpeed = this.running ? 5.2F : 8.2F;
      this.progress = this.progress + (targetProgress - this.progress) * (1.0F - (float)Math.exp(-animationSpeed * deltaSeconds));
      if (this.running && this.progress > 0.9995F) {
         this.progress = 1.0F;
      } else if (!this.running && this.progress < 0.003F) {
         this.progress = 0.0F;
      }
   }
// https://t.me/pasterplace

   public float frameDeltaSeconds() {
      return this.frameDeltaSeconds;
   }
// https://t.me/pasterplace

   public float progress() {
      return this.progress;
   }
// https://t.me/pasterplace

   public float easedProgress() {
      float clampedProgress = Math.clamp(this.progress, 0.0F, 1.0F);
      return 1.0F - (float)Math.pow(1.0F - clampedProgress, 3.0);
   }
}

