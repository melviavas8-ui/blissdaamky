package dev.daamky;

import dev.daamky.mixin.client.MinecraftAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;

final class BotInputController {
    private static final String pf9d21094 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MinecraftClient client;
   private final KeyBindingStateSnapshot keyBindingState;
   private final Input keyboardInput;
// https://t.me/pasterplace

   BotInputController(MinecraftClient client, KeyBindingStateSnapshot keyBindingState) {
      this.client = client;
      this.keyBindingState = keyBindingState;
      this.keyboardInput = new KeyboardInput(client.options);
   }
// https://t.me/pasterplace

   Input keyboardInput() {
      return this.keyboardInput;
   }
// https://t.me/pasterplace

   void setMovementKeysPressed(boolean pressed) {
      this.keyBindingState.setUsePressed(pressed);
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

   void updateCrosshairAndAttack() {
      if (this.client.currentScreen == null) {
         this.client.gameRenderer.updateCrosshairTarget(1.0F);
         MinecraftAccessor client = (MinecraftAccessor)this.client;
         if (this.client.options.attackKey.isPressed()) {
            int previousAttackCooldown = this.client.attackCooldown;
            this.client.attackCooldown = 0;
// https://t.me/pasterplace

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

