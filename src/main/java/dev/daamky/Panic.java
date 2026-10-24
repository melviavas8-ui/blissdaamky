package dev.daamky;

import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Panic extends ModuleStructure {
   public Panic() {
      super("Panic", "Instantly disables all modules", ModuleCategory.WORLD);
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      for (Object module : ModuleRegistry.getInstance().getModules()) {
         if (module != this && ((ModuleStructure)module).isEnabled()) {
            ((ModuleStructure)module).setEnabled(false);
         }
      }

      ClientChatLogger.II1Il11l111II11IIl("All modules disabled");
      this.setEnabled(false);
   }
}

