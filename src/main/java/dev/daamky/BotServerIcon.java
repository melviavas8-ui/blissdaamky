package dev.daamky;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.WorldIcon;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.network.ServerInfo.Status;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.network.NetworkingBackend;
import net.minecraft.util.Identifier;

public final class BotServerIcon {
   private static final MultiplayerServerListPinger SERVER_LIST_PINGER = new MultiplayerServerListPinger();
   private static final ExecutorService PING_EXECUTOR = Executors.newFixedThreadPool(3, runnable -> {
      Thread thread = new Thread(runnable, "bot-server-icon");
      thread.setDaemon(true);
      return thread;
   });
   private static final Map<String, BotServerIcon.IconCacheEntry> ICON_CACHE = new ConcurrentHashMap<>();

   private BotServerIcon() {
   }

   public static Identifier getServerIcon(String serverAddress) {
      if (serverAddress != null && !serverAddress.isEmpty() && !"No Server".equalsIgnoreCase(serverAddress)) {
         Object cacheEntry = ICON_CACHE.get(serverAddress);
         if (cacheEntry == null) {
            MinecraftClient client = MinecraftClient.getInstance();
            ServerInfo serverInfo = new ServerInfo(serverAddress, serverAddress, ServerType.OTHER);
            WorldIcon worldIcon = WorldIcon.forServer(client.getTextureManager(), serverAddress);
            cacheEntry = new BotServerIcon.IconCacheEntry(serverInfo, worldIcon);
            Object existingCacheEntry = ICON_CACHE.putIfAbsent(serverAddress, (BotServerIcon.IconCacheEntry)cacheEntry);
            if (existingCacheEntry != null) {
               cacheEntry = existingCacheEntry;
            } else {
               pingServer((BotServerIcon.IconCacheEntry)cacheEntry);
            }
         }

         return ((BotServerIcon.IconCacheEntry)cacheEntry).hasIcon ? ((BotServerIcon.IconCacheEntry)cacheEntry).worldIcon.getTextureId() : null;
      } else {
         return null;
      }
   }

   private static void pingServer(BotServerIcon.IconCacheEntry cacheEntry) {
      cacheEntry.serverInfo.setStatus(Status.PINGING);
      PING_EXECUTOR.submit(
         () -> {
            try {
               SERVER_LIST_PINGER.add(
                  cacheEntry.serverInfo, () -> {}, () -> {}, NetworkingBackend.remote(MinecraftClient.getInstance().options.shouldUseNativeTransport())
               );
            } catch (Exception pingFailure) {
               cacheEntry.serverInfo.setStatus(Status.UNREACHABLE);
            }
         }
      );
   }

   public static void tick() {
      try {
         SERVER_LIST_PINGER.tick();
      } catch (Throwable tickFailure) {
      }

      for (Object cacheEntry : ICON_CACHE.values()) {
         byte[] faviconBytes = ((BotServerIcon.IconCacheEntry)cacheEntry).serverInfo.getFavicon();
         if (faviconBytes != null && faviconBytes != ((BotServerIcon.IconCacheEntry)cacheEntry).lastFaviconBytes) {
            try {
               ((BotServerIcon.IconCacheEntry)cacheEntry).worldIcon.load(NativeImage.read(faviconBytes));
               ((BotServerIcon.IconCacheEntry)cacheEntry).lastFaviconBytes = faviconBytes;
               ((BotServerIcon.IconCacheEntry)cacheEntry).hasIcon = true;
            } catch (Throwable faviconDecodeFailure) {
            }
         }
      }
   }

   public static void clear() {
      try {
         SERVER_LIST_PINGER.cancel();
      } catch (Throwable cancelFailure) {
      }

      for (Object cacheEntry : ICON_CACHE.values()) {
         try {
            ((BotServerIcon.IconCacheEntry)cacheEntry).worldIcon.close();
         } catch (Throwable iconCloseFailure) {
         }
      }

      ICON_CACHE.clear();
   }

   private static final class IconCacheEntry {
      final ServerInfo serverInfo;
      final WorldIcon worldIcon;
      byte[] lastFaviconBytes;
      boolean hasIcon;

      IconCacheEntry(ServerInfo serverInfo, WorldIcon worldIcon) {
         this.serverInfo = serverInfo;
         this.worldIcon = worldIcon;
      }
   }
}

