package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

public final class PlayerSkinQuadRenderState implements SimpleGuiElementRenderState {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private final Matrix3x2f matrices;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final int packedTopCornerRadii;
    private final int packedBottomCornerRadii;
    private final int topLeftColor;
    private final int topRightColor;
    private final int bottomRightColor;
    private final int bottomLeftColor;
    private final Identifier textureId;
    private final boolean linearTextureFiltering;
    private final @Nullable ScreenRect scissorArea;
    private final ScreenRect transformedBounds;
    private @Nullable TextureSetup textureSetup;
// https://t.me/pasterplace

    PlayerSkinQuadRenderState(
    Matrix3x2f matrices,
    float x,
    float y,
    float width,
    float height,
    int packedTopCornerRadii,
    int packedBottomCornerRadii,
    int topLeftColor,
    int topRightColor,
    int bottomRightColor,
    int bottomLeftColor,
    Identifier textureId,
    boolean linearTextureFiltering,
    @Nullable ScreenRect scissorArea
    ) {
        this.matrices = matrices;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.packedTopCornerRadii = packedTopCornerRadii;
        this.packedBottomCornerRadii = packedBottomCornerRadii;
        this.topLeftColor = topLeftColor;
        this.topRightColor = topRightColor;
        this.bottomRightColor = bottomRightColor;
        this.bottomLeftColor = bottomLeftColor;
        this.textureId = textureId;
        this.linearTextureFiltering = linearTextureFiltering;
        this.scissorArea = scissorArea;
        int paddedLeftX = (int)Math.floor(x - 1.0F);
        int paddedTopY = (int)Math.floor(y - 1.0F);

        int paddedWidth = (int)Math.ceil(width + 2.0F) + 1;

        ScreenRect transformedBounds = new ScreenRect(paddedLeftX, paddedTopY, paddedWidth, (int)Math.ceil(height + 2.0F) + 1).transform(matrices);
        ScreenRect clippedBounds = scissorArea != null ? transformedBounds.intersection(scissorArea) : transformedBounds;
        this.transformedBounds = clippedBounds != null ? clippedBounds : ScreenRect.empty();
    }
// https://t.me/pasterplace

    public void setupVertices(VertexConsumer vertices) {
        float halfWidthUvRadius = this.width * 0.5F + 1.0F;
        float halfHeightUvRadius = this.height * 0.5F + 1.0F;
        float leftX = this.x - 1.0F;
        float topY = this.y - 1.0F;
        float rightX = this.x + this.width + 1.0F;
        float bottomY = this.y + this.height + 1.0F;
        this.emitVertex(vertices, leftX, topY, -halfWidthUvRadius, -halfHeightUvRadius, this.topLeftColor);
        this.emitVertex(vertices, leftX, bottomY, -halfWidthUvRadius, halfHeightUvRadius, this.bottomLeftColor);
        this.emitVertex(vertices, rightX, bottomY, halfWidthUvRadius, halfHeightUvRadius, this.bottomRightColor);
        this.emitVertex(vertices, rightX, topY, halfWidthUvRadius, -halfHeightUvRadius, this.topRightColor);
    }
// https://t.me/pasterplace

    private void emitVertex(VertexConsumer vertices, float x, float y, float u, float v, int argbColor) {
        vertices.vertex(this.matrices, x, y)
        .texture(u, v)
        .light(this.packedTopCornerRadii, this.packedBottomCornerRadii)
        .color(argbColor);
    }
// https://t.me/pasterplace

    public RenderPipeline pipeline() {
        return PlayerHeadRenderPipeline.l1I1IlllIlI;
    }
// https://t.me/pasterplace

    public TextureSetup textureSetup() {
        if (this.textureSetup == null) {
            GpuTextureView textureView = null;
// https://t.me/pasterplace

            try {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.getTextureManager() != null) {
                    AbstractTexture abstractTexture = client.getTextureManager().getTexture(this.textureId);
                    if (abstractTexture != null) {
                        textureView = abstractTexture.getGlTextureView();
                    }
                }
            } catch (Throwable ignoredFailure) {
            }

            if (textureView == null) {
                return TextureSetup.empty();
            }

            this.textureSetup = TextureSetup.of(
            textureView, RenderSystem.getSamplerCache().get(this.linearTextureFiltering ? FilterMode.LINEAR : FilterMode.NEAREST)
            );
        }

        return this.textureSetup;
    }
// https://t.me/pasterplace

    public @Nullable ScreenRect scissorArea() {
        return this.scissorArea;
    }
// https://t.me/pasterplace

    public @Nullable ScreenRect bounds() {
        return this.transformedBounds;
    }
}

