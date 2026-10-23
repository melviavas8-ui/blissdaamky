package dev.daamky;

public interface BotSessionProfile {
   String id();
// https://t.me/pasterplace

   boolean supportsServer(String serverAddress);
// https://t.me/pasterplace

   BotSessionCallback callbackFor(BotSession disconnect);
// https://t.me/pasterplace

   BotResourcePackHandler resourcePackHandler();
// https://t.me/pasterplace

   ProxyServerBridge proxyServerBridge();
// https://t.me/pasterplace

   BotTransferIgnored transferIgnoredHandler();
}
