package dev.daamky;

import java.util.concurrent.CopyOnWriteArrayList;

public final class DisconnectListenerRegistry {
   private static final CopyOnWriteArrayList<DisconnectScreenListener> II1Il11l111II11IIl = new CopyOnWriteArrayList<>();

   private DisconnectListenerRegistry() {
   }

   public static void II1Il11l111II11IIl(DisconnectScreenListener recoveredInterface3) {
      II1Il11l111II11IIl.addIfAbsent(recoveredInterface3);
   }

   public static void l1I1IlllIlI(DisconnectScreenListener recoveredInterface3) {
      II1Il11l111II11IIl.remove(recoveredInterface3);
   }

   static void II1Il11l111II11IIl(BotSession disconnect, BotSession.BotConnectionStatus previousStatus, BotSession.BotConnectionStatus currentStatus) {
      for (Object listener : II1Il11l111II11IIl) {
         ((DisconnectScreenListener)listener).onConnectionStateChanged(disconnect, previousStatus, currentStatus);
      }
   }

   static void II1Il11l111II11IIl(BotSession disconnect, String disconnectReason) {
      for (Object listener : II1Il11l111II11IIl) {
         ((DisconnectScreenListener)listener).onDisconnect(disconnect, disconnectReason);
      }
   }
}

