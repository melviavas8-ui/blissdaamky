package dev.daamky;

import net.minecraft.client.MinecraftClient;

public final class NotificationScheduler {
    private static final String p161bb2e2 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private NotificationScheduler() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(String title, String message, long durationMillis) {
      II1Il11l111II11IIl(title, message, null, durationMillis);
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(String title, String message, RgbaColor l1I1IlllIlI4, long durationMillis) {
      float durationSeconds = (float)Math.max(500L, durationMillis) / 1000.0F;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null) {
         client.execute(() -> Notifications.showNotification(title, message, l1I1IlllIlI4, durationSeconds));
      }
   }
}

