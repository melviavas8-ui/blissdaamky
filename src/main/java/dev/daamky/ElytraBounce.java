package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ElytraBounce extends ModuleStructure {
   private static final PlayerInput EMPTY_PLAYER_INPUT = new PlayerInput(false, false, false, false, false, false, false);
   private final MultiOptionSetting mode = new MultiOptionSetting("Режим", "Пакет", "Пакет", "Пакет Old", "Легит", "Прыжок");
   private final BooleanSetting resetSprintWhenUsingItem = new BooleanSetting("Сброс при использовании", false);
   private final Stopwatch glideStartTimer = new Stopwatch();
   private boolean shouldStartGliding;
   private boolean jumpKeyForcedByModule;
   private boolean userJumpKeyWasPressed;
   private boolean elytraEquippedLastTick;
   private int itemUseDelayTicks;

   public ElytraBounce() {
      super("ElytraBounce", "Отскок и прыжки на элитрах по земле", ModuleCategory.MOVEMENT);
      this.resetSprintWhenUsingItem.visibleWhen(() -> this.mode.isSelected("Пакет Old"));
      this.addSettings(this.mode, this.resetSprintWhenUsingItem);
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.shouldStartGliding = false;
      this.itemUseDelayTicks = 0;
      this.userJumpKeyWasPressed = CLIENT.options.jumpKey.isPressed();
      this.glideStartTimer.reset();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.releaseJumpKey();
      if (this.mode.isSelected("Легит") && this.hasActiveClientWorld() && !this.userJumpKeyWasPressed) {
         PacketSender.II1Il11l111II11IIl(new PlayerInputC2SPacket(EMPTY_PLAYER_INPUT));
         CLIENT.options.jumpKey.setPressed(false);
      }

      this.elytraEquippedLastTick = false;
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()
         && !CLIENT.player.isInFluid()
         && !CLIENT.player.isSwimming()
         && !CLIENT.player.hasVehicle()
         && !CLIENT.player.getAbilities().flying) {
         if (this.mode.isSelected("Легит")) {
            this.tickLegitMode();
         } else if (this.hasElytraEquipped()) {
            if (!this.mode.isSelected("Прыжок")) {
               this.releaseJumpKey();
            }

            switch ((String)this.mode.getValue()) {
               case "Пакет":
                  this.tickPacketMode();
                  break;
               case "Пакет Old":
                  this.tickLegacyPacketMode();
                  break;
               case "Прыжок":
                  this.tickJumpMode();
            }
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onMotion(EventMotion eventMotion) {
      if (eventMotion.isPre() && this.mode.isSelected("Прыжок") && this.hasActiveClientWorld() && CLIENT.player.isGliding()) {
         if (Math.abs(CLIENT.player.getPitch()) > 30.0F) {
            CLIENT.player.setPitch(0.0F);
            eventMotion.setPitch(0.0F);
         }

         CLIENT.player.headYaw = CLIENT.player.getYaw();
         CLIENT.player.bodyYaw = CLIENT.player.getYaw();
      }
   }

   private void tickPacketMode() {
      if (this.hasJumpClearance()) {
         if (CLIENT.player.isOnGround()) {
            if (MovementUtils.isMoving() && !CLIENT.player.horizontalCollision) {
               CLIENT.player.setSprinting(true);
            }

            CLIENT.player.jump();
         } else if (!CLIENT.player.isGliding()) {
            this.startGliding();
         }
      }
   }

   private void tickLegacyPacketMode() {
      if (CLIENT.player.isUsingItem() && this.resetSprintWhenUsingItem.getValue()) {
         if (CLIENT.player.isSprinting() && !CLIENT.player.horizontalCollision) {
            PacketSender.II1Il11l111II11IIl(new ClientCommandC2SPacket(CLIENT.player, Mode.STOP_SPRINTING));
            CLIENT.player.setSprinting(false);
         }

         this.itemUseDelayTicks = 5;
      } else if (this.itemUseDelayTicks > 0) {
         this.itemUseDelayTicks--;
         return;
      }

      if ((!this.resetSprintWhenUsingItem.getValue() || !CLIENT.player.isUsingItem()) && MovementUtils.isMoving()) {
         if (CLIENT.player.isOnGround() && this.hasJumpClearance()) {
            if (!CLIENT.player.horizontalCollision) {
               CLIENT.player.setSprinting(true);
            }

            CLIENT.player.jump();
         } else if (!CLIENT.player.isGliding()) {
            this.startGliding();
         }
      }
   }

   private void tickLegitMode() {
      if (this.hasJumpClearance()) {
         if (CLIENT.player.isOnGround()) {
            this.shouldStartGliding = true;
         }

         boolean elytraEquipped = this.hasElytraEquipped();
         if (this.elytraEquippedLastTick && !elytraEquipped) {
            PacketSender.II1Il11l111II11IIl(new PlayerInputC2SPacket(EMPTY_PLAYER_INPUT));
            CLIENT.options.jumpKey.setPressed(false);
         }

         if (!this.elytraEquippedLastTick && elytraEquipped) {
            this.userJumpKeyWasPressed = CLIENT.options.jumpKey.isPressed();
         }

         this.elytraEquippedLastTick = elytraEquipped;
         if (elytraEquipped
            && this.shouldStartGliding
            && !CLIENT.player.isGliding()
            && CLIENT.player.getPitch() > 0.0F
            && this.glideStartTimer.hasElapsed(800L)
            && this.canStartGliding()) {
            this.glideStartTimer.reset();
            this.shouldStartGliding = false;
            PacketSender.II1Il11l111II11IIl(new PlayerInputC2SPacket(EMPTY_PLAYER_INPUT));
            CLIENT.options.jumpKey.setPressed(false);
            PacketSender.II1Il11l111II11IIl(new ClientCommandC2SPacket(CLIENT.player, Mode.START_FALL_FLYING));
            if (!this.userJumpKeyWasPressed) {
               CLIENT.options.jumpKey.setPressed(true);
            }

            PacketSender.II1Il11l111II11IIl(new PlayerInputC2SPacket(CLIENT.player.input.playerInput));
         }
      }
   }

   private void tickJumpMode() {
      if (CLIENT.player.isOnGround()) {
         this.releaseJumpKey();
         if (this.hasJumpClearance()) {
            CLIENT.player.jump();
         }
      } else {
         CLIENT.options.jumpKey.setPressed(true);
         this.jumpKeyForcedByModule = true;
         if (!CLIENT.player.isGliding()) {
            this.startGliding();
         } else {
            Vec3d velocity = CLIENT.player.getVelocity();
            boolean hasVerticalMotion = velocity.y > 0.08 || CLIENT.player.fallDistance > 0.1;
            boolean horizontalSpeedIsLow = Math.abs(velocity.x) <= 0.01 && Math.abs(velocity.z) <= 0.01;
            if (hasVerticalMotion && horizontalSpeedIsLow) {
               CLIENT.player.setVelocity(0.0, velocity.y + 0.03062, 0.0);
            }
         }
      }
   }

   private void releaseJumpKey() {
      if (this.jumpKeyForcedByModule) {
         CLIENT.options.jumpKey.setPressed(false);
         this.jumpKeyForcedByModule = false;
      }
   }

   private boolean canStartGliding() {
      ItemStack chestArmor = CLIENT.player.getEquippedStack(EquipmentSlot.CHEST);
      return chestArmor.getDamage() < chestArmor.getMaxDamage() - 1
         && !CLIENT.player.isClimbing()
         && !CLIENT.player.hasStatusEffect(StatusEffects.LEVITATION)
         && !CLIENT.player.isSleeping()
         && !CLIENT.player.isInLava();
   }

   private void startGliding() {
      if (this.canStartGliding()) {
         PacketSender.II1Il11l111II11IIl(new ClientCommandC2SPacket(CLIENT.player, Mode.START_FALL_FLYING));
         CLIENT.player.startGliding();
      }
   }

   private boolean hasElytraEquipped() {
      return CLIENT.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
   }

   private boolean hasJumpClearance() {
      BlockPos playerBlockPos = CLIENT.player.getBlockPos();

      for (int verticalOffset = 1; verticalOffset <= 2; verticalOffset++) {
         BlockPos clearanceBlockPos = playerBlockPos.up(verticalOffset);
         if (!CLIENT.world.getBlockState(clearanceBlockPos).getCollisionShape(CLIENT.world, clearanceBlockPos).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}

