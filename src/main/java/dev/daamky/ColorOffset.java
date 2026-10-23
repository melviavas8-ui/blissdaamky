package dev.daamky;

public record ColorOffset(int colorArgb, float offsetX, float offsetY, float leadingExtent, float trailingExtent) {
    public static ColorOffset offset(int colorArgb, float offsetX, float offsetY) {
        return offsetWithTrailingExtent(colorArgb, offsetX, offsetY, 0.0F);
    }

    public static ColorOffset offset(int colorArgb, float offsetX, float offsetY, float trailingExtent) {
        return offsetWithTrailingExtent(colorArgb, offsetX, offsetY, trailingExtent);
    }

    public static ColorOffset offset(RgbaColor color, float offsetX, float offsetY) {
        return offset(color.toArgb(), offsetX, offsetY);
    }

    public static ColorOffset offset(RgbaColor color, float offsetX, float offsetY, float trailingExtent) {
        return offsetWithTrailingExtent(color.toArgb(), offsetX, offsetY, trailingExtent);
    }

    public static ColorOffset offsetWithTrailingExtent(int colorArgb, float offsetX, float offsetY, float trailingExtent) {
        return new ColorOffset(colorArgb, offsetX, offsetY, 0.0F, Math.max(trailingExtent, 0.0F));
    }

    public static ColorOffset leadingExtent(int colorArgb, float extent) {
        return new ColorOffset(colorArgb, 0.0F, 0.0F, Math.max(extent, 0.0F), 0.0F);
    }

    public static ColorOffset leadingExtent(RgbaColor color, float extent) {
        return leadingExtent(color.toArgb(), extent);
    }

    public static ColorOffset centeredExtent(int colorArgb, float extent) {
        float nonNegativeExtent = Math.max(extent, 0.0F);
        return new ColorOffset(colorArgb, 0.0F, 0.0F, nonNegativeExtent * 0.5F, nonNegativeExtent);
    }

    public static ColorOffset centeredExtent(RgbaColor color, float extent) {
        return centeredExtent(color.toArgb(), extent);
    }

    public ColorOffset withColor(int colorArgb) {
        return new ColorOffset(colorArgb, this.offsetX, this.offsetY, this.leadingExtent, this.trailingExtent);
    }

    public ColorOffset withColor(RgbaColor color) {
        return withColor(color.toArgb());
    }

    public float totalExtent() {
        return Math.max(Math.abs(this.offsetX), Math.abs(this.offsetY)) + this.leadingExtent + this.trailingExtent;
    }
}
