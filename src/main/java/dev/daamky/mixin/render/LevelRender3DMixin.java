package dev.daamky.mixin.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.daamky.ClientInitializer;
import dev.daamky.BotPictureInPictureRenderer;
import dev.daamky.DaamkyWorldRender;
import dev.daamky.core.event.impl.EventRender3D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.fog.FogRenderer;
import net.minecraft.client.render.fog.FogRenderer.FogType;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class LevelRender3DMixin {
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   private Camera camera;
   @Shadow
   @Final
   private FogRenderer fogRenderer;
   @Unique
   private long daamky$lastFrame;

   @Inject(
      method = "renderWorld",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/memory/ObjectAllocator;Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
         shift = Shift.AFTER
      )
   )
   private void daamky$renderWorld3D(CallbackInfo callbackInfo) {
      if (!BotPictureInPictureRenderer.renderingBotWorld && this.client.world != null && this.client.player != null && ClientInitializer.getEventBus() != null) {
         long frameStartNanos = System.nanoTime();
         float frameDeltaSeconds = this.daamky$lastFrame == 0L ? 0.0F : Math.min((float)(frameStartNanos - this.daamky$lastFrame) / 1.0E9F, 0.1F);
         this.daamky$lastFrame = frameStartNanos;
         float tickDelta = this.client.getRenderTickCounter().getTickProgress(false);
         DaamkyWorldRender.updateCameraState(this.camera.getCameraPos(), this.camera.getYaw(), this.camera.getPitch(), tickDelta);

         try {
            ClientInitializer.getEventBus().post(EventRender3D.get(tickDelta, this.camera.getCameraPos(), frameDeltaSeconds));
         } finally {
            RenderSystem.setShaderFog(this.fogRenderer.getFogBuffer(FogType.NONE));
            DaamkyWorldRender.flushBufferedGeometry(new Matrix4f().rotation(this.camera.getRotation().conjugate(new Quaternionf())));
         }
      }
   }
}

