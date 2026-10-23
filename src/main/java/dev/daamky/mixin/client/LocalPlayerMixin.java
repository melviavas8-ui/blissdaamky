package dev.daamky.mixin.client;

import dev.daamky.CLIENTCloseContainer;
import dev.daamky.ClientInitializer;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventPostTick;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class LocalPlayerMixin {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Unique
   private float daamky$realYaw;
   @Unique
   private float daamky$realPitch;
// https://t.me/pasterplace

   @Inject(method = "tick()V", at = @At("HEAD"))
   private void daamky$onTick(CallbackInfo callbackInfo) {
      if (ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().post(EventUpdate.get());
      }
   }
// https://t.me/pasterplace

   @Inject(method = "closeScreen()V", at = @At("HEAD"))
   private void daamky$debugClose(CallbackInfo callbackInfo) {
      CLIENTCloseContainer.logCloseContainer();
   }
// https://t.me/pasterplace

   @Inject(method = "tick()V", at = @At("TAIL"))
   private void daamky$onTickTail(CallbackInfo callbackInfo) {
      if (ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().post(EventPostTick.get());
      }
   }
// https://t.me/pasterplace

   @Inject(method = "sendMovementPackets()V", at = @At("HEAD"))
   private void daamky$onSendPositionHead(CallbackInfo callbackInfo) {
      ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
      this.daamky$realYaw = player.getYaw();
      this.daamky$realPitch = player.getPitch();
      if (ClientInitializer.getEventBus() != null) {
         EventMotion motionEvent = ClientInitializer.getEventBus().post(EventMotion.pre(this.daamky$realYaw, this.daamky$realPitch));
         player.setYaw(motionEvent.yaw());
         player.setPitch(motionEvent.pitch());
      }
   }
// https://t.me/pasterplace

   @Inject(method = "sendMovementPackets()V", at = @At("TAIL"))
   private void daamky$onSendPositionTail(CallbackInfo callbackInfo) {
      ClientPlayerEntity player = (ClientPlayerEntity)(Object)this;
      float serverYaw = player.getYaw();
      float serverPitch = player.getPitch();
      player.setYaw(this.daamky$realYaw);
      player.setPitch(this.daamky$realPitch);
      if (ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().post(EventMotion.post(serverYaw, serverPitch));
      }
   }
}

