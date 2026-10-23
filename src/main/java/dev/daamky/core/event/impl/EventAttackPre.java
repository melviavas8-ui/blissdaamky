package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class EventAttackPre extends Event {
    private static final String pa1f0afb7 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final PlayerEntity attacker;
   private final Entity target;
// https://t.me/pasterplace

   public EventAttackPre(PlayerEntity attacker, Entity target) {
      this.attacker = attacker;
      this.target = target;
   }
// https://t.me/pasterplace

   public PlayerEntity getAttacker() {
      return this.attacker;
   }
// https://t.me/pasterplace

   public Entity getTarget() {
      return this.target;
   }
}

