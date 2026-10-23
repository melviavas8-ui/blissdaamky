package dev.daamky;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;

public final class Cooldowns extends AnimatedListHudElement {
    private static final String p2f46b230 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public Cooldowns() {
      super("Cooldowns", "cooldowns", 7.0F, 260.0F);
   }
// https://t.me/pasterplace

   @Override
   protected String getHeaderGlyph() {
      return "P";
   }
// https://t.me/pasterplace

   @Override
   protected boolean shouldShowTrailingProgress() {
      return true;
   }
// https://t.me/pasterplace

   @Override
   protected String getHeaderLabel() {
      return "Cooldowns";
   }
// https://t.me/pasterplace

   @Override
   protected List<AnimatedListHudElement.HudListEntry> getEntries(boolean chatScreenOpen) {
      ArrayList arrayList = new ArrayList();
      if (CLIENT.player == null) {
         return arrayList;
      }

      ItemCooldownManager itemCooldownManager = CLIENT.player.getItemCooldownManager();
      PlayerInventory playerInventory = CLIENT.player.getInventory();
      HashSet<Item> hashSet = new HashSet<>();
      ArrayList<ItemStack> arrayList2 = new ArrayList<>();

      for (int slotIndex = 0; slotIndex < 36; slotIndex++) {
         arrayList2.add(playerInventory.getStack(slotIndex));
      }

      arrayList2.add(CLIENT.player.getEquippedStack(EquipmentSlot.OFFHAND));
// https://t.me/pasterplace

       for (ItemStack cooldownStack : arrayList2) {
          if (!cooldownStack.isEmpty() && hashSet.add(cooldownStack.getItem()) && itemCooldownManager.isCoolingDown(cooldownStack)) {
             float cooldownProgress = itemCooldownManager.getCooldownProgress(cooldownStack, 0.0F);
            arrayList.add(
               new AnimatedListHudElement.HudListEntry(
                   cooldownStack.getName().getString(), Math.round(cooldownProgress * 100.0F) + "%", cooldownStack.copy(), null, null, cooldownProgress
               )
            );
         }
      }

      return arrayList;
   }
}

