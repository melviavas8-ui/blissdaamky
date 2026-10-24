package dev.daamky.mixin.render;

import dev.daamky.DaamkyEspPreview;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GameRenderer.class)
public abstract class GuiRendererPreviewMixin {
   @ModifyArg(
      method = "<init>",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/gui/render/state/GuiRenderState;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/command/RenderDispatcher;Ljava/util/List;)V"
      ),
      index = 4
   )
   private List<SpecialGuiElementRenderer<?>> daamky$addPreviewRenderer(List<SpecialGuiElementRenderer<?>> renderers) {
      ArrayList previewRenderers = new ArrayList(renderers);
      previewRenderers.add(new DaamkyEspPreview(MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(), MinecraftClient.getInstance().getEntityRenderDispatcher()));
      return previewRenderers;
   }
}

