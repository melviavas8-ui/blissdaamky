package dev.daamky;

public final class Beautifuly extends ModuleStructure {
    private static final String p940adb50 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Насыщенность", 1.3F, 0.0F, 3.0F, 0.05F);
   private final FloatSetting IllI1l1l1 = new FloatSetting("Теплота", 0.3F, -1.0F, 1.0F, 0.05F);
// https://t.me/pasterplace

   public Beautifuly() {
      super("Beautifuly", "Насыщенность и теплота цветов", ModuleCategory.RENDER);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   public static Beautifuly l1I1IIl111Il1l1l() {
      return ModuleRegistry.getInstance().getModuleByClass(Beautifuly.class);
   }
// https://t.me/pasterplace

   public float IIIlll111l111() {
      return this.l1I1IlllIlI.getValue();
   }
// https://t.me/pasterplace

   public float lI1IlIl1I11II111l11l() {
      return this.IllI1l1l1.getValue();
   }
}

