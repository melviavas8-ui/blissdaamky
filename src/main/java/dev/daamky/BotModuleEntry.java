package dev.daamky;

public final class BotModuleEntry {
    private static final String pd90880d9 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final String II1Il11l111II11IIl;
   private final ModuleStructure l1I1IlllIlI;
// https://t.me/pasterplace

   BotModuleEntry(String displayName, ModuleStructure moduleStructure) {
      this.II1Il11l111II11IIl = displayName;
      this.l1I1IlllIlI = moduleStructure;
   }
// https://t.me/pasterplace

   public String II1Il11l111II11IIl() {
      return this.l1I1IlllIlI.getName();
   }
// https://t.me/pasterplace

   public String l1I1IlllIlI() {
      return this.l1I1IlllIlI.getDescription();
   }
// https://t.me/pasterplace

   public boolean IllI1l1l1() {
      return ModuleToggleProfileManager.isModuleEnabledInProfile(this.II1Il11l111II11IIl, this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   public void II1Il11l111II11IIl(boolean enabled) {
      ModuleToggleProfileManager.setModuleEnabledInProfile(this.II1Il11l111II11IIl, this.l1I1IlllIlI, enabled);
   }
// https://t.me/pasterplace

   public void l1lll11l1l() {
      this.II1Il11l111II11IIl(!this.IllI1l1l1());
   }
}

