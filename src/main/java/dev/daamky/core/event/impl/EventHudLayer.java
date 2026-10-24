package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.client.gui.DrawContext;

public final class EventHudLayer extends Event {
   private static final EventHudLayer INSTANCE = new EventHudLayer();
   private DrawContext graphics;

   private EventHudLayer() {
   }

   public static EventHudLayer get(DrawContext drawContext) {
      INSTANCE.graphics = drawContext;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }

   public DrawContext graphics() {
      return this.graphics;
   }
}

