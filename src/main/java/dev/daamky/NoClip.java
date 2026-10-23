package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoClip extends ModuleStructure {
    private static final String pc36eb2c7 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public NoClip() {
      super("NoClip", "Клипает через блоки по одному блоку в направлении взгляда", ModuleCategory.MOVEMENT);
   }
// https://t.me/pasterplace

   public static boolean l1I1IIl111Il1l1l() {
      ModuleStructure moduleStructure = ModuleRegistry.getInstance().getModuleByClass(NoClip.class);
      return moduleStructure != null && moduleStructure.isEnabled();
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()
         && CLIENT.player.isAlive()
         && !CLIENT.player.hasVehicle()
         && CLIENT.player.getAbilities().flying) {
         Vec3d lookDirection = CLIENT.player.getRotationVector().normalize();
         Box box = CLIENT.player.getBoundingBox().contract(0.001).stretch(lookDirection.multiply(0.15));
         if (CLIENT.world.getBlockCollisions(CLIENT.player, box).iterator().hasNext()) {
            Vec3d nextPosition = CLIENT.player.getEntityPos().add(lookDirection);
            CLIENT.player.setPosition(nextPosition.x, nextPosition.y, nextPosition.z);
         }
      }
   }
}

