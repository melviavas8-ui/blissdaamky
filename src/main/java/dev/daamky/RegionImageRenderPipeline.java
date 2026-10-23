package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class RegionImageRenderPipeline {
    private static final String p4940efbd = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public static final RenderPipeline II1Il11l111II11IIl = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.POSITION_TEX_COLOR_SNIPPET})
         .withLocation(Identifier.of("daamky", "pipeline/region_image"))
         .withVertexShader(Identifier.of("daamky", "core/region_image"))
         .withFragmentShader(Identifier.of("daamky", "core/region_image"))
         .withVertexFormat(VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, DrawMode.QUADS)
         .build()
   );
// https://t.me/pasterplace

   private RegionImageRenderPipeline() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl() {
   }
}

