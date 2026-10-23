package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventDisplay;
import dev.daamky.core.event.impl.EventRender3D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class Prediction extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final MsdfFont FONT = Medium.DEFAULT_FONT;
   private static final float LABEL_TEXT_SCALE = 5.8F;
   private static final float LABEL_PADDING = 4.0F;
   private static final float LABEL_CORNER_RADIUS = 3.0F;
   private static final float LABEL_HEIGHT = 8.0F;
   private final BooleanSetting trajectoryEnabled = new BooleanSetting("Траектория", true);
   private final BooleanSetting impactLabelsEnabled = new BooleanSetting("Метка падения", true);
   private final BooleanSetting showImpactTime = new BooleanSetting("Время", true);
   private final BooleanSetting throughWalls = new BooleanSetting("Сквозь стены", true);
   private final FloatSetting trajectoryLineThickness = new FloatSetting("Толщина", 2.0F, 0.5F, 5.0F, 0.1F);
   private final List<Prediction.ProjectileImpactPrediction> predictedImpacts = new ArrayList<>();
// https://t.me/pasterplace

   public Prediction() {
      super("Prediction", "Показывает точки падения летящих снарядов", ModuleCategory.RENDER);
      this.addSettings(this.trajectoryEnabled, this.impactLabelsEnabled, this.showImpactTime, this.throughWalls, this.trajectoryLineThickness);
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.predictedImpacts.clear();
   }
// https://t.me/pasterplace

   private static ItemStack getProjectileDisplayStack(Entity projectile) {
      if (projectile instanceof TridentEntity) {
         return new ItemStack(Items.TRIDENT);
      } else if (projectile instanceof PersistentProjectileEntity persistentProjectile) {
         ItemStack projectileItemStack = persistentProjectile.getItemStack();
         return projectileItemStack.isEmpty() ? new ItemStack(Items.ARROW) : projectileItemStack;
      } else {
         return projectile instanceof FlyingItemEntity flyingItemEntity ? flyingItemEntity.getStack() : ItemStack.EMPTY;
      }
   }
// https://t.me/pasterplace

   private static boolean isSupportedProjectile(Entity projectile) {
      return projectile instanceof PersistentProjectileEntity || projectile instanceof ThrownItemEntity;
   }
// https://t.me/pasterplace

   @EventTarget
   public void onRender3D(EventRender3D renderEvent) {
      this.predictedImpacts.clear();
      if (CLIENT.player != null && CLIENT.world != null) {
         boolean renderThroughWalls = this.throughWalls.getValue();

          for (Entity projectile : CLIENT.world.getEntities()) {
             if (isSupportedProjectile(projectile) && projectile.isAlive()) {
               Vec3d projectileVelocity = projectile.getVelocity();
               if (!(projectileVelocity.lengthSquared() < 1.0E-4)) {
                  double projectileGravity = projectile.getFinalGravity();
                  Vec3d projectilePosition = projectile.getEntityPos();
                   ArrayList<Vec3d> trajectoryPoints = new ArrayList<>();
                  trajectoryPoints.add(DaamkyWorldRender.getInterpolatedPosition((Entity)projectile));
                   HitResult impactHitResult = null;
                  int simulationTickCount = 0;
                  Entity projectileOwnerCandidate;
                  if (projectile instanceof ProjectileEntity) {
                     ProjectileEntity projectileEntity = (ProjectileEntity)projectile;
                     projectileOwnerCandidate = projectileEntity.getOwner();
                  } else {
                     projectileOwnerCandidate = null;
                  }

                  Entity projectileOwner = projectileOwnerCandidate;

                  for (int simulationStep = 0; simulationStep < 300; simulationStep++) {
                     simulationTickCount++;
                     Vec3d nextProjectilePosition = projectilePosition.add(projectileVelocity);
                     BlockHitResult blockHitResult = CLIENT.world
                        .raycast(new RaycastContext(projectilePosition, nextProjectilePosition, ShapeType.COLLIDER, FluidHandling.NONE, (Entity)projectile));
                     Vec3d segmentEndPosition = blockHitResult.getType() != Type.MISS ? blockHitResult.getPos() : nextProjectilePosition;
                     Box box = new Box(projectilePosition, segmentEndPosition).expand(1.0);
                     EntityHitResult entityHitResult = ProjectileUtil.raycast(
                        (Entity)projectile,
                        projectilePosition,
                        segmentEndPosition,
                        box,
                        candidateEntity -> candidateEntity != projectile && candidateEntity != projectileOwner && candidateEntity.canHit() && !candidateEntity.isSpectator(),
                        projectilePosition.squaredDistanceTo(segmentEndPosition)
                     );
                     if (entityHitResult != null) {
                        trajectoryPoints.add(entityHitResult.getPos());
                        impactHitResult = entityHitResult;
                        break;
                     }
// https://t.me/pasterplace

                     if (blockHitResult.getType() != Type.MISS) {
                        trajectoryPoints.add(blockHitResult.getPos());
                         impactHitResult = blockHitResult;
                        break;
                     }

                     trajectoryPoints.add(nextProjectilePosition);
                     projectilePosition = nextProjectilePosition;
                     boolean insideStillWater = CLIENT.world.getFluidState(BlockPos.ofFloored(projectilePosition)).isStill();
                     projectileVelocity = projectileVelocity.multiply(insideStillWater ? 0.6 : 0.99).subtract(0.0, projectileGravity, 0.0);
                     if (projectilePosition.y < CLIENT.world.getBottomY() - 16) {
                        break;
                     }
                  }

                  if (this.trajectoryEnabled.getValue()) {
                     int trajectoryPointCount = trajectoryPoints.size();

                     for (int segmentIndex = 1; segmentIndex < trajectoryPointCount; segmentIndex++) {
                        RgbaColor segmentStartColor = AnimatedColorUtils.getAnimatedAccentColor((float)(segmentIndex - 1) / trajectoryPointCount * 4.0F, 255);
                        RgbaColor segmentEndColor = AnimatedColorUtils.getAnimatedAccentColor((float)segmentIndex / trajectoryPointCount * 4.0F, 255);
                        DaamkyWorldRender.drawGradientLine(
                           (Vec3d)trajectoryPoints.get(segmentIndex - 1),
                           (Vec3d)trajectoryPoints.get(segmentIndex),
                           segmentStartColor,
                           segmentEndColor,
                           this.trajectoryLineThickness.getValue(),
                           renderThroughWalls
                        );
                     }
                  }

                  if (impactHitResult != null) {
                      Vec3d impactPosition = trajectoryPoints.getLast();
                     double impactMarkerHalfExtent = 0.15;
                     RgbaColor impactColor = AnimatedColorUtils.getAnimatedAccentColor(0.0F, 255);
                     Box impactMarkerBounds = new Box(
                         impactPosition.x - impactMarkerHalfExtent,
                         impactPosition.y - impactMarkerHalfExtent,
                         impactPosition.z - impactMarkerHalfExtent,
                         impactPosition.x + impactMarkerHalfExtent,
                         impactPosition.y + impactMarkerHalfExtent,
                         impactPosition.z + impactMarkerHalfExtent
                     );
                     DaamkyWorldRender.drawFilledBox(impactMarkerBounds, impactColor.withAlpha(50), renderThroughWalls);
                     DaamkyWorldRender.drawBoxOutline(impactMarkerBounds, impactColor.withAlpha(230), this.trajectoryLineThickness.getValue(), renderThroughWalls);
                      ItemStack projectileItemStack = getProjectileDisplayStack(projectile);
                     String displayName = projectileItemStack.isEmpty() ? projectile.getDisplayName().getString() : projectileItemStack.getName().getString();
                      this.predictedImpacts.add(new Prediction.ProjectileImpactPrediction(impactPosition, projectileItemStack, displayName, simulationTickCount));
                  }
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   public void onOverlayRender(EventDisplay displayEvent) {
      if (this.impactLabelsEnabled.getValue() && !this.predictedImpacts.isEmpty()) {
         if (CLIENT.currentScreen == null || CLIENT.currentScreen instanceof ChatScreen) {
            DrawContext drawContext = displayEvent.graphics();
            ArrayList itemIconPositions = new ArrayList();
            ArrayList itemIconStacks = new ArrayList();

            for (Object impactPrediction : this.predictedImpacts) {
               WorldToScreenProjection.ScreenPoint screenPosition = WorldToScreenProjection.project(
                  ((Prediction.ProjectileImpactPrediction)impactPrediction).position().add(0.0, 0.4, 0.0)
               );
               if (screenPosition != null && screenPosition.inFront()) {
                  String displayName = ((Prediction.ProjectileImpactPrediction)impactPrediction).displayName();
                  String remainingTimeLabel = this.showImpactTime.getValue()
                     ? String.format(Locale.US, "%.1fс", ((Prediction.ProjectileImpactPrediction)impactPrediction).remainingTicks() / 20.0F)
                     : "";
                  boolean hasItemIcon = !((Prediction.ProjectileImpactPrediction)impactPrediction).itemStack().isEmpty();
                  float displayNameWidth = FONT.measureWidth(displayName, 5.8F);
                  float remainingTimeWidth = remainingTimeLabel.isEmpty() ? 0.0F : FONT.measureWidth(remainingTimeLabel, 5.8F) + 4.0F;
                  float itemIconWidth = hasItemIcon ? 11.0F : 0.0F;
                  float labelWidth = 4.0F + itemIconWidth + displayNameWidth + remainingTimeWidth + 4.0F;
                  float labelHeight = 11.8F;
                  float labelX = screenPosition.x() - labelWidth / 2.0F;
                  float labelY = screenPosition.y() - labelHeight;
                  BrokkeIcons.drawRoundedPanelBackground(drawContext, labelX, labelY, labelWidth, labelHeight, 3.5F, 1.0F);
                  float textY = labelY + (labelHeight - FONT.metrics().lineHeight() * 5.8F) / 2.0F;
                  float textX = labelX + 4.0F;
                  if (hasItemIcon) {
                     itemIconPositions.add(new float[]{textX + 4.0F, labelY + labelHeight / 2.0F});
                     itemIconStacks.add(((Prediction.ProjectileImpactPrediction)impactPrediction).itemStack());
                     textX += itemIconWidth;
                  }

                  GuiTextBuilder.createAt(FONT, displayName, textX, textY, 5.8F)
                     .color(Accent.getTextColorWithAlpha(250).toArgb())
                     .render(drawContext);
                  if (!remainingTimeLabel.isEmpty()) {
                     GuiTextBuilder.createAt(FONT, remainingTimeLabel, textX + displayNameWidth + 4.0F, textY, 5.8F)
                        .color(Accent.getAccentColorWithAlpha(255).toArgb())
                        .render(drawContext);
                  }
               }
            }

            if (!itemIconPositions.isEmpty()) {
               GuiEventRenderer.flushAndStartRootLayer(drawContext);

               for (int itemIconIndex = 0; itemIconIndex < itemIconPositions.size(); itemIconIndex++) {
                  Object iconPosition = (float[])itemIconPositions.get(itemIconIndex);
                  drawContext.getMatrices().pushMatrix();
                  drawContext.getMatrices().translate((float)((Object[])iconPosition)[0], (float)((Object[])iconPosition)[1]);
                  drawContext.getMatrices().scale(0.5F);
                  drawContext.getMatrices().translate(-8.0F, -8.0F);
                  GuiEventRenderer.flushBatchedElements();
                  drawContext.drawItem((ItemStack)itemIconStacks.get(itemIconIndex), 0, 0);
                  drawContext.getMatrices().popMatrix();
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private record ProjectileImpactPrediction(Vec3d position, ItemStack itemStack, String displayName, int remainingTicks) {

      private ProjectileImpactPrediction(Vec3d position, ItemStack itemStack, String displayName, int remainingTicks) {
         this.position = position;
         this.itemStack = itemStack;
         this.displayName = displayName;
         this.remainingTicks = remainingTicks;
      }
   }
}

