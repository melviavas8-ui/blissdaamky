package dev.daamky;

public interface DisconnectScreenListener {
   default void onConnectionStateChanged(BotSession disconnect, BotSession.BotConnectionStatus previousStatus, BotSession.BotConnectionStatus currentStatus) {
   }

   default void onDisconnect(BotSession disconnect, String reason) {
   }
}
