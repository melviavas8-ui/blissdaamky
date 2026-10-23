package dev.daamky;

import net.minecraft.client.gui.DrawContext;

public final class ClickGuiSidebar {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final String[] TAB_LABELS = new String[]{"Modules", "Visuals", "Client", "Social"};
   private static final ModuleCategory[][] TAB_CATEGORIES = new ModuleCategory[][]{
      {ModuleCategory.COMBAT, ModuleCategory.MOVEMENT, ModuleCategory.PLAYER, ModuleCategory.WORLD},
      {ModuleCategory.RENDER},
      {ModuleCategory.BOTS, ModuleCategory.COSMETICS, ModuleCategory.THEMES, ModuleCategory.CLOUD_CONFIG},
      {ModuleCategory.GLOBALS}
   };
   private static final float TAB_HEADER_HEIGHT = 11.0F;
   private static final float CATEGORY_ROW_HEIGHT = 13.0F;
   private static final float TAB_SECTION_SPACING = 3.0F;
   private static final float SIDEBAR_PADDING = 6.0F;
   private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
   private ModuleCategory previousCategory = ModuleCategory.COMBAT;
   private final float[] hoverProgressByCategory = new float[ModuleCategory.values().length];
   private final float[] rowYByCategory = new float[ModuleCategory.values().length];
   private float selectedIndicatorY = -1.0F;
   private float panelTop;
   private float panelWidth;
   private float frameDeltaSeconds = 0.016F;
// https://t.me/pasterplace

   public void setFrameDeltaSeconds(float frameDeltaSeconds) {
      this.frameDeltaSeconds = frameDeltaSeconds;
   }
// https://t.me/pasterplace

   public void render(DrawContext drawContext, float mouseX, float mouseY, float sidebarX, float sidebarY, float sidebarWidth, float animationProgress) {
      if (!(animationProgress <= 0.0F)) {
         this.panelTop = sidebarX;
         this.panelWidth = sidebarWidth;
         float rowY = sidebarY;
         float selectedRowY = -1.0F;

         for (int tabIndex = 0; tabIndex < TAB_LABELS.length; tabIndex++) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, TAB_LABELS[tabIndex], sidebarX + 6.0F + 2.0F, rowY + 5.5F, 4.2F)
               .baselinePosition(TextBaselinePosition.CENTER)
               .color(Accent.getDeepTextColor((int)(150.0F * animationProgress)))
               .render(drawContext);
            rowY += 11.0F;
// https://t.me/pasterplace

            for (ModuleCategory category : TAB_CATEGORIES[tabIndex]) {
               int categoryOrdinal = category.ordinal();
               this.rowYByCategory[categoryOrdinal] = rowY;
               boolean isHovered = mouseX >= sidebarX && mouseX <= sidebarX + sidebarWidth && mouseY >= rowY && mouseY <= rowY + 13.0F;
               this.hoverProgressByCategory[categoryOrdinal] = this.hoverProgressByCategory[categoryOrdinal]
                  + ((isHovered ? 1.0F : 0.0F) - this.hoverProgressByCategory[categoryOrdinal]) * (1.0F - (float)Math.exp(-10.0F * this.frameDeltaSeconds));
               boolean isSelected = category == this.selectedCategory;
               if (isSelected) {
                  selectedRowY = rowY;
               } else if (this.hoverProgressByCategory[categoryOrdinal] > 0.01F) {
                  RoundedGradientRectBuilder.at(sidebarX + 6.0F, rowY, sidebarWidth - 12.0F, 13.0F)
                     .cornerRadius(3.5F)
                     .color(Accent.getTextColorWithAlpha((int)(7.0F * this.hoverProgressByCategory[categoryOrdinal] * animationProgress)))
                     .render(drawContext);
               }

               rowY += 13.0F;
            }

            rowY += 3.0F;
         }

         if (selectedRowY >= 0.0F) {
            this.selectedIndicatorY = this.selectedIndicatorY < 0.0F
               ? selectedRowY
               : this.selectedIndicatorY + (selectedRowY - this.selectedIndicatorY) * (1.0F - (float)Math.exp(-11.0F * this.frameDeltaSeconds));
            RoundedGradientRectBuilder.at(sidebarX + 6.0F, this.selectedIndicatorY, sidebarWidth - 12.0F, 13.0F)
               .cornerRadius(3.5F)
               .color(Accent.getTextColorWithAlpha((int)(14.0F * animationProgress)))
               .render(drawContext);
         }

         for (int tabIndex = 0; tabIndex < TAB_LABELS.length; tabIndex++) {
            for (ModuleCategory category : TAB_CATEGORIES[tabIndex]) {
               int categoryOrdinal = category.ordinal();
               boolean isSelected = category == this.selectedCategory;
               float categoryLabelY = this.rowYByCategory[categoryOrdinal] + 6.5F;
               int categoryTextColor = ColorUtils.interpolateColor(
                  Accent.getDeepTextColor((int)(210.0F * animationProgress)).toArgb(),
                  Accent.getTextColorWithAlpha((int)(255.0F * animationProgress)).toArgb(),
                  isSelected ? 1.0F : this.hoverProgressByCategory[categoryOrdinal]
               );
               GuiTextBuilder.createAt(Medium.ICON_FONT, category.l1I1IlllIlI(), sidebarX + 6.0F + 7.0F, categoryLabelY, 5.6F)
                  .centered()
                  .color(isSelected ? Accent.getAccentColorWithAlpha((int)(255.0F * animationProgress)).toArgb() : categoryTextColor)
                  .render(drawContext);
               GuiTextBuilder.createAt(Medium.DEFAULT_FONT, category.II1Il11l111II11IIl(), sidebarX + 6.0F + 13.5F, categoryLabelY, 4.8F)
                  .baselinePosition(TextBaselinePosition.CENTER)
                  .color(categoryTextColor)
                  .render(drawContext);
               if (category == ModuleCategory.GLOBALS) {
                  int unreadCount = GlobalsClient.getInstance().getTotalUnreadCount();
                  if (unreadCount > 0) {
                     String unreadCountLabel = unreadCount > 99 ? "99+" : String.valueOf(unreadCount);
                     float badgeWidth = Math.max(7.0F, Medium.DEFAULT_FONT.measureWidth(unreadCountLabel, 3.6F) + 4.0F);
                     float badgeX = sidebarX + sidebarWidth - 6.0F - 3.0F - badgeWidth;
                     RoundedGradientRectBuilder.at(badgeX, categoryLabelY - 3.5F, badgeWidth, 7.0F)
                        .cornerRadius(3.5F)
                        .color(Accent.getAccentColorWithAlpha((int)(255.0F * animationProgress)))
                        .render(drawContext);
                     GuiTextBuilder.createAt(Medium.DEFAULT_FONT, unreadCountLabel, badgeX + badgeWidth * 0.5F, categoryLabelY, 3.6F)
                        .centered()
                        .color(Accent.getTextColorWithAlpha((int)(255.0F * animationProgress)))
                        .render(drawContext);
                  }
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   public boolean handleClick(float mouseX, float mouseY, int button) {
      if (button != 0) {
         return false;
      }

      if (!(mouseX < this.panelTop) && !(mouseX > this.panelTop + this.panelWidth)) {
         for (int tabIndex = 0; tabIndex < TAB_LABELS.length; tabIndex++) {
            for (ModuleCategory category : TAB_CATEGORIES[tabIndex]) {
               float categoryRowY = this.rowYByCategory[category.ordinal()];
               if (mouseY >= categoryRowY && mouseY <= categoryRowY + 13.0F) {
                  this.setCategory(category);
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   public ModuleCategory getSelectedCategory() {
      return this.selectedCategory;
   }
// https://t.me/pasterplace

   public ModuleCategory getPreviousCategory() {
      return this.previousCategory;
   }
// https://t.me/pasterplace

   public ModuleCategory getDisplayCategory() {
      if (this.selectedCategory != null) {
         return this.selectedCategory;
      } else {
         return this.previousCategory != null ? this.previousCategory : ModuleCategory.COMBAT;
      }
   }
// https://t.me/pasterplace

   public static String getTabName(ModuleCategory moduleCategory) {
      for (int tabIndex = 0; tabIndex < TAB_LABELS.length; tabIndex++) {
         for (ModuleCategory categoriesInTab : TAB_CATEGORIES[tabIndex]) {
            if (categoriesInTab == moduleCategory) {
               return TAB_LABELS[tabIndex];
            }
         }
      }

      return "";
   }
// https://t.me/pasterplace

   public void setCategory(ModuleCategory moduleCategory) {
      if (moduleCategory != null) {
         if (this.selectedCategory != moduleCategory) {
            if (this.selectedCategory != null) {
               this.previousCategory = this.selectedCategory;
            }

            this.selectedCategory = moduleCategory;
         }
      }
   }
}

