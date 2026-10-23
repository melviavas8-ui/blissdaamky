package dev.daamky.mixin.render;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
   @Accessor("x")
   double daamky$getX();
// https://t.me/pasterplace

   @Accessor("y")
   double daamky$getY();
// https://t.me/pasterplace

   @Accessor("z")
   double daamky$getZ();
}
