package dev.daamky;

import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Optimizer extends ModuleStructure {
   public Optimizer() {
      super("Optimizer", "Оптимизация клиента ZakoOpt (автор Zako, https://t.me/StarikZako, LGPL-3.0)", ModuleCategory.RENDER);
      this.setEnabled(true);
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      setOptimizerEnabled(true);
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      setOptimizerEnabled(false);
   }

   private static void setOptimizerEnabled(boolean enabled) {
   }

   public static boolean shouldSimplifyEntityRender(double squaredDistanceToCamera, boolean isPlayer) {
      return false;
   }

   public static boolean shouldCullParticleAt(double x, double y, double z) {
      return false;
   }

   public static boolean shouldSkipParticlePhysicsAt(double x, double y, double z) {
      return false;
   }

   public static boolean shouldCullSpawnerAt(double x, double y, double z) {
      return false;
   }
}

