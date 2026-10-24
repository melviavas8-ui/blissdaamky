package dev.daamky;

public final class ActionSequenceOutcomeTracker {
   private int currentActionSequence = -1;
   private boolean actionAcknowledged;
   private Boolean interactionAccepted;
   private boolean packetObserved;
   private int lastPacketSequence;

   public synchronized void acknowledgeThrough(int sequenceId) {
      if (this.currentActionSequence >= 0 && sequenceId >= this.currentActionSequence) {
         this.actionAcknowledged = true;
      }
   }

   public synchronized void recordInteractionResult(boolean accepted) {
      if (this.currentActionSequence >= 0 && !Boolean.FALSE.equals(this.interactionAccepted)) {
         this.interactionAccepted = accepted;
      }
   }

   public synchronized void beginActionSequence(int sequenceId) {
      if (sequenceId < 0) {
         throw new IllegalArgumentException("An action sequence must be non-negative");
      }

      if (sequenceId != this.currentActionSequence) {
         this.currentActionSequence = sequenceId;
         this.actionAcknowledged = false;
         this.interactionAccepted = null;
         this.packetObserved = false;
      }
   }

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

