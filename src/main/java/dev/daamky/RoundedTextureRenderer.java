package dev.daamky;

import net.minecraft.client.gui.DrawContext;

public final class RoundedTextureRenderer {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float MAX_CORNER_RADIUS = 63.5F;
// https://t.me/pasterplace

   private RoundedTextureRenderer() {
   }
// https://t.me/pasterplace

   public static void drawRoundedTexture(
      DrawContext drawContext,
      AvatarTexture avatarTexture,
      float x,
      float y,
      float width,
      float height,
      float cornerRadius,
      float textureUStart,
      float textureVStart,
      float textureUWidth,
      float textureVHeight,
      int colorArgb
   ) {
      if (!(width <= 0.0F) && !(height <= 0.0F) && !(textureUWidth <= 0.0F) && !(textureVHeight <= 0.0F) && colorArgb >>> 24 != 0) {
         float maxCornerRadius = Math.min(Math.min(width, height) * 0.5F, 63.5F);
         int packedCornerRadius = Math.clamp(Math.round(Math.clamp(cornerRadius, 0.0F, maxCornerRadius) * 2.0F), 0, 127);
         GuiRenderStateBatcher.addOrBatch(
            drawContext,
            new RoundedTextureRenderState(
               GuiRenderStateBatcher.getMatrix(drawContext),
               x,
               y,
               width,
               height,
               packedCornerRadius | packedCornerRadius << 7,
               packedCornerRadius | packedCornerRadius << 7,
               colorArgb,
               avatarTexture.textureIdentifier(),
               textureUStart,
               textureVStart,
               textureUWidth,
               textureVHeight,
               drawContext.scissorStack.peekLast()
            )
         );
      }
   }
}

