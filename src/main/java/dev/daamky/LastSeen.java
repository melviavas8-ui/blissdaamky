package dev.daamky;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.util.Uuids;

public final class LastSeen {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public final String userId;
   public final String username;
   public final String bio;
   public final boolean isOnline;
   public final long lastSeenAt;
   public final long accountCreatedAt;
   public final String displayNickname;
   public final String minecraftUuid;
   public final String serverName;
   public final JsonObject cosmeticsData;
   public final int unreadCount;
   public final long peerReadTimestamp;
   public final Chat lastMessage;
   private GameProfile cachedGameProfile;
// https://t.me/pasterplace

   private LastSeen(JsonObject jsonObject) {
      this.userId = getString(jsonObject, "uid");
      this.username = getString(jsonObject, "name");
      this.bio = getString(jsonObject, "bio");
      this.isOnline = jsonObject.has("online") && jsonObject.get("online").getAsBoolean();
      this.lastSeenAt = getLong(jsonObject, "lastSeen");
      this.accountCreatedAt = getLong(jsonObject, "createdAt");
      this.displayNickname = getString(jsonObject, "nick");
      this.minecraftUuid = getString(jsonObject, "mcUuid");
      this.serverName = getString(jsonObject, "server");
      this.cosmeticsData = jsonObject.has("cosmetics") && jsonObject.get("cosmetics").isJsonObject()
         ? jsonObject.getAsJsonObject("cosmetics")
         : new JsonObject();
      this.unreadCount = (int)getLong(jsonObject, "unread");
      this.peerReadTimestamp = getLong(jsonObject, "peerRead");
      this.lastMessage = jsonObject.has("last") && jsonObject.get("last").isJsonObject()
         ? Chat.fromJson(jsonObject.getAsJsonObject("last"))
         : null;
   }
// https://t.me/pasterplace

   public static LastSeen fromJson(JsonObject jsonObject) {
      return new LastSeen(jsonObject);
   }
// https://t.me/pasterplace

   public String getFormattedUserId() {
      return this.userId.length() == 8
         ? this.userId.substring(0, 4) + "-" + this.userId.substring(4)
         : this.userId;
   }
// https://t.me/pasterplace

   public GameProfile getGameProfile() {
      if (this.cachedGameProfile == null) {
         String profileName = this.displayNickname.isEmpty() ? this.username : this.displayNickname;

         UUID profileUuid;
         try {
            profileUuid = this.minecraftUuid.isEmpty() ? Uuids.getOfflinePlayerUuid(profileName) : UUID.fromString(this.minecraftUuid);
         } catch (IllegalArgumentException invalidUuidFailure) {
            profileUuid = Uuids.getOfflinePlayerUuid(profileName);
         }

         this.cachedGameProfile = new GameProfile(profileUuid, profileName);
      }
// https://t.me/pasterplace

      return this.cachedGameProfile;
   }
// https://t.me/pasterplace

   static String getString(JsonObject jsonObject, String propertyName) {
      JsonElement jsonElement = jsonObject.get(propertyName);
      return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : "";
   }
// https://t.me/pasterplace

   static long getLong(JsonObject jsonObject, String propertyName) {
      JsonElement jsonElement = jsonObject.get(propertyName);
      return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsLong() : 0L;
   }
}

