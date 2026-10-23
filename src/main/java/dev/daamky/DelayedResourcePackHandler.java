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
    private static final String pb9892026 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
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
// https://t.me/pasterplace

   private static void sendStatusIfConnected(ClientConnection clientConnection, UUID uUID, Status status) {
      if (clientConnection.isOpen()) {
         clientConnection.send(new ResourcePackStatusC2SPacket(uUID, status));
      }
   }
}

