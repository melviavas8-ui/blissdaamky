package dev.daamky;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;

public final class TextureQuadBuilder {
   private static final float MAX_CORNER_RADIUS = 63.5F;
   private Identifier textureId;
   private float x;
   private float y;
   private float width;
   private float height;
   private float topLeftCornerRadius;
   private float topRightCornerRadius;
   private float bottomRightCornerRadius;
   private float bottomLeftCornerRadius;
   private boolean useUniformCornerRadii;
   private int topLeftTintArgb = -1;
   private int topRightTintArgb = -1;
   private int bottomRightTintArgb = -1;
   private int bottomLeftTintArgb = -1;
   private float opacity = 1.0F;
   private boolean unusedFlagOne;
   private boolean unusedFlagTwo;
   private boolean linearFiltering = true;
   private TextureQuadMode mode = TextureQuadMode.STRETCH;
   private int overlayColorArgb;
   private float overlayOpacity;

   private TextureQuadBuilder(Identifier identifier) {
      this.textureId = identifier;
   }

   public static TextureQuadBuilder of(Identifier identifier) {
      return new TextureQuadBuilder(identifier);
   }

   public static TextureQuadBuilder of(AvatarTexture avatarTexture) {
      return new TextureQuadBuilder(avatarTexture.textureIdentifier());
   }

   public static TextureQuadBuilder at(Identifier identifier, float x, float y, float width, float height) {
      return new TextureQuadBuilder(identifier).bounds(x, y, width, height);
   }

   public static TextureQuadBuilder at(AvatarTexture avatarTexture, float x, float y, float width, float height) {
      return new TextureQuadBuilder(avatarTexture.textureIdentifier()).bounds(x, y, width, height);
   }

   public TextureQuadBuilder texture(Identifier identifier) {
      this.textureId = identifier;
      return this;
   }

   public TextureQuadBuilder texture(AvatarTexture avatarTexture) {
      return this.texture(avatarTexture.textureIdentifier());
   }

   public TextureQuadBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }

   public TextureQuadBuilder size(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
   }

   public TextureQuadBuilder size(float size) {
      return this.size(size, size);
   }

   public TextureQuadBuilder bounds(float x, float y, float width, float height) {
      return this.position(x, y).size(width, height);
   }

   public TextureQuadBuilder widthWithTextureScale(float width) {
      return this.size(width, width / Avatar.getTextureSize(this.textureId).II1Il11l111II11IIl());
   }

   public TextureQuadBuilder heightWithTextureScale(float height) {
      return this.size(height * Avatar.getTextureSize(this.textureId).II1Il11l111II11IIl(), height);
   }

   public TextureQuadBuilder scaleToTextureDimensions(float scale) {
      TextureSize textureSize = Avatar.getTextureSize(this.textureId);
      return this.size(textureSize.l1I1IlllIlI() * scale, textureSize.IllI1l1l1() * scale);
   }

   public TextureQuadBuilder mode(TextureQuadMode mode) {
      this.mode = mode;
      return this;
   }

   public TextureQuadBuilder contain() {
      return this.mode(TextureQuadMode.CONTAIN);
   }

   public TextureQuadBuilder roundAllCorners() {
      this.useUniformCornerRadii = true;
      return this;
   }

   public TextureQuadBuilder cornerRadius(float radius) {
      return this.cornerRadii(radius, radius, radius, radius);
   }

   public TextureQuadBuilder cornerRadii(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
      this.useUniformCornerRadii = false;
      this.topLeftCornerRadius = topLeftRadius;
      this.topRightCornerRadius = topRightRadius;
      this.bottomRightCornerRadius = bottomRightRadius;
      this.bottomLeftCornerRadius = bottomLeftRadius;
      return this;
   }

   public TextureQuadBuilder topCornerRadius(float radius) {
      this.useUniformCornerRadii = false;
      this.topLeftCornerRadius = radius;
      this.topRightCornerRadius = radius;
      return this;
   }

   public TextureQuadBuilder bottomCornerRadius(float radius) {
      this.useUniformCornerRadii = false;
      this.bottomLeftCornerRadius = radius;
      this.bottomRightCornerRadius = radius;
      return this;
   }

   public TextureQuadBuilder leftCornerRadius(float radius) {
      this.useUniformCornerRadii = false;
      this.topLeftCornerRadius = radius;
      this.bottomLeftCornerRadius = radius;
      return this;
   }

   public TextureQuadBuilder rightCornerRadius(float radius) {
      this.useUniformCornerRadii = false;
      this.topRightCornerRadius = radius;
      this.bottomRightCornerRadius = radius;
      return this;
   }

   public TextureQuadBuilder tint(int argb) {
      return this.cornerTints(argb, argb, argb, argb);
   }

   public TextureQuadBuilder verticalGradient(int topArgb, int bottomArgb) {
      return this.cornerTints(topArgb, topArgb, bottomArgb, bottomArgb);
   }

   public TextureQuadBuilder horizontalGradient(int leftArgb, int rightArgb) {
      return this.cornerTints(leftArgb, rightArgb, rightArgb, leftArgb);
   }

   public TextureQuadBuilder cornerTints(int topLeftArgb, int topRightArgb, int bottomRightArgb, int bottomLeftArgb) {
      this.topLeftTintArgb = topLeftArgb;
      this.topRightTintArgb = topRightArgb;
      this.bottomRightTintArgb = bottomRightArgb;
      this.bottomLeftTintArgb = bottomLeftArgb;
      return this;
   }

   public TextureQuadBuilder tint(RgbaColor color) {
      return this.tint(color.toArgb());
   }

   public TextureQuadBuilder verticalGradient(RgbaColor topColor, RgbaColor bottomColor) {
      return this.verticalGradient(topColor.toArgb(), bottomColor.toArgb());
   }

   public TextureQuadBuilder horizontalGradient(RgbaColor leftColor, RgbaColor rightColor) {
      return this.horizontalGradient(leftColor.toArgb(), rightColor.toArgb());
   }

   public TextureQuadBuilder cornerTints(RgbaColor topLeftColor, RgbaColor topRightColor, RgbaColor bottomRightColor, RgbaColor bottomLeftColor) {
      return this.cornerTints(topLeftColor.toArgb(), topRightColor.toArgb(), bottomRightColor.toArgb(), bottomLeftColor.toArgb());
   }

   public TextureQuadBuilder overlay(int argb, float alpha) {
      this.overlayColorArgb = argb;
      this.overlayOpacity = Math.clamp(alpha, 0.0F, 1.0F);
      return this;
   }

   public TextureQuadBuilder overlay(RgbaColor color, float alpha) {
      return this.overlay(color.toArgb(), alpha);
   }

   public TextureQuadBuilder opacity(float opacity) {
      this.opacity = Math.clamp(opacity, 0.0F, 1.0F);
      return this;
   }

   public TextureQuadBuilder setUnusedFlagOne(boolean enabled) {
      this.unusedFlagOne = enabled;
      return this;
   }

   public TextureQuadBuilder setUnusedFlagTwo(boolean enabled) {
      this.unusedFlagTwo = enabled;
      return this;
   }

   public TextureQuadBuilder linearFiltering(boolean enabled) {
      this.linearFiltering = enabled;
      return this;
   }

   public TextureSize textureSize() {
      return Avatar.getTextureSize(this.textureId);
   }

   public float getWidth() {
      return this.width;
   }

   public float getHeight() {
      return this.height;
   }

   public TexturedQuadRenderState buildRenderState(DrawContext drawContext) {
      float[] renderBounds = this.calculateBounds();
      float maximumCornerRadius = Math.min(Math.min(renderBounds[2], renderBounds[3]) * 0.5F, 63.5F);
      int topLeftRadius = packCornerRadius(this.useUniformCornerRadii ? maximumCornerRadius : this.topLeftCornerRadius, maximumCornerRadius);
      int topRightRadius = packCornerRadius(this.useUniformCornerRadii ? maximumCornerRadius : this.topRightCornerRadius, maximumCornerRadius);
      int bottomRightRadius = packCornerRadius(this.useUniformCornerRadii ? maximumCornerRadius : this.bottomRightCornerRadius, maximumCornerRadius);
      int bottomLeftRadius = packCornerRadius(this.useUniformCornerRadii ? maximumCornerRadius : this.bottomLeftCornerRadius, maximumCornerRadius);
      int packedTopRadii = topLeftRadius | topRightRadius << 7;
      int packedBottomRadii = bottomLeftRadius | bottomRightRadius << 7;
      return new TexturedQuadRenderState(
         GuiRenderStateBatcher.getMatrix(drawContext),
         renderBounds[0],
         renderBounds[1],
         renderBounds[2],
         renderBounds[3],
         packedTopRadii,
         packedBottomRadii,
         this.applyOpacity(this.topLeftTintArgb),
         this.applyOpacity(this.topRightTintArgb),
         this.applyOpacity(this.bottomRightTintArgb),
         this.applyOpacity(this.bottomLeftTintArgb),
         this.textureId,
         this.linearFiltering,
         drawContext.scissorStack.peekLast()
      );
   }

   public void render(DrawContext drawContext) {
      if (!(this.width <= 0.0F) && !(this.height <= 0.0F) && !(this.opacity <= 0.0F)) {
         GuiRenderStateBatcher.addOrBatch(drawContext, this.buildRenderState(drawContext));
         if (this.overlayOpacity > 0.0F && this.overlayColorArgb >>> 24 > 0) {
            float[] renderBounds = this.calculateBounds();
            float maximumCornerRadius = Math.min(Math.min(renderBounds[2], renderBounds[3]) * 0.5F, 63.5F);
            RoundedGradientRectBuilder.at(renderBounds[0], renderBounds[1], renderBounds[2], renderBounds[3])
               .cornerRadii(
                  this.useUniformCornerRadii ? maximumCornerRadius : this.topLeftCornerRadius,
                  this.useUniformCornerRadii ? maximumCornerRadius : this.topRightCornerRadius,
                  this.useUniformCornerRadii ? maximumCornerRadius : this.bottomRightCornerRadius,
                  this.useUniformCornerRadii ? maximumCornerRadius : this.bottomLeftCornerRadius
               )
               .color(this.overlayColorArgb)
               .opacity(this.opacity * this.overlayOpacity)
               .render(drawContext);
         }
      }
   }

   private float[] calculateBounds() {
      if (this.mode == TextureQuadMode.STRETCH) {
         return new float[]{this.x, this.y, this.width, this.height};
      }

      TextureSize textureSize = Avatar.getTextureSize(this.textureId);
      float scaleToFit = Math.min(this.width / textureSize.l1I1IlllIlI(), this.height / textureSize.IllI1l1l1());
      float renderedWidth = textureSize.l1I1IlllIlI() * scaleToFit;
      float renderedHeight = textureSize.IllI1l1l1() * scaleToFit;
      return new float[]{this.x + (this.width - renderedWidth) * 0.5F, this.y + (this.height - renderedHeight) * 0.5F, renderedWidth, renderedHeight};
   }

   private int applyOpacity(int argb) {
      int alpha = Math.round((argb >>> 24) * this.opacity);
      return alpha << 24 | argb & 16777215;
   }

   private static int packCornerRadius(float radius, float maximumRadius) {
      return Math.clamp(Math.round(Math.clamp(radius, 0.0F, maximumRadius) * 2.0F), 0, 127);
   }
}

