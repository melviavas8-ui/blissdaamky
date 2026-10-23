package dev.daamky;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.ShapeIndexBuffer;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.util.BufferAllocator;
import org.jspecify.annotations.Nullable;

public final class DynamicTransforms {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final List<DynamicTransforms.DrawSubmission> pendingDrawSubmissions = new ArrayList<>();
    private static final BufferAllocator bufferAllocator = new BufferAllocator(65536);
    private static final List<GpuBuffer> vertexBuffers = new ArrayList<>();
    private static int nextBufferIndex;
// https://t.me/pasterplace

    private DynamicTransforms() {
    }
// https://t.me/pasterplace

    public static BufferBuilder createBufferBuilder(DrawMode drawMode, VertexFormat vertexFormat) {
        return new BufferBuilder(bufferAllocator, drawMode, vertexFormat);
    }
// https://t.me/pasterplace

    public static void submitDraw(BufferBuilder bufferBuilder, DrawMode drawMode, RenderPipeline renderPipeline, @Nullable GpuTextureView gpuTextureView) {
        submitDraw(bufferBuilder, drawMode, renderPipeline, gpuTextureView, false);
    }
// https://t.me/pasterplace

    public static void submitDraw(
    BufferBuilder bufferBuilder, DrawMode drawMode, RenderPipeline renderPipeline, @Nullable GpuTextureView gpuTextureView, boolean nearestFiltering
    ) {
        BuiltBuffer builtBuffer = bufferBuilder.endNullable();

        label73: {
            label74: {
                try {
                    if (builtBuffer == null) {
                        break label73;
                    }
// https://t.me/pasterplace

                    int indexCount = builtBuffer.getDrawParameters().indexCount();
                    if (indexCount <= 0) {
                        break label74;
                    }

                    ByteBuffer byteBuffer = builtBuffer.getBuffer();
                    int bufferSize = byteBuffer.remaining();
                    GpuBuffer gpuBuffer = nextBufferIndex < vertexBuffers.size() ? vertexBuffers.get(nextBufferIndex) : null;
                    if (gpuBuffer == null || gpuBuffer.isClosed() || gpuBuffer.size() < bufferSize) {
                        if (gpuBuffer != null) {
                            gpuBuffer.close();
                        }

                        gpuBuffer = RenderSystem.getDevice()
                        .createBuffer(() -> "Daamky immediate geometry", 40, Math.max(16384, Integer.highestOneBit(bufferSize - 1) << 1));
                        if (nextBufferIndex < vertexBuffers.size()) {
                            vertexBuffers.set(nextBufferIndex, gpuBuffer);
                        } else {
                            vertexBuffers.add(gpuBuffer);
                        }
                    }

                    nextBufferIndex++;
                    GpuBufferSlice gpuBufferSlice = gpuBuffer.slice(0L, bufferSize);
                    RenderSystem.getDevice().createCommandEncoder().writeToBuffer(gpuBufferSlice, byteBuffer);
                    RenderSystem.getSequentialBuffer(drawMode).getIndexBuffer(indexCount);
                    pendingDrawSubmissions.add(new DynamicTransforms.DrawSubmission(gpuBufferSlice, indexCount, drawMode, renderPipeline, gpuTextureView, nearestFiltering));
                } catch (Throwable submissionFailure) {
                    if (builtBuffer != null) {
                        try {
                            builtBuffer.close();
                        } catch (Throwable builtBufferCloseFailure) {
                            submissionFailure.addSuppressed(builtBufferCloseFailure);
                        }
                    }

                    throw submissionFailure;
                }

                if (builtBuffer != null) {
                    builtBuffer.close();
                }

                return;
            }

            if (builtBuffer != null) {
                builtBuffer.close();
            }

            return;
        }

        if (builtBuffer != null) {
            builtBuffer.close();
        }
    }
// https://t.me/pasterplace

    static boolean hasPendingDrawSubmissions() {
        return !pendingDrawSubmissions.isEmpty();
    }
// https://t.me/pasterplace

    static void renderSubmissions(RenderPass renderPass, GpuBufferSlice gpuBufferSlice) {
        for (Object drawSubmission : pendingDrawSubmissions) {
            renderPass.setPipeline(((DynamicTransforms.DrawSubmission)drawSubmission).pipeline());
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            if (((DynamicTransforms.DrawSubmission)drawSubmission).textureView() != null) {
                renderPass.bindTexture(
                "Sampler0",
                ((DynamicTransforms.DrawSubmission)drawSubmission).textureView(),
                RenderSystem.getSamplerCache().get(((DynamicTransforms.DrawSubmission)drawSubmission).nearestFiltering() ? FilterMode.NEAREST : FilterMode.LINEAR)
                );
            }

            renderPass.setVertexBuffer(0, ((DynamicTransforms.DrawSubmission)drawSubmission).vertexBufferSlice().buffer());
            ShapeIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(((DynamicTransforms.DrawSubmission)drawSubmission).drawMode());
            renderPass.setIndexBuffer(sequentialBuffer.getIndexBuffer(((DynamicTransforms.DrawSubmission)drawSubmission).indexCount()), sequentialBuffer.getIndexType());
            renderPass.drawIndexed(0, 0, ((DynamicTransforms.DrawSubmission)drawSubmission).indexCount(), 1);
        }
    }
// https://t.me/pasterplace

    static void clearSubmissions() {

        pendingDrawSubmissions.clear();
        bufferAllocator.clear();
    }
// https://t.me/pasterplace

    private record DrawSubmission(GpuBufferSlice vertexBufferSlice, int indexCount, DrawMode drawMode, RenderPipeline pipeline, @Nullable GpuTextureView textureView, boolean nearestFiltering) {

        private DrawSubmission(GpuBufferSlice vertexBufferSlice, int indexCount, DrawMode drawMode, RenderPipeline pipeline, @Nullable GpuTextureView textureView, boolean nearestFiltering) {
            this.vertexBufferSlice = vertexBufferSlice;
            this.indexCount = indexCount;
            this.drawMode = drawMode;
            this.pipeline = pipeline;
            this.textureView = textureView;
            this.nearestFiltering = nearestFiltering;
        }
    }
}

