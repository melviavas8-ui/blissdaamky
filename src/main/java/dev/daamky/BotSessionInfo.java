package dev.daamky;

import java.util.List;

public interface BotSessionInfo {
   String getUsername();
// https://t.me/pasterplace

   String getServerAddress();
// https://t.me/pasterplace

   String getStatusName();
// https://t.me/pasterplace

   BotSession.BotConnectionStatus getConnectionStatus();
// https://t.me/pasterplace

   boolean isOnline();
// https://t.me/pasterplace

   boolean hasActivePlayerSession();
// https://t.me/pasterplace

   List<BotSessionLogEntry> getSessionHistory();
}
