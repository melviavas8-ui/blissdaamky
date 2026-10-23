package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.entity.Entity;

public final class EventAttack extends Event {
    private static final String p77e9f656 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static boolean lastCancelled;
   private final Entity target;
   private final boolean post;
// https://t.me/pasterplace

   public static boolean consumeCancelled() {
      boolean cancelled = lastCancelled;
      lastCancelled = false;
      return cancelled;
   }
// https://t.me/pasterplace

   public static void markCancelled() {
      lastCancelled = true;
   }
// https://t.me/pasterplace

   public EventAttack(Entity target, boolean post) {
      this.target = target;
      this.post = post;
   }
// https://t.me/pasterplace

   public Entity target() {
      return this.target;
   }
// https://t.me/pasterplace

   public boolean isPre() {
      return !this.post;
   }
// https://t.me/pasterplace

   public boolean isPost() {
      return this.post;
   }
}

