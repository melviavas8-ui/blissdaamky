package dev.daamky;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class EmojiTextRenderer {
   public static final float EMOJI_RENDER_SCALE = 1.45F;

   private EmojiTextRenderer() {
   }

   private static List<EmojiTextRenderer.EmojiRun> createTextRuns(String inputText, float fontScale) {
      ArrayList textRuns = new ArrayList();
      StringBuilder textBuffer = new StringBuilder();
      int codePointLength = 0;

      while (codePointLength < inputText.length()) {
         int matchedEmojiLength = Emoji.matchEmojiLength(inputText, codePointLength);
         if (matchedEmojiLength > 0) {
            flushTextRun(textRuns, textBuffer, fontScale);
            Emoji.EmojiEntry emojiEntry = Emoji.findEmojiAt(inputText, codePointLength);
            textRuns.add(new EmojiTextRenderer.EmojiRun(null, emojiEntry, fontScale * 1.45F + 0.6F));
            codePointLength += matchedEmojiLength;
         } else {
            char character = inputText.charAt(codePointLength);
            if (character == '\n') {
               flushTextRun(textRuns, textBuffer, fontScale);
               textRuns.add(new EmojiTextRenderer.EmojiRun("\n", null, 0.0F));
            } else {
               if (character == '️' || character == 8205) {
                  codePointLength++;
                  continue;
               }

               textBuffer.append(character);
               if (character == ' ') {
                  flushTextRun(textRuns, textBuffer, fontScale);
               }
            }

            codePointLength++;
         }
      }

      flushTextRun(textRuns, textBuffer, fontScale);
      return textRuns;
   }

   private static void flushTextRun(List<EmojiTextRenderer.EmojiRun> items, StringBuilder stringBuilder, float fontScale) {
      if (!stringBuilder.isEmpty()) {
         String textRun = stringBuilder.toString();
         items.add(new EmojiTextRenderer.EmojiRun(textRun, null, Medium.DEFAULT_FONT.measureWidth(textRun, fontScale)));
         stringBuilder.setLength(0);
      }
   }

   public static float measureTextWidth(String inputText, float fontScale) {
      float totalWidth = 0.0F;

      for (Object textRun : createTextRuns(inputText, fontScale)) {
         totalWidth += ((EmojiTextRenderer.EmojiRun)textRun).width();
      }

      return totalWidth;
   }

   public static boolean isEmojiOnlyText(String inputText) {
      List emojiRuns = createTextRuns(inputText.strip(), 1.0F);
      if (!emojiRuns.isEmpty() && emojiRuns.size() <= 3) {
         for (Object emojiRun : emojiRuns) {
            if (((EmojiTextRenderer.EmojiRun)emojiRun).emoji() == null) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static List<EmojiTextRenderer.EmojiLineLayout> layoutLines(String inputText, float fontScale, float maximumLineWidth) {
      ArrayList lines = new ArrayList();
      ArrayList currentLineRuns = new ArrayList();
      float currentLineWidth = 0.0F;

      for (Object textRun : createTextRuns(inputText, fontScale)) {
         if ("\n".equals(((EmojiTextRenderer.EmojiRun)textRun).text())) {
            lines.add(new EmojiTextRenderer.EmojiLineLayout(currentLineRuns, currentLineWidth));
            currentLineRuns = new ArrayList();
            currentLineWidth = 0.0F;
         } else {
            if (currentLineWidth + ((EmojiTextRenderer.EmojiRun)textRun).width() > maximumLineWidth && !currentLineRuns.isEmpty()) {
               lines.add(new EmojiTextRenderer.EmojiLineLayout(currentLineRuns, currentLineWidth));
               currentLineRuns = new ArrayList();
               currentLineWidth = 0.0F;
               if (((EmojiTextRenderer.EmojiRun)textRun).text() != null && ((EmojiTextRenderer.EmojiRun)textRun).text().equals(" ")
                  )
                {
                  continue;
               }
            }

            if (((EmojiTextRenderer.EmojiRun)textRun).text() != null && ((EmojiTextRenderer.EmojiRun)textRun).width() > maximumLineWidth) {
               String remainingText = ((EmojiTextRenderer.EmojiRun)textRun).text();

               while (!remainingText.isEmpty()) {
                  int maximumFragmentLength = 1;

                  while (maximumFragmentLength < remainingText.length() && currentLineWidth + Medium.DEFAULT_FONT.measureWidth(remainingText.substring(0, maximumFragmentLength + 1), fontScale) <= maximumLineWidth) {
                     maximumFragmentLength++;
                  }

                  String lineFragment = remainingText.substring(0, maximumFragmentLength);
                  float fragmentWidth = Medium.DEFAULT_FONT.measureWidth(lineFragment, fontScale);
                  currentLineRuns.add(new EmojiTextRenderer.EmojiRun(lineFragment, null, fragmentWidth));
                  currentLineWidth += fragmentWidth;
                  remainingText = remainingText.substring(maximumFragmentLength);
                  if (!remainingText.isEmpty()) {
                     lines.add(new EmojiTextRenderer.EmojiLineLayout(currentLineRuns, currentLineWidth));
                     currentLineRuns = new ArrayList();
                     currentLineWidth = 0.0F;
                  }
               }
            } else {
               currentLineRuns.add(textRun);
               currentLineWidth += ((EmojiTextRenderer.EmojiRun)textRun).width();
            }
         }
      }

      lines.add(new EmojiTextRenderer.EmojiLineLayout(currentLineRuns, currentLineWidth));
      return lines;
   }

   public static void renderLine(
      DrawContext drawContext, EmojiTextRenderer.EmojiLineLayout lineLayout, float startX, float baselineY, float fontScale, int textColor, float opacity
   ) {
      float cursorX = startX;

      for (Object run : lineLayout.runs()) {
         if (((EmojiTextRenderer.EmojiRun)run).emoji() != null) {
            renderEmoji(drawContext, ((EmojiTextRenderer.EmojiRun)run).emoji(), cursorX + 0.3F, baselineY, fontScale * 1.45F, opacity);
         } else if (!((EmojiTextRenderer.EmojiRun)run).text().isBlank()) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, ((EmojiTextRenderer.EmojiRun)run).text(), cursorX, baselineY, fontScale)
               .baselinePosition(TextBaselinePosition.CENTER)
               .color(textColor)
               .render(drawContext);
         }

         cursorX += ((EmojiTextRenderer.EmojiRun)run).width();
      }
   }

   public static void renderEmoji(DrawContext drawContext, Emoji.EmojiEntry emojiEntry, float x, float y, float size, float opacity) {
      Identifier identifier = Emoji.getEmojiTextureIdentifier(emojiEntry);
      if (identifier == null) {
         RoundedGradientRectBuilder.at(x + size * 0.15F, y - size * 0.35F, size * 0.7F, size * 0.7F)
            .cornerRadius(size * 0.35F)
            .color(587202559)
            .opacity(opacity)
            .render(drawContext);
      } else {
         TextureQuadBuilder.at(identifier, x, y - size * 0.5F, size, size).opacity(opacity).render(drawContext);
      }
   }

   public static String truncateText(String inputText, float fontScale, float maximumWidth) {
      if (measureTextWidth(inputText, fontScale) <= maximumWidth) {
         return inputText;
      }

      String truncatedText = inputText;

      while (!truncatedText.isEmpty() && measureTextWidth(truncatedText + "...", fontScale) > maximumWidth) {
         truncatedText = truncatedText.substring(0, truncatedText.offsetByCodePoints(truncatedText.length(), -1));
      }

      return truncatedText + "...";
   }

   public record EmojiLineLayout(List<EmojiTextRenderer.EmojiRun> runs, float width) {

      public EmojiLineLayout(List<EmojiTextRenderer.EmojiRun> runs, float width) {
         this.runs = runs;
         this.width = width;
      }
   }

   public record EmojiRun(String text, Emoji.EmojiEntry emoji, float width) {

      public EmojiRun(String text, Emoji.EmojiEntry emoji, float width) {
         this.text = text;
         this.emoji = emoji;
         this.width = width;
      }
   }
}

