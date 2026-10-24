package dev.daamky;

import java.util.List;

public interface BotSessionInfo {
   String getUsername();

   String getServerAddress();

   String getStatusName();

   BotSession.BotConnectionStatus getConnectionStatus();

   boolean isOnline();

   boolean hasActivePlayerSession();

   List<BotSessionLogEntry> getSessionHistory();
}
