package dev.daamky;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

public final class Viafabricplus {
    private static final String p38c8f0eb = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final boolean II1Il11l111II11IIl = FabricLoader.getInstance().isModLoaded("viafabricplus");
// https://t.me/pasterplace

   private Viafabricplus() {
   }
// https://t.me/pasterplace

   public static boolean II1Il11l111II11IIl() {
      if (!II1Il11l111II11IIl) {
         return true;
      }
// https://t.me/pasterplace

      try {
         Class<?> api = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus");
         Object implementation = api.getMethod("getImpl").invoke(null);
         Object targetVersion = implementation.getClass().getMethod("getTargetVersion").invoke(implementation);
         Object version = targetVersion.getClass().getMethod("getVersion").invoke(targetVersion);
         return ((Number)version).intValue() >= SharedConstants.getProtocolVersion();
      } catch (Throwable versionLookupFailure) {
         return true;
      }
   }
// https://t.me/pasterplace

   public static boolean l1I1IlllIlI() {
      return II1Il11l111II11IIl();
   }
// https://t.me/pasterplace

}

