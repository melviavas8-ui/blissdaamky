package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventElytraVelocity;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ElytraFly extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting flightMode = new MultiOptionSetting("Режим", "Контроль", "Контроль", "ReallyWorld глайд", "Страйф", "Набор высоты");
   private final FloatSetting horizontalSpeed = new FloatSetting("Скорость", 1.6F, 0.2F, 4.0F, 0.05F);
   private final FloatSetting verticalSpeed = new FloatSetting("Вертикальная скорость", 0.8F, 0.1F, 3.0F, 0.05F);
   private final BooleanSetting autoTakeoff = new BooleanSetting("Авто взлёт", true);
   private final BooleanSetting autoFirework = new BooleanSetting("Авто фейерверк", true);
   private final FloatSetting fireworkDelaySeconds = new FloatSetting("Задержка фейерверка", 1.0F, 0.1F, 2.0F, 0.1F);
   private final BooleanSetting jumpToGainHeight = new BooleanSetting("Только при прыжке", false);
   private final Stopwatch fireworkCooldown = new Stopwatch();
   private volatile int positionCorrectionTicks;
   private volatile boolean suppressNextMovementPacket;
   private boolean heightGainActive;
   private boolean flightRotationCaptured;
   private float previousYaw;
   private float previousPitch;
   private double lockedFlightY = Double.NaN;
// https://t.me/pasterplace

   public ElytraFly() {
      super("ElytraFly", "Управляемый полёт на элитрах", ModuleCategory.MOVEMENT);
      this.horizontalSpeed.visibleWhen(() -> this.flightMode.isSelected("Контроль"));
      this.verticalSpeed.visibleWhen(() -> this.flightMode.isSelected("Контроль"));
      this.autoTakeoff.visibleWhen(() -> this.flightMode.isSelected("Страйф"));
      this.autoFirework.visibleWhen(() -> this.flightMode.isSelected("Страйф"));
      this.fireworkDelaySeconds.visibleWhen(() -> this.flightMode.isSelected("Страйф") && this.autoFirework.getValue());
      this.jumpToGainHeight.visibleWhen(() -> this.flightMode.isSelected("Набор высоты"));
      this.addSettings(
         this.flightMode, this.horizontalSpeed, this.verticalSpeed, this.autoTakeoff, this.autoFirework, this.fireworkDelaySeconds, this.jumpToGainHeight
      );
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.flightMode.getValue();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.positionCorrectionTicks = 0;
      this.suppressNextMovementPacket = false;
      this.heightGainActive = false;
      this.flightRotationCaptured = false;
      this.fireworkCooldown.reset();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.restoreFlightRotation();
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (this.flightMode.isSelected("Страйф") && this.autoTakeoff.getValue()) {
            this.tryAutoTakeoff();
         }

         if (CLIENT.player.isGliding()) {
            if (this.positionCorrectionTicks > 0) {
               this.positionCorrectionTicks--;
            }

            switch ((String)this.flightMode.getValue()) {
               case "Контроль":
                  this.handleControlMode();
                  break;
               case "Страйф":
                  this.handleStrafeMode();
                  break;
               case "Набор высоты":
                  this.handleHeightGainMode();
            }
         } else {
            this.heightGainActive = false;
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onMotion(EventMotion eventMotion) {
      if (!eventMotion.isPost()) {
         if (this.flightMode.isSelected("Контроль") && CLIENT.player != null && CLIENT.player.isGliding()) {
            if (!Double.isNaN(this.lockedFlightY) && Math.abs(CLIENT.player.getY() - this.lockedFlightY) < 1.0) {
               CLIENT.player
                  .setPosition(CLIENT.player.getX(), this.lockedFlightY, CLIENT.player.getZ());
            }
         } else {
            this.lockedFlightY = Double.NaN;
         }
      } else {
         this.restoreFlightRotation();
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onPacket(EventPacket eventPacket) {
      if (CLIENT.player != null) {
         if (eventPacket.isReceive() && eventPacket.packet() instanceof PlayerPositionLookS2CPacket) {
            this.positionCorrectionTicks = 2;
            this.suppressNextMovementPacket = true;
         } else {
            if (this.flightMode.isSelected("ReallyWorld глайд") && eventPacket.isSend() && eventPacket.packet() instanceof PlayerMoveC2SPacket) {
               if (CLIENT.player.isGliding() && this.positionCorrectionTicks == 0 && !this.suppressNextMovementPacket) {
                  eventPacket.cancel();
                  PacketSender.l1I1IlllIlI(new OnGroundOnly(true, true));
               }

               this.suppressNextMovementPacket = false;
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onElytraVelocity(EventElytraVelocity eventElytraVelocity) {
      if (this.flightMode.isSelected("ReallyWorld глайд")) {
         Vec3d velocity = eventElytraVelocity.velocity();
         if (this.positionCorrectionTicks < 1) {
            eventElytraVelocity.setVelocity(new Vec3d(velocity.x / 0.99 * 0.3, velocity.y / 0.98 * 0.3, velocity.z / 0.99 * 0.3));
         } else {
            double yawRadians = Math.toRadians(CLIENT.player.getYaw());
            eventElytraVelocity.setVelocity(velocity.add(-Math.sin(yawRadians) * 0.09, 0.03, Math.cos(yawRadians) * 0.09));
         }
      }
   }
// https://t.me/pasterplace

   private void handleControlMode() {
      double verticalVelocity = 0.0;
      if (CLIENT.options.jumpKey.isPressed()) {
         verticalVelocity = this.verticalSpeed.getValue().floatValue();
         this.lockedFlightY = Double.NaN;
      } else if (CLIENT.options.sneakKey.isPressed()) {
         verticalVelocity = -this.verticalSpeed.getValue();
         this.lockedFlightY = Double.NaN;
      } else if (Double.isNaN(this.lockedFlightY)) {
         this.lockedFlightY = CLIENT.player.getY();
      }
// https://t.me/pasterplace

      CLIENT.player.setVelocity(new Vec3d(0.0, verticalVelocity, 0.0));
      if (MovementUtils.isMoving()) {
         MovementUtils.setHorizontalSpeed(this.horizontalSpeed.getValue().floatValue());
      }
   }
// https://t.me/pasterplace

   private void handleStrafeMode() {
      PlayerInput playerInput = CLIENT.player.input.playerInput;
      float forwardInput = playerInput.forward() == playerInput.backward() ? 0.0F : (playerInput.forward() ? 1.0F : -1.0F);
      float strafeInput = playerInput.left() == playerInput.right() ? 0.0F : (playerInput.left() ? 1.0F : -1.0F);
      boolean jumpPressed = CLIENT.options.jumpKey.isPressed();
      boolean sneakPressed = CLIENT.options.sneakKey.isPressed();
      float movementYaw = MovementUtils.isMoving()
         ? MovementUtils.getMovementDirectionRadians() * (180.0F / (float)Math.PI)
         : CLIENT.player.getYaw();
      float pitchOffset = !jumpPressed && !sneakPressed ? 0.0F : (strafeInput + forwardInput > 0.1F ? -45.0F : -90.0F);
      if (sneakPressed) {
         pitchOffset = -pitchOffset;
      }

      this.setFlightRotation(movementYaw, pitchOffset);
      if (this.autoFirework.getValue()
         && !CLIENT.player.isUsingItem()
         && this.fireworkCooldown.hasElapsed((long)(this.fireworkDelaySeconds.getValue() * 1000.0F))
         && InventoryUtils.useItemFromInventory(candidateStack -> candidateStack.isOf(Items.FIREWORK_ROCKET), Priority.PriorityCategory.DEFAULT)) {
         this.fireworkCooldown.reset();
      }
   }
// https://t.me/pasterplace

   private void handleHeightGainMode() {
      if (this.jumpToGainHeight.getValue() && !CLIENT.options.jumpKey.isPressed()) {
         this.heightGainActive = false;
      } else {
         double horizontalSpeed = Math.round(
               Math.hypot(
                     CLIENT.player.getX() - CLIENT.player.lastX,
                     CLIENT.player.getZ() - CLIENT.player.lastZ
                  )
                  * 2000.0
            )
            / 100.0;
         if (this.heightGainActive) {
            if (horizontalSpeed >= 18.9) {
               this.heightGainActive = false;
            }
         } else if (horizontalSpeed < 14.0) {
            this.heightGainActive = true;
         }

         float flightPitch = !this.heightGainActive && CLIENT.player.getY() >= 320.0 ? 0.0F : (this.heightGainActive ? 60.0F : -60.0F);
         this.setFlightRotation(CLIENT.player.getYaw(), flightPitch);
      }
   }
// https://t.me/pasterplace

   private void tryAutoTakeoff() {
      if (!CLIENT.player.isGliding()
         && !CLIENT.player.isInFluid()
         && !CLIENT.player.hasVehicle()
         && CLIENT.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
         if (CLIENT.player.isOnGround()) {
            CLIENT.player.jump();
         } else if (CLIENT.player.checkGliding()) {
            PacketSender.II1Il11l111II11IIl(new ClientCommandC2SPacket(CLIENT.player, Mode.START_FALL_FLYING));
         }
      }
   }
// https://t.me/pasterplace

   private void setFlightRotation(float yaw, float pitch) {
      if (!this.flightRotationCaptured) {
         this.previousYaw = CLIENT.player.getYaw();
         this.previousPitch = CLIENT.player.getPitch();
         this.flightRotationCaptured = true;
      }

      CLIENT.player.setYaw(yaw);
      CLIENT.player.setPitch(MathHelper.clamp(pitch, -90.0F, 90.0F));
   }
// https://t.me/pasterplace

   private void restoreFlightRotation() {
      if (this.flightRotationCaptured && CLIENT.player != null) {
         CLIENT.player.setYaw(this.previousYaw);
         CLIENT.player.setPitch(this.previousPitch);
         this.flightRotationCaptured = false;
      } else {
         this.flightRotationCaptured = false;
      }
   }
}

