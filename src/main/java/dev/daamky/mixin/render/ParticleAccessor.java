package dev.daamky.mixin.render;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Particle.class)
public interface ParticleAccessor {
   @Accessor("x")
   double daamky$getX();

   @Accessor("y")
   double daamky$getY();

   @Accessor("z")
   double daamky$getZ();
}
