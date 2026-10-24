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
   @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$particleLod(Particle particle, CallbackInfo callbackInfo) {
      ParticleAccessor particleAccessor = (ParticleAccessor)particle;
      if (Optimizer.shouldCullParticleAt(particleAccessor.daamky$getX(), particleAccessor.daamky$getY(), particleAccessor.daamky$getZ())) {
         callbackInfo.cancel();
      }
   }
}

