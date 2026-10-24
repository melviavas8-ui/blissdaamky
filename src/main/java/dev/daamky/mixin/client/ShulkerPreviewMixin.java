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
   @Shadow
   protected @Nullable Slot focusedSlot;

   @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
   private void daamky$shulkerPreview(DrawContext drawContext, int mouseX, int mouseY, CallbackInfo callbackInfo) {
      if (this.focusedSlot != null && this.focusedSlot.hasStack() && ShulkerPreview.requestPreview(this.focusedSlot.getStack(), mouseX, mouseY)) {
         callbackInfo.cancel();
      }
   }
}

