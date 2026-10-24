package dev.daamky;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;

public final class Viafabricplus {
   private static final boolean II1Il11l111II11IIl = FabricLoader.getInstance().isModLoaded("viafabricplus");

   private Viafabricplus() {
   }

   public static boolean II1Il11l111II11IIl() {
      if (!II1Il11l111II11IIl) {
         return true;
      }

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

   public static boolean l1I1IlllIlI() {
      return II1Il11l111II11IIl();
   }

}

