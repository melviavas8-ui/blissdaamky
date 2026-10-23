package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventDisplay;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.core.event.impl.EventUpdate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoLeave extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private final BooleanSetting autoLeaveEnabled = new BooleanSetting("Авто-выход", true);
    private final MultiOptionSetting leaveAction = new MultiOptionSetting("Действие", "Выход", "Выход", "/spawn", "/hub", "/home");
    private final FloatSetting playerLeaveDistance = new FloatSetting("Дистанция", 30.0F, 5.0F, 128.0F, 1.0F, "бл.");
    private final BooleanSetting damageIndicatorEnabled = new BooleanSetting("Индикатор урона", false);
    private final FloatSetting indicatorDistanceFromCenter = new FloatSetting("Дистанция от прицела", 38.0F, 10.0F, 150.0F, 1.0F);
    private final FloatSetting indicatorArcWidth = new FloatSetting("Ширина дуги", 60.0F, 15.0F, 160.0F, 1.0F);
    private final FloatSetting indicatorThickness = new FloatSetting("Толщина", 3.0F, 1.0F, 10.0F, 0.5F);
    private final FloatSetting indicatorDurationMillis = new FloatSetting("Длительность", 1500.0F, 250.0F, 5000.0F, 50.0F, "мс");
    private final FloatSetting trackedPlayerRadius = new FloatSetting("Радиус событий", 30.0F, 4.0F, 64.0F, 1.0F);
    private final BooleanSetting showIncomingDamage = new BooleanSetting("Когда бьют тебя", true);
    private final BooleanSetting showOutgoingDamage = new BooleanSetting("Когда бьют врага", true);
    private final BooleanSetting showHealing = new BooleanSetting("Хил", true);
    private final BooleanSetting ignoreFriends = new BooleanSetting("Игнорировать друзей", true);
    private final MultiOptionSetting indicatorColorMode = new MultiOptionSetting("Цвет", "Урон/Хил", "Урон/Хил", "Тема");
    private final List<AutoLeave.DamageIndicatorEvent> damageIndicatorEvents = new ArrayList<>();
    private final Map<UUID, Float> previousHealthByUuid = new HashMap<>();
    private float previousLocalPlayerHealth = Float.NaN;
// https://t.me/pasterplace

    public AutoLeave() {
        super("AutoLeave", "Покидает сервер при появлении игрока и показывает индикатор урона", ModuleCategory.PLAYER);
        this.leaveAction.visibleWhen(this.autoLeaveEnabled::getValue);
        this.playerLeaveDistance.visibleWhen(this.autoLeaveEnabled::getValue);
        this.indicatorDistanceFromCenter.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.indicatorArcWidth.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.indicatorThickness.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.indicatorDurationMillis.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.trackedPlayerRadius.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.showIncomingDamage.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.showOutgoingDamage.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.showHealing.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.ignoreFriends.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.indicatorColorMode.visibleWhen(this.damageIndicatorEnabled::getValue);
        this.addSettings(
        this.autoLeaveEnabled,
        this.leaveAction,
        this.playerLeaveDistance,
        this.damageIndicatorEnabled,
        this.indicatorDistanceFromCenter,
        this.indicatorArcWidth,
        this.indicatorThickness,
        this.indicatorDurationMillis,
        this.trackedPlayerRadius,
        this.showIncomingDamage,
        this.showOutgoingDamage,
        this.showHealing,
        this.ignoreFriends,
        this.indicatorColorMode
        );
    }
// https://t.me/pasterplace

    @Override
    public String getDisplayInfo() {
        return this.autoLeaveEnabled.getValue() ? this.leaveAction.getValue() : null;
    }
// https://t.me/pasterplace

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onEnable() {
        this.resetTrackingState();
    }
// https://t.me/pasterplace

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onDisable() {
        this.resetTrackingState();
    }
// https://t.me/pasterplace

    private void resetTrackingState() {
        this.damageIndicatorEvents.clear();
        this.previousHealthByUuid.clear();
        this.previousLocalPlayerHealth = Float.NaN;
    }
// https://t.me/pasterplace

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void detectNearbyPlayer(EventUpdate eventUpdate) {
        if (this.autoLeaveEnabled.getValue() && this.hasActiveClientWorld()) {
            for (PlayerEntity targetPlayer : CLIENT.world.getPlayers()) {
                if (targetPlayer != CLIENT.player
                && !FriendManager.getInstance().isFriend(targetPlayer)
                && !AntiBot.II1Il11l111II11IIl(targetPlayer)
                && !(targetPlayer.distanceTo(CLIENT.player) > this.playerLeaveDistance.getValue())) {
                    String nearbyPlayerName = targetPlayer.getGameProfile().name();
                    if (!this.leaveAction.isSelected("Выход")) {
                        CLIENT.getNetworkHandler().sendChatCommand(this.leaveAction.getValue().substring(1));
                    } else {
                        CLIENT.getNetworkHandler().getConnection().disconnect(Text.literal("[AutoLeave] Рядом игрок " + nearbyPlayerName));
                    }

                    this.setEnabled(false);
                    return;
                }
            }
        }
    }
// https://t.me/pasterplace

    private boolean isEligibleForIndicator(AbstractClientPlayerEntity entity) {
        return entity != CLIENT.player
        && entity.isAlive()
        && CLIENT.player.distanceTo(entity) <= this.trackedPlayerRadius.getValue()
        && (!this.ignoreFriends.getValue() || !FriendManager.getInstance().isFriend(entity));
    }
// https://t.me/pasterplace

    private LivingEntity findNearestTrackedPlayer() {
        Object nearestPlayer = null;
        double nearestDistance = this.trackedPlayerRadius.getValue().floatValue();

        for (Object candidatePlayer : CLIENT.world.getPlayers()) {
            if (this.isEligibleForIndicator((AbstractClientPlayerEntity)candidatePlayer)) {
                double candidateDistance = CLIENT.player.distanceTo((Entity)candidatePlayer);
                if (candidateDistance < nearestDistance) {
                    nearestDistance = candidateDistance;
                    nearestPlayer = candidatePlayer;
                }
            }
        }

        return (LivingEntity)nearestPlayer;
    }
// https://t.me/pasterplace

    @EventTarget
    public void updateHealthHistory(EventTick eventTick) {
        if (this.damageIndicatorEnabled.getValue() && CLIENT.player != null && CLIENT.world != null) {
            float currentHealth = CLIENT.player.getHealth() + CLIENT.player.getAbsorptionAmount();
            if (!Float.isNaN(this.previousLocalPlayerHealth) && CLIENT.player.isAlive() && this.previousLocalPlayerHealth > 0.5F) {
                float healthChange = currentHealth - this.previousLocalPlayerHealth;
                if (healthChange < -0.05F && this.showIncomingDamage.getValue()) {
                    this.recordDamageIndicator(false, -healthChange, this.findNearestTrackedPlayer());
                } else if (healthChange > 0.05F && this.showHealing.getValue()) {
                    this.recordDamageIndicator(true, healthChange, null);
                }
            }

            this.previousLocalPlayerHealth = currentHealth;
            HashMap<UUID, Float> currentHealthByUuid = new HashMap<>();
// https://t.me/pasterplace

            for (AbstractClientPlayerEntity trackedPlayer : CLIENT.world.getPlayers()) {
                if (this.isEligibleForIndicator(trackedPlayer)) {
                    float trackedPlayerHealth = trackedPlayer.getHealth() + trackedPlayer.getAbsorptionAmount();
                    currentHealthByUuid.put(trackedPlayer.getUuid(), trackedPlayerHealth);
                    Float previousHealth = this.previousHealthByUuid.get(trackedPlayer.getUuid());
                    if (previousHealth != null) {
                        float healthChange = trackedPlayerHealth - previousHealth;
                        if (healthChange < -0.05F && this.showOutgoingDamage.getValue()) {
                            this.recordDamageIndicator(false, -healthChange, trackedPlayer);
                        } else if (healthChange > 0.05F && previousHealth > 0.5F && this.showHealing.getValue()) {
                            this.recordDamageIndicator(true, healthChange, trackedPlayer);
                        }
                    }
                }
            }

            this.previousHealthByUuid.clear();
            this.previousHealthByUuid.putAll(currentHealthByUuid);
        } else {
            this.resetTrackingState();
        }
    }
// https://t.me/pasterplace

    private void recordDamageIndicator(boolean healing, float healthChange, LivingEntity targetEntity) {
        long eventTimestampMillis = System.currentTimeMillis();
        if (!this.damageIndicatorEvents.isEmpty()) {
            Object previousEvent = this.damageIndicatorEvents.get(this.damageIndicatorEvents.size() - 1);
            if (eventTimestampMillis - ((AutoLeave.DamageIndicatorEvent)previousEvent).timestampMillis <= 130L
            && ((AutoLeave.DamageIndicatorEvent)previousEvent).healing == healing
            && ((AutoLeave.DamageIndicatorEvent)previousEvent).entity == targetEntity) {
                ((AutoLeave.DamageIndicatorEvent)previousEvent).healthChange += healthChange;
                return;
            }
        }

        this.damageIndicatorEvents.add(new AutoLeave.DamageIndicatorEvent(eventTimestampMillis, healing, healthChange, targetEntity));

        while (this.damageIndicatorEvents.size() > 24) {
            this.damageIndicatorEvents.remove(0);
        }
    }
// https://t.me/pasterplace

    private float getEntityYawRelativeToCamera(LivingEntity entity, float tickDelta) {
        double relativeX = entity.lastX + (entity.getX() - entity.lastX) * tickDelta - CLIENT.player.getX();
        double relativeZ = entity.lastZ + (entity.getZ() - entity.lastZ) * tickDelta - CLIENT.player.getZ();
        float cameraYaw = CLIENT.gameRenderer.getCamera().getYaw();
        float entityYaw = (float)Math.toDegrees(Math.atan2(relativeZ, relativeX)) - 90.0F;
        return entityYaw - cameraYaw - 90.0F;
    }
// https://t.me/pasterplace

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void renderDamageIndicators(EventDisplay eventDisplay) {
        if (this.damageIndicatorEnabled.getValue()
        && CLIENT.player != null
        && CLIENT.world != null
        && !CLIENT.options.hudHidden
        && !this.damageIndicatorEvents.isEmpty()) {
            long nowMillis = System.currentTimeMillis();
            float durationMillis = this.indicatorDurationMillis.getValue();
            this.damageIndicatorEvents.removeIf(expiredEvent -> (float)(nowMillis - expiredEvent.timestampMillis) >= durationMillis);
            DrawContext drawContext = eventDisplay.graphics();
            float viewportCenterX = GuiEventRenderer.getScreenWidth() * 0.5F;
            float viewportCenterY = GuiEventRenderer.getScreenHeight() * 0.5F;
            float tickDelta = CLIENT.getRenderTickCounter().getTickProgress(true);

            for (Object damageEvent : this.damageIndicatorEvents) {
                float eventAgeProgress = (float)(nowMillis - ((AutoLeave.DamageIndicatorEvent)damageEvent).timestampMillis) / durationMillis;
                float animationOpacity;
                if (!(
                (
                animationOpacity = Math.clamp((float)(nowMillis - ((AutoLeave.DamageIndicatorEvent)damageEvent).timestampMillis) / 60.0F, 0.0F, 1.0F)
                * (1.0F - eventAgeProgress)
                * (1.0F - eventAgeProgress)
                )
                <= 0.01F
                )) {
                    float indicatorYaw;
                    if (((AutoLeave.DamageIndicatorEvent)damageEvent).entity != null
                    && ((AutoLeave.DamageIndicatorEvent)damageEvent).entity.isAlive()
                    && !((AutoLeave.DamageIndicatorEvent)damageEvent).entity.isRemoved()) {
                        indicatorYaw = this.getEntityYawRelativeToCamera(((AutoLeave.DamageIndicatorEvent)damageEvent).entity, tickDelta);
                    } else {
                        indicatorYaw = !((AutoLeave.DamageIndicatorEvent)damageEvent).healing ? 90.0F : -90.0F;
                    }

                    float magnitudeScale = Math.clamp(0.4F + ((AutoLeave.DamageIndicatorEvent)damageEvent).healthChange / 14.0F, 0.4F, 1.0F);
                    RgbaColor indicatorColor = this.getIndicatorColor(((AutoLeave.DamageIndicatorEvent)damageEvent).healing, magnitudeScale);
                    this.drawDamageArc(
                    drawContext,
                    viewportCenterX,
                    viewportCenterY,
                    this.indicatorDistanceFromCenter.getValue(),
                    indicatorYaw,
                    this.indicatorArcWidth.getValue() * magnitudeScale,
                    this.indicatorThickness.getValue() * magnitudeScale,
                    indicatorColor,
                    animationOpacity
                    );
                }
            }
        }
    }
// https://t.me/pasterplace

    private RgbaColor getIndicatorColor(boolean healing, float magnitude) {

        this.indicatorColorMode.getValue();
        int scaledMagnitude = (int)(magnitude * 100.0F);
        return healing
        ? RgbaColor.rgb(40 + scaledMagnitude / 3, 160 + scaledMagnitude * 3 / 4, 80 + scaledMagnitude / 3)
        : RgbaColor.rgb(160 + scaledMagnitude * 9 / 10, 20 + scaledMagnitude / 3, 16 + scaledMagnitude / 3);
    }
// https://t.me/pasterplace

    private void drawDamageArc(
    DrawContext drawContext,
    float viewportCenterX,
    float viewportCenterY,
    float radius,
    float centerAngleDegrees,
    float arcWidthDegrees,
    float baseThickness,
    RgbaColor l1I1IlllIlI4,
    float opacity
    ) {
        int segmentCount = Math.max(10, (int)(arcWidthDegrees / 3.0F));
        float arcStartAngleDegrees = centerAngleDegrees - arcWidthDegrees * 0.5F;

        for (int segmentIndex = 0; segmentIndex <= segmentCount; segmentIndex++) {
            float segmentProgress = (float)segmentIndex / segmentCount;
            float edgeFade = 1.0F - Math.abs(segmentProgress * 2.0F - 1.0F);
            edgeFade = edgeFade * edgeFade * (3.0F - 2.0F * edgeFade);
            double angleRadians = Math.toRadians(arcStartAngleDegrees + segmentProgress * arcWidthDegrees);
            float segmentCenterX = viewportCenterX + (float)Math.cos(angleRadians) * radius;
            float segmentCenterY = viewportCenterY + (float)Math.sin(angleRadians) * radius;
            float segmentThickness = baseThickness * (0.55F + 0.45F * edgeFade);
            RoundedGradientRectBuilder.at(segmentCenterX - segmentThickness * 0.5F, segmentCenterY - segmentThickness * 0.5F, segmentThickness, segmentThickness)
            .cornerRadius(segmentThickness * 0.5F)
            .color(l1I1IlllIlI4.withAlpha((int)(235.0F * opacity * edgeFade)))
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    private static final class DamageIndicatorEvent {
        final long timestampMillis;
        final boolean healing;
        final LivingEntity entity;
        float healthChange;

        DamageIndicatorEvent(long timestampMillis, boolean healing, float healthChange, LivingEntity entity) {
            this.timestampMillis = timestampMillis;
            this.healing = healing;
            this.healthChange = healthChange;
            this.entity = entity;
        }
    }
}

