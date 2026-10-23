package dev.daamky;

import java.util.ArrayDeque;
import java.util.Queue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public final class UseItemRotationQueue {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final Queue<PlayerInteractItemC2SPacket> queuedItemUsePackets = new ArrayDeque<>();
    private static boolean flushingQueue;
    private static float lastYaw = Float.NaN;
    private static float lastPitch = Float.NaN;
// https://t.me/pasterplace

    private UseItemRotationQueue() {
    }
// https://t.me/pasterplace

    public static boolean hasQueuedPackets() {
        return !queuedItemUsePackets.isEmpty();
    }
// https://t.me/pasterplace

    public static boolean queueIfItemUsePacket(Packet<?> packet) {
        if (flushingQueue || MinecraftClient.getInstance().player == null) {
            return false;
        } else if (packet instanceof PlayerInteractBlockC2SPacket || packet instanceof PlayerActionC2SPacket playerActionPacket && playerActionPacket.getSequence() > 0) {
            flushUsingCurrentRotation();
            return false;
        } else if (packet instanceof PlayerInteractItemC2SPacket interactItemPacket) {
            queuedItemUsePackets.add(interactItemPacket);
            return true;
        } else {
            return false;
        }
    }
// https://t.me/pasterplace

    public static void flushQueuedPackets() {
        flushUsingCurrentRotation();
    }
// https://t.me/pasterplace

    private static void flushUsingCurrentRotation() {
        if (!queuedItemUsePackets.isEmpty()) {
            float yaw = Float.isNaN(lastYaw) ? MinecraftClient.getInstance().player.getYaw() : lastYaw;
            float pitch = Float.isNaN(lastPitch) ? MinecraftClient.getInstance().player.getPitch() : lastPitch;
            sendQueuedPacketsWithRotation(yaw, pitch);
        }
    }
// https://t.me/pasterplace

    public static void onPlayerMovePacket(PlayerMoveC2SPacket playerMoveC2SPacket) {
        float yaw;
        float pitch;
        if (playerMoveC2SPacket.changesLook()) {
            yaw = playerMoveC2SPacket.getYaw(0.0F);
            pitch = playerMoveC2SPacket.getPitch(0.0F);
            lastYaw = yaw;
            lastPitch = pitch;
        } else if (!Float.isNaN(lastYaw)) {
            yaw = lastYaw;
            pitch = lastPitch;
        } else {
            yaw = MinecraftClient.getInstance().player.getYaw();
            pitch = MinecraftClient.getInstance().player.getPitch();
        }
// https://t.me/pasterplace

        if (!queuedItemUsePackets.isEmpty()) {
            sendQueuedPacketsWithRotation(yaw, pitch);
        }
    }
// https://t.me/pasterplace

    private static void sendQueuedPacketsWithRotation(float yaw, float pitch) {

        try {
            while (!queuedItemUsePackets.isEmpty()) {
                PlayerInteractItemC2SPacket queuedPacket = queuedItemUsePackets.poll();
                PacketSender.l1I1IlllIlI(new PlayerInteractItemC2SPacket(queuedPacket.getHand(), queuedPacket.getSequence(), yaw, pitch));
            }
        } finally {

        }
    }
// https://t.me/pasterplace

    public static void clearQueue() {
        queuedItemUsePackets.clear();

    }
}

