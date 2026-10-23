package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoSlotChange extends ModuleStructure {
    private static final String pd36b49 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private volatile boolean l1I1IlllIlI;
// https://t.me/pasterplace

   public NoSlotChange() {
      super("NoSlotChange", "Игнорирует принудительную смену слота сервером", ModuleCategory.PLAYER);
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

                  return;
               }

               return;
            }
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.l1I1IlllIlI = false;
   }
}

