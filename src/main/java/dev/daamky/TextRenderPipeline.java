package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class TextRenderPipeline {
   public static final RenderPipeline II1Il11l111II11IIl = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.POSITION_TEX_COLOR_SNIPPET})
         .withLocation(Identifier.of("daamky", "pipeline/text"))
         .withVertexShader(Identifier.of("daamky", "core/text"))
         .withFragmentShader(Identifier.of("daamky", "core/text"))
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_LIGHT_COLOR, DrawMode.QUADS)
         .build()
   );

   private TextRenderPipeline() {
   }

   public static void II1Il11l111II11IIl() {
   }
}

