package dev.daamky;

public final class FullBright extends ModuleStructure {
    private static final String p135b700f = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "Ночное зрение", "Ночное зрение", "Гамма");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Яркость", 10.0F, 1.0F, 15.0F, 0.5F);
// https://t.me/pasterplace

   public FullBright() {
      super("FullBright", "Позволяет видеть в темноте", ModuleCategory.RENDER);
      this.IllI1l1l1.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Гамма"));
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   public static FullBright l1I1IIl111Il1l1l() {
      return ModuleRegistry.getInstance().getModuleByClass(FullBright.class);
   }
// https://t.me/pasterplace

   public static float IllI1l1l1(float originalValue) {
      FullBright fullBright = l1I1IIl111Il1l1l();
      return fullBright != null && fullBright.isEnabled() && !fullBright.l1I1IlllIlI.isSelected("Гамма") ? 1.0F : originalValue;
   }
// https://t.me/pasterplace

   public static float l1lll11l1l(float originalValue) {
      FullBright fullBright = l1I1IIl111Il1l1l();
      return fullBright != null && fullBright.isEnabled() ? 0.0F : originalValue;
   }
// https://t.me/pasterplace

   public static float lIIl11l111lIIl1ll(float originalValue) {
      FullBright fullBright = l1I1IIl111Il1l1l();
      return fullBright != null && fullBright.isEnabled() && fullBright.l1I1IlllIlI.isSelected("Гамма") ? fullBright.IllI1l1l1.getValue() : originalValue;
   }
}

