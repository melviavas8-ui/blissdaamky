package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class PlayerHelper extends ModuleStructure {
    private static final String SOURCE_CHANNEL_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final int HOTBAR_SLOT_INDEX = 8;
   private static final String BOUNCY_BOOTS_NAME = "Попрыгун";
   private static final int SHULKER_HOTBAR_SLOT_INDEX = 8;
   private static final Set<Item> CONSUMABLE_ITEMS = Set.of(
      Items.GOLDEN_APPLE,
      Items.ENCHANTED_GOLDEN_APPLE,
      Items.GOLDEN_CARROT,
      Items.CHORUS_FRUIT,
      Items.ENDER_PEARL,
      Items.ENDER_EYE,
      Items.EXPERIENCE_BOTTLE,
      Items.POTION,
      Items.SPLASH_POTION,
      Items.LINGERING_POTION,
      Items.FIREWORK_ROCKET,
      Items.SNOWBALL
   );
   private static final Set<Item> VALUABLE_MATERIAL_ITEMS = Set.of(
      Items.NETHERITE_INGOT,
      Items.NETHERITE_SCRAP,
      Items.NETHERITE_BLOCK,
      Items.ANCIENT_DEBRIS,
      Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
      Items.NETHER_STAR,
      Items.BEACON,
      Items.ELYTRA,
      Items.DIAMOND,
      Items.DIAMOND_BLOCK,
      Items.EMERALD,
      Items.EMERALD_BLOCK,
      Items.ECHO_SHARD,
      Items.HEAVY_CORE,
      Items.DRAGON_EGG,
      Items.DRAGON_BREATH,
      Items.ENDER_CHEST
   );
   private static final Set<Item> VALUABLE_ARMOR_ITEMS = Set.of(
      Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, Items.GOLDEN_HELMET, Items.GOLDEN_BOOTS
   );
   private static final Set<Item> VALUABLE_WEAPON_ITEMS = Set.of(
      Items.NETHERITE_SWORD, Items.NETHERITE_AXE, Items.MACE, Items.TRIDENT, Items.CROSSBOW, Items.BOW, Items.NETHERITE_PICKAXE
   );
   private static final Set<Item> HEAD_ITEMS = Set.of(
      Items.PLAYER_HEAD, Items.WITHER_SKELETON_SKULL, Items.SKELETON_SKULL, Items.DRAGON_HEAD, Items.CREEPER_HEAD, Items.ZOMBIE_HEAD, Items.PIGLIN_HEAD
   );
   private final ReallyWorld swapModeSetting = new ReallyWorld();
   private final BooleanSetting autoShulkerEnabled = new BooleanSetting("Авто-шалкер", true);
   private final MultiOptionSetting storageMode = new MultiOptionSetting("Режим", "По бинду", "По бинду", "Автоматический");
   private final IntSetting shulkerBind = new IntSetting("Бинд", 0);
   private final MultiToggleSetting valuableCategories = new MultiToggleSetting("Ценности")
      .addOption("Броня", true)
      .addOption("Оружие", true)
      .addOption("Книги", true)
      .addOption("Тотемы", true)
      .addOption("Головы", true)
      .addOption("Расходники", true)
      .addOption("Ресурсы", true);
   private final FloatSetting autoStorageDelay = new FloatSetting("Задержка", 300.0F, 0.0F, 2000.0F, 50.0F, "мс");
   private final BooleanSetting swapBootsEnabled = new BooleanSetting("Свап ботинок", true);
   private final IntSetting swapBootsBind = new IntSetting("Бинд ботинок", 0);
   private final BooleanSetting autoMaceBootsEnabled = new BooleanSetting("Авто с булавой", false);
   private final Stopwatch autoStorageTimer = new Stopwatch();
   private final Stopwatch shulkerInteractionTimer = new Stopwatch();
   private PlayerHelper.InventorySwapState storageState = PlayerHelper.InventorySwapState.IDLE;
   private boolean storageRequested;
   private int sourceInventorySlot = -1;
   private int previousSelectedSlot = -1;
   private int previousValuableItemCount;
   private boolean lastMaceMode;
// https://t.me/pasterplace

   public PlayerHelper() {
      super("Player Helper", "Складывает ценности в шалкер и меняет ботинки-попрыгуны", ModuleCategory.PLAYER);
      this.storageMode.visibleWhen(this.autoShulkerEnabled::getValue);
      this.shulkerBind.visibleWhen(() -> this.autoShulkerEnabled.getValue() && this.storageMode.isSelected("По бинду"));
      this.valuableCategories.visibleWhen(this.autoShulkerEnabled::getValue);
      this.autoStorageDelay.visibleWhen(() -> this.autoShulkerEnabled.getValue() && this.storageMode.isSelected("Автоматический"));
      this.swapBootsBind.visibleWhen(this.swapBootsEnabled::getValue);
      this.autoMaceBootsEnabled.visibleWhen(this.swapBootsEnabled::getValue);
      this.addSettings(
         this.autoShulkerEnabled,
         this.storageMode,
         this.shulkerBind,
         this.valuableCategories,
         this.autoStorageDelay,
         this.swapBootsEnabled,
         this.swapBootsBind,
         this.autoMaceBootsEnabled,
         this.swapModeSetting
      );
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.previousValuableItemCount = !this.hasActiveClientWorld() ? 0 : this.countPlayerItems();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.storageState = PlayerHelper.InventorySwapState.IDLE;
      this.storageRequested = false;
      this.lastMaceMode = false;
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange worldChange) {
      this.storageState = PlayerHelper.InventorySwapState.IDLE;
      this.storageRequested = false;
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onKey(EventKey keyEvent) {
      if (this.hasActiveClientWorld() && CLIENT.currentScreen == null) {
         int keyCode = dev.daamky.KeybindManager.encodeKey(keyEvent.key(), keyEvent.isMouse());
         if (this.autoShulkerEnabled.getValue()
            && this.storageMode.isSelected("По бинду")
            && this.shulkerBind.matchesKeyCode(keyCode)) {
            this.storageRequested = true;
         }

         if (this.swapBootsEnabled.getValue() && this.swapBootsBind.matchesKeyCode(keyCode)) {
            this.swapBouncyBoots(!this.isBouncyBoots(this.getEquippedBoots()));
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate updateEvent) {
      if (this.hasActiveClientWorld()) {
         if (this.swapBootsEnabled.getValue() && this.autoMaceBootsEnabled.getValue()) {
            this.updateMaceBoots();
         }

         if (this.autoShulkerEnabled.getValue() || this.storageState != PlayerHelper.InventorySwapState.IDLE) {
            switch (this.storageState) {
               case IDLE:
                  this.startShulkerDeposit();
                  break;
               case OPENING_SHULKER:
                  this.openShulkerForDeposit();
                  break;
               case DEPOSITING_ITEMS:
                  this.depositValuableItems();
                  break;
               case RESTORING_INVENTORY:
                  this.restoreInventoryAfterDeposit();
            }
         }
      }
   }
// https://t.me/pasterplace

   private void updateMaceBoots() {
      boolean holdingMace = CLIENT.player.getMainHandStack().isOf(Items.MACE);
      if (holdingMace != this.lastMaceMode) {
         if (this.isBouncyBoots(this.getEquippedBoots()) == holdingMace
            || this.swapBouncyBoots(holdingMace)
            || InventoryUtils.findItemSlot(candidateBoots -> candidateBoots.isIn(ItemTags.FOOT_ARMOR) && this.isBouncyBoots(candidateBoots) == holdingMace) == -1) {
            this.lastMaceMode = holdingMace;
         }
      }
   }
// https://t.me/pasterplace

   private void startShulkerDeposit() {
      int valuableItemCount = this.countPlayerItems();
      if (this.storageMode.isSelected("Автоматический") && valuableItemCount > this.previousValuableItemCount) {
         this.storageRequested = true;
         this.autoStorageTimer.reset();
      }
// https://t.me/pasterplace

      this.previousValuableItemCount = valuableItemCount;
      if (this.storageRequested && CLIENT.currentScreen == null) {
         if (!this.storageMode.isSelected("Автоматический")
            || this.autoStorageTimer.hasElapsed(this.autoStorageDelay.getValue().longValue())) {
            this.storageRequested = false;
            if (valuableItemCount != 0 && InventoryUtils.canSwapInventorySlots()) {
               int shulkerInventorySlot = InventoryUtils.findItemSlot(this::isShulkerOrBackpack);
               if (shulkerInventorySlot != -1) {
                  this.previousSelectedSlot = InventoryUtils.getSelectedHotbarSlot();
                  if (shulkerInventorySlot < 9) {
                     this.sourceInventorySlot = -1;
                     InventoryUtils.setSelectedHotbarSlot(shulkerInventorySlot);
                  } else {
                     this.sourceInventorySlot = shulkerInventorySlot;
                     InventoryUtils.swapInventorySlotWithHotbar(shulkerInventorySlot, 8, this.swapModeSetting.getInventorySwapCategory());
                     InventoryUtils.setSelectedHotbarSlot(8);
                  }

                  this.storageState = PlayerHelper.InventorySwapState.OPENING_SHULKER;
                  this.autoStorageTimer.reset();
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private void openShulkerForDeposit() {
      if (this.hasOpenContainer()) {
         this.storageState = PlayerHelper.InventorySwapState.DEPOSITING_ITEMS;
         this.autoStorageTimer.reset();
      } else if (this.autoStorageTimer.hasElapsed(3000L)) {
         this.storageState = PlayerHelper.InventorySwapState.RESTORING_INVENTORY;
         this.autoStorageTimer.reset();
      } else {
         if (CLIENT.currentScreen == null
            && this.autoStorageTimer.hasElapsed(150L)
            && this.isShulkerOrBackpack(CLIENT.player.getMainHandStack())) {
            CLIENT.interactionManager.interactItem(CLIENT.player, Hand.MAIN_HAND);
            this.autoStorageTimer.reset();
         }
      }
   }
// https://t.me/pasterplace

   private void depositValuableItems() {
      if (this.hasOpenContainer() && !this.autoStorageTimer.hasElapsed(15000L)) {
         if (this.shulkerInteractionTimer.hasElapsed(50L)) {
            ScreenHandler playerScreenHandler = CLIENT.player.currentScreenHandler;

            for (Slot slot : playerScreenHandler.slots) {
               if (slot.inventory instanceof PlayerInventory
                  && slot.getIndex() >= 9
                  && slot.getIndex() < 36
                  && this.isValuableItem(slot.getStack())) {
                  CLIENT.interactionManager
                     .clickSlot(playerScreenHandler.syncId, ((Slot)slot).id, 0, SlotActionType.QUICK_MOVE, CLIENT.player);
                  this.shulkerInteractionTimer.reset();
                  return;
               }
            }

            this.closeShulkerAndAdvance();
         }
      } else {
         this.closeShulkerAndAdvance();
      }
   }
// https://t.me/pasterplace

   private void closeShulkerAndAdvance() {
      if (this.hasOpenContainer()) {
         CLIENT.player.closeHandledScreen();
      }

      this.storageState = PlayerHelper.InventorySwapState.RESTORING_INVENTORY;
      this.autoStorageTimer.reset();
   }
// https://t.me/pasterplace

   private void restoreInventoryAfterDeposit() {
      if (this.autoStorageTimer.hasElapsed(150L) && (CLIENT.currentScreen == null || this.autoStorageTimer.hasElapsed(3000L))) {
         if (this.sourceInventorySlot != -1
            && InventoryUtils.canSwapInventorySlots()
            && this.isShulkerOrBackpack(CLIENT.player.getInventory().getStack(8))) {
            InventoryUtils.swapInventorySlotWithHotbar(this.sourceInventorySlot, 8, this.swapModeSetting.getInventorySwapCategory());
         }

         if (this.previousSelectedSlot != -1) {
            InventoryUtils.setSelectedHotbarSlot(this.previousSelectedSlot);
         }

         this.sourceInventorySlot = -1;
         this.previousSelectedSlot = -1;
          this.previousValuableItemCount = this.countPlayerItems();
         this.storageState = PlayerHelper.InventorySwapState.IDLE;
      }
   }
// https://t.me/pasterplace

   private boolean hasOpenContainer() {
      return CLIENT.player.currentScreenHandler != CLIENT.player.playerScreenHandler;
   }
// https://t.me/pasterplace

    private int countPlayerItems() {
      int valuableItemCount = 0;

      for (int inventorySlot = 9; inventorySlot < 36; inventorySlot++) {
         if (this.isValuableItem(CLIENT.player.getInventory().getStack(inventorySlot))) {
            valuableItemCount++;
         }
      }

      return valuableItemCount;
   }
// https://t.me/pasterplace

   private boolean isShulkerOrBackpack(ItemStack stack) {
      if (!InventoryUtils.itemNameContains(stack, "рюкзак")) {
         Item item = stack.getItem();
         if (item instanceof BlockItem) {
             BlockItem blockItem = (BlockItem)item;
            if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }
// https://t.me/pasterplace

   private boolean isValuableItem(ItemStack stack) {
      if (!stack.isEmpty() && !this.isShulkerOrBackpack(stack)) {
         Item item = stack.getItem();
         return this.valuableCategories.isOptionEnabled("Броня") && VALUABLE_ARMOR_ITEMS.contains(item)
            || this.valuableCategories.isOptionEnabled("Оружие") && VALUABLE_WEAPON_ITEMS.contains(item)
            || this.valuableCategories.isOptionEnabled("Книги") && item == Items.ENCHANTED_BOOK
            || this.valuableCategories.isOptionEnabled("Тотемы") && item == Items.TOTEM_OF_UNDYING
            || this.valuableCategories.isOptionEnabled("Головы") && HEAD_ITEMS.contains(item)
            || this.valuableCategories.isOptionEnabled("Расходники") && CONSUMABLE_ITEMS.contains(item)
            || this.valuableCategories.isOptionEnabled("Ресурсы") && VALUABLE_MATERIAL_ITEMS.contains(item);
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   private ItemStack getEquippedBoots() {
      return CLIENT.player.getEquippedStack(EquipmentSlot.FEET);
   }
// https://t.me/pasterplace

   private boolean isBouncyBoots(ItemStack boots) {
      return InventoryUtils.itemNameContains(boots, "Попрыгун");
   }
// https://t.me/pasterplace

   private boolean swapBouncyBoots(boolean wantBouncyBoots) {
      if (!InventoryUtils.canSwapInventorySlots()) {
         return false;
      }

       Predicate<ItemStack> bootsPredicate = candidateBoots -> candidateBoots.isIn(ItemTags.FOOT_ARMOR) && this.isBouncyBoots(candidateBoots) == wantBouncyBoots;
      int bootsInventoryIndex = InventoryUtils.findItemSlot(bootsPredicate);
      if (bootsInventoryIndex == -1) {
         return false;
      }

      Priority.getInstance().scheduleAction(this.swapModeSetting.getInventorySwapCategory(), () -> {
         if (bootsInventoryIndex < 9) {
            InventoryUtils.clickSlot(8, bootsInventoryIndex, SlotActionType.SWAP);
         } else {
            int playerInventorySlot = InventoryUtils.toScreenHandlerSlot(bootsInventoryIndex);
            InventoryUtils.clickSlot(playerInventorySlot, 8, SlotActionType.SWAP);
            InventoryUtils.clickSlot(8, 8, SlotActionType.SWAP);
            InventoryUtils.clickSlot(playerInventorySlot, 8, SlotActionType.SWAP);
         }
      });
      return true;
   }
// https://t.me/pasterplace

   private enum InventorySwapState {
      IDLE,
      OPENING_SHULKER,
      DEPOSITING_ITEMS,
      RESTORING_INVENTORY;

      // $VF: synthetic method
      private static PlayerHelper.InventorySwapState[] II1Il11l111II11IIl() {
         return new PlayerHelper.InventorySwapState[]{IDLE, OPENING_SHULKER, DEPOSITING_ITEMS, RESTORING_INVENTORY};
      }
   }
}

