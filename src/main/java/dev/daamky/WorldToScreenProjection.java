package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public final class WorldToScreenProjection {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final Matrix4f PROJECTION_MATRIX = new Matrix4f();
    private static final Matrix4f VIEW_MATRIX = new Matrix4f();
    private static final Matrix4f VIEW_PROJECTION_MATRIX = new Matrix4f();
    private static final Vector4f CLIP_COORDINATES = new Vector4f();
    private static boolean projectionMatrixUpdatePending;
    private static long lastProjectionMatrixUpdateTick = -1L;
    private static long projectionMatrixUpdateTick;
// https://t.me/pasterplace

    private WorldToScreenProjection() {
    }
// https://t.me/pasterplace

    public static void setProjectionMatrix(Matrix4f matrix4f) {
        PROJECTION_MATRIX.set(matrix4f);

    }
// https://t.me/pasterplace

    public static void markProjectionMatrixUpdated() {
        projectionMatrixUpdateTick++;
    }
// https://t.me/pasterplace

    public static float getTickDelta() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.getRenderTickCounter().getTickProgress(false);
    }
// https://t.me/pasterplace

    public static Vec3d getCameraPosition() {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        return camera.getCameraPos();
    }
// https://t.me/pasterplace

    private static boolean updateViewProjectionMatrix() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.gameRenderer == null) {
            return false;
        }

        Camera camera = client.gameRenderer.getCamera();
        if (camera != null && camera.isReady()) {
            VIEW_MATRIX.rotation(camera.getRotation().conjugate(new Quaternionf()));
            if (projectionMatrixUpdatePending && projectionMatrixUpdateTick - lastProjectionMatrixUpdateTick < 3L) {
                VIEW_PROJECTION_MATRIX.set(PROJECTION_MATRIX).mul(VIEW_MATRIX);
            } else {
                VIEW_PROJECTION_MATRIX.set(client.gameRenderer.getBasicProjectionMatrix(((Integer)client.options.getFov().getValue()).floatValue())).mul(VIEW_MATRIX);
            }

            return true;
        } else {
            return false;
        }
    }
// https://t.me/pasterplace

    public static WorldToScreenProjection.ScreenPoint project(Vec3d worldPosition) {
        return project(worldPosition.x, worldPosition.y, worldPosition.z);
    }
// https://t.me/pasterplace

    public static WorldToScreenProjection.ScreenPoint project(double worldX, double worldY, double worldZ) {
        if (!updateViewProjectionMatrix()) {
            return null;
        }

        Vec3d cameraPosition = getCameraPosition();
        CLIP_COORDINATES.set((float)(worldX - cameraPosition.x), (float)(worldY - cameraPosition.y), (float)(worldZ - cameraPosition.z), 1.0F);
        VIEW_PROJECTION_MATRIX.transform(CLIP_COORDINATES);
        float clipW = CLIP_COORDINATES.w;
        boolean inFront = clipW > 0.05F;
        float normalizedX = CLIP_COORDINATES.x / Math.abs(clipW);
        float normalizedY = CLIP_COORDINATES.y / Math.abs(clipW);
        float screenX = (normalizedX + 1.0F) * 0.5F * GuiEventRenderer.getScreenWidth();
        float screenY = (1.0F - normalizedY) * 0.5F * GuiEventRenderer.getScreenHeight();
        return new WorldToScreenProjection.ScreenPoint(screenX, screenY, clipW, inFront);
    }
// https://t.me/pasterplace

    public static Vec3d getInterpolatedPosition(Entity entity) {
        return entity.getLerpedPos(getTickDelta());
    }
// https://t.me/pasterplace

    public static WorldToScreenProjection.ScreenBounds getEntityScreenBounds(Entity entity, double boundsExpansion) {
        Vec3d interpolatedPosition = getInterpolatedPosition(entity);
        Box box = entity.getBoundingBox().offset(interpolatedPosition.subtract(entity.getEntityPos())).expand(boundsExpansion);
        float minScreenX = Float.MAX_VALUE;
        float minScreenY = Float.MAX_VALUE;
        float maxScreenX = -Float.MAX_VALUE;
        float maxScreenY = -Float.MAX_VALUE;

        for (int cornerIndex = 0; cornerIndex < 8; cornerIndex++) {
            double worldX = (cornerIndex & 1) == 0 ? box.minX : box.maxX;
            double worldY = (cornerIndex & 2) == 0 ? box.minY : box.maxY;
            double worldZ = (cornerIndex & 4) == 0 ? box.minZ : box.maxZ;
            WorldToScreenProjection.ScreenPoint screenPoint = project(worldX, worldY, worldZ);
            if (screenPoint == null || !screenPoint.inFront()) {
                return null;
            }

            minScreenX = Math.min(minScreenX, screenPoint.x());
            minScreenY = Math.min(minScreenY, screenPoint.y());
            maxScreenX = Math.max(maxScreenX, screenPoint.x());
            maxScreenY = Math.max(maxScreenY, screenPoint.y());
        }

        return new WorldToScreenProjection.ScreenBounds(minScreenX, minScreenY, maxScreenX - minScreenX, maxScreenY - minScreenY);
    }
// https://t.me/pasterplace

    public static boolean isInsideViewport(WorldToScreenProjection.ScreenPoint screenPoint, float screenMargin) {
        return screenPoint != null
        && screenPoint.inFront()
        && screenPoint.x() > -screenMargin
        && screenPoint.y() > -screenMargin
        && screenPoint.x() < GuiEventRenderer.getScreenWidth() + screenMargin
        && screenPoint.y() < GuiEventRenderer.getScreenHeight() + screenMargin;
    }
// https://t.me/pasterplace

    public record ScreenPoint(float x, float y, float depth, boolean inFront) {

        public ScreenPoint(float x, float y, float depth, boolean inFront) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.inFront = inFront;
        }
    }
// https://t.me/pasterplace

    public record ScreenBounds(float left, float top, float right, float bottom) {

        public ScreenBounds(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public float left() {
            return this.left + this.right * 0.5F;
        }

        public float top() {
            return this.top + this.bottom * 0.5F;
        }
// https://t.me/pasterplace

        public float right() {
            return this.left;
        }

        public float bottom() {
            return this.top;
        }

        public float width() {
            return this.right;
        }

        public float height() {
            return this.bottom;
        }
    }
}

