package dev.daamky;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventRender3D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Particles extends ModuleStructure {
    private static final int MAX_PARTICLE_COUNT = 1200;
    private static final float[][][] OCTAHEDRON_MODEL = buildEdgeModel(
    new float[][]{{0.0F, 1.4F, 0.0F}, {0.0F, -1.4F, 0.0F}, {1.0F, 0.0F, 0.0F}, {0.0F, 0.0F, 1.0F}, {-1.0F, 0.0F, 0.0F}, {0.0F, 0.0F, -1.0F}},
    new int[][]{{0, 2}, {0, 3}, {0, 4}, {0, 5}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {2, 3}, {3, 4}, {4, 5}, {5, 2}}
    );
    private static final float[][][] CUBE_MODEL = buildEdgeModel(
    new float[][]{
        {-0.7F, -0.7F, -0.7F},
        {0.7F, -0.7F, -0.7F},
        {0.7F, 0.7F, -0.7F},
        {-0.7F, 0.7F, -0.7F},
        {-0.7F, -0.7F, 0.7F},
        {0.7F, -0.7F, 0.7F},
        {0.7F, 0.7F, 0.7F},
        {-0.7F, 0.7F, 0.7F}
    },
    new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}}
    );
    private static final float[][][] PYRAMID_MODEL = buildEdgeModel(
    new float[][]{{0.0F, 1.1F, 0.0F}, {1.0F, -0.6F, 0.0F}, {-0.5F, -0.6F, 0.87F}, {-0.5F, -0.6F, -0.87F}},
    new int[][]{{0, 1}, {0, 2}, {0, 3}, {1, 2}, {2, 3}, {3, 1}}
    );
    private static final float[][][] CRYSTAL_MODEL = buildEdgeModel(
    new float[][]{
        {0.0F, 0.0F, 0.0F}, {1.4F, 0.0F, 0.0F}, {0.0F, 0.35F, 0.0F}, {-1.4F, 0.0F, 0.0F}, {0.0F, -0.35F, 0.0F}, {0.0F, 0.0F, 0.6F}, {0.0F, 0.0F, -0.6F}
    },
    new int[][]{{1, 2}, {2, 3}, {3, 4}, {4, 1}, {1, 5}, {5, 3}, {3, 6}, {6, 1}, {2, 5}, {5, 4}, {4, 6}, {6, 2}}
    );
    private static final float[][][] SPHERE_MODEL = generateSphereParticleData();
    private static final float[][][] RING_MODEL = generateRingParticleData();
    private static final float[][][][] PARTICLE_MODELS = new float[][][][]{OCTAHEDRON_MODEL, CUBE_MODEL, PYRAMID_MODEL, CRYSTAL_MODEL, SPHERE_MODEL, RING_MODEL};
    private final MultiOptionSetting renderModeSetting = new MultiOptionSetting("Режим", "2D", "2D", "3D");
    private final MultiOptionSetting threeDimensionalShapeSetting = new MultiOptionSetting(
    "Форма 3D", "Микс", "Микс", "Кристалл", "Куб", "Пирамида", "Диск", "Спираль", "Кольца"
    );
    private final MultiOptionSetting twoDimensionalShapeSetting = new MultiOptionSetting(
    "Форма", "Звезда", "Звезда", "Сердце", "Снежинка", "Корона", "Доллар", "Ромб", "Светлячок", "Огонёк", "Тыковка", "Призрак", "Свечение", "Искры"
    );
    private final MultiToggleSetting particleSourcesSetting = new MultiToggleSetting("Источники")
    .addOption("Удар", true)
    .addOption("Тотем", true)
    .addOption("Снаряды", true)
    .addOption("Ходьба", false)
    .addOption("Вокруг игрока", false);
    private final MultiOptionSetting colorModeSetting = new MultiOptionSetting("Цвет", "Тема", "Тема", "Переливание", "Случайный", "Белый");
    private final BooleanSetting physicsEnabledSetting = new BooleanSetting("Физика", true);
    private final BooleanSetting glowEnabledSetting = new BooleanSetting("Свечение", true);
    private final FloatSetting glowRadiusSetting = new FloatSetting("Радиус свечения", 2.5F, 0.5F, 5.0F, 0.1F);
    private final FloatSetting glowStrengthSetting = new FloatSetting("Сила свечения", 2.0F, 0.2F, 4.0F, 0.1F);
    private final FloatSetting particleCountSetting = new FloatSetting("Количество", 14.0F, 2.0F, 50.0F, 1.0F);
    private final FloatSetting particleSizeSetting = new FloatSetting("Размер", 0.18F, 0.05F, 0.6F, 0.01F);
    private final FloatSetting particleLifetimeSetting = new FloatSetting("Время жизни", 1.8F, 0.3F, 6.0F, 0.1F, " с");
    private final FloatSetting particleSpreadSetting = new FloatSetting("Разброс", 1.0F, 0.2F, 3.0F, 0.1F);
    private final List<Particles.Particle> particles = new ArrayList<>();
    private float walkingSpawnAccumulator;
    private float projectileSpawnAccumulator;
    private Vec3d lastWalkingPosition;

    public Particles() {
        super("Particles", "Частицы при ударах, тотемах, снарядах и движении", ModuleCategory.RENDER);
        this.twoDimensionalShapeSetting.visibleWhen(() -> this.renderModeSetting.isSelected("2D"));
        this.threeDimensionalShapeSetting.visibleWhen(() -> this.renderModeSetting.isSelected("3D"));
        this.glowRadiusSetting.visibleWhen(() -> this.renderModeSetting.isSelected("3D") && this.glowEnabledSetting.getValue());
        this.glowStrengthSetting.visibleWhen(() -> this.renderModeSetting.isSelected("3D") && this.glowEnabledSetting.getValue());
        this.addSettings(
        this.renderModeSetting,
        this.twoDimensionalShapeSetting,
        this.threeDimensionalShapeSetting,
        this.particleSourcesSetting,
        this.colorModeSetting,
        this.physicsEnabledSetting,
        this.glowEnabledSetting,
        this.glowRadiusSetting,
        this.glowStrengthSetting,
        this.particleCountSetting,
        this.particleSizeSetting,
        this.particleLifetimeSetting,
        this.particleSpreadSetting
        );
    }

    @Override
    public String getDisplayInfo() {
        return this.renderModeSetting.isSelected("3D") ? this.threeDimensionalShapeSetting.getValue() : this.twoDimensionalShapeSetting.getValue();
    }

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onDisable() {
        this.particles.clear();
        this.lastWalkingPosition = null;
    }

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void onAttack(EventAttack eventAttack) {
        if (eventAttack.isPre() && this.particleSourcesSetting.isOptionEnabled("Удар") && eventAttack.target() != null) {
            Entity attackedEntity = eventAttack.target();
            Vec3d impactPosition = attackedEntity.getEntityPos().add(0.0, attackedEntity.getHeight() * 0.6, 0.0);
            this.spawnParticles(impactPosition, this.particleCountSetting.getValue().intValue(), 0.6, attackedEntity.getWidth() * 0.5);
        }
    }

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void onPacket(EventPacket eventPacket) {
        if (eventPacket.isReceive() && this.particleSourcesSetting.isOptionEnabled("Тотем")) {
            Packet packet = eventPacket.packet();
            if (packet instanceof EntityStatusS2CPacket) {
                EntityStatusS2CPacket statusPacket = (EntityStatusS2CPacket)packet;
                if (statusPacket.getStatus() == 35) {
                    CLIENT.execute(
                    () -> {
                        if (this.isEnabled() && CLIENT.world != null) {
                            Entity totemEntity = statusPacket.getEntity(CLIENT.world);
                            if (totemEntity != null) {
                                this.spawnParticles(
                                totemEntity.getEntityPos().add(0.0, totemEntity.getHeight() * 0.5, 0.0),
                                this.particleCountSetting.getValue().intValue() * 3,
                                0.9,
                                totemEntity.getWidth()
                                );
                            }
                        }
                    }
                    );
                    return;
                }
            }
        }
    }

    private void spawnParticles(Vec3d origin, int particleCount, double launchSpeed, double spawnRadius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double velocitySpread = this.particleSpreadSetting.getValue().floatValue();

        for (int particleIndex = 0; particleIndex < particleCount; particleIndex++) {
            Vec3d velocity = new Vec3d(random.nextGaussian(), random.nextDouble() * 1.2 + 0.2, random.nextGaussian())
            .normalize()
            .multiply(launchSpeed * velocitySpread * (0.5 + random.nextDouble()) * 6.0);
            Vec3d particlePosition = origin.add(
            (random.nextDouble() - 0.5) * spawnRadius, (random.nextDouble() - 0.5) * spawnRadius, (random.nextDouble() - 0.5) * spawnRadius
            );
            this.addParticle(
            new Particles.Particle(particlePosition, velocity, random.nextFloat() * 6.0F, 0.7F + random.nextFloat() * 0.6F)
            );
        }
    }

    private void addParticle(Particles.Particle particle) {
        this.particles.add(particle);
        int excessParticleCount = this.particles.size() - 1200;
        if (excessParticleCount > 0) {
            this.particles.subList(0, excessParticleCount).clear();
        }
    }

    private void spawnWalkingParticles(Vec3d playerPosition, float tickDelta) {
        this.walkingSpawnAccumulator = this.walkingSpawnAccumulator + tickDelta * this.particleCountSetting.getValue();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        while (this.walkingSpawnAccumulator >= 1.0F) {
            this.walkingSpawnAccumulator--;
            Vec3d particlePosition = playerPosition.add(
            (random.nextDouble() - 0.5) * 6.0 * this.particleSpreadSetting.getValue().floatValue(),
            random.nextDouble() * 3.0,
            (random.nextDouble() - 0.5) * 6.0 * this.particleSpreadSetting.getValue().floatValue()
            );
            Vec3d velocity = new Vec3d(
            (random.nextDouble() - 0.5) * 0.3, random.nextDouble() * 0.25, (random.nextDouble() - 0.5) * 0.3
            );
            this.addParticle(
            new Particles.Particle(particlePosition, velocity, random.nextFloat() * 6.0F, 0.6F + random.nextFloat()).markGravityDisabled()
            );
        }
    }

    private void spawnProjectileParticles(float tickDelta) {
        this.projectileSpawnAccumulator += tickDelta * 30.0F;
        if (!(this.projectileSpawnAccumulator < 1.0F)) {
            this.projectileSpawnAccumulator = 0.0F;
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (Entity projectile : CLIENT.world.getEntities()) {
                if (projectile instanceof ProjectileEntity
                && !(projectile.getVelocity().lengthSquared() < 0.01)
                && (!(projectile instanceof FireworkRocketEntity) || !(projectile.squaredDistanceTo(CLIENT.player) < 4.0))) {
                    Vec3d particlePosition = DaamkyWorldRender.getInterpolatedPosition(projectile).add(0.0, projectile.getHeight() * 0.5, 0.0);
                    Vec3d velocity = new Vec3d(random.nextGaussian() * 0.15, random.nextDouble() * 0.2, random.nextGaussian() * 0.15);
                    this.addParticle(
                    new Particles.Particle(particlePosition, velocity, random.nextFloat() * 6.0F, 0.5F + random.nextFloat() * 0.4F)
                    .markGravityDisabled()
                    );
                }
            }
        }
    }

    @EventTarget
    public void renderParticles(EventRender3D renderEvent) {
        if (CLIENT.player != null && CLIENT.world != null) {
            float tickDelta = renderEvent.delta();
            Vec3d playerPosition = DaamkyWorldRender.getInterpolatedPosition(CLIENT.player);
            if (this.particleSourcesSetting.isOptionEnabled("Ходьба")) {
                if (this.lastWalkingPosition != null
                && this.lastWalkingPosition.squaredDistanceTo(playerPosition) > 0.04
                && CLIENT.player.isOnGround()) {
                    this.spawnParticles(playerPosition.add(0.0, 0.1, 0.0), 2, 0.15, 0.4);
                    this.lastWalkingPosition = playerPosition;
                } else if (this.lastWalkingPosition == null) {
                    this.lastWalkingPosition = playerPosition;
                }
            }

            if (this.particleSourcesSetting.isOptionEnabled("Вокруг игрока")) {
                this.spawnWalkingParticles(playerPosition, tickDelta);
            }

            if (this.particleSourcesSetting.isOptionEnabled("Снаряды")) {
                this.spawnProjectileParticles(tickDelta);
            }

            if (!this.particles.isEmpty()) {
                if (this.renderModeSetting.isSelected("3D")) {
                    this.renderThreeDimensionalParticles(tickDelta);
                } else {
                    Identifier textureIdentifier = this.getTextureForShape();
                    GpuTextureView textureView = textureIdentifier != null ? GpuTextureViewCache.II1Il11l111II11IIl(textureIdentifier) : null;
                    BufferBuilder bufferBuilder = textureView != null ? DynamicTransforms.createBufferBuilder(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR) : null;
                    Vector3f[] cameraBasis = DaamkyWorldRender.getCameraPlaneBasis();
                    float lifetimeSeconds = this.particleLifetimeSetting.getValue();
                    float particleSize = this.particleSizeSetting.getValue();
                    int retainedParticleCount = 0;
                    int initialParticleCount = this.particles.size();

                    for (int particleIndex = 0; particleIndex < initialParticleCount; particleIndex++) {
                        Object particleObject = this.particles.get(particleIndex);
                        ((Particles.Particle)particleObject).age += tickDelta;
                        float lifetimeProgress = ((Particles.Particle)particleObject).age
                        / (lifetimeSeconds * ((Particles.Particle)particleObject).scale);
                        if (!(lifetimeProgress >= 1.0F)) {
                            this.particles.set(retainedParticleCount++, (Particles.Particle)particleObject);
                            ((Particles.Particle)particleObject).update(tickDelta);
                            float fadeAlpha = lifetimeProgress < 0.1F ? lifetimeProgress / 0.1F : 1.0F - (lifetimeProgress - 0.1F) / 0.9F;
                            float scaleIn = lifetimeProgress < 0.15F ? 0.4F + 0.6F * (lifetimeProgress / 0.15F) : 1.0F;
                            RgbaColor particleColor = this.getParticleColor((Particles.Particle)particleObject, (int)(235.0F * fadeAlpha));
                            if (bufferBuilder != null) {
                                if (this.glowEnabledSetting.getValue()) {
                                    DaamkyWorldRender.drawBloomRing(
                                    ((Particles.Particle)particleObject).position, particleSize * 1.1F * scaleIn, particleColor.withAlpha((int)(70.0F * fadeAlpha)), false
                                    );
                                }

                                drawBillboardQuad(
                                bufferBuilder,
                                cameraBasis,
                                ((Particles.Particle)particleObject).position,
                                particleSize * scaleIn,
                                ((Particles.Particle)particleObject).rotationAngle
                                + ((Particles.Particle)particleObject).age * ((Particles.Particle)particleObject).rotationSpeed,
                                particleColor
                                );
                            } else if (this.twoDimensionalShapeSetting.isSelected("Искры")) {
                                Vec3d trailStart = ((Particles.Particle)particleObject).position
                                .subtract(((Particles.Particle)particleObject).velocity.multiply(0.05));
                                DaamkyWorldRender.drawGradientLine(
                                trailStart, ((Particles.Particle)particleObject).position, particleColor.withAlpha(0), particleColor, 2.0F, false
                                );
                                DaamkyWorldRender.drawBloomRing(
                                ((Particles.Particle)particleObject).position, particleSize * 0.6F, particleColor.withAlpha((int)(140.0F * fadeAlpha)), false
                                );
                            } else {
                                DaamkyWorldRender.drawBloomRing(((Particles.Particle)particleObject).position, particleSize * scaleIn, particleColor, false);
                                DaamkyWorldRender.drawBloomRing(
                                ((Particles.Particle)particleObject).position,
                                particleSize * 0.35F * scaleIn,
                                RgbaColor.WHITE.withAlpha((int)(150.0F * fadeAlpha)),
                                false
                                );
                            }
                        }
                    }

                    if (retainedParticleCount < initialParticleCount) {
                        this.particles.subList(retainedParticleCount, initialParticleCount).clear();
                    }

                    if (bufferBuilder != null) {
                        DynamicTransforms.submitDraw(bufferBuilder, DrawMode.QUADS, WorldRenderPipelines.I1l11IIlIIIll1l1lI1II, textureView);
                    }
                }
            }
        }
    }

    private void renderThreeDimensionalParticles(float tickDelta) {
        float lifetimeSeconds = this.particleLifetimeSetting.getValue();
        float particleSize = this.particleSizeSetting.getValue();
        Quaternionf rotation = new Quaternionf();
        Vector3f firstTransformedVertex = new Vector3f();
        Vector3f secondTransformedVertex = new Vector3f();
        int shapeIndex = this.threeDimensionalShapeSetting.getSelectedIndex() - 1;
        boolean glowEnabled = this.glowEnabledSetting.getValue();
        if (glowEnabled) {
            DaamkyWorldRender.beginBloomCapture(this.glowRadiusSetting.getValue(), this.glowStrengthSetting.getValue(), false);
        }

        int retainedParticleCount = 0;
        int initialParticleCount = this.particles.size();

        for (int particleIndex = 0; particleIndex < initialParticleCount; particleIndex++) {
            Object particleObject = this.particles.get(particleIndex);
            ((Particles.Particle)particleObject).age += tickDelta;
            float lifetimeProgress = ((Particles.Particle)particleObject).age / (lifetimeSeconds * ((Particles.Particle)particleObject).scale);
            if (!(lifetimeProgress >= 1.0F)) {
                this.particles.set(retainedParticleCount++, (Particles.Particle)particleObject);
                ((Particles.Particle)particleObject).update(tickDelta);
                float fadeAlpha = lifetimeProgress < 0.1F ? lifetimeProgress / 0.1F : 1.0F - (lifetimeProgress - 0.1F) / 0.9F;
                float renderScale = (lifetimeProgress < 0.15F ? 0.4F + 0.6F * (lifetimeProgress / 0.15F) : 1.0F) * particleSize;
                RgbaColor particleColor = this.getParticleColor((Particles.Particle)particleObject, (int)(235.0F * fadeAlpha));
                float[][][] particleModel = PARTICLE_MODELS[shapeIndex >= 0 ? shapeIndex : ((Particles.Particle)particleObject).modelIndex];
                float rotationAngle = ((Particles.Particle)particleObject).rotationAngle
                + ((Particles.Particle)particleObject).age * ((Particles.Particle)particleObject).rotationSpeed * 1.5F;
                rotation.identity().rotateAxis(rotationAngle, ((Particles.Particle)particleObject).rotationAxis).rotateY(rotationAngle * 0.6F);
                float[][] modelVertices = particleModel[0];
                float[][] modelEdges = particleModel[1];

                for (float[] edgeIndices : modelEdges) {
                    float[] firstVertex = modelVertices[(int)edgeIndices[0]];
                    float[] secondVertex = modelVertices[(int)edgeIndices[1]];
                    rotation.transform(firstTransformedVertex.set(firstVertex[0], firstVertex[1], firstVertex[2]).mul(renderScale));
                    rotation.transform(secondTransformedVertex.set(secondVertex[0], secondVertex[1], secondVertex[2]).mul(renderScale));
                    Vec3d worldStart = ((Particles.Particle)particleObject).position.add(firstTransformedVertex.x, firstTransformedVertex.y, firstTransformedVertex.z);
                    Vec3d worldEnd = ((Particles.Particle)particleObject).position.add(secondTransformedVertex.x, secondTransformedVertex.y, secondTransformedVertex.z);
                    DaamkyWorldRender.drawLine(worldStart, worldEnd, particleColor, 2.0F, false);
                    DaamkyWorldRender.drawLine(worldStart, worldEnd, RgbaColor.WHITE.withAlpha((int)(120.0F * fadeAlpha)), 0.8F, false);
                }
            }
        }

        if (glowEnabled) {
            DaamkyWorldRender.endBloomCapture();
        }

        if (retainedParticleCount < initialParticleCount) {
            this.particles.subList(retainedParticleCount, initialParticleCount).clear();
        }
    }

    private static float[][][] buildEdgeModel(float[][] vertices, int[][] edgeIndices) {
        float[][] edges = new float[edgeIndices.length][];

        for (int edgeIndex = 0; edgeIndex < edgeIndices.length; edgeIndex++) {
            edges[edgeIndex] = new float[]{edgeIndices[edgeIndex][0], edgeIndices[edgeIndex][1]};
        }

        return new float[][][]{vertices, edges};
    }

    private static float[][][] generateSphereParticleData() {
        float[][] vertices = new float[64][];

        float[][] edges = new float[63][];

        for (int vertexIndex = 0; vertexIndex < 64; vertexIndex++) {
            float latitude = vertexIndex;

            float normalizedHeight = latitude / 63.0F;
            float verticalComponent = 1.0F - 2.0F * normalizedHeight;
            float ringRadius = MathHelper.sqrt(Math.max(0.0F, 1.0F - verticalComponent * verticalComponent));
            float spiralAngle = normalizedHeight * (float) (Math.PI * 2) * 6.0F;
            vertices[vertexIndex] = new float[]{MathHelper.cos(spiralAngle) * ringRadius, verticalComponent, MathHelper.sin(spiralAngle) * ringRadius};
            if (vertexIndex > 0) {
                edges[vertexIndex - 1] = new float[]{vertexIndex - 1, vertexIndex};
            }
        }

        return new float[][][]{vertices, edges};
    }

    private static float[][][] generateRingParticleData() {

        float[][] vertices = new float[40][];

        float[][] edges = new float[40][];

        for (int segmentIndex = 0; segmentIndex < 20; segmentIndex++) {
            float angleRadians = segmentIndex / 20.0F * (float) (Math.PI * 2);
            vertices[segmentIndex] = new float[]{MathHelper.cos(angleRadians), 0.0F, MathHelper.sin(angleRadians)};
            vertices[20 + segmentIndex] = new float[]{MathHelper.cos(angleRadians) * 0.55F, MathHelper.sin(angleRadians) * 0.55F, 0.0F};
            edges[segmentIndex] = new float[]{segmentIndex, (segmentIndex + 1) % 20};
            edges[20 + segmentIndex] = new float[]{20 + segmentIndex, 20 + (segmentIndex + 1) % 20};
        }

        return new float[][][]{vertices, edges};
    }

    private Identifier getTextureForShape() {
        String textureName = switch ((String)this.twoDimensionalShapeSetting.getValue()) {
        case "Звезда" -> "star";
        case "Сердце" -> "heart";
        case "Снежинка" -> "snowflake";
        case "Корона" -> "crown";
        case "Доллар" -> "dollar";
        case "Ромб" -> "rhombus";
        case "Светлячок" -> "firefly";
        case "Огонёк" -> "firepart";
        case "Тыковка" -> "pumpkin";
        case "Призрак" -> "ghost";
            default -> null;
        };
        return textureName == null ? null : Identifier.of("daamky", "textures/particles/" + textureName + ".png");
    }

    private RgbaColor getParticleColor(Particles.Particle particle, int alpha) {
        return switch ((String)this.colorModeSetting.getValue()) {
        case "Переливание" -> AnimatedColorUtils.getAnimatedAccentColor(particle.colorPhase + AnimatedColorUtils.animationTime(), alpha);
        case "Случайный" -> RgbaColor.fromArgb(MathHelper.hsvToRgb(particle.randomHue, 0.65F, 1.0F) | 0xFF000000).withAlpha(alpha);
        case "Белый" -> RgbaColor.WHITE.withAlpha(alpha);
            default -> AnimatedColorUtils.getAnimatedAccentColor(particle.colorPhase, alpha);
        };
    }

    private static void drawBillboardQuad(
    BufferBuilder bufferBuilder, Vector3f[] cameraPlaneBasis, Vec3d worldPosition, float size, float rotationRadians, RgbaColor color
    ) {
        Vec3d cameraPosition = DaamkyWorldRender.getCameraPosition();
        float cosineRadius = MathHelper.cos(rotationRadians) * size;
        float sineRadius = MathHelper.sin(rotationRadians) * size;
        Vector3f cameraRight = cameraPlaneBasis[0];
        Vector3f cameraUp = cameraPlaneBasis[1];
        float rightX = cameraRight.x * cosineRadius + cameraUp.x * sineRadius;
        float rightY = cameraRight.y * cosineRadius + cameraUp.y * sineRadius;
        float rightZ = cameraRight.z * cosineRadius + cameraUp.z * sineRadius;
        float upX = cameraUp.x * cosineRadius - cameraRight.x * sineRadius;
        float upY = cameraUp.y * cosineRadius - cameraRight.y * sineRadius;
        float upZ = cameraUp.z * cosineRadius - cameraRight.z * sineRadius;
        float cameraRelativeX = (float)(worldPosition.x - cameraPosition.x);
        float cameraRelativeY = (float)(worldPosition.y - cameraPosition.y);
        float cameraRelativeZ = (float)(worldPosition.z - cameraPosition.z);
        int colorArgb = color.toArgb();
        bufferBuilder.vertex(cameraRelativeX - rightX - upX, cameraRelativeY - rightY - upY, cameraRelativeZ - rightZ - upZ).texture(0.0F, 1.0F).color(colorArgb);
        bufferBuilder.vertex(cameraRelativeX + rightX - upX, cameraRelativeY + rightY - upY, cameraRelativeZ + rightZ - upZ).texture(1.0F, 1.0F).color(colorArgb);
        bufferBuilder.vertex(cameraRelativeX + rightX + upX, cameraRelativeY + rightY + upY, cameraRelativeZ + rightZ + upZ).texture(1.0F, 0.0F).color(colorArgb);
        bufferBuilder.vertex(cameraRelativeX - rightX + upX, cameraRelativeY - rightY + upY, cameraRelativeZ - rightZ + upZ).texture(0.0F, 0.0F).color(colorArgb);
    }

    private final class Particle {
        private Vec3d position;
        private Vec3d velocity;
        private final float colorPhase;
        private final float scale;
        private final float randomHue;
        private final float rotationAngle;
        private final float rotationSpeed;
        private final int modelIndex;
        private final Vector3f rotationAxis;
        private float age;
        private boolean gravityDisabled;

        private Particle(Vec3d position, Vec3d velocity, float initialRotation, float scale) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            this.position = position;
            this.velocity = velocity;
            this.colorPhase = initialRotation;
            this.scale = scale;
            this.randomHue = random.nextFloat();
            this.rotationAngle = random.nextFloat() * (float) (Math.PI * 2);
            this.rotationSpeed = (random.nextFloat() - 0.5F) * 4.0F;
            this.modelIndex = random.nextInt(Particles.PARTICLE_MODELS.length);
            this.rotationAxis = new Vector3f(random.nextFloat() - 0.5F, random.nextFloat() - 0.5F, random.nextFloat() - 0.5F)
            .normalize();
        }

        private Particles.Particle markGravityDisabled() {
            this.gravityDisabled = true;
            return this;
        }

        private void update(float deltaSeconds) {
            if (Particles.this.physicsEnabledSetting.getValue() && !this.gravityDisabled) {
                this.velocity = this.velocity.add(0.0, -9.0 * deltaSeconds, 0.0);
            }

            this.velocity = this.velocity.multiply(Math.max(0.0, 1.0 - deltaSeconds * (this.gravityDisabled ? 0.3 : 1.2)));
            Vec3d nextPosition = this.position.add(this.velocity.multiply(deltaSeconds));
            if (Particles.this.physicsEnabledSetting.getValue() && Particles.CLIENT.world != null) {
                BlockPos xCollisionBlock = BlockPos.ofFloored(nextPosition.x, this.position.y, this.position.z);
                if (!Particles.CLIENT.world.getBlockState(xCollisionBlock).getCollisionShape(Particles.CLIENT.world, xCollisionBlock).isEmpty()) {
                    this.velocity = new Vec3d(-this.velocity.x * 0.5, this.velocity.y, this.velocity.z);
                }

                BlockPos yCollisionBlock = BlockPos.ofFloored(this.position.x, nextPosition.y, this.position.z);
                if (!Particles.CLIENT.world.getBlockState(yCollisionBlock).getCollisionShape(Particles.CLIENT.world, yCollisionBlock).isEmpty()) {
                    this.velocity = new Vec3d(this.velocity.x * 0.8, -this.velocity.y * 0.45, this.velocity.z * 0.8);
                }

                BlockPos zCollisionBlock = BlockPos.ofFloored(this.position.x, this.position.y, nextPosition.z);
                if (!Particles.CLIENT.world.getBlockState(zCollisionBlock).getCollisionShape(Particles.CLIENT.world, zCollisionBlock).isEmpty()) {
                    this.velocity = new Vec3d(this.velocity.x, this.velocity.y, -this.velocity.z * 0.5);
                }

                nextPosition = this.position.add(this.velocity.multiply(deltaSeconds));
            }

            this.position = nextPosition;
        }
    }
}

