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
   @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
   private void daamky$noEffectFog(CameraSubmersionType submersionType, Entity entity, CallbackInfoReturnable<Boolean> callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Плохие эффекты")) {
         callbackInfo.setReturnValue(false);
      }
   }
}

