package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventTick;
import net.minecraft.client.gui.screen.DeathScreen;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoRespawn extends ModuleStructure {
    private static final String pb821403f = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final BooleanSetting l1I1IlllIlI = new BooleanSetting("Координаты смерти", true);
// https://t.me/pasterplace

   public AutoRespawn() {
      super("AutoRespawn", "Автоматически возрождается после смерти", ModuleCategory.PLAYER);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventTick eventTick) {
      if (CLIENT.player != null && CLIENT.currentScreen instanceof DeathScreen) {
         if (this.l1I1IlllIlI.getValue()) {
            ClientChatLogger.II1Il11l111II11IIl(
               String.format(
                  "Смерть: %d %d %d",
                  CLIENT.player.getBlockX(),
                  CLIENT.player.getBlockY(),
                  CLIENT.player.getBlockZ()
               )
            );
         }
// https://t.me/pasterplace

         CLIENT.player.requestRespawn();
         CLIENT.setScreen(null);
      }
   }
}

