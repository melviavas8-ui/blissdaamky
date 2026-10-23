package dev.daamky;

import net.minecraft.network.packet.Packet;

public interface BotPacketListenerProxy {
   Packet<?> filterOutgoingPacket(Packet<?> packet);
// https://t.me/pasterplace

   void onOutgoingPacketSent(Packet<?> packet);
}
