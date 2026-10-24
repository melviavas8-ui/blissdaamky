package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoFriendDamage extends ModuleStructure {
   public NoFriendDamage() {
      super("NoFriendDamage", "Не даёт ударить игрока из списка друзей", ModuleCategory.COMBAT);
      this.setEnabled(true);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventAttack eventAttack) {
      if (eventAttack.isPre() && FriendManager.getInstance().isFriend(eventAttack.target())) {
         eventAttack.cancel();
      }
   }
}

