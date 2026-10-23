package dev.daamky;

import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Panic extends ModuleStructure {
    private static final String p2be85e01 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public Panic() {
      super("Panic", "Instantly disables all modules", ModuleCategory.WORLD);
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      for (Object module : ModuleRegistry.getInstance().getModules()) {
         if (module != this && ((ModuleStructure)module).isEnabled()) {
            ((ModuleStructure)module).setEnabled(false);
         }
      }
// https://t.me/pasterplace

      ClientChatLogger.II1Il11l111II11IIl("All modules disabled");
      this.setEnabled(false);
   }
}

