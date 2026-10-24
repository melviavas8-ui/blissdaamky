package dev.daamky;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class FriendManager {
   private static final FriendManager INSTANCE = new FriendManager();
   private final Set<String> friendNames = new LinkedHashSet<>();

   private FriendManager() {
   }

   public static FriendManager getInstance() {
      return INSTANCE;
   }

   public boolean isFriend(String username) {
      return username != null && (this.friendNames.contains(username.toLowerCase(Locale.ROOT)) || GlobalsClient.getInstance().isFriend(username));
   }

   public boolean isFriend(Entity entity) {
      return entity instanceof PlayerEntity player && this.isFriend(player.getGameProfile().name());
   }

   public void addFriend(String username) {
      this.friendNames.add(username.toLowerCase(Locale.ROOT));
   }

   public void removeFriend(String username) {
      this.friendNames.remove(username.toLowerCase(Locale.ROOT));
   }

   public boolean toggleFriend(String username) {
      if (this.isFriend(username)) {
         this.removeFriend(username);
         return false;
      } else {
         this.addFriend(username);
         return true;
      }
   }

   public Set<String> getFriendNames() {
      return Collections.unmodifiableSet(this.friendNames);
   }

   public void clearFriends() {
      this.friendNames.clear();
   }
}

