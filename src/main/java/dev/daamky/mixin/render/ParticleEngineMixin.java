package dev.daamky.mixin.render;

import dev.daamky.Optimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleEngineMixin {
    private static final String p9a54ffbf = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$particleLod(Particle particle, CallbackInfo callbackInfo) {
      ParticleAccessor particleAccessor = (ParticleAccessor)particle;
      if (Optimizer.shouldCullParticleAt(particleAccessor.daamky$getX(), particleAccessor.daamky$getY(), particleAccessor.daamky$getZ())) {
         callbackInfo.cancel();
      }
   }
}

