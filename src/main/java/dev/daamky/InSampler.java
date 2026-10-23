package dev.daamky;

import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;

public final class InSampler {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final int BLUR_READY_FRAME_COUNT = 5;
    private static final Down downsampleFramebuffers = new Down();
    private static final Set<KawaseGaussianBlurPass> pendingBlurPasses = new LinkedHashSet<>();
    private static final Set<KawaseGaussianBlurPass> completedBlurPasses = new HashSet<>();
    private static int blurReadyFrameCount = 5;
// https://t.me/pasterplace

    private InSampler() {
    }
// https://t.me/pasterplace

    public static void enqueueBlurPass(KawaseGaussianBlurPass iI1Il11l111II11IIl8) {
        pendingBlurPasses.add(iI1Il11l111II11IIl8);
    }
// https://t.me/pasterplace

    public static boolean isBlurPassComplete(KawaseGaussianBlurPass iI1Il11l111II11IIl8) {
        return isBlurReady() && completedBlurPasses.contains(iI1Il11l111II11IIl8);
    }
// https://t.me/pasterplace

    public static boolean isBlurReady() {
        return blurReadyFrameCount >= 5;
    }
// https://t.me/pasterplace

    public static void clearCompletedBlurPasses() {
        completedBlurPasses.clear();

    }
// https://t.me/pasterplace

    public static void clearPendingBlurPasses() {
        pendingBlurPasses.clear();
    }
// https://t.me/pasterplace

    public static GpuTextureView getBlurTexture(KawaseGaussianBlurPass iI1Il11l111II11IIl8) {
        Framebuffer framebuffer = getMainFramebuffer();
        if (framebuffer != null && framebuffer.textureWidth > 0 && framebuffer.textureHeight > 0) {
            clearCompletedPassesAfterResize(framebuffer);
            SimpleFramebuffer simpleFramebuffer = downsampleFramebuffers.getPassFramebuffer(iI1Il11l111II11IIl8);
            return simpleFramebuffer != null ? simpleFramebuffer.getColorAttachmentView() : null;
        } else {
            return null;
        }
    }
// https://t.me/pasterplace

    public static void processPendingBlurPasses() {
        advanceBlurReadiness();
        if (!pendingBlurPasses.isEmpty()) {
            Framebuffer framebuffer = getMainFramebuffer();
            if (framebuffer != null && framebuffer.textureWidth > 0 && framebuffer.textureHeight > 0) {
                GpuTextureView gpuTextureView = framebuffer.getColorAttachmentView();
                if (gpuTextureView == null) {
                    pendingBlurPasses.clear();
                } else {
                    clearCompletedPassesAfterResize(framebuffer);
                    int highestRequiredPass = pendingBlurPasses.stream()
                    .mapToInt(iI1Il11l111II11IIl8 -> iI1Il11l111II11IIl8.mode().l1I1IlllIlI(iI1Il11l111II11IIl8.passIndex()))
                    .max()
                    .orElse(0);
                    boolean passesReady = true;
// https://t.me/pasterplace

                    for (int passIndex = 1; passIndex <= highestRequiredPass; passIndex++) {
                        SimpleFramebuffer simpleFramebuffer = downsampleFramebuffers.getFramebuffer(passIndex);
                        if (simpleFramebuffer == null || !renderBlurPass(Downsample.l1I1IlllIlI, gpuTextureView, simpleFramebuffer)) {
                            passesReady = false;
                            break;
                        }

                        gpuTextureView = simpleFramebuffer.getColorAttachmentView();
                        if (gpuTextureView == null) {
                            passesReady = false;
                            break;
                        }
                    }

                    for (Object blurPass : pendingBlurPasses) {
                        if (passesReady && executeBlurPass((KawaseGaussianBlurPass)blurPass)) {
                            completedBlurPasses.add((KawaseGaussianBlurPass)blurPass);
                        } else {
                            completedBlurPasses.remove(blurPass);
                        }
                    }

                    pendingBlurPasses.clear();
                }
            } else {
                pendingBlurPasses.clear();
            }
        }
    }
// https://t.me/pasterplace

    public static void close() {
        pendingBlurPasses.clear();
        completedBlurPasses.clear();
        downsampleFramebuffers.close();
    }
// https://t.me/pasterplace

    private static void advanceBlurReadiness() {
        if (!isBlurReady()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.world != null && client.player != null && !(client.currentScreen instanceof LevelLoadingScreen)) {
                blurReadyFrameCount++;
            }
        }
    }
// https://t.me/pasterplace

    private static boolean executeBlurPass(KawaseGaussianBlurPass iI1Il11l111II11IIl8) {
        return switch (iI1Il11l111II11IIl8.mode()) {
        case KAWASE -> executeKawasePasses(iI1Il11l111II11IIl8);
        case GAUSSIAN -> executeRepeatedBlurPasses(iI1Il11l111II11IIl8, Downsample.l1lll11l1l, Downsample.lIIl11l111lIIl1ll);
        case BOX -> executeRepeatedBlurPasses(iI1Il11l111II11IIl8, Downsample.l1IIl11lI, Downsample.I1lIIII1III1lIlIl1l);
        };
    }
// https://t.me/pasterplace

    private static boolean executeKawasePasses(KawaseGaussianBlurPass iI1Il11l111II11IIl8) {
        SimpleFramebuffer simpleFramebuffer = downsampleFramebuffers.getFramebuffer(iI1Il11l111II11IIl8.passIndex());
        if (simpleFramebuffer == null) {
            return false;
        }

        GpuTextureView gpuTextureView = simpleFramebuffer.getColorAttachmentView();
        if (gpuTextureView == null) {
            return false;
        }

        for (int passIndex = iI1Il11l111II11IIl8.passIndex() - 1; passIndex >= 1; passIndex--) {
            SimpleFramebuffer framebuffer = passIndex == 1 ? downsampleFramebuffers.getPassFramebuffer(iI1Il11l111II11IIl8) : downsampleFramebuffers.getTemporaryFramebuffer(passIndex);
            if (framebuffer == null || !renderBlurPass(Downsample.IllI1l1l1, gpuTextureView, framebuffer)) {
                return false;
            }

            gpuTextureView = framebuffer.getColorAttachmentView();
            if (gpuTextureView == null) {
                return false;
            }
        }

        return true;
    }
// https://t.me/pasterplace

    private static boolean executeRepeatedBlurPasses(KawaseGaussianBlurPass iI1Il11l111II11IIl8, RenderPipeline renderPipeline, RenderPipeline renderPipeline2) {
        int requiredPassCount = iI1Il11l111II11IIl8.mode().II1Il11l111II11IIl(iI1Il11l111II11IIl8.passIndex());
        int repetitions = iI1Il11l111II11IIl8.passIndex() % 2 == 0 ? 2 : 1;
        SimpleFramebuffer simpleFramebuffer = downsampleFramebuffers.getTemporaryFramebuffer(requiredPassCount);
        SimpleFramebuffer simpleFramebuffer2 = downsampleFramebuffers.getPassFramebuffer(iI1Il11l111II11IIl8);
        SimpleFramebuffer simpleFramebuffer3 = downsampleFramebuffers.getFramebuffer(requiredPassCount);
        if (simpleFramebuffer != null && simpleFramebuffer2 != null && simpleFramebuffer3 != null) {
            GpuTextureView gpuTextureView = simpleFramebuffer3.getColorAttachmentView();
            if (gpuTextureView == null) {
                return false;
            }
// https://t.me/pasterplace

            for (int blurIteration = 0; blurIteration < repetitions; blurIteration++) {
                if (!renderBlurPass(renderPipeline, gpuTextureView, simpleFramebuffer)) {
                    return false;
                }

                if (simpleFramebuffer.getColorAttachmentView() == null) {
                    return false;
                }

                if (!renderBlurPass(renderPipeline2, simpleFramebuffer.getColorAttachmentView(), simpleFramebuffer2)) {
                    return false;
                }

                gpuTextureView = simpleFramebuffer2.getColorAttachmentView();
                if (gpuTextureView == null) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }
// https://t.me/pasterplace

    private static boolean renderBlurPass(RenderPipeline renderPipeline, GpuTextureView gpuTextureView, SimpleFramebuffer simpleFramebuffer) {
        if (gpuTextureView != null && simpleFramebuffer != null) {
            GpuTextureView gpuTextureView2 = simpleFramebuffer.getColorAttachmentView();
            if (gpuTextureView2 == null) {
                return false;
            }

            CompiledRenderPipeline compiledRenderPipeline = RenderSystem.getDevice().precompilePipeline(renderPipeline);
            if (compiledRenderPipeline != null && compiledRenderPipeline.isValid()) {
                GpuSampler gpuSampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
                if (gpuSampler != null && RenderSystem.getDevice() != null) {
                    try {
                        RenderPass renderPass = RenderSystem.getDevice()
                        .createCommandEncoder()
                        .createRenderPass(() -> "Blur " + renderPipeline.getLocation().getPath(), gpuTextureView2, OptionalInt.empty());

                        try {
                            renderPass.setPipeline(renderPipeline);
                            RenderSystem.bindDefaultUniforms(renderPass);
                            renderPass.bindTexture("InSampler", gpuTextureView, gpuSampler);
                            renderPass.draw(0, 3);

                        } catch (Throwable renderFailure) {
                            if (renderPass != null) {
                                try {
                                    renderPass.close();
                                } catch (Throwable renderPassCloseFailure) {
                                    renderFailure.addSuppressed(renderPassCloseFailure);
                                }
                            }

                            throw renderFailure;
                        }

                        if (renderPass != null) {
                            renderPass.close();
                        }

                        return true;
                    } catch (Throwable pipelineFailure) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
// https://t.me/pasterplace

    private static void clearCompletedPassesAfterResize(Framebuffer framebuffer) {
        if (downsampleFramebuffers.ensureFramebufferSize(framebuffer)) {
            completedBlurPasses.clear();
        }
    }
// https://t.me/pasterplace

    private static Framebuffer getMainFramebuffer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.gameRenderer != null ? client.getFramebuffer() : null;
    }
}

