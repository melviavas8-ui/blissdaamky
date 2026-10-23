package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import java.util.EnumSet;
import net.minecraft.entity.EntityPosition;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoRotate extends ModuleStructure {
    private static final String p50f9c45c = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public NoRotate() {
      super("NoRotate", "Не даёт серверу поворачивать камеру", ModuleCategory.PLAYER);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void II1Il11l111II11IIl(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet2 = (PlayerPositionLookS2CPacket)packet;
            if (packet2.relatives().contains(PositionFlag.X_ROT)
               && packet2.relatives().contains(PositionFlag.Y_ROT)
               && packet2.change().yaw() == 0.0F
               && packet2.change().pitch() == 0.0F) {
               return;
            }
// https://t.me/pasterplace

            eventPacket.cancel();
            EntityPosition entityPosition = packet2.change();
            EnumSet enumSet = EnumSet.noneOf(PositionFlag.class);
            enumSet.addAll(packet2.relatives());
            enumSet.add(PositionFlag.X_ROT);
            enumSet.add(PositionFlag.Y_ROT);
            PlayerPositionLookS2CPacket playerPositionLookS2CPacket = new PlayerPositionLookS2CPacket(
               packet2.teleportId(), new EntityPosition(entityPosition.position(), entityPosition.deltaMovement(), 0.0F, 0.0F), enumSet
            );
            CLIENT.execute(() -> {
               if (CLIENT.getNetworkHandler() != null) {
                  CLIENT.getNetworkHandler().onPlayerPositionLook(playerPositionLookS2CPacket);
               }
            });
            return;
         }
      }
   }
}

