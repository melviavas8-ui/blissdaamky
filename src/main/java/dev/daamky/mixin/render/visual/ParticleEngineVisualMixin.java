package dev.daamky.mixin.render.visual;

import dev.daamky.NoRender;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleEngineVisualMixin {
   @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
   private void daamky$noParticles(Particle particle, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Частицы")) {
         callbackInfo.cancel();
      }
   }
}

