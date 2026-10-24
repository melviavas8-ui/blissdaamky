package dev.daamky;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

final class BalancePayoutTracker {
   private static final long PAYOUT_COOLDOWN_MILLIS = 5000L;
   private volatile boolean enabled;
   private volatile String payoutRecipient = "";
   private volatile long minimumPayoutAmount;
   private final Map<String, Long> lastPayoutAtByUsername = new ConcurrentHashMap<>();

   boolean isPayoutEnabled() {
      return this.enabled;
   }

   void setPayoutEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   String getPayoutRecipient() {
      return this.payoutRecipient;
   }

   void setPayoutRecipient(String payoutRecipient) {
      this.payoutRecipient = payoutRecipient;
   }

   long getMinimumPayoutAmount() {
      return this.minimumPayoutAmount;
   }

   void setMinimumPayoutAmount(long minimumPayoutAmount) {
      this.minimumPayoutAmount = minimumPayoutAmount;
   }

   long getSessionBalance(BotSession disconnect) {
      if (!disconnect.isOnline()) {
         return -1L;
      }

      ClientWorld joined = disconnect.getClientWorld();
      if (joined == null) {
         return -1L;
      }

      ClientPlayerEntity clientPlayerEntity = disconnect.getSessionPlayer();
      String scoreboardPlayerName = clientPlayerEntity != null ? clientPlayerEntity.getNameForScoreboard() : disconnect.getUsername();
      return ScoreboardBalanceParser.findDisplayedBalance(joined.getScoreboard(), scoreboardPlayerName);
   }

   void processPayouts(List<BotSession> items) {
      if (this.enabled) {
         for (Object session : items) {
            long balance = this.getSessionBalance((BotSession)session);
            long payableAmount = balance - Math.max(0L, this.minimumPayoutAmount);
            if (balance > 0L && payableAmount > 0L) {
               this.sendPayout((BotSession)session, payableAmount);
            }
         }
      }
   }

   void payCurrentBalances(List<BotSession> items) {
      for (Object session : items) {
         long balance = this.getSessionBalance((BotSession)session);
         if (balance > 0L) {
            this.sendPayout((BotSession)session, balance);
         }
      }
   }

   private void sendPayout(BotSession disconnect, long payoutAmount) {
      String recipientUsername = this.payoutRecipient == null ? "" : this.payoutRecipient.trim();
      if (!recipientUsername.isEmpty() && !recipientUsername.equalsIgnoreCase(disconnect.getUsername()) && payoutAmount > 0L) {
         long payoutAttemptMillis = System.currentTimeMillis();
         Long lastSent = this.lastPayoutAtByUsername.get(disconnect.getUsername());
         if (lastSent == null || payoutAttemptMillis - lastSent >= 5000L) {
            if (disconnect.sendChatMessage("/pay " + recipientUsername + " " + payoutAmount)) {
               this.lastPayoutAtByUsername.put(disconnect.getUsername(), payoutAttemptMillis);
            }
         }
      }
   }
}

