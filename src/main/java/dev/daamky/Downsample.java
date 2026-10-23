package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class Downsample {
    private static final String p8986600c = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public static final float II1Il11l111II11IIl = 1.0F;
   public static final RenderPipeline l1I1IlllIlI = II1Il11l111II11IIl("downsample", "blur_down", false, false);
   public static final RenderPipeline IllI1l1l1 = II1Il11l111II11IIl("upsample", "blur_up", false, false);
   public static final RenderPipeline l1lll11l1l = II1Il11l111II11IIl("gaussian_h", "blur_separable", false, false);
   public static final RenderPipeline lIIl11l111lIIl1ll = II1Il11l111II11IIl("gaussian_v", "blur_separable", true, false);
   public static final RenderPipeline l1IIl11lI = II1Il11l111II11IIl("box_h", "blur_separable", false, true);
   public static final RenderPipeline I1lIIII1III1lIlIl1l = II1Il11l111II11IIl("box_v", "blur_separable", true, true);
   public static final RenderPipeline l11lIlIIll1III1 = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[]{RenderPipelines.POSITION_TEX_COLOR_SNIPPET})
         .withLocation(II1Il11l111II11IIl("pipeline/blur_panel"))
         .withVertexShader(II1Il11l111II11IIl("core/blur_panel"))
         .withFragmentShader(II1Il11l111II11IIl("core/blur_panel"))
         .withVertexFormat(VertexFormats.POSITION_TEXTURE_LIGHT_COLOR, DrawMode.QUADS)
         .build()
   );
// https://t.me/pasterplace

   private Downsample() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl() {
   }
// https://t.me/pasterplace

   private static RenderPipeline II1Il11l111II11IIl(String pipelineName, String fragmentShaderName, boolean verticalBlur, boolean boxKernel) {
      Builder builder = RenderPipeline.builder(new Snippet[0])
         .withLocation(II1Il11l111II11IIl("pipeline/blur_" + pipelineName))
         .withVertexShader(II1Il11l111II11IIl("core/blur_quad"))
         .withFragmentShader(II1Il11l111II11IIl("core/" + fragmentShaderName))
         .withSampler("InSampler")
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withoutBlend()
         .withDepthWrite(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST);
      if (verticalBlur) {
         builder.withShaderDefine("BLUR_VERTICAL");
      }

      if (boxKernel) {
         builder.withShaderDefine("KERNEL_BOX");
      }
// https://t.me/pasterplace

      return RenderPipelines.register(builder.build());
   }
// https://t.me/pasterplace

   private static Identifier II1Il11l111II11IIl(String resourcePath) {
      return Identifier.of("daamky", resourcePath);
   }
}

