package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventPostTick extends Event {
   private static final EventPostTick INSTANCE = new EventPostTick();

   private EventPostTick() {
   }

   public static EventPostTick get() {
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }
}

