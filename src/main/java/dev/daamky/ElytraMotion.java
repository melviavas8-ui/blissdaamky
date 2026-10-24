package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventElytraVelocity;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ElytraMotion extends ModuleStructure {
   private static final float AIM_YAW_THRESHOLD = 20.0F;
   private final MultiOptionSetting mode = new MultiOptionSetting("Режим", "Новый", "Новый", "Старый");
   private boolean pausedForTarget;
   private boolean noGravityApplied;
   private Vec3d pausedPosition;
   private Vec3d savedVelocity;

   public ElytraMotion() {
      super("ElytraMotion", "Зависает на элитрах, пока KillAura бьёт цель", ModuleCategory.MOVEMENT);
      this.addSetting(this.mode);
   }

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.restorePlayerGravity();
      this.clearNewModePause();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.noGravityApplied = false;
      this.clearNewModePause();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (!this.mode.isSelected("Новый")) {
            this.clearNewModePause();
            this.updateLegacyMode();
         } else {
            this.restorePlayerGravity();
            this.updateNewMode();
         }
      }
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onElytraVelocity(EventElytraVelocity eventElytraVelocity) {
      if (this.mode.isSelected("Новый") && this.pausedForTarget && this.pausedPosition != null) {
         eventElytraVelocity.setVelocity(Vec3d.ZERO);
         CLIENT.player.setPosition(this.pausedPosition.x, this.pausedPosition.y, this.pausedPosition.z);
      }
   }

   private void updateNewMode() {
      if (!CLIENT.player.isGliding()) {
         this.clearNewModePause();
      } else {
         Aura aura = Aura.getInstance();
         LivingEntity targetEntity = aura != null && aura.isEnabled() ? aura.getTarget() : null;
         if (targetEntity != null
            && CLIENT.player.distanceTo(targetEntity) <= aura.getAttackRange()
            && !this.isTargetOutsideAimCone(targetEntity)
            && !this.isHandledByElytraTarget(targetEntity)) {
            if (!this.pausedForTarget) {
               this.pausedPosition = CLIENT.player.getEntityPos();
               this.savedVelocity = CLIENT.player.getVelocity();
            }

            this.pausedForTarget = true;
         } else {
            if (this.savedVelocity != null) {
               CLIENT.player.setVelocity(this.savedVelocity.x, 0.0, this.savedVelocity.z);
            }

            this.clearNewModePause();
         }
      }
   }

   private void updateLegacyMode() {
      Aura aura = Aura.getInstance();
      LivingEntity targetEntity = aura != null && aura.isEnabled() ? aura.getTarget() : null;
      if (targetEntity == null) {
         this.restorePlayerGravity();
      } else {
         boolean shouldPauseMovement = CLIENT.player.isGliding()
            && CLIENT.player
                  .getEyePos()
                  .distanceTo(targetEntity.getBoundingBox().getCenter().add(0.0, (targetEntity.getY() - targetEntity.lastY) * 2.0, 0.0))
               < aura.getAttackRange()
            && !this.isTargetOutsideAimCone(targetEntity)
            && !this.isHandledByElytraTarget(targetEntity);
         if (shouldPauseMovement) {
            CLIENT.player.setVelocity(Vec3d.ZERO);
            CLIENT.player.setNoGravity(true);
            this.noGravityApplied = true;
         } else {
            this.restorePlayerGravity();
         }
      }
   }

   private boolean isTargetOutsideAimCone(LivingEntity livingEntity) {
      Rotation lIll1Il1lll111l11l1I1llllI2 = RotationPriorityManager.getInstance().getCurrentRotation();
      Rotation lIll1Il1lll111l11l1I1llllI22 = RotationUtils.lookAt(
         CLIENT.player.getEyePos(), livingEntity.getBoundingBox().getCenter()
      );
      return Math.abs(MathHelper.wrapDegrees(lIll1Il1lll111l11l1I1llllI22.getYaw() - lIll1Il1lll111l11l1I1llllI2.getYaw())) >= 20.0F;
   }

   private boolean isHandledByElytraTarget(LivingEntity livingEntity) {
      ElytraTarget elytraTarget = ElytraTarget.getInstance();
      return elytraTarget != null && elytraTarget.isCurrentTarget(livingEntity);
   }

   private void clearNewModePause() {
      this.pausedForTarget = false;
      this.pausedPosition = null;
      this.savedVelocity = null;
   }

   private void restorePlayerGravity() {
      if (this.noGravityApplied && CLIENT.player != null) {
         CLIENT.player.setNoGravity(false);
      }

      this.noGravityApplied = false;
   }
}

