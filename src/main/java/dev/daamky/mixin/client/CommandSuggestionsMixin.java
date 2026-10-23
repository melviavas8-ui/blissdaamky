package dev.daamky.mixin.client;

import com.mojang.brigadier.suggestion.Suggestions;
import dev.daamky.Friend;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatInputSuggestor.class)
public abstract class CommandSuggestionsMixin {
    private static final String p3967d4fb = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   @Final
   private TextFieldWidget textField;
   @Shadow
   @Final
   private List<OrderedText> messages;
   @Shadow
   private CompletableFuture<Suggestions> pendingSuggestions;
   @Shadow
   private boolean completingSuggestions;
// https://t.me/pasterplace

   @Shadow
   public abstract void show(boolean visible);
// https://t.me/pasterplace

   @Shadow
   public abstract void clearWindow();
// https://t.me/pasterplace

   @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
   private void daamky$clientCommands(CallbackInfo callbackInfo) {
      String commandText = this.textField.getText();
      if (commandText.startsWith(".")) {
         callbackInfo.cancel();
         if (!this.completingSuggestions) {
            this.textField.setSuggestion(null);
            this.clearWindow();
         }
// https://t.me/pasterplace

         this.messages.clear();
         int cursorIndex = this.textField.getCursor();
         this.pendingSuggestions = Friend.suggestCommands(commandText, cursorIndex);
         if (!this.completingSuggestions && !this.pendingSuggestions.join().isEmpty()) {
            this.show(false);
         }
      }
   }
}

