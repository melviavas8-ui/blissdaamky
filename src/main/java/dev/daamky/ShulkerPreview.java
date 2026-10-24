package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventDisplay;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public final class ShulkerPreview extends ModuleStructure {
    private final BooleanSetting showItemName = new BooleanSetting("Название", true);
    private final BooleanSetting showEmptySlots = new BooleanSetting("Пустые", false);
    private ItemStack pendingPreviewStack = ItemStack.EMPTY;
    private int cursorX;
    private int cursorY;
    private long previewRequestedAtMillis;

    public ShulkerPreview() {
        super("ShulkerPreview", "Показывает содержимое шалкера при наведении", ModuleCategory.RENDER);
        this.addSettings(this.showItemName, this.showEmptySlots);
    }

    public static boolean requestPreview(ItemStack candidateStack, int cursorX, int cursorY) {
        ModuleStructure registeredModule = ModuleRegistry.getInstance().getModuleByClass(ShulkerPreview.class);
        if (registeredModule instanceof ShulkerPreview previewModule && registeredModule.isEnabled() && previewModule.isPreviewableShulker(candidateStack)) {
            previewModule.pendingPreviewStack = candidateStack;
            previewModule.cursorX = cursorX;
            previewModule.cursorY = cursorY;
            previewModule.previewRequestedAtMillis = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    private boolean isPreviewableShulker(ItemStack candidateStack) {
        if (candidateStack != null && !candidateStack.isEmpty()) {
            if (candidateStack.getItem() instanceof BlockItem shulkerBlockItem) {
                if (shulkerBlockItem.getBlock() instanceof ShulkerBoxBlock) {
                    ContainerComponent containerContents = candidateStack.get(DataComponentTypes.CONTAINER);
                    boolean hasNonEmptyItems = containerContents != null && containerContents.iterateNonEmpty().iterator().hasNext();
                    return hasNonEmptyItems || this.showEmptySlots.getValue();
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @EventTarget
    public void renderPreview(EventDisplay eventDisplay) {
        if (!this.pendingPreviewStack.isEmpty()
        && CLIENT.currentScreen instanceof HandledScreen
        && System.currentTimeMillis() - this.previewRequestedAtMillis <= 100L) {
            ItemStack previewStack = this.pendingPreviewStack;
            this.pendingPreviewStack = ItemStack.EMPTY;
            DrawContext drawContext = eventDisplay.graphics();
            float windowScale = CLIENT.getWindow().getScaleFactor();
            float itemScale = windowScale / 2.0F;
            DefaultedList<ItemStack> previewSlots = DefaultedList.ofSize(27, ItemStack.EMPTY);
            ContainerComponent containerContents = previewStack.get(DataComponentTypes.CONTAINER);
            if (containerContents != null) {
                containerContents.copyTo(previewSlots);
            }

            float slotSize = 18.0F * itemScale;
            float titleHeight = this.showItemName.getValue() ? 12.0F : 0.0F;
            float gridWidth = slotSize * 9.0F;

            float panelWidth = gridWidth + 10.0F;
            gridWidth = slotSize * 3.0F;

            float panelHeight = gridWidth + 10.0F + titleHeight;
            float panelX = (this.cursorX + 12) * itemScale;
            float panelY = (this.cursorY - 12) * itemScale;
            float screenWidth = GuiEventRenderer.getScreenWidth();
            float screenHeight = GuiEventRenderer.getScreenHeight();
            if (panelX + panelWidth > screenWidth - 4.0F) {
                panelX = this.cursorX * itemScale - panelWidth - 12.0F;
            }

            panelX = Math.max(4.0F, panelX);
            panelY = Math.clamp(panelY, 4.0F, Math.max(4.0F, screenHeight - panelHeight - 4.0F));
            Accent accent = Accent.getInstance();
            if (accent.isGlassStyle()) {
                BlurredRectangleBuilder.create(panelX, panelY, panelWidth, panelHeight)
                .cornerRadius(5.0F)
                .blurMode(KawaseGaussianBlurMode.KAWASE)
                .blurPasses((int)accent.getBlurRadius())
                .color(Accent.getTintedBackgroundColor(140))
                .render(drawContext);
            } else {
                KawaseBlurBuilder.create(panelX, panelY, panelWidth, panelHeight)
                .cornerRadius(5.0F)
                .blurMode(KawaseGaussianBlurMode.KAWASE)
                .blurPasses((int)accent.getBlurRadius())
                .color(Accent.getTintedBackgroundColor(200))
                .render(drawContext);
            }

            RoundedGradientRectBuilder.at(panelX, panelY, panelWidth, panelHeight)
            .cornerRadius(5.0F)
            .color(Accent.getAccentBackgroundColor(accent.isGlassStyle() ? 26 : 36))
            .render(drawContext);
            RoundedRectBuilder.createAt(panelX, panelY, panelWidth, panelHeight)
            .cornerRadius(5.0F)
            .borderWidth(0.85F)
            .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), 70))
            .render(drawContext);
            if (this.showItemName.getValue()) {
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, previewStack.getName().getString(), panelX + 5.0F + 1.0F, panelY + 5.0F + 4.0F, 6.0F)
                .baselinePosition(TextBaselinePosition.CENTER)
                .color(Accent.getTextColorWithAlpha(255))
                .render(drawContext);
            }

            float slotsOriginX = panelX + 5.0F;
            float slotsOriginY = panelY + 5.0F + titleHeight;

            for (int slotIndex = 0; slotIndex < 27; slotIndex++) {
                float slotX = slotsOriginX + slotIndex % 9 * slotSize + 1.0F;
                float slotY = slotsOriginY + slotIndex / 9 * slotSize + 1.0F;
                RoundedGradientRectBuilder.at(slotX, slotY, slotSize - 2.0F, slotSize - 2.0F)
                .cornerRadius(2.5F)
                .color(Accent.getAccentColorWithAlpha(14))
                .render(drawContext);
            }

            GuiEventRenderer.flushAndStartRootLayer(drawContext);
            drawContext.getMatrices().pushMatrix();
            drawContext.getMatrices().scale(itemScale);

            for (int itemIndex = 0; itemIndex < 27; itemIndex++) {
                ItemStack slotStack = previewSlots.get(itemIndex);
                if (!slotStack.isEmpty()) {
                    int itemX = Math.round((slotsOriginX + itemIndex % 9 * slotSize) / itemScale) + 1;
                    int itemY = Math.round((slotsOriginY + itemIndex / 9 * slotSize) / itemScale) + 1;
                    GuiEventRenderer.flushBatchedElements();
                    drawContext.drawItem(slotStack, itemX, itemY);
                    drawContext.drawStackOverlay(CLIENT.textRenderer, slotStack, itemX, itemY);
                }
            }

            drawContext.getMatrices().popMatrix();
        } else {
            this.pendingPreviewStack = ItemStack.EMPTY;
        }
    }
}

