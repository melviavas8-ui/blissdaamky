package dev.daamky;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;

public final class Scoreboard extends HudElementPosition {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final Comparator<ScoreboardEntry> SCORE_ENTRY_ORDER = Comparator.comparing(ScoreboardEntry::value)
      .reversed()
      .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);
   private static final float HORIZONTAL_INNER_PADDING = 7.0F;
   private static final float PANEL_TOP_PADDING = 5.0F;
   private static final float ENTRY_LINE_HEIGHT = 8.0F;
   private static final float ENTRY_SECTION_PADDING = 4.0F;
   private static final float CUSTOM_FONT_SIZE = 5.7F;
   private static final float VANILLA_FONT_HORIZONTAL_SCALE = 1.25F;
   private static final float VANILLA_FONT_SCALE = 0.5181818F;
   private List<Scoreboard.RenderedScoreboardEntry> headerEntries = List.of();
   private float headerWidth;
   private final List<Scoreboard.ScoreboardLayout> scoreboardRows = new ArrayList<>();
   private float panelWidth = 120.0F;
   private float panelHeight = 30.0F;
// https://t.me/pasterplace

   public Scoreboard() {
      super("Scoreboard", "scoreboard", 9999.0F, 9999.0F);
      this.elementWidth = this.panelWidth;
      this.elementHeight = this.panelHeight;
   }
// https://t.me/pasterplace

   @Override
   public void clampPositionToScreen(float screenWidth, float screenHeight) {
      this.setPosition(screenWidth - this.elementWidth - 6.0F, screenHeight * 0.5F - this.elementHeight * 0.5F);
   }
// https://t.me/pasterplace

   public static ScoreboardObjective getDisplayedObjective() {
      if (CLIENT.world != null && CLIENT.player != null) {
         net.minecraft.scoreboard.Scoreboard scoreboard = CLIENT.world.getScoreboard();
         Team team = scoreboard.getScoreHolderTeam(CLIENT.player.getNameForScoreboard());
         ScoreboardDisplaySlot displaySlot = team != null ? ScoreboardDisplaySlot.fromFormatting(team.getColor()) : null;
         if (displaySlot != null) {
            ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(displaySlot);
            if (scoreboardObjective != null) {
               return scoreboardObjective;
            }
         }

         return scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
      } else {
         return null;
      }
   }
// https://t.me/pasterplace

   @Override
   public void updateAnimation(float tickDelta) {
      this.scoreboardRows.clear();
      ScoreboardObjective scoreboardObjective = getDisplayedObjective();
      if (scoreboardObjective == null) {
         this.headerEntries = List.of();
         this.headerWidth = 0.0F;
      } else {
         net.minecraft.scoreboard.Scoreboard scoreboard = scoreboardObjective.getScoreboard();
         NumberFormat numberFormat = scoreboardObjective.getNumberFormatOr(StyledNumberFormat.RED);
         this.headerEntries = toRenderedEntries(scoreboardObjective.getDisplayName());
         this.headerWidth = measureRenderedEntries(this.headerEntries);
         scoreboard.getScoreboardEntries(scoreboardObjective)
            .stream()
            .filter(scoreboardEntry -> !scoreboardEntry.hidden())
            .sorted(SCORE_ENTRY_ORDER)
            .limit(15L)
            .forEach(scoreboardEntry -> {
               Team team = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
               List nameEntries = toRenderedEntries(Team.decorateName(team, scoreboardEntry.name()));
               List scoreEntries = toRenderedEntries(scoreboardEntry.formatted(numberFormat));
               this.scoreboardRows.add(new Scoreboard.ScoreboardLayout(nameEntries, scoreEntries, measureRenderedEntries(nameEntries), measureRenderedEntries(scoreEntries)));
            });
      }

      float contentWidth = this.headerWidth;
// https://t.me/pasterplace

      for (Object scoreboardRow : this.scoreboardRows) {
         contentWidth = Math.max(
            contentWidth,
            ((Scoreboard.ScoreboardLayout)scoreboardRow).nameWidth()
               + (((Scoreboard.ScoreboardLayout)scoreboardRow).scoreWidth() > 0.0F ? 12.0F + ((Scoreboard.ScoreboardLayout)scoreboardRow).scoreWidth() : 0.0F)
         );
      }
// https://t.me/pasterplace

      float targetPanelWidth = 14.0F + Math.max(80.0F, contentWidth);
      float targetPanelHeight = 18.0F + (this.scoreboardRows.isEmpty() ? 0.0F : 4.0F + this.scoreboardRows.size() * 8.0F);
      this.panelWidth = interpolate(this.panelWidth, targetPanelWidth, 12.0F, tickDelta);
      this.panelHeight = interpolate(this.panelHeight, targetPanelHeight, 12.0F, tickDelta);
      this.elementWidth = this.panelWidth;
      this.elementHeight = this.panelHeight;
   }
// https://t.me/pasterplace

   @Override
   public boolean isVisibleInCurrentScreen(boolean chatScreenOpen) {
      return chatScreenOpen || !this.headerEntries.isEmpty();
   }
// https://t.me/pasterplace

   @Override
   public void render(HudRenderContext renderContext, float visibilityAlpha) {
      DrawContext drawContext = renderContext.drawContext();
      float x = this.getX();
      float y = this.getY();
      BrokkeIcons.drawPanelBackground(drawContext, x, y, this.elementWidth, this.elementHeight, visibilityAlpha);
      float headerY = y + 5.0F + 4.0F;
      if (this.headerEntries.isEmpty()) {
         String emptyStateLabel = "Scoreboard";
         BrokkeIcons.drawCenteredText(
            drawContext, emptyStateLabel, x + (this.elementWidth - BrokkeIcons.measureTextWidth(emptyStateLabel, 5.7F)) * 0.5F, headerY, 5.7F, BrokkeIcons.lightGrayWithAlpha(visibilityAlpha)
         );
      } else {
         renderEntries(renderContext, this.headerEntries, x + (this.elementWidth - this.headerWidth) * 0.5F, headerY, visibilityAlpha);
         if (!this.scoreboardRows.isEmpty()) {
            float dividerY = y + 5.0F + 8.0F + 2.0F;
            BrokkeIcons.drawHorizontalDivider(drawContext, x + 7.0F, dividerY, this.elementWidth - 14.0F, visibilityAlpha);
            float entriesY = dividerY + 2.0F + 4.0F;
            float bottomY = y + this.elementHeight - 2.5F;

            for (Object scoreboardRow : this.scoreboardRows) {
               if (entriesY > bottomY) {
                  break;
               }

               renderEntries(renderContext, ((Scoreboard.ScoreboardLayout)scoreboardRow).nameLines(), x + 7.0F, entriesY, visibilityAlpha);
               if (((Scoreboard.ScoreboardLayout)scoreboardRow).scoreWidth() > 0.0F) {
                  renderEntries(
                     renderContext,
                     ((Scoreboard.ScoreboardLayout)scoreboardRow).scoreLines(),
                     x + this.elementWidth - 7.0F - ((Scoreboard.ScoreboardLayout)scoreboardRow).scoreWidth(),
                     entriesY,
                     visibilityAlpha
                  );
               }

               entriesY += 8.0F;
            }
         }
      }
   }
// https://t.me/pasterplace

   private static void renderEntries(HudRenderContext renderContext, List<Scoreboard.RenderedScoreboardEntry> renderedEntries, float x, float y, float alpha) {
      DrawContext drawContext = renderContext.drawContext();
      float cursorX = x;

      for (Object renderedEntry : renderedEntries) {
         if (((Scoreboard.RenderedScoreboardEntry)renderedEntry).useMinecraftFont()) {
            float nativeTextX = cursorX;
            MutableText mutableText = Text.literal(((Scoreboard.RenderedScoreboardEntry)renderedEntry).segmentText())
               .fillStyle(((Scoreboard.RenderedScoreboardEntry)renderedEntry).style());
            int argbColor = ColorHelper.withAlpha(Math.round(255.0F * alpha), ((Scoreboard.RenderedScoreboardEntry)renderedEntry).argbColor());
            renderContext.postRenderCallbacks().add(() -> {
               drawContext.getMatrices().pushMatrix();
               drawContext.getMatrices().translate(nativeTextX, y - 2.331818F);
               drawContext.getMatrices().scale(0.64772725F, 0.5181818F);
               drawContext.drawText(CLIENT.textRenderer, mutableText, 0, 0, argbColor, false);
               drawContext.getMatrices().popMatrix();
            });
         } else {
            drawContext.getMatrices().pushMatrix();
            drawContext.getMatrices().translate(cursorX, 0.0F);
            drawContext.getMatrices().scale(1.25F, 1.0F);
            BrokkeIcons.drawCenteredText(
               drawContext,
               ((Scoreboard.RenderedScoreboardEntry)renderedEntry).segmentText(),
               0.0F,
               y,
               5.7F,
               RgbaColor.fromArgb(((Scoreboard.RenderedScoreboardEntry)renderedEntry).argbColor()).multiplyAlpha(alpha)
            );
            drawContext.getMatrices().popMatrix();
         }

         cursorX += ((Scoreboard.RenderedScoreboardEntry)renderedEntry).width();
      }
   }
// https://t.me/pasterplace

   private static float measureRenderedEntries(List<Scoreboard.RenderedScoreboardEntry> renderedEntries) {
      float totalWidth = 0.0F;

      for (Object renderedEntry : renderedEntries) {
         totalWidth += ((Scoreboard.RenderedScoreboardEntry)renderedEntry).width();
      }

      return totalWidth;
   }
// https://t.me/pasterplace

   private static List<Scoreboard.RenderedScoreboardEntry> toRenderedEntries(Text sourceText) {
      ArrayList renderedEntries = new ArrayList();
      if (sourceText == null) {
         return renderedEntries;
      }

      int defaultArgbColor = BrokkeIcons.lightGrayWithAlpha(1.0F).toArgb();
      sourceText.visit((style, literalText) -> {
         int segmentArgbColor = style.getColor() != null ? 0xFF000000 | style.getColor().getRgb() : defaultArgbColor;
         appendFormattedText(renderedEntries, literalText, segmentArgbColor, defaultArgbColor, style);
         return Optional.empty();
      }, Style.EMPTY);
      return renderedEntries;
   }
// https://t.me/pasterplace

   private static void appendFormattedText(List<Scoreboard.RenderedScoreboardEntry> renderedEntries, String formattedText, int initialArgbColor, int defaultArgbColor, Style style) {
      StringBuilder segmentBuffer = new StringBuilder();
      int currentArgbColor = initialArgbColor;

      for (int characterOffset = 0; characterOffset < formattedText.length(); characterOffset++) {
         char formattingMarker = formattedText.charAt(characterOffset);
         if (formattingMarker == 167 && characterOffset + 1 < formattedText.length()) {
            Formatting formatting = Formatting.byCode(Character.toLowerCase(formattedText.charAt(characterOffset + 1)));
            characterOffset++;
            if (formatting != null) {
               TextColor textColor = TextColor.fromFormatting(formatting);
               if (textColor != null || formatting == Formatting.RESET) {
                  appendTextSegments(renderedEntries, segmentBuffer, currentArgbColor, style);
                  currentArgbColor = textColor == null ? defaultArgbColor : 0xFF000000 | textColor.getRgb();
               }
            }
         } else {
            segmentBuffer.append(formattingMarker);
         }
      }

      appendTextSegments(renderedEntries, segmentBuffer, currentArgbColor, style);
   }
// https://t.me/pasterplace

   private static void appendTextSegments(List<Scoreboard.RenderedScoreboardEntry> renderedEntries, StringBuilder textBuffer, int argbColor, Style style) {
      if (!textBuffer.isEmpty()) {
         String plainText = textBuffer.toString();
         textBuffer.setLength(0);
         Style coloredStyle = style.withColor(argbColor & 16777215);
         StringBuilder segmentBuffer = new StringBuilder();
         boolean previousUseMinecraftFont = false;
         int characterOffset = 0;

         while (characterOffset < plainText.length()) {
            int codePoint = plainText.codePointAt(characterOffset);
            boolean useMinecraftFont = !Character.isWhitespace(codePoint) && !Medium.DEFAULT_FONT.hasGlyph(codePoint);
            if (!segmentBuffer.isEmpty() && useMinecraftFont != previousUseMinecraftFont) {
               renderedEntries.add(createRenderedEntry(segmentBuffer.toString(), argbColor, coloredStyle, previousUseMinecraftFont));
               segmentBuffer.setLength(0);
            }

            previousUseMinecraftFont = useMinecraftFont;
            segmentBuffer.appendCodePoint(codePoint);
            characterOffset += Character.charCount(codePoint);
         }

         if (!segmentBuffer.isEmpty()) {
            renderedEntries.add(createRenderedEntry(segmentBuffer.toString(), argbColor, coloredStyle, previousUseMinecraftFont));
         }
      }
   }
// https://t.me/pasterplace

   private static Scoreboard.RenderedScoreboardEntry createRenderedEntry(String segmentText, int argbColor, Style style, boolean useMinecraftFont) {
      float renderedWidth = useMinecraftFont
         ? CLIENT.textRenderer.getWidth(Text.literal(segmentText).fillStyle(style)) * 0.5181818F
         : BrokkeIcons.measureTextWidth(segmentText, 5.7F);
      return new Scoreboard.RenderedScoreboardEntry(segmentText, argbColor, style, useMinecraftFont, renderedWidth * 1.25F);
   }
// https://t.me/pasterplace

   private record ScoreboardLayout(List<Scoreboard.RenderedScoreboardEntry> nameLines, List<Scoreboard.RenderedScoreboardEntry> scoreLines, float nameWidth, float scoreWidth) {

      private ScoreboardLayout(List<Scoreboard.RenderedScoreboardEntry> nameLines, List<Scoreboard.RenderedScoreboardEntry> scoreLines, float nameWidth, float scoreWidth) {
         this.nameLines = nameLines;
         this.scoreLines = scoreLines;
         this.nameWidth = nameWidth;
         this.scoreWidth = scoreWidth;
      }
   }
// https://t.me/pasterplace

   private record RenderedScoreboardEntry(String segmentText, int argbColor, Style style, boolean useMinecraftFont, float width) {

      private RenderedScoreboardEntry(String segmentText, int argbColor, Style style, boolean useMinecraftFont, float width) {
         this.segmentText = segmentText;
         this.argbColor = argbColor;
         this.style = style;
         this.useMinecraftFont = useMinecraftFont;
         this.width = width;
      }
   }
}

