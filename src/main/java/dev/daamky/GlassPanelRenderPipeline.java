package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class GlassPanelRenderPipeline {
   public static final float II1Il11l111II11IIl = 1.0F;
   public static final RenderPipeline l1I1IlllIlI = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.GUI_SNIPPET})
         .withLocation(Identifier.of("daamky", "pipeline/glass_panel"))
         .withVertexShader(Identifier.of("daamky", "core/glass_panel"))
         .withFragmentShader(Identifier.of("daamky", "core/glass_panel"))
         .withSampler("Sampler0")
         .withSampler("Sampler1")
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_LIGHT_COLOR, DrawMode.QUADS)
         .build()
   );

   private GlassPanelRenderPipeline() {
   }

   public static void II1Il11l111II11IIl() {
   }
}

