package dev.daamky;

import net.minecraft.client.util.math.MatrixStack;

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
    private final BooleanSetting resetSettingsButton = new BooleanSetting("Reset", false);

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
}

