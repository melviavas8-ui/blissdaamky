package dev.daamky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryStack;

final class BloomPostProcessor {
   private static final RenderPipeline DOWNSAMPLE_PIPELINE = createPipeline("world_bloom_downsample", "hands/hands_downsample", null, null);
   private static final RenderPipeline GAUSSIAN_BLUR_PIPELINE = createPipeline("world_bloom_gaussian", "hands/hands_gaussian", "HandsBlur", null);
   private static final RenderPipeline COMPOSITE_PIPELINE = createPipeline(
      "world_bloom_composite", "core/world_bloom_composite", "BloomData", BlendFunction.ADDITIVE
   );
   private static Framebuffer sceneFramebuffer;
   private static final Framebuffer[] blurMipFramebuffers = new Framebuffer[2];
   private static final Framebuffer[] blurTemporaryFramebuffers = new Framebuffer[2];
   private static final GpuBuffer[] horizontalBlurUniformBuffers = new GpuBuffer[2];
   private static final GpuBuffer[] verticalBlurUniformBuffers = new GpuBuffer[2];
   private static final GpuBuffer[] compositeUniformBuffers = new GpuBuffer[2];

   private BloomPostProcessor() {
   }

   static void initialize() {
   }

   private static RenderPipeline createPipeline(String pipelineName, String fragmentShaderPath, String uniformName, BlendFunction blendFunction) {
      Builder pipelineBuilder = RenderPipeline.builder(new Snippet[0])
         .withLocation(Identifier.of("daamky", "pipeline/" + pipelineName))
         .withVertexShader(Identifier.ofVanilla("core/screenquad"))
         .withFragmentShader(Identifier.of("daamky", fragmentShaderPath))
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withSampler("Sampler0")
         .withDepthWrite(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST);
      if (uniformName != null) {
         pipelineBuilder.withUniform(uniformName, UniformType.UNIFORM_BUFFER);
      }

      if (blendFunction != null) {
         pipelineBuilder.withBlend(blendFunction);
      } else {
         pipelineBuilder.withoutBlend();
      }

      return RenderPipelines.register(pipelineBuilder.build());
   }

   static void renderBloom(
      GpuTextureView outputColorTexture,
      GpuTextureView sceneDepthTexture,
      GpuBufferSlice dynamicTransformsSlice,
      BufferedWorldGeometry[] bloomGeometryBuffers,
      float blurStrength,
      float compositeStrength
   ) {
      int width = outputColorTexture.getWidth(0);
      int height = outputColorTexture.getHeight(0);
      if (sceneDepthTexture != null && sceneDepthTexture.getWidth(0) == width && sceneDepthTexture.getHeight(0) == height) {
         CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
         sceneFramebuffer = getOrResizeFramebuffer(sceneFramebuffer, "daamky-bloom-scene", width, height);

         for (BufferedWorldGeometry geometryBufferToPrepare : bloomGeometryBuffers) {
            geometryBufferToPrepare.IllI1l1l1();
         }

         ArrayList drawBufferQueue = new ArrayList();

         try {
            RenderPass sceneRenderPass = commandEncoder.createRenderPass(
               () -> "Daamky bloom scene", sceneFramebuffer.getColorAttachmentView(), OptionalInt.of(0), sceneDepthTexture, OptionalDouble.empty()
            );

            try {
               for (BufferedWorldGeometry geometryBuffer : bloomGeometryBuffers) {
                  geometryBuffer.II1Il11l111II11IIl(sceneRenderPass, dynamicTransformsSlice, drawBufferQueue);
               }
            } catch (Throwable drawFailure) {
               if (sceneRenderPass != null) {
                  try {
                     sceneRenderPass.close();
                  } catch (Throwable closeFailure) {
                     drawFailure.addSuppressed(closeFailure);
                  }
               }

               throw drawFailure;
            }

            if (sceneRenderPass != null) {
               sceneRenderPass.close();
            }
         } finally {
            for (BufferedWorldGeometry geometryBufferToDiscard : bloomGeometryBuffers) {
               geometryBufferToDiscard.l1lll11l1l();
            }
         }

         Framebuffer sourceFramebuffer = sceneFramebuffer;
         float[] blurSigmaByLevel = new float[]{1.2F + blurStrength * 0.35F, 1.0F + blurStrength * 0.9F};
         float[] compositeStrengthByLevel = new float[]{compositeStrength * 0.55F, compositeStrength * 0.85F};

         for (int blurLevel = 0; blurLevel < blurMipFramebuffers.length; blurLevel++) {
            int mipWidth = Math.max(2, (sourceFramebuffer.textureWidth + 1) / 2);
            int mipHeight = Math.max(2, (sourceFramebuffer.textureHeight + 1) / 2);
            blurMipFramebuffers[blurLevel] = getOrResizeFramebuffer(blurMipFramebuffers[blurLevel], "daamky-bloom-level" + blurLevel, mipWidth, mipHeight);
            blurTemporaryFramebuffers[blurLevel] = getOrResizeFramebuffer(blurTemporaryFramebuffers[blurLevel], "daamky-bloom-temp" + blurLevel, mipWidth, mipHeight);
            drawFullscreenPass(commandEncoder, sourceFramebuffer, blurMipFramebuffers[blurLevel], DOWNSAMPLE_PIPELINE, null);
            prepareBlurUniformBuffers(commandEncoder, blurLevel, mipWidth, mipHeight, blurSigmaByLevel[blurLevel]);
            drawFullscreenPass(commandEncoder, blurMipFramebuffers[blurLevel], blurTemporaryFramebuffers[blurLevel], GAUSSIAN_BLUR_PIPELINE, horizontalBlurUniformBuffers[blurLevel]);
            drawFullscreenPass(commandEncoder, blurTemporaryFramebuffers[blurLevel], blurMipFramebuffers[blurLevel], GAUSSIAN_BLUR_PIPELINE, verticalBlurUniformBuffers[blurLevel]);
            drawFullscreenPass(commandEncoder, blurMipFramebuffers[blurLevel], blurTemporaryFramebuffers[blurLevel], GAUSSIAN_BLUR_PIPELINE, horizontalBlurUniformBuffers[blurLevel]);
            drawFullscreenPass(commandEncoder, blurTemporaryFramebuffers[blurLevel], blurMipFramebuffers[blurLevel], GAUSSIAN_BLUR_PIPELINE, verticalBlurUniformBuffers[blurLevel]);
            sourceFramebuffer = blurMipFramebuffers[blurLevel];
         }

         for (int compositeLevel = 0; compositeLevel < blurMipFramebuffers.length; compositeLevel++) {
            compositeUniformBuffers[compositeLevel] = getOrCreateGpuBuffer(compositeUniformBuffers[compositeLevel], 16, "daamky-bloom-composite");
            MemoryStack uniformMemoryStack = MemoryStack.stackPush();

            try {
               ByteBuffer uniformData = Std140Builder.onStack(uniformMemoryStack, 16).putVec4(compositeStrengthByLevel[compositeLevel], 1.6F, 0.0F, 0.0F).get();
               commandEncoder.writeToBuffer(compositeUniformBuffers[compositeLevel].slice(), uniformData);
            } catch (Throwable writeCompositeUniformFailure) {
               if (uniformMemoryStack != null) {
                  try {
                     uniformMemoryStack.close();
                  } catch (Throwable closeUniformStackFailure) {
                     writeCompositeUniformFailure.addSuppressed(closeUniformStackFailure);
                  }
               }

               throw writeCompositeUniformFailure;
            }

            if (uniformMemoryStack != null) {
               uniformMemoryStack.close();
            }

            Framebuffer blurredFramebuffer = blurMipFramebuffers[compositeLevel];
            GpuBuffer compositeUniformBuffer = compositeUniformBuffers[compositeLevel];
            RenderPass compositePass = commandEncoder.createRenderPass(() -> "Daamky bloom composite", outputColorTexture, OptionalInt.empty());

            try {
               compositePass.setPipeline(COMPOSITE_PIPELINE);
               compositePass.bindTexture("Sampler0", blurredFramebuffer.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
               compositePass.setUniform("BloomData", compositeUniformBuffer);
               compositePass.draw(0, 3);
            } catch (Throwable compositePassFailure) {
               if (compositePass != null) {
                  try {
                     compositePass.close();
                  } catch (Throwable closeCompositePassFailure) {
                     compositePassFailure.addSuppressed(closeCompositePassFailure);
                  }
               }

               throw compositePassFailure;
            }

            if (compositePass != null) {
               compositePass.close();
            }
         }
      } else {
         for (BufferedWorldGeometry geometryBufferToDiscard : bloomGeometryBuffers) {
            geometryBufferToDiscard.l1lll11l1l();
         }
      }
   }

   private static void drawFullscreenPass(
      CommandEncoder commandEncoder, Framebuffer sourceFramebuffer, Framebuffer destinationFramebuffer, RenderPipeline blurPipeline, GpuBuffer blurUniformBuffer
   ) {
      RenderPass blurPass = commandEncoder.createRenderPass(() -> "Daamky bloom blur", destinationFramebuffer.getColorAttachmentView(), OptionalInt.empty());

      try {
         blurPass.setPipeline(blurPipeline);
         blurPass.bindTexture("Sampler0", sourceFramebuffer.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
         if (blurUniformBuffer != null) {
            blurPass.setUniform("HandsBlur", blurUniformBuffer);
         }

         blurPass.draw(0, 3);
      } catch (Throwable renderFailure) {
         if (blurPass != null) {
            try {
               blurPass.close();
            } catch (Throwable closeFailure) {
               renderFailure.addSuppressed(closeFailure);
            }
         }

         throw renderFailure;
      }

      if (blurPass != null) {
         blurPass.close();
      }
   }

   private static void prepareBlurUniformBuffers(CommandEncoder commandEncoder, int blurLevel, int textureWidth, int textureHeight, float blurSigma) {
      horizontalBlurUniformBuffers[blurLevel] = getOrCreateGpuBuffer(horizontalBlurUniformBuffers[blurLevel], 48, "daamky-bloom-h");
      verticalBlurUniformBuffers[blurLevel] = getOrCreateGpuBuffer(verticalBlurUniformBuffers[blurLevel], 48, "daamky-bloom-v");
      float[] gaussianWeights = new float[7];
      float totalGaussianWeight = 1.0F;
      gaussianWeights[0] = 1.0F;

      for (int weightIndex = 1; weightIndex < gaussianWeights.length; weightIndex++) {
         gaussianWeights[weightIndex] = (float)Math.exp(-(weightIndex * weightIndex) / (2.0F * blurSigma * blurSigma));
         totalGaussianWeight += gaussianWeights[weightIndex] * 2.0F;
      }

      float[] sampleOffsets = new float[3];
      float[] sampleWeights = new float[3];

      for (int sampleIndex = 0; sampleIndex < sampleWeights.length; sampleIndex++) {
         int firstWeightIndex = sampleIndex * 2 + 1;
         float combinedSampleWeight = gaussianWeights[firstWeightIndex] + gaussianWeights[firstWeightIndex + 1];
         sampleOffsets[sampleIndex] = firstWeightIndex + (combinedSampleWeight > 0.0F ? gaussianWeights[firstWeightIndex + 1] / combinedSampleWeight : 0.0F);
         sampleWeights[sampleIndex] = combinedSampleWeight / totalGaussianWeight;
      }

      MemoryStack uniformMemoryStack = MemoryStack.stackPush();

      try {
         for (int blurDirectionIndex = 0; blurDirectionIndex < 2; blurDirectionIndex++) {
            ByteBuffer uniformData = Std140Builder.onStack(uniformMemoryStack, 48)
               .putVec4(blurDirectionIndex == 0 ? 1.0F / textureWidth : 0.0F, blurDirectionIndex == 1 ? 1.0F / textureHeight : 0.0F, 0.0F, 0.0F)
               .putVec4(1.0F / totalGaussianWeight, sampleWeights[0], sampleWeights[1], sampleWeights[2])
               .putVec4(sampleOffsets[0], sampleOffsets[1], sampleOffsets[2], 0.0F)
               .get();
            commandEncoder.writeToBuffer((blurDirectionIndex == 0 ? horizontalBlurUniformBuffers[blurLevel] : verticalBlurUniformBuffers[blurLevel]).slice(), uniformData);
         }
      } catch (Throwable uniformWriteFailure) {
         if (uniformMemoryStack != null) {
            try {
               uniformMemoryStack.close();
            } catch (Throwable closeUniformStackFailure) {
               uniformWriteFailure.addSuppressed(closeUniformStackFailure);
            }
         }

         throw uniformWriteFailure;
      }

      if (uniformMemoryStack != null) {
         uniformMemoryStack.close();
      }
   }

   private static GpuBuffer getOrCreateGpuBuffer(GpuBuffer existingBuffer, int requiredSize, String debugLabel) {
      return existingBuffer != null && !existingBuffer.isClosed() ? existingBuffer : RenderSystem.getDevice().createBuffer(() -> debugLabel, 136, requiredSize);
   }

   private static Framebuffer getOrResizeFramebuffer(Framebuffer existingFramebuffer, String debugLabel, int targetWidth, int targetHeight) {
      if (existingFramebuffer == null) {
         return new SimpleFramebuffer(debugLabel, targetWidth, targetHeight, false);
      }

      if (existingFramebuffer.textureWidth != targetWidth || existingFramebuffer.textureHeight != targetHeight) {
         existingFramebuffer.resize(targetWidth, targetHeight);
      }

      return existingFramebuffer;
   }
}

