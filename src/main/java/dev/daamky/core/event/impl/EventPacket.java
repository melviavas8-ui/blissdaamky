package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.network.packet.Packet;

public final class EventPacket extends Event {
   private final Packet<?> packet;
   private final EventPacket.Direction direction;

   public EventPacket(Packet<?> packet, EventPacket.Direction direction) {
      this.packet = packet;
      this.direction = direction;
   }

   public Packet<?> packet() {
      return this.packet;
   }

   public EventPacket.Direction direction() {
      return this.direction;
   }

   public boolean isSend() {
      return this.direction == EventPacket.Direction.SEND;
   }

   public boolean isReceive() {
      return this.direction == EventPacket.Direction.RECEIVE;
   }

   public enum Direction {
      SEND,
      RECEIVE;

      // $VF: synthetic method
      private static EventPacket.Direction[] $values() {
         return new EventPacket.Direction[]{SEND, RECEIVE};
      }
   }
}

