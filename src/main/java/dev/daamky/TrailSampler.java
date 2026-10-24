package dev.daamky;

import com.mojang.blaze3d.buffers.GpuBuffer;
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
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryStack;

public final class TrailSampler {
   public static final TrailSampler INSTANCE = new TrailSampler();
   private final RenderPipeline compositePipeline = createCompositePipeline(-1);
   private final RenderPipeline[] compositeStylePipelines = new RenderPipeline[6];
   private final RenderPipeline noEffectCompositePipeline;
   private final RenderPipeline handMaskPipeline;
   private final RenderPipeline bloomDownsamplePipeline;
   private final RenderPipeline maskDownsamplePipeline;
   private final RenderPipeline maskUpsamplePipeline;
   private final RenderPipeline bloomGaussianPipeline;
   private final RenderPipeline maskGaussianPipeline;
   private final RenderPipeline supportMaskPipeline;
   private final RenderPipeline itemBloomDownsamplePipeline;
   private final RenderPipeline itemBloomUpsamplePipeline;
   private final RenderPipeline itemBloomGaussianPipeline;
   private final RenderPipeline itemColorPipeline;
   private final RenderPipeline outerGlowPipeline;
   private final RenderPipeline handOutlinePipeline;
   private final RenderPipeline glowOutlinePipeline;
   private final RenderPipeline normalTrailDisplacePipeline;
   private final RenderPipeline energyTrailDisplacePipeline;
   private final RenderPipeline flameTrailPipeline;
   private final RenderPipeline energyTrailPipeline;
   private final RenderPipeline ribbonTrailDisplacePipeline;
   private final RenderPipeline ribbonTrailPipeline;
   private final RenderPipeline trailBlitPipeline;
   private Framebuffer handMaskFramebuffer;
   private Framebuffer supportFramebuffer;
   private final TrailSampler.TrailFramebuffer bloomTrailFramebuffers;
   private final TrailSampler.TrailFramebuffer itemBloomFramebuffers;
   private final TrailSampler.TrailFramebuffer mirrorFramebuffers;
   private SimpleFramebuffer trailHistoryFront;
   private SimpleFramebuffer trailHistoryBack;
   private GpuBuffer handsUniformBuffer;
   private GpuBuffer outerGlowUniformBuffer;
   private GpuBuffer outlineUniformBuffer;
   private GpuBuffer trailDisplaceUniformBuffer;
   private GpuBuffer flameTrailUniformBuffer;
   private GpuBuffer energyTrailUniformBuffer;
   private GpuBuffer ribbonTrailUniformBuffer;
   private long lastFrameNanos;
   private double animatedTrailTimeSeconds;
   private int trailAnimationPhase;
   private float handUseScale;
   private float handSwingScale;
   private boolean animationStateInitialized;
   private float swingTrailPulse;
   private boolean wasHandSwinging;
   private float previousCameraYaw;
   private float previousCameraPitch;
   private boolean hasPreviousCameraRotation;
   private int previousTrailStyle = -1;
   private boolean previousFirstPersonMode;
   private final TrailSampler.TrailSampleState glowSampleState = new TrailSampler.TrailSampleState();
   private final TrailSampler.TrailSampleState outlineSampleState = new TrailSampler.TrailSampleState();
   private static int pipelineIdCounter;

   private TrailSampler() {
      this.noEffectCompositePipeline = createHandsPipeline(
         "hands_nothing_composite",
         TextureFormat.RGBA8,
         Optional.of(BlendFunction.TRANSLUCENT),
         -2,
         new String[]{"HandSampler", "HandMaskSampler", "BloomSampler"},
         new String[]{"HandsData"},
         false
      );
      this.handMaskPipeline = createHandsPipeline(
         "hands_mask", TextureFormat.RED8, Optional.empty(), -2, new String[]{"HandSampler", "HandDepthSampler"}, new String[0], false
      );
      this.bloomDownsamplePipeline = createSimpleHandsPipeline("hands_downsample", TextureFormat.RGBA8, false);
      this.maskDownsamplePipeline = createSimpleHandsPipeline("hands_downsample", TextureFormat.RED8, false);
      this.maskUpsamplePipeline = createSimpleHandsPipeline("hands_upsample", TextureFormat.RED8, false);
      this.bloomGaussianPipeline = createSimpleHandsPipeline("hands_gaussian", TextureFormat.RGBA8, true);
      this.maskGaussianPipeline = createSimpleHandsPipeline("hands_gaussian", TextureFormat.RED8, true);
      this.supportMaskPipeline = createSimpleHandsPipeline("hands_support", TextureFormat.RED8, false);
      this.itemBloomDownsamplePipeline = createSimpleHandsPipeline("hands_downsample", TextureFormat.RGBA8, false);
      this.itemBloomUpsamplePipeline = createSimpleHandsPipeline("hands_upsample", TextureFormat.RGBA8, false);
      this.itemBloomGaussianPipeline = createSimpleHandsPipeline("hands_gaussian", TextureFormat.RGBA8, true);
      this.itemColorPipeline = createHandsPipeline(
         "hands_item_color", TextureFormat.RGBA8, Optional.empty(), -2, new String[]{"HandSampler", "HandMaskSampler"}, new String[0], false
      );
      this.outerGlowPipeline = createHandsPipeline(
         "hands_outer_glow",
         TextureFormat.RGBA8,
         Optional.of(BlendFunction.LIGHTNING),
         -2,
         new String[]{"BloomSampler", "HandMaskSampler"},
         new String[]{"HandsGlow"},
         false
      );
      this.handOutlinePipeline = createHandsPipeline(
         "hands_outline",
         TextureFormat.RGBA8,
         Optional.of(BlendFunction.TRANSLUCENT),
         -2,
         new String[]{"HandSampler", "BloomSampler", "HandMaskSampler"},
         new String[]{"HandsGlow"},
         false
      );
      this.glowOutlinePipeline = createHandsPipeline(
         "hands_glow_outline",
         TextureFormat.RGBA8,
         Optional.of(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA),
         -2,
         new String[]{"HandSampler", "BloomSampler", "HandMaskSampler"},
         new String[]{"HandsGlow"},
         false
      );
      this.normalTrailDisplacePipeline = createHandsPipeline(
         "hands_trail_displace", TextureFormat.RGBA8, Optional.empty(), -2, new String[]{"TrailSampler"}, new String[]{"TrailDisplace"}, false
      );
      this.energyTrailDisplacePipeline = createHandsPipeline(
         "hands_trail_energy_displace", TextureFormat.RGBA8, Optional.empty(), -2, new String[]{"TrailSampler"}, new String[]{"TrailDisplace"}, false
      );
      this.flameTrailPipeline = createHandsPipeline(
         "hands_trail_flame",
         TextureFormat.RGBA8,
         Optional.of(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA),
         -2,
         new String[]{"BloomSampler"},
         new String[]{"TrailFlame"},
         false
      );
      this.energyTrailPipeline = createHandsPipeline(
         "hands_trail_energy",
         TextureFormat.RGBA8,
         Optional.of(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA),
         -2,
         new String[]{"BloomSampler"},
         new String[]{"TrailEnergy"},
         false
      );
      this.ribbonTrailDisplacePipeline = createHandsPipeline(
         "hands_trail_ribbon_displace", TextureFormat.RGBA8, Optional.empty(), -2, new String[]{"TrailSampler"}, new String[]{"TrailDisplace"}, false
      );
      this.ribbonTrailPipeline = createHandsPipeline(
         "hands_trail_ribbon",
         TextureFormat.RGBA8,
         Optional.of(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA),
         -2,
         new String[]{"BloomSampler"},
         new String[]{"TrailRibbon"},
         false
      );
      this.trailBlitPipeline = createHandsPipeline(
         "hands_trail_blit", TextureFormat.RGBA8, Optional.of(BlendFunction.ADDITIVE), -2, new String[]{"TrailSampler"}, new String[0], false
      );

      for (int compositeStyleIndex = 0; compositeStyleIndex < this.compositeStylePipelines.length; compositeStyleIndex++) {
         this.compositeStylePipelines[compositeStyleIndex] = createCompositePipeline(compositeStyleIndex);
      }

      this.bloomTrailFramebuffers = new TrailSampler.TrailFramebuffer("hands-bloom", TextureFormat.RED8);
      this.itemBloomFramebuffers = new TrailSampler.TrailFramebuffer("hands-item-bloom", TextureFormat.RGBA8);
      this.mirrorFramebuffers = new TrailSampler.TrailFramebuffer("hands-mirror", TextureFormat.RGBA8);
   }

   public static void initialize() {
   }

   private static RenderPipeline createHandsPipeline(
      String fragmentShaderName, TextureFormat textureFormat, Optional<BlendFunction> blendFunction, int handsStyle, String[] samplerNames, String[] uniformNames, boolean useHandMask
   ) {
      String pipelineSuffix = fragmentShaderName + "_" + textureFormat.name().toLowerCase() + (handsStyle >= -1 ? "_s" + (handsStyle + 1) : "") + "_" + pipelineIdCounter++;
      Builder pipelineBuilder = RenderPipeline.builder(new Snippet[0])
         .withLocation(Identifier.of("daamky", "pipeline/hands/" + pipelineSuffix))
         .withVertexShader(Identifier.ofVanilla("core/screenquad"))
         .withFragmentShader(Identifier.of("daamky", "hands/" + fragmentShaderName))
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withDepthWrite(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST);

      for (String samplerName : samplerNames) {
         pipelineBuilder.withSampler(samplerName);
      }

      for (String uniformName : uniformNames) {
         pipelineBuilder.withUniform(uniformName, UniformType.UNIFORM_BUFFER);
      }

      if (blendFunction.isPresent()) {
         pipelineBuilder.withBlend((BlendFunction)blendFunction.get());
      } else {
         pipelineBuilder.withoutBlend();
      }

      if (useHandMask) {
         pipelineBuilder.withShaderDefine("HANDS_MASK");
      }

      if (handsStyle >= -1) {
         pipelineBuilder.withShaderDefine("HANDS_STYLE", handsStyle);
      }

      return RenderPipelines.register(pipelineBuilder.build());
   }

   private static RenderPipeline createSimpleHandsPipeline(String fragmentShaderName, TextureFormat textureFormat, boolean useMaskVariant) {
      return createHandsPipeline(
         fragmentShaderName,
         textureFormat,
         Optional.empty(),
         -2,
         new String[]{"Sampler0"},
         useMaskVariant ? new String[]{"HandsBlur"} : new String[0],
         textureFormat == TextureFormat.RED8
      );
   }

   private static RenderPipeline createCompositePipeline(int handsStyle) {
      return createHandsPipeline(
         "hands_composite",
         TextureFormat.RGBA8,
         Optional.of(BlendFunction.TRANSLUCENT),
         handsStyle,
         new String[]{"HandSampler", "HandDepthSampler", "HandMaskSampler", "HandSupportSampler", "BlurredSceneSampler", "BloomSampler"},
         new String[]{"HandsData"},
         false
      );
   }

   private static GpuSampler linearSampler() {
      return RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
   }

   private static GpuSampler nearestSampler() {
      return RenderSystem.getSamplerCache().get(FilterMode.NEAREST);
   }

   private static Std140Builder putColor(Std140Builder uniformBuilder, int argbColor) {
      return uniformBuilder.putVec4((argbColor >>> 16 & 0xFF) / 255.0F, (argbColor >>> 8 & 0xFF) / 255.0F, (argbColor & 0xFF) / 255.0F, (argbColor >>> 24 & 0xFF) / 255.0F);
   }

   private static GpuBuffer ensureGpuBuffer(GpuBuffer existingBuffer, int bufferSizeBytes, String debugLabel) {
      return existingBuffer != null && !existingBuffer.isClosed() ? existingBuffer : RenderSystem.getDevice().createBuffer(() -> debugLabel, 136, bufferSizeBytes);
   }

   private static void closeGpuBuffer(GpuBuffer gpuBuffer) {
      if (gpuBuffer != null && !gpuBuffer.isClosed()) {
         gpuBuffer.close();
      }
   }

   private static Framebuffer getOrResizeFramebuffer(Framebuffer existingFramebuffer, String debugLabel, int width, int height, TextureFormat textureFormat) {
      if (existingFramebuffer == null) {
         return new SimpleFramebuffer(debugLabel, width, height, false);
      }

      if (existingFramebuffer.textureWidth != width || existingFramebuffer.textureHeight != height) {
         existingFramebuffer.resize(width, height);
      }

      return existingFramebuffer;
   }

   private void renderGaussianPass(
      Framebuffer sourceFramebuffer, Framebuffer destinationFramebuffer, RenderPipeline gaussianPipeline, GpuBuffer blurUniformBuffer, CommandEncoder commandEncoder
   ) {
      RenderPass gaussianPass = commandEncoder.createRenderPass(() -> "handsBlur", destinationFramebuffer.getColorAttachmentView(), OptionalInt.empty());

      try {
         gaussianPass.setPipeline(gaussianPipeline);
         gaussianPass.bindTexture("Sampler0", sourceFramebuffer.getColorAttachmentView(), linearSampler());
         if (blurUniformBuffer != null) {
            gaussianPass.setUniform("HandsBlur", blurUniformBuffer);
         }

         gaussianPass.draw(0, 3);
      } catch (Throwable renderFailure) {
         if (gaussianPass != null) {
            try {
               gaussianPass.close();
            } catch (Throwable closeFailure) {
               renderFailure.addSuppressed(closeFailure);
            }
         }

         throw renderFailure;
      }

      if (gaussianPass != null) {
         gaussianPass.close();
      }
   }

   private void clearTrailHistory(CommandEncoder commandEncoder) {
      if (this.trailHistoryFront != null && this.trailHistoryBack != null) {
         commandEncoder.clearColorTexture(this.trailHistoryFront.getColorAttachment(), 0);
         commandEncoder.clearColorTexture(this.trailHistoryBack.getColorAttachment(), 0);
      }
   }

   private void ensureTrailHistoryFramebuffers(int width, int height, CommandEncoder commandEncoder) {
      if (this.trailHistoryFront == null) {
         this.trailHistoryFront = new SimpleFramebuffer("hands-trail-a", width, height, false);
         this.trailHistoryBack = new SimpleFramebuffer("hands-trail-b", width, height, false);
         this.clearTrailHistory(commandEncoder);
      } else if (this.trailHistoryFront.textureWidth != width || this.trailHistoryFront.textureHeight != height) {
         this.trailHistoryFront.resize(width, height);
         this.trailHistoryBack.resize(width, height);
         this.clearTrailHistory(commandEncoder);
      }
   }

   private void renderEnergyTrailPass(CommandEncoder commandEncoder, Framebuffer bloomFramebuffer, Hands hands, float animationPhase, float energyScale) {
      MemoryStack uniformStack = MemoryStack.stackPush();

      try {
         Std140Builder uniformBuilder = Std140Builder.onStack(uniformStack, 64)
            .putVec4(hands.getGlowBrightness(), hands.isItemColorEnabled() ? 1.0F : 0.0F, 0.0F, animationPhase)
            .putVec4(energyScale, hands.getEnergyTrailBrightness(), hands.getEnergyPulse(), 0.0F);
         putColor(uniformBuilder, hands.getGlowColorArgb());
         putColor(uniformBuilder, hands.getSecondaryGlowColorArgb());
         ByteBuffer uniformData = uniformBuilder.get();
         this.energyTrailUniformBuffer = ensureGpuBuffer(this.energyTrailUniformBuffer, 64, "handsTrailEnergyData");
         commandEncoder.writeToBuffer(this.energyTrailUniformBuffer.slice(), uniformData);
      } catch (Throwable uniformWriteFailure) {
         if (uniformStack != null) {
            try {
               uniformStack.close();
            } catch (Throwable stackCloseFailure) {
               uniformWriteFailure.addSuppressed(stackCloseFailure);
            }
         }

         throw uniformWriteFailure;
      }

      if (uniformStack != null) {
         uniformStack.close();
      }

      RenderPass energyPass = commandEncoder.createRenderPass(() -> "handsTrailEnergy", this.trailHistoryBack.getColorAttachmentView(), OptionalInt.empty());

      try {
         energyPass.setPipeline(this.energyTrailPipeline);
         energyPass.bindTexture("BloomSampler", bloomFramebuffer.getColorAttachmentView(), linearSampler());
         energyPass.setUniform("TrailEnergy", this.energyTrailUniformBuffer);
         energyPass.draw(0, 3);
      } catch (Throwable renderFailure) {
         if (energyPass != null) {
            try {
               energyPass.close();
            } catch (Throwable closeFailure) {
               renderFailure.addSuppressed(closeFailure);
            }
         }

         throw renderFailure;
      }

      if (energyPass != null) {
         energyPass.close();
      }
   }

   private void renderFlameTrailPass(CommandEncoder commandEncoder, Framebuffer bloomFramebuffer, Hands hands, float animationPhase) {
      MemoryStack uniformStack = MemoryStack.stackPush();

      try {
         Std140Builder uniformBuilder = Std140Builder.onStack(uniformStack, 48).putVec4(hands.getGlowBrightness(), hands.isItemColorEnabled() ? 1.0F : 0.0F, 0.0F, animationPhase);
         putColor(uniformBuilder, hands.getGlowColorArgb());
         putColor(uniformBuilder, hands.getSecondaryGlowColorArgb());
         ByteBuffer uniformData = uniformBuilder.get();
         this.flameTrailUniformBuffer = ensureGpuBuffer(this.flameTrailUniformBuffer, 48, "handsTrailFlameData");
         commandEncoder.writeToBuffer(this.flameTrailUniformBuffer.slice(), uniformData);
      } catch (Throwable uniformWriteFailure) {
         if (uniformStack != null) {
            try {
               uniformStack.close();
            } catch (Throwable stackCloseFailure) {
               uniformWriteFailure.addSuppressed(stackCloseFailure);
            }
         }

         throw uniformWriteFailure;
      }

      if (uniformStack != null) {
         uniformStack.close();
      }

      RenderPass flamePass = commandEncoder.createRenderPass(() -> "handsTrailFlame", this.trailHistoryBack.getColorAttachmentView(), OptionalInt.empty());

      try {
         flamePass.setPipeline(this.flameTrailPipeline);
         flamePass.bindTexture("BloomSampler", bloomFramebuffer.getColorAttachmentView(), linearSampler());
         flamePass.setUniform("TrailFlame", this.flameTrailUniformBuffer);
         flamePass.draw(0, 3);
      } catch (Throwable renderFailure) {
         if (flamePass != null) {
            try {
               flamePass.close();
            } catch (Throwable closeFailure) {
               renderFailure.addSuppressed(closeFailure);
            }
         }

         throw renderFailure;
      }

      if (flamePass != null) {
         flamePass.close();
      }
   }

   private void renderRibbonTrailPass(CommandEncoder commandEncoder, Framebuffer bloomFramebuffer, Hands hands, float ribbonScale, float animationPhase) {
      float scaledRibbonWidth = hands.getGlowBrightness() * hands.getRibbonBrightness() * this.handSwingScale;
      if (!(scaledRibbonWidth <= 0.001F)) {
         MemoryStack uniformStack = MemoryStack.stackPush();

         try {
            Std140Builder uniformBuilder = Std140Builder.onStack(uniformStack, 64)
               .putVec4(scaledRibbonWidth, animationPhase, ribbonScale, hands.getRibbonWidth())
               .putVec4(
                  hands.getRibbonCurvature(), (float)bloomFramebuffer.textureWidth / Math.max(1, bloomFramebuffer.textureHeight), hands.isItemColorEnabled() ? 1.0F : 0.0F, 0.0F
               );
            putColor(uniformBuilder, hands.getGlowColorArgb());
            putColor(uniformBuilder, hands.getSecondaryGlowColorArgb());
            this.ribbonTrailUniformBuffer = ensureGpuBuffer(this.ribbonTrailUniformBuffer, 64, "handsTrailRibbonData");
            commandEncoder.writeToBuffer(this.ribbonTrailUniformBuffer.slice(), uniformBuilder.get());
         } catch (Throwable uniformWriteFailure) {
            if (uniformStack != null) {
               try {
                  uniformStack.close();
               } catch (Throwable stackCloseFailure) {
                  uniformWriteFailure.addSuppressed(stackCloseFailure);
               }
            }

            throw uniformWriteFailure;
         }

         if (uniformStack != null) {
            uniformStack.close();
         }

         RenderPass ribbonPass = commandEncoder.createRenderPass(() -> "handsTrailRibbon", this.trailHistoryBack.getColorAttachmentView(), OptionalInt.empty());

         try {
            ribbonPass.setPipeline(this.ribbonTrailPipeline);
            ribbonPass.bindTexture("BloomSampler", bloomFramebuffer.getColorAttachmentView(), linearSampler());
            ribbonPass.setUniform("TrailRibbon", this.ribbonTrailUniformBuffer);
            ribbonPass.draw(0, 3);
         } catch (Throwable renderFailure) {
            if (ribbonPass != null) {
               try {
                  ribbonPass.close();
               } catch (Throwable closeFailure) {
                  renderFailure.addSuppressed(closeFailure);
               }
            }

            throw renderFailure;
         }

         if (ribbonPass != null) {
            ribbonPass.close();
         }
      }
   }

   private void updateTrailAnimationState(Hands hands, MinecraftClient minecraftClient, float deltaSeconds) {
      float targetHandUseScale = hands.getRibbonLength();
      float targetSwingScale = 1.0F;
      boolean isEatingOrDrinking = false;
      if (minecraftClient.player != null && minecraftClient.player.isUsingItem()) {
         UseAction useAction = minecraftClient.player.getActiveItem().getUseAction();
         isEatingOrDrinking = useAction == UseAction.EAT || useAction == UseAction.DRINK;
      }

      if (isEatingOrDrinking) {
         targetHandUseScale = hands.areRibbonsEnabledWhileEating() ? hands.getEatRibbonLength() : 0.15F;
         targetSwingScale = hands.areRibbonsEnabledWhileEating() ? hands.getEatRibbonStrength() : 0.0F;
      } else if (minecraftClient.player != null && minecraftClient.player.handSwinging) {
         targetHandUseScale = hands.areRibbonsEnabledOnHit() ? hands.getHitRibbonLength() : 0.15F;
         targetSwingScale = hands.areRibbonsEnabledOnHit() ? hands.getHitRibbonStrength() : 0.0F;
      }

      float interpolationFactor = this.animationStateInitialized ? (float)(-Math.expm1(-deltaSeconds * 16.0F)) : 1.0F;
      this.handUseScale = this.handUseScale + (targetHandUseScale - this.handUseScale) * interpolationFactor;
      this.handSwingScale = this.handSwingScale + (targetSwingScale - this.handSwingScale) * interpolationFactor;
      this.animationStateInitialized = true;
      this.animatedTrailTimeSeconds = this.animatedTrailTimeSeconds + deltaSeconds * hands.getRibbonWaveSpeed();
      this.trailAnimationPhase = this.trailAnimationPhase + 1 & 4095;
   }

   private void renderHandMask(Framebuffer handColorFramebuffer, CommandEncoder commandEncoder) {
      RenderPass maskPass = commandEncoder.createRenderPass(() -> "handsMask", this.handMaskFramebuffer.getColorAttachmentView(), OptionalInt.empty());

      try {
         maskPass.setPipeline(this.handMaskPipeline);
         maskPass.bindTexture("HandSampler", handColorFramebuffer.getColorAttachmentView(), nearestSampler());
         maskPass.bindTexture("HandDepthSampler", handColorFramebuffer.getDepthAttachmentView(), nearestSampler());
         maskPass.draw(0, 3);
      } catch (Throwable renderFailure) {
         if (maskPass != null) {
            try {
               maskPass.close();
            } catch (Throwable closeFailure) {
               renderFailure.addSuppressed(closeFailure);
            }
         }

         throw renderFailure;
      }

      if (maskPass != null) {
         maskPass.close();
      }
   }

   private void renderItemColor(Framebuffer sourceFramebuffer, Framebuffer destinationFramebuffer, CommandEncoder commandEncoder) {
      RenderPass itemColorPass = commandEncoder.createRenderPass(() -> "handsItemColor", destinationFramebuffer.getColorAttachmentView(), OptionalInt.empty());

      try {
         itemColorPass.setPipeline(this.itemColorPipeline);
         itemColorPass.bindTexture("HandSampler", sourceFramebuffer.getColorAttachmentView(), nearestSampler());
         itemColorPass.bindTexture("HandMaskSampler", this.handMaskFramebuffer.getColorAttachmentView(), nearestSampler());
         itemColorPass.draw(0, 3);
      } catch (Throwable renderFailure) {
         if (itemColorPass != null) {
            try {
               itemColorPass.close();
            } catch (Throwable closeFailure) {
               renderFailure.addSuppressed(closeFailure);
            }
         }

         throw renderFailure;
      }

      if (itemColorPass != null) {
         itemColorPass.close();
      }
   }

   private Framebuffer renderDownsampledBlur(
      Framebuffer sourceFramebuffer,
      TrailSampler.TrailFramebuffer trailFramebuffer,
      int downsamplePassCount,
      float gaussianRadius,
      boolean compositeIntoFirstLevel,
      Framebuffer firstPassSourceFramebuffer,
      CommandEncoder commandEncoder
   ) {
      Framebuffer currentDownsampleFramebuffer = sourceFramebuffer;

      for (int downsampleLevel = 0; downsampleLevel < downsamplePassCount; downsampleLevel++) {
         int downsampleWidth = Math.max(2, (currentDownsampleFramebuffer.textureWidth + 1) / 2);
         int downsampleHeight = Math.max(2, (currentDownsampleFramebuffer.textureHeight + 1) / 2);
         if (trailFramebuffer.downsampleFramebuffers.size() <= downsampleLevel) {
            trailFramebuffer.downsampleFramebuffers.add(null);
         }

         Framebuffer downsampleDestination = getOrResizeFramebuffer(trailFramebuffer.downsampleFramebuffers.get(downsampleLevel), trailFramebuffer.resourceName + downsampleLevel, downsampleWidth, downsampleHeight, trailFramebuffer.textureFormat);
         trailFramebuffer.downsampleFramebuffers.set(downsampleLevel, downsampleDestination);
         if (firstPassSourceFramebuffer != null && downsampleLevel == 0) {
            this.renderItemColor(firstPassSourceFramebuffer, downsampleDestination, commandEncoder);
         } else {
            this.renderGaussianPass(
               currentDownsampleFramebuffer,
               downsampleDestination,
               trailFramebuffer.isMaskTexture ? this.maskDownsamplePipeline : (trailFramebuffer == this.itemBloomFramebuffers ? this.itemBloomDownsamplePipeline : this.bloomDownsamplePipeline),
               null,
               commandEncoder
            );
         }

         currentDownsampleFramebuffer = downsampleDestination;
      }

      if (trailFramebuffer != this.mirrorFramebuffers && downsamplePassCount == 1 && gaussianRadius <= 0.75F) {
         return currentDownsampleFramebuffer;
      } else {
         trailFramebuffer.gaussianPingFramebuffer = getOrResizeFramebuffer(
            trailFramebuffer.gaussianPingFramebuffer, trailFramebuffer.resourceName, currentDownsampleFramebuffer.textureWidth, currentDownsampleFramebuffer.textureHeight, trailFramebuffer.textureFormat
         );
         this.updateGaussianUniforms(trailFramebuffer, currentDownsampleFramebuffer.textureWidth, currentDownsampleFramebuffer.textureHeight, gaussianRadius, commandEncoder);
         RenderPipeline gaussianPipeline = trailFramebuffer.isMaskTexture ? this.maskGaussianPipeline : (trailFramebuffer == this.itemBloomFramebuffers ? this.itemBloomGaussianPipeline : this.bloomGaussianPipeline);
         this.renderGaussianPass(currentDownsampleFramebuffer, trailFramebuffer.gaussianPingFramebuffer, gaussianPipeline, trailFramebuffer.horizontalGaussianUniformBuffer, commandEncoder);
         this.renderGaussianPass(trailFramebuffer.gaussianPingFramebuffer, currentDownsampleFramebuffer, gaussianPipeline, trailFramebuffer.verticalGaussianUniformBuffer, commandEncoder);
         if (compositeIntoFirstLevel && downsamplePassCount > 1) {
            Framebuffer firstDownsampleFramebuffer = trailFramebuffer.downsampleFramebuffers.getFirst();
            this.renderGaussianPass(
               currentDownsampleFramebuffer,
               firstDownsampleFramebuffer,
               trailFramebuffer == this.itemBloomFramebuffers ? this.itemBloomUpsamplePipeline : this.maskUpsamplePipeline,
               null,
               commandEncoder
            );
            return firstDownsampleFramebuffer;
         } else {
            return currentDownsampleFramebuffer;
         }
      }
   }

   private void updateGaussianUniforms(TrailSampler.TrailFramebuffer trailFramebuffer, int width, int height, float gaussianRadius, CommandEncoder commandEncoder) {
      boolean uniformBuffersReady = trailFramebuffer.horizontalGaussianUniformBuffer != null && !trailFramebuffer.horizontalGaussianUniformBuffer.isClosed() && trailFramebuffer.verticalGaussianUniformBuffer != null && !trailFramebuffer.verticalGaussianUniformBuffer.isClosed();
      if (!uniformBuffersReady || trailFramebuffer.cachedWidth != width || trailFramebuffer.cachedHeight != height || Float.compare(trailFramebuffer.cachedGaussianRadius, gaussianRadius) != 0) {
         trailFramebuffer.horizontalGaussianUniformBuffer = ensureGpuBuffer(trailFramebuffer.horizontalGaussianUniformBuffer, 48, trailFramebuffer.resourceName);
         trailFramebuffer.verticalGaussianUniformBuffer = ensureGpuBuffer(trailFramebuffer.verticalGaussianUniformBuffer, 48, trailFramebuffer.resourceName);
         float[] gaussianWeights = new float[7];
         float weightSum = 1.0F;
         gaussianWeights[0] = 1.0F;

         for (int weightIndex = 1; weightIndex < gaussianWeights.length; weightIndex++) {
            gaussianWeights[weightIndex] = (float)Math.exp(-(weightIndex * weightIndex) / (2.0F * gaussianRadius * gaussianRadius));
            weightSum += gaussianWeights[weightIndex] * 2.0F;
         }

         float[] sampleOffsets = new float[3];
         float[] sampleWeights = new float[3];

         for (int tapIndex = 0; tapIndex < sampleWeights.length; tapIndex++) {
            int firstTapIndex = tapIndex * 2 + 1;
            float byteBuffer = gaussianWeights[firstTapIndex] + gaussianWeights[firstTapIndex + 1];
            sampleOffsets[tapIndex] = firstTapIndex + (byteBuffer > 0.0F ? gaussianWeights[firstTapIndex + 1] / byteBuffer : 0.0F);
            sampleWeights[tapIndex] = byteBuffer / weightSum;
         }

         MemoryStack uniformStack = MemoryStack.stackPush();

         try {
            for (int blurPassIndex = 0; blurPassIndex < 2; blurPassIndex++) {
               ByteBuffer uniformData = Std140Builder.onStack(uniformStack, 48)
                  .putVec4(blurPassIndex == 0 ? 1.0F / width : 0.0F, blurPassIndex == 1 ? 1.0F / height : 0.0F, 0.0F, 0.0F)
                  .putVec4(1.0F / weightSum, sampleWeights[0], sampleWeights[1], sampleWeights[2])
                  .putVec4(sampleOffsets[0], sampleOffsets[1], sampleOffsets[2], 0.0F)
                  .get();
               commandEncoder.writeToBuffer((blurPassIndex == 0 ? trailFramebuffer.horizontalGaussianUniformBuffer : trailFramebuffer.verticalGaussianUniformBuffer).slice(), uniformData);
            }
         } catch (Throwable uniformWriteFailure) {
            if (uniformStack != null) {
               try {
                  uniformStack.close();
               } catch (Throwable stackCloseFailure) {
                  uniformWriteFailure.addSuppressed(stackCloseFailure);
               }
            }

            throw uniformWriteFailure;
         }

         if (uniformStack != null) {
            uniformStack.close();
         }

         trailFramebuffer.cachedWidth = width;
         trailFramebuffer.cachedHeight = height;
         trailFramebuffer.cachedGaussianRadius = gaussianRadius;
      }
   }

   private Framebuffer createTrailBloom(Framebuffer sourceFramebuffer, Hands hands, int downsamplePassCount, float gaussianRadius, boolean compositeIntoFirstLevel, CommandEncoder commandEncoder) {
      return this.renderDownsampledBlur(
         this.handMaskFramebuffer,
         hands.isItemColorEnabled() ? this.itemBloomFramebuffers : this.bloomTrailFramebuffers,
         downsamplePassCount,
         gaussianRadius,
         compositeIntoFirstLevel,
         hands.isItemColorEnabled() ? sourceFramebuffer : null,
         commandEncoder
      );
   }

   private void renderGlowOrOutline(
      Framebuffer bloomFramebuffer,
      Framebuffer handMaskFramebuffer,
      Framebuffer originalHandFramebuffer,
      Framebuffer outputFramebuffer,
      Hands hands,
      boolean includeFillColor,
      boolean drawOutline,
      RenderPipeline glowOrOutlinePipeline,
      CommandEncoder commandEncoder
   ) {
      float fillStrength = includeFillColor ? hands.getGlowBrightness() : 0.0F;
      int fillColorArgb = hands.getGlowColorArgb();
      int outlineColorArgb = hands.getSecondaryGlowColorArgb();
      int glowColorArgb = hands.getOutlineColorArgb();
      float effectStrength = drawOutline ? hands.getOutlineThickness() : 0.0F;
      GpuBuffer cachedUniformBuffer = drawOutline ? this.outlineUniformBuffer : this.outerGlowUniformBuffer;
      GpuBuffer effectUniformBuffer = ensureGpuBuffer(cachedUniformBuffer, 80, drawOutline ? "handsOutlineData" : "handsGlowData");
      TrailSampler.TrailSampleState sampleState = drawOutline ? this.outlineSampleState : this.glowSampleState;
      if (effectUniformBuffer != cachedUniformBuffer
         || !sampleState.matches(fillStrength, fillColorArgb, outlineColorArgb, glowColorArgb, handMaskFramebuffer.textureWidth, handMaskFramebuffer.textureHeight, effectStrength, drawOutline, hands.isItemColorEnabled())) {
         MemoryStack uniformStack = MemoryStack.stackPush();

         try {
            Std140Builder uniformBuilder = Std140Builder.onStack(uniformStack, 80).putVec4(fillStrength, hands.isItemColorEnabled() ? 1.0F : 0.0F, 0.0F, 0.0F);
            putColor(uniformBuilder, fillColorArgb);
            putColor(uniformBuilder, outlineColorArgb);
            putColor(uniformBuilder, glowColorArgb);
            commandEncoder.writeToBuffer(
               effectUniformBuffer.slice(), uniformBuilder.putVec4(handMaskFramebuffer.textureWidth, handMaskFramebuffer.textureHeight, effectStrength, drawOutline ? 1.0F : 0.0F).get()
            );
         } catch (Throwable uniformWriteFailure) {
            if (uniformStack != null) {
               try {
                  uniformStack.close();
               } catch (Throwable stackCloseFailure) {
                  uniformWriteFailure.addSuppressed(stackCloseFailure);
               }
            }

            throw uniformWriteFailure;
         }

         if (uniformStack != null) {
            uniformStack.close();
         }

         sampleState.update(fillStrength, fillColorArgb, outlineColorArgb, glowColorArgb, handMaskFramebuffer.textureWidth, handMaskFramebuffer.textureHeight, effectStrength, drawOutline, hands.isItemColorEnabled());
      }

      if (drawOutline) {
         this.outlineUniformBuffer = effectUniformBuffer;
      } else {
         this.outerGlowUniformBuffer = effectUniformBuffer;
      }

      String renderPassLabel = includeFillColor && drawOutline ? "handsGlowOutline" : (drawOutline ? "handsOutline" : "handsOuterGlow");
      RenderPass effectPass = commandEncoder.createRenderPass(() -> renderPassLabel, outputFramebuffer.getColorAttachmentView(), OptionalInt.empty());

      try {
         effectPass.setPipeline(glowOrOutlinePipeline);
         effectPass.bindTexture("BloomSampler", bloomFramebuffer.getColorAttachmentView(), linearSampler());
         effectPass.bindTexture("HandMaskSampler", handMaskFramebuffer.getColorAttachmentView(), nearestSampler());
         if (drawOutline) {
            effectPass.bindTexture("HandSampler", originalHandFramebuffer.getColorAttachmentView(), nearestSampler());
         }

         effectPass.setUniform("HandsGlow", effectUniformBuffer);
         effectPass.draw(0, 3);
      } catch (Throwable renderFailure) {
         if (effectPass != null) {
            try {
               effectPass.close();
            } catch (Throwable closeFailure) {
               renderFailure.addSuppressed(closeFailure);
            }
         }

         throw renderFailure;
      }

      if (effectPass != null) {
         effectPass.close();
      }
   }

   private void renderDisplacedTrail(Framebuffer currentTrailFramebuffer, Framebuffer outputFramebuffer, Hands hands, CommandEncoder commandEncoder) {
      this.ensureTrailHistoryFramebuffers(currentTrailFramebuffer.textureWidth, currentTrailFramebuffer.textureHeight, commandEncoder);
      boolean iIlI11IIIIl111lIl = hands.isEnergyTrailMode();
      boolean isRibbonTrail = hands.isRibbonTrailMode();
      if (this.previousTrailStyle != hands.getTrailModeIndex()) {
         this.clearTrailHistory(commandEncoder);
         this.previousTrailStyle = hands.getTrailModeIndex();
         this.resetTrailAnimationState();
      }

      long currentTimeNanos = System.nanoTime();
      float deltaSeconds = this.lastFrameNanos > 0L ? Math.min(0.1F, (float)(currentTimeNanos - this.lastFrameNanos) / 1.0E9F) : 0.016666668F;
      this.lastFrameNanos = currentTimeNanos;
      float animationTimeSeconds = (float)(currentTimeNanos % 100000000000L) / 1.0E9F;
      MinecraftClient minecraftClient = MinecraftClient.getInstance();
      if (isRibbonTrail) {
         this.updateTrailAnimationState(hands, minecraftClient, deltaSeconds);
         animationTimeSeconds = (float)this.animatedTrailTimeSeconds;
      }

      float trailAmplitude = hands.getEnergyMovement();
      float displacementSpeed = isRibbonTrail ? hands.getRibbonLift() : (iIlI11IIIIl111lIl ? 0.035F * trailAmplitude : hands.getTrailLift());
      float oscillationStrength = isRibbonTrail ? 0.0F : (iIlI11IIIIl111lIl ? 0.018F * trailAmplitude : hands.getTrailSway());
      float cameraSensitivity = isRibbonTrail ? hands.getRibbonCameraStrength() : (iIlI11IIIIl111lIl ? hands.getEnergyTrailBehindCamera() : hands.getTrailCameraOffset());
      float frameDisplacement = displacementSpeed * deltaSeconds;
      float oscillationDisplacement = ((float)Math.sin(animationTimeSeconds * (float) (Math.PI * 8.0 / 5.0)) - (float)Math.sin((animationTimeSeconds - deltaSeconds) * (float) (Math.PI * 8.0 / 5.0))) * oscillationStrength;
      float yawDisplacement = 0.0F;
      float pitchDisplacement = 0.0F;
      Camera camera = minecraftClient.gameRenderer != null ? minecraftClient.gameRenderer.getCamera() : null;
      if (camera != null) {
         float cameraYaw = camera.getYaw();
         float cameraPitch = camera.getPitch();
         if (this.hasPreviousCameraRotation) {
            float yawDelta = cameraYaw - this.previousCameraYaw;

            while (yawDelta > 180.0F) {
               yawDelta -= 360.0F;
            }

            while (yawDelta < -180.0F) {
               yawDelta += 360.0F;
            }

            float pitchDelta = cameraPitch - this.previousCameraPitch;
            float fovScale = Math.max(1.0F, ((Integer)minecraftClient.options.getFov().getValue()).intValue());
            yawDisplacement = Math.clamp(-(yawDelta / fovScale) * cameraSensitivity, -0.25F, 0.25F);
            pitchDisplacement = Math.clamp(pitchDelta / fovScale * cameraSensitivity, -0.25F, 0.25F);
         }

         this.previousCameraYaw = cameraYaw;
         this.previousCameraPitch = cameraPitch;
         this.hasPreviousCameraRotation = true;
      }

      boolean isHandSwinging = minecraftClient.player != null && minecraftClient.player.handSwinging;
      boolean startedHandSwing = hands.isDefaultTrailMode() && hands.isBlowOffOnHitEnabled() && isHandSwinging && !this.wasHandSwinging;
      this.wasHandSwinging = isHandSwinging;
      if (startedHandSwing) {
         this.swingTrailPulse = 1.0F;
      }

      float displacementScale = (
            isRibbonTrail
               ? 0.045F / Math.max(0.15F, this.handUseScale)
               : (iIlI11IIIIl111lIl ? 0.048F / Math.max(0.4F, hands.getEnergyTrailLength()) : hands.getTrailFadeSpeed())
         )
         + this.swingTrailPulse * hands.getBlowOffStrength() * 0.012F;
      this.swingTrailPulse = Math.max(0.0F, this.swingTrailPulse - deltaSeconds / 0.45F);
      float trailOpacity = isRibbonTrail ? hands.getRibbonCurvature() : (iIlI11IIIIl111lIl ? 0.32F * trailAmplitude : hands.getTrailTurbulence());
      float trailSpeed = !isRibbonTrail && !iIlI11IIIIl111lIl ? hands.getTrailFlicker() : 0.0F;
      MemoryStack uniformStack = MemoryStack.stackPush();

      try {
         ByteBuffer uniformData = Std140Builder.onStack(uniformStack, 48)
            .putVec2(oscillationDisplacement + yawDisplacement, frameDisplacement + pitchDisplacement)
            .putVec2(this.trailHistoryFront.textureWidth, this.trailHistoryFront.textureHeight)
            .putVec4(displacementScale, animationTimeSeconds, deltaSeconds, trailOpacity)
            .putVec4(isRibbonTrail ? hands.getRibbonWaveSpeed() : trailSpeed, isRibbonTrail ? this.trailAnimationPhase : 0.0F, 0.0F, 0.0F)
            .get();
         this.trailDisplaceUniformBuffer = ensureGpuBuffer(this.trailDisplaceUniformBuffer, 48, "handsTrailDisplaceData");
         commandEncoder.writeToBuffer(this.trailDisplaceUniformBuffer.slice(), uniformData);
      } catch (Throwable uniformWriteFailure) {
         if (uniformStack != null) {
            try {
               uniformStack.close();
            } catch (Throwable stackCloseFailure) {
               uniformWriteFailure.addSuppressed(stackCloseFailure);
            }
         }

         throw uniformWriteFailure;
      }

      if (uniformStack != null) {
         uniformStack.close();
      }

      RenderPass displacementPass = commandEncoder.createRenderPass(() -> "handsTrailDisplace", this.trailHistoryBack.getColorAttachmentView(), OptionalInt.of(0));

      try {
         displacementPass.setPipeline(isRibbonTrail ? this.ribbonTrailDisplacePipeline : (iIlI11IIIIl111lIl ? this.energyTrailDisplacePipeline : this.normalTrailDisplacePipeline));
         displacementPass.bindTexture("TrailSampler", this.trailHistoryFront.getColorAttachmentView(), linearSampler());
         displacementPass.setUniform("TrailDisplace", this.trailDisplaceUniformBuffer);
         displacementPass.draw(0, 3);
      } catch (Throwable displacementPassFailure) {
         if (displacementPass != null) {
            try {
               displacementPass.close();
            } catch (Throwable closeFailure) {
               displacementPassFailure.addSuppressed(closeFailure);
            }
         }

         throw displacementPassFailure;
      }

      if (displacementPass != null) {
         displacementPass.close();
      }

      if (isRibbonTrail) {
         this.renderRibbonTrailPass(commandEncoder, currentTrailFramebuffer, hands, deltaSeconds, animationTimeSeconds);
      } else if (iIlI11IIIIl111lIl) {
         this.renderEnergyTrailPass(commandEncoder, currentTrailFramebuffer, hands, deltaSeconds, animationTimeSeconds);
      } else {
         this.renderFlameTrailPass(commandEncoder, currentTrailFramebuffer, hands, deltaSeconds);
      }

      RenderPass trailBlitPass = commandEncoder.createRenderPass(() -> "handsTrailBlit", outputFramebuffer.getColorAttachmentView(), OptionalInt.empty());

      try {
         trailBlitPass.setPipeline(this.trailBlitPipeline);
         trailBlitPass.bindTexture("TrailSampler", this.trailHistoryBack.getColorAttachmentView(), linearSampler());
         trailBlitPass.draw(0, 3);
      } catch (Throwable blitPassFailure) {
         if (trailBlitPass != null) {
            try {
               trailBlitPass.close();
            } catch (Throwable closeFailure) {
               blitPassFailure.addSuppressed(closeFailure);
            }
         }

         throw blitPassFailure;
      }

      if (trailBlitPass != null) {
         trailBlitPass.close();
      }

      SimpleFramebuffer previousHistoryFront = this.trailHistoryFront;
      this.trailHistoryFront = this.trailHistoryBack;
      this.trailHistoryBack = previousHistoryFront;
   }

   private void resetTrailAnimationState() {
      this.lastFrameNanos = 0L;
      this.swingTrailPulse = 0.0F;
      this.wasHandSwinging = false;
      this.hasPreviousCameraRotation = false;
      this.animatedTrailTimeSeconds = 0.0;
      this.trailAnimationPhase = 0;
      this.animationStateInitialized = false;
   }

   private void closeTrailFramebuffer(TrailSampler.TrailFramebuffer trailFramebuffer) {
      for (Framebuffer downsampleFramebuffer : trailFramebuffer.downsampleFramebuffers) {
         downsampleFramebuffer.delete();
      }

      trailFramebuffer.downsampleFramebuffers.clear();
      if (trailFramebuffer.gaussianPingFramebuffer != null) {
         trailFramebuffer.gaussianPingFramebuffer.delete();
      }

      trailFramebuffer.gaussianPingFramebuffer = null;
      closeGpuBuffer(trailFramebuffer.horizontalGaussianUniformBuffer);
      closeGpuBuffer(trailFramebuffer.verticalGaussianUniformBuffer);
      trailFramebuffer.horizontalGaussianUniformBuffer = null;
      trailFramebuffer.verticalGaussianUniformBuffer = null;
      trailFramebuffer.cachedGaussianRadius = Float.NaN;
   }

   public void close() {
      if (this.handMaskFramebuffer != null) {
         this.handMaskFramebuffer.delete();
      }

      if (this.supportFramebuffer != null) {
         this.supportFramebuffer.delete();
      }

      this.handMaskFramebuffer = null;
      this.supportFramebuffer = null;
      this.closeTrailFramebuffer(this.bloomTrailFramebuffers);
      this.closeTrailFramebuffer(this.itemBloomFramebuffers);
      this.closeTrailFramebuffer(this.mirrorFramebuffers);
      if (this.trailHistoryFront != null) {
         this.trailHistoryFront.delete();
      }

      if (this.trailHistoryBack != null) {
         this.trailHistoryBack.delete();
      }

      this.trailHistoryFront = null;
      this.trailHistoryBack = null;
      this.resetTrailAnimationState();
      this.previousTrailStyle = -1;
      this.previousFirstPersonMode = false;
      this.glowSampleState.reset();
      this.outlineSampleState.reset();
      closeGpuBuffer(this.handsUniformBuffer);
      closeGpuBuffer(this.outerGlowUniformBuffer);
      closeGpuBuffer(this.outlineUniformBuffer);
      closeGpuBuffer(this.trailDisplaceUniformBuffer);
      closeGpuBuffer(this.flameTrailUniformBuffer);
      closeGpuBuffer(this.energyTrailUniformBuffer);
      closeGpuBuffer(this.ribbonTrailUniformBuffer);
      this.handsUniformBuffer = null;
      this.outerGlowUniformBuffer = null;
      this.outlineUniformBuffer = null;
      this.trailDisplaceUniformBuffer = null;
      this.flameTrailUniformBuffer = null;
      this.energyTrailUniformBuffer = null;
      this.ribbonTrailUniformBuffer = null;
   }

   public void render(Framebuffer handColorFramebuffer, Framebuffer outputFramebuffer, Hands hands) {
      CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
      if (this.previousFirstPersonMode != hands.isItemColorEnabled()) {
         this.previousFirstPersonMode = hands.isItemColorEnabled();
         this.glowSampleState.reset();
         this.outlineSampleState.reset();
         this.clearTrailHistory(commandEncoder);
         this.closeTrailFramebuffer(this.previousFirstPersonMode ? this.bloomTrailFramebuffers : this.itemBloomFramebuffers);
      }

      boolean useItemMode = hands.isMirrorMode();
      boolean useTrailBloom = hands.isTrailEffectEnabled();
      boolean hasOuterGlow = hands.isGlowEnabled() && hands.getGlowBrightness() > 0.001F;
      boolean needsBloomBlur = hands.isGlowEnabled() && (hasOuterGlow || hands.getInnerGlow() > 0.001F || useTrailBloom);
      int glowColorArgb = hands.getOutlineColorArgb();
      boolean hasOutline = hands.isOutlineEnabled() && hands.getOutlineThickness() > 0.001F && glowColorArgb >>> 24 != 0;
      boolean opaqueGlowOutline = hasOuterGlow && hasOutline && glowColorArgb >>> 24 == 255;
      Framebuffer blurSourceFramebuffer = handColorFramebuffer;
      if (useItemMode && hands.getColorBlend() > 0.001F) {
         blurSourceFramebuffer = this.renderDownsampledBlur(
            outputFramebuffer, this.mirrorFramebuffers, 2, (0.6F + hands.getBlurStrength() * 3.8F) * 0.25F, false, null, commandEncoder
         );
      }

      this.handMaskFramebuffer = getOrResizeFramebuffer(
         this.handMaskFramebuffer, "hands-mask", handColorFramebuffer.textureWidth, handColorFramebuffer.textureHeight, TextureFormat.RED8
      );
      this.renderHandMask(handColorFramebuffer, commandEncoder);
      boolean useDirectComposite = hands.isNothingMode();
      if (!useDirectComposite) {
         this.supportFramebuffer = getOrResizeFramebuffer(
            this.supportFramebuffer, "hands-support", Math.max(2, (handColorFramebuffer.textureWidth + 1) / 2), Math.max(2, (handColorFramebuffer.textureHeight + 1) / 2), TextureFormat.RED8
         );
         this.renderGaussianPass(this.handMaskFramebuffer, this.supportFramebuffer, this.supportMaskPipeline, null, commandEncoder);
      }

      if (!useTrailBloom && this.previousTrailStyle != -1) {
         this.clearTrailHistory(commandEncoder);
         this.previousTrailStyle = -1;
         this.resetTrailAnimationState();
      }

      Framebuffer renderedBloomFramebuffer = null;
      if (needsBloomBlur) {
         float blurStrength = Math.clamp(hands.getBlurRadius(), 1.0F, 5.0F);
         if (hands.isEnergyTrailMode()) {
            blurStrength = Math.max(3.0F, blurStrength);
         }

         int blurPassCount = blurStrength <= 1.0F ? 1 : 2;
         float gaussianRadius = (1.5F + (blurStrength - 1.0F) * 2.5F) / (1 << blurPassCount);
         renderedBloomFramebuffer = this.createTrailBloom(handColorFramebuffer, hands, blurPassCount, gaussianRadius, useTrailBloom, commandEncoder);
         if (useTrailBloom) {
            this.renderDisplacedTrail(renderedBloomFramebuffer, outputFramebuffer, hands, commandEncoder);
         }

         if (hasOuterGlow && !opaqueGlowOutline) {
            this.renderGlowOrOutline(
               renderedBloomFramebuffer, this.handMaskFramebuffer, handColorFramebuffer, outputFramebuffer, hands, true, false, this.outerGlowPipeline, commandEncoder
            );
         }
      }

      if (hasOutline) {
         if (renderedBloomFramebuffer == null) {
            renderedBloomFramebuffer = this.createTrailBloom(handColorFramebuffer, hands, 1, 0.75F, false, commandEncoder);
         }

         this.renderGlowOrOutline(
            renderedBloomFramebuffer,
            this.handMaskFramebuffer,
            handColorFramebuffer,
            outputFramebuffer,
            hands,
            opaqueGlowOutline,
            true,
            opaqueGlowOutline ? this.glowOutlinePipeline : this.handOutlinePipeline,
            commandEncoder
         );
      }

      MemoryStack uniformStack = MemoryStack.stackPush();

      try {
         float fieldOfViewDegrees = ((Integer)MinecraftClient.getInstance().options.getFov().getValue()).intValue();
         float halfFovTangent = (float)Math.tan(Math.toRadians(fieldOfViewDegrees) * 0.5);
         float framebufferAspectRatio = (float)outputFramebuffer.textureWidth / Math.max(1, outputFramebuffer.textureHeight);
         Std140Builder uniformBuilder = Std140Builder.onStack(uniformStack, 240)
            .putVec4(hands.getRenderModeIndex(), hands.getShaderStyleIndex(), hands.getElapsedSeconds(), hands.getShaderSpeed())
            .putVec4(hands.getShaderScale(), hands.getShaderGlow(), hands.getShaderOpacity(), hands.getShaderShadowStrength())
            .putVec4(
               hands.isStarFieldEnabled() ? 1.0F : 0.0F,
               hands.isShaderShadowPreserved() ? 1.0F : 0.0F,
               hands.isRainbowEnabled() ? 1.0F : 0.0F,
               hands.isShadowRetentionEnabled() ? 1.0F : 0.0F
            )
            .putVec4(hands.getFillOpacity(), hands.getShadowStrength(), hands.getRainbowSpeed(), hands.getRainbowScale())
            .putVec4(hands.getColorBlend(), hands.getMirrorDistortion(), needsBloomBlur ? 1.0F : 0.0F, hands.getBlurRadius())
            .putVec4(hands.getGlowBrightness(), hands.isItemColorEnabled() ? 1.0F : 0.0F, 0.0F, hands.getInnerGlow())
            .putVec4(framebufferAspectRatio, halfFovTangent, hands.getShaderQualitySamples(), hands.getBlurStrength());
         putColor(uniformBuilder, hands.getShaderColorArgb());
         putColor(uniformBuilder, hands.getSecondaryShaderColorArgb());
         putColor(uniformBuilder, hands.getFillColorArgb());
         putColor(uniformBuilder, hands.getBaseAccentColorArgb());
         putColor(uniformBuilder, hands.getGlowColorArgb());
         putColor(uniformBuilder, hands.getSecondaryGlowColorArgb());
         ByteBuffer uniformData = uniformBuilder.putVec2(handColorFramebuffer.textureWidth, handColorFramebuffer.textureHeight).get();
         this.handsUniformBuffer = ensureGpuBuffer(this.handsUniformBuffer, 240, "handsData");
         commandEncoder.writeToBuffer(this.handsUniformBuffer.slice(), uniformData);
      } catch (Throwable uniformWriteFailure) {
         if (uniformStack != null) {
            try {
               uniformStack.close();
            } catch (Throwable stackCloseFailure) {
               uniformWriteFailure.addSuppressed(stackCloseFailure);
            }
         }

         throw uniformWriteFailure;
      }

      if (uniformStack != null) {
         uniformStack.close();
      }

      RenderPipeline compositePipeline = useDirectComposite ? this.noEffectCompositePipeline : this.compositePipeline;
      if (!useDirectComposite && hands.isShaderMode() && hands.getShaderOpacity() > 0.001F) {
         int compositeStyleIndex = Math.clamp((int)hands.getShaderStyleIndex(), 0, this.compositeStylePipelines.length - 1);
         compositePipeline = this.compositeStylePipelines[compositeStyleIndex];
      }

      Framebuffer bloomFramebuffer = renderedBloomFramebuffer != null ? renderedBloomFramebuffer : this.handMaskFramebuffer;
      RenderPass compositePass = commandEncoder.createRenderPass(() -> "handsComposite", outputFramebuffer.getColorAttachmentView(), OptionalInt.empty());

      try {
         compositePass.setPipeline(compositePipeline);
         compositePass.bindTexture("HandSampler", handColorFramebuffer.getColorAttachmentView(), nearestSampler());
         compositePass.bindTexture("BloomSampler", bloomFramebuffer.getColorAttachmentView(), linearSampler());
         compositePass.bindTexture("HandMaskSampler", this.handMaskFramebuffer.getColorAttachmentView(), nearestSampler());
         if (!useDirectComposite) {
            compositePass.bindTexture("HandSupportSampler", this.supportFramebuffer.getColorAttachmentView(), linearSampler());
            compositePass.bindTexture("HandDepthSampler", handColorFramebuffer.getDepthAttachmentView(), nearestSampler());
            compositePass.bindTexture("BlurredSceneSampler", blurSourceFramebuffer.getColorAttachmentView(), linearSampler());
         }

         compositePass.setUniform("HandsData", this.handsUniformBuffer);
         compositePass.draw(0, 3);
      } catch (Throwable compositePassFailure) {
         if (compositePass != null) {
            try {
               compositePass.close();
            } catch (Throwable closeFailure) {
               compositePassFailure.addSuppressed(closeFailure);
            }
         }

         throw compositePassFailure;
      }

      if (compositePass != null) {
         compositePass.close();
      }
   }

   private static final class TrailFramebuffer {
      private final String resourceName;
      private final boolean isMaskTexture;
      private final TextureFormat textureFormat;
      private final List<Framebuffer> downsampleFramebuffers = new ArrayList<>(2);
      private Framebuffer gaussianPingFramebuffer;
      private GpuBuffer horizontalGaussianUniformBuffer;
      private GpuBuffer verticalGaussianUniformBuffer;
      private int cachedWidth;
      private int cachedHeight;
      private float cachedGaussianRadius = Float.NaN;

      private TrailFramebuffer(String resourceName, TextureFormat textureFormat) {
         this.resourceName = resourceName;
         this.textureFormat = textureFormat;
         this.isMaskTexture = textureFormat == TextureFormat.RED8;
      }
   }

   private static final class TrailSampleState {
      private float cachedIntensity = Float.NaN;
      private int cachedFillColor;
      private int cachedOutlineColor;
      private int cachedGlowColor;
      private int cachedWidth = -1;
      private int cachedHeight = -1;
      private float cachedGlowStrength = Float.NaN;
      private boolean cachedOutlineEnabled;
      private boolean cachedItemMode;

      private void update(
         float intensity, int fillColorArgb, int outlineColorArgb, int glowColorArgb, int width, int height, float glowStrength, boolean outlineEnabled, boolean itemMode
      ) {
         this.cachedIntensity = intensity;
         this.cachedFillColor = fillColorArgb;
         this.cachedOutlineColor = outlineColorArgb;
         this.cachedGlowColor = glowColorArgb;
         this.cachedWidth = width;
         this.cachedHeight = height;
         this.cachedGlowStrength = glowStrength;
         this.cachedOutlineEnabled = outlineEnabled;
         this.cachedItemMode = itemMode;
      }

      private void reset() {
         this.cachedIntensity = Float.NaN;
         this.cachedWidth = -1;
         this.cachedHeight = -1;
         this.cachedGlowStrength = Float.NaN;
      }

      private boolean matches(
         float intensity, int fillColorArgb, int outlineColorArgb, int glowColorArgb, int width, int height, float glowStrength, boolean outlineEnabled, boolean itemMode
      ) {
         return Float.compare(this.cachedIntensity, intensity) == 0
            && this.cachedFillColor == fillColorArgb
            && this.cachedOutlineColor == outlineColorArgb
            && this.cachedGlowColor == glowColorArgb
            && this.cachedWidth == width
            && this.cachedHeight == height
            && Float.compare(this.cachedGlowStrength, glowStrength) == 0
            && this.cachedOutlineEnabled == outlineEnabled
            && this.cachedItemMode == itemMode;
      }
   }
}

