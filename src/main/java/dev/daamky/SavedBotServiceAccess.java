package dev.daamky;

public interface SavedBotServiceAccess {
   boolean switchToSession(String username);

   void restorePreviousSession();

   boolean isActiveSessionReady();

   BotSessionInfo getActiveSession();

   float getSwitchNoticeAlpha();
}
