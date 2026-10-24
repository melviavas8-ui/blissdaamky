package dev.daamky;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public final class Emoji {
   private static final String emojiCdnBaseUrl = System.getProperty(
      "daamky.emoji.cdn", "https://cdn.jsdelivr.net/npm/emoji-datasource-apple@15.0.1/img/apple/64/"
   );
   private static final Map<String, List<Emoji.EmojiEntry>> emojiEntriesByCategory = new LinkedHashMap<>();
   private static final Map<String, Emoji.EmojiEntry> emojiEntriesByShortcode = new HashMap<>();
   private static final Map<String, Identifier> emojiTextureIdentifiers = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> pendingEmojiTextureLoads = new ConcurrentHashMap<>();
   private static final Path emojiCacheDirectory = FabricLoader.getInstance().getGameDir().resolve("daamky").resolve("emoji");
   private static final HttpClient emojiHttpClient = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(6L))
      .followRedirects(Redirect.NORMAL)
      .build();
   private static final ExecutorService emojiDownloadExecutor = Executors.newFixedThreadPool(3, runnable -> {
      Thread thread = new Thread(runnable, "daamky-emoji");
      thread.setDaemon(true);
      return thread;
   });

   private Emoji() {
   }

   private static void registerEmojiGroup(String categoryName, String encodedEmojiSequences) {
      ArrayList entries = new ArrayList();

      for (String codePointSequence : encodedEmojiSequences.trim().split("\\s+")) {
         StringBuilder emojiBuilder = new StringBuilder();

         for (String codePointHex : codePointSequence.split("-")) {
            emojiBuilder.appendCodePoint(Integer.parseInt(codePointHex, 16));
         }

         Emoji.EmojiEntry emojiEntry = new Emoji.EmojiEntry(emojiBuilder.toString(), codePointSequence);
         entries.add(emojiEntry);
         emojiEntriesByShortcode.putIfAbsent(emojiEntry.shortcode(), emojiEntry);
         if (emojiEntry.shortcode().endsWith("️")) {
            emojiEntriesByShortcode.putIfAbsent(emojiEntry.shortcode().substring(0, emojiEntry.shortcode().length() - 1), emojiEntry);
         }
      }

      emojiEntriesByCategory.put(categoryName, entries);
   }

   public static Map<String, List<Emoji.EmojiEntry>> getEmojiGroups() {
      return emojiEntriesByCategory;
   }

   public static Emoji.EmojiEntry findEmojiEntry(String emojiText) {
      return emojiEntriesByShortcode.get(emojiText);
   }

   public static Emoji.EmojiEntry findEmojiAt(String sourceText, int startIndex) {
      for (int candidateLength = Math.min(4, sourceText.length() - startIndex); candidateLength >= 1; candidateLength--) {
         Object emojiEntry = emojiEntriesByShortcode.get(sourceText.substring(startIndex, startIndex + candidateLength));
         if (emojiEntry != null) {
            return (Emoji.EmojiEntry)emojiEntry;
         }
      }

      return null;
   }

   public static int matchEmojiLength(String sourceText, int startIndex) {
      for (int candidateLength = Math.min(4, sourceText.length() - startIndex); candidateLength >= 1; candidateLength--) {
         if (emojiEntriesByShortcode.containsKey(sourceText.substring(startIndex, startIndex + candidateLength))) {
            int matchEndIndex = startIndex + candidateLength;
            if (matchEndIndex < sourceText.length() && sourceText.charAt(matchEndIndex) == '️') {
               matchEndIndex++;
            }

            return matchEndIndex - startIndex;
         }
      }

      return 0;
   }

   public static String removeEmojiSequences(String sourceText) {
      StringBuilder cleanedTextBuilder = new StringBuilder();
      int sourceIndex = 0;
      boolean removedEmojiSequence = false;

      while (sourceIndex < sourceText.length()) {
         int matchedSequenceLength = matchEmojiLength(sourceText, sourceIndex);
         if (matchedSequenceLength > 0) {
            removedEmojiSequence = true;
            sourceIndex += matchedSequenceLength;
         } else {
            int codePoint = sourceText.codePointAt(sourceIndex);
            if (codePoint < 8192 || codePoint > 12287 && codePoint < 126976) {
               cleanedTextBuilder.appendCodePoint(codePoint == 10 ? 32 : codePoint);
            }

            sourceIndex += Character.charCount(codePoint);
         }
      }

      String cleanedText = cleanedTextBuilder.toString().trim();
      return cleanedText.isEmpty() && removedEmojiSequence ? "Эмодзи" : cleanedText;
   }

   public static Identifier getEmojiTextureIdentifier(Emoji.EmojiEntry emojiEntry) {
      Object cachedTextureIdentifier = emojiTextureIdentifiers.get(emojiEntry.emojiText());
      if (cachedTextureIdentifier != null) {
         return (Identifier)cachedTextureIdentifier;
      }

      if (pendingEmojiTextureLoads.putIfAbsent(emojiEntry.emojiText(), Boolean.TRUE) == null) {
         emojiDownloadExecutor.submit(() -> loadEmojiTexture(emojiEntry.emojiText()));
      }

      return null;
   }

   private static void loadEmojiTexture(String emojiCode) {
      try {
         Path cacheFile = emojiCacheDirectory.resolve(emojiCode + ".png");
         Object imageBytes;
         if (Files.isRegularFile(cacheFile)) {
            imageBytes = Files.readAllBytes(cacheFile);
         } else {
            HttpResponse downloadResponse = emojiHttpClient.send(
               HttpRequest.newBuilder(URI.create(emojiCdnBaseUrl + emojiCode + ".png")).timeout(Duration.ofSeconds(10L)).GET().build(),
               BodyHandlers.ofByteArray()
            );
            if (downloadResponse.statusCode() != 200) {
               return;
            }

            imageBytes = (byte[])downloadResponse.body();
            Files.createDirectories(emojiCacheDirectory);
            Files.write(cacheFile, (byte[])imageBytes);
         }

         NativeImage decodedImage = NativeImage.read(new ByteArrayInputStream((byte[])imageBytes));
         Identifier textureIdentifier = Identifier.of("daamky", "emoji/" + emojiCode.replace('-', '_'));
         MinecraftClient.getInstance().execute(() -> {
            try {
               NativeImageBackedTexture registeredTexture = new NativeImageBackedTexture(textureIdentifier::toString, decodedImage);
               MinecraftClient.getInstance().getTextureManager().registerTexture(textureIdentifier, registeredTexture);
               emojiTextureIdentifiers.put(emojiCode, textureIdentifier);
            } catch (Exception registrationException) {
               decodedImage.close();
            }
         });
      } catch (Exception downloadException) {
      }
   }

   static {
      registerEmojiGroup(
         "ð",
         "1f600 1f603 1f604 1f601 1f606 1f605 1f923 1f602 1f642 1f609 1f60a 1f607 1f970 1f60d 1f929 1f618 1f617 1f61a 1f60b 1f61b 1f61c 1f92a 1f61d 1f911 1f917 1f92d 1f92b 1f914 1f610 1f611 1f636 1f60f 1f612 1f644 1f62c 1f925 1f60c 1f614 1f62a 1f924 1f634 1f637 1f912 1f915 1f922 1f92e 1f927 1f975 1f976 1f974 1f635 1f92f 1f920 1f973 1f60e 1f913 1f9d0 1f615 1f61f 1f641 1f62e 1f62f 1f632 1f633 1f97a 1f626 1f627 1f628 1f630 1f625 1f622 1f62d 1f631 1f616 1f623 1f61e 1f613 1f629 1f62b 1f971 1f624 1f621 1f620 1f92c 1f608 1f47f 1f480 1f4a9 1f921 1f479 1f47b 1f47d 1f916 1f63a 1f639 1f63b 1f640"
      );
      registerEmojiGroup(
         "ð",
         "1f44d 1f44e 1f44c 1f90c 1f90f 270c-fe0f 1f91e 1f91f 1f918 1f919 1f448 1f449 1f446 1f447 261d-fe0f 270b 1f91a 1f590-fe0f 1f596 1f44b 1f44f 1f64c 1f450 1f932 1f91d 1f64f 270d-fe0f 1f4aa 1f485 1f933 1f440 1f9e0 1f445 1f444 1f476 1f466 1f467 1f468 1f469 1f474 1f475 1f46e 1f477 1f482 1f575-fe0f 1f385"
      );
      registerEmojiGroup(
         "❤️",
         "2764-fe0f 1f9e1 1f49b 1f49a 1f499 1f49c 1f5a4 1f90d 1f90e 1f494 1f495 1f49e 1f493 1f497 1f496 1f498 1f49d 1f49f 1f48b 1f4af 1f4a2 1f4a5 1f4ab 1f4a6 1f4a8 1f4ac 1f4ad 1f4a4 1f525 2728 1f31f 2b50 26a1 2705 274c 2753 2757 26a0-fe0f 1f6ab 1f51e 2795 2796 1f197 1f195 1f199 1f192"
      );
      registerEmojiGroup(
         "ð",
         "1f3ae 1f5e1-fe0f 2694-fe0f 1f6e1-fe0f 1f3f9 1f48e 1f4b0 1f4b8 1f451 1f680 1f4a3 1f3af 1f3b2 1f3c6 1f947 1f948 1f949 1f3c5 1f389 1f38a 1f381 1f388 1f9e8 1f52b 1fa93 26cf-fe0f 1f528 1f9f2 1f9ea 1f4e6 1f4f1 1f4bb 1f3a7 1f3b5 1f514 1f4a1 1f511 1f512 1f4cc 1f4dd"
      );
      registerEmojiGroup(
         "ð",
         "1f436 1f431 1f42d 1f439 1f430 1f98a 1f43b 1f43c 1f428 1f42f 1f981 1f42e 1f437 1f438 1f435 1f648 1f649 1f64a 1f414 1f427 1f426 1f985 1f989 1f43a 1f417 1f434 1f984 1f41d 1f40c 1f98b 1f41b 1f577-fe0f 1f40d 1f422 1f419 1f988 1f42c 1f433 1f40a 1f409 1f335 1f332 1f340 1f339 1f33b 1f337 1f344 1f30d 1f319 2600-fe0f 1f308 2601-fe0f 2744-fe0f 1f30a"
      );
      registerEmojiGroup(
         "ð",
         "1f34f 1f34e 1f350 1f34a 1f34b 1f34c 1f349 1f347 1f353 1f352 1f351 1f96d 1f34d 1f965 1f95d 1f345 1f951 1f955 1f33d 1f336-fe0f 1f952 1f966 1f354 1f35f 1f355 1f32d 1f96a 1f32e 1f37f 1f9c0 1f356 1f357 1f969 1f953 1f95a 1f35e 1f950 1f369 1f36a 1f382 1f370 1f9c1 1f36b 1f36c 1f36d 1f366 1f368 2615 1f375 1f37a 1f37b 1f942 1f377 1f964 1f9c3"
      );
   }

   public record EmojiEntry(String shortcode, String emojiText) {

      public EmojiEntry(String shortcode, String emojiText) {
         this.shortcode = shortcode;
         this.emojiText = emojiText;
      }
   }
}

