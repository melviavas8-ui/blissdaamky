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
   @ModifyVariable(
      method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private static String daamky$protectFormatted(String sourceText, String formattedText, int limit, Style style, Style resetStyle, CharacterVisitor visitor) {
      return limit == 0 ? NameProtect.II1Il11l111II11IIl(sourceText) : sourceText;
   }

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

