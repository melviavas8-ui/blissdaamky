package dev.daamky.core.event;

public abstract class Event {
    private static final String p5a08e03e = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private boolean cancelled;
// https://t.me/pasterplace

   public boolean isCancelled() {
      return this.cancelled;
   }
// https://t.me/pasterplace

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
// https://t.me/pasterplace

   public void cancel() {
      this.cancelled = true;
   }
}

