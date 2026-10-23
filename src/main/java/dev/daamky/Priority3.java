package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public final class Priority3 {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final Priority3 INSTANCE = new Priority3();
   private final Deque<Packet<?>> queuedPackets = new ArrayDeque<>();
// https://t.me/pasterplace

   private Priority3() {
   }
// https://t.me/pasterplace

   public static Priority3 getInstance() {
      return INSTANCE;
   }
// https://t.me/pasterplace

   public void queuePackets(Collection<Packet<?>> items) {
      this.queuedPackets.addAll(items);
   }
// https://t.me/pasterplace

   public boolean isQueueEmpty() {
      return this.queuedPackets.isEmpty();
   }
// https://t.me/pasterplace

   @EventTarget(priority = 4)
   public void onPacket(EventPacket eventPacket) {
      if (!this.queuedPackets.isEmpty() && eventPacket.isSend() && !eventPacket.isCancelled()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof PlayerMoveC2SPacket || packet instanceof ClientCommandC2SPacket || packet instanceof PlayerInputC2SPacket) {
            eventPacket.cancel();
            this.queuedPackets.addLast(packet);
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      while (!this.queuedPackets.isEmpty()) {
         Object packet = this.queuedPackets.pollFirst();
         PacketSender.l1I1IlllIlI((Packet<?>)packet);
         if (packet instanceof PlayerMoveC2SPacket) {
             PlayerMoveC2SPacket movementPacket = (PlayerMoveC2SPacket)packet;
            if (movementPacket.changesPosition()) {
               return;
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.queuedPackets.clear();
   }
}

