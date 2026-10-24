package dev.daamky;

import java.util.ArrayList;
import java.util.List;

public final class ModuleProfile {
   private final String II1Il11l111II11IIl;

   public ModuleProfile(String profileName) {
      this.II1Il11l111II11IIl = profileName;
   }

   public List<BotModuleEntry> II1Il11l111II11IIl() {
      ArrayList arrayList = new ArrayList();

      for (Object module : ModuleRegistry.getInstance().getModules()) {
         if (ModuleToggleProfileManager.isProfileManagedModule((ModuleStructure)module)) {
            arrayList.add(new BotModuleEntry(this.II1Il11l111II11IIl, (ModuleStructure)module));
         }
      }

      return arrayList;
   }

   public BotModuleEntry II1Il11l111II11IIl(String moduleName) {
      for (Object moduleEntry : this.II1Il11l111II11IIl()) {
         if (((BotModuleEntry)moduleEntry).II1Il11l111II11IIl().equalsIgnoreCase(moduleName)) {
            return (BotModuleEntry)moduleEntry;
         }
      }

      return null;
   }
}

