package dev.daamky;

public final class Stopwatch {
   private long startTimeMillis = System.currentTimeMillis();

   public boolean hasElapsed(long timeoutMillis) {
      return System.currentTimeMillis() - this.startTimeMillis >= timeoutMillis;
   }

   public boolean hasElapsed(long timeoutMillis, boolean resetOnElapsed) {
      if (this.hasElapsed(timeoutMillis)) {
         if (resetOnElapsed) {
            this.reset();
         }

         return true;
      } else {
         return false;
      }
   }

   public long getElapsedMillis() {
      return System.currentTimeMillis() - this.startTimeMillis;
   }

   public void reset() {
      this.startTimeMillis = System.currentTimeMillis();
   }

   public void setElapsedMillis(long elapsedMillis) {
      this.startTimeMillis = System.currentTimeMillis() - elapsedMillis;
   }
}

