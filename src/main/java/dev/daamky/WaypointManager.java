package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventHudLayer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/** Stores and renders player waypoint markers. */
public final class WaypointManager {
    private static final WaypointManager INSTANCE = new WaypointManager();
    private static final MsdfFont FONT = Medium.DEFAULT_FONT;
    private static final ItemStack COMPASS_ICON = new ItemStack(Items.COMPASS);
    private static final float TEXT_SIZE = 5.8f;
    private static final float LABEL_HEIGHT = 11.8f;

    private final List<Waypoint> waypoints = new CopyOnWriteArrayList<>();

    private WaypointManager() {
    }

    public static WaypointManager getInstance() {
        return INSTANCE;
    }

    public List<Waypoint> waypoints() {
        return this.waypoints;
    }

    public void addWaypoint(Waypoint waypoint) {
        this.waypoints.removeIf(existing -> existing.x() == waypoint.x()
            && existing.y() == waypoint.y() && existing.z() == waypoint.z());
        this.waypoints.add(waypoint);
    }

    public boolean removeWaypoint(String name) {
        return this.waypoints.removeIf(waypoint -> waypoint.name().equalsIgnoreCase(name));
    }

    public void clearWaypoints() {
        this.waypoints.clear();
    }

    @EventTarget
    public void renderWaypoints(EventHudLayer event) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (this.waypoints.isEmpty() || client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }

        DrawContext context = event.graphics();
        List<WaypointLabel> labels = this.waypoints.stream()
            .map(waypoint -> projectWaypoint(client, waypoint))
            .filter(label -> label != null)
            .toList();
        for (WaypointLabel label : labels) {
            renderLabel(context, label, true);
        }
        for (WaypointLabel label : labels) {
            renderLabel(context, label, false);
        }
    }

    private static WaypointLabel projectWaypoint(MinecraftClient client, Waypoint waypoint) {
        Vec3d worldPosition = new Vec3d(waypoint.x() + 0.5, waypoint.y() + 1.0, waypoint.z() + 0.5);
        WorldToScreenProjection.ScreenPoint screen = WorldToScreenProjection.project(worldPosition);
        if (!WorldToScreenProjection.isInsideViewport(screen, 50.0f)) {
            return null;
        }

        double distance = client.player.getEntityPos().distanceTo(worldPosition);
        float scale = (float)Math.max(0.75, Math.min(1.0, 1.0 - distance / 150.0));
        return new WaypointLabel(waypoint.name(), Math.round(distance) + "м",
            screen.x(), screen.y(), scale);
    }

    private static void renderLabel(DrawContext context, WaypointLabel label, boolean backgroundOnly) {
        float nameWidth = FONT.measureWidth(label.name(), TEXT_SIZE);
        float distanceWidth = FONT.measureWidth(label.distance(), TEXT_SIZE);
        float width = 15.5f + nameWidth + 3.5f + 2.0f + 3.5f + distanceWidth + 4.0f;
        float left = label.screenX() - width / 2.0f;
        float top = label.screenY() - LABEL_HEIGHT;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(label.screenX(), label.screenY());
        context.getMatrices().scale(label.scale());
        context.getMatrices().translate(-label.screenX(), -label.screenY());
        if (backgroundOnly) {
            BrokkeIcons.drawRoundedPanelBackground(context, left, top, width, LABEL_HEIGHT, 3.5f, 1.0f);
            context.getMatrices().popMatrix();
            return;
        }

        float iconX = left + 8.0f;
        float centerY = top + LABEL_HEIGHT / 2.0f;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(iconX, centerY);
        context.getMatrices().scale(0.5f);
        context.getMatrices().translate(-8.0f, -8.0f);
        GuiEventRenderer.flushBatchedElements();
        context.drawItem(COMPASS_ICON, 0, 0);
        context.getMatrices().popMatrix();

        float textY = top + (LABEL_HEIGHT - FONT.metrics().lineHeight() * TEXT_SIZE) / 2.0f;
        float textX = left + 4.0f + 8.0f + 3.5f;
        GuiTextBuilder.createAt(FONT, label.name(), textX, textY, TEXT_SIZE)
            .color(Accent.getTextColorWithAlpha(250).toArgb())
            .render(context);
        RoundedGradientRectBuilder.at(textX + nameWidth + 3.5f, top + (LABEL_HEIGHT - 2.0f) / 2.0f, 2.0f, 2.0f)
            .cornerRadius(1.0f)
            .color(Accent.getAccentColorWithAlpha(110))
            .render(context);
        GuiTextBuilder.createAt(FONT, label.distance(), textX + nameWidth + 5.5f, textY, TEXT_SIZE)
            .color(Accent.getAccentColorWithAlpha(255).toArgb())
            .render(context);
        context.getMatrices().popMatrix();
    }

    public record Waypoint(String name, int x, int y, int z) {
    }

    private record WaypointLabel(String name, String distance, float screenX, float screenY, float scale) {
    }
}
