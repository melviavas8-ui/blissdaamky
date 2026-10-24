package dev.daamky;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public final class Guest {
   private static volatile String II1Il11l111II11IIl = "Guest";
   private static volatile Identifier l1I1IlllIlI = Avatar.DEFAULT_AVATAR.textureIdentifier();

   private Guest() {
   }

   public static void II1Il11l111II11IIl() {
   }

   public static String l1I1IlllIlI() {
      if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
         return II1Il11l111II11IIl;
      }

      CloudConfigClient.UserIdentity identity = CloudConfigClient.getInstance().getAuthenticatedUser();
      if (identity != null && II1Il11l111II11IIl(identity.username())) {
         return identity.username();
      }

      LastSeen lastSeen = GlobalsClient.getInstance().getCurrentUser();
      return lastSeen != null && II1Il11l111II11IIl(lastSeen.username) ? lastSeen.username : II1Il11l111II11IIl;
   }

   private static boolean II1Il11l111II11IIl(String playerName) {
      return playerName != null && !playerName.isBlank() && !playerName.equalsIgnoreCase("Guest");
   }

   public static Identifier IllI1l1l1() {
      return l1I1IlllIlI != null ? l1I1IlllIlI : Avatar.DEFAULT_AVATAR.textureIdentifier();
   }
}

