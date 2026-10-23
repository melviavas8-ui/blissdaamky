package dev.daamky;

public final class ReallyWorld extends MultiOptionSetting {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public ReallyWorld() {
      super("Swap Mode", "ReallyWorld", "Normal", "ReallyWorld", "FunTime");
   }
// https://t.me/pasterplace

   public Priority.PriorityCategory getInventorySwapCategory() {
      return switch ((String)this.getValue()) {
         case "ReallyWorld" -> Priority.PriorityCategory.REALLYWORLD;
         case "FunTime" -> Priority.PriorityCategory.FUNTIME;
         default -> Priority.PriorityCategory.DEFAULT;
      };
   }
}

