package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Flight extends ModuleStructure {
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "Ванилла", "Ванилла", "Планирование", "Креатив");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Скорость", 1.0F, 0.1F, 5.0F, 0.05F);
   private final FloatSetting l1lll11l1l = new FloatSetting("Вертикальная скорость", 0.6F, 0.1F, 3.0F, 0.05F);
   private final FloatSetting lIIl11l111lIIl1ll = new FloatSetting("Падение", 0.04F, 0.0F, 0.3F, 0.005F);
   private boolean l1IIl11lI;

   public Flight() {
      super("Flight", "Позволяет летать", ModuleCategory.MOVEMENT);
      this.l1lll11l1l.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Ванилла"));
      this.lIIl11l111lIIl1ll.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Планирование"));
      this.IllI1l1l1.visibleWhen(() -> !this.l1I1IlllIlI.isSelected("Креатив"));
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1, this.l1lll11l1l, this.lIIl11l111lIIl1ll);
   }

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      if (CLIENT.player != null) {
         this.l1IIl11lI = CLIENT.player.getAbilities().flying;
      }
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      if (CLIENT.player != null
         && this.l1I1IlllIlI.isSelected("Креатив")
         && !CLIENT.player.isCreative()
         && !CLIENT.player.isSpectator()) {
         CLIENT.player.getAbilities().flying = this.l1IIl11lI;
         CLIENT.player.getAbilities().allowFlying = false;
      }
   }

   @EventTarget
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         switch ((String)this.l1I1IlllIlI.getValue()) {
            case "Ванилла":
               double verticalVelocity = 0.0;
               if (CLIENT.options.jumpKey.isPressed()) {
                  verticalVelocity = 0.0 + this.l1lll11l1l.getValue().floatValue();
               }

               if (CLIENT.options.sneakKey.isPressed()) {
                  verticalVelocity -= this.l1lll11l1l.getValue().floatValue();
               }

               CLIENT.player.setVelocity(0.0, verticalVelocity, 0.0);
               MovementUtils.setHorizontalSpeed(this.IllI1l1l1.getValue().floatValue());
               CLIENT.player.fallDistance = 0.0;
               break;
            case "Планирование":
               Vec3d playerVelocity = CLIENT.player.getVelocity();
               if (!CLIENT.player.isOnGround() && playerVelocity.y < 0.0) {
                  CLIENT.player.setVelocity(playerVelocity.x, -this.lIIl11l111lIIl1ll.getValue(), playerVelocity.z);
                  if (MovementUtils.isMoving()) {
                     MovementUtils.setHorizontalSpeed(Math.max(MovementUtils.getHorizontalSpeed(), this.IllI1l1l1.getValue().floatValue() * 0.3));
                  }
               }
               break;
            case "Креатив":
               CLIENT.player.getAbilities().allowFlying = true;
               CLIENT.player.getAbilities().flying = true;
         }
      }
   }
}

