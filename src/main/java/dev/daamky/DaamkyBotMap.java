package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventDisplay;
import java.util.List;
import java.util.Locale;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.MapColor.Brightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

public final class DaamkyBotMap {
    private static final DaamkyBotMap INSTANCE = new DaamkyBotMap();
    private static final int MAP_SIZE = 96;
    private static final Identifier MAP_TEXTURE_ID = Identifier.of("daamky", "bot/map");
    private NativeImageBackedTexture mapTexture;
    private long lastMapRefreshMillis;
    private float fadeOpacity;

    private DaamkyBotMap() {
    }

    public static DaamkyBotMap getInstance() {
        return INSTANCE;
    }

    private static boolean shouldRenderMap() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || Search.getInstance().shouldRender()) {
            return false;
        } else {
            return AutoQuest.isPvpQuestActive() && AutoQuest.getActiveQuestBot() != null
            ? true
            : SavedBots.getSessionRegistry().isPanelOpen() && !SavedBots.getSessionRegistry().getSessions().isEmpty();
        }
    }

    @EventTarget
    public void onDisplay(EventDisplay eventDisplay) {
        float targetOpacity = shouldRenderMap() ? 1.0F : 0.0F;
        this.fadeOpacity = this.fadeOpacity + (targetOpacity - this.fadeOpacity) * 0.18F;
        if (this.fadeOpacity < 0.01F) {
            this.fadeOpacity = 0.0F;
        } else {
            BotSession activeBot = AutoQuest.isPvpQuestActive() ? AutoQuest.getActiveQuestBot() : null;
            List savedBots = SavedBots.getSessionRegistry().getSessions();
            if (activeBot != null || !savedBots.isEmpty()) {
                this.renderBotCard(eventDisplay.graphics(), activeBot != null ? activeBot : (BotSession)savedBots.get(0), this.fadeOpacity);
            }
        }
    }

    private void renderBotCard(DrawContext drawContext, BotSession disconnect, float opacity) {
        float screenWidth = GuiEventRenderer.getScreenWidth();
        float screenHeight = GuiEventRenderer.getScreenHeight();
        float cardWidth = Math.min(screenWidth * 0.42F, 230.0F);
        float cardHeight = screenHeight - 16.0F;
        float cardLeft = screenWidth - cardWidth - 8.0F;
        float accentColor = Accent.getInstance().getCornerRadius();
        KawaseBlurBuilder.create(cardLeft, 8.0F, cardWidth, cardHeight)
        .cornerRadius(accentColor)
        .blurMode(KawaseGaussianBlurMode.KAWASE)
        .blurPasses((int)Accent.getInstance().getBlurRadius())
        .color(Accent.getTintedBackgroundColor((int)(190.0F * opacity)))
        .opacity(opacity)
        .render(drawContext);
        RoundedGradientRectBuilder.at(cardLeft, 8.0F, cardWidth, cardHeight)
        .cornerRadius(accentColor)
        .color(Accent.getAccentBackgroundColor((int)(30.0F * opacity)))
        .render(drawContext);
        RoundedRectBuilder.createAt(cardLeft, 8.0F, cardWidth, cardHeight)
        .cornerRadius(accentColor)
        .borderWidth(0.85F)
        .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)(70.0F * opacity)))
        .alpha(opacity)
        .render(drawContext);
        float contentLeft = cardLeft + 6.0F;
        GuiTextBuilder.createAt(Medium.ICON_FONT, "G", contentLeft + 3.0F, 18.0F, 6.0F)
        .centered()
        .color(Accent.getAccentColorWithAlpha((int)(235.0F * opacity)))
        .render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, disconnect.getUsername(), contentLeft + 9.0F, 18.0F, 6.4F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getTextColorWithAlpha((int)(250.0F * opacity)))
        .render(drawContext);
        String connectionStateText = disconnect.getDetailedStatus();
        float connectionStateWidth = Medium.DEFAULT_FONT.measureWidth(connectionStateText, 4.6F);
        int connectionStateColor = disconnect.isConfiguredLobbyReady()
        ? RgbaColor.rgba(90, 220, 120, (int)(255.0F * opacity)).toArgb()
        : (
        disconnect.isOfflineOrDisconnected()
        ? RgbaColor.rgba(235, 90, 90, (int)(255.0F * opacity)).toArgb()
        : RgbaColor.rgba(240, 200, 80, (int)(255.0F * opacity)).toArgb()
        );
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, connectionStateText, cardLeft + cardWidth - 6.0F - connectionStateWidth, 18.0F, 4.6F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(connectionStateColor)
        .render(drawContext);
        ClientPlayerEntity clientPlayerEntity = disconnect.getPlayer();
        ClientWorld clientWorld = disconnect.getSessionWorld();
        String statusDetailsText;
        if (clientPlayerEntity != null) {
            statusDetailsText = String.format(
            Locale.ROOT,
            "HP %.0f  |  Еда %d  |  %d %d %d",
            clientPlayerEntity.getHealth(),
            clientPlayerEntity.getHungerManager().getFoodLevel(),
            clientPlayerEntity.getBlockX(),
            clientPlayerEntity.getBlockY(),
            clientPlayerEntity.getBlockZ()
            );
        } else if (disconnect.isOfflineOrDisconnected() && !disconnect.getLastDisconnectReason().isEmpty()) {
            statusDetailsText = disconnect.getLastDisconnectReason();
        } else {
            statusDetailsText = "Мир бота ещё не загружен";
        }

        GuiTextBuilder.createAt(
        Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(statusDetailsText, 4.4F, cardWidth - 12.0F), contentLeft, 28.0F, 4.4F
        )
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(220.0F * opacity)))
        .render(drawContext);

        float mapSize = Math.min(cardWidth - 12.0F, cardHeight * 0.48F);
        float mapLeft = cardLeft + (cardWidth - mapSize) * 0.5F;
        RoundedGradientRectBuilder.at(mapLeft, 35.0F, mapSize, mapSize)
        .cornerRadius(accentColor * 0.5F)
        .color(RgbaColor.rgba(0, 0, 0, (int)(90.0F * opacity)))
        .render(drawContext);
        if (clientPlayerEntity != null && clientWorld != null) {
            this.updateMapTexture(clientWorld, clientPlayerEntity);
            if (this.mapTexture != null) {
                TextureQuadBuilder.at(MAP_TEXTURE_ID, mapLeft, 35.0F, mapSize, mapSize).opacity(opacity).render(drawContext);
            }

            this.renderWorldMarkers(drawContext, clientWorld, clientPlayerEntity, mapLeft, 35.0F, mapSize, opacity);
        } else {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, disconnect.getDetailedStatus(), mapLeft + mapSize * 0.5F, 35.0F + mapSize * 0.5F, 5.0F)
            .centered()
            .color(Accent.getDeepTextColor((int)(180.0F * opacity)))
            .render(drawContext);
        }

        RoundedRectBuilder.createAt(mapLeft, 35.0F, mapSize, mapSize)
        .cornerRadius(accentColor * 0.5F)
        .borderWidth(0.6F)
        .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)(40.0F * opacity)))
        .alpha(opacity)
        .render(drawContext);
        float hotbarTop = 35.0F + mapSize + 5.0F;

        float slotSize = Math.min((cardWidth - 12.0F) / 9.0F, 14.0F);
        float hotbarLeft = cardLeft + (cardWidth - slotSize * 9.0F) * 0.5F;

        for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
            float slotLeft = hotbarLeft + slotIndex * slotSize;
            boolean isSelectedSlot = clientPlayerEntity != null && clientPlayerEntity.getInventory().getSelectedSlot() == slotIndex;
            RoundedGradientRectBuilder.at(slotLeft + 0.5F, hotbarTop, slotSize - 1.0F, slotSize - 1.0F)
            .cornerRadius(2.0F)
            .color(
            isSelectedSlot ? Accent.getAccentColorWithAlpha((int)(90.0F * opacity)).toArgb() : RgbaColor.rgba(0, 0, 0, (int)(70.0F * opacity)).toArgb()
            )
            .render(drawContext);
            if (clientPlayerEntity != null && opacity > 0.4F) {
                BrokkeIcons.drawItemStack(drawContext, clientPlayerEntity.getInventory().getStack(slotIndex), slotLeft + 1.5F, hotbarTop + 1.0F, slotSize - 3.0F);
            }
        }

        float chatTop = hotbarTop + slotSize + 5.0F;
        float chatBottom = 8.0F + cardHeight - 6.0F;

        int visibleChatLineCount = Math.max(0, (int)((chatBottom - chatTop) / 6.6F));
        List<String> chatLines = disconnect.getFormattedSessionHistory();
        int firstVisibleChatLine = Math.max(0, chatLines.size() - visibleChatLineCount);
        float chatLineY = chatBottom - (chatLines.size() - firstVisibleChatLine) * 6.6F + 3.3F;

        for (int chatLineIndex = firstVisibleChatLine; chatLineIndex < chatLines.size(); chatLineIndex++) {
            String chatLine = chatLines.get(chatLineIndex);
            boolean hasPlayerChatMarker = chatLine.contains("] > ");

            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth((String)chatLine, 4.0F, cardWidth - 12.0F), contentLeft, chatLineY, 4.0F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(hasPlayerChatMarker ? Accent.getLighterAccentColor((int)(240.0F * opacity)) : Accent.getDarkerTextColor((int)(220.0F * opacity)))
            .render(drawContext);
            chatLineY += 6.6F;
        }
    }

    private void renderWorldMarkers(
    DrawContext drawContext, ClientWorld clientWorld, ClientPlayerEntity clientPlayerEntity, float mapLeft, float mapTop, float mapSize, float opacity
    ) {
        float blockToPixelScale = mapSize / 96.0F;
        float mapCenterX = mapLeft + mapSize * 0.5F;
        float mapCenterY = mapTop + mapSize * 0.5F;

        for (AbstractClientPlayerEntity player : clientWorld.getPlayers()) {
            if (player != clientPlayerEntity) {
                double relativePlayerX = player.getX() - clientPlayerEntity.getX();
                double relativePlayerZ = player.getZ() - clientPlayerEntity.getZ();
                if (!(Math.abs(relativePlayerX) > 48.0) && !(Math.abs(relativePlayerZ) > 48.0)) {
                    float markerX = mapCenterX + (float)relativePlayerX * blockToPixelScale;
                    float markerY = mapCenterY + (float)relativePlayerZ * blockToPixelScale;
                    RoundedGradientRectBuilder.at(markerX - 1.6F, markerY - 1.6F, 3.2F, 3.2F)
                    .cornerRadius(1.6F)
                    .color(RgbaColor.rgba(235, 80, 80, (int)(255.0F * opacity)))
                    .render(drawContext);
                    GuiTextBuilder.createAt(Medium.DEFAULT_FONT, player.getName().getString(), markerX, markerY - 4.0F, 3.4F)
                    .centered()
                    .color(RgbaColor.rgba(255, 255, 255, (int)(230.0F * opacity)))
                    .render(drawContext);
                }
            }
        }

        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().translate(mapCenterX, mapCenterY);
        drawContext.getMatrices().rotate((clientPlayerEntity.getYaw() + 180.0F) * (float) (Math.PI / 180.0));
        RoundedGradientRectBuilder.at(-0.6F, -4.5F, 1.2F, 4.5F)
        .cornerRadius(0.6F)
        .color(Accent.getAccentColorWithAlpha((int)(255.0F * opacity)))
        .render(drawContext);
        drawContext.getMatrices().popMatrix();
        RoundedGradientRectBuilder.at(mapCenterX - 2.0F, mapCenterY - 2.0F, 4.0F, 4.0F)
        .cornerRadius(2.0F)
        .color(RgbaColor.rgba(255, 255, 255, (int)(255.0F * opacity)))
        .render(drawContext);
    }

    private void updateMapTexture(ClientWorld clientWorld, ClientPlayerEntity clientPlayerEntity) {
        long currentTimeMillis = System.currentTimeMillis();
        if (this.mapTexture == null || currentTimeMillis - this.lastMapRefreshMillis >= 300L) {
            this.lastMapRefreshMillis = currentTimeMillis;
            if (this.mapTexture == null) {
                this.mapTexture = new NativeImageBackedTexture(() -> "daamky_bot_map", 96, 96, true);
                MinecraftClient.getInstance().getTextureManager().registerTexture(MAP_TEXTURE_ID, this.mapTexture);
                Avatar.cacheTextureSize(MAP_TEXTURE_ID, 96, 96);
            }

            NativeImage nativeImage = this.mapTexture.getImage();
            if (nativeImage != null) {
                int mapOriginBlockX = clientPlayerEntity.getBlockX() - 48;
                int mapOriginBlockZ = clientPlayerEntity.getBlockZ() - 48;
                Mutable mutable = new Mutable();

                for (int pixelX = 0; pixelX < 96; pixelX++) {
                    int previousSurfaceY = Integer.MIN_VALUE;

                    for (int pixelZ = 0; pixelZ < 96; pixelZ++) {
                        int blockX = mapOriginBlockX + pixelX;
                        int blockZ = mapOriginBlockZ + pixelZ;
                        WorldChunk worldChunk = clientWorld.getChunkManager().getChunk(blockX >> 4, blockZ >> 4, ChunkStatus.FULL, false);
                        if (worldChunk == null) {
                            nativeImage.setColorArgb(pixelX, pixelZ, -15724524);
                            previousSurfaceY = Integer.MIN_VALUE;
                        } else {
                            int sampleY = worldChunk.sampleHeightmap(Type.WORLD_SURFACE, blockX & 15, blockZ & 15);
                            mutable.set(blockX, sampleY, blockZ);
                            BlockState blockState = worldChunk.getBlockState(mutable);

                            int surfaceY;
                            for (surfaceY = sampleY;
                            blockState.getMapColor(clientWorld, mutable) == MapColor.CLEAR && surfaceY > clientWorld.getBottomY();
                            blockState = worldChunk.getBlockState(mutable)
                            ) {
                                mutable.setY(--surfaceY);
                            }

                            MapColor mapColor = blockState.getMapColor(clientWorld, mutable);
                            Brightness brightness = previousSurfaceY != Integer.MIN_VALUE && surfaceY != previousSurfaceY
                            ? (surfaceY > previousSurfaceY ? Brightness.HIGH : Brightness.LOW)
                            : Brightness.NORMAL;
                            nativeImage.setColorArgb(pixelX, pixelZ, mapColor == MapColor.CLEAR ? -15724524 : mapColor.getRenderColor(brightness));
                            previousSurfaceY = surfaceY;
                        }
                    }
                }

                this.mapTexture.upload();
            }
        }
    }
}

