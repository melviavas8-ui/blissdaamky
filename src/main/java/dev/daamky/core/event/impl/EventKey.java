package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventKey extends Event {
    private static final String pf0fc2093 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final int key;
   private final boolean mouse;
// https://t.me/pasterplace

   public EventKey(int keyCode, boolean mouseInput) {
      this.key = keyCode;
      this.mouse = mouseInput;
   }
// https://t.me/pasterplace

   public int key() {
      return this.key;
   }
// https://t.me/pasterplace

   public boolean isMouse() {
      return this.mouse;
   }
}

