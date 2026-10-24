package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventHudLayer;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

public final class ViewModel extends ModuleStructure {
    private final FloatSetting rightHandX = new FloatSetting("Правая рука X", 0.0F, -2.0F, 2.0F, 0.01F);
    private final FloatSetting rightHandY = new FloatSetting("Правая рука Y", 0.0F, -2.0F, 2.0F, 0.01F);
    private final FloatSetting rightHandZ = new FloatSetting("Правая рука Z", 0.0F, -2.0F, 2.0F, 0.01F);
    private final FloatSetting rightHandScale = new FloatSetting("Размер правой руки", 1.0F, 0.1F, 3.0F, 0.01F);
    private final FloatSetting leftHandX = new FloatSetting("Левая рука X", 0.0F, -2.0F, 2.0F, 0.01F);
    private final FloatSetting leftHandY = new FloatSetting("Левая рука Y", 0.0F, -2.0F, 2.0F, 0.01F);
    private final FloatSetting leftHandZ = new FloatSetting("Левая рука Z", 0.0F, -2.0F, 2.0F, 0.01F);
    private final FloatSetting leftHandScale = new FloatSetting("Размер левой руки", 1.0F, 0.1F, 3.0F, 0.01F);
    private final BooleanSetting scaleHands = new BooleanSetting("Уменьшать руку", false);
    private final BooleanSetting onlyWithAuraTarget = new BooleanSetting("Только с аурой", false);
    private final BooleanSetting onlyDuringSwing = new BooleanSetting("Только при ударе", false);
    private final BooleanSetting chatEdit = new BooleanSetting("Настройка в чате", true);
    private final BooleanSetting resetSettingsButton = new BooleanSetting("Reset", false);

    // Модель руки во view-space (единицы блоков): якорь предмета в руке и подъём визуального центра над ним.
    private static final float HAND_ANCHOR_X = 0.56F;
    private static final float HAND_ANCHOR_Y = -0.52F;
    private static final float HAND_ANCHOR_Z = -0.72F;
    private static final float HAND_CENTER_RISE = 0.24F;
    private static final float HAND_HIT_RADIUS = 0.30F;
    private static final float SCALE_SCROLL_STEP = 0.05F;

    // Состояние редактирования в чате: 0 = основная рука (настройки "Правая"), 1 = вторая рука ("Левая").
    private int hoveredHand = -1;
    private int draggedHand = -1;
    private boolean primaryWasDown;
    private float dragStartMouseX;
    private float dragStartMouseY;
    private float dragStartHandX;
    private float dragStartHandY;

    public ViewModel() {
        super("ViewModel", "Позволяет изменить положение и размер рук", ModuleCategory.RENDER);
        this.resetSettingsButton.onChanged(resetRequested -> {
            if (resetRequested) {
                this.resetSettings();
                this.resetSettingsButton.setValue(false);
            }
        });
        this.addSettings(
        this.rightHandX,
        this.rightHandY,
        this.rightHandZ,
        this.rightHandScale,
        this.leftHandX,
        this.leftHandY,
        this.leftHandZ,
        this.leftHandScale,
        this.scaleHands,
        this.onlyWithAuraTarget,
        this.onlyDuringSwing,
        this.chatEdit,
        this.resetSettingsButton
        );
    }

    private void resetSettings() {
        this.rightHandX.setValue(Float.valueOf(0.0F));
        this.rightHandY.setValue(Float.valueOf(0.0F));
        this.rightHandZ.setValue(Float.valueOf(0.0F));
        this.rightHandScale.setValue(Float.valueOf(1.0F));
        this.leftHandX.setValue(Float.valueOf(0.0F));
        this.leftHandY.setValue(Float.valueOf(0.0F));
        this.leftHandZ.setValue(Float.valueOf(0.0F));
        this.leftHandScale.setValue(Float.valueOf(1.0F));
    }

    public static ViewModel getInstanceIfActive() {
        ViewModel module = (ViewModel)ModuleRegistry.getInstance().getModuleByClass(ViewModel.class);
        return module != null && module.isEnabled() && module.shouldApplyTransform() ? module : null;
    }

    private boolean shouldApplyTransform() {
        if (!this.hasActiveClientWorld()) {
            return false;
        }

        if (this.onlyWithAuraTarget.getValue()) {
            Aura aura = Aura.getInstance();
            if (aura == null || aura.getTarget() == null) {
                return false;
            }
        }

        return !this.onlyDuringSwing.getValue() || CLIENT.player.handSwingProgress > 0.15F;
    }

    public void translateHand(MatrixStack matrices, boolean mainHand) {
        if (mainHand) {
            matrices.translate(this.rightHandX.getValue(), this.rightHandY.getValue(), this.rightHandZ.getValue());
        } else {
            matrices.translate(this.leftHandX.getValue(), this.leftHandY.getValue(), this.leftHandZ.getValue());
        }
    }

    public void scaleHand(MatrixStack matrices, boolean mainHand) {
        float scale = mainHand ? this.rightHandScale.getValue() : this.leftHandScale.getValue();
        matrices.scale(scale, scale, scale);
    }

    public void transformHand(MatrixStack matrices, boolean mainHand) {
        this.translateHand(matrices, mainHand);
        if (this.scaleHands.getValue()) {
            this.scaleHand(matrices, mainHand);
        }
    }

    @Override
    protected void onDisable() {
        this.resetEditState();
    }

    private void resetEditState() {
        this.hoveredHand = -1;
        this.draggedHand = -1;
        this.primaryWasDown = false;
    }

    private boolean isChatEditActive() {
        return this.isEnabled()
            && this.chatEdit.getValue()
            && CLIENT.currentScreen instanceof ChatScreen
            && CLIENT.player != null
            && CLIENT.world != null
            && this.shouldApplyTransform();
    }

    private FloatSetting handXSetting(int handIndex) {
        return handIndex == 0 ? this.rightHandX : this.leftHandX;
    }

    private FloatSetting handYSetting(int handIndex) {
        return handIndex == 0 ? this.rightHandY : this.leftHandY;
    }

    private FloatSetting handZSetting(int handIndex) {
        return handIndex == 0 ? this.rightHandZ : this.leftHandZ;
    }

    private FloatSetting handScaleSetting(int handIndex) {
        return handIndex == 0 ? this.rightHandScale : this.leftHandScale;
    }

    private static float getMouseX() {
        Window window = CLIENT.getWindow();
        return (float)(CLIENT.mouse.getX() * window.getFramebufferWidth() / Math.max(1, window.getWidth()) / 2.0);
    }

    private static float getMouseY() {
        Window window = CLIENT.getWindow();
        return (float)(CLIENT.mouse.getY() * window.getFramebufferHeight() / Math.max(1, window.getHeight()) / 2.0);
    }

    /**
     * Где на экране рука. Возвращает {центр X, центр Y, радиус, пикселей на единицу} в тех же единицах,
     * что и HUD (framebuffer / 2), либо null, если руку посчитать нельзя.
     */
    private float[] computeHandGeometry(int handIndex) {
        ClientPlayerEntity player = CLIENT.player;
        if (player == null) {
            return null;
        }

        boolean mainHand = handIndex == 0;
        Hand hand = mainHand ? Hand.MAIN_HAND : Hand.OFF_HAND;
        Arm arm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
        float side = arm == Arm.RIGHT ? 1.0F : -1.0F;
        float offsetX = this.handXSetting(handIndex).getValue();
        float offsetY = this.handYSetting(handIndex).getValue();
        float offsetZ = this.handZSetting(handIndex).getValue();
        float scale = this.handScaleSetting(handIndex).getValue();

        float visualScale;
        float centerX;
        float centerY;
        float centerZ;
        if (player.getStackInHand(hand).isEmpty()) {
            // Пустая рука масштабируется вокруг камеры (только если включено "Уменьшать руку"), поэтому сдвигается вместе с масштабом.
            visualScale = this.scaleHands.getValue() ? scale : 1.0F;
            centerX = offsetX + visualScale * side * HAND_ANCHOR_X;
            centerY = offsetY + visualScale * (HAND_ANCHOR_Y + HAND_CENTER_RISE);
            centerZ = offsetZ + visualScale * HAND_ANCHOR_Z;
        } else {
            // Предмет масштабируется вокруг своей точки, якорь остаётся на месте.
            visualScale = scale;
            centerX = offsetX + side * HAND_ANCHOR_X;
            centerY = offsetY + HAND_ANCHOR_Y + scale * HAND_CENTER_RISE;
            centerZ = offsetZ + HAND_ANCHOR_Z;
        }

        float depth = -centerZ;
        if (depth < 0.1F) {
            return null;
        }

        float screenWidth = GuiEventRenderer.getScreenWidth();
        float screenHeight = GuiEventRenderer.getScreenHeight();
        if (screenWidth <= 1.0F || screenHeight <= 1.0F) {
            return null;
        }

        float fov = CLIENT.options.getFov().getValue();
        float tanHalfFov = (float)Math.tan(Math.toRadians(fov) / 2.0);
        float pixelsPerUnit = screenHeight * 0.5F / (tanHalfFov * depth);
        float screenX = screenWidth * 0.5F + centerX * pixelsPerUnit;
        float screenY = screenHeight * 0.5F - centerY * pixelsPerUnit;
        float radius = HAND_HIT_RADIUS * Math.max(visualScale, 0.2F) * pixelsPerUnit;
        return new float[]{screenX, screenY, radius, pixelsPerUnit};
    }

    private static float hitDistanceSquared(float[] geometry, float mouseX, float mouseY) {
        if (geometry == null) {
            return -1.0F;
        }

        float deltaX = mouseX - geometry[0];
        float deltaY = mouseY - geometry[1];
        float distanceSquared = deltaX * deltaX + deltaY * deltaY;
        return distanceSquared <= geometry[2] * geometry[2] ? distanceSquared : -1.0F;
    }

    private static int pickHand(float[] mainGeometry, float[] offGeometry, float mouseX, float mouseY) {
        float mainDistance = hitDistanceSquared(mainGeometry, mouseX, mouseY);
        float offDistance = hitDistanceSquared(offGeometry, mouseX, mouseY);
        if (mainDistance < 0.0F && offDistance < 0.0F) {
            return -1;
        }

        if (mainDistance < 0.0F) {
            return 1;
        }

        if (offDistance < 0.0F) {
            return 0;
        }

        return mainDistance <= offDistance ? 0 : 1;
    }

    private static boolean isBlockedByOtherUi(float mouseX, float mouseY) {
        float chatBarTop = GuiEventRenderer.getScreenHeight() - 16.0F * (float)CLIENT.getWindow().getScaleFactor() / 2.0F;
        return mouseY > chatBarTop || HudModule.isPointOverEditableElement(mouseX, mouseY);
    }

    @EventTarget(priority = 3)
    public void onHudLayer(EventHudLayer eventHudLayer) {
        if (!this.isChatEditActive()) {
            this.resetEditState();
            return;
        }

        float mouseX = getMouseX();
        float mouseY = getMouseY();
        boolean primaryDown = MouseButtonState.isPrimaryButtonDown();
        float[] mainGeometry = this.computeHandGeometry(0);
        float[] offGeometry = this.computeHandGeometry(1);
        boolean blocked = isBlockedByOtherUi(mouseX, mouseY);

        if (primaryDown && !this.primaryWasDown) {
            this.draggedHand = blocked ? -1 : pickHand(mainGeometry, offGeometry, mouseX, mouseY);
            if (this.draggedHand >= 0) {
                this.dragStartMouseX = mouseX;
                this.dragStartMouseY = mouseY;
                this.dragStartHandX = this.handXSetting(this.draggedHand).getValue();
                this.dragStartHandY = this.handYSetting(this.draggedHand).getValue();
            }
        }

        if (!primaryDown) {
            this.draggedHand = -1;
        }

        this.primaryWasDown = primaryDown;
        float[] draggedGeometry = this.draggedHand == 0 ? mainGeometry : this.draggedHand == 1 ? offGeometry : null;
        if (this.draggedHand >= 0 && draggedGeometry != null) {
            FloatSetting xSetting = this.handXSetting(this.draggedHand);
            FloatSetting ySetting = this.handYSetting(this.draggedHand);
            float newX = this.dragStartHandX + (mouseX - this.dragStartMouseX) / draggedGeometry[3];
            float newY = this.dragStartHandY - (mouseY - this.dragStartMouseY) / draggedGeometry[3];
            xSetting.setValue(Float.valueOf(Math.round(Math.clamp(newX, xSetting.minimum(), xSetting.maximum()) * 100.0F) / 100.0F));
            ySetting.setValue(Float.valueOf(Math.round(Math.clamp(newY, ySetting.minimum(), ySetting.maximum()) * 100.0F) / 100.0F));
        }

        this.hoveredHand = this.draggedHand >= 0 ? this.draggedHand : blocked ? -1 : pickHand(mainGeometry, offGeometry, mouseX, mouseY);
        if (this.hoveredHand >= 0) {
            float[] geometry = this.hoveredHand == 0 ? mainGeometry : offGeometry;
            if (geometry != null) {
                this.renderHandHighlight(eventHudLayer.graphics(), geometry, this.hoveredHand, this.draggedHand >= 0);
            }
        }
    }

    private void renderHandHighlight(DrawContext drawContext, float[] geometry, int handIndex, boolean dragging) {
        float centerX = geometry[0];
        float centerY = geometry[1];
        float radius = geometry[2];
        GuiEventRenderer.flushAndStartRootLayer(drawContext);
        RoundedGradientRectBuilder.at(centerX - radius, centerY - radius, radius * 2.0F, radius * 2.0F)
        .cornerRadius(radius)
        .color(Accent.getAccentColorWithAlpha(dragging ? 46 : 24))
        .render(drawContext);
        RoundedRectBuilder.createAt(centerX - radius, centerY - radius, radius * 2.0F, radius * 2.0F)
        .cornerRadius(radius)
        .borderWidth(1.0F)
        .color(Accent.getAccentColorWithAlpha(dragging ? 230 : 160))
        .render(drawContext);
        String label = String.format(
            Locale.ROOT,
            "%s   X %.2f   Y %.2f   x%.2f",
            handIndex == 0 ? "Правая рука" : "Левая рука",
            this.handXSetting(handIndex).getValue(),
            this.handYSetting(handIndex).getValue(),
            this.handScaleSetting(handIndex).getValue()
        );
        float labelY = Math.max(10.0F, centerY - radius - 8.0F);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, label, centerX, labelY, 6.5F)
        .alignment(HorizontalTextAlignment.CENTER)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getTextColorWithAlpha(235))
        .render(drawContext);
    }

    /** Колесо мыши в чате над рукой меняет её размер. Возвращает true, если прокрутка забрана. */
    public static boolean handleChatScroll(double verticalAmount) {
        ViewModel module = getInstanceIfActive();
        if (module == null || verticalAmount == 0.0 || !module.isChatEditActive()) {
            return false;
        }

        int handIndex = module.draggedHand >= 0 ? module.draggedHand : module.hoveredHand;
        if (handIndex < 0) {
            return false;
        }

        FloatSetting scaleSetting = module.handScaleSetting(handIndex);
        float newScale = scaleSetting.getValue() + (float)Math.signum(verticalAmount) * SCALE_SCROLL_STEP;
        newScale = Math.clamp(newScale, scaleSetting.minimum(), scaleSetting.maximum());
        scaleSetting.setValue(Float.valueOf(Math.round(newScale * 100.0F) / 100.0F));
        if (!module.scaleHands.getValue()) {
            // Без этого размер менялся бы только у предметов, а пустая рука осталась бы прежней.
            module.scaleHands.setValue(true);
        }

        return true;
    }
}
