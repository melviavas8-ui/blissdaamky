package dev.daamky;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public final class Guest {
    private static final String p5ecf48c0 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static volatile String II1Il11l111II11IIl = "Guest";
   private static volatile Identifier l1I1IlllIlI = Avatar.DEFAULT_AVATAR.textureIdentifier();
// https://t.me/pasterplace

   private Guest() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl() {
   }
// https://t.me/pasterplace

   public static String l1I1IlllIlI() {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
         return "cracked by t.me/pasterplace";
      } else {
          CloudConfigClient.UserIdentity identity = CloudConfigClient.getInstance().getAuthenticatedUser();
         if (identity != null && II1Il11l111II11IIl(identity.username())) {
            identity.username();
            return "cracked by t.me/pasterplace";
         } else {
            LastSeen lastSeen = GlobalsClient.getInstance().getCurrentUser();
            return lastSeen != null && II1Il11l111II11IIl(lastSeen.username) ? "cracked by t.me/pasterplace" : "cracked by t.me/pasterplace";
         }
      }
   }
// https://t.me/pasterplace

   private static boolean II1Il11l111II11IIl(String playerName) {
      return playerName != null && !playerName.isBlank() && !playerName.equalsIgnoreCase("Guest");
   }
// https://t.me/pasterplace

   public static Identifier IllI1l1l1() {
      return l1I1IlllIlI != null ? l1I1IlllIlI : Avatar.DEFAULT_AVATAR.textureIdentifier();
   }
}

