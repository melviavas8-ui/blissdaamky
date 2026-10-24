package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class EventAttackPre extends Event {
   private final PlayerEntity attacker;
   private final Entity target;

   public EventAttackPre(PlayerEntity attacker, Entity target) {
      this.attacker = attacker;
      this.target = target;
   }

   public PlayerEntity getAttacker() {
      return this.attacker;
   }

   public Entity getTarget() {
      return this.target;
   }
}

