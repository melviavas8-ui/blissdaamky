package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class JumpCirclePostPipeline {
    private static final String p7cdd2082 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public static final RenderPipeline II1Il11l111II11IIl = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[0])
         .withLocation(Identifier.of("daamky", "pipeline/post/jump_circle"))
         .withVertexShader(Identifier.ofVanilla("core/screenquad"))
         .withFragmentShader(Identifier.of("daamky", "post/jump_circle"))
         .withSampler("SceneSampler")
         .withSampler("DepthSampler")
         .withUniform("WorldJumpCircleUniforms", UniformType.UNIFORM_BUFFER)
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withoutBlend()
         .withDepthWrite(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .build()
   );
// https://t.me/pasterplace

   private JumpCirclePostPipeline() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl() {
   }
}

