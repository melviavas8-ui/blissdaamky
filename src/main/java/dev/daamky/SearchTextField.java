package dev.daamky;

import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

final class SearchTextField {
   private final String label;
   private final int maxLength;
   private final Consumer<String> onSubmit;
   private String text = "";
   private boolean focused;
   private float focusAnimation;

   SearchTextField(String label, int maximumLength, Consumer<String> submitHandler) {
      this.label = label;
      this.maxLength = maximumLength;
      this.onSubmit = submitHandler;
   }

   String getText() {
      return this.text;
   }

   void setText(String newText) {
      this.text = newText;
   }

   boolean isFocused() {
      return this.focused;
   }

   void setFocused(boolean focused) {
      this.focused = focused;
   }

   void render(DrawContext drawContext, float x, float y, float width, float height, long timeMillis, float opacity) {
      this.focusAnimation = this.focusAnimation + ((this.focused ? 1.0F : 0.0F) - this.focusAnimation) * 0.25F;
      float cornerRadius = Accent.getInstance().getCornerRadius() * 0.5F;
      RoundedGradientRectBuilder.at(x, y, width, height)
         .cornerRadius(cornerRadius)
         .color(RgbaColor.rgba(0, 0, 0, (int)(60.0F * opacity)))
         .render(drawContext);
      RoundedRectBuilder.createAt(x, y, width, height)
         .cornerRadius(cornerRadius)
         .borderWidth(0.6F)
         .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)((30.0F + 120.0F * this.focusAnimation) * opacity)))
         .alpha(opacity)
         .render(drawContext);
      float textX = x + 4.0F;
      float textMaxWidth = width - 8.0F;
      float textBaselineY = y + height * 0.5F;
      if (this.text.isEmpty() && !this.focused) {
         GuiTextBuilder.createAt(
               Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(this.label, 4.8F, textMaxWidth), textX, textBaselineY, 4.8F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(150.0F * opacity)))
            .render(drawContext);
      } else {
         String visibleText = this.text;

         while (!visibleText.isEmpty() && Medium.DEFAULT_FONT.measureWidth(visibleText, 4.8F) > textMaxWidth - 2.0F) {
            visibleText = visibleText.substring(1);
         }

         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(x),
            PixelRounding.II1Il11l111II11IIl(y),
            PixelRounding.l1I1IlllIlI(x + width),
            PixelRounding.l1I1IlllIlI(y + height)
         );

         try {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, visibleText, textX, textBaselineY, 4.8F)
               .baselinePosition(TextBaselinePosition.CENTER)
               .color(Accent.getTextColorWithAlpha((int)(245.0F * opacity)))
               .render(drawContext);
            if (this.focused && timeMillis / 500L % 2L == 0L) {
               float cursorX = textX + Medium.DEFAULT_FONT.measureWidth(visibleText, 4.8F) + 0.6F;
               RoundedGradientRectBuilder.at(cursorX, textBaselineY - 3.0F, 0.6F, 6.0F)
                  .cornerRadius(0.0F)
                  .color(Accent.getAccentColorWithAlpha((int)(255.0F * opacity)))
                  .render(drawContext);
            }
         } finally {
            drawContext.disableScissor();
         }
      }
   }

   boolean keyPressed(KeyInput keyInput) {
      int keyCode = keyInput.getKeycode();
      if (keyCode == 256) {
         this.focused = false;
         return true;
      }

      if (keyCode == 257 || keyCode == 335) {
         this.onSubmit.accept(this.text);
         return true;
      }

      if (keyCode == 259) {
         if (!this.text.isEmpty()) {
            this.text = keyInput.hasCtrl() ? "" : this.text.substring(0, this.text.offsetByCodePoints(this.text.length(), -1));
         }

         return true;
      } else if (keyInput.hasCtrl() && keyCode == 86) {
         String clipboardText = MinecraftClient.getInstance().keyboard.getClipboard();
         if (clipboardText != null) {
            this.appendText(clipboardText.replace("\n", " ").replace("\r", ""));
         }

         return true;
      } else {
         return true;
      }
   }

   boolean charTyped(CharInput charInput) {
      int codePoint = charInput.codepoint();
      if (codePoint >= 32 && codePoint != 127) {
         this.appendText(charInput.asString());
      }

      return true;
   }

   private void appendText(String appendedText) {
      String combinedText = this.text + appendedText;
      if (combinedText.length() > this.maxLength) {
         combinedText = combinedText.substring(0, this.maxLength);
      }

      this.text = combinedText;
   }
}

