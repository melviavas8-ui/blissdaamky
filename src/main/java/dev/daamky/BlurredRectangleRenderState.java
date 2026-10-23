package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.gl.GpuSampler;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public final class BlurredRectangleRenderState implements SimpleGuiElementRenderState {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private final KawaseGaussianBlurPass blurPass;
    private final Matrix3x2f transformationMatrix;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final int blockLight;
    private final int skyLight;
    private final int topLeftColor;
    private final int topRightColor;
    private final int bottomRightColor;
    private final int bottomLeftColor;
    private final @Nullable ScreenRect scissorArea;
    private final ScreenRect bounds;
    private @Nullable TextureSetup cachedTextureSetup;
// https://t.me/pasterplace

    BlurredRectangleRenderState(
    KawaseGaussianBlurPass blurPass,
    Matrix3x2f transformationMatrix,
    float x,
    float y,
    float width,
    float height,
    int blockLight,
    int skyLight,
    int topLeftColor,
    int topRightColor,
    int bottomRightColor,
    int bottomLeftColor,
    @Nullable ScreenRect scissorArea
    ) {
        this.blurPass = blurPass;
        this.transformationMatrix = transformationMatrix;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.topLeftColor = topLeftColor;
        this.topRightColor = topRightColor;
        this.bottomRightColor = bottomRightColor;
        this.bottomLeftColor = bottomLeftColor;
        this.scissorArea = scissorArea;
        int left = (int)Math.floor(x - 1.0F);
        int top = (int)Math.floor(y - 1.0F);
// https://t.me/pasterplace

        int right = (int)Math.ceil(width + 2.0F) + 1;

        ScreenRect transformedBounds = new ScreenRect(left, top, right, (int)Math.ceil(height + 2.0F) + 1).transform(transformationMatrix);
        ScreenRect clippedBounds = scissorArea != null ? transformedBounds.intersection(scissorArea) : transformedBounds;
        this.bounds = clippedBounds != null ? clippedBounds : ScreenRect.empty();
    }
// https://t.me/pasterplace

    public void setupVertices(VertexConsumer vertices) {
        float uExtent = this.width * 0.5F + 1.0F;
        float vExtent = this.height * 0.5F + 1.0F;
        float left = this.x - 1.0F;
        float top = this.y - 1.0F;
        float right = this.x + this.width + 1.0F;
        float bottom = this.y + this.height + 1.0F;
        this.emitVertex(vertices, left, top, -uExtent, -vExtent, this.topLeftColor);
        this.emitVertex(vertices, left, bottom, -uExtent, vExtent, this.bottomLeftColor);
        this.emitVertex(vertices, right, bottom, uExtent, vExtent, this.bottomRightColor);
        this.emitVertex(vertices, right, top, uExtent, -vExtent, this.topRightColor);
    }
// https://t.me/pasterplace

    private void emitVertex(VertexConsumer vertices, float x, float y, float u, float v, int color) {
        vertices.vertex(this.transformationMatrix, x, y)
        .texture(u, v)
        .light(this.blockLight, this.skyLight)
        .color(color);
    }
// https://t.me/pasterplace

    public RenderPipeline pipeline() {
        return GlassPanelRenderPipeline.l1I1IlllIlI;
    }
// https://t.me/pasterplace

    public TextureSetup textureSetup() {
        if (this.cachedTextureSetup == null) {
            KawaseGaussianBlurPass intermediateBlurPass = new KawaseGaussianBlurPass(KawaseGaussianBlurMode.KAWASE, 1);
            InSampler.enqueueBlurPass(this.blurPass);
            InSampler.enqueueBlurPass(intermediateBlurPass);
            GpuTextureView gpuTextureView = InSampler.getBlurTexture(this.blurPass);
            if (gpuTextureView == null) {
                return TextureSetup.empty();
            }

            GpuTextureView gpuTextureView2 = InSampler.getBlurTexture(intermediateBlurPass);
            if (gpuTextureView2 == null) {
                gpuTextureView2 = gpuTextureView;
            }

            GpuSampler gpuSampler = RenderSystem.getSamplerCache().get(FilterMode.LINEAR);
            this.cachedTextureSetup = TextureSetup.of(gpuTextureView, gpuSampler, gpuTextureView2, gpuSampler);
        }

        return this.cachedTextureSetup;
    }
// https://t.me/pasterplace

    public @Nullable ScreenRect scissorArea() {
        return this.scissorArea;
    }
// https://t.me/pasterplace

    public @Nullable ScreenRect bounds() {
        return this.bounds;
    }
}

