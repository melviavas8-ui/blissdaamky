package dev.daamky;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public final class BotAvatars {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final String MOD_ID = "daamky";
   private static final String AVATAR_TEXTURE_PATH = "bot_avatar/";
   private static final String AVATAR_URL_TEMPLATE = "https://minotar.net/helm/%s/64.png";
   private static final Map<String, Identifier> AVATAR_TEXTURES = new ConcurrentHashMap<>();
   private static final Set<String> AVATAR_LOADS_IN_PROGRESS = ConcurrentHashMap.newKeySet();
// https://t.me/pasterplace

   private BotAvatars() {
   }
// https://t.me/pasterplace

   public static Identifier getAvatarTexture(String username) {
      if (username != null && !username.isEmpty()) {
         String normalizedUsername = username.toLowerCase(Locale.ROOT);
         Object cachedTexture = AVATAR_TEXTURES.get(normalizedUsername);
         if (cachedTexture != null) {
            return (Identifier)cachedTexture;
         }

         loadAvatarAsync(username, normalizedUsername);
         return null;
      } else {
         return null;
      }
   }
// https://t.me/pasterplace

   private static void loadAvatarAsync(String username, String normalizedUsername) {
      if (AVATAR_LOADS_IN_PROGRESS.add(normalizedUsername)) {
         Thread thread = new Thread(() -> {
            try {
               byte[] var2x = loadAvatarBytes(username, normalizedUsername);
               if (var2x != null && var2x.length != 0) {
                  NativeImage nativeImage = NativeImage.read(var2x);
                  MinecraftClient client = MinecraftClient.getInstance();
                  client.execute(() -> {
                     try {
                        Identifier identifier = Identifier.of("daamky", "bot_avatar/" + Integer.toHexString(normalizedUsername.hashCode()));
                        client.getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(() -> "bot-avatar-" + normalizedUsername, nativeImage));
                        AVATAR_TEXTURES.put(normalizedUsername, identifier);
                     } catch (Throwable var4x) {
                     }
                  });
                  return;
               }
            } catch (Throwable failure) {
               return;
            } finally {
               AVATAR_LOADS_IN_PROGRESS.remove(normalizedUsername);
            }
         }, "bot-avatar-" + normalizedUsername);
         thread.setDaemon(true);
         thread.start();
      }
   }
// https://t.me/pasterplace

   private static byte[] loadAvatarBytes(String username, String normalizedUsername) throws Exception {
      Path path = getAvatarCacheDirectory().resolve(Integer.toHexString(normalizedUsername.hashCode()) + ".png");

      try {
         if (Files.isRegularFile(path)) {
            byte[] cachedAvatarBytes = Files.readAllBytes(path);
            if (cachedAvatarBytes.length > 0) {
               return cachedAvatarBytes;
            }
         }
      } catch (Exception cacheReadFailure) {
      }

      byte[] downloadedAvatarBytes = downloadBytes(String.format("https://minotar.net/helm/%s/64.png", username));
      if (downloadedAvatarBytes != null && downloadedAvatarBytes.length > 0) {
         try {
            Files.createDirectories(path.getParent());
            Files.write(path, downloadedAvatarBytes);
         } catch (Exception cacheWriteFailure) {
         }
      }

      return downloadedAvatarBytes;
   }
// https://t.me/pasterplace

   private static Path getAvatarCacheDirectory() {
      return MinecraftClient.getInstance().runDirectory.toPath().resolve("daamky").resolve("bot_avatars");
   }
// https://t.me/pasterplace

   private static byte[] downloadBytes(String url) throws Exception {
      HttpURLConnection uRLConnection = (HttpURLConnection)URI.create(url).toURL().openConnection();
      uRLConnection.setConnectTimeout(5000);
      uRLConnection.setReadTimeout(5000);
      uRLConnection.setRequestProperty("User-Agent", "Daamky");

      try (InputStream inputStream = uRLConnection.getInputStream()) {
         return inputStream.readAllBytes();
      } finally {
         uRLConnection.disconnect();
      }
   }
// https://t.me/pasterplace

   public static void clearAvatarTextures() {
      MinecraftClient client = MinecraftClient.getInstance();

      for (Object textureId : AVATAR_TEXTURES.values()) {
         try {
            client.getTextureManager().destroyTexture((Identifier)textureId);
         } catch (Throwable destroyFailure) {
         }
      }
// https://t.me/pasterplace

      AVATAR_TEXTURES.clear();
   }
}

