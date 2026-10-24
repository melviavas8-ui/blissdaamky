package dev.daamky.mixin.render.visual;

import dev.daamky.NoRender;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherRendering.class)
public abstract class WeatherEffectRendererMixin {
   @Inject(method = "buildPrecipitationPieces", at = @At("TAIL"))
   private void daamky$noWeather(World world, int ticks, float tickDelta, Vec3d cameraPosition, WeatherRenderState weatherRenderState, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Погода")) {
         weatherRenderState.intensity = 0.0F;
         weatherRenderState.rainPieces.clear();
         weatherRenderState.snowPieces.clear();
      }
   }

   @Inject(method = "addParticlesAndSound", at = @At("HEAD"), cancellable = true)
   private void daamky$noRainParticles(ClientWorld world, Camera camera, int ticks, ParticlesMode particlesMode, int packedLight, CallbackInfo callbackInfo) {
      if (NoRender.II1Il11l111II11IIl("Погода")) {
         callbackInfo.cancel();
      }
   }
}

