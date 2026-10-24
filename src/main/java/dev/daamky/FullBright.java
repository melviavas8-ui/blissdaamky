package dev.daamky;

public final class FullBright extends ModuleStructure {
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "Ночное зрение", "Ночное зрение", "Гамма");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Яркость", 10.0F, 1.0F, 15.0F, 0.5F);

   public FullBright() {
      super("FullBright", "Позволяет видеть в темноте", ModuleCategory.RENDER);
      this.IllI1l1l1.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Гамма"));
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }

   public static FullBright l1I1IIl111Il1l1l() {
      return ModuleRegistry.getInstance().getModuleByClass(FullBright.class);
   }

   public static float IllI1l1l1(float originalValue) {
      FullBright fullBright = l1I1IIl111Il1l1l();
      return fullBright != null && fullBright.isEnabled() && !fullBright.l1I1IlllIlI.isSelected("Гамма") ? 1.0F : originalValue;
   }

   public static float l1lll11l1l(float originalValue) {
      FullBright fullBright = l1I1IIl111Il1l1l();
      return fullBright != null && fullBright.isEnabled() ? 0.0F : originalValue;
   }

   public static float lIIl11l111lIIl1ll(float originalValue) {
      FullBright fullBright = l1I1IIl111Il1l1l();
      return fullBright != null && fullBright.isEnabled() && fullBright.l1I1IlllIlI.isSelected("Гамма") ? fullBright.IllI1l1l1.getValue() : originalValue;
   }
}

