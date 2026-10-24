package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Timer extends ModuleStructure {
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Скорость", 1.5F, 0.1F, 5.0F, 0.05F, "x");
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("Только в движении", false);
   private final BooleanSetting l1lll11l1l = new BooleanSetting("Умный (балансировка)", false);
   private final FloatSetting lIIl11l111lIIl1ll = new FloatSetting("Запас тиков", 20.0F, 5.0F, 100.0F, 1.0F);
   private float l1IIl11lI;
   private boolean I1lIIII1III1lIlIl1l;

   public Timer() {
      super("Timer", "Изменяет скорость игровых тиков", ModuleCategory.MOVEMENT);
      this.lIIl11l111lIIl1ll.visibleWhen(this.l1lll11l1l::getValue);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l, this.lIIl11l111lIIl1ll);
   }

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.formatValue();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.l1IIl11lI = this.lIIl11l111lIIl1ll.getValue();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (!this.hasActiveClientWorld()) {
         this.I1lIIII1III1lIlIl1l = false;
      } else {
         int shouldApplyTimer = this.IllI1l1l1.getValue() && !MovementUtils.isMoving() ? 0 : 1;
         if (!this.l1lll11l1l.getValue()) {
            this.I1lIIII1III1lIlIl1l = shouldApplyTimer != 0;
         } else if (shouldApplyTimer != 0 && this.l1IIl11lI > 0.0F) {
            this.l1IIl11lI = this.l1IIl11lI - (this.l1I1IlllIlI.getValue() - 1.0F);
            this.I1lIIII1III1lIlIl1l = true;
         } else {
            this.I1lIIII1III1lIlIl1l = false;
            this.l1IIl11lI = Math.min(this.lIIl11l111lIIl1ll.getValue(), this.l1IIl11lI + 0.5F);
         }
      }
   }

   public static float l1I1IIl111Il1l1l() {
      Timer moduleStructure = (Timer)ModuleRegistry.getInstance().getModuleByClass(Timer.class);
      if (moduleStructure != null && moduleStructure.isEnabled() && moduleStructure.I1lIIII1III1lIlIl1l) {
         return Math.max(0.1F, moduleStructure.l1I1IlllIlI.getValue());
      }

      float timerMultiplier = ElytraBoost.getElytraTimerMultiplier();
      return timerMultiplier != 1.0F ? timerMultiplier : Speed.getReallyWorldSpeedMultiplier();
   }
}

