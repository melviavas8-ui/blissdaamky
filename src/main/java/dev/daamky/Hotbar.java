package dev.daamky;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.world.GameMode;

public final class Hotbar extends HudElementPosition {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float HOTBAR_SLOT_SIZE = 16.0F;
   private static final float HOTBAR_SLOT_GAP = 2.0F;
   private static final float HOTBAR_PANEL_PADDING = 3.0F;
   private static final float ITEM_RENDER_SIZE = 11.0F;
   private static final float STATUS_PANEL_HEIGHT = 12.0F;
   private static final float ITEM_NAME_DISPLAY_DURATION = 2.0F;
   private static final float OFFHAND_PANEL_GAP = 3.0F;
   private static final RgbaColor LOW_HEALTH_COLOR = RgbaColor.rgb(255, 92, 110);
   private static final RgbaColor ABSORPTION_COLOR = RgbaColor.rgb(255, 205, 90);
   private static final RgbaColor FOOD_BAR_COLOR = RgbaColor.rgb(255, 170, 80);
   private static final RgbaColor EXPERIENCE_LEVEL_COLOR = RgbaColor.rgb(140, 235, 110);
   private static final float HEALTH_ICON_SIZE = 6.5F;
   private float selectedSlotOffsetX = -1.0F;
   private float healthRatio;
   private float absorptionRatio;
   private float hungerRatio;
   private float delayedHealthRatio;
   private int previousSelectedHotbarSlot = -1;
   private ItemStack previousSelectedItemStack = ItemStack.EMPTY;
   private float itemNameDisplayTimeRemaining;
// https://t.me/pasterplace

   public Hotbar() {
      super("Hotbar", "hotbar", 0.0F, 0.0F);
      this.elementWidth = 166.0F;
      this.elementHeight = 22.0F;
   }
// https://t.me/pasterplace

   @Override
   public boolean isDraggable() {
      return false;
   }
// https://t.me/pasterplace

   @Override
   public boolean containsPoint(float pointX, float pointY) {
      return false;
   }
// https://t.me/pasterplace

   @Override
   public boolean isHiddenByPosition() {
      return false;
   }
// https://t.me/pasterplace

   @Override
   public float getX() {
      return GuiEventRenderer.getScreenWidth() * 0.5F - this.elementWidth * 0.5F;
   }
// https://t.me/pasterplace

   @Override
   public float getY() {
      return GuiEventRenderer.getScreenHeight() - this.elementHeight - 3.0F;
   }
// https://t.me/pasterplace

   private static boolean hasStatusBars() {
      return CLIENT.interactionManager != null && CLIENT.interactionManager.hasStatusBars();
   }
// https://t.me/pasterplace

   @Override
   public void updateAnimation(float tickDelta) {
      ClientPlayerEntity clientPlayerEntity = CLIENT.player;
      if (clientPlayerEntity != null) {
         float maximumHealth = Math.max(1.0F, clientPlayerEntity.getMaxHealth());
         float healthRatio = Math.clamp(clientPlayerEntity.getHealth() / maximumHealth, 0.0F, 1.0F);
         this.healthRatio = interpolate(this.healthRatio, healthRatio, 14.0F, tickDelta);
         this.delayedHealthRatio = healthRatio > this.delayedHealthRatio ? this.healthRatio : interpolate(this.delayedHealthRatio, healthRatio, 3.0F, tickDelta);
         this.absorptionRatio = interpolate(this.absorptionRatio, Math.clamp(clientPlayerEntity.getAbsorptionAmount() / maximumHealth, 0.0F, 1.0F), 12.0F, tickDelta);
         this.hungerRatio = interpolate(this.hungerRatio, clientPlayerEntity.getHungerManager().getFoodLevel() / 20.0F, 12.0F, tickDelta);
         int selectedHotbarSlot = clientPlayerEntity.getInventory().getSelectedSlot();
         float selectedSlotOffsetX = 3.0F + selectedHotbarSlot * 18.0F;
         this.selectedSlotOffsetX = this.selectedSlotOffsetX < 0.0F ? selectedSlotOffsetX : interpolate(this.selectedSlotOffsetX, selectedSlotOffsetX, 18.0F, tickDelta);
         ItemStack selectedHotbarStack = clientPlayerEntity.getInventory().getStack(selectedHotbarSlot);
         if (selectedHotbarSlot != this.previousSelectedHotbarSlot || !ItemStack.areItemsAndComponentsEqual(selectedHotbarStack, this.previousSelectedItemStack)) {
            this.itemNameDisplayTimeRemaining = selectedHotbarStack.isEmpty() ? 0.0F : 2.0F;
            this.previousSelectedHotbarSlot = selectedHotbarSlot;
            this.previousSelectedItemStack = selectedHotbarStack.copy();
         }

         this.itemNameDisplayTimeRemaining = Math.max(0.0F, this.itemNameDisplayTimeRemaining - tickDelta);
      }
   }
// https://t.me/pasterplace

   @Override
   public boolean isVisibleInCurrentScreen(boolean chatScreenOpen) {
      return CLIENT.player != null
         && CLIENT.interactionManager != null
         && CLIENT.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR;
   }
// https://t.me/pasterplace

   @Override
   public void render(HudRenderContext renderContext, float visibilityAlpha) {
      ClientPlayerEntity clientPlayerEntity = CLIENT.player;
      if (clientPlayerEntity != null) {
         DrawContext drawContext = renderContext.drawContext();
         float hotbarLeft = this.getX();
         float hotbarTop = this.getY();
         BrokkeIcons.drawPanelBackground(drawContext, hotbarLeft, hotbarTop, this.elementWidth, this.elementHeight, visibilityAlpha);
         float slotTop = hotbarTop + 3.0F;

         for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
            BrokkeIcons.drawRoundedPill(drawContext, hotbarLeft + 3.0F + slotIndex * 18.0F, slotTop, 16.0F, 16.0F, visibilityAlpha);
         }

         float selectedSlotIndicatorX = hotbarLeft + this.selectedSlotOffsetX;
         RoundedGradientRectBuilder.at(selectedSlotIndicatorX, slotTop, 16.0F, 16.0F)
            .cornerRadius(2.5F)
            .color(Accent.getAccentColorWithAlpha((int)(60.0F * visibilityAlpha)))
            .render(drawContext);

         for (int hotbarSlotIndex = 0; hotbarSlotIndex < 9; hotbarSlotIndex++) {
            renderHotbarItem(renderContext, clientPlayerEntity.getInventory().getStack(hotbarSlotIndex), hotbarLeft + 3.0F + hotbarSlotIndex * 18.0F, slotTop, visibilityAlpha);
         }

         ItemStack offhandStack = clientPlayerEntity.getOffHandStack();
         if (!offhandStack.isEmpty()) {
            float offhandPanelWidth = 22.0F;
            float offhandPanelX = clientPlayerEntity.getMainArm().getOpposite() == Arm.LEFT ? hotbarLeft - offhandPanelWidth - 4.0F : hotbarLeft + this.elementWidth + 4.0F;
            BrokkeIcons.drawPanelBackground(drawContext, offhandPanelX, hotbarTop, offhandPanelWidth, this.elementHeight, visibilityAlpha);
            BrokkeIcons.drawRoundedPill(drawContext, offhandPanelX + 3.0F, slotTop, 16.0F, 16.0F, visibilityAlpha);
            renderHotbarItem(renderContext, offhandStack, offhandPanelX + 3.0F, slotTop, visibilityAlpha);
         }
// https://t.me/pasterplace

         float attackCooldownProgress = clientPlayerEntity.getAttackCooldownProgress(0.0F);
         if (attackCooldownProgress < 1.0F) {
            BrokkeIcons.drawProgressBar(drawContext, selectedSlotIndicatorX + 2.0F, slotTop + 16.0F - 2.2F, 12.0F, 1.2F, attackCooldownProgress, BrokkeIcons.whiteWithAlpha(visibilityAlpha), visibilityAlpha);
         }

         float itemNameAnchorY = hotbarTop;
         String experienceLabel = CLIENT.interactionManager.hasExperienceBar() && clientPlayerEntity.experienceLevel > 0 ? String.valueOf(clientPlayerEntity.experienceLevel) : null;
         float experienceLabelWidth = experienceLabel != null ? BrokkeIcons.measureTextWidth(experienceLabel, 5.7F) : 0.0F;
         if (hasStatusBars()) {
            float statusPanelTopY = hotbarTop - 2.0F - 12.0F;
            float sidePanelWidth = experienceLabel != null ? experienceLabelWidth + 8.0F : 2.0F;
            float sidePanelX = (this.elementWidth - sidePanelWidth) * 0.5F;
            this.renderHealthPanel(drawContext, clientPlayerEntity, hotbarLeft, statusPanelTopY, sidePanelX, visibilityAlpha);
            this.renderFoodPanel(drawContext, clientPlayerEntity, hotbarLeft + sidePanelX + sidePanelWidth, statusPanelTopY, sidePanelX, visibilityAlpha);
            if (experienceLabel != null) {
               BrokkeIcons.drawCenteredText(
                  drawContext, experienceLabel, hotbarLeft + this.elementWidth * 0.5F - experienceLabelWidth * 0.5F, statusPanelTopY + 6.0F, 5.7F, EXPERIENCE_LEVEL_COLOR.withAlphaNormalized(visibilityAlpha)
               );
            }

            itemNameAnchorY = statusPanelTopY;
         } else if (experienceLabel != null) {
            BrokkeIcons.drawCenteredText(
               drawContext, experienceLabel, hotbarLeft + this.elementWidth * 0.5F - experienceLabelWidth * 0.5F, hotbarTop - 5.0F, 5.7F, EXPERIENCE_LEVEL_COLOR.withAlphaNormalized(visibilityAlpha)
            );
            itemNameAnchorY = hotbarTop - 9.0F;
         }

         if (this.itemNameDisplayTimeRemaining > 0.0F && !this.previousSelectedItemStack.isEmpty()) {
            float itemNameAlpha = visibilityAlpha * Math.clamp(this.itemNameDisplayTimeRemaining / 0.4F, 0.0F, 1.0F);
            String displayedItemName = this.previousSelectedItemStack.getName().getString();
            float displayedItemNameWidth = BrokkeIcons.measureTextWidth(displayedItemName, 5.7F) + 10.0F;
            float itemNameX = hotbarLeft + this.elementWidth * 0.5F - displayedItemNameWidth * 0.5F;
            float itemNameY = itemNameAnchorY - 2.0F - 11.0F;
            BrokkeIcons.drawPanelBackground(drawContext, itemNameX, itemNameY, displayedItemNameWidth, 11.0F, itemNameAlpha);
            BrokkeIcons.drawCenteredText(drawContext, displayedItemName, itemNameX + 5.0F, itemNameY + 5.5F, 5.7F, BrokkeIcons.lightGrayWithAlpha(itemNameAlpha));
         }
      }
   }
// https://t.me/pasterplace

   private void renderHealthPanel(DrawContext drawContext, PlayerEntity playerEntity, float panelX, float panelY, float panelWidth, float visibilityAlpha) {
      BrokkeIcons.drawPanelBackground(drawContext, panelX, panelY, panelWidth, 12.0F, visibilityAlpha);
      float barCenterY = panelY + 6.0F;
      drawHudIcon(
         drawContext, Avatar.HEART_AVATAR, panelX + 7.0F, barCenterY, (this.absorptionRatio > 0.01F ? ABSORPTION_COLOR : LOW_HEALTH_COLOR).withAlphaNormalized(visibilityAlpha)
      );
      String healthLabel = String.valueOf((int)Math.ceil(playerEntity.getHealth() + playerEntity.getAbsorptionAmount()));
      float healthLabelWidth = BrokkeIcons.measureTextWidth(healthLabel, 4.8F);
      BrokkeIcons.drawCenteredText(drawContext, healthLabel, panelX + panelWidth - 5.0F - healthLabelWidth, barCenterY, 4.8F, BrokkeIcons.lightGrayWithAlpha(visibilityAlpha));
      float healthBarX = panelX + 13.0F;
      float healthBarWidth = panelWidth - 13.0F - 8.0F - healthLabelWidth;
      float healthBarHeight = 2.4F;
      float healthBarY = barCenterY - healthBarHeight * 0.5F;
      RoundedGradientRectBuilder.at(healthBarX, healthBarY, healthBarWidth, healthBarHeight)
         .cornerRadius(healthBarHeight * 0.5F)
         .color(Accent.getAccentColorWithAlpha((int)(22.0F * visibilityAlpha)))
         .render(drawContext);
      if (this.delayedHealthRatio > this.healthRatio + 0.002F) {
         RoundedGradientRectBuilder.at(healthBarX, healthBarY, Math.max(healthBarHeight, healthBarWidth * this.delayedHealthRatio), healthBarHeight)
            .cornerRadius(healthBarHeight * 0.5F)
            .color(RgbaColor.rgba(255, 255, 255, (int)(110.0F * visibilityAlpha)))
            .render(drawContext);
      }

      if (this.healthRatio > 0.002F) {
         RoundedGradientRectBuilder.at(healthBarX, healthBarY, Math.max(healthBarHeight, healthBarWidth * this.healthRatio), healthBarHeight)
            .cornerRadius(healthBarHeight * 0.5F)
            .color(LOW_HEALTH_COLOR.withAlphaNormalized(visibilityAlpha))
            .render(drawContext);
      }

      if (this.absorptionRatio > 0.01F) {
         RoundedGradientRectBuilder.at(healthBarX, healthBarY, Math.max(healthBarHeight, healthBarWidth * this.absorptionRatio), healthBarHeight)
            .cornerRadius(healthBarHeight * 0.5F)
            .color(ABSORPTION_COLOR.withAlphaNormalized(visibilityAlpha * 0.9F))
            .render(drawContext);
      }
   }
// https://t.me/pasterplace

   private void renderFoodPanel(DrawContext drawContext, PlayerEntity playerEntity, float panelX, float panelY, float panelWidth, float visibilityAlpha) {
      BrokkeIcons.drawPanelBackground(drawContext, panelX, panelY, panelWidth, 12.0F, visibilityAlpha);
      float barCenterY = panelY + 6.0F;
      String foodLevelLabel = String.valueOf(playerEntity.getHungerManager().getFoodLevel());
      float foodLabelWidth = BrokkeIcons.measureTextWidth(foodLevelLabel, 4.8F);
      BrokkeIcons.drawCenteredText(drawContext, foodLevelLabel, panelX + 5.0F, barCenterY, 4.8F, BrokkeIcons.lightGrayWithAlpha(visibilityAlpha));
      drawHudIcon(drawContext, Avatar.DRUMSTICK_AVATAR, panelX + panelWidth - 7.0F, barCenterY, FOOD_BAR_COLOR.withAlphaNormalized(visibilityAlpha));
      float foodBarX = panelX + 8.0F + foodLabelWidth;
      float foodBarWidth = panelWidth - 8.0F - foodLabelWidth - 13.0F;
      float foodBarHeight = 2.4F;
      float foodBarY = barCenterY - foodBarHeight * 0.5F;
      RoundedGradientRectBuilder.at(foodBarX, foodBarY, foodBarWidth, foodBarHeight)
         .cornerRadius(foodBarHeight * 0.5F)
         .color(Accent.getAccentColorWithAlpha((int)(22.0F * visibilityAlpha)))
         .render(drawContext);
      float hungerRatio = Math.clamp(this.hungerRatio, 0.0F, 1.0F);
      if (hungerRatio > 0.002F) {
         float filledFoodBarWidth = Math.max(foodBarHeight, foodBarWidth * hungerRatio);
         RoundedGradientRectBuilder.at(foodBarX + foodBarWidth - filledFoodBarWidth, foodBarY, filledFoodBarWidth, foodBarHeight)
            .cornerRadius(foodBarHeight * 0.5F)
            .color(FOOD_BAR_COLOR.withAlphaNormalized(visibilityAlpha))
            .render(drawContext);
      }
   }
// https://t.me/pasterplace

   private static void renderHotbarItem(HudRenderContext renderContext, ItemStack stack, float slotX, float slotY, float visibilityAlpha) {
      if (!stack.isEmpty() && !(visibilityAlpha < 0.4F)) {
         DrawContext drawContext = renderContext.drawContext();
         float itemIconX = slotX + 2.5F;
         float itemIconY = slotY + 2.5F;
         renderContext.postRenderCallbacks().add(() -> BrokkeIcons.drawItemStack(drawContext, stack, itemIconX, itemIconY, 11.0F));
         float cooldownProgress = CLIENT.player == null
            ? 0.0F
            : CLIENT.player.getItemCooldownManager().getCooldownProgress(stack, CLIENT.getRenderTickCounter().getTickProgress(true));
         if (cooldownProgress > 0.0F) {
            float cooldownOverlayHeight = 16.0F * cooldownProgress;
            renderContext.postRenderCallbacks()
               .add(
                  () -> RoundedGradientRectBuilder.at(slotX, slotY + 16.0F - cooldownOverlayHeight, 16.0F, cooldownOverlayHeight)
                     .cornerRadius(2.5F)
                     .color(RgbaColor.rgba(0, 0, 0, (int)(140.0F * visibilityAlpha)))
                     .render(drawContext)
               );
         }

         if (stack.isItemBarVisible()) {
            float itemBarProgress = stack.getItemBarStep() / 13.0F;
            RgbaColor tintColor = RgbaColor.fromArgb(0xFF000000 | stack.getItemBarColor()).withAlphaNormalized(visibilityAlpha);
            renderContext.postRenderCallbacks()
               .add(() -> BrokkeIcons.drawProgressBar(drawContext, slotX + 3.0F, slotY + 16.0F - 2.4F, 10.0F, 1.0F, itemBarProgress, tintColor, visibilityAlpha));
         }

         if (stack.getCount() > 1) {
            String stackCountText = String.valueOf(stack.getCount());
            float stackCountTextWidth = BrokkeIcons.measureTextWidth(stackCountText, 3.8F);
            renderContext.postRenderCallbacks()
               .add(
                  () -> BrokkeIcons.drawCenteredText(
                     drawContext, stackCountText, slotX + 16.0F - stackCountTextWidth - 1.2F, slotY + 16.0F - 2.8F, 3.8F, BrokkeIcons.lightGrayWithAlpha(visibilityAlpha)
                  )
               );
         }
      }
   }
// https://t.me/pasterplace

   private static void drawHudIcon(
      DrawContext drawContext, AvatarTexture texture, float x, float y, RgbaColor tintColor
   ) {
      TextureQuadBuilder.at(texture, x - 3.25F, y - 3.25F, 6.5F, 6.5F)
         .tint(tintColor)
         .render(drawContext);
   }
}

