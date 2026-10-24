package dev.daamky;

import dev.daamky.mixin.client.BossHealthOverlayAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.PlayerListEntry;

public final class Watermark extends HudElementPosition {
    private static final float AVATAR_SIZE = 16.0F;
    private static final float HUD_EDGE_PADDING = 7.0F;
    private static final float SETTINGS_PANEL_WIDTH = 118.0F;
    private static final float PANEL_INNER_PADDING = 4.0F;
    private static final float PANEL_SECTION_HEIGHT = 17.0F;
    private static final float PANEL_ROW_SPACING = 15.0F;
    private static final float SETTINGS_PANEL_HEIGHT = 81.0F;
    private static final float TOGGLE_ITEM_PADDING = 5.0F;
    private static final float TOGGLE_MARKER_SIZE = 5.0F;
    private static final float TOGGLE_TRACK_WIDTH = 13.0F;
    private static final float TOGGLE_TRACK_HEIGHT = 7.0F;
    private static final float TOGGLE_MARKER_INSET = 2.0F;
    private static final float TOGGLE_TRACK_RADIUS = 1.5F;
    private static final float WATERMARK_TITLE_FONT_SIZE = 5.4F;
    private static final float STATUS_LABEL_FONT_SIZE = 4.8F;
    private static final float PANEL_LABEL_FONT_SIZE = 4.6F;
    private static final String[] POSITION_OPTIONS = new String[]{"Слева", "Центр", "Справа"};
    private static final String[] METRIC_OPTIONS = new String[]{"User", "Ping", "FPS"};
    private final MultiToggleSetting metricVisibilitySetting;
    private final MultiOptionSetting watermarkStyleSetting;
    private final MultiOptionSetting positionSetting;
    private float currentWatermarkWidth = 160.0F;
    private float targetWatermarkWidth = 160.0F;
    private float animatedFps = 60.0F;
    private float animatedPing = 0.0F;
    private float currentAlignedX = Float.NaN;
    private float currentAlignedY = Float.NaN;
    private boolean settingsPanelOpen;
    private float settingsPanelProgress;
    private float animatedPositionSelection = -1.0F;
    private final float[] metricVisibilityProgress = new float[METRIC_OPTIONS.length];
    private float settingsPanelX;
    private float settingsPanelY;
    private float recordingOverlayVisibility;
    private float finetuneOverlayVisibility;
    private float recordingOverlayWidth = 70.0F;
    private float finetuneProgress;

    public Watermark(MultiToggleSetting metricVisibilitySetting, MultiOptionSetting styleSetting, MultiOptionSetting positionSetting) {
        super("Watermark", "watermark", 7.0F, 7.0F);
        this.metricVisibilitySetting = metricVisibilitySetting;
        this.watermarkStyleSetting = styleSetting;
        this.positionSetting = positionSetting;
        this.elementWidth = this.currentWatermarkWidth;
        this.elementHeight = 16.0F;
    }

    @Override
    public void updateAnimation(float tickDelta) {
        this.animatedFps = interpolate(this.animatedFps, CLIENT.getCurrentFps(), 7.0F, tickDelta);
        this.animatedPing = interpolate(this.animatedPing, this.getPlayerPing(), 7.0F, tickDelta);
        this.currentWatermarkWidth = interpolate(this.currentWatermarkWidth, this.targetWatermarkWidth, 8.0F, tickDelta);
        this.elementWidth = this.currentWatermarkWidth;
        if (!(CLIENT.currentScreen instanceof ChatScreen)) {
            this.settingsPanelOpen = false;
        }

        this.settingsPanelProgress = interpolate(this.settingsPanelProgress, this.settingsPanelOpen ? 1.0F : 0.0F, 12.0F, tickDelta);
        if (!this.settingsPanelOpen && this.settingsPanelProgress < 0.002F) {
            this.settingsPanelProgress = 0.0F;
        }

        int selectedPositionIndex = this.positionSetting.getSelectedIndex();
        this.animatedPositionSelection = this.animatedPositionSelection < 0.0F ? selectedPositionIndex : interpolate(this.animatedPositionSelection, selectedPositionIndex, 14.0F, tickDelta);

        for (int metricIndex = 0; metricIndex < METRIC_OPTIONS.length; metricIndex++) {
            this.metricVisibilityProgress[metricIndex] = interpolate(
            this.metricVisibilityProgress[metricIndex], this.metricVisibilitySetting.isOptionEnabled(METRIC_OPTIONS[metricIndex]) ? 1.0F : 0.0F, 12.0F, tickDelta
            );
        }

        NeuroRotationController finetune = NeuroRotationController.getInstance();
        boolean isFinetuneActive = finetune.isTrainingOverlayVisible();
        this.recordingOverlayVisibility = interpolate(this.recordingOverlayVisibility, !isFinetuneActive && !finetune.isRecording() ? 0.0F : 1.0F, 9.0F, tickDelta);
        this.finetuneOverlayVisibility = interpolate(this.finetuneOverlayVisibility, isFinetuneActive ? 1.0F : 0.0F, 9.0F, tickDelta);
        this.finetuneProgress = interpolate(this.finetuneProgress, finetune.getTrainingProgress(), 6.0F, tickDelta);
        float targetAlignedX = this.getAlignedX();
        float targetAlignedY = this.getAlignedY();
        this.currentAlignedX = Float.isNaN(this.currentAlignedX) ? targetAlignedX : interpolate(this.currentAlignedX, targetAlignedX, 10.0F, tickDelta);
        this.currentAlignedY = Float.isNaN(this.currentAlignedY) ? targetAlignedY : interpolate(this.currentAlignedY, targetAlignedY, 10.0F, tickDelta);
    }

    private float getAlignedX() {
        float screenWidth = GuiEventRenderer.getScreenWidth();

        return switch (this.positionSetting.getSelectedIndex()) {
        case 1 -> (screenWidth - this.elementWidth) * 0.5F;
        case 2 -> screenWidth - this.elementWidth - 7.0F;
            default -> 7.0F;
        };
    }

    private float getAlignedY() {
        if (this.positionSetting.getSelectedIndex() != 1) {
            return 7.0F;
        }

        int bossBarCount = this.getBossBarCount();
        if (bossBarCount == 0) {
            return 7.0F;
        }

        float windowScale = CLIENT.getWindow().getScaleFactor();
        return (bossBarCount * 19.0F - 2.0F) * windowScale / 2.0F + 1.0F;
    }

    private int getBossBarCount() {
        return CLIENT.inGameHud != null && !NoRender.II1Il11l111II11IIl("Босс-бар")
        ? ((BossHealthOverlayAccessor)CLIENT.inGameHud.getBossBarHud()).daamky$getEvents().size()
        : 0;
    }

    @Override
    public float getX() {
        return Float.isNaN(this.currentAlignedX) ? this.getAlignedX() : this.currentAlignedX;
    }

    @Override
    public float getY() {
        return Float.isNaN(this.currentAlignedY) ? this.getAlignedY() : this.currentAlignedY;
    }

    @Override
    public boolean isHiddenByPosition() {
        return false;
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    public boolean handlePrimaryClick(float mouseX, float mouseY) {
        if (this.settingsPanelOpen && containsRectangle(mouseX, mouseY, this.settingsPanelX, this.settingsPanelY, 118.0F, 81.0F)) {
            float optionsStartY = this.settingsPanelY + 17.0F;
            if (mouseY >= optionsStartY) {
                int clickedRowIndex = (int)((mouseY - optionsStartY) / 15.0F);
                if (clickedRowIndex == 0) {
                    this.positionSetting.setSelectedIndex((this.positionSetting.getSelectedIndex() + 1) % POSITION_OPTIONS.length);
                } else if (clickedRowIndex <= METRIC_OPTIONS.length) {
                    BooleanSetting metricSetting = this.metricVisibilitySetting.getOption(METRIC_OPTIONS[clickedRowIndex - 1]);
                    metricSetting.setValue(!metricSetting.getValue());
                }
            }

            return true;
        } else {
            this.settingsPanelOpen = false;
            return false;
        }
    }

    @Override
    public boolean handleSecondaryClick(float mouseX, float mouseY) {
        if (this.containsPoint(mouseX, mouseY)) {
            this.settingsPanelOpen = !this.settingsPanelOpen;
            return true;
        } else {
            return false;
        }
    }

    private static boolean containsRectangle(float pointX, float pointY, float rectangleX, float rectangleY, float rectangleWidth, float rectangleHeight) {
        return pointX >= rectangleX && pointX <= rectangleX + rectangleWidth && pointY >= rectangleY && pointY <= rectangleY + rectangleHeight;
    }

    private int getPlayerPing() {
        if (CLIENT.getNetworkHandler() != null && CLIENT.player != null) {
            PlayerListEntry playerEntry = CLIENT.getNetworkHandler().getPlayerListEntry(CLIENT.player.getUuid());
            if (playerEntry != null) {
                return Math.max(0, playerEntry.getLatency());
            }
        }

        return 0;
    }

    @Override
    public boolean isVisibleInCurrentScreen(boolean chatScreenOpen) {
        return true;
    }

    @Override
    public void render(HudRenderContext renderContext, float renderAlpha) {
        DrawContext drawContext = renderContext.drawContext();
        float watermarkX = this.getX();
        float watermarkY = this.getY();
        BrokkeIcons.drawPanelBackground(drawContext, watermarkX, watermarkY, this.elementWidth, 16.0F, renderAlpha);
        int watermarkColorArgb = this.getStyleColor(renderContext.elapsedTime(), renderAlpha);
        float contentX = watermarkX + 5.0F;
        float textBaselineY = watermarkY + 8.0F;
        float avatarIconSize = 9.0F;
        TextureQuadBuilder.at(Avatar.LOGO_AVATAR, contentX, textBaselineY - avatarIconSize * 0.5F, avatarIconSize, avatarIconSize)
        .tint(watermarkColorArgb)
        .opacity(renderAlpha)
        .render(drawContext);
        contentX += avatarIconSize + 3.5F;
        BrokkeIcons.drawCenteredText(drawContext, "Daamky", contentX, textBaselineY, 6.2F, Accent.getTextColorWithAlpha((int)(255.0F * renderAlpha)));
        contentX += BrokkeIcons.measureTextWidth("Daamky", 6.2F) + 5.5F;
        if (this.metricVisibilitySetting.isOptionEnabled("User")) {
            contentX = this.drawWatermarkMetric(drawContext, contentX, textBaselineY, "D", Guest.l1I1IlllIlI(), watermarkColorArgb, renderAlpha);
        }

        if (this.metricVisibilitySetting.isOptionEnabled("Ping")) {
            contentX = this.drawWatermarkMetric(drawContext, contentX, textBaselineY, "E", Math.round(this.animatedPing) + " ms", watermarkColorArgb, renderAlpha);
        }

        if (this.metricVisibilitySetting.isOptionEnabled("FPS")) {
            contentX = this.drawWatermarkMetric(drawContext, contentX, textBaselineY, "C", Math.max(1, Math.round(this.animatedFps)) + " fps", watermarkColorArgb, renderAlpha);
        }

        this.targetWatermarkWidth = contentX - watermarkX + 1.0F;
        if (this.recordingOverlayVisibility > 0.01F) {
            this.renderFinetuneOverlay(drawContext, watermarkX, watermarkY, renderAlpha * this.recordingOverlayVisibility, watermarkColorArgb);
        }

        if (this.settingsPanelProgress > 0.005F) {
            renderContext.postRenderCallbacks().add(() -> this.renderSettingsPanel(drawContext, renderAlpha * this.settingsPanelProgress, watermarkColorArgb));
        }
    }

    private void renderFinetuneOverlay(DrawContext drawContext, float renderAlpha, float watermarkX, float watermarkY, int accentColorArgb) {
        NeuroRotationController finetuneModule = NeuroRotationController.getInstance();
        float recordingEaseOutAlpha = 1.0F - (1.0F - this.recordingOverlayVisibility) * (1.0F - this.recordingOverlayVisibility);
        float finetuneTransition = this.finetuneOverlayVisibility * this.finetuneOverlayVisibility * (3.0F - 2.0F * this.finetuneOverlayVisibility);
        String recordingLabel = "REC " + (finetuneModule.getRecordingName() == null ? "" : finetuneModule.getRecordingName());
        String elapsedTicksLabel = finetuneModule.getRecordedSampleCount() + " тиков";
        float contentWidthTarget = 14.0F + BrokkeIcons.measureTextWidth(recordingLabel, 4.8F) + 10.0F + BrokkeIcons.measureTextWidth(elapsedTicksLabel, 4.8F) + 6.0F;
        float minimumOverlayWidth = Math.max(130.0F, Math.min(this.elementWidth, 170.0F));
        float targetOverlayWidth = contentWidthTarget + (minimumOverlayWidth - contentWidthTarget) * finetuneTransition;
        this.recordingOverlayWidth = this.recordingOverlayWidth + (targetOverlayWidth - this.recordingOverlayWidth) * 0.25F;
        float currentOverlayWidth = this.recordingOverlayWidth;
        float overlayHeight = (11.0F + 17.0F * finetuneTransition) * recordingEaseOutAlpha;
        if (!(overlayHeight < 1.0F)) {
            float overlayX = renderAlpha + this.elementWidth * 0.5F - currentOverlayWidth * 0.5F;
            float overlayY = watermarkX + 16.0F + 2.0F;
            BrokkeIcons.drawRoundedPanelBackground(drawContext, overlayX, overlayY, currentOverlayWidth, overlayHeight, Math.min(5.0F, overlayHeight * 0.5F), watermarkY);
            float recordingOpacity = watermarkY * Math.clamp((recordingEaseOutAlpha - 0.55F) / 0.45F, 0.0F, 1.0F);
            if (!(recordingOpacity <= 0.01F)) {
                float recordingTextAlpha = recordingOpacity * (1.0F - finetuneTransition);
                if (recordingTextAlpha > 0.01F) {
                    float recordingTextY = overlayY + 5.5F;
                    float pulseAlpha = 0.55F + 0.45F * (float)Math.sin(System.currentTimeMillis() / 220.0);
                    BrokkeIcons.drawColorSwatch(
                    drawContext, overlayX + 7.0F, recordingTextY, 3.4F, RgbaColor.rgba(235, 75, 75, (int)(255.0F * recordingTextAlpha * pulseAlpha))
                    );
                    float recordingMarkerX = overlayX + 12.0F;
                    BrokkeIcons.drawCenteredText(drawContext, recordingLabel, recordingMarkerX, recordingTextY, 4.8F, Accent.getTextColorWithAlpha((int)(240.0F * recordingTextAlpha)));
                    recordingMarkerX += BrokkeIcons.measureTextWidth(recordingLabel, 4.8F) + 2.5F;
                    BrokkeIcons.drawVerticalDivider(drawContext, recordingMarkerX, recordingTextY, recordingTextAlpha);
                    recordingMarkerX += 5.0F;
                    BrokkeIcons.drawCenteredText(drawContext, elapsedTicksLabel, recordingMarkerX, recordingTextY, 4.8F, BrokkeIcons.mutedGrayWithAlpha(recordingTextAlpha));
                }

                float finetuneOpacity = recordingOpacity * finetuneTransition;
                if (!(finetuneOpacity <= 0.01F)) {
                    float finetuneIconX = overlayX + 6.0F;
                    float finetuneContentWidth = currentOverlayWidth - 12.0F;
                    float finetuneIconY = overlayY + 6.5F;
                    TextureQuadBuilder.at(Avatar.LOGO_AVATAR, finetuneIconX, finetuneIconY - 3.0F, 6.0F, 6.0F)
                    .tint(accentColorArgb)
                    .opacity(finetuneOpacity)
                    .render(drawContext);
                    String finetuneName = "Neuro · " + finetuneModule.getTrainingModelName();
                    BrokkeIcons.drawCenteredText(
                    drawContext,
                    Medium.DEFAULT_FONT.truncateToWidth(finetuneName, 4.8F, finetuneContentWidth * 0.55F),
                    finetuneIconX + 8.5F,
                    finetuneIconY,
                    4.8F,
                    Accent.getTextColorWithAlpha((int)(250.0F * finetuneOpacity))
                    );
                    String finetuneStatusText = finetuneModule.getDisplayInfo();
                    boolean hasFinetuneError = finetuneModule.hasTrainingError();
                    RgbaColor finetuneStatusColor = hasFinetuneError
                    ? RgbaColor.rgba(235, 90, 90, (int)(245.0F * finetuneOpacity))
                    : RgbaColor.fromArgb(accentColorArgb).multiplyAlpha(finetuneOpacity);
                    BrokkeIcons.drawCenteredText(drawContext, finetuneStatusText, finetuneIconX + finetuneContentWidth - BrokkeIcons.measureTextWidth(finetuneStatusText, 4.8F), finetuneIconY, 4.8F, finetuneStatusColor);
                    float progressBarY = overlayY + 12.5F;
                    RgbaColor progressTrackColor = hasFinetuneError ? RgbaColor.rgba(235, 90, 90, (int)(255.0F * finetuneOpacity)) : RgbaColor.fromArgb(accentColorArgb);
                    BrokkeIcons.drawProgressBar(drawContext, finetuneIconX, progressBarY, finetuneContentWidth, 3.0F, Math.clamp(this.finetuneProgress, 0.0F, 1.0F), progressTrackColor, finetuneOpacity);
                    float progressLabelY = overlayY + 21.5F;
                    String progressPercentLabel = Math.round(Math.clamp(this.finetuneProgress, 0.0F, 1.0F) * 100.0F) + "%";
                    BrokkeIcons.drawCenteredText(
                    drawContext,
                    Medium.DEFAULT_FONT.truncateToWidth(finetuneModule.getTrainingSummary(), 4.2F, finetuneContentWidth - 18.0F),
                    finetuneIconX,
                    progressLabelY,
                    4.2F,
                    BrokkeIcons.mutedGrayWithAlpha(finetuneOpacity)
                    );
                    BrokkeIcons.drawCenteredText(
                    drawContext,
                    progressPercentLabel,
                    finetuneIconX + finetuneContentWidth - BrokkeIcons.measureTextWidth(progressPercentLabel, 4.2F),
                    progressLabelY,
                    4.2F,
                    Accent.getTextColorWithAlpha((int)(230.0F * finetuneOpacity))
                    );
                }
            }
        }
    }

    private void renderSettingsPanel(DrawContext drawContext, float renderAlpha, int watermarkColorArgb) {
        float screenWidth = GuiEventRenderer.getScreenWidth();
        float screenHeight = GuiEventRenderer.getScreenHeight();
        float watermarkX = this.getX();
        float watermarkY = this.getY();
        float panelX = Math.clamp(watermarkX + this.elementWidth * 0.5F - 59.0F, 7.0F, screenWidth - 118.0F - 7.0F);
        boolean panelAppearsBelow = watermarkY + 16.0F + 4.0F + 81.0F < screenHeight;
        float panelY = panelAppearsBelow ? watermarkY + 16.0F + 4.0F : watermarkY - 81.0F - 4.0F;
        this.settingsPanelX = panelX;
        this.settingsPanelY = panelY;
        float panelAlpha = 1.0F - (1.0F - this.settingsPanelProgress) * (1.0F - this.settingsPanelProgress);
        float animatedPanelY = panelY + (panelAppearsBelow ? -6.0F : 6.0F) * (1.0F - panelAlpha);
        BrokkeIcons.drawPanelBackground(drawContext, panelX, animatedPanelY, 118.0F, 81.0F, renderAlpha);
        float contentLeft = panelX + 4.0F;
        float contentWidth = 110.0F;
        float contentRight = contentLeft + contentWidth - 4.0F;
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Watermark", contentLeft + 4.0F, animatedPanelY + 8.5F, 5.4F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getTextColorWithAlpha((int)(255.0F * renderAlpha)))
        .render(drawContext);
        BrokkeIcons.drawHorizontalDivider(drawContext, contentLeft, animatedPanelY + 17.0F - 1.0F, contentWidth, renderAlpha);
        float headerY = animatedPanelY + 17.0F;
        float rowY = headerY + 7.5F;
        drawDecorativeDots(drawContext, contentLeft + 4.0F, rowY, renderAlpha);
        drawSettingsLabel(drawContext, "Позиция", contentLeft, rowY, renderAlpha);
        String positionLabel = POSITION_OPTIONS[this.positionSetting.getSelectedIndex()];
        float positionLabelWidth = Medium.DEFAULT_FONT.measureWidth(positionLabel, 4.6F);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, positionLabel, contentRight - positionLabelWidth, rowY, 4.6F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getTextColorWithAlpha((int)(245.0F * renderAlpha)))
        .render(drawContext);
        int panelBackgroundArgb = watermarkColorArgb & 16777215 | (int)((watermarkColorArgb >>> 24 & 0xFF) * this.settingsPanelProgress) << 24;
        float positionOptionsX = contentRight - positionLabelWidth - 4.0F - (2.0F * (POSITION_OPTIONS.length + 2) + 1.5F * (POSITION_OPTIONS.length - 1));

        for (int positionOptionIndex = 0; positionOptionIndex < POSITION_OPTIONS.length; positionOptionIndex++) {
            float positionOptionProgress = Math.clamp(1.0F - Math.abs(this.animatedPositionSelection - positionOptionIndex), 0.0F, 1.0F);
            float positionOptionWidth = 2.0F + 4.0F * positionOptionProgress;
            RoundedGradientRectBuilder.at(positionOptionsX, rowY - 1.0F, positionOptionWidth, 2.0F)
            .cornerRadius(1.0F)
            .color(ColorUtils.interpolateColor(Accent.getTextColorWithAlpha((int)(45.0F * renderAlpha)).toArgb(), panelBackgroundArgb, positionOptionProgress))
            .render(drawContext);
            positionOptionsX += positionOptionWidth + 1.5F;
        }

        for (int metricIndex = 0; metricIndex < METRIC_OPTIONS.length; metricIndex++) {
            headerY += 15.0F;
            rowY = headerY + 7.5F;
            float metricVisibility = this.metricVisibilityProgress[metricIndex];
            RoundedGradientRectBuilder.at(contentLeft + 4.0F, rowY - 2.5F, 5.0F, 5.0F)
            .cornerRadius(1.5F)
            .color(
            ColorUtils.interpolateColor(
            Accent.getTextColorWithAlpha((int)(45.0F * renderAlpha)).toArgb(), Accent.getAccentColorWithAlpha((int)(255.0F * renderAlpha)).toArgb(), metricVisibility
            )
            )
            .render(drawContext);
            drawSettingsLabel(drawContext, METRIC_OPTIONS[metricIndex], contentLeft, rowY, renderAlpha);
            drawToggle(drawContext, metricVisibility, contentRight, rowY, renderAlpha);
        }
    }

    private static void drawSettingsLabel(DrawContext drawContext, String label, float x, float y, float alpha) {
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, label, x + 4.0F + 5.0F + 5.0F, y, 4.8F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDarkerTextColor((int)(235.0F * alpha)))
        .render(drawContext);
    }

    private static void drawDecorativeDots(DrawContext drawContext, float x, float y, float alpha) {
        float startY = y - 2.5F;

        for (int dotIndex = 0; dotIndex < 3; dotIndex++) {
            RoundedGradientRectBuilder.at(x, startY + 0.5F + dotIndex * 1.75F, 5.0F, 0.7F)
            .cornerRadius(0.35F)
            .color(Accent.getAccentColorWithAlpha((int)(255.0F * alpha)))
            .render(drawContext);
        }
    }

    private static void drawToggle(DrawContext drawContext, float toggleProgress, float rightEdgeX, float centerY, float alpha) {
        float offLabelWidth = Medium.DEFAULT_FONT.measureWidth("OFF", 4.6F);
        if (toggleProgress > 0.01F) {
            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT, "ON", rightEdgeX - Medium.DEFAULT_FONT.measureWidth("ON", 4.6F), centerY + (1.0F - toggleProgress) * 3.0F, 4.6F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(250.0F * toggleProgress * alpha)))
            .render(drawContext);
        }

        if (toggleProgress < 0.99F) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "OFF", rightEdgeX - offLabelWidth, centerY - toggleProgress * 3.0F, 4.6F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(210.0F * (1.0F - toggleProgress) * alpha)))
            .render(drawContext);
        }

        float trackX = rightEdgeX - offLabelWidth - 4.0F - 13.0F;
        float trackY = centerY - 3.5F;
        RoundedGradientRectBuilder.at(trackX, trackY, 13.0F, 7.0F)
        .cornerRadius(3.5F)
        .color(
        ColorUtils.interpolateColor(
        RgbaColor.rgba(0, 0, 0, (int)(80.0F * alpha)).toArgb(),
        Accent.getAccentColorWithAlpha((int)(218.0F * alpha)).toArgb(),
        toggleProgress
        )
        )
        .render(drawContext);
        float knobRadius = 1.1F;
        float knobSize = 7.0F - knobRadius * 2.0F;
        float knobMinX = trackX + knobRadius;
        float knobMaxX = trackX + 13.0F - knobRadius - knobSize;
        RoundedGradientRectBuilder.at(knobMinX + (knobMaxX - knobMinX) * toggleProgress, trackY + knobRadius, knobSize, knobSize)
        .cornerRadius(knobSize * 0.5F)
        .color(RgbaColor.rgba(255, 255, 255, (int)(255.0F * alpha)))
        .render(drawContext);
    }

    private float drawWatermarkMetric(DrawContext drawContext, float x, float y, String iconGlyph, String value, int colorArgb, float alpha) {
        BrokkeIcons.drawVerticalDivider(drawContext, x, y, alpha);
        x += 5.0F;
        GuiTextBuilder.createAt(Medium.ICON_FONT, iconGlyph, x, y, 5.5F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(RgbaColor.fromArgb(colorArgb).multiplyAlpha(0.94F))
        .alpha(alpha)
        .render(drawContext);
        x += 7.2F;
        BrokkeIcons.drawCenteredText(drawContext, value, x, y, 5.7F, BrokkeIcons.lightGrayWithAlpha(alpha));
        return x + BrokkeIcons.measureTextWidth(value, 5.7F) + 5.5F;
    }

    private int getStyleColor(float healthRatio, float alpha) {
        Object selectedStyle = this.watermarkStyleSetting.getValue();

        {

            if (false && CLIENT.player != null) {
                float healthFraction = Math.clamp(CLIENT.player.getHealth() / CLIENT.player.getMaxHealth(), 0.0F, 1.0F);
                return RgbaColor.rgba(
                (int)(255.0F - healthFraction * 80.0F), (int)(120.0F + healthFraction * 110.0F), (int)(160.0F + healthFraction * 40.0F), (int)(255.0F * alpha)
                )
                .toArgb();
            } else {
                return Accent.getAccentColorWithAlpha((int)(255.0F * alpha)).toArgb();
            }
        }
    }
}

