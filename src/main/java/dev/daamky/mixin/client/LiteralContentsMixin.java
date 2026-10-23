package dev.daamky.mixin.client;

import dev.daamky.NameProtect;
import java.util.Optional;
import net.minecraft.text.Style;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.text.StringVisitable.StyledVisitor;
import net.minecraft.text.StringVisitable.Visitor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Literal.class)
public abstract class LiteralContentsMixin {
    private static final String pc3a1bd40 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   @Final
   private String string;
// https://t.me/pasterplace

   @Inject(method = "visit(Lnet/minecraft/text/StringVisitable$Visitor;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
   private <T> void daamky$protectVisit(Visitor<T> visitor, CallbackInfoReturnable<Optional<T>> callbackInfo) {
      String protectedText = NameProtect.II1Il11l111II11IIl(this.string);
      if (protectedText != this.string) {
         callbackInfo.setReturnValue(visitor.accept(protectedText));
      }
   }
// https://t.me/pasterplace

   @Inject(
      method = "visit(Lnet/minecraft/text/StringVisitable$StyledVisitor;Lnet/minecraft/text/Style;)Ljava/util/Optional;",
      at = @At("HEAD"),
      cancellable = true
   )
   private <T> void daamky$protectStyledVisit(StyledVisitor<T> styledVisitor, Style style, CallbackInfoReturnable<Optional<T>> callbackInfo) {
      String protectedText = NameProtect.II1Il11l111II11IIl(this.string);
      if (protectedText != this.string) {
         callbackInfo.setReturnValue(styledVisitor.accept(style, protectedText));
      }
   }
}

