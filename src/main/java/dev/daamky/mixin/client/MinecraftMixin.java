package dev.daamky.mixin.client;

import dev.daamky.ClientInitializer;
import dev.daamky.BotClientPlayHandler;
import dev.daamky.InSampler;
import dev.daamky.UseItemRotationQueue;
import dev.daamky.WingsAnimationController;
import dev.daamky.OutgoingPacketBuffer;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.core.event.impl.EventWorldChange;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftMixin {
   @Shadow
   public ClientPlayerEntity player;

   @Inject(method = "tick()V", at = @At("HEAD"))
   private void daamky$onTick(CallbackInfo callbackInfo) {
      WingsAnimationController.tickAnimationInput();
      if (this.player != null && ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().post(EventTick.get());
      }
   }

   @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"))
   private void daamky$onSetLevel(ClientWorld newWorld, CallbackInfo callbackInfo) {
      if (!BotClientPlayHandler.isClientStateIsolationActive()) {
         InSampler.clearCompletedBlurPasses();
         if (ClientInitializer.getEventBus() != null) {
            OutgoingPacketBuffer.clear();
            UseItemRotationQueue.clearQueue();
            ClientInitializer.getEventBus().post(new EventWorldChange());
         }
      }
   }
}

