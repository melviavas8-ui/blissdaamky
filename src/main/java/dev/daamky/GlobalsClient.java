package dev.daamky;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;

public final class GlobalsClient {
    public static final String GLOBAL_CHAT_ID = "global";
    private static final String DEFAULT_API_BASE_URL = "https://api.daamky.pw";
    private static final GlobalsClient INSTANCE = new GlobalsClient();
    private final Path settingsFile = FabricLoader.getInstance().getGameDir().resolve("daamky").resolve("globals.json");
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6L)).build();
    private final ScheduledExecutorService requestExecutor = Executors.newScheduledThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "daamky-globals");
        thread.setDaemon(true);
        return thread;
    });
    private String apiBaseUrl = "https://api.daamky.pw";
    private String accessToken;
    private boolean enabled;
    private volatile GlobalsClient.ConnectionState connectionState = GlobalsClient.ConnectionState.RECONNECTING;
    private volatile String connectionStatusMessage = "Подключение...";
    private volatile long lastEventSequence;
    private volatile boolean stateRefreshPending;
    private LastSeen currentUser;
    private List<LastSeen> friends = new ArrayList<>();
    private List<LastSeen> incomingFriendRequests = new ArrayList<>();
    private List<LastSeen> outgoingFriendRequests = new ArrayList<>();
    private int globalOnlineCount;
    private int globalMemberCount;
    private final Map<String, Integer> unreadCountsByChat = new HashMap<>();
    private final Map<String, Chat> latestMessageByChat = new HashMap<>();
    private final Map<String, Long> lastReadMessageIdByChat = new HashMap<>();
    private final Map<String, GlobalsClient.ChatSessionState> chatSessions = new HashMap<>();
    private final Map<String, Map<String, Long>> typingPlayersByChat = new HashMap<>();
    private final Map<String, Long> lastTypingRequestAtByChat = new HashMap<>();
    private volatile Map<String, GlobalsClient.PlayerCosmetics> cosmeticsByPlayerName = Map.of();
    private volatile Set<String> friendNamesLowercase = Set.of();
    private String selectedChatId;
    private int stateRevision;
    private final Map<String, ArrayDeque<Long>> recentMessageSendTimesByChat = new HashMap<>();
    private final Map<String, String> lastSentMessageByChat = new HashMap<>();
    private final Map<String, Long> lastSentMessageAtByChat = new HashMap<>();

    private GlobalsClient() {
        this.loadSavedSettings();
    }

    public static GlobalsClient getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (!this.enabled) {
            this.enabled = true;
            Thread pollThread = new Thread(this::connectionLoop, "daamky-globals-poll");
            pollThread.setDaemon(true);
            pollThread.start();
            this.requestExecutor.scheduleWithFixedDelay(this::sendPresenceHeartbeat, 20L, 20L, TimeUnit.SECONDS);
            this.requestExecutor.scheduleWithFixedDelay(this::refreshCosmetics, 4L, 8L, TimeUnit.SECONDS);
        }
    }

    private void connectionLoop() {
        long retryDelayMillis = 2000L;

        while (true) {
            try {
                JsonObject helloResponse = this.sendApiRequest("POST", "/hello", this.buildClientInfoPayload()).getAsJsonObject();
                this.lastEventSequence = helloResponse.get("seq").getAsLong();
                LastSeen currentUser = LastSeen.fromJson(helloResponse.getAsJsonObject("me"));
                this.executeOnClientThread(() -> this.currentUser = currentUser);
                this.refreshStateFromServer();
                this.connectionState = GlobalsClient.ConnectionState.CONNECTED;
                this.connectionStatusMessage = "В сети";
                retryDelayMillis = 2000L;

                while (true) {
                    JsonObject pollResponse = this.sendApiRequest("GET", "/poll?since=" + this.lastEventSequence, (JsonObject)null).getAsJsonObject();
                    this.lastEventSequence = pollResponse.get("seq").getAsLong();
                    if (pollResponse.has("reset") && pollResponse.get("reset").getAsBoolean()) {
                        this.refreshStateFromServer();
                        this.executeOnClientThread(this::refreshOpenChatHistories);
                    } else {
                        for (JsonElement eventElement : pollResponse.getAsJsonArray("events")) {
                            JsonObject eventJson = eventElement.getAsJsonObject();
                            this.executeOnClientThread(() -> this.handleServerEvent(eventJson));
                        }

                        if (this.stateRefreshPending) {
                            this.stateRefreshPending = false;
                            this.refreshStateFromServer();
                        }
                    }
                }
            } catch (GlobalsClient.GlobalsApiException apiException) {
                this.connectionState = GlobalsClient.ConnectionState.ERROR;
                this.connectionStatusMessage = apiException.getMessage();
            } catch (Exception connectionFailure) {
                this.connectionState = GlobalsClient.ConnectionState.ERROR;
                this.connectionStatusMessage = "Сервер недоступен";
            }

            try {
                Thread.sleep(retryDelayMillis);
            } catch (InterruptedException interrupted) {
                return;
            }

            retryDelayMillis = Math.min(retryDelayMillis * 2L, 30000L);
            this.connectionState = GlobalsClient.ConnectionState.RECONNECTING;
            this.connectionStatusMessage = "Переподключение...";
        }
    }

    private void refreshStateFromServer() throws IOException, InterruptedException {
        JsonObject stateSnapshot = this.sendApiRequest("GET", "/state", (JsonObject)null).getAsJsonObject();
        this.executeOnClientThread(() -> this.applyStateSnapshot(stateSnapshot));
    }

    private void applyStateSnapshot(JsonObject snapshotJson) {
        this.currentUser = LastSeen.fromJson(snapshotJson.getAsJsonObject("me"));
        ArrayList friendList = new ArrayList();
        HashSet normalizedFriendNames = new HashSet();

        for (JsonElement friendElement : snapshotJson.getAsJsonArray("friends")) {
            LastSeen friend = LastSeen.fromJson(friendElement.getAsJsonObject());
            friendList.add(friend);
            this.unreadCountsByChat.put(friend.userId, friend.unreadCount);
            this.lastReadMessageIdByChat.put(friend.userId, Math.max(this.lastReadMessageIdByChat.getOrDefault(friend.userId, 0L), friend.peerReadTimestamp));
            if (friend.lastMessage != null) {
                this.latestMessageByChat.put(friend.userId, friend.lastMessage);
            } else {
                this.latestMessageByChat.remove(friend.userId);
            }

            if (!friend.displayNickname.isEmpty()) {
                normalizedFriendNames.add(friend.displayNickname.toLowerCase(Locale.ROOT));
            }
        }

        this.friends = friendList;
        this.friendNamesLowercase = Collections.unmodifiableSet(normalizedFriendNames);
        this.incomingFriendRequests = parseLastSeenUsers(snapshotJson.getAsJsonArray("incoming"));
        this.outgoingFriendRequests = parseLastSeenUsers(snapshotJson.getAsJsonArray("outgoing"));
        JsonObject globalStatsJson = snapshotJson.getAsJsonObject("global");
        this.unreadCountsByChat.put("global", (int)LastSeen.getLong(globalStatsJson, "unread"));
        this.globalOnlineCount = (int)LastSeen.getLong(globalStatsJson, "online");
        this.globalMemberCount = (int)LastSeen.getLong(globalStatsJson, "members");
        if (globalStatsJson.has("last") && globalStatsJson.get("last").isJsonObject()) {
            this.latestMessageByChat.put("global", Chat.fromJson(globalStatsJson.getAsJsonObject("last")));
        }

        if (this.selectedChatId != null && !"global".equals(this.selectedChatId) && this.findFriendByName(this.selectedChatId) == null) {
            this.selectedChatId = null;
        }

        this.stateRevision++;
        if (this.selectedChatId != null && this.isGlobalsCategoryActive()) {
            this.sendReadReceipt(this.selectedChatId);
        }
    }

    private static List<LastSeen> parseLastSeenUsers(JsonArray userArray) {
        ArrayList users = new ArrayList();

        for (JsonElement userElement : userArray) {
            users.add(LastSeen.fromJson(userElement.getAsJsonObject()));
        }

        return users;
    }

    private void handleServerEvent(JsonObject eventJson) {
        String eventType = LastSeen.getString(eventJson, "type");
        switch (eventType) {
        case "message":
            this.handleIncomingMessage(Chat.fromJson(eventJson.getAsJsonObject("msg")));
            break;
        case "delete":
            long deletedMessageId = LastSeen.getLong(eventJson, "id");
            String chatId = "global".equals(LastSeen.getString(eventJson, "chat"))
            ? "global"
            : (
            this.currentUser != null && this.currentUser.userId.equals(LastSeen.getString(eventJson, "from"))
            ? LastSeen.getString(eventJson, "to")
            : LastSeen.getString(eventJson, "from")
            );
            Object chatSession = this.chatSessions.get(chatId);
            if (chatSession != null && ((GlobalsClient.ChatSessionState)chatSession).messages.removeIf(candidateMessage -> candidateMessage.getMessageId() == deletedMessageId)) {
                ((GlobalsClient.ChatSessionState)chatSession).revision++;
                Chat latestMessage = ((GlobalsClient.ChatSessionState)chatSession).messages.isEmpty()
                ? null
                : ((GlobalsClient.ChatSessionState)chatSession).messages.get(((GlobalsClient.ChatSessionState)chatSession).messages.size() - 1);
                if (latestMessage != null) {
                    this.latestMessageByChat.put(chatId, latestMessage);
                } else {
                    this.latestMessageByChat.remove(chatId);
                }
            }
            break;
        case "read":
            this.lastReadMessageIdByChat.merge(LastSeen.getString(eventJson, "peer"), LastSeen.getLong(eventJson, "id"), Math::max);
            break;
        case "typing":
            String typingChatId = LastSeen.getString(eventJson, "chat");
            this.typingPlayersByChat
            .computeIfAbsent(typingChatId, playerName -> new HashMap<>())
            .put(LastSeen.getString(eventJson, "fromName"), System.currentTimeMillis() + 5000L);
            break;
        case "state":
            this.stateRefreshPending = true;
            break;
        case "request":
            NotificationScheduler.II1Il11l111II11IIl("Globals", LastSeen.getString(eventJson, "from") + " хочет добавить вас в друзья", 3500L);
            break;
        case "accepted":
            NotificationScheduler.II1Il11l111II11IIl("Globals", LastSeen.getString(eventJson, "from") + " теперь у вас в друзьях", 3500L);
        }
    }

    private void handleIncomingMessage(Chat chat) {
        if (this.currentUser != null) {
            String chatId = chat.displayName(this.currentUser.userId);
            Object chatSession = this.chatSessions.get(chatId);
            if (chatSession != null
            && ((GlobalsClient.ChatSessionState)chatSession).historyLoaded
            && ((GlobalsClient.ChatSessionState)chatSession).messages.stream().noneMatch(existingMessage -> existingMessage.getMessageId() == chat.getMessageId())) {
                ((GlobalsClient.ChatSessionState)chatSession).messages.add(chat);
                ((GlobalsClient.ChatSessionState)chatSession).revision++;
            }

            this.latestMessageByChat.put(chatId, chat);
            Map<String, Long> typingExpirations = this.typingPlayersByChat.get(chat.isGlobal() ? "global" : chat.senderId());
            if (typingExpirations != null) {
                typingExpirations.remove(chat.getSenderName());
            }

            boolean sentByCurrentUser = this.currentUser.userId.equals(chat.senderId());
            if (!sentByCurrentUser) {
                if (chatId.equals(this.selectedChatId) && this.isGlobalsCategoryActive()) {
                    this.sendReadReceipt(chatId);
                } else {
                    this.unreadCountsByChat.merge(chatId, 1, Integer::sum);
                    if (!chat.isGlobal()) {
                        NotificationScheduler.II1Il11l111II11IIl(chat.getSenderName(), Emoji.removeEmojiSequences(chat.getMessage()), 3500L);
                    }
                }
            }
        }
    }

    private void refreshOpenChatHistories() {
        for (java.util.Map.Entry<?, ?> sessionEntry : this.chatSessions.entrySet()) {
            if (((GlobalsClient.ChatSessionState)sessionEntry.getValue()).historyLoaded) {
                ((GlobalsClient.ChatSessionState)sessionEntry.getValue()).historyLoaded = false;
                this.loadChatHistory((String)sessionEntry.getKey());
            }
        }
    }

    private boolean isGlobalsCategoryActive() {
        Search search = Search.getInstance();
        return search.isOpen() && search.getCategorySidebar().getSelectedCategory() == ModuleCategory.GLOBALS;
    }

    public void selectChat(String chatId) {
        this.selectedChatId = chatId;
        if (chatId != null) {
            GlobalsClient.ChatSessionState chatSession = this.getOrCreateChatSession(chatId);
            if (!chatSession.historyLoaded && !chatSession.historyLoading) {
                this.loadChatHistory(chatId);
            }

            this.sendReadReceipt(chatId);
        }
    }

    public String getSelectedChatId() {
        return this.selectedChatId;
    }

    public void markSelectedChatReadIfUnread() {
        if (this.selectedChatId != null && this.unreadCountsByChat.getOrDefault(this.selectedChatId, 0) > 0) {
            this.sendReadReceipt(this.selectedChatId);
        }
    }

    public GlobalsClient.ChatSessionState getOrCreateChatSession(String chatId) {
        return this.chatSessions.computeIfAbsent(chatId, requestedChatId -> new GlobalsClient.ChatSessionState());
    }

    private void loadChatHistory(String chatId) {
        GlobalsClient.ChatSessionState chatSession = this.getOrCreateChatSession(chatId);
        chatSession.historyLoading = true;
        this.sendAsyncApiRequest("GET", "/messages?peer=" + encodeQueryParameter(chatId) + "&limit=60", null, messageArray -> {
            chatSession.messages.clear();

            for (JsonElement messageElement : messageArray.getAsJsonArray()) {
                chatSession.messages.add(Chat.fromJson(messageElement.getAsJsonObject()));
            }

            chatSession.hasOlderMessages = chatSession.messages.size() >= 60;
            chatSession.historyLoaded = true;
            chatSession.historyLoading = false;
            chatSession.revision++;
            if (chatId.equals(this.selectedChatId)) {
                this.sendReadReceipt(chatId);
            }
        }, errorMessage -> chatSession.historyLoading = false);
    }

    public void loadOlderChatMessages(String chatId) {
        GlobalsClient.ChatSessionState chatSession = this.getOrCreateChatSession(chatId);
        if (chatSession.historyLoaded && !chatSession.historyLoading && chatSession.hasOlderMessages && !chatSession.messages.isEmpty()) {
            chatSession.historyLoading = true;
            long oldestMessageId = chatSession.messages.get(0).getMessageId();
            this.sendAsyncApiRequest("GET", "/messages?peer=" + encodeQueryParameter(chatId) + "&limit=60&before=" + oldestMessageId, null, olderMessageArray -> {
                ArrayList olderMessages = new ArrayList();

                for (JsonElement messageElement : olderMessageArray.getAsJsonArray()) {
                    olderMessages.add(Chat.fromJson(messageElement.getAsJsonObject()));
                }

                chatSession.messages.addAll(0, olderMessages);
                chatSession.hasOlderMessages = olderMessages.size() >= 60;
                chatSession.historyLoading = false;
                chatSession.revision++;
            }, errorMessage -> chatSession.historyLoading = false);
        }
    }

    private void sendReadReceipt(String chatId) {
        Object chatSession = this.chatSessions.get(chatId);
        Chat latestMessage = chatSession != null && !((GlobalsClient.ChatSessionState)chatSession).messages.isEmpty()
        ? ((GlobalsClient.ChatSessionState)chatSession).messages.get(((GlobalsClient.ChatSessionState)chatSession).messages.size() - 1)
        : this.latestMessageByChat.get(chatId);
        this.unreadCountsByChat.put(chatId, 0);
        if (latestMessage != null) {
            JsonObject readReceiptPayload = new JsonObject();
            readReceiptPayload.addProperty("peer", chatId);
            readReceiptPayload.addProperty("id", latestMessage.getMessageId());
            this.sendAsyncApiRequest("POST", "/read", readReceiptPayload, responseJson -> {}, errorMessage -> {});
        }
    }

    public void sendChatMessage(String chatId, String messageText, Consumer<String> errorCallback) {
        String validationError = this.validateMessageSend(chatId, messageText);
        if (validationError != null) {
            errorCallback.accept(validationError);
        } else {
            JsonObject messagePayload = new JsonObject();
            messagePayload.addProperty("peer", chatId);
            messagePayload.addProperty("text", messageText);
            this.sendAsyncApiRequest(
            "POST", "/messages", messagePayload, sentMessageJson -> this.handleIncomingMessage(Chat.fromJson(sentMessageJson.getAsJsonObject())), errorCallback
            );
        }
    }

    private String validateMessageSend(String chatId, String messageText) {
        long nowMillis = System.currentTimeMillis();
        ArrayDeque<Long> recentSendTimestamps = this.recentMessageSendTimesByChat.computeIfAbsent(chatId, chatKey -> new ArrayDeque<>());

        while (!recentSendTimestamps.isEmpty() && nowMillis - recentSendTimestamps.peekFirst() > 10000L) {
            recentSendTimestamps.pollFirst();
        }

        if (!recentSendTimestamps.isEmpty() && nowMillis - recentSendTimestamps.peekLast() < 700L) {
            return "Слишком быстро, подождите немного";
        }

        if (recentSendTimestamps.size() >= 5) {
            return "Не спамьте: подождите " + ((10000L - (nowMillis - recentSendTimestamps.peekFirst())) / 1000L + 1L) + " с";
        }

        String normalizedMessage = messageText.trim().toLowerCase(Locale.ROOT);
        if (normalizedMessage.equals(this.lastSentMessageByChat.get(chatId)) && nowMillis - this.lastSentMessageAtByChat.getOrDefault(chatId, 0L) < 15000L) {
            return "Это сообщение уже отправлено";
        }

        recentSendTimestamps.addLast(nowMillis);
        this.lastSentMessageByChat.put(chatId, normalizedMessage);
        this.lastSentMessageAtByChat.put(chatId, nowMillis);
        return null;
    }

    public void deleteChatMessage(String chatId, long messageId, Consumer<String> errorCallback) {
        JsonObject deletePayload = new JsonObject();
        deletePayload.addProperty("peer", chatId);
        deletePayload.addProperty("id", messageId);
        this.sendAsyncApiRequest("POST", "/messages/delete", deletePayload, responseJson -> {}, errorCallback);
    }

    public void sendTypingIndicator(String chatId) {
        long nowMillis = System.currentTimeMillis();
        if (nowMillis - this.lastTypingRequestAtByChat.getOrDefault(chatId, 0L) >= 3000L && this.connectionState == GlobalsClient.ConnectionState.CONNECTED) {
            this.lastTypingRequestAtByChat.put(chatId, nowMillis);
            JsonObject typingPayload = new JsonObject();
            typingPayload.addProperty("peer", chatId);
            this.sendAsyncApiRequest("POST", "/typing", typingPayload, responseJson -> {}, errorMessage -> {});
        }
    }

    public List<String> getTypingPlayerNames(String chatId) {
        Map<String, Long> typingExpiryByPlayer = this.typingPlayersByChat.get(chatId);
        if (typingExpiryByPlayer == null) {
            return List.of();
        }

        long nowMillis = System.currentTimeMillis();
        typingExpiryByPlayer.values().removeIf(expiresAtMillis -> expiresAtMillis < nowMillis);
        return new ArrayList<>(typingExpiryByPlayer.keySet());
    }

    public void sendFriendRequest(String playerNameQuery, Consumer<String> successCallback, Consumer<String> errorCallback) {
        JsonObject requestPayload = new JsonObject();
        requestPayload.addProperty("query", playerNameQuery);
        this.sendAsyncApiRequest(
        "POST", "/friends/add", requestPayload, responseJson -> successCallback.accept(LastSeen.getString(responseJson.getAsJsonObject(), "status")), errorCallback
        );
    }

    public void updateFriendRelationship(String relationshipAction, String userId, Consumer<String> errorCallback) {
        JsonObject requestPayload = new JsonObject();
        requestPayload.addProperty("uid", userId);
        this.sendAsyncApiRequest("POST", "/friends/" + relationshipAction, requestPayload, responseJson -> this.stateRefreshPending = true, errorCallback);
    }

    public void updateProfile(String displayName, String biography, Runnable successCallback, Consumer<String> errorCallback) {
        JsonObject profilePayload = new JsonObject();
        if (displayName != null) {
            profilePayload.addProperty("name", displayName);
        }

        if (biography != null) {
            profilePayload.addProperty("bio", biography);
        }

        this.sendAsyncApiRequest("PUT", "/profile", profilePayload, updatedProfileJson -> {
            this.currentUser = LastSeen.fromJson(updatedProfileJson.getAsJsonObject());
            this.stateRevision++;
            successCallback.run();
        }, errorCallback);
    }

    private void sendPresenceHeartbeat() {
        try {
            if (this.connectionState == GlobalsClient.ConnectionState.CONNECTED) {
                JsonObject[] payloadHolder = new JsonObject[1];
                MinecraftClient.getInstance().submit(() -> payloadHolder[0] = this.buildClientInfoPayload()).join();
                this.sendApiRequest("POST", "/presence", payloadHolder[0]);
            }
        } catch (Exception presenceRequestFailure) {
        }
    }

    private void refreshCosmetics() {
        try {
            if (this.connectionState != GlobalsClient.ConnectionState.CONNECTED) {
                return;
            }

            JsonArray onlinePlayerNames = (JsonArray)MinecraftClient.getInstance().submit(this::collectOnlinePlayerNames).join();
            if (onlinePlayerNames.isEmpty()) {
                this.cosmeticsByPlayerName = Map.of();
                return;
            }

            JsonObject cosmeticsRequest = new JsonObject();
            cosmeticsRequest.add("nicks", onlinePlayerNames);
            JsonObject cosmeticsResponse = this.sendApiRequest("POST", "/cosmetics", cosmeticsRequest).getAsJsonObject();
            HashMap cosmeticsByName = new HashMap();

            for (java.util.Map.Entry<?, ?> playerCosmeticsEntry : cosmeticsResponse.entrySet()) {
                JsonObject playerCosmeticsJson = ((JsonElement)playerCosmeticsEntry.getValue()).getAsJsonObject();
                Map cosmeticsBySlot = Cape.getInstance().parseSelection(playerCosmeticsJson.getAsJsonObject("cosmetics"));
                cosmeticsByName.put(
                ((String)playerCosmeticsEntry.getKey()).toLowerCase(Locale.ROOT),
                new GlobalsClient.PlayerCosmetics(
                LastSeen.getString(playerCosmeticsJson, "uid"),
                LastSeen.getString(playerCosmeticsJson, "name"),
                playerCosmeticsJson.get("friend").getAsBoolean(),
                List.copyOf(cosmeticsBySlot.values())
                )
                );
            }

            this.cosmeticsByPlayerName = Collections.unmodifiableMap(cosmeticsByName);
        } catch (Exception cosmeticsRefreshFailure) {
        }
    }

    private JsonArray collectOnlinePlayerNames() {
        JsonArray playerNames = new JsonArray();
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient.getNetworkHandler() != null && minecraftClient.player != null) {
            String currentPlayerName = minecraftClient.player.getGameProfile().name();

            for (PlayerListEntry playerListEntry : minecraftClient.getNetworkHandler().getPlayerList()) {
                String listedPlayerName = playerListEntry.getProfile().name();
                if (listedPlayerName != null && !listedPlayerName.equalsIgnoreCase(currentPlayerName) && listedPlayerName.matches("[A-Za-z0-9_]{1,16}") && playerNames.size() < 200) {
                    playerNames.add(listedPlayerName);
                }
            }

            return playerNames;
        } else {
            return playerNames;
        }
    }

    private JsonObject buildClientInfoPayload() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        JsonObject clientInfoJson = new JsonObject();
        clientInfoJson.addProperty("nick", minecraftClient.getGameProfile().name());
        clientInfoJson.addProperty("mcUuid", minecraftClient.getGameProfile().id().toString());
        ServerInfo currentServerInfo = minecraftClient.getCurrentServerEntry();
        clientInfoJson.addProperty("server", currentServerInfo != null ? currentServerInfo.address : (minecraftClient.isIntegratedServerRunning() ? "Одиночная игра" : ""));
        clientInfoJson.add("cosmetics", Cape.getInstance().serializeSelection());
        return clientInfoJson;
    }

    public GlobalsClient.PlayerCosmetics getCosmeticsForPlayer(String playerName) {
        return playerName == null ? null : this.cosmeticsByPlayerName.get(playerName.toLowerCase(Locale.ROOT));
    }

    public boolean isFriend(String playerName) {
        if (playerName == null) {
            return false;
        }

        String normalizedPlayerName = playerName.toLowerCase(Locale.ROOT);
        if (this.friendNamesLowercase.contains(normalizedPlayerName)) {
            return true;
        }

        Object playerCosmetics = this.cosmeticsByPlayerName.get(normalizedPlayerName);
        return playerCosmetics != null && ((GlobalsClient.PlayerCosmetics)playerCosmetics).isFriend();
    }

    public LastSeen getCurrentUser() {
        return this.currentUser;
    }

    public List<LastSeen> getFriends() {
        return this.friends;
    }

    public List<LastSeen> getIncomingFriendRequests() {
        return this.incomingFriendRequests;
    }

    public List<LastSeen> getOutgoingFriendRequests() {
        return this.outgoingFriendRequests;
    }

    public LastSeen findFriendByName(String playerName) {
        for (Object friend : this.friends) {
            if (((LastSeen)friend).userId.equals(playerName)) {
                return (LastSeen)friend;
            }
        }

        return null;
    }

    public int getUnreadCount(String chatId) {

        return this.unreadCountsByChat.getOrDefault(chatId, 0);
    }

    public int getTotalUnreadCount() {
        int totalUnreadCount = 0;

        for (int unreadCount : this.unreadCountsByChat.values()) {
            totalUnreadCount += unreadCount;
        }

        return totalUnreadCount;
    }

    public Chat getLatestMessage(String chatId) {
        return this.latestMessageByChat.get(chatId);
    }

    public long getLastReadMessageId(String chatId) {
        return this.lastReadMessageIdByChat.getOrDefault(chatId, 0L);
    }

    public int getGlobalOnlineCount() {
        return this.globalOnlineCount;
    }

    public int getGlobalMemberCount() {
        return this.globalMemberCount;
    }

    public int getStateRevision() {
        return this.stateRevision;
    }

    public GlobalsClient.ConnectionState getConnectionState() {
        return this.connectionState;
    }

    public String getConnectionStatusMessage() {
        return this.connectionStatusMessage;
    }

    public String getApiBaseUrl() {
        return this.apiBaseUrl;
    }

    private void executeOnClientThread(Runnable task) {
        MinecraftClient.getInstance().execute(task);
    }

    private void sendAsyncApiRequest(String httpMethod, String apiPath, JsonObject requestBody, Consumer<JsonElement> responseCallback, Consumer<String> errorCallback) {
        CompletableFuture.<JsonElement>supplyAsync(() -> {
            try {
                return this.sendApiRequest(httpMethod, apiPath, requestBody);
            } catch (Exception requestFailure) {
                throw new CompletionException(requestFailure);
            }
        }, this.requestExecutor).whenComplete((responseJson, completionFailure) -> this.executeOnClientThread(() -> {
            if (completionFailure == null) {
                responseCallback.accept(responseJson);
            } else {
                Throwable apiFailure = completionFailure instanceof CompletionException && completionFailure.getCause() != null ? completionFailure.getCause() : completionFailure;
                errorCallback.accept(apiFailure instanceof GlobalsClient.GlobalsApiException ? apiFailure.getMessage() : "Сервер недоступен");
            }
        }));
    }

    private JsonElement sendApiRequest(String httpMethod, String apiPath, JsonObject requestBody) throws IOException, InterruptedException {
        Builder requestBuilder = HttpRequest.newBuilder(URI.create(this.apiBaseUrl + "/api/v1/globals" + apiPath))
        .timeout(Duration.ofSeconds(apiPath.startsWith("/poll") ? 40L : 10L))
        .header("Authorization", "Bearer " + this.accessToken)
        .header("Content-Type", "application/json");
        BodyPublisher requestBodyPublisher = requestBody == null ? BodyPublishers.noBody() : BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8);
        HttpResponse httpResponse = this.httpClient.send(requestBuilder.method(httpMethod, requestBodyPublisher).build(), BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (httpResponse.statusCode() / 100 != 2) {
            String errorResponseBody;
            try {
                errorResponseBody = JsonParser.parseString((String)httpResponse.body()).getAsJsonObject().get("error").getAsString();
            } catch (RuntimeException responseParseFailure) {
                errorResponseBody = "Ошибка сервера (" + httpResponse.statusCode() + ")";
            }

            throw new GlobalsClient.GlobalsApiException(errorResponseBody);
        } else {
            return JsonParser.parseString((String)httpResponse.body());
        }
    }

    private static String encodeQueryParameter(String queryValue) {
        return URLEncoder.encode(queryValue, StandardCharsets.UTF_8);
    }

    private void loadSavedSettings() {
        try {
            if (Files.isRegularFile(this.settingsFile)) {
                JsonObject settingsJson = JsonParser.parseString(Files.readString(this.settingsFile, StandardCharsets.UTF_8)).getAsJsonObject();
                if (settingsJson.has("url")) {
                    this.apiBaseUrl = settingsJson.get("url").getAsString().replaceAll("/+$", "");
                }

                if (settingsJson.has("token") && settingsJson.get("token").getAsString().matches("[0-9a-f]{64}")) {
                    this.accessToken = settingsJson.get("token").getAsString();
                }
            }
        } catch (IOException | RuntimeException settingsReadFailure) {
            settingsReadFailure.printStackTrace();
        }

        String baseUrlOverride = System.getProperty("daamky.globals.url");
        if (baseUrlOverride != null && !baseUrlOverride.isBlank()) {
            this.apiBaseUrl = baseUrlOverride.replaceAll("/+$", "");
        }

        if (this.accessToken == null) {
            byte[] tokenBytes = new byte[32];
            new SecureRandom().nextBytes(tokenBytes);
            this.accessToken = HexFormat.of().formatHex(tokenBytes);
            this.saveSettings();
        }
    }

    private void saveSettings() {
        JsonObject settingsJson = new JsonObject();
        settingsJson.addProperty("url", this.apiBaseUrl);
        settingsJson.addProperty("token", this.accessToken);

        try {
            Files.createDirectories(this.settingsFile.getParent());
            Files.writeString(this.settingsFile, settingsJson.toString(), StandardCharsets.UTF_8);
        } catch (IOException settingsWriteFailure) {
            settingsWriteFailure.printStackTrace();
        }
    }

    public static final class ChatSessionState {
        public final List<Chat> messages = new ArrayList<>();
        public boolean historyLoaded;
        public boolean historyLoading;
        public boolean hasOlderMessages = true;
        public int revision;
    }

    public record PlayerCosmetics(String userId, String playerName, boolean isFriend, Collection<Model> cosmetics) {

        public PlayerCosmetics(String userId, String playerName, boolean isFriend, Collection<Model> cosmetics) {
            this.userId = userId;
            this.playerName = playerName;
            this.isFriend = isFriend;
            this.cosmetics = cosmetics;
        }
    }

    public enum ConnectionState {
        RECONNECTING,
        CONNECTED,
        ERROR;

        // $VF: synthetic method
        private static GlobalsClient.ConnectionState[] allConnectionStates() {
            return new GlobalsClient.ConnectionState[]{RECONNECTING, CONNECTED, ERROR};
        }
    }

    public static final class GlobalsApiException extends RuntimeException {
        public GlobalsApiException(String message) {
            super(message);
        }
    }
}

