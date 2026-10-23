package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoTotem extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final ReallyWorld inventorySwapProfile = new ReallyWorld();
   private final FloatSetting healthThreshold = new FloatSetting("Здоровье", 6.0F, 1.0F, 20.0F, 0.5F, "HP");
   private final FloatSetting elytraHealthThreshold = new FloatSetting("Здоровье на элитрах", 10.0F, 1.0F, 20.0F, 0.5F, "HP");
   private final MultiToggleSetting enabledThreats = new MultiToggleSetting("Угрозы")
      .addOption("Кристаллы", true)
      .addOption("Якорь", true)
      .addOption("Динамит", true)
      .addOption("Падение", true)
      .addOption("Пустота", true);
   private final BooleanSetting keepCurrentOffhandItem = new BooleanSetting("Не убирать тотем", false);
   private final BooleanSetting restoreReplacedItem = new BooleanSetting("Возвращать предмет", true);
   private final BooleanSetting avoidSwappingInMenus = new BooleanSetting("Не свапать в меню", true);
   private final FloatSetting delay = new FloatSetting("Задержка", 50.0F, 0.0F, 500.0F, 10.0F, "мс");
   private final Stopwatch swapStopwatch = new Stopwatch();
   private ItemStack previousOffhandItem = ItemStack.EMPTY;
// https://t.me/pasterplace

   public AutoTotem() {
      super("AutoTotem", "Берёт тотем во вторую руку при опасности", ModuleCategory.COMBAT);
      this.addSettings(
         this.healthThreshold, this.elytraHealthThreshold, this.enabledThreats, this.keepCurrentOffhandItem, this.restoreReplacedItem, this.avoidSwappingInMenus, this.delay, this.inventorySwapProfile
      );
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return String.valueOf(InventoryUtils.countMatchingItems(stack -> stack.isOf(Items.TOTEM_OF_UNDYING)));
   }
// https://t.me/pasterplace

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && !CLIENT.player.isCreative()) {
         if (!this.avoidSwappingInMenus.getValue()
            || !(CLIENT.currentScreen instanceof HandledScreen)
            || CLIENT.currentScreen instanceof InventoryScreen) {
            if (InventoryUtils.canSwapInventorySlots() && this.swapStopwatch.hasElapsed(this.delay.getValue().longValue())) {
               boolean offhandHasTotem = CLIENT.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
               boolean totemRequired = this.isTotemRequired();
               if (totemRequired && !offhandHasTotem) {
                  int totemSlot = InventoryUtils.findItemSlot(itemStackx -> itemStackx.isOf(Items.TOTEM_OF_UNDYING));
                  if (totemSlot != -1) {
                     ItemStack replacedOffhandStack = CLIENT.player.getOffHandStack();
                     if (this.previousOffhandItem.isEmpty() && !replacedOffhandStack.isEmpty()) {
                        this.previousOffhandItem = replacedOffhandStack.copy();
                     }

                     InventoryUtils.swapInventorySlotWithOffhand(totemSlot, this.inventorySwapProfile.getInventorySwapCategory());
                     this.swapStopwatch.reset();
                  }
               } else if (!totemRequired && !this.previousOffhandItem.isEmpty()) {
                  if (this.keepCurrentOffhandItem.getValue() || !this.restoreReplacedItem.getValue()) {
                     this.previousOffhandItem = ItemStack.EMPTY;
                  } else if (!offhandHasTotem && !CLIENT.player.getOffHandStack().isEmpty()) {
                     this.previousOffhandItem = ItemStack.EMPTY;
                  } else {
                     int restoreSlot = InventoryUtils.findItemSlot(itemStackx -> ItemStack.areItemsAndComponentsEqual(itemStackx, this.previousOffhandItem));
                     if (restoreSlot != -1) {
                        InventoryUtils.swapInventorySlotWithOffhand(restoreSlot, this.inventorySwapProfile.getInventorySwapCategory());
                        this.swapStopwatch.reset();
                     }

                     this.previousOffhandItem = ItemStack.EMPTY;
                  }
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private boolean isTotemRequired() {
      float currentHealth = CombatUtils.getEffectiveHealth((net.minecraft.entity.LivingEntity)CLIENT.player);
      boolean wearingElytra = CLIENT.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
      if (!(currentHealth <= this.healthThreshold.getValue()) && (!wearingElytra || !(currentHealth <= this.elytraHealthThreshold.getValue()))) {
         if (this.enabledThreats.isOptionEnabled("Падение")
            && CLIENT.player.fallDistance > 10.0
            && !CLIENT.player.isGliding()
            && !CLIENT.player.isTouchingWater()) {
            float fallDamageEstimate = (float)CLIENT.player.fallDistance - 3.0F;
            if (currentHealth - fallDamageEstimate <= 4.0F) {
               return true;
            }
         }

         if (this.enabledThreats.isOptionEnabled("Пустота") && CLIENT.player.getY() < CLIENT.world.getBottomY() + 2) {
            return true;
         }

          for (Entity nearbyEntity : CLIENT.world.getEntities()) {
            double entityDistance = nearbyEntity.distanceTo(CLIENT.player);
            if (this.enabledThreats.isOptionEnabled("Кристаллы") && nearbyEntity instanceof EndCrystalEntity && entityDistance < 6.0) {
               return true;
            }

            if (this.enabledThreats.isOptionEnabled("Динамит") && (nearbyEntity instanceof TntEntity || nearbyEntity instanceof TntMinecartEntity) && entityDistance < 6.0) {
               return true;
            }
         }

         if (this.enabledThreats.isOptionEnabled("Якорь")) {
            BlockPos playerBlockPosition = CLIENT.player.getBlockPos();
// https://t.me/pasterplace

            for (int horizontalOffsetX = -4; horizontalOffsetX <= 4; horizontalOffsetX++) {
               for (int verticalOffset = -2; verticalOffset <= 3; verticalOffset++) {
                  for (int horizontalOffsetZ = -4; horizontalOffsetZ <= 4; horizontalOffsetZ++) {
                     if (CLIENT.world.getBlockState(playerBlockPosition.add(horizontalOffsetX, verticalOffset, horizontalOffsetZ)).isOf(Blocks.RESPAWN_ANCHOR)) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.previousOffhandItem = ItemStack.EMPTY;
   }
}

