package dev.daamky;

import java.util.Locale;
import net.minecraft.client.gui.DrawContext;

public final class GuestAvatarRenderer {
   private GuestAvatarRenderer() {
   }

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

