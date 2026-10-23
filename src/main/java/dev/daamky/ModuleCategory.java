package dev.daamky;

public enum ModuleCategory {
   COMBAT("Combat", "A"),
   MOVEMENT("Movement", "B"),
   RENDER("Render", "C"),
   PLAYER("Player", "D"),
   WORLD("World", "E"),
   BOTS("Bots", "G"),
   COSMETICS("Cosmetics", "H"),
   THEMES("Themes", "J"),
   CLOUD_CONFIG("Claude config", "F"),
   GLOBALS("Globals", "G");
// https://t.me/pasterplace

   private final String I1l11IIlIIIll1l1lI1II;
   private final String I1I1lI111lII1;
// https://t.me/pasterplace

   ModuleCategory(String categoryName, String iconGlyph) {
      this.I1l11IIlIIIll1l1lI1II = categoryName;
      this.I1I1lI111lII1 = iconGlyph;
   }
// https://t.me/pasterplace

   public String II1Il11l111II11IIl() {
      return this.I1l11IIlIIIll1l1lI1II;
   }
// https://t.me/pasterplace

   public String l1I1IlllIlI() {
      return this.I1I1lI111lII1;
   }
// https://t.me/pasterplace

   // $VF: synthetic method
   private static ModuleCategory[] IllI1l1l1() {
      return new ModuleCategory[]{
         COMBAT, MOVEMENT, RENDER, PLAYER, WORLD, BOTS, COSMETICS, THEMES, CLOUD_CONFIG, GLOBALS
      };
   }
}
