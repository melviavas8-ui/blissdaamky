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
    private static final String pd3041370 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
   private void daamky$noDestroyParticles(BlockPos blockPosition, BlockState blockState, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Частицы разрушения")) {
         callbackInfo.cancel();
      }
   }
// https://t.me/pasterplace

   @Inject(method = "spawnBlockBreakingParticle", at = @At("HEAD"), cancellable = true)
   private void daamky$noBreakingParticles(BlockPos blockPosition, Direction direction, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Частицы разрушения")) {
         callbackInfo.cancel();
      }
   }
}

