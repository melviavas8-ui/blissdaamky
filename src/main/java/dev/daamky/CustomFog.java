package dev.daamky;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import org.joml.Vector4f;

public final class CustomFog extends ModuleStructure {
   private final MultiOptionSetting mode = new MultiOptionSetting("Режим", "Обычный", "Обычный", "Блюр", "Блюр + цвет");
   private final FloatSetting colorStrength = new FloatSetting("Сила цвета", 0.6F, 0.05F, 1.0F, 0.05F);
   private final BooleanSetting changeFogDistance = new BooleanSetting("Менять дистанцию", true);
   private final FloatSetting fogStartDistance = new FloatSetting("Начало тумана", 10.0F, 0.0F, 256.0F, 1.0F);
   private final FloatSetting fogEndDistance = new FloatSetting("Конец тумана", 64.0F, 1.0F, 512.0F, 1.0F);
   private final BooleanSetting changeFogColor = new BooleanSetting("Менять цвет", true);
   private final MultiOptionSetting fogColorSource = new MultiOptionSetting("Цвет", "Тема", "Тема", "Свой");
   private final ColorSetting customFogColor = new ColorSetting("Свой цвет", 16777215);

   public CustomFog() {
      super("CustomFog", "Изменяет цвет и дистанцию тумана", ModuleCategory.RENDER);
      this.changeFogDistance.visibleWhen(() -> this.mode.isSelected("Обычный"));
      this.fogStartDistance.visibleWhen(() -> !this.mode.isSelected("Обычный") || this.changeFogDistance.getValue());
      this.fogEndDistance.visibleWhen(() -> !this.mode.isSelected("Обычный") || this.changeFogDistance.getValue());
      this.changeFogColor.visibleWhen(() -> this.mode.isSelected("Обычный"));
      this.colorStrength.visibleWhen(() -> this.mode.isSelected("Блюр + цвет"));
      this.fogColorSource.visibleWhen(this::usesCustomFogSettings);
      this.customFogColor.visibleWhen(() -> this.usesCustomFogSettings() && this.fogColorSource.isSelected("Свой"));
      this.addSettings(
         this.mode,
         this.changeFogDistance,
         this.fogStartDistance,
         this.fogEndDistance,
         this.changeFogColor,
         this.colorStrength,
         this.fogColorSource,
         this.customFogColor
      );
   }

   private boolean usesCustomFogSettings() {
      return this.mode.isSelected("Блюр + цвет") || this.mode.isSelected("Обычный") && this.changeFogColor.getValue();
   }

   public float getFogModeValue() {
      return switch ((String)this.mode.getValue()) {
         case "Блюр" -> 1.0F;
         case "Блюр + цвет" -> 2.0F;
         default -> 0.0F;
      };
   }

   public float getFogStartDistance() {
      return Math.min(this.fogStartDistance.getValue(), this.fogEndDistance.getValue() - 1.0F);
   }

   public float getFogEndDistance() {
      return this.fogEndDistance.getValue();
   }

   public float[] getFogColorWithStrength() {
      float[] fogRgb = this.getFogColorRgb();
      return new float[]{fogRgb[0], fogRgb[1], fogRgb[2], this.colorStrength.getValue()};
   }

   private float[] getFogColorRgb() {
      if (this.fogColorSource.isSelected("Тема")) {
         RgbaColor l1I1IlllIlI4 = Accent.getAccentColorWithAlpha(255);
         return new float[]{l1I1IlllIlI4.red() / 255.0F, l1I1IlllIlI4.green() / 255.0F, l1I1IlllIlI4.blue() / 255.0F};
      } else {
         return new float[]{this.customFogColor.getRed(), this.customFogColor.getGreen(), this.customFogColor.getBlue()};
      }
   }

   public static CustomFog getInstance() {
      return ModuleRegistry.getInstance().getModuleByClass(CustomFog.class);
   }

   public static void applyFog(FogData fogData, Vector4f vector4f, Camera camera) {
      boolean hideFog = NoRender.II1Il11l111II11IIl("Туман");
      CustomFog customFog = getInstance();
      boolean customFogEnabled = customFog != null && customFog.isEnabled() && customFog.mode.isSelected("Обычный");
      if (hideFog || customFogEnabled) {
         if (camera.getSubmersionType() == CameraSubmersionType.NONE) {
            Entity entity = camera.getFocusedEntity();
            if (entity instanceof LivingEntity) {
                LivingEntity entity2 = (LivingEntity)entity;
               if (entity2.hasStatusEffect(StatusEffects.BLINDNESS) || entity2.hasStatusEffect(StatusEffects.DARKNESS)) {
                  return;
               }
            }

            if (customFogEnabled && customFog.changeFogDistance.getValue()) {
               float fogEndDistance = customFog.fogEndDistance.getValue();
               float fogStartDistance = Math.min(customFog.fogStartDistance.getValue(), fogEndDistance - 1.0F);
               fogData.environmentalStart = fogStartDistance;
               fogData.environmentalEnd = fogEndDistance;
               fogData.renderDistanceStart = Math.min(fogData.renderDistanceStart, fogStartDistance);
               fogData.renderDistanceEnd = Math.min(fogData.renderDistanceEnd, fogEndDistance);
               fogData.skyEnd = Math.min(fogData.skyEnd, fogEndDistance);
               fogData.cloudEnd = Math.min(fogData.cloudEnd, fogEndDistance);
            } else if (hideFog) {
               fogData.environmentalStart = 1.0E7F;
               fogData.environmentalEnd = 2.0E7F;
            }

            if (customFogEnabled && customFog.changeFogColor.getValue()) {
               float blue;
               float red;
               float green;
               if (customFog.fogColorSource.isSelected("Тема")) {
                  RgbaColor l1I1IlllIlI4 = Accent.getAccentColorWithAlpha(255);
                  red = l1I1IlllIlI4.red() / 255.0F;
                  green = l1I1IlllIlI4.green() / 255.0F;
                  blue = l1I1IlllIlI4.blue() / 255.0F;
               } else {
                  red = customFog.customFogColor.getRed();
                  green = customFog.customFogColor.getGreen();
                  blue = customFog.customFogColor.getBlue();
               }

               vector4f.set(red, green, blue, 1.0F);
            }
         }
      }
   }
}

