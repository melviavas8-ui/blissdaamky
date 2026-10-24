package dev.daamky.mixin.client;

import dev.daamky.ClientCommandDispatcher;
import dev.daamky.SavedBots;
import dev.daamky.ServerHelper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPacketListenerMixin {
   @Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$onChat(String chatMessage, CallbackInfo callbackInfo) {
      if (ClientCommandDispatcher.getInstance().dispatch(chatMessage) || ServerHelper.shouldFilterChatMessage(chatMessage)) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
   private void daamky$muteMainSystemChat(GameMessageS2CPacket packet, CallbackInfo callbackInfo) {
      if (SavedBots.isActiveSessionReady() && !SavedBots.ownsActiveSessionObject(this)) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
   private void daamky$muteMainPlayerChat(ChatMessageS2CPacket packet, CallbackInfo callbackInfo) {
      if (SavedBots.isActiveSessionReady() && !SavedBots.ownsActiveSessionObject(this)) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "onProfilelessChatMessage", at = @At("HEAD"), cancellable = true)
   private void daamky$muteMainDisguisedChat(ProfilelessChatMessageS2CPacket packet, CallbackInfo callbackInfo) {
      if (SavedBots.isActiveSessionReady() && !SavedBots.ownsActiveSessionObject(this)) {
         callbackInfo.cancel();
      }
   }
}

