package dev.daamky;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkingBackend;
import net.minecraft.network.message.LastSeenMessageList.Acknowledgment;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public final class BotSession implements SavedBotSession {
    private static final int MAX_RECONNECT_ATTEMPTS = 100;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final String username;
    private volatile String serverAddress;
    private volatile int serverPort;
    private volatile BotSessionProfile botSessionProfile;
    private volatile BotSession.BotConnectionStatus connectionStatus = BotSession.BotConnectionStatus.OFFLINE;
    private volatile ClientConnection clientConnection;
    private volatile Joined joinedSession;
    private volatile boolean connectionDisconnectionHandled;
    private volatile String lastDisconnectReason = "";
    private volatile boolean excludedFromSavedSessionRegistry;
    private volatile boolean autoReconnectEnabled = true;
    private volatile boolean manuallyDisconnected;
    private volatile long reconnectAtMillis;
    private final ModuleProfile moduleProfile;
    private final List<BotSessionLogEntry> sessionHistory = new ArrayList<>();
    private final List<Text> chatMessages = new ArrayList<>();
    private final KeyBindingStateSnapshot keyBindingStateSnapshot = new KeyBindingStateSnapshot();
    private volatile int targetLobbyNumber;
    private volatile String targetLobbyPassword = "";
    private volatile BotSession.ConnectionPhase connectionPhase = BotSession.ConnectionPhase.IDLE;
    private volatile boolean targetLobbyFound;
    private volatile long lastAuthenticationCommandAt;
    private volatile long lastLobbyNavigationAt;
    private int lastHandledScreenSyncId = -1;
    private long nextLobbyNavigationAt;

    BotSession(String username, String serverAddress, int serverPort) {
        this.username = username;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.botSessionProfile = SettingFactoryRegistry.II1Il11l111II11IIl(serverAddress);
        this.moduleProfile = new ModuleProfile(username);
    }

    public void connect() {
        this.manuallyDisconnected = false;
        this.botSessionProfile.proxyServerBridge().ensureProxyMenuButton();
        this.lastDisconnectReason = "";
        this.connectionDisconnectionHandled = false;
        if (this.targetLobbyNumber > 0) {
            this.configureTargetLobby(this.targetLobbyNumber, this.targetLobbyPassword);
        }

        this.connectionStatus = BotSession.BotConnectionStatus.CONNECTING;
        Thread connectionThread = new Thread(this::connectToServer, "Daamky Bot " + this.username);
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    void updateServerAddressAndReconnect(String serverAddress, int serverPort) {
        ClientConnection clientConnection = this.clientConnection;
        if (clientConnection == null || !clientConnection.isOpen()) {
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;
            this.botSessionProfile = SettingFactoryRegistry.II1Il11l111II11IIl(serverAddress);
            this.clientConnection = null;
            this.joinedSession = null;
            this.connect();
        }
    }

    private void connectToServer() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();

        try {
            ServerAddress resolvedServerAddress = new ServerAddress(this.serverAddress, this.serverPort);
            Optional resolvedSocketAddress = AllowedAddressResolver.DEFAULT.resolve(resolvedServerAddress).map(Address::getInetSocketAddress);
            if (resolvedSocketAddress.isEmpty()) {
                this.lastDisconnectReason = "Не удалось найти сервер";
                this.setConnectionStatus(BotSession.BotConnectionStatus.DISCONNECTED);
                return;
            }

            ClientConnection clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
            ClientConnection.connect((InetSocketAddress)resolvedSocketAddress.get(), NetworkingBackend.remote(minecraftClient.options.shouldUseNativeTransport()), clientConnection)
            .syncUninterruptibly();
            this.clientConnection = clientConnection;
            SavedBots.registerBotClientConnection(clientConnection);
            clientConnection.connect(
            this.serverAddress, this.serverPort, BotLoginPacketHandler.II1Il11l111II11IIl(clientConnection, minecraftClient, this, this.botSessionProfile)
            );
            Thread.sleep(500L);
            clientConnection.send(new LoginHelloC2SPacket(this.username, this.botSessionProfile.proxyServerBridge().offlinePlayerUuid(this.username)));
            this.connectionStatus = BotSession.BotConnectionStatus.AUTHENTICATING;
        } catch (Exception exception) {
            this.lastDisconnectReason = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
            this.setConnectionStatus(BotSession.BotConnectionStatus.DISCONNECTED);
            exception.printStackTrace();
        }
    }

    public void tickConnection() {
        if (this.isOfflineOrDisconnected() && this.reconnectAtMillis != 0L && System.currentTimeMillis() >= this.reconnectAtMillis) {
            this.reconnectAtMillis = 0L;
            if (this.serverPort > 0) {
                this.updateServerAddressAndReconnect(this.serverAddress, this.serverPort);
            }
        }

        ClientConnection clientConnection = this.clientConnection;
        if (clientConnection != null) {
            if (clientConnection.isOpen()) {
                if (!SavedBots.isSwitchingToSession(this)) {
                    clientConnection.tick();
                }
            } else if (!this.connectionDisconnectionHandled) {
                this.connectionDisconnectionHandled = true;
                clientConnection.handleDisconnection();
                SavedBots.unregisterBotClientConnection(clientConnection);
            }
        }
    }

    @Override
    public boolean sendChatMessage(String message) {
        ClientConnection clientConnection = this.clientConnection;
        if (this.joinedSession != null && clientConnection != null && clientConnection.isOpen() && message != null && !message.isBlank()) {
            if (message.startsWith("/")) {
                clientConnection.send(new CommandExecutionC2SPacket(message.substring(1)));
            } else {
                clientConnection.send(
                new ChatMessageC2SPacket(message, Instant.now(), ThreadLocalRandom.current().nextLong(), null, new Acknowledgment(0, new BitSet(), (byte)0))
                );
            }

            this.appendSessionHistory("> " + message);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void disconnect(String reason) {
        this.manuallyDisconnected = true;
        this.reconnectAtMillis = 0L;
        ClientConnection clientConnection = this.clientConnection;
        if (clientConnection != null && clientConnection.isOpen()) {
            clientConnection.disconnect(Text.literal(reason));
        }

        this.keyBindingStateSnapshot.clear();
        this.setConnectionStatus(BotSession.BotConnectionStatus.DISCONNECTED);
    }

    public void disconnectFromServer() {
        this.disconnect("disconnect");
    }

    void setConnectionStatus(BotSession.BotConnectionStatus newStatus) {
        BotSession.BotConnectionStatus previousStatus = this.connectionStatus;
        this.connectionStatus = newStatus;
        DisconnectListenerRegistry.II1Il11l111II11IIl(this, previousStatus, newStatus);
    }

    void setJoinedSession(Joined joinedSession) {
        this.joinedSession = joinedSession;
    }

    boolean ownsSessionObject(Object sessionObject) {
        return this.joinedSession != null && this.joinedSession.isCameraEntity(sessionObject);
    }

    boolean hasPlayerControlContext() {
        return this.joinedSession != null && this.joinedSession.enterClientWorld();
    }

    void resetPlayerLoadedState() {
        if (this.joinedSession != null) {
            this.joinedSession.resetClientWorldState();
        }
    }

    void appendSessionHistory(String message) {
        synchronized (this.sessionHistory) {
            this.sessionHistory.add(new BotSessionLogEntry(LocalTime.now().format(TIME_FORMAT), message));

            while (this.sessionHistory.size() > 100) {
                this.sessionHistory.remove(0);
            }
        }
    }

    @Override
    public List<BotSessionLogEntry> getSessionHistory() {
        synchronized (this.sessionHistory) {
            return List.copyOf(this.sessionHistory);
        }
    }

    void appendChatMessage(Text message) {
        if (message != null) {
            this.chatMessages.add(message);

            while (this.chatMessages.size() > 100) {
                this.chatMessages.remove(0);
            }
        }
    }

    List<Text> getChatMessages() {
        return this.chatMessages;
    }

    void handleDisconnection(String reason) {
        if (this.autoReconnectEnabled && !this.manuallyDisconnected && this.serverPort > 0) {
            this.reconnectAtMillis = System.currentTimeMillis() + 5000L;
        }

        this.lastDisconnectReason = reason == null ? "" : reason;
        this.keyBindingStateSnapshot.clear();
        DisconnectListenerRegistry.II1Il11l111II11IIl(this, reason);
        this.setConnectionStatus(BotSession.BotConnectionStatus.DISCONNECTED);
        BotCaptchaLog.II1Il11l111II11IIl("[" + this.username + "] DISCONNECTED: " + BotCaptchaLog.l1I1IlllIlI(reason));
        if (!this.excludedFromSavedSessionRegistry) {
            MinecraftClient.getInstance().execute(() -> ClientChatLogger.l1I1IlllIlI("Bot " + this.username + " disconnected: " + reason));
        }
    }

    KeyBindingStateSnapshot getKeyBindingStateSnapshot() {
        return this.keyBindingStateSnapshot;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getServerAddress() {
        return this.botSessionProfile.proxyServerBridge().isUnavailableServer(this.serverAddress, this.serverPort)
        ? "No Server"
        : this.serverAddress + ":" + this.serverPort;
    }

    @Override
    public String getStatusName() {
        return this.connectionStatus.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public BotSession.BotConnectionStatus getConnectionStatus() {
        return this.connectionStatus;
    }

    @Override
    public boolean isOnline() {
        return this.connectionStatus == BotSession.BotConnectionStatus.ONLINE;
    }

    public boolean isOfflineOrDisconnected() {
        return this.connectionStatus == BotSession.BotConnectionStatus.OFFLINE || this.connectionStatus == BotSession.BotConnectionStatus.DISCONNECTED;
    }

    public boolean hasOnlineStatus() {
        return this.isOnline();
    }

    public boolean isAutoReconnectEnabled() {
        return this.autoReconnectEnabled;
    }

    public void setAutoReconnectEnabled(boolean enabled) {
        this.autoReconnectEnabled = enabled;
        if (!enabled) {
            this.reconnectAtMillis = 0L;
        }
    }

    public ModuleProfile getModuleProfile() {
        return this.moduleProfile;
    }

    public List<String> getFormattedSessionHistory() {
        ArrayList formattedHistory = new ArrayList();

        for (Object historyEntry : this.getSessionHistory()) {
            formattedHistory.add("[" + ((BotSessionLogEntry)historyEntry).serverName() + "] " + ((BotSessionLogEntry)historyEntry).message());
        }

        return formattedHistory;
    }

    public ClientWorld getSessionWorld() {
        return this.getClientWorld();
    }

    public ClientPlayerEntity getPlayer() {
        return this.getSessionPlayer();
    }

    public String getDetailedStatus() {
        return switch (this.connectionStatus) {
        case OFFLINE, DISCONNECTED -> "Оффлайн";
        case CONNECTING, AUTHENTICATING, LOADING -> "Подключение";
        case ONLINE -> {
                switch (this.connectionPhase) {
                case IDLE:
                    yield "В игре";
                case AUTHENTICATING:
                    yield "Авторизация / капча";
                case SELECTING_SERVER:
                    yield "Выбор грифа";
                case IN_SERVER_LOBBY:
                    yield "На грифе #" + this.targetLobbyNumber;
                default:
                    throw new MatchException(null, null);
                }
            }
        };
    }

    public String getLastDisconnectReason() {
        return this.lastDisconnectReason;
    }

    public boolean isExcludedFromSavedSessionRegistry() {
        return this.excludedFromSavedSessionRegistry;
    }

    void setExcludedFromSavedSessionRegistry(boolean suppressed) {
        this.excludedFromSavedSessionRegistry = suppressed;
    }

    ClientWorld getClientWorld() {
        return this.joinedSession != null ? this.joinedSession.getClientWorld() : null;
    }

    ClientPlayerEntity getSessionPlayer() {
        return this.joinedSession != null ? this.joinedSession.getClientPlayer() : null;
    }

    Entity getCameraEntity() {
        return this.joinedSession != null ? this.joinedSession.getCameraEntity() : null;
    }

    ClientPlayerInteractionManager getInteractionManager() {
        return this.joinedSession != null ? this.joinedSession.getInteractionManager() : null;
    }

    Screen getCurrentScreen() {
        return this.joinedSession != null ? this.joinedSession.getCurrentScreen() : null;
    }

    boolean runOnSessionThread(Runnable runnable) {
        return this.joinedSession != null && this.joinedSession.runWithScreenRestored(runnable);
    }

    @Override
    public boolean hasActivePlayerSession() {
        return this.isOnline() && this.getClientWorld() != null && this.getCameraEntity() != null;
    }

    public long getScoreboardBalance() {
        ClientWorld clientWorld = this.getClientWorld();
        if (clientWorld == null) {
            return -1L;
        }

        ClientPlayerEntity sessionPlayer = this.getSessionPlayer();
        String scoreboardPlayerName = sessionPlayer != null ? sessionPlayer.getNameForScoreboard() : this.username;
        return dev.daamky.ScoreboardBalanceParser.findBalanceInScoreboard(clientWorld.getScoreboard(), scoreboardPlayerName);
    }

    public void configureTargetLobby(int lobbyNumber, String lobbyPassword) {
        this.targetLobbyNumber = lobbyNumber;
        this.targetLobbyPassword = lobbyPassword;
        this.targetLobbyFound = false;
        this.connectionPhase = lobbyNumber > 0 ? BotSession.ConnectionPhase.AUTHENTICATING : BotSession.ConnectionPhase.IDLE;
        this.lastAuthenticationCommandAt = 0L;
    }

    public int getTargetLobbyNumber() {
        return this.targetLobbyNumber;
    }

    public boolean isConfiguredLobbyReady() {
        return this.connectionStatus == BotSession.BotConnectionStatus.ONLINE
        && this.connectionPhase == BotSession.ConnectionPhase.IN_SERVER_LOBBY
        && this.getSessionPlayer() != null
        && System.currentTimeMillis() - this.lastLobbyNavigationAt > 3000L;
    }

    public void returnToServerSelector() {
        if (this.sendChatMessage("/hub")) {
            this.connectionPhase = BotSession.ConnectionPhase.SELECTING_SERVER;
            this.targetLobbyFound = false;
            this.lastHandledScreenSyncId = -1;
            this.lastLobbyNavigationAt = System.currentTimeMillis();
            this.nextLobbyNavigationAt = this.lastLobbyNavigationAt + 4000L;
        }
    }

    void onGameJoinOrRespawn() {
        if (this.connectionPhase == BotSession.ConnectionPhase.IN_SERVER_LOBBY) {
            this.connectionPhase = BotSession.ConnectionPhase.SELECTING_SERVER;
            this.targetLobbyFound = false;
        }

        this.updateLobbyNavigationState();
    }

    void updateLobbyNavigationState() {
        this.lastLobbyNavigationAt = System.currentTimeMillis();
        this.nextLobbyNavigationAt = this.lastLobbyNavigationAt + 3000L;
        this.lastHandledScreenSyncId = -1;
        if (this.targetLobbyFound && this.connectionPhase == BotSession.ConnectionPhase.SELECTING_SERVER) {
            this.connectionPhase = BotSession.ConnectionPhase.IN_SERVER_LOBBY;
        }
    }

    void handleServerMessage(String message) {
        if (message != null && !message.isBlank()) {
            CaptchaLinkOpener.II1Il11l111II11IIl(message, this.username);
            String lowercaseMessage = message.toLowerCase(Locale.ROOT);
            if (this.targetLobbyNumber > 0
            && (
            this.connectionPhase == BotSession.ConnectionPhase.SELECTING_SERVER
            || this.connectionPhase == BotSession.ConnectionPhase.IN_SERVER_LOBBY
            )
            && lowercaseMessage.contains("не удалось подключиться")) {
                this.connectionPhase = BotSession.ConnectionPhase.SELECTING_SERVER;
                this.targetLobbyFound = false;
                this.lastHandledScreenSyncId = -1;
                this.nextLobbyNavigationAt = System.currentTimeMillis() + 5000L;
            }

            this.handleAuthenticationMessage(message);
        }
    }

    private void handleAuthenticationMessage(String message) {
        if (this.targetLobbyNumber > 0 && this.connectionPhase == BotSession.ConnectionPhase.AUTHENTICATING) {
            String lowercaseMessage = message.toLowerCase(Locale.ROOT);
            long nowMillis = System.currentTimeMillis();
            if (lowercaseMessage.contains("успешн")) {
                this.connectionPhase = BotSession.ConnectionPhase.SELECTING_SERVER;
                this.nextLobbyNavigationAt = nowMillis + 2000L;
            } else if (nowMillis - this.lastAuthenticationCommandAt >= 3000L) {
                if (lowercaseMessage.contains("/reg")) {
                    this.lastAuthenticationCommandAt = nowMillis;
                    this.sendChatMessage("/reg " + this.targetLobbyPassword + " " + this.targetLobbyPassword);
                } else if (lowercaseMessage.contains("/login")) {
                    this.lastAuthenticationCommandAt = nowMillis;
                    this.sendChatMessage("/login " + this.targetLobbyPassword);
                }
            }
        }
    }

    void handleLobbySelection(ClientPlayerEntity clientPlayerEntity, ClientPlayerInteractionManager clientPlayerInteractionManager) {
        if (this.targetLobbyNumber > 0 && clientPlayerEntity != null && clientPlayerInteractionManager != null) {
            long nowMillis = System.currentTimeMillis();
            if (this.connectionPhase == BotSession.ConnectionPhase.AUTHENTICATING) {
                boolean hasLobbyCompass = false;

                for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
                    if (clientPlayerEntity.getInventory().getStack(hotbarSlot).isOf(Items.COMPASS)) {
                        hasLobbyCompass = true;
                        break;
                    }
                }

                if (hasLobbyCompass || this.lastAuthenticationCommandAt != 0L && nowMillis - this.lastAuthenticationCommandAt > 6000L || this.lastAuthenticationCommandAt == 0L && nowMillis - this.lastLobbyNavigationAt > 20000L) {
                    this.connectionPhase = BotSession.ConnectionPhase.SELECTING_SERVER;
                    this.nextLobbyNavigationAt = Math.max(this.nextLobbyNavigationAt, nowMillis + 1000L);
                }
            }

            if (this.connectionPhase == BotSession.ConnectionPhase.SELECTING_SERVER && nowMillis >= this.nextLobbyNavigationAt) {
                if (clientPlayerEntity.currentScreenHandler == clientPlayerEntity.playerScreenHandler) {
                    for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
                        if (clientPlayerEntity.getInventory().getStack(hotbarSlot).isOf(Items.COMPASS)) {
                            clientPlayerEntity.getInventory().setSelectedSlot(hotbarSlot);
                            clientPlayerEntity.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
                            clientPlayerInteractionManager.interactItem(clientPlayerEntity, Hand.MAIN_HAND);
                            this.lastHandledScreenSyncId = -1;
                            this.nextLobbyNavigationAt = nowMillis + 3000L;
                            return;
                        }
                    }
                } else {
                    ScreenHandler selectionScreen = clientPlayerEntity.currentScreenHandler;
                    if (selectionScreen.syncId != this.lastHandledScreenSyncId) {
                        String screenTitle = this.joinedSession == null ? "" : this.joinedSession.getCurrentScreenTitle().toLowerCase(Locale.ROOT);
                        int matchingSlot = -1;
                        if (screenTitle.contains("выбор сервера") || screenTitle.contains("выберите сервер")) {
                            matchingSlot = findMatchingSlot(selectionScreen, itemName -> itemName.contains("гриф") || itemName.contains("анарх"));
                        } else if (screenTitle.contains("выбор мира") || screenTitle.contains("гриф") || screenTitle.contains("анарх")) {
                            Pattern lobbyNumberPattern = Pattern.compile("(^|\\D)" + this.targetLobbyNumber + "(\\D|$)");
                            matchingSlot = findMatchingSlot(selectionScreen, itemName -> lobbyNumberPattern.matcher(itemName).find());
                            if (matchingSlot != -1) {
                                this.targetLobbyFound = true;
                            }
                        }

                        if (matchingSlot != -1) {
                            clientPlayerInteractionManager.clickSlot(selectionScreen.syncId, matchingSlot, 0, SlotActionType.PICKUP, clientPlayerEntity);
                            this.lastHandledScreenSyncId = selectionScreen.syncId;
                            this.nextLobbyNavigationAt = nowMillis + 1500L;
                        }
                    }
                }
            }
        }
    }

    private static int findMatchingSlot(ScreenHandler containerScreenHandler, Predicate<String> namePredicate) {
        int containerSlotCount = Math.max(0, containerScreenHandler.slots.size() - 36);

        for (int slotIndex = 0; slotIndex < containerSlotCount; slotIndex++) {
            ItemStack stack = ((Slot)containerScreenHandler.slots.get(slotIndex)).getStack();
            if (!stack.isEmpty() && namePredicate.test(stack.getName().getString().toLowerCase(Locale.ROOT))) {
                return slotIndex;
            }
        }

        return -1;
    }

    private enum ConnectionPhase {
        IDLE,
        AUTHENTICATING,
        SELECTING_SERVER,
        IN_SERVER_LOBBY;

        // $VF: synthetic method
        private static BotSession.ConnectionPhase[] getAllPhases() {
            return new BotSession.ConnectionPhase[]{IDLE, AUTHENTICATING, SELECTING_SERVER, IN_SERVER_LOBBY};
        }
    }

    public enum BotConnectionStatus {
        OFFLINE("Оффлайн"),
        CONNECTING("Подключение"),
        AUTHENTICATING("Вход"),
        LOADING("Загрузка"),
        ONLINE("Онлайн"),
        DISCONNECTED("Оффлайн");

        private final String displayName;

        BotConnectionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        // $VF: synthetic method
        private static BotSession.BotConnectionStatus[] getAllStatuses() {
            return new BotSession.BotConnectionStatus[]{OFFLINE, CONNECTING, AUTHENTICATING, LOADING, ONLINE, DISCONNECTED};
        }
    }
}

