package dev.daamky;

import net.minecraft.client.gui.DrawContext;

public final class KawaseBlurBuilder {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
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
// https://t.me/pasterplace

   private KawaseBlurBuilder() {
   }
// https://t.me/pasterplace

   public static KawaseBlurBuilder create() {
      return new KawaseBlurBuilder();
   }
// https://t.me/pasterplace

   public static KawaseBlurBuilder create(float x, float y, float width, float height) {
      return new KawaseBlurBuilder().bounds(x, y, width, height);
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder size(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder bounds(float x, float y, float width, float height) {
      return this.position(x, y).size(width, height);
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder cornerRadius(float radius) {
      return this.cornerRadii(radius, radius, radius, radius);
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder cornerRadii(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
      this.topLeftRadius = topLeftRadius;
      this.topRightRadius = topRightRadius;
      this.bottomRightRadius = bottomRightRadius;
      this.bottomLeftRadius = bottomLeftRadius;
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder topCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.topRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder bottomCornerRadius(float radius) {
      this.bottomLeftRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder leftCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.bottomLeftRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder rightCornerRadius(float radius) {
      this.topRightRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder blurMode(KawaseGaussianBlurMode mode) {
      this.blurMode = mode;
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder blurPasses(int passes) {
      this.blurPasses = Math.clamp(passes, 1, 6);
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder color(int argbColor) {
      return this.cornerColors(argbColor, argbColor, argbColor, argbColor);
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder color(RgbaColor color) {
      return this.color(color.toArgb());
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder verticalGradientColors(RgbaColor topColor, RgbaColor bottomColor) {
      return this.cornerColors(topColor.toArgb(), topColor.toArgb(), bottomColor.toArgb(), bottomColor.toArgb());
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder horizontalGradientColors(RgbaColor leftColor, RgbaColor rightColor) {
      return this.cornerColors(leftColor.toArgb(), rightColor.toArgb(), rightColor.toArgb(), leftColor.toArgb());
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder cornerColors(int topLeftArgb, int topRightArgb, int bottomRightArgb, int bottomLeftArgb) {
      this.topLeftArgb = topLeftArgb;
      this.topRightArgb = topRightArgb;
      this.bottomRightArgb = bottomRightArgb;
      this.bottomLeftArgb = bottomLeftArgb;
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurBuilder opacity(float opacity) {
      this.opacity = Math.clamp(opacity, 0.0F, 1.0F);
      return this;
   }
// https://t.me/pasterplace

   public KawaseBlurRenderState build(DrawContext drawContext) {
      float maximumCornerRadius = Math.min(Math.min(this.width, this.height) * 0.5F, 31.5F);
      int encodedTopLeftRadius = encodeCornerRadius(this.topLeftRadius, maximumCornerRadius);
      int encodedTopRightRadius = encodeCornerRadius(this.topRightRadius, maximumCornerRadius);
      int encodedBottomRightRadius = encodeCornerRadius(this.bottomRightRadius, maximumCornerRadius);
      int encodedBottomLeftRadius = encodeCornerRadius(this.bottomLeftRadius, maximumCornerRadius);
      int encodedOpacity = Math.round(this.opacity * 31.0F);
      KawaseGaussianBlurPass blurPass = new KawaseGaussianBlurPass(this.blurMode, this.blurPasses);
      return new KawaseBlurRenderState(
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
// https://t.me/pasterplace

   public void render(DrawContext drawContext) {
      if (!(this.width <= 0.0F) && !(this.height <= 0.0F) && !(this.opacity <= 0.0F)) {
         KawaseGaussianBlurPass blurPass = new KawaseGaussianBlurPass(this.blurMode, this.blurPasses);
         InSampler.enqueueBlurPass(blurPass);
         if (InSampler.isBlurPassComplete(blurPass)) {
            GuiRenderStateBatcher.addOrBatch(drawContext, this.build(drawContext));
         }
      }
   }
// https://t.me/pasterplace

   private static int encodeCornerRadius(float radius, float maximumRadius) {
      return Math.clamp(Math.round(Math.clamp(radius, 0.0F, maximumRadius) * 2.0F), 0, 63);
   }
}

