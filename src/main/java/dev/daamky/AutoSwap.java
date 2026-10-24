package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import dev.daamky.core.event.impl.EventUpdate;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoSwap extends ModuleStructure {
   private static final String[] SWAP_ITEM_OPTIONS = new String[]{"Тотем", "Гепл", "Щит", "Шар", "Фейерверк"};
   private final MultiOptionSetting primaryItemSetting = new MultiOptionSetting("Первый предмет", "Тотем", SWAP_ITEM_OPTIONS);
   private final MultiOptionSetting secondaryItemSetting = new MultiOptionSetting("Второй предмет", "Гепл", SWAP_ITEM_OPTIONS);
   private final ReallyWorld inventorySwapProfile = new ReallyWorld();
   private final IntSetting swapKeybind = new IntSetting("Бинд свапа", 0);
   private boolean swapPending;

   public AutoSwap() {
      super("AutoSwap", "По бинду меняет предмет во второй руке", ModuleCategory.COMBAT);
      this.addSettings(this.swapKeybind, this.primaryItemSetting, this.secondaryItemSetting, this.inventorySwapProfile);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onKey(EventKey eventKey) {
      if (this.hasActiveClientWorld()
         && CLIENT.currentScreen == null
         && this.swapKeybind.matchesKeyCode(dev.daamky.KeybindManager.encodeKey(eventKey.key(), eventKey.isMouse()))) {
         this.swapPending = true;
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.swapPending) {
         this.swapPending = false;
         if (this.hasActiveClientWorld() && InventoryUtils.canSwapInventorySlots()) {
            Predicate firstItemPredicate = predicateForItemType(this.primaryItemSetting.getValue());
            Predicate secondItemPredicate = predicateForItemType(this.secondaryItemSetting.getValue());
            Predicate selectedItemPredicate = !firstItemPredicate.test(CLIENT.player.getOffHandStack()) ? firstItemPredicate : secondItemPredicate;
            int inventorySlot;
            if ((inventorySlot = InventoryUtils.findItemSlot(selectedItemPredicate)) != -1) {
               InventoryUtils.swapInventorySlotWithOffhand(inventorySlot, this.inventorySwapProfile.getInventorySwapCategory());
            }
         }
      }
   }

   private static Predicate<ItemStack> predicateForItemType(String itemType) {
      return switch (itemType) {
         case "Тотем" -> candidateStack -> candidateStack.isOf(Items.TOTEM_OF_UNDYING);
         case "Гепл" -> candidateStack -> candidateStack.isOf(Items.GOLDEN_APPLE) || candidateStack.isOf(Items.ENCHANTED_GOLDEN_APPLE);
         case "Щит" -> candidateStack -> candidateStack.isOf(Items.SHIELD);
         case "Шар" -> candidateStack -> candidateStack.isOf(Items.PLAYER_HEAD);
         case "Фейерверк" -> candidateStack -> candidateStack.isOf(Items.FIREWORK_ROCKET);
         default -> candidateStack -> false;
      };
   }
}

