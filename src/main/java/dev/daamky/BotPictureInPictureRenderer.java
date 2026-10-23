package dev.daamky;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import dev.daamky.mixin.client.MinecraftAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;

public final class BotPictureInPictureRenderer implements DisconnectScreenExtension {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    public static final BotPictureInPictureRenderer INSTANCE = new BotPictureInPictureRenderer();
    public static final Identifier TEXTURE_ID = Identifier.of("daamky", "bot/pip");
    public static volatile boolean renderingBotWorld;
    private static final float PREVIEW_WIDTH = 232.0F;
    private static final float PREVIEW_HEIGHT = 130.5F;
    private final BotPictureInPictureRenderer.FramebufferTexture framebufferTexture = new BotPictureInPictureRenderer.FramebufferTexture();
    private BotSession activeSession;
    private ClientWorld activeWorld;
    private WorldRenderer worldRenderer;
    private SimpleFramebuffer framebuffer;
    private boolean textureRegistered;
    private boolean renderingDisabled;
// https://t.me/pasterplace

    private BotPictureInPictureRenderer() {
    }
// https://t.me/pasterplace

    @Override
    public boolean hasPictureInPicture(BotSessionInfo sessionInfo) {
        return this.activeSession == sessionInfo && this.framebuffer != null && this.textureRegistered;
    }
// https://t.me/pasterplace

    public void renderBotWorldPreview(BotSessionInfo sessionInfo) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!renderingBotWorld && !this.renderingDisabled) {
            if (sessionInfo instanceof BotSession targetSession && targetSession.hasActivePlayerSession()) {
                ClientWorld joined = targetSession.getClientWorld();
                ClientPlayerEntity clientPlayerEntity = targetSession.getSessionPlayer();
                Entity entity = targetSession.getCameraEntity();
                ClientPlayerInteractionManager joined2 = targetSession.getInteractionManager();
                if (joined != null && entity != null && clientPlayerEntity != null) {
                    int windowScaleFactor = (int)Math.max(1.0, client.getWindow().getScaleFactor());
                    int framebufferWidth = Math.max(1, Math.round(232.0F * windowScaleFactor));
                    int framebufferHeight = Math.max(1, Math.round(130.5F * windowScaleFactor));

                    try {
                        this.prepareFramebuffer(client, targetSession, joined, framebufferWidth, framebufferHeight);
                    } catch (Throwable initialRenderFailure) {
                        this.disableAfterRenderFailure(initialRenderFailure);
                        return;
                    }
// https://t.me/pasterplace

                    if (this.worldRenderer != null && this.framebuffer != null) {
                        MinecraftAccessor client2 = (MinecraftAccessor)client;
                        ClientWorld clientWorld = client.world;
                        ClientPlayerEntity clientPlayerEntity2 = client.player;
                        ClientPlayerInteractionManager clientPlayerInteractionManager = client.interactionManager;
                        Entity entity2 = client.getCameraEntity();
                        HitResult hitResult = client.crosshairTarget;
                        Entity entity3 = client.targetedEntity;
                        Framebuffer framebuffer = client.getFramebuffer();
                        WorldRenderer worldRenderer = client.worldRenderer;
                        renderingBotWorld = true;

                        try {
                            client.world = joined;
                            client.player = clientPlayerEntity;
                            client.interactionManager = joined2;
                            client2.daamky$setCameraEntity(entity);
                            client2.daamky$setMainRenderTarget(this.framebuffer);
                            client2.daamky$setLevelRenderer(this.worldRenderer);
                            RenderSystem.getDevice()
                            .createCommandEncoder()
                            .clearColorAndDepthTextures(this.framebuffer.getColorAttachment(), 0, this.framebuffer.getDepthAttachment(), 1.0);
                            client.gameRenderer.updateCamera(client.getRenderTickCounter());
                            this.worldRenderer.tick(client.gameRenderer.getCamera());
                            client.gameRenderer.renderWorld(client.getRenderTickCounter());
                        } catch (Throwable worldRenderFailure) {
                            this.disableAfterRenderFailure(worldRenderFailure);
                        } finally {
                            client2.daamky$setLevelRenderer(worldRenderer);
                            client2.daamky$setMainRenderTarget(framebuffer);
                            client.world = clientWorld;
                            client.player = clientPlayerEntity2;
                            client.interactionManager = clientPlayerInteractionManager;
                            client2.daamky$setCameraEntity(entity2);
                            client.crosshairTarget = hitResult;
                            client.targetedEntity = entity3;
                            if (clientWorld != null && clientPlayerEntity2 != null) {
                                client.gameRenderer.updateCamera(client.getRenderTickCounter());
                            }

                            renderingBotWorld = false;
                        }
                    }
                }
            } else {
                if (this.activeSession != null) {
                    this.closePictureInPicture();
                }
            }
        }
    }
// https://t.me/pasterplace

    public void renderScreenPreview(
    BotSessionInfo lI1ll11IlIl1II1lIlll1I, DrawContext drawContext, float x, float y, float width, float height
    ) {
        if (lI1ll11IlIl1II1lIlll1I instanceof BotSession botSession) {
            Screen screen = botSession.getCurrentScreen();
            if (screen != null) {
                botSession.runOnSessionThread(() -> this.renderScreenContents(screen, drawContext, x, y, width, height));
            }
        }
    }
// https://t.me/pasterplace

    private void prepareFramebuffer(MinecraftClient client, BotSession disconnect, ClientWorld clientWorld, int framebufferWidth, int framebufferHeight) {
        boolean framebufferChanged = this.framebuffer == null || this.framebuffer.textureWidth != framebufferWidth || this.framebuffer.textureHeight != framebufferHeight;
        if (framebufferChanged) {
            if (this.framebuffer != null) {
                this.framebuffer.delete();
            }

            this.framebuffer = new SimpleFramebuffer("Daamky Bot PiP", framebufferWidth, framebufferHeight, true);
            this.framebufferTexture.attachFramebuffer(this.framebuffer);
            if (!this.textureRegistered) {
                client.getTextureManager().registerTexture(TEXTURE_ID, this.framebufferTexture);
                this.textureRegistered = true;
            }
        }

        boolean worldChanged = disconnect != this.activeSession || clientWorld != this.activeWorld || this.worldRenderer == null;
        if (this.worldRenderer == null) {
            this.worldRenderer = new WorldRenderer(
            client,
            client.getEntityRenderDispatcher(),
            client.getBlockEntityRenderDispatcher(),
            new BufferBuilderStorage(Math.max(1, 0)),
            client.gameRenderer.getEntityRenderStates(),
            client.gameRenderer.getEntityRenderDispatcher()
            );
            this.worldRenderer.reload(client.getResourceManager());
        }

        if (worldChanged) {
            this.worldRenderer.setWorld(clientWorld);
            this.activeSession = disconnect;
            this.activeWorld = clientWorld;
        }

        if (framebufferChanged || worldChanged) {
            this.worldRenderer.onResized(framebufferWidth, framebufferHeight);
        }
    }
// https://t.me/pasterplace

    private void renderScreenContents(Screen screen, DrawContext drawContext, float x, float y, float width, float height) {
        MinecraftClient client = MinecraftClient.getInstance();
        float screenWidth = client.getWindow().getScaledWidth();
        float screenHeight = client.getWindow().getScaledHeight();
        float scale = Math.min(width / screenWidth, height / screenHeight);
        float offsetX = x + (width - screenWidth * scale) * 0.5F;
        float offsetY = y + (height - screenHeight * scale) * 0.5F;
        drawContext.enableScissor((int)x, (int)y, (int)Math.ceil(x + width), (int)Math.ceil(y + height));
        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().translate(offsetX, offsetY);
        drawContext.getMatrices().scale(scale, scale);

        try {
            screen.render(drawContext, -10000, -10000, client.getRenderTickCounter().getTickProgress(false));
        } catch (Throwable ignoredScreenRenderFailure) {
        } finally {
            drawContext.getMatrices().popMatrix();
            drawContext.disableScissor();
        }
    }
// https://t.me/pasterplace

    private void disableAfterRenderFailure(Throwable throwable) {
        if (!this.renderingDisabled) {
            this.renderingDisabled = true;
            System.err.println("[BotViewRenderer] render failed, PiP disabled:");
            throwable.printStackTrace();
        }
    }
// https://t.me/pasterplace

    @Override
    public void closePictureInPicture() {
        if (this.worldRenderer != null) {
            try {
                this.worldRenderer.setWorld(null);
                this.worldRenderer.close();
            } catch (Throwable ignoredWorldRendererCloseFailure) {
            }

            this.worldRenderer = null;
        }

        if (this.textureRegistered) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(TEXTURE_ID);
            this.textureRegistered = false;
        }

        if (this.framebuffer != null) {
            this.framebuffer.delete();
            this.framebuffer = null;
        }

        this.activeSession = null;
        this.activeWorld = null;
    }
// https://t.me/pasterplace

    private static final class FramebufferTexture extends AbstractTexture {
        FramebufferTexture() {
            this.sampler = RenderSystem.getSamplerCache()
            .get(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
        }

        void attachFramebuffer(Framebuffer framebuffer) {
            this.glTexture = framebuffer.getColorAttachment();
            this.glTextureView = framebuffer.getColorAttachmentView();
        }

        public void close() {
            this.glTexture = null;
            this.glTextureView = null;
        }
    }
}

