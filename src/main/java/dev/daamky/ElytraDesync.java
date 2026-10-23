package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ElytraDesync extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting mode = new MultiOptionSetting("Режим", "Реал", "Реал", "Пакет");
   private final FloatSetting yawStrength = new FloatSetting("Сила yaw", 25.0F, 5.0F, 60.0F, 1.0F);
   private final FloatSetting swaySpeed = new FloatSetting("Скорость виляния", 0.35F, 0.05F, 1.0F, 0.05F);
   private final BooleanSetting modifyPitch = new BooleanSetting("Вилять питчем", false);
   private final FloatSetting pitchStrength = new FloatSetting("Сила питч", 8.0F, 1.0F, 25.0F, 1.0F);
   private final FloatSetting minimumSpeed = new FloatSetting("Мин. скорость", 15.0F, 0.0F, 40.0F, 1.0F);
   private final BooleanSetting onlyWhenPlayerNearby = new BooleanSetting("Только если враг рядом", true);
   private final FloatSetting nearbyPlayerRange = new FloatSetting("Радиус врага", 30.0F, 5.0F, 64.0F, 1.0F);
   private double oscillationPhase;
   private float lastAppliedYawOffset;
   private float lastAppliedPitchOffset;
   private float lastPacketYaw = Float.NaN;
   private float lastPacketPitch = Float.NaN;
// https://t.me/pasterplace

   public ElytraDesync() {
      super("ElytraDesync", "Ломает перегон врага на элитре", ModuleCategory.MOVEMENT);
      this.pitchStrength.visibleWhen(this.modifyPitch::getValue);
      this.nearbyPlayerRange.visibleWhen(this.onlyWhenPlayerNearby::getValue);
      this.addSettings(
         this.mode, this.yawStrength, this.swaySpeed, this.modifyPitch, this.pitchStrength, this.minimumSpeed, this.onlyWhenPlayerNearby, this.nearbyPlayerRange
      );
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.resetOscillationState();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.restorePlayerRotation();
      this.resetOscillationState();
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.resetOscillationState();
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (this.mode.isSelected("Реал") && this.canApplyDesync()) {
            this.oscillationPhase = this.oscillationPhase + this.swaySpeed.getValue().floatValue();
            float yawOscillation = (float)(this.yawStrength.getValue().floatValue() * Math.sin(this.oscillationPhase));
            float pitchOscillation = !this.modifyPitch.getValue()
               ? 0.0F
               : (float)(this.pitchStrength.getValue().floatValue() * Math.sin(this.oscillationPhase + (Math.PI / 2)));
            float baseYaw = CLIENT.player.getYaw() - this.lastAppliedYawOffset;
            float previousPitchOffset;
            float adjustedPitch = MathHelper.clamp((previousPitchOffset = CLIENT.player.getPitch() - this.lastAppliedPitchOffset) + pitchOscillation, -90.0F, 90.0F);
            CLIENT.player.setYaw(baseYaw + yawOscillation);
            CLIENT.player.setPitch(adjustedPitch);
            this.lastAppliedYawOffset = yawOscillation;
            this.lastAppliedPitchOffset = adjustedPitch - previousPitchOffset;
         } else {
            this.restorePlayerRotation();
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onMotion(EventMotion eventMotion) {
      if (eventMotion.isPre() && this.mode.isSelected("Пакет") && this.hasActiveClientWorld() && this.canApplyDesync()) {
         this.oscillationPhase = this.oscillationPhase + this.swaySpeed.getValue().floatValue();
         float adjustedYaw;
         if ((adjustedYaw = eventMotion.yaw() + (float)(this.yawStrength.getValue().floatValue() * Math.sin(this.oscillationPhase))) == this.lastPacketYaw) {
            adjustedYaw += 1.0E-4F;
         }

         eventMotion.setYaw(adjustedYaw);
         this.lastPacketYaw = adjustedYaw;
         if (this.modifyPitch.getValue()) {
            float adjustedPitch;
            if ((
                  adjustedPitch = MathHelper.clamp(
                     eventMotion.pitch() + (float)(this.pitchStrength.getValue().floatValue() * Math.sin(this.oscillationPhase + (Math.PI / 2))), -90.0F, 90.0F
                  )
               )
               == this.lastPacketPitch) {
               adjustedPitch += 1.0E-4F;
            }
// https://t.me/pasterplace

            eventMotion.setPitch(adjustedPitch);
            this.lastPacketPitch = adjustedPitch;
         }
      }
   }
// https://t.me/pasterplace

   private boolean canApplyDesync() {
      if (CLIENT.player.isGliding() && !(MovementUtils.getHorizontalSpeed() * 20.0 < this.minimumSpeed.getValue().floatValue())) {
         if (!this.onlyWhenPlayerNearby.getValue()) {
            return true;
         }

         double nearbyPlayerRange = this.nearbyPlayerRange.getValue().floatValue();

          for (PlayerEntity nearbyPlayer : CLIENT.world.getPlayers()) {
            if (nearbyPlayer != CLIENT.player && nearbyPlayer.isAlive() && CLIENT.player.distanceTo((Entity)nearbyPlayer) <= nearbyPlayerRange) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   private void restorePlayerRotation() {
      if (CLIENT.player != null && (this.lastAppliedYawOffset != 0.0F || this.lastAppliedPitchOffset != 0.0F)) {
         CLIENT.player.setYaw(CLIENT.player.getYaw() - this.lastAppliedYawOffset);
         CLIENT.player.setPitch(MathHelper.clamp(CLIENT.player.getPitch() - this.lastAppliedPitchOffset, -90.0F, 90.0F));
      }

      this.lastAppliedYawOffset = 0.0F;
      this.lastAppliedPitchOffset = 0.0F;
   }
// https://t.me/pasterplace

   private void resetOscillationState() {
      this.oscillationPhase = 0.0;
      this.lastAppliedYawOffset = 0.0F;
      this.lastAppliedPitchOffset = 0.0F;
      this.lastPacketYaw = Float.NaN;
      this.lastPacketPitch = Float.NaN;
   }
}

