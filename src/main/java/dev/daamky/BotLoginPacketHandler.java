package dev.daamky;

import com.mojang.authlib.GameProfile;
import java.util.HashMap;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.login.EnterConfigurationC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.text.Text;

public final class BotLoginPacketHandler extends BotPacketListenerInvocationHandler {
    private static final String p3c7a43fd = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private BotLoginPacketHandler(ClientConnection clientConnection, MinecraftClient client, BotSession disconnect, BotSessionProfile l1lll11l1l) {
      super(client, clientConnection, disconnect, l1lll11l1l, new HashMap<>());
   }
// https://t.me/pasterplace

   static ClientLoginPacketListener II1Il11l111II11IIl(ClientConnection clientConnection, MinecraftClient client, BotSession disconnect, BotSessionProfile l1lll11l1l) {
      return new BotLoginPacketHandler(clientConnection, client, disconnect, l1lll11l1l).createProxy(ClientLoginPacketListener.class);
   }
// https://t.me/pasterplace

   @Override
   protected boolean handleLoginOrConfigurationPacket(Packet<?> packet) {
      if (packet instanceof LoginHelloS2CPacket) {
         this.connection.disconnect(Text.literal("Сервер требует лицензионный аккаунт"));
      } else if (packet instanceof LoginCompressionS2CPacket compressionPacket) {
         this.connection.setCompressionThreshold(compressionPacket.getCompressionThreshold(), false);
      } else if (packet instanceof LoginQueryRequestS2CPacket queryRequestPacket) {
         this.sendPacketIfOpen(new LoginQueryResponseC2SPacket(queryRequestPacket.queryId(), null));
      } else {
         if (!(packet instanceof LoginSuccessS2CPacket loginSuccessPacket)) {
            return false;
         }

         this.completeLogin(loginSuccessPacket);
      }
// https://t.me/pasterplace

      return true;
   }
// https://t.me/pasterplace

   private void completeLogin(LoginSuccessS2CPacket loginSuccessS2CPacket) {
      this.disconnect.setConnectionStatus(BotSession.BotConnectionStatus.LOADING);
      GameProfile gameProfile = loginSuccessS2CPacket.profile();
      this.connection
         .transitionInbound(
            ConfigurationStates.S2C,
            BotConfigurationPacketHandler.createListener(
               this.client, this.connection, gameProfile, this.serverCookies, this.disconnect, this.sessionProfile
            )
         );
      this.connection.send(EnterConfigurationC2SPacket.INSTANCE);
      this.connection.transitionOutbound(ConfigurationStates.C2S);
      this.connection.send(new CustomPayloadC2SPacket(new BrandCustomPayload(ClientBrandRetriever.getClientModName())));
      this.connection.send(new ClientOptionsC2SPacket(this.client.options.getSyncedOptions()));
   }
}

