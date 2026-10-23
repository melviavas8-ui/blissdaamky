package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoFix extends ModuleStructure {
    private static final String p9ad26872 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final EquipmentSlot[] l1I1IlllIlI = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private final FloatSetting IllI1l1l1 = new FloatSetting("Задержка", 5.0F, 1.0F, 60.0F, 1.0F, "сек");
   private final Stopwatch l1lll11l1l = new Stopwatch();
// https://t.me/pasterplace

   public AutoFix() {
      super("AutoFix", "Пишет /fix, если броня повреждена и нет PvP", ModuleCategory.PLAYER);
      this.addSetting(this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && !Aceopxykhbtm.II1Il11l111II11IIl() && this.l1lll11l1l.hasElapsed((long)(this.IllI1l1l1.getValue() * 1000.0F))) {
         if (this.l1I1IIl111Il1l1l()) {
            CLIENT.getNetworkHandler().sendChatCommand("fix");
            this.l1lll11l1l.reset();
         }
      }
   }
// https://t.me/pasterplace

   private boolean l1I1IIl111Il1l1l() {
      for (EquipmentSlot equipmentSlot : l1I1IlllIlI) {
         ItemStack equippedStack = CLIENT.player.getEquippedStack(equipmentSlot);
         if (!equippedStack.isEmpty() && equippedStack.isDamageable() && equippedStack.getDamage() > 0) {
            return true;
         }
      }
// https://t.me/pasterplace

      return false;
   }
}

