package dev.daamky.mixin.render.visual;

import dev.daamky.NoRender;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientLevelVisualMixin {
   @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
   private void daamky$noDestroyParticles(BlockPos blockPosition, BlockState blockState, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Частицы разрушения")) {
         callbackInfo.cancel();
      }
   }

   @Inject(method = "spawnBlockBreakingParticle", at = @At("HEAD"), cancellable = true)
   private void daamky$noBreakingParticles(BlockPos blockPosition, Direction direction, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Частицы разрушения")) {
         callbackInfo.cancel();
      }
   }
}

