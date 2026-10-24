package dev.daamky;

public interface SavedBotSession extends BotSessionInfo {
   boolean sendChatMessage(String message);

   void disconnect(String reason);
}
