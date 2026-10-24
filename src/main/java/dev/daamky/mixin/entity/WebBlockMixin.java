package dev.daamky.mixin.entity;

import dev.daamky.NoWeb;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CobwebBlock.class)
public class WebBlockMixin {
   @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
   private void daamky$entityInside(BlockState blockState, World world, BlockPos blockPosition, Entity entity, EntityCollisionHandler collisionHandler, boolean isMoving, CallbackInfo callbackInfo) {
      if (NoWeb.isIgnoreModeEnabled() && entity == MinecraftClient.getInstance().player) {
         callbackInfo.cancel();
         NoWeb.sendStopDestroyBlockPacket(blockPosition);
      }
   }
}

