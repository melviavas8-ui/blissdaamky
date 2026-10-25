package dev.daamky;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import net.minecraft.client.MinecraftClient;

/**
 * Диагностика зависаний. Каждый кадр рендера вызывает beat(). Если кадров нет дольше 4 секунд,
 * фоновый поток пишет стеки потоков в консоль (попадёт в latest.log) и в файл daamky_freeze.log
 * в папке игры. По стеку Render thread видно, где именно застряла игра.
 */
public final class FreezeWatchdog {
    private static final long FREEZE_THRESHOLD_MILLIS = 4000L;
    private static final long REPEAT_DUMP_MILLIS = 6000L;
    private static final int MAX_FRAMES = 80;
    private static volatile long lastBeatMillis;
    private static volatile boolean started;

    private FreezeWatchdog() {
    }

    public static void beat() {
        lastBeatMillis = System.currentTimeMillis();
        if (!started) {
            synchronized (FreezeWatchdog.class) {
                if (!started) {
                    started = true;
                    Thread watchdogThread = new Thread(FreezeWatchdog::watch, "Daamky-Watchdog");
                    watchdogThread.setDaemon(true);
                    watchdogThread.start();
                }
            }
        }
    }

    private static void watch() {
        long lastDumpMillis = 0L;
        while (true) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException interrupted) {
                return;
            }

            long now = System.currentTimeMillis();
            long stalledMillis = now - lastBeatMillis;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null) {
                continue;
            }

            if (stalledMillis >= FREEZE_THRESHOLD_MILLIS && now - lastDumpMillis >= REPEAT_DUMP_MILLIS) {
                lastDumpMillis = now;
                dump(client, stalledMillis);
            }
        }
    }

    private static void dump(MinecraftClient client, long stalledMillis) {
        StringBuilder report = new StringBuilder();
        report.append("[DAAMKY-WATCHDOG] no frames for ").append(stalledMillis).append(" ms at ").append(new java.util.Date()).append('\n');
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            Thread thread = entry.getKey();
            StackTraceElement[] frames = entry.getValue();
            String name = thread.getName();
            boolean important = name.equals("Render thread") || name.equals("Server thread");
            boolean suspicious = false;
            if (!important && thread.getState() == Thread.State.RUNNABLE) {
                for (StackTraceElement frame : frames) {
                    String className = frame.getClassName();
                    if (className.startsWith("dev.daamky") || className.contains("baritone")) {
                        suspicious = true;
                        break;
                    }
                }
            }

            if (!important && !suspicious) {
                continue;
            }

            report.append("--- ").append(name).append(" (").append(thread.getState()).append(")\n");
            int count = Math.min(frames.length, MAX_FRAMES);
            for (int index = 0; index < count; index++) {
                report.append("    at ").append(frames[index]).append('\n');
            }
        }

        String text = report.toString();
        System.err.println(text);
        try (PrintWriter writer = new PrintWriter(new FileWriter(new File(client.runDirectory, "daamky_freeze.log"), true))) {
            writer.println(text);
        } catch (IOException ignored) {
        }
    }
}
