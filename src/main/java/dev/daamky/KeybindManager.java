package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import java.util.Locale;
import net.minecraft.client.util.InputUtil.Type;

public final class KeybindManager {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public static final int UNBOUND_KEY = 0;
   private static final KeybindManager INSTANCE = new KeybindManager();
// https://t.me/pasterplace

   private KeybindManager() {
   }
// https://t.me/pasterplace

   public static KeybindManager getInstance() {
      return INSTANCE;
   }
// https://t.me/pasterplace

   public static int encodeMouseButton(int mouseButtonCode) {
      return -(mouseButtonCode + 1);
   }
// https://t.me/pasterplace

   public static int encodeKey(int keyCode, boolean mouseInput) {
      return mouseInput ? encodeMouseButton(keyCode) : keyCode;
   }
// https://t.me/pasterplace

   public static boolean isMouseButton(int encodedKey) {
      return encodedKey < 0;
   }
// https://t.me/pasterplace

   public static String getDisplayName(int keyCode) {
      if (keyCode == 0) {
         return "";
      }
// https://t.me/pasterplace

      if (isMouseButton(keyCode)) {
         return "M" + -keyCode;
      }

      String localizedKeyName = Type.KEYSYM.createFromCode(keyCode).getLocalizedText().getString();
      return localizedKeyName.length() > 6 ? localizedKeyName.substring(0, 6).toUpperCase(Locale.ROOT) : localizedKeyName.toUpperCase(Locale.ROOT);
   }
// https://t.me/pasterplace

   @EventTarget
   public void onKey(EventKey eventKey) {
      int encodedKey = encodeKey(eventKey.key(), eventKey.isMouse());
      if (encodedKey != 0) {
         for (Object module : ModuleRegistry.getInstance().getModules()) {
            if (((ModuleStructure)module).getKeybind() == encodedKey) {
               ((ModuleStructure)module).toggle();
            }
         }
      }
   }
}

