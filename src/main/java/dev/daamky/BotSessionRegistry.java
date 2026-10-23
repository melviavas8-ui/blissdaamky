package dev.daamky;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

final class BotSessionRegistry {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   final List<BotSession> registeredSessions = new CopyOnWriteArrayList<>();
   private final List<BotSession> connectingSessions = new CopyOnWriteArrayList<>();
   private final List<BotSession> temporarySessions = new CopyOnWriteArrayList<>();
   private final List<String> ignoredUsernames = new CopyOnWriteArrayList<>();
   private final Set<String> offlineSessionUsernames = ConcurrentHashMap.newKeySet();
   private final Map<String, String> rolesByUsername = new ConcurrentHashMap<>();
// https://t.me/pasterplace

   BotSession findSession(String username) {
      for (Object registeredSession : this.registeredSessions) {
         if (((BotSession)registeredSession).getUsername().equalsIgnoreCase(username)) {
            return (BotSession)registeredSession;
         }
      }

      return null;
   }
// https://t.me/pasterplace

   BotSession findTemporarySession(String username) {
      for (Object temporarySession : this.temporarySessions) {
         if (((BotSession)temporarySession).getUsername().equalsIgnoreCase(username)) {
            return (BotSession)temporarySession;
         }
      }

      return null;
   }
// https://t.me/pasterplace

   boolean addOfflineSession(String username) {
      if (username != null && !username.isBlank() && this.findSession(username) == null) {
         for (Object connectingSession : this.connectingSessions) {
            if (((BotSession)connectingSession).getUsername().equalsIgnoreCase(username)) {
               return false;
            }
         }

         this.rememberOfflineUsername(username);
         this.registeredSessions.add(new BotSession(username, "No Server", -1));
         return true;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   BotSession createTemporarySession(String username) {
      BotSession disconnect = new BotSession(username, "No Server", -1);
      disconnect.setExcludedFromSavedSessionRegistry(true);
      disconnect.setAutoReconnectEnabled(false);
      this.temporarySessions.add(disconnect);
      return disconnect;
   }
// https://t.me/pasterplace

   void removeTemporarySession(BotSession disconnect) {
      this.temporarySessions.remove(disconnect);
      ModuleToggleProfileManager.clearProfileCallbackState(disconnect.getUsername());
   }
// https://t.me/pasterplace

   BotSession createSession(String username, String serverAddress, int serverPort) {
      this.rememberOfflineUsername(username);
      ModuleToggleProfileManager.removeProfileState(username);
      this.registeredSessions.removeIf(disconnectx -> !disconnectx.isOnline() && disconnectx.getUsername().equalsIgnoreCase(username));
      BotSession disconnect = new BotSession(username, serverAddress, serverPort);
      this.connectingSessions.add(disconnect);
      disconnect.connect();
      return disconnect;
   }
// https://t.me/pasterplace

   void removeSession(BotSession disconnect) {
      this.registeredSessions.remove(disconnect);
      this.ignoredUsernames.removeIf(username -> username.equalsIgnoreCase(disconnect.getUsername()));
      this.rolesByUsername.remove(normalizeUsername(disconnect.getUsername()));
      ModuleToggleProfileManager.clearProfileCallbackState(disconnect.getUsername());
   }
// https://t.me/pasterplace

   private static String normalizeUsername(String username) {
      return username == null ? "" : username.toLowerCase(Locale.ROOT);
   }
// https://t.me/pasterplace

   String getRoleForUsername(String username) {
      Object role = this.rolesByUsername.get(normalizeUsername(username));
      return (String)(role == null ? "" : role);
   }
// https://t.me/pasterplace

   void setRoleForUsername(String username, String role) {
      if (username != null && !username.isBlank()) {
         if (role != null && !role.isBlank()) {
            this.rolesByUsername.put(normalizeUsername(username), role.trim());
         } else {
            this.rolesByUsername.remove(normalizeUsername(username));
         }
      }
   }
// https://t.me/pasterplace

   JsonObject serializeRoles() {
      JsonObject jsonObject = new JsonObject();

      for (java.util.Map.Entry<?, ?> roleEntry : this.rolesByUsername.entrySet()) {
         jsonObject.addProperty((String)roleEntry.getKey(), (String)roleEntry.getValue());
      }

      return jsonObject;
   }
// https://t.me/pasterplace

   void loadRoles(JsonObject jsonObject) {
      if (jsonObject != null) {
         this.rolesByUsername.clear();
// https://t.me/pasterplace

         for (Object username : jsonObject.keySet()) {
            try {
               this.setRoleForUsername((String)username, jsonObject.get((String)username).getAsString());
            } catch (Exception invalidRoleValue) {
            }
         }
      }
   }
// https://t.me/pasterplace

   void rememberOfflineUsername(String username) {
      if (username != null && !username.isBlank()) {
         this.offlineSessionUsernames.add(username);
         BotAvatars.getAvatarTexture(username);
      }
   }
// https://t.me/pasterplace

   JsonArray serializeOfflineSessions() {
      JsonArray jsonArray = new JsonArray();

      for (String username : this.offlineSessionUsernames) {
         if (!username.isBlank()) {
            jsonArray.add((String)username);
         }
      }

      return jsonArray;
   }
// https://t.me/pasterplace

   void loadOfflineSessions(JsonArray jsonArray) {
      if (jsonArray != null) {
         this.registeredSessions.removeIf(disconnect -> !disconnect.isOnline() && "No Server".equalsIgnoreCase(disconnect.getServerAddress()));
         this.offlineSessionUsernames.clear();

         for (int entryIndex = 0; entryIndex < jsonArray.size(); entryIndex++) {
            try {
               String asString = jsonArray.get(entryIndex).getAsString();
               if (asString != null && !asString.isBlank()) {
                  this.addOfflineSession(asString);
               }
            } catch (Exception invalidUsernameEntry) {
            }
         }
      }
   }
// https://t.me/pasterplace

   void registerSession(BotSession disconnect) {
      this.connectingSessions.remove(disconnect);
      this.registeredSessions.removeIf(disconnect2 -> disconnect2 != disconnect && disconnect2.getUsername().equalsIgnoreCase(disconnect.getUsername()));
      if (!this.registeredSessions.contains(disconnect)) {
         this.registeredSessions.add(disconnect);
      }
   }
// https://t.me/pasterplace

   void registerSessionIfAbsent(BotSession disconnect) {
      this.connectingSessions.remove(disconnect);
      this.ignoredUsernames.removeIf(username -> username.equalsIgnoreCase(disconnect.getUsername()));
      ModuleToggleProfileManager.clearProfileCallbackState(disconnect.getUsername());
      if (this.findSession(disconnect.getUsername()) == null) {
         this.registeredSessions.add(disconnect);
      }
   }
// https://t.me/pasterplace

   boolean isIgnoredSession(BotSession disconnect) {
      if (disconnect == null) {
         return false;
      }

      String illI1l1l1 = disconnect.getUsername();

      for (String ignoredUsername : this.ignoredUsernames) {
         if (ignoredUsername.equalsIgnoreCase(illI1l1l1)) {
            return true;
         }
      }

      return false;
   }
// https://t.me/pasterplace

   void tickSessions() {
      for (Object connectingSession : this.connectingSessions) {
         ((BotSession)connectingSession).tickConnection();
      }

      for (Object registeredSession : this.registeredSessions) {
         ((BotSession)registeredSession).tickConnection();
      }

      for (Object temporarySession : this.temporarySessions) {
         ((BotSession)temporarySession).tickConnection();
      }
   }
// https://t.me/pasterplace

   void disconnectAllSessions() {
      for (Object connectingSession : this.connectingSessions) {
         ((BotSession)connectingSession).disconnect("shutdown");
      }

      for (Object registeredSession : this.registeredSessions) {
         ((BotSession)registeredSession).disconnect("shutdown");
      }

      for (Object temporarySession : this.temporarySessions) {
         ((BotSession)temporarySession).disconnect("shutdown");
      }
// https://t.me/pasterplace

      this.connectingSessions.clear();
      this.registeredSessions.clear();
      this.temporarySessions.clear();
      this.ignoredUsernames.clear();
   }
}

