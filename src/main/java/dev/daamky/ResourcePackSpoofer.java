package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ResourcePackSpoofer extends ModuleStructure {
    private static final String p4459fb49 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Mode", "Accept", "Accept", "Decline");
// https://t.me/pasterplace

   public ResourcePackSpoofer() {
      super("ResourcePackSpoofer", "Prevents server resource pack from downloading", ModuleCategory.WORLD);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.l1I1IlllIlI.getValue();
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof ResourcePackSendS2CPacket) {
            ResourcePackSendS2CPacket packet2 = (ResourcePackSendS2CPacket)packet;
            if (CLIENT.getNetworkHandler() != null) {
               eventPacket.cancel();
               if (!this.l1I1IlllIlI.isSelected("Decline") && !this.l1I1IlllIlI.isSelected("Отклонить")) {
                  CLIENT.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(packet2.id(), Status.ACCEPTED));
                  CLIENT.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(packet2.id(), Status.DOWNLOADED));
                  CLIENT.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(packet2.id(), Status.SUCCESSFULLY_LOADED));
                  return;
               }
// https://t.me/pasterplace

               CLIENT.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(packet2.id(), Status.DECLINED));
               return;
            }
         }
      }
   }
}

