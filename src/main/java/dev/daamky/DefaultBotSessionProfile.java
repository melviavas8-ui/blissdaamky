package dev.daamky;

public class DefaultBotSessionProfile implements BotSessionProfile {
   private final ProxyServerBridge proxyMenuButton = new ProxyMenuButton();
   private final BotResourcePackHandler resourcePackHandler = new ImmediateResourcePackHandler();
   private final BotTransferIgnored transferIgnoredHandler = new BotTransferIgnoredImpl();

   @Override
   public String id() {
      return "default";
   }

   @Override
   public boolean supportsServer(String serverAddress) {
      return true;
   }

   @Override
   public BotSessionCallback callbackFor(BotSession disconnect) {
      return BotSessionCallback.NO_OP;
   }

   @Override
   public BotResourcePackHandler resourcePackHandler() {
      return this.resourcePackHandler;
   }

   @Override
   public ProxyServerBridge proxyServerBridge() {
      return this.proxyMenuButton;
   }

   @Override
   public BotTransferIgnored transferIgnoredHandler() {
      return this.transferIgnoredHandler;
   }
}

