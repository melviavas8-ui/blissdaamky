package dev.daamky.mixin.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.Window;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Window.class)
public abstract class WindowMixin {
   @Redirect(
      method = "setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Icons;getIcons(Lnet/minecraft/resource/ResourcePack;)Ljava/util/List;")
   )
   private List<InputSupplier<InputStream>> daamky$customIcons(Icons icons, ResourcePack resourcePack) throws IOException {
      int[] iconSizes = new int[]{256, 128, 48, 32, 16};
      ArrayList<InputSupplier<InputStream>> iconSuppliers = new ArrayList<>();

      for (int iconSize : iconSizes) {
         String iconResourcePath = "/assets/daamky/textures/icon/icon_" + iconSize + "x" + iconSize + ".png";
         if (WindowMixin.class.getResource(iconResourcePath) == null) {
            return icons.getIcons(resourcePack);
         }

         iconSuppliers.add(() -> WindowMixin.class.getResourceAsStream(iconResourcePath));
      }

      return iconSuppliers;
   }
}

