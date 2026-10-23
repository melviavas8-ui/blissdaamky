package dev.daamky;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;

public final class PlayerMovementPacketFactory {
    private static final String pdaad9501 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private PlayerMovementPacketFactory() {
   }
// https://t.me/pasterplace

   public static PlayerMoveC2SPacket II1Il11l111II11IIl(PlayerMoveC2SPacket playerMoveC2SPacket, boolean onGround) {
      return l1I1IlllIlI(playerMoveC2SPacket, 0.0, onGround);
   }
// https://t.me/pasterplace

   public static PlayerMoveC2SPacket II1Il11l111II11IIl(PlayerMoveC2SPacket playerMoveC2SPacket, double verticalOffset, boolean onGround) {
      return l1I1IlllIlI(playerMoveC2SPacket, verticalOffset, onGround);
   }
// https://t.me/pasterplace

   public static PlayerMoveC2SPacket II1Il11l111II11IIl(PlayerMoveC2SPacket playerMoveC2SPacket) {
      return (PlayerMoveC2SPacket)(playerMoveC2SPacket.changesLook()
         ? new LookAndOnGround(
            playerMoveC2SPacket.getYaw(0.0F),
            playerMoveC2SPacket.getPitch(0.0F),
            playerMoveC2SPacket.isOnGround(),
            playerMoveC2SPacket.horizontalCollision()
         )
         : new OnGroundOnly(playerMoveC2SPacket.isOnGround(), playerMoveC2SPacket.horizontalCollision()));
   }
// https://t.me/pasterplace

   private static PlayerMoveC2SPacket l1I1IlllIlI(PlayerMoveC2SPacket playerMoveC2SPacket, double verticalOffset, boolean onGround) {
      boolean horizontalCollision = playerMoveC2SPacket.horizontalCollision();
      if (playerMoveC2SPacket.changesPosition() && playerMoveC2SPacket.changesLook()) {
         return new Full(
            playerMoveC2SPacket.getX(0.0),
            playerMoveC2SPacket.getY(0.0) + verticalOffset,
            playerMoveC2SPacket.getZ(0.0),
            playerMoveC2SPacket.getYaw(0.0F),
            playerMoveC2SPacket.getPitch(0.0F),
            onGround,
            horizontalCollision
         );
      } else if (playerMoveC2SPacket.changesPosition()) {
         return new PositionAndOnGround(
            playerMoveC2SPacket.getX(0.0), playerMoveC2SPacket.getY(0.0) + verticalOffset, playerMoveC2SPacket.getZ(0.0), onGround, horizontalCollision
         );
      } else {
         return (PlayerMoveC2SPacket)(playerMoveC2SPacket.changesLook()
            ? new LookAndOnGround(playerMoveC2SPacket.getYaw(0.0F), playerMoveC2SPacket.getPitch(0.0F), onGround, horizontalCollision)
            : new OnGroundOnly(onGround, horizontalCollision));
      }
   }
}

