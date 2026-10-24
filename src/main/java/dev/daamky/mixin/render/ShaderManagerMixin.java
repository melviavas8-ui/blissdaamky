package dev.daamky.mixin.render;

import java.util.List;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShaderLoader.class)
public abstract class ShaderManagerMixin {
   @Unique
   private static final Logger DAAMKY_LOGGER = LoggerFactory.getLogger("Daamky/Shaders");

   @Redirect(
      method = "apply(Lnet/minecraft/client/gl/ShaderLoader$Definitions;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
      at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0)
   )
   private boolean daamky$skipOptionalPipeline(List<Object> pipelineList, Object pipelineEntry) {
      if (pipelineEntry instanceof Identifier pipelineId && "daamky".equals(pipelineId.getNamespace())) {
         DAAMKY_LOGGER.warn("Pipeline {} is not supported by this GPU driver, effect disabled", pipelineId);
         return false;
      } else {
         return pipelineList.add(pipelineEntry);
      }
   }
}

