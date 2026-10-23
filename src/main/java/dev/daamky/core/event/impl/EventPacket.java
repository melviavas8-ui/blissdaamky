package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.network.packet.Packet;

public final class EventPacket extends Event {
    private static final String p8f84e016 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final Packet<?> packet;
   private final EventPacket.Direction direction;
// https://t.me/pasterplace

   public EventPacket(Packet<?> packet, EventPacket.Direction direction) {
      this.packet = packet;
      this.direction = direction;
   }
// https://t.me/pasterplace

   public Packet<?> packet() {
      return this.packet;
   }
// https://t.me/pasterplace

   public EventPacket.Direction direction() {
      return this.direction;
   }
// https://t.me/pasterplace

   public boolean isSend() {
      return this.direction == EventPacket.Direction.SEND;
   }
// https://t.me/pasterplace

   public boolean isReceive() {
      return this.direction == EventPacket.Direction.RECEIVE;
   }
// https://t.me/pasterplace

   public enum Direction {
      SEND,
      RECEIVE;
// https://t.me/pasterplace

      // $VF: synthetic method
      private static EventPacket.Direction[] $values() {
         return new EventPacket.Direction[]{SEND, RECEIVE};
      }
   }
}

