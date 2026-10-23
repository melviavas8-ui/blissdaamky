package dev.daamky;

public interface SavedBotSession extends BotSessionInfo {
   boolean sendChatMessage(String message);
// https://t.me/pasterplace

   void disconnect(String reason);
}
