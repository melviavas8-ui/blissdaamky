package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class WorldPostProcessPipelines {
   public static final RenderPipeline II1Il11l111II11IIl = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[0])
         .withLocation(Identifier.of("daamky", "pipeline/post/world_fx"))
         .withVertexShader(Identifier.ofVanilla("core/screenquad"))
         .withFragmentShader(Identifier.of("daamky", "post/world_fx"))
         .withSampler("SceneSampler")
         .withSampler("DepthSampler")
         .withUniform("WorldFxUniforms", UniformType.UNIFORM_BUFFER)
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withoutBlend()
         .withDepthWrite(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .build()
   );
   public static final RenderPipeline l1I1IlllIlI = RenderPipelines.register(
      RenderPipeline.builder(new Snippet[0])
         .withLocation(Identifier.of("daamky", "pipeline/post/beautifuly"))
         .withVertexShader(Identifier.ofVanilla("core/screenquad"))
         .withFragmentShader(Identifier.of("daamky", "post/beautifuly"))
         .withSampler("SceneSampler")
         .withUniform("BeautifulyUniforms", UniformType.UNIFORM_BUFFER)
         .withVertexFormat(VertexFormats.EMPTY, DrawMode.TRIANGLES)
         .withoutBlend()
         .withDepthWrite(false)
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .build()
   );

   private WorldPostProcessPipelines() {
   }

   public static void II1Il11l111II11IIl() {
   }
}

