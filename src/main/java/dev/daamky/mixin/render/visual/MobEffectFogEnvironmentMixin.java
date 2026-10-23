package dev.daamky.mixin.render.visual;

import dev.daamky.NoRender;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectFogModifier.class)
public abstract class MobEffectFogEnvironmentMixin {
    private static final String p75d7f21d = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
   private void daamky$noEffectFog(CameraSubmersionType submersionType, Entity entity, CallbackInfoReturnable<Boolean> callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Плохие эффекты")) {
         callbackInfo.setReturnValue(false);
      }
   }
}

