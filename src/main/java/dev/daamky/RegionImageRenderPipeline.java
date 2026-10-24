package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class RegionImageRenderPipeline {
   public static final RenderPipeline II1Il11l111II11IIl = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.POSITION_TEX_COLOR_SNIPPET})
         .withLocation(Identifier.of("daamky", "pipeline/region_image"))
         .withVertexShader(Identifier.of("daamky", "core/region_image"))
         .withFragmentShader(Identifier.of("daamky", "core/region_image"))
         .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, DrawMode.QUADS)
         .build()
   );

   private RegionImageRenderPipeline() {
   }

   public static void II1Il11l111II11IIl() {
   }
}

