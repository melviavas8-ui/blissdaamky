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
    private static final String p5cca35d = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
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
// https://t.me/pasterplace

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

