package dev.daamky.mixin.entity;

import dev.daamky.HitBoxes;
import dev.daamky.NoPush;
import dev.daamky.NoWeb;
import dev.daamky.RotationPriorityManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
   @Redirect(method = "updateVelocity(FLnet/minecraft/util/math/Vec3d;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getYaw()F"))
   private float daamky$moveRelativeYaw(Entity entity) {
      return entity == MinecraftClient.getInstance().player ? RotationPriorityManager.getInstance().resolveYaw(entity.getYaw()) : entity.getYaw();
   }

   @Inject(method = "getTargetingMargin()F", at = @At("RETURN"), cancellable = true)
   private void daamky$expandPickRadius(CallbackInfoReturnable<Float> callbackInfo) {
      float pickRadius = HitBoxes.II1Il11l111II11IIl((Entity)(Object)this);
      if (pickRadius > 0.0F) {
         callbackInfo.setReturnValue((Float)callbackInfo.getReturnValue() + pickRadius);
      }
   }

   @Inject(method = "slowMovement(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$noWeb(BlockState blockState, Vec3d movementMultiplier, CallbackInfo callbackInfo) {
      if ((Entity)(Object)this == MinecraftClient.getInstance().player && NoWeb.shouldIgnoreBlock(blockState)) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "pushAwayFrom(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$noEntityPush(Entity otherEntity, CallbackInfo callbackInfo) {
      ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
      if (clientPlayer != null
         && NoPush.l1I1IIl111Il1l1l()
         && (clientPlayer.getUuid().equals(((Entity)(Object)this).getUuid()) || clientPlayer.getUuid().equals(otherEntity.getUuid()))) {
         callbackInfo.cancel();
      }
   }
}

