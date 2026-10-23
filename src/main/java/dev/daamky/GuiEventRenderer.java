package dev.daamky;

import dev.daamky.core.event.impl.EventDisplay;
import dev.daamky.core.event.impl.EventHudLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.render.state.GuiRenderState;

public final class GuiEventRenderer {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public static final float GUI_RENDER_SCALE = 2.0F;
// https://t.me/pasterplace

   private GuiEventRenderer() {
   }
// https://t.me/pasterplace

   public static float getScreenWidth() {
      return MinecraftClient.getInstance().getWindow().getFramebufferWidth() / 2.0F;
   }
// https://t.me/pasterplace

   public static float getScreenHeight() {
      return MinecraftClient.getInstance().getWindow().getFramebufferHeight() / 2.0F;
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

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
// https://t.me/pasterplace

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
// https://t.me/pasterplace

   public static void flushBatchedElements() {
      GuiRenderStateBatcher.flush();
   }
// https://t.me/pasterplace

   public static void flushAndStartRootLayer(DrawContext drawContext) {
      GuiRenderStateBatcher.flush();
      drawContext.createNewRootLayer();
   }
}

