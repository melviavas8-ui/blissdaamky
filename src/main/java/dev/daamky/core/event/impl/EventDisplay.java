package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.client.gui.DrawContext;

public final class EventDisplay extends Event {
    private static final String pc4fdfb90 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final EventDisplay INSTANCE = new EventDisplay();
   private DrawContext graphics;
// https://t.me/pasterplace

   private EventDisplay() {
   }
// https://t.me/pasterplace

   public static EventDisplay get(DrawContext drawContext) {
      INSTANCE.graphics = drawContext;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }
// https://t.me/pasterplace

   public DrawContext graphics() {
      return this.graphics;
   }
}

