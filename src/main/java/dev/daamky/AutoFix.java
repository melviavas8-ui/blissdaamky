package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoFix extends ModuleStructure {
   private static final EquipmentSlot[] l1I1IlllIlI = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private final FloatSetting IllI1l1l1 = new FloatSetting("Задержка", 5.0F, 1.0F, 60.0F, 1.0F, "сек");
   private final Stopwatch l1lll11l1l = new Stopwatch();

   public AutoFix() {
      super("AutoFix", "Пишет /fix, если броня повреждена и нет PvP", ModuleCategory.PLAYER);
      this.addSetting(this.IllI1l1l1);
   }

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

   private boolean l1I1IIl111Il1l1l() {
      for (EquipmentSlot equipmentSlot : l1I1IlllIlI) {
         ItemStack equippedStack = CLIENT.player.getEquippedStack(equipmentSlot);
         if (!equippedStack.isEmpty() && equippedStack.isDamageable() && equippedStack.getDamage() > 0) {
            return true;
         }
      }

      return false;
   }
}

