package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ClientChatLogger {
   private ClientChatLogger() {
   }

   public static void II1Il11l111II11IIl(String message) {
      II1Il11l111II11IIl(
         Text.literal("[Daamky] ").formatted(Formatting.LIGHT_PURPLE).append(Text.literal(message).formatted(Formatting.GRAY))
      );
   }

   public static void l1I1IlllIlI(String message) {
      II1Il11l111II11IIl(
         Text.literal("[Daamky] ").formatted(Formatting.LIGHT_PURPLE).append(Text.literal(message).formatted(Formatting.RED))
      );
   }

   private static void II1Il11l111II11IIl(Text message) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.inGameHud != null && client.player != null) {
         client.inGameHud.getChatHud().addMessage(message);
      }
   }
}

