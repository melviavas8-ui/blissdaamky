package dev.daamky;

import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Hands extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting renderModeSetting = new MultiOptionSetting(
      "Режим", "Nothing", "Nothing", "Заливка", "Шейдер", "Зеркало", "Шейдер + Зеркало"
   );
   private final BooleanSetting autoThemeColorsSetting = new BooleanSetting("Авто цвет под тему", true);
   private final BooleanSetting autoItemColorsSetting = new BooleanSetting("Авто цвет под предмет", false);
   private final ColorSetting fillColorSetting = new ColorSetting("Цвет заливки", 16729156);
   private final ColorSetting shaderColorSetting = new ColorSetting("Цвет шейдера", 13144831);
   private final ColorSetting secondaryShaderColorSetting = new ColorSetting("Второй цвет шейдера", 5242696);
   private final ColorSetting glowColorSetting = new ColorSetting("Цвет свечения", 16765503);
   private final ColorSetting secondaryGlowColorSetting = new ColorSetting("Второй цвет свечения", 16722432);
   private final ColorSetting outlineColorSetting = new ColorSetting("Цвет обводки", 16777215);
   private final FloatSetting colorBlendSetting = new FloatSetting("Смешивание", 0.5F, 0.0F, 1.0F, 0.01F);
   private final FloatSetting mirrorDistortionSetting = new FloatSetting("Искажение зеркала", 0.0F, 0.0F, 1.0F, 0.05F);
   private final FloatSetting blurStrengthSetting = new FloatSetting("Сила размытия", 1.0F, 0.1F, 3.0F, 0.05F);
   private final BooleanSetting rainbowEnabledSetting = new BooleanSetting("Радужная", false);
   private final FloatSetting rainbowSpeedSetting = new FloatSetting("Скорость радуги", 0.4F, 0.0F, 2.0F, 0.05F);
   private final FloatSetting rainbowScaleSetting = new FloatSetting("Масштаб радуги", 1.0F, 0.2F, 3.0F, 0.1F);
   private final FloatSetting fillOpacitySetting = new FloatSetting("Прозрачность заливки", 0.8F, 0.0F, 1.0F, 0.05F);
   private final BooleanSetting keepShadowsSetting = new BooleanSetting("Сохранить тени", true);
   private final FloatSetting shadowStrengthSetting = new FloatSetting("Сила теней", 0.3F, 0.0F, 1.0F, 0.05F);
   private final MultiOptionSetting shaderStyleSetting = new MultiOptionSetting(
      "Стиль шейдера", "Шейдер", "Шейдер", "HandShader", "Туманность", "Космос", "Градиент", "Homie"
   );
   private final BooleanSetting useSecondShaderColorSetting = new BooleanSetting("Второй цвет", false);
   private final FloatSetting shaderSpeedSetting = new FloatSetting("Скорость шейдера", 1.5F, 0.05F, 2.5F, 0.05F);
   private final FloatSetting shaderScaleSetting = new FloatSetting("Масштаб шейдера", 1.5F, 0.2F, 5.0F, 0.1F);
   private final FloatSetting shaderGlowSetting = new FloatSetting("Свечение шейдера", 1.2F, 0.0F, 3.0F, 0.05F);
   private final FloatSetting shaderQualitySetting = new FloatSetting("Качество шейдера", 2.0F, 1.0F, 3.0F, 1.0F);
   private final BooleanSetting starFieldSetting = new BooleanSetting("Звёзды", true);
   private final FloatSetting shaderOpacitySetting = new FloatSetting("Прозрачность шейдера", 1.0F, 0.0F, 1.0F, 0.05F);
   private final BooleanSetting preserveShaderShadowsSetting = new BooleanSetting("Сохранить тени шейдера", false);
   private final FloatSetting shaderShadowStrengthSetting = new FloatSetting("Сила теней шейдера", 0.5F, 0.0F, 1.0F, 0.05F);
   private final BooleanSetting glowEnabledSetting = new BooleanSetting("Глов", true);
   private final FloatSetting blurRadiusSetting = new FloatSetting("Размытие", 3.0F, 1.0F, 5.0F, 1.0F);
   private final FloatSetting glowBrightnessSetting = new FloatSetting("Яркость", 1.0F, 0.5F, 5.0F, 0.1F);
   private final FloatSetting innerGlowSetting = new FloatSetting("Внутреннее свечение", 0.65F, 0.0F, 1.5F, 0.05F);
   private final BooleanSetting outlineEnabledSetting = new BooleanSetting("Обводка", true);
   private final FloatSetting outlineThicknessSetting = new FloatSetting("Толщина обводки", 1.0F, 0.5F, 2.0F, 0.5F);
   private final BooleanSetting trailEnabledSetting = new BooleanSetting("Шлейф", true);
   private final MultiOptionSetting trailModeSetting = new MultiOptionSetting("Режим шлейфа", "Обычный", "Обычный", "Энергия", "Ленты");
   private final FloatSetting energyTrailLengthSetting = new FloatSetting("Длина энергии", 1.0F, 0.4F, 2.0F, 0.05F);
   private final FloatSetting energyTrailBrightnessSetting = new FloatSetting("Яркость энергии", 1.35F, 0.5F, 2.5F, 0.05F);
   private final FloatSetting energyMovementSetting = new FloatSetting("Движение энергии", 1.3F, 0.0F, 2.0F, 0.05F);
   private final FloatSetting energyPulseSetting = new FloatSetting("Пульсация энергии", 1.0F, 0.0F, 2.0F, 0.05F);
   private final FloatSetting energyTrailBehindCameraSetting = new FloatSetting("Шлейф за камерой", 0.05F, 0.0F, 2.0F, 0.05F);
   private final FloatSetting ribbonLengthSetting = new FloatSetting("Длина лент", 0.3F, 0.15F, 3.0F, 0.05F);
   private final FloatSetting ribbonWidthSetting = new FloatSetting("Ширина лент", 0.55F, 0.25F, 1.5F, 0.05F);
   private final FloatSetting ribbonBrightnessSetting = new FloatSetting("Яркость лент", 1.0F, 0.0F, 2.5F, 0.05F);
   private final FloatSetting ribbonCameraStrengthSetting = new FloatSetting("Сила лент за камерой", 0.1F, 0.0F, 2.0F, 0.05F);
   private final FloatSetting ribbonLiftSetting = new FloatSetting("Подъём лент", 0.32F, 0.0F, 1.0F, 0.01F);
   private final FloatSetting ribbonCurvatureSetting = new FloatSetting("Изгиб лент", 2.0F, 0.0F, 2.0F, 0.05F);
   private final FloatSetting ribbonWaveSpeedSetting = new FloatSetting("Скорость волн", 1.5F, 0.0F, 3.0F, 0.05F);
   private final BooleanSetting ribbonsOnHitSetting = new BooleanSetting("Ленты при ударе", true);
   private final FloatSetting hitRibbonLengthSetting = new FloatSetting("Длина при ударе", 0.0F, 0.15F, 3.0F, 0.05F);
   private final FloatSetting hitRibbonStrengthSetting = new FloatSetting("Сила при ударе", 1.15F, 0.0F, 2.5F, 0.05F);
   private final BooleanSetting ribbonsWhileEatingSetting = new BooleanSetting("Ленты при еде", true);
   private final FloatSetting eatRibbonLengthSetting = new FloatSetting("Длина при еде", 0.65F, 0.15F, 3.0F, 0.05F);
   private final FloatSetting eatRibbonStrengthSetting = new FloatSetting("Сила при еде", 0.7F, 0.0F, 2.5F, 0.05F);
   private final FloatSetting trailFadeSpeedSetting = new FloatSetting("Скорость затухания", 0.04F, 0.002F, 0.2F, 0.002F);
   private final FloatSetting trailLiftSetting = new FloatSetting("Подъём", 0.2F, 0.0F, 1.5F, 0.05F);
   private final FloatSetting trailSwaySetting = new FloatSetting("Качание", 0.0F, 0.0F, 0.2F, 0.005F);
   private final FloatSetting trailCameraOffsetSetting = new FloatSetting("Шлейф от камеры", 0.0F, 0.0F, 2.0F, 0.05F);
   private final FloatSetting turbulenceSetting = new FloatSetting("Турбулентность", 0.02F, 0.0F, 0.6F, 0.01F);
   private final FloatSetting flickerSetting = new FloatSetting("Мерцание", 0.2F, 0.0F, 0.2F, 0.01F);
   private final BooleanSetting blowOffOnHitSetting = new BooleanSetting("Сдув при ударе", false);
   private final FloatSetting blowOffStrengthSetting = new FloatSetting("Сила сдува", 4.0F, 1.0F, 10.0F, 0.5F);
   private long animationStartedAtMillis = System.currentTimeMillis();
// https://t.me/pasterplace

   public Hands() {
      super("Hands", "Визуальные эффекты рук от первого лица", ModuleCategory.RENDER);
      this.colorBlendSetting.visibleWhen(this::isMirrorMode);
      this.mirrorDistortionSetting.visibleWhen(this::isMirrorMode);
      this.blurStrengthSetting.visibleWhen(this::isMirrorMode);
      this.rainbowEnabledSetting.visibleWhen(this::isFillMode);
      this.rainbowSpeedSetting.visibleWhen(() -> this.isFillMode() && this.rainbowEnabledSetting.getValue());
      this.rainbowScaleSetting.visibleWhen(() -> this.isFillMode() && this.rainbowEnabledSetting.getValue());
      this.fillOpacitySetting.visibleWhen(this::isFillMode);
      this.keepShadowsSetting.visibleWhen(this::isFillMode);
      this.shadowStrengthSetting.visibleWhen(() -> this.isFillMode() && this.keepShadowsSetting.getValue());
      this.shaderStyleSetting.visibleWhen(this::isShaderMode);
      this.useSecondShaderColorSetting.visibleWhen(this::isShaderMode);
      this.fillColorSetting.visibleWhen(() -> !this.autoThemeColorsSetting.getValue() && this.isFillMode());
      this.shaderColorSetting.visibleWhen(() -> !this.autoThemeColorsSetting.getValue() && this.isShaderMode());
      this.secondaryShaderColorSetting.visibleWhen(() -> !this.autoThemeColorsSetting.getValue() && this.isShaderMode() && this.useSecondShaderColorSetting.getValue());
      this.glowColorSetting.visibleWhen(() -> !this.autoThemeColorsSetting.getValue() && this.glowEnabledSetting.getValue());
      this.secondaryGlowColorSetting.visibleWhen(() -> !this.autoThemeColorsSetting.getValue() && this.glowEnabledSetting.getValue());
      this.outlineColorSetting.visibleWhen(() -> !this.autoThemeColorsSetting.getValue() && !this.autoItemColorsSetting.getValue() && this.outlineEnabledSetting.getValue());
      this.shaderSpeedSetting.visibleWhen(this::isShaderMode);
      this.shaderScaleSetting.visibleWhen(this::isShaderMode);
      this.shaderGlowSetting.visibleWhen(this::isShaderMode);
      this.shaderQualitySetting.visibleWhen(() -> this.isShaderMode() && !this.shaderStyleSetting.isSelected("Градиент"));
      this.starFieldSetting.visibleWhen(this::isShaderMode);
      this.shaderOpacitySetting.visibleWhen(this::isShaderMode);
      this.preserveShaderShadowsSetting.visibleWhen(this::isShaderMode);
      this.shaderShadowStrengthSetting.visibleWhen(() -> this.isShaderMode() && this.preserveShaderShadowsSetting.getValue());
      this.blurRadiusSetting.visibleWhen(this.glowEnabledSetting::getValue);
      this.glowBrightnessSetting.visibleWhen(this.glowEnabledSetting::getValue);
      this.innerGlowSetting.visibleWhen(this.glowEnabledSetting::getValue);
      this.outlineThicknessSetting.visibleWhen(this.outlineEnabledSetting::getValue);
      this.trailEnabledSetting.visibleWhen(this.glowEnabledSetting::getValue);
      this.trailModeSetting.visibleWhen(this::isTrailEffectEnabled);
      this.energyTrailLengthSetting.visibleWhen(this::isEnergyTrailMode);
      this.energyTrailBrightnessSetting.visibleWhen(this::isEnergyTrailMode);
      this.energyMovementSetting.visibleWhen(this::isEnergyTrailMode);
      this.energyPulseSetting.visibleWhen(this::isEnergyTrailMode);
      this.energyTrailBehindCameraSetting.visibleWhen(this::isEnergyTrailMode);
      this.ribbonLengthSetting.visibleWhen(this::isRibbonTrailMode);
      this.ribbonWidthSetting.visibleWhen(this::isRibbonTrailMode);
      this.ribbonBrightnessSetting.visibleWhen(this::isRibbonTrailMode);
      this.ribbonCameraStrengthSetting.visibleWhen(this::isRibbonTrailMode);
      this.ribbonLiftSetting.visibleWhen(this::isRibbonTrailMode);
      this.ribbonCurvatureSetting.visibleWhen(this::isRibbonTrailMode);
      this.ribbonWaveSpeedSetting.visibleWhen(this::isRibbonTrailMode);
      this.ribbonsOnHitSetting.visibleWhen(this::isRibbonTrailMode);
      this.hitRibbonLengthSetting.visibleWhen(() -> this.isRibbonTrailMode() && this.ribbonsOnHitSetting.getValue());
      this.hitRibbonStrengthSetting.visibleWhen(() -> this.isRibbonTrailMode() && this.ribbonsOnHitSetting.getValue());
      this.ribbonsWhileEatingSetting.visibleWhen(this::isRibbonTrailMode);
      this.eatRibbonLengthSetting.visibleWhen(() -> this.isRibbonTrailMode() && this.ribbonsWhileEatingSetting.getValue());
      this.eatRibbonStrengthSetting.visibleWhen(() -> this.isRibbonTrailMode() && this.ribbonsWhileEatingSetting.getValue());
      this.trailFadeSpeedSetting.visibleWhen(this::isDefaultTrailMode);
      this.trailLiftSetting.visibleWhen(this::isDefaultTrailMode);
      this.trailSwaySetting.visibleWhen(this::isDefaultTrailMode);
      this.trailCameraOffsetSetting.visibleWhen(this::isDefaultTrailMode);
      this.turbulenceSetting.visibleWhen(this::isDefaultTrailMode);
      this.flickerSetting.visibleWhen(this::isDefaultTrailMode);
      this.blowOffOnHitSetting.visibleWhen(this::isDefaultTrailMode);
      this.blowOffStrengthSetting.visibleWhen(() -> this.isDefaultTrailMode() && this.blowOffOnHitSetting.getValue());
      this.addSettings(
         this.renderModeSetting,
         this.autoThemeColorsSetting,
         this.autoItemColorsSetting,
         this.fillColorSetting,
         this.shaderColorSetting,
         this.secondaryShaderColorSetting,
         this.glowColorSetting,
         this.secondaryGlowColorSetting,
         this.outlineColorSetting,
         this.colorBlendSetting,
         this.mirrorDistortionSetting,
         this.blurStrengthSetting,
         this.rainbowEnabledSetting,
         this.rainbowSpeedSetting,
         this.rainbowScaleSetting,
         this.fillOpacitySetting,
         this.keepShadowsSetting,
         this.shadowStrengthSetting,
         this.shaderStyleSetting,
         this.useSecondShaderColorSetting,
         this.shaderSpeedSetting,
         this.shaderScaleSetting,
         this.shaderGlowSetting,
         this.shaderQualitySetting,
         this.starFieldSetting,
         this.shaderOpacitySetting,
         this.preserveShaderShadowsSetting,
         this.shaderShadowStrengthSetting,
         this.glowEnabledSetting,
         this.blurRadiusSetting,
         this.glowBrightnessSetting,
         this.innerGlowSetting,
         this.outlineEnabledSetting,
         this.outlineThicknessSetting,
         this.trailEnabledSetting,
         this.trailModeSetting,
         this.energyTrailLengthSetting,
         this.energyTrailBrightnessSetting,
         this.energyMovementSetting,
         this.energyPulseSetting,
         this.energyTrailBehindCameraSetting,
         this.ribbonLengthSetting,
         this.ribbonWidthSetting,
         this.ribbonBrightnessSetting,
         this.ribbonCameraStrengthSetting,
         this.ribbonLiftSetting,
         this.ribbonCurvatureSetting,
         this.ribbonWaveSpeedSetting,
         this.ribbonsOnHitSetting,
         this.hitRibbonLengthSetting,
         this.hitRibbonStrengthSetting,
         this.ribbonsWhileEatingSetting,
         this.eatRibbonLengthSetting,
         this.eatRibbonStrengthSetting,
         this.trailFadeSpeedSetting,
         this.trailLiftSetting,
         this.trailSwaySetting,
         this.trailCameraOffsetSetting,
         this.turbulenceSetting,
         this.flickerSetting,
         this.blowOffOnHitSetting,
         this.blowOffStrengthSetting
      );
   }
// https://t.me/pasterplace

   public static Hands getInstance() {
      return ModuleRegistry.getInstance().getModuleByClass(Hands.class);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.renderModeSetting.getValue();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.animationStartedAtMillis = System.currentTimeMillis();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      HandsCapture.destroyFramebuffer();
      TrailSampler.INSTANCE.close();
   }
// https://t.me/pasterplace

   public boolean isEffectActive() {
      return this.isEnabled() && (!this.isNothingMode() || this.glowEnabledSetting.getValue() || this.outlineEnabledSetting.getValue());
   }
// https://t.me/pasterplace

   public boolean isFillMode() {
      return this.renderModeSetting.isSelected("Заливка");
   }
// https://t.me/pasterplace

   public boolean isNothingMode() {
      return this.renderModeSetting.isSelected("Nothing");
   }
// https://t.me/pasterplace

   public boolean isShaderMode() {
      return this.renderModeSetting.isSelected("Шейдер") || this.renderModeSetting.isSelected("Шейдер + Зеркало");
   }
// https://t.me/pasterplace

   public boolean isMirrorMode() {
      return this.renderModeSetting.isSelected("Зеркало") || this.renderModeSetting.isSelected("Шейдер + Зеркало");
   }
// https://t.me/pasterplace

   public boolean isTrailEffectEnabled() {
      return this.glowEnabledSetting.getValue() && this.trailEnabledSetting.getValue();
   }
// https://t.me/pasterplace

   public boolean isEnergyTrailMode() {
      return this.isTrailEffectEnabled() && this.trailModeSetting.isSelected("Энергия");
   }
// https://t.me/pasterplace

   public boolean isRibbonTrailMode() {
      return this.isTrailEffectEnabled() && this.trailModeSetting.isSelected("Ленты");
   }
// https://t.me/pasterplace

   public boolean isDefaultTrailMode() {
      return this.isTrailEffectEnabled() && this.trailModeSetting.isSelected("Обычный");
   }
// https://t.me/pasterplace

   public int getTrailModeIndex() {
      return this.isRibbonTrailMode() ? 2 : (this.isEnergyTrailMode() ? 1 : 0);
   }
// https://t.me/pasterplace

   public float getRenderModeIndex() {
      if (this.isNothingMode()) {
         return 4.0F;
      } else if (this.isFillMode()) {
         return 0.0F;
      } else if (this.renderModeSetting.isSelected("Шейдер")) {
         return 1.0F;
      } else {
         return this.renderModeSetting.isSelected("Зеркало") ? 2.0F : 3.0F;
      }
   }
// https://t.me/pasterplace

   public float getShaderStyleIndex() {
      return switch ((String)this.shaderStyleSetting.getValue()) {
         case "HandShader" -> 4.0F;
         case "Туманность" -> 1.0F;
         case "Космос" -> 2.0F;
         case "Градиент" -> 3.0F;
         case "Homie" -> 5.0F;
         default -> 0.0F;
      };
   }
// https://t.me/pasterplace

   public float getShaderQualitySamples() {
      int qualityLevel = Math.round(this.shaderQualitySetting.getValue());
      return qualityLevel == 1 ? 3.0F : (qualityLevel == 3 ? 8.0F : 5.0F);
   }
// https://t.me/pasterplace

   public float getElapsedSeconds() {
      return (float)(System.currentTimeMillis() - this.animationStartedAtMillis) / 1000.0F;
   }
// https://t.me/pasterplace

   private static int getAccentColorArgb() {
      return Accent.getAccentColorWithAlpha(255).toArgb();
   }
// https://t.me/pasterplace

   private static int getLighterAccentColorArgb() {
      return Accent.getLighterAccentColor(255).toArgb();
   }
// https://t.me/pasterplace

   public int getShaderColorArgb() {
      return this.autoThemeColorsSetting.getValue() ? getAccentColorArgb() : this.shaderColorSetting.getArgbColor();
   }
// https://t.me/pasterplace

   public int getSecondaryShaderColorArgb() {
      if (this.autoThemeColorsSetting.getValue()) {
         return this.useSecondShaderColorSetting.getValue() ? getLighterAccentColorArgb() : getAccentColorArgb();
      } else {
         return this.useSecondShaderColorSetting.getValue() ? this.secondaryShaderColorSetting.getArgbColor() : this.shaderColorSetting.getArgbColor();
      }
   }
// https://t.me/pasterplace

   public int getFillColorArgb() {
      return this.autoThemeColorsSetting.getValue() ? getAccentColorArgb() : this.fillColorSetting.getArgbColor();
   }
// https://t.me/pasterplace

   public int getBaseAccentColorArgb() {
      return getAccentColorArgb();
   }
// https://t.me/pasterplace

   public int getGlowColorArgb() {
      return this.autoThemeColorsSetting.getValue() ? getAccentColorArgb() : this.glowColorSetting.getArgbColor();
   }
// https://t.me/pasterplace

   public int getSecondaryGlowColorArgb() {
      return this.autoThemeColorsSetting.getValue() ? getLighterAccentColorArgb() : this.secondaryGlowColorSetting.getArgbColor();
   }
// https://t.me/pasterplace

   public int getOutlineColorArgb() {
      if (this.autoItemColorsSetting.getValue()) {
         return -1;
      } else {
         return this.autoThemeColorsSetting.getValue() ? getAccentColorArgb() : this.outlineColorSetting.getArgbColor();
      }
   }
// https://t.me/pasterplace

   public boolean isItemColorEnabled() {
      return this.autoItemColorsSetting.getValue();
   }
// https://t.me/pasterplace

   public float getColorBlend() {
      return this.colorBlendSetting.getValue();
   }
// https://t.me/pasterplace

   public float getMirrorDistortion() {
      return this.mirrorDistortionSetting.getValue();
   }
// https://t.me/pasterplace

   public float getBlurStrength() {
      return this.blurStrengthSetting.getValue();
   }
// https://t.me/pasterplace

   public boolean isRainbowEnabled() {
      return this.rainbowEnabledSetting.getValue();
   }
// https://t.me/pasterplace

   public float getRainbowSpeed() {
      return this.rainbowSpeedSetting.getValue();
   }
// https://t.me/pasterplace

   public float getRainbowScale() {
      return this.rainbowScaleSetting.getValue();
   }
// https://t.me/pasterplace

   public float getFillOpacity() {
      return this.fillOpacitySetting.getValue();
   }
// https://t.me/pasterplace

   public boolean isShadowRetentionEnabled() {
      return this.keepShadowsSetting.getValue();
   }
// https://t.me/pasterplace

   public float getShadowStrength() {
      return this.shadowStrengthSetting.getValue();
   }
// https://t.me/pasterplace

   public float getShaderSpeed() {
      return this.shaderSpeedSetting.getValue();
   }
// https://t.me/pasterplace

   public float getShaderScale() {
      return this.shaderScaleSetting.getValue();
   }
// https://t.me/pasterplace

   public float getShaderGlow() {
      return this.shaderGlowSetting.getValue();
   }
// https://t.me/pasterplace

   public boolean isStarFieldEnabled() {
      return this.starFieldSetting.getValue();
   }
// https://t.me/pasterplace

   public float getShaderOpacity() {
      return this.shaderOpacitySetting.getValue();
   }
// https://t.me/pasterplace

   public boolean isShaderShadowPreserved() {
      return this.preserveShaderShadowsSetting.getValue();
   }
// https://t.me/pasterplace

   public float getShaderShadowStrength() {
      return this.shaderShadowStrengthSetting.getValue();
   }
// https://t.me/pasterplace

   public boolean isGlowEnabled() {
      return this.glowEnabledSetting.getValue();
   }
// https://t.me/pasterplace

   public float getBlurRadius() {
      return this.blurRadiusSetting.getValue();
   }
// https://t.me/pasterplace

   public float getGlowBrightness() {
      return this.glowBrightnessSetting.getValue();
   }
// https://t.me/pasterplace

   public float getInnerGlow() {
      return this.innerGlowSetting.getValue();
   }
// https://t.me/pasterplace

   public boolean isOutlineEnabled() {
      return this.outlineEnabledSetting.getValue();
   }
// https://t.me/pasterplace

   public float getOutlineThickness() {
      return this.outlineThicknessSetting.getValue();
   }
// https://t.me/pasterplace

   public float getEnergyTrailLength() {
      return this.energyTrailLengthSetting.getValue();
   }
// https://t.me/pasterplace

   public float getEnergyTrailBrightness() {
      return this.energyTrailBrightnessSetting.getValue();
   }
// https://t.me/pasterplace

   public float getEnergyMovement() {
      return this.energyMovementSetting.getValue();
   }
// https://t.me/pasterplace

   public float getEnergyPulse() {
      return this.energyPulseSetting.getValue();
   }
// https://t.me/pasterplace

   public float getEnergyTrailBehindCamera() {
      return this.energyTrailBehindCameraSetting.getValue();
   }
// https://t.me/pasterplace

   public float getRibbonLength() {
      return this.ribbonLengthSetting.getValue();
   }
// https://t.me/pasterplace

   public float getRibbonWidth() {
      return this.ribbonWidthSetting.getValue();
   }
// https://t.me/pasterplace

   public float getRibbonBrightness() {
      return this.ribbonBrightnessSetting.getValue();
   }
// https://t.me/pasterplace

   public float getRibbonCameraStrength() {
      return this.ribbonCameraStrengthSetting.getValue();
   }
// https://t.me/pasterplace

   public float getRibbonLift() {
      return this.ribbonLiftSetting.getValue();
   }
// https://t.me/pasterplace

   public float getRibbonCurvature() {
      return this.ribbonCurvatureSetting.getValue();
   }
// https://t.me/pasterplace

   public float getRibbonWaveSpeed() {
      return this.ribbonWaveSpeedSetting.getValue();
   }
// https://t.me/pasterplace

   public boolean areRibbonsEnabledOnHit() {
      return this.ribbonsOnHitSetting.getValue();
   }
// https://t.me/pasterplace

   public float getHitRibbonLength() {
      return this.hitRibbonLengthSetting.getValue();
   }
// https://t.me/pasterplace

   public float getHitRibbonStrength() {
      return this.hitRibbonStrengthSetting.getValue();
   }
// https://t.me/pasterplace

   public boolean areRibbonsEnabledWhileEating() {
      return this.ribbonsWhileEatingSetting.getValue();
   }
// https://t.me/pasterplace

   public float getEatRibbonLength() {
      return this.eatRibbonLengthSetting.getValue();
   }
// https://t.me/pasterplace

   public float getEatRibbonStrength() {
      return this.eatRibbonStrengthSetting.getValue();
   }
// https://t.me/pasterplace

   public float getTrailFadeSpeed() {
      return this.trailFadeSpeedSetting.getValue();
   }
// https://t.me/pasterplace

   public float getTrailLift() {
      return this.trailLiftSetting.getValue();
   }
// https://t.me/pasterplace

   public float getTrailSway() {
      return this.trailSwaySetting.getValue();
   }
// https://t.me/pasterplace

   public float getTrailCameraOffset() {
      return this.trailCameraOffsetSetting.getValue();
   }
// https://t.me/pasterplace

   public float getTrailTurbulence() {
      return this.turbulenceSetting.getValue();
   }
// https://t.me/pasterplace

   public float getTrailFlicker() {
      return this.flickerSetting.getValue();
   }
// https://t.me/pasterplace

   public boolean isBlowOffOnHitEnabled() {
      return this.blowOffOnHitSetting.getValue();
   }
// https://t.me/pasterplace

   public float getBlowOffStrength() {
      return this.blowOffStrengthSetting.getValue();
   }
}

