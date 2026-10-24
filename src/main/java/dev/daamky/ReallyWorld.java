package dev.daamky;

public final class ReallyWorld extends MultiOptionSetting {
   public ReallyWorld() {
      super("Swap Mode", "ReallyWorld", "Normal", "ReallyWorld", "FunTime");
   }

   public Priority.PriorityCategory getInventorySwapCategory() {
      return switch ((String)this.getValue()) {
         case "ReallyWorld" -> Priority.PriorityCategory.REALLYWORLD;
         case "FunTime" -> Priority.PriorityCategory.FUNTIME;
         default -> Priority.PriorityCategory.DEFAULT;
      };
   }
}

