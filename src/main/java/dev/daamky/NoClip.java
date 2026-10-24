package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoClip extends ModuleStructure {
   public NoClip() {
      super("NoClip", "Клипает через блоки по одному блоку в направлении взгляда", ModuleCategory.MOVEMENT);
   }

   public static boolean l1I1IIl111Il1l1l() {
      ModuleStructure moduleStructure = ModuleRegistry.getInstance().getModuleByClass(NoClip.class);
      return moduleStructure != null && moduleStructure.isEnabled();
   }

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

