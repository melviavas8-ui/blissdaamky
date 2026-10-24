package dev.daamky;

import dev.daamky.core.event.impl.EventDisplay;
import dev.daamky.core.event.impl.EventHudLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;

public final class GuiEventRenderer {
   public static final float GUI_RENDER_SCALE = 2.0F;

   private GuiEventRenderer() {
   }

   public static float getScreenWidth() {
      return MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 2.0F;
   }

   public static float getScreenHeight() {
      return MinecraftClient.getInstance().getWindow().getFramebufferHeight() / 2.0F;
   }

   public static void dispatchDisplayEvent(MinecraftClient client, GuiRenderState guiRenderState) {
      if (client != null && guiRenderState != null && client.getWindow() != null) {
         int windowScaleFactor = client.getWindow().getScaleFactor();
         if (windowScaleFactor > 0) {
            DrawContext drawContext = new DrawContext(client, guiRenderState, 0, 0);
            drawContext.createNewRootLayer();
            drawContext.getMatrices().pushMatrix();
            drawContext.getMatrices().scale(2.0F / windowScaleFactor);
            GuiRenderStateBatcher.beginBatching(drawContext);

            try {
               ClientInitializer.getEventBus().post(EventDisplay.get(drawContext));
            } finally {
               GuiRenderStateBatcher.endBatching();
               drawContext.getMatrices().popMatrix();
            }
         }
      }
   }

   public static void dispatchHudLayerEvent(MinecraftClient client, GuiRenderState guiRenderState) {
      if (client != null && guiRenderState != null && client.getWindow() != null && ClientInitializer.getEventBus() != null) {
         if (client.isFinishedLoading() && client.getOverlay() == null) {
            int windowScaleFactor = client.getWindow().getScaleFactor();
            if (windowScaleFactor > 0) {
               DrawContext drawContext = new DrawContext(client, guiRenderState, 0, 0);
               drawContext.createNewRootLayer();
               drawContext.getMatrices().pushMatrix();
               drawContext.getMatrices().scale(2.0F / windowScaleFactor);
               GuiRenderStateBatcher.beginHudBatching(drawContext);

               try {
                  ClientInitializer.getEventBus().post(EventHudLayer.get(drawContext));
               } finally {
                  GuiRenderStateBatcher.endBatching();
                  drawContext.getMatrices().popMatrix();
               }

               drawContext.createNewRootLayer();
            }
         }
      }
   }

   public static void flushBatchedElements() {
      GuiRenderStateBatcher.flush();
   }

   public static void flushAndStartRootLayer(DrawContext drawContext) {
      GuiRenderStateBatcher.flush();
      drawContext.createNewRootLayer();
   }
}

