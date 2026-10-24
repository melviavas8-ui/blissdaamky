package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventUpdate extends Event {
   private static final EventUpdate INSTANCE = new EventUpdate();

   private EventUpdate() {
   }

   public static EventUpdate get() {
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }
}

