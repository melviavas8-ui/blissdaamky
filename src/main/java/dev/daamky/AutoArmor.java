package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoArmor extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final ReallyWorld inventorySwapProfile = new ReallyWorld();
   private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final int[] ARMOR_HANDLER_SLOTS = new int[]{5, 6, 7, 8};
   private final FloatSetting actionDelay = new FloatSetting("Задержка", 150.0F, 0.0F, 1000.0F, 10.0F, "мс");
   private final BooleanSetting onlyInInventory = new BooleanSetting("Только в инвентаре", false);
   private final BooleanSetting keepElytraEquipped = new BooleanSetting("Не снимать элитры", true);
   private final Stopwatch actionStopwatch = new Stopwatch();
// https://t.me/pasterplace

   public AutoArmor() {
      super("AutoArmor", "Надевает лучшую броню из инвентаря", ModuleCategory.PLAYER);
      this.addSettings(this.actionDelay, this.onlyInInventory, this.keepElytraEquipped, this.inventorySwapProfile);
   }
// https://t.me/pasterplace

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()
         && InventoryUtils.canSwapInventorySlots()
         && this.actionStopwatch.hasElapsed(this.actionDelay.getValue().longValue())) {
         if (this.onlyInInventory.getValue()
            ? CLIENT.currentScreen instanceof InventoryScreen
            : CLIENT.currentScreen == null || CLIENT.currentScreen instanceof InventoryScreen) {
            if (CLIENT.player.currentScreenHandler.getCursorStack().isEmpty()) {
               for (int armorSlotIndex = 0; armorSlotIndex < ARMOR_SLOTS.length; armorSlotIndex++) {
                  EquipmentSlot equipmentSlot = ARMOR_SLOTS[armorSlotIndex];
                  ItemStack equippedStack = CLIENT.player.getEquippedStack(equipmentSlot);
                  if ((!this.keepElytraEquipped.getValue() || !equippedStack.isOf(Items.ELYTRA))
                     && (
                        equippedStack.isEmpty()
                           || CLIENT.player.isCreative()
                           || !EnchantmentHelper.hasAnyEnchantmentsWith(equippedStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)
                     )) {
                     float currentArmorScore = getArmorScore(equippedStack, equipmentSlot);
                     int bestInventorySlot = -1;
                     float bestArmorScore = currentArmorScore;

                     for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
                        float candidateArmorScore = getArmorScore(CLIENT.player.getInventory().getStack(inventorySlot), equipmentSlot);
                        if (candidateArmorScore > bestArmorScore) {
                           bestArmorScore = candidateArmorScore;
                           bestInventorySlot = inventorySlot;
                        }
                     }

                     if (bestInventorySlot != -1) {
                        int screenHandlerSlot = InventoryUtils.toScreenHandlerSlot(bestInventorySlot);
                        if (equippedStack.isEmpty()) {
                           InventoryUtils.quickMoveSlot(screenHandlerSlot, this.inventorySwapProfile.getInventorySwapCategory());
                        } else {
                           int armorHandlerSlot = ARMOR_HANDLER_SLOTS[armorSlotIndex];
                           Priority.getInstance().scheduleAction(this.inventorySwapProfile.getInventorySwapCategory(), () -> {
                              InventoryUtils.clickSlot(screenHandlerSlot, 0, SlotActionType.PICKUP);
                              InventoryUtils.clickSlot(armorHandlerSlot, 0, SlotActionType.PICKUP);
                              InventoryUtils.clickSlot(screenHandlerSlot, 0, SlotActionType.PICKUP);
                           });
                        }
// https://t.me/pasterplace

                        this.actionStopwatch.reset();
                        return;
                     }
                  }
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private static float getArmorScore(ItemStack armorStack, EquipmentSlot equipmentSlot) {
      if (armorStack.isEmpty()) {
         return 0.0F;
      }

      EquippableComponent equippable = armorStack.get(DataComponentTypes.EQUIPPABLE);
      if (equippable != null && equippable.slot() == equipmentSlot && !armorStack.isOf(Items.ELYTRA)) {
         AttributeModifiersComponent modifiers = armorStack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
         float armorPoints = 0.0F;
         float armorToughness = 0.0F;

         for (AttributeModifiersComponent.Entry attributeModifier : modifiers.modifiers()) {
            if (attributeModifier.attribute().matches(EntityAttributes.ARMOR)) {
               armorPoints += (float)attributeModifier.modifier().value();
            } else if (attributeModifier.attribute().matches(EntityAttributes.ARMOR_TOUGHNESS)) {
               armorToughness += (float)attributeModifier.modifier().value();
            }
         }

         float durabilityMultiplier = armorStack.isDamageable() ? 1.0F - (float)armorStack.getDamage() / Math.max(1, armorStack.getMaxDamage()) : 1.0F;
         return armorPoints * 10.0F + armorToughness * 4.0F + durabilityMultiplier + 0.01F;
      } else {
         return 0.0F;
      }
   }
}

