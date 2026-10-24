package dev.daamky.mixin.render;

import dev.daamky.BeautifulyUniforms;
import dev.daamky.BotPictureInPictureRenderer;
import dev.daamky.GuiEventRenderer;
import dev.daamky.WorldFxUniforms;
import dev.daamky.WorldJumpCircleUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.state.WorldRenderState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   GuiRenderState guiState;
   @Shadow
   @Final
   private WorldRenderState worldRenderState;
   @Shadow
   @Final
   private Camera camera;

   @Inject(
      method = "render",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V")
   )
   private void modid$extractDisplay(RenderTickCounter tickCounter, boolean renderWorld, CallbackInfo callbackInfo) {
      if (this.client.isFinishedLoading() && this.client.inGameHud != null && this.client.getOverlay() == null) {
         GuiEventRenderer.dispatchDisplayEvent(this.client, this.guiState);
      }
   }

   @ModifyArg(
      method = "renderWorld",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/RawProjectionMatrix;set(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
      )
   )
   private Matrix4f daamky$captureLevelProjection(Matrix4f projectionMatrix) {
      if (BotPictureInPictureRenderer.renderingBotWorld) {
         return projectionMatrix;
      }

      WorldJumpCircleUniforms.getInstance().captureProjectionMatrix(projectionMatrix);
      WorldFxUniforms.getInstance().captureProjectionMatrix(projectionMatrix);
      return projectionMatrix;
   }

   @Inject(
      method = "renderWorld",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/ProjectionMatrix3;set(IIF)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;")
   )
   private void daamky$renderLevelBeforeHand(CallbackInfo callbackInfo) {
      if (!BotPictureInPictureRenderer.renderingBotWorld && this.client.isFinishedLoading() && this.client.player != null && this.worldRenderState != null) {
         Matrix4f inverseCameraRotation = new Matrix4f().rotation(this.camera.getRotation().conjugate(new Quaternionf()));
         WorldFxUniforms.getInstance().renderWorldEffects(this.worldRenderState.cameraRenderState, inverseCameraRotation, this.worldRenderState.skyRenderState.sunAngle);
         BeautifulyUniforms.II1Il11l111II11IIl().l1I1IlllIlI();
         WorldJumpCircleUniforms.getInstance().render(this.worldRenderState.cameraRenderState, inverseCameraRotation);
      }
   }
}

