package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class WindHop extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting mode = new MultiOptionSetting(
      "Режим", "При использовании", "При использовании", "По бинду", "Автоматический"
   );
   private final IntSetting keybind = new IntSetting("Бинд", 0);
   private boolean windChargeRequested;
   private boolean usingRotation;
   private Hand pendingHand;
   private int pendingHotbarSlot = -1;
   private int rotationWaitTicks;
// https://t.me/pasterplace

   public WindHop() {
      super("WindHop", "Бросает заряд ветра под ноги", ModuleCategory.MOVEMENT);
      this.keybind.visibleWhen(() -> this.mode.isSelected("По бинду"));
      this.addSettings(this.mode, this.keybind);
   }
// https://t.me/pasterplace

   public static WindHop getInstance() {
      return ModuleRegistry.getInstance().getModuleByClass(WindHop.class);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.windChargeRequested = false;
      this.clearPendingUse();
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }
// https://t.me/pasterplace

   private void clearPendingUse() {
      this.pendingHand = null;
      this.pendingHotbarSlot = -1;
      this.rotationWaitTicks = 0;
   }
// https://t.me/pasterplace

   public boolean interceptWindChargeUse(PlayerEntity playerEntity, Hand hand) {
      if (this.isEnabled()
         && !this.usingRotation
         && this.mode.isSelected("При использовании")
         && playerEntity == CLIENT.player
         && this.pendingHand == null) {
         ItemStack windChargeStack = playerEntity.getStackInHand(hand);
         if (windChargeStack.isOf(Items.WIND_CHARGE) && !playerEntity.isTouchingWater() && !playerEntity.getItemCooldownManager().isCoolingDown(windChargeStack)) {
            this.pendingHand = hand;
            this.pendingHotbarSlot = hand == Hand.MAIN_HAND ? InventoryUtils.getSelectedHotbarSlot() : -1;
            this.rotationWaitTicks = 0;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onKey(EventKey eventKey) {
      if (this.hasActiveClientWorld()
         && this.mode.isSelected("По бинду")
         && CLIENT.currentScreen == null
         && this.keybind.matchesKeyCode(dev.daamky.KeybindManager.encodeKey(eventKey.key(), eventKey.isMouse()))) {
         this.windChargeRequested = true;
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (this.pendingHand != null) {
            this.performPendingWindChargeUse();
         } else if (!this.mode.isSelected("При использовании")
            && !CLIENT.player.isTouchingWater()
            && (!this.mode.isSelected("Автоматический") || CLIENT.player.isOnGround())) {
            if (!this.mode.isSelected("По бинду") || this.windChargeRequested) {
               this.windChargeRequested = false;
               if (!CLIENT.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.WIND_CHARGE))) {
                  int windChargeSlot;
                  if ((windChargeSlot = InventoryUtils.findHotbarSlot(candidateStack -> candidateStack.isOf(Items.WIND_CHARGE))) != -1) {
                     this.pendingHand = Hand.MAIN_HAND;
                     this.pendingHotbarSlot = windChargeSlot;
                     this.rotationWaitTicks = 0;
                  }
               }
            }
         }
      } else {
         this.clearPendingUse();
      }
   }
// https://t.me/pasterplace

   private void performPendingWindChargeUse() {
      this.rotationWaitTicks++;
      if (this.rotationWaitTicks <= 10 && !CLIENT.player.isTouchingWater()) {
         Rotation lIll1Il1lll111l11l1I1llllI2 = Rotation.of(
            CLIENT.player.getYaw(), 90.0F
         );
         RotationPriorityManager priority2 = RotationPriorityManager.getInstance();
         if (priority2.isFocusedBy(this) && !(priority2.getLastMotionRotation().getPitch() < 89.0F)) {
            Rotation lIll1Il1lll111l11l1I1llllI22 = priority2.getLastMotionRotation();
            this.usingRotation = true;
// https://t.me/pasterplace

            try {
               if (this.pendingHand == Hand.OFF_HAND) {
                  if (CLIENT.player.getOffHandStack().isOf(Items.WIND_CHARGE)) {
                     InventoryUtils.interactItemWithRotation(Hand.OFF_HAND, lIll1Il1lll111l11l1I1llllI22.getYaw(), lIll1Il1lll111l11l1I1llllI22.getPitch());
                  }
               } else if (this.pendingHotbarSlot != -1
                  && CLIENT.player.getInventory().getStack(this.pendingHotbarSlot).isOf(Items.WIND_CHARGE)) {
                  InventoryUtils.useHotbarSlot(
                     this.pendingHotbarSlot, lIll1Il1lll111l11l1I1llllI22.getYaw(), lIll1Il1lll111l11l1I1llllI22.getPitch()
                  );
               }
            } finally {
               this.usingRotation = false;
               this.clearPendingUse();
            }
         } else {
            priority2.requestRotation(lIll1Il1lll111l11l1I1llllI2, FocusMode.FREE, 100, this);
         }
      } else {
         this.clearPendingUse();
      }
   }
}

