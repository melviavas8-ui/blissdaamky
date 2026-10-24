package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.util.math.Vec3d;

public final class EventElytraVelocity extends Event {
   private final Vec3d previous;
   private Vec3d velocity;

   public EventElytraVelocity(Vec3d previousVelocity, Vec3d velocity) {
      this.previous = previousVelocity;
      this.velocity = velocity;
   }

   public Vec3d previous() {
      return this.previous;
   }

   public Vec3d velocity() {
      return this.velocity;
   }

   public void setVelocity(Vec3d velocity) {
      this.velocity = velocity;
   }
}

