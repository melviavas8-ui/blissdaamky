package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.mixin.client.MinecraftAccessor;
import dev.daamky.mixin.client.MultiPlayerGameModeAccessor;
import dev.daamky.mixin.entity.LivingEntityAccessor;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoDelay extends ModuleStructure {
   private final MultiToggleSetting l1I1IlllIlI = new MultiToggleSetting("Убирать задержку")
      .addOption("Прыжок", true)
      .addOption("Использование", true)
      .addOption("Ломание", false);

   public NoDelay() {
      super("NoDelay", "Убирает задержки прыжка, использования и ломания", ModuleCategory.PLAYER);
      this.addSetting(this.l1I1IlllIlI);
   }

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

         if (this.l1I1IlllIlI.isOptionEnabled("Ломание")) {
            ((MultiPlayerGameModeAccessor)CLIENT.interactionManager).daamky$setDestroyDelay(0);
         }
      }
   }
}

