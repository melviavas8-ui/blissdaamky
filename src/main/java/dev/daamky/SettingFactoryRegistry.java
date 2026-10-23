package dev.daamky;

import java.util.concurrent.CopyOnWriteArrayList;

public final class SettingFactoryRegistry {
    private static final String pe3c614a3 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final CopyOnWriteArrayList<BotSessionProfile> II1Il11l111II11IIl = new CopyOnWriteArrayList<>();
   private static final BotSessionProfile l1I1IlllIlI = new DefaultBotSessionProfile();
// https://t.me/pasterplace

   private SettingFactoryRegistry() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(BotSessionProfile l1lll11l1l) {
      II1Il11l111II11IIl.addIfAbsent(l1lll11l1l);
   }
// https://t.me/pasterplace

   public static BotSessionProfile II1Il11l111II11IIl(String serverAddress) {
      for (Object profile : II1Il11l111II11IIl) {
         if (((BotSessionProfile)profile).supportsServer(serverAddress)) {
            return (BotSessionProfile)profile;
         }
      }
// https://t.me/pasterplace

      return l1I1IlllIlI;
   }
// https://t.me/pasterplace

   static {
      II1Il11l111II11IIl(new Skytime());
   }
}

