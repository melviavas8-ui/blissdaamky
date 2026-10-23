package dev.daamky;

import java.util.ArrayList;
import java.util.List;

public final class Keybinds extends AnimatedListHudElement {
    private static final String p8316bf61 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public Keybinds() {
      super("Keybinds", "keybinds", 7.0F, 200.0F);
   }
// https://t.me/pasterplace

   @Override
   protected String getHeaderGlyph() {
      return "Q";
   }
// https://t.me/pasterplace

   @Override
   protected String getEntryGlyph(AnimatedListHudElement.HudListEntry hudEntry) {
      ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();
      ModuleStructure module = moduleRegistry == null ? null : moduleRegistry.getModuleByName(hudEntry.title());
      return module == null ? null : module.getCategory().l1I1IlllIlI();
   }
// https://t.me/pasterplace

   @Override
   protected boolean renderDetailAsBadge() {
      return true;
   }
// https://t.me/pasterplace

   @Override
   protected String getHeaderLabel() {
      return "Keybinds";
   }
// https://t.me/pasterplace

   @Override
   protected List<AnimatedListHudElement.HudListEntry> getEntries(boolean chatScreenOpen) {
      ArrayList arrayList = new ArrayList();
      ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();
      if (moduleRegistry == null) {
         return arrayList;
      }

      for (Object module : moduleRegistry.getModules()) {
         if (((ModuleStructure)module).isEnabled() && ((ModuleStructure)module).getKeybind() != 0) {
            arrayList.add(
               new AnimatedListHudElement.HudListEntry(
                  ((ModuleStructure)module).getName(),
                  dev.daamky.KeybindManager.getDisplayName(((ModuleStructure)module).getKeybind()),
                  null,
                  BrokkeIcons.whiteWithAlpha(1.0F),
                  null,
                  -1.0F
               )
            );
         }
      }
// https://t.me/pasterplace

      return arrayList;
   }
}

