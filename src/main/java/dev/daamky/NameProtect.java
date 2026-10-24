package dev.daamky;

import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;

public final class NameProtect extends ModuleStructure {
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Replacement", "Daamky", "Daamky", "Protected", "Me");
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("Hide Friends", true);

   public NameProtect() {
      super("NameProtect", "Hides your username on screen", ModuleCategory.WORLD);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }

   public static String II1Il11l111II11IIl(String inputText) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (inputText != null && !inputText.isEmpty() && client != null) {
         ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();
         NameProtect nameProtectModule = moduleRegistry == null ? null : moduleRegistry.getModuleByClass(NameProtect.class);
         if (nameProtectModule != null && nameProtectModule.isEnabled()) {
            String displayText = inputText;
            String playerName = client.player != null ? client.player.getGameProfile().name() : client.getSession().getUsername();
            if (playerName != null && !playerName.isEmpty() && displayText.contains(playerName)) {
               displayText = displayText.replace(playerName, nameProtectModule.l1I1IlllIlI.getValue());
            }

            if (nameProtectModule.IllI1l1l1.getValue()) {
               for (Object friendName : FriendManager.getInstance().getFriendNames()) {
                  if (displayText.toLowerCase(Locale.ROOT).contains((CharSequence)friendName)) {
                     displayText = displayText.replaceAll("(?i)" + Pattern.quote((String)friendName), "Friend");
                  }
               }
            }

            return displayText;
         } else {
            return inputText;
         }
      } else {
         return inputText;
      }
   }
}

