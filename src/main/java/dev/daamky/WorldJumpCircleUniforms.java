package dev.daamky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.state.CameraRenderState;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

public class WorldJumpCircleUniforms {
   private static final WorldJumpCircleUniforms INSTANCE = new WorldJumpCircleUniforms();
   private static final int MAX_JUMP_CIRCLES = 16;
   private static final int UNIFORM_BUFFER_SIZE = new Std140SizeCalculator().putMat4f().putMat4f().putVec4().putVec4().get() + 768;
   private GpuBuffer uniformBuffer;
   private SimpleFramebuffer sceneCopyFramebuffer;
   private final Matrix4f projectionMatrix = new Matrix4f();
   private volatile boolean projectionMatrixCaptured = false;

   public static WorldJumpCircleUniforms getInstance() {
      return INSTANCE;
   }

   public void captureProjectionMatrix(Matrix4f matrix4f) {
      if (matrix4f != null) {
         this.projectionMatrix.set(matrix4f);
         this.projectionMatrixCaptured = true;
      }
   }

   private void ensureUniformBuffer() {
      if (this.uniformBuffer == null || this.uniformBuffer.isClosed()) {
         this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "jump circles ubo", 136, UNIFORM_BUFFER_SIZE);
      }
   }

   public void render(CameraRenderState cameraRenderState, Matrix4f matrix4f) {
      JumpCircle jumpCircle = JumpCircle.getInstance();
      if (jumpCircle != null && jumpCircle.isEnabled() && jumpCircle.isPostEffectEnabled() && jumpCircle.hasActiveCircles()) {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.gameRenderer != null) {
            Framebuffer framebuffer = client.getFramebuffer();
            if (isFramebufferReady(framebuffer) && cameraRenderState != null && cameraRenderState.initialized && this.projectionMatrixCaptured) {
               jumpCircle.removeExpiredCircles();
               if (jumpCircle.hasActiveCircles()) {
                  Matrix4f capturedProjectionMatrix = this.projectionMatrixCaptured ? this.projectionMatrix : null;
                  if (capturedProjectionMatrix != null) {
                     Matrix4f matrix4f2 = new Matrix4f(capturedProjectionMatrix).invert();
                     Matrix4f matrix4f3 = new Matrix4f(matrix4f);
                     Matrix4f matrix4f4 = new Matrix4f(matrix4f3).invert();
                     this.ensureSceneCopyFramebuffer(framebuffer.textureWidth, framebuffer.textureHeight);
                     this.ensureUniformBuffer();
                     RenderSystem.getDevice()
                        .createCommandEncoder()
                        .copyTextureToTexture(
                           framebuffer.getColorAttachment(), this.sceneCopyFramebuffer.getColorAttachment(), 0, 0, 0, 0, 0, framebuffer.textureWidth, framebuffer.textureHeight
                        );
                     this.writeCircleUniforms(jumpCircle, cameraRenderState, matrix4f2, matrix4f4, framebuffer.textureWidth, framebuffer.textureHeight);
                     GpuSampler gpuSampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
                     GpuSampler gpuSampler2 = RenderSystem.getSamplerCache().get(FilterMode.NEAREST);
                     RenderPass renderPass = RenderSystem.getDevice()
                        .createCommandEncoder()
                        .createRenderPass(() -> "jump circles pass", framebuffer.getColorAttachmentView(), OptionalInt.empty());

                     try {
                        renderPass.setPipeline(JumpCirclePostPipeline.II1Il11l111II11IIl);
                        RenderSystem.bindDefaultUniforms(renderPass);
                        renderPass.setUniform("WorldJumpCircleUniforms", this.uniformBuffer);
                        renderPass.bindTexture("SceneSampler", this.sceneCopyFramebuffer.getColorAttachmentView(), gpuSampler);
                        renderPass.bindTexture("DepthSampler", framebuffer.getDepthAttachmentView(), gpuSampler2);
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
   }

   private void writeCircleUniforms(JumpCircle jumpCircle, CameraRenderState cameraRenderState, Matrix4f matrix4f, Matrix4f matrix4f2, int framebufferWidth, int framebufferHeight) {
      List items = jumpCircle.getCircleEntries();
      int circleCount = Math.min(items.size(), 16);
      MemoryStack memoryStack = MemoryStack.stackPush();

      try {
         Std140Builder std140Builder = Std140Builder.onStack(memoryStack, UNIFORM_BUFFER_SIZE)
            .putMat4f(matrix4f)
            .putMat4f(matrix4f2)
            .putVec4(
               (float)cameraRenderState.pos.x,
               (float)cameraRenderState.pos.y,
               (float)cameraRenderState.pos.z,
               0.0F
            )
            .putVec4(1.0F / framebufferWidth, 1.0F / framebufferHeight, (float)(System.currentTimeMillis() % 1000000L / 1000.0), circleCount);

         for (int byteBuffer = 0; byteBuffer < 16; byteBuffer++) {
            if (byteBuffer < circleCount) {
               Object circleEntry = (JumpCircle.JumpCircleEntry)items.get(byteBuffer);
               std140Builder.putVec4(
                  (float)((JumpCircle.JumpCircleEntry)circleEntry).getPosition().x,
                  (float)((JumpCircle.JumpCircleEntry)circleEntry).getPosition().y,
                  (float)((JumpCircle.JumpCircleEntry)circleEntry).getPosition().z,
                  ((JumpCircle.JumpCircleEntry)circleEntry).getLifeProgress()
               );
               int colorArgb = ((JumpCircle.JumpCircleEntry)circleEntry).getColorArgb();
               std140Builder.putVec4(
                  (colorArgb >> 16 & 0xFF) / 255.0F, (colorArgb >> 8 & 0xFF) / 255.0F, (colorArgb & 0xFF) / 255.0F, ((JumpCircle.JumpCircleEntry)circleEntry).getRadius()
               );
               std140Builder.putVec4(
                  ((JumpCircle.JumpCircleEntry)circleEntry).getGlowPower(),
                  ((JumpCircle.JumpCircleEntry)circleEntry).getRingWidth(),
                  ((JumpCircle.JumpCircleEntry)circleEntry).getEffectParameterX(),
                  1.5F
               );
            } else {
               std140Builder.putVec4(0.0F, 0.0F, 0.0F, 0.0F);
               std140Builder.putVec4(0.0F, 0.0F, 0.0F, 0.0F);
               std140Builder.putVec4(0.0F, 0.0F, 0.0F, 0.0F);
            }
         }

         ByteBuffer byteBuffer = std140Builder.get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.uniformBuffer.slice(), byteBuffer);
      } catch (Throwable bufferWriteFailure) {
         if (memoryStack != null) {
            try {
               memoryStack.close();
            } catch (Throwable memoryStackCloseFailure) {
               bufferWriteFailure.addSuppressed(memoryStackCloseFailure);
            }
         }

         throw bufferWriteFailure;
      }

      if (memoryStack != null) {
         memoryStack.close();
      }
   }

   private void ensureSceneCopyFramebuffer(int framebufferWidth, int framebufferHeight) {
      if (this.sceneCopyFramebuffer == null || this.sceneCopyFramebuffer.textureWidth != framebufferWidth || this.sceneCopyFramebuffer.textureHeight != framebufferHeight) {
         if (this.sceneCopyFramebuffer != null) {
            this.sceneCopyFramebuffer.delete();
         }

         this.sceneCopyFramebuffer = new SimpleFramebuffer("jump scene copy", framebufferWidth, framebufferHeight, false);
      }
   }

   private static boolean isFramebufferReady(Framebuffer framebuffer) {
      return framebuffer != null
         && framebuffer.textureWidth > 0
         && framebuffer.textureHeight > 0
         && framebuffer.getColorAttachment() != null
         && framebuffer.getDepthAttachment() != null;
   }

   public void close() {
      if (this.sceneCopyFramebuffer != null) {
         this.sceneCopyFramebuffer.delete();
         this.sceneCopyFramebuffer = null;
      }

      if (this.uniformBuffer != null && !this.uniformBuffer.isClosed()) {
         this.uniformBuffer.close();
         this.uniformBuffer = null;
      }
   }
}

