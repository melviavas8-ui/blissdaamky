package dev.daamky;

import net.minecraft.client.MinecraftClient;

public final class NotificationScheduler {
   private NotificationScheduler() {
   }

   public static void II1Il11l111II11IIl(String title, String message, long durationMillis) {
      II1Il11l111II11IIl(title, message, null, durationMillis);
   }

   public static void II1Il11l111II11IIl(String title, String message, RgbaColor l1I1IlllIlI4, long durationMillis) {
      float durationSeconds = (float)Math.max(500L, durationMillis) / 1000.0F;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null) {
         client.execute(() -> Notifications.showNotification(title, message, l1I1IlllIlI4, durationSeconds));
      }
   }
}

