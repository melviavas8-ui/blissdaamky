package dev.daamky;

import java.util.Locale;

public class Skytime implements BotSessionProfile {
    private static final String p978df5ea = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final ProxyServerBridge II1Il11l111II11IIl = new ProxyMenuButton();
   private final BotResourcePackHandler l1I1IlllIlI = new DelayedResourcePackHandler();
   private final BotTransferIgnored IllI1l1l1 = new BotTransferIgnoredImpl();
// https://t.me/pasterplace

   @Override
   public String id() {
      return "funtime";
   }
// https://t.me/pasterplace

   @Override
   public boolean supportsServer(String serverAddress) {
      if (serverAddress == null) {
         return false;
      }
// https://t.me/pasterplace

      String normalizedServerAddress = serverAddress.toLowerCase(Locale.ROOT);
      return normalizedServerAddress.contains("funtime") || normalizedServerAddress.contains("skytime") || normalizedServerAddress.contains("space-times") || normalizedServerAddress.contains("funsky") || normalizedServerAddress.contains("holyworld");
   }
// https://t.me/pasterplace

   @Override
   public BotSessionCallback callbackFor(BotSession disconnect) {
      return new Captcha(disconnect);
   }
// https://t.me/pasterplace

   @Override
   public BotResourcePackHandler resourcePackHandler() {
      return this.l1I1IlllIlI;
   }
// https://t.me/pasterplace

   @Override
   public ProxyServerBridge proxyServerBridge() {
      return this.II1Il11l111II11IIl;
   }
// https://t.me/pasterplace

   @Override
   public BotTransferIgnored transferIgnoredHandler() {
      return this.IllI1l1l1;
   }
}

