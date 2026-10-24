package dev.daamky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.state.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

public final class WorldFxUniforms {
   private static final WorldFxUniforms INSTANCE = new WorldFxUniforms();
   private static final int UNIFORM_BUFFER_SIZE_BYTES = new Std140SizeCalculator()
      .putMat4f()
      .putMat4f()
      .putMat4f()
      .putMat4f()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec4()
      .putVec4()
      .get();
   private GpuBuffer uniformBuffer;
   private SimpleFramebuffer sceneCopyFramebuffer;
   private final Matrix4f capturedProjectionMatrix = new Matrix4f();
   private boolean hasCapturedProjectionMatrix;

   public static WorldFxUniforms getInstance() {
      return INSTANCE;
   }

   public void captureProjectionMatrix(Matrix4f matrix4f) {
      if (matrix4f != null) {
         this.capturedProjectionMatrix.set(matrix4f);
         this.hasCapturedProjectionMatrix = true;
      }
   }

   public void renderWorldEffects(CameraRenderState cameraRenderState, Matrix4f matrix4f, float sunAngle) {
      Ambience ambience = Ambience.getInstance();
      CustomFog customFog = CustomFog.getInstance();
      float skyEffectMode = ambience != null && ambience.isEnabled() ? ambience.getSkyStyleIndex() : 0.0F;
      float fogEnabled = ambience != null && ambience.isEnabled() && ambience.isFogEnabled() ? 1.0F : 0.0F;
      float wetWorldEnabled = ambience != null && ambience.isEnabled() && ambience.isWetWeatherEnabled() ? 1.0F : 0.0F;
      float customFogMode = customFog != null && customFog.isEnabled() ? customFog.getFogModeValue() : 0.0F;
      float sunRayStrength = ambience != null && ambience.isEnabled() ? ambience.getSunRayStrength() : 0.0F;
      if (skyEffectMode != 0.0F || fogEnabled != 0.0F || wetWorldEnabled != 0.0F || customFogMode != 0.0F || sunRayStrength != 0.0F) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.gameRenderer != null && cameraRenderState != null && cameraRenderState.initialized && this.hasCapturedProjectionMatrix) {
            Framebuffer framebuffer = client.getFramebuffer();
            if (framebuffer != null
               && framebuffer.textureWidth > 0
               && framebuffer.textureHeight > 0
               && framebuffer.getColorAttachment() != null
               && framebuffer.getDepthAttachment() != null) {
               Matrix4f matrix4f2 = new Matrix4f(this.capturedProjectionMatrix);
               Matrix4f matrix4f3 = new Matrix4f(matrix4f);
               Matrix4f matrix4f4 = new Matrix4f(matrix4f2).invert();
               Matrix4f matrix4f5 = new Matrix4f(matrix4f3).invert();
               if (this.sceneCopyFramebuffer == null || this.sceneCopyFramebuffer.textureWidth != framebuffer.textureWidth || this.sceneCopyFramebuffer.textureHeight != framebuffer.textureHeight) {
                  if (this.sceneCopyFramebuffer != null) {
                     this.sceneCopyFramebuffer.delete();
                  }

                  this.sceneCopyFramebuffer = new SimpleFramebuffer("daamky world fx copy", framebuffer.textureWidth, framebuffer.textureHeight, false);
               }

               if (this.uniformBuffer == null || this.uniformBuffer.isClosed()) {
                  this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "daamky world fx ubo", 136, UNIFORM_BUFFER_SIZE_BYTES);
               }

               RenderSystem.getDevice()
                  .createCommandEncoder()
                  .copyTextureToTexture(
                     framebuffer.getColorAttachment(), this.sceneCopyFramebuffer.getColorAttachment(), 0, 0, 0, 0, 0, framebuffer.textureWidth, framebuffer.textureHeight
                  );
               float[] fogColor = customFog != null ? customFog.getFogColorWithStrength() : new float[]{0.6F, 0.63F, 0.67F, 0.0F};
               float[] fogDistances = customFog != null ? new float[]{customFog.getFogStartDistance(), customFog.getFogEndDistance()} : new float[]{10.0F, 64.0F};
               float[] themeColor = ambience != null ? ambience.getAccentRgb() : new float[]{0.5F, 0.7F, 1.0F};
               float ambientDensity = ambience != null ? ambience.getFogDensity() : 0.0F;
               float humidity = ambience != null ? ambience.getHumidity() : 0.0F;
               Vector3f vector3f = cameraRenderState.orientation.transform(new Vector3f(0.0F, 0.0F, -1.0F));
               Vector3f vector3f2 = cameraRenderState.orientation.transform(new Vector3f(0.0F, 1.0F, 0.0F));
               Vector3f vector3f3 = cameraRenderState.orientation.transform(new Vector3f(1.0F, 0.0F, 0.0F));
               Vector3f vector3f4 = new Matrix4f().rotateY((float)Math.toRadians(-90.0)).rotateX(sunAngle).transformDirection(new Vector3f(0.0F, 1.0F, 0.0F));
               MemoryStack memoryStack = MemoryStack.stackPush();

               try {
                  Std140Builder std140Builder = Std140Builder.onStack(memoryStack, UNIFORM_BUFFER_SIZE_BYTES)
                     .putMat4f(matrix4f4)
                     .putMat4f(matrix4f5)
                     .putMat4f(matrix4f2)
                     .putMat4f(matrix4f3)
                     .putVec4(
                        (float)cameraRenderState.pos.x,
                        (float)cameraRenderState.pos.y,
                        (float)cameraRenderState.pos.z,
                        0.0F
                     )
                     .putVec4(1.0F / framebuffer.textureWidth, 1.0F / framebuffer.textureHeight, (float)(System.currentTimeMillis() % 1000000L / 1000.0), 0.0F)
                     .putVec4(skyEffectMode, fogEnabled, wetWorldEnabled, customFogMode)
                     .putVec4(fogColor[0], fogColor[1], fogColor[2], fogColor[3])
                     .putVec4(fogDistances[0], fogDistances[1], ambientDensity, humidity)
                     .putVec4(themeColor[0], themeColor[1], themeColor[2], 0.0F)
                     .putVec4(vector3f.x, vector3f.y, vector3f.z, 1.0F / matrix4f2.m00())
                     .putVec4(vector3f2.x, vector3f2.y, vector3f2.z, 1.0F / matrix4f2.m11())
                     .putVec4(vector3f3.x, vector3f3.y, vector3f3.z, 0.0F)
                     .putVec4(vector3f4.x, vector3f4.y, vector3f4.z, sunRayStrength);
                  RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.uniformBuffer.slice(), std140Builder.get());
               } catch (Throwable bufferWriteFailure) {
                  if (memoryStack != null) {
                     try {
                        memoryStack.close();
                     } catch (Throwable bufferCloseFailure) {
                        bufferWriteFailure.addSuppressed(bufferCloseFailure);
                     }
                  }

                  throw bufferWriteFailure;
               }

               if (memoryStack != null) {
                  memoryStack.close();
               }

               RenderPass renderPass = RenderSystem.getDevice()
                  .createCommandEncoder()
                  .createRenderPass(() -> "daamky world fx", framebuffer.getColorAttachmentView(), OptionalInt.empty());

               try {
                  renderPass.setPipeline(WorldPostProcessPipelines.II1Il11l111II11IIl);
                  RenderSystem.bindDefaultUniforms(renderPass);
                  renderPass.setUniform("WorldFxUniforms", this.uniformBuffer);
                  renderPass.bindTexture("SceneSampler", this.sceneCopyFramebuffer.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
                  renderPass.bindTexture("DepthSampler", framebuffer.getDepthAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.NEAREST));
                  renderPass.draw(0, 3);
               } catch (Throwable renderPassFailure) {
                  if (renderPass != null) {
                     try {
                        renderPass.close();
                     } catch (Throwable renderPassCloseFailure) {
                        renderPassFailure.addSuppressed(renderPassCloseFailure);
                     }
                  }

                  throw renderPassFailure;
               }

               if (renderPass != null) {
                  renderPass.close();
               }
            }
         }
      }
   }
}

