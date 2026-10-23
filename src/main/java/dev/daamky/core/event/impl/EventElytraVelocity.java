package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.util.math.Vec3d;

public final class EventElytraVelocity extends Event {
    private static final String p534f93b0 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final Vec3d previous;
   private Vec3d velocity;
// https://t.me/pasterplace

   public EventElytraVelocity(Vec3d previousVelocity, Vec3d velocity) {
      this.previous = previousVelocity;
      this.velocity = velocity;
   }
// https://t.me/pasterplace

   public Vec3d previous() {
      return this.previous;
   }
// https://t.me/pasterplace

   public Vec3d velocity() {
      return this.velocity;
   }
// https://t.me/pasterplace

   public void setVelocity(Vec3d velocity) {
      this.velocity = velocity;
   }
}

