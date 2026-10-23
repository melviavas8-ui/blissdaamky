package dev.daamky;

public enum KawaseGaussianBlurMode {
   KAWASE,
   GAUSSIAN,
   BOX;
// https://t.me/pasterplace

   public static final int l1lll11l1l = 6;
// https://t.me/pasterplace

   public int II1Il11l111II11IIl(int inputSize) {
      return this == KAWASE ? 1 : (inputSize + 1) / 2;
   }
// https://t.me/pasterplace

   public int l1I1IlllIlI(int inputSize) {
      return this == KAWASE ? inputSize : this.II1Il11l111II11IIl(inputSize);
   }
// https://t.me/pasterplace

   // $VF: synthetic method
   private static KawaseGaussianBlurMode[] II1Il11l111II11IIl() {
      return new KawaseGaussianBlurMode[]{KAWASE, GAUSSIAN, BOX};
   }
}
