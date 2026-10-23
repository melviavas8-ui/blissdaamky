package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoFriendDamage extends ModuleStructure {
    private static final String p3eccd90a = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public NoFriendDamage() {
      super("NoFriendDamage", "Не даёт ударить игрока из списка друзей", ModuleCategory.COMBAT);
      this.setEnabled(true);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventAttack eventAttack) {
      if (eventAttack.isPre() && FriendManager.getInstance().isFriend(eventAttack.target())) {
         eventAttack.cancel();
      }
   }
}

