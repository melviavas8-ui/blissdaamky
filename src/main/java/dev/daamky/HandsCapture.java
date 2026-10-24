package dev.daamky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;

public final class HandsCapture {
    private static SimpleFramebuffer captureFramebuffer;
    private static boolean captureEnabled;

    private HandsCapture() {
    }

    public static void initialize() {
        TrailSampler.initialize();
    }

    public static boolean isCaptureAvailable() {
        return captureEnabled && captureFramebuffer != null;
    }

    public static Framebuffer getFramebuffer() {
        return captureFramebuffer;
    }

    public static GpuTextureView getColorAttachmentView() {
        return captureFramebuffer.getColorAttachmentView();
    }

    public static GpuTextureView getDepthAttachmentView() {
        return captureFramebuffer.getDepthAttachmentView();
    }

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

        }
    }

    public static void setOutputOverride(boolean captureEnabled) {
        if (captureEnabled && captureFramebuffer != null) {
            RenderSystem.outputColorTextureOverride = captureFramebuffer.getColorAttachmentView();
            RenderSystem.outputDepthTextureOverride = captureFramebuffer.getDepthAttachmentView();
        } else {
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
        }
    }

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

    public static void destroyFramebuffer() {

        if (captureFramebuffer != null) {
            captureFramebuffer.delete();
            captureFramebuffer = null;
        }
    }
}

