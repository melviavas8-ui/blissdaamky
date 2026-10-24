package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class KillEffect extends ModuleStructure {
    private static final long RECENT_ATTACK_EXPIRY_MILLIS = 3000L;
    private final MultiOptionSetting effectType = new MultiOptionSetting("Эффект", "Молния", "Молния", "Взрыв душ", "Тотем", "Спираль");
    private final FloatSetting particleCount = new FloatSetting("Кол-во частиц", 40.0F, 10.0F, 150.0F, 5.0F);
    private final Map<Integer, Long> recentlyAttackedEntityTimestamps = new HashMap<>();

    public KillEffect() {
        super("KillEffect", "Визуальный эффект при убийстве", ModuleCategory.RENDER);
        this.particleCount.visibleWhen(() -> !this.effectType.isSelected("Молния"));
        this.addSettings(this.effectType, this.particleCount);
    }

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onDisable() {
        this.recentlyAttackedEntityTimestamps.clear();
    }

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void onWorldChange(EventWorldChange eventWorldChange) {
        this.recentlyAttackedEntityTimestamps.clear();
    }

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void onAttack(EventAttack eventAttack) {
        Entity entity = eventAttack.target();
        if (entity instanceof LivingEntity) {
            Entity entity2 = (LivingEntity)entity;
            if (entity2 != CLIENT.player) {
                this.recentlyAttackedEntityTimestamps.put(entity2.getId(), System.currentTimeMillis());
            }
        }
    }

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void onUpdate(EventUpdate eventUpdate) {
        if (this.hasActiveClientWorld() && !this.recentlyAttackedEntityTimestamps.isEmpty()) {
            ClientWorld clientWorld = CLIENT.world;
            long currentTimeMillis = System.currentTimeMillis();
            Iterator<Map.Entry<Integer, Long>> attackTimestampIterator = this.recentlyAttackedEntityTimestamps.entrySet().iterator();

            while (attackTimestampIterator.hasNext()) {
                Map.Entry<Integer, Long> attackTimestampEntry = attackTimestampIterator.next();
                if (currentTimeMillis - attackTimestampEntry.getValue() <= 3000L) {
                    Entity entity = clientWorld.getEntityById(attackTimestampEntry.getKey());
                    if (!(entity instanceof LivingEntity livingEntity)) {
                        attackTimestampIterator.remove();
                    } else if (!livingEntity.isAlive()) {
                        attackTimestampIterator.remove();
                        this.spawnKillEffect(clientWorld, livingEntity);
                    }
                } else {
                    attackTimestampIterator.remove();
                }
            }
        }
    }

    private void spawnKillEffect(ClientWorld clientWorld, LivingEntity livingEntity) {
        Vec3d entityPosition = livingEntity.getEntityPos();
        float entityHeight = livingEntity.getHeight();
        int particleCount = Math.round(this.particleCount.getValue());
        switch ((String)this.effectType.getValue()) {
        case "Молния":
            LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(clientWorld, SpawnReason.TRIGGERED);
            if (lightningEntity != null) {
                lightningEntity.setCosmetic(true);
                lightningEntity.refreshPositionAfterTeleport(entityPosition);
                clientWorld.addEntity(lightningEntity);
            }
            break;
        case "Взрыв душ":
            for (int particleIndex = 0; particleIndex < particleCount; particleIndex++) {
                double velocityX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3;
                double velocityY = ThreadLocalRandom.current().nextDouble() * 0.25;
                double velocityZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.3;
                spawnParticle(
                clientWorld,
                particleIndex % 2 == 0 ? ParticleTypes.SOUL : ParticleTypes.SOUL_FIRE_FLAME,
                entityPosition.x,
                entityPosition.y + entityHeight * ThreadLocalRandom.current().nextDouble(),
                entityPosition.z,
                velocityX,
                velocityY,
                velocityZ
                );
            }
            break;
        case "Тотем":
            for (int particleIndex = 0; particleIndex < particleCount; particleIndex++) {
                double velocityX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.8;
                double velocityY = ThreadLocalRandom.current().nextDouble() * 0.8;
                double velocityZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.8;
                spawnParticle(
                clientWorld, ParticleTypes.TOTEM_OF_UNDYING, entityPosition.x, entityPosition.y + entityHeight * 0.5, entityPosition.z, velocityX, velocityY, velocityZ
                );
            }
            break;
        default:
            for (int particleIndex = 0; particleIndex < particleCount; particleIndex++) {
                float heightProgress = (float)particleIndex / particleCount;
                float spiralAngle = heightProgress * (float) (Math.PI * 2) * 3.0F;

                spawnParticle(
                clientWorld,
                ParticleTypes.END_ROD,
                entityPosition.x + MathHelper.cos(spiralAngle) * 0.6,
                entityPosition.y + heightProgress * entityHeight * 1.5,
                entityPosition.z + MathHelper.sin(spiralAngle) * 0.6,
                MathHelper.cos(spiralAngle) * 0.05,
                0.02,
                MathHelper.sin(spiralAngle) * 0.05
                );
            }
        }
    }

    private static void spawnParticle(
    ClientWorld clientWorld, ParticleEffect particleEffect, double x, double y, double z, double velocityX, double velocityY, double velocityZ
    ) {
        clientWorld.addParticleClient(particleEffect, x, y, z, velocityX, velocityY, velocityZ);
    }
}

