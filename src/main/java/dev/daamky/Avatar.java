package dev.daamky;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;

public final class Avatar {
   private static final String NAMESPACE = "daamky";
   private static final String IMAGE_DIRECTORY = "images/";
   private static final Map<String, AvatarTexture> TEXTURES_BY_NAME = new LinkedHashMap<>();
   private static final Map<Identifier, TextureSize> TEXTURE_SIZES = new ConcurrentHashMap<>();
   public static final AvatarTexture DEFAULT_AVATAR = create("avatar");
   public static final AvatarTexture LOGO_AVATAR = create("logo");
   public static final AvatarTexture HEART_AVATAR = create("heart");
   public static final AvatarTexture DRUMSTICK_AVATAR = create("drumstick");

   private Avatar() {
   }

   public static void initialize() {
   }

   public static AvatarTexture create(String avatarName) {
      return create(avatarName, "images/" + avatarName + ".png");
   }

   public static AvatarTexture create(String avatarName, String resourcePath) {
      return TEXTURES_BY_NAME.computeIfAbsent(avatarName, text2x -> new AvatarTexture(text2x, Identifier.of("daamky", resourcePath)));
   }

   public static AvatarTexture get(String avatarName) {
      Object avatarTexture = TEXTURES_BY_NAME.get(avatarName);
      if (avatarTexture == null) {
         throw new IllegalArgumentException("Unknown image: " + avatarName);
      } else {
         return (AvatarTexture)avatarTexture;
      }
   }

   public static boolean contains(String avatarName) {
      return TEXTURES_BY_NAME.containsKey(avatarName);
   }

   public static Collection<AvatarTexture> all() {
      return Collections.unmodifiableCollection(TEXTURES_BY_NAME.values());
   }

   public static TextureSize getTextureSize(Identifier identifier) {
      return TEXTURE_SIZES.computeIfAbsent(identifier, Avatar::loadTextureSize);
   }

   public static void cacheTextureSize(Identifier identifier, int width, int height) {
      TEXTURE_SIZES.put(identifier, new TextureSize(width, height));
   }

   public static void clearTextureSizes() {
      TEXTURE_SIZES.clear();
   }

   private static TextureSize loadTextureSize(Identifier identifier) {
      try {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client != null && client.getResourceManager() != null) {
            TextureSize recordExtension7;
            try (InputStream inputStream = client.getResourceManager().getResourceOrThrow(identifier).getInputStream()) {
               NativeImage nativeImage = NativeImage.read(inputStream);

               try {
                  recordExtension7 = new TextureSize(nativeImage.getWidth(), nativeImage.getHeight());
               } catch (Throwable imageReadFailure) {
                  if (nativeImage != null) {
                     try {
                        nativeImage.close();
                     } catch (Throwable imageCloseFailure) {
                        imageReadFailure.addSuppressed(imageCloseFailure);
                     }
                  }

                  throw imageReadFailure;
               }

               if (nativeImage != null) {
                  nativeImage.close();
               }
            }

            return recordExtension7;
         } else {
            return new TextureSize(1, 1);
         }
      } catch (Throwable resourceReadFailure) {
         return new TextureSize(1, 1);
      }
   }
}

