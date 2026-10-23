package dev.daamky;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.MinecraftClient;

public final class BotCaptchaLog {
    private static final String p88ad7427 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final DateTimeFormatter II1Il11l111II11IIl = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
   private static final ConcurrentLinkedQueue<String> l1I1IlllIlI = new ConcurrentLinkedQueue<>();
   private static volatile boolean IllI1l1l1;
// https://t.me/pasterplace

   private BotCaptchaLog() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(String captchaMessage) {
      l1I1IlllIlI.add(LocalTime.now().format(II1Il11l111II11IIl) + " " + captchaMessage);
      II1Il11l111II11IIl();
   }
// https://t.me/pasterplace

   public static String l1I1IlllIlI(String message) {
      if (message == null) {
         return "null";
      }

      String sanitizedMessage = message.trim().replace('\n', ' ');
      return sanitizedMessage.length() > 140 ? sanitizedMessage.substring(0, 140) + "…" : sanitizedMessage;
   }
// https://t.me/pasterplace

   private static void II1Il11l111II11IIl() {
      if (!IllI1l1l1) {
         synchronized (BotCaptchaLog.class) {
            if (!IllI1l1l1) {
               IllI1l1l1 = true;
               ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
                  Thread thread = new Thread(runnable, "bot-captcha-log");
                  thread.setDaemon(true);
                  return thread;
               });
               scheduledExecutorService.scheduleWithFixedDelay(BotCaptchaLog::l1I1IlllIlI, 0L, 400L, TimeUnit.MILLISECONDS);
            }
         }
      }
   }
// https://t.me/pasterplace

   private static void l1I1IlllIlI() {
      if (!l1I1IlllIlI.isEmpty()) {
         ArrayList arrayList = new ArrayList();

         Object logLine;
         while ((logLine = l1I1IlllIlI.poll()) != null) {
            arrayList.add(logLine);
         }
// https://t.me/pasterplace

         try {
            Path path = MinecraftClient.getInstance().runDirectory.toPath().resolve("captcha.log");
            Files.write(path, arrayList, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
         } catch (Throwable writeFailure) {
         }
      }
   }
}

