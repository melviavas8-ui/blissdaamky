package dev.daamky;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import org.jspecify.annotations.Nullable;

public record EspPreviewRenderState(
    float yawOffset,
    float pitchOffset,
    float verticalOffset,
    int left,
    int top,
    int right,
    int bottom,
    float scale,
    @Nullable ScreenRect screenRect,
    @Nullable ScreenRect bounds
) implements SpecialGuiElementRenderState {
    public EspPreviewRenderState(
        float yawOffset,
        float pitchOffset,
        float verticalOffset,
        int left,
        int top,
        int right,
        int bottom,
        float scale,
        @Nullable ScreenRect screenRect
    ) {
        this(
            yawOffset,
            pitchOffset,
            verticalOffset,
            left,
            top,
            right,
            bottom,
            scale,
            screenRect,
            SpecialGuiElementRenderState.createBounds(left, top, right, bottom, screenRect)
        );
    }

    @Override
    public int x1() {
        return this.left;
    }

    @Override
    public int y1() {
        return this.top;
    }

    @Override
    public int x2() {
        return this.right;
    }

    @Override
    public int y2() {
        return this.bottom;
    }

    @Override
    public @Nullable ScreenRect scissorArea() {
        return this.screenRect;
    }
}
