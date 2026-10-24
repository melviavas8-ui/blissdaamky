package dev.daamky;

import net.minecraft.client.gui.DrawContext;

public final class GuiPanelRenderer {
    private GuiPanelRenderer() {
    }

    public static void renderPanel(
    DrawContext drawContext,
    float x,
    float y,
    float width,
    float height,
    float cornerRadius,
    float opacity,
    boolean useBlur,
    RgbaColor outlineColor
    ) {
        if (!(opacity <= 0.01F) && !(width <= 0.0F) && !(height <= 0.0F)) {
            Accent accent = Accent.getInstance();
            boolean glassStyle = accent.isGlassStyle();
            RoundedOutlineBuilder.at(x, y, width, height)
            .cornerRadius(cornerRadius)
            .outlineWidth(8.0F)
            .color(RgbaColor.rgba(0, 0, 0, (int)(70.0F * opacity)))
            .render(drawContext);
            if (useBlur) {
                if (glassStyle) {
                    BlurredRectangleBuilder.create(x, y, width, height)
                    .cornerRadius(cornerRadius)
                    .blurMode(KawaseGaussianBlurMode.KAWASE)
                    .blurPasses((int)accent.getBlurRadius())
                    .color(Accent.getTintedBackgroundColor((int)(120.0F * opacity)))
                    .opacity(opacity)
                    .render(drawContext);
                } else {
                    KawaseBlurBuilder.create(x, y, width, height)
                    .cornerRadius(cornerRadius)
                    .blurMode(KawaseGaussianBlurMode.KAWASE)
                    .blurPasses((int)accent.getBlurRadius())
                    .color(Accent.getTintedBackgroundColor((int)(180.0F * opacity)))
                    .opacity(opacity)
                    .render(drawContext);
                }
            } else {
                RoundedGradientRectBuilder.at(x, y, width, height)
                .cornerRadius(cornerRadius)
                .color(Accent.getTintedBackgroundColor((int)(200.0F * opacity)))
                .render(drawContext);
            }

            RoundedGradientRectBuilder accentOverlayBuilder = RoundedGradientRectBuilder.at(x, y, width, height).cornerRadius(cornerRadius);
            if (glassStyle) {

            }

            accentOverlayBuilder.color(Accent.getAccentBackgroundColor((int)(36.0F * opacity))).render(drawContext);
            RoundedRectBuilder.createAt(x, y, width, height)
            .cornerRadius(cornerRadius)
            .borderWidth(0.85F)
            .color(outlineColor != null ? outlineColor : Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)(70.0F * opacity)))
            .alpha(opacity)
            .render(drawContext);
        }
    }

    public static float smoothInterpolate(float startValue, float targetValue, float interpolationSpeed, float frameDelta) {
        return startValue + (targetValue - startValue) * (1.0F - (float)Math.exp(-interpolationSpeed * frameDelta));
    }

    public static RgbaColor getHealthColor(float healthRatio, int alpha) {
        float clampedHealthRatio = Math.clamp(healthRatio, 0.0F, 1.0F);
        RgbaColor lowHealthColor = RgbaColor.rgba(255, 86, 96, alpha);
        RgbaColor mediumHealthColor = RgbaColor.rgba(255, 200, 90, alpha);
        RgbaColor highHealthColor = RgbaColor.rgba(110, 230, 150, alpha);
        return clampedHealthRatio < 0.5F ? lowHealthColor.mix(mediumHealthColor, clampedHealthRatio * 2.0F) : mediumHealthColor.mix(highHealthColor, (clampedHealthRatio - 0.5F) * 2.0F);
    }
}

