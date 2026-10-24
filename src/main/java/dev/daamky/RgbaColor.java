package dev.daamky;

import java.util.Locale;

public record RgbaColor(int colorValue) {
    public static RgbaColor WHITE;
    public static RgbaColor BLACK;
    public static RgbaColor TRANSPARENT;


    public static RgbaColor fromArgb(int i0) {
        return new RgbaColor(i0);
    }

    public static RgbaColor rgb(int i0, int i1, int i2) {
        return RgbaColor.rgba(i0, i1, i2, 255);
    }

    public static RgbaColor rgba(int i0, int i1, int i2, int i3) {
        return new RgbaColor((((RgbaColor.clampByte(i3) << 24) | (RgbaColor.clampByte(i0) << 16)) | (RgbaColor.clampByte(i1) << 8)) | RgbaColor.clampByte(i2));
    }

    public static RgbaColor rgbaWithOpacity(int i0, int i1, int i2, float f0) {
        return RgbaColor.rgba(i0, i1, i2, Math.round(f0 * 255.0f));
    }

    public static RgbaColor fromNormalized(float f0, float f1, float f2, float f3) {
        return RgbaColor.rgba(Math.round(f0 * 255.0f), Math.round(f1 * 255.0f), Math.round(f2 * 255.0f), Math.round(f3 * 255.0f));
    }

    public static RgbaColor parseHex(String str0) {
        if (str0 == null) {
            throw new IllegalArgumentException("Color value cannot be null");
        }

        String hex = stripHexPrefix(str0);
        if (hex.length() == 3 || hex.length() == 4) {
            StringBuilder expanded = new StringBuilder(hex.length() * 2 + (hex.length() == 3 ? 2 : 0));
            for (int index = 0; index < hex.length(); index++) {
                appendRepeatedHexDigit(expanded, hex.charAt(index));
            }
            if (hex.length() == 3) {
                expanded.append("FF");
            }
            hex = expanded.toString();
        } else if (hex.length() == 6) {
            hex += "FF";
        }

        if (hex.length() != 8) {
            throw new IllegalArgumentException("Expected a 3, 4, 6, or 8 digit hex color: " + str0);
        }

        int red = parseHexByte(hex, 0);
        int green = parseHexByte(hex, 2);
        int blue = parseHexByte(hex, 4);
        int alpha = parseHexByte(hex, 6);
        return rgba(red, green, blue, alpha);
    }

    public static RgbaColor fromHsv(float f0, float f1, float f2) {
        return RgbaColor.fromHsvWithAlpha(f0, f1, f2, 1.0f);
    }

    public static RgbaColor fromHsvWithAlpha(float f0, float f1, float f2, float f3) {
        float f11 = 0.0f;
        float f12 = 0.0f;
        RgbaColor l1i1illlili40 = null;
        float f4 = 0.0f;

        float f7 = 0.0f;
        float f8 = 0.0f;
        float f9 = 0.0f;
        float f10 = 0.0f;
        f4 = (f0 - ((float) Math.floor((double) f0))) * 6.0f;
        f11 = RgbaColor.clampUnit(f1);

        f12 = RgbaColor.clampUnit(f2);

        f7 = f4 - ((float) Math.floor((double) f4));
        f8 = f12 * (1.0f - f11);
        f9 = f12 * (1.0f - (f11 * f7));
        f10 = f12 * (1.0f - (f11 * (1.0f - f7)));
        switch ((int) f4) {
        case 0:
            l1i1illlili40 = RgbaColor.fromNormalized(f12, f10, f8, f3);
            break;
        case 1:
            l1i1illlili40 = RgbaColor.fromNormalized(f9, f12, f8, f3);
            break;
        case 2:
            l1i1illlili40 = RgbaColor.fromNormalized(f8, f12, f10, f3);
            break;
        case 3:
            l1i1illlili40 = RgbaColor.fromNormalized(f8, f9, f12, f3);
            break;
        case 4:
            l1i1illlili40 = RgbaColor.fromNormalized(f10, f8, f12, f3);
            break;
        default:
            l1i1illlili40 = RgbaColor.fromNormalized(f12, f8, f9, f3);
            break;
        }
        return l1i1illlili40;
    }

    public int red() {
        return (this.colorValue >> 16) & 255;
    }

    public int green() {
        return (this.colorValue >> 8) & 255;
    }

    public int blue() {
        return this.colorValue & 255;
    }

    public int alpha() {
        return this.colorValue >>> 24;
    }

    public RgbaColor withAlpha(int i0) {
        return new RgbaColor((RgbaColor.clampByte(i0) << 24) | (this.colorValue & 16777215));
    }

    public RgbaColor withAlphaNormalized(float f0) {
        return withAlpha(Math.round(RgbaColor.clampUnit(f0) * 255.0f));
    }

    public RgbaColor multiplyAlpha(float f0) {
        return withAlpha(Math.round(((float) alpha()) * RgbaColor.clampUnit(f0)));
    }

    public RgbaColor mix(RgbaColor l1i1illlili40, float f0) {
        float f2 = 0.0f;

        f2 = RgbaColor.clampUnit(f0);

        return RgbaColor.rgba(RgbaColor.interpolateChannel(red(), l1i1illlili40.red(), f2), RgbaColor.interpolateChannel(green(), l1i1illlili40.green(), f2), RgbaColor.interpolateChannel(blue(), l1i1illlili40.blue(), f2), RgbaColor.interpolateChannel(alpha(), l1i1illlili40.alpha(), f2));
    }

    public RgbaColor lighten(float f0) {
        return mix(RgbaColor.rgba(255, 255, 255, alpha()), f0);
    }

    public RgbaColor darken(float f0) {
        return mix(RgbaColor.rgba(0, 0, 0, alpha()), f0);
    }

    public String toHexString() {
        Object[] arrobj0 = null;
        arrobj0 = new Object[4];
        arrobj0[0] = (Integer) red();
        arrobj0[1] = (Integer) green();
        arrobj0[2] = (Integer) blue();
        arrobj0[3] = (Integer) alpha();
        return String.format(Locale.ROOT, "#%02X%02X%02X%02X", arrobj0);
    }

    private static String stripHexPrefix(String str0) {
        String hex = str0.strip();
        if (hex.startsWith("#")) {
            return hex.substring(1);
        }
        return hex.startsWith("0x") || hex.startsWith("0X") ? hex.substring(2) : hex;
    }

    private static int parseHexByte(String str0, int i0) {
        return Integer.parseInt(str0, i0, i0 + 2, 16);
    }

    private static int interpolateChannel(int i0, int i1, float f0) {
        return Math.round(((float) i0) + (((float) (i1 - i0)) * f0));
    }

    private static int clampByte(int i0) {
        return Math.clamp((long) i0, 0, 255);
    }

    private static float clampUnit(float f0) {
        return Math.clamp(f0, 0.0f, 1.0f);
    }

    public int toArgb() {
        return this.colorValue;
    }

    private static void appendRepeatedHexDigit(StringBuilder sb0, int i0) {
        sb0.append((char) i0).append((char) i0);
        return;
    }

    static {
        RgbaColor.WHITE = new RgbaColor(-1);
        RgbaColor.BLACK = new RgbaColor(-16777216);
        RgbaColor.TRANSPARENT = new RgbaColor(0);
    }

}

