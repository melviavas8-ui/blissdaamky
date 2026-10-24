package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventTick extends Event {
   private static final EventTick INSTANCE = new EventTick();

   private EventTick() {
   }

   public static EventTick get() {
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }
}

