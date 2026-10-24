package dev.daamky;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.util.Uuids;

public final class LastSeen {
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

   public static LastSeen fromJson(JsonObject jsonObject) {
      return new LastSeen(jsonObject);
   }

   public String getFormattedUserId() {
      return this.userId.length() == 8
         ? this.userId.substring(0, 4) + "-" + this.userId.substring(4)
         : this.userId;
   }

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

      return this.cachedGameProfile;
   }

   static String getString(JsonObject jsonObject, String propertyName) {
      JsonElement jsonElement = jsonObject.get(propertyName);
      return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : "";
   }

   static long getLong(JsonObject jsonObject, String propertyName) {
      JsonElement jsonElement = jsonObject.get(propertyName);
      return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsLong() : 0L;
   }
}

