package dev.daamky;

import net.minecraft.client.gui.DrawContext;

public final class RoundedGradientRectBuilder {
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

   private RoundedGradientRectBuilder() {
   }

   public static RoundedGradientRectBuilder create() {
      return new RoundedGradientRectBuilder();
   }

   public static RoundedGradientRectBuilder at(float x, float y, float width, float height) {
      return new RoundedGradientRectBuilder().bounds(x, y, width, height);
   }

   public RoundedGradientRectBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }

   public RoundedGradientRectBuilder size(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
   }

   public RoundedGradientRectBuilder bounds(float x, float y, float width, float height) {
      return this.position(x, y).size(width, height);
   }

   public RoundedGradientRectBuilder cornerRadius(float cornerRadius) {
      return this.cornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius);
   }

   public RoundedGradientRectBuilder cornerRadii(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
      this.topLeftRadius = topLeftRadius;
      this.topRightRadius = topRightRadius;
      this.bottomRightRadius = bottomRightRadius;
      this.bottomLeftRadius = bottomLeftRadius;
      return this;
   }

   public RoundedGradientRectBuilder topCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.topRightRadius = radius;
      return this;
   }

   public RoundedGradientRectBuilder bottomCornerRadius(float radius) {
      this.bottomLeftRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }

   public RoundedGradientRectBuilder leftCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.bottomLeftRadius = radius;
      return this;
   }

   public RoundedGradientRectBuilder rightCornerRadius(float radius) {
      this.topRightRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }

   public RoundedGradientRectBuilder color(int argbColor) {
      return this.cornerColors(argbColor, argbColor, argbColor, argbColor);
   }

   public RoundedGradientRectBuilder verticalGradient(int topColor, int bottomColor) {
      return this.cornerColors(topColor, topColor, bottomColor, bottomColor);
   }

   public RoundedGradientRectBuilder horizontalGradient(int leftColor, int rightColor) {
      return this.cornerColors(leftColor, rightColor, rightColor, leftColor);
   }

   public RoundedGradientRectBuilder cornerColors(int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
      this.topLeftColor = topLeftColor;
      this.topRightColor = topRightColor;
      this.bottomRightColor = bottomRightColor;
      this.bottomLeftColor = bottomLeftColor;
      return this;
   }

   public RoundedGradientRectBuilder color(RgbaColor color) {
      return this.color(color.toArgb());
   }

   public RoundedGradientRectBuilder verticalGradient(RgbaColor topColor, RgbaColor bottomColor) {
      return this.verticalGradient(topColor.toArgb(), bottomColor.toArgb());
   }

   public RoundedGradientRectBuilder horizontalGradient(RgbaColor leftColor, RgbaColor rightColor) {
      return this.horizontalGradient(leftColor.toArgb(), rightColor.toArgb());
   }

   public RoundedGradientRectBuilder cornerColors(RgbaColor topLeftColor, RgbaColor topRightColor, RgbaColor bottomRightColor, RgbaColor bottomLeftColor) {
      return this.cornerColors(topLeftColor.toArgb(), topRightColor.toArgb(), bottomRightColor.toArgb(), bottomLeftColor.toArgb());
   }

   public RoundedGradientRectBuilder opacity(float opacity) {
      this.opacity = Math.clamp(opacity, 0.0F, 1.0F);
      return this;
   }

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

   public void render(DrawContext drawContext) {
      if (!(this.width <= 0.0F) && !(this.height <= 0.0F) && !(this.opacity <= 0.0F)) {
         GuiRenderStateBatcher.addOrBatch(drawContext, this.toRenderState(drawContext));
      }
   }

   private int applyOpacity(int argbColor) {
      int scaledAlpha = Math.round((argbColor >>> 24) * this.opacity);
      return scaledAlpha << 24 | argbColor & 16777215;
   }

   private static int encodeCornerRadius(float cornerRadius, float maximumCornerRadius) {
      return Math.clamp(Math.round(Math.clamp(cornerRadius, 0.0F, maximumCornerRadius) * 2.0F), 0, 255);
   }
}

