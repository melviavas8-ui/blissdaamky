package dev.daamky;

public final class NoRender extends ModuleStructure {
   public static final String l1I1IlllIlI = "Огонь";
   public static final String IllI1l1l1 = "Тряска камеры";
   public static final String l1lll11l1l = "Анимация тотема";
   public static final String lIIl11l111lIIl1ll = "Тыква на экране";
   public static final String l1IIl11lI = "Вода на экране";
   public static final String I1lIIII1III1lIlIl1l = "Блоки на экране";
   public static final String l11lIlIIll1III1 = "Босс-бар";
   public static final String string = "Скорборд";
   public static final String Ill111IlI11l = "Тайтлы";
   public static final String I1l11IIlIIIll1l1lI1II = "Виньетка";
   public static final String I1I1lI111lII1 = "Плохие эффекты";
   public static final String lIll1Il1lll111l11l1I1llllI = "Погода";
   public static final String lIIIlI1III1 = "Туман";
   public static final String IIl1IlII1III1IIl1IIl = "Частицы";
   public static final String l11II1IIl1IIlIll1IIl = "Частицы разрушения";
   public static final String l1I1IIl111Il1l1l = "Камера клип";
   public static final String IIIlll111l111 = "Броня";
   public static final String lI1IlIl1I11II111l11l = "Стрелы в игроке";
   public static final String l1IIIIl1lII1IlIIIll1 = "Заморозка";
   private final MultiToggleSetting l11IllI1I11 = new MultiToggleSetting("Убирать")
      .addOption("Огонь", true)
      .addOption("Тряска камеры", true)
      .addOption("Анимация тотема", false)
      .addOption("Тыква на экране", true)
      .addOption("Вода на экране", true)
      .addOption("Блоки на экране", true)
      .addOption("Босс-бар", false)
      .addOption("Скорборд", false)
      .addOption("Тайтлы", false)
      .addOption("Виньетка", true)
      .addOption("Плохие эффекты", true)
      .addOption("Погода", false)
      .addOption("Туман", false)
      .addOption("Частицы", false)
      .addOption("Частицы разрушения", true)
      .addOption("Камера клип", true)
      .addOption("Броня", false)
      .addOption("Стрелы в игроке", true)
      .addOption("Заморозка", true);

   public NoRender() {
      super("NoRender", "Убирает лишние визуальные эффекты", ModuleCategory.RENDER);
      this.addSetting(this.l11IllI1I11);
   }

   public static boolean II1Il11l111II11IIl(String optionName) {
      NoRender module = (NoRender)ModuleRegistry.getInstance().getModuleByClass(NoRender.class);
      return module != null && module.isEnabled() && module.l11IllI1I11.isOptionEnabled(optionName);
   }
}

