package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventInput;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoSprint extends ModuleStructure {
   private final BooleanSetting l1I1IlllIlI = new BooleanSetting("Не сбрасывать при ударе", false);

   public AutoSprint() {
      super("AutoSprint", "Автоматически включает бег", ModuleCategory.MOVEMENT);
      this.addSetting(this.l1I1IlllIlI);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventInput eventInput) {
      if (this.hasActiveClientWorld()) {
         Aura aura = Aura.getInstance();
         if (aura == null || !aura.isSprintResetActive() || this.l1I1IlllIlI.getValue()) {
            if (eventInput.forward() > 0.0F && !eventInput.sneak()) {
               eventInput.setSprint(true);
            }
         }
      }
   }
}

