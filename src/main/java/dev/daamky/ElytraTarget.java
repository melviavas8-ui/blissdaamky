package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import dev.daamky.core.event.impl.EventInput;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import dev.daamky.mixin.client.MultiPlayerGameModeAccessor;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ElytraTarget extends ModuleStructure {
   private static final Stopwatch fireworkSwapStopwatch = new Stopwatch();
   private final BooleanSetting autoFireworks = new BooleanSetting("Автофейерверки", true);
   private final BooleanSetting smartFireworks = new BooleanSetting("Умные фейерверки", true);
   private final FloatSetting fireworkHotbarSlot = new FloatSetting("Слот фейерверка", 7.0F, 1.0F, 9.0F, 1.0F);
   private final FloatSetting fireworkDelaySeconds = new FloatSetting("Задержка фейерверка", 0.45F, 0.25F, 3.0F, 0.05F, "с");
   private final FloatSetting maximumTargetDistance = new FloatSetting("Дистанция", 24.0F, 6.0F, 50.0F, 1.0F, "бл.");
   private final MultiOptionSetting predictionMode = new MultiOptionSetting("Предикт", "Позиция сервера", "Движение", "Позиция сервера");
   private final FloatSetting movementPredictionLead = new FloatSetting("Опережение", 3.0F, 0.0F, 5.0F, 0.1F, "т.");
   private final BooleanSetting freezeInAir = new BooleanSetting("Заморозка в воздухе", false);
   private final FloatSetting freezeDistance = new FloatSetting("Дистанция заморозки", 3.0F, 1.0F, 10.0F, 0.1F, "бл.");
   private LivingEntity targetEntity;
   private int fireworkSwapState;
   private int lastAttackAge = -1;
   private boolean attackTriggeredFirework;
   private int previousHotbarSlot = -1;
   private Vec3d targetPredictionPosition = Vec3d.ZERO;
   private double freezePeakY = Double.NaN;
   private boolean isFrozenInAir;
   private Vec3d freezePosition = Vec3d.ZERO;

   public ElytraTarget() {
      super("ElytraTarget", "Наводится на цели на элитрах с предиктом", ModuleCategory.COMBAT);
      this.smartFireworks.visibleWhen(this.autoFireworks::getValue);
      this.fireworkHotbarSlot.visibleWhen(this.autoFireworks::getValue);
      this.fireworkDelaySeconds.visibleWhen(() -> this.autoFireworks.getValue() && this.smartFireworks.getValue());
      this.movementPredictionLead.visibleWhen(() -> this.predictionMode.isSelected("Движение"));
      this.freezeDistance.visibleWhen(this.freezeInAir::getValue);
      this.addSettings(
         this.autoFireworks,
         this.smartFireworks,
         this.fireworkHotbarSlot,
         this.fireworkDelaySeconds,
         this.maximumTargetDistance,
         this.predictionMode,
         this.movementPredictionLead,
         this.freezeInAir,
         this.freezeDistance
      );
   }

   public static ElytraTarget getInstance() {
      return ModuleRegistry.getInstance().getModuleByClass(ElytraTarget.class);
   }

   public static boolean isElytraTargetModuleEnabled() {
      ElytraTarget elytraTargetModule = getInstance();
      return elytraTargetModule != null && elytraTargetModule.isEnabled();
   }

   public float getTargetRange() {
      return this.maximumTargetDistance.getValue();
   }

   public LivingEntity getTarget() {
      return this.targetEntity;
   }

   public Vec3d getTargetPosition() {
      return this.targetPredictionPosition;
   }

   public boolean isTargetingWhileGliding() {
      return this.isEnabled()
         && this.targetEntity != null
         && CLIENT.player != null
         && CLIENT.player.isGliding()
         && RotationPriorityManager.getInstance().isFocusedBy(this);
   }

   private static boolean isSilentElytraRotationActive() {
      return CLIENT.player != null && CLIENT.player.isGliding() && RotationPriorityManager.getInstance().hasCurrentRotation();
   }

   public static Vec3d getSilentRotationDirection(Vec3d requestedDirection) {
      if (!isSilentElytraRotationActive()) {
         return requestedDirection;
      }

      Rotation currentRotation = RotationPriorityManager.getInstance().getCurrentRotation();
      return Vec3d.fromPolar(currentRotation.getPitch(), currentRotation.getYaw());
   }

   public static float getSilentRotationPitch(float requestedPitch) {
      return !isSilentElytraRotationActive() ? requestedPitch : RotationPriorityManager.getInstance().getCurrentRotation().getPitch();
   }

   public boolean isCurrentTarget(LivingEntity candidateTarget) {
      return this.isEnabled()
         && candidateTarget != null
         && candidateTarget == this.targetEntity
         && CLIENT.player != null
         && CLIENT.player.isGliding();
   }

   public Vec3d getPredictedPositionOrCurrent(LivingEntity target) {
      Vec3d predictedPosition = this.predictTargetPosition(target);
      return predictedPosition != null ? predictedPosition : target.getEntityPos();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.clearTarget();
      this.resetFreezeState();
      this.restorePreviousHotbarSlot();
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.setEnabled(false);
   }

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      if (!this.hasActiveClientWorld()) {
         this.clearTarget();
      } else {
         boolean attackedThisTick = this.wasAttackedThisTick();
         if (!attackedThisTick) {
            this.advanceFireworkSwap();
         }

         Aura auraModule = Aura.getInstance();
         if (auraModule != null && auraModule.isEnabled() && this.hasElytraEquipped()) {
            this.targetEntity = this.getAuraPlayerTarget(auraModule);
            this.updateRotationRequest(this.targetEntity);
            if (this.autoFireworks.getValue() && !attackedThisTick) {
               if (this.attackTriggeredFirework) {
                  this.attackTriggeredFirework = false;
                  double targetDistance = this.targetEntity != null ? CLIENT.player.distanceTo(this.targetEntity) : Double.MAX_VALUE;
                  if (fireworkSwapStopwatch.hasElapsed(this.getFireworkDelayMillis(this.targetEntity, targetDistance))) {
                     this.useFirework();
                  }
               } else {
                  this.trySmartFirework(this.targetEntity);
               }
            }

            if (this.targetEntity != null) {
               this.requestStartGliding();
            }
         } else {
            this.clearTarget();
            RotationPriorityManager.getInstance().clearRotationRequest(this);
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onAttack(EventAttack eventAttack) {
      if (this.hasActiveClientWorld()) {
         this.lastAttackAge = CLIENT.player.age;
         if (eventAttack.isPost() && this.hasElytraEquipped() && this.autoFireworks.getValue()) {
            this.attackTriggeredFirework = true;
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void handleFreezeInput(EventInput eventInput) {
      if (this.freezeInAir.getValue()
         && this.hasActiveClientWorld()
         && CLIENT.player.isGliding()
         && this.targetEntity != null
         && !(CLIENT.player.distanceTo(this.targetEntity) >= this.freezeDistance.getValue())) {
         double playerY = CLIENT.player.getY();
         if (!CLIENT.player.isOnGround()) {
            if (!this.isFrozenInAir) {
               if (Double.isNaN(this.freezePeakY) || playerY > this.freezePeakY) {
                  this.freezePeakY = playerY;
               } else if (playerY < this.freezePeakY) {
                  this.isFrozenInAir = true;
                  this.freezePosition = CLIENT.player.getEntityPos();
                  this.freezePeakY = playerY;
               }
            }
         } else {
            this.freezePeakY = Double.NaN;
            this.isFrozenInAir = false;
         }

         if (this.isFrozenInAir) {
            eventInput.setForward(0.0F);
            eventInput.setStrafe(0.0F);
            CLIENT.player.setVelocity(Vec3d.ZERO);
            CLIENT.player
               .setPosition(this.freezePosition.x, this.freezePosition.y, this.freezePosition.z);
         }
      } else {
         this.resetFreezeState();
      }
   }

   private LivingEntity getAuraPlayerTarget(Aura auraModule) {
      if (auraModule == null) {
         return null;
      }

      LivingEntity target = auraModule.getTarget();
      return target instanceof PlayerEntity ? target : null;
   }

   private void updateRotationRequest(LivingEntity target) {
      if (target != null && CLIENT.player.isGliding()) {
         Vec3d predictedPosition = this.predictTargetPosition(target);
         if (predictedPosition == null) {
            this.targetPredictionPosition = Vec3d.ZERO;
         } else {
            this.targetPredictionPosition = predictedPosition;
            Rotation currentRotation = RotationPriorityManager.getInstance().getCurrentRotation();
            Rotation desiredRotation = RotationUtils.lookAt(CLIENT.player.getEyePos(), predictedPosition);
            Rotation adjustedRotation = new Rotation(
               currentRotation.getYaw()
                  + MathHelper.wrapDegrees(desiredRotation.getYaw() - currentRotation.getYaw()),
               desiredRotation.getPitch()
            );
            RotationPriorityManager.getInstance().requestRotation(adjustedRotation, FocusMode.FREE, 180.0F, 3, false, this);
         }
      } else {
         this.targetPredictionPosition = Vec3d.ZERO;
         RotationPriorityManager.getInstance().clearRotationRequest(this);
      }
   }

   private Vec3d predictTargetPosition(LivingEntity target) {
      if (target == null) {
         return null;
      }

      if (this.predictionMode.isSelected("Движение")) {
         if (target instanceof PlayerEntity) {
            Vec3d targetMotion = target.getEntityPos().subtract(target.lastX, target.lastY, target.lastZ);
            return target.getEyePos().add(targetMotion.multiply(this.movementPredictionLead.getValue().floatValue()));
         } else {
            return target.getEyePos();
         }
      } else {
         return target.getTrackedPosition().getPos().add(0.0, target.getHeight() * 0.5, 0.0);
      }
   }

   private void trySmartFirework(LivingEntity target) {
      if (CLIENT.player.isGliding()) {
         double targetDistance = target == null ? Double.MAX_VALUE : CLIENT.player.distanceTo(target);
         boolean targetIsFasterPlayer = target instanceof PlayerEntity playerTarget && this.isTargetMovingFaster(playerTarget);
         boolean playerBelowTarget = CLIENT.player.getY()
            < (target != null ? target.getY() + 3.0 : CLIENT.player.getY() + 5.0);
         long fireworkDelayMillis = this.getFireworkDelayMillis(target, targetDistance);
         boolean shouldUseFirework = this.smartFireworks.getValue() ? targetDistance > 15.0 || targetIsFasterPlayer || this.isBelowTargetThreshold() || playerBelowTarget : targetDistance > 15.0 || targetIsFasterPlayer;
         if (shouldUseFirework && fireworkSwapStopwatch.hasElapsed(fireworkDelayMillis)) {
            this.useFirework();
         }
      }
   }

   private long getFireworkDelayMillis(LivingEntity target, double targetDistance) {
      long baseDelayMillis = (long)(this.fireworkDelaySeconds.getValue() * 1000.0F);
      if (!this.smartFireworks.getValue()) {
         return baseDelayMillis;
      } else if (target instanceof PlayerEntity playerTarget && this.isTargetMovingFaster(playerTarget)) {
         return (long)((float)baseDelayMillis * 0.68F);
      } else if (targetDistance < 8.0) {
         return (long)((float)baseDelayMillis * 1.35F);
      } else {
         return this.isBelowTargetThreshold() ? (long)((float)baseDelayMillis * 0.78F) : baseDelayMillis;
      }
   }

   private boolean isBelowTargetThreshold() {
      return CLIENT.player.getY()
         < (this.targetEntity != null ? this.targetEntity.getY() + 2.0 : CLIENT.world.getSeaLevel());
   }

   private boolean isTargetMovingFaster(PlayerEntity targetPlayer) {
      return CLIENT.player.getVelocity().horizontalLengthSquared() + 1.0E-4 < targetPlayer.getVelocity().horizontalLengthSquared();
   }

   private void useFirework() {
      if (this.fireworkSwapState == 0) {
         int hotbarRocketSlot = InventoryUtils.findHotbarSlot(rocketStack -> rocketStack.isOf(Items.FIREWORK_ROCKET));
         if (hotbarRocketSlot != -1) {
            int previousHotbarSlot = CLIENT.player.getInventory().getSelectedSlot();
            if (previousHotbarSlot == hotbarRocketSlot) {
               this.previousHotbarSlot = -1;
               this.fireworkSwapState = 2;
            } else {
               this.previousHotbarSlot = previousHotbarSlot;
               CLIENT.player.getInventory().setSelectedSlot(hotbarRocketSlot);
               ((MultiPlayerGameModeAccessor)CLIENT.interactionManager).daamky$syncSelectedSlot();
               this.fireworkSwapState = 1;
            }

            fireworkSwapStopwatch.reset();
         } else {
            int inventoryRocketSlot = InventoryUtils.findInventorySlot(rocketStack -> rocketStack.isOf(Items.FIREWORK_ROCKET));
            if (inventoryRocketSlot != -1 && InventoryUtils.canSwapInventorySlots()) {
               InventoryUtils.swapInventorySlotWithHotbar(inventoryRocketSlot, this.fireworkHotbarSlot.getValue().intValue() - 1, Priority.PriorityCategory.REALLYWORLD);
               fireworkSwapStopwatch.reset();
            }
         }
      }
   }

   private boolean wasAttackedThisTick() {
      return CLIENT.player.age == this.lastAttackAge;
   }

   public boolean isFireworkSequenceActive() {
      return this.isEnabled() && this.fireworkSwapState != 0;
   }

   private void advanceFireworkSwap() {
      if (this.fireworkSwapState != 0) {
         if (this.fireworkSwapState == 1) {
            this.fireworkSwapState = 2;
         } else if (this.fireworkSwapState == 2) {
            if (CLIENT.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET) && CLIENT.player.isGliding()) {
               Rotation lastMotionRotation = RotationPriorityManager.getInstance().getLastMotionRotation();
               InventoryUtils.interactItemWithRotation(Hand.MAIN_HAND, lastMotionRotation.getYaw(), lastMotionRotation.getPitch());
            }

            this.fireworkSwapState = this.previousHotbarSlot != -1 ? 3 : 0;
         } else {
            if (this.previousHotbarSlot != -1) {
               CLIENT.player.getInventory().setSelectedSlot(this.previousHotbarSlot);
               ((MultiPlayerGameModeAccessor)CLIENT.interactionManager).daamky$syncSelectedSlot();
            }

            this.previousHotbarSlot = -1;
            this.fireworkSwapState = 0;
         }
      }
   }

   private void requestStartGliding() {
      if (this.hasElytraEquipped() && CLIENT.player.isSprinting() && CLIENT.player.input.hasForwardMovement()) {
         if (!CLIENT.player.isGliding() && !CLIENT.player.isOnGround() && CLIENT.player.checkGliding()) {
            PacketSender.II1Il11l111II11IIl(new ClientCommandC2SPacket(CLIENT.player, Mode.START_FALL_FLYING));
         }
      }
   }

   private boolean hasElytraEquipped() {
      ItemStack chestStack = CLIENT.player.getEquippedStack(EquipmentSlot.CHEST);
      return chestStack.isOf(Items.ELYTRA);
   }

   private void restorePreviousHotbarSlot() {
      if (this.previousHotbarSlot != -1 && CLIENT.player != null && CLIENT.interactionManager != null) {
         CLIENT.player.getInventory().setSelectedSlot(this.previousHotbarSlot);
         ((MultiPlayerGameModeAccessor)CLIENT.interactionManager).daamky$syncSelectedSlot();
      }

      this.previousHotbarSlot = -1;
      this.fireworkSwapState = 0;
   }

   private void clearTarget() {
      this.targetEntity = null;
      this.targetPredictionPosition = Vec3d.ZERO;
   }

   private void resetFreezeState() {
      this.isFrozenInAir = false;
      this.freezePeakY = Double.NaN;
      this.freezePosition = Vec3d.ZERO;
   }
}

