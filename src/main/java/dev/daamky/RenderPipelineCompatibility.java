package dev.daamky;

import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RenderPipelineCompatibility {
    private static final String pafe1eb74 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final Logger II1Il11l111II11IIl = LoggerFactory.getLogger("Daamky/RenderCompat");
   private static Boolean l1I1IlllIlI;
// https://t.me/pasterplace

   private RenderPipelineCompatibility() {
   }
// https://t.me/pasterplace

   public static boolean II1Il11l111II11IIl() {
      if (l1I1IlllIlI != null) {
         return l1I1IlllIlI;
      }

      GpuDevice device;
      try {
         device = RenderSystem.getDevice();
      } catch (Throwable deviceLookupFailure) {
         return false;
      }

      if (device == null) {
         return false;
      }

      boolean allPipelinesValid = true;

      try {
         II1Il11l111II11IIl.info("GPU: {} | {} | {}", new Object[]{device.getVendor(), device.getRenderer(), device.getVersion()});
      } catch (Throwable deviceInfoFailure) {
      }
// https://t.me/pasterplace

      RenderPipeline[] pipelinesToValidate = new RenderPipeline[]{
         RoundedRectangleRenderPipeline.l1I1IlllIlI, ImageRenderPipeline.l1I1IlllIlI, RegionImageRenderPipeline.II1Il11l111II11IIl, TextRenderPipeline.II1Il11l111II11IIl, PlayerHeadRenderPipeline.l1I1IlllIlI
      };

      for (RenderPipeline pipeline : pipelinesToValidate) {
         try {
            CompiledRenderPipeline compiledRenderPipeline = device.precompilePipeline(pipeline);
            if (compiledRenderPipeline == null || !compiledRenderPipeline.isValid()) {
               II1Il11l111II11IIl.error("Pipeline {} failed to compile on this GPU, falling back to vanilla menu", pipeline.getLocation());
               allPipelinesValid = false;
            }
         } catch (Throwable pipelineCompileFailure) {
            II1Il11l111II11IIl.error("Pipeline {} crashed during compile, falling back to vanilla menu", pipeline.getLocation(), pipelineCompileFailure);
            allPipelinesValid = false;
         }
      }

      l1I1IlllIlI = allPipelinesValid;
      return allPipelinesValid;
   }
}

