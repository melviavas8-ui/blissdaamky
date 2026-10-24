package dev.daamky;

import net.minecraft.client.gui.DrawContext;
import org.jspecify.annotations.Nullable;

public final class GuiTextBuilder {
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

   private GuiTextBuilder() {
   }

   public static GuiTextBuilder create() {
      return new GuiTextBuilder();
   }

   public static GuiTextBuilder create(String displayText) {
      return new GuiTextBuilder().text(displayText);
   }

   public static GuiTextBuilder create(MsdfFont font, String displayText) {
      return new GuiTextBuilder().font(font).text(displayText);
   }

   public static GuiTextBuilder createAt(MsdfFont font, String displayText, float x, float y, float fontSize) {
      return create(font, displayText).position(x, y).fontSize(fontSize);
   }

   public GuiTextBuilder font(MsdfFont font) {
      this.font = font;
      return this;
   }

   public GuiTextBuilder fontByName(String fontName) {
      return this.font(Medium.getFont(fontName));
   }

   public GuiTextBuilder text(String displayText) {
      this.text = displayText == null ? "" : displayText;
      return this;
   }

   public GuiTextBuilder position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }

   public GuiTextBuilder fontSize(float fontSize) {
      this.fontSize = fontSize;
      return this;
   }

   public GuiTextBuilder atlasScale(float atlasScale) {
      this.atlasScale = atlasScale;
      return this;
   }

   public GuiTextBuilder alignment(HorizontalTextAlignment alignment) {
      this.alignment = alignment;
      return this;
   }

   public GuiTextBuilder baselinePosition(TextBaselinePosition baselinePosition) {
      this.baselinePosition = baselinePosition;
      return this;
   }

   public GuiTextBuilder centered() {
      return this.alignment(HorizontalTextAlignment.CENTER).baselinePosition(TextBaselinePosition.CENTER);
   }

   public GuiTextBuilder centeredAt(float x, float y) {
      return this.position(x, y).centered();
   }

   public GuiTextBuilder color(int argbColor) {
      this.baseColor = argbColor;
      this.colorProvider = null;
      return this;
   }

   public GuiTextBuilder color(RgbaColor color) {
      return this.color(color.toArgb());
   }

   public GuiTextBuilder verticalGradient(int startColor, int endColor) {
      this.colorProvider = (glyphTextIndex, textureU, textureV) -> ColorUtils.interpolateColor(startColor, endColor, textureV);
      return this;
   }

   public GuiTextBuilder verticalGradient(RgbaColor startColor, RgbaColor endColor) {
      return this.verticalGradient(startColor.toArgb(), endColor.toArgb());
   }

   public GuiTextBuilder horizontalGradient(int startColor, int endColor) {
      this.colorProvider = (glyphTextIndex, textureU, textureV) -> ColorUtils.interpolateColor(startColor, endColor, textureU);
      return this;
   }

   public GuiTextBuilder horizontalGradient(RgbaColor startColor, RgbaColor endColor) {
      return this.horizontalGradient(startColor.toArgb(), endColor.toArgb());
   }

   public GuiTextBuilder cornerColors(int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
      this.colorProvider = (glyphTextIndex, textureU, textureV) -> ColorUtils.interpolateColorBilinear(
         topLeftColor, topRightColor, bottomRightColor, bottomLeftColor, textureU, textureV
      );
      return this;
   }

   public GuiTextBuilder cornerColors(RgbaColor topLeftColor, RgbaColor topRightColor, RgbaColor bottomRightColor, RgbaColor bottomLeftColor) {
      return this.cornerColors(topLeftColor.toArgb(), topRightColor.toArgb(), bottomRightColor.toArgb(), bottomLeftColor.toArgb());
   }

   public GuiTextBuilder colorGradient(ColorGradient gradient) {
      this.colorProvider = (glyphTextIndex, textureU, textureV) -> gradient.sampleColor(textureU, textureV);
      return this;
   }

   public GuiTextBuilder colorProvider(GlyphColorProvider colorProvider) {
      this.colorProvider = colorProvider;
      return this;
   }

   public GuiTextBuilder alpha(float alpha) {
      this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
      return this;
   }

   public MsdfFont font() {
      return this.font;
   }

   public String text() {
      return this.text;
   }

   public GlyphAtlasEntry glyphEntry() {
      return GlyphAtlasEntry.getOrCreate(this.font, this.text, this.atlasScale / Math.max(this.fontSize, 0.001F));
   }

   public float width() {
      return this.glyphEntry().advanceWidth() * this.fontSize;
   }

   public float height() {
      return this.glyphEntry().lineHeight() * this.fontSize;
   }

   public TextBounds measure() {
      GlyphAtlasEntry glyphEntry = this.glyphEntry();
      return new TextBounds(
         this.alignedX(glyphEntry), this.baselineY(glyphEntry), glyphEntry.advanceWidth() * this.fontSize, glyphEntry.lineHeight() * this.fontSize
      );
   }

   public GlyphRenderState buildRenderState(DrawContext drawContext) {
      return this.createRenderState(drawContext, this.glyphEntry());
   }

   public void render(DrawContext drawContext) {
      if (!this.text.isEmpty() && !(this.fontSize <= 0.0F) && !(this.alpha <= 0.0F)) {
         GlyphAtlasEntry glyphEntry = this.glyphEntry();
         if (glyphEntry.glyphCount() != 0) {
            GuiRenderStateBatcher.addOrBatch(drawContext, this.createRenderState(drawContext, glyphEntry));
         }
      }
   }

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

   private float alignedX(GlyphAtlasEntry glyphEntry) {
      return this.x - glyphEntry.advanceWidth() * this.fontSize * this.alignment.II1Il11l111II11IIl();
   }

   private float baselineY(GlyphAtlasEntry glyphEntry) {
      return switch (this.baselinePosition) {
         case TOP -> this.y;
         case CENTER -> this.y - glyphEntry.lineHeight() * this.fontSize * 0.5F;
         case BOTTOM -> this.y - glyphEntry.lineHeight() * this.fontSize;
         case BASELINE -> this.y - glyphEntry.ascender() * this.fontSize;
      };
   }
}

