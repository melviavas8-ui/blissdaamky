package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting.Type;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.command.RenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public final class DaamkyEspPreview extends SpecialGuiElementRenderer<EspPreviewRenderState> {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final double DROPPED_ITEM_OFFSET_X = 0.85;
    private static final double DROPPED_ITEM_OFFSET_Z = 0.35;
    private static volatile float projectedHeadX = 0.5F;
    private static volatile float projectedHeadY = 0.1F;
    private static volatile boolean previewActive = false;
    private final EntityRenderManager entityRenderManager;
// https://t.me/pasterplace

    public DaamkyEspPreview(Immediate immediate, EntityRenderManager entityRenderManager) {
        super(immediate);
        this.entityRenderManager = entityRenderManager;
    }
// https://t.me/pasterplace

    public static boolean isPreviewActive() {
        return previewActive;
    }
// https://t.me/pasterplace

    public static float getProjectedHeadX() {
        return projectedHeadX;
    }
// https://t.me/pasterplace

    public static float getProjectedHeadY() {
        return projectedHeadY;
    }
// https://t.me/pasterplace

    public Class<EspPreviewRenderState> getElementClass() {
        return EspPreviewRenderState.class;
    }
// https://t.me/pasterplace

    protected float getYOffset(int previewWidth, int previewHeight) {
        return previewWidth / 2.0F;
    }
// https://t.me/pasterplace

    protected String getName() {
        return "daamky esp preview";
    }
// https://t.me/pasterplace

    @Override
    public void render(EspPreviewRenderState previewState, MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        RenderDispatcher renderDispatcher = client.gameRenderer.getEntityRenderDispatcher();
        OrderedRenderCommandQueueImpl orderedRenderCommandQueueImpl = renderDispatcher.getQueue();
        ClientPlayerEntity clientPlayerEntity = client.player;
        if (clientPlayerEntity == null) {
// https://t.me/pasterplace

        } else {
            float renderTickDelta = client.getRenderTickCounter().getTickProgress(false);
            float bodyYaw = clientPlayerEntity.bodyYaw;
            float previousBodyYaw = clientPlayerEntity.lastBodyYaw;
            float headYaw = clientPlayerEntity.headYaw;
            float previousHeadYaw = clientPlayerEntity.lastHeadYaw;
            float playerYaw = clientPlayerEntity.getYaw();
            float previousPlayerYaw = clientPlayerEntity.lastYaw;
            float playerPitch = clientPlayerEntity.getPitch();
            float previousPlayerPitch = clientPlayerEntity.lastPitch;
            clientPlayerEntity.bodyYaw = 0.0F;
            clientPlayerEntity.lastBodyYaw = 0.0F;
            clientPlayerEntity.headYaw = 0.0F;
            clientPlayerEntity.lastHeadYaw = 0.0F;
            clientPlayerEntity.setYaw(0.0F);
            clientPlayerEntity.lastYaw = 0.0F;
            clientPlayerEntity.setPitch(0.0F);
            clientPlayerEntity.lastPitch = 0.0F;

            try {
                this.renderPlayerPreview(previewState, matrices, orderedRenderCommandQueueImpl, renderDispatcher, client, clientPlayerEntity, renderTickDelta);
            } finally {
                clientPlayerEntity.bodyYaw = bodyYaw;
                clientPlayerEntity.lastBodyYaw = previousBodyYaw;
                clientPlayerEntity.headYaw = headYaw;
                clientPlayerEntity.lastHeadYaw = previousHeadYaw;
                clientPlayerEntity.setYaw(playerYaw);
                clientPlayerEntity.lastYaw = previousPlayerYaw;
                clientPlayerEntity.setPitch(playerPitch);
                clientPlayerEntity.lastPitch = previousPlayerPitch;
            }
        }
    }
// https://t.me/pasterplace

    private void renderPlayerPreview(
    EspPreviewRenderState previewState,
    MatrixStack matrices,
    OrderedRenderCommandQueue orderedRenderCommandQueue,
    RenderDispatcher renderDispatcher,
    MinecraftClient client,
    ClientPlayerEntity clientPlayerEntity,
    float renderTickDelta
    ) {
        client.gameRenderer.getDiffuseLighting().setShaderLights(Type.ENTITY_IN_UI);
        EntityRenderer entityRenderer = this.entityRenderManager.getRenderer(clientPlayerEntity);
        EntityRenderState entityRenderState = entityRenderer.getAndUpdateRenderState(clientPlayerEntity, renderTickDelta);
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        entityRenderState.light = 15728880;
        entityRenderState.displayName = null;
        if (entityRenderState instanceof PlayerEntityRenderState) {
            PlayerEntityRenderState entityRenderState2 = (PlayerEntityRenderState)entityRenderState;
            entityRenderState2.playerName = null;
        }

        float interpolatedBodyYaw = MathHelper.lerpAngleDegrees(renderTickDelta, clientPlayerEntity.lastBodyYaw, clientPlayerEntity.bodyYaw);
        if (entityRenderState instanceof LivingEntityRenderState) {
            LivingEntityRenderState entityRenderState3 = (LivingEntityRenderState)entityRenderState;
            interpolatedBodyYaw = entityRenderState3.bodyYaw;
        }

        matrices.push();
        matrices.translate(0.0F, previewState.verticalOffset(), 0.0F);
        matrices.multiply(new Quaternionf().rotateZ((float) Math.PI));
        matrices.multiply(new Quaternionf().rotateX(previewState.pitchOffset() * (float) (Math.PI / 180.0)));
        matrices.multiply(new Quaternionf().rotateY((interpolatedBodyYaw - 180.0F + previewState.yawOffset()) * (float) (Math.PI / 180.0)));
        Matrix4f matrix4f = new Matrix4f(matrices.peek().getPositionMatrix());
        CameraRenderState cameraRenderState = new CameraRenderState();
        this.entityRenderManager.render(entityRenderState, cameraRenderState, 0.0, 0.0, 0.0, matrices, orderedRenderCommandQueue);
        ESP esp = (ESP)ModuleRegistry.getInstance().getModuleByClass(ESP.class);
        if (esp != null) {
            esp.renderDroppedItemModel(matrices, orderedRenderCommandQueue, 0.85, 0.0, 0.35);
        }

        matrices.pop();
        renderDispatcher.render();
        Vec3d interpolatedPlayerPosition = DaamkyWorldRender.getInterpolatedPosition(clientPlayerEntity, renderTickDelta);
        DaamkyWorldRender.updateCameraState(interpolatedPlayerPosition, interpolatedBodyYaw + 180.0F - previewState.yawOffset(), previewState.pitchOffset(), renderTickDelta);
        DaamkyWorldRender.setLineWidthMultiplier(3.0F);

        try {
            ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();
            ESP esp2 = (ESP)moduleRegistry.getModuleByClass(ESP.class);
            if (esp2 != null) {
                esp2.renderEntity(clientPlayerEntity);
                esp2.renderDroppedItemMarker(interpolatedPlayerPosition.add(0.85, 0.0, 0.35));
            }

            ChinaHat chinaHat = (ChinaHat)moduleRegistry.getModuleByClass(ChinaHat.class);
            if (chinaHat != null && chinaHat.isEnabled()) {
                chinaHat.renderForPlayer(clientPlayerEntity);
            }
        } finally {
            DaamkyWorldRender.flushBufferedGeometryToOutputOverride(matrix4f);
        }

        int guiScale = client.getWindow().getScaleFactor();
        float previewPixelWidth = (previewState.x2() - previewState.x1()) * guiScale;
        float previewPixelHeight = (previewState.y2() - previewState.y1()) * guiScale;
        Vector4f vector4f = matrix4f.transform(new Vector4f(0.0F, clientPlayerEntity.getHeight() + 0.15F, 0.0F, 1.0F));
        projectedHeadX = vector4f.x / previewPixelWidth;
        projectedHeadY = vector4f.y / previewPixelHeight;

    }
// https://t.me/pasterplace

}

