package dev.daamky.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientChunkLoadProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayNetworkHandler.class)
public interface BotClientPacketListenerAccessor {
   @Invoker("setLoaded")
   void daamky$setClientLoaded(boolean loaded);
// https://t.me/pasterplace

   @Accessor("chunkLoadProgress")
   void daamky$setLevelLoadTracker(ClientChunkLoadProgress levelLoadProgress);
}
