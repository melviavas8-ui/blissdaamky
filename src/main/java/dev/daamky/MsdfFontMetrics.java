package dev.daamky;

public record MsdfFontMetrics(float lineHeight, float ascender, float descender, float underlineY, float underlineThickness) {

   public MsdfFontMetrics(float lineHeight, float ascender, float descender, float underlineY, float underlineThickness) {
      this.lineHeight = lineHeight;
      this.ascender = ascender;
      this.descender = descender;
      this.underlineY = underlineY;
      this.underlineThickness = underlineThickness;
   }
}

