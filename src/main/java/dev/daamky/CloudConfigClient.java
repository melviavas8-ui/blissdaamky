/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.loader.api.FabricLoader
 */
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;

public final class CloudConfigClient {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    public static final Pattern CONFIG_NAME_PATTERN = Pattern.compile("[A-Za-z0-9А-Яа-яЁё _.-]{1,32}");
    private static final String DEFAULT_API_URL = "https://api.daamky.pw";
    private static final CloudConfigClient INSTANCE = new CloudConfigClient();
    private final Path CREDENTIALS_FILE = FabricLoader.getInstance().getGameDir().resolve("daamky").resolve("cloud.json");
    private final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
    private String apiBaseUrl = "https://api.daamky.pw";
    private String apiToken;
    private volatile UserIdentity authenticatedUser;
    private volatile String lastErrorMessage = "";
// https://t.me/pasterplace

    private CloudConfigClient() {
        this.loadCredentials();
    }
// https://t.me/pasterplace

    public static CloudConfigClient getInstance() {
        return INSTANCE;
    }
// https://t.me/pasterplace

    public String getTokenFingerprint() {
        try {
            byte[] tokenBytes = MessageDigest.getInstance("SHA-256").digest(this.apiToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(tokenBytes, 0, 4);
        }
        catch (NoSuchAlgorithmException digestException) {
            return "????????";
        }
    }
// https://t.me/pasterplace

    public String getApiBaseUrl() {
        return this.apiBaseUrl;
    }
// https://t.me/pasterplace

    public UserIdentity getAuthenticatedUser() {
        return this.authenticatedUser;
    }
// https://t.me/pasterplace

    public String getLastErrorMessage() {
        return this.lastErrorMessage;
    }
// https://t.me/pasterplace

    public CompletableFuture<UserIdentity> fetchCurrentUser() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(this.apiBaseUrl + "/api/v1/client/me")).timeout(Duration.ofSeconds(10L)).header("Authorization", "Bearer " + this.apiToken).GET();
        return this.sendRequest(requestBuilder).thenApply(responseBody -> {
            UserIdentity identity;
            JsonObject identityJson = JsonParser.parseString((String)responseBody).getAsJsonObject();
            this.authenticatedUser = identity = new UserIdentity(identityJson.get("uid").getAsLong(), identityJson.get("username").getAsString());
            this.lastErrorMessage = "";
            return identity;
        }).whenComplete((fetchedIdentity, requestFailure) -> {
            if (requestFailure != null) {
                Throwable rootCause = requestFailure.getCause() != null ? requestFailure.getCause() : requestFailure;
                this.authenticatedUser = null;
                this.lastErrorMessage = rootCause instanceof CloudApiException ? rootCause.getMessage() : "Нет связи с сервером";
            }
        });
    }
// https://t.me/pasterplace

    public CompletableFuture<List<CloudConfigEntry>> listPublicConfigs() {
        return this.sendRequest(this.createConfigRequest("").GET()).thenApply(responseBody -> {
            ArrayList<CloudConfigEntry> configEntries = new ArrayList<>();
            JsonArray configArray = JsonParser.parseString((String)responseBody).getAsJsonArray();
            for (JsonElement configElement : configArray) {
                JsonObject configJson = configElement.getAsJsonObject();
                configEntries.add(new CloudConfigEntry(configJson.get("name").getAsString(), configJson.get("size").getAsLong(), configJson.get("updatedAt").getAsLong(), null, "", ConfigEndpoint.PUBLIC));
            }
            return configEntries;
        });
    }
// https://t.me/pasterplace

    public CompletableFuture<List<CloudConfigEntry>> listUserConfigs() {
        return this.sendRequest(this.createUserConfigRequest("").GET()).thenApply(responseBody -> CloudConfigClient.parseConfigEntries(responseBody, ConfigEndpoint.USER));
    }
// https://t.me/pasterplace

    public CompletableFuture<List<CloudConfigEntry>> listSharedConfigs() {
        return this.sendRequest(this.createSharedConfigRequest("").GET()).thenApply(responseBody -> CloudConfigClient.parseConfigEntries(responseBody, ConfigEndpoint.SHARED));
    }
// https://t.me/pasterplace

    private static List<CloudConfigEntry> parseConfigEntries(String responseBody, ConfigEndpoint endpoint) {
        ArrayList<CloudConfigEntry> configEntries = new ArrayList<>();
        for (JsonElement configElement : JsonParser.parseString((String)responseBody).getAsJsonArray()) {
            JsonObject configJson = configElement.getAsJsonObject();
            configEntries.add(new CloudConfigEntry(configJson.get("name").getAsString(), configJson.has("size") ? configJson.get("size").getAsLong() : 0L, configJson.has("updatedAt") ? configJson.get("updatedAt").getAsLong() : 0L, configJson.get("id").getAsString(), configJson.has("author") && !configJson.get("author").isJsonNull() ? configJson.get("author").getAsString() : "", endpoint));
        }
        return configEntries;
    }
// https://t.me/pasterplace

    public CompletableFuture<String> createShareCode(String configName) {
        return this.sendRequest(this.createConfigRequest(CloudConfigClient.encodeConfigName(configName) + "/share").POST(HttpRequest.BodyPublishers.noBody())).thenApply(responseBody -> JsonParser.parseString((String)responseBody).getAsJsonObject().get("code").getAsString());
    }
// https://t.me/pasterplace

    public CompletableFuture<Void> revokeShareCode(String configName) {
        return this.sendRequest(this.createConfigRequest(CloudConfigClient.encodeConfigName(configName) + "/share").DELETE()).thenApply(responseBody -> null);
    }
// https://t.me/pasterplace

    public CompletableFuture<Void> redeemShareCode(String shareCode) {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("code", shareCode);
        return this.sendRequest(this.createSharedConfigRequest("").header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(requestJson.toString(), StandardCharsets.UTF_8))).thenApply(responseBody -> null);
    }
// https://t.me/pasterplace

    public CompletableFuture<JsonObject> fetchSharedConfig(String sharedConfigId) {
        return this.sendRequest(this.createSharedConfigRequest(CloudConfigClient.encodePathSegment(sharedConfigId)).GET()).thenApply(responseBody -> JsonParser.parseString((String)responseBody).getAsJsonObject());
    }
// https://t.me/pasterplace

    public CompletableFuture<Void> deleteSharedConfig(String sharedConfigId) {
        return this.sendRequest(this.createSharedConfigRequest(CloudConfigClient.encodePathSegment(sharedConfigId)).DELETE()).thenApply(responseBody -> null);
    }
// https://t.me/pasterplace

    private static String encodePathSegment(String pathSegment) {
        return "/" + URLEncoder.encode(pathSegment, StandardCharsets.UTF_8).replace("+", "%20");
    }
// https://t.me/pasterplace

    private HttpRequest.Builder createSharedConfigRequest(String pathSuffix) {
        return HttpRequest.newBuilder(URI.create(this.apiBaseUrl + "/api/v1/shared" + pathSuffix)).timeout(Duration.ofSeconds(10L)).header("Authorization", "Bearer " + this.apiToken);
    }
// https://t.me/pasterplace

    public CompletableFuture<JsonObject> fetchUserConfig(String configId) {
        return this.sendRequest(this.createUserConfigRequest("/" + URLEncoder.encode(configId, StandardCharsets.UTF_8).replace("+", "%20")).GET()).thenApply(responseBody -> JsonParser.parseString((String)responseBody).getAsJsonObject());
    }
// https://t.me/pasterplace

    private HttpRequest.Builder createUserConfigRequest(String pathSuffix) {
        return HttpRequest.newBuilder(URI.create(this.apiBaseUrl + "/api/v1/client/configs" + pathSuffix)).timeout(Duration.ofSeconds(10L)).header("Authorization", "Bearer " + this.apiToken);
    }
// https://t.me/pasterplace

    public CompletableFuture<JsonObject> fetchPublicConfig(String configName) {
        return this.sendRequest(this.createConfigRequest(CloudConfigClient.encodeConfigName(configName)).GET()).thenApply(responseBody -> JsonParser.parseString((String)responseBody).getAsJsonObject());
    }
// https://t.me/pasterplace

    public CompletableFuture<Void> updateCloudConfig(String configName, JsonObject configJson) {
        return this.sendRequest(this.createConfigRequest(CloudConfigClient.encodeConfigName(configName)).header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(configJson.toString(), StandardCharsets.UTF_8))).thenApply(responseBody -> null);
    }
// https://t.me/pasterplace

    public CompletableFuture<Void> deleteCloudConfig(String configName) {
        return this.sendRequest(this.createConfigRequest(CloudConfigClient.encodeConfigName(configName)).DELETE()).thenApply(responseBody -> null);
    }
// https://t.me/pasterplace

    private static String encodeConfigName(String configName) {
        if (!CONFIG_NAME_PATTERN.matcher(configName).matches()) {
            throw new IllegalArgumentException("Недопустимое имя конфига");
        }
        return "/" + URLEncoder.encode(configName, StandardCharsets.UTF_8).replace("+", "%20");
    }
// https://t.me/pasterplace

    private HttpRequest.Builder createConfigRequest(String pathSuffix) {
        return HttpRequest.newBuilder(URI.create(this.apiBaseUrl + "/api/v1/configs" + pathSuffix)).timeout(Duration.ofSeconds(10L)).header("Authorization", "Bearer " + this.apiToken);
    }
// https://t.me/pasterplace

    private CompletableFuture<String> sendRequest(HttpRequest.Builder requestBuilder) {
        return this.HTTP_CLIENT.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).thenApply(response -> {
            if (response.statusCode() / 100 != 2) {
                throw new CloudApiException(response.statusCode(), CloudConfigClient.parseApiErrorMessage((String)response.body()));
            }
            return (String)response.body();
        });
    }
// https://t.me/pasterplace

    private static String parseApiErrorMessage(String responseBody) {
        try {
            return JsonParser.parseString((String)responseBody).getAsJsonObject().get("error").getAsString();
        }
        catch (RuntimeException parseException) {
            return "Ошибка сервера";
        }
    }
// https://t.me/pasterplace

    private void loadCredentials() {
        try {
            if (Files.isRegularFile(this.CREDENTIALS_FILE, new LinkOption[0])) {
                JsonObject credentialsJson = JsonParser.parseString((String)Files.readString(this.CREDENTIALS_FILE, StandardCharsets.UTF_8)).getAsJsonObject();
                if (credentialsJson.has("url")) {
                    this.apiBaseUrl = credentialsJson.get("url").getAsString().replaceAll("/+$", "");
                }
                if (credentialsJson.has("token") && credentialsJson.get("token").getAsString().matches("[0-9a-f]{64}")) {
                    this.apiToken = credentialsJson.get("token").getAsString();
                }
            }
        }
        catch (IOException | RuntimeException credentialsReadException) {
            credentialsReadException.printStackTrace();
        }
        String configuredApiUrl = System.getProperty("daamky.cloud.url");
        if (configuredApiUrl != null && !configuredApiUrl.isBlank()) {
            this.apiBaseUrl = configuredApiUrl.replaceAll("/+$", "");
        }
        if (this.apiToken == null) {
            byte[] generatedTokenBytes = new byte[32];
            new SecureRandom().nextBytes(generatedTokenBytes);
            this.apiToken = HexFormat.of().formatHex(generatedTokenBytes);
            this.saveCredentials();
        }
    }
// https://t.me/pasterplace

    private void saveCredentials() {
        JsonObject credentialsJson = new JsonObject();
        credentialsJson.addProperty("url", this.apiBaseUrl);
        credentialsJson.addProperty("token", this.apiToken);
        try {
            Files.createDirectories(this.CREDENTIALS_FILE.getParent(), new FileAttribute[0]);
            Files.writeString(this.CREDENTIALS_FILE, (CharSequence)credentialsJson.toString(), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (IOException credentialsWriteException) {
            credentialsWriteException.printStackTrace();
        }
    }

    public record UserIdentity(long userId, String username) {
    }

    public record CloudConfigEntry(String name, long size, long updatedAt, String id, String author, ConfigEndpoint endpoint) {
        public boolean isUserConfig() {
            return this.endpoint == ConfigEndpoint.USER;
        }

        public boolean isSharedConfig() {
            return this.endpoint == ConfigEndpoint.SHARED;
        }

        public boolean isPublicConfig() {
            return this.endpoint == ConfigEndpoint.PUBLIC;
        }

        public String displayName() {
            return switch (this.endpoint) {
                case PUBLIC -> this.name;
                case USER -> "#" + this.id;
                case SHARED -> "@" + this.id;
            };
        }
    }

    public enum ConfigEndpoint {
        PUBLIC,
        USER,
        SHARED
    }

    public static final class CloudApiException extends RuntimeException {
        private final int statusCode;

        public CloudApiException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int statusCode() {
            return this.statusCode;
        }
    }
}


