package dev.daamky;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public record HudRenderContext(
    DrawContext drawContext,
    MinecraftClient client,
    boolean chatScreenOpen,
    List<Runnable> postRenderCallbacks,
    float elapsedTime
) {}
