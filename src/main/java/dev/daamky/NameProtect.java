package dev.daamky;

import java.util.Locale;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;

public final class NameProtect extends ModuleStructure {
    private static final String p4f06a254 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting l1I1IlllIlI = new MultiOptionSetting("Replacement", "Daamky", "Daamky", "Protected", "Me");
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("Hide Friends", true);
// https://t.me/pasterplace

   public NameProtect() {
      super("NameProtect", "Hides your username on screen", ModuleCategory.WORLD);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }
// https://t.me/pasterplace

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
// https://t.me/pasterplace

            return displayText;
         } else {
            return inputText;
         }
      } else {
         return inputText;
      }
   }
}

