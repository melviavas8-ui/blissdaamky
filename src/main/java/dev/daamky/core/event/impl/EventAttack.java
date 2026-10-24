package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.entity.Entity;

public final class EventAttack extends Event {
   private static boolean lastCancelled;
   private final Entity target;
   private final boolean post;

   public static boolean consumeCancelled() {
      boolean cancelled = lastCancelled;
      lastCancelled = false;
      return cancelled;
   }

   public static void markCancelled() {
      lastCancelled = true;
   }

   public EventAttack(Entity target, boolean post) {
      this.target = target;
      this.post = post;
   }

   public Entity target() {
      return this.target;
   }

   public boolean isPre() {
      return !this.post;
   }

   public boolean isPost() {
      return this.post;
   }
}

