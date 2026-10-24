package dev.daamky;

public interface BotSessionProfile {
   String id();

   boolean supportsServer(String serverAddress);

   BotSessionCallback callbackFor(BotSession disconnect);

   BotResourcePackHandler resourcePackHandler();

   ProxyServerBridge proxyServerBridge();

   BotTransferIgnored transferIgnoredHandler();
}
