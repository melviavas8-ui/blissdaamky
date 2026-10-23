package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventMotion extends Event {
    private static final String p80616ea8 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final EventMotion INSTANCE = new EventMotion();
   private float yaw;
   private float pitch;
   private boolean post;
// https://t.me/pasterplace

   private EventMotion() {
   }
// https://t.me/pasterplace

   public static EventMotion pre(float yaw, float pitch) {
      INSTANCE.yaw = yaw;
      INSTANCE.pitch = pitch;
      INSTANCE.post = false;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }
// https://t.me/pasterplace

   public static EventMotion post(float yaw, float pitch) {
      INSTANCE.yaw = yaw;
      INSTANCE.pitch = pitch;
      INSTANCE.post = true;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }
// https://t.me/pasterplace

   public float yaw() {
      return this.yaw;
   }
// https://t.me/pasterplace

   public float pitch() {
      return this.pitch;
   }
// https://t.me/pasterplace

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
// https://t.me/pasterplace

   public void setPitch(float pitch) {
      this.pitch = pitch;
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

