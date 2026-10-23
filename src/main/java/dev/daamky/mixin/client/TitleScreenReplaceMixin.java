package dev.daamky.mixin.client;

import dev.daamky.MainMenuScreen;
import dev.daamky.RenderPipelineCompatibility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftClient.class)
public abstract class TitleScreenReplaceMixin {
    private static final String pf87ce687 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
   private Screen daamky$replaceTitle(Screen currentScreen) {
      if (!(currentScreen instanceof TitleScreen)) {
         return currentScreen;
      }
// https://t.me/pasterplace

      try {
         return RenderPipelineCompatibility.II1Il11l111II11IIl() ? new MainMenuScreen() : currentScreen;
      } catch (Throwable failure) {
         return currentScreen;
      }
   }
}

