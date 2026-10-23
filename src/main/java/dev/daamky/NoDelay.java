package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.mixin.client.MinecraftAccessor;
import dev.daamky.mixin.client.MultiPlayerGameModeAccessor;
import dev.daamky.mixin.entity.LivingEntityAccessor;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoDelay extends ModuleStructure {
    private static final String pabf52396 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiToggleSetting l1I1IlllIlI = new MultiToggleSetting("Убирать задержку")
      .addOption("Прыжок", true)
      .addOption("Использование", true)
      .addOption("Ломание", false);
// https://t.me/pasterplace

   public NoDelay() {
      super("NoDelay", "Убирает задержки прыжка, использования и ломания", ModuleCategory.PLAYER);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (this.l1I1IlllIlI.isOptionEnabled("Прыжок")) {
            ((LivingEntityAccessor)CLIENT.player).daamky$setNoJumpDelay(0);
         }

         if (this.l1I1IlllIlI.isOptionEnabled("Использование")) {
            ((MinecraftAccessor)CLIENT).daamky$setRightClickDelay(0);
         }
// https://t.me/pasterplace

         if (this.l1I1IlllIlI.isOptionEnabled("Ломание")) {
            ((MultiPlayerGameModeAccessor)CLIENT.interactionManager).daamky$setDestroyDelay(0);
         }
      }
   }
}

