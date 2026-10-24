package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

final class KeyBindingStateSnapshot {
   private boolean forwardPressed;
   private boolean backPressed;
   private boolean leftPressed;
   private boolean rightPressed;
   private boolean jumpPressed;
   private boolean sneakPressed;
   private boolean sprintPressed;
   private boolean usePressed;
   private boolean attackPressed;

   static KeyBindingStateSnapshot capture(MinecraftClient client) {
      KeyBindingStateSnapshot snapshot = new KeyBindingStateSnapshot();
      snapshot.readFrom(client);
      return snapshot;
   }

   void readFrom(MinecraftClient client) {
      this.forwardPressed = isPressed(client.options.forwardKey);
      this.backPressed = isPressed(client.options.backKey);
      this.leftPressed = isPressed(client.options.leftKey);
      this.rightPressed = isPressed(client.options.rightKey);
      this.jumpPressed = isPressed(client.options.jumpKey);
      this.sneakPressed = isPressed(client.options.sneakKey);
      this.sprintPressed = isPressed(client.options.sprintKey);
      this.usePressed = isPressed(client.options.useKey);
      this.attackPressed = isPressed(client.options.attackKey);
   }

   void restoreTo(MinecraftClient client) {
      client.options.forwardKey.setPressed(this.forwardPressed);
      client.options.backKey.setPressed(this.backPressed);
      client.options.leftKey.setPressed(this.leftPressed);
      client.options.rightKey.setPressed(this.rightPressed);
      client.options.jumpKey.setPressed(this.jumpPressed);
      client.options.sneakKey.setPressed(this.sneakPressed);
      client.options.sprintKey.setPressed(this.sprintPressed);
      client.options.useKey.setPressed(this.usePressed);
      client.options.attackKey.setPressed(this.attackPressed);
   }

   void setUsePressed(boolean pressed) {
      this.usePressed = pressed;
   }

   void copyFrom(KeyBindingStateSnapshot snapshot) {
      if (snapshot == null) {
         this.clear();
      } else {
         this.forwardPressed = snapshot.forwardPressed;
         this.backPressed = snapshot.backPressed;
         this.leftPressed = snapshot.leftPressed;
         this.rightPressed = snapshot.rightPressed;
         this.jumpPressed = snapshot.jumpPressed;
         this.sneakPressed = snapshot.sneakPressed;
         this.sprintPressed = snapshot.sprintPressed;
         this.usePressed = snapshot.usePressed;
         this.attackPressed = snapshot.attackPressed;
      }
   }

   void clear() {
      this.forwardPressed = false;
      this.backPressed = false;
      this.leftPressed = false;
      this.rightPressed = false;
      this.jumpPressed = false;
      this.sneakPressed = false;
      this.sprintPressed = false;
      this.usePressed = false;
      this.attackPressed = false;
   }

   private static boolean isPressed(KeyBinding keyBinding) {
      return keyBinding != null && keyBinding.isPressed();
   }
}

