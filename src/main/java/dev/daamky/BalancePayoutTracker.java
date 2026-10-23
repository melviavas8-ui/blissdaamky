package dev.daamky;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

final class BalancePayoutTracker {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final long PAYOUT_COOLDOWN_MILLIS = 5000L;
   private volatile boolean enabled;
   private volatile String payoutRecipient = "";
   private volatile long minimumPayoutAmount;
   private final Map<String, Long> lastPayoutAtByUsername = new ConcurrentHashMap<>();
// https://t.me/pasterplace

   boolean isPayoutEnabled() {
      return this.enabled;
   }
// https://t.me/pasterplace

   void setPayoutEnabled(boolean enabled) {
      this.enabled = enabled;
   }
// https://t.me/pasterplace

   String getPayoutRecipient() {
      return this.payoutRecipient;
   }
// https://t.me/pasterplace

   void setPayoutRecipient(String payoutRecipient) {
      this.payoutRecipient = payoutRecipient;
   }
// https://t.me/pasterplace

   long getMinimumPayoutAmount() {
      return this.minimumPayoutAmount;
   }
// https://t.me/pasterplace

   void setMinimumPayoutAmount(long minimumPayoutAmount) {
      this.minimumPayoutAmount = minimumPayoutAmount;
   }
// https://t.me/pasterplace

   long getSessionBalance(BotSession disconnect) {
      if (!disconnect.isOnline()) {
         return -1L;
      }

      ClientWorld joined = disconnect.getClientWorld();
      if (joined == null) {
         return -1L;
      }
// https://t.me/pasterplace

      ClientPlayerEntity clientPlayerEntity = disconnect.getSessionPlayer();
      String scoreboardPlayerName = clientPlayerEntity != null ? clientPlayerEntity.getNameForScoreboard() : disconnect.getUsername();
      return ScoreboardBalanceParser.findDisplayedBalance(joined.getScoreboard(), scoreboardPlayerName);
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

   void payCurrentBalances(List<BotSession> items) {
      for (Object session : items) {
         long balance = this.getSessionBalance((BotSession)session);
         if (balance > 0L) {
            this.sendPayout((BotSession)session, balance);
         }
      }
   }
// https://t.me/pasterplace

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

