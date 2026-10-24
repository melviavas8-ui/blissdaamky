package dev.daamky;

public final class NoPush extends ModuleStructure {
   private final MultiToggleSetting l1I1IlllIlI = new MultiToggleSetting("Не отталкиваться от")
      .addOption("Сущностей", true)
      .addOption("Воды", true);

   public NoPush() {
      super("NoPush", "Убирает отталкивание от сущностей и течения", ModuleCategory.PLAYER);
      this.addSetting(this.l1I1IlllIlI);
   }

   private static NoPush lI1IlIl1I11II111l11l() {
      NoPush module = (NoPush)ModuleRegistry.getInstance().getModuleByClass(NoPush.class);
      return module != null && module.isEnabled() ? module : null;
   }

   public static boolean l1I1IIl111Il1l1l() {
      NoPush noPush = lI1IlIl1I11II111l11l();
      return noPush != null && noPush.l1I1IlllIlI.isOptionEnabled("Сущностей");
   }

   public static boolean IIIlll111l111() {
      NoPush noPush = lI1IlIl1I11II111l11l();
      return noPush != null && noPush.l1I1IlllIlI.isOptionEnabled("Воды");
   }
}

