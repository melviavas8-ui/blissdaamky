package dev.daamky;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public abstract class AnimatedListHudElement extends HudElementPosition {
   private static final float DEFAULT_ROW_HEIGHT = 15.0F;
   private static final float ENTRY_ROW_HEIGHT = 11.0F;
   private static final float HORIZONTAL_PADDING = 5.5F;
   private static final float MINIMUM_LIST_WIDTH = 64.0F;
   private float animatedListWidth = 64.0F;
   private float animatedListHeight = 15.0F;
   private List<AnimatedListHudElement.HudListEntry> visibleEntries = List.of();
   private final Map<String, Float> entryVisibilityProgress = new HashMap<>();
   private final Map<String, Float> entryProgressValues = new HashMap<>();

   protected String getHeaderGlyph() {
      return null;
   }

   protected MsdfFont getHeaderFont() {
      return Medium.ICON_FONT;
   }

   protected ItemStack getHeaderItemStack() {
      return null;
   }

   protected String getEntryGlyph(AnimatedListHudElement.HudListEntry hudEntry) {
      return null;
   }

   protected boolean renderDetailAsBadge() {
      return false;
   }

   protected boolean shouldShowTrailingProgress() {
      return false;
   }

   protected boolean shouldShowDetailProgress() {
      return false;
   }

   protected AnimatedListHudElement(String displayName, String settingsKey, float initialX, float initialY) {
      super(displayName, settingsKey, initialX, initialY);
      this.elementWidth = 64.0F;
      this.elementHeight = 15.0F;
   }

   protected abstract String getHeaderLabel();

   protected abstract List<AnimatedListHudElement.HudListEntry> getEntries(boolean chatScreenOpen);

   @Override
   public void updateAnimation(float tickDelta) {
      this.animatedListWidth = interpolate(this.animatedListWidth, this.getAnimatedWidth(), 9.0F, tickDelta);
      this.animatedListHeight = interpolate(this.animatedListHeight, this.getAnimatedHeight(), 9.0F, tickDelta);
      this.elementWidth = this.animatedListWidth;
      this.elementHeight = this.animatedListHeight;
      HashMap nextVisibilityProgress = new HashMap();

      for (Object entryObject : this.visibleEntries) {
         nextVisibilityProgress.put(
            ((AnimatedListHudElement.HudListEntry)entryObject).title(),
            interpolate(
               this.entryVisibilityProgress.getOrDefault(((AnimatedListHudElement.HudListEntry)entryObject).title(), 0.0F), 1.0F, 10.0F, tickDelta
            )
         );
      }

      this.entryVisibilityProgress.clear();
      this.entryVisibilityProgress.putAll(nextVisibilityProgress);
      HashMap nextProgressValues = new HashMap();

      for (Object entryObject : this.visibleEntries) {
         if (!(((AnimatedListHudElement.HudListEntry)entryObject).animationProgress() < 0.0F)) {
            Float currentProgress = this.entryProgressValues.get(((AnimatedListHudElement.HudListEntry)entryObject).title());
            nextProgressValues.put(
               ((AnimatedListHudElement.HudListEntry)entryObject).title(),
               currentProgress == null
                  ? ((AnimatedListHudElement.HudListEntry)entryObject).animationProgress()
                  : interpolate(currentProgress, ((AnimatedListHudElement.HudListEntry)entryObject).animationProgress(), 12.0F, tickDelta)
            );
         }
      }

      this.entryProgressValues.clear();
      this.entryProgressValues.putAll(nextProgressValues);
   }

   private float getAnimatedHeight() {
      return this.visibleEntries.isEmpty() ? 15.0F : 17.0F + this.visibleEntries.size() * 11.0F + 2.5F;
   }

   private float getAnimatedWidth() {
      float headerWidth = BrokkeIcons.measureTextWidth(this.getHeaderLabel(), 6.2F) + 26.0F;

      for (Object entryObject : this.visibleEntries) {
         float entryWidth = BrokkeIcons.measureTextWidth(((AnimatedListHudElement.HudListEntry)entryObject).title(), 5.7F) + 9.0F;
         if (((AnimatedListHudElement.HudListEntry)entryObject).icon() != null
            || ((AnimatedListHudElement.HudListEntry)entryObject).textColor() != null
            || ((AnimatedListHudElement.HudListEntry)entryObject).iconId() != null) {
            entryWidth += 10.0F;
         }

         if (this.shouldShowTrailingProgress() && ((AnimatedListHudElement.HudListEntry)entryObject).animationProgress() >= 0.0F) {
            entryWidth += 10.0F;
         }

         if (this.shouldShowDetailProgress() && ((AnimatedListHudElement.HudListEntry)entryObject).animationProgress() >= 0.0F) {
            entryWidth += 28.0F;
         } else if (((AnimatedListHudElement.HudListEntry)entryObject).detail() != null) {
            entryWidth += BrokkeIcons.measureTextWidth(((AnimatedListHudElement.HudListEntry)entryObject).detail(), this.renderDetailAsBadge() ? 4.8F : 5.7F)
               + (this.renderDetailAsBadge() ? 6.0F : 0.0F);
         }

         headerWidth = Math.max(headerWidth, entryWidth);
      }

      return Math.max(64.0F, headerWidth + 11.0F - 8.0F);
   }

   @Override
   public boolean isVisibleInCurrentScreen(boolean chatScreenOpen) {
      this.visibleEntries = this.getEntries(chatScreenOpen);
      return chatScreenOpen || !this.visibleEntries.isEmpty();
   }

   @Override
   public void render(HudRenderContext renderContext, float alpha) {
      DrawContext drawContext = renderContext.drawContext();
      float elementX = this.getX();
      float elementY = this.getY();
      float boxWidth = this.animatedListWidth;
      float boxHeight = this.animatedListHeight;
      BrokkeIcons.drawPanelBackground(drawContext, elementX, elementY, boxWidth, boxHeight, alpha);
      float headerY = elementY + 7.5F;
      String headerGlyph = this.getHeaderGlyph();
      ItemStack headerItem = this.getHeaderItemStack();
      float contentX = elementX + 5.5F + 6.0F;
      if (headerItem != null) {
         float headerItemX = elementX + 5.5F - 0.5F;
         float headerItemY = headerY - 3.75F;
         if (alpha > 0.4F) {
            renderContext.postRenderCallbacks().add(() -> BrokkeIcons.drawItemStack(drawContext, headerItem, headerItemX, headerItemY, 7.5F));
         }

         contentX = elementX + 5.5F + 9.5F;
      } else if (headerGlyph != null) {
         BrokkeIcons.drawCenteredTextWithFont(drawContext, this.getHeaderFont(), headerGlyph, elementX + 5.5F + 3.0F, headerY, 6.0F, alpha);
         contentX = elementX + 5.5F + 9.5F;
      } else {
         BrokkeIcons.drawColorSwatch(drawContext, elementX + 5.5F + 1.5F, headerY, 3.0F, BrokkeIcons.whiteWithAlpha(alpha));
      }

      BrokkeIcons.drawCenteredText(drawContext, this.getHeaderLabel(), contentX, headerY, 6.2F, BrokkeIcons.lightGrayWithAlpha(alpha));
      String entryCountText = String.valueOf(this.visibleEntries.size());
      float entryCountWidth = BrokkeIcons.measureTextWidth(entryCountText, 4.8F);
      float countBadgeWidth = Math.max(8.0F, entryCountWidth + 5.0F);
      float countBadgeX = elementX + boxWidth - 5.5F - countBadgeWidth;
      RoundedGradientRectBuilder.at(countBadgeX, headerY - 3.75F, countBadgeWidth, 7.5F)
         .cornerRadius(3.75F)
         .color(BrokkeIcons.whiteWithAlpha(alpha * 0.16F))
         .render(drawContext);
      BrokkeIcons.drawCenteredText(drawContext, entryCountText, countBadgeX + (countBadgeWidth - entryCountWidth) * 0.5F, headerY, 4.8F, BrokkeIcons.whiteWithAlpha(alpha));
      if (!this.visibleEntries.isEmpty()) {
         float firstEntryY = elementY + 15.0F + 2.0F;
         float entriesBottomY = elementY + boxHeight + 1.0F;

         for (Object entryObject : this.visibleEntries) {
            if (firstEntryY + 11.0F > entriesBottomY) {
               break;
            }

            float entryVisibility = this.entryVisibilityProgress.getOrDefault(((AnimatedListHudElement.HudListEntry)entryObject).title(), 1.0F);
            float entryAlpha = alpha * entryVisibility;
            float entryCenterY = firstEntryY + 5.5F;
            float entryIconX = elementX + 5.5F - (1.0F - entryVisibility) * 4.0F;
            if (((AnimatedListHudElement.HudListEntry)entryObject).iconId() != null) {
               Identifier iconTexture = ((AnimatedListHudElement.HudListEntry)entryObject).iconId();
               float iconTextureX = entryIconX - 0.5F;
               float iconTextureY = entryCenterY - 4.5F;
               float iconAlpha = alpha;
               renderContext.postRenderCallbacks().add(() -> BrokkeIcons.drawTexture(drawContext, iconTexture, iconTextureX, iconTextureY, 9.0F, iconAlpha));
               entryIconX += 10.0F;
            } else if (((AnimatedListHudElement.HudListEntry)entryObject).icon() != null) {
               ItemStack entryItemIcon = ((AnimatedListHudElement.HudListEntry)entryObject).icon();
               float entryItemX = entryIconX;
               float entryItemY = entryCenterY - 4.0F;
               if (alpha > 0.4F) {
                  renderContext.postRenderCallbacks().add(() -> BrokkeIcons.drawItemStack(drawContext, entryItemIcon, entryItemX, entryItemY, 8.0F));
               }

               entryIconX += 10.0F;
            } else if (this.getEntryGlyph((AnimatedListHudElement.HudListEntry)entryObject) != null) {
               BrokkeIcons.drawCenteredText(
                  drawContext, this.getEntryGlyph((AnimatedListHudElement.HudListEntry)entryObject), entryIconX + 2.5F, entryCenterY, 5.2F, entryAlpha
               );
               entryIconX += 8.0F;
            } else if (((AnimatedListHudElement.HudListEntry)entryObject).textColor() != null) {
               BrokkeIcons.drawColorSwatch(
                  drawContext, entryIconX + 2.5F, entryCenterY, 3.5F, ((AnimatedListHudElement.HudListEntry)entryObject).textColor().multiplyAlpha(alpha)
               );
               entryIconX += 10.0F;
            }

            BrokkeIcons.drawCenteredText(
               drawContext, ((AnimatedListHudElement.HudListEntry)entryObject).title(), entryIconX, entryCenterY, 5.7F, BrokkeIcons.lightGrayWithAlpha(entryAlpha)
            );
            if (this.shouldShowTrailingProgress() && ((AnimatedListHudElement.HudListEntry)entryObject).animationProgress() >= 0.0F) {
               drawProgressRing(
                  drawContext,
                  elementX + boxWidth - 5.5F - 3.5F,
                  entryCenterY,
                  3.0F,
                  this.entryProgressValues
                     .getOrDefault(
                        ((AnimatedListHudElement.HudListEntry)entryObject).title(), ((AnimatedListHudElement.HudListEntry)entryObject).animationProgress()
                     ),
                  entryAlpha
               );
               firstEntryY += 11.0F;
            } else {
               if (((AnimatedListHudElement.HudListEntry)entryObject).detail() != null && this.renderDetailAsBadge()) {
                  float detailTextWidth = BrokkeIcons.measureTextWidth(((AnimatedListHudElement.HudListEntry)entryObject).detail(), 4.8F);
                  float detailBadgeWidth = Math.max(8.5F, detailTextWidth + 6.0F);
                  float detailBadgeX = elementX + boxWidth - 5.5F - detailBadgeWidth;
                  RoundedGradientRectBuilder.at(detailBadgeX, entryCenterY - 4.0F, detailBadgeWidth, 8.0F)
                     .cornerRadius(2.5F)
                     .color(BrokkeIcons.whiteWithAlpha(entryAlpha * 0.14F))
                     .render(drawContext);
                  BrokkeIcons.drawCenteredText(
                     drawContext,
                     ((AnimatedListHudElement.HudListEntry)entryObject).detail(),
                     detailBadgeX + (detailBadgeWidth - detailTextWidth) * 0.5F,
                     entryCenterY,
                     4.8F,
                     BrokkeIcons.whiteWithAlpha(entryAlpha)
                  );
               } else if (((AnimatedListHudElement.HudListEntry)entryObject).detail() != null) {
                  RgbaColor detailBackgroundColor = ((AnimatedListHudElement.HudListEntry)entryObject).backgroundColor() != null
                     ? ((AnimatedListHudElement.HudListEntry)entryObject).backgroundColor().multiplyAlpha(alpha)
                     : BrokkeIcons.mutedGrayWithAlpha(alpha);
                  float detailRightEdge = elementX + boxWidth - 5.5F;
                  if (this.shouldShowDetailProgress() && ((AnimatedListHudElement.HudListEntry)entryObject).animationProgress() >= 0.0F) {
                     drawProgressRing(
                        drawContext,
                        detailRightEdge - 3.0F,
                        entryCenterY,
                        3.0F,
                        this.entryProgressValues
                           .getOrDefault(
                              ((AnimatedListHudElement.HudListEntry)entryObject).title(), ((AnimatedListHudElement.HudListEntry)entryObject).animationProgress()
                           ),
                        entryAlpha
                     );
                     detailRightEdge -= 10.0F;
                  }

                  BrokkeIcons.drawCenteredText(
                     drawContext,
                     ((AnimatedListHudElement.HudListEntry)entryObject).detail(),
                     detailRightEdge - BrokkeIcons.measureTextWidth(((AnimatedListHudElement.HudListEntry)entryObject).detail(), 5.7F),
                     entryCenterY,
                     5.7F,
                     detailBackgroundColor
                  );
               }

               if (((AnimatedListHudElement.HudListEntry)entryObject).animationProgress() >= 0.0F && !this.shouldShowDetailProgress()) {
                  BrokkeIcons.drawProgressBar(
                     drawContext,
                     entryIconX,
                     firstEntryY + 11.0F - 1.6F,
                     elementX + boxWidth - 5.5F - entryIconX,
                     1.1F,
                     ((AnimatedListHudElement.HudListEntry)entryObject).animationProgress(),
                     BrokkeIcons.whiteWithAlpha(alpha * 0.85F),
                     alpha
                  );
               }

               firstEntryY += 11.0F;
            }
         }
      }
   }

   public static void drawProgressRing(DrawContext drawContext, float centerX, float centerY, float radius, float progress, float alpha) {
      drawProgressRing(drawContext, centerX, centerY, radius, 1.1F, 22, progress, alpha);
   }

   public static void drawProgressRing(
      DrawContext drawContext, float centerX, float centerY, float radius, float dotRadius, int segmentCount, float progress, float alpha
   ) {
      float clampedProgress = Math.clamp(progress, 0.0F, 1.0F);
      float filledSegments = clampedProgress * segmentCount;
      float animationPhase = (float)(System.currentTimeMillis() % 2400L) / 2400.0F;

      for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
         double angleRadians = (-Math.PI / 2) + (double)segmentIndex / segmentCount * Math.PI * 2.0;
         float dotX = centerX + (float)Math.cos(angleRadians) * radius;
         float dotY = centerY + (float)Math.sin(angleRadians) * radius;
         float segmentProgress = Math.clamp(filledSegments - segmentIndex, 0.0F, 1.0F);
         BrokkeIcons.drawColorSwatch(drawContext, dotX, dotY, dotRadius, BrokkeIcons.whiteWithAlpha(alpha * 0.14F));
         if (segmentProgress > 0.0F) {
            float activeSegment = segmentIndex == (int)filledSegments ? 1.0F : 0.0F;
            float pulseAlpha = activeSegment * (0.5F + 0.5F * (float)Math.sin(animationPhase * Math.PI * 2.0));
            BrokkeIcons.drawColorSwatch(drawContext, dotX, dotY, dotRadius + pulseAlpha * 0.6F, BrokkeIcons.whiteWithAlpha(alpha * segmentProgress));
         }
      }
   }

   public static void drawProgressRing(
      DrawContext drawContext,
      float centerX,
      float centerY,
      float radius,
      float dotRadius,
      int segmentCount,
      float progress,
      RgbaColor color,
      float alpha
   ) {
      float filledSegments = Math.clamp(progress, 0.0F, 1.0F) * segmentCount;

      for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
         float segmentProgress = Math.clamp(filledSegments - segmentIndex, 0.0F, 1.0F);
         if (segmentProgress <= 0.0F) {
            break;
         }

         double angleRadians = (-Math.PI / 2) + (double)segmentIndex / segmentCount * Math.PI * 2.0;
         float dotX = centerX + (float)Math.cos(angleRadians) * radius;
         float dotY = centerY + (float)Math.sin(angleRadians) * radius;
         BrokkeIcons.drawColorSwatch(drawContext, dotX, dotY, dotRadius, color.multiplyAlpha(alpha * segmentProgress));
      }
   }

   public record HudListEntry(String title, String detail, ItemStack icon, RgbaColor textColor, RgbaColor backgroundColor, float animationProgress, Identifier iconId) {

      public HudListEntry(String title, String detail, ItemStack icon, RgbaColor textColor, RgbaColor backgroundColor, float animationProgress) {
         this(title, detail, icon, textColor, backgroundColor, animationProgress, null);
      }

      public HudListEntry(String title, String detail, ItemStack icon, RgbaColor textColor, RgbaColor backgroundColor, float animationProgress, Identifier iconId) {
         this.title = title;
         this.detail = detail;
         this.icon = icon;
         this.textColor = textColor;
         this.backgroundColor = backgroundColor;
         this.animationProgress = animationProgress;
         this.iconId = iconId;
      }

      public static AnimatedListHudElement.HudListEntry simpleEntry(String title, String detail) {
         return new AnimatedListHudElement.HudListEntry(title, detail, null, null, null, -1.0F);
      }
   }
}

