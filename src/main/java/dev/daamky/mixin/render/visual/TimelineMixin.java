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
    private static final String p59da3ad8 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
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

