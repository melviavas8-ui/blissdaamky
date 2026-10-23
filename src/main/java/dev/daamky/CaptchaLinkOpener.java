package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Util;

public final class CaptchaLinkOpener {
    private static final String pe3dc9ddb = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final CaptchaLinkOpener II1Il11l111II11IIl = new CaptchaLinkOpener();
   private static final Pattern l1I1IlllIlI = Pattern.compile("https?://captcha\\.[^\\s]+");
   private static final Set<String> IllI1l1l1 = ConcurrentHashMap.newKeySet();
// https://t.me/pasterplace

   private CaptchaLinkOpener() {
   }
// https://t.me/pasterplace

   public static CaptchaLinkOpener II1Il11l111II11IIl() {
      return II1Il11l111II11IIl;
   }
// https://t.me/pasterplace

   @EventTarget
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof GameMessageS2CPacket) {
             GameMessageS2CPacket packet2 = (GameMessageS2CPacket)packet;
             II1Il11l111II11IIl(packet2.content().getString(), null);
         }
      }
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(String chatMessage, String serverName) {
      if (chatMessage != null) {
         Matcher matcher = l1I1IlllIlI.matcher(chatMessage.replace("\n", ""));
         if (matcher.find()) {
            String captchaUrl = matcher.group();
            if (IllI1l1l1.add(captchaUrl)) {
               MinecraftClient.getInstance().execute(() -> {
                  ClientChatLogger.II1Il11l111II11IIl("Капча" + (serverName == null ? "" : " для " + serverName) + ", открываю в браузере: " + captchaUrl);
                  Util.getOperatingSystem().open(captchaUrl);
               });
            }
         }
      }
   }
}

