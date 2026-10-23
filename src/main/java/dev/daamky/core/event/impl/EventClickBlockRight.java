package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public final class EventClickBlockRight extends Event {
    private static final String pfc667dbb = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final ClientPlayerEntity player;
   private final ClientWorld level;
   private final Hand hand;
   private final BlockHitResult hitResult;
// https://t.me/pasterplace

   public EventClickBlockRight(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult) {
      this.player = player;
      this.level = world;
      this.hand = hand;
      this.hitResult = hitResult;
   }
// https://t.me/pasterplace

   public ClientPlayerEntity player() {
      return this.player;
   }
// https://t.me/pasterplace

   public ClientWorld level() {
      return this.level;
   }
// https://t.me/pasterplace

   public Hand hand() {
      return this.hand;
   }
// https://t.me/pasterplace

   public BlockHitResult hitResult() {
      return this.hitResult;
   }
}

