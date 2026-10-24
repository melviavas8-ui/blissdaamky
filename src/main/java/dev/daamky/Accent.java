package dev.daamky;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Random;

public final class Accent {
   private static final RgbaColor DEFAULT_ACCENT_COLOR = RgbaColor.rgb(120, 190, 255);
   private static final RgbaColor DEFAULT_BACKGROUND_COLOR = RgbaColor.rgb(18, 36, 64);
   private static final RgbaColor LIGHT_TEXT_COLOR = RgbaColor.rgb(238, 244, 252);
   private static final RgbaColor BLACK_BACKGROUND_COLOR = RgbaColor.rgb(12, 12, 14);
   private static final RgbaColor DEFAULT_TEXT_COLOR = RgbaColor.rgb(242, 244, 248);
   private static final List<Accent.ThemePreset> THEME_PRESETS = List.of(
      new Accent.ThemePreset("Sakura", RgbaColor.rgb(255, 165, 210), RgbaColor.rgb(68, 25, 52), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Ocean", DEFAULT_ACCENT_COLOR, DEFAULT_BACKGROUND_COLOR, LIGHT_TEXT_COLOR),
      new Accent.ThemePreset("Mint", RgbaColor.rgb(130, 235, 200), RgbaColor.rgb(14, 46, 42), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Amber", RgbaColor.rgb(255, 190, 120), RgbaColor.rgb(58, 36, 16), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Violet", RgbaColor.rgb(190, 160, 255), RgbaColor.rgb(38, 26, 68), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Crimson", RgbaColor.rgb(255, 95, 110), RgbaColor.rgb(62, 18, 24), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Lime", RgbaColor.rgb(185, 245, 110), RgbaColor.rgb(30, 50, 16), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Sky", RgbaColor.rgb(110, 225, 255), RgbaColor.rgb(14, 42, 56), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Peach", RgbaColor.rgb(255, 160, 130), RgbaColor.rgb(62, 32, 26), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Lavender", RgbaColor.rgb(215, 190, 255), RgbaColor.rgb(44, 34, 64), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Gold", RgbaColor.rgb(255, 215, 90), RgbaColor.rgb(56, 44, 12), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Mono", RgbaColor.rgb(225, 228, 236), RgbaColor.rgb(30, 30, 36), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Rose", RgbaColor.rgb(255, 120, 160), RgbaColor.rgb(60, 20, 36), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Coral", RgbaColor.rgb(255, 130, 100), RgbaColor.rgb(60, 26, 20), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Sunset", RgbaColor.rgb(255, 150, 80), RgbaColor.rgb(56, 26, 30), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Lemon", RgbaColor.rgb(250, 240, 120), RgbaColor.rgb(48, 46, 14), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Emerald", RgbaColor.rgb(80, 220, 140), RgbaColor.rgb(12, 44, 28), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Teal", RgbaColor.rgb(70, 210, 200), RgbaColor.rgb(10, 42, 42), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Ice", RgbaColor.rgb(190, 230, 255), RgbaColor.rgb(24, 36, 50), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Cobalt", RgbaColor.rgb(90, 130, 255), RgbaColor.rgb(16, 22, 60), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Indigo", RgbaColor.rgb(140, 120, 255), RgbaColor.rgb(26, 20, 62), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Grape", RgbaColor.rgb(200, 110, 255), RgbaColor.rgb(42, 18, 60), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Magenta", RgbaColor.rgb(255, 90, 220), RgbaColor.rgb(56, 14, 50), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Blood", RgbaColor.rgb(220, 40, 60), RgbaColor.rgb(36, 8, 12), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Forest", RgbaColor.rgb(120, 190, 100), RgbaColor.rgb(20, 36, 18), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Sand", RgbaColor.rgb(230, 200, 160), RgbaColor.rgb(48, 40, 30), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Steel", RgbaColor.rgb(150, 170, 190), RgbaColor.rgb(26, 30, 36), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Neon", RgbaColor.rgb(60, 255, 170), RgbaColor.rgb(8, 30, 24), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Aqua", RgbaColor.rgb(80, 240, 255), RgbaColor.rgb(8, 34, 44), DEFAULT_TEXT_COLOR),
      new Accent.ThemePreset("Candy", RgbaColor.rgb(255, 180, 230), RgbaColor.rgb(54, 30, 50), DEFAULT_TEXT_COLOR)
   );
   private static final Random RANDOM = new Random();
   private static final Accent INSTANCE = new Accent();
   private RgbaColor accentColor = DEFAULT_ACCENT_COLOR;
   private RgbaColor backgroundColor = DEFAULT_BACKGROUND_COLOR;
   private RgbaColor textColor = LIGHT_TEXT_COLOR;
   private float blurRadius = 4.0F;
   private float opacity = 1.0F;
   private float cornerRadius = 9.0F;
   private float outlineOpacity = 0.17F;
   private float tintIntensity = 0.4F;
   private boolean autoBackgroundEnabled = false;
   private boolean blackBackgroundEnabled = false;
   private boolean accentBackgroundEnabled = false;
   private Accent.GuiStyle guiStyle = Accent.GuiStyle.GLASS;

   private Accent() {
   }

   public static Accent getInstance() {
      return INSTANCE;
   }

   public static List<Accent.ThemePreset> getThemePresets() {
      return THEME_PRESETS;
   }

   public RgbaColor getColor(Accent.ColorRole role) {
      return switch (role) {
         case ACCENT -> this.accentColor;
         case BACKGROUND -> this.backgroundColor;
         case TEXT -> this.textColor;
      };
   }

   public void setColor(Accent.ColorRole role, RgbaColor color) {
      RgbaColor opaqueColor = color.withAlpha(255);
      switch (role) {
         case ACCENT:
            this.accentColor = opaqueColor;
            if (this.autoBackgroundEnabled) {
               this.backgroundColor = deriveBackgroundColorFromAccent(opaqueColor);
            }
            break;
         case BACKGROUND:
            this.backgroundColor = opaqueColor;
            break;
         case TEXT:
            this.textColor = opaqueColor;
      }
   }

   private static RgbaColor deriveBackgroundColorFromAccent(RgbaColor accentColor) {
      float[] hsv = rgbToHsv(accentColor);
      return RgbaColor.fromHsv(hsv[0], Math.min(0.62F, hsv[1] * 0.78F), 0.21F).withAlpha(255);
   }

   public static float[] rgbToHsv(RgbaColor color) {
      float red = color.red() / 255.0F;
      float green = color.green() / 255.0F;
      float blue = color.blue() / 255.0F;
      float maximumChannel = Math.max(red, Math.max(green, blue));
      float minimumChannel = Math.min(red, Math.min(green, blue));
      float chroma = maximumChannel - minimumChannel;
      float hue = 0.0F;
      if (chroma > 1.0E-5F) {
         if (maximumChannel == red) {
            hue = (green - blue) / chroma / 6.0F;
         } else if (maximumChannel == green) {
            hue = (2.0F + (blue - red) / chroma) / 6.0F;
         } else {
            hue = (4.0F + (red - green) / chroma) / 6.0F;
         }

         hue -= (float)Math.floor(hue);
      }

      return new float[]{hue, maximumChannel <= 0.0F ? 0.0F : chroma / maximumChannel, maximumChannel};
   }

   public void applyTheme(Accent.ThemePreset themePreset) {
      this.accentColor = themePreset.accentColor().withAlpha(255);
      this.backgroundColor = themePreset.backgroundColor().withAlpha(255);
      this.textColor = themePreset.textColor().withAlpha(255);
   }

   public boolean matchesTheme(Accent.ThemePreset themePreset) {
      return this.accentColor.toArgb() == themePreset.accentColor().withAlpha(255).toArgb()
         && this.backgroundColor.toArgb() == themePreset.backgroundColor().withAlpha(255).toArgb()
         && this.textColor.toArgb() == themePreset.textColor().withAlpha(255).toArgb();
   }

   public void randomizeAccent() {
      float hue = RANDOM.nextFloat();
      float saturation = 0.45F + RANDOM.nextFloat() * 0.45F;
      this.accentColor = RgbaColor.fromHsv(hue, saturation, 0.92F + RANDOM.nextFloat() * 0.08F).withAlpha(255);
      this.backgroundColor = deriveBackgroundColorFromAccent(this.accentColor);
      this.textColor = RgbaColor.fromHsv(hue, 0.06F, 0.97F).withAlpha(255);
   }

   public void resetToDefaults() {
      this.accentColor = DEFAULT_ACCENT_COLOR;
      this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
      this.textColor = LIGHT_TEXT_COLOR;
      this.blurRadius = 4.0F;
      this.opacity = 1.0F;
      this.cornerRadius = 9.0F;
      this.outlineOpacity = 0.17F;
      this.tintIntensity = 0.4F;
      this.autoBackgroundEnabled = false;
      this.blackBackgroundEnabled = false;
      this.accentBackgroundEnabled = false;
      this.guiStyle = Accent.GuiStyle.GLASS;
   }

   public float getBlurRadius() {
      return this.blurRadius;
   }

   public void setBlurRadius(float blurRadius) {
      this.blurRadius = Math.clamp(blurRadius, 1.0F, 6.0F);
   }

   public float getOpacity() {
      return this.opacity;
   }

   public void setOpacity(float opacity) {
      this.opacity = Math.clamp(opacity, 0.0F, 1.0F);
   }

   public JsonObject toJson() {
      JsonObject settings = new JsonObject();
      settings.addProperty("accent", packRgb(this.accentColor));
      settings.addProperty("background", packRgb(this.backgroundColor));
      settings.addProperty("text", packRgb(this.textColor));
      settings.addProperty("blur", this.blurRadius);
      settings.addProperty("opacity", this.opacity);
      settings.addProperty("outline", this.outlineOpacity);
      settings.addProperty("tint", this.tintIntensity);
      settings.addProperty("autoBackground", this.autoBackgroundEnabled);
      settings.addProperty("blackBackground", this.blackBackgroundEnabled);
      settings.addProperty("accentBackground", this.accentBackgroundEnabled);
      settings.addProperty("style", this.guiStyle.name());
      return settings;
   }

   public void fromJson(JsonObject settings) {
      try {
         if (settings.has("accent")) {
            this.accentColor = unpackRgb(settings.get("accent").getAsInt());
         }

         if (settings.has("background")) {
            this.backgroundColor = unpackRgb(settings.get("background").getAsInt());
         }

         if (settings.has("text")) {
            this.textColor = unpackRgb(settings.get("text").getAsInt());
         }

         if (settings.has("blur")) {
            this.setBlurRadius(settings.get("blur").getAsFloat());
         }

         if (settings.has("opacity")) {
            this.setOpacity(settings.get("opacity").getAsFloat());
         }

         if (settings.has("outline")) {
            this.setOutlineOpacity(settings.get("outline").getAsFloat());
         }

         if (settings.has("tint")) {
            this.setTintIntensity(settings.get("tint").getAsFloat());
         }

         if (settings.has("autoBackground")) {
            this.autoBackgroundEnabled = settings.get("autoBackground").getAsBoolean();
         }

         if (settings.has("blackBackground")) {
            this.blackBackgroundEnabled = settings.get("blackBackground").getAsBoolean();
         }

         if (settings.has("accentBackground")) {
            this.accentBackgroundEnabled = settings.get("accentBackground").getAsBoolean();
         }

         if (settings.has("style")) {
            this.guiStyle = Accent.GuiStyle.valueOf(settings.get("style").getAsString());
         }
      } catch (RuntimeException invalidSettingsException) {
         invalidSettingsException.printStackTrace();
      }

      this.cornerRadius = 9.0F;
   }

   private static int packRgb(RgbaColor color) {
      return color.red() << 16 | color.green() << 8 | color.blue();
   }

   private static RgbaColor unpackRgb(int packedRgb) {
      return RgbaColor.rgb(packedRgb >> 16 & 0xFF, packedRgb >> 8 & 0xFF, packedRgb & 0xFF);
   }

   public float getCornerRadius() {
      return this.cornerRadius;
   }

   public void setCornerRadius(float cornerRadius) {
      this.cornerRadius = Math.clamp(cornerRadius, 0.0F, 16.0F);
   }

   public float getOutlineOpacity() {
      return this.outlineOpacity;
   }

   public void setOutlineOpacity(float outlineOpacity) {
      this.outlineOpacity = Math.clamp(outlineOpacity, 0.0F, 1.0F);
   }

   public float getTintIntensity() {
      return this.tintIntensity;
   }

   public void setTintIntensity(float tintIntensity) {
      this.tintIntensity = Math.clamp(tintIntensity, 0.0F, 2.0F);
   }

   public Accent.GuiStyle getGuiStyle() {
      return this.guiStyle;
   }

   public void setGuiStyle(Accent.GuiStyle style) {
      this.guiStyle = style;
   }

   public boolean isGlassStyle() {
      return this.guiStyle == Accent.GuiStyle.GLASS;
   }

   public boolean isAccentBackgroundEnabled() {
      return this.accentBackgroundEnabled;
   }

   public void setAccentBackgroundEnabled(boolean enabled) {
      this.accentBackgroundEnabled = enabled;
   }

   public boolean isBlackBackgroundEnabled() {
      return this.blackBackgroundEnabled;
   }

   public void setBlackBackgroundEnabled(boolean enabled) {
      this.blackBackgroundEnabled = enabled;
   }

   public boolean isAutoBackgroundEnabled() {
      return this.autoBackgroundEnabled;
   }

   public void setAutoBackgroundEnabled(boolean enabled) {
      this.autoBackgroundEnabled = enabled;
      if (enabled) {
         this.backgroundColor = deriveBackgroundColorFromAccent(this.accentColor);
      }
   }

   public static RgbaColor getAccentColorWithAlpha(int alpha) {
      return INSTANCE.accentColor.withAlpha(alpha);
   }

   public static RgbaColor getLighterAccentColor(int alpha) {
      return INSTANCE.accentColor.lighten(0.22F).withAlpha(alpha);
   }

   public static RgbaColor getDarkerAccentColor(int alpha) {
      return INSTANCE.accentColor.darken(0.22F).withAlpha(alpha);
   }

   public static RgbaColor getAccentBackgroundColor(int alpha) {
      return !INSTANCE.accentBackgroundEnabled
         ? INSTANCE.accentColor.withAlpha(0)
         : INSTANCE.accentColor.withAlpha((int)Math.clamp(alpha * INSTANCE.tintIntensity, 0.0F, 255.0F));
   }

   public static RgbaColor applyOutlineOpacity(RgbaColor color, int alpha) {
      return color.withAlpha((int)Math.clamp(alpha * INSTANCE.outlineOpacity, 0.0F, 255.0F));
   }

   public static RgbaColor getTintedBackgroundColor(int alpha) {
      return INSTANCE.getEffectiveBackgroundColor().darken(0.18F).withAlpha((int)(alpha * INSTANCE.opacity));
   }

   public static RgbaColor getDeepBackgroundColor(int alpha) {
      return INSTANCE.getEffectiveBackgroundColor().darken(0.45F).withAlpha((int)(alpha * INSTANCE.opacity));
   }

   private RgbaColor getEffectiveBackgroundColor() {
      return this.blackBackgroundEnabled ? BLACK_BACKGROUND_COLOR : this.backgroundColor;
   }

   public static RgbaColor getTextColorWithAlpha(int alpha) {
      return INSTANCE.textColor.withAlpha(alpha);
   }

   public static RgbaColor getDarkerTextColor(int alpha) {
      return INSTANCE.textColor.darken(0.18F).withAlpha(alpha);
   }

   public static RgbaColor getDeepTextColor(int alpha) {
      return INSTANCE.textColor.darken(0.28F).withAlpha(alpha);
   }

   public record ThemePreset(String name, RgbaColor accentColor, RgbaColor backgroundColor, RgbaColor textColor) {

      public ThemePreset(String name, RgbaColor accentColor, RgbaColor backgroundColor, RgbaColor textColor) {
         this.name = name;
         this.accentColor = accentColor;
         this.backgroundColor = backgroundColor;
         this.textColor = textColor;
      }
   }

   public enum GuiStyle {
      MINIMAL("Minimal"),
      GLASS("Glass");

      private final String displayName;

      GuiStyle(String displayName) {
         this.displayName = displayName;
      }

      public String displayName() {
         return this.displayName;
      }

      // $VF: synthetic method
      private static Accent.GuiStyle[] $values() {
         return new Accent.GuiStyle[]{MINIMAL, GLASS};
      }
   }

   public enum ColorRole {
      ACCENT("Accent"),
      BACKGROUND("Background"),
      TEXT("Text");

      private final String displayName;

      ColorRole(String displayName) {
         this.displayName = displayName;
      }

      public String displayName() {
         return this.displayName;
      }

      // $VF: synthetic method
      private static Accent.ColorRole[] $values() {
         return new Accent.ColorRole[]{ACCENT, BACKGROUND, TEXT};
      }
   }
   public static RgbaColor colorWithMutedAccent(int alpha) {
      return Accent.INSTANCE.textColor.darken(0.28F).withAlpha(alpha);
   }

}

