package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class InventoryCleaner extends ModuleStructure {
    private static final String p1991b6de = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final ReallyWorld l1I1IlllIlI = new ReallyWorld();
   private final MultiToggleSetting IllI1l1l1 = new MultiToggleSetting("Выбрасывать")
      .addOption("Земля и камень", true)
      .addOption("Семена", true)
      .addOption("Гнилая плоть", true)
      .addOption("Кости и нити", false)
      .addOption("Яды", true);
   private final FloatSetting l1lll11l1l = new FloatSetting("Задержка", 120.0F, 0.0F, 1000.0F, 10.0F, "мс");
   private final Stopwatch lIIl11l111lIIl1ll = new Stopwatch();
// https://t.me/pasterplace

   public InventoryCleaner() {
      super("InventoryCleaner", "Выбрасывает мусор из инвентаря", ModuleCategory.PLAYER);
      this.addSettings(this.IllI1l1l1, this.l1lll11l1l, this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && InventoryUtils.canSwapInventorySlots() && this.lIIl11l111lIIl1ll.hasElapsed(this.l1lll11l1l.getValue().longValue())) {
         if (CLIENT.currentScreen == null || CLIENT.currentScreen instanceof InventoryScreen) {
            for (int slotIndex = 9; slotIndex < 36; slotIndex++) {
               if (this.II1Il11l111II11IIl(CLIENT.player.getInventory().getStack(slotIndex))) {
                  int slotIndexToThrow = slotIndex;
                  Priority.getInstance()
                     .scheduleAction(this.l1I1IlllIlI.getInventorySwapCategory(), () -> InventoryUtils.clickSlot(slotIndexToThrow, 1, SlotActionType.THROW));
                  this.lIIl11l111lIIl1ll.reset();
                  return;
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private boolean II1Il11l111II11IIl(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }
// https://t.me/pasterplace

      if (!this.IllI1l1l1.isOptionEnabled("Земля и камень")
         || !stack.isOf(Items.DIRT)
            && !stack.isOf(Items.GRAVEL)
            && !stack.isOf(Items.DIORITE)
            && !stack.isOf(Items.GRANITE)
            && !stack.isOf(Items.ANDESITE)
            && !stack.isOf(Items.TUFF)
            && !stack.isOf(Items.NETHERRACK)) {
         if (!this.IllI1l1l1.isOptionEnabled("Семена")
            || !stack.isOf(Items.WHEAT_SEEDS)
               && !stack.isOf(Items.BEETROOT_SEEDS)
               && !stack.isOf(Items.MELON_SEEDS)
               && !stack.isOf(Items.PUMPKIN_SEEDS)) {
            if (this.IllI1l1l1.isOptionEnabled("Гнилая плоть") && stack.isOf(Items.ROTTEN_FLESH)) {
               return true;
            } else {
               return !this.IllI1l1l1.isOptionEnabled("Кости и нити")
                     || !stack.isOf(Items.BONE) && !stack.isOf(Items.STRING) && !stack.isOf(Items.SPIDER_EYE)
                  ? this.IllI1l1l1.isOptionEnabled("Яды") && (stack.isOf(Items.POISONOUS_POTATO) || stack.isOf(Items.PUFFERFISH))
                  : true;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }
}

