package dev.daamky;

import java.util.concurrent.ScheduledExecutorService;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public interface BotResourcePackHandler {
   void handleResourcePackOffer(
      ResourcePackSendS2CPacket resourcePackSendS2CPacket, ClientConnection clientConnection, ScheduledExecutorService scheduledExecutorService
   );
}
