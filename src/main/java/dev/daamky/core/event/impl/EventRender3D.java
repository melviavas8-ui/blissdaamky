package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.util.math.Vec3d;

public final class EventRender3D extends Event {
   private static final EventRender3D INSTANCE = new EventRender3D();
   private float partialTicks;
   private Vec3d cameraPos = Vec3d.ZERO;
   private float delta;

   private EventRender3D() {
   }

   public static EventRender3D get(float partialTicks, Vec3d cameraPosition, float delta) {
      INSTANCE.partialTicks = partialTicks;
      INSTANCE.cameraPos = cameraPosition;
      INSTANCE.delta = delta;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }

   public float partialTicks() {
      return this.partialTicks;
   }

   public Vec3d cameraPos() {
      return this.cameraPos;
   }

   public float delta() {
      return this.delta;
   }
}

