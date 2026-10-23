package dev.daamky.mixin.render.visual;

import com.llamalad7.mixinextras.sugar.Local;
import dev.daamky.CustomFog;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(
      method = "applyFog",
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/blaze3d/systems/CommandEncoder;mapBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;ZZ)Lcom/mojang/blaze3d/buffers/GpuBuffer$MappedView;"
      )
   )
   private void daamky$customFog(
      Camera camera,
      int viewDistance,
      RenderTickCounter tickCounter,
      float skyDarkness,
      ClientWorld world,
      CallbackInfoReturnable<Vector4f> callbackInfo,
      @Local FogData fogData,
      @Local Vector4f fogColor
   ) {
      CustomFog.applyFog(fogData, fogColor, camera);
   }
}

