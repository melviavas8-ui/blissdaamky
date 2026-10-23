package dev.daamky;

import net.minecraft.client.gui.DrawContext;

public final class RoundedGradientRectBuilder {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private float x;
   private float y;
   private float width;
   private float height;
   private float topLeftRadius;
   private float topRightRadius;
   private float bottomRightRadius;
   private float bottomLeftRadius;
   private int topLeftColor = -1;
   private int topRightColor = -1;
   private int bottomRightColor = -1;
   private int bottomLeftColor = -1;
   private float opacity = 1.0F;
// https://t.me/pasterplace

   private RoundedGradientRectBuilder() {
   }
// https://t.me/pasterplace

   public static RoundedGradientRectBuilder create() {
      return new RoundedGradientRectBuilder();
   }
// https://t.me/pasterplace

   public static RoundedGradientRectBuilder at(float x, float y, float width, float height) {
      return new RoundedGradientRectBuilder().bounds(x, y, width, height);
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder size(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder bounds(float x, float y, float width, float height) {
      return this.position(x, y).size(width, height);
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder cornerRadius(float cornerRadius) {
      return this.cornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius);
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder cornerRadii(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
      this.topLeftRadius = topLeftRadius;
      this.topRightRadius = topRightRadius;
      this.bottomRightRadius = bottomRightRadius;
      this.bottomLeftRadius = bottomLeftRadius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder topCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.topRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder bottomCornerRadius(float radius) {
      this.bottomLeftRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder leftCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.bottomLeftRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder rightCornerRadius(float radius) {
      this.topRightRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder color(int argbColor) {
      return this.cornerColors(argbColor, argbColor, argbColor, argbColor);
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder verticalGradient(int topColor, int bottomColor) {
      return this.cornerColors(topColor, topColor, bottomColor, bottomColor);
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder horizontalGradient(int leftColor, int rightColor) {
      return this.cornerColors(leftColor, rightColor, rightColor, leftColor);
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder cornerColors(int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
      this.topLeftColor = topLeftColor;
      this.topRightColor = topRightColor;
      this.bottomRightColor = bottomRightColor;
      this.bottomLeftColor = bottomLeftColor;
      return this;
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder color(RgbaColor color) {
      return this.color(color.toArgb());
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder verticalGradient(RgbaColor topColor, RgbaColor bottomColor) {
      return this.verticalGradient(topColor.toArgb(), bottomColor.toArgb());
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder horizontalGradient(RgbaColor leftColor, RgbaColor rightColor) {
      return this.horizontalGradient(leftColor.toArgb(), rightColor.toArgb());
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder cornerColors(RgbaColor topLeftColor, RgbaColor topRightColor, RgbaColor bottomRightColor, RgbaColor bottomLeftColor) {
      return this.cornerColors(topLeftColor.toArgb(), topRightColor.toArgb(), bottomRightColor.toArgb(), bottomLeftColor.toArgb());
   }
// https://t.me/pasterplace

   public RoundedGradientRectBuilder opacity(float opacity) {
      this.opacity = Math.clamp(opacity, 0.0F, 1.0F);
      return this;
   }
// https://t.me/pasterplace

   public GradientRectangleRenderState toRenderState(DrawContext drawContext) {
      float maximumCornerRadius = Math.min(this.width, this.height) * 0.5F;
      int topLeftRadiusByte = encodeCornerRadius(this.topLeftRadius, maximumCornerRadius);
      int topRightRadiusByte = encodeCornerRadius(this.topRightRadius, maximumCornerRadius);
      int bottomRightRadiusByte = encodeCornerRadius(this.bottomRightRadius, maximumCornerRadius);
      int bottomLeftRadiusByte = encodeCornerRadius(this.bottomLeftRadius, maximumCornerRadius);
      return new GradientRectangleRenderState(
         GuiRenderStateBatcher.getMatrix(drawContext),
         this.x,
         this.y,
         this.width,
         this.height,
         topLeftRadiusByte | topRightRadiusByte << 8,
         bottomLeftRadiusByte | bottomRightRadiusByte << 8,
         this.applyOpacity(this.topLeftColor),
         this.applyOpacity(this.topRightColor),
         this.applyOpacity(this.bottomRightColor),
         this.applyOpacity(this.bottomLeftColor),
         drawContext.scissorStack.peekLast()
      );
   }
// https://t.me/pasterplace

   public void render(DrawContext drawContext) {
      if (!(this.width <= 0.0F) && !(this.height <= 0.0F) && !(this.opacity <= 0.0F)) {
         GuiRenderStateBatcher.addOrBatch(drawContext, this.toRenderState(drawContext));
      }
   }
// https://t.me/pasterplace

   private int applyOpacity(int argbColor) {
      int scaledAlpha = Math.round((argbColor >>> 24) * this.opacity);
      return scaledAlpha << 24 | argbColor & 16777215;
   }
// https://t.me/pasterplace

   private static int encodeCornerRadius(float cornerRadius, float maximumCornerRadius) {
      return Math.clamp(Math.round(Math.clamp(cornerRadius, 0.0F, maximumCornerRadius) * 2.0F), 0, 255);
   }
}

