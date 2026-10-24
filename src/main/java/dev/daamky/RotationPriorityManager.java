package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventInput;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationPriorityManager {
   private static final RotationPriorityManager INSTANCE = new RotationPriorityManager();
   private Rotation currentRotation;
   private Rotation rotationSnapshot;
   private RotationPriorityManager.RotationTarget pendingRotationRequest;
   private RotationPriorityManager.RotationTarget activeRotationRequest;
   private boolean returningToPlayerRotation;
   private float currentBodyYaw;
   private float previousBodyYaw;
   private boolean bodyYawOverrideEnabled;
   private Rotation lastMotionRotation;

   private RotationPriorityManager() {
   }

   public static RotationPriorityManager getInstance() {
      return INSTANCE;
   }

   public void requestRotation(
      Rotation requestedRotation, FocusMode focusMode, float weight, int priority, boolean persistent, Object owner
   ) {
      if (requestedRotation != null
         && !Float.isNaN(requestedRotation.getYaw())
         && !Float.isNaN(requestedRotation.getPitch())) {
         if (this.pendingRotationRequest == null || this.pendingRotationRequest.priority() <= priority) {
            this.pendingRotationRequest = new RotationPriorityManager.RotationTarget(requestedRotation, focusMode, Math.max(1.0F, weight), priority, persistent, owner);
         }
      }
   }

   public void requestRotation(Rotation requestedRotation, FocusMode focusMode, int priority, Object owner) {
      this.requestRotation(requestedRotation, focusMode, 45.0F, priority, false, owner);
   }

   public void clearRotationRequest(Object owner) {
      if (this.pendingRotationRequest != null && this.pendingRotationRequest.owner() == owner) {
         this.pendingRotationRequest = null;
      }
   }

   public boolean hasCurrentRotation() {
      return this.currentRotation != null;
   }

   public boolean isFocusedBy(Object owner) {
      return this.activeRotationRequest != null && this.activeRotationRequest.owner() == owner && !this.returningToPlayerRotation;
   }

   public Rotation getCurrentRotation() {
      if (this.currentRotation != null) {
         return this.currentRotation;
      }

      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      return clientPlayerEntity == null
         ? new Rotation(0.0F, 0.0F)
         : new Rotation(clientPlayerEntity.getYaw(), clientPlayerEntity.getPitch());
   }

   public Rotation getLastMotionRotation() {
      if (this.lastMotionRotation != null) {
         return this.lastMotionRotation;
      }

      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      return clientPlayerEntity == null
         ? new Rotation(0.0F, 0.0F)
         : new Rotation(clientPlayerEntity.getYaw(), clientPlayerEntity.getPitch());
   }

   public Rotation getRotationSnapshot() {
      return this.rotationSnapshot != null ? this.rotationSnapshot : this.getCurrentRotation();
   }

   public float resolveYaw(float fallbackYaw) {
      return this.currentRotation != null && this.activeRotationRequest != null && this.activeRotationRequest.focusMode() != FocusMode.NONE
         ? this.currentRotation.getYaw()
         : fallbackYaw;
   }

   public Vec3d resolveLookVector(Vec3d fallbackLookVector) {
      return this.currentRotation != null ? Vec3d.fromPolar(this.currentRotation.getPitch(), this.currentRotation.getYaw()) : fallbackLookVector;
   }

   public boolean isBodyYawOverrideActive() {
      return this.bodyYawOverrideEnabled && this.currentRotation != null;
   }

   public float getInterpolatedYaw(float tickDelta) {
      Rotation previousRotation = this.getRotationSnapshot();
      return MathHelper.lerpAngleDegrees(tickDelta, previousRotation.getYaw(), this.getCurrentRotation().getYaw());
   }

   public float getInterpolatedPitch(float tickDelta) {
      return MathHelper.lerp(tickDelta, this.getRotationSnapshot().getPitch(), this.getCurrentRotation().getPitch());
   }

   public float getInterpolatedBodyYaw(float tickDelta) {
      return MathHelper.lerpAngleDegrees(tickDelta, this.previousBodyYaw, this.currentBodyYaw);
   }

   private void updateBodyYaw(ClientPlayerEntity clientPlayerEntity) {
      this.previousBodyYaw = this.currentBodyYaw;
      if (this.currentRotation == null) {
         this.currentBodyYaw = clientPlayerEntity.bodyYaw;
         this.previousBodyYaw = clientPlayerEntity.lastBodyYaw;
         this.bodyYawOverrideEnabled = false;
      } else {
         if (!this.bodyYawOverrideEnabled) {
            this.currentBodyYaw = clientPlayerEntity.bodyYaw;
            this.previousBodyYaw = this.currentBodyYaw;
            this.bodyYawOverrideEnabled = true;
         }

         float yawDifference = MathHelper.wrapDegrees(this.currentRotation.getYaw() - this.currentBodyYaw);
         if (Math.abs(yawDifference) > 50.0F) {
            this.currentBodyYaw = this.currentBodyYaw + (yawDifference - Math.signum(yawDifference) * 50.0F);
         }

         if (clientPlayerEntity.getVelocity().horizontalLengthSquared() > 0.0025) {
            this.currentBodyYaw = this.currentBodyYaw + MathHelper.wrapDegrees(this.currentRotation.getYaw() - this.currentBodyYaw) * 0.3F;
         }
      }
   }

   public void reset() {
      this.currentRotation = null;
      this.rotationSnapshot = null;
      this.pendingRotationRequest = null;
      this.activeRotationRequest = null;
      this.returningToPlayerRotation = false;
      this.bodyYawOverrideEnabled = false;
   }

   @EventTarget(priority = 4)
   public void onUpdate(EventUpdate eventUpdate) {
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      if (clientPlayerEntity == null) {
         this.reset();
      } else {
         Rotation playerRotation = new Rotation(
            clientPlayerEntity.getYaw(), clientPlayerEntity.getPitch()
         );
         this.rotationSnapshot = this.currentRotation != null ? this.currentRotation : playerRotation;
         if (this.pendingRotationRequest != null) {
            Rotation startingRotation = this.currentRotation != null ? this.currentRotation : playerRotation;
            Rotation snappedRequestedRotation = RotationUtils.snapToSensitivityGrid(
               startingRotation, this.pendingRotationRequest.rotation()
            );
            this.currentRotation = snappedRequestedRotation;
            this.activeRotationRequest = this.pendingRotationRequest;
            this.returningToPlayerRotation = false;
            if (this.pendingRotationRequest.persistent()) {
               clientPlayerEntity.setYaw(snappedRequestedRotation.getYaw());
               clientPlayerEntity.setPitch(snappedRequestedRotation.getPitch());
            }
         } else if (this.currentRotation == null || this.activeRotationRequest == null) {
            this.currentRotation = null;
            this.activeRotationRequest = null;
         } else if (this.activeRotationRequest.persistent()) {
            this.reset();
         } else {
            this.returningToPlayerRotation = true;
            Rotation limitedReturnRotation = RotationUtils.limitRotationChange(
               this.currentRotation, playerRotation, this.activeRotationRequest.weight(), this.activeRotationRequest.weight()
            );
            Rotation snappedReturnRotation = RotationUtils.snapToSensitivityGrid(this.currentRotation, limitedReturnRotation);
            if (snappedReturnRotation.angularDistanceTo(playerRotation) <= Math.max(1.0F, (float)RotationUtils.mouseSensitivityStep() * 2.0F)) {
               float wholeTurnOffset = Math.round((this.currentRotation.getYaw() - playerRotation.getYaw()) / 360.0F) * 360.0F;
               if (wholeTurnOffset != 0.0F) {
                  clientPlayerEntity.setYaw(clientPlayerEntity.getYaw() + wholeTurnOffset);
                  clientPlayerEntity.lastYaw += wholeTurnOffset;
                  clientPlayerEntity.headYaw += wholeTurnOffset;
                  clientPlayerEntity.lastHeadYaw += wholeTurnOffset;
                  clientPlayerEntity.bodyYaw += wholeTurnOffset;
                  clientPlayerEntity.lastBodyYaw += wholeTurnOffset;
               }

               this.currentRotation = null;
               this.activeRotationRequest = null;
               this.returningToPlayerRotation = false;
            } else {
               this.currentRotation = snappedReturnRotation;
            }
         }

         this.pendingRotationRequest = null;
         this.updateBodyYaw(clientPlayerEntity);
      }
   }

   @EventTarget(priority = 4)
   public void onMotion(EventMotion eventMotion) {
      if (eventMotion.isPre() && this.currentRotation != null) {
         eventMotion.setYaw(this.currentRotation.getYaw());
         eventMotion.setPitch(this.currentRotation.getPitch());
      }

      if (eventMotion.isPost()) {
         this.lastMotionRotation = new Rotation(eventMotion.yaw(), eventMotion.pitch());
      }
   }

   @EventTarget(priority = 4)
   public void onInput(EventInput eventInput) {
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      if (clientPlayerEntity != null
         && this.currentRotation != null
         && this.activeRotationRequest != null
         && this.activeRotationRequest.focusMode() != FocusMode.NONE) {
         float forwardInput = eventInput.forward();
         float strafeInput = eventInput.strafe();
         if (forwardInput != 0.0F || strafeInput != 0.0F) {
            float inputMagnitude = Math.max(Math.abs(forwardInput), Math.abs(strafeInput));
            if (this.activeRotationRequest.focusMode() == FocusMode.FOCUSED) {
               eventInput.setForward(inputMagnitude);
               eventInput.setStrafe(0.0F);
            } else {
               double desiredMovementYaw = movementInputToYaw(clientPlayerEntity.getYaw(), forwardInput, strafeInput);
               float correctedForwardInput = 0.0F;
               float correctedStrafeInput = 0.0F;
               double closestYawDifference = Double.MAX_VALUE;

               for (int forwardCandidate = -1; forwardCandidate <= 1; forwardCandidate++) {
                  for (int strafeCandidate = -1; strafeCandidate <= 1; strafeCandidate++) {
                     if (forwardCandidate != 0 || strafeCandidate != 0) {
                        double yawDifference = Math.abs(MathHelper.wrapDegrees(desiredMovementYaw - movementInputToYaw(this.currentRotation.getYaw(), forwardCandidate, strafeCandidate)));
                        if (yawDifference < closestYawDifference) {
                           closestYawDifference = yawDifference;
                           correctedForwardInput = forwardCandidate;
                           correctedStrafeInput = strafeCandidate;
                        }
                     }
                  }
               }

               eventInput.setForward(correctedForwardInput * inputMagnitude);
               eventInput.setStrafe(correctedStrafeInput * inputMagnitude);
            }
         }
      }
   }

   @EventTarget
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.reset();
   }

   private static double movementInputToYaw(float playerYaw, float forwardInput, float strafeInput) {
      float yawRadians = playerYaw * (float) (Math.PI / 180.0);
      double sineYaw = MathHelper.sin(yawRadians);
      double cosineYaw = MathHelper.cos(yawRadians);
      double movementX = strafeInput * cosineYaw - forwardInput * sineYaw;
      double movementZ = forwardInput * cosineYaw + strafeInput * sineYaw;
      return Math.toDegrees(Math.atan2(movementZ, movementX));
   }

   public record RotationTarget(Rotation rotation, FocusMode focusMode, float weight, int priority, boolean persistent, Object owner) {

      public RotationTarget(Rotation rotation, FocusMode focusMode, float weight, int priority, boolean persistent, Object owner) {
         this.rotation = rotation;
         this.focusMode = focusMode;
         this.weight = weight;
         this.priority = priority;
         this.persistent = persistent;
         this.owner = owner;
      }
   }
}

