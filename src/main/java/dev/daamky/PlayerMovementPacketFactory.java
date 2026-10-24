package dev.daamky;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;

public final class PlayerMovementPacketFactory {
   private PlayerMovementPacketFactory() {
   }

   public static PlayerMoveC2SPacket II1Il11l111II11IIl(PlayerMoveC2SPacket playerMoveC2SPacket, boolean onGround) {
      return l1I1IlllIlI(playerMoveC2SPacket, 0.0, onGround);
   }

   public static PlayerMoveC2SPacket II1Il11l111II11IIl(PlayerMoveC2SPacket playerMoveC2SPacket, double verticalOffset, boolean onGround) {
      return l1I1IlllIlI(playerMoveC2SPacket, verticalOffset, onGround);
   }

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

