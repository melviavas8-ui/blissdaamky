package dev.daamky.mixin.render;

import dev.daamky.Optimizer;
import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Particle.class)
public abstract class ParticleMixin {
   @Shadow
   protected double x;
   @Shadow
   protected double y;
   @Shadow
   protected double z;
   @Shadow
   protected boolean collidesWithWorld;
   @Unique
   private boolean daamky$physicsSuppressed;

   @Inject(method = "move(DDD)V", at = @At("HEAD"))
   private void daamky$simplePhysics(double velocityX, double velocityY, double velocityZ, CallbackInfo callbackInfo) {
      boolean skipCollisionPhysics = Optimizer.shouldSkipParticlePhysicsAt(this.x, this.y, this.z);
      if (this.collidesWithWorld && skipCollisionPhysics) {
         this.collidesWithWorld = false;
         this.daamky$physicsSuppressed = true;
      } else if (this.daamky$physicsSuppressed && !skipCollisionPhysics) {
         this.collidesWithWorld = true;
         this.daamky$physicsSuppressed = false;
      }
   }
}

