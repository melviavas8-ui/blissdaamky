package dev.daamky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.ShapeIndexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.util.BufferAllocator;

final class BufferedWorldGeometry {
    private static final String pa702500c = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final DrawMode II1Il11l111II11IIl;
   private final VertexFormat l1I1IlllIlI;
   private final RenderPipeline IllI1l1l1;
   private final BufferAllocator l1lll11l1l = new BufferAllocator(65536);
   private BufferBuilder lIIl11l111lIIl1ll;
   private GpuBuffer l1IIl11lI;
   private int I1lIIII1III1lIlIl1l;
// https://t.me/pasterplace

   BufferedWorldGeometry(DrawMode drawMode, VertexFormat vertexFormat, RenderPipeline renderPipeline) {
      this.II1Il11l111II11IIl = drawMode;
      this.l1I1IlllIlI = vertexFormat;
      this.IllI1l1l1 = renderPipeline;
   }
// https://t.me/pasterplace

   BufferBuilder II1Il11l111II11IIl() {
      if (this.lIIl11l111lIIl1ll == null) {
         this.lIIl11l111lIIl1ll = new BufferBuilder(this.l1lll11l1l, this.II1Il11l111II11IIl, this.l1I1IlllIlI);
      }

      return this.lIIl11l111lIIl1ll;
   }
// https://t.me/pasterplace

   boolean l1I1IlllIlI() {
      return this.lIIl11l111lIIl1ll != null;
   }
// https://t.me/pasterplace

   void IllI1l1l1() {
      this.I1lIIII1III1lIlIl1l = 0;
      if (this.lIIl11l111lIIl1ll != null) {
         BufferBuilder bufferBuilder = this.lIIl11l111lIIl1ll;
         this.lIIl11l111lIIl1ll = null;
         BuiltBuffer builtBuffer = bufferBuilder.endNullable();

         label58: {
            try {
               if (builtBuffer == null) {
                  break label58;
               }

               ByteBuffer byteBuffer = builtBuffer.getBuffer();
               int bufferSize = byteBuffer.remaining();
               if (this.l1IIl11lI == null || this.l1IIl11lI.isClosed() || this.l1IIl11lI.size() < bufferSize) {
                  if (this.l1IIl11lI != null) {
                     this.l1IIl11lI.close();
                  }

                  this.l1IIl11lI = RenderSystem.getDevice()
                     .createBuffer(() -> "Daamky world geometry", 40, Math.max(16384, Integer.highestOneBit(bufferSize - 1) << 1));
               }

               RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.l1IIl11lI.slice(0L, bufferSize), byteBuffer);
               this.I1lIIII1III1lIlIl1l = builtBuffer.getDrawParameters().indexCount();
               RenderSystem.getSequentialBuffer(this.II1Il11l111II11IIl).getIndexBuffer(this.I1lIIII1III1lIlIl1l);
            } catch (Throwable uploadFailure) {
               if (builtBuffer != null) {
                  try {
                     builtBuffer.close();
                  } catch (Throwable builtBufferCloseFailure) {
                     uploadFailure.addSuppressed(builtBufferCloseFailure);
                  }
               }
// https://t.me/pasterplace

               throw uploadFailure;
            }

            if (builtBuffer != null) {
               builtBuffer.close();
            }

            return;
         }

         if (builtBuffer != null) {
            builtBuffer.close();
         }
      }
   }
// https://t.me/pasterplace

   void II1Il11l111II11IIl(RenderPass renderPass, GpuBufferSlice gpuBufferSlice, List<GpuBuffer> items) {
      if (this.I1lIIII1III1lIlIl1l > 0) {
         int indexCount = this.I1lIIII1III1lIlIl1l;
         this.I1lIIII1III1lIlIl1l = 0;
         renderPass.setPipeline(this.IllI1l1l1);
         RenderSystem.bindDefaultUniforms(renderPass);
         renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
         renderPass.setVertexBuffer(0, this.l1IIl11lI);
         ShapeIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(this.II1Il11l111II11IIl);
         renderPass.setIndexBuffer(sequentialBuffer.getIndexBuffer(indexCount), sequentialBuffer.getIndexType());
         renderPass.drawIndexed(0, 0, indexCount, 1);
      }
   }
// https://t.me/pasterplace

   void l1lll11l1l() {
      this.I1lIIII1III1lIlIl1l = 0;
      if (this.lIIl11l111lIIl1ll != null) {
         BuiltBuffer builtBuffer = this.lIIl11l111lIIl1ll.endNullable();
         if (builtBuffer != null) {
            builtBuffer.close();
         }

         this.lIIl11l111lIIl1ll = null;
      }

      this.l1lll11l1l.clear();
   }
}

