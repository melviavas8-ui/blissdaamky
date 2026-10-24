package dev.daamky;

import net.minecraft.network.packet.Packet;

public interface BotPacketListenerProxy {
   Packet<?> filterOutgoingPacket(Packet<?> packet);

   void onOutgoingPacketSent(Packet<?> packet);
}
