package dev.daamky;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

public final class BotPacketLog {
    private static final String p70856863 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final long II1Il11l111II11IIl = 25000L;
    private static final ConcurrentLinkedQueue<String> l1I1IlllIlI = new ConcurrentLinkedQueue<>();
    private static volatile long IllI1l1l1;
    private static volatile boolean l1lll11l1l;
// https://t.me/pasterplace

    private BotPacketLog() {
    }
// https://t.me/pasterplace

    public static void II1Il11l111II11IIl(String packetDirection, Object sessionContext, Packet<?> packet) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            String simpleName = packet.getClass().getSimpleName();
// https://t.me/pasterplace

            long logStartTimeMillis = IllI1l1l1;
            if (logStartTimeMillis == 0L || currentTimeMillis - logStartTimeMillis > 25000L) {
                return;
            }

            l1I1IlllIlI.add(String.format("%6d %s %-30s %s", currentTimeMillis - logStartTimeMillis, packetDirection, sessionContext == null ? "null" : sessionContext.getClass().getSimpleName(), simpleName));
            II1Il11l111II11IIl();
        } catch (Throwable logFailure) {
        }
    }
// https://t.me/pasterplace

    private static void II1Il11l111II11IIl() {
        if (!l1lll11l1l) {
            synchronized (BotPacketLog.class) {
                if (!l1lll11l1l) {
                    l1lll11l1l = true;
                    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
                        Thread thread = new Thread(runnable, "bot-packet-log");
                        thread.setDaemon(true);
                        return thread;
                    });
                    scheduledExecutorService.scheduleWithFixedDelay(BotPacketLog::l1I1IlllIlI, 0L, 400L, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
// https://t.me/pasterplace

    private static void l1I1IlllIlI() {
        if (!l1I1IlllIlI.isEmpty()) {
            ArrayList arrayList = new ArrayList();

            Object queuedLogLine;
            while ((queuedLogLine = l1I1IlllIlI.poll()) != null) {
                arrayList.add(queuedLogLine);
            }

            try {
                Path path = MinecraftClient.getInstance().runDirectory.toPath().resolve("bot-packets.log");
                Files.write(path, arrayList, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Throwable writeFailure) {
            }
        }
    }
}

