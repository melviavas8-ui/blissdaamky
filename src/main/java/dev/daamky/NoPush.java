package dev.daamky;

public final class NoPush extends ModuleStructure {
    private static final String p6ffc743d = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiToggleSetting l1I1IlllIlI = new MultiToggleSetting("Не отталкиваться от")
      .addOption("Сущностей", true)
      .addOption("Воды", true);
// https://t.me/pasterplace

   public NoPush() {
      super("NoPush", "Убирает отталкивание от сущностей и течения", ModuleCategory.PLAYER);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   private static NoPush lI1IlIl1I11II111l11l() {
      NoPush module = (NoPush)ModuleRegistry.getInstance().getModuleByClass(NoPush.class);
      return module != null && module.isEnabled() ? module : null;
   }
// https://t.me/pasterplace

   public static boolean l1I1IIl111Il1l1l() {
      NoPush noPush = lI1IlIl1I11II111l11l();
      return noPush != null && noPush.l1I1IlllIlI.isOptionEnabled("Сущностей");
   }
// https://t.me/pasterplace

   public static boolean IIIlll111l111() {
      NoPush noPush = lI1IlIl1I11II111l11l();
      return noPush != null && noPush.l1I1IlllIlI.isOptionEnabled("Воды");
   }
}

