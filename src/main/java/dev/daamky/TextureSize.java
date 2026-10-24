package dev.daamky;

public record TextureSize(int width, int height) {

   public TextureSize(int width, int height) {
      this.width = width;
      this.height = height;
   }

   public float II1Il11l111II11IIl() {
      return this.height > 0 ? (float)this.width / this.height : 1.0F;
   }

   public int l1I1IlllIlI() {
      return this.width;
   }

   public int IllI1l1l1() {
      return this.height;
   }
}

