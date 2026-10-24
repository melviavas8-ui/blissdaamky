package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;

public final class TeleportController {
   private static final TeleportController INSTANCE = new TeleportController();
   private static final double MAX_VERTICAL_CLIP_DISTANCE = 190.0;
   private static final double SURFACE_CLEARANCE = 2.0;
   private static final double MAX_HORIZONTAL_HOP_DISTANCE = 48.0;
   private static final int CHUNK_WAIT_TICKS = 3;
   private static final int INITIAL_HOP_DELAY_TICKS = 1;
   private volatile TeleportController.TeleportState teleportState = TeleportController.TeleportState.IDLE;
   private volatile boolean rollbackDetected;
   private Vec3d currentPosition;
   private Vec3d targetPosition;
   private double bedrockLevelY;
   private boolean returnToSurface;
   private int delayTicks;

   private TeleportController() {
   }

   public static TeleportController getInstance() {
      return INSTANCE;
   }

   public boolean isTeleporting() {
      return this.teleportState != TeleportController.TeleportState.IDLE;
   }

   public void cancelTeleport() {
      boolean wasTeleporting = this.isTeleporting();
      this.teleportState = TeleportController.TeleportState.IDLE;
      if (wasTeleporting) {
         ClientChatLogger.II1Il11l111II11IIl("Телепорт остановлен.");
      }
   }

   public void startTeleport(ClientPlayerEntity player, ClientWorld world, double targetX, double targetY, double targetZ) {
      this.startTeleport(player, world, targetX, targetY, targetZ, true);
   }

   public void startTeleport(ClientPlayerEntity player, ClientWorld world, double targetX, double targetY, double targetZ, boolean returnToSurface) {
      if (!player.getAbilities().allowFlying) {
         ClientChatLogger.l1I1IlllIlI("Нужен /fly.");
      } else {
         double bedrockY = world.getBottomY() - 2.0;
         if (!(player.getY() - bedrockY > 190.0) && (!returnToSurface || !(targetY - bedrockY > 190.0))) {
            if (!player.getAbilities().flying) {
               player.getAbilities().flying = true;
               player.sendAbilitiesUpdate();
            }

            this.currentPosition = player.getEntityPos();
            this.targetPosition = new Vec3d(targetX, targetY, targetZ);
            this.bedrockLevelY = bedrockY;
            this.returnToSurface = returnToSurface;
            this.rollbackDetected = false;
            this.delayTicks = 0;
            this.teleportState = TeleportController.TeleportState.DESCENDING_TO_BEDROCK;
            TeleportLog.II1Il11l111II11IIl(
               "start from %.1f %.1f %.1f to %.1f %.1f %.1f surface=%s",
               this.currentPosition.x,
               this.currentPosition.y,
               this.currentPosition.z,
               targetX,
               targetY,
               targetZ,
               returnToSurface
            );
         } else {
            ClientChatLogger.l1I1IlllIlI(String.format(Locale.ROOT, "Слишком высоко: клип под бедрок работает до Y=%.0f.", bedrockY + 190.0));
         }
      }
   }

   @EventTarget
   public void onTick(EventTick eventTick) {
      if (this.rollbackDetected) {
         this.rollbackDetected = false;
         ClientChatLogger.l1I1IlllIlI("Сервер откатил телепорт.");
      }

      if (this.teleportState != TeleportController.TeleportState.IDLE) {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientPlayerEntity clientPlayerEntity = client.player;
         ClientWorld clientWorld = client.world;
         if (clientPlayerEntity != null && clientWorld != null) {
            if (this.delayTicks > 0) {
               this.delayTicks--;
            } else {
               switch (this.teleportState) {
                  case DESCENDING_TO_BEDROCK:
                     this.hopTo(
                        new Vec3d(this.currentPosition.x, this.bedrockLevelY, this.currentPosition.z), TeleportController.TeleportState.TRAVELING_UNDER_BEDROCK, 1
                     );
                     break;
                  case TRAVELING_UNDER_BEDROCK:
                     this.advanceUnderBedrock(clientWorld);
                     break;
                  case ASCENDING_TO_TARGET:
                     this.moveToSafeTargetHeight(clientPlayerEntity, clientWorld);
               }
            }

            this.holdPlayerAtCurrentPosition(clientPlayerEntity);
         } else {
            this.teleportState = TeleportController.TeleportState.IDLE;
         }
      }
   }

   private void holdPlayerAtCurrentPosition(ClientPlayerEntity clientPlayerEntity) {
      clientPlayerEntity.setPosition(this.currentPosition);
      clientPlayerEntity.setVelocity(Vec3d.ZERO);
      clientPlayerEntity.fallDistance = 0.0;
   }

   @EventTarget
   public void onWorldChange(EventWorldChange eventWorldChange) {
      if (this.teleportState != TeleportController.TeleportState.IDLE) {
         TeleportLog.II1Il11l111II11IIl("world change during %s", this.teleportState);
      }

      this.teleportState = TeleportController.TeleportState.IDLE;
      this.rollbackDetected = false;
   }

   @EventTarget
   public void onPacket(EventPacket eventPacket) {
      if (this.teleportState != TeleportController.TeleportState.IDLE) {
         if (eventPacket.isReceive()) {
            Packet packet = eventPacket.packet();
            if (packet instanceof PlayerPositionLookS2CPacket) {
               PlayerPositionLookS2CPacket positionLookPacket = (PlayerPositionLookS2CPacket)packet;
               Vec3d serverPosition = positionLookPacket.change().position();
               TeleportLog.II1Il11l111II11IIl(
                  "rollback during %s ours %.2f %.2f %.2f server %.2f %.2f %.2f",
                  this.teleportState,
                  this.currentPosition.x,
                  this.currentPosition.y,
                  this.currentPosition.z,
                  serverPosition.x,
                  serverPosition.y,
                  serverPosition.z
               );
               this.teleportState = TeleportController.TeleportState.IDLE;
               this.rollbackDetected = true;
            }
         }
      }
   }

   private void advanceUnderBedrock(ClientWorld clientWorld) {
      double deltaX = this.targetPosition.x - this.currentPosition.x;
      double deltaZ = this.targetPosition.z - this.currentPosition.z;
      double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      if (horizontalDistance < 0.001) {
         if (!this.returnToSurface) {
            TeleportLog.II1Il11l111II11IIl("done under bedrock %.2f %.2f %.2f", this.currentPosition.x, this.currentPosition.y, this.currentPosition.z);
            this.teleportState = TeleportController.TeleportState.IDLE;
         } else {
            this.teleportState = TeleportController.TeleportState.ASCENDING_TO_TARGET;
         }
      } else {
         double hopFraction = Math.min(48.0, horizontalDistance) / horizontalDistance;
         Vec3d nextPosition = new Vec3d(this.currentPosition.x + deltaX * hopFraction, this.bedrockLevelY, this.currentPosition.z + deltaZ * hopFraction);
         if (clientWorld.isChunkLoaded(ChunkSectionPos.getSectionCoordFloored(nextPosition.x), ChunkSectionPos.getSectionCoordFloored(nextPosition.z))) {
            this.hopTo(nextPosition, TeleportController.TeleportState.TRAVELING_UNDER_BEDROCK, 3);
         }
      }
   }

   private void moveToSafeTargetHeight(ClientPlayerEntity clientPlayerEntity, ClientWorld clientWorld) {
       Double targetY = this.findSafeTargetY(clientPlayerEntity, clientWorld);
       if (targetY == null) {
         this.teleportState = TeleportController.TeleportState.IDLE;
         ClientChatLogger.l1I1IlllIlI("Над целью нет свободного места, укажите другую Y.");
      } else {
          this.targetPosition = new Vec3d(this.targetPosition.x, targetY, this.targetPosition.z);
         this.hopTo(this.targetPosition, TeleportController.TeleportState.IDLE, 0);
         TeleportLog.II1Il11l111II11IIl("done at %.2f %.2f %.2f", this.currentPosition.x, this.currentPosition.y, this.currentPosition.z);
         ClientChatLogger.II1Il11l111II11IIl(
            String.format(
               Locale.ROOT,
               "Телепортирован на X=%.2f, Y=%.2f, Z=%.2f.",
               this.targetPosition.x,
               this.targetPosition.y,
               this.targetPosition.z
            )
         );
      }
   }

   private void hopTo(Vec3d nextPosition, TeleportController.TeleportState nextState, int delayTicks) {
      TeleportLog.II1Il11l111II11IIl(
         "hop %.2f %.2f %.2f -> %.2f %.2f %.2f",
         this.currentPosition.x,
         this.currentPosition.y,
         this.currentPosition.z,
         nextPosition.x,
         nextPosition.y,
         nextPosition.z
      );
      this.currentPosition = nextPosition;
      this.teleportState = nextState;
      this.delayTicks = delayTicks;
   }

   private Double findSafeTargetY(ClientPlayerEntity clientPlayerEntity, ClientWorld clientWorld) {
      for (double candidateY = this.targetPosition.y; candidateY <= this.bedrockLevelY + 190.0; candidateY = Math.floor(candidateY) + 1.0) {
         Vec3d candidatePosition = new Vec3d(this.targetPosition.x, candidateY, this.targetPosition.z);
         if (clientWorld.isSpaceEmpty(clientPlayerEntity, clientPlayerEntity.getDimensions(EntityPose.STANDING).getBoxAt(candidatePosition))) {
            return candidateY;
         }
      }

      return null;
   }

   private enum TeleportState {
      IDLE,
      DESCENDING_TO_BEDROCK,
      TRAVELING_UNDER_BEDROCK,
      ASCENDING_TO_TARGET;

      // $VF: synthetic method
      private static TeleportController.TeleportState[] II1Il11l111II11IIl() {
         return new TeleportController.TeleportState[]{IDLE, DESCENDING_TO_BEDROCK, TRAVELING_UNDER_BEDROCK, ASCENDING_TO_TARGET};
      }
   }
}

