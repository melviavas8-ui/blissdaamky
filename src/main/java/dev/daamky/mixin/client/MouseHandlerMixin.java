package dev.daamky.mixin.client;

import dev.daamky.ClientInitializer;
import dev.daamky.NeuroRotationController;
import dev.daamky.FreeCamera;
import dev.daamky.MouseButtonState;
import dev.daamky.core.event.impl.EventKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseHandlerMixin {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   @Final
   private MinecraftClient client;
// https://t.me/pasterplace

   @Inject(method = "onMouseButton(JLnet/minecraft/client/input/MouseInput;I)V", at = @At("HEAD"))
   private void daamky$onButton(long windowHandle, MouseInput mouseInput, int action, CallbackInfo callbackInfo) {
      if (windowHandle == this.client.getWindow().getHandle()) {
         MouseButtonState.recordButtonState(mouseInput.button(), action);
      }

      if (action == 1 && this.client.inGameHud != null && this.client.currentScreen == null && this.client.player != null) {
         if (windowHandle != 0L && windowHandle == this.client.getWindow().getHandle()) {
            if (ClientInitializer.getEventBus() != null) {
               ClientInitializer.getEventBus().post(new EventKey(mouseInput.button(), true));
            }
         }
      }
   }
// https://t.me/pasterplace

   @Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
   private void daamky$freeCameraTurn(ClientPlayerEntity player, double deltaX, double deltaY) {
      if (FreeCamera.getEnabledInstance() == null) {
         NeuroRotationController.accumulateMouseDelta(deltaX, deltaY);
      }
// https://t.me/pasterplace

      FreeCamera freeCamera = FreeCamera.getEnabledInstance();
      if (freeCamera != null) {
         freeCamera.changeLookDirection(deltaX, deltaY);
      } else {
         player.changeLookDirection(deltaX, deltaY);
      }
   }
}

