package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ElytraHelper extends ModuleStructure {
   private static final int MIN_TAKEOFF_TICKS = 6;
   private static final int MAX_TAKEOFF_TICKS = 8;
   private static final List<Item> CHESTPLATE_PRIORITY = List.of(
      Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.COPPER_CHESTPLATE, Items.LEATHER_CHESTPLATE
   );
   private final ReallyWorld serverSwapRules = new ReallyWorld();
   private final IntSetting chestplateSwapKey = new IntSetting("Свап элитры/нагрудника", 0);
   private final IntSetting fireworkKey = new IntSetting("Бинд фейерверка", 0);
   private final IntSetting takeoffKey = new IntSetting("Бинд взлёта", 0);
   private final BooleanSetting autoDeploy = new BooleanSetting("Авто-раскрытие", false);
   private final BooleanSetting equipChestplateOnLanding = new BooleanSetting("Нагрудник при приземлении", false);
   private final BooleanSetting autoFirework = new BooleanSetting("Авто-фейерверк", false);
   private final FloatSetting fireworkDelay = new FloatSetting("Задержка фейерверка", 1500.0F, 300.0F, 5000.0F, 100.0F, "мс");
   private final Stopwatch fireworkDelayTimer = new Stopwatch();
   private ElytraHelper.FlightState flightState = ElytraHelper.FlightState.IDLE;
   private int stateTicks;
   private int stateDelayTicks;
   private int glideStartAttempts;
   private int glidingTicks;
   private boolean wasGliding;

   public ElytraHelper() {
      super("ElytraHelper", "Свап элитр, фейерверки по бинду, взлёт и авто-буст", ModuleCategory.MOVEMENT);
      this.fireworkDelay.visibleWhen(this.autoFirework::getValue);
      this.addSettings(
         this.chestplateSwapKey,
         this.fireworkKey,
         this.takeoffKey,
         this.autoDeploy,
         this.equipChestplateOnLanding,
         this.autoFirework,
         this.fireworkDelay,
         this.serverSwapRules
      );
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.flightState = ElytraHelper.FlightState.IDLE;
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.flightState = ElytraHelper.FlightState.IDLE;
      this.wasGliding = false;
      this.glidingTicks = 0;
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onKey(EventKey eventKey) {
      if (this.hasActiveClientWorld() && CLIENT.currentScreen == null) {
         int keyCode = dev.daamky.KeybindManager.encodeKey(eventKey.key(), eventKey.isMouse());
         if (this.chestplateSwapKey.matchesKeyCode(keyCode)) {
            this.equipElytra();
         }

         if (this.fireworkKey.matchesKeyCode(keyCode) && CLIENT.player.isGliding()) {
            this.useFirework();
         }

         if (this.takeoffKey.matchesKeyCode(keyCode) && this.flightState == ElytraHelper.FlightState.IDLE) {
            this.startTakeoffSequence();
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         boolean isGliding = CLIENT.player.isGliding();
         this.glidingTicks = !isGliding ? 0 : this.glidingTicks + 1;
         if (this.equipChestplateOnLanding.getValue() && this.wasGliding && !isGliding && CLIENT.player.isOnGround() && this.isWearingElytra()) {
            this.equipElytra();
         }

         this.wasGliding = isGliding;
         if (this.flightState == ElytraHelper.FlightState.IDLE) {
            if (this.autoDeploy.getValue()
               && this.hasUsableElytraEquipped()
               && !CLIENT.player.isOnGround()
               && !isGliding
               && !CLIENT.player.isTouchingWater()
               && !CLIENT.player.isInLava()
               && !CLIENT.player.isClimbing()
               && !CLIENT.player.getAbilities().flying
               && CLIENT.player.getVelocity().y < -0.08
               && CLIENT.player.fallDistance > 1.0) {
               this.tryStartGliding();
            }

            if (this.autoFirework.getValue()
               && isGliding
               && CLIENT.options.forwardKey.isPressed()
               && this.fireworkDelayTimer.hasElapsed(this.fireworkDelay.getValue().longValue())) {
               this.useFirework();
            }
         } else {
            this.tickFlightSequence();
         }
      }
   }

   private void startTakeoffSequence() {
      if (!CLIENT.player.isGliding()
         && !CLIENT.player.isTouchingWater()
         && !CLIENT.player.isClimbing()
         && !CLIENT.player.getAbilities().flying) {
         if (InventoryUtils.findItemSlot(candidateStack -> candidateStack.isOf(Items.FIREWORK_ROCKET)) != -1) {
            if (!this.isWearingElytra()) {
               if (InventoryUtils.findItemSlot(this::isUsableElytra) == -1) {
                  return;
               }

               this.equipElytra();
            }

            this.flightState = ElytraHelper.FlightState.PREPARING_TAKEOFF;
            this.stateTicks = 0;
            this.glideStartAttempts = 0;
         }
      }
   }

   private void tickFlightSequence() {
      this.stateTicks++;
      switch (this.flightState) {
         case PREPARING_TAKEOFF:
            if (CLIENT.player.isOnGround()) {
               if (this.stateTicks > 6) {
                  this.flightState = ElytraHelper.FlightState.IDLE;
                  return;
               }

               if (this.isWearingElytra()) {
                  CLIENT.player.jump();
               }

               return;
            }

            this.flightState = ElytraHelper.FlightState.WAITING_TO_GLIDE;
            this.stateTicks = 0;
            this.stateDelayTicks = ThreadLocalRandom.current().nextInt(3, 7);
            break;
         case WAITING_TO_GLIDE:
            if (this.stateTicks >= this.stateDelayTicks) {
               this.flightState = ElytraHelper.FlightState.STARTING_GLIDE;
               this.stateTicks = 0;
            }
            break;
         case STARTING_GLIDE:
            if (CLIENT.player.isGliding()) {
               this.flightState = ElytraHelper.FlightState.BOOSTING;
               this.stateTicks = 0;
               this.stateDelayTicks = ThreadLocalRandom.current().nextInt(3, 9);
               return;
            }

            if (CLIENT.player.isOnGround() || this.glideStartAttempts >= 3) {
               this.flightState = ElytraHelper.FlightState.IDLE;
               return;
            }

            this.glideStartAttempts++;
            this.tryStartGliding();
            break;
         case BOOSTING:
            if (!CLIENT.player.isGliding()) {
               this.flightState = ElytraHelper.FlightState.IDLE;
               return;
            }

            if (this.stateTicks >= this.stateDelayTicks) {
               this.useFirework();
               this.flightState = ElytraHelper.FlightState.IDLE;
            }
            break;
         default:
            this.flightState = ElytraHelper.FlightState.IDLE;
      }
   }

   private void tryStartGliding() {
      if (this.hasUsableElytraEquipped()) {
         if (CLIENT.player.checkGliding()) {
            CLIENT.player.networkHandler.sendPacket(new ClientCommandC2SPacket(CLIENT.player, Mode.START_FALL_FLYING));
         }
      }
   }

   private void useFirework() {
      if (InventoryUtils.useItemFromInventory(rocketStack -> rocketStack.isOf(Items.FIREWORK_ROCKET), this.serverSwapRules.getInventorySwapCategory())) {
         this.fireworkDelayTimer.reset();
      }
   }

   private boolean isWearingElytra() {
      return CLIENT.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
   }

   private boolean hasUsableElytraEquipped() {
      return this.isUsableElytra(CLIENT.player.getEquippedStack(EquipmentSlot.CHEST));
   }

   private boolean isUsableElytra(ItemStack candidateElytra) {
      return candidateElytra.isOf(Items.ELYTRA) && candidateElytra.getDamage() < candidateElytra.getMaxDamage() - 1;
   }

   private int findPreferredChestplateSlot() {
      for (Object fireworkItem : CHESTPLATE_PRIORITY) {
         int inventorySlot = InventoryUtils.findItemSlot(candidateStack -> candidateStack.isOf((Item)fireworkItem));
         if (inventorySlot != -1) {
            return inventorySlot;
         }
      }

      return -1;
   }

   private void equipElytra() {
      if (InventoryUtils.canSwapInventorySlots()) {
         int elytraSlot = this.isWearingElytra() ? this.findPreferredChestplateSlot() : InventoryUtils.findItemSlot(this::isUsableElytra);
         if (elytraSlot != -1) {
            Priority.getInstance().scheduleAction(this.serverSwapRules.getInventorySwapCategory(), () -> {
               if (elytraSlot < 9) {
                  InventoryUtils.clickSlot(6, elytraSlot, SlotActionType.SWAP);
               } else {
                  int var1x = InventoryUtils.toScreenHandlerSlot(elytraSlot);
                  InventoryUtils.clickSlot(var1x, 8, SlotActionType.SWAP);
                  InventoryUtils.clickSlot(6, 8, SlotActionType.SWAP);
                  InventoryUtils.clickSlot(var1x, 8, SlotActionType.SWAP);
               }
            });
         }
      }
   }

   private enum FlightState {
      IDLE,
      PREPARING_TAKEOFF,
      WAITING_TO_GLIDE,
      STARTING_GLIDE,
      BOOSTING;

      // $VF: synthetic method
      private static ElytraHelper.FlightState[] II1Il11l111II11IIl() {
         return new ElytraHelper.FlightState[]{IDLE, PREPARING_TAKEOFF, WAITING_TO_GLIDE, STARTING_GLIDE, BOOSTING};
      }
   }
}

