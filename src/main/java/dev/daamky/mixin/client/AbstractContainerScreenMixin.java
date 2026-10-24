package dev.daamky.mixin.client;

import dev.daamky.ItemScroller;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class AbstractContainerScreenMixin {
   @Shadow
   protected abstract Slot getSlotAt(double mouseX, double mouseY);

   @Inject(method = "mouseClicked", at = @At("HEAD"))
   private void daamky$scrollerClick(Click click, boolean doubleClick, CallbackInfoReturnable<Boolean> callbackInfo) {
      ItemScroller.II1Il11l111II11IIl((HandledScreen<?>)(Object)this, this.getSlotAt(click.x(), click.y()));
   }

   @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
   private void daamky$scrollerDrag(Click click, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> callbackInfo) {
      if (ItemScroller.II1Il11l111II11IIl((HandledScreen<?>)(Object)this, click, this.getSlotAt(click.x(), click.y()))) {
         callbackInfo.setReturnValue(true);
      }
   }
}

