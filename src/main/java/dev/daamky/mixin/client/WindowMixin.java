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
    private static final String p4fa4d47 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
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
// https://t.me/pasterplace

      return iconSuppliers;
   }
}

