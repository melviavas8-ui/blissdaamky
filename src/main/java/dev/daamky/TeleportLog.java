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
   private static final DateTimeFormatter II1Il11l111II11IIl = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

   private TeleportLog() {
   }

   public static void II1Il11l111II11IIl(String formatString, Object... objectArray) {
      String formattedLine = "[" + LocalTime.now().format(II1Il11l111II11IIl) + "] " + String.format(Locale.ROOT, formatString, objectArray) + System.lineSeparator();

      try {
         Path path = MinecraftClient.getInstance().runDirectory.toPath().resolve("daamky").resolve("tp-log.txt");
         Files.createDirectories(path.getParent());
         Files.writeString(path, formattedLine, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      } catch (IOException ignoredIOException) {
      }
   }
}

