package dev.daamky;

import com.mojang.blaze3d.textures.GpuTextureView;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public final class GpuTextureViewCache {
    private static final String p63d1593f = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final Map<Identifier, GpuTextureView> II1Il11l111II11IIl = new HashMap<>();
// https://t.me/pasterplace

   private GpuTextureViewCache() {
   }
// https://t.me/pasterplace

   public static GpuTextureView II1Il11l111II11IIl(Identifier identifier) {
      if (II1Il11l111II11IIl.containsKey(identifier)) {
         return II1Il11l111II11IIl.get(identifier);
      }

      GpuTextureView gpuTextureView = null;

      try {
         Optional optional = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
         if (optional.isPresent()) {
            try (InputStream inputStream = ((Resource)optional.get()).getInputStream()) {
               NativeImage nativeImage = NativeImage.read(inputStream);
               gpuTextureView = new NativeImageBackedTexture(identifier::toString, nativeImage).getGlTextureView();
            }
         }
      } catch (Exception loadException) {
      }
// https://t.me/pasterplace

      II1Il11l111II11IIl.put(identifier, gpuTextureView);
      return gpuTextureView;
   }
}

