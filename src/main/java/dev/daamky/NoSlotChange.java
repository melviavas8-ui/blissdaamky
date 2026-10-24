package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoSlotChange extends ModuleStructure {
   private volatile boolean l1I1IlllIlI;

   public NoSlotChange() {
      super("NoSlotChange", "Игнорирует принудительную смену слота сервером", ModuleCategory.PLAYER);
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof UpdateSelectedSlotS2CPacket) {
            UpdateSelectedSlotS2CPacket packet2 = (UpdateSelectedSlotS2CPacket)packet;
            if (CLIENT.player != null) {
               if (packet2.slot() != InventoryUtils.getSelectedHotbarSlot()) {
                  eventPacket.cancel();
                  if (!this.l1I1IlllIlI) {
                     this.l1I1IlllIlI = true;
                     CLIENT.execute(() -> {
                        this.l1I1IlllIlI = false;
                        if (CLIENT.player != null && CLIENT.getNetworkHandler() != null) {
                           PacketSender.II1Il11l111II11IIl(new UpdateSelectedSlotC2SPacket(InventoryUtils.getSelectedHotbarSlot()));
                        }
                     });
                     return;
                  }

                  return;
               }

               return;
            }
         }
      }
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.l1I1IlllIlI = false;
   }
}

