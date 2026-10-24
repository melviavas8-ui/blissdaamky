package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.client.MinecraftClient;

public final class Priority4 {
   private static final Priority4 INSTANCE = new Priority4();
   private int currentPriority;
   private Rotation requestedRotation;
   private float yawSpeed;
   private float pitchSpeed;
   private float minimumTurnSpeed;
   private int requestPriority;
   private int maximumRequestAge;
   private int requestPlayerAge = Integer.MIN_VALUE;
   private boolean eventListenerRegistered;

   private Priority4() {
   }

   public static Priority4 getInstance() {
      INSTANCE.registerEventListener();
      return INSTANCE;
   }

   public static Rotation getLastMotionRotation() {
      return RotationPriorityManager.getInstance().getLastMotionRotation();
   }

   public static Rotation getCurrentRotation() {
      return RotationPriorityManager.getInstance().getCurrentRotation();
   }

   public int getPriority() {
      return this.currentPriority;
   }

   public void setPriority(int priority) {
      this.currentPriority = priority;
   }

   public Rotation getRequestedRotation() {
      return this.requestedRotation;
   }

   public static void requestRotation(
      Rotation requestedRotation, float yawSpeed, float pitchSpeed, float minimumTurnSpeed, float maximumTurnSpeed, int requestPriority, int maximumRequestAge
   ) {
      Priority4 priority4 = getInstance();
      if (priority4.currentPriority <= maximumRequestAge) {
         priority4.minimumTurnSpeed = Math.max(minimumTurnSpeed, maximumTurnSpeed);
         if (requestedRotation == null) {
            priority4.clearRotationRequest();
         } else {
            priority4.requestedRotation = requestedRotation;
            priority4.yawSpeed = yawSpeed;
            priority4.pitchSpeed = pitchSpeed;
            priority4.requestPriority = requestPriority;
            priority4.currentPriority = maximumRequestAge;
            priority4.maximumRequestAge = 0;
            MinecraftClient client = MinecraftClient.getInstance();
            priority4.requestPlayerAge = client.player == null ? Integer.MIN_VALUE : client.player.age;
            priority4.applyRotationRequest();
         }
      }
   }

   public static void requestRotation(Rotation requestedRotation, float yawSpeed, float pitchSpeed, int requestPriority, int maximumRequestAge) {
      requestRotation(requestedRotation, yawSpeed, yawSpeed, pitchSpeed, pitchSpeed, requestPriority, maximumRequestAge);
   }

   public static void requestRotation(Rotation requestedRotation, float rotationSpeed, int requestPriority, int maximumRequestAge) {
      requestRotation(requestedRotation, rotationSpeed, rotationSpeed, requestPriority, maximumRequestAge);
   }

   private void applyRotationRequest() {
      if (this.requestedRotation != null && MinecraftClient.getInstance().player != null) {
         Rotation lIll1Il1lll111l11l1I1llllI2 = RotationUtils.limitRotationChange(
            RotationPriorityManager.getInstance().getCurrentRotation(), this.requestedRotation, this.yawSpeed, this.pitchSpeed
         );
         RotationPriorityManager.getInstance()
            .requestRotation(lIll1Il1lll111l11l1I1llllI2, FocusMode.FREE, Math.max(1.0F, this.minimumTurnSpeed), this.currentPriority, false, this);
      }
   }

   private void clearRotationRequest() {
      RotationPriorityManager.getInstance().clearRotationRequest(this);
      this.requestedRotation = null;
      this.currentPriority = 0;
      this.maximumRequestAge = 0;
   }

   private void registerEventListener() {
      if (!this.eventListenerRegistered && ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().register(this);
         this.eventListenerRegistered = true;
      }
   }

   @EventTarget(priority = 3)
   public void onUpdate(EventUpdate eventUpdate) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null) {
         this.clearRotationRequest();
      } else if (this.requestedRotation != null && this.requestPlayerAge != client.player.age) {
         if (++this.maximumRequestAge > this.requestPriority) {
            this.clearRotationRequest();
         } else {
            this.applyRotationRequest();
         }
      }
   }
}

