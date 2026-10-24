package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ElytraBoost extends ModuleStructure {
    private static final double DEFAULT_BOOST_MULTIPLIER = 1.5;
    private final MultiOptionSetting modeSetting = new MultiOptionSetting(
    "Режим", "Статичный", "Статичный", "Динамический", "Кастом", "ReallyWorld", "RW 1.21", "BravoHvH", "Super Firework", "Advanced", "Advanced Stable"
    );
    private final FloatSetting boostStrengthSetting = new FloatSetting("Сила буста", 1.5F, 0.1F, 6.0F, 0.05F);
    private final FloatSetting[] customYawBoostSettings = new FloatSetting[9];
    private final FloatSetting[] customPitchBoostSettings = new FloatSetting[9];
    private final BooleanSetting elytraTimerEnabled = new BooleanSetting("Таймер элитры", false);
    private final FloatSetting elytraTimerMultiplier = new FloatSetting("Сила таймера", 1.5F, 1.0F, 5.0F, 0.1F, "x");
    private final FloatSetting cooldown = new FloatSetting("Сброс таймера", 0.5F, 0.0F, 0.9F, 0.001F, "x");
    private static final float[] reallyWorldYawMultipliers = new float[]{1.6F, 1.63F, 1.66F, 1.71F, 1.73F, 1.81F, 1.81F, 1.81F, 1.81F};
    private static final float[] reallyWorldPitchMultipliers = new float[]{1.6F, 1.62F, 1.6F, 1.62F, 1.64F, 1.68F, 1.75F, 1.9F, 2.16F};
    private static final float[] rw121YawMultipliers = new float[]{1.5F, 1.5F, 1.5F, 1.5F, 1.7F, 1.81F, 1.81F, 2.0F, 2.0F};
    private static final float[] rw121PitchMultipliers = new float[]{1.5F, 1.5F, 1.5F, 1.5F, 1.5F, 1.5F, 1.75F, 1.9F, 2.0F};
    private static final float[] bravoYawMultipliers = new float[]{1.65F, 1.68F, 1.73F, 1.78F, 1.84F, 1.89F, 1.98F, 2.05F, 2.04F};
    private static final float[] bravoPitchMultipliers = new float[]{1.61F, 1.66F, 1.76F, 1.84F, 1.87F, 1.93F, 1.98F, 2.05F, 2.04F};
    private int timerAlternationCounter;
    private boolean elytraTimerActive;

    public ElytraBoost() {
        super("ElytraBoost", "Ускоряет буст фейерверка на элитрах", ModuleCategory.MOVEMENT);
        this.boostStrengthSetting.visibleWhen(() -> this.modeSetting.isSelected("Статичный") || this.modeSetting.isSelected("Super Firework"));

        for (int settingIndex = 0; settingIndex < 9; settingIndex++) {

            this.customYawBoostSettings[settingIndex] = new FloatSetting("Yaw 0-5", 1.52F + settingIndex * 0.01F, 1.5F, 3.0F, 0.01F);
            this.customPitchBoostSettings[settingIndex] = new FloatSetting("Pitch " + settingIndex * 5 + "-" + (settingIndex * 5 + 5), 1.51F + settingIndex * 0.01F, 1.5F, 3.0F, 0.01F);
            this.customYawBoostSettings[settingIndex].visibleWhen(() -> this.modeSetting.isSelected("Кастом"));
            this.customPitchBoostSettings[settingIndex].visibleWhen(() -> this.modeSetting.isSelected("Кастом"));
        }

        this.elytraTimerMultiplier.visibleWhen(this.elytraTimerEnabled::getValue);
        this.cooldown.visibleWhen(this.elytraTimerEnabled::getValue);
        this.addSettings(this.modeSetting, this.boostStrengthSetting);
        this.addSettings(this.customYawBoostSettings);
        this.addSettings(this.customPitchBoostSettings);
        this.addSettings(this.elytraTimerEnabled, this.elytraTimerMultiplier, this.cooldown);
    }

    @Override
    public String getDisplayInfo() {
        return this.modeSetting.getValue();
    }

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onDisable() {
        this.timerAlternationCounter = 0;
        this.elytraTimerActive = false;
    }

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void onUpdate(EventUpdate eventUpdate) {
        if (!this.hasActiveClientWorld()) {
            this.elytraTimerActive = false;
        } else {
            this.elytraTimerActive = this.elytraTimerEnabled.getValue() && CLIENT.player.isGliding();
            if (this.elytraTimerActive) {
                this.timerAlternationCounter++;
            }
        }
    }

    public static Vec3d getConfiguredBoostDirection() {
        ElytraBoost elytraBoostModule = (ElytraBoost)ModuleRegistry.getInstance().getModuleByClass(ElytraBoost.class);
        return elytraBoostModule != null
        && elytraBoostModule.isEnabled()
        && CLIENT.player != null
        && !elytraBoostModule.modeSetting.isSelected("Динамический")
        ? elytraBoostModule.calculateModeBoostVector()
        : null;
    }

    public static Vec3d adjustDynamicBoostDirection(Vec3d originalDirection) {
        ElytraBoost elytraBoostModule = (ElytraBoost)ModuleRegistry.getInstance().getModuleByClass(ElytraBoost.class);
        if (elytraBoostModule != null
        && elytraBoostModule.isEnabled()
        && elytraBoostModule.modeSetting.isSelected("Динамический")
        && CLIENT.player != null) {
            Vec3d playerVelocity = CLIENT.player.getVelocity();
            double horizontalSpeed = playerVelocity.horizontalLength();
            if (horizontalSpeed < 1.0E-6 && Math.abs(playerVelocity.y) < 1.0E-6) {
                return originalDirection;
            }

            float yawDegrees = MathHelper.wrapDegrees((float)(MathHelper.atan2(playerVelocity.z, playerVelocity.x) * 180.0F / (float)Math.PI) - 90.0F);
            float pitchDegrees = (float)(-(MathHelper.atan2(playerVelocity.y, horizontalSpeed) * 180.0F / (float)Math.PI));
            return Vec3d.fromPolar(pitchDegrees, yawDegrees);
        } else {
            return originalDirection;
        }
    }

    public static Vec3d getRotationDirectionVector() {
        Rotation rotation = RotationPriorityManager.getInstance().getCurrentRotation();
        return Vec3d.fromPolar(rotation.getPitch(), rotation.getYaw());
    }

    private Vec3d calculateModeBoostVector() {
        Rotation rotation = RotationPriorityManager.getInstance().getCurrentRotation();
        float yawDegrees = rotation.getYaw();
        float pitchDegrees = rotation.getPitch();
        double horizontalBoost;
        double verticalBoost;
        switch ((String)this.modeSetting.getValue()) {
        case "Кастом":
            horizontalBoost = this.customYawBoostSettings[getAngleSectorIndex(yawDegrees)].getValue().floatValue();
            verticalBoost = this.customPitchBoostSettings[getAngleSectorIndex(pitchDegrees)].getValue().floatValue();
            horizontalBoost = Math.max(horizontalBoost, verticalBoost);
            break;
        case "ReallyWorld":
            verticalBoost = reallyWorldPitchMultipliers[getAngleSectorIndex(pitchDegrees)];
            horizontalBoost = Math.max(reallyWorldYawMultipliers[getAngleSectorIndex(yawDegrees)], verticalBoost);
            break;
        case "RW 1.21":
            verticalBoost = rw121PitchMultipliers[getAngleSectorIndex(pitchDegrees)];
            horizontalBoost = Math.max(rw121YawMultipliers[getAngleSectorIndex(yawDegrees)], verticalBoost);
            break;
        case "BravoHvH":
            verticalBoost = bravoPitchMultipliers[getAngleSectorIndex(pitchDegrees)];
            horizontalBoost = Math.max(bravoYawMultipliers[getAngleSectorIndex(yawDegrees)], verticalBoost);
            break;
        case "Super Firework":
            horizontalBoost = calculateSuperFireworkMultiplier(yawDegrees, pitchDegrees) + this.boostStrengthSetting.getValue().floatValue() - 1.5;
            verticalBoost = horizontalBoost;
            break;
        case "Advanced":
        case "Advanced Stable":
            boolean stableMode = this.modeSetting.isSelected("Advanced Stable");
            float normalizedYaw = normalizeYawAngle(yawDegrees);
            float absolutePitch = Math.abs(pitchDegrees);
            calculateAdvancedBoostMultiplier(normalizedYaw, absolutePitch);
            if (stableMode) {
            }

            horizontalBoost = 0.98F;
            verticalBoost = calculateAdvancedPitchMultiplier(absolutePitch, stableMode);
            break;
        default:
            horizontalBoost = this.boostStrengthSetting.getValue().floatValue();
            verticalBoost = horizontalBoost;
        }

        return new Vec3d(horizontalBoost, verticalBoost, horizontalBoost);
    }

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void onPacketReceived(EventPacket eventPacket) {
        if (eventPacket.isReceive() && eventPacket.packet() instanceof PlayerPositionLookS2CPacket) {
            CLIENT.execute(() -> {
                if (this.timerAlternationCounter % 2 == 1) {
                    this.timerAlternationCounter++;
                }
            });
        }
    }

    public static float getElytraTimerMultiplier() {
        ElytraBoost elytraBoostModule = (ElytraBoost)ModuleRegistry.getInstance().getModuleByClass(ElytraBoost.class);
        return elytraBoostModule != null && elytraBoostModule.isEnabled() && elytraBoostModule.elytraTimerActive
        ? elytraBoostModule.timerAlternationCounter % 2 == 0 ? elytraBoostModule.elytraTimerMultiplier.getValue() : elytraBoostModule.cooldown.getValue()
        : 1.0F;
    }

    private static int getAngleSectorIndex(float angleDegrees) {
        float foldedAngleDegrees = Math.abs(MathHelper.wrapDegrees(angleDegrees));
        if (foldedAngleDegrees > 90.0F) {
            foldedAngleDegrees = 180.0F - foldedAngleDegrees;
        }

        if (foldedAngleDegrees > 45.0F) {
            foldedAngleDegrees = 90.0F - foldedAngleDegrees;
        }

        return MathHelper.clamp((int)(foldedAngleDegrees / 5.0F), 0, 8);
    }

    private static double calculateSuperFireworkMultiplier(float yawDegrees, float pitchDegrees) {
        double normalizedYawDegrees = Math.abs(yawDegrees % 360.0F);
        if (normalizedYawDegrees > 180.0) {
            normalizedYawDegrees = 360.0 - normalizedYawDegrees;
        }

        double distanceToNearestBoostAngle = Math.min(Math.abs(normalizedYawDegrees - 45.0), Math.abs(normalizedYawDegrees - 135.0));
        double yawMultiplier = 1.0 + 0.47 * Math.exp(-(distanceToNearestBoostAngle * distanceToNearestBoostAngle) / 288.0);
        double pitchMultiplier;
        if (pitchDegrees >= -45.0F && pitchDegrees <= 20.0F) {
            double adjustedPitchDegrees = pitchDegrees + 10.0;
            pitchMultiplier = 1.0 + 0.05 * Math.exp(-(adjustedPitchDegrees * adjustedPitchDegrees) / 800.0);
        } else if (pitchDegrees > 20.0F) {
            pitchMultiplier = 1.0 - Math.min(0.35, (pitchDegrees - 20.0) / 70.0 * 0.35);
        } else {
            pitchMultiplier = 1.0 - Math.min(0.3, (Math.abs(pitchDegrees) - 45.0) / 45.0 * 0.3);
        }

        return Math.clamp(yawMultiplier * pitchMultiplier, 0.65, 1.49) + 1.5 - 1.49;
    }

    private static float normalizeYawAngle(float yawDegrees) {
        float normalizedYawDegrees = yawDegrees % 180.0F;
        if (normalizedYawDegrees > 90.0F) {
            normalizedYawDegrees -= 180.0F;
        } else if (normalizedYawDegrees < -90.0F) {
            normalizedYawDegrees += 180.0F;
        }

        return Math.abs(normalizedYawDegrees);
    }

    private static double calculateAdvancedBoostMultiplier(float yawDegrees, float pitchDegrees) {
        int roundedYawDegrees = (int)Math.ceil(yawDegrees);
        double boostMultiplier;
        if (pitchDegrees >= 40.0F && pitchDegrees <= 50.0F) {
            boostMultiplier = 2.0;
        } else if (pitchDegrees >= 38.0F && pitchDegrees <= 52.0F) {
            boostMultiplier = 1.98;
        } else if (pitchDegrees >= 32.0F && pitchDegrees <= 58.0F) {
            boostMultiplier = 1.97;
        } else if (matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 33, 57) || matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 34, 56)) {
            boostMultiplier = 1.964;
        } else if (matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 35, 55) || matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 36, 54)) {
            boostMultiplier = 1.965;
        } else if (matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 37, 53) || matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 38, 52) || matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 39, 51)) {
            boostMultiplier = 1.966;
        } else if (matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 40, 50)) {
            boostMultiplier = 1.967;
        } else if (matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 41, 49)) {
            boostMultiplier = 1.968;
        } else if (matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 42, 48) || matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 43, 47)) {
            boostMultiplier = 1.969;
        } else if (matchesAngleBoundary(roundedYawDegrees, pitchDegrees, 44, 46) || roundedYawDegrees == 45 || pitchDegrees == 45.0F) {
            boostMultiplier = 1.9695;
        } else if (isAngleWithinRange(roundedYawDegrees, pitchDegrees, 29, 61)) {
            boostMultiplier = 1.963;
        } else if (isAngleWithinRange(roundedYawDegrees, pitchDegrees, 27, 63)) {
            boostMultiplier = 1.84;
        } else if (isAngleWithinRange(roundedYawDegrees, pitchDegrees, 26, 64)) {
            boostMultiplier = 1.8;
        } else if (isAngleWithinRange(roundedYawDegrees, pitchDegrees, 15, 75)) {
            boostMultiplier = 1.74;
        } else if (isAngleWithinRange(roundedYawDegrees, pitchDegrees, 13, 77)) {
            boostMultiplier = 1.7;
        } else if (isAngleWithinRange(roundedYawDegrees, pitchDegrees, 12, 78)) {
            boostMultiplier = 1.671;
        } else {
            boostMultiplier = 1.626;
        }

        if (boostMultiplier < 1.9 && pitchDegrees > 10.0F) {
            boostMultiplier += 0.05;
        }

        return boostMultiplier;
    }

    private static boolean matchesAngleBoundary(int roundedYawDegrees, float pitchDegrees, int firstBoundary, int secondBoundary) {
        return roundedYawDegrees == firstBoundary || roundedYawDegrees == secondBoundary || pitchDegrees == firstBoundary || pitchDegrees == secondBoundary;
    }

    private static boolean isAngleWithinRange(int roundedYawDegrees, float pitchDegrees, int minimumAngle, int maximumAngle) {
        return roundedYawDegrees >= minimumAngle && roundedYawDegrees <= maximumAngle || pitchDegrees >= minimumAngle && pitchDegrees <= maximumAngle;
    }

    private static double calculateAdvancedPitchMultiplier(float pitchDegrees, boolean stableMode) {
        if (pitchDegrees >= 30.0F && pitchDegrees <= 40.0F) {

            if (stableMode) {
            }

            return 0.95F;
        } else if (pitchDegrees >= 35.0F && pitchDegrees <= 45.0F) {

            if (stableMode) {
            }

            return 0.95F;
        } else if (pitchDegrees >= 40.0F && pitchDegrees <= 50.0F) {

            if (stableMode) {
            }

            return 0.95F;
        } else if (pitchDegrees >= 50.0F && pitchDegrees <= 60.0F) {

            if (stableMode) {
            }

            return 0.95F;
        } else if (pitchDegrees >= 51.0F && pitchDegrees <= 61.0F) {

            if (stableMode) {
            }

            return 0.98F;
        } else {
            return pitchDegrees >= 52.0F && pitchDegrees <= 65.0F ? 1.7 : 1.6;
        }
    }
}

