package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

public final class PacketSender {
   private static final ThreadLocal<Boolean> II1Il11l111II11IIl = ThreadLocal.withInitial(() -> false);

   private PacketSender() {
   }

   public static void II1Il11l111II11IIl(Packet<?> packet) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.getNetworkHandler() != null) {
         client.getNetworkHandler().sendPacket(packet);
      }
   }

   public static void l1I1IlllIlI(Packet<?> packet) {
      II1Il11l111II11IIl.set(true);

      try {
         II1Il11l111II11IIl(packet);
      } finally {
         II1Il11l111II11IIl.set(false);
      }
   }

   public static boolean II1Il11l111II11IIl() {
      return II1Il11l111II11IIl.get();
   }
}

