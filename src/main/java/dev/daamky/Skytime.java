package dev.daamky;

import java.util.Locale;

public class Skytime implements BotSessionProfile {
   private final ProxyServerBridge II1Il11l111II11IIl = new ProxyMenuButton();
   private final BotResourcePackHandler l1I1IlllIlI = new DelayedResourcePackHandler();
   private final BotTransferIgnored IllI1l1l1 = new BotTransferIgnoredImpl();

   @Override
   public String id() {
      return "funtime";
   }

   @Override
   public boolean supportsServer(String serverAddress) {
      if (serverAddress == null) {
         return false;
      }

      String normalizedServerAddress = serverAddress.toLowerCase(Locale.ROOT);
      return normalizedServerAddress.contains("funtime") || normalizedServerAddress.contains("skytime") || normalizedServerAddress.contains("space-times") || normalizedServerAddress.contains("funsky") || normalizedServerAddress.contains("holyworld");
   }

   @Override
   public BotSessionCallback callbackFor(BotSession disconnect) {
      return new Captcha(disconnect);
   }

   @Override
   public BotResourcePackHandler resourcePackHandler() {
      return this.l1I1IlllIlI;
   }

   @Override
   public ProxyServerBridge proxyServerBridge() {
      return this.II1Il11l111II11IIl;
   }

   @Override
   public BotTransferIgnored transferIgnoredHandler() {
      return this.IllI1l1l1;
   }
}

