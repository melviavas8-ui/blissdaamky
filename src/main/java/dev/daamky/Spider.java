package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Spider extends ModuleStructure {
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "Ванилла", "Ванилла", "Matrix");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Скорость", 0.2F, 0.05F, 0.6F, 0.01F);
   private int l1lll11l1l;

   public Spider() {
      super("Spider", "Позволяет взбираться по стенам", ModuleCategory.MOVEMENT);
      this.IllI1l1l1.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Ванилла"));
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && CLIENT.player.horizontalCollision) {
         Vec3d playerVelocity = CLIENT.player.getVelocity();
         if (!this.l1I1IlllIlI.isSelected("Ванилла")) {
            if (this.l1lll11l1l++ - 3 == 0) {
               CLIENT.player.setVelocity(playerVelocity.x, 0.42, playerVelocity.z);
            }
         } else {
            CLIENT.player.setVelocity(playerVelocity.x, this.IllI1l1l1.getValue().floatValue(), playerVelocity.z);
         }

         CLIENT.player.fallDistance = 0.0;
      } else {
         this.l1lll11l1l = 0;
      }
   }
}

