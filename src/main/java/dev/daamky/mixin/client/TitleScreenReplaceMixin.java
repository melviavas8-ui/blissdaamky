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
   @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
   private Screen daamky$replaceTitle(Screen currentScreen) {
      if (!(currentScreen instanceof TitleScreen)) {
         return currentScreen;
      }

      try {
         return RenderPipelineCompatibility.II1Il11l111II11IIl() ? new MainMenuScreen() : currentScreen;
      } catch (Throwable failure) {
         return currentScreen;
      }
   }
}

