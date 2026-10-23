package dev.daamky;

import java.util.ArrayList;
import java.util.List;

public final class ModuleProfile {
    private static final String pe0aec076 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final String II1Il11l111II11IIl;
// https://t.me/pasterplace

   public ModuleProfile(String profileName) {
      this.II1Il11l111II11IIl = profileName;
   }
// https://t.me/pasterplace

   public List<BotModuleEntry> II1Il11l111II11IIl() {
      ArrayList arrayList = new ArrayList();

      for (Object module : ModuleRegistry.getInstance().getModules()) {
         if (ModuleToggleProfileManager.isProfileManagedModule((ModuleStructure)module)) {
            arrayList.add(new BotModuleEntry(this.II1Il11l111II11IIl, (ModuleStructure)module));
         }
      }

      return arrayList;
   }
// https://t.me/pasterplace

   public BotModuleEntry II1Il11l111II11IIl(String moduleName) {
      for (Object moduleEntry : this.II1Il11l111II11IIl()) {
         if (((BotModuleEntry)moduleEntry).II1Il11l111II11IIl().equalsIgnoreCase(moduleName)) {
            return (BotModuleEntry)moduleEntry;
         }
      }
// https://t.me/pasterplace

      return null;
   }
}

