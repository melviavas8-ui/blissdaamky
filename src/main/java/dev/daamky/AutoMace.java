package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoMace extends ModuleStructure {
    private static final String pc987aab3 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Мин. высота падения", 1.5F, 0.5F, 10.0F, 0.5F, "бл.");
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("Возвращать слот", true);
   private int l1lll11l1l = -1;
   private int lIIl11l111lIIl1ll;
// https://t.me/pasterplace

   public AutoMace() {
      super("AutoMace", "Берёт булаву для удара в падении", ModuleCategory.COMBAT);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventAttack eventAttack) {
      if (eventAttack.isPre() && this.hasActiveClientWorld() && eventAttack.target() instanceof LivingEntity) {
         if (!(CLIENT.player.fallDistance < this.l1I1IlllIlI.getValue().floatValue())
            && !CLIENT.player.isOnGround()
            && !CLIENT.player.getMainHandStack().isOf(Items.MACE)) {
            int maceHotbarSlot;
            if ((maceHotbarSlot = InventoryUtils.findHotbarSlot(candidateStack -> candidateStack.isOf(Items.MACE))) != -1) {
               if (this.l1lll11l1l == -1) {
                  this.l1lll11l1l = InventoryUtils.getSelectedHotbarSlot();
               }
// https://t.me/pasterplace

               InventoryUtils.setSelectedHotbarSlot(maceHotbarSlot);
               this.lIIl11l111lIIl1ll = 2;
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.l1lll11l1l != -1 && this.hasActiveClientWorld() && --this.lIIl11l111lIIl1ll <= 0) {
         if (this.IllI1l1l1.getValue()) {
            InventoryUtils.setSelectedHotbarSlot(this.l1lll11l1l);
         }

         this.l1lll11l1l = -1;
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.l1lll11l1l = -1;
   }
}

