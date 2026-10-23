package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class CrystalOptimizer extends ModuleStructure {
    private static final String pe7b2ae99 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public CrystalOptimizer() {
      super("CrystalOptimizer", "Мгновенно убирает кристалл после удара для быстрой установки следующего", ModuleCategory.COMBAT);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventAttack eventAttack) {
      if (eventAttack.isPost() && this.hasActiveClientWorld() && CLIENT.world != null) {
         Entity entity = eventAttack.target();
         if (entity instanceof EndCrystalEntity) {
            Entity entity2 = (EndCrystalEntity)entity;
            if (entity2.isAlive() && !entity2.isInvulnerable()) {
               CLIENT.world.removeEntity(entity2.getId(), RemovalReason.KILLED);
            }
         }
      }
   }
}

