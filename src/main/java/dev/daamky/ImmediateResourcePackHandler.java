package dev.daamky;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public class ImmediateResourcePackHandler implements BotResourcePackHandler {
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

