package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.util.math.Vec3d;

public final class EventRender3D extends Event {
    private static final String pa8355e77 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final EventRender3D INSTANCE = new EventRender3D();
   private float partialTicks;
   private Vec3d cameraPos = Vec3d.ZERO;
   private float delta;
// https://t.me/pasterplace

   private EventRender3D() {
   }
// https://t.me/pasterplace

   public static EventRender3D get(float partialTicks, Vec3d cameraPosition, float delta) {
      INSTANCE.partialTicks = partialTicks;
      INSTANCE.cameraPos = cameraPosition;
      INSTANCE.delta = delta;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }
// https://t.me/pasterplace

   public float partialTicks() {
      return this.partialTicks;
   }
// https://t.me/pasterplace

   public Vec3d cameraPos() {
      return this.cameraPos;
   }
// https://t.me/pasterplace

   public float delta() {
      return this.delta;
   }
}

