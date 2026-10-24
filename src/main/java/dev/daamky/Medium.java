package dev.daamky;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.Identifier;

public final class Medium {
    private static final String NAMESPACE = "daamky";
    private static final String FONT_RESOURCE_PREFIX = "fonts/";
    private static final Map<String, MsdfFont> FONTS_BY_NAME = new LinkedHashMap<>();
    public static final MsdfFont DEFAULT_FONT = loadFont("medium");
    public static final MsdfFont ICON_FONT = loadFont("lucide");
    private static MsdfFont currentFont = DEFAULT_FONT;

    private Medium() {
    }

    public static void initializeFonts() {
    }

    public static MsdfFont loadFont(String fontName) {
        return loadFontFromPath(fontName, "fonts/" + fontName);
    }

    public static MsdfFont loadFontFromPath(String fontName, String resourcePath) {
        return FONTS_BY_NAME.computeIfAbsent(fontName, text2x -> new MsdfFont(text2x, getResourceIdentifier(resourcePath + ".json"), getResourceIdentifier(resourcePath + ".png")));
    }

    public static MsdfFont getFont(String fontName) {
        Object font = FONTS_BY_NAME.get(fontName);
        if (font == null) {
            throw new IllegalArgumentException("Unknown font: " + fontName);
        } else {
            return (MsdfFont)font;
        }
    }

    public static boolean isFontLoaded(String fontName) {
        return FONTS_BY_NAME.containsKey(fontName);
    }

    public static Collection<MsdfFont> getLoadedFonts() {
        return Collections.unmodifiableCollection(FONTS_BY_NAME.values());
    }

    public static MsdfFont getCurrentFont() {
        return currentFont;
    }

    public static void setCurrentFont(MsdfFont font) {
        currentFont = font;
    }

    public static void clearFontCaches() {
        FONTS_BY_NAME.values().forEach(MsdfFont::clearCachedAtlasData);
        GlyphAtlasEntry.clearCache();
    }

    private static Identifier getResourceIdentifier(String resourcePath) {
        return Identifier.of("daamky", resourcePath);
    }
}

