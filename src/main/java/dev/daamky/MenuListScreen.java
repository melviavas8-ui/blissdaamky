package dev.daamky;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

/**
 * Общая основа для экранов-списков в стиле главного меню и экрана аккаунтов:
 * слева панель действий, справа поиск и прокручиваемый список карточек.
 */
abstract class MenuListScreen extends MenuBackground {
   protected static final float SIDE_PANEL_WIDTH = 92.0F;
   protected static final float LIST_WIDTH = 188.0F;
   protected static final float LIST_HEIGHT = 120.0F;
   protected static final float ROW_HEIGHT = 26.0F;
   protected static final float ROW_PITCH = 30.0F;
   private static final float COLUMN_GAP = 8.0F;
   private static final long DOUBLE_CLICK_MILLIS = 350L;

   private final Screen previousScreen;
   private final String screenTitle;
   protected final SearchTextField searchInput = new SearchTextField("Search", 24, text -> {});
   private final List<ButtonHit> buttonHits = new ArrayList<>();
   private final List<InputHit> inputHits = new ArrayList<>();
   protected int selectedIndex = -1;
   private float scrollOffset;
   private float targetScroll;
   private String lastFilterText = "";
   private int lastClickedIndex = -1;
   private long lastClickMillis;
   private String statusMessage = "";
   private long statusStartedAtMillis;

   MenuListScreen(String screenTitle, Screen previousScreen) {
      super(screenTitle);
      this.screenTitle = screenTitle;
      this.previousScreen = previousScreen;
   }

   // ---------- то, что реализуют конкретные экраны ----------

   protected abstract int getEntryCount();

   protected abstract void drawEntry(DrawContext drawContext, int index, float cardLeft, float cardTop, float cardWidth, float opacity);

   protected abstract void drawSidePanel(DrawContext drawContext, float left, float top, float blurRadius, float opacity, long timeMillis);

   protected abstract void onFilterChanged(String filterLowercase);

   protected abstract void onSelect(int index);

   protected abstract void onActivate(int index);

   protected abstract void onAction(String actionId);

   protected abstract String getCountText();

   protected abstract String getEmptyText();

   protected void onFrame() {
   }

   // ---------- общие помощники ----------

   @Override
   protected float getScreenScale() {
      return 1.1F;
   }

   @Override
   public void close() {
      this.transitionTo(this.previousScreen);
   }

   protected final void showStatus(String message) {
      this.statusMessage = message;
      this.statusStartedAtMillis = System.currentTimeMillis();
   }

   protected final float getBlurRadius() {
      return Math.min(Accent.getInstance().getCornerRadius() * 0.5F, 5.0F);
   }

   protected final float getPanelLeft() {
      return (this.viewportWidth - 288.0F) * 0.5F;
   }

   protected final float getPanelTop() {
      return this.viewportHeight * 0.5F - 62.0F;
   }

   private float getListLeft() {
      return this.getPanelLeft() + SIDE_PANEL_WIDTH + COLUMN_GAP;
   }

   private float getListTop() {
      return this.getPanelTop() + 18.0F;
   }

   protected final void drawPanelHeader(DrawContext drawContext, String title, float left, float top, float width, float blurRadius, float opacity) {
      this.drawGlassPanel(drawContext, left, top, width, 15.0F, blurRadius, opacity, 0.0F);
      this.drawText(
         drawContext, Medium.DEFAULT_FONT, title, left + 6.0F, top + 7.5F, 5.0F, HorizontalTextAlignment.LEFT, Accent.getTextColorWithAlpha((int)(230.0F * opacity))
      );
   }

   protected final void drawGlassBox(DrawContext drawContext, float left, float top, float width, float height, float blurRadius, float opacity) {
      this.drawGlassPanel(drawContext, left, top, width, height, blurRadius, opacity, 0.0F);
   }

   protected final void drawInput(
      DrawContext drawContext, SearchTextField field, float left, float top, float width, float height, long timeMillis, float opacity
   ) {
      field.render(drawContext, left, top, width, height, timeMillis, opacity);
      this.inputHits.add(new InputHit(field, left, top, width, height));
   }

   protected final void drawActionButton(
      DrawContext drawContext, String actionId, String label, String hint, float left, float top, float width, float blurRadius, float opacity
   ) {
      float hover = this.getHoverProgress(actionId, this.isMouseWithin(left, top, width, 14.0F));
      this.buttonHits.add(new ButtonHit(actionId, left, top, width, 14.0F));
      this.pushPivotScale(drawContext, left + width * 0.5F, top + 7.0F, (1.0F + 0.025F * hover) * this.getTextChangeScale(actionId));
      this.drawGlassPanel(drawContext, left, top, width, 14.0F, blurRadius, opacity, hover);
      if (hover > 0.01F) {
         RoundedGradientRectBuilder.at(left + 1.5F, top + 7.0F - 3.0F * hover, 1.0F, 6.0F * hover)
            .cornerRadius(0.5F)
            .color(Accent.getAccentColorWithAlpha((int)(255.0F * opacity * hover)))
            .render(drawContext);
      }

      boolean hasHint = hint != null && !hint.isEmpty();
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         label,
         hasHint ? left + 6.0F + 1.5F * hover : left + width * 0.5F,
         top + 7.0F,
         4.8F,
         hasHint ? HorizontalTextAlignment.LEFT : HorizontalTextAlignment.CENTER,
         Accent.getDarkerTextColor((int)((190.0F + 65.0F * hover) * opacity))
      );
      if (hasHint) {
         this.drawText(
            drawContext,
            Medium.DEFAULT_FONT,
            hint,
            left + width - 5.0F,
            top + 7.0F,
            4.0F,
            HorizontalTextAlignment.RIGHT,
            Accent.getDeepTextColor((int)((140.0F + 90.0F * hover) * opacity))
         );
      }

      this.popTransformMatrix(drawContext);
   }

   protected final void clampSelection() {
      int count = this.getEntryCount();
      if (count <= 0) {
         this.selectedIndex = -1;
      } else if (this.selectedIndex >= count) {
         this.selectedIndex = count - 1;
      }
   }

   protected final void resetScroll() {
      this.targetScroll = 0.0F;
      this.scrollOffset = 0.0F;
   }

   private float getMaxScroll(int count) {
      return Math.max(0.0F, count * ROW_PITCH + 6.0F - LIST_HEIGHT);
   }

   private void ensureRowVisible(int index) {
      float rowTop = 6.0F + index * ROW_PITCH;
      if (rowTop < this.targetScroll) {
         this.targetScroll = rowTop - 6.0F;
      } else if (rowTop + ROW_HEIGHT > this.targetScroll + LIST_HEIGHT - 6.0F) {
         this.targetScroll = rowTop + ROW_HEIGHT - LIST_HEIGHT + 6.0F;
      }
   }

   protected final void selectIndex(int index) {
      this.selectedIndex = index;
      this.ensureRowVisible(index);
      this.onSelect(index);
   }

   // ---------- отрисовка ----------

   @Override
   protected void drawContent(DrawContext drawContext, float contentAlpha, long currentTimeMillis) {
      this.onFrame();
      this.buttonHits.clear();
      this.inputHits.clear();
      int count = this.getEntryCount();
      this.targetScroll = Math.clamp(this.targetScroll, 0.0F, this.getMaxScroll(count));
      this.scrollOffset = this.scrollOffset + (this.targetScroll - this.scrollOffset) * (1.0F - (float)Math.exp(-this.frameDeltaSeconds * 16.0F));
      float menuAlpha = 1.0F - this.getTransitionProgress();
      float panelLeft = this.getPanelLeft();
      float panelTop = this.getPanelTop();
      float blurRadius = this.getBlurRadius();
      float centerX = this.viewportWidth * 0.5F;
      float headerProgress = this.easeOutCubic(0.0F, 550.0F);
      float headerY = panelTop - 26.0F - (1.0F - headerProgress) * 10.0F;
      this.pushPivotScale(drawContext, centerX, headerY, 0.9F + 0.1F * this.easeOutBack(0.0F, 650.0F));
      this.drawText(
         drawContext,
         BOLD_FONT,
         this.screenTitle.toUpperCase(Locale.ROOT),
         centerX,
         headerY,
         24.0F,
         HorizontalTextAlignment.CENTER,
         Accent.getTextColorWithAlpha((int)(245.0F * headerProgress * menuAlpha))
      );
      this.popTransformMatrix(drawContext);
      float panelVisibility = this.easeOutCubic(60.0F, 450.0F);
      this.pushPivotScale(
         drawContext,
         centerX,
         panelTop - 8.0F + 77.0F,
         0.94F + 0.06F * this.easeOutBack(60.0F, 600.0F) - this.getTransitionProgress() * 0.03F
      );
      this.drawAccentPanel(drawContext, panelLeft - 8.0F, panelTop - 8.0F, 304.0F, 154.0F, panelVisibility * menuAlpha);
      float sideProgress = this.easeOutCubic(160.0F, 450.0F);
      this.drawSidePanel(drawContext, panelLeft - (1.0F - sideProgress) * 10.0F, panelTop, blurRadius, sideProgress * menuAlpha, currentTimeMillis);
      float listProgress = this.easeOutCubic(200.0F, 450.0F);
      this.drawListPanel(
         drawContext, panelLeft + SIDE_PANEL_WIDTH + COLUMN_GAP + (1.0F - listProgress) * 10.0F, panelTop, blurRadius, listProgress * menuAlpha, currentTimeMillis
      );
      this.popTransformMatrix(drawContext);
      float hintVisibility = this.easeOutCubic(380.0F, 450.0F) * menuAlpha;
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         "Esc - back   |   Up / Down - select   |   Enter - open   |   F5 - refresh",
         centerX,
         panelTop + 146.0F + 8.0F,
         4.2F,
         HorizontalTextAlignment.CENTER,
         Accent.getDeepTextColor((int)(110.0F * hintVisibility))
      );
      this.drawStatusMessage(drawContext, centerX, panelTop + 146.0F + 22.0F, contentAlpha, currentTimeMillis);
   }

   private void drawStatusMessage(DrawContext drawContext, float centerX, float statusY, float contentAlpha, long currentTimeMillis) {
      long elapsed = currentTimeMillis - this.statusStartedAtMillis;
      if (this.statusMessage.isEmpty() || elapsed >= 2500L) {
         return;
      }

      float entry = Math.min(1.0F, (float)elapsed / 220.0F);
      entry = 1.0F - (1.0F - entry) * (1.0F - entry) * (1.0F - entry);
      float exit = Math.min(1.0F, (float)(2500L - elapsed) / 350.0F);
      float alpha = contentAlpha * entry * exit;
      float width = Medium.DEFAULT_FONT.measureWidth(this.statusMessage, 4.8F) + 14.0F;
      float y = statusY + (1.0F - entry) * 6.0F;
      this.pushPivotScale(drawContext, centerX, y, 0.9F + 0.1F * entry);
      this.drawGlassPanel(drawContext, centerX - width * 0.5F, y - 6.0F, width, 12.0F, 6.0F, alpha, 0.4F);
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         this.statusMessage,
         centerX,
         y,
         4.8F,
         HorizontalTextAlignment.CENTER,
         Accent.getAccentColorWithAlpha((int)(235.0F * alpha))
      );
      this.popTransformMatrix(drawContext);
   }

   private void drawListPanel(DrawContext drawContext, float left, float top, float blurRadius, float opacity, long timeMillis) {
      this.drawInput(drawContext, this.searchInput, left, top, SIDE_PANEL_WIDTH, 15.0F, timeMillis, opacity);
      this.drawCenteredText(drawContext, "M", left + SIDE_PANEL_WIDTH - 7.0F, top + 7.5F, 4.6F, Accent.getDeepTextColor((int)(160.0F * opacity)));
      this.drawGlassPanel(drawContext, left + SIDE_PANEL_WIDTH + 4.0F, top, LIST_WIDTH - SIDE_PANEL_WIDTH - 4.0F, 15.0F, blurRadius, opacity, 0.0F);
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         this.getCountText(),
         left + LIST_WIDTH - 6.0F,
         top + 7.5F,
         4.6F,
         HorizontalTextAlignment.RIGHT,
         Accent.getDeepTextColor((int)(140.0F * opacity))
      );
      float listTop = top + 18.0F;
      this.drawGlassPanel(drawContext, left, listTop, LIST_WIDTH, LIST_HEIGHT, blurRadius, opacity, 0.0F);
      int count = this.getEntryCount();
      if (count <= 0) {
         this.drawText(
            drawContext,
            Medium.DEFAULT_FONT,
            this.getEmptyText(),
            left + LIST_WIDTH * 0.5F,
            listTop + LIST_HEIGHT * 0.5F,
            4.8F,
            HorizontalTextAlignment.CENTER,
            Accent.getDeepTextColor((int)(120.0F * opacity))
         );
         return;
      }

      float cardLeft = left + 6.0F;
      float cardWidth = LIST_WIDTH - 12.0F;
      drawContext.enableScissor(
         PixelRounding.II1Il11l111II11IIl(left),
         PixelRounding.II1Il11l111II11IIl(listTop + 1.0F),
         PixelRounding.l1I1IlllIlI(left + LIST_WIDTH),
         PixelRounding.l1I1IlllIlI(listTop + LIST_HEIGHT - 1.0F)
      );

      try {
         for (int index = 0; index < count; index++) {
            float cardTop = listTop + 6.0F + index * ROW_PITCH - this.scrollOffset;
            if (cardTop + ROW_HEIGHT < listTop || cardTop > listTop + LIST_HEIGHT) {
               continue;
            }

            this.drawCard(drawContext, index, cardLeft, cardTop, cardWidth, blurRadius, opacity, listTop);
         }
      } finally {
         drawContext.disableScissor();
      }

      float maxScroll = this.getMaxScroll(count);
      if (maxScroll > 0.0F) {
         float trackHeight = LIST_HEIGHT - 8.0F;
         float thumbHeight = Math.max(12.0F, trackHeight * LIST_HEIGHT / (LIST_HEIGHT + maxScroll));
         float thumbTop = listTop + 4.0F + (trackHeight - thumbHeight) * (this.scrollOffset / maxScroll);
         RoundedGradientRectBuilder.at(left + LIST_WIDTH - 3.0F, thumbTop, 1.2F, thumbHeight)
            .cornerRadius(0.6F)
            .color(Accent.getAccentColorWithAlpha((int)(120.0F * opacity)))
            .render(drawContext);
      }
   }

   private void drawCard(
      DrawContext drawContext, int index, float cardLeft, float cardTop, float cardWidth, float blurRadius, float opacity, float listTop
   ) {
      boolean visible = this.mouseY >= listTop && this.mouseY <= listTop + LIST_HEIGHT;
      String key = "row:" + index;
      float hover = this.getHoverProgress(key, visible && this.isMouseWithin(cardLeft, cardTop, cardWidth, ROW_HEIGHT));
      float selected = this.getHoverProgress(key + ":sel", index == this.selectedIndex);
      this.pushPivotScale(drawContext, cardLeft + cardWidth * 0.5F, cardTop + ROW_HEIGHT * 0.5F, 1.0F + 0.02F * hover);
      this.drawGlassPanel(drawContext, cardLeft, cardTop, cardWidth, ROW_HEIGHT, blurRadius, opacity, Math.max(hover, selected * 0.6F));
      if (selected > 0.01F) {
         RoundedGradientRectBuilder.at(cardLeft, cardTop, cardWidth, ROW_HEIGHT)
            .cornerRadius(blurRadius)
            .color(Accent.getAccentColorWithAlpha((int)(22.0F * selected * opacity)))
            .render(drawContext);
         RoundedGradientRectBuilder.at(cardLeft + 1.5F, cardTop + ROW_HEIGHT * 0.5F - 6.0F * selected, 1.2F, 12.0F * selected)
            .cornerRadius(0.6F)
            .color(Accent.getAccentColorWithAlpha((int)(255.0F * selected * opacity)))
            .render(drawContext);
      }

      this.drawEntry(drawContext, index, cardLeft, cardTop, cardWidth, opacity);
      this.popTransformMatrix(drawContext);
   }

   // ---------- ввод ----------

   private SearchTextField getFocusedInput() {
      for (InputHit hit : this.inputHits) {
         if (hit.field.isFocused()) {
            return hit.field;
         }
      }

      return null;
   }

   private void afterInputChange() {
      String text = this.searchInput.getText();
      if (!text.equals(this.lastFilterText)) {
         this.lastFilterText = text;
         this.targetScroll = 0.0F;
         this.onFilterChanged(text.trim().toLowerCase(Locale.ROOT));
         this.clampSelection();
      }
   }

   @Override
   protected boolean handleContentClick(float mouseX, float mouseY, int mouseButton) {
      for (InputHit hit : this.inputHits) {
         hit.field.setFocused(mouseX >= hit.left && mouseX <= hit.left + hit.width && mouseY >= hit.top && mouseY <= hit.top + hit.height);
      }

      if (mouseButton != 0) {
         return true;
      }

      for (ButtonHit hit : this.buttonHits) {
         if (mouseX >= hit.left && mouseX <= hit.left + hit.width && mouseY >= hit.top && mouseY <= hit.top + hit.height) {
            this.markTextChanged(hit.id);
            this.onAction(hit.id);
            return true;
         }
      }

      float listLeft = this.getListLeft();
      float listTop = this.getListTop();
      float cardLeft = listLeft + 6.0F;
      float cardWidth = LIST_WIDTH - 12.0F;
      if (mouseX >= cardLeft && mouseX <= cardLeft + cardWidth && mouseY >= listTop && mouseY <= listTop + LIST_HEIGHT) {
         float relativeY = mouseY - listTop - 6.0F + this.scrollOffset;
         int index = (int)Math.floor(relativeY / ROW_PITCH);
         if (relativeY >= 0.0F && index >= 0 && index < this.getEntryCount() && relativeY - index * ROW_PITCH <= ROW_HEIGHT) {
            long now = System.currentTimeMillis();
            boolean doubleClick = index == this.lastClickedIndex && now - this.lastClickMillis <= DOUBLE_CLICK_MILLIS;
            this.lastClickedIndex = index;
            this.lastClickMillis = now;
            this.selectIndex(index);
            if (doubleClick) {
               this.onActivate(index);
            }

            return true;
         }
      }

      return false;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalScrollAmount, double verticalScrollAmount) {
      this.targetScroll -= (float)verticalScrollAmount * 20.0F;
      return true;
   }

   @Override
   public boolean keyPressed(KeyInput keyInput) {
      int keyCode = keyInput.getKeycode();
      SearchTextField focused = this.getFocusedInput();
      if (focused != null) {
         if (keyCode == 256) {
            focused.setFocused(false);
            return true;
         }

         boolean handled = focused.keyPressed(keyInput);
         this.afterInputChange();
         return handled;
      }

      if (keyCode == 256) {
         this.close();
         return true;
      }

      int count = this.getEntryCount();
      if (keyCode == 264 && count > 0) {
         this.selectIndex(Math.min(count - 1, this.selectedIndex + 1));
         return true;
      }

      if (keyCode == 265 && count > 0) {
         this.selectIndex(Math.max(0, this.selectedIndex - 1));
         return true;
      }

      if ((keyCode == 257 || keyCode == 335) && this.selectedIndex >= 0 && this.selectedIndex < count) {
         this.onActivate(this.selectedIndex);
         return true;
      }

      if (keyCode == 294) {
         this.onAction("refresh");
         return true;
      }

      return super.keyPressed(keyInput);
   }

   @Override
   public boolean charTyped(CharInput charInput) {
      SearchTextField focused = this.getFocusedInput();
      if (focused != null) {
         boolean handled = focused.charTyped(charInput);
         this.afterInputChange();
         return handled;
      }

      return super.charTyped(charInput);
   }

   private record ButtonHit(String id, float left, float top, float width, float height) {
   }

   private record InputHit(SearchTextField field, float left, float top, float width, float height) {
   }
}
