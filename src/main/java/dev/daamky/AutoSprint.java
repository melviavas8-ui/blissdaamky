package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventInput;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoSprint extends ModuleStructure {
    private static final String pfd2f81cf = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final BooleanSetting l1I1IlllIlI = new BooleanSetting("Не сбрасывать при ударе", false);
// https://t.me/pasterplace

   public AutoSprint() {
      super("AutoSprint", "Автоматически включает бег", ModuleCategory.MOVEMENT);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

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

