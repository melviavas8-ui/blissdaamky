package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventInput extends Event {
   private static final EventInput INSTANCE = new EventInput();
   private float forward;
   private float strafe;
   private boolean jump;
   private boolean sneak;
   private boolean sprint;

   private EventInput() {
   }

   public static EventInput get(float forwardInput, float strafeInput, boolean jumpPressed, boolean sneakPressed, boolean sprintPressed) {
      INSTANCE.forward = forwardInput;
      INSTANCE.strafe = strafeInput;
      INSTANCE.jump = jumpPressed;
      INSTANCE.sneak = sneakPressed;
      INSTANCE.sprint = sprintPressed;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }

   public float forward() {
      return this.forward;
   }

   public float strafe() {
      return this.strafe;
   }

   public boolean jump() {
      return this.jump;
   }

   public boolean sneak() {
      return this.sneak;
   }

   public boolean sprint() {
      return this.sprint;
   }

   public void setForward(float forward) {
      this.forward = forward;
   }

   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   public void setJump(boolean jump) {
      this.jump = jump;
   }

   public void setSneak(boolean sneak) {
      this.sneak = sneak;
   }

   public void setSprint(boolean sprint) {
      this.sprint = sprint;
   }
}

