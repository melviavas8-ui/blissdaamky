package dev.daamky;

public final class ActionSequenceOutcomeTracker {
    private static final String p68f74b9 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private int currentActionSequence = -1;
   private boolean actionAcknowledged;
   private Boolean interactionAccepted;
   private boolean packetObserved;
   private int lastPacketSequence;
// https://t.me/pasterplace

   public synchronized void acknowledgeThrough(int sequenceId) {
      if (this.currentActionSequence >= 0 && sequenceId >= this.currentActionSequence) {
         this.actionAcknowledged = true;
      }
   }
// https://t.me/pasterplace

   public synchronized void recordInteractionResult(boolean accepted) {
      if (this.currentActionSequence >= 0 && !Boolean.FALSE.equals(this.interactionAccepted)) {
         this.interactionAccepted = accepted;
      }
   }
// https://t.me/pasterplace

   public synchronized void beginActionSequence(int sequenceId) {
      if (sequenceId < 0) {
         throw new IllegalArgumentException("An action sequence must be non-negative");
      }
// https://t.me/pasterplace

      if (sequenceId != this.currentActionSequence) {
         this.currentActionSequence = sequenceId;
         this.actionAcknowledged = false;
         this.interactionAccepted = null;
         this.packetObserved = false;
      }
   }
// https://t.me/pasterplace

   public synchronized ActionSequenceOutcomeTracker.PacketOutcome classifyPacketOutcome(int packetSequence, boolean shouldForward) {
      if (this.currentActionSequence >= 0 && (this.actionAcknowledged || this.interactionAccepted != null)) {
         if (!this.packetObserved) {
            this.packetObserved = true;
            this.lastPacketSequence = packetSequence;
            return ActionSequenceOutcomeTracker.PacketOutcome.CONTINUE;
         } else if (packetSequence <= this.lastPacketSequence) {
            return ActionSequenceOutcomeTracker.PacketOutcome.CONTINUE;
         } else if (Boolean.FALSE.equals(this.interactionAccepted)) {
            return ActionSequenceOutcomeTracker.PacketOutcome.CANCEL;
         } else if (shouldForward) {
            return ActionSequenceOutcomeTracker.PacketOutcome.FORWARD;
         } else {
            return this.actionAcknowledged ? ActionSequenceOutcomeTracker.PacketOutcome.CANCEL : ActionSequenceOutcomeTracker.PacketOutcome.CONTINUE;
         }
      } else {
         return ActionSequenceOutcomeTracker.PacketOutcome.CONTINUE;
      }
   }
// https://t.me/pasterplace

   public enum PacketOutcome {
      CONTINUE,
      FORWARD,
      CANCEL;

      // $VF: synthetic method
      private static ActionSequenceOutcomeTracker.PacketOutcome[] II1Il11l111II11IIl() {
         return new ActionSequenceOutcomeTracker.PacketOutcome[]{CONTINUE, FORWARD, CANCEL};
      }
   }
}

