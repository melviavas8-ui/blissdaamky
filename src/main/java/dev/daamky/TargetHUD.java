package dev.daamky;

import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class TargetHUD extends HudElementPosition {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float DEFAULT_WIDTH = 92.0F;
   private static final float DEFAULT_HEIGHT = 26.0F;
   private LivingEntity visibleTarget;
   private LivingEntity currentTarget;
   private long lastTargetSeenAtMillis;
   private float healthRatio;
   private float damageTrailRatio;
   private float absorptionRatio;
   private float hurtOverlayAlpha;
// https://t.me/pasterplace

   public TargetHUD() {
      super("Target HUD", "target", 9999.0F, 9999.0F);
      this.elementWidth = 92.0F;
      this.elementHeight = 26.0F;
   }
// https://t.me/pasterplace

   @Override
   public void clampPositionToScreen(float viewportWidth, float viewportHeight) {
      this.setPosition(viewportWidth * 0.5F + 18.0F, viewportHeight * 0.5F + 18.0F);
   }
// https://t.me/pasterplace

   private LivingEntity resolveTarget(boolean allowLocalPlayerFallback) {
      if (CLIENT.player == null) {
         return null;
      }
// https://t.me/pasterplace

      Aura aura = Aura.getRegisteredAura();
      if (aura != null && aura.isEnabled() && aura.getTarget() != null) {
         return aura.getTarget();
      }

      Entity pointedEntity = CLIENT.targetedEntity;
      if (pointedEntity instanceof PlayerEntity pointedPlayer) {
         if (pointedPlayer != CLIENT.player) {
            return pointedPlayer;
         }
      }

      return allowLocalPlayerFallback ? CLIENT.player : null;
   }
// https://t.me/pasterplace

   @Override
   public void updateAnimation(float frameDelta) {
      LivingEntity selectedTarget = this.resolveTarget(CLIENT.currentScreen instanceof ChatScreen);
      long currentTimeMillis = System.currentTimeMillis();
      if (selectedTarget != null) {
         if (selectedTarget != this.currentTarget) {
            this.currentTarget = selectedTarget;
            this.healthRatio = selectedTarget.getHealth() / Math.max(1.0F, selectedTarget.getMaxHealth());
            this.damageTrailRatio = this.healthRatio;
         }

         this.lastTargetSeenAtMillis = currentTimeMillis;
      }

      this.visibleTarget = selectedTarget == null && currentTimeMillis - this.lastTargetSeenAtMillis >= 600L ? null : this.currentTarget;
      if (this.currentTarget != null && (this.currentTarget.isRemoved() || !this.currentTarget.isAlive()) && selectedTarget == null) {
         this.visibleTarget = null;
      }

      if (this.currentTarget != null) {
         float maximumHealth = Math.max(1.0F, this.currentTarget.getMaxHealth());
         float currentHealthRatio = Math.clamp(this.currentTarget.getHealth() / maximumHealth, 0.0F, 1.0F);
         this.healthRatio = interpolate(this.healthRatio, currentHealthRatio, 10.0F, frameDelta);
         this.damageTrailRatio = this.damageTrailRatio > this.healthRatio ? interpolate(this.damageTrailRatio, this.healthRatio, 2.5F, frameDelta) : this.healthRatio;
         this.absorptionRatio = interpolate(
            this.absorptionRatio, Math.clamp(this.currentTarget.getAbsorptionAmount() / maximumHealth, 0.0F, 1.0F), 8.0F, frameDelta
         );
         this.hurtOverlayAlpha = interpolate(this.hurtOverlayAlpha, this.currentTarget.hurtTime > 0 ? 1.0F : 0.0F, 14.0F, frameDelta);
      }
   }
// https://t.me/pasterplace

   @Override
   public boolean isVisibleInCurrentScreen(boolean chatScreenOpen) {
      return this.visibleTarget != null;
   }
// https://t.me/pasterplace

   @Override
   public void render(HudRenderContext renderContext, float opacity) {
      LivingEntity target = this.currentTarget;
      if (target != null) {
         DrawContext drawContext = renderContext.drawContext();
         float x = this.getX();
         float y = this.getY();
         float scale = 0.92F + 0.08F * opacity;
         float transformCenterX = x + 46.0F;
         float transformCenterY = y + 13.0F;
         drawContext.getMatrices().pushMatrix();
         drawContext.getMatrices().translate(transformCenterX, transformCenterY);
         drawContext.getMatrices().scale(scale);
         drawContext.getMatrices().translate(-transformCenterX, -transformCenterY);
         BrokkeIcons.drawRoundedPanelBackground(drawContext, x, y, 92.0F, 26.0F, 4.0F, opacity);
         float portraitInset = 4.0F;
         float portraitSize = 26.0F - portraitInset * 2.0F;
         if (target instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity playerAvatar = (AbstractClientPlayerEntity)target;
            PlayerSkinQuadBuilder.at(x + portraitInset, y + portraitInset, portraitSize)
               .playerSkin(playerAvatar)
               .cornerRadius(3.0F)
               .overlay(RgbaColor.rgb(255, 70, 70).toArgb(), 0.45F * this.hurtOverlayAlpha)
               .opacity(opacity)
               .render(drawContext);
         } else {
            RoundedGradientRectBuilder.at(x + portraitInset, y + portraitInset, portraitSize, portraitSize)
               .cornerRadius(3.0F)
               .color(BrokkeIcons.whiteWithAlpha(opacity * 0.18F))
               .render(drawContext);
            String fallbackPortraitText = target.getName().getString();
            fallbackPortraitText = fallbackPortraitText.isEmpty() ? "?" : fallbackPortraitText.substring(0, 1).toUpperCase(Locale.ROOT);
            BrokkeIcons.drawCenteredText(
               drawContext,
               fallbackPortraitText,
               x + portraitInset + portraitSize * 0.5F - BrokkeIcons.measureTextWidth(fallbackPortraitText, 7.0F) * 0.5F,
               y + portraitInset + portraitSize * 0.5F,
               7.0F,
               BrokkeIcons.lightGrayWithAlpha(opacity)
            );
         }

         float nameStartX = x + portraitInset + portraitSize + 5.0F;
         float healthRingRadius = 13.0F - portraitInset - 0.5F;
         float healthRingCenterX = x + 92.0F - portraitInset - healthRingRadius - 0.5F;
         float healthRingCenterY = y + 13.0F;
         float nameRegionEndX = healthRingCenterX - healthRingRadius - 4.0F;
         String displayName = target.getName().getString();
         float displayNameFontSize = 5.6F;

         while (displayName.length() > 1 && BrokkeIcons.measureTextWidth(displayName, displayNameFontSize) > nameRegionEndX - nameStartX) {
            displayName = displayName.substring(0, displayName.length() - 1);
         }

         String secondaryLabel;
         label42: {
            BrokkeIcons.drawCenteredText(drawContext, displayName, nameStartX, y + 13.0F - 3.2F, displayNameFontSize, BrokkeIcons.lightGrayWithAlpha(opacity));
            if (target instanceof PlayerEntity) {
               LivingEntity targetPlayer = (PlayerEntity)target;
               if (targetPlayer.getAbsorptionAmount() > 0.0F) {
                  secondaryLabel = String.format(Locale.ROOT, "+%.0f", targetPlayer.getAbsorptionAmount());
                  break label42;
               }
            }

            if (CLIENT.player != null) {
               secondaryLabel = String.format(Locale.ROOT, "%.1fm", CLIENT.player.distanceTo(target));
            } else {
               secondaryLabel = "";
            }
         }

         RgbaColor secondaryLabelColor = target.getAbsorptionAmount() > 0.0F
            ? RgbaColor.rgba(255, 200, 90, (int)(255.0F * opacity))
            : BrokkeIcons.mutedGrayWithAlpha(opacity * 0.8F);
         BrokkeIcons.drawCenteredText(drawContext, secondaryLabel, nameStartX, y + 13.0F + 3.6F, 4.8F, secondaryLabelColor);
         AnimatedListHudElement.drawProgressRing(drawContext, healthRingCenterX, healthRingCenterY, healthRingRadius, 1.25F, 30, this.healthRatio, opacity);
         if (this.absorptionRatio > 0.005F) {
            AnimatedListHudElement.drawProgressRing(
               drawContext, healthRingCenterX, healthRingCenterY, healthRingRadius, 1.25F, 30, this.absorptionRatio, RgbaColor.rgb(255, 200, 90), opacity
            );
         }

         String totalHealthText = String.valueOf((int)Math.ceil(target.getHealth() + target.getAbsorptionAmount()));
         float totalHealthFontSize = 4.6F;
         BrokkeIcons.drawCenteredText(
            drawContext, totalHealthText, healthRingCenterX - BrokkeIcons.measureTextWidth(totalHealthText, totalHealthFontSize) * 0.5F, healthRingCenterY, totalHealthFontSize, BrokkeIcons.whiteWithAlpha(opacity)
         );
         drawContext.getMatrices().popMatrix();
      }
   }
}

