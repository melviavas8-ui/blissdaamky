package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class CrystalOptimizer extends ModuleStructure {
   public CrystalOptimizer() {
      super("CrystalOptimizer", "Мгновенно убирает кристалл после удара для быстрой установки следующего", ModuleCategory.COMBAT);
   }

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

