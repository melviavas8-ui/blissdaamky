package dev.daamky;

public record MsdfFontMetrics(float lineHeight, float ascender, float descender, float underlineY, float underlineThickness) {
    private static final String ped8168bc = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   public MsdfFontMetrics(float lineHeight, float ascender, float descender, float underlineY, float underlineThickness) {
      this.lineHeight = lineHeight;
      this.ascender = ascender;
      this.descender = descender;
      this.underlineY = underlineY;
      this.underlineThickness = underlineThickness;
   }
}

