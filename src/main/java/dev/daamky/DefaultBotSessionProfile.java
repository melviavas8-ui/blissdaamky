package dev.daamky;

public class DefaultBotSessionProfile implements BotSessionProfile {
    private static final String pb96ae98 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final ProxyServerBridge proxyMenuButton = new ProxyMenuButton();
   private final BotResourcePackHandler resourcePackHandler = new ImmediateResourcePackHandler();
   private final BotTransferIgnored transferIgnoredHandler = new BotTransferIgnoredImpl();
// https://t.me/pasterplace

   @Override
   public String id() {
      return "default";
   }
// https://t.me/pasterplace

   @Override
   public boolean supportsServer(String serverAddress) {
      return true;
   }
// https://t.me/pasterplace

   @Override
   public BotSessionCallback callbackFor(BotSession disconnect) {
      return BotSessionCallback.NO_OP;
   }
// https://t.me/pasterplace

   @Override
   public BotResourcePackHandler resourcePackHandler() {
      return this.resourcePackHandler;
   }
// https://t.me/pasterplace

   @Override
   public ProxyServerBridge proxyServerBridge() {
      return this.proxyMenuButton;
   }
// https://t.me/pasterplace

   @Override
   public BotTransferIgnored transferIgnoredHandler() {
      return this.transferIgnoredHandler;
   }
}

