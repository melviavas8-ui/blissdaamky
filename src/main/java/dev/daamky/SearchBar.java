package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

public final class SearchBar {
   private float searchBarX = Float.MAX_VALUE;
   private static final float SEARCH_BAR_HEIGHT = 11.5F;
   private static final float SEARCH_BAR_WIDTH = 80.0F;
   private static final float SEARCH_PANEL_WIDTH = 80.0F;
   private static final float CORNER_RADIUS = 3.0F;
   private static final float TEXT_FONT_SIZE = 5.6F;
   private static final float TEXT_START_X_OFFSET = 13.8F;
   private String searchText = "";
   private boolean enabled = false;
   private int caretIndex = 0;
   private int visibleTextStartIndex = 0;
   private long lastInputMillis = 0L;
   private float visibilityProgress = 0.0F;
   private float searchHoverProgress = 0.0F;
   private float clearButtonHoverProgress = 0.0F;
   private float inputWidgetX;
   private float searchBarY;
   private float searchBarWidth;
   private float clearButtonX;
   private float clearButtonY;
   private float clearButtonSize;
   private float frameDeltaSeconds = 0.016F;

   public float getSearchBarX() {
      return this.searchBarX;
   }

   public void updateVisibilityAnimation(float frameDeltaSeconds) {
      this.frameDeltaSeconds = frameDeltaSeconds;
      float targetVisibilityProgress = this.enabled ? 1.0F : 0.0F;
      this.visibilityProgress = this.visibilityProgress
         + (targetVisibilityProgress - this.visibilityProgress) * (1.0F - (float)Math.exp(-5.5F * frameDeltaSeconds));
      if (this.enabled && this.visibilityProgress > 0.999F) {
         this.visibilityProgress = 1.0F;
      } else if (!this.enabled && this.visibilityProgress < 0.001F) {
         this.visibilityProgress = 0.0F;
      }
   }

   public void renderSearchBar(
      DrawContext drawContext, float mouseX, float mouseY, float panelX, float panelY, float panelWidth, float panelHeight, float opacity
   ) {
      if (!(opacity <= 0.0F)) {
         float searchBarWidth = 80.0F + 0.0F * this.visibilityProgress;
         float searchBarX = panelX + panelWidth - searchBarWidth - 6.0F;
         this.searchBarX = searchBarX;
         float searchBarY = panelY + 4.5F;
         float searchBarHeight = 11.5F;
         this.inputWidgetX = searchBarX;
         this.searchBarY = searchBarY;
         this.searchBarWidth = searchBarWidth;
         boolean isMouseOverSearchBar = mouseX >= searchBarX && mouseX <= searchBarX + searchBarWidth && mouseY >= searchBarY && mouseY <= searchBarY + searchBarHeight;
         float targetHoverProgress = isMouseOverSearchBar ? 1.0F : 0.0F;
         this.searchHoverProgress = this.searchHoverProgress + (targetHoverProgress - this.searchHoverProgress) * (1.0F - (float)Math.exp(-5.5F * this.frameDeltaSeconds));
         int backgroundAlpha = (int)((58.0F + 22.0F * this.searchHoverProgress + 34.0F * this.visibilityProgress) * opacity);
         RoundedGradientRectBuilder.at(searchBarX, searchBarY, searchBarWidth, searchBarHeight)
            .cornerRadius(3.0F)
            .color(RgbaColor.rgba(0, 0, 0, backgroundAlpha))
            .render(drawContext);
         int outlineAlpha = (int)((7.0F + 6.0F * this.searchHoverProgress + 11.0F * this.visibilityProgress) * opacity);
         RoundedGradientRectBuilder.at(searchBarX, searchBarY, searchBarWidth, searchBarHeight)
            .cornerRadius(3.0F)
            .color(RgbaColor.rgba(255, 255, 255, outlineAlpha))
            .render(drawContext);
         float logoFontSize = 5.8F;
         float logoX = searchBarX + 6.5F;
         float logoY = searchBarY + searchBarHeight * 0.5F;
         int logoRed = (int)(168.0F + 87.0F * this.visibilityProgress);
         int logoGreen = (int)(173.0F + 12.0F * this.visibilityProgress);
         int logoBlue = (int)(190.0F + 35.0F * this.visibilityProgress);
         int logoAlpha = (int)((205.0F + 50.0F * this.visibilityProgress) * opacity);
         GuiTextBuilder.createAt(Medium.ICON_FONT, "M", logoX, logoY, logoFontSize)
            .centered()
            .color(RgbaColor.rgba(logoRed, logoGreen, logoBlue, logoAlpha))
            .render(drawContext);
         float textX = searchBarX + 13.8F;
         float textY = searchBarY + searchBarHeight * 0.5F;
         float textFontSize = 5.6F;
         float trailingControlPadding = this.searchText.isEmpty() ? 24.0F : 14.0F;
         float visibleTextWidth = searchBarWidth - 13.8F - trailingControlPadding;
         if (this.searchText.isEmpty()) {
            int placeholderAlpha = (int)((165.0F - 35.0F * this.visibilityProgress) * opacity);
            String placeholderText = this.enabled ? "" : "Search...";
            if (!placeholderText.isEmpty()) {
               GuiTextBuilder.createAt(Medium.DEFAULT_FONT, placeholderText, textX, textY, textFontSize)
                  .baselinePosition(TextBaselinePosition.CENTER)
                  .color(RgbaColor.rgba(156, 161, 176, placeholderAlpha))
                  .render(drawContext);
            }

            this.renderCaret(drawContext, textX, textY, Accent.getAccentColorWithAlpha(255).toArgb(), opacity);
            float hintOpacity = opacity * (1.0F - this.visibilityProgress);
            float hintWidth = 17.5F;
            float hintHeight = 7.2F;
            float hintX = searchBarX + searchBarWidth - hintWidth - 2.8F;
            float hintY = searchBarY + (searchBarHeight - hintHeight) * 0.5F;
            if (hintOpacity > 0.01F) {
               RoundedGradientRectBuilder.at(hintX, hintY, hintWidth, hintHeight)
                  .cornerRadius(2.0F)
                  .color(RgbaColor.rgba(255, 255, 255, (int)(12.0F * hintOpacity)))
                  .render(drawContext);
               GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Ctrl F", hintX + hintWidth * 0.5F, hintY + hintHeight * 0.5F, 4.0F)
                  .centered()
                  .color(RgbaColor.rgba(162, 167, 182, (int)(205.0F * hintOpacity)))
                  .render(drawContext);
            }
         } else {
            this.ensureCaretVisible(visibleTextWidth);
            String visibleSearchText = this.searchText.substring(this.visibleTextStartIndex);

            while (Medium.DEFAULT_FONT.measureWidth(visibleSearchText, textFontSize) > visibleTextWidth && visibleSearchText.length() > 1) {
               visibleSearchText = visibleSearchText.substring(0, visibleSearchText.length() - 1);
            }

            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, visibleSearchText, textX, textY, textFontSize)
               .baselinePosition(TextBaselinePosition.CENTER)
               .color(RgbaColor.rgba(246, 248, 253, (int)(255.0F * opacity)))
               .render(drawContext);
            float caretOffsetX = Medium.DEFAULT_FONT.measureWidth(this.searchText.substring(this.visibleTextStartIndex, this.caretIndex), textFontSize);
            this.renderCaret(drawContext, textX + caretOffsetX, textY, Accent.getLighterAccentColor(255).toArgb(), opacity);
            float clearButtonSize = 8.5F;
            float clearButtonX = searchBarX + searchBarWidth - clearButtonSize - 3.0F;
            float clearButtonY = searchBarY + (searchBarHeight - clearButtonSize) * 0.5F;
            this.clearButtonX = clearButtonX;
            this.clearButtonY = clearButtonY;
            this.clearButtonSize = clearButtonSize;
            boolean isMouseOverClearButton = mouseX >= clearButtonX && mouseX <= clearButtonX + clearButtonSize && mouseY >= clearButtonY && mouseY <= clearButtonY + clearButtonSize;
            float targetClearButtonHoverProgress = isMouseOverClearButton ? 1.0F : 0.0F;
            this.clearButtonHoverProgress = this.clearButtonHoverProgress + (targetClearButtonHoverProgress - this.clearButtonHoverProgress) * (1.0F - (float)Math.exp(-5.5F * this.frameDeltaSeconds));
            RoundedGradientRectBuilder.at(clearButtonX, clearButtonY, clearButtonSize, clearButtonSize)
               .cornerRadius(2.0F)
               .color(RgbaColor.rgba(255, 255, 255, (int)((12.0F + 25.0F * this.clearButtonHoverProgress) * opacity)))
               .render(drawContext);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "x", clearButtonX + clearButtonSize * 0.5F, clearButtonY + clearButtonSize * 0.5F - 0.5F, 6.0F)
               .centered()
               .color(RgbaColor.rgba(180, 185, 200, (int)((200.0F + 55.0F * this.clearButtonHoverProgress) * opacity)))
               .render(drawContext);
         }
      }
   }

   private void renderCaret(DrawContext drawContext, float caretX, float caretY, int caretColor, float opacity) {
      if (!(this.visibilityProgress <= 0.01F)) {
         long elapsedSinceLastInputMillis = System.currentTimeMillis() - this.lastInputMillis;
         float blinkOpacity = elapsedSinceLastInputMillis < 450L ? 1.0F : (float)(0.5 + 0.5 * Math.cos(System.currentTimeMillis() % 1100L / 1100.0 * Math.PI * 2.0));
         float caretHeight = 5.8799996F;
         int caretAlpha = (int)(235.0F * blinkOpacity * this.visibilityProgress * opacity);
         if (caretAlpha > 1) {
            RoundedGradientRectBuilder.at(caretX - 0.35F, caretY - caretHeight * 0.5F, 0.7F, caretHeight)
               .cornerRadius(0.35F)
               .color(RgbaColor.fromArgb(caretColor).withAlpha(caretAlpha))
               .render(drawContext);
         }
      }
   }

   private void ensureCaretVisible(float availableTextWidth) {
      this.caretIndex = Math.clamp(this.caretIndex, 0, this.searchText.length());
      if (this.visibleTextStartIndex > this.caretIndex) {
         this.visibleTextStartIndex = this.caretIndex;
      }

      while (
         this.visibleTextStartIndex < this.caretIndex
            && Medium.DEFAULT_FONT.measureWidth(this.searchText.substring(this.visibleTextStartIndex, this.caretIndex), 5.6F) > availableTextWidth
      ) {
         this.visibleTextStartIndex++;
      }

      while (
         this.visibleTextStartIndex > 0
            && Medium.DEFAULT_FONT.measureWidth(this.searchText.substring(this.visibleTextStartIndex - 1), 5.6F) <= availableTextWidth
      ) {
         this.visibleTextStartIndex--;
      }
   }

   private int findWordStartBeforeCaret(int caretIndex) {
      int wordStartIndex = Math.clamp(caretIndex, 0, this.searchText.length());

      while (wordStartIndex > 0 && this.searchText.charAt(wordStartIndex - 1) == ' ') {
         wordStartIndex--;
      }

      while (wordStartIndex > 0 && this.searchText.charAt(wordStartIndex - 1) != ' ') {
         wordStartIndex--;
      }

      return wordStartIndex;
   }

   private int findWordEndAfterCaret(int caretIndex) {
      int scanIndex = Math.clamp(caretIndex, 0, this.searchText.length());
      int textLength = this.searchText.length();

      while (scanIndex < textLength && this.searchText.charAt(scanIndex) == ' ') {
         scanIndex++;
      }

      while (scanIndex < textLength && this.searchText.charAt(scanIndex) != ' ') {
         scanIndex++;
      }

      return scanIndex;
   }

   private void replaceTextRange(int startIndex, int endIndex, String replacementText) {
      int rangeStart = Math.clamp(Math.min(startIndex, endIndex), 0, this.searchText.length());
      int rangeEnd = Math.clamp(Math.max(startIndex, endIndex), 0, this.searchText.length());
      this.searchText = this.searchText.substring(0, rangeStart) + replacementText + this.searchText.substring(rangeEnd);
      this.caretIndex = rangeStart + replacementText.length();
      this.lastInputMillis = System.currentTimeMillis();
   }

   private void moveCaretFromMouse(float mouseX) {
      this.visibleTextStartIndex = Math.clamp(this.visibleTextStartIndex, 0, this.searchText.length());
      float mouseXFromTextStart = mouseX - (this.inputWidgetX + 13.8F);
      int nearestCaretIndex = this.visibleTextStartIndex;
      float nearestDistance = Float.MAX_VALUE;

      for (int candidateCaretIndex = this.visibleTextStartIndex; candidateCaretIndex <= this.searchText.length(); candidateCaretIndex++) {
         float candidateDistance = Math.abs(Medium.DEFAULT_FONT.measureWidth(this.searchText.substring(this.visibleTextStartIndex, candidateCaretIndex), 5.6F) - mouseXFromTextStart);
         if (candidateDistance < nearestDistance) {
            nearestDistance = candidateDistance;
            nearestCaretIndex = candidateCaretIndex;
         }
      }

      this.caretIndex = nearestCaretIndex;
      this.lastInputMillis = System.currentTimeMillis();
   }

   public boolean onMouseClick(float mouseX, float mouseY, int button) {
      if (button == 0) {
         if (!this.searchText.isEmpty()
            && mouseX >= this.clearButtonX
            && mouseX <= this.clearButtonX + this.clearButtonSize
            && mouseY >= this.clearButtonY
            && mouseY <= this.clearButtonY + this.clearButtonSize) {
            this.searchText = "";
            this.caretIndex = 0;
            this.visibleTextStartIndex = 0;
            this.lastInputMillis = System.currentTimeMillis();
            return true;
         }

         if (mouseX >= this.inputWidgetX
            && mouseX <= this.inputWidgetX + this.searchBarWidth
            && mouseY >= this.searchBarY
            && mouseY <= this.searchBarY + 11.5F) {
            this.updateEnabledState(true);
            this.moveCaretFromMouse(mouseX);
            return true;
         }

         this.updateEnabledState(false);
      }

      return false;
   }

   public boolean onKeyInput(KeyInput keyInput) {
      if (keyInput.hasCtrl() && keyInput.getKeycode() == 70) {
         this.updateEnabledState(true);
         return true;
      }

      if (!this.enabled) {
         return false;
      }

      if (keyInput.getKeycode() == 256) {
         this.updateEnabledState(false);
         return false;
      }

      if (keyInput.getKeycode() == 257 || keyInput.getKeycode() == 335) {
         this.updateEnabledState(false);
         return true;
      }

      if (keyInput.getKeycode() == 263) {
         this.caretIndex = keyInput.hasCtrl() ? this.findWordStartBeforeCaret(this.caretIndex) : Math.max(0, this.caretIndex - 1);
         this.lastInputMillis = System.currentTimeMillis();
         return true;
      }

      if (keyInput.getKeycode() == 262) {
         this.caretIndex = keyInput.hasCtrl() ? this.findWordEndAfterCaret(this.caretIndex) : Math.min(this.searchText.length(), this.caretIndex + 1);
         this.lastInputMillis = System.currentTimeMillis();
         return true;
      }

      if (keyInput.getKeycode() == 268) {
         this.caretIndex = 0;
         this.lastInputMillis = System.currentTimeMillis();
         return true;
      }

      if (keyInput.getKeycode() == 269) {
         this.caretIndex = this.searchText.length();
         this.lastInputMillis = System.currentTimeMillis();
         return true;
      }

      if (keyInput.getKeycode() == 259) {
         if (this.caretIndex > 0) {
            this.replaceTextRange(keyInput.hasCtrl() ? this.findWordStartBeforeCaret(this.caretIndex) : this.caretIndex - 1, this.caretIndex, "");
         }

         return true;
      } else if (keyInput.getKeycode() == 261) {
         if (this.caretIndex < this.searchText.length()) {
            this.replaceTextRange(this.caretIndex, keyInput.hasCtrl() ? this.findWordEndAfterCaret(this.caretIndex) : this.caretIndex + 1, "");
         }

         return true;
      } else {
         if (keyInput.hasCtrl() && keyInput.getKeycode() == 65) {
            this.caretIndex = this.searchText.length();
            this.lastInputMillis = System.currentTimeMillis();
            return true;
         }

         if (keyInput.hasCtrl() && keyInput.getKeycode() == 86) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.keyboard != null) {
               String clipboardText = client.keyboard.getClipboard();
               if (clipboardText != null && !clipboardText.isEmpty()) {
                  StringBuilder stringBuilder = new StringBuilder();

                  for (char character : clipboardText.toCharArray()) {
                     if (character >= ' ' && character != 127) {
                        stringBuilder.append(character);
                     }
                  }

                  if (stringBuilder.length() > 0) {
                     this.replaceTextRange(this.caretIndex, this.caretIndex, stringBuilder.toString());
                  }
               }
            }

            return true;
         } else {
            return true;
         }
      }
   }

   public boolean onCharacterInput(CharInput charInput) {
      if (!this.enabled) {
         return false;
      } else {
         int codePoint = charInput.codepoint();
         if (codePoint >= 32 && codePoint != 127) {
            this.replaceTextRange(this.caretIndex, this.caretIndex, charInput.asString());
            return true;
         } else {
            return false;
         }
      }
   }

   public String getSearchText() {
      return this.searchText;
   }

   public void setSearchText(String searchText) {
      this.searchText = searchText == null ? "" : searchText;
      this.caretIndex = this.searchText.length();
      this.visibleTextStartIndex = 0;
      this.lastInputMillis = System.currentTimeMillis();
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.updateEnabledState(enabled);
   }

   private void updateEnabledState(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         if (enabled) {
            this.caretIndex = this.searchText.length();
            this.lastInputMillis = System.currentTimeMillis();
         }
      }
   }
}

