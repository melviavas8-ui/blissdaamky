package dev.daamky;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public final class OutgoingPacketBuffer {
    private static final Deque<Packet<?>> queuedPackets = new ArrayDeque<>();
    private static boolean bufferingEnabled;
    private static boolean bypassingPackets;
    private static boolean flushingPackets;

    private OutgoingPacketBuffer() {
    }

    public static void withBypass(Runnable runnable) {
        boolean wasBypassingPackets = bypassingPackets;
        bypassingPackets = true;
        try {
            runnable.run();
        } finally {
            bypassingPackets = wasBypassingPackets;

        }
    }

    public static void startBuffering() {
        bufferingEnabled = true;
    }

    public static boolean queuePacketIfBuffering(Packet<?> packet) {
        if (bufferingEnabled && !bypassingPackets && !flushingPackets && isBufferablePacket(packet)) {
            queuedPackets.add(packet);
            return true;
        } else {
            return false;
        }
    }

    public static void flush() {
        bufferingEnabled = false;
        if (!queuedPackets.isEmpty()) {
            flushingPackets = true;
            try {
                while (!queuedPackets.isEmpty()) {
                    PacketSender.l1I1IlllIlI(queuedPackets.poll());
                }
            } finally {
                flushingPackets = false;
            }
        }
    }

    public static void clear() {
        queuedPackets.clear();
        bufferingEnabled = false;
    }

    private static boolean isBufferablePacket(Packet<?> packet) {
        return packet instanceof PlayerInteractEntityC2SPacket
        || packet instanceof PlayerInteractItemC2SPacket
        || packet instanceof PlayerInteractBlockC2SPacket
        || packet instanceof PlayerActionC2SPacket
        || packet instanceof ClientCommandC2SPacket
        || packet instanceof UpdateSelectedSlotC2SPacket
        || packet instanceof ClickSlotC2SPacket
        || packet instanceof CloseHandledScreenC2SPacket;
    }
}

