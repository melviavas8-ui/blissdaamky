package dev.daamky;

import java.time.LocalTime;

public final class Ambience extends ModuleStructure {
   private final MultiOptionSetting timeOfDaySetting = new MultiOptionSetting(
      "Время", "Ночь", "Не менять", "Рассвет", "Утро", "День", "Вечер", "Заход солнца", "Ночь", "Время из реальной жизни", "Своё"
   );
   private final FloatSetting customHourSetting = new FloatSetting("Час", 12.0F, 0.0F, 24.0F, 0.5F);
   private final MultiOptionSetting skyStyleSetting = new MultiOptionSetting(
      "Небо", "Обычное", "Обычное", "Сок", "Северное сияние", "Звёздная ночь", "Закат", "Облака", "Туманность"
   );
   private final MultiOptionSetting weatherModeSetting = new MultiOptionSetting(
      "Погода", "Обычная", "Обычная", "Туман", "Мокрый мир", "Туман + мокрый мир"
   );
   private final FloatSetting fogDensitySetting = new FloatSetting("Плотность тумана", 0.6F, 0.1F, 1.0F, 0.05F);
   private final BooleanSetting sunRaysEnabledSetting = new BooleanSetting("Лучи солнца", true);
   private final FloatSetting sunRayStrengthSetting = new FloatSetting("Сила лучей", 1.0F, 0.1F, 2.0F, 0.05F);
   private final FloatSetting humiditySetting = new FloatSetting("Влажность", 0.8F, 0.1F, 1.0F, 0.05F);

   public Ambience() {
      super("Ambience", "Время суток, небо и погода", ModuleCategory.RENDER);
      this.customHourSetting.visibleWhen(() -> this.timeOfDaySetting.isSelected("Своё"));
      this.fogDensitySetting.visibleWhen(this::isFogEnabled);
      this.humiditySetting.visibleWhen(this::isWetWeatherEnabled);
      this.sunRayStrengthSetting.visibleWhen(this.sunRaysEnabledSetting::getValue);
      this.addSettings(
         this.timeOfDaySetting,
         this.customHourSetting,
         this.skyStyleSetting,
         this.weatherModeSetting,
         this.fogDensitySetting,
         this.humiditySetting,
         this.sunRaysEnabledSetting,
         this.sunRayStrengthSetting
      );
   }

   public static Ambience getInstance() {
      return ModuleRegistry.getInstance().getModuleByClass(Ambience.class);
   }

   public float getSkyStyleIndex() {
      return switch ((String)this.skyStyleSetting.getValue()) {
         case "Сок" -> 1.0F;
         case "Северное сияние" -> 2.0F;
         case "Звёздная ночь" -> 3.0F;
         case "Закат" -> 4.0F;
         case "Облака" -> 5.0F;
         case "Туманность" -> 6.0F;
         default -> 0.0F;
      };
   }

   public float getSunRayStrength() {
      return this.sunRaysEnabledSetting.getValue() ? this.sunRayStrengthSetting.getValue() : 0.0F;
   }

   public boolean isFogEnabled() {
      return this.weatherModeSetting.isSelected("Туман") || this.weatherModeSetting.isSelected("Туман + мокрый мир");
   }

   public boolean isWetWeatherEnabled() {
      return this.weatherModeSetting.isSelected("Мокрый мир") || this.weatherModeSetting.isSelected("Туман + мокрый мир");
   }

   public float getFogDensity() {
      return this.fogDensitySetting.getValue();
   }

   public float getHumidity() {
      return this.humiditySetting.getValue();
   }

   public float[] getAccentRgb() {
      RgbaColor l1I1IlllIlI4 = Accent.getAccentColorWithAlpha(255);
      return new float[]{l1I1IlllIlI4.red() / 255.0F, l1I1IlllIlI4.green() / 255.0F, l1I1IlllIlI4.blue() / 255.0F};
   }

   public static long adjustWorldTime(long worldTime) {
      Ambience moduleStructure = (Ambience)ModuleRegistry.getInstance().getModuleByClass(Ambience.class);
      if (moduleStructure != null && moduleStructure.isEnabled() && !moduleStructure.timeOfDaySetting.isSelected("Не менять")) {
         long dayStartTime = Math.floorDiv(worldTime, 24000L) * 24000L;
         return dayStartTime + moduleStructure.getSelectedTimeOfDay();
      } else {
         return worldTime;
      }
   }

   private long getSelectedTimeOfDay() {
      return switch ((String)this.timeOfDaySetting.getValue()) {
         case "Рассвет" -> 23000L;
         case "Утро" -> 1000L;
         case "День" -> 6000L;
         case "Вечер" -> 12000L;
         case "Заход солнца" -> 12800L;
         case "Ночь" -> 18000L;
         case "Время из реальной жизни" -> {
            LocalTime localTime = LocalTime.now();
            yield worldTimeFromDayProgress(localTime.getHour() + localTime.getMinute() / 60.0F);
         }
         default -> worldTimeFromDayProgress(this.customHourSetting.getValue());
      };
   }

   private static long worldTimeFromDayProgress(float dayProgress) {
      return Math.floorMod((long)(dayProgress * 1000.0F) + 18000L, 24000L);
   }
}

