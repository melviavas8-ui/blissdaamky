package dev.daamky;

import com.mojang.authlib.GameProfile;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientDynamicRegistryType;
import net.minecraft.client.network.ClientRegistries;
import net.minecraft.client.resource.ClientDataPackManager;
import net.minecraft.client.world.ClientChunkLoadProgress;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.config.AcceptCodeOfConductC2SPacket;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.config.CodeOfConductS2CPacket;
import net.minecraft.network.packet.s2c.config.DynamicRegistriesS2CPacket;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.packet.s2c.config.SelectKnownPacksS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.network.state.PlayStateFactories.PacketCodecModifierContext;
import net.minecraft.registry.DynamicRegistryManager.Immutable;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;

public final class BotConfigurationPacketHandler extends BotPacketListenerInvocationHandler {
   private final ClientRegistries clientRegistries = new ClientRegistries();
   private final GameProfile gameProfile;
   private FeatureSet enabledFeatures = FeatureFlags.DEFAULT_ENABLED_FEATURES;
   private ClientDataPackManager dataPackManager;

   private BotConfigurationPacketHandler(
      MinecraftClient client,
      ClientConnection clientConnection,
      GameProfile gameProfile,
      Map<Identifier, byte[]> entries,
      BotSession disconnect,
      BotSessionProfile l1lll11l1l
   ) {
      super(client, clientConnection, disconnect, l1lll11l1l, entries);
      this.gameProfile = gameProfile;
   }

   static ClientConfigurationPacketListener createListener(
      MinecraftClient client,
      ClientConnection clientConnection,
      GameProfile gameProfile,
      Map<Identifier, byte[]> entries,
      BotSession disconnect,
      BotSessionProfile l1lll11l1l
   ) {
      return new BotConfigurationPacketHandler(client, clientConnection, gameProfile, entries, disconnect, l1lll11l1l)
         .createProxy(ClientConfigurationPacketListener.class);
   }

   @Override
   protected boolean handleLoginOrConfigurationPacket(Packet<?> packet) {
      if (packet instanceof DynamicRegistriesS2CPacket registryPacket) {
         this.clientRegistries.putDynamicRegistry(registryPacket.registry(), registryPacket.entries());
      } else if (packet instanceof SynchronizeTagsS2CPacket tagsPacket) {
         this.clientRegistries.putTags(tagsPacket.getGroups());
      } else if (packet instanceof FeaturesS2CPacket featuresPacket) {
         this.enabledFeatures = FeatureFlags.FEATURE_MANAGER.featureSetOf(featuresPacket.features());
      } else if (packet instanceof SelectKnownPacksS2CPacket knownPacksPacket) {
         if (this.dataPackManager == null) {
            this.dataPackManager = new ClientDataPackManager();
         }

         this.sendPacketIfOpen(new SelectKnownPacksC2SPacket(this.dataPackManager.getCommonKnownPacks(knownPacksPacket.knownPacks())));
      } else if (packet instanceof CodeOfConductS2CPacket) {
         this.sendPacketIfOpen(AcceptCodeOfConductC2SPacket.INSTANCE);
      } else {
         if (!(packet instanceof ReadyS2CPacket)) {
            return false;
         }

         this.finishConfiguration();
      }

      return true;
   }

   private void finishConfiguration() {
      Immutable immutable = ClientDynamicRegistryType.createCombinedDynamicRegistries().getCombinedRegistryManager();
      Immutable immutable2;
      if (this.dataPackManager == null) {
         immutable2 = this.clientRegistries.createRegistryManager(ResourceFactory.MISSING, immutable, true);
      } else {
         LifecycledResourceManager lifecycledResourceManager = this.dataPackManager.createResourceManager();

         try {
            immutable2 = this.clientRegistries.createRegistryManager(lifecycledResourceManager, immutable, true);
         } catch (Throwable registryCreationFailure) {
            if (lifecycledResourceManager != null) {
               try {
                  lifecycledResourceManager.close();
               } catch (Throwable resourceCloseFailure) {
                  registryCreationFailure.addSuppressed(resourceCloseFailure);
               }
            }

            throw registryCreationFailure;
         }

         if (lifecycledResourceManager != null) {
            lifecycledResourceManager.close();
         }
      }

      ClientConnectionState clientConnectionState = new ClientConnectionState(
         new ClientChunkLoadProgress(),
         this.gameProfile,
         this.client.getTelemetryManager().createWorldSession(false, null, null),
         immutable2,
         this.enabledFeatures,
         this.serverBrand,
         null,
         null,
         this.serverCookies,
         null,
         this.customReportDetails,
         this.serverLinks,
         new HashMap(),
         false
      );
      Joined joined = new Joined(this.client, this.connection, clientConnectionState, this.disconnect, this.sessionProfile);
      this.disconnect.setJoinedSession(joined);
      this.connection.transitionInbound(PlayStateFactories.S2C.bind(RegistryByteBuf.makeFactory(immutable2)), joined.getPlayPacketListener());
      this.connection.send(ReadyC2SPacket.INSTANCE);
      this.connection
         .transitionOutbound(PlayStateFactories.C2S.bind(RegistryByteBuf.makeFactory(immutable2), (PacketCodecModifierContext)() -> false));
   }
}

