package dev.daamky.mixin.network;

import dev.daamky.BotPacketLog;
import dev.daamky.CLIENTCloseContainer;
import dev.daamky.ClientInitializer;
import dev.daamky.PlayerMovementPacketFactory;
import dev.daamky.UseItemRotationQueue;
import dev.daamky.PacketSender;
import dev.daamky.OutgoingPacketBuffer;
import dev.daamky.BotPacketListenerProxy;
import dev.daamky.SavedBots;
import dev.daamky.core.event.impl.EventPacket;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientTickEndC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ConnectionMixin {
   @Unique
   private boolean daamky$positionThisTick;
   @Unique
   private boolean daamky$teleportReply;

   @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
   private void daamky$onSend(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo callbackInfo) {
      if (SavedBots.isBotClientConnection((ClientConnection)(Object)this)) {
         BotPacketLog.II1Il11l111II11IIl(">>", ((ClientConnection)(Object)this).getPacketListener(), packet);
         if (((ClientConnection)(Object)this).getPacketListener() instanceof BotPacketListenerProxy botPacketListener) {
            Packet filteredPacket = botPacketListener.filterOutgoingPacket(packet);
            if (filteredPacket == null) {
               callbackInfo.cancel();
            }
         }
      } else {
         CLIENTCloseContainer.logOutgoingPacket(packet, "SEND");
         if (!PacketSender.II1Il11l111II11IIl()
            && ClientInitializer.getEventBus() != null
            && ClientInitializer.getEventBus().post(new EventPacket(packet, EventPacket.Direction.SEND)).isCancelled()) {
            CLIENTCloseContainer.logOutgoingPacket(packet, "CANCELLED");
            callbackInfo.cancel();
         } else if (OutgoingPacketBuffer.queuePacketIfBuffering(packet)) {
            CLIENTCloseContainer.logOutgoingPacket(packet, "DEFERRED");
            callbackInfo.cancel();
         } else if (UseItemRotationQueue.queueIfItemUsePacket(packet)) {
            callbackInfo.cancel();
         } else {
            if (packet instanceof ClientTickEndC2SPacket) {
               UseItemRotationQueue.flushQueuedPackets();
               this.daamky$positionThisTick = false;
            } else if (packet instanceof TeleportConfirmC2SPacket) {
               this.daamky$teleportReply = true;
            } else if (packet instanceof PlayerMoveC2SPacket movementPacket) {
               UseItemRotationQueue.onPlayerMovePacket(movementPacket);
               if (this.daamky$teleportReply && movementPacket.changesPosition()) {
                  this.daamky$teleportReply = false;
                  return;
               }

               OutgoingPacketBuffer.startBuffering();
               if (movementPacket.changesPosition()) {
                  if (this.daamky$positionThisTick) {
                     callbackInfo.cancel();
                     PacketSender.l1I1IlllIlI(PlayerMovementPacketFactory.II1Il11l111II11IIl(movementPacket));
                     return;
                  }

                  this.daamky$positionThisTick = true;
               }
            }
         }
      }
   }

   @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("TAIL"))
   private void daamky$afterSend(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo callbackInfo) {
      if (packet instanceof ClientTickEndC2SPacket && !SavedBots.isBotClientConnection((ClientConnection)(Object)this)) {
         OutgoingPacketBuffer.flush();
      }
   }

   @Inject(
      method = "handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private static void daamky$onReceive(Packet<?> packet, PacketListener listener, CallbackInfo callbackInfo) {
      if (ClientInitializer.getEventBus() != null && !SavedBots.isDisconnectSessionView(listener)) {
         CLIENTCloseContainer.logIncomingPacket(packet);
         if (ClientInitializer.getEventBus().post(new EventPacket(packet, EventPacket.Direction.RECEIVE)).isCancelled()) {
            callbackInfo.cancel();
         }
      }
   }
}

