package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventDisplay;
import java.time.LocalDate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

public final class Search {
    private static final Search INSTANCE = new Search();
    private final EasedAnimation openAnimation = new EasedAnimation();
    private final SearchBar searchBar = new SearchBar();
    private final ClickGuiSidebar categorySidebar = new ClickGuiSidebar();
    private final ModuleSettingsPanel moduleSettingsPanel = new ModuleSettingsPanel();
    private final Blur themePanel = new Blur();
    private final Cosmetics cosmeticsPanel = new Cosmetics();
    private final Bots botsPanel = new Bots();
    private final ClaudeConfig claudeConfig = new ClaudeConfig();
    private final Myid globalsPanel = new Myid();
    private final NameTagPreviewWidget nameTagPreview = new NameTagPreviewWidget();
    private final float BASE_PANEL_WIDTH = 520.0F;
    private final float PANEL_HEIGHT = 340.0F;
    private final float SIDEBAR_WIDTH = 100.0F;
    private final float HEADER_HEIGHT = 26.0F;
    private float panelX = 0.0F;
    private float panelY = 0.0F;
    private float mouseX = 0.0F;
    private float mouseY = 0.0F;
    private final String currentDateLabel = LocalDate.now().toString().replace('-', '.');

    private Search() {
    }

    public static Search getInstance() {
        return INSTANCE;
    }

    public void open() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.inGameHud != null && !(client.currentScreen instanceof DaamkyClickGuiScreen)) {
            client.setScreen(new DaamkyClickGuiScreen());
        }

        this.openAnimation.start();
    }

    public void close() {
        this.openAnimation.stop();
        this.searchBar.setEnabled(false);
        this.botsPanel.clearInputFocus();
        this.claudeConfig.clearSearchFocus();
        this.globalsPanel.clearFocusedInput();
        this.moduleSettingsPanel.clearSelection();
        ClientConfigManager.getInstance().saveConfig();
    }

    public void toggle() {
        if (this.openAnimation.isRunning()) {
            this.close();
        } else {
            this.open();
        }
    }

    public boolean isOpen() {
        return this.openAnimation.isRunning();
    }

    public boolean shouldRender() {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean isClickGuiScreenDisplayed = client != null && client.inGameHud != null && client.currentScreen instanceof DaamkyClickGuiScreen;
        return this.openAnimation.isRunning() || !this.openAnimation.isFinished() || isClickGuiScreenDisplayed;
    }

    private boolean hasModuleListCategory() {
        ModuleCategory moduleCategory = this.categorySidebar.getSelectedCategory();
        return moduleCategory != null
        && moduleCategory != ModuleCategory.THEMES
        && moduleCategory != ModuleCategory.COSMETICS
        && moduleCategory != ModuleCategory.BOTS
        && moduleCategory != ModuleCategory.CLOUD_CONFIG
        && moduleCategory != ModuleCategory.GLOBALS;
    }

    public boolean handleMouseClick(Click click) {
        if (!this.openAnimation.isRunning()) {
            return false;
        }

        this.updateMousePosition(click.x(), click.y());
        int button = click.button();
        if (this.moduleSettingsPanel.hasSelectedModule()) {
            if (button >= 3) {
                this.moduleSettingsPanel.setSelectedModuleKeybind(dev.daamky.KeybindManager.encodeMouseButton(button));
                ClientSounds.playGuiClickSound();
                return true;
            }

            if (button == 1) {
                this.moduleSettingsPanel.setSelectedModuleKeybind(0);
                ClientSounds.playGuiClickSound();
                return true;
            }

            if (button == 0) {
                this.moduleSettingsPanel.clearSelectedModule();
                return true;
            }
        }

        if (this.hasModuleListCategory() && this.moduleSettingsPanel.onMouseClicked(this.mouseX, this.mouseY, button)) {
            ClientSounds.playGuiClickSound();
            return true;
        }

        if (this.categorySidebar.handleClick(this.mouseX, this.mouseY, button)) {
            this.moduleSettingsPanel.clearSelectedModule();
            ClientSounds.playGuiClickSound();
            return true;
        }

        if (this.supportsModuleSearch() && this.searchBar.onMouseClick(this.mouseX, this.mouseY, button)) {
            this.moduleSettingsPanel.clearSelectedModule();
            ClientSounds.playGuiClickSound();
            return true;
        }

        if (this.categorySidebar.getSelectedCategory() == ModuleCategory.THEMES) {
            if (this.themePanel.mouseClicked(this.mouseX, this.mouseY, button)) {
                ClientSounds.playGuiClickSound();
                return true;
            }
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.COSMETICS) {
            if (this.cosmeticsPanel.mouseClicked(this.mouseX, this.mouseY, button)) {
                ClientSounds.playGuiClickSound();
                return true;
            }
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.BOTS) {
            if (this.botsPanel.mouseClicked(this.mouseX, this.mouseY, button)) {
                ClientSounds.playGuiClickSound();
                return true;
            }
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.CLOUD_CONFIG) {
            if (this.claudeConfig.mouseClicked(this.mouseX, this.mouseY, button)) {
                ClientSounds.playGuiClickSound();
                return true;
            }
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.GLOBALS) {
            if (this.globalsPanel.mouseClicked(this.mouseX, this.mouseY, button)) {
                return true;
            }
        } else {
            if (this.nameTagPreview.beginDrag(this.mouseX, this.mouseY, button)) {
                return true;
            }

            if (this.moduleSettingsPanel.handleMouseClick(this.mouseX, this.mouseY, button)) {
                ClientSounds.playGuiClickSound();
                return true;
            }
        }

        if (button == 0) {
            float previewWidth = this.BASE_PANEL_WIDTH + 130.0F * this.nameTagPreview.getVisibilityProgress();
            if (this.mouseX >= this.panelX
            && this.mouseX <= this.panelX + previewWidth
            && this.mouseY >= this.panelY
            && this.mouseY <= this.panelY + this.PANEL_HEIGHT) {
                return true;
            }
        }

        return false;
    }

    public boolean handleMouseScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.openAnimation.isRunning()) {
            return false;
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.THEMES) {
            return this.themePanel.mouseScrolled(this.mouseX, this.mouseY, verticalAmount);
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.COSMETICS) {
            return this.cosmeticsPanel.mouseScrolled(this.mouseX, this.mouseY, verticalAmount);
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.BOTS) {
            return this.botsPanel.mouseScrolled(this.mouseX, this.mouseY, verticalAmount);
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.CLOUD_CONFIG) {
            return this.claudeConfig.mouseScrolled(this.mouseX, this.mouseY, verticalAmount);
        } else {
            return this.categorySidebar.getSelectedCategory() == ModuleCategory.GLOBALS
            ? this.globalsPanel.mouseScrolled(this.mouseX, this.mouseY, verticalAmount)
            : this.moduleSettingsPanel.handleMouseScroll(this.mouseX, this.mouseY, verticalAmount);
        }
    }

    public boolean handleKeyInput(KeyInput keyInput) {
        if (!this.openAnimation.isRunning()) {
            return false;
        }

        if (this.categorySidebar.getSelectedCategory() == ModuleCategory.THEMES && this.themePanel.keyPressed(keyInput)) {
            return true;
        }

        if (this.categorySidebar.getSelectedCategory() == ModuleCategory.BOTS && this.botsPanel.keyPressed(keyInput)) {
            return true;
        }

        if (this.categorySidebar.getSelectedCategory() == ModuleCategory.CLOUD_CONFIG && this.claudeConfig.keyPressed(keyInput)) {
            return true;
        }

        if (this.categorySidebar.getSelectedCategory() == ModuleCategory.GLOBALS) {
            if (this.globalsPanel.keyPressed(keyInput)) {
                return true;
            }

            if (keyInput.getKeycode() != 256) {
                return false;
            }
        }

        if (!this.moduleSettingsPanel.hasSelectedModule()) {
            if (this.moduleSettingsPanel.isKeybindCaptureActive()) {
                this.moduleSettingsPanel.setKeybindCapture(keyInput.getKeycode());
                return true;
            }

            if (keyInput.getKeycode() == 256) {
                if (this.moduleSettingsPanel.cancelOptionSelection()) {
                    return true;
                } else if (this.searchBar.isEnabled()) {
                    this.searchBar.setEnabled(false);
                    return true;
                } else if (!this.searchBar.getSearchText().isEmpty()) {
                    this.searchBar.setSearchText("");
                    return true;
                } else {
                    return false;
                }
            } else {
                return this.searchBar.onKeyInput(keyInput);
            }
        } else {
            int keyCode = keyInput.getKeycode();
            if (keyCode != 256 && keyCode != 261 && keyCode != 259) {
                this.moduleSettingsPanel.setSelectedModuleKeybind(keyCode);
            } else {
                this.moduleSettingsPanel.setSelectedModuleKeybind(0);
            }

            ClientSounds.playGuiClickSound();
            return true;
        }
    }

    public boolean handleCharInput(CharInput charInput) {
        if (!this.openAnimation.isRunning()) {
            return false;
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.THEMES && this.themePanel.charTyped(charInput)) {
            return true;
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.BOTS && this.botsPanel.charTyped(charInput)) {
            return true;
        } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.CLOUD_CONFIG && this.claudeConfig.charTyped(charInput)) {
            return true;
        } else {
            return this.categorySidebar.getSelectedCategory() == ModuleCategory.GLOBALS
            ? this.globalsPanel.charTyped(charInput)
            : this.searchBar.onCharacterInput(charInput);
        }
    }

    public SearchBar getSearchBar() {
        return this.searchBar;
    }

    public ClickGuiSidebar getCategorySidebar() {
        return this.categorySidebar;
    }

    public void handleMouseRelease(Click click) {
        this.updateMousePosition(click.x(), click.y());
        this.moduleSettingsPanel.handleMouseRelease(click.button());
        this.themePanel.mouseReleased(click.button());
        this.nameTagPreview.endDrag(click.button());
    }

    private boolean supportsModuleSearch() {
        ModuleCategory moduleCategory = this.categorySidebar.getSelectedCategory();
        return moduleCategory != ModuleCategory.BOTS && moduleCategory != ModuleCategory.CLOUD_CONFIG && moduleCategory != ModuleCategory.GLOBALS;
    }

    public boolean isTextInputFocused() {
        return this.openAnimation.isRunning() && (this.searchBar.isEnabled() || this.isGlobalsInputFocused());
    }

    public boolean isGlobalsInputFocused() {
        return this.openAnimation.isRunning() && this.categorySidebar.getSelectedCategory() == ModuleCategory.GLOBALS && this.globalsPanel.hasFocusedInput();
    }

    private boolean shouldShowNameTagPreview() {
        return this.categorySidebar.getSelectedCategory() == ModuleCategory.RENDER && this.searchBar.getSearchText().isBlank();
    }

    public void updateMousePosition(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getWindow() != null) {
            this.mouseX = (float)(mouseX * client.getWindow().getScaleFactor() / 2.0);
            this.mouseY = (float)(mouseY * client.getWindow().getScaleFactor() / 2.0);
            this.moduleSettingsPanel.handleMouseDrag(this.mouseX, this.mouseY);
            this.themePanel.mouseDragged(this.mouseX, this.mouseY);
            this.nameTagPreview.dragTo(this.mouseX, this.mouseY);
        }
    }

    @EventTarget
    public void onDisplay(EventDisplay eventDisplay) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (this.openAnimation.isRunning() && client != null && client.inGameHud != null && !(client.currentScreen instanceof DaamkyClickGuiScreen)) {
            this.close();
        }

        this.openAnimation.update();
        float frameDeltaSeconds = this.openAnimation.frameDeltaSeconds();
        this.searchBar.updateVisibilityAnimation(frameDeltaSeconds);
        this.categorySidebar.setFrameDeltaSeconds(frameDeltaSeconds);
        this.moduleSettingsPanel.updateAnimation(frameDeltaSeconds);
        this.themePanel.updateAnimationState(frameDeltaSeconds);
        this.cosmeticsPanel.updateAnimationState(frameDeltaSeconds);
        this.botsPanel.updateAnimationState(frameDeltaSeconds);
        this.claudeConfig.tickAnimation(frameDeltaSeconds);
        this.globalsPanel.updateAnimations(frameDeltaSeconds);
        this.nameTagPreview.updateVisibility(frameDeltaSeconds, this.shouldShowNameTagPreview());
        ModuleRegistry.getInstance().updateAnimations(frameDeltaSeconds);
        float easedProgress = this.openAnimation.easedProgress();
        if (easedProgress > 0.001F) {
            this.renderClickGui(eventDisplay.graphics(), easedProgress);
        } else if (!this.openAnimation.isRunning()) {
            MinecraftClient client2 = MinecraftClient.getInstance();
            if (client2 != null && client2.inGameHud != null && client2.currentScreen instanceof DaamkyClickGuiScreen) {
                client2.setScreen(null);
            }
        }
    }

    private void renderClickGui(DrawContext drawContext, float animationProgress) {
        float viewportWidth = GuiEventRenderer.getScreenWidth();
        float viewportHeight = GuiEventRenderer.getScreenHeight();
        float panelWidth = this.BASE_PANEL_WIDTH + 130.0F * this.nameTagPreview.getVisibilityProgress();
        this.panelX = (viewportWidth - panelWidth) / 2.0F;
        this.panelY = (viewportHeight - this.PANEL_HEIGHT) / 2.0F;
        float transformScale = 0.85F + 0.15F * animationProgress;
        float transformCenterX = this.panelX + panelWidth / 2.0F;
        float transformCenterY = this.panelY + this.PANEL_HEIGHT / 2.0F;
        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().translate(transformCenterX, transformCenterY);
        drawContext.getMatrices().scale(transformScale, transformScale);
        drawContext.getMatrices().translate(-transformCenterX, -transformCenterY);

        try {
            Accent accentSettings = Accent.getInstance();
            float blurRadius = accentSettings.getCornerRadius();
            boolean customBlurEnabled = accentSettings.isGlassStyle();
            if (customBlurEnabled) {
                BlurredRectangleBuilder.create(this.panelX, this.panelY, panelWidth, this.PANEL_HEIGHT)
                .cornerRadius(blurRadius)
                .blurMode(KawaseGaussianBlurMode.KAWASE)
                .blurPasses((int)accentSettings.getBlurRadius())
                .color(Accent.getTintedBackgroundColor((int)(185.0F * animationProgress)))
                .opacity(animationProgress)
                .render(drawContext);
            } else {
                KawaseBlurBuilder.create(this.panelX, this.panelY, panelWidth, this.PANEL_HEIGHT)
                .cornerRadius(blurRadius)
                .blurMode(KawaseGaussianBlurMode.KAWASE)
                .blurPasses((int)accentSettings.getBlurRadius())
                .color(Accent.getTintedBackgroundColor((int)(180.0F * animationProgress)))
                .opacity(animationProgress)
                .render(drawContext);
            }

            RoundedGradientRectBuilder panelBackground = RoundedGradientRectBuilder.at(this.panelX, this.panelY, panelWidth, this.PANEL_HEIGHT)
            .cornerRadius(blurRadius);
            if (customBlurEnabled) {

            }

            panelBackground.color(Accent.getAccentBackgroundColor((int)(36.0F * animationProgress))).render(drawContext);
            RoundedGradientRectBuilder.at(this.panelX, this.panelY, this.SIDEBAR_WIDTH, this.PANEL_HEIGHT)
            .cornerRadii(blurRadius, 0.0F, 0.0F, blurRadius)
            .color(Accent.getTextColorWithAlpha((int)(4.0F * animationProgress)))
            .render(drawContext);
            RoundedRectBuilder.createAt(this.panelX, this.panelY, panelWidth, this.PANEL_HEIGHT)
            .cornerRadius(blurRadius)
            .borderWidth(0.85F)
            .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)(70.0F * animationProgress)))
            .alpha(animationProgress)
            .render(drawContext);
            int separatorColor = Accent.getTextColorWithAlpha((int)(12.0F * animationProgress)).toArgb();
            RoundedGradientRectBuilder.at(this.panelX + this.SIDEBAR_WIDTH, this.panelY, 0.75F, this.PANEL_HEIGHT)
            .cornerRadius(0.0F)
            .color(separatorColor)
            .render(drawContext);
            RoundedGradientRectBuilder.at(this.panelX + this.SIDEBAR_WIDTH, this.panelY + this.HEADER_HEIGHT, 420.0F, 0.75F)
            .cornerRadius(0.0F)
            .color(separatorColor)
            .render(drawContext);
            this.renderSidebarFooter(drawContext, animationProgress);
            this.renderHeader(drawContext, animationProgress);
            float contentLeft = this.panelX + this.SIDEBAR_WIDTH;
            float contentTop = this.panelY + this.HEADER_HEIGHT;
            float contentRight = contentLeft + 160.0F;
            if (this.categorySidebar.getSelectedCategory() == ModuleCategory.COSMETICS) {
                this.cosmeticsPanel
                .render(
                drawContext,
                this.mouseX,
                this.mouseY,
                contentLeft + 3.0F,
                contentTop + 4.0F,
                contentRight - contentLeft - 6.0F,
                300.0F,
                contentRight + 2.0F,
                contentTop + 5.0F,
                this.panelX + panelWidth - contentRight - 2.0F,
                300.0F,
                animationProgress
                );
                RoundedGradientRectBuilder.at(contentRight, contentTop, 0.75F, 310.0F).cornerRadius(0.0F).color(separatorColor).render(drawContext);
            } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.BOTS) {
                this.botsPanel
                .render(
                drawContext,
                this.mouseX,
                this.mouseY,
                contentLeft + 3.0F,
                contentTop + 4.0F,
                contentRight - contentLeft - 6.0F,
                300.0F,
                contentRight + 2.0F,
                contentTop + 5.0F,
                this.panelX + this.BASE_PANEL_WIDTH - contentRight - 2.0F,
                300.0F,
                animationProgress
                );
                RoundedGradientRectBuilder.at(contentRight, contentTop, 0.75F, 310.0F).cornerRadius(0.0F).color(separatorColor).render(drawContext);
            } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.CLOUD_CONFIG) {
                this.claudeConfig
                .render(
                drawContext,
                this.mouseX,
                this.mouseY,
                contentLeft + 3.0F,
                contentTop + 4.0F,
                contentRight - contentLeft - 6.0F,
                300.0F,
                contentRight + 2.0F,
                contentTop + 5.0F,
                this.panelX + panelWidth - contentRight - 2.0F,
                300.0F,
                animationProgress
                );
                RoundedGradientRectBuilder.at(contentRight, contentTop, 0.75F, 310.0F).cornerRadius(0.0F).color(separatorColor).render(drawContext);
            } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.GLOBALS) {
                this.globalsPanel
                .render(
                drawContext, this.mouseX, this.mouseY, contentLeft, contentTop, this.panelX + panelWidth - contentLeft, 310.0F, animationProgress
                );
            } else if (this.categorySidebar.getSelectedCategory() == ModuleCategory.THEMES) {
                this.themePanel
                .render(
                drawContext,
                this.mouseX,
                this.mouseY,
                contentLeft + 3.0F,
                contentTop + 4.0F,
                contentRight - contentLeft - 6.0F,
                300.0F,
                contentRight + 2.0F,
                contentTop + 5.0F,
                this.panelX + panelWidth - contentRight - 2.0F,
                300.0F,
                animationProgress
                );
                RoundedGradientRectBuilder.at(contentRight, contentTop, 0.75F, 310.0F).cornerRadius(0.0F).color(separatorColor).render(drawContext);
            } else {
                float moduleListTop = contentTop + 1.0F;

                this.moduleSettingsPanel
                .renderModuleList(
                drawContext,
                this.mouseX,
                this.mouseY,
                contentLeft,
                moduleListTop,
                420.0F,
                300.0F,
                this.categorySidebar.getDisplayCategory(),
                this.searchBar.getSearchText(),
                animationProgress
                );
            }

            if (this.nameTagPreview.isVisible()) {
                ESP espModule = ModuleRegistry.getInstance().getModuleByClass(ESP.class);
                drawContext.enableScissor(
                PixelRounding.II1Il11l111II11IIl(this.panelX + this.BASE_PANEL_WIDTH),
                PixelRounding.II1Il11l111II11IIl(this.panelY),
                PixelRounding.l1I1IlllIlI(this.panelX + panelWidth),
                PixelRounding.l1I1IlllIlI(this.panelY + this.PANEL_HEIGHT)
                );

                try {
                    this.nameTagPreview
                    .renderPreview(
                    drawContext, espModule != null ? espModule.getEntityRenderMode() : "", this.panelX + this.BASE_PANEL_WIDTH, this.panelY, this.PANEL_HEIGHT, animationProgress
                    );
                } finally {
                    drawContext.disableScissor();
                }
            }
        } finally {
            drawContext.getMatrices().popMatrix();
        }
    }

    private void renderSidebarFooter(DrawContext drawContext, float animationProgress) {
        TextureQuadBuilder.at(Avatar.LOGO_AVATAR, this.panelX + 42.0F - 4.5F, this.panelY + 8.0F, 9.0F, 9.0F)
        .tint(Accent.getAccentColorWithAlpha((int)(255.0F * animationProgress)).toArgb())
        .opacity(animationProgress)
        .render(drawContext);
        float footerX = this.panelX + 6.0F;
        float footerY = this.panelY + this.PANEL_HEIGHT - 20.0F - 6.0F;
        RoundedGradientRectBuilder.at(footerX, footerY, 72.0F, 20.0F)
        .cornerRadius(3.5F)
        .color(Accent.getTextColorWithAlpha((int)(10.0F * animationProgress)))
        .render(drawContext);
        GuestAvatarRenderer.II1Il11l111II11IIl(drawContext, footerX + 3.5F, footerY + 3.5F, 13.0F, 3.5F, animationProgress);
        float profileTextX = footerX + 3.5F + 13.0F + 4.0F;
        float profileNameWidth = footerX + 72.0F - 3.0F - profileTextX;
        GuiTextBuilder.createAt(
        Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(Guest.l1I1IlllIlI(), 5.0F, profileNameWidth), profileTextX, footerY + 10.0F - 2.6F, 5.0F
        )
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getTextColorWithAlpha((int)(250.0F * animationProgress)))
        .render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, this.currentDateLabel, profileTextX, footerY + 10.0F + 3.4F, 3.8F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(170.0F * animationProgress)))
        .render(drawContext);
        this.categorySidebar
        .render(drawContext, this.mouseX, this.mouseY, this.panelX, this.panelY + this.HEADER_HEIGHT + 3.0F, this.SIDEBAR_WIDTH, animationProgress);
    }

    private void renderHeader(DrawContext drawContext, float animationProgress) {
        float headerX = this.panelX + this.SIDEBAR_WIDTH + 9.0F;
        float headerY = this.panelY + 11.0F;
        ModuleCategory moduleCategory = this.categorySidebar.getDisplayCategory();
        boolean searchMode = this.hasModuleListCategory() && !this.searchBar.getSearchText().isBlank();
        String headerTitle = searchMode ? "Search" : ClickGuiSidebar.getTabName(moduleCategory);
        String breadcrumbSeparator = " / ";
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, headerTitle + breadcrumbSeparator, headerX, headerY, 4.8F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(190.0F * animationProgress)))
        .render(drawContext);
        float categoryTitleX = headerX + Medium.DEFAULT_FONT.measureWidth(headerTitle + breadcrumbSeparator, 4.8F) + 1.0F;
        if (!searchMode) {
            GuiTextBuilder.createAt(Medium.ICON_FONT, moduleCategory.l1I1IlllIlI(), categoryTitleX + 3.0F, headerY, 5.4F)
            .centered()
            .color(Accent.getAccentColorWithAlpha((int)(255.0F * animationProgress)))
            .render(drawContext);
            categoryTitleX += 8.5F;
        }

        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, searchMode ? "Результаты" : moduleCategory.II1Il11l111II11IIl(), categoryTitleX, headerY, 4.8F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getTextColorWithAlpha((int)(250.0F * animationProgress)))
        .render(drawContext);
        if (this.supportsModuleSearch()) {
            this.searchBar
            .renderSearchBar(
            drawContext, this.mouseX, this.mouseY, this.panelX, this.panelY + 0.75F, this.BASE_PANEL_WIDTH, this.PANEL_HEIGHT, animationProgress
            );
        }
    }
}

