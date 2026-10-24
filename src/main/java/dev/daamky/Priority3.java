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
   private static final Priority3 INSTANCE = new Priority3();
   private final Deque<Packet<?>> queuedPackets = new ArrayDeque<>();

   private Priority3() {
   }

   public static Priority3 getInstance() {
      return INSTANCE;
   }

   public void queuePackets(Collection<Packet<?>> items) {
      this.queuedPackets.addAll(items);
   }

   public boolean isQueueEmpty() {
      return this.queuedPackets.isEmpty();
   }

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

   @EventTarget
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.queuedPackets.clear();
   }
}

