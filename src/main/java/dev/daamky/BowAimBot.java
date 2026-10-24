package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class BowAimBot extends ModuleStructure {
   private final FloatSetting targetRange = new FloatSetting("Дистанция", 60.0F, 10.0F, 120.0F, 1.0F, "бл.");
   private final FloatSetting fieldOfView = new FloatSetting("Угол обзора", 90.0F, 10.0F, 180.0F, 5.0F, "°");
   private final FloatSetting rotationSpeed = new FloatSetting("Скорость наводки", 35.0F, 5.0F, 180.0F, 1.0F, "°");
   private final BooleanSetting predictTargetMovement = new BooleanSetting("Предикт движения", true);
   private final BooleanSetting useCameraRotation = new BooleanSetting("Наводка камерой", false);
   private final BooleanSetting ignoreFriends = new BooleanSetting("Игнорировать друзей", true);
   private PlayerEntity targetPlayer;

   public BowAimBot() {
      super("BowAimBot", "Наводит лук и арбалет на игроков с учётом баллистики", ModuleCategory.COMBAT);
      this.addSettings(this.targetRange, this.fieldOfView, this.rotationSpeed, this.predictTargetMovement, this.useCameraRotation, this.ignoreFriends);
   }

   public PlayerEntity getTarget() {
      return this.targetPlayer;
   }

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      this.targetPlayer = null;
      if (this.hasActiveClientWorld() && CLIENT.player.isUsingItem()) {
         ItemStack activeItemStack = CLIENT.player.getActiveItem();
         boolean isBow = activeItemStack.getItem() instanceof BowItem;
         boolean usesFixedProjectileVelocity = activeItemStack.isOf(Items.CROSSBOW) || activeItemStack.isOf(Items.TRIDENT);
         if (isBow || usesFixedProjectileVelocity) {
            Vec3d shooterEyePosition = CLIENT.player.getEyePos();
            Rotation lIll1Il1lll111l11l1I1llllI2 = new Rotation(
               CLIENT.player.getYaw(), CLIENT.player.getPitch()
            );
            double closestTargetAngle = Double.MAX_VALUE;

             for (PlayerEntity candidatePlayer : CLIENT.world.getPlayers()) {
               if (candidatePlayer != CLIENT.player
                  && candidatePlayer.isAlive()
                  && !candidatePlayer.isSpectator()
                  && !AntiBot.II1Il11l111II11IIl((Entity)candidatePlayer)
                  && (!this.ignoreFriends.getValue() || !FriendManager.getInstance().isFriend((Entity)candidatePlayer))) {
                  double targetDistance = candidatePlayer.distanceTo(CLIENT.player);
                  if (!(targetDistance > this.targetRange.getValue().floatValue()) && CLIENT.player.canSee((Entity)candidatePlayer)) {
                     float angularDistance = RotationUtils.angularDistance(lIll1Il1lll111l11l1I1llllI2, shooterEyePosition, candidatePlayer.getBoundingBox().getCenter());
                     if (!(angularDistance > this.fieldOfView.getValue()) && angularDistance < closestTargetAngle) {
                        closestTargetAngle = angularDistance;
                        this.targetPlayer = (PlayerEntity)candidatePlayer;
                     }
                  }
               }
            }

            if (this.targetPlayer != null) {
               float projectileVelocity = isBow ? BowItem.getPullProgress(CLIENT.player.getItemUseTime()) * 3.0F : 3.15F;
               if (projectileVelocity < 0.1F) {
                  projectileVelocity = 0.1F;
               }

               Vec3d aimPoint = this.targetPlayer.getBoundingBox().getCenter();
               if (this.predictTargetMovement.getValue()) {
                  double estimatedFlightTicks = shooterEyePosition.distanceTo(aimPoint) / projectileVelocity;
                  Vec3d targetVelocity = this.targetPlayer.getVelocity();
                  aimPoint = aimPoint.add(targetVelocity.x * estimatedFlightTicks, 0.0, targetVelocity.z * estimatedFlightTicks);
               }

               double deltaX = aimPoint.x - shooterEyePosition.x;
               double deltaZ = aimPoint.z - shooterEyePosition.z;
               double deltaY = aimPoint.y - shooterEyePosition.y;
               double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
               double velocitySquared = projectileVelocity * projectileVelocity;
               double trajectoryDiscriminant = velocitySquared * velocitySquared - 0.05 * (0.05 * horizontalDistance * horizontalDistance + 2.0 * deltaY * velocitySquared);
               float aimPitch;
               if (trajectoryDiscriminant < 0.0) {
                  aimPitch = RotationUtils.lookAt(shooterEyePosition, aimPoint).getPitch();
               } else {
                  aimPitch = (float)(-Math.toDegrees(Math.atan((velocitySquared - Math.sqrt(trajectoryDiscriminant)) / (0.05 * horizontalDistance))));
               }

               float aimYaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F;
               Rotation lIll1Il1lll111l11l1I1llllI22 = new Rotation(
                  MathHelper.wrapDegrees(aimYaw), MathHelper.clamp(aimPitch, -90.0F, 90.0F)
               );
               Rotation lIll1Il1lll111l11l1I1llllI23 = RotationUtils.limitRotationChange(
                  RotationPriorityManager.getInstance().getCurrentRotation(), lIll1Il1lll111l11l1I1llllI22, this.rotationSpeed.getValue(), this.rotationSpeed.getValue()
               );
               RotationPriorityManager.getInstance()
                  .requestRotation(lIll1Il1lll111l11l1I1llllI23, FocusMode.FREE, 40.0F, 3, this.useCameraRotation.getValue(), this);
            }
         }
      }
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.targetPlayer = null;
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }
}

