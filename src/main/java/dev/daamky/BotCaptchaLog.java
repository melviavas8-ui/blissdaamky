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
   private static final DateTimeFormatter II1Il11l111II11IIl = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
   private static final ConcurrentLinkedQueue<String> l1I1IlllIlI = new ConcurrentLinkedQueue<>();
   private static volatile boolean IllI1l1l1;

   private BotCaptchaLog() {
   }

   public static void II1Il11l111II11IIl(String captchaMessage) {
      l1I1IlllIlI.add(LocalTime.now().format(II1Il11l111II11IIl) + " " + captchaMessage);
      II1Il11l111II11IIl();
   }

   public static String l1I1IlllIlI(String message) {
      if (message == null) {
         return "null";
      }

      String sanitizedMessage = message.trim().replace('\n', ' ');
      return sanitizedMessage.length() > 140 ? sanitizedMessage.substring(0, 140) + "…" : sanitizedMessage;
   }

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

   private static void l1I1IlllIlI() {
      if (!l1I1IlllIlI.isEmpty()) {
         ArrayList arrayList = new ArrayList();

         Object logLine;
         while ((logLine = l1I1IlllIlI.poll()) != null) {
            arrayList.add(logLine);
         }

         try {
            Path path = MinecraftClient.getInstance().runDirectory.toPath().resolve("captcha.log");
            Files.write(path, arrayList, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
         } catch (Throwable writeFailure) {
         }
      }
   }
}

