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
    private static final String p87b02c1e = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$onChat(String chatMessage, CallbackInfo callbackInfo) {
      if (ClientCommandDispatcher.getInstance().dispatch(chatMessage) || ServerHelper.shouldFilterChatMessage(chatMessage)) {
         callbackInfo.cancel();
      }
   }
// https://t.me/pasterplace

   @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
   private void daamky$muteMainSystemChat(GameMessageS2CPacket packet, CallbackInfo callbackInfo) {
      if (SavedBots.isActiveSessionReady() && !SavedBots.ownsActiveSessionObject(this)) {
         callbackInfo.cancel();
      }
   }
// https://t.me/pasterplace

   @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
   private void daamky$muteMainPlayerChat(ChatMessageS2CPacket packet, CallbackInfo callbackInfo) {
      if (SavedBots.isActiveSessionReady() && !SavedBots.ownsActiveSessionObject(this)) {
         callbackInfo.cancel();
      }
   }
// https://t.me/pasterplace

   @Inject(method = "onProfilelessChatMessage", at = @At("HEAD"), cancellable = true)
   private void daamky$muteMainDisguisedChat(ProfilelessChatMessageS2CPacket packet, CallbackInfo callbackInfo) {
      if (SavedBots.isActiveSessionReady() && !SavedBots.ownsActiveSessionObject(this)) {
         callbackInfo.cancel();
      }
   }
}

