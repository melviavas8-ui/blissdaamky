package dev.daamky;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.ClientConnection;

public final class SavedBots {
   private static final BotSessionRegistry sessionStore = new BotSessionRegistry();
   private static final BotSessionSwitcher sessionSwitcher = new BotSessionSwitcher();
   private static final BalancePayoutTracker balancePayoutTracker = new BalancePayoutTracker();
   private static final Set<ClientConnection> botClientConnections = ConcurrentHashMap.newKeySet();
   private static final String[] randomUsernamePrefixes = new String[]{
      "Silent",
      "Dark",
      "Swift",
      "Frosty",
      "Lucky",
      "Crazy",
      "Happy",
      "Shadow",
      "Golden",
      "Wild",
      "Brave",
      "Lazy",
      "Sneaky",
      "Mighty",
      "Rapid",
      "Cosmic",
      "Fuzzy",
      "Night",
      "Storm",
      "Iron",
      "Red",
      "Blue",
      "Toxic",
      "Pixel",
      "Mega",
      "Cool",
      "Angry",
      "Funny",
      "Sleepy",
      "Little"
   };
   private static final String[] randomUsernameSuffixes = new String[]{
      "Fox",
      "Wolf",
      "Cat",
      "Bear",
      "Tiger",
      "Hawk",
      "Panda",
      "Shark",
      "Dragon",
      "Ninja",
      "Gamer",
      "Knight",
      "Rider",
      "Hunter",
      "Miner",
      "Raven",
      "Lion",
      "Ghost",
      "Bunny",
      "Frog",
      "Owl",
      "Duck",
      "Viper",
      "Steve",
      "Pilot",
      "Wizard",
      "Cookie",
      "Potato",
      "Mango",
      "Boss"
   };
   private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_]{3,16}");
   private static final SavedBots.SavedBotSessionRegistry sessionRegistry = new SavedBots.SavedBotSessionRegistry();
   private static volatile boolean registryPanelOpen;
   private static final SavedBotSessionRepository sessionRepository = new SavedBotSessionRepository() {
      @Override
      public List<SavedBotSession> getAllSessions() {
         return List.copyOf(SavedBots.sessionStore.registeredSessions);
      }

      @Override
      public List<SavedBotSession> getOnlineSessions() {
         ArrayList<SavedBotSession> onlineSessions = new ArrayList<>();
         for (BotSession session : SavedBots.sessionStore.registeredSessions) {
            if (session.isOnline()) {
               onlineSessions.add(session);
            }
         }
         return List.copyOf(onlineSessions);
      }

      @Override
      public SavedBotSession findSession(String username) {
         return SavedBots.findSession(username);
      }

      @Override
      public boolean addOfflineSession(String username) {
         return SavedBots.addOfflineSession(username);
      }

      @Override
      public boolean removeSession(String username) {
         return SavedBots.removeSessionByUsername(username);
      }

      @Override
      public SavedBotSession createSession(String username, String serverAddress, int serverPort) {
         return SavedBots.createSession(username, serverAddress, serverPort);
      }
   };
   private static final SavedBotServiceAccess serviceAccess = new SavedBotServiceAccess() {
      @Override
      public boolean switchToSession(String username) {
         return SavedBots.switchToSession(username);
      }

      @Override
      public void restorePreviousSession() {
         SavedBots.restorePreviousSession();
      }

      @Override
      public boolean isActiveSessionReady() {
         return SavedBots.isActiveSessionReady();
      }

      @Override
      public BotSessionInfo getActiveSession() {
         return SavedBots.getActiveSession();
      }

      @Override
      public float getSwitchNoticeAlpha() {
         return SavedBots.getSwitchNoticeAlpha();
      }
   };

   private SavedBots() {
   }

   public static float getSwitchNoticeAlpha() {
      return sessionSwitcher.getSwitchNoticeAlpha();
   }

   public static List<BotSession> getSavedSessions() {
      return List.copyOf(sessionStore.registeredSessions);
   }

   public static BotSession createSession(String username, String serverAddress, int serverPort) {
      return sessionStore.createSession(username, serverAddress, serverPort);
   }

   public static boolean addOfflineSession(String username) {
      return sessionStore.addOfflineSession(username);
   }

   public static JsonArray serializeOfflineSessions() {
      return sessionStore.serializeOfflineSessions();
   }

   public static void loadOfflineSessions(JsonArray jsonArray) {
      sessionStore.loadOfflineSessions(jsonArray);
   }

   public static BotSession findSession(String username) {
      return sessionStore.findSession(username);
   }

   public static String getRoleForUsername(String username) {
      return sessionStore.getRoleForUsername(username);
   }

   public static void setRoleForUsername(String username, String role) {
      sessionStore.setRoleForUsername(username, role);
   }

   public static JsonObject serializeRoles() {
      return sessionStore.serializeRoles();
   }

   public static void loadRoles(JsonObject jsonObject) {
      sessionStore.loadRoles(jsonObject);
   }

   public static JsonObject serializeSettings() {
      JsonObject rootSettings = new JsonObject();
      rootSettings.add("savedBots", serializeOfflineSessions());
      rootSettings.add("botRoles", serializeRoles());
      rootSettings.addProperty("panel", registryPanelOpen);
      JsonObject sessionSettingsByUsername = new JsonObject();

      for (Object session : sessionStore.registeredSessions) {
         JsonObject sessionSettings = new JsonObject();
         sessionSettings.addProperty("reconnect", ((BotSession)session).isAutoReconnectEnabled());
         JsonArray enabledModuleNames = new JsonArray();

         for (Object moduleEntry : ((BotSession)session).getModuleProfile().II1Il11l111II11IIl()) {
            if (((BotModuleEntry)moduleEntry).IllI1l1l1()) {
               enabledModuleNames.add(((BotModuleEntry)moduleEntry).II1Il11l111II11IIl());
            }
         }

         sessionSettings.add("modules", enabledModuleNames);
         sessionSettingsByUsername.add(((BotSession)session).getUsername(), sessionSettings);
      }

      rootSettings.add("botSettings", sessionSettingsByUsername);
      return rootSettings;
   }

   public static void loadSettings(JsonObject jsonObject) {
      if (jsonObject != null) {
         if (jsonObject.has("savedBots") && jsonObject.get("savedBots").isJsonArray()) {
            loadOfflineSessions(jsonObject.getAsJsonArray("savedBots"));
         } else if (jsonObject.has("offlineBots") && jsonObject.get("offlineBots").isJsonArray()) {
            loadOfflineSessions(jsonObject.getAsJsonArray("offlineBots"));
         } else if (jsonObject.has("bots") && jsonObject.get("bots").isJsonArray()) {
            JsonArray legacySessionNames = new JsonArray();
            jsonObject.getAsJsonArray("bots").forEach(legacyBotEntry -> {
               if (legacyBotEntry.isJsonObject() && legacyBotEntry.getAsJsonObject().has("name")) {
                  legacySessionNames.add(legacyBotEntry.getAsJsonObject().get("name").getAsString());
               } else if (legacyBotEntry.isJsonPrimitive()) {
                  legacySessionNames.add(legacyBotEntry.getAsString());
               }
            });
            loadOfflineSessions(legacySessionNames);
         }

         if (jsonObject.has("botRoles") && jsonObject.get("botRoles").isJsonObject()) {
            loadRoles(jsonObject.getAsJsonObject("botRoles"));
         }

         if (jsonObject.has("panel")) {
            registryPanelOpen = jsonObject.get("panel").getAsBoolean();
         }

         JsonObject sessionSettingsByUsername = jsonObject.has("botSettings") && jsonObject.get("botSettings").isJsonObject() ? jsonObject.getAsJsonObject("botSettings") : null;
         JsonArray legacyBotEntries = jsonObject.has("bots") && jsonObject.get("bots").isJsonArray() ? jsonObject.getAsJsonArray("bots") : null;

         for (Object session : sessionStore.registeredSessions) {
            JsonObject sessionSettings = null;
            if (sessionSettingsByUsername != null && sessionSettingsByUsername.has(((BotSession)session).getUsername()) && sessionSettingsByUsername.get(((BotSession)session).getUsername()).isJsonObject()) {
               sessionSettings = sessionSettingsByUsername.getAsJsonObject(((BotSession)session).getUsername());
            } else if (legacyBotEntries != null) {
               for (JsonElement legacyBotEntry : legacyBotEntries) {
                  if (legacyBotEntry.isJsonObject()
                     && legacyBotEntry.getAsJsonObject().has("name")
                     && legacyBotEntry.getAsJsonObject().get("name").getAsString().equalsIgnoreCase(((BotSession)session).getUsername())) {
                     sessionSettings = legacyBotEntry.getAsJsonObject();
                  }
               }
            }

            if (sessionSettings != null) {
               if (sessionSettings.has("reconnect")) {
                  ((BotSession)session).setAutoReconnectEnabled(sessionSettings.get("reconnect").getAsBoolean());
               }

               if (sessionSettings.has("modules") && sessionSettings.get("modules").isJsonArray()) {
                  for (JsonElement moduleNameElement : sessionSettings.getAsJsonArray("modules")) {
                     BotModuleEntry moduleEntry = ((BotSession)session).getModuleProfile().II1Il11l111II11IIl(moduleNameElement.getAsString());
                     if (moduleEntry != null) {
                        moduleEntry.II1Il11l111II11IIl(true);
                     }
                  }
               }
            }
         }
      }
   }

   public static boolean removeSessionByUsername(String username) {
      BotSession disconnect = sessionStore.findSession(username);
      if (disconnect == null) {
         return false;
      }

      disconnect.disconnect("removed");
      sessionStore.removeSession(disconnect);
      sessionSwitcher.restoreIfActive(disconnect);
      ModuleToggleProfileManager.removeProfileState(username);
      return true;
   }

   public static boolean switchToSession(String username) {
      boolean switchedToSession = false;

      for (Object session : sessionStore.registeredSessions) {
         if (((BotSession)session).getUsername().equals(username)) {
            sessionSwitcher.switchToDisconnect((BotSession)session);
            switchedToSession = true;
         }
      }

      return switchedToSession;
   }

   public static void restorePreviousSession() {
      sessionSwitcher.restorePreviousSession();
   }

   public static BotSession getActiveSession() {
      return sessionSwitcher.activeDisconnect();
   }

   public static boolean isActiveSessionReady() {
      return sessionSwitcher.isActiveDisconnectReady();
   }

   public static boolean ownsActiveSessionObject(Object sessionObject) {
      return sessionSwitcher.owns(sessionObject);
   }

   public static boolean isSwitchingToSession(BotSession disconnect) {
      return sessionSwitcher.isSwitchingTo(disconnect);
   }

   public static void runWithSessionStateIsolation(Runnable runnable) {
      sessionSwitcher.runWithClientStateIsolation(runnable);
   }

   static boolean isManuallyDisconnected(BotSession disconnect) {
      return sessionStore.isIgnoredSession(disconnect);
   }

   public static void tickConnections() {
      sessionStore.tickSessions();
      sessionSwitcher.tickQueuedDisconnect();
      sessionSwitcher.tickSavedConnection();
      balancePayoutTracker.processPayouts(sessionStore.registeredSessions);
   }

   public static long getScoreboardBalance(BotSession disconnect) {
      return balancePayoutTracker.getSessionBalance(disconnect);
   }

   public static boolean isBalancePayoutEnabled() {
      return balancePayoutTracker.isPayoutEnabled();
   }

   public static void setBalancePayoutEnabled(boolean enabled) {
      balancePayoutTracker.setPayoutEnabled(enabled);
   }

   public static void resetPayoutTracking() {
      balancePayoutTracker.payCurrentBalances(sessionStore.registeredSessions);
   }

   public static String getPayoutRecipient() {
      return balancePayoutTracker.getPayoutRecipient();
   }

   public static void setPayoutRecipient(String recipientUsername) {
      balancePayoutTracker.setPayoutRecipient(recipientUsername == null ? "" : recipientUsername.trim());
   }

   public static long getMinimumPayoutAmount() {
      return balancePayoutTracker.getMinimumPayoutAmount();
   }

   public static void setMinimumPayoutAmount(long minimumPayoutAmount) {
      balancePayoutTracker.setMinimumPayoutAmount(Math.max(0L, minimumPayoutAmount));
   }

   public static void shutdownSessions() {
      sessionStore.disconnectAllSessions();
      sessionSwitcher.restorePreviousSession();
   }

   public static BotSession createRandomSession() {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      String generatedUsername = null;

      for (int attemptIndex = 0; attemptIndex < 200; attemptIndex++) {
         String usernameStem = randomUsernamePrefixes[random.nextInt(randomUsernamePrefixes.length)] + randomUsernameSuffixes[random.nextInt(randomUsernameSuffixes.length)];

         String candidateUsername = switch (random.nextInt(3)) {
            case 0 -> usernameStem + random.nextInt(10, 100);
            case 1 -> usernameStem + "_" + random.nextInt(1, 1000);
            default -> usernameStem.toLowerCase(Locale.ROOT) + random.nextInt(100, 10000);
         };
         if (candidateUsername.length() <= 16 && sessionStore.findSession(candidateUsername) == null && sessionStore.findTemporarySession(candidateUsername) == null) {
            generatedUsername = candidateUsername;
            break;
         }
      }

      if (generatedUsername == null) {
         generatedUsername = "Player" + random.nextInt(100000, 1000000);
      }

      return sessionStore.createTemporarySession(generatedUsername);
   }

   public static void removeSession(BotSession disconnect) {
      if (disconnect != null) {
         disconnect.disconnect("removed");
         sessionStore.removeTemporarySession(disconnect);
         ModuleToggleProfileManager.removeProfileState(disconnect.getUsername());
      }
   }

   public static boolean configureSessionForCurrentServer(BotSession disconnect) {
      ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
      if (disconnect != null && serverInfo != null && serverInfo.address != null && !serverInfo.address.isBlank()) {
         ServerAddress serverAddress = ServerAddress.parse(serverInfo.address.trim());
         disconnect.updateServerAddressAndReconnect(serverAddress.getAddress(), serverAddress.getPort());
         return true;
      } else {
         return false;
      }
   }

   static void registerBotClientConnection(ClientConnection clientConnection) {
      botClientConnections.add(clientConnection);
   }

   static void unregisterBotClientConnection(ClientConnection clientConnection) {
      botClientConnections.remove(clientConnection);
   }

   public static boolean isBotClientConnection(ClientConnection clientConnection) {
      return clientConnection != null && botClientConnections.contains(clientConnection);
   }

   public static boolean isDisconnectSessionView(Object viewObject) {
      return viewObject instanceof DisconnectSessionView;
   }

   static void registerOfflineSession(BotSession disconnect) {
      sessionStore.registerSession(disconnect);
   }

   static void finalizeSessionDisconnect(BotSession disconnect) {
      sessionSwitcher.clearQueuedDisconnect(disconnect);
      sessionStore.registerSessionIfAbsent(disconnect);
   }

   static void queueSessionDisconnect(BotSession disconnect) {
      sessionSwitcher.queueDisconnect(disconnect);
   }

   public static boolean isValidUsername(String username) {
      return username != null && USERNAME_PATTERN.matcher(username).matches();
   }

   public static SavedBots.SavedBotSessionRegistry getSessionRegistry() {
      return sessionRegistry;
   }

   public static SavedBotSessionRepository getSessionRepository() {
      return sessionRepository;
   }

   public static SavedBotServiceAccess getServiceAccess() {
      return serviceAccess;
   }

   static {
      DisconnectListenerRegistry.II1Il11l111II11IIl(new DisconnectScreenListener() {
         @Override
         public void onConnectionStateChanged(BotSession disconnect, BotSession.BotConnectionStatus previousStatus, BotSession.BotConnectionStatus currentStatus) {
            if (!disconnect.isExcludedFromSavedSessionRegistry()) {
               if (currentStatus == BotSession.BotConnectionStatus.ONLINE) {
                  SavedBots.registerOfflineSession(disconnect);
               } else if (currentStatus == BotSession.BotConnectionStatus.DISCONNECTED) {
                  SavedBots.finalizeSessionDisconnect(disconnect);
               }
            }
         }

         @Override
         public void onDisconnect(BotSession disconnect, String reason) {
            SavedBots.sessionSwitcher.restoreIfActive(disconnect);
         }
      });
   }

   public static final class SavedBotSessionRegistry {
      private SavedBotSessionRegistry() {
      }

      public List<BotSession> getSessions() {
         return SavedBots.getSavedSessions();
      }

      public BotSession createOfflineSessionIfAbsent(String username) {
         return SavedBots.isValidUsername(username) && SavedBots.addOfflineSession(username) ? SavedBots.findSession(username) : null;
      }

      public void removeSession(BotSession disconnect) {
         if (disconnect != null) {
            SavedBots.removeSessionByUsername(disconnect.getUsername());
         }
      }

      public String getCurrentServerAddress() {
         ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
         return serverInfo == null ? null : serverInfo.address;
      }

      public boolean assignCurrentServerToSession(BotSession disconnect) {
         return SavedBots.configureSessionForCurrentServer(disconnect);
      }

      public int countOfflineSessionsAssignedToCurrentServer() {
         if (this.getCurrentServerAddress() == null) {
            return 0;
         }

         int offlineSessionCount = 0;

         for (Object session : SavedBots.sessionStore.registeredSessions) {
            if (((BotSession)session).isOfflineOrDisconnected() && SavedBots.configureSessionForCurrentServer((BotSession)session)) {
               offlineSessionCount++;
            }
         }

         return offlineSessionCount;
      }

      public void disconnectAllSessions() {
         for (Object session : SavedBots.sessionStore.registeredSessions) {
            ((BotSession)session).disconnect("disconnect");
         }
      }

      public int sendChatToAllSessions(String message) {
         int sentMessageCount = 0;

         for (Object session : SavedBots.sessionStore.registeredSessions) {
            if (((BotSession)session).sendChatMessage(message)) {
               sentMessageCount++;
            }
         }

         return sentMessageCount;
      }

      public long countOnlineSessions() {
         return SavedBots.sessionStore.registeredSessions.stream().filter(BotSession::isOnline).count();
      }

      public boolean isPanelOpen() {
         return SavedBots.registryPanelOpen;
      }

      public void setPanelOpen(boolean open) {
         SavedBots.registryPanelOpen = open;
      }
   }
}

