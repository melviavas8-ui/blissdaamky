package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Spider extends ModuleStructure {
    private static final String pb10c6713 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Режим", "Ванилла", "Ванилла", "Matrix");
   private final FloatSetting IllI1l1l1 = new FloatSetting("Скорость", 0.2F, 0.05F, 0.6F, 0.01F);
   private int l1lll11l1l;
// https://t.me/pasterplace

   public Spider() {
      super("Spider", "Позволяет взбираться по стенам", ModuleCategory.MOVEMENT);
      this.IllI1l1l1.visibleWhen(() -> this.l1I1IlllIlI.isSelected("Ванилла"));
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

         CLIENT.player.fallDistance = 0.0;
      } else {
         this.l1lll11l1l = 0;
      }
   }
}

