package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventPostTick extends Event {
    private static final String pda8af161 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final EventPostTick INSTANCE = new EventPostTick();
// https://t.me/pasterplace

   private EventPostTick() {
   }
// https://t.me/pasterplace

   public static EventPostTick get() {
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }
}

