package dev.daamky;

public final class Stopwatch {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private long startTimeMillis = System.currentTimeMillis();
// https://t.me/pasterplace

   public boolean hasElapsed(long timeoutMillis) {
      return System.currentTimeMillis() - this.startTimeMillis >= timeoutMillis;
   }
// https://t.me/pasterplace

   public boolean hasElapsed(long timeoutMillis, boolean resetOnElapsed) {
      if (this.hasElapsed(timeoutMillis)) {
         if (resetOnElapsed) {
            this.reset();
         }
// https://t.me/pasterplace

         return true;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   public long getElapsedMillis() {
      return System.currentTimeMillis() - this.startTimeMillis;
   }
// https://t.me/pasterplace

   public void reset() {
      this.startTimeMillis = System.currentTimeMillis();
   }
// https://t.me/pasterplace

   public void setElapsedMillis(long elapsedMillis) {
      this.startTimeMillis = System.currentTimeMillis() - elapsedMillis;
   }
}

