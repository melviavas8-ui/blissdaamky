package dev.daamky;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;

final class Down implements AutoCloseable {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final int MAX_BLUR_LEVELS = 7;
   private final SimpleFramebuffer[] framebuffers = new SimpleFramebuffer[7];
   private final SimpleFramebuffer[] temporaryFramebuffers = new SimpleFramebuffer[7];
   private final Map<KawaseGaussianBlurPass, SimpleFramebuffer> passFramebuffers = new HashMap<>();
   private int framebufferWidth;
   private int framebufferHeight;
// https://t.me/pasterplace

   boolean ensureFramebufferSize(Framebuffer framebuffer) {
      if (framebuffer == null || framebuffer.textureWidth <= 0 || framebuffer.textureHeight <= 0) {
         return false;
      }

      if (framebuffer.textureWidth == this.framebufferWidth && framebuffer.textureHeight == this.framebufferHeight) {
         return false;
      }
// https://t.me/pasterplace

      this.close();
      this.framebufferWidth = framebuffer.textureWidth;
      this.framebufferHeight = framebuffer.textureHeight;
      return true;
   }
// https://t.me/pasterplace

   SimpleFramebuffer getFramebuffer(int framebufferIndex) {
      return this.framebufferWidth > 0 && this.framebufferHeight > 0 && framebufferIndex >= 0 && framebufferIndex < 7 ? this.getFramebuffer(this.framebuffers, framebufferIndex, "down") : null;
   }
// https://t.me/pasterplace

   SimpleFramebuffer getTemporaryFramebuffer(int temporaryFramebufferIndex) {
      return this.framebufferWidth > 0 && this.framebufferHeight > 0 && temporaryFramebufferIndex >= 0 && temporaryFramebufferIndex < 7 ? this.getFramebuffer(this.temporaryFramebuffers, temporaryFramebufferIndex, "temp") : null;
   }
// https://t.me/pasterplace

   SimpleFramebuffer getPassFramebuffer(KawaseGaussianBlurPass iI1Il11l111II11IIl8) {
      if (this.framebufferWidth > 0 && this.framebufferHeight > 0) {
         return iI1Il11l111II11IIl8.mode() == KawaseGaussianBlurMode.KAWASE && iI1Il11l111II11IIl8.passIndex() == 1
            ? this.getFramebuffer(1)
            : this.passFramebuffers
               .computeIfAbsent(
                  iI1Il11l111II11IIl8,
                  iI1Il11l111II11IIl8x -> this.createFramebuffer(
                     "out " + iI1Il11l111II11IIl8x.mode() + " " + iI1Il11l111II11IIl8x.passIndex(),
                     iI1Il11l111II11IIl8x.mode().II1Il11l111II11IIl(iI1Il11l111II11IIl8x.passIndex())
                  )
               );
      } else {
         return null;
      }
   }
// https://t.me/pasterplace

   @Override
   public void close() {
      deleteFramebuffers(this.framebuffers);
      deleteFramebuffers(this.temporaryFramebuffers);
      this.passFramebuffers.values().forEach(Framebuffer::delete);
      this.passFramebuffers.clear();
      this.framebufferWidth = 0;
      this.framebufferHeight = 0;
   }
// https://t.me/pasterplace

   private SimpleFramebuffer getFramebuffer(SimpleFramebuffer[] simpleFramebufferArray, int framebufferIndex, String framebufferLabel) {
      if (simpleFramebufferArray[framebufferIndex] == null) {
         simpleFramebufferArray[framebufferIndex] = this.createFramebuffer(framebufferLabel, framebufferIndex);
      }

      return simpleFramebufferArray[framebufferIndex];
   }
// https://t.me/pasterplace

   private SimpleFramebuffer createFramebuffer(String framebufferLabel, int framebufferIndex) {
      return new SimpleFramebuffer(
         "blur " + framebufferLabel + " L" + framebufferIndex, downsampleDimension(this.framebufferWidth, framebufferIndex), downsampleDimension(this.framebufferHeight, framebufferIndex), false
      );
   }
// https://t.me/pasterplace

   private static int downsampleDimension(int dimension, int downsampleLevel) {
      int downsampledDimension = dimension;

      for (int levelIndex = 0; levelIndex < downsampleLevel; levelIndex++) {
         downsampledDimension = Math.max(1, downsampledDimension + 1 >> 1);
      }

      return downsampledDimension;
   }
// https://t.me/pasterplace

   private static void deleteFramebuffers(SimpleFramebuffer[] simpleFramebufferArray) {
      Arrays.stream(simpleFramebufferArray).filter(simpleFramebuffer -> simpleFramebuffer != null).forEach(Framebuffer::delete);
      Arrays.fill(simpleFramebufferArray, null);
   }
}

