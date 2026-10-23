package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

public final class PacketSender {
    private static final String p6ce9d8e1 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final ThreadLocal<Boolean> II1Il11l111II11IIl = ThreadLocal.withInitial(() -> false);
// https://t.me/pasterplace

   private PacketSender() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(Packet<?> packet) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.getNetworkHandler() != null) {
         client.getNetworkHandler().sendPacket(packet);
      }
   }
// https://t.me/pasterplace

   public static void l1I1IlllIlI(Packet<?> packet) {
      II1Il11l111II11IIl.set(true);
// https://t.me/pasterplace

      try {
         II1Il11l111II11IIl(packet);
      } finally {
         II1Il11l111II11IIl.set(false);
      }
   }
// https://t.me/pasterplace

   public static boolean II1Il11l111II11IIl() {
      return II1Il11l111II11IIl.get();
   }
}

