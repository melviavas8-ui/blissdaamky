package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventTick;
import net.minecraft.client.gui.screen.DeathScreen;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoRespawn extends ModuleStructure {
   private final BooleanSetting l1I1IlllIlI = new BooleanSetting("Координаты смерти", true);

   public AutoRespawn() {
      super("AutoRespawn", "Автоматически возрождается после смерти", ModuleCategory.PLAYER);
      this.addSetting(this.l1I1IlllIlI);
   }

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

         CLIENT.player.requestRespawn();
         CLIENT.setScreen(null);
      }
   }
}

