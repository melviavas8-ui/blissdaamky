/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.session.Session
 *  net.minecraft.util.Uuids
 */
package dev.daamky;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import dev.daamky.mixin.client.MinecraftAccessor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.util.Uuids;

public final class Accounts {
    private static final Accounts INSTANCE = new Accounts();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String[] RANDOM_USERNAME_ADJECTIVES = new String[]{"Dark", "Silent", "Night", "Frost", "Toxic", "Shadow", "Crazy", "Lunar", "Ghost", "Swift", "Rapid", "Neon"};
    private static final String[] RANDOM_USERNAME_NOUNS = new String[]{"Wolf", "Fox", "Hunter", "Blade", "Rider", "Storm", "Ace", "Viper", "Pvp", "King", "Duck", "Raven"};
    private final Path accountsFile = FabricLoader.getInstance().getGameDir().resolve("daamky").resolve("accounts.json");
    private final List<AccountEntry> accounts = new ArrayList<AccountEntry>();
    private AccountEntry lastRemovedAccount;
    private boolean accountsLoaded;

    private Accounts() {
    }

    public static Accounts getInstance() {
        return INSTANCE;
    }

    public List<AccountEntry> getAccounts() {
        this.loadAccounts();
        ArrayList<AccountEntry> arrayList = new ArrayList<AccountEntry>(this.accounts);
        arrayList.sort(Comparator.comparing((AccountEntry account) -> !account.IllI1l1l1).thenComparingLong(account -> -account.l1I1IlllIlI));
        return arrayList;
    }

    public int getAccountCount() {
        this.loadAccounts();
        return this.accounts.size();
    }

    public boolean isValidUsername(String username) {
        return username != null && username.matches("[A-Za-z0-9_]{3,16}");
    }

    public AccountEntry getOrCreateAccount(String username) {
        String trimmedUsername;
        this.loadAccounts();
        String normalizedUsername = trimmedUsername = username == null ? "" : username.trim();
        if (!this.isValidUsername(trimmedUsername)) {
            return null;
        }
        for (AccountEntry existingAccount : this.accounts) {
            if (!existingAccount.II1Il11l111II11IIl.equalsIgnoreCase(trimmedUsername)) continue;
            return existingAccount;
        }
        AccountEntry accounts$II1Il11l111II11IIl = new AccountEntry(trimmedUsername, System.currentTimeMillis(), false);
        this.accounts.add(accounts$II1Il11l111II11IIl);
        this.saveAccounts();
        return accounts$II1Il11l111II11IIl;
    }

    public AccountEntry createRandomAccount() {
        String generatedUsername;
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        while ((generatedUsername = RANDOM_USERNAME_ADJECTIVES[threadLocalRandom.nextInt(RANDOM_USERNAME_ADJECTIVES.length)] + RANDOM_USERNAME_NOUNS[threadLocalRandom.nextInt(RANDOM_USERNAME_NOUNS.length)] + threadLocalRandom.nextInt(10, 1000)).length() > 16) {
        }
        return this.getOrCreateAccount(generatedUsername);
    }

    public void removeAccount(AccountEntry accounts$II1Il11l111II11IIl) {
        this.loadAccounts();
        if (this.accounts.remove(accounts$II1Il11l111II11IIl)) {
            this.lastRemovedAccount = accounts$II1Il11l111II11IIl;
            this.saveAccounts();
        }
    }

    public boolean restoreLastRemovedAccount() {
        this.loadAccounts();
        if (this.lastRemovedAccount == null) {
            return false;
        }
        this.accounts.add(this.lastRemovedAccount);
        this.lastRemovedAccount = null;
        this.saveAccounts();
        return true;
    }

    public void togglePinned(AccountEntry accounts$II1Il11l111II11IIl) {
        accounts$II1Il11l111II11IIl.IllI1l1l1 = !accounts$II1Il11l111II11IIl.IllI1l1l1;
        this.saveAccounts();
    }

    public boolean isCurrentAccount(AccountEntry accounts$II1Il11l111II11IIl) {
        return MinecraftClient.getInstance().getSession().getUsername().equalsIgnoreCase(accounts$II1Il11l111II11IIl.II1Il11l111II11IIl);
    }

    public void login(AccountEntry accounts$II1Il11l111II11IIl) {
        MinecraftClient client = MinecraftClient.getInstance();
        UUID uUID = Uuids.getOfflinePlayerUuid((String)accounts$II1Il11l111II11IIl.II1Il11l111II11IIl);
        MinecraftAccessor client2 = (MinecraftAccessor)client;
        client2.daamky$setUser(new Session(accounts$II1Il11l111II11IIl.II1Il11l111II11IIl, uUID, "", Optional.empty(), Optional.empty()));
        client2.daamky$setProfileFuture(CompletableFuture.completedFuture(new ProfileResult(new GameProfile(uUID, accounts$II1Il11l111II11IIl.II1Il11l111II11IIl))));
        accounts$II1Il11l111II11IIl.l1lll11l1l = System.currentTimeMillis();
        this.saveAccounts();
    }

    private void loadAccounts() {
        if (this.accountsLoaded) {
            return;
        }
        this.accountsLoaded = true;
        if (!Files.exists(this.accountsFile, new LinkOption[0])) {
            return;
        }
        try {
            JsonObject asJsonObject = JsonParser.parseString((String)Files.readString(this.accountsFile, StandardCharsets.UTF_8)).getAsJsonObject();
            for (JsonElement accountJsonElement : asJsonObject.getAsJsonArray("accounts")) {
                JsonObject asJsonObject2 = accountJsonElement.getAsJsonObject();
                AccountEntry accounts$II1Il11l111II11IIl = new AccountEntry(asJsonObject2.get("name").getAsString(), asJsonObject2.get("created").getAsLong(), asJsonObject2.has("pinned") && asJsonObject2.get("pinned").getAsBoolean());
                long l = accounts$II1Il11l111II11IIl.l1lll11l1l = asJsonObject2.has("lastLogin") ? asJsonObject2.get("lastLogin").getAsLong() : 0L;
                if (!this.isValidUsername(accounts$II1Il11l111II11IIl.II1Il11l111II11IIl)) continue;
                this.accounts.add(accounts$II1Il11l111II11IIl);
            }
        }
        catch (Exception asJsonObject) {
            // empty catch block
        }
    }

    private void saveAccounts() {
        JsonArray jsonArray = new JsonArray();
        for (AccountEntry accountEntry : this.accounts) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", accountEntry.II1Il11l111II11IIl);
            jsonObject.addProperty("created", (Number)accountEntry.l1I1IlllIlI);
            jsonObject.addProperty("pinned", Boolean.valueOf(accountEntry.IllI1l1l1));
            jsonObject.addProperty("lastLogin", (Number)accountEntry.l1lll11l1l);
            jsonArray.add((JsonElement)jsonObject);
        }
        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.add("accounts", (JsonElement)jsonArray);
        try {
            Files.createDirectories(this.accountsFile.getParent(), new FileAttribute[0]);
            Files.writeString(this.accountsFile, (CharSequence)GSON.toJson((JsonElement)jsonObject2), StandardCharsets.UTF_8, new OpenOption[0]);
        }
        catch (Exception writeFailure) {
            // empty catch block
        }
    }


    static final class AccountEntry {
        final String II1Il11l111II11IIl;
        final long l1I1IlllIlI;
        boolean IllI1l1l1;
        long l1lll11l1l;

        AccountEntry(String name, long created, boolean pinned) {
            this.II1Il11l111II11IIl = name;
            this.l1I1IlllIlI = created;
            this.IllI1l1l1 = pinned;
        }

        String username() { return this.II1Il11l111II11IIl; }
        long createdAt() { return this.l1I1IlllIlI; }
        boolean pinned() { return this.IllI1l1l1; }
        long lastLogin() { return this.l1lll11l1l; }
    }
}


