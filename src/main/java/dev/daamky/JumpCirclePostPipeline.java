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

   private JumpCirclePostPipeline() {
   }

   public static void II1Il11l111II11IIl() {
   }
}

