package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class WaterSpeed extends ModuleStructure {
    private static final String p702373e8 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Скорость", 0.35F, 0.1F, 1.5F, 0.01F);
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("В лаве", false);
// https://t.me/pasterplace

   public WaterSpeed() {
      super("WaterSpeed", "Ускоряет плавание", ModuleCategory.MOVEMENT);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && MovementUtils.isMoving()) {
         if (CLIENT.player.isTouchingWater() || this.IllI1l1l1.getValue() && CLIENT.player.isInLava()) {
            MovementUtils.setHorizontalSpeed(Math.max(MovementUtils.getHorizontalSpeed(), this.l1I1IlllIlI.getValue().floatValue()));
         }
      }
   }
}

