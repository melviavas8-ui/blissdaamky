package dev.daamky;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public class ImmediateResourcePackHandler implements BotResourcePackHandler {
    private static final String p1c71de98 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Override
   public void handleResourcePackOffer(
      ResourcePackSendS2CPacket resourcePackSendS2CPacket, ClientConnection clientConnection, ScheduledExecutorService scheduledExecutorService
   ) {
      UUID uUID = resourcePackSendS2CPacket.id();
      if (clientConnection.isOpen()) {
         clientConnection.send(new ResourcePackStatusC2SPacket(uUID, Status.ACCEPTED));
         clientConnection.send(new ResourcePackStatusC2SPacket(uUID, Status.DOWNLOADED));
         clientConnection.send(new ResourcePackStatusC2SPacket(uUID, Status.SUCCESSFULLY_LOADED));
      }
   }
}

