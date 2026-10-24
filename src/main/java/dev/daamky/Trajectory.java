package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventRender3D;
import java.util.ArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class Trajectory extends ModuleStructure {
    private final BooleanSetting showImpactMarker = new BooleanSetting("Точка падения", true);
    private final BooleanSetting showEntityHits = new BooleanSetting("Попадание в сущность", true);
    private final BooleanSetting renderThroughWalls = new BooleanSetting("Сквозь стены", false);
    private final FloatSetting lineWidth = new FloatSetting("Толщина", 2.0F, 0.5F, 5.0F, 0.1F);

    public Trajectory() {
        super("Trajectory", "Показывает траекторию полёта снарядов", ModuleCategory.RENDER);
        this.addSettings(this.showImpactMarker, this.showEntityHits, this.renderThroughWalls, this.lineWidth);
    }

    private Trajectory.TrajectoryImpact createTrajectoryImpact(ItemStack projectileStack) {
        Item projectileItem = projectileStack.getItem();
        if (projectileItem == Items.BOW) {
            int useTicks = CLIENT.player.getItemUseTime();
            if (CLIENT.player.isUsingItem() && useTicks > 0) {
                float pullStrength = BowItem.getPullProgress(useTicks);
                return pullStrength < 0.1F ? null : new Trajectory.TrajectoryImpact(pullStrength * 3.0F, 0.05, 0.0F);
            } else {
                return null;
            }
        } else if (projectileItem == Items.CROSSBOW) {
            return CrossbowItem.isCharged(projectileStack) ? new Trajectory.TrajectoryImpact(3.15F, 0.05, 0.0F) : null;
        } else if (projectileItem == Items.TRIDENT) {
            return CLIENT.player.isUsingItem() ? new Trajectory.TrajectoryImpact(2.5F, 0.05, 0.0F) : null;
        } else if (projectileItem == Items.ENDER_PEARL || projectileItem == Items.SNOWBALL || projectileItem == Items.EGG) {
            return new Trajectory.TrajectoryImpact(1.5F, 0.03, 0.0F);
        } else if (projectileItem == Items.SPLASH_POTION || projectileItem == Items.LINGERING_POTION) {
            return new Trajectory.TrajectoryImpact(0.5F, 0.05, -20.0F);
        } else {
            return projectileItem == Items.EXPERIENCE_BOTTLE ? new Trajectory.TrajectoryImpact(0.7F, 0.07, -20.0F) : null;
        }
    }

    @EventTarget
    public void renderTrajectory(EventRender3D eventRender3D) {
        if (CLIENT.player != null && CLIENT.world != null) {
            ItemStack projectileStack = CLIENT.player.getStackInHand(Hand.MAIN_HAND);
            Trajectory.TrajectoryImpact impactParameters = this.createTrajectoryImpact(projectileStack);
            if (impactParameters == null) {
                projectileStack = CLIENT.player.getStackInHand(Hand.OFF_HAND);
                impactParameters = this.createTrajectoryImpact(projectileStack);
            }

            if (impactParameters != null) {
                float partialTicks = eventRender3D.partialTicks();
                float yawRadians = CLIENT.player.getLerpedYaw(partialTicks) * (float) (Math.PI / 180.0);
                float pitchRadians = (CLIENT.player.getLerpedPitch(partialTicks) + impactParameters.verticalOffset()) * (float) (Math.PI / 180.0);
                Vec3d eyePosition = DaamkyWorldRender.getInterpolatedPosition(CLIENT.player)
                .add(0.0, CLIENT.player.getStandingEyeHeight(), 0.0);
                Vec3d launchDirection = new Vec3d(
                -MathHelper.sin(yawRadians) * MathHelper.cos(pitchRadians),
                -MathHelper.sin(pitchRadians),
                MathHelper.cos(yawRadians) * MathHelper.cos(pitchRadians)
                )
                .normalize();
                Vec3d velocityStep = launchDirection.multiply(impactParameters.impactTime());
                Vec3d currentPosition = eyePosition.add(-MathHelper.cos(yawRadians) * 0.16, -0.1, -MathHelper.sin(yawRadians) * 0.16);
                ArrayList trajectoryPoints = new ArrayList();
                trajectoryPoints.add(currentPosition);
                HitResult terminalImpact = null;

                for (int simulationStep = 0; simulationStep < 300; simulationStep++) {
                    Vec3d nextPosition = currentPosition.add(velocityStep);
                    BlockHitResult blockCollision = CLIENT.world
                    .raycast(new RaycastContext(currentPosition, nextPosition, ShapeType.COLLIDER, FluidHandling.NONE, CLIENT.player));
                    Vec3d collisionPosition = blockCollision.getType() != Type.MISS ? blockCollision.getPos() : nextPosition;
                    if (this.showEntityHits.getValue()) {
                        Box entitySearchBounds = new Box(currentPosition, collisionPosition).expand(1.0);
                        EntityHitResult entityCollision = ProjectileUtil.raycast(
                        CLIENT.player,
                        currentPosition,
                        collisionPosition,
                        entitySearchBounds,
                        hitEntity -> hitEntity != CLIENT.player && hitEntity.canHit() && !hitEntity.isSpectator(),
                        currentPosition.squaredDistanceTo(collisionPosition)
                        );
                        if (entityCollision != null) {
                            trajectoryPoints.add(entityCollision.getPos());
                            terminalImpact = entityCollision;
                            break;
                        }
                    }

                    if (blockCollision.getType() != Type.MISS) {
                        trajectoryPoints.add(blockCollision.getPos());
                        terminalImpact = blockCollision;
                        break;
                    }

                    trajectoryPoints.add(nextPosition);
                    currentPosition = nextPosition;
                    boolean inStillFluid = CLIENT.world.getFluidState(BlockPos.ofFloored(currentPosition)).isStill();
                    velocityStep = velocityStep.multiply(inStillFluid ? 0.6 : 0.99).subtract(0.0, impactParameters.distance(), 0.0);
                    if (currentPosition.y < CLIENT.world.getBottomY() - 16) {
                        break;
                    }
                }

                boolean renderThroughWalls = this.renderThroughWalls.getValue();
                int pointCount = trajectoryPoints.size();

                for (int segmentIndex = 1; segmentIndex < pointCount; segmentIndex++) {
                    float segmentProgress = (float)segmentIndex / pointCount;
                    RgbaColor segmentStartColor = AnimatedColorUtils.getAnimatedAccentColor(segmentProgress * 4.0F, (int)(255.0F * Math.min(1.0F, (segmentIndex - 1) / 4.0F)));
                    RgbaColor segmentEndColor = AnimatedColorUtils.getAnimatedAccentColor((float)(segmentIndex + 1) / pointCount * 4.0F, (int)(255.0F * Math.min(1.0F, segmentIndex / 4.0F)));
                    DaamkyWorldRender.drawGradientLine(
                    (Vec3d)trajectoryPoints.get(segmentIndex - 1), (Vec3d)trajectoryPoints.get(segmentIndex), segmentStartColor, segmentEndColor, this.lineWidth.getValue(), renderThroughWalls
                    );
                }

                if (this.showImpactMarker.getValue() && terminalImpact != null) {
                    RgbaColor impactMarkerColor = AnimatedColorUtils.getAnimatedAccentColor(0.0F, 255);
                    if (terminalImpact instanceof EntityHitResult hitEntityResult) {
                        Entity hitEntity = hitEntityResult.getEntity();
                        Box entityBounds = DaamkyWorldRender.getInterpolatedBoundingBox(hitEntity).expand(0.05);
                        RgbaColor entityImpactColor = RgbaColor.rgb(255, 80, 80);
                        DaamkyWorldRender.drawFilledBox(entityBounds, entityImpactColor.withAlpha(50), renderThroughWalls);
                        DaamkyWorldRender.drawBoxOutline(entityBounds, entityImpactColor.withAlpha(230), this.lineWidth.getValue(), renderThroughWalls);
                    } else {
                        BlockHitResult hitBlockResult = (BlockHitResult)terminalImpact;
                        Vec3d blockImpactPosition = hitBlockResult.getPos();
                        Direction impactFace = hitBlockResult.getSide();
                        if (impactFace.getAxis() == Axis.Y) {
                            Vec3d markerPosition = blockImpactPosition.add(0.0, impactFace == Direction.UP ? 0.01 : -0.01, 0.0);
                            float pulseProgress = (float)(Math.sin(AnimatedColorUtils.animationTime() * 4.0F) * 0.5 + 0.5);
                            DaamkyWorldRender.drawCircleOutline(
                            markerPosition, 0.35 + pulseProgress * 0.08, impactMarkerColor.withAlpha(240), this.lineWidth.getValue(), 40, renderThroughWalls
                            );
                            DaamkyWorldRender.drawRing(
                            markerPosition, 0.0, 0.35 + pulseProgress * 0.08, impactMarkerColor.withAlpha(90), impactMarkerColor.withAlpha(20), 40, false, renderThroughWalls
                            );
                        } else {

                            Box blockImpactBounds = new Box(
                            blockImpactPosition.x - 0.25,
                            blockImpactPosition.y - 0.25,
                            blockImpactPosition.z - 0.25,
                            blockImpactPosition.x + 0.25,
                            blockImpactPosition.y + 0.25,
                            blockImpactPosition.z + 0.25
                            );
                            DaamkyWorldRender.drawFilledBox(blockImpactBounds, impactMarkerColor.withAlpha(50), renderThroughWalls);
                            DaamkyWorldRender.drawBoxOutline(blockImpactBounds, impactMarkerColor.withAlpha(230), this.lineWidth.getValue(), renderThroughWalls);
                        }
                    }
                }
            }
        }
    }

    private record TrajectoryImpact(float impactTime, double distance, float verticalOffset) {

        private TrajectoryImpact(float impactTime, double distance, float verticalOffset) {
            this.impactTime = impactTime;
            this.distance = distance;
            this.verticalOffset = verticalOffset;
        }
    }
}

