package dev.daamky;

import java.util.concurrent.CopyOnWriteArrayList;

public final class SettingFactoryRegistry {
   private static final CopyOnWriteArrayList<BotSessionProfile> II1Il11l111II11IIl = new CopyOnWriteArrayList<>();
   private static final BotSessionProfile l1I1IlllIlI = new DefaultBotSessionProfile();

   private SettingFactoryRegistry() {
   }

   public static void II1Il11l111II11IIl(BotSessionProfile l1lll11l1l) {
      II1Il11l111II11IIl.addIfAbsent(l1lll11l1l);
   }

   public static BotSessionProfile II1Il11l111II11IIl(String serverAddress) {
      for (Object profile : II1Il11l111II11IIl) {
         if (((BotSessionProfile)profile).supportsServer(serverAddress)) {
            return (BotSessionProfile)profile;
         }
      }

      return l1I1IlllIlI;
   }

   static {
      II1Il11l111II11IIl(new Skytime());
   }
}

