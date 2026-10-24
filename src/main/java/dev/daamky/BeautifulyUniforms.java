package dev.daamky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import org.lwjgl.system.MemoryStack;

public final class BeautifulyUniforms {
   private static final BeautifulyUniforms II1Il11l111II11IIl = new BeautifulyUniforms();
   private static final int l1I1IlllIlI = new Std140SizeCalculator().putVec4().get();
   private GpuBuffer IllI1l1l1;
   private SimpleFramebuffer l1lll11l1l;

   public static BeautifulyUniforms II1Il11l111II11IIl() {
      return II1Il11l111II11IIl;
   }

   public void l1I1IlllIlI() {
      Beautifuly beautifuly = Beautifuly.l1I1IIl111Il1l1l();
      if (beautifuly != null && beautifuly.isEnabled()) {
         float saturation = beautifuly.IIIlll111l111();
         float warmth = beautifuly.lI1IlIl1I11II111l11l();
         if (saturation != 1.0F || warmth != 0.0F) {
            Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
            if (framebuffer != null && framebuffer.textureWidth > 0 && framebuffer.textureHeight > 0 && framebuffer.getColorAttachment() != null) {
               if (this.l1lll11l1l == null || this.l1lll11l1l.textureWidth != framebuffer.textureWidth || this.l1lll11l1l.textureHeight != framebuffer.textureHeight) {
                  if (this.l1lll11l1l != null) {
                     this.l1lll11l1l.delete();
                  }

                  this.l1lll11l1l = new SimpleFramebuffer("daamky beautifuly copy", framebuffer.textureWidth, framebuffer.textureHeight, false);
               }

               if (this.IllI1l1l1 == null || this.IllI1l1l1.isClosed()) {
                  this.IllI1l1l1 = RenderSystem.getDevice().createBuffer(() -> "daamky beautifuly ubo", 136, l1I1IlllIlI);
               }

               RenderSystem.getDevice()
                  .createCommandEncoder()
                  .copyTextureToTexture(
                     framebuffer.getColorAttachment(), this.l1lll11l1l.getColorAttachment(), 0, 0, 0, 0, 0, framebuffer.textureWidth, framebuffer.textureHeight
                  );
               MemoryStack memoryStack = MemoryStack.stackPush();

               try {
                  Std140Builder std140Builder = Std140Builder.onStack(memoryStack, l1I1IlllIlI).putVec4(saturation, warmth, 0.0F, 0.0F);
                  RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.IllI1l1l1.slice(), std140Builder.get());
               } catch (Throwable bufferWriteFailure) {
                  if (memoryStack != null) {
                     try {
                        memoryStack.close();
                     } catch (Throwable stackCloseFailure) {
                        bufferWriteFailure.addSuppressed(stackCloseFailure);
                     }
                  }

                  throw bufferWriteFailure;
               }

               if (memoryStack != null) {
                  memoryStack.close();
               }

               RenderPass renderPass = RenderSystem.getDevice()
                  .createCommandEncoder()
                  .createRenderPass(() -> "daamky beautifuly", framebuffer.getColorAttachmentView(), OptionalInt.empty());

               try {
                  renderPass.setPipeline(WorldPostProcessPipelines.l1I1IlllIlI);
                  RenderSystem.bindDefaultUniforms(renderPass);
                  renderPass.setUniform("BeautifulyUniforms", this.IllI1l1l1);
                  renderPass.bindTexture("SceneSampler", this.l1lll11l1l.getColorAttachmentView(), RenderSystem.getSamplerCache().get(FilterMode.NEAREST));
                  renderPass.draw(0, 3);
               } catch (Throwable renderPassFailure) {
                  if (renderPass != null) {
                     try {
                        renderPass.close();
                     } catch (Throwable renderPassCloseFailure) {
                        renderPassFailure.addSuppressed(renderPassCloseFailure);
                     }
                  }

                  throw renderPassFailure;
               }

               if (renderPass != null) {
                  renderPass.close();
               }
            }
         }
      }
   }
}

