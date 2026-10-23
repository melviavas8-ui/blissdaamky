package dev.daamky.mixin.client;

import dev.daamky.ShulkerPreview;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class ShulkerPreviewMixin {
    private static final String p3260ff53 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   protected @Nullable Slot focusedSlot;
// https://t.me/pasterplace

   @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
   private void daamky$shulkerPreview(DrawContext drawContext, int mouseX, int mouseY, CallbackInfo callbackInfo) {
      if (this.focusedSlot != null && this.focusedSlot.hasStack() && ShulkerPreview.requestPreview(this.focusedSlot.getStack(), mouseX, mouseY)) {
         callbackInfo.cancel();
      }
   }
}

