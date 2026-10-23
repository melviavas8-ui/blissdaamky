package dev.daamky;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class DaamkyWorldRender {
    private static final String TELEGRAM_CHANNEL_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final BufferedWorldGeometry VISIBLE_LINE_GEOMETRY = new BufferedWorldGeometry(
    DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, WorldRenderPipelines.II1Il11l111II11IIl
    );
    private static final BufferedWorldGeometry THROUGH_WALL_LINE_GEOMETRY = new BufferedWorldGeometry(DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, WorldRenderPipelines.l1I1IlllIlI);
    private static final BufferedWorldGeometry VISIBLE_QUAD_GEOMETRY = new BufferedWorldGeometry(DrawMode.QUADS, VertexFormats.POSITION_COLOR, WorldRenderPipelines.IllI1l1l1);
    private static final BufferedWorldGeometry THROUGH_WALL_QUAD_GEOMETRY = new BufferedWorldGeometry(DrawMode.QUADS, VertexFormats.POSITION_COLOR, WorldRenderPipelines.l1lll11l1l);
    private static final BufferedWorldGeometry VISIBLE_ADDITIVE_QUAD_GEOMETRY = new BufferedWorldGeometry(
    DrawMode.QUADS, VertexFormats.POSITION_COLOR, WorldRenderPipelines.lIIl11l111lIIl1ll
    );
    private static final BufferedWorldGeometry THROUGH_WALL_ADDITIVE_QUAD_GEOMETRY = new BufferedWorldGeometry(DrawMode.QUADS, VertexFormats.POSITION_COLOR, WorldRenderPipelines.l1IIl11lI);
    private static final BufferedWorldGeometry[] WORLD_GEOMETRY_BUFFERS = new BufferedWorldGeometry[]{
        VISIBLE_QUAD_GEOMETRY, VISIBLE_ADDITIVE_QUAD_GEOMETRY, VISIBLE_LINE_GEOMETRY, THROUGH_WALL_QUAD_GEOMETRY, THROUGH_WALL_ADDITIVE_QUAD_GEOMETRY, THROUGH_WALL_LINE_GEOMETRY
    };
    private static final BufferedWorldGeometry VISIBLE_BLOOM_QUAD_GEOMETRY = new BufferedWorldGeometry(DrawMode.QUADS, VertexFormats.POSITION_COLOR, WorldRenderPipelines.IllI1l1l1);
    private static final BufferedWorldGeometry VISIBLE_BLOOM_LINE_GEOMETRY = new BufferedWorldGeometry(
    DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, WorldRenderPipelines.II1Il11l111II11IIl
    );
    private static final BufferedWorldGeometry THROUGH_WALL_BLOOM_QUAD_GEOMETRY = new BufferedWorldGeometry(DrawMode.QUADS, VertexFormats.POSITION_COLOR, WorldRenderPipelines.l1lll11l1l);
    private static final BufferedWorldGeometry THROUGH_WALL_BLOOM_LINE_GEOMETRY = new BufferedWorldGeometry(DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, WorldRenderPipelines.l1I1IlllIlI);
    private static final BufferedWorldGeometry[] BLOOM_GEOMETRY_BUFFERS = new BufferedWorldGeometry[]{VISIBLE_BLOOM_QUAD_GEOMETRY, VISIBLE_BLOOM_LINE_GEOMETRY, THROUGH_WALL_BLOOM_QUAD_GEOMETRY, THROUGH_WALL_BLOOM_LINE_GEOMETRY};
    private static boolean BLOOM_CAPTURE_ACTIVE;
    private static boolean BLOOM_THROUGH_WALLS;
    private static float BLOOM_BLUR_STRENGTH;
    private static float BLOOM_COMPOSITE_STRENGTH;
    private static final Identifier BLOOM_TEXTURE = Identifier.of("daamky", "textures/targetesp/bloom.png");
    private static boolean SMOOTH_GEOMETRY_ENABLED;
    private static boolean TEXTURED_BLOOM_THROUGH_WALLS;
    private static float SMOOTH_GEOMETRY_SCALE;
    private static float TEXTURED_QUAD_OPACITY;
    private static BufferBuilder TEXTURED_QUAD_BUFFER;
    private static Vec3d CAMERA_POSITION = Vec3d.ZERO;
    private static float TICK_DELTA = 1.0F;
    private static boolean CAMERA_STATE_AVAILABLE;
    private static float LINE_WIDTH_MULTIPLIER = 1.0F;
    private static float CAMERA_YAW;
    private static float CAMERA_PITCH;
    private static float CAMERA_RIGHT_X;
    private static float CAMERA_RIGHT_Z;
    private static float CAMERA_UP_X;
    private static float CAMERA_UP_Y;
    private static float CAMERA_UP_Z;
    private static final float[] BLOOM_RING_RADII = new float[]{0.0F, 0.1F, 0.24F, 0.42F, 0.64F, 0.84F, 1.0F};
    private static final float[] BLOOM_RING_ALPHA_FACTORS = new float[BLOOM_RING_RADII.length];
// https://t.me/pasterplace

    private DaamkyWorldRender() {
    }
// https://t.me/pasterplace

    public static void updateCameraState(Vec3d cameraPosition, float yaw, float pitch, float tickDelta) {
        CAMERA_POSITION = cameraPosition;
        CAMERA_YAW = yaw;
        CAMERA_PITCH = pitch;
        TICK_DELTA = tickDelta;
        CAMERA_STATE_AVAILABLE = true;
        updateCameraPlaneBasis();
    }
// https://t.me/pasterplace

    private static void updateCameraPlaneBasis() {
        float yawRadians = CAMERA_YAW * (float) (Math.PI / 180.0);
        float pitchRadians = CAMERA_PITCH * (float) (Math.PI / 180.0);
        float sinYaw = MathHelper.sin(yawRadians);
        float cosYaw = MathHelper.cos(yawRadians);
        float sinPitch = MathHelper.sin(pitchRadians);
        CAMERA_RIGHT_X = -cosYaw;
        CAMERA_RIGHT_Z = -sinYaw;
        CAMERA_UP_X = -sinYaw * sinPitch;
        CAMERA_UP_Y = MathHelper.cos(pitchRadians);
        CAMERA_UP_Z = cosYaw * sinPitch;
    }
// https://t.me/pasterplace

    public static void flushBufferedGeometry(Matrix4f matrix4f) {
        CAMERA_STATE_AVAILABLE = false;
        Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
        renderBufferedGeometry(matrix4f, framebuffer.getColorAttachmentView(), framebuffer.getDepthAttachmentView());
    }
// https://t.me/pasterplace

    public static void setLineWidthMultiplier(float lineWidthMultiplier) {
        LINE_WIDTH_MULTIPLIER = lineWidthMultiplier;
    }
// https://t.me/pasterplace

    public static void flushBufferedGeometryToOutputOverride(Matrix4f matrix4f) {
        CAMERA_STATE_AVAILABLE = false;
        LINE_WIDTH_MULTIPLIER = 1.0F;
        if (RenderSystem.outputColorTextureOverride == null) {
            discardBufferedGeometry();
        } else {
            renderBufferedGeometry(matrix4f, RenderSystem.outputColorTextureOverride, RenderSystem.outputDepthTextureOverride);
        }
    }
// https://t.me/pasterplace

    private static void discardBufferedGeometry() {
        DynamicTransforms.clearSubmissions();

        for (BufferedWorldGeometry geometryBuffer : WORLD_GEOMETRY_BUFFERS) {
            geometryBuffer.l1lll11l1l();
        }

        for (BufferedWorldGeometry bloomBuffer : BLOOM_GEOMETRY_BUFFERS) {
            bloomBuffer.l1lll11l1l();
        }
    }
// https://t.me/pasterplace

    private static void renderBufferedGeometry(Matrix4f matrix4f, GpuTextureView gpuTextureView, GpuTextureView gpuTextureView2) {
        boolean hasWorldGeometry = false;

        for (BufferedWorldGeometry geometryBuffer : WORLD_GEOMETRY_BUFFERS) {
            hasWorldGeometry |= geometryBuffer.l1I1IlllIlI();
        }
// https://t.me/pasterplace

        hasWorldGeometry |= DynamicTransforms.hasPendingDrawSubmissions();
        boolean hasBloomGeometry = false;

        for (BufferedWorldGeometry bloomBuffer : BLOOM_GEOMETRY_BUFFERS) {
            hasBloomGeometry |= bloomBuffer.l1I1IlllIlI();
        }

        if (hasWorldGeometry || hasBloomGeometry) {
            for (BufferedWorldGeometry geometryBuffer : WORLD_GEOMETRY_BUFFERS) {
                geometryBuffer.IllI1l1l1();
            }
// https://t.me/pasterplace

            GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms()
            .write(new Matrix4f(matrix4f), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
            ArrayList<com.mojang.blaze3d.buffers.GpuBuffer> temporaryBuffers = new ArrayList<>();
// https://t.me/pasterplace

            try {
                RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "Daamky world render", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());

                try {
                    for (BufferedWorldGeometry geometryBuffer : WORLD_GEOMETRY_BUFFERS) {
                        geometryBuffer.II1Il11l111II11IIl(renderPass, gpuBufferSlice, temporaryBuffers);
                    }

                    DynamicTransforms.renderSubmissions(renderPass, gpuBufferSlice);
                } catch (Throwable renderFailure) {
                    if (renderPass != null) {
                        try {
                            renderPass.close();
                        } catch (Throwable closeFailure) {
                            renderFailure.addSuppressed(closeFailure);
                        }
                    }

                    throw renderFailure;
                }
// https://t.me/pasterplace

                if (renderPass != null) {
                    renderPass.close();
                }
            } finally {
                DynamicTransforms.clearSubmissions();

                for (BufferedWorldGeometry geometryBuffer : WORLD_GEOMETRY_BUFFERS) {
                    geometryBuffer.l1lll11l1l();
                }

                for (com.mojang.blaze3d.buffers.GpuBuffer buffer : temporaryBuffers) {
                    buffer.close();
                }
            }

            if (hasBloomGeometry) {
                BloomPostProcessor.renderBloom(gpuTextureView, gpuTextureView2, gpuBufferSlice, BLOOM_GEOMETRY_BUFFERS, BLOOM_BLUR_STRENGTH, BLOOM_COMPOSITE_STRENGTH);
            }
        }
    }
// https://t.me/pasterplace

    public static boolean hasCameraState() {
        return CAMERA_STATE_AVAILABLE;
    }
// https://t.me/pasterplace

    public static Vec3d getCameraPosition() {
        return CAMERA_POSITION;
    }
// https://t.me/pasterplace

    public static float getCameraYaw() {
        return CAMERA_YAW;
    }
// https://t.me/pasterplace

    public static float getCameraPitch() {
        return CAMERA_PITCH;
    }
// https://t.me/pasterplace

    public static float getTickDelta() {
        return TICK_DELTA;
    }
// https://t.me/pasterplace

    public static Vec3d getInterpolatedPosition(Entity entity) {
        return getInterpolatedPosition(entity, TICK_DELTA);
    }
// https://t.me/pasterplace

    public static Vec3d getInterpolatedPosition(Entity entity, float partialTick) {
        return new Vec3d(
        MathHelper.lerp(partialTick, entity.lastX, entity.getX()),
        MathHelper.lerp(partialTick, entity.lastY, entity.getY()),
        MathHelper.lerp(partialTick, entity.lastZ, entity.getZ())
        );
    }
// https://t.me/pasterplace

    public static Box getInterpolatedBoundingBox(Entity entity) {
        Vec3d interpolatedPosition = getInterpolatedPosition(entity);
        return entity.getBoundingBox()
        .offset(interpolatedPosition.x - entity.getX(), interpolatedPosition.y - entity.getY(), interpolatedPosition.z - entity.getZ());
    }
// https://t.me/pasterplace

    public static Vec3d getCameraLookDirection() {
        float yawRadians = CAMERA_YAW * (float) (Math.PI / 180.0);
        float pitchRadians = CAMERA_PITCH * (float) (Math.PI / 180.0);
        float cosPitch = MathHelper.cos(pitchRadians);
        return new Vec3d(-MathHelper.sin(yawRadians) * cosPitch, -MathHelper.sin(pitchRadians), MathHelper.cos(yawRadians) * cosPitch);
    }
// https://t.me/pasterplace

    private static BufferedWorldGeometry selectLineGeometry(boolean throughWalls) {
        return throughWalls ? THROUGH_WALL_LINE_GEOMETRY : VISIBLE_LINE_GEOMETRY;
    }
// https://t.me/pasterplace

    private static BufferedWorldGeometry selectQuadGeometry(boolean throughWalls) {
        return throughWalls ? THROUGH_WALL_QUAD_GEOMETRY : VISIBLE_QUAD_GEOMETRY;
    }
// https://t.me/pasterplace

    private static BufferedWorldGeometry selectAdditiveQuadGeometry(boolean throughWalls) {
        return throughWalls ? THROUGH_WALL_ADDITIVE_QUAD_GEOMETRY : VISIBLE_ADDITIVE_QUAD_GEOMETRY;
    }
// https://t.me/pasterplace

    public static void beginTexturedQuadBuffer(float unusedWidth, float unusedHeight) {
        beginTexturedQuadBuffer(unusedWidth, unusedHeight, false);
    }
// https://t.me/pasterplace

    public static void beginTexturedQuadBuffer(float unusedWidth, float unusedHeight, boolean unusedThroughWalls) {

        TEXTURED_QUAD_BUFFER = DynamicTransforms.createBufferBuilder(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
    }
// https://t.me/pasterplace

    public static void beginBloomCapture(float blurStrength, float compositeStrength, boolean throughWalls) {
        BLOOM_CAPTURE_ACTIVE = true;
        BLOOM_THROUGH_WALLS = throughWalls;
        BLOOM_BLUR_STRENGTH = blurStrength;
        BLOOM_COMPOSITE_STRENGTH = compositeStrength;
    }
// https://t.me/pasterplace

    public static void endBloomCapture() {
        BLOOM_CAPTURE_ACTIVE = false;
    }
// https://t.me/pasterplace

    public static void flushTexturedQuadBuffer() {

        if (TEXTURED_QUAD_BUFFER != null) {
            GpuTextureView gpuTextureView = GpuTextureViewCache.II1Il11l111II11IIl(BLOOM_TEXTURE);
            if (gpuTextureView != null) {
                DynamicTransforms.submitDraw(
                TEXTURED_QUAD_BUFFER, DrawMode.QUADS, TEXTURED_BLOOM_THROUGH_WALLS ? WorldRenderPipelines.l11lIlIIll1III1 : WorldRenderPipelines.I1lIIII1III1lIlIl1l, gpuTextureView
                );
            } else {
                DynamicTransforms.submitDraw(
                TEXTURED_QUAD_BUFFER, DrawMode.QUADS, TEXTURED_BLOOM_THROUGH_WALLS ? WorldRenderPipelines.l1IIl11lI : WorldRenderPipelines.lIIl11l111lIIl1ll, null
                );
            }

            TEXTURED_QUAD_BUFFER = null;
        }
    }
// https://t.me/pasterplace

    private static void appendCameraFacingTexturedQuad(float centerX, float centerY, float centerZ, float halfSize, int colorArgb) {
        int adjustedAlpha = Math.min(255, (int)((colorArgb >>> 24 & 0xFF) * TEXTURED_QUAD_OPACITY * 0.18F));
        if (adjustedAlpha > 2) {
            int adjustedColorArgb = colorArgb & 16777215 | adjustedAlpha << 24;
            float rightX = CAMERA_RIGHT_X * halfSize;
            float rightZ = CAMERA_RIGHT_Z * halfSize;
            float upX = CAMERA_UP_X * halfSize;
            float upY = CAMERA_UP_Y * halfSize;
            float upZ = CAMERA_UP_Z * halfSize;
            TEXTURED_QUAD_BUFFER.vertex(centerX - rightX - upX, centerY - upY, centerZ - rightZ - upZ).texture(0.0F, 1.0F).color(adjustedColorArgb);
            TEXTURED_QUAD_BUFFER.vertex(centerX + rightX - upX, centerY - upY, centerZ + rightZ - upZ).texture(1.0F, 1.0F).color(adjustedColorArgb);
            TEXTURED_QUAD_BUFFER.vertex(centerX + rightX + upX, centerY + upY, centerZ + rightZ + upZ).texture(1.0F, 0.0F).color(adjustedColorArgb);
            TEXTURED_QUAD_BUFFER.vertex(centerX - rightX + upX, centerY + upY, centerZ - rightZ + upZ).texture(0.0F, 0.0F).color(adjustedColorArgb);
        }
    }
// https://t.me/pasterplace

    private static int interpolateArgb(int startArgb, int endArgb, float progress) {
        int startRed = startArgb >> 16 & 0xFF;
        int startGreen = startArgb >> 8 & 0xFF;
        int startBlue = startArgb & 0xFF;
        int startAlpha = startArgb >>> 24;
        int endRed = endArgb >> 16 & 0xFF;
        int endGreen = endArgb >> 8 & 0xFF;
        int endBlue = endArgb & 0xFF;
        int endAlpha = endArgb >>> 24;
        return (int)(startAlpha + (endAlpha - startAlpha) * progress) << 24
        | (int)(startRed + (endRed - startRed) * progress) << 16
        | (int)(startGreen + (endGreen - startGreen) * progress) << 8
        | (int)(startBlue + (endBlue - startBlue) * progress);
    }
// https://t.me/pasterplace

    private static void drawSmoothLine(
    float startX, float startY, float startZ, float endX, float endY, float endZ, int startColorArgb, int endColorArgb, float lineLength
    ) {
        float strokeSampleSpacing = 0.06F * SMOOTH_GEOMETRY_SCALE;
        int segmentCount = Math.max(1, Math.min(128, (int)Math.ceil(lineLength / Math.max(0.02F, strokeSampleSpacing * 0.6F))));

        for (int segmentIndex = 0; segmentIndex <= segmentCount; segmentIndex++) {
            float segmentProgress = (float)segmentIndex / segmentCount;
            appendCameraFacingTexturedQuad(
            startX + (endX - startX) * segmentProgress,
            startY + (endY - startY) * segmentProgress,
            startZ + (endZ - startZ) * segmentProgress,
            strokeSampleSpacing,
            interpolateArgb(startColorArgb, endColorArgb, segmentProgress)
            );
        }
    }
// https://t.me/pasterplace

    private static void drawSmoothGradientQuad(Vec3d firstCorner, Vec3d secondCorner, Vec3d thirdCorner, Vec3d fourthCorner, int firstCornerColorArgb, int secondCornerColorArgb, int thirdCornerColorArgb, int fourthCornerColorArgb) {
        float strokeHalfWidth = 0.07F * SMOOTH_GEOMETRY_SCALE;
        float maximumSampleSpacing = Math.max(0.03F, strokeHalfWidth * 0.7F);
        if (thirdCorner.squaredDistanceTo(fourthCorner) < 1.0E-10) {
            fourthCorner = firstCorner;
            fourthCornerColorArgb = firstCornerColorArgb;
        }

        double longestEdgeLength = Math.max(Math.max(firstCorner.distanceTo(secondCorner), fourthCorner.distanceTo(thirdCorner)), Math.max(firstCorner.distanceTo(fourthCorner), secondCorner.distanceTo(thirdCorner)));
        int edgeSampleCount = Math.max(1, Math.min(48, (int)Math.ceil(longestEdgeLength / maximumSampleSpacing)));
        float firstCornerRelativeX = (float)(firstCorner.x - CAMERA_POSITION.x);
        float firstCornerRelativeY = (float)(firstCorner.y - CAMERA_POSITION.y);
        float firstCornerRelativeZ = (float)(firstCorner.z - CAMERA_POSITION.z);
        float secondCornerRelativeX = (float)(secondCorner.x - CAMERA_POSITION.x);
        float secondCornerRelativeY = (float)(secondCorner.y - CAMERA_POSITION.y);
        float secondCornerRelativeZ = (float)(secondCorner.z - CAMERA_POSITION.z);
        float thirdCornerRelativeX = (float)(thirdCorner.x - CAMERA_POSITION.x);
        float thirdCornerRelativeY = (float)(thirdCorner.y - CAMERA_POSITION.y);
        float thirdCornerRelativeZ = (float)(thirdCorner.z - CAMERA_POSITION.z);
        float fourthCornerRelativeX = (float)(fourthCorner.x - CAMERA_POSITION.x);
        float fourthCornerRelativeY = (float)(fourthCorner.y - CAMERA_POSITION.y);
        float fourthCornerRelativeZ = (float)(fourthCorner.z - CAMERA_POSITION.z);

        for (int edgeSampleIndex = 0; edgeSampleIndex <= edgeSampleCount; edgeSampleIndex++) {
            float edgeSampleProgress = (float)edgeSampleIndex / edgeSampleCount;
            float firstEdgeX = firstCornerRelativeX + (secondCornerRelativeX - firstCornerRelativeX) * edgeSampleProgress;
            float firstEdgeY = firstCornerRelativeY + (secondCornerRelativeY - firstCornerRelativeY) * edgeSampleProgress;
            float firstEdgeZ = firstCornerRelativeZ + (secondCornerRelativeZ - firstCornerRelativeZ) * edgeSampleProgress;
            float secondEdgeX = fourthCornerRelativeX + (thirdCornerRelativeX - fourthCornerRelativeX) * edgeSampleProgress;
            float secondEdgeY = fourthCornerRelativeY + (thirdCornerRelativeY - fourthCornerRelativeY) * edgeSampleProgress;
            float secondEdgeZ = fourthCornerRelativeZ + (thirdCornerRelativeZ - fourthCornerRelativeZ) * edgeSampleProgress;
            int firstEdgeColorArgb = interpolateArgb(firstCornerColorArgb, secondCornerColorArgb, edgeSampleProgress);
            int secondEdgeColorArgb = interpolateArgb(fourthCornerColorArgb, thirdCornerColorArgb, edgeSampleProgress);
            float crossStripDeltaX = secondEdgeX - firstEdgeX;
            float crossStripDeltaY = secondEdgeY - firstEdgeY;
            float crossStripDeltaZ = secondEdgeZ - firstEdgeZ;
            int crossStripSampleCount = Math.max(1, Math.min(48, (int)Math.ceil(MathHelper.sqrt(crossStripDeltaX * crossStripDeltaX + crossStripDeltaY * crossStripDeltaY + crossStripDeltaZ * crossStripDeltaZ) / maximumSampleSpacing)));

            for (int crossStripSampleIndex = 0; crossStripSampleIndex <= crossStripSampleCount; crossStripSampleIndex++) {
                float crossStripProgress = (float)crossStripSampleIndex / crossStripSampleCount;
                int interpolatedColorArgb = interpolateArgb(firstEdgeColorArgb, secondEdgeColorArgb, crossStripProgress);
                int bloomColorArgb = interpolatedColorArgb & 16777215 | (int)((interpolatedColorArgb >>> 24 & 0xFF) * 0.35F) << 24;
                appendCameraFacingTexturedQuad(firstEdgeX + crossStripDeltaX * crossStripProgress, firstEdgeY + crossStripDeltaY * crossStripProgress, firstEdgeZ + crossStripDeltaZ * crossStripProgress, strokeHalfWidth, bloomColorArgb);
            }
        }
    }
// https://t.me/pasterplace

    public static void drawLine(Vec3d startPosition, Vec3d endPosition, RgbaColor lineColor, float lineWidth, boolean throughWalls) {
        drawLine(
        startPosition.x,
        startPosition.y,
        startPosition.z,
        endPosition.x,
        endPosition.y,
        endPosition.z,
        lineColor.toArgb(),
        lineColor.toArgb(),
        lineWidth,
        throughWalls
        );
    }
// https://t.me/pasterplace

    public static void drawGradientLine(Vec3d startPosition, Vec3d endPosition, RgbaColor startColor, RgbaColor endColor, float lineWidth, boolean throughWalls) {
        drawLine(
        startPosition.x,
        startPosition.y,
        startPosition.z,
        endPosition.x,
        endPosition.y,
        endPosition.z,
        startColor.toArgb(),
        endColor.toArgb(),
        lineWidth,
        throughWalls
        );
    }
// https://t.me/pasterplace

    public static void drawLine(
    double startX,
    double startY,
    double startZ,
    double endX,
    double endY,
    double endZ,
    int startColorArgb,
    int endColorArgb,
    float lineWidth,
    boolean throughWalls
    ) {
        float startRelativeX = (float)(startX - CAMERA_POSITION.x);
        float startRelativeY = (float)(startY - CAMERA_POSITION.y);
        float startRelativeZ = (float)(startZ - CAMERA_POSITION.z);
        float endRelativeX = (float)(endX - CAMERA_POSITION.x);
        float endRelativeY = (float)(endY - CAMERA_POSITION.y);
        float endRelativeZ = (float)(endZ - CAMERA_POSITION.z);
        float directionX = endRelativeX - startRelativeX;
        float directionY = endRelativeY - startRelativeY;
        float directionZ = endRelativeZ - startRelativeZ;
        float segmentLength = MathHelper.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
        if (!(segmentLength < 1.0E-6F)) {
            if (SMOOTH_GEOMETRY_ENABLED) {
                drawSmoothLine(startRelativeX, startRelativeY, startRelativeZ, endRelativeX, endRelativeY, endRelativeZ, startColorArgb, endColorArgb, segmentLength);
            } else {
                directionX /= segmentLength;
                directionY /= segmentLength;
                directionZ /= segmentLength;
                BufferBuilder lineBufferBuilder = selectLineGeometry(throughWalls).II1Il11l111II11IIl();
                float scaledLineWidth = lineWidth * LINE_WIDTH_MULTIPLIER;
                lineBufferBuilder.vertex(startRelativeX, startRelativeY, startRelativeZ).color(startColorArgb).normal(directionX, directionY, directionZ).lineWidth(scaledLineWidth);
                lineBufferBuilder.vertex(endRelativeX, endRelativeY, endRelativeZ).color(endColorArgb).normal(directionX, directionY, directionZ).lineWidth(scaledLineWidth);
                if (BLOOM_CAPTURE_ACTIVE) {
                    BufferBuilder bloomLineBufferBuilder = (!throughWalls && !BLOOM_THROUGH_WALLS ? VISIBLE_BLOOM_LINE_GEOMETRY : THROUGH_WALL_BLOOM_LINE_GEOMETRY).II1Il11l111II11IIl();
                    bloomLineBufferBuilder.vertex(startRelativeX, startRelativeY, startRelativeZ).color(startColorArgb).normal(directionX, directionY, directionZ).lineWidth(scaledLineWidth * 1.6F);
                    bloomLineBufferBuilder.vertex(endRelativeX, endRelativeY, endRelativeZ).color(endColorArgb).normal(directionX, directionY, directionZ).lineWidth(scaledLineWidth * 1.6F);
                }
            }
        }
    }
// https://t.me/pasterplace

    public static void drawPolyline(List<Vec3d> points, RgbaColor lineColor, float lineWidth, boolean throughWalls) {
        for (int pointIndex = 1; pointIndex < points.size(); pointIndex++) {
            drawLine((Vec3d)points.get(pointIndex - 1), (Vec3d)points.get(pointIndex), lineColor, lineWidth, throughWalls);
        }
    }
// https://t.me/pasterplace

    public static void drawCameraToPointGlow(Vec3d targetPosition, RgbaColor glowColor, float lineWidth) {
        drawCameraToPointGradientLine(targetPosition, glowColor.withAlpha(glowColor.alpha() / 4), glowColor, lineWidth);
    }
// https://t.me/pasterplace

    public static void drawCameraToPointGradientLine(Vec3d targetPosition, RgbaColor startColor, RgbaColor endColor, float lineWidth) {
        Vec3d cameraOrigin = CAMERA_POSITION.add(getCameraLookDirection().multiply(0.5));
        drawGradientLine(cameraOrigin, targetPosition, startColor, endColor, lineWidth, true);
    }
// https://t.me/pasterplace

    public static void drawBoxOutline(Box box, RgbaColor lineColor, float lineWidth, boolean throughWalls) {
        int colorArgb = lineColor.toArgb();
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;
        drawLine(minX, minY, minZ, maxX, minY, minZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(maxX, minY, minZ, maxX, minY, maxZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(maxX, minY, maxZ, minX, minY, maxZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(minX, minY, maxZ, minX, minY, minZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(minX, maxY, minZ, maxX, maxY, minZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(maxX, maxY, minZ, maxX, maxY, maxZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(maxX, maxY, maxZ, minX, maxY, maxZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(minX, maxY, maxZ, minX, maxY, minZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(minX, minY, minZ, minX, maxY, minZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(maxX, minY, minZ, maxX, maxY, minZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(maxX, minY, maxZ, maxX, maxY, maxZ, colorArgb, colorArgb, lineWidth, throughWalls);
        drawLine(minX, minY, maxZ, minX, maxY, maxZ, colorArgb, colorArgb, lineWidth, throughWalls);
    }
// https://t.me/pasterplace

    public static void drawFilledBox(Box box, RgbaColor fillColor, boolean throughWalls) {
        drawGradientBox(box, fillColor, fillColor, throughWalls);
    }
// https://t.me/pasterplace

    public static void drawGradientBox(Box box, RgbaColor bottomColor, RgbaColor topColor, boolean throughWalls) {
        if (SMOOTH_GEOMETRY_ENABLED) {
            int bottomColorArgb = bottomColor.toArgb();
            int topColorArgb = topColor.toArgb();
            Vec3d bottomNearLeft = new Vec3d(box.minX, box.minY, box.minZ);
            Vec3d bottomNearRight = new Vec3d(box.maxX, box.minY, box.minZ);
            Vec3d bottomFarRight = new Vec3d(box.maxX, box.minY, box.maxZ);
            Vec3d bottomFarLeft = new Vec3d(box.minX, box.minY, box.maxZ);
            Vec3d topNearLeft = new Vec3d(box.minX, box.maxY, box.minZ);
            Vec3d topNearRight = new Vec3d(box.maxX, box.maxY, box.minZ);
            Vec3d topFarRight = new Vec3d(box.maxX, box.maxY, box.maxZ);
            Vec3d topFarLeft = new Vec3d(box.minX, box.maxY, box.maxZ);
            drawSmoothGradientQuad(bottomNearLeft, bottomNearRight, bottomFarRight, bottomFarLeft, bottomColorArgb, bottomColorArgb, bottomColorArgb, bottomColorArgb);
            drawSmoothGradientQuad(topNearLeft, topNearRight, topFarRight, topFarLeft, topColorArgb, topColorArgb, topColorArgb, topColorArgb);
            drawSmoothGradientQuad(bottomNearLeft, bottomNearRight, topNearRight, topNearLeft, bottomColorArgb, bottomColorArgb, topColorArgb, topColorArgb);
            drawSmoothGradientQuad(bottomNearRight, bottomFarRight, topFarRight, topNearRight, bottomColorArgb, bottomColorArgb, topColorArgb, topColorArgb);
            drawSmoothGradientQuad(bottomFarRight, bottomFarLeft, topFarLeft, topFarRight, bottomColorArgb, bottomColorArgb, topColorArgb, topColorArgb);
            drawSmoothGradientQuad(bottomFarLeft, bottomNearLeft, topNearLeft, topFarLeft, bottomColorArgb, bottomColorArgb, topColorArgb, topColorArgb);
        } else {
            float minXRelative = (float)(box.minX - CAMERA_POSITION.x);
            float minYRelative = (float)(box.minY - CAMERA_POSITION.y);
            float minZRelative = (float)(box.minZ - CAMERA_POSITION.z);
            float maxXRelative = (float)(box.maxX - CAMERA_POSITION.x);
            float maxYRelative = (float)(box.maxY - CAMERA_POSITION.y);
            float maxZRelative = (float)(box.maxZ - CAMERA_POSITION.z);
            int bottomColorArgb = bottomColor.toArgb();
            int topColorArgb = topColor.toArgb();
            BufferBuilder bufferBuilder = selectQuadGeometry(throughWalls).II1Il11l111II11IIl();
            bufferBuilder.vertex(minXRelative, minYRelative, minZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(maxXRelative, minYRelative, minZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(maxXRelative, minYRelative, maxZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(minXRelative, minYRelative, maxZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(minXRelative, maxYRelative, minZRelative).color(topColorArgb);
            bufferBuilder.vertex(minXRelative, maxYRelative, maxZRelative).color(topColorArgb);
            bufferBuilder.vertex(maxXRelative, maxYRelative, maxZRelative).color(topColorArgb);
            bufferBuilder.vertex(maxXRelative, maxYRelative, minZRelative).color(topColorArgb);
            bufferBuilder.vertex(minXRelative, minYRelative, minZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(minXRelative, maxYRelative, minZRelative).color(topColorArgb);
            bufferBuilder.vertex(maxXRelative, maxYRelative, minZRelative).color(topColorArgb);
            bufferBuilder.vertex(maxXRelative, minYRelative, minZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(maxXRelative, minYRelative, minZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(maxXRelative, maxYRelative, minZRelative).color(topColorArgb);
            bufferBuilder.vertex(maxXRelative, maxYRelative, maxZRelative).color(topColorArgb);
            bufferBuilder.vertex(maxXRelative, minYRelative, maxZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(minXRelative, minYRelative, maxZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(maxXRelative, minYRelative, maxZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(maxXRelative, maxYRelative, maxZRelative).color(topColorArgb);
            bufferBuilder.vertex(minXRelative, maxYRelative, maxZRelative).color(topColorArgb);
            bufferBuilder.vertex(minXRelative, minYRelative, minZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(minXRelative, minYRelative, maxZRelative).color(bottomColorArgb);
            bufferBuilder.vertex(minXRelative, maxYRelative, maxZRelative).color(topColorArgb);
            bufferBuilder.vertex(minXRelative, maxYRelative, minZRelative).color(topColorArgb);
        }
    }
// https://t.me/pasterplace

    public static void drawBoxFillAndOutline(Box box, RgbaColor fillColor, RgbaColor outlineColor, float outlineWidth, boolean throughWalls) {
        if (fillColor != null && fillColor.alpha() > 0) {
            drawFilledBox(box, fillColor, throughWalls);
        }

        if (outlineColor != null && outlineColor.alpha() > 0) {
            drawBoxOutline(box, outlineColor, outlineWidth, throughWalls);
        }
    }
// https://t.me/pasterplace

    public static void drawGradientQuad(
    Vec3d vertexA,
    Vec3d vertexB,
    Vec3d vertexC,
    Vec3d vertexD,
    RgbaColor vertexAColor,
    RgbaColor vertexBColor,
    RgbaColor vertexCColor,
    RgbaColor vertexDColor,
    boolean throughWalls
    ) {
        if (SMOOTH_GEOMETRY_ENABLED) {
            drawSmoothGradientQuad(
            vertexA, vertexB, vertexC, vertexD, vertexAColor.toArgb(), vertexBColor.toArgb(), vertexCColor.toArgb(), vertexDColor.toArgb()
            );
        } else {
            BufferBuilder bufferBuilder = selectQuadGeometry(throughWalls).II1Il11l111II11IIl();
            appendCameraRelativeVertex(bufferBuilder, vertexA, vertexAColor.toArgb());
            appendCameraRelativeVertex(bufferBuilder, vertexB, vertexBColor.toArgb());
            appendCameraRelativeVertex(bufferBuilder, vertexC, vertexCColor.toArgb());
            appendCameraRelativeVertex(bufferBuilder, vertexD, vertexDColor.toArgb());
            if (BLOOM_CAPTURE_ACTIVE) {
                BufferBuilder bufferBuilder2 = (!throughWalls && !BLOOM_THROUGH_WALLS ? VISIBLE_BLOOM_QUAD_GEOMETRY : THROUGH_WALL_BLOOM_QUAD_GEOMETRY).II1Il11l111II11IIl();
                appendCameraRelativeVertex(bufferBuilder2, vertexA, vertexAColor.toArgb());
                appendCameraRelativeVertex(bufferBuilder2, vertexB, vertexBColor.toArgb());
                appendCameraRelativeVertex(bufferBuilder2, vertexC, vertexCColor.toArgb());
                appendCameraRelativeVertex(bufferBuilder2, vertexD, vertexDColor.toArgb());
            }
        }
    }
// https://t.me/pasterplace

    public static void drawQuad(Vec3d vertexA, Vec3d vertexB, Vec3d vertexC, Vec3d vertexD, RgbaColor color, boolean throughWalls) {
        drawGradientQuad(vertexA, vertexB, vertexC, vertexD, color, color, color, color, throughWalls);
    }
// https://t.me/pasterplace

    public static void drawTriangle(
    Vec3d vertexA, Vec3d vertexB, Vec3d vertexC, RgbaColor colorA, RgbaColor colorB, RgbaColor colorC, boolean throughWalls
    ) {
        drawGradientQuad(vertexA, vertexB, vertexC, vertexC, colorA, colorB, colorC, colorC, throughWalls);
    }
// https://t.me/pasterplace

    public static void drawAdditiveGradientQuad(
    Vec3d vertexA,
    Vec3d vertexB,
    Vec3d vertexC,
    Vec3d vertexD,
    RgbaColor vertexAColor,
    RgbaColor vertexBColor,
    RgbaColor vertexCColor,
    RgbaColor vertexDColor,
    boolean throughWalls
    ) {
        if (!SMOOTH_GEOMETRY_ENABLED) {
            BufferBuilder bufferBuilder = selectAdditiveQuadGeometry(throughWalls).II1Il11l111II11IIl();
            appendCameraRelativeVertex(bufferBuilder, vertexA, vertexAColor.toArgb());
            appendCameraRelativeVertex(bufferBuilder, vertexB, vertexBColor.toArgb());
            appendCameraRelativeVertex(bufferBuilder, vertexC, vertexCColor.toArgb());
            appendCameraRelativeVertex(bufferBuilder, vertexD, vertexDColor.toArgb());
        }
    }
// https://t.me/pasterplace

    private static void appendCameraRelativeVertex(BufferBuilder bufferBuilder, Vec3d worldPosition, int colorArgb) {
        bufferBuilder.vertex(
        (float)(worldPosition.x - CAMERA_POSITION.x),
        (float)(worldPosition.y - CAMERA_POSITION.y),
        (float)(worldPosition.z - CAMERA_POSITION.z)
        )
        .color(colorArgb);
    }
// https://t.me/pasterplace

    public static Vector3f[] getCameraPlaneBasis() {
        return new Vector3f[]{new Vector3f(CAMERA_RIGHT_X, 0.0F, CAMERA_RIGHT_Z), new Vector3f(CAMERA_UP_X, CAMERA_UP_Y, CAMERA_UP_Z)};
    }
// https://t.me/pasterplace

    public static boolean shouldRenderBox(Box box) {
        Vec3d boxCenter = box.getCenter();
        double cameraDeltaX = boxCenter.x - CAMERA_POSITION.x;
        double cameraDeltaY = boxCenter.y - CAMERA_POSITION.y;
        double cameraDeltaZ = boxCenter.z - CAMERA_POSITION.z;
        double visibilityRadius = Math.max(box.getLengthX(), Math.max(box.getLengthY(), box.getLengthZ())) * 0.9 + 0.5;
        double squaredDistance = cameraDeltaX * cameraDeltaX + cameraDeltaY * cameraDeltaY + cameraDeltaZ * cameraDeltaZ;
        if (squaredDistance <= visibilityRadius * visibilityRadius) {
            return true;
        }

        Vec3d cameraLookDirection = getCameraLookDirection();
        double cameraForwardDistance = cameraDeltaX * cameraLookDirection.x + cameraDeltaY * cameraLookDirection.y + cameraDeltaZ * cameraLookDirection.z;
        if (cameraForwardDistance < -visibilityRadius) {
            return false;
        }

        double distance = Math.sqrt(squaredDistance);
        return cameraForwardDistance + visibilityRadius >= distance * 0.15;
    }
// https://t.me/pasterplace

    public static void drawBloomRing(Vec3d center, float radius, RgbaColor ringColor, boolean throughWalls) {
        if (!SMOOTH_GEOMETRY_ENABLED) {
            Vector3f[] cameraPlaneBasis = getCameraPlaneBasis();
            Vector3f cameraRight = cameraPlaneBasis[0];
            Vector3f cameraUp = cameraPlaneBasis[1];
            float radiusScale = radius * 1.35F;
            int baseAlpha = ringColor.alpha();

            float[] cosineBySegment = new float[17];

            float[] sineBySegment = new float[17];

            for (int segmentIndex = 0; segmentIndex <= 16; segmentIndex++) {
                float angleRadians = (float)(segmentIndex * Math.PI * 2.0 / 16.0);
                cosineBySegment[segmentIndex] = MathHelper.cos(angleRadians);
                sineBySegment[segmentIndex] = MathHelper.sin(angleRadians);
            }

            BufferBuilder bloomBuffer = selectAdditiveQuadGeometry(throughWalls).II1Il11l111II11IIl();

            for (int radialBandIndex = 0; radialBandIndex < BLOOM_RING_RADII.length - 1; radialBandIndex++) {
                float innerRadius = BLOOM_RING_RADII[radialBandIndex] * radiusScale;
                float outerRadius = BLOOM_RING_RADII[radialBandIndex + 1] * radiusScale;
                int innerColorArgb = ringColor.withAlpha((int)(baseAlpha * BLOOM_RING_ALPHA_FACTORS[radialBandIndex])).toArgb();
                int outerColorArgb = ringColor.withAlpha((int)(baseAlpha * BLOOM_RING_ALPHA_FACTORS[radialBandIndex + 1])).toArgb();

                for (int segmentIndex = 0; segmentIndex < 16; segmentIndex++) {
                    Vec3d innerStart = getCameraPlaneOffsetPosition(center, cameraRight, cameraUp, cosineBySegment[segmentIndex] * innerRadius, sineBySegment[segmentIndex] * innerRadius);
                    Vec3d outerStart = getCameraPlaneOffsetPosition(center, cameraRight, cameraUp, cosineBySegment[segmentIndex] * outerRadius, sineBySegment[segmentIndex] * outerRadius);
                    Vec3d outerEnd = getCameraPlaneOffsetPosition(center, cameraRight, cameraUp, cosineBySegment[segmentIndex + 1] * outerRadius, sineBySegment[segmentIndex + 1] * outerRadius);
                    Vec3d innerEnd = getCameraPlaneOffsetPosition(center, cameraRight, cameraUp, cosineBySegment[segmentIndex + 1] * innerRadius, sineBySegment[segmentIndex + 1] * innerRadius);
                    appendCameraRelativeVertex(bloomBuffer, innerStart, innerColorArgb);
                    appendCameraRelativeVertex(bloomBuffer, outerStart, outerColorArgb);
                    appendCameraRelativeVertex(bloomBuffer, outerEnd, outerColorArgb);
                    appendCameraRelativeVertex(bloomBuffer, innerEnd, innerColorArgb);
                }
            }
        }
    }
// https://t.me/pasterplace

    private static Vec3d getCameraPlaneOffsetPosition(Vec3d origin, Vector3f cameraRight, Vector3f cameraUp, float rightOffset, float upOffset) {
        return origin.add(cameraRight.x * rightOffset + cameraUp.x * upOffset, cameraRight.y * rightOffset + cameraUp.y * upOffset, cameraRight.z * rightOffset + cameraUp.z * upOffset);
    }
// https://t.me/pasterplace

    public static void drawBillboardQuad(Vec3d center, float halfSize, RgbaColor color, boolean throughWalls) {
        Vector3f[] vector3fArray = getCameraPlaneBasis();
        Vector3f cameraRight = vector3fArray[0];
        Vector3f cameraUp = vector3fArray[1];
        Vec3d topLeft = center.add(-cameraRight.x * halfSize - cameraUp.x * halfSize, -cameraRight.y * halfSize - cameraUp.y * halfSize, -cameraRight.z * halfSize - cameraUp.z * halfSize);
        Vec3d topRight = center.add(cameraRight.x * halfSize - cameraUp.x * halfSize, cameraRight.y * halfSize - cameraUp.y * halfSize, cameraRight.z * halfSize - cameraUp.z * halfSize);
        Vec3d bottomRight = center.add(cameraRight.x * halfSize + cameraUp.x * halfSize, cameraRight.y * halfSize + cameraUp.y * halfSize, cameraRight.z * halfSize + cameraUp.z * halfSize);
        Vec3d bottomLeft = center.add(-cameraRight.x * halfSize + cameraUp.x * halfSize, -cameraRight.y * halfSize + cameraUp.y * halfSize, -cameraRight.z * halfSize + cameraUp.z * halfSize);
        drawQuad(topLeft, topRight, bottomRight, bottomLeft, color, throughWalls);
    }
// https://t.me/pasterplace

    public static void drawCircleOutline(Vec3d center, double radius, RgbaColor lineColor, float lineWidth, int segmentCount, boolean throughWalls) {
        double previousX = center.x + radius;
        double previousZ = center.z;
        int colorArgb = lineColor.toArgb();
// https://t.me/pasterplace

        for (int segmentIndex = 1; segmentIndex <= segmentCount; segmentIndex++) {
            double angleRadians = segmentIndex * Math.PI * 2.0 / segmentCount;
            double currentX = center.x + Math.cos(angleRadians) * radius;
            double currentZ = center.z + Math.sin(angleRadians) * radius;
            drawLine(previousX, center.y, previousZ, currentX, center.y, currentZ, colorArgb, colorArgb, lineWidth, throughWalls);
            previousX = currentX;
            previousZ = currentZ;
        }
    }
// https://t.me/pasterplace

    public static void drawRing(
    Vec3d center, double outerRadius, double innerRadius, RgbaColor outerColor, RgbaColor innerColor, int segmentCount, boolean useAdditiveGeometry, boolean throughWalls
    ) {
        for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
            double startAngleRadians = segmentIndex * Math.PI * 2.0 / segmentCount;
            double endAngleRadians = (segmentIndex + 1) * Math.PI * 2.0 / segmentCount;
            Vec3d outerStart = center.add(Math.cos(startAngleRadians) * outerRadius, 0.0, Math.sin(startAngleRadians) * outerRadius);
            Vec3d outerEnd = center.add(Math.cos(endAngleRadians) * outerRadius, 0.0, Math.sin(endAngleRadians) * outerRadius);
            Vec3d innerStart = center.add(Math.cos(startAngleRadians) * innerRadius, 0.0, Math.sin(startAngleRadians) * innerRadius);
            Vec3d innerEnd = center.add(Math.cos(endAngleRadians) * innerRadius, 0.0, Math.sin(endAngleRadians) * innerRadius);
            if (useAdditiveGeometry) {
                drawAdditiveGradientQuad(outerStart, innerStart, innerEnd, outerEnd, outerColor, innerColor, innerColor, outerColor, throughWalls);
            } else {
                drawGradientQuad(outerStart, innerStart, innerEnd, outerEnd, outerColor, innerColor, innerColor, outerColor, throughWalls);
            }
        }
    }
// https://t.me/pasterplace

    static {
        for (int var0 = 0; var0 < BLOOM_RING_RADII.length; var0++) {
            float var1 = BLOOM_RING_RADII[var0];
            BLOOM_RING_ALPHA_FACTORS[var0] = var0 == BLOOM_RING_RADII.length - 1 ? 0.0F : (float)Math.exp(-5.5 * var1 * var1);
        }
    }
}

