package dev.daamky;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class FriendManager {
    private static final String p82f59c53 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final FriendManager INSTANCE = new FriendManager();
   private final Set<String> friendNames = new LinkedHashSet<>();
// https://t.me/pasterplace

   private FriendManager() {
   }
// https://t.me/pasterplace

   public static FriendManager getInstance() {
      return INSTANCE;
   }
// https://t.me/pasterplace

   public boolean isFriend(String username) {
      return username != null && (this.friendNames.contains(username.toLowerCase(Locale.ROOT)) || GlobalsClient.getInstance().isFriend(username));
   }
// https://t.me/pasterplace

   public boolean isFriend(Entity entity) {
      return entity instanceof PlayerEntity player && this.isFriend(player.getGameProfile().name());
   }
// https://t.me/pasterplace

   public void addFriend(String username) {
      this.friendNames.add(username.toLowerCase(Locale.ROOT));
   }
// https://t.me/pasterplace

   public void removeFriend(String username) {
      this.friendNames.remove(username.toLowerCase(Locale.ROOT));
   }
// https://t.me/pasterplace

   public boolean toggleFriend(String username) {
      if (this.isFriend(username)) {
         this.removeFriend(username);
         return false;
      } else {
         this.addFriend(username);
         return true;
      }
   }
// https://t.me/pasterplace

   public Set<String> getFriendNames() {
      return Collections.unmodifiableSet(this.friendNames);
   }
// https://t.me/pasterplace

   public void clearFriends() {
      this.friendNames.clear();
   }
}

