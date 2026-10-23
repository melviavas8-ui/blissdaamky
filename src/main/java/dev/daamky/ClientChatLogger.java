package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ClientChatLogger {
    private static final String pd033340 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private ClientChatLogger() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(String message) {
      II1Il11l111II11IIl(
         Text.literal("[Daamky] ").formatted(Formatting.LIGHT_PURPLE).append(Text.literal(message).formatted(Formatting.GRAY))
      );
   }
// https://t.me/pasterplace

   public static void l1I1IlllIlI(String message) {
      II1Il11l111II11IIl(
         Text.literal("[Daamky] ").formatted(Formatting.LIGHT_PURPLE).append(Text.literal(message).formatted(Formatting.RED))
      );
   }
// https://t.me/pasterplace

   private static void II1Il11l111II11IIl(Text message) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.inGameHud != null && client.player != null) {
         client.inGameHud.getChatHud().addMessage(message);
      }
   }
}

