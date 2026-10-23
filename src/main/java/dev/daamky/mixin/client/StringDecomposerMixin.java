package dev.daamky.mixin.client;

import dev.daamky.NameProtect;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextVisitFactory.class)
public abstract class StringDecomposerMixin {
    private static final String pa5365b69 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @ModifyVariable(
      method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private static String daamky$protectFormatted(String sourceText, String formattedText, int limit, Style style, Style resetStyle, CharacterVisitor visitor) {
      return limit == 0 ? NameProtect.II1Il11l111II11IIl(sourceText) : sourceText;
   }
// https://t.me/pasterplace

   @ModifyVariable(
      method = "visitForwards(Ljava/lang/String;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private static String daamky$protectPlain(String plainText) {
      return NameProtect.II1Il11l111II11IIl(plainText);
   }
}

