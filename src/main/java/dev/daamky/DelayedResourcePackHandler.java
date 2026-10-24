package dev.daamky;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public class DelayedResourcePackHandler implements BotResourcePackHandler {
   @Override
   public void handleResourcePackOffer(
      ResourcePackSendS2CPacket resourcePackSendS2CPacket, ClientConnection clientConnection, ScheduledExecutorService scheduledExecutorService
   ) {
      UUID uUID = resourcePackSendS2CPacket.id();
      clientConnection.send(new ResourcePackStatusC2SPacket(uUID, Status.ACCEPTED));
      long downloadDelayMillis = 600L + ThreadLocalRandom.current().nextLong(900L);
      long loadedStatusDelayMillis = downloadDelayMillis + 300L + ThreadLocalRandom.current().nextLong(600L);
      scheduledExecutorService.schedule(() -> sendStatusIfConnected(clientConnection, uUID, Status.DOWNLOADED), downloadDelayMillis, TimeUnit.MILLISECONDS);
      scheduledExecutorService.schedule(() -> sendStatusIfConnected(clientConnection, uUID, Status.SUCCESSFULLY_LOADED), loadedStatusDelayMillis, TimeUnit.MILLISECONDS);
   }

   private static void sendStatusIfConnected(ClientConnection clientConnection, UUID uUID, Status status) {
      if (clientConnection.isOpen()) {
         clientConnection.send(new ResourcePackStatusC2SPacket(uUID, status));
      }
   }
}

