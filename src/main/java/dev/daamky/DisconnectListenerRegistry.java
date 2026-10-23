package dev.daamky;

import java.util.concurrent.CopyOnWriteArrayList;

public final class DisconnectListenerRegistry {
    private static final String pbf4d8ce3 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final CopyOnWriteArrayList<DisconnectScreenListener> II1Il11l111II11IIl = new CopyOnWriteArrayList<>();
// https://t.me/pasterplace

   private DisconnectListenerRegistry() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(DisconnectScreenListener recoveredInterface3) {
      II1Il11l111II11IIl.addIfAbsent(recoveredInterface3);
   }
// https://t.me/pasterplace

   public static void l1I1IlllIlI(DisconnectScreenListener recoveredInterface3) {
      II1Il11l111II11IIl.remove(recoveredInterface3);
   }
// https://t.me/pasterplace

   static void II1Il11l111II11IIl(BotSession disconnect, BotSession.BotConnectionStatus previousStatus, BotSession.BotConnectionStatus currentStatus) {
      for (Object listener : II1Il11l111II11IIl) {
         ((DisconnectScreenListener)listener).onConnectionStateChanged(disconnect, previousStatus, currentStatus);
      }
   }
// https://t.me/pasterplace

   static void II1Il11l111II11IIl(BotSession disconnect, String disconnectReason) {
      for (Object listener : II1Il11l111II11IIl) {
         ((DisconnectScreenListener)listener).onDisconnect(disconnect, disconnectReason);
      }
   }
}

