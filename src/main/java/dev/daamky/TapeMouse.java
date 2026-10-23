package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class TapeMouse extends ModuleStructure {
    private static final String p67c97955 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Клавиша", "Левая", "Левая", "Правая");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Задержка", 1.0F, 1.0F, 30.0F, 1.0F, "т");
   private int l1lll11l1l;
// https://t.me/pasterplace

   public TapeMouse() {
      super("TapeMouse", "Автоматические клики с задержкой", ModuleCategory.PLAYER);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.l1lll11l1l = 0;
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && CLIENT.interactionManager != null && CLIENT.currentScreen == null) {
         if (!(++this.l1lll11l1l < this.IllI1l1l1.getValue())) {
            this.l1lll11l1l = 0;
            if (!this.l1I1IlllIlI.isSelected("Правая")) {
               HitResult hitResult = CLIENT.crosshairTarget;
               if (!(hitResult instanceof EntityHitResult)) {
                  if (hitResult instanceof BlockHitResult) {
                      BlockHitResult hitResult3 = (BlockHitResult)hitResult;
                     if (hitResult.getType() == Type.BLOCK
                        && CLIENT.interactionManager.attackBlock(hitResult3.getBlockPos(), hitResult3.getSide())) {
                        CLIENT.player.swingHand(Hand.MAIN_HAND);
                     }
                  }
               } else {
                   EntityHitResult hitResult2 = (EntityHitResult)hitResult;
                  CombatUtils.attackEntity(hitResult2.getEntity());
               }
            } else {
               CLIENT.interactionManager.interactItem(CLIENT.player, Hand.MAIN_HAND);
            }
         }
      }
   }
}

