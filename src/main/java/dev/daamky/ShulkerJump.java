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
    private static final String p30bd15e = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting l1I1IlllIlI = new FloatSetting("Сила", 2.33F, 0.5F, 4.0F, 0.01F);
// https://t.me/pasterplace

   public ShulkerJump() {
      super("ShulkerJump", "Подбрасывает вверх при открытии шалкера рядом", ModuleCategory.MOVEMENT);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && CLIENT.currentScreen instanceof ShulkerBoxScreen && this.l1I1IIl111Il1l1l()) {
         Vec3d playerVelocity = CLIENT.player.getVelocity();
         CLIENT.player.setVelocity(playerVelocity.x, playerVelocity.y + this.l1I1IlllIlI.getValue().floatValue(), playerVelocity.z);
      }
   }
// https://t.me/pasterplace

   private boolean l1I1IIl111Il1l1l() {
      BlockPos playerBlockPos = CLIENT.player.getBlockPos();
// https://t.me/pasterplace

      for (Object iteratedBlockPos : BlockPos.iterate(playerBlockPos.add(-1, -1, -1), playerBlockPos.add(1, 1, 1))) {
         if (CLIENT.world.getBlockState((BlockPos)iteratedBlockPos).getBlock() instanceof ShulkerBoxBlock) {
            return true;
         }
      }

      return false;
   }
}

