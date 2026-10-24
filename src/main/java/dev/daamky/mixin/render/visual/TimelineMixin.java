package dev.daamky.mixin.render.visual;

import dev.daamky.Ambience;
import net.minecraft.world.World;
import net.minecraft.world.attribute.timeline.Timeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Timeline.class)
public abstract class TimelineMixin {
   @Inject(method = "getRawTimeOfDay", at = @At("RETURN"), cancellable = true)
   private void daamky$ambienceTime(World world, CallbackInfoReturnable<Long> callbackInfo) {
      if (world.isClient()) {
         long vanillaTimeOfDay = callbackInfo.getReturnValueJ();
         long ambienceTimeOfDay = Ambience.adjustWorldTime(vanillaTimeOfDay);
         if (ambienceTimeOfDay != vanillaTimeOfDay) {
            callbackInfo.setReturnValue(ambienceTimeOfDay);
         }
      }
   }
}

