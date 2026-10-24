package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

public final class GuiRenderStateBatcher {
   private static final List<GuiRenderStateBatch> pendingBatches = new ArrayList<>();
   private static Matrix3x2f cachedMatrix = new Matrix3x2f();
   private static @Nullable DrawContext activeDrawContext;

   private GuiRenderStateBatcher() {
   }

   public static void beginBatching(DrawContext drawContext) {
      flush();
      activeDrawContext = drawContext;
   }

   public static void beginHudBatching(DrawContext drawContext) {
      beginBatching(drawContext);
   }

   public static void endBatching() {
      flush();
      activeDrawContext = null;
   }

   public static boolean isBatching() {
      return activeDrawContext != null;
   }

   public static void flush() {
      DrawContext drawContext = activeDrawContext;
      if (drawContext != null && !pendingBatches.isEmpty()) {
         int batchIndex = 0;

         for (int pendingBatchCount = pendingBatches.size(); batchIndex < pendingBatchCount; batchIndex++) {
            Object renderBatch = pendingBatches.get(batchIndex);
            ((GuiRenderStateBatch)renderBatch).II1Il11l111II11IIl();
            drawContext.state.addSimpleElement((SimpleGuiElementRenderState)renderBatch);
         }

         pendingBatches.clear();
      }
   }

   public static void addOrBatch(DrawContext drawContext, SimpleGuiElementRenderState simpleGuiElementRenderState) {
      DrawContext drawContext2 = activeDrawContext;
      if (drawContext2 != null && drawContext2.state == drawContext.state) {
         ScreenRect screenRect = simpleGuiElementRenderState.bounds();
         if (screenRect == null) {
            flush();
            drawContext.state.addSimpleElement(simpleGuiElementRenderState);
         } else {
            int left = screenRect.getLeft();
            int top = screenRect.getTop();
            int right = left + screenRect.width();
            int bottom = top + screenRect.height();
            if (right > left && bottom > top) {
               RenderPipeline renderPipeline = simpleGuiElementRenderState.pipeline();
               TextureSetup textureSetup = simpleGuiElementRenderState.textureSetup();
               ScreenRect screenRect2 = simpleGuiElementRenderState.scissorArea();
               GuiRenderStateBatch simpleGuiElementRenderStateImpl3 = null;

               for (int batchIndex = pendingBatches.size() - 1; batchIndex >= 0; batchIndex--) {
                  Object renderBatch = pendingBatches.get(batchIndex);
                  if (simpleGuiElementRenderStateImpl3 == null
                     && ((GuiRenderStateBatch)renderBatch).l1I1IlllIlI(renderPipeline, textureSetup, screenRect2)) {
                     simpleGuiElementRenderStateImpl3 = (GuiRenderStateBatch)renderBatch;
                  }

                  if (((GuiRenderStateBatch)renderBatch).intersectsAny(left, top, right, bottom)) {
                     break;
                  }
               }

               if (simpleGuiElementRenderStateImpl3 == null) {
                  simpleGuiElementRenderStateImpl3 = createBatch(renderPipeline, textureSetup, screenRect2);
                  pendingBatches.add(simpleGuiElementRenderStateImpl3);
               }

               simpleGuiElementRenderStateImpl3.II1Il11l111II11IIl(simpleGuiElementRenderState, left, top, right, bottom);
            }
         }
      } else {
         drawContext.state.addSimpleElement(simpleGuiElementRenderState);
      }
   }

   public static Matrix3x2f getMatrix(DrawContext drawContext) {
      Matrix3x2fStack matrix3x2fStack = drawContext.getMatrices();
      if (!cachedMatrix.equals(matrix3x2fStack, 0.0F)) {
         cachedMatrix = new Matrix3x2f(matrix3x2fStack);
      }

      return cachedMatrix;
   }

   private static GuiRenderStateBatch createBatch(RenderPipeline renderPipeline, TextureSetup textureSetup, @Nullable ScreenRect screenRect) {
      GuiRenderStateBatch simpleGuiElementRenderStateImpl3 = new GuiRenderStateBatch();
      simpleGuiElementRenderStateImpl3.II1Il11l111II11IIl(renderPipeline, textureSetup, screenRect);
      return simpleGuiElementRenderStateImpl3;
   }
}

