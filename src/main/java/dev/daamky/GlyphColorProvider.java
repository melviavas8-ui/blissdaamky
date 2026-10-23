package dev.daamky;

@FunctionalInterface
public interface GlyphColorProvider {
   int color(int codePoint, float x, float y);
}
