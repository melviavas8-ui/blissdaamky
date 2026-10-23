package dev.daamky;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.text.Text;

public interface BotTransferIgnored {
   default void II1Il11l111II11IIl(ServerTransferS2CPacket serverTransferS2CPacket, ClientConnection clientConnection) {
      clientConnection.disconnect(Text.literal("Bot transfer ignored"));
   }
}
