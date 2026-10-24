package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class BowSpammer extends ModuleStructure {
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Скорость", 250.0F, 1.0F, 1000.0F, 1.0F, "мс");
   private final Stopwatch IllI1l1l1 = new Stopwatch();
   private boolean l1lll11l1l;
   private Hand lIIl11l111lIIl1ll;

   public BowSpammer() {
      super("BowSpammer", "Быстрый цикл отпускания и натяжки лука", ModuleCategory.COMBAT);
      this.addSetting(this.l1I1IlllIlI);
   }

   @Override
   public String getDisplayInfo() {
      return String.valueOf(Math.round(this.l1I1IlllIlI.getValue()));
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      super.onDisable();
      this.l1lll11l1l = false;
      this.lIIl11l111lIIl1ll = null;
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && CLIENT.interactionManager != null) {
         if (!this.l1lll11l1l) {
            if (CLIENT.player.isUsingItem()
               && CLIENT.player.getActiveItem().isOf(Items.BOW)
               && CLIENT.player.getItemUseTime() >= 3) {
               if (this.IllI1l1l1.hasElapsed(this.l1I1IlllIlI.getValue().longValue())) {
                  this.lIIl11l111lIIl1ll = CLIENT.player.getActiveHand();
                  CLIENT.interactionManager.stopUsingItem(CLIENT.player);
                  this.l1lll11l1l = true;
                  this.IllI1l1l1.reset();
               }
            }
         } else {
            if (this.lIIl11l111lIIl1ll != null) {
               CLIENT.interactionManager.interactItem(CLIENT.player, this.lIIl11l111lIIl1ll);
            }

            this.l1lll11l1l = false;
            this.lIIl11l111lIIl1ll = null;
         }
      }
   }
}

