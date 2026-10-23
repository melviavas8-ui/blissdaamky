package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.client.MinecraftClient;

public final class Priority4 {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
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
// https://t.me/pasterplace

   private Priority4() {
   }
// https://t.me/pasterplace

   public static Priority4 getInstance() {
      INSTANCE.registerEventListener();
      return INSTANCE;
   }
// https://t.me/pasterplace

   public static Rotation getLastMotionRotation() {
      return RotationPriorityManager.getInstance().getLastMotionRotation();
   }
// https://t.me/pasterplace

   public static Rotation getCurrentRotation() {
      return RotationPriorityManager.getInstance().getCurrentRotation();
   }
// https://t.me/pasterplace

   public int getPriority() {
      return this.currentPriority;
   }
// https://t.me/pasterplace

   public void setPriority(int priority) {
      this.currentPriority = priority;
   }
// https://t.me/pasterplace

   public Rotation getRequestedRotation() {
      return this.requestedRotation;
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

   public static void requestRotation(Rotation requestedRotation, float yawSpeed, float pitchSpeed, int requestPriority, int maximumRequestAge) {
      requestRotation(requestedRotation, yawSpeed, yawSpeed, pitchSpeed, pitchSpeed, requestPriority, maximumRequestAge);
   }
// https://t.me/pasterplace

   public static void requestRotation(Rotation requestedRotation, float rotationSpeed, int requestPriority, int maximumRequestAge) {
      requestRotation(requestedRotation, rotationSpeed, rotationSpeed, requestPriority, maximumRequestAge);
   }
// https://t.me/pasterplace

   private void applyRotationRequest() {
      if (this.requestedRotation != null && MinecraftClient.getInstance().player != null) {
         Rotation lIll1Il1lll111l11l1I1llllI2 = RotationUtils.limitRotationChange(
            RotationPriorityManager.getInstance().getCurrentRotation(), this.requestedRotation, this.yawSpeed, this.pitchSpeed
         );
         RotationPriorityManager.getInstance()
            .requestRotation(lIll1Il1lll111l11l1I1llllI2, FocusMode.FREE, Math.max(1.0F, this.minimumTurnSpeed), this.currentPriority, false, this);
      }
   }
// https://t.me/pasterplace

   private void clearRotationRequest() {
      RotationPriorityManager.getInstance().clearRotationRequest(this);
      this.requestedRotation = null;
      this.currentPriority = 0;
      this.maximumRequestAge = 0;
   }
// https://t.me/pasterplace

   private void registerEventListener() {
      if (!this.eventListenerRegistered && ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().register(this);
         this.eventListenerRegistered = true;
      }
   }
// https://t.me/pasterplace

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

