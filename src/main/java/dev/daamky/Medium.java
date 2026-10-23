package dev.daamky;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.Identifier;

public final class Medium {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final String NAMESPACE = "daamky";
    private static final String FONT_RESOURCE_PREFIX = "fonts/";
    private static final Map<String, MsdfFont> FONTS_BY_NAME = new LinkedHashMap<>();
    public static final MsdfFont DEFAULT_FONT = loadFont("medium");
    public static final MsdfFont ICON_FONT = loadFont("lucide");
    private static MsdfFont currentFont = DEFAULT_FONT;
// https://t.me/pasterplace

    private Medium() {
    }
// https://t.me/pasterplace

    public static void initializeFonts() {
    }
// https://t.me/pasterplace

    public static MsdfFont loadFont(String fontName) {
        return loadFontFromPath(fontName, "fonts/" + fontName);
    }
// https://t.me/pasterplace

    public static MsdfFont loadFontFromPath(String fontName, String resourcePath) {
        return FONTS_BY_NAME.computeIfAbsent(fontName, text2x -> new MsdfFont(text2x, getResourceIdentifier(resourcePath + ".json"), getResourceIdentifier(resourcePath + ".png")));
    }
// https://t.me/pasterplace

    public static MsdfFont getFont(String fontName) {
        Object font = FONTS_BY_NAME.get(fontName);
        if (font == null) {
            throw new IllegalArgumentException("Unknown font: " + fontName);
        } else {
            return (MsdfFont)font;
        }
    }
// https://t.me/pasterplace

    public static boolean isFontLoaded(String fontName) {
        return FONTS_BY_NAME.containsKey(fontName);
    }
// https://t.me/pasterplace

    public static Collection<MsdfFont> getLoadedFonts() {
        return Collections.unmodifiableCollection(FONTS_BY_NAME.values());
    }
// https://t.me/pasterplace

    public static MsdfFont getCurrentFont() {
        return currentFont;
    }
// https://t.me/pasterplace

    public static void setCurrentFont(MsdfFont font) {
// https://t.me/pasterplace
        currentFont = font;
    }
// https://t.me/pasterplace

    public static void clearFontCaches() {
        FONTS_BY_NAME.values().forEach(MsdfFont::clearCachedAtlasData);
        GlyphAtlasEntry.clearCache();
    }
// https://t.me/pasterplace

    private static Identifier getResourceIdentifier(String resourcePath) {
        return Identifier.of("daamky", resourcePath);
    }
}

