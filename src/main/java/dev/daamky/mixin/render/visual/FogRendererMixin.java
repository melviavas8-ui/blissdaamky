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

