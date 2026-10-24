package dev.daamky;

public record KawaseGaussianBlurPass(KawaseGaussianBlurMode mode, int passIndex) {

   public KawaseGaussianBlurPass(KawaseGaussianBlurMode mode, int passIndex) {
      passIndex = Math.clamp(passIndex, 1, 6);
      this.mode = mode;
      this.passIndex = passIndex;
   }
}

