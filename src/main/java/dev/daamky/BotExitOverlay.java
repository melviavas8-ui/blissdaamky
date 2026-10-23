package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class BotExitOverlay {
    private static final String p18d709f4 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final BotExitOverlay II1Il11l111II11IIl = new BotExitOverlay();
// https://t.me/pasterplace

    private BotExitOverlay() {
    }
// https://t.me/pasterplace

    public static BotExitOverlay II1Il11l111II11IIl() {
        return II1Il11l111II11IIl;
    }
// https://t.me/pasterplace

    @EventTarget
    public void II1Il11l111II11IIl(EventDisplay eventDisplay) {
        DrawContext drawContext = eventDisplay.graphics();
        float screenWidth = GuiEventRenderer.getScreenWidth();
        float screenHeight = GuiEventRenderer.getScreenHeight();
        if (SavedBots.isActiveSessionReady() && MinecraftClient.getInstance().currentScreen == null) {
            String disconnectHint = "ESC — выйти из бота";
// https://t.me/pasterplace

            float hintWidth = Medium.DEFAULT_FONT.measureWidth(disconnectHint, 7.0F);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, disconnectHint, (screenWidth - hintWidth) / 2.0F, screenHeight - 50.0F, 7.0F)
            .color(-1258291201)
            .render(drawContext);
        }

        float switchNoticeAlpha = SavedBots.getSwitchNoticeAlpha();
        if (switchNoticeAlpha > 0.0F) {
            int overlayAlpha = (int)(switchNoticeAlpha * 255.0F);
            RoundedGradientRectBuilder.at(0.0F, 0.0F, screenWidth, screenHeight).cornerRadius(0.0F).color(overlayAlpha << 24).render(drawContext);
        }
    }
}

