package dev.daamky;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public class JumpCircle extends ModuleStructure {
    public static JumpCircle INSTANCE;
    public final FloatSetting circleRadius = new FloatSetting("Radius", 1.6F, 1.0F, 5.0F, 0.1F);
    public final FloatSetting circleDurationSeconds = new FloatSetting("Duration", 0.9F, 0.2F, 3.0F, 0.1F, " s");
    public final BooleanSetting synchronizeThemeColor = new BooleanSetting("Sync Theme", true);
    public final MultiOptionSetting circleColorSetting = new MultiOptionSetting("Color", "Blue", "Blue", "Purple", "Cyan", "Red", "White");
    private final List<JumpCircle.JumpCircleEntry> circleEntries = new CopyOnWriteArrayList<>();

    public JumpCircle() {
        super("JumpCircle", "Expanding circles upon jumping", ModuleCategory.RENDER);

        this.circleColorSetting.visibleWhen(() -> !this.synchronizeThemeColor.getValue());
        this.addSettings(this.circleRadius, this.circleDurationSeconds, this.synchronizeThemeColor, this.circleColorSetting);
    }

    public static JumpCircle getInstance() {
        return INSTANCE != null ? INSTANCE : ModuleRegistry.getInstance().getModuleByClass(JumpCircle.class);
    }

    public boolean isPostEffectEnabled() {
        return true;
    }

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onDisable() {
        this.circleEntries.clear();
        WorldJumpCircleUniforms.getInstance().close();
    }

    public List<JumpCircle.JumpCircleEntry> getCircleEntries() {
        return Collections.unmodifiableList(this.circleEntries);
    }

    public boolean hasActiveCircles() {
        return this.isEnabled() && !this.circleEntries.isEmpty();
    }

    public void removeExpiredCircles() {
        long currentTimeMillis = System.currentTimeMillis();
        this.circleEntries.removeIf(circleEntry -> currentTimeMillis - circleEntry.startTimeMillis >= circleEntry.durationMillis);
    }

    public int getCircleColor() {
        if (this.synchronizeThemeColor.getValue()) {
            return Accent.getInstance().getColor(Accent.ColorRole.ACCENT).toArgb();
        }

        return switch ((String)this.circleColorSetting.getValue()) {
        case "Purple", "Фиолетовый" -> -6598176;
        case "Blue", "Синий" -> -11104513;
        case "Cyan", "Бирюзовый" -> -16718337;
        case "Red", "Красный" -> -46261;
        case "White", "Белый" -> -1;
            default -> -9863681;
        };
    }

    public static void addCircleForLocalPlayer(LivingEntity livingEntity) {
        if (INSTANCE != null && INSTANCE.isEnabled()) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.player != null && livingEntity != null) {
                if (livingEntity == client.player) {
                    Vec3d playerPosition = client.player.getEntityPos();
                    int colorArgb = INSTANCE.getCircleColor();
                    long durationMillis = (long)(INSTANCE.circleDurationSeconds.getValue() * 1000.0F);
                    INSTANCE.circleEntries
                    .add(new JumpCircle.JumpCircleEntry(playerPosition, System.currentTimeMillis(), durationMillis, INSTANCE.circleRadius.getValue(), 0.35F, 0.0F, 1.0F, colorArgb));
                }
            }
        }
    }

    public static void addCircleForCurrentPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            addCircleForLocalPlayer(client.player);
        }
    }

    public static class JumpCircleEntry {
        private final Vec3d position;
        private final long startTimeMillis;
        private final long durationMillis;
        private final float radius;
        private final float effectParameterX;
        private final float glowPower;
        private final float ringWidth;
        private final int colorArgb;

        public JumpCircleEntry(Vec3d position, long startTimeMillis, long durationMillis, float radius, float effectParameterX, float glowPower, float ringWidth, int colorArgb) {
            this.position = position;
            this.startTimeMillis = startTimeMillis;
            this.durationMillis = durationMillis;
            this.radius = radius;
            this.effectParameterX = effectParameterX;
            this.glowPower = glowPower;
            this.ringWidth = ringWidth;
            this.colorArgb = colorArgb;
        }

        public Vec3d getPosition() {
            return this.position;
        }

        public long getStartTimeMillis() {
            return this.startTimeMillis;
        }

        public long getDurationMillis() {
            return this.durationMillis;
        }

        public float getRadius() {
            return this.radius;
        }

        public float getEffectParameterX() {
            return this.effectParameterX;
        }

        public float getGlowPower() {
            return this.glowPower;
        }

        public float getRingWidth() {
            return this.ringWidth;
        }

        public int getColorArgb() {
            return this.colorArgb;
        }

        public float getLifeProgress() {
            long elapsedMillis = System.currentTimeMillis() - this.startTimeMillis;
            return Math.min(1.0F, (float)elapsedMillis / (float)this.durationMillis);
        }
    }
}

