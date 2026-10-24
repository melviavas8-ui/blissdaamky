package dev.daamky;

import net.minecraft.client.gui.DrawContext;

public final class RoundedOutlineBuilder {
   private static final float MAX_CORNER_RADIUS = 31.0F;
   private static final float MAX_SHADOW_SPREAD = 4.0F;
   private static final float SHADOW_SPREAD_STEP = 0.2F;
   private float x;
   private float y;
   private float width;
   private float height;
   private float topLeftRadius;
   private float topRightRadius;
   private float bottomRightRadius;
   private float bottomLeftRadius;
   private float outlineWidth = 8.0F;
   private float outlineOutset;
   private float offsetX;
   private float offsetY;
   private float shadowStrength = 1.0F;
   private int topLeftColor = -16777216;
   private int topRightColor = -16777216;
   private int bottomRightColor = -16777216;
   private int bottomLeftColor = -16777216;
   private float opacity = 1.0F;

   private RoundedOutlineBuilder() {
   }

   public static RoundedOutlineBuilder create() {
      return new RoundedOutlineBuilder();
   }

   public static RoundedOutlineBuilder at(float x, float y, float width, float height) {
      return new RoundedOutlineBuilder().bounds(x, y, width, height);
   }

   public RoundedOutlineBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }

   public RoundedOutlineBuilder size(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
   }

   public RoundedOutlineBuilder bounds(float x, float y, float width, float height) {
      return this.position(x, y).size(width, height);
   }

   public RoundedOutlineBuilder cornerRadius(float radius) {
      return this.cornerRadii(radius, radius, radius, radius);
   }

   public RoundedOutlineBuilder cornerRadii(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
      this.topLeftRadius = topLeftRadius;
      this.topRightRadius = topRightRadius;
      this.bottomRightRadius = bottomRightRadius;
      this.bottomLeftRadius = bottomLeftRadius;
      return this;
   }

   public RoundedOutlineBuilder topCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.topRightRadius = radius;
      return this;
   }

   public RoundedOutlineBuilder bottomCornerRadius(float radius) {
      this.bottomLeftRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }

   public RoundedOutlineBuilder leftCornerRadius(float radius) {
      this.topLeftRadius = radius;
      this.bottomLeftRadius = radius;
      return this;
   }

   public RoundedOutlineBuilder rightCornerRadius(float radius) {
      this.topRightRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }

   public RoundedOutlineBuilder outlineWidth(float width) {
      this.outlineWidth = Math.clamp(width, 0.0F, 63.5F);
      return this;
   }

   public RoundedOutlineBuilder outlineOutset(float outset) {
      this.outlineOutset = outset;
      return this;
   }

   public RoundedOutlineBuilder offset(float x, float y) {
      this.offsetX = x;
      this.offsetY = y;
      return this;
   }

   public RoundedOutlineBuilder shadowStrength(float strength) {
      this.shadowStrength = Math.clamp(strength, 0.0F, 4.0F);
      return this;
   }

   public RoundedOutlineBuilder color(int color) {
      return this.cornerColors(color, color, color, color);
   }

   public RoundedOutlineBuilder verticalGradient(int topColor, int bottomColor) {
      return this.cornerColors(topColor, topColor, bottomColor, bottomColor);
   }

   public RoundedOutlineBuilder horizontalGradient(int leftColor, int rightColor) {
      return this.cornerColors(leftColor, rightColor, rightColor, leftColor);
   }

   public RoundedOutlineBuilder cornerColors(int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
      this.topLeftColor = topLeftColor;
      this.topRightColor = topRightColor;
      this.bottomRightColor = bottomRightColor;
      this.bottomLeftColor = bottomLeftColor;
      return this;
   }

   public RoundedOutlineBuilder color(RgbaColor l1I1IlllIlI4) {
      return this.color(l1I1IlllIlI4.toArgb());
   }

   public RoundedOutlineBuilder verticalGradient(RgbaColor l1I1IlllIlI4, RgbaColor l1I1IlllIlI42) {
      return this.verticalGradient(l1I1IlllIlI4.toArgb(), l1I1IlllIlI42.toArgb());
   }

   public RoundedOutlineBuilder horizontalGradient(RgbaColor l1I1IlllIlI4, RgbaColor l1I1IlllIlI42) {
      return this.horizontalGradient(l1I1IlllIlI4.toArgb(), l1I1IlllIlI42.toArgb());
   }

   public RoundedOutlineBuilder cornerColors(RgbaColor l1I1IlllIlI4, RgbaColor l1I1IlllIlI42, RgbaColor l1I1IlllIlI43, RgbaColor l1I1IlllIlI44) {
      return this.cornerColors(l1I1IlllIlI4.toArgb(), l1I1IlllIlI42.toArgb(), l1I1IlllIlI43.toArgb(), l1I1IlllIlI44.toArgb());
   }

   public RoundedOutlineBuilder opacity(float opacity) {
      this.opacity = Math.clamp(opacity, 0.0F, 1.0F);
      return this;
   }

   public RoundedOutlineRenderState buildState(DrawContext drawContext) {
      int encodedOutlineWidth = Math.clamp(Math.round(this.outlineWidth * 2.0F), 0, 127);
      int encodedShadowSpread = Math.clamp(Math.round((Math.max(this.shadowStrength, 1.0F) - 1.0F) / 0.2F), 0, 15);
      float expandedWidth = this.width + this.outlineOutset * 2.0F;
      float expandedHeight = this.height + this.outlineOutset * 2.0F;
      float maximumCornerRadius = Math.min(Math.min(expandedWidth, expandedHeight) * 0.5F, 31.0F);
      int packedTopLeftRadius = clampRadius(this.topLeftRadius + this.outlineOutset, maximumCornerRadius);
      int packedTopRightRadius = clampRadius(this.topRightRadius + this.outlineOutset, maximumCornerRadius);
      int packedBottomRightRadius = clampRadius(this.bottomRightRadius + this.outlineOutset, maximumCornerRadius);
      int packedBottomLeftRadius = clampRadius(this.bottomLeftRadius + this.outlineOutset, maximumCornerRadius);
      float effectiveOpacity = this.opacity * Math.min(this.shadowStrength, 1.0F);
      return new RoundedOutlineRenderState(
         GuiRenderStateBatcher.getMatrix(drawContext),
         this.x + this.offsetX - this.outlineOutset,
         this.y + this.offsetY - this.outlineOutset,
         expandedWidth,
         expandedHeight,
         encodedOutlineWidth * 0.5F,
         packedTopLeftRadius | packedTopRightRadius << 5 | packedBottomRightRadius << 10,
         packedBottomLeftRadius | encodedOutlineWidth << 5 | encodedShadowSpread << 12,
         applyOpacity(this.topLeftColor, effectiveOpacity),
         applyOpacity(this.topRightColor, effectiveOpacity),
         applyOpacity(this.bottomRightColor, effectiveOpacity),
         applyOpacity(this.bottomLeftColor, effectiveOpacity),
         drawContext.scissorStack.peekLast()
      );
   }

   public void render(DrawContext drawContext) {
      if (!(this.width + this.outlineOutset * 2.0F <= 0.0F)
         && !(this.height + this.outlineOutset * 2.0F <= 0.0F)
         && !(this.opacity <= 0.0F)
         && !(this.shadowStrength <= 0.0F)) {
         GuiRenderStateBatcher.addOrBatch(drawContext, this.buildState(drawContext));
      }
   }

   private static int applyOpacity(int argbColor, float opacity) {
      int alpha = Math.round((argbColor >>> 24) * opacity);
      return alpha << 24 | argbColor & 16777215;
   }

   private static int clampRadius(float radius, float maximumRadius) {
      return Math.clamp(Math.round(Math.clamp(radius, 0.0F, maximumRadius)), 0, 31);
   }
}

