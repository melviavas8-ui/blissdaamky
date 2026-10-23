package dev.daamky;

import com.mojang.authlib.GameProfile;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;

public final class PlayerSkinQuadBuilder {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float MAX_CORNER_RADIUS = 63.5F;
   private float x;
   private float y;
   private float width;
   private float height;
   private float topLeftRadius;
   private float topRightRadius;
   private float bottomRightRadius;
   private float bottomLeftRadius;
   private boolean fullyRoundedCorners;
   private int topLeftColor = -1;
   private int topRightColor = -1;
   private int bottomRightColor = -1;
   private int bottomLeftColor = -1;
   private float opacity = 1.0F;
   private boolean requestedLinearFiltering = true;
   private boolean linearTextureFiltering;
   private int overlayColor;
   private float overlayOpacity;
   private Supplier<Identifier> textureSupplier = () -> PlayerSkinTextureCache.II1Il11l111II11IIl(MinecraftClient.getInstance().getGameProfile())
      .get()
      .body()
      .texturePath();
// https://t.me/pasterplace

   private PlayerSkinQuadBuilder() {
   }
// https://t.me/pasterplace

   public static PlayerSkinQuadBuilder create() {
      return new PlayerSkinQuadBuilder();
   }
// https://t.me/pasterplace

   public static PlayerSkinQuadBuilder at(float x, float y, float size) {
      return new PlayerSkinQuadBuilder().position(x, y).size(size);
   }
// https://t.me/pasterplace

   public static PlayerSkinQuadBuilder at(float x, float y, float width, float height) {
      return new PlayerSkinQuadBuilder().bounds(x, y, width, height);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder size(float size) {
      return this.size(size, size);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder size(float width, float height) {
      this.width = width;
      this.height = height;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder bounds(float x, float y, float width, float height) {
      return this.position(x, y).size(width, height);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder skinTextures(SkinTextures skinTextures) {
      this.textureSupplier = () -> skinTextures.body().texturePath();
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder skinTextures(Supplier<SkinTextures> supplier) {
      this.textureSupplier = () -> ((SkinTextures)supplier.get()).body().texturePath();
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder skinProfile(GameProfile gameProfile) {
      return this.skinTextures(PlayerSkinTextureCache.II1Il11l111II11IIl(gameProfile));
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder playerSkin(AbstractClientPlayerEntity abstractClientPlayerEntity) {
      return this.skinTextures(abstractClientPlayerEntity::getSkin);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder playerListSkin(PlayerListEntry playerListEntry) {
      return this.skinTextures(playerListEntry::getSkinTextures);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder localPlayerSkin() {
      return this.skinProfile(MinecraftClient.getInstance().getGameProfile());
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder texture(Identifier identifier) {
      this.textureSupplier = () -> identifier;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder requestedLinearFiltering(boolean enabled) {
      this.requestedLinearFiltering = enabled;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder linearTextureFiltering(boolean enabled) {
      this.linearTextureFiltering = enabled;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder fullyRounded() {
      this.fullyRoundedCorners = true;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder cornerRadius(float radius) {
      return this.cornerRadii(radius, radius, radius, radius);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder cornerRadii(float topLeftRadius, float topRightRadius, float bottomRightRadius, float bottomLeftRadius) {
      this.fullyRoundedCorners = false;
      this.topLeftRadius = topLeftRadius;
      this.topRightRadius = topRightRadius;
      this.bottomRightRadius = bottomRightRadius;
      this.bottomLeftRadius = bottomLeftRadius;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder topCornerRadius(float radius) {
      this.fullyRoundedCorners = false;
      this.topLeftRadius = radius;
      this.topRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder bottomCornerRadius(float radius) {
      this.fullyRoundedCorners = false;
      this.bottomLeftRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder leftCornerRadius(float radius) {
      this.fullyRoundedCorners = false;
      this.topLeftRadius = radius;
      this.bottomLeftRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder rightCornerRadius(float radius) {
      this.fullyRoundedCorners = false;
      this.topRightRadius = radius;
      this.bottomRightRadius = radius;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder color(int color) {
      return this.cornerColors(color, color, color, color);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder verticalGradient(int topColor, int bottomColor) {
      return this.cornerColors(topColor, topColor, bottomColor, bottomColor);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder horizontalGradient(int leftColor, int rightColor) {
      return this.cornerColors(leftColor, rightColor, rightColor, leftColor);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder cornerColors(int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
      this.topLeftColor = topLeftColor;
      this.topRightColor = topRightColor;
      this.bottomRightColor = bottomRightColor;
      this.bottomLeftColor = bottomLeftColor;
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder color(RgbaColor l1I1IlllIlI4) {
      return this.color(l1I1IlllIlI4.toArgb());
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder verticalGradient(RgbaColor l1I1IlllIlI4, RgbaColor l1I1IlllIlI42) {
      return this.verticalGradient(l1I1IlllIlI4.toArgb(), l1I1IlllIlI42.toArgb());
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder horizontalGradient(RgbaColor l1I1IlllIlI4, RgbaColor l1I1IlllIlI42) {
      return this.horizontalGradient(l1I1IlllIlI4.toArgb(), l1I1IlllIlI42.toArgb());
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder cornerColors(RgbaColor l1I1IlllIlI4, RgbaColor l1I1IlllIlI42, RgbaColor l1I1IlllIlI43, RgbaColor l1I1IlllIlI44) {
      return this.cornerColors(l1I1IlllIlI4.toArgb(), l1I1IlllIlI42.toArgb(), l1I1IlllIlI43.toArgb(), l1I1IlllIlI44.toArgb());
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder overlay(int color, float opacity) {
      this.overlayColor = color;
      this.overlayOpacity = Math.clamp(opacity, 0.0F, 1.0F);
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder overlay(RgbaColor l1I1IlllIlI4, float opacity) {
      return this.overlay(l1I1IlllIlI4.toArgb(), opacity);
   }
// https://t.me/pasterplace

   public PlayerSkinQuadBuilder opacity(float opacity) {
      this.opacity = Math.clamp(opacity, 0.0F, 1.0F);
      return this;
   }
// https://t.me/pasterplace

   public PlayerSkinQuadRenderState buildState(DrawContext drawContext) {
      float maximumCornerRadius = Math.min(Math.min(this.width, this.height) * 0.5F, 63.5F);
      int topLeftRadius = clampCornerRadius(this.fullyRoundedCorners ? maximumCornerRadius : this.topLeftRadius, maximumCornerRadius);
      int topRightRadius = clampCornerRadius(this.fullyRoundedCorners ? maximumCornerRadius : this.topRightRadius, maximumCornerRadius);
      int bottomRightRadius = clampCornerRadius(this.fullyRoundedCorners ? maximumCornerRadius : this.bottomRightRadius, maximumCornerRadius);
      int bottomLeftRadius = clampCornerRadius(this.fullyRoundedCorners ? maximumCornerRadius : this.bottomLeftRadius, maximumCornerRadius);
      int packedTopCornerRadii = topLeftRadius | topRightRadius << 7;
      int packedBottomCornerRadii = bottomLeftRadius | bottomRightRadius << 7;
      return new PlayerSkinQuadRenderState(
         GuiRenderStateBatcher.getMatrix(drawContext),
         this.x,
         this.y,
         this.width,
         this.height,
         packedTopCornerRadii,
         packedBottomCornerRadii,
         this.applyOpacity(this.topLeftColor),
         this.applyOpacity(this.topRightColor),
         this.applyOpacity(this.bottomRightColor),
         this.applyOpacity(this.bottomLeftColor),
         this.textureSupplier.get(),
         this.linearTextureFiltering,
         drawContext.scissorStack.peekLast()
      );
   }
// https://t.me/pasterplace

   public void render(DrawContext drawContext) {
      if (!(this.width <= 0.0F) && !(this.height <= 0.0F) && !(this.opacity <= 0.0F)) {
         GuiRenderStateBatcher.addOrBatch(drawContext, this.buildState(drawContext));
         if (this.overlayOpacity > 0.0F && this.overlayColor >>> 24 > 0) {
            float maximumOverlayCornerRadius = Math.min(Math.min(this.width, this.height) * 0.5F, 63.5F);
            RoundedGradientRectBuilder.at(this.x, this.y, this.width, this.height)
               .cornerRadii(
                  this.fullyRoundedCorners ? maximumOverlayCornerRadius : this.topLeftRadius,
                  this.fullyRoundedCorners ? maximumOverlayCornerRadius : this.topRightRadius,
                  this.fullyRoundedCorners ? maximumOverlayCornerRadius : this.bottomRightRadius,
                  this.fullyRoundedCorners ? maximumOverlayCornerRadius : this.bottomLeftRadius
               )
               .color(this.overlayColor)
               .opacity(this.opacity * this.overlayOpacity)
               .render(drawContext);
         }
      }
   }
// https://t.me/pasterplace

   private int applyOpacity(int argbColor) {
      int alpha = Math.round((argbColor >>> 24) * this.opacity);
      return alpha << 24 | argbColor & 16777215;
   }
// https://t.me/pasterplace

   private static int clampCornerRadius(float radius, float maximumRadius) {
      return Math.clamp(Math.round(Math.clamp(radius, 0.0F, maximumRadius) * 2.0F), 0, 127);
   }
}

