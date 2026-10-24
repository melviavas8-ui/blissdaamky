package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventKey;
import java.util.Locale;
import net.minecraft.client.util.InputUtil.Type;

public final class KeybindManager {
   public static final int UNBOUND_KEY = 0;
   private static final KeybindManager INSTANCE = new KeybindManager();

   private KeybindManager() {
   }

   public static KeybindManager getInstance() {
      return INSTANCE;
   }

   public static int encodeMouseButton(int mouseButtonCode) {
      return -(mouseButtonCode + 1);
   }

   public static int encodeKey(int keyCode, boolean mouseInput) {
      return mouseInput ? encodeMouseButton(keyCode) : keyCode;
   }

   public static boolean isMouseButton(int encodedKey) {
      return encodedKey < 0;
   }

   public static String getDisplayName(int keyCode) {
      if (keyCode == 0) {
         return "";
      }

      if (isMouseButton(keyCode)) {
         return "M" + -keyCode;
      }

      String localizedKeyName = Type.KEYSYM.createFromCode(keyCode).getLocalizedText().getString();
      return localizedKeyName.length() > 6 ? localizedKeyName.substring(0, 6).toUpperCase(Locale.ROOT) : localizedKeyName.toUpperCase(Locale.ROOT);
   }

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

