package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventKey extends Event {
   private final int key;
   private final boolean mouse;

   public EventKey(int keyCode, boolean mouseInput) {
      this.key = keyCode;
      this.mouse = mouseInput;
   }

   public int key() {
      return this.key;
   }

   public boolean isMouse() {
      return this.mouse;
   }
}

