package dev.daamky;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public interface ProxyServerBridge {
   default UUID offlinePlayerUuid(String playerName) {
      return UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes(StandardCharsets.UTF_8));
   }
// https://t.me/pasterplace

   default void ensureProxyMenuButton() {
   }
// https://t.me/pasterplace

   default boolean isUnavailableServer(String serverAddress, int playerCount) {
      return playerCount < 0 || "No Server".equalsIgnoreCase(serverAddress);
   }
}
