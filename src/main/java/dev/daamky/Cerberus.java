package dev.daamky;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.text.StyleSpriteSource.Font;

final class Cerberus {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final Map<Integer, Cerberus.PlayerRank> RANKS_BY_CODE_POINT = new HashMap<>();
   private static final Map<String, Cerberus.PlayerRank> RANKS_BY_GROUP_ID = new HashMap<>();
// https://t.me/pasterplace

   private Cerberus() {
   }
// https://t.me/pasterplace

   private static void registerCodePointRank(int unicodeCodePoint, String rankName, int colorRgb) {
      RANKS_BY_CODE_POINT.put(unicodeCodePoint, new Cerberus.PlayerRank(rankName, 0xFF000000 | colorRgb));
   }
// https://t.me/pasterplace

   private static void registerGroupRank(String groupId, String displayName, int colorRgb) {
      RANKS_BY_GROUP_ID.put("custom:groups/" + groupId, new Cerberus.PlayerRank(displayName, 0xFF000000 | colorRgb));
   }
// https://t.me/pasterplace

   static Cerberus.PlayerRank getPlayerRank(Text component) {
      return component == null ? null : (Cerberus.PlayerRank)component.visit((style, textx) -> {
         Cerberus.PlayerRank playerRank = getStyleRank(style);
         if (playerRank != null && !textx.isEmpty()) {
            return Optional.of(playerRank);
         }

         int textIndex = 0;

         while (textIndex < textx.length()) {
            int codePoint = textx.codePointAt(textIndex);
            Object glyphRank = RANKS_BY_CODE_POINT.get(codePoint);
            if (glyphRank != null) {
               return Optional.of(glyphRank);
            }

            textIndex += Character.charCount(codePoint);
         }

         return Optional.empty();
      }, Style.EMPTY).orElse(null);
   }
// https://t.me/pasterplace

   private static Cerberus.PlayerRank getStyleRank(Style style) {
      StyleSpriteSource styleSpriteSource = style.getFont();
      if (styleSpriteSource instanceof Font) {
         Font font = (Font)styleSpriteSource;
         return RANKS_BY_GROUP_ID.get(font.id().toString());
      } else {
         return null;
      }
   }
// https://t.me/pasterplace

   static {
      registerCodePointRank(42240, "PLAYER", 7895160);
      registerCodePointRank(42244, "HERO", 6582779);
      registerCodePointRank(42248, "TITAN", 14075946);
      registerCodePointRank(42258, "AVENGER", 6667576);
      registerCodePointRank(42262, "OVERLORD", 4233174);
      registerCodePointRank(42272, "MAGISTER", 13271612);
      registerCodePointRank(42276, "IMPERATOR", 13253692);
      registerCodePointRank(42280, "DRAGON", 16069614);
      registerCodePointRank(42336, "D.HELPER", 14075946);
      registerCodePointRank(42290, "BULL", 7950794);
      registerCodePointRank(42294, "TIGER", 13271612);
      registerCodePointRank(42308, "DRACULA", 13253692);
      registerCodePointRank(42326, "BUNNY", 4473154);
      registerCodePointRank(42312, "COBRA", 8377942);
      registerCodePointRank(42304, "HYDRA", 6060039);
      registerCodePointRank(42322, "RABBIT", 7895160);
      registerCodePointRank(42249, "HELPER", 14075946);
      registerCodePointRank(42259, "ML.MODER", 6582779);
      registerCodePointRank(42263, "MODER", 6582779);
      registerCodePointRank(42273, "MODER+", 7950794);
      registerCodePointRank(42277, "ST.MODER", 6582779);
      registerCodePointRank(42281, "GL.MODER", 7950794);
      registerCodePointRank(42291, "ML.ADMIN", 4233174);
      registerCodePointRank(42295, "ADMIN", 13253692);
      registerCodePointRank(42241, "MEDIA", 7950794);
      registerCodePointRank(42245, "YT", 16777215);
      registerGroupRank("hydra", "Гидра", 9498256);
      registerGroupRank("cerberus", "Цербер", 65280);
      registerGroupRank("triton", "Тритон", 11393254);
      registerGroupRank("phoenix", "Феникс", 12255232);
      registerGroupRank("pandar", "Пандар", 16711680);
      registerGroupRank("heat", "Жара", 16729344);
      registerGroupRank("cold", "Холод", 8900346);
      registerGroupRank("kronos", "Кронос", 9109643);
      registerGroupRank("summer", "Лето", 16766720);
      registerGroupRank("winter", "Зима", 15792383);
      registerGroupRank("phobos", "Фобос", 16753920);
      registerGroupRank("ares", "Арес", 16766720);
      registerGroupRank("aristocrat", "Аристократ", 6591981);
      registerGroupRank("youtuber", "Ютубер", 16711680);
      registerGroupRank("helper", "Хелпер", 12092939);
      registerGroupRank("shelper", "Ст Хелпер", 16766720);
      registerGroupRank("moder", "Модер", 255);
      registerGroupRank("smoder", "Ст Модер", 4286945);
      registerGroupRank("admin", "Админ", 16711680);
      registerGroupRank("default", "Игрок", 16777215);
   }
// https://t.me/pasterplace

   record PlayerRank(String rankName, int colorArgb) {
// https://t.me/pasterplace

      PlayerRank(String rankName, int colorArgb) {
         this.rankName = rankName;
         this.colorArgb = colorArgb;
      }
   }
}

