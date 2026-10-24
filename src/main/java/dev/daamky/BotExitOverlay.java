package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class BotExitOverlay {
    private static final BotExitOverlay II1Il11l111II11IIl = new BotExitOverlay();

    private BotExitOverlay() {
    }

    public static BotExitOverlay II1Il11l111II11IIl() {
        return II1Il11l111II11IIl;
    }

    @EventTarget
    public void II1Il11l111II11IIl(EventDisplay eventDisplay) {
        DrawContext drawContext = eventDisplay.graphics();
        float screenWidth = GuiEventRenderer.getScreenWidth();
        float screenHeight = GuiEventRenderer.getScreenHeight();
        if (SavedBots.isActiveSessionReady() && MinecraftClient.getInstance().currentScreen == null) {
            String disconnectHint = "ESC — выйти из бота";

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

