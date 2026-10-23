package dev.daamky;

public final class InvisibleOpacity extends ModuleStructure {
    private static final String p28a08b7a = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Прозрачность", 0.35F, 0.05F, 1.0F, 0.05F);
// https://t.me/pasterplace

   public InvisibleOpacity() {
      super("InvisibleOpacity", "Делает невидимых игроков полупрозрачными", ModuleCategory.RENDER);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   public static boolean l1I1IIl111Il1l1l() {
      ModuleStructure moduleStructure = ModuleRegistry.getInstance().getModuleByClass(InvisibleOpacity.class);
      return moduleStructure != null && moduleStructure.isEnabled();
   }
// https://t.me/pasterplace

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

