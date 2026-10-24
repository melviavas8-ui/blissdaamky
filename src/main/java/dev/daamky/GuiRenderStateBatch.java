package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jspecify.annotations.Nullable;

public final class GuiRenderStateBatch implements SimpleGuiElementRenderState {
   private static final int II1Il11l111II11IIl = 4096;
   private final List<SimpleGuiElementRenderState> l1I1IlllIlI = new ArrayList<>(32);
   private int[] IllI1l1l1 = new int[128];
   private RenderPipeline l1lll11l1l;
   private TextureSetup lIIl11l111lIIl1ll;
   private @Nullable ScreenRect l1IIl11lI;
   private @Nullable ScreenRect I1lIIII1III1lIlIl1l;
   private int l11lIlIIll1III1;
   private int intValue;
   private int Ill111IlI11l;
   private int I1l11IIlIIIll1l1lI1II;

   void II1Il11l111II11IIl(RenderPipeline renderPipeline, TextureSetup textureSetup, @Nullable ScreenRect screenRect) {
      this.l1lll11l1l = renderPipeline;
      this.lIIl11l111lIIl1ll = textureSetup;
      this.l1IIl11lI = screenRect;
      this.I1lIIII1III1lIlIl1l = null;
      this.l11lIlIIll1III1 = Integer.MAX_VALUE;
      this.intValue = Integer.MAX_VALUE;
      this.Ill111IlI11l = Integer.MIN_VALUE;
      this.I1l11IIlIIIll1l1lI1II = Integer.MIN_VALUE;
   }

   boolean l1I1IlllIlI(RenderPipeline renderPipeline, TextureSetup textureSetup, @Nullable ScreenRect screenRect) {
      return this.l1I1IlllIlI.size() < 4096
         && this.l1lll11l1l == renderPipeline
         && Objects.equals(this.l1IIl11lI, screenRect)
         && this.lIIl11l111lIIl1ll.equals(textureSetup);
   }

   boolean intersectsAny(int left, int top, int right, int bottom) {
      if (left < this.Ill111IlI11l && right > this.l11lIlIIll1III1 && top < this.I1l11IIlIIIll1l1lI1II && bottom > this.intValue) {
         int[] boundsByElement = this.IllI1l1l1;
         int coordinateIndex = 0;

         for (int coordinateCount = this.l1I1IlllIlI.size() << 2; coordinateIndex < coordinateCount; coordinateIndex += 4) {
            if (left < boundsByElement[coordinateIndex + 2]
               && right > boundsByElement[coordinateIndex]
               && top < boundsByElement[coordinateIndex + 3]
               && bottom > boundsByElement[coordinateIndex + 1]) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   void II1Il11l111II11IIl(SimpleGuiElementRenderState simpleGuiElementRenderState, int left, int top, int right, int bottom) {
      int vertexArrayIndex = this.l1I1IlllIlI.size() << 2;
      if (vertexArrayIndex + 4 > this.IllI1l1l1.length) {
         this.IllI1l1l1 = Arrays.copyOf(this.IllI1l1l1, this.IllI1l1l1.length << 1);
      }

      this.IllI1l1l1[vertexArrayIndex] = left;
      this.IllI1l1l1[vertexArrayIndex + 1] = top;
      this.IllI1l1l1[vertexArrayIndex + 2] = right;
      this.IllI1l1l1[vertexArrayIndex + 3] = bottom;
      this.l1I1IlllIlI.add(simpleGuiElementRenderState);
      this.l11lIlIIll1III1 = Math.min(this.l11lIlIIll1III1, left);
      this.intValue = Math.min(this.intValue, top);
      this.Ill111IlI11l = Math.max(this.Ill111IlI11l, right);
      this.I1l11IIlIIIll1l1lI1II = Math.max(this.I1l11IIlIIIll1l1lI1II, bottom);
   }

   void II1Il11l111II11IIl() {
      this.I1lIIII1III1lIlIl1l = new ScreenRect(
         this.l11lIlIIll1III1, this.intValue, this.Ill111IlI11l - this.l11lIlIIll1III1, this.I1l11IIlIIIll1l1lI1II - this.intValue
      );
   }

   public void setupVertices(VertexConsumer vertices) {
      List items = this.l1I1IlllIlI;
      int elementIndex = 0;

      for (int elementCount = items.size(); elementIndex < elementCount; elementIndex++) {
         ((SimpleGuiElementRenderState)items.get(elementIndex)).setupVertices(vertices);
      }
   }

   public RenderPipeline pipeline() {
      return this.l1lll11l1l;
   }

   public TextureSetup textureSetup() {
      return this.lIIl11l111lIIl1ll;
   }

   public @Nullable ScreenRect scissorArea() {
      return this.l1IIl11lI;
   }

   public @Nullable ScreenRect bounds() {
      return this.I1lIIII1III1lIlIl1l;
   }
}

