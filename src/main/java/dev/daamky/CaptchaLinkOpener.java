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
   private static final CaptchaLinkOpener II1Il11l111II11IIl = new CaptchaLinkOpener();
   private static final Pattern l1I1IlllIlI = Pattern.compile("https?://captcha\\.[^\\s]+");
   private static final Set<String> IllI1l1l1 = ConcurrentHashMap.newKeySet();

   private CaptchaLinkOpener() {
   }

   public static CaptchaLinkOpener II1Il11l111II11IIl() {
      return II1Il11l111II11IIl;
   }

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

