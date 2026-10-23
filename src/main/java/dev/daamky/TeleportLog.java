package dev.daamky;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;

public final class TeleportLog {
    private static final String p96f6961a = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final DateTimeFormatter II1Il11l111II11IIl = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
// https://t.me/pasterplace

   private TeleportLog() {
   }
// https://t.me/pasterplace

   public static void II1Il11l111II11IIl(String formatString, Object... objectArray) {
      String formattedLine = "[" + LocalTime.now().format(II1Il11l111II11IIl) + "] " + String.format(Locale.ROOT, formatString, objectArray) + System.lineSeparator();
// https://t.me/pasterplace

      try {
         Path path = MinecraftClient.getInstance().runDirectory.toPath().resolve("daamky").resolve("tp-log.txt");
         Files.createDirectories(path.getParent());
         Files.writeString(path, formattedLine, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      } catch (IOException ignoredIOException) {
      }
   }
}

