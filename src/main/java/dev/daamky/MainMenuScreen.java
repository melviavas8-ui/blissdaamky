package dev.daamky;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

public final class MainMenuScreen extends MenuBackground {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final AvatarTexture LOGO_TEXTURE = Avatar.create("logo_hd");
    private static final float MENU_BUTTON_WIDTH = 118.0F;
    private static final float MENU_BUTTON_HEIGHT = 16.0F;
    private static final float QUIT_BUTTON_GAP = 3.5F;
    private static final String[][] MAIN_MENU_ENTRIES = new String[][]{{"MultiPlayer", "C"}, {"SinglePlayer", "D"}, {"Accounts", "G"}};
    private final String MINECRAFT_VERSION_LABEL = FabricLoader.getInstance()
    .getModContainer("minecraft")
    .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
    .orElse("");
// https://t.me/pasterplace

    public MainMenuScreen() {
        super("Daamky");
    }
// https://t.me/pasterplace

    public boolean shouldCloseOnEsc() {
        return false;
    }
// https://t.me/pasterplace

    private float getMenuLeft() {
        return (this.viewportWidth - 118.0F) * 0.5F;
    }
// https://t.me/pasterplace

    private float getFirstMenuEntryY() {
        return this.viewportHeight * 0.5F - 8.0F;
    }
// https://t.me/pasterplace

    @Override
    protected void drawContent(DrawContext drawContext, float tickDelta, long animationTimeMillis) {
        float viewportCenterX = this.viewportWidth * 0.5F;
        float menuLeft = this.getMenuLeft();
        float firstEntryY = this.getFirstMenuEntryY();
        float transitionProgress = this.getTransitionProgress();
        float remainingOpacity = 1.0F - transitionProgress;
        float logoOpacity = this.easeOutCubic(0.0F, 520.0F) * remainingOpacity;
        float logoScale = 0.55F + 0.45F * this.easeOutBack(0.0F, 620.0F) + transitionProgress * 0.08F;
        float logoY = firstEntryY - 46.0F + (1.0F - this.easeOutCubic(0.0F, 520.0F)) * 8.0F;
        float logoBobOffset = (float)Math.sin(animationTimeMillis / 900.0) * 1.2F;
        this.pushPivotScale(drawContext, viewportCenterX, logoY + logoBobOffset, logoScale);
        TextureQuadBuilder.at(LOGO_TEXTURE, viewportCenterX - 13.0F, logoY - 13.0F + logoBobOffset, 26.0F, 26.0F)
        .tint(Accent.getAccentColorWithAlpha(255).toArgb())
        .opacity(logoOpacity)
        .render(drawContext);
        this.popTransformMatrix(drawContext);
        float titleOpacity = this.easeOutCubic(90.0F, 450.0F) * remainingOpacity;
        this.drawText(
        drawContext,
        BOLD_FONT,
        "Daamky",
        viewportCenterX,
        logoY + 21.0F + (1.0F - titleOpacity) * 5.0F,
        9.0F,
        HorizontalTextAlignment.CENTER,
        Accent.getTextColorWithAlpha((int)(225.0F * titleOpacity))
        );
        this.drawText(
        drawContext,
        Medium.DEFAULT_FONT,
        this.MINECRAFT_VERSION_LABEL,
        viewportCenterX,
        logoY + 29.0F + (1.0F - titleOpacity) * 5.0F,
        5.0F,
        HorizontalTextAlignment.CENTER,
        Accent.getDeepTextColor((int)(150.0F * titleOpacity))
        );
        float panelBlur = Math.min(Accent.getInstance().getCornerRadius() * 0.5F, 5.0F);

        for (int entryIndex = 0; entryIndex < MAIN_MENU_ENTRIES.length; entryIndex++) {
            float entryOpacity = this.easeOutCubic(150.0F + entryIndex * 60.0F, 480.0F) * remainingOpacity;
            float entryY = firstEntryY + entryIndex * 19.5F + (1.0F - this.easeOutCubic(150.0F + entryIndex * 60.0F, 480.0F)) * 14.0F + transitionProgress * (entryIndex + 1) * 3.0F;
            this.drawMenuEntry(drawContext, "menu" + entryIndex, MAIN_MENU_ENTRIES[entryIndex][0], MAIN_MENU_ENTRIES[entryIndex][1], menuLeft, entryY, 118.0F, panelBlur, entryOpacity);
        }

        float settingsEntrance = this.easeOutCubic(150.0F + MAIN_MENU_ENTRIES.length * 60.0F, 480.0F);
        float settingsOpacity = settingsEntrance * remainingOpacity;
        float settingsY = firstEntryY + MAIN_MENU_ENTRIES.length * 19.5F + (1.0F - settingsEntrance) * 14.0F + transitionProgress * (MAIN_MENU_ENTRIES.length + 1) * 3.0F;
        this.drawMenuEntry(drawContext, "settings", "Settings", "E", menuLeft, settingsY, 98.5F, panelBlur, settingsOpacity);
        float quitButtonX = menuLeft + 98.5F + 3.5F;
        float quitHoverProgress = this.getHoverProgress("quit", this.isMouseWithin(quitButtonX, settingsY, 16.0F, 16.0F));
        this.pushPivotScale(drawContext, quitButtonX + 8.0F, settingsY + 8.0F, (1.0F + 0.06F * quitHoverProgress) * this.getTextChangeScale("quit"));
        this.drawGlassPanel(drawContext, quitButtonX, settingsY, 16.0F, 16.0F, panelBlur, settingsOpacity, quitHoverProgress);
        RoundedGradientRectBuilder.at(quitButtonX, settingsY, 16.0F, 16.0F)
        .cornerRadius(panelBlur)
        .color(RgbaColor.rgba(235, 64, 64, (int)((10.0F + 30.0F * quitHoverProgress) * settingsOpacity)))
        .render(drawContext);
        this.drawQuitIcon(drawContext, quitButtonX + 8.0F, settingsY + 8.0F, RgbaColor.rgba(240, 70, 70, (int)((200.0F + 55.0F * quitHoverProgress) * settingsOpacity)));
        this.popTransformMatrix(drawContext);
        float sessionPanelEntrance = this.easeOutCubic(500.0F, 500.0F);
        this.drawSessionPanel(drawContext, viewportCenterX, settingsY + 16.0F + 18.0F + (1.0F - sessionPanelEntrance) * 8.0F, sessionPanelEntrance * remainingOpacity);
    }
// https://t.me/pasterplace

    private void drawMenuEntry(
    DrawContext drawContext, String hoverKey, String entryLabel, String keyLabel, float entryX, float entryY, float entryWidth, float panelBlur, float entryOpacity
    ) {
        float hoverProgress = this.getHoverProgress(hoverKey, this.isMouseWithin(entryX, entryY, entryWidth, 16.0F));
        float entryScale = (1.0F + 0.03F * hoverProgress) * this.getTextChangeScale(hoverKey);
        this.pushPivotScale(drawContext, entryX + entryWidth * 0.5F, entryY + 8.0F, entryScale);
        this.drawGlassPanel(drawContext, entryX, entryY, entryWidth, 16.0F, panelBlur, entryOpacity, hoverProgress);
        if (hoverProgress > 0.01F) {
            RoundedGradientRectBuilder.at(entryX + 1.5F, entryY + 8.0F - 3.5F * hoverProgress, 1.0F, 7.0F * hoverProgress)
            .cornerRadius(0.5F)
            .color(Accent.getAccentColorWithAlpha((int)(255.0F * entryOpacity * hoverProgress)))
            .render(drawContext);
        }
// https://t.me/pasterplace

        this.drawCenteredText(
        drawContext,
        keyLabel,
        entryX + 9.0F + 1.5F * hoverProgress,
        entryY + 8.0F,
        5.4F,
        Accent.getDeepTextColor((int)((150.0F + 80.0F * hoverProgress) * entryOpacity)).mix(Accent.getAccentColorWithAlpha((int)(255.0F * entryOpacity)), hoverProgress * 0.6F)
        );
        this.drawText(
        drawContext,
        Medium.DEFAULT_FONT,
        entryLabel,
        entryX + entryWidth * 0.5F + (entryWidth == 118.0F ? 0.0F : 4.0F),
        entryY + 8.0F,
        5.4F,
        HorizontalTextAlignment.CENTER,
        Accent.getDarkerTextColor((int)((170.0F + 85.0F * hoverProgress) * entryOpacity))
        );
        this.popTransformMatrix(drawContext);
    }
// https://t.me/pasterplace

    private void drawQuitIcon(DrawContext drawContext, float centerX, float centerY, RgbaColor l1I1IlllIlI4) {
        for (int segmentIndex = 0; segmentIndex <= 40; segmentIndex++) {
            double angleRadians = Math.toRadians(-50.0 + 280.0 * segmentIndex / 40.0);
            float arcPointX = centerX + (float)Math.cos(angleRadians) * 3.0F;
            float arcPointY = centerY + 0.4F + (float)Math.sin(angleRadians) * 3.0F;
            RoundedGradientRectBuilder.at(arcPointX - 0.45F, arcPointY - 0.45F, 0.9F, 0.9F)
            .cornerRadius(0.45F)
            .color(l1I1IlllIlI4)
            .render(drawContext);
        }

        RoundedGradientRectBuilder.at(centerX - 0.45F, centerY - 4.0F, 0.9F, 3.8F)
        .cornerRadius(0.45F)
        .color(l1I1IlllIlI4)
        .render(drawContext);
    }
// https://t.me/pasterplace

    private void drawSessionPanel(DrawContext drawContext, float centerX, float panelY, float panelOpacity) {
        String username = MinecraftClient.getInstance().getSession().getUsername();
        float usernameWidth = Medium.DEFAULT_FONT.measureWidth(username, 4.8F);
        float panelWidth = usernameWidth + 22.0F;
        float panelLeft = centerX - panelWidth * 0.5F;
        float hoverProgress = this.getHoverProgress("session", this.isMouseWithin(panelLeft, panelY - 7.0F, panelWidth, 14.0F));
        this.drawGlassPanel(drawContext, panelLeft, panelY - 7.0F, panelWidth, 14.0F, 7.0F, panelOpacity, hoverProgress);
        PlayerSkinQuadBuilder.at(panelLeft + 3.0F, panelY - 4.5F, 9.0F).localPlayerSkin().cornerRadius(2.0F).opacity(panelOpacity).render(drawContext);
        this.drawText(
        drawContext,
        Medium.DEFAULT_FONT,
        username,
        panelLeft + 15.0F,
        panelY,
        4.8F,
        HorizontalTextAlignment.LEFT,
        Accent.getDarkerTextColor((int)(220.0F * panelOpacity))
        );
    }
// https://t.me/pasterplace

    @Override
    protected boolean handleContentClick(float mouseX, float mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        float menuLeft = this.getMenuLeft();
        float firstEntryY = this.getFirstMenuEntryY();

        for (int entryIndex = 0; entryIndex < MAIN_MENU_ENTRIES.length; entryIndex++) {
            float entryY = firstEntryY + entryIndex * 19.5F;
            if (mouseX >= menuLeft && mouseX <= menuLeft + 118.0F && mouseY >= entryY && mouseY <= entryY + 16.0F) {
                this.markTextChanged("menu" + entryIndex);
                switch (entryIndex) {
                case 0:
                    this.transitionTo(new MultiplayerScreen(this));
                    break;
                case 1:
                    this.transitionTo(new SelectWorldScreen(this));
                    break;
                default:
                    this.transitionTo(new Plus(this));
                }

                return true;
            }
        }

        float settingsY = firstEntryY + MAIN_MENU_ENTRIES.length * 19.5F;

        if (mouseY >= settingsY && mouseY <= settingsY + 16.0F) {
            if (mouseX >= menuLeft && mouseX <= menuLeft + 98.5F) {
                this.markTextChanged("settings");
                this.transitionTo(new OptionsScreen(this, client.options));
                return true;
            }

            float quitButtonX = menuLeft + 98.5F + 3.5F;
            if (mouseX >= quitButtonX && mouseX <= quitButtonX + 16.0F) {
                this.markTextChanged("quit");
                client.scheduleStop();
                return true;
            }
        }

        float sessionPanelY = settingsY + 16.0F + 18.0F;
        float sessionPanelWidth = Medium.DEFAULT_FONT.measureWidth(client.getSession().getUsername(), 4.8F) + 22.0F;
        if (mouseY >= sessionPanelY - 7.0F && mouseY <= sessionPanelY + 7.0F && Math.abs(mouseX - this.viewportWidth * 0.5F) <= sessionPanelWidth * 0.5F) {
            this.transitionTo(new Plus(this));
            return true;
        } else {
            return false;
        }
    }
}

