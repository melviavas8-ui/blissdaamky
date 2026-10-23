package dev.daamky;

import java.util.List;

public interface SavedBotSessionRepository {
   List<SavedBotSession> getAllSessions();
// https://t.me/pasterplace

   List<SavedBotSession> getOnlineSessions();
// https://t.me/pasterplace

   SavedBotSession findSession(String username);
// https://t.me/pasterplace

   boolean addOfflineSession(String username);
// https://t.me/pasterplace

   boolean removeSession(String username);
// https://t.me/pasterplace

   SavedBotSession createSession(String username, String serverAddress, int serverPort);
}
