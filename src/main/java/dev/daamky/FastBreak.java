package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.mixin.client.MultiPlayerGameModeAccessor;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class FastBreak extends ModuleStructure {
    private static final String pa43277f5 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "Прогресс", "Прогресс", "Без задержки");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Порог ломания", 0.75F, 0.5F, 1.0F, 0.01F);
// https://t.me/pasterplace

   public FastBreak() {
      super("FastBreak", "Ускоряет ломание блоков", ModuleCategory.PLAYER);
      this.IllI1l1l1.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Прогресс"));
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         MultiPlayerGameModeAccessor interactionManagerAccessor = (MultiPlayerGameModeAccessor)CLIENT.interactionManager;
         interactionManagerAccessor.daamky$setDestroyDelay(0);
         if (this.l1I1IlllIlI.isSelected("Прогресс")
            && CLIENT.interactionManager.isBreakingBlock()
            && interactionManagerAccessor.daamky$getDestroyProgress() >= this.IllI1l1l1.getValue()) {
            interactionManagerAccessor.daamky$setDestroyProgress(1.0F);
         }
      }
   }
}

