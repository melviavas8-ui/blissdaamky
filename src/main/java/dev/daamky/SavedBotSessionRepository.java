package dev.daamky;

import java.util.List;

public interface SavedBotSessionRepository {
   List<SavedBotSession> getAllSessions();

   List<SavedBotSession> getOnlineSessions();

   SavedBotSession findSession(String username);

   boolean addOfflineSession(String username);

   boolean removeSession(String username);

   SavedBotSession createSession(String username, String serverAddress, int serverPort);
}
