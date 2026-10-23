package dev.daamky.mixin.client;

import dev.daamky.AutoQuest;
import dev.daamky.ClientInitializer;
import dev.daamky.NoInteract;
import dev.daamky.WindHop;
import dev.daamky.core.event.impl.EventAttack;
import dev.daamky.core.event.impl.EventAttackPre;
import dev.daamky.core.event.impl.EventClickBlockRight;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MultiPlayerGameModeMixin {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "breakBlock(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"))
   private void daamky$onDestroyBlock(BlockPos blockPosition, CallbackInfoReturnable<Boolean> callbackInfo) {
      ClientWorld clientWorld = MinecraftClient.getInstance().world;
      if (clientWorld != null) {
         AutoQuest.onBlockMined(clientWorld.getBlockState(blockPosition));
      }
   }
// https://t.me/pasterplace

   @Inject(method = "attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$onAttackHead(PlayerEntity attackingPlayer, Entity targetEntity, CallbackInfo callbackInfo) {
      if (ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().post(new EventAttackPre(attackingPlayer, targetEntity));
         if (ClientInitializer.getEventBus().post(new EventAttack(targetEntity, false)).isCancelled()) {
            EventAttack.markCancelled();
            callbackInfo.cancel();
         }
      }
   }
// https://t.me/pasterplace

   @Inject(
      method = "interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
      at = @At("HEAD"),
      cancellable = true
   )
   private void daamky$noInteract(ClientPlayerEntity player, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> callbackInfo) {
      if (ClientInitializer.getEventBus() != null
         && ClientInitializer.getEventBus().post(new EventClickBlockRight(player, (ClientWorld)player.getEntityWorld(), hand, blockHitResult)).isCancelled()) {
         ((MultiPlayerGameModeAccessor)this).daamky$syncSelectedSlot();
         callbackInfo.setReturnValue(ActionResult.PASS);
      } else {
         if (NoInteract.II1Il11l111II11IIl(blockHitResult.getBlockPos())) {
            callbackInfo.setReturnValue(ActionResult.PASS);
         }
      }
   }
// https://t.me/pasterplace

   @Inject(method = "attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
   private void daamky$onAttackTail(PlayerEntity attackingPlayer, Entity targetEntity, CallbackInfo callbackInfo) {
      if (ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().post(new EventAttack(targetEntity, true));
      }
   }
// https://t.me/pasterplace

   @Inject(
      method = "interactItem(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
      at = @At("HEAD"),
      cancellable = true
   )
   private void daamky$windHopBegin(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> callbackInfo) {
      WindHop windHop = WindHop.getInstance();
      if (windHop != null && windHop.interceptWindChargeUse(player, hand)) {
         callbackInfo.setReturnValue(ActionResult.PASS);
      }
   }
}

