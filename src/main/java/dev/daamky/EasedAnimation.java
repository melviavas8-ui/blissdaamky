package dev.daamky;

public final class EasedAnimation {
   private float progress = 0.0F;
   private boolean running = false;
   private float frameDeltaSeconds = 0.016F;
   private long lastUpdateNanos = System.nanoTime();

   public void start() {
      this.running = true;
   }

   public void stop() {
      this.running = false;
   }

   public boolean isRunning() {
      return this.running;
   }

   public boolean isFinished() {
      return !this.running && this.progress <= 0.001F;
   }

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

   public float frameDeltaSeconds() {
      return this.frameDeltaSeconds;
   }

   public float progress() {
      return this.progress;
   }

   public float easedProgress() {
      float clampedProgress = Math.clamp(this.progress, 0.0F, 1.0F);
      return 1.0F - (float)Math.pow(1.0F - clampedProgress, 3.0);
   }
}

