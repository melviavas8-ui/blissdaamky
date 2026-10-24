package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.client.gui.DrawContext;

public final class EventDisplay extends Event {
   private static final EventDisplay INSTANCE = new EventDisplay();
   private DrawContext graphics;

   private EventDisplay() {
   }

   public static EventDisplay get(DrawContext drawContext) {
      INSTANCE.graphics = drawContext;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }

   public DrawContext graphics() {
      return this.graphics;
   }
}

