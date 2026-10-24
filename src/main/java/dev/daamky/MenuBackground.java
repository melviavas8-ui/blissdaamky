package dev.daamky;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

abstract class MenuBackground extends Screen {
    private static final AvatarTexture BACKGROUND_TEXTURE = Avatar.create("menu_background");
    private static final AvatarTexture BLURRED_BACKGROUND_TEXTURE = Avatar.create("menu_background_blur");
    private float backgroundImageX;
    private float backgroundImageY;
    private float backgroundImageWidth;
    private float backgroundImageHeight;
    private float smoothedBackgroundOffsetX;
    private float smoothedBackgroundOffsetY;
    static final MsdfFont BOLD_FONT = Medium.loadFont("brokke_bold");
    private final Map<String, Float> textHoverProgress = new HashMap<>();
    private final Map<String, Long> textChangeTimestamps = new HashMap<>();
    private long screenOpenedAtMillis = System.currentTimeMillis();
    private long previousFrameTimeMillis = System.currentTimeMillis();
    private long transitionStartedAtMillis = -1L;
    private Screen nextScreen;
    protected float frameDeltaSeconds;
    protected long frameTimeMillis;
    protected float mouseX;
    protected float mouseY;
    protected float viewportWidth;
    protected float viewportHeight;

    protected float getScreenScale() {
        return 1.35F;
    }

    MenuBackground(String screenTitle) {
        super(Text.literal(screenTitle));
    }

    protected abstract void drawContent(DrawContext drawContext, float contentAnimationProgress, long renderTimeMillis);

    protected boolean handleContentClick(float mouseX, float mouseY, int mouseButton) {
        return false;
    }

    protected void init() {
        super.init();
        if (this.transitionStartedAtMillis >= 0L || this.nextScreen != null) {
            this.screenOpenedAtMillis = System.currentTimeMillis();
        }

        this.transitionStartedAtMillis = -1L;
        this.nextScreen = null;
    }

    protected void transitionTo(Screen screen) {
        if (this.transitionStartedAtMillis < 0L) {
            this.transitionStartedAtMillis = System.currentTimeMillis();
            this.nextScreen = screen;
        }
    }

    protected boolean isTransitioning() {
        return this.transitionStartedAtMillis >= 0L;
    }

    protected float easeOutCubic(float startDelayMillis, float durationMillis) {
        float progress = Math.clamp(((float)(this.frameTimeMillis - this.screenOpenedAtMillis) - startDelayMillis) / durationMillis, 0.0F, 1.0F);
        return 1.0F - (float)Math.pow(1.0F - progress, 3.0);
    }

    protected float easeOutBack(float startDelayMillis, float durationMillis) {
        float progress = Math.clamp(((float)(this.frameTimeMillis - this.screenOpenedAtMillis) - startDelayMillis) / durationMillis, 0.0F, 1.0F);

        float overshootOffset = progress - 1.0F;
        return 1.0F + 2.4F * overshootOffset * overshootOffset * overshootOffset + 1.4F * overshootOffset * overshootOffset;
    }

    protected float getTransitionProgress() {
        if (this.transitionStartedAtMillis < 0L) {
            return 0.0F;
        }

        float progress = Math.clamp((float)(this.frameTimeMillis - this.transitionStartedAtMillis) / 180.0F, 0.0F, 1.0F);
        return progress * progress;
    }

    protected void markTextChanged(String animationKey) {
        this.textChangeTimestamps.put(animationKey, this.frameTimeMillis);
    }

    protected float getTextChangeScale(String animationKey) {
        Long lastChange = this.textChangeTimestamps.get(animationKey);
        if (lastChange == null) {
            return 1.0F;
        }

        float elapsedProgress = (float)(this.frameTimeMillis - lastChange) / 220.0F;
        return elapsedProgress >= 1.0F ? 1.0F : 1.0F - 0.06F * (float)Math.sin(elapsedProgress * Math.PI);
    }

    protected void pushPivotScale(DrawContext drawContext, float pivotX, float pivotY, float scale) {
        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().translate(pivotX, pivotY);
        drawContext.getMatrices().scale(scale, scale);
        drawContext.getMatrices().translate(-pivotX, -pivotY);
    }

    protected void popTransformMatrix(DrawContext drawContext) {
        drawContext.getMatrices().popMatrix();
    }

    public void render(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int windowScaleFactor = client.getWindow().getScaleFactor();
        if (windowScaleFactor > 0) {
            float halfWindowScaleFactor = windowScaleFactor / 2.0F;
            this.mouseX = mouseX * halfWindowScaleFactor / this.getScreenScale();
            this.mouseY = mouseY * halfWindowScaleFactor / this.getScreenScale();
            this.viewportWidth = GuiEventRenderer.getScreenWidth() / this.getScreenScale();
            this.viewportHeight = GuiEventRenderer.getScreenHeight() / this.getScreenScale();
            this.frameTimeMillis = System.currentTimeMillis();
            this.frameDeltaSeconds = Math.clamp((float)(this.frameTimeMillis - this.previousFrameTimeMillis) / 1000.0F, 0.0F, 0.1F);
            this.previousFrameTimeMillis = this.frameTimeMillis;
            float contentAnimationProgress = this.easeOutCubic(0.0F, 420.0F) * (1.0F - this.getTransitionProgress());
            if (this.transitionStartedAtMillis >= 0L && this.frameTimeMillis - this.transitionStartedAtMillis >= 180L && this.nextScreen != null) {
                Screen screen = this.nextScreen;
                this.nextScreen = null;
                client.setScreen(screen);
            } else {
                drawContext.getMatrices().pushMatrix();
                drawContext.getMatrices().scale(2.0F / windowScaleFactor * this.getScreenScale());
                GuiRenderStateBatcher.beginBatching(drawContext);

                try {
                    this.renderBackgroundImage(drawContext, this.frameTimeMillis);
                    this.drawContent(drawContext, contentAnimationProgress, this.frameTimeMillis);
                } finally {
                    GuiRenderStateBatcher.endBatching();
                    drawContext.getMatrices().popMatrix();
                }
            }
        }
    }

    public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
    }

    public void renderInGameBackground(DrawContext drawContext) {
    }

    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (this.transitionStartedAtMillis >= 0L) {
            return true;
        }

        float halfWindowScaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor() / 2.0F;
        return this.handleContentClick(
        (float)click.x() * halfWindowScaleFactor / this.getScreenScale(), (float)click.y() * halfWindowScaleFactor / this.getScreenScale(), click.button()
        )
        ? true
        : super.mouseClicked(click, doubleClick);
    }

    private void renderBackgroundImage(DrawContext drawContext, long renderTimeMillis) {
        RoundedGradientRectBuilder.at(0.0F, 0.0F, this.viewportWidth, this.viewportHeight)
        .cornerRadius(0.0F)
        .color(RgbaColor.rgb(8, 9, 14))
        .render(drawContext);
        float zoomFactor = 1.06F + 0.015F * (float)Math.sin(renderTimeMillis / 9000.0);
        float imageWidth = Math.max(this.viewportWidth, this.viewportHeight * 16.0F / 9.0F) * zoomFactor;
        float imageHeight = imageWidth * 9.0F / 16.0F;
        float targetOffsetX = (Math.clamp(this.mouseX / Math.max(1.0F, this.viewportWidth), 0.0F, 1.0F) - 0.5F) * (imageWidth - this.viewportWidth) * 0.6F;
        float targetOffsetY = (Math.clamp(this.mouseY / Math.max(1.0F, this.viewportHeight), 0.0F, 1.0F) - 0.5F)
        * (imageHeight - this.viewportHeight)
        * 0.6F;
        if (!Float.isFinite(this.smoothedBackgroundOffsetX) || !Float.isFinite(this.smoothedBackgroundOffsetY)) {
            this.smoothedBackgroundOffsetX = 0.0F;
            this.smoothedBackgroundOffsetY = 0.0F;
        }

        this.smoothedBackgroundOffsetX = this.smoothedBackgroundOffsetX + (targetOffsetX - this.smoothedBackgroundOffsetX) * (1.0F - (float)Math.exp(-this.frameDeltaSeconds * 4.0F));
        this.smoothedBackgroundOffsetY = this.smoothedBackgroundOffsetY + (targetOffsetY - this.smoothedBackgroundOffsetY) * (1.0F - (float)Math.exp(-this.frameDeltaSeconds * 4.0F));
        this.backgroundImageX = (this.viewportWidth - imageWidth) * 0.5F - this.smoothedBackgroundOffsetX;
        this.backgroundImageY = (this.viewportHeight - imageHeight) * 0.5F - this.smoothedBackgroundOffsetY;
        this.backgroundImageWidth = imageWidth;
        this.backgroundImageHeight = imageHeight;
        TextureQuadBuilder.at(BACKGROUND_TEXTURE, this.backgroundImageX, this.backgroundImageY, this.backgroundImageWidth, this.backgroundImageHeight)
        .render(drawContext);
    }

    protected boolean isMouseWithin(float left, float top, float width, float height) {
        return this.mouseX >= left && this.mouseX <= left + width && this.mouseY >= top && this.mouseY <= top + height;
    }

    protected float getHoverProgress(String animationKey, boolean hovered) {
        float progress = this.textHoverProgress.getOrDefault(animationKey, 0.0F);
        progress += ((hovered && this.transitionStartedAtMillis < 0L ? 1.0F : 0.0F) - progress) * (1.0F - (float)Math.exp(-this.frameDeltaSeconds * 14.0F));
        this.textHoverProgress.put(animationKey, progress);
        return progress;
    }

    protected void drawBlurredPanel(
    DrawContext drawContext, float x, float y, float width, float height, float cornerRadius, float opacity, RgbaColor l1I1IlllIlI4
    ) {
        if (!(opacity <= 0.002F) && !(width <= 0.0F) && !(height <= 0.0F)) {
            RoundedTextureRenderer.drawRoundedTexture(
            drawContext,
            BLURRED_BACKGROUND_TEXTURE,
            x,
            y,
            width,
            height,
            cornerRadius,
            this.backgroundImageX,
            this.backgroundImageY,
            this.backgroundImageWidth,
            this.backgroundImageHeight,
            RgbaColor.WHITE.withAlpha((int)(255.0F * opacity)).toArgb()
            );
            RoundedGradientRectBuilder.at(x, y, width, height)
            .cornerRadius(cornerRadius)
            .color(l1I1IlllIlI4.withAlpha((int)(l1I1IlllIlI4.alpha() * opacity)))
            .render(drawContext);
        }
    }

    protected void drawGlassPanel(
    DrawContext drawContext, float x, float y, float width, float height, float cornerRadius, float panelOpacity, float highlightOpacity
    ) {
        this.drawBlurredPanel(drawContext, x, y, width, height, cornerRadius, panelOpacity, RgbaColor.rgba(0, 0, 0, 80));
        RoundedGradientRectBuilder.at(x, y, width, height)
        .cornerRadius(cornerRadius)
        .color(RgbaColor.rgba(255, 255, 255, (int)(22.0F * highlightOpacity * panelOpacity)))
        .render(drawContext);
    }

    protected void drawAccentPanel(DrawContext drawContext, float x, float y, float width, float height, float cornerRadius) {
        this.drawBlurredPanel(
        drawContext, x, y, width, height, Accent.getInstance().getCornerRadius(), cornerRadius, RgbaColor.rgba(0, 0, 0, 50)
        );
    }

    protected void drawCenteredText(DrawContext drawContext, String label, float centerX, float centerY, float fontSize, RgbaColor l1I1IlllIlI4) {
        GuiTextBuilder.createAt(Medium.ICON_FONT, label, centerX, centerY, fontSize)
        .alignment(HorizontalTextAlignment.CENTER)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(l1I1IlllIlI4)
        .render(drawContext);
    }

    protected void drawText(
    DrawContext drawContext,
    MsdfFont font,
    String label,
    float x,
    float y,
    float fontSize,
    HorizontalTextAlignment cENTERLEFTRIGHT,
    RgbaColor l1I1IlllIlI4
    ) {
        GuiTextBuilder.createAt(font, label, x, y, fontSize)
        .alignment(cENTERLEFTRIGHT)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(l1I1IlllIlI4)
        .render(drawContext);
    }
}

