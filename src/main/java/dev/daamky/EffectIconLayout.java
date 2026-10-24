package dev.daamky;

record EffectIconLayout(int left, int top, boolean width, boolean height) {

   EffectIconLayout(int left, int top, boolean width, boolean height) {
      this.left = left;
      this.top = top;
      this.width = width;
      this.height = height;
   }
}

