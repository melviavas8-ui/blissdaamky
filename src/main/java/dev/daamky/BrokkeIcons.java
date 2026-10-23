package dev.daamky;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public final class BrokkeIcons {
    private static final String sourceAttributionUrl = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    public static final float DEFAULT_FONT_SIZE = 5.0F;
    public static final float HEADER_FONT_SIZE = 6.2F;
    public static final float BODY_FONT_SIZE = 5.7F;
    public static final float SMALL_FONT_SIZE = 4.8F;
    public static final MsdfFont iconFont = Medium.loadFont("brokke_icons");
// https://t.me/pasterplace

    private BrokkeIcons() {
    }
// https://t.me/pasterplace

    public static void drawPanelBackground(DrawContext drawContext, float x, float y, float width, float height, float opacity) {
        drawRoundedPanelBackground(drawContext, x, y, width, height, 5.0F, opacity);
    }
// https://t.me/pasterplace

    public static void drawRoundedPanelBackground(DrawContext drawContext, float x, float y, float width, float height, float cornerRadius, float opacity) {
        if (!(opacity <= 0.005F) && !(width <= 0.5F) && !(height <= 0.5F)) {
            Accent accent = Accent.getInstance();
            boolean accentBlurEnabled = accent.isGlassStyle();
            if (!InSampler.isBlurReady()) {
                RoundedGradientRectBuilder.at(x, y, width, height)
                .cornerRadius(cornerRadius)
                .color(Accent.getTintedBackgroundColor((int)(220.0F * opacity)))
                .render(drawContext);
            } else if (accentBlurEnabled) {
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

            RoundedGradientRectBuilder panelFill = RoundedGradientRectBuilder.at(x, y, width, height).cornerRadius(cornerRadius);
            if (accentBlurEnabled) {
// https://t.me/pasterplace

            }

            panelFill.color(Accent.getAccentBackgroundColor((int)(36.0F * opacity))).render(drawContext);
            RoundedRectBuilder.createAt(x, y, width, height)
            .cornerRadius(cornerRadius)
            .borderWidth(0.85F)
            .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)(70.0F * opacity)))
            .alpha(opacity)
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    public static void drawPanelOutline(DrawContext drawContext, float x, float y, float width, float height, float opacity) {
        if (!(opacity <= 0.01F)) {
            RoundedRectBuilder.createAt(x - 1.5F, y - 1.5F, width + 3.0F, height + 3.0F)
            .cornerRadius(6.5F)
            .borderWidth(0.85F)
            .color(Accent.getAccentColorWithAlpha((int)(160.0F * opacity)))
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    public static void drawCenteredText(DrawContext drawContext, String displayText, float x, float baselineY, float fontSize, RgbaColor textColor) {
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, displayText, x, baselineY, fontSize)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(textColor)
        .render(drawContext);
    }
// https://t.me/pasterplace

    public static float measureTextWidth(String displayText, float fontSize) {
        return Medium.DEFAULT_FONT.measureWidth(displayText, fontSize);
    }
// https://t.me/pasterplace

    public static void drawVerticalDivider(DrawContext drawContext, float x, float centerY, float opacity) {
        RoundedGradientRectBuilder.at(x, centerY - 3.85F, 0.85F, 7.7F)
        .cornerRadius(0.0F)
        .color(Accent.getAccentColorWithAlpha((int)(14.0F * opacity)))
        .render(drawContext);
    }
// https://t.me/pasterplace

    public static void drawHorizontalDivider(DrawContext drawContext, float x, float y, float width, float opacity) {
        RoundedGradientRectBuilder.at(x, y, width, 0.85F)
        .cornerRadius(0.0F)
        .color(Accent.getAccentColorWithAlpha((int)(14.0F * opacity)))
        .render(drawContext);
    }
// https://t.me/pasterplace

    public static void drawColorSwatch(DrawContext drawContext, float centerX, float centerY, float diameter, RgbaColor swatchColor) {
        RoundedGradientRectBuilder.at(centerX - diameter * 0.5F, centerY - diameter * 0.5F, diameter, diameter)
        .cornerRadius(diameter * 0.5F)
        .color(swatchColor)
        .render(drawContext);
    }
// https://t.me/pasterplace

    public static void drawRoundedPill(DrawContext drawContext, float x, float y, float width, float height, float opacity) {
        RoundedGradientRectBuilder.at(x, y, width, height)
        .cornerRadius(2.5F)
        .color(Accent.getAccentColorWithAlpha((int)(16.0F * opacity)))
        .render(drawContext);
    }
// https://t.me/pasterplace

    public static void drawProgressBar(
    DrawContext drawContext, float x, float y, float width, float height, float progress, RgbaColor progressColor, float opacity
    ) {
        RoundedGradientRectBuilder.at(x, y, width, height)
        .cornerRadius(height * 0.5F)
        .color(Accent.getAccentColorWithAlpha((int)(22.0F * opacity)))
        .render(drawContext);
        float clampedProgress = Math.clamp(progress, 0.0F, 1.0F);
        if (clampedProgress > 0.001F) {
            RoundedGradientRectBuilder.at(x, y, Math.max(height, width * clampedProgress), height)
            .cornerRadius(height * 0.5F)
            .color(progressColor)
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    public static RgbaColor whiteWithAlpha(float alpha) {
        return Accent.getAccentColorWithAlpha((int)(255.0F * alpha));
    }
// https://t.me/pasterplace

    public static RgbaColor lightGrayWithAlpha(float alpha) {
        return Accent.getTextColorWithAlpha((int)(250.0F * alpha));
    }
// https://t.me/pasterplace

    public static RgbaColor mutedGrayWithAlpha(float alpha) {
        return Accent.getDarkerTextColor((int)(230.0F * alpha));
    }
// https://t.me/pasterplace

    public static RgbaColor progressColor(float progress, float alpha) {
        float clampedProgress = Math.clamp(progress, 0.0F, 1.0F);
        int red = (int)(255.0F - clampedProgress * 120.0F);
        int green = (int)(110.0F + clampedProgress * 120.0F);
        int blue = (int)(110.0F + clampedProgress * 40.0F);
        return RgbaColor.rgba(red, green, blue, (int)(255.0F * alpha));
    }
// https://t.me/pasterplace

    public static void drawCenteredText(DrawContext drawContext, String displayText, float x, float baselineY, float fontSize, float opacity) {
        drawCenteredTextWithFont(drawContext, Medium.ICON_FONT, displayText, x, baselineY, fontSize, opacity);
    }
// https://t.me/pasterplace

    public static void drawCenteredTextWithFont(
    DrawContext drawContext, MsdfFont font, String displayText, float x, float baselineY, float fontSize, float opacity
    ) {
        GuiTextBuilder.createAt(font, displayText, x, baselineY, fontSize)
        .centered()
        .color(whiteWithAlpha(opacity))
        .render(drawContext);
    }
// https://t.me/pasterplace

    public static void drawTexture(DrawContext drawContext, Identifier identifier, float x, float y, float size, float opacity) {
        if (identifier != null) {
            drawContext.getMatrices().pushMatrix();
            drawContext.getMatrices().translate(x, y);
            drawContext.getMatrices().scale(size / 18.0F);
            drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, 0, 0, 18, 18, ColorHelper.getWhite(Math.clamp(opacity, 0.0F, 1.0F)));
            drawContext.getMatrices().popMatrix();
        }
    }
// https://t.me/pasterplace

    public static void drawItemStack(DrawContext drawContext, ItemStack stack, float x, float y, float size) {
        if (stack != null && !stack.isEmpty()) {
            drawContext.getMatrices().pushMatrix();
            drawContext.getMatrices().translate(x, y);
            drawContext.getMatrices().scale(size / 16.0F);
            GuiEventRenderer.flushBatchedElements();
            drawContext.drawItem(stack, 0, 0);
            drawContext.getMatrices().popMatrix();
        }
    }
// https://t.me/pasterplace

    public static String toRomanNumeral(int value) {
        return switch (value) {
        case 1 -> "I";
        case 2 -> "II";
        case 3 -> "III";
        case 4 -> "IV";
        case 5 -> "V";
        case 6 -> "VI";
        case 7 -> "VII";
        case 8 -> "VIII";
        case 9 -> "IX";
        case 10 -> "X";
            default -> String.valueOf(value);
        };
    }
// https://t.me/pasterplace

    public static String formatDurationTicks(int durationTicks) {
        int totalSeconds = Math.max(0, durationTicks / 20);
        return totalSeconds / 60 + ":" + String.format("%02d", totalSeconds % 60);
    }
}

