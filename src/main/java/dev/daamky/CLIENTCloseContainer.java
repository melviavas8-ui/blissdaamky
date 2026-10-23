package dev.daamky;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

public final class CLIENTCloseContainer {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final Path LOG_FILE = Path.of("daamky", "container-debug.txt");
// https://t.me/pasterplace

   private CLIENTCloseContainer() {
   }
// https://t.me/pasterplace

   public static void logOutgoingPacket(Packet<?> packet, String direction) {
      if (packet instanceof ClickSlotC2SPacket clickSlotPacket) {
         appendLogEntry(
            direction
               + " CLICK id="
                + clickSlotPacket.syncId()
               + " state="
                + clickSlotPacket.revision()
               + " slot="
                + clickSlotPacket.slot()
               + " btn="
                + clickSlotPacket.button()
               + " type="
                + clickSlotPacket.actionType()
               + " menuState="
               + getCurrentScreenHandlerState(),
            true
         );
      } else if (packet instanceof CloseHandledScreenC2SPacket closeHandledScreenPacket) {
         appendLogEntry(direction + " CLOSE id=" + closeHandledScreenPacket.getSyncId(), true);
      } else if (packet instanceof UpdateSelectedSlotC2SPacket selectedSlotPacket) {
         appendLogEntry(direction + " CARRIED slot=" + selectedSlotPacket.getSelectedSlot(), false);
      }
   }
// https://t.me/pasterplace

   public static void logIncomingPacket(Packet<?> packet) {
      if (packet instanceof CloseScreenS2CPacket closeScreenPacket) {
         appendLogEntry("RECV CLOSE id=" + closeScreenPacket.getSyncId(), false);
      } else if (packet instanceof OpenScreenS2CPacket openScreenPacket) {
         appendLogEntry("RECV OPEN id=" + openScreenPacket.getSyncId() + " title=" + openScreenPacket.getName().getString(), false);
      } else if (packet instanceof InventoryS2CPacket inventoryPacket) {
          appendLogEntry("RECV CONTENT id=" + inventoryPacket.syncId() + " state=" + inventoryPacket.revision(), false);
      } else if (packet instanceof ScreenHandlerSlotUpdateS2CPacket slotUpdatePacket) {
         appendLogEntry("RECV SLOT id=" + slotUpdatePacket.getSyncId() + " state=" + slotUpdatePacket.getRevision() + " slot=" + slotUpdatePacket.getSlot(), false);
      }
   }
// https://t.me/pasterplace

   public static void logCloseContainer() {
      appendLogEntry("CLIENT closeContainer", true);
   }
// https://t.me/pasterplace

   private static String getCurrentScreenHandlerState() {
      MinecraftClient client = MinecraftClient.getInstance();
      return client.player == null ? "-" : client.player.currentScreenHandler.syncId + "/" + client.player.currentScreenHandler.getRevision();
   }
// https://t.me/pasterplace

   private static void appendLogEntry(String logMessage, boolean includeStackTrace) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(LocalTime.now()).append(' ').append(Thread.currentThread().getName()).append(' ').append(logMessage).append('\n');
      if (includeStackTrace) {
         StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

         for (int stackTraceIndex = 2; stackTraceIndex < Math.min(stackTrace.length, 22); stackTraceIndex++) {
            stringBuilder.append("    at ").append(stackTrace[stackTraceIndex]).append('\n');
         }
      }
// https://t.me/pasterplace

      try {
         Files.writeString(LOG_FILE, stringBuilder.toString(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      } catch (IOException writeFailure) {
      }
   }
}

