package dev.daamky;

public final class Beautifuly extends ModuleStructure {
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Насыщенность", 1.3F, 0.0F, 3.0F, 0.05F);
   private final FloatSetting IllI1l1l1 = new FloatSetting("Теплота", 0.3F, -1.0F, 1.0F, 0.05F);

   public Beautifuly() {
      super("Beautifuly", "Насыщенность и теплота цветов", ModuleCategory.RENDER);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }

   public static Beautifuly l1I1IIl111Il1l1l() {
      return ModuleRegistry.getInstance().getModuleByClass(Beautifuly.class);
   }

   public float IIIlll111l111() {
      return this.l1I1IlllIlI.getValue();
   }

   public float lI1IlIl1I11II111l11l() {
      return this.IllI1l1l1.getValue();
   }
}

