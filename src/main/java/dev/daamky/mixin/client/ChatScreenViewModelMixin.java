package dev.daamky.mixin.client;

import dev.daamky.ViewModel;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenViewModelMixin {
   @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
   private void daamky$viewModelScroll(
      double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> callbackInfo
   ) {
      if (ViewModel.handleChatScroll(verticalAmount)) {
         callbackInfo.setReturnValue(true);
      }
   }
}
