package dev.daamky;

import java.util.Locale;
import net.minecraft.client.gui.DrawContext;

public final class GuestAvatarRenderer {
    private static final String pd195155f = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private GuestAvatarRenderer() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(DrawContext drawContext, float x, float y, float size, float cornerRadius, float opacity) {
      RoundedGradientRectBuilder.at(x, y, size, size)
         .cornerRadius(cornerRadius)
         .verticalGradient(Accent.getAccentColorWithAlpha((int)(230.0F * opacity)), Accent.getAccentColorWithAlpha((int)(120.0F * opacity)))
         .render(drawContext);
      String guestName = Guest.l1I1IlllIlI();
      String guestInitial = guestName != null && !guestName.isBlank() ? guestName.substring(0, 1).toUpperCase(Locale.ROOT) : "?";
      GuiTextBuilder.createAt(Medium.DEFAULT_FONT, guestInitial, x + size * 0.5F, y + size * 0.5F, size * 0.5F)
         .centered()
         .color(RgbaColor.rgba(255, 255, 255, (int)(250.0F * opacity)))
         .render(drawContext);
   }
}

