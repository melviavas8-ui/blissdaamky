package dev.daamky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;

public final class HandsCapture {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static SimpleFramebuffer captureFramebuffer;
    private static boolean captureEnabled;
// https://t.me/pasterplace

    private HandsCapture() {
    }
// https://t.me/pasterplace

    public static void initialize() {
        TrailSampler.initialize();
    }
// https://t.me/pasterplace

    public static boolean isCaptureAvailable() {
        return captureEnabled && captureFramebuffer != null;
    }
// https://t.me/pasterplace

    public static Framebuffer getFramebuffer() {
        return captureFramebuffer;
    }
// https://t.me/pasterplace

    public static GpuTextureView getColorAttachmentView() {
        return captureFramebuffer.getColorAttachmentView();
    }
// https://t.me/pasterplace

    public static GpuTextureView getDepthAttachmentView() {
        return captureFramebuffer.getDepthAttachmentView();
    }
// https://t.me/pasterplace

    public static void prepareFramebuffer() {

        Hands hands = Hands.getInstance();
        MinecraftClient client = MinecraftClient.getInstance();
        if (hands != null && hands.isEffectActive() && client.player != null && client.world != null && client.gameRenderer != null) {
            Framebuffer framebuffer = client.getFramebuffer();
            int textureWidth = Math.max(1, framebuffer.textureWidth);
            int textureHeight = Math.max(1, framebuffer.textureHeight);
            if (captureFramebuffer == null) {
                captureFramebuffer = new SimpleFramebuffer("hands-capture", textureWidth, textureHeight, true);
            } else if (captureFramebuffer.textureWidth != textureWidth || captureFramebuffer.textureHeight != textureHeight) {
                captureFramebuffer.resize(textureWidth, textureHeight);
            }

            RenderSystem.getDevice()
            .createCommandEncoder()
            .clearColorAndDepthTextures(captureFramebuffer.getColorAttachment(), 0, captureFramebuffer.getDepthAttachment(), 1.0);
// https://t.me/pasterplace

        }
    }
// https://t.me/pasterplace

    public static void setOutputOverride(boolean captureEnabled) {
        if (captureEnabled && captureFramebuffer != null) {
            RenderSystem.outputColorTextureOverride = captureFramebuffer.getColorAttachmentView();
            RenderSystem.outputDepthTextureOverride = captureFramebuffer.getDepthAttachmentView();
        } else {
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
        }
    }
// https://t.me/pasterplace

    public static void renderCapturedTrails() {
        if (captureEnabled) {
            captureEnabled = false;
            Hands hands = Hands.getInstance();
            MinecraftClient client = MinecraftClient.getInstance();
            if (hands != null && hands.isEnabled() && captureFramebuffer != null && client.gameRenderer != null) {
                Framebuffer framebuffer = client.getFramebuffer();
                if (framebuffer != null) {
                    TrailSampler.INSTANCE.render(captureFramebuffer, framebuffer, hands);
                }
            }
        }
    }
// https://t.me/pasterplace

    public static void destroyFramebuffer() {

        if (captureFramebuffer != null) {
            captureFramebuffer.delete();
            captureFramebuffer = null;
        }
    }
}

