package dev.daamky;

import dev.daamky.mixin.client.MinecraftAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;

final class BotInputController {
   private final MinecraftClient client;
   private final KeyBindingStateSnapshot keyBindingState;
   private final Input keyboardInput;

   BotInputController(MinecraftClient client, KeyBindingStateSnapshot keyBindingState) {
      this.client = client;
      this.keyBindingState = keyBindingState;
      this.keyboardInput = new KeyboardInput(client.options);
   }

   Input keyboardInput() {
      return this.keyboardInput;
   }

   void setMovementKeysPressed(boolean pressed) {
      this.keyBindingState.setUsePressed(pressed);
   }

   void runWithKeyBindingsRestored(Runnable runnable) {
      KeyBindingStateSnapshot previousKeyBindingState = KeyBindingStateSnapshot.capture(this.client);
      this.keyBindingState.restoreTo(this.client);

      try {
         runnable.run();
      } finally {
         this.keyBindingState.readFrom(this.client);
         previousKeyBindingState.restoreTo(this.client);
      }
   }

   void updateCrosshairAndAttack() {
      if (this.client.currentScreen == null) {
         this.client.gameRenderer.updateCrosshairTarget(1.0F);
         MinecraftAccessor client = (MinecraftAccessor)this.client;
         if (this.client.options.attackKey.isPressed()) {
            int previousAttackCooldown = this.client.attackCooldown;
            this.client.attackCooldown = 0;

            try {
               client.daamky$startAttack();
            } finally {
               this.client.attackCooldown = previousAttackCooldown;
            }
         }

         if (this.client.options.useKey.isPressed()) {
            client.daamky$startUseItem();
         }
      }
   }
}

