package dev.daamky;

import net.minecraft.client.gui.DrawContext;

public final class RoundedRectBuilder {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float MAX_CORNER_RADIUS = 31.5F;
   private static final float BORDER_WIDTH_PACKING_SCALE = 16.0F;
   private float x;
   private float y;
   private float width;
   private float height;
   private float borderWidth = 1.0F;
   private float topLeftRadius;
   private float topRightRadius;
   private float bottomRightRadius;
   private float bottomLeftRadius;
   private int topLeftColor = -1;
   private int topRightColor = -1;
   private int bottomRightColor = -1;
   private int bottomLeftColor = -1;
   private float alpha = 1.0F;
// https://t.me/pasterplace

   private RoundedRectBuilder() {
   }
// https://t.me/pasterplace

   public static RoundedRectBuilder create() {
      return new RoundedRectBuilder();
   }
// https://t.me/pasterplace

   public static RoundedRectBuilder createAt(float x, float y, float width, float height) {
      return new RoundedRectBuilder().bounds(x, y, width, height);
   }
// https://t.me/pasterplace

   public RoundedRectBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }
// https://t.me/pasterplace

   public RoundedRectBuilder size(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
   }
// https://t.me/pasterplace

   public RoundedRectBuilder bounds(float x, float y, float width, float height) {
      return this.position(x, y).size(width, height);
   }
// https://t.me/pasterplace

   public RoundedRectBuilder borderWidth(float borderWidth) {
      this.borderWidth = Math.clamp(borderWidth, 0.0F, 15.9375F);
      return this;
   }
// https://t.me/pasterplace

   public RoundedRectBuilder cornerRadius(float radius) {
      return this.cornerRadii(radius, radius, radius, radius);
   }
// https://t.me/pasterplace

   public RoundedRectBuilder cornerRadii(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
      this.topLeftRadius = topLeftRadius;
      this.topRightRadius = topRightRadius;
      this.bottomRightRadius = bottomRightRadius;
      this.bottomLeftRadius = bottomLeftRadius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedRectBuilder topCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.topRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedRectBuilder bottomCornerRadius(float radius) {
      this.bottomLeftRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedRectBuilder leftCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.bottomLeftRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedRectBuilder rightCornerRadius(float radius) {
      this.topRightRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public RoundedRectBuilder color(int argbColor) {
      return this.cornerColors(argbColor, argbColor, argbColor, argbColor);
   }
// https://t.me/pasterplace

   public RoundedRectBuilder color(RgbaColor color) {
      return this.color(color.toArgb());
   }
// https://t.me/pasterplace

   public RoundedRectBuilder verticalGradient(int topColor, int bottomColor) {
      return this.cornerColors(topColor, topColor, bottomColor, bottomColor);
   }
// https://t.me/pasterplace

   public RoundedRectBuilder verticalGradient(RgbaColor topColor, RgbaColor bottomColor) {
      return this.verticalGradient(topColor.toArgb(), bottomColor.toArgb());
   }
// https://t.me/pasterplace

   public RoundedRectBuilder horizontalGradient(int leftColor, int rightColor) {
      return this.cornerColors(leftColor, rightColor, rightColor, leftColor);
   }
// https://t.me/pasterplace

   public RoundedRectBuilder horizontalGradient(RgbaColor leftColor, RgbaColor rightColor) {
      return this.horizontalGradient(leftColor.toArgb(), rightColor.toArgb());
   }
// https://t.me/pasterplace

   public RoundedRectBuilder cornerColors(int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
      this.topLeftColor = topLeftColor;
      this.topRightColor = topRightColor;
      this.bottomRightColor = bottomRightColor;
      this.bottomLeftColor = bottomLeftColor;
      return this;
   }
// https://t.me/pasterplace

   public RoundedRectBuilder cornerColors(RgbaColor topLeftColor, RgbaColor topRightColor, RgbaColor bottomRightColor, RgbaColor bottomLeftColor) {
      return this.cornerColors(topLeftColor.toArgb(), topRightColor.toArgb(), bottomRightColor.toArgb(), bottomLeftColor.toArgb());
   }
// https://t.me/pasterplace

   public RoundedRectBuilder alpha(float alpha) {
      this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
      return this;
   }
// https://t.me/pasterplace

   public RoundedQuadRenderState buildRenderState(DrawContext drawContext) {
      float maximumEncodedRadius = Math.min(Math.min(this.width, this.height) * 0.5F, 31.5F);
      int topLeftRadiusCode = encodeCornerRadius(this.topLeftRadius, maximumEncodedRadius);
      int topRightRadiusCode = encodeCornerRadius(this.topRightRadius, maximumEncodedRadius);
      int bottomRightRadiusCode = encodeCornerRadius(this.bottomRightRadius, maximumEncodedRadius);
      int bottomLeftRadiusCode = encodeCornerRadius(this.bottomLeftRadius, maximumEncodedRadius);
      int packedBorderWidth = Math.clamp(Math.round(this.borderWidth * 16.0F), 0, 255);
      return new RoundedQuadRenderState(
         GuiRenderStateBatcher.getMatrix(drawContext),
         this.x,
         this.y,
         this.width,
         this.height,
         topLeftRadiusCode | topRightRadiusCode << 6 | (packedBorderWidth & 15) << 12,
         bottomLeftRadiusCode | bottomRightRadiusCode << 6 | packedBorderWidth >> 4 << 12,
         this.applyAlpha(this.topLeftColor),
         this.applyAlpha(this.topRightColor),
         this.applyAlpha(this.bottomRightColor),
         this.applyAlpha(this.bottomLeftColor),
         drawContext.scissorStack.peekLast()
      );
   }
// https://t.me/pasterplace

   public void render(DrawContext drawContext) {
      if (!(this.width <= 0.0F) && !(this.height <= 0.0F) && !(this.borderWidth <= 0.0F) && !(this.alpha <= 0.0F)) {
         GuiRenderStateBatcher.addOrBatch(drawContext, this.buildRenderState(drawContext));
      }
   }
// https://t.me/pasterplace

   private int applyAlpha(int argbColor) {
      int adjustedAlpha = Math.round((argbColor >>> 24) * this.alpha);
      return adjustedAlpha << 24 | argbColor & 16777215;
   }
// https://t.me/pasterplace

   private static int encodeCornerRadius(float radius, float maximumRadius) {
      return Math.clamp(Math.round(Math.clamp(radius, 0.0F, maximumRadius) * 2.0F), 0, 63);
   }
}

