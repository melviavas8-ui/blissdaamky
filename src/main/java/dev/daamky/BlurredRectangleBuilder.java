package dev.daamky;

import net.minecraft.client.gui.DrawContext;

public final class BlurredRectangleBuilder {
   private static final float MAX_CORNER_RADIUS = 31.5F;
   private static final float MAX_OPACITY_LEVELS = 31.0F;
   private float x;
   private float y;
   private float width;
   private float height;
   private float topLeftRadius;
   private float topRightRadius;
   private float bottomRightRadius;
   private float bottomLeftRadius;
   private KawaseGaussianBlurMode blurMode = KawaseGaussianBlurMode.KAWASE;
   private int blurPasses = 3;
   private int topLeftArgb;
   private int topRightArgb;
   private int bottomRightArgb;
   private int bottomLeftArgb;
   private float opacity = 1.0F;

   private BlurredRectangleBuilder() {
   }

   public static BlurredRectangleBuilder create() {
      return new BlurredRectangleBuilder();
   }

   public static BlurredRectangleBuilder create(float x, float y, float width, float height) {
      return new BlurredRectangleBuilder().bounds(x, y, width, height);
   }

   public BlurredRectangleBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }

   public BlurredRectangleBuilder size(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
   }

   public BlurredRectangleBuilder bounds(float x, float y, float width, float height) {
      return this.position(x, y).size(width, height);
   }

   public BlurredRectangleBuilder cornerRadius(float radius) {
      return this.cornerRadii(radius, radius, radius, radius);
   }

   public BlurredRectangleBuilder cornerRadii(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
      this.topLeftRadius = topLeftRadius;
      this.topRightRadius = topRightRadius;
      this.bottomRightRadius = bottomRightRadius;
      this.bottomLeftRadius = bottomLeftRadius;
      return this;
   }

   public BlurredRectangleBuilder blurMode(KawaseGaussianBlurMode mode) {
      this.blurMode = mode;
      return this;
   }

   public BlurredRectangleBuilder blurPasses(int passes) {
      this.blurPasses = Math.clamp(passes, 1, 6);
      return this;
   }

   public BlurredRectangleBuilder color(int argbColor) {
      return this.cornerColors(argbColor, argbColor, argbColor, argbColor);
   }

   public BlurredRectangleBuilder color(RgbaColor color) {
      return this.color(color.toArgb());
   }

   public BlurredRectangleBuilder verticalGradientColors(RgbaColor topColor, RgbaColor bottomColor) {
      return this.cornerColors(topColor.toArgb(), topColor.toArgb(), bottomColor.toArgb(), bottomColor.toArgb());
   }

   public BlurredRectangleBuilder cornerColors(int topLeftArgb, int topRightArgb, int bottomRightArgb, int bottomLeftArgb) {
      this.topLeftArgb = topLeftArgb;
      this.topRightArgb = topRightArgb;
      this.bottomRightArgb = bottomRightArgb;
      this.bottomLeftArgb = bottomLeftArgb;
      return this;
   }

   public BlurredRectangleBuilder opacity(float opacity) {
      this.opacity = Math.clamp(opacity, 0.0F, 1.0F);
      return this;
   }

   public BlurredRectangleRenderState build(DrawContext drawContext) {
      float maximumCornerRadius = Math.min(Math.min(this.width, this.height) * 0.5F, 31.5F);
      int encodedTopLeftRadius = encodeCornerRadius(this.topLeftRadius, maximumCornerRadius);
      int encodedTopRightRadius = encodeCornerRadius(this.topRightRadius, maximumCornerRadius);
      int encodedBottomRightRadius = encodeCornerRadius(this.bottomRightRadius, maximumCornerRadius);
      int encodedBottomLeftRadius = encodeCornerRadius(this.bottomLeftRadius, maximumCornerRadius);
      int encodedOpacity = Math.round(this.opacity * 31.0F);
      KawaseGaussianBlurPass blurPass = new KawaseGaussianBlurPass(this.blurMode, this.blurPasses);
      return new BlurredRectangleRenderState(
         blurPass,
         GuiRenderStateBatcher.getMatrix(drawContext),
         this.x,
         this.y,
         this.width,
         this.height,
         encodedTopLeftRadius | encodedTopRightRadius << 6 | this.blurMode.II1Il11l111II11IIl(this.blurPasses) << 12 | (encodedOpacity & 1) << 15,
         encodedBottomLeftRadius | encodedBottomRightRadius << 6 | encodedOpacity >> 1 << 12,
         this.topLeftArgb,
         this.topRightArgb,
         this.bottomRightArgb,
         this.bottomLeftArgb,
         drawContext.scissorStack.peekLast()
      );
   }

   public void render(DrawContext drawContext) {
      if (!(this.width <= 0.0F) && !(this.height <= 0.0F) && !(this.opacity <= 0.0F)) {
         KawaseGaussianBlurPass blurPass = new KawaseGaussianBlurPass(this.blurMode, this.blurPasses);
         KawaseGaussianBlurPass fallbackBlurPass = new KawaseGaussianBlurPass(KawaseGaussianBlurMode.KAWASE, 1);
         InSampler.enqueueBlurPass(blurPass);
         InSampler.enqueueBlurPass(fallbackBlurPass);
         if (InSampler.isBlurPassComplete(blurPass) && InSampler.isBlurPassComplete(fallbackBlurPass)) {
            GuiRenderStateBatcher.addOrBatch(drawContext, this.build(drawContext));
         }
      }
   }

   private static int encodeCornerRadius(float radius, float maximumRadius) {
      return Math.clamp(Math.round(Math.clamp(radius, 0.0F, maximumRadius) * 2.0F), 0, 63);
   }
}

