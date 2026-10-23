package dev.daamky;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class SwingAnimation extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static volatile int oneShotSwingDurationTicks = -1;
    private static volatile int pendingSwingDurationTicks = -1;
    private final MultiOptionSetting animationStyle = new MultiOptionSetting(
    "Анимация", "Плавная", "Плавная", "Вперёд", "На себя", "Блок", "Вниз", "Протык", "Нур", "Акриен", "Блок-удар", "Взмах"
    );
    private final FloatSetting swingStrength = new FloatSetting("Сила взмаха", 5.0F, 1.0F, 10.0F, 1.0F);
    private final FloatSetting swingSpeed = new FloatSetting("Скорость", 7.0F, 1.0F, 10.0F, 1.0F);
    private final FloatSetting swingAngle = new FloatSetting("Угол", 0.0F, 0.0F, 360.0F, 1.0F);
    private final FloatSetting handHeight = new FloatSetting("Высота", 0.0F, 0.0F, 1.0F, 0.1F);
    private final BooleanSetting requiresAuraTarget = new BooleanSetting("Только с аурой", false);
    private final BooleanSetting requiresActiveSwing = new BooleanSetting("Только при ударе", false);
// https://t.me/pasterplace

    public SwingAnimation() {
        super("SwingAnimation", "Позволяет изменить анимацию удара и скорость взмаха", ModuleCategory.RENDER);
        this.swingStrength
        .visibleWhen(
        () -> !this.animationStyle.isSelected("Блок-удар")
        && !this.animationStyle.isSelected("Акриен")
        && !this.animationStyle.isSelected("Нур")
        );
        this.swingAngle
        .visibleWhen(
        () -> this.animationStyle.isSelected("На себя") || this.animationStyle.isSelected("Взмах") || this.animationStyle.isSelected("Блок")
        );
        this.addSettings(
        this.animationStyle, this.swingStrength, this.swingSpeed, this.swingAngle, this.handHeight, this.requiresAuraTarget, this.requiresActiveSwing
        );
    }
// https://t.me/pasterplace

    @Override
    public String getDisplayInfo() {
        return this.animationStyle.getValue();
    }
// https://t.me/pasterplace

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onDisable() {
// https://t.me/pasterplace

        super.onDisable();
    }
// https://t.me/pasterplace

    public static SwingAnimation getActiveInstance() {
        SwingAnimation module = (SwingAnimation)ModuleRegistry.getInstance().getModuleByClass(SwingAnimation.class);
        return module != null && module.isEnabled() && module.canAnimate() ? module : null;
    }
// https://t.me/pasterplace

    private boolean canAnimate() {
        if (!this.hasActiveClientWorld()) {
            return false;
        }

        if (this.requiresAuraTarget.getValue()) {
            Aura aura = Aura.getInstance();
            if (aura == null || aura.getTarget() == null) {
                return false;
            }
        }

        return !this.requiresActiveSwing.getValue() || CLIENT.player.handSwingTicks >= 1;
    }
// https://t.me/pasterplace

    public static void prepareSwingAnimation() {
        SwingAnimation swingAnimation = getActiveInstance();
        if (swingAnimation != null) {
            int swingDurationTicks = Math.max(1, Math.round(swingAnimation.swingSpeed.getValue() * 2.0F));

        }
    }
// https://t.me/pasterplace

    public static int adjustSwingDuration(int defaultDurationTicks) {
        ModuleStructure moduleStructure = ModuleRegistry.getInstance().getModuleByClass(SwingAnimation.class);
        if (moduleStructure != null && moduleStructure.isEnabled()) {
            int pendingDurationTicks = oneShotSwingDurationTicks;
            if (pendingDurationTicks > 0) {
                oneShotSwingDurationTicks = -1;
                return pendingDurationTicks;
            } else {
                return pendingSwingDurationTicks > 0 ? pendingSwingDurationTicks : defaultDurationTicks;
            }
        } else {
            return defaultDurationTicks;
        }
    }
// https://t.me/pasterplace

    public void applySwingTransform(MatrixStack matrices, Arm arm, float swingProgress, float armHeightProgress, float equipAnimationProgress) {
        float armDirection = arm == Arm.LEFT ? -1.0F : 1.0F;
        float handHeightOffset = this.handHeight.getValue() * armHeightProgress;
        float swingSine = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
        switch ((String)this.animationStyle.getValue()) {
        case "Плавная":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset, -0.72F);
            float swingStrengthDegrees = this.swingStrength.getValue() * 10.0F;
            float swingCurve = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((45.0F + swingCurve * (-swingStrengthDegrees / 4.0F)) * armDirection));
            float swingRotationSine = MathHelper.sin(MathHelper.sqrt(swingProgress * swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swingRotationSine * -(swingStrengthDegrees / 4.0F) * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingRotationSine * -swingStrengthDegrees));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45.0F * armDirection));
            break;
        case "Вперёд":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset, -0.72F);

            matrices.translate(0.0, 0.0, -0.3 * swingSine);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(swingSine * -35.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swingSine * 35.0F * armDirection));
            break;
        case "На себя":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset, -0.72F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60.0F * armDirection));
            matrices.multiply(
            RotationAxis.POSITIVE_X.rotationDegrees(-this.swingAngle.getValue() - this.swingStrength.getValue() * 10.0F * swingSine)
            );
            break;
        case "Блок":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset, -0.72F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-30.0F * armDirection));
            matrices.multiply(
            RotationAxis.POSITIVE_X.rotationDegrees(-this.swingAngle.getValue() - this.swingStrength.getValue() * 10.0F * swingSine)
            );
            break;
        case "Вниз":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset - swingSine * this.swingStrength.getValue() / 24.0F, -0.72F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-30.0F * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
            break;
        case "Протык":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset, -0.72F);
            matrices.scale(1.0F, 1.0F, 1.0F + swingSine * this.swingStrength.getValue() / 4.0F);
            matrices.translate(0.0F, 0.0F, -0.265F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0F));
            break;
        case "Нур":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset, -0.72F);
            float sqrtSwingProgress = MathHelper.sqrt(swingProgress);
            float rootProgressSine = MathHelper.sin(sqrtSwingProgress * (float) Math.PI);
            float swingProgressSine = MathHelper.sin(swingProgress * (float) Math.PI);
            matrices.translate((0.4F - rootProgressSine * 0.2F) * armDirection, -0.2F + rootProgressSine * 0.3F, -0.5F - swingProgressSine * 0.2F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(91.0F * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((-40.0F + rootProgressSine * -100.0F) * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0F));
            break;
        case "Акриен":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset, -0.72F);
            matrices.scale(0.8F, 0.8F, 0.8F);
            float inverseEquipProgress = 1.0F - equipAnimationProgress;
            matrices.translate((0.3 - swingSine * 0.15F) * armDirection, 0.2F - inverseEquipProgress * 0.12F, -0.15F - swingSine * 0.13F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((76.0F - swingSine * 10.0F) * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((-16.0F - swingSine * 8.0F) * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-83.0F - swingSine * 26.0F));
            break;
        case "Блок-удар":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset, -0.72F);
            float attackSine = MathHelper.sin((float)(swingProgress * swingProgress * Math.PI));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * armDirection));
            float blockHitSwingSine = MathHelper.sin((float)(MathHelper.sqrt(swingProgress) * Math.PI));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(attackSine * -20.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(blockHitSwingSine * -20.0F * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(blockHitSwingSine * -80.0F));
            matrices.translate(0.4F * armDirection, 0.2F, 0.2F);
            matrices.translate(-0.5F * armDirection, 0.08F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(20.0F * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(20.0F * armDirection));
            break;
        case "Взмах":
            matrices.translate(0.56F * armDirection, -0.52F - handHeightOffset, -0.72F);
            float swipeSqrtProgress = MathHelper.sqrt(swingProgress);
            float swipeRootProgressSine = MathHelper.sin(swipeSqrtProgress * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((-77.0F + swipeRootProgressSine * -(this.swingStrength.getValue() * 10.0F)) * armDirection));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.swingAngle.getValue()));
        }
    }
}

