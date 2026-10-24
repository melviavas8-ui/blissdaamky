package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventRender3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Tracers extends ModuleStructure {
   private final MultiToggleSetting targetSettings = new MultiToggleSetting("Цели")
      .addOption("Игроки", true)
      .addOption("Друзья", true)
      .addOption("Монстры", false)
      .addOption("Предметы", false)
      .addOption("Невидимые", true);
   private final BooleanSetting ignoreUnarmoredSetting = new BooleanSetting("Игнорировать голых", false);
   private final MultiOptionSetting colorModeSetting = new MultiOptionSetting("Цвет", "Тема", "Тема", "Дистанция");
   private final MultiOptionSetting targetPointSetting = new MultiOptionSetting("Точка", "Тело", "Ноги", "Тело", "Голова");
   private final FloatSetting lineThicknessSetting = new FloatSetting("Толщина", 1.5F, 0.5F, 4.0F, 0.1F);
   private final FloatSetting maxDistanceSetting = new FloatSetting("Дистанция", 128.0F, 16.0F, 256.0F, 4.0F, " бл");
   private final BooleanSetting suppressCameraShakeSetting = new BooleanSetting("Без покачивания камеры", true);

   public static boolean isCameraShakeSuppressed() {
      Tracers moduleStructure = (Tracers)ModuleRegistry.getInstance().getModuleByClass(Tracers.class);
      return moduleStructure != null && moduleStructure.isEnabled() && moduleStructure.suppressCameraShakeSetting.getValue();
   }

   public Tracers() {
      super("Tracers", "Рисует линии к сущностям", ModuleCategory.RENDER);
      this.addSettings(
         this.targetSettings, this.ignoreUnarmoredSetting, this.colorModeSetting, this.targetPointSetting, this.lineThicknessSetting, this.maxDistanceSetting, this.suppressCameraShakeSetting
      );
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onRender3D(EventRender3D eventRender3D) {
      if (CLIENT.world != null && CLIENT.player != null) {
         double maxDistance = this.maxDistanceSetting.getValue().floatValue();

         for (Entity entity : CLIENT.world.getEntities()) {
            double distance;
            if (entity != CLIENT.player && entity.isAlive() && !((distance = entity.distanceTo(CLIENT.player)) > maxDistance)) {
               RgbaColor l1I1IlllIlI4 = this.getEntityColor((Entity)entity, distance / maxDistance);
               if (l1I1IlllIlI4 != null) {
                  Vec3d entityPosition = DaamkyWorldRender.getInterpolatedPosition(entity);

                  double anchorHeight = switch ((String)this.targetPointSetting.getValue()) {
                     case "Ноги" -> 0.0;
                     case "Голова" -> entity.getHeight();
                     default -> entity.getHeight() * 0.5;
                  };
                  Vec3d anchorPosition = entityPosition.add(0.0, anchorHeight, 0.0);
                  DaamkyWorldRender.drawCameraToPointGradientLine(anchorPosition, l1I1IlllIlI4.withAlpha(60), l1I1IlllIlI4, this.lineThicknessSetting.getValue());
                  DaamkyWorldRender.drawBloomRing(anchorPosition, 0.18F, l1I1IlllIlI4.withAlpha(180), true);
               }
            }
         }
      }
   }

   private RgbaColor getEntityColor(Entity entity, double normalizedDistance) {
      if (entity instanceof PlayerEntity playerEntity) {
         if (playerEntity.isSpectator()) {
            return null;
         } else if (playerEntity.isInvisible() && !this.targetSettings.isOptionEnabled("Невидимые")) {
            return null;
         } else if (FriendManager.getInstance().isFriend(playerEntity)) {
            return this.targetSettings.isOptionEnabled("Друзья") ? RgbaColor.rgb(80, 255, 140).withAlpha(230) : null;
         } else if (!this.targetSettings.isOptionEnabled("Игроки")) {
            return null;
         } else {
            return this.ignoreUnarmoredSetting.getValue() && this.isUnarmored(playerEntity) ? null : this.getDistanceColor(entity, normalizedDistance);
         }
      } else if (entity instanceof HostileEntity) {
         return this.targetSettings.isOptionEnabled("Монстры") ? RgbaColor.rgb(255, 85, 85).withAlpha(220) : null;
      } else if (entity instanceof ItemEntity) {
         return this.targetSettings.isOptionEnabled("Предметы") ? RgbaColor.rgb(255, 215, 70).withAlpha(200) : null;
      } else {
         return null;
      }
   }

   private RgbaColor getDistanceColor(Entity entity, double normalizedDistance) {
      return this.colorModeSetting.isSelected("Дистанция")
         ? AnimatedColorUtils.getHealthColor((float)normalizedDistance, 230)
         : AnimatedColorUtils.getAnimatedAccentColor(entity.getId() * 0.4F, 230);
   }

   private boolean isUnarmored(PlayerEntity playerEntity) {
      for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
         if (!playerEntity.getEquippedStack(equipmentSlot).isEmpty()) {
            return false;
         }
      }

      return true;
   }
}

