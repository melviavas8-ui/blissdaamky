package dev.daamky;

public record BotSessionLogEntry(String serverName, String message) {

   public BotSessionLogEntry(String serverName, String message) {
      this.serverName = serverName;
      this.message = message;
   }
}

