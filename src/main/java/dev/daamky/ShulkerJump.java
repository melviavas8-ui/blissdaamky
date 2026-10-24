package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ShulkerJump extends ModuleStructure {
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Сила", 2.33F, 0.5F, 4.0F, 0.01F);

   public ShulkerJump() {
      super("ShulkerJump", "Подбрасывает вверх при открытии шалкера рядом", ModuleCategory.MOVEMENT);
      this.addSetting(this.l1I1IlllIlI);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && CLIENT.currentScreen instanceof ShulkerBoxScreen && this.l1I1IIl111Il1l1l()) {
         Vec3d playerVelocity = CLIENT.player.getVelocity();
         CLIENT.player.setVelocity(playerVelocity.x, playerVelocity.y + this.l1I1IlllIlI.getValue().floatValue(), playerVelocity.z);
      }
   }

   private boolean l1I1IIl111Il1l1l() {
      BlockPos playerBlockPos = CLIENT.player.getBlockPos();

      for (Object iteratedBlockPos : BlockPos.iterate(playerBlockPos.add(-1, -1, -1), playerBlockPos.add(1, 1, 1))) {
         if (CLIENT.world.getBlockState((BlockPos)iteratedBlockPos).getBlock() instanceof ShulkerBoxBlock) {
            return true;
         }
      }

      return false;
   }
}

