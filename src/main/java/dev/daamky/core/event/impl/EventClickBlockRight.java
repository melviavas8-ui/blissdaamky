package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public final class EventClickBlockRight extends Event {
   private final ClientPlayerEntity player;
   private final ClientWorld level;
   private final Hand hand;
   private final BlockHitResult hitResult;

   public EventClickBlockRight(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult) {
      this.player = player;
      this.level = world;
      this.hand = hand;
      this.hitResult = hitResult;
   }

   public ClientPlayerEntity player() {
      return this.player;
   }

   public ClientWorld level() {
      return this.level;
   }

   public Hand hand() {
      return this.hand;
   }

   public BlockHitResult hitResult() {
      return this.hitResult;
   }
}

