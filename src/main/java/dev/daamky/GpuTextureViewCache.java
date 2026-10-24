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
   private static final Map<Identifier, GpuTextureView> II1Il11l111II11IIl = new HashMap<>();

   private GpuTextureViewCache() {
   }

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

      II1Il11l111II11IIl.put(identifier, gpuTextureView);
      return gpuTextureView;
   }
}

