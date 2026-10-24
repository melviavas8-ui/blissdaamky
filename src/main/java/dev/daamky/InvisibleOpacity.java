package dev.daamky;

public final class InvisibleOpacity extends ModuleStructure {
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Прозрачность", 0.35F, 0.05F, 1.0F, 0.05F);

   public InvisibleOpacity() {
      super("InvisibleOpacity", "Делает невидимых игроков полупрозрачными", ModuleCategory.RENDER);
      this.addSetting(this.l1I1IlllIlI);
   }

   public static boolean l1I1IIl111Il1l1l() {
      ModuleStructure moduleStructure = ModuleRegistry.getInstance().getModuleByClass(InvisibleOpacity.class);
      return moduleStructure != null && moduleStructure.isEnabled();
   }

   public static int l1I1IlllIlI(int originalColor) {
      InvisibleOpacity module = (InvisibleOpacity)ModuleRegistry.getInstance().getModuleByClass(InvisibleOpacity.class);
      if (module != null && module.isEnabled()) {
         int opacityAlpha = Math.round(Math.clamp(module.l1I1IlllIlI.getValue(), 0.0F, 1.0F) * 255.0F);
         return opacityAlpha << 24 | 16777215;
      } else {
         return originalColor;
      }
   }
}

