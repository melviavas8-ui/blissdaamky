package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventMotion extends Event {
   private static final EventMotion INSTANCE = new EventMotion();
   private float yaw;
   private float pitch;
   private boolean post;

   private EventMotion() {
   }

   public static EventMotion pre(float yaw, float pitch) {
      INSTANCE.yaw = yaw;
      INSTANCE.pitch = pitch;
      INSTANCE.post = false;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }

   public static EventMotion post(float yaw, float pitch) {
      INSTANCE.yaw = yaw;
      INSTANCE.pitch = pitch;
      INSTANCE.post = true;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }

   public float yaw() {
      return this.yaw;
   }

   public float pitch() {
      return this.pitch;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public boolean isPre() {
      return !this.post;
   }

   public boolean isPost() {
      return this.post;
   }
}

