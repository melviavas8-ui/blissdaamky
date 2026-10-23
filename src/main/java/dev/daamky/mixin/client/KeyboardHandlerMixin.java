package dev.daamky.mixin.client;

import dev.daamky.ClientInitializer;
import dev.daamky.SavedBots;
import dev.daamky.Search;
import dev.daamky.core.event.impl.EventKey;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardHandlerMixin {
    private static final String p4dd69e3a = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   @Final
   private MinecraftClient client;
// https://t.me/pasterplace

   @Inject(method = "onKey(JILnet/minecraft/client/input/KeyInput;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$onKeyPress(long windowHandle, int action, KeyInput keyInput, CallbackInfo callbackInfo) {
      if (action == 1 && this.client != null && this.client.inGameHud != null && this.client.currentScreen == null) {
         if (SavedBots.isActiveSessionReady() && keyInput.getKeycode() == 256) {
            SavedBots.restorePreviousSession();
            callbackInfo.cancel();
         } else if (keyInput.getKeycode() == 344) {
            Search.getInstance().open();
            callbackInfo.cancel();
         } else {
            if (this.client.player != null && ClientInitializer.getEventBus() != null) {
               ClientInitializer.getEventBus().post(new EventKey(keyInput.getKeycode(), false));
            }
         }
      }
   }
}

