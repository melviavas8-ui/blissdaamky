package dev.daamky;

public interface SavedBotServiceAccess {
   boolean switchToSession(String username);
// https://t.me/pasterplace

   void restorePreviousSession();
// https://t.me/pasterplace

   boolean isActiveSessionReady();
// https://t.me/pasterplace

   BotSessionInfo getActiveSession();
// https://t.me/pasterplace

   float getSwitchNoticeAlpha();
}
