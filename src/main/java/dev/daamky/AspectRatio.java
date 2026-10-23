package dev.daamky;

public final class AspectRatio extends ModuleStructure {
    private static final String pcb494965 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Соотношение", "4:3", "4:3", "16:9", "1:1", "16:10", "Кастомное");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Значение", 1.0F, 0.1F, 5.0F, 0.05F);
   private final BooleanSetting l1lll11l1l = new BooleanSetting("Растягивать предметы", true);
// https://t.me/pasterplace

   public AspectRatio() {
      super("AspectRatio", "Изменяет соотношение сторон экрана", ModuleCategory.RENDER);
      this.IllI1l1l1.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Кастомное"));
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l);
   }
// https://t.me/pasterplace

   public static float II1Il11l111II11IIl(float fallbackWidth, float referenceHeight) {
      AspectRatio moduleStructure = (AspectRatio)ModuleRegistry.getInstance().getModuleByClass(AspectRatio.class);
      return moduleStructure != null && moduleStructure.isEnabled() && !(referenceHeight <= 0.0F) ? referenceHeight * moduleStructure.l1I1IIl111Il1l1l() : fallbackWidth;
   }
// https://t.me/pasterplace

   public static float l1I1IlllIlI(float fallbackWidth, float referenceHeight) {
      AspectRatio moduleStructure = (AspectRatio)ModuleRegistry.getInstance().getModuleByClass(AspectRatio.class);
      return moduleStructure != null && moduleStructure.isEnabled() && moduleStructure.l1lll11l1l.getValue() && !(referenceHeight <= 0.0F)
         ? referenceHeight * moduleStructure.l1I1IIl111Il1l1l()
         : fallbackWidth;
   }
// https://t.me/pasterplace

   private float l1I1IIl111Il1l1l() {
      return switch ((String)this.l1I1IlllIlI.getValue()) {
         case "4:3" -> 1.3333334F;
         case "16:9" -> 1.7777778F;
         case "1:1" -> 1.0F;
         case "16:10" -> 1.6F;
         default -> this.IllI1l1l1.getValue();
      };
   }
}

