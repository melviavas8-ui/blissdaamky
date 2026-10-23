package dev.daamky;

import dev.daamky.mixin.client.BossHealthOverlayAccessor;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;

public final class Aceopxykhbtm {
    private static final String p6c445d1f = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final String II1Il11l111II11IIl = "асеорхукнвтм";
   private static final String l1I1IlllIlI = "aceopxykhbtm";
// https://t.me/pasterplace

   private Aceopxykhbtm() {
   }
// https://t.me/pasterplace

   public static boolean II1Il11l111II11IIl() {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.inGameHud == null) {
         return false;
      }
// https://t.me/pasterplace

      for (net.minecraft.client.gui.hud.ClientBossBar bossBar : ((BossHealthOverlayAccessor)client.inGameHud.getBossBarHud()).daamky$getEvents().values()) {
         if (bossBar.getName() != null && II1Il11l111II11IIl(bossBar.getName().getString())) {
            return true;
         }
      }

      return false;
   }
// https://t.me/pasterplace

   private static boolean II1Il11l111II11IIl(String message) {
      String lowercaseMessage = message.toLowerCase(Locale.ROOT);
      if (!lowercaseMessage.contains("бой") && !lowercaseMessage.contains("бою") && !lowercaseMessage.contains("пвп")) {
         StringBuilder stringBuilder = new StringBuilder(lowercaseMessage.length());

         for (int characterIndex = 0; characterIndex < lowercaseMessage.length(); characterIndex++) {
            char character = lowercaseMessage.charAt(characterIndex);
            int latinCharacterIndex = "асеорхукнвтм".indexOf(character);
            stringBuilder.append(latinCharacterIndex < 0 ? character : "aceopxykhbtm".charAt(latinCharacterIndex));
         }

         return stringBuilder.indexOf("pvp") >= 0;
      } else {
         return true;
      }
   }
}

