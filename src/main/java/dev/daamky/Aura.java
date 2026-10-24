package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventInput;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import dev.daamky.mixin.client.LocalPlayerAccessor;
import dev.daamky.mixin.client.MinecraftAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Aura extends ModuleStructure {
   private static final float MAX_SMOOTH_ROTATION_STEP = 95.0F;
   private static final float MAX_REALLYWORLD2_ROTATION_STEP = 120.0F;
   private static final float MAX_MATRIX_ROTATION_STEP = 85.0F;
   private static final float ROTATION_REQUEST_PRIORITY = 75.0F;
   private static final float MATRIX_DRIFT_AMPLITUDE = 1.2F;
   private static final float ELYTRA_LEAD_TICKS = 2.0F;
   private static final float MIN_ATTACK_COOLDOWN = 0.92F;
   private static final int AUTO_JUMP_COOLDOWN_LIMIT = 6;
   private static final int TARGET_HURT_TIME_LIMIT = 2;
   private final MultiOptionSetting rotationModeSetting = new MultiOptionSetting(
      "Ротация", "ReallyWorld", "Плавная", "Matrix", "ReallyWorld", "WhiteRise", "Neuro", "Нет"
   );
   private final MultiOptionSetting movementCorrectionSetting = new MultiOptionSetting("Коррекция движения", "Свободная", "Свободная", "Сфокусированная", "Нет");
   private final FloatSetting attackRangeSetting = new FloatSetting("Дистанция атаки", 3.0F, 2.0F, 6.0F, 0.05F, "бл.");
   private final FloatSetting aimRangeSetting = new FloatSetting("Доп. дистанция наводки", 1.0F, 0.0F, 6.0F, 0.1F, "бл.");
   private final MultiOptionSetting targetSortSetting = new MultiOptionSetting(
      "Сортировка", "Умная", "Умная", "Дистанция", "Здоровье", "Поле зрения", "Броня"
   );
   private final MultiToggleSetting targetFilters = new MultiToggleSetting("Цели")
      .addOption("Игроки", true)
      .addOption("Голые", true)
      .addOption("Невидимые", true)
      .addOption("Друзья", false)
      .addOption("Союзники", false)
      .addOption("Мобы", false)
      .addOption("Животные", false);
   private final MultiToggleSetting attackOptions = new MultiToggleSetting("Атака")
      .addOption("Только криты", true)
      .addOption("Умные криты", true)
      .addOption("Авто-прыжок", false)
      .addOption("Проверка наводки", true)
      .addOption("Ломать щит", true)
      .addOption("Отжимать щит", true)
      .addOption("Через стены", false);
   private final MultiOptionSetting sprintResetSetting = new MultiOptionSetting("Сброс спринта", "Умный", "Умный", "Обычный", "Пакетный", "Нет");
   private final MultiToggleSetting attackRestrictions = new MultiToggleSetting("Не атаковать")
      .addOption("При еде", true)
      .addOption("В инвентаре", true)
      .addOption("Без оружия", false);
   private LivingEntity currentTarget;
   private int previousHotbarSlot = -1;
   private float smoothedRotationYaw;
   private float smoothedRotationPitch;
   private float rotationSmoothingTicks;
   private boolean sprintResetThisTick;
   private LivingEntity pendingAttackTarget;
   private boolean sprintResetPending;
   private boolean packetSprintReset;
   private LivingEntity sprintResetTarget;
   private LivingEntity deferredAttackTarget;
   private long lastAttackTimeMillis;
   private LivingEntity whiteRiseTarget;
   private float whiteRiseYaw;
   private float whiteRisePitch;
   private float whiteRiseProgress;
   private boolean whiteRiseAccelerating;
   private boolean whiteRiseInitialized;
   private int whiteRiseTick;

   public Aura() {
      super("Aura", "Автоматически наводится и атакует цели вокруг", ModuleCategory.COMBAT);
      this.movementCorrectionSetting.visibleWhen(() -> !this.rotationModeSetting.isSelected("Нет"));
      this.rotationModeSetting.withDescription("Как поворачивать голову к цели");
      this.movementCorrectionSetting.withDescription("Подстройка движения под ротацию");
      this.attackRangeSetting.withDescription("Максимальная дистанция для удара");
      this.aimRangeSetting.withDescription("Доп. дистанция поиска целей и поворота");
      this.targetSortSetting.withDescription("Порядок выбора целей");
      this.targetFilters.withDescription("Кого атаковать");
      this.attackOptions.withDescription("Условия для удара");
      this.sprintResetSetting.withDescription("Как сбрасывать спринт перед ударом");
      this.attackRestrictions.withDescription("Когда не атаковать");
      this.addSettings(
         this.rotationModeSetting,
         this.movementCorrectionSetting,
         this.attackRangeSetting,
         this.aimRangeSetting,
         this.targetSortSetting,
         this.targetFilters,
         this.attackOptions,
         this.sprintResetSetting,
         this.attackRestrictions
      );
   }

   public float getAttackRange() {
      return this.attackRangeSetting.getValue();
   }

   public LivingEntity getTarget() {
      return this.currentTarget;
   }

   @Override
   public String getDisplayInfo() {
      return this.rotationModeSetting.getValue();
   }

   public boolean isSprintResetActive() {
      return this.isEnabled() && this.sprintResetThisTick;
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.currentTarget = null;
      this.pendingAttackTarget = null;
      this.sprintResetThisTick = false;
      this.resetWhiteRiseRotationState();
      this.clearSprintReset();
      this.restorePreviousHotbarSlot();
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange worldChange) {
      this.currentTarget = null;
      this.previousHotbarSlot = -1;
      this.sprintResetThisTick = false;
   }

   @EventTarget
   public void updateTargetAndRotation(EventUpdate updateEvent) {
      this.sprintResetThisTick = false;
      if (!this.hasActiveClientWorld()) {
         this.currentTarget = null;
      } else {
         this.restorePreviousHotbarSlot();
         this.currentTarget = this.findTarget();
         if (this.currentTarget == null) {
            this.smoothedRotationYaw = 0.0F;
            this.smoothedRotationPitch = 0.0F;
            this.resetWhiteRiseRotationState();
            if (this.rotationModeSetting.isSelected("Neuro")) {
               Rotation currentRotation = NeuroRotationController.getInstance()
                  .predictRotationWithoutTarget(RotationPriorityManager.getInstance().getCurrentRotation());
               if (currentRotation != null) {
                  RotationPriorityManager.getInstance().requestRotation(currentRotation, this.getFocusMode(), 75.0F, 2, false, this);
               }
            }
         } else {
            this.rotationSmoothingTicks++;
            Vec3d playerEyePosition = CLIENT.player.getEyePos();
            Vec3d targetAimPosition = RotationUtils.getAimPoint(this.currentTarget, playerEyePosition, this.attackOptions.isOptionEnabled("Через стены"));
            if (this.isCurrentElytraTarget()) {
               this.smoothedRotationYaw = 0.0F;
               this.smoothedRotationPitch = 0.0F;
            } else {
               if (this.currentTarget.isGliding()) {
                  targetAimPosition = targetAimPosition.add(this.currentTarget.getVelocity().multiply(2.0));
               }

               Rotation currentRotation = RotationPriorityManager.getInstance().getCurrentRotation();
               Rotation desiredRotation = RotationUtils.lookAt(playerEyePosition, targetAimPosition);
               Rotation targetRotation = new Rotation(
                  currentRotation.getYaw()
                     + MathHelper.wrapDegrees(desiredRotation.getYaw() - currentRotation.getYaw()),
                  desiredRotation.getPitch()
               );
               boolean attackAllowed = this.canAttackTarget();

               Rotation selectedRotation = switch ((String)this.rotationModeSetting.getValue()) {
                  case "Мгновенная" -> targetRotation;
                  case "Плавная" -> this.getSmoothRotation(currentRotation, targetRotation);
                  case "ReallyWorld 2" -> this.getReallyWorld2Rotation(currentRotation, targetRotation);
                  case "Matrix" -> this.getMatrixRotation(currentRotation, targetRotation);
                  case "ReallyWorld" -> RotationUtils.snapToSensitivityGrid(currentRotation, targetRotation);
                  case "WhiteRise" -> this.getWhiteRiseRotation(currentRotation, targetRotation);
                  case "Neuro" -> NeuroRotationController.getInstance().isModelLoaded()
                     ? NeuroRotationController.getInstance().predictRotationForTarget(currentRotation, targetRotation, this.currentTarget)
                     : null;
                  case "Резкая" -> attackAllowed && this.isTargetWithinRange() ? targetRotation : null;
                  default -> null;
               };
               if (selectedRotation != null && this.movementCorrectionSetting.isSelected("Сфокусированная") && !this.rotationModeSetting.isSelected("Neuro")) {
                  selectedRotation = new Rotation(
                     RotationUtils.snapToSensitivityGrid(currentRotation, targetRotation).getYaw(), selectedRotation.getPitch()
                  );
               }

               if (selectedRotation != null) {
                  RotationPriorityManager.getInstance().requestRotation(selectedRotation, this.getFocusMode(), 75.0F, 2, false, this);
               }

               if (this.shouldDelaySprintResetForCriticalHit()) {
                  this.sprintResetThisTick = true;
                  CLIENT.player.setSprinting(false);
               }
            }
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void updateInput(EventInput inputEvent) {
      if (this.sprintResetThisTick || this.sprintResetPending) {
         inputEvent.setSprint(false);
      }

      if (this.shouldAutoJump()) {
         inputEvent.setJump(true);
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void queueAttackIfReady(EventMotion motionEvent) {
      if (motionEvent.isPost() && this.hasActiveClientWorld() && this.currentTarget != null && this.canAttackTarget() && this.isTargetWithinRange()) {
         if (!this.isElytraTargetSlowEnough()) {
            if (this.attackOptions.isOptionEnabled("Проверка наводки")
               && !this.rotationModeSetting.isSelected("Нет")
               && !this.isCurrentElytraTarget()) {
               Rotation eventRotation = new Rotation(motionEvent.yaw(), motionEvent.pitch());
               if (!RotationUtils.isLookingAtEntity(
                  eventRotation, this.currentTarget, this.attackRangeSetting.getValue().floatValue() + 0.3
               )) {
                  return;
               }
            }

            if (this.attackOptions.isOptionEnabled("Через стены") || CLIENT.player.canSee(this.currentTarget)) {
               this.pendingAttackTarget = this.currentTarget;
            }
         } else {
            if (this.isTargetInsideRotationRay(new Rotation(motionEvent.yaw(), motionEvent.pitch()))) {
               this.pendingAttackTarget = this.currentTarget;
            }
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void restoreSprintAfterAttackMotion(EventMotion motionEvent) {
      if (motionEvent.isPost() && this.sprintResetPending && this.hasActiveClientWorld()) {
         if (!this.daamky$wasSprinting()) {
            this.finishSprintReset(true);
         }
      }
   }

   @EventTarget(priority = 0)
   public void processPendingAttack(EventUpdate updateEvent) {
      LivingEntity pendingAttackTarget = this.pendingAttackTarget;
      this.pendingAttackTarget = null;
      if (!this.hasActiveClientWorld()) {
         this.clearSprintReset();
      } else {
         LivingEntity deferredAttackTarget = this.deferredAttackTarget;
         this.deferredAttackTarget = null;
         if (deferredAttackTarget != null) {
            this.attackIfReady(deferredAttackTarget);
         } else if (this.sprintResetPending) {
            CLIENT.player.setSprinting(false);
            if (this.packetSprintReset) {
               this.sendStopSprintingPacket();
               this.finishSprintReset(true);
            }
         } else if (pendingAttackTarget != null && pendingAttackTarget == this.currentTarget && pendingAttackTarget.isAlive() && this.canAttackTarget() && this.isTargetWithinRange()) {
            if (!this.isRotationCoordinatorBusy() && !this.prepareSmartCriticalSprintReset() && !this.prepareSprintResetForAttack(pendingAttackTarget)) {
               this.attackTarget();
            }
         }
      }
   }

   private boolean isRotationCoordinatorBusy() {
      ElytraTarget elytraTarget = ElytraTarget.getInstance();
      return elytraTarget != null && elytraTarget.isFireworkSequenceActive() || UseItemRotationQueue.hasQueuedPackets() || Priority.getInstance().hasQueuedActions();
   }

   private boolean daamky$wasSprinting() {
      return ((LocalPlayerAccessor)CLIENT.player).daamky$wasSprinting();
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void beginSprintReset(LivingEntity target, boolean sendPacketReset) {
      this.sprintResetPending = true;
      this.packetSprintReset = sendPacketReset;
      this.sprintResetTarget = target;
      CLIENT.player.setSprinting(false);
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void clearSprintReset() {
      this.sprintResetPending = false;
      this.packetSprintReset = false;
      this.sprintResetTarget = null;
      this.deferredAttackTarget = null;
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void sendStopSprintingPacket() {
      if (this.daamky$wasSprinting()) {
         PacketSender.II1Il11l111II11IIl(new ClientCommandC2SPacket(CLIENT.player, Mode.STOP_SPRINTING));
         ((LocalPlayerAccessor)CLIENT.player).daamky$setWasSprinting(false);
      }
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void finishSprintReset(boolean attackAfterReset) {
      LivingEntity sprintResetTarget = this.sprintResetTarget;
      this.sprintResetPending = false;
      this.packetSprintReset = false;
      this.sprintResetTarget = null;
      if (sprintResetTarget != null) {
         if (!attackAfterReset) {
            this.deferredAttackTarget = sprintResetTarget;
         } else {
            this.attackIfReady(sprintResetTarget);
         }
      }
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void attackIfReady(LivingEntity target) {
      if (target == this.currentTarget && target.isAlive() && this.canAttackTarget() && this.isTargetWithinRange() && !this.isRotationCoordinatorBusy()) {
         this.attackTarget();
      }
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private boolean prepareSmartCriticalSprintReset() {
      if (!this.sprintResetSetting.isSelected("Нет") && !this.isInWaterOrSwimming() && this.requiresCriticalHits() && this.isDescendingWhileSprinting()) {
         this.beginSprintReset(null, false);
         return true;
      } else {
         return false;
      }
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private boolean prepareSprintResetForAttack(LivingEntity target) {
      boolean normalSprintReset = this.sprintResetSetting.isSelected("Обычный");
      boolean packetSprintReset = this.sprintResetSetting.isSelected("Пакетный");
      if ((normalSprintReset || packetSprintReset) && !this.isInWaterOrSwimming()) {
         if (!CLIENT.player.isSprinting() && !this.daamky$wasSprinting()) {
            return false;
         }

         this.beginSprintReset(target, packetSprintReset);
         if (packetSprintReset) {
            this.sendStopSprintingPacket();
            this.finishSprintReset(true);
         }

         return true;
      } else {
         return false;
      }
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private boolean isDescendingWhileSprinting() {
      return CLIENT.player.isSprinting()
            && !(CombatUtils.getAttackCooldownProgress() < 0.75F)
            && !CombatUtils.isInInvalidCriticalState(CLIENT.player)
         ? !CLIENT.player.isOnGround() && CLIENT.player.getVelocity().y < 0.0
         : false;
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private boolean shouldDelaySprintResetForCriticalHit() {
      if (!this.sprintResetSetting.isSelected("Нет")
         && !this.sprintResetSetting.isSelected("Пакетный")
         && this.currentTarget != null
         && !this.isInWaterOrSwimming()
         && this.requiresCriticalHits()) {
         int targetWithinAttackRange = !(
               RotationUtils.distanceToBox(CLIENT.player.getEyePos(), this.currentTarget.getBoundingBox())
                  <= this.attackRangeSetting.getValue().floatValue() + 0.5
            )
            ? 0
            : 1;
         int criticalWindowOpen;
         if ((
                  criticalWindowOpen = CLIENT.player.isOnGround()
                        || !(CLIENT.player.getVelocity().y < 0.1) && !(this.getCriticalCooldownRemainder() <= 2.0F)
                     ? 0
                     : 1
               )
               != 0
            && this.isFallingIntoBlock()
            && this.getCriticalCooldownRemainder() > 1.0F) {
            return false;
         }

         int recentAttackCooldownWindow = System.currentTimeMillis() - this.lastAttackTimeMillis >= 100L ? 0 : 1;
         return !this.sprintResetSetting.isSelected("Обычный") ? targetWithinAttackRange != 0 && (criticalWindowOpen != 0 || recentAttackCooldownWindow != 0) : targetWithinAttackRange != 0 && criticalWindowOpen != 0 && this.getCriticalCooldownRemainder() <= 2.0F;
      } else {
         return false;
      }
   }

   private boolean isInWaterOrSwimming() {
      return CLIENT.player.isTouchingWater() || CLIENT.player.isSubmergedInWater() || CLIENT.player.isSwimming();
   }

   private float getCriticalCooldownRemainder() {
      return Math.max(0.0F, (0.92F - CombatUtils.getAttackCooldownProgress()) * CLIENT.player.getAttackCooldownProgressPerTick());
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private boolean shouldAutoJump() {
      if (!this.attackOptions.isOptionEnabled("Авто-прыжок") || !this.hasActiveClientWorld() || this.currentTarget == null || !this.requiresCriticalHits()) {
         return false;
      } else {
         return CLIENT.player.isOnGround() && !CLIENT.player.isSneaking() && CLIENT.currentScreen == null
            ? RotationUtils.distanceToBox(CLIENT.player.getEyePos(), this.currentTarget.getBoundingBox())
                  <= this.attackRangeSetting.getValue().floatValue() + 0.5
               && this.getCriticalCooldownRemainder() <= 6.0F
            : false;
      }
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void attackTarget() {
      if (CLIENT.player.isUsingItem()) {
         if (!this.attackOptions.isOptionEnabled("Отжимать щит") || !CLIENT.player.getActiveItem().isOf(Items.SHIELD)) {
            return;
         }

         CLIENT.interactionManager.stopUsingItem(CLIENT.player);
         MinecraftAccessor minecraftClientAccessor = (MinecraftAccessor)CLIENT;
         minecraftClientAccessor.daamky$setRightClickDelay(Math.max(minecraftClientAccessor.daamky$getRightClickDelay(), 2));
      }

      if (this.attackOptions.isOptionEnabled("Ломать щит")) {
         LivingEntity attackTarget = this.currentTarget;
         if (attackTarget instanceof PlayerEntity) {
            LivingEntity playerTarget = (PlayerEntity)attackTarget;
            int axeHotbarSlot;
            if (playerTarget.isBlocking()
               && (axeHotbarSlot = InventoryUtils.findHotbarSlot(candidateStack -> candidateStack.isIn(ItemTags.AXES))) != -1
               && axeHotbarSlot != InventoryUtils.getSelectedHotbarSlot()) {
               this.previousHotbarSlot = InventoryUtils.getSelectedHotbarSlot();
               InventoryUtils.setSelectedHotbarSlot(axeHotbarSlot);
            }
         }
      }

      CombatUtils.isInCobweb(this.currentTarget);
      CombatUtils.attackEntity(this.currentTarget);
      this.lastAttackTimeMillis = System.currentTimeMillis();
      this.restorePreviousHotbarSlot();
   }

   private boolean isCurrentElytraTarget() {
      ElytraTarget elytraTarget = ElytraTarget.getInstance();
      return elytraTarget != null
         && elytraTarget.isEnabled()
         && this.currentTarget != null
         && CLIENT.player.isGliding()
         && elytraTarget.getTarget() == this.currentTarget;
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void restorePreviousHotbarSlot() {
      if (this.previousHotbarSlot != -1 && CLIENT.player != null) {
         InventoryUtils.setSelectedHotbarSlot(this.previousHotbarSlot);
      }

      this.previousHotbarSlot = -1;
   }

   private boolean requiresCriticalHits() {
      return this.attackOptions.isOptionEnabled("Только криты") && !CombatUtils.isInInvalidCriticalState(CLIENT.player);
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private FocusMode getFocusMode() {
      return switch ((String)this.movementCorrectionSetting.getValue()) {
         case "Свободная" -> FocusMode.FREE;
         case "Сфокусированная" -> FocusMode.FOCUSED;
         default -> FocusMode.NONE;
      };
   }

   private boolean isTargetWithinRange() {
      return this.currentTarget != null
         && RotationUtils.distanceToBox(CLIENT.player.getEyePos(), this.currentTarget.getBoundingBox())
            <= this.attackRangeSetting.getValue().floatValue();
   }

   private boolean canAttackTarget() {
      if (this.currentTarget == null) {
         return false;
      } else if (this.attackRestrictions.isOptionEnabled("При еде")
         && CLIENT.player.isUsingItem()
         && !CLIENT.player.getActiveItem().isOf(Items.SHIELD)) {
         return false;
      } else if (this.attackRestrictions.isOptionEnabled("В инвентаре") && CLIENT.currentScreen != null) {
         return false;
      } else if (this.attackRestrictions.isOptionEnabled("Без оружия") && !CombatUtils.isInInvalidCriticalState(CLIENT.player)) {
         return false;
      } else if (CombatUtils.getAttackCooldownProgress() < 0.92F) {
         return false;
      } else if (this.attackOptions.isOptionEnabled("Умные криты") && !this.isTargetRecentlyHurt()) {
         return false;
      } else if (this.isElytraTargetSlowEnough()
         || !this.attackOptions.isOptionEnabled("Только криты")
         || CombatUtils.isInInvalidCriticalState(CLIENT.player)) {
         return true;
      } else if (CombatUtils.isFallingForCriticalHit(CLIENT.player) && CLIENT.player.getVelocity().y < 0.0) {
         return true;
      } else if (!CLIENT.player.isOnGround()
         && CLIENT.player.getVelocity().y >= 0.0
         && this.isPredictedTargetOutOfRange()
         && CombatUtils.getAttackCooldownProgress() >= 1.0F) {
         return this.attackOptions.isOptionEnabled("Умные криты");
      } else if (!CLIENT.player.isOnGround()) {
         return false;
      } else if (!this.attackOptions.isOptionEnabled("Умные криты") || CLIENT.options.jumpKey.isPressed()) {
         return false;
      } else {
         return this.attackOptions.isOptionEnabled("Авто-прыжок") && this.hasJumpClearance()
            ? false
            : !this.hasJumpClearance() || CombatUtils.getAttackCooldownProgress() >= 1.0F && this.isPredictedTargetOutOfRange();
      }
   }

   private boolean isTargetRecentlyHurt() {
      return this.currentTarget.hurtTime <= 2;
   }

   private boolean hasJumpClearance() {
      return CLIENT.world.isSpaceEmpty(CLIENT.player, CLIENT.player.getBoundingBox().offset(0.0, 0.3, 0.0));
   }

   private boolean isPredictedTargetOutOfRange() {
      Vec3d playerEyePosition = CLIENT.player.getEyePos();
      Box box = this.currentTarget.getBoundingBox().offset(this.currentTarget.getVelocity().multiply(3.0));
      return RotationUtils.distanceToBox(playerEyePosition, box) > this.attackRangeSetting.getValue().floatValue();
   }

   private boolean isFallingIntoBlock() {
      double verticalVelocity = CLIENT.player.getVelocity().y;
      return verticalVelocity < 0.0
         && !CLIENT.world
            .isSpaceEmpty(CLIENT.player, CLIENT.player.getBoundingBox().offset(0.0, verticalVelocity - 0.08, 0.0));
   }

   private boolean isElytraTargeting() {
      return ElytraTarget.isElytraTargetModuleEnabled();
   }

   private boolean isElytraTargetSlowEnough() {
      return this.isElytraTargeting() && CLIENT.player.isGliding() && CLIENT.player.getVelocity().length() < 6.0;
   }

   private Box getTrackedBoundingBox(LivingEntity livingEntity) {
      Vec3d trackedPosition = livingEntity.getTrackedPosition().getPos();
      return livingEntity.getBoundingBox().offset(trackedPosition.subtract(livingEntity.getEntityPos()));
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private boolean isTargetInsideRotationRay(Rotation rotation) {
      Vec3d playerEyePosition = CLIENT.player.getEyePos();
      double attackRange = this.attackRangeSetting.getValue().floatValue() - 0.15;
      Box targetBoundingBox = this.getTrackedBoundingBox(this.currentTarget);
      if (!(RotationUtils.distanceToBox(playerEyePosition, targetBoundingBox) > attackRange)) {
         return !targetBoundingBox.contains(playerEyePosition)
            ? targetBoundingBox.raycast(playerEyePosition, playerEyePosition.add(rotation.directionVector().multiply(attackRange))).isPresent()
            : true;
      } else {
         return false;
      }
   }

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private LivingEntity findTarget() {
      boolean usingElytraTarget;
      double targetSearchRange = !(usingElytraTarget = this.isElytraTargeting())
         ? this.attackRangeSetting.getValue() + this.aimRangeSetting.getValue()
         : ElytraTarget.getInstance().getTargetRange();
      Vec3d playerEyePosition = CLIENT.player.getEyePos();
      Rotation playerRotation = new Rotation(
         CLIENT.player.getYaw(), CLIENT.player.getPitch()
      );
      ArrayList<LivingEntity> candidates = new ArrayList<>();

      for (Entity entity : CLIENT.world.getEntities()) {
         if (entity instanceof LivingEntity livingEntity
            && this.isValidTarget(livingEntity)
            && (!usingElytraTarget || livingEntity instanceof PlayerEntity)
            && !(RotationUtils.distanceToBox(playerEyePosition, livingEntity.getBoundingBox()) > targetSearchRange)) {
            candidates.add(livingEntity);
         }
      }

      if (candidates.isEmpty()) {
         return null;
      }

      if (this.currentTarget != null && candidates.contains(this.currentTarget) && this.targetSortSetting.isSelected("Умная")) {
         return this.currentTarget;
      }

      candidates.sort(
         !usingElytraTarget
            ? this.createTargetComparator(playerEyePosition, playerRotation)
            : Comparator.comparingDouble(livingEntity -> RotationUtils.distanceToBox(playerEyePosition, livingEntity.getBoundingBox()))
      );
      return candidates.getFirst();
   }

   private Comparator<LivingEntity> createTargetComparator(Vec3d playerEyePosition, Rotation playerRotation) {
      return switch ((String)this.targetSortSetting.getValue()) {
         case "Здоровье" -> Comparator.comparingDouble(CombatUtils::getEffectiveHealth);
         case "Поле зрения" -> Comparator.comparingDouble(
            livingEntity -> RotationUtils.angularDistance(playerRotation, playerEyePosition, livingEntity.getBoundingBox().getCenter())
         );
         case "Броня" -> Comparator.comparingDouble(livingEntity -> livingEntity.getArmor());
         case "Дистанция" -> Comparator.comparingDouble(livingEntity -> RotationUtils.distanceToBox(playerEyePosition, livingEntity.getBoundingBox()));
         default -> Comparator.comparingDouble(
            livingEntity -> RotationUtils.distanceToBox(playerEyePosition, livingEntity.getBoundingBox()) * 2.0
               + CombatUtils.getEffectiveHealth(livingEntity) * 0.15
               + RotationUtils.angularDistance(playerRotation, playerEyePosition, livingEntity.getBoundingBox().getCenter()) * 0.02
         );
      };
   }

   private boolean isValidTarget(LivingEntity candidate) {
      if (candidate == CLIENT.player || !candidate.isAlive() || candidate.isDead() || candidate instanceof ArmorStandEntity) {
         return false;
      }

      if (candidate.isInvisible() && !this.targetFilters.isOptionEnabled("Невидимые")) {
         return false;
      }

      if (candidate instanceof PlayerEntity playerTarget) {
         if (this.targetFilters.isOptionEnabled("Игроки") && !playerTarget.isSpectator() && !playerTarget.isCreative()) {
            if (!this.targetFilters.isOptionEnabled("Друзья") && FriendManager.getInstance().isFriend(playerTarget)) {
               return false;
            } else if (!this.targetFilters.isOptionEnabled("Союзники") && CLIENT.player.isTeammate(playerTarget)) {
               return false;
            } else {
               return !this.targetFilters.isOptionEnabled("Голые") && isPlayerUnarmored(playerTarget) ? false : !AntiBot.II1Il11l111II11IIl(playerTarget);
            }
         } else {
            return false;
         }
      } else if (candidate instanceof Monster) {
         return this.targetFilters.isOptionEnabled("Мобы");
      } else {
         return !(candidate instanceof AnimalEntity) && !(candidate instanceof WaterCreatureEntity)
            ? this.targetFilters.isOptionEnabled("Мобы")
            : this.targetFilters.isOptionEnabled("Животные");
      }
   }

   private static boolean isPlayerUnarmored(PlayerEntity player) {
      for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
         if (!player.getEquippedStack(equipmentSlot).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private static float randomFloatBetween(float minimum, float maximum) {
      return minimum + ThreadLocalRandom.current().nextFloat() * (maximum - minimum);
   }

   private Rotation getSmoothRotation(
      Rotation currentRotation, Rotation targetRotation
   ) {
      float yawDelta = currentRotation.yawDeltaTo(targetRotation);
      float pitchDelta = currentRotation.pitchDeltaTo(targetRotation);
      float totalAngularDistance = (float)Math.hypot(yawDelta, pitchDelta);
      if (totalAngularDistance < 0.01F) {
         return targetRotation;
      }

      float rotationSpeedScale = MathHelper.clamp(totalAngularDistance / 45.0F, 0.3F, 1.0F);
      float maximumAngularStep = 95.0F * rotationSpeedScale * randomFloatBetween(0.88F, 1.08F);
      float yawStep = Math.abs(yawDelta) / totalAngularDistance * maximumAngularStep;
      float pitchStep = Math.abs(pitchDelta) / totalAngularDistance * maximumAngularStep * 0.75F;
      return RotationUtils.limitRotationChange(currentRotation, targetRotation, yawStep, Math.max(pitchStep, 0.5F));
   }

   private Rotation getReallyWorld2Rotation(
      Rotation currentRotation, Rotation targetRotation
   ) {
      float maximumAngularStep = 120.0F;
      float yawStep = MathHelper.clamp(
         currentRotation.yawDeltaTo(targetRotation) * randomFloatBetween(0.55F, 0.7F), -maximumAngularStep, maximumAngularStep
      );
      float pitchStep = MathHelper.clamp(
         currentRotation.pitchDeltaTo(targetRotation) * randomFloatBetween(0.45F, 0.6F), -maximumAngularStep * 0.7F, maximumAngularStep * 0.7F
      );
      return Math.abs(yawStep) < 0.2F && Math.abs(pitchStep) < 0.2F
         ? currentRotation
         : new Rotation(
            currentRotation.getYaw() + yawStep, MathHelper.clamp(currentRotation.getPitch() + pitchStep, -90.0F, 90.0F)
         );
   }

   private Rotation getMatrixRotation(
      Rotation currentRotation, Rotation targetRotation
   ) {
      float yawDelta = currentRotation.yawDeltaTo(targetRotation);
      float pitchDelta = currentRotation.pitchDeltaTo(targetRotation);
      float maximumAngularStep = 85.0F;
      float yawStep = MathHelper.clamp(yawDelta * randomFloatBetween(0.42F, 0.58F), -maximumAngularStep, maximumAngularStep);
      float pitchStep = MathHelper.clamp(pitchDelta * randomFloatBetween(0.3F, 0.42F), -maximumAngularStep * 0.6F, maximumAngularStep * 0.6F);
      this.smoothedRotationYaw = MathHelper.lerp(0.55F, this.smoothedRotationYaw, yawStep);
      this.smoothedRotationPitch = MathHelper.lerp(0.45F, this.smoothedRotationPitch, pitchStep);
      float driftAmplitude = 1.2F;
      float yawDrift = (float)(Math.sin(this.rotationSmoothingTicks * 0.71) + Math.sin(this.rotationSmoothingTicks * 1.37) * 0.5) * driftAmplitude * 0.6F;
      float pitchDrift = (float)Math.cos(this.rotationSmoothingTicks * 0.93) * driftAmplitude * 0.35F;
      return new Rotation(
         currentRotation.getYaw() + this.smoothedRotationYaw + yawDrift,
         MathHelper.clamp(currentRotation.getPitch() + this.smoothedRotationPitch + pitchDrift, -90.0F, 90.0F)
      );
   }

   public void selectNeuroRotation() {
      this.rotationModeSetting.setSelectedIndex(this.rotationModeSetting.getOptions().indexOf("Neuro"));
   }

   private void resetWhiteRiseRotationState() {
      this.whiteRiseTarget = null;
      this.whiteRiseProgress = 0.0F;
      this.whiteRiseAccelerating = false;
      this.whiteRiseTick = 0;
      this.whiteRiseInitialized = false;
   }

   private Rotation getWhiteRiseRotation(
      Rotation currentRotation, Rotation targetRotation
   ) {
      if (CLIENT.player.isBlocking()) {
         this.whiteRiseYaw = currentRotation.getYaw();
         this.whiteRisePitch = currentRotation.getPitch();
         this.whiteRiseInitialized = true;
         return currentRotation;
      }

      if (!this.whiteRiseInitialized) {
         this.whiteRiseYaw = currentRotation.getYaw();
         this.whiteRisePitch = currentRotation.getPitch();
         this.whiteRiseInitialized = true;
      }

      if (this.whiteRiseTarget != this.currentTarget) {
         this.whiteRiseTarget = this.currentTarget;
         this.whiteRiseProgress = 0.0F;
         this.whiteRiseAccelerating = false;
         this.whiteRiseTick = 0;
      }

      this.whiteRiseTick++;
      float rotationJitter = (float)((Math.sin(this.whiteRiseTick * 0.17) * 0.12 + (Math.random() * 0.08 - 0.04)) * 0.7F);
      float targetYaw = targetRotation.getYaw();
      float targetPitch = targetRotation.getPitch();
      float yawGap = Math.abs(MathHelper.wrapDegrees(targetYaw - this.whiteRiseYaw));
      boolean criticalHitReady = CombatUtils.getAttackCooldownProgress() > 0.9F && this.getCriticalCooldownRemainder() <= 1.0F;
      boolean isGliding = CLIENT.player.isGliding();
      if (!this.whiteRiseAccelerating) {
         float accelerationStep = 0.0055F;
         if (yawGap > 60.0F) {
            accelerationStep += 0.028800001F;
         } else if (yawGap > 30.0F) {
            accelerationStep += 0.014400001F;
         } else {
            accelerationStep += 0.0072000003F;
         }

         if (criticalHitReady) {
            accelerationStep += 0.012857143F;
         }

         this.whiteRiseProgress += accelerationStep * (1.6F + rotationJitter);
         if (this.whiteRiseProgress >= 0.22F) {
            this.whiteRiseAccelerating = true;
         }
      } else {
         float decelerationStep = criticalHitReady ? 0.045F : 0.008F;
         this.whiteRiseProgress -= decelerationStep * (2.1F + rotationJitter);
         if (this.whiteRiseProgress <= -0.04F) {
            this.whiteRiseAccelerating = false;
         }
      }

      float interpolationProgress = MathHelper.clamp(this.whiteRiseProgress, 0.14F, isGliding ? 0.38F : 0.26F);
      if (criticalHitReady) {
         interpolationProgress = Math.min(interpolationProgress + 0.1F, isGliding ? 0.46F : 0.34F);
      }

      interpolationProgress += rotationJitter * 0.5F;
      if (this.whiteRiseTick % 7 == 0) {
         interpolationProgress += 0.03F;
      }

      float yawError = MathHelper.wrapDegrees(targetYaw - this.whiteRiseYaw);
      float pitchError = targetPitch - this.whiteRisePitch;
      float maximumYawStep = isGliding ? 85.0F : (criticalHitReady ? 70.0F : 60.0F);
      float maximumPitchStep = isGliding ? 30.0F : (criticalHitReady ? 20.0F : 14.0F);
      yawError = MathHelper.clamp(yawError, -maximumYawStep, maximumYawStep);
      pitchError = MathHelper.clamp(pitchError, -maximumPitchStep, maximumPitchStep);
      float pitchStep = interpolationProgress * 1.6F;
      float yawStep = interpolationProgress * (3.3F + rotationJitter * 0.4F);
      float nextYaw = this.whiteRiseYaw + yawError * yawStep;
      float nextPitch = this.whiteRisePitch + pitchError * pitchStep;
      float mouseSensitivityStep = (float)RotationUtils.mouseSensitivityStep();
      if (mouseSensitivityStep > 0.0F) {
         nextYaw = this.whiteRiseYaw + Math.round((nextYaw - this.whiteRiseYaw) / mouseSensitivityStep) * mouseSensitivityStep;
         nextPitch = this.whiteRisePitch + Math.round((nextPitch - this.whiteRisePitch) / mouseSensitivityStep) * mouseSensitivityStep;
      }

      nextPitch = MathHelper.clamp(nextPitch, -89.0F, 89.0F);
      this.whiteRiseYaw = nextYaw;
      this.whiteRisePitch = nextPitch;
      return new Rotation(nextYaw, nextPitch);
   }

   public static Aura getInstance() {
      return ModuleRegistry.getInstance().getModuleByClass(Aura.class);
   }

   public static Aura getRegisteredAura() {
      return getInstance();
   }
}

