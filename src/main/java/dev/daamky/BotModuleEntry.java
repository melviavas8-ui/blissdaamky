package dev.daamky;

public final class BotModuleEntry {
   private final String II1Il11l111II11IIl;
   private final ModuleStructure l1I1IlllIlI;

   BotModuleEntry(String displayName, ModuleStructure moduleStructure) {
      this.II1Il11l111II11IIl = displayName;
      this.l1I1IlllIlI = moduleStructure;
   }

   public String II1Il11l111II11IIl() {
      return this.l1I1IlllIlI.getName();
   }

   public String l1I1IlllIlI() {
      return this.l1I1IlllIlI.getDescription();
   }

   public boolean IllI1l1l1() {
      return ModuleToggleProfileManager.isModuleEnabledInProfile(this.II1Il11l111II11IIl, this.l1I1IlllIlI);
   }

   public void II1Il11l111II11IIl(boolean enabled) {
      ModuleToggleProfileManager.setModuleEnabledInProfile(this.II1Il11l111II11IIl, this.l1I1IlllIlI, enabled);
   }

   public void l1lll11l1l() {
      this.II1Il11l111II11IIl(!this.IllI1l1l1());
   }
}

