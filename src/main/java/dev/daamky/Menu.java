package dev.daamky;

/**
 * Выбор вида ClickGUI: Daamky (стандартный) или Dropdown (колонки категорий с выпадающими настройками).
 */
public final class Menu extends ModuleStructure {
   private static Menu instance;
   private final MultiOptionSetting type = new MultiOptionSetting("Тип", "Daamky", "Daamky", "Dropdown");

   public Menu() {
      super("Menu", "Выбор вида ClickGUI: Daamky или Dropdown", ModuleCategory.RENDER);
      this.addSettings(this.type);
      instance = this;
   }

   public static boolean isDropdownSelected() {
      return instance != null && instance.type.isSelected("Dropdown");
   }
}
