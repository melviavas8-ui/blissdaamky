package dev.daamky.mixin.client;

import dev.daamky.ClientInitializer;
import dev.daamky.core.event.impl.EventInput;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    private static final String pe59638fa = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "tick()V", at = @At("TAIL"))
   private void daamky$onTick(CallbackInfo callbackInfo) {
      if (ClientInitializer.getEventBus() != null) {
         PlayerInput playerInput = this.playerInput;
         float forwardInput = playerInput.forward() == playerInput.backward() ? 0.0F : (playerInput.forward() ? 1.0F : -1.0F);
         float strafeInput = playerInput.left() == playerInput.right() ? 0.0F : (playerInput.left() ? 1.0F : -1.0F);
         EventInput eventInput = ClientInitializer.getEventBus().post(EventInput.get(forwardInput, strafeInput, playerInput.jump(), playerInput.sneak(), playerInput.sprint()));
         float normalizedForward = Math.signum(eventInput.forward());
         float normalizedStrafe = Math.signum(eventInput.strafe());
         if (normalizedForward != forwardInput || normalizedStrafe != strafeInput || eventInput.jump() != playerInput.jump() || eventInput.sneak() != playerInput.sneak() || eventInput.sprint() != playerInput.sprint()) {
            this.playerInput = new PlayerInput(normalizedForward > 0.0F, normalizedForward < 0.0F, normalizedStrafe > 0.0F, normalizedStrafe < 0.0F, eventInput.jump(), eventInput.sneak(), eventInput.sprint());
            this.movementVector = new Vec2f(normalizedStrafe, normalizedForward).normalize();
         }
      }
   }
}

