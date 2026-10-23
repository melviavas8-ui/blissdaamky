package dev.daamky;

import net.minecraft.client.gui.DrawContext;
import org.jspecify.annotations.Nullable;

public final class GuiTextBuilder {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public static final float DEFAULT_FONT_SIZE = 9.0F;
   private MsdfFont font = Medium.getCurrentFont();
   private String text = "";
   private float x;
   private float y;
   private float fontSize = 9.0F;
   private float atlasScale;
   private HorizontalTextAlignment alignment = HorizontalTextAlignment.LEFT;
   private TextBaselinePosition baselinePosition = TextBaselinePosition.TOP;
   private int baseColor = -1;
   private @Nullable GlyphColorProvider colorProvider;
   private float alpha = 1.0F;
// https://t.me/pasterplace

   private GuiTextBuilder() {
   }
// https://t.me/pasterplace

   public static GuiTextBuilder create() {
      return new GuiTextBuilder();
   }
// https://t.me/pasterplace

   public static GuiTextBuilder create(String displayText) {
      return new GuiTextBuilder().text(displayText);
   }
// https://t.me/pasterplace

   public static GuiTextBuilder create(MsdfFont font, String displayText) {
      return new GuiTextBuilder().font(font).text(displayText);
   }
// https://t.me/pasterplace

   public static GuiTextBuilder createAt(MsdfFont font, String displayText, float x, float y, float fontSize) {
      return create(font, displayText).position(x, y).fontSize(fontSize);
   }
// https://t.me/pasterplace

   public GuiTextBuilder font(MsdfFont font) {
      this.font = font;
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder fontByName(String fontName) {
      return this.font(Medium.getFont(fontName));
   }
// https://t.me/pasterplace

   public GuiTextBuilder text(String displayText) {
      this.text = displayText == null ? "" : displayText;
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder fontSize(float fontSize) {
      this.fontSize = fontSize;
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder atlasScale(float atlasScale) {
      this.atlasScale = atlasScale;
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder alignment(HorizontalTextAlignment alignment) {
      this.alignment = alignment;
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder baselinePosition(TextBaselinePosition baselinePosition) {
      this.baselinePosition = baselinePosition;
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder centered() {
      return this.alignment(HorizontalTextAlignment.CENTER).baselinePosition(TextBaselinePosition.CENTER);
   }
// https://t.me/pasterplace

   public GuiTextBuilder centeredAt(float x, float y) {
      return this.position(x, y).centered();
   }
// https://t.me/pasterplace

   public GuiTextBuilder color(int argbColor) {
      this.baseColor = argbColor;
      this.colorProvider = null;
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder color(RgbaColor color) {
      return this.color(color.toArgb());
   }
// https://t.me/pasterplace

   public GuiTextBuilder verticalGradient(int startColor, int endColor) {
      this.colorProvider = (glyphTextIndex, textureU, textureV) -> ColorUtils.interpolateColor(startColor, endColor, textureV);
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder verticalGradient(RgbaColor startColor, RgbaColor endColor) {
      return this.verticalGradient(startColor.toArgb(), endColor.toArgb());
   }
// https://t.me/pasterplace

   public GuiTextBuilder horizontalGradient(int startColor, int endColor) {
      this.colorProvider = (glyphTextIndex, textureU, textureV) -> ColorUtils.interpolateColor(startColor, endColor, textureU);
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder horizontalGradient(RgbaColor startColor, RgbaColor endColor) {
      return this.horizontalGradient(startColor.toArgb(), endColor.toArgb());
   }
// https://t.me/pasterplace

   public GuiTextBuilder cornerColors(int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
      this.colorProvider = (glyphTextIndex, textureU, textureV) -> ColorUtils.interpolateColorBilinear(
         topLeftColor, topRightColor, bottomRightColor, bottomLeftColor, textureU, textureV
      );
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder cornerColors(RgbaColor topLeftColor, RgbaColor topRightColor, RgbaColor bottomRightColor, RgbaColor bottomLeftColor) {
      return this.cornerColors(topLeftColor.toArgb(), topRightColor.toArgb(), bottomRightColor.toArgb(), bottomLeftColor.toArgb());
   }
// https://t.me/pasterplace

   public GuiTextBuilder colorGradient(ColorGradient gradient) {
      this.colorProvider = (glyphTextIndex, textureU, textureV) -> gradient.sampleColor(textureU, textureV);
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder colorProvider(GlyphColorProvider colorProvider) {
      this.colorProvider = colorProvider;
      return this;
   }
// https://t.me/pasterplace

   public GuiTextBuilder alpha(float alpha) {
      this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
      return this;
   }
// https://t.me/pasterplace

   public MsdfFont font() {
      return this.font;
   }
// https://t.me/pasterplace

   public String text() {
      return this.text;
   }
// https://t.me/pasterplace

   public GlyphAtlasEntry glyphEntry() {
      return GlyphAtlasEntry.getOrCreate(this.font, this.text, this.atlasScale / Math.max(this.fontSize, 0.001F));
   }
// https://t.me/pasterplace

   public float width() {
      return this.glyphEntry().advanceWidth() * this.fontSize;
   }
// https://t.me/pasterplace

   public float height() {
      return this.glyphEntry().lineHeight() * this.fontSize;
   }
// https://t.me/pasterplace

   public TextBounds measure() {
      GlyphAtlasEntry glyphEntry = this.glyphEntry();
      return new TextBounds(
         this.alignedX(glyphEntry), this.baselineY(glyphEntry), glyphEntry.advanceWidth() * this.fontSize, glyphEntry.lineHeight() * this.fontSize
      );
   }
// https://t.me/pasterplace

   public GlyphRenderState buildRenderState(DrawContext drawContext) {
      return this.createRenderState(drawContext, this.glyphEntry());
   }
// https://t.me/pasterplace

   public void render(DrawContext drawContext) {
      if (!this.text.isEmpty() && !(this.fontSize <= 0.0F) && !(this.alpha <= 0.0F)) {
         GlyphAtlasEntry glyphEntry = this.glyphEntry();
         if (glyphEntry.glyphCount() != 0) {
            GuiRenderStateBatcher.addOrBatch(drawContext, this.createRenderState(drawContext, glyphEntry));
         }
      }
   }
// https://t.me/pasterplace

   private GlyphRenderState createRenderState(DrawContext drawContext, GlyphAtlasEntry glyphEntry) {
      return new GlyphRenderState(
         glyphEntry,
         GuiRenderStateBatcher.getMatrix(drawContext),
         this.alignedX(glyphEntry),
         this.baselineY(glyphEntry),
         this.fontSize,
         this.baseColor,
         this.colorProvider,
         this.alpha,
         drawContext.scissorStack.peekLast()
      );
   }
// https://t.me/pasterplace

   private float alignedX(GlyphAtlasEntry glyphEntry) {
      return this.x - glyphEntry.advanceWidth() * this.fontSize * this.alignment.II1Il11l111II11IIl();
   }
// https://t.me/pasterplace

   private float baselineY(GlyphAtlasEntry glyphEntry) {
      return switch (this.baselinePosition) {
         case TOP -> this.y;
         case CENTER -> this.y - glyphEntry.lineHeight() * this.fontSize * 0.5F;
         case BOTTOM -> this.y - glyphEntry.lineHeight() * this.fontSize;
         case BASELINE -> this.y - glyphEntry.ascender() * this.fontSize;
      };
   }
}

