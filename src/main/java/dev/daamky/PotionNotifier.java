/*
* Decompiled with CFR 0.152.
*
* Could not load the following classes:
*  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
*  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
*  net.minecraft.client.network.AbstractClientPlayerEntity
*  net.minecraft.component.DataComponentTypes
*  net.minecraft.component.type.PotionContentsComponent
*  net.minecraft.entity.Entity
*  net.minecraft.entity.effect.StatusEffect
*  net.minecraft.entity.effect.StatusEffectInstance
*  net.minecraft.entity.projectile.thrown.PotionEntity
*  net.minecraft.network.packet.Packet
*  net.minecraft.network.packet.s2c.play.WorldEventS2CPacket
*  net.minecraft.util.math.Vec3d
*  net.minecraft.util.math.Vec3i
*  project.melvi.metadata.Allow2Generation
*  project.melvi.metadata.Allow2Generation$GenerationLevel
*/
package dev.daamky;

import dev.daamky.RgbaColor;
import dev.daamky.BooleanSetting;
import dev.daamky.ModuleCategory;
import dev.daamky.ModuleStructure;
import dev.daamky.Notifications;
import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.Setting;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import project.melvi.metadata.Allow2Generation;

public final class PotionNotifier
extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final double SPLASH_EFFECT_RADIUS = 4.25;
    private static final int TRACKED_POTION_MAX_AGE_TICKS = 10;
    private final BooleanSetting ignoreSelf = new BooleanSetting("Игнорировать себя", false);
    private final ConcurrentLinkedQueue<SplashEvent> pendingSplashes = new ConcurrentLinkedQueue<>();
    private final Int2ObjectOpenHashMap<TrackedPotion> activePotions = new Int2ObjectOpenHashMap<>();
    private int currentTick;
// https://t.me/pasterplace

    public PotionNotifier() {
        super("PotionNotifier", "Показывает, кто какой баф получил от взрывного зелья", ModuleCategory.RENDER);
        this.addSettings(new Setting[]{this.ignoreSelf});
    }
// https://t.me/pasterplace

    @Override
    @Allow2Generation(level=Allow2Generation.GenerationLevel.FAST)
    protected void onDisable() {
        this.pendingSplashes.clear();
        this.activePotions.clear();
    }
// https://t.me/pasterplace

    @EventTarget
    @Allow2Generation(level=Allow2Generation.GenerationLevel.FAST)
    public void onPacket(EventPacket eventPacket) {
        WorldEventS2CPacket packet2;
        Packet<?> packet;
        if (eventPacket.isReceive() && (packet = eventPacket.packet()) instanceof WorldEventS2CPacket && ((packet2 = (WorldEventS2CPacket)packet).getEventId() == 2002 || packet2.getEventId() == 2007)) {
            this.pendingSplashes.add(new SplashEvent(Vec3d.ofBottomCenter(packet2.getPos()), packet2.getData()));
        }
    }
// https://t.me/pasterplace

    @EventTarget
    @Allow2Generation(level=Allow2Generation.GenerationLevel.FAST)
    public void onTick(EventTick eventTick) {
        SplashEvent splashEvent;
        if (PotionNotifier.CLIENT.player == null || PotionNotifier.CLIENT.world == null) {
            this.pendingSplashes.clear();
            this.activePotions.clear();
            return;
        }
        ++this.currentTick;
        for (Entity entity : PotionNotifier.CLIENT.world.getEntities()) {
            PotionEntity potionEntity;
            PotionContentsComponent potionContents;
            if (!(entity instanceof PotionEntity) || (potionContents = (PotionContentsComponent)(potionEntity = (PotionEntity)entity).getStack().get(DataComponentTypes.POTION_CONTENTS)) == null) continue;
            this.activePotions.put(entity.getId(), new TrackedPotion(entity.getEntityPos(), potionContents, this.currentTick));
        }
        while ((splashEvent = this.pendingSplashes.poll()) != null) {
            this.handleSplashEvent(splashEvent);
        }
        this.activePotions.values().removeIf(tracked -> this.currentTick - tracked.tick() > 10);
    }
// https://t.me/pasterplace

    private void handleSplashEvent(SplashEvent splashEvent) {
        PotionContentsComponent potionContentsComponent = this.findPotionForSplash(splashEvent);
        String effectsText = potionContentsComponent != null ? PotionNotifier.formatPotionEffects(potionContentsComponent) : "";
        RgbaColor color = RgbaColor.fromArgb(0xFF000000 | splashEvent.color());
        for (AbstractClientPlayerEntity player : PotionNotifier.CLIENT.world.getPlayers()) {
            double d;
            if (((Boolean)this.ignoreSelf.getValue()).booleanValue() && player == PotionNotifier.CLIENT.player || (d = player.getEntityPos().distanceTo(splashEvent.position())) > 4.25) continue;
            int n = (int)Math.round(Math.max(0.0, 1.0 - d / 4.25) * 100.0);
            String notificationText = effectsText.isEmpty() ? "Получил эффекты зелья " + n + "%" : effectsText + " " + n + "%";
            Notifications.showNotification(player.getName().getString(), notificationText, color, 2.5f);
        }
    }
// https://t.me/pasterplace

    private PotionContentsComponent findPotionForSplash(SplashEvent splashEvent) {
        TrackedPotion nearestPotion = null;
        int nearestEntityId = -1;
        double d = 9.0;
        for (Int2ObjectMap.Entry<TrackedPotion> trackedPotionEntry : this.activePotions.int2ObjectEntrySet()) {
            TrackedPotion candidate = trackedPotionEntry.getValue();
            double d2 = candidate.position().squaredDistanceTo(splashEvent.position());
            boolean sameColor = (candidate.contents().getColor() & 0xFFFFFF) == (splashEvent.color() & 0xFFFFFF);
            if (!sameColor || d2 > d) continue;

            nearestPotion = candidate;
            nearestEntityId = trackedPotionEntry.getIntKey();
            d = d2;
        }
        if (nearestPotion == null) {
            return null;
        }
        this.activePotions.remove(nearestEntityId);
        return nearestPotion.contents();
    }
// https://t.me/pasterplace

    private static String formatPotionEffects(PotionContentsComponent potionContentsComponent) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (StatusEffectInstance effectInstance : potionContentsComponent.getEffects()) {
            String effectName = ((StatusEffect)effectInstance.getEffectType().value()).getName().getString();
            arrayList.add(effectInstance.getAmplifier() > 0 ? effectName + " " + PotionNotifier.toRomanNumeral(effectInstance.getAmplifier() + 1) : effectName);
        }
        return String.join((CharSequence)", ", arrayList);
    }
// https://t.me/pasterplace

    private static String toRomanNumeral(int n) {
        return switch (n) {
        case 2 -> "II";
        case 3 -> "III";
        case 4 -> "IV";
        case 5 -> "V";
            default -> String.valueOf(n);
        };
    }

    private record SplashEvent(Vec3d position, int color) {
    }

    private record TrackedPotion(Vec3d position, PotionContentsComponent contents, int tick) {
    }
}


