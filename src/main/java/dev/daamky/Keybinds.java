package dev.daamky;

import java.util.ArrayList;
import java.util.List;

public final class Keybinds extends AnimatedListHudElement {
   public Keybinds() {
      super("Keybinds", "keybinds", 7.0F, 200.0F);
   }

   @Override
   protected String getHeaderGlyph() {
      return "Q";
   }

   @Override
   protected String getEntryGlyph(AnimatedListHudElement.HudListEntry hudEntry) {
      ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();
      ModuleStructure module = moduleRegistry == null ? null : moduleRegistry.getModuleByName(hudEntry.title());
      return module == null ? null : module.getCategory().l1I1IlllIlI();
   }

   @Override
   protected boolean renderDetailAsBadge() {
      return true;
   }

   @Override
   protected String getHeaderLabel() {
      return "Keybinds";
   }

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

      return arrayList;
   }
}

