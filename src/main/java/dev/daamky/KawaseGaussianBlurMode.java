package dev.daamky;

public enum KawaseGaussianBlurMode {
   KAWASE,
   GAUSSIAN,
   BOX;

   public static final int l1lll11l1l = 6;

   public int II1Il11l111II11IIl(int inputSize) {
      return this == KAWASE ? 1 : (inputSize + 1) / 2;
   }

   public int l1I1IlllIlI(int inputSize) {
      return this == KAWASE ? inputSize : this.II1Il11l111II11IIl(inputSize);
   }

   // $VF: synthetic method
   private static KawaseGaussianBlurMode[] II1Il11l111II11IIl() {
      return new KawaseGaussianBlurMode[]{KAWASE, GAUSSIAN, BOX};
   }
}
