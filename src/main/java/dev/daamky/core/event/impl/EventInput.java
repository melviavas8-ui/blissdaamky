package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;

public final class EventInput extends Event {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final EventInput INSTANCE = new EventInput();
   private float forward;
   private float strafe;
   private boolean jump;
   private boolean sneak;
   private boolean sprint;
// https://t.me/pasterplace

   private EventInput() {
   }
// https://t.me/pasterplace

   public static EventInput get(float forwardInput, float strafeInput, boolean jumpPressed, boolean sneakPressed, boolean sprintPressed) {
      INSTANCE.forward = forwardInput;
      INSTANCE.strafe = strafeInput;
      INSTANCE.jump = jumpPressed;
      INSTANCE.sneak = sneakPressed;
      INSTANCE.sprint = sprintPressed;
      INSTANCE.setCancelled(false);
      return INSTANCE;
   }
// https://t.me/pasterplace

   public float forward() {
      return this.forward;
   }
// https://t.me/pasterplace

   public float strafe() {
      return this.strafe;
   }
// https://t.me/pasterplace

   public boolean jump() {
      return this.jump;
   }
// https://t.me/pasterplace

   public boolean sneak() {
      return this.sneak;
   }
// https://t.me/pasterplace

   public boolean sprint() {
      return this.sprint;
   }
// https://t.me/pasterplace

   public void setForward(float forward) {
      this.forward = forward;
   }
// https://t.me/pasterplace

   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }
// https://t.me/pasterplace

   public void setJump(boolean jump) {
      this.jump = jump;
   }
// https://t.me/pasterplace

   public void setSneak(boolean sneak) {
      this.sneak = sneak;
   }
// https://t.me/pasterplace

   public void setSprint(boolean sprint) {
      this.sprint = sprint;
   }
}

