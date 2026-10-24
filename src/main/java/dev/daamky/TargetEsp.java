package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventAttack;
import dev.daamky.core.event.impl.EventRender3D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class TargetEsp extends ModuleStructure {
   private static final Identifier BLOOM_TEXTURE = Identifier.of("daamky", "textures/targetesp/bloom.png");
   private static final Identifier GLOW_TEXTURE = Identifier.of("daamky", "textures/targetesp/glowing.png");
   private static final Identifier CUBE_TEXTURE = Identifier.of("daamky", "textures/targetesp/cube.png");
   private static final Vector3f[] CUBE_VERTICES = new Vector3f[]{
      new Vector3f(0.0F, 1.5F, 0.0F),
      new Vector3f(0.0F, -1.5F, 0.0F),
      new Vector3f(1.0F, 0.0F, 0.0F),
      new Vector3f(-1.0F, 0.0F, 0.0F),
      new Vector3f(0.0F, 0.0F, 1.0F),
      new Vector3f(0.0F, 0.0F, -1.0F)
   };
   private static final int[][] CUBE_TRIANGLES = new int[][]{{0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2}, {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}};
   private static final float[] CUBE_FACE_BRIGHTNESS = new float[]{1.0F, 0.8F, 0.6F, 0.9F, 0.7F, 0.5F, 0.4F, 0.6F};
   private static final Map<Identifier, GpuTextureView> TEXTURE_CACHE = new HashMap<>();
   private final MultiOptionSetting modeSetting = new MultiOptionSetting(
      "Режим", "Кристаллы", "Кристаллы", "Души", "Кругляшок", "Jello", "Призраки", "Молнии", "Cube"
   );
   private final FloatSetting rotationSpeedSetting = new FloatSetting("Скорость вращения", 1.0F, 0.1F, 3.0F, 0.05F);
   private final FloatSetting lightningCountSetting = new FloatSetting("Кол-во молний", 4.0F, 1.0F, 10.0F, 1.0F);
   private final FloatSetting lightningWidthSetting = new FloatSetting("Толщина молний", 1.0F, 0.1F, 5.0F, 0.1F);
   private final BooleanSetting colorOnHitSetting = new BooleanSetting("Окрашивание при ударе", true);
   private final BooleanSetting showOnCrosshairSetting = new BooleanSetting("При наводке", true);
   private float fadeProgress;
   private float rotationDegrees;
   private long lastFrameTimeMillis = System.currentTimeMillis();
   private LivingEntity currentTarget;
   private Vec3d lastTargetPosition;
   private float lastTargetWidth = 0.6F;
   private float lastTargetHeight = 1.8F;
   private final List<TargetEsp.LightningTrail> lightningTrails = new ArrayList<>();
   private long lastLightningSpawnMillis;
   private long lastAttackMillis;
   private LivingEntity lastAttackedTarget;
   private final List<List<TargetEsp.PositionSample>> ghostTrailHistory = new ArrayList<>();
   private final List<TargetEsp.GhostParticle> ghostParticles = new ArrayList<>();
   private long lastGhostParticleSpawnMillis;
   private Vec3d cameraPosition = Vec3d.ZERO;
   private Vector3f cameraRight = new Vector3f(1.0F, 0.0F, 0.0F);
   private Vector3f cameraUp = new Vector3f(0.0F, 1.0F, 0.0F);

   public TargetEsp() {
      super("TargetEsp", "Отображение таргета при включенной киллауре", ModuleCategory.RENDER);
      this.rotationSpeedSetting.visibleWhen(() -> !this.modeSetting.isSelected("Молнии"));
      this.lightningCountSetting.visibleWhen(() -> this.modeSetting.isSelected("Молнии"));
      this.lightningWidthSetting.visibleWhen(() -> this.modeSetting.isSelected("Молнии"));
      this.addSettings(
         this.modeSetting, this.rotationSpeedSetting, this.lightningCountSetting, this.lightningWidthSetting, this.colorOnHitSetting, this.showOnCrosshairSetting
      );
   }

   private void clearGhostTrails() {
      for (List<TargetEsp.PositionSample> trailHistory : this.ghostTrailHistory) {
         trailHistory.clear();
      }

      this.ghostParticles.clear();
   }

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onAttack(EventAttack event) {
      if (event.isPre()) {
         if (event.target() instanceof LivingEntity livingTarget) {
            this.lastAttackMillis = System.currentTimeMillis();
            this.lastAttackedTarget = livingTarget;
         }
      }
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.fadeProgress = 0.0F;
      this.currentTarget = null;
      this.lastTargetPosition = null;
      this.clearGhostTrails();
      this.lightningTrails.clear();
   }

   private LivingEntity findCurrentTarget() {
      Aura aura = Aura.getInstance();
      if (aura != null && aura.isEnabled() && aura.getTarget() != null && aura.getTarget().isAlive()) {
         return aura.getTarget();
      }

      if (this.showOnCrosshairSetting.getValue()) {
         if (CLIENT.targetedEntity instanceof LivingEntity livingTarget) {
            if (livingTarget.isAlive() && livingTarget != CLIENT.player) {
               return livingTarget;
            }
         }
      }

      return this.lastAttackedTarget != null && this.lastAttackedTarget.isAlive() && !this.lastAttackedTarget.isRemoved() && System.currentTimeMillis() - this.lastAttackMillis < 2000L
         ? this.lastAttackedTarget
         : null;
   }

   @EventTarget
   public void onRender3D(EventRender3D event) {
      if (CLIENT.player != null && CLIENT.world != null && !CLIENT.options.hudHidden) {
         LivingEntity target = this.findCurrentTarget();
         boolean hasTarget = target != null && target.isAlive();
         long currentTimeMillis = System.currentTimeMillis();
         float deltaMillis = Math.min(100.0F, Math.max(1.0F, (float)(currentTimeMillis - this.lastFrameTimeMillis)));
         this.lastFrameTimeMillis = currentTimeMillis;
         float fadeStep = hasTarget ? deltaMillis / 160.0F : deltaMillis / 280.0F;
         this.fadeProgress = approach(this.fadeProgress, hasTarget ? 1.0F : 0.0F, fadeStep);
         float partialTicks = event.partialTicks();
         if (hasTarget) {
            if (this.currentTarget != null && this.currentTarget != target) {
               this.clearGhostTrails();
               this.lightningTrails.clear();
            }

            this.currentTarget = target;
            this.lastTargetPosition = DaamkyWorldRender.getInterpolatedPosition(target, partialTicks);
            this.lastTargetWidth = target.getWidth();
            this.lastTargetHeight = target.getHeight();
         }

         if (this.fadeProgress <= 0.001F) {
            this.currentTarget = null;
            this.lastTargetPosition = null;
            this.clearGhostTrails();
            this.lightningTrails.clear();
         } else {
            this.rotationDegrees = this.rotationDegrees + deltaMillis / 1000.0F * 60.0F * this.rotationSpeedSetting.getValue();
            if (this.rotationDegrees > 360000.0F) {
               this.rotationDegrees %= 360.0F;
            }

            this.cameraPosition = DaamkyWorldRender.getCameraPosition();
            Vector3f[] cameraPlaneBasis = DaamkyWorldRender.getCameraPlaneBasis();
            this.cameraRight = cameraPlaneBasis[0];
            this.cameraUp = cameraPlaneBasis[1];
            Vec3d targetRenderPosition = this.getTargetRenderPosition(partialTicks);
            if (targetRenderPosition != null) {
               RgbaColor accentColor = Accent.getAccentColorWithAlpha(255);
               float red = accentColor.red() / 255.0F;
               float green = accentColor.green() / 255.0F;
               float blue = accentColor.blue() / 255.0F;
               float hitFlashAmount = this.getHurtFlashAmount(this.currentTarget);
               if (hitFlashAmount > 0.001F) {
                  red += (1.0F - red) * hitFlashAmount;
                  green *= 1.0F - hitFlashAmount;
                  blue *= 1.0F - hitFlashAmount;
               }

      GpuTextureView bloomTextureView = this.getTextureView(BLOOM_TEXTURE);
      GpuTextureView glowTextureView = this.getTextureView(GLOW_TEXTURE);
               float alpha = this.fadeProgress;
               switch ((String)this.modeSetting.getValue()) {
                  case "Души":
                     if (glowTextureView != null) {
                        BufferBuilder soulBuffer = createTexturedBuffer();
                        this.drawSoulEffect(soulBuffer, targetRenderPosition, this.currentTarget, red, green, blue, alpha);
                        drawTexturedBuffer(soulBuffer, glowTextureView, WorldRenderPipelines.I1lIIII1III1lIlIl1l);
                     }
                     break;
                  case "Кругляшок":
                     if (glowTextureView != null) {
                        BufferBuilder orbitingDotsBuffer = createTexturedBuffer();
                        this.drawOrbitingDots(orbitingDotsBuffer, targetRenderPosition, this.currentTarget, red, green, blue, alpha);
                        drawTexturedBuffer(orbitingDotsBuffer, glowTextureView, WorldRenderPipelines.I1lIIII1III1lIlIl1l);
                     }
                     break;
                  case "Jello":
                     if (bloomTextureView != null) {
                        BufferBuilder jelloTrailBuffer = createTexturedBuffer();
                        this.drawJelloTrail(jelloTrailBuffer, targetRenderPosition, this.currentTarget, red, green, blue, alpha);
                        drawTexturedBuffer(jelloTrailBuffer, bloomTextureView, WorldRenderPipelines.I1lIIII1III1lIlIl1l);
                     }

                     if (glowTextureView != null) {
                        BufferBuilder jelloRingBuffer = createTexturedBuffer();
                        this.drawJelloRing(jelloRingBuffer, targetRenderPosition, this.currentTarget, red, green, blue, alpha);
                        drawTexturedBuffer(jelloRingBuffer, glowTextureView, WorldRenderPipelines.I1lIIII1III1lIlIl1l);
                     }
                     break;
                  case "Призраки":
                     this.updateGhostTrails(targetRenderPosition, this.currentTarget, currentTimeMillis, deltaMillis);
                     if (bloomTextureView != null) {
                        BufferBuilder ghostTrailBuffer = createTexturedBuffer();
                        this.drawGhostTrail(ghostTrailBuffer, red, green, blue, alpha, currentTimeMillis);
                        drawTexturedBuffer(ghostTrailBuffer, bloomTextureView, WorldRenderPipelines.I1lIIII1III1lIlIl1l);
                     }

                     if (glowTextureView != null) {
                        BufferBuilder ghostGlowBuffer = createTexturedBuffer();
                        this.drawGhostGlow(ghostGlowBuffer, red, green, blue, alpha, currentTimeMillis);
                        drawTexturedBuffer(ghostGlowBuffer, glowTextureView, WorldRenderPipelines.I1lIIII1III1lIlIl1l);
                     }
                     break;
                  case "Молнии":
                     this.updateLightningTrails(targetRenderPosition, hasTarget, currentTimeMillis);
                     break;
                  case "Cube":
                     GpuTextureView cubeTextureView = this.getTextureView(CUBE_TEXTURE);
                     if (cubeTextureView != null) {
                        BufferBuilder cubeBuffer = createTexturedBuffer();
                        this.drawRotatingCube(cubeBuffer, targetRenderPosition, this.currentTarget, red, green, blue, alpha);
                        drawTexturedBuffer(cubeBuffer, cubeTextureView, WorldRenderPipelines.renderPipeline);
                     }
                     break;
                  default:
                     BufferBuilder crystalMeshBuffer = createColorBuffer();
                     this.drawCrystalMesh(crystalMeshBuffer, targetRenderPosition, this.currentTarget, red, green, blue, alpha);
                     DynamicTransforms.submitDraw(crystalMeshBuffer, DrawMode.TRIANGLES, WorldRenderPipelines.Ill111IlI11l, null);
                     if (bloomTextureView != null) {
                        BufferBuilder crystalOrbitBuffer = createTexturedBuffer();
                        this.drawCrystalOrbit(crystalOrbitBuffer, targetRenderPosition, this.currentTarget, red, green, blue, alpha);
                        drawTexturedBuffer(crystalOrbitBuffer, bloomTextureView, WorldRenderPipelines.I1lIIII1III1lIlIl1l);
                     }
               }
            }
         }
      }
   }

   private static BufferBuilder createTexturedBuffer() {
      return DynamicTransforms.createBufferBuilder(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
   }

   private static BufferBuilder createColorBuffer() {
      return DynamicTransforms.createBufferBuilder(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
   }

   private static void drawTexturedBuffer(BufferBuilder bufferBuilder, GpuTextureView gpuTextureView, RenderPipeline renderPipeline) {
      DynamicTransforms.submitDraw(bufferBuilder, DrawMode.QUADS, renderPipeline, gpuTextureView);
   }

   private float getEntityWidth(LivingEntity target) {
      return target != null ? target.getWidth() : this.lastTargetWidth;
   }

   private float getEntityHeight(LivingEntity target) {
      return target != null ? target.getHeight() : this.lastTargetHeight;
   }

   private void drawCrystalMesh(BufferBuilder buffer, Vec3d targetPosition, LivingEntity target, float red, float green, float blue, float alpha) {
      float baseOrbitRadius = this.getEntityWidth(target) * 1.5F;
      float targetHeight = this.getEntityHeight(target);
      float orbitRadius = baseOrbitRadius * (1.2F - 0.5F * alpha);
      Matrix4f crystalTransform = new Matrix4f();
      Quaternionf faceRotation = new Quaternionf();
      Vector3f faceDirection = new Vector3f();
      Vector3f worldUp = new Vector3f(0.0F, 1.0F, 0.0F);
      Vector3f transformedVertex = new Vector3f();

      for (int angleDegrees = 0; angleDegrees < 360; angleDegrees += 20) {
         double orbitAngleRadians = Math.toRadians(angleDegrees + this.rotationDegrees * 0.3F);
         double orbitOffsetX = Math.sin(orbitAngleRadians) * orbitRadius;
         double orbitOffsetZ = Math.cos(orbitAngleRadians) * orbitRadius;
         double verticalOffset = 0.1 + targetHeight * Math.abs(Math.sin(angleDegrees));
         Vec3d crystalPosition = new Vec3d(targetPosition.x + orbitOffsetX, targetPosition.y + verticalOffset, targetPosition.z + orbitOffsetZ);
         Vec3d targetCenter = targetPosition.add(0.0, targetHeight / 2.0, 0.0);
         Vec3d crystalSurfacePosition = targetPosition.add(orbitOffsetX, 1.0, orbitOffsetZ);
         faceDirection.set(
               (float)(targetCenter.x - crystalSurfacePosition.x), (float)(targetCenter.y - crystalSurfacePosition.y), (float)(targetCenter.z - crystalSurfacePosition.z)
            )
            .normalize();
         faceRotation.identity().rotationTo(worldUp, faceDirection);
         float crystalScale = 0.1F * Math.min(1.0F, alpha * 1.25F);
         crystalTransform.translation(
               (float)(crystalPosition.x - this.cameraPosition.x),
               (float)(crystalPosition.y - this.cameraPosition.y),
               (float)(crystalPosition.z - this.cameraPosition.z)
            )
            .rotate(faceRotation)
            .scale(crystalScale, crystalScale, crystalScale);

         for (int triangleIndex = 0; triangleIndex < CUBE_TRIANGLES.length; triangleIndex++) {
            int[] triangleVertexIndices = CUBE_TRIANGLES[triangleIndex];
            float faceBrightness = CUBE_FACE_BRIGHTNESS[triangleIndex];

            for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
               Vector3f modelVertex = CUBE_VERTICES[triangleVertexIndices[vertexIndex]];
               crystalTransform.transformPosition(modelVertex.x, modelVertex.y, modelVertex.z, transformedVertex);
               buffer.vertex(transformedVertex.x, transformedVertex.y, transformedVertex.z).color(red * faceBrightness, green * faceBrightness, blue * faceBrightness, alpha);
            }
         }
      }
   }

   private void drawCrystalOrbit(BufferBuilder buffer, Vec3d targetPosition, LivingEntity target, float red, float green, float blue, float alpha) {
      float baseOrbitRadius = this.getEntityWidth(target) * 1.5F;
      float targetHeight = this.getEntityHeight(target);
      float orbitRadius = baseOrbitRadius * (1.2F - 0.5F * alpha);

      for (int angleDegrees = 0; angleDegrees < 360; angleDegrees += 20) {
         double orbitAngleRadians = Math.toRadians(angleDegrees + this.rotationDegrees * 0.3F);
         double orbitOffsetX = Math.sin(orbitAngleRadians) * orbitRadius;
         double orbitOffsetZ = Math.cos(orbitAngleRadians) * orbitRadius;
         double verticalOffset = 0.1 + targetHeight * Math.abs(Math.sin(angleDegrees));
         Vec3d crystalPosition = new Vec3d(targetPosition.x + orbitOffsetX, targetPosition.y + verticalOffset, targetPosition.z + orbitOffsetZ);
         this.drawBillboard(buffer, crystalPosition, Math.min(1.0F, alpha * 1.25F), red, green, blue, alpha * 0.2F);
      }
   }

   private void drawSoulEffect(BufferBuilder buffer, Vec3d targetPosition, LivingEntity target, float red, float green, float blue, float alpha) {
      float baseOrbitRadius = this.getEntityWidth(target) * 1.5F;
      float targetHeight = this.getEntityHeight(target);
      float orbitRadius = baseOrbitRadius * Math.max(0.5F, 1.2F - 0.5F * alpha);
      byte dotProgress = 0;
      byte gapDegreesRemaining = 0;

      for (int angleDegrees = 0; angleDegrees < 360; angleDegrees += 2) {
         float innerSpriteSize = (0.23F + 0.005F * dotProgress) * Math.min(1.0F, alpha * 1.25F);
         float outerSpriteSize = (0.7F + 0.005F * dotProgress) * Math.min(1.0F, alpha * 1.25F);
         if (gapDegreesRemaining > 0) {
            gapDegreesRemaining -= 2;
         } else {
            dotProgress += 2;
            if (dotProgress > 50) {
               gapDegreesRemaining = 100;
               dotProgress = 0;
            } else {
               double orbitAngleRadians = Math.toRadians(angleDegrees + this.rotationDegrees);
               double orbitOffsetX = Math.sin(orbitAngleRadians) * orbitRadius;
               double orbitOffsetZ = Math.cos(orbitAngleRadians) * orbitRadius;
               double verticalOffset = targetHeight / 1.5F + targetHeight / 3.0F * Math.sin(Math.toRadians(angleDegrees / 2.0F + this.rotationDegrees / 5.0F));
               Vec3d spritePosition = new Vec3d(targetPosition.x + orbitOffsetX, targetPosition.y + verticalOffset, targetPosition.z + orbitOffsetZ);
               this.drawBillboard(buffer, spritePosition, outerSpriteSize, red, green, blue, alpha * 0.02F);
               this.drawBillboard(buffer, spritePosition, innerSpriteSize, red, green, blue, alpha * 0.5F);
            }
         }
      }
   }

   private void drawOrbitingDots(BufferBuilder buffer, Vec3d targetPosition, LivingEntity target, float red, float green, float blue, float alpha) {
      float baseOrbitRadius = this.getEntityWidth(target) * 1.5F;
      float targetHeight = this.getEntityHeight(target);
      float hurtFlashAmount = this.getHurtFlashAmount(target);
      float orbitRadius = baseOrbitRadius * Math.max(0.5F, 0.7F - 0.2F * hurtFlashAmount + (1.0F - alpha) * 0.4F);

      for (int angleDegrees = 0; angleDegrees < 360; angleDegrees += 2) {
         if (angleDegrees / 45 % 2 != 0) {
            double orbitAngleRadians = Math.toRadians(angleDegrees + this.rotationDegrees);
            double orbitOffsetX = Math.sin(orbitAngleRadians) * orbitRadius;
            double orbitOffsetZ = Math.cos(orbitAngleRadians) * orbitRadius;
            double verticalOffset = targetHeight / 1.75F + targetHeight / 2.0F * Math.sin(Math.toRadians(this.rotationDegrees / 2.0F));
            Vec3d dotPosition = new Vec3d(targetPosition.x + orbitOffsetX, targetPosition.y + verticalOffset, targetPosition.z + orbitOffsetZ);
            this.drawBillboard(buffer, dotPosition, 0.8F * Math.min(1.0F, alpha * 1.25F), red, green, blue, alpha * 0.03F);
            this.drawBillboard(buffer, dotPosition, 0.3F * Math.min(1.0F, alpha * 1.25F), red, green, blue, alpha * 0.7F);
         }
      }
   }

   private void drawJelloTrail(BufferBuilder buffer, Vec3d targetPosition, LivingEntity target, float red, float green, float blue, float alpha) {
      float baseOrbitRadius = this.getEntityWidth(target) * 1.45F;
      float targetHeight = this.getEntityHeight(target);
      float hurtFlashAmount = this.getHurtFlashAmount(target);
      float orbitRadius = baseOrbitRadius * Math.max(0.5F, 0.7F - 0.2F * hurtFlashAmount + (1.0F - alpha) * 0.4F);

      for (int angleDegrees = 0; angleDegrees < 360; angleDegrees += 2) {
         double orbitAngleRadians = Math.toRadians(angleDegrees + this.rotationDegrees);
         double orbitOffsetX = Math.sin(orbitAngleRadians) * orbitRadius;
         double orbitOffsetZ = Math.cos(orbitAngleRadians) * orbitRadius;

         for (int trailStep = 0; trailStep < 15; trailStep++) {
            double verticalOffset = targetHeight / 1.75F + targetHeight / 2.0F * Math.sin(Math.toRadians(this.rotationDegrees / 1.5F + trailStep * 2.0F));
            Vec3d spritePosition = new Vec3d(targetPosition.x + orbitOffsetX, targetPosition.y + verticalOffset, targetPosition.z + orbitOffsetZ);
            float spriteAlpha = alpha * (trailStep / 15.0F) * 0.05F;
            this.drawBillboard(buffer, spritePosition, 0.2F * Math.min(1.0F, alpha * 1.25F), red, green, blue, spriteAlpha);
         }
      }
   }

   private void drawJelloRing(BufferBuilder buffer, Vec3d targetPosition, LivingEntity target, float red, float green, float blue, float alpha) {
      float baseOrbitRadius = this.getEntityWidth(target) * 1.45F;
      float targetHeight = this.getEntityHeight(target);
      float hurtFlashAmount = this.getHurtFlashAmount(target);
      float orbitRadius = baseOrbitRadius * Math.max(0.5F, 0.7F - 0.2F * hurtFlashAmount + (1.0F - alpha) * 0.4F);

      for (int angleDegrees = 0; angleDegrees < 360; angleDegrees += 2) {
         double orbitAngleRadians = Math.toRadians(angleDegrees + this.rotationDegrees);
         double orbitOffsetX = Math.sin(orbitAngleRadians) * orbitRadius;
         double orbitOffsetZ = Math.cos(orbitAngleRadians) * orbitRadius;
         double verticalOffset = targetHeight / 1.75F + targetHeight / 2.0F * Math.sin(Math.toRadians(this.rotationDegrees / 1.5F + 30.0F));
         Vec3d spritePosition = new Vec3d(targetPosition.x + orbitOffsetX, targetPosition.y + verticalOffset, targetPosition.z + orbitOffsetZ);
         this.drawBillboard(buffer, spritePosition, 0.2F * Math.min(1.0F, alpha * 1.25F), red, green, blue, alpha * 0.2F);
      }
   }

   private void updateGhostTrails(Vec3d targetPosition, LivingEntity target, long currentTimeMillis, float frameDeltaMillis) {
      float widthScale = this.getEntityWidth(target) * 1.4F;
      float targetHeight = this.getEntityHeight(target);
      float orbitRadius = widthScale * Math.max(0.6F, 1.2F - 0.4F * this.fadeProgress);

      while (this.ghostTrailHistory.size() < 4) {
         this.ghostTrailHistory.add(new ArrayList<>());
      }

      ArrayList<Vec3d> trailAnchorPositions = new ArrayList<>();
      double[] trailHeightFractions = new double[]{0.88, 0.62, 0.38, 0.15};

      for (int trailIndex = 0; trailIndex < 4; trailIndex++) {
         double orbitAngleRadians = Math.toRadians(this.rotationDegrees * 1.2 + trailIndex * 90.0);
         double bobOffset = Math.sin(Math.toRadians(this.rotationDegrees * 0.8 + trailIndex * 120.0)) * 0.15;
         double trailOrbitRadius = orbitRadius * (1.0 + bobOffset);
         double trailOffsetX = Math.sin(orbitAngleRadians) * trailOrbitRadius;
         double trailOffsetZ = Math.cos(orbitAngleRadians) * trailOrbitRadius;
         double trailOffsetY = targetHeight * trailHeightFractions[trailIndex] + Math.sin(Math.toRadians(this.rotationDegrees * 1.5 + trailIndex * 75.0)) * (targetHeight * 0.15);
         trailOffsetY += Math.sin(orbitAngleRadians) * (targetHeight * 0.1);
         Vec3d trailPosition = new Vec3d(targetPosition.x + trailOffsetX, targetPosition.y + trailOffsetY, targetPosition.z + trailOffsetZ);
         trailAnchorPositions.add(trailPosition);
         List<TargetEsp.PositionSample> trailSamples = this.ghostTrailHistory.get(trailIndex);
         if (trailSamples.isEmpty()) {
            trailSamples.add(0, new TargetEsp.PositionSample(trailPosition, currentTimeMillis));
         } else {
            double distanceSquared = ((TargetEsp.PositionSample)trailSamples.get(0)).position().squaredDistanceTo(trailPosition);
            if (distanceSquared > 9.0) {
               trailSamples.clear();
               trailSamples.add(0, new TargetEsp.PositionSample(trailPosition, currentTimeMillis));
            } else if (distanceSquared > 4.0E-4) {
               trailSamples.add(0, new TargetEsp.PositionSample(trailPosition, currentTimeMillis));
            }
         }

         trailSamples.removeIf(sample -> currentTimeMillis - sample.timestampMillis() > 420L);
      }

      if (currentTimeMillis - this.lastGhostParticleSpawnMillis > 25L) {
         this.lastGhostParticleSpawnMillis = currentTimeMillis;
         ThreadLocalRandom random = ThreadLocalRandom.current();

          for (Vec3d trailAnchor : trailAnchorPositions) {
            if (this.ghostParticles.size() >= 80) {
               break;
            }

            if (random.nextFloat() < 0.65F) {
               Vec3d particlePosition = trailAnchor.add(
                  (random.nextDouble() - 0.5) * 0.18, (random.nextDouble() - 0.5) * 0.18, (random.nextDouble() - 0.5) * 0.18
               );
               Vec3d particleVelocity = new Vec3d(
                  (random.nextDouble() - 0.5) * 0.3, random.nextDouble() * 0.25 + 0.05, (random.nextDouble() - 0.5) * 0.3
               );
               long particleLifetimeMillis = random.nextLong(600L, 1200L);
               float particleSize = 0.05F + random.nextFloat() * 0.07F;
               this.ghostParticles.add(new TargetEsp.GhostParticle(particlePosition, particleVelocity, currentTimeMillis, particleLifetimeMillis, particleSize));
            }
         }
      }

      float deltaSeconds = frameDeltaMillis / 1000.0F;
      Iterator<TargetEsp.GhostParticle> particleIterator = this.ghostParticles.iterator();

      while (particleIterator.hasNext()) {
         TargetEsp.GhostParticle particle = particleIterator.next();
         if (currentTimeMillis - particle.createdAtMillis >= particle.durationMillis) {
            particleIterator.remove();
         } else {
            particle.position = particle.position.add(particle.velocity.multiply(deltaSeconds));
            particle.velocity = particle.velocity.multiply(Math.pow(0.7, deltaSeconds * 10.0F));
         }
      }
   }

   private void drawGhostTrail(BufferBuilder buffer, float red, float green, float blue, float alpha, long currentTimeMillis) {
      float trailOpacityScale = Math.min(1.0F, alpha * 1.25F);

      for (List<TargetEsp.PositionSample> trailSamples : this.ghostTrailHistory) {
         if (!trailSamples.isEmpty()) {
            this.drawBillboard(
               buffer, ((TargetEsp.PositionSample)trailSamples.get(0)).position(), 0.85F * trailOpacityScale, red, green, blue, alpha * 0.35F
            );

            for (int segmentIndex = 0; segmentIndex < trailSamples.size() - 1; segmentIndex++) {
               TargetEsp.PositionSample fromSample = trailSamples.get(segmentIndex);
               TargetEsp.PositionSample toSample = trailSamples.get(segmentIndex + 1);
               int segmentSteps = Math.max(
                  1, (int)(fromSample.position().distanceTo(toSample.position()) / 0.045)
               );

               for (int stepIndex = 0; stepIndex < segmentSteps; stepIndex++) {
                  double interpolation = (double)stepIndex / segmentSteps;
                  Vec3d interpolatedPosition = fromSample.position().lerp(toSample.position(), interpolation);
                  float trailOpacity = getTrailPointOpacity(fromSample, toSample, interpolation, currentTimeMillis);
                  this.drawBillboard(buffer, interpolatedPosition, (0.65F * trailOpacity + 0.1F) * trailOpacityScale, red, green, blue, alpha * 0.08F * trailOpacity * trailOpacity);
               }
            }
         }
      }

      for (TargetEsp.GhostParticle particle : this.ghostParticles) {
         float particleOpacity = getGhostParticleOpacity(particle, currentTimeMillis) * alpha;
         this.drawBillboard(
            buffer,
            particle.position,
            particle.size * 3.0F * trailOpacityScale,
            red,
            green,
            blue,
            particleOpacity * 0.12F
         );
      }
   }

   private void drawGhostGlow(BufferBuilder buffer, float red, float green, float blue, float alpha, long currentTimeMillis) {
      float glowOpacityScale = Math.min(1.0F, alpha * 1.25F);

      for (List<TargetEsp.PositionSample> trailSamples : this.ghostTrailHistory) {
         if (!trailSamples.isEmpty()) {
            Vec3d latestTrailPosition = ((TargetEsp.PositionSample)trailSamples.get(0)).position();
            this.drawBillboard(buffer, latestTrailPosition, 0.38F * glowOpacityScale, red, green, blue, alpha * 0.75F);
            this.drawBillboard(buffer, latestTrailPosition, 0.18F * glowOpacityScale, 1.0F, 1.0F, 1.0F, alpha * 0.95F);

            for (int segmentIndex = 0; segmentIndex < trailSamples.size() - 1; segmentIndex++) {
               TargetEsp.PositionSample fromSample = trailSamples.get(segmentIndex);
               TargetEsp.PositionSample toSample = trailSamples.get(segmentIndex + 1);
               int segmentSteps = Math.max(
                  1, (int)(fromSample.position().distanceTo(toSample.position()) / 0.045)
               );

               for (int stepIndex = 0; stepIndex < segmentSteps; stepIndex++) {
                  double interpolation = (double)stepIndex / segmentSteps;
                  Vec3d interpolatedPosition = fromSample.position().lerp(toSample.position(), interpolation);
                  float trailOpacity = getTrailPointOpacity(fromSample, toSample, interpolation, currentTimeMillis);
                  float highlightStrength = trailOpacity * 0.5F;
                  float highlightRed = Math.min(1.0F, red + (1.0F - red) * highlightStrength);
                  float highlightGreen = Math.min(1.0F, green + (1.0F - green) * highlightStrength);
                  float highlightBlue = Math.min(1.0F, blue + (1.0F - blue) * highlightStrength);
                  this.drawBillboard(buffer, interpolatedPosition, (0.24F * trailOpacity * trailOpacity + 0.03F) * glowOpacityScale, highlightRed, highlightGreen, highlightBlue, alpha * 0.65F * trailOpacity * trailOpacity);
               }
            }
         }
      }

      for (TargetEsp.GhostParticle particle : this.ghostParticles) {
         float particleOpacity = getGhostParticleOpacity(particle, currentTimeMillis) * alpha;
         this.drawBillboard(
            buffer,
            particle.position,
            particle.size * glowOpacityScale,
            red,
            green,
            blue,
            particleOpacity * 0.85F
         );
         this.drawBillboard(
            buffer,
            particle.position,
            particle.size * 0.5F * glowOpacityScale,
            1.0F,
            1.0F,
            1.0F,
            particleOpacity * 0.9F
         );
      }
   }

   private static float getTrailPointOpacity(TargetEsp.PositionSample from, TargetEsp.PositionSample to, double interpolation, long currentTimeMillis) {
      double interpolatedTimestamp = from.timestampMillis() * (1.0 - interpolation) + to.timestampMillis() * interpolation;
      float ageProgress = Math.clamp((float)(currentTimeMillis - interpolatedTimestamp) / 420.0F, 0.0F, 1.0F);
      return 1.0F - ageProgress;
   }

   private static float getGhostParticleOpacity(TargetEsp.GhostParticle particle, long currentTimeMillis) {
      float ageProgress = Math.clamp((float)(currentTimeMillis - particle.createdAtMillis) / (float)particle.durationMillis, 0.0F, 1.0F);
      return (float)Math.sin(ageProgress * Math.PI);
   }

   private void drawRotatingCube(BufferBuilder buffer, Vec3d targetPosition, LivingEntity target, float red, float green, float blue, float alpha) {
      float entityHeight = this.getEntityHeight(target);
      float entityWidth = this.getEntityWidth(target);
      Vec3d cubeCenter = targetPosition.add(0.0, entityHeight * 0.5, 0.0);
      float cubeSize = Math.max(entityWidth * 1.4F, 0.9F) * Math.min(1.0F, alpha * 1.25F);
      float cubeRotationDegrees = 45.0F + this.rotationDegrees * 0.5F;
      this.drawRotatedBillboard(buffer, cubeCenter, cubeSize, cubeRotationDegrees, red, green, blue, alpha);
   }

   private void drawBillboard(BufferBuilder buffer, Vec3d position, float size, float red, float green, float blue, float alpha) {
      this.drawRotatedBillboard(buffer, position, size, 0.0F, red, green, blue, alpha);
   }

   private void drawRotatedBillboard(
      BufferBuilder buffer, Vec3d position, float size, float rotationDegrees, float red, float green, float blue, float alpha
   ) {
      if (!(alpha <= 0.001F) && !(size <= 0.0F)) {
         double cameraDistance = position.distanceTo(this.cameraPosition);
         if (!(cameraDistance < 0.15)) {
            float billboardSize = size;
            float billboardAlpha = alpha;
            if (cameraDistance < size * 0.6F) {
               float distanceScale = (float)((cameraDistance - 0.15) / (size * 0.6F - 0.15));
               if (distanceScale <= 0.0F) {
                  return;
               }

               distanceScale = Math.clamp(distanceScale, 0.0F, 1.0F);
               billboardSize = Math.max(0.05F, size * distanceScale);
               billboardAlpha = alpha * distanceScale;
            }

            float halfSize = billboardSize * 0.5F;
            float cameraRelativeX = (float)(position.x - this.cameraPosition.x);
            float cameraRelativeY = (float)(position.y - this.cameraPosition.y);
            float cameraRelativeZ = (float)(position.z - this.cameraPosition.z);
            float rotationCosine = 1.0F;
            float rotationSine = 0.0F;
            if (rotationDegrees != 0.0F) {
               float rotationRadians = rotationDegrees * (float) (Math.PI / 180.0);
               rotationCosine = MathHelper.cos(rotationRadians);
               rotationSine = MathHelper.sin(rotationRadians);
            }

            this.vertexAtCameraOffset(buffer, cameraRelativeX, cameraRelativeY, cameraRelativeZ, -halfSize, halfSize, rotationCosine, rotationSine)
               .texture(0.0F, 1.0F)
               .color(red, green, blue, billboardAlpha);
            this.vertexAtCameraOffset(buffer, cameraRelativeX, cameraRelativeY, cameraRelativeZ, halfSize, halfSize, rotationCosine, rotationSine)
               .texture(1.0F, 1.0F)
               .color(red, green, blue, billboardAlpha);
            this.vertexAtCameraOffset(buffer, cameraRelativeX, cameraRelativeY, cameraRelativeZ, halfSize, -halfSize, rotationCosine, rotationSine)
               .texture(1.0F, 0.0F)
               .color(red, green, blue, billboardAlpha);
            this.vertexAtCameraOffset(buffer, cameraRelativeX, cameraRelativeY, cameraRelativeZ, -halfSize, -halfSize, rotationCosine, rotationSine)
               .texture(0.0F, 0.0F)
               .color(red, green, blue, billboardAlpha);
         }
      }
   }

   private BufferBuilder vertexAtCameraOffset(
      BufferBuilder buffer, float x, float y, float z, float rightOffset, float upOffset, float cosRotation, float sinRotation
   ) {
      float rotatedRightOffset = rightOffset * cosRotation - upOffset * sinRotation;
      float rotatedUpOffset = rightOffset * sinRotation + upOffset * cosRotation;
      buffer.vertex(
         x + this.cameraRight.x * rotatedRightOffset + this.cameraUp.x * rotatedUpOffset,
         y + this.cameraRight.y * rotatedRightOffset + this.cameraUp.y * rotatedUpOffset,
         z + this.cameraRight.z * rotatedRightOffset + this.cameraUp.z * rotatedUpOffset
      );
      return buffer;
   }

   private void updateLightningTrails(Vec3d targetPosition, boolean hasTarget, long currentTimeMillis) {
      if (this.currentTarget != null || this.lastTargetPosition != null) {
         float configuredTrailCount = this.lightningCountSetting.getValue();
         int maxTrailCount = Math.max(4, Math.round(configuredTrailCount) * 2);
         long spawnIntervalMillis = (long)Math.max(15.0F, 65.0F / (configuredTrailCount / 3.0F));
         if (hasTarget && this.currentTarget != null && this.currentTarget.isAlive() && currentTimeMillis - this.lastLightningSpawnMillis > spawnIntervalMillis && this.lightningTrails.size() < maxTrailCount) {
            this.lightningTrails.add(this.createLightningTrail(this.currentTarget));
            this.lastLightningSpawnMillis = currentTimeMillis;
         }

         this.lightningTrails.removeIf(expiredTrail -> currentTimeMillis - expiredTrail.createdAtMillis > expiredTrail.durationMillis);
         if (!this.lightningTrails.isEmpty()) {
            float hitFlashAmount = this.getHitFlashAmount(this.currentTarget);
            int baseColorArgb = Accent.getAccentColorWithAlpha(255).toArgb() | 0xFF000000;
            int trailColorArgb = blendArgb(baseColorArgb, -50116, hitFlashAmount);
            int highlightColorArgb = blendArgb(-1, -50116, hitFlashAmount);
            float widthScale = this.lightningWidthSetting.getValue();
            BufferBuilder lightningRibbonBuffer = createColorBuffer();
            boolean hasRibbonGeometry = false;

            for (TargetEsp.LightningTrail trail : this.lightningTrails) {
               float trailOpacity = this.getLightningOpacity(trail, currentTimeMillis) * this.fadeProgress;
               if (!(trailOpacity <= 0.02F)) {
                  int ribbonAlpha = (int)(trailOpacity * 50.0F);
                  hasRibbonGeometry |= this.drawLightningRibbon(
                     lightningRibbonBuffer, targetPosition, trail.points, 0.032F * widthScale, withAlpha(trailColorArgb, ribbonAlpha)
                  );

                  for (List<Vec3d> branchPoints : trail.branches) {
                     hasRibbonGeometry |= this.drawLightningRibbon(
                        lightningRibbonBuffer, targetPosition, branchPoints, 0.02F * widthScale, withAlpha(trailColorArgb, (int)(ribbonAlpha * 0.85F))
                     );
                  }
               }
            }

            for (TargetEsp.LightningTrail trail : this.lightningTrails) {
               float trailOpacity = this.getLightningOpacity(trail, currentTimeMillis) * this.fadeProgress;
               if (!(trailOpacity <= 0.02F)) {
                  int ribbonAlpha = (int)(trailOpacity * 105.0F);
                  hasRibbonGeometry |= this.drawLightningRibbon(
                     lightningRibbonBuffer, targetPosition, trail.points, 0.014F * widthScale, withAlpha(trailColorArgb, ribbonAlpha)
                  );

                  for (List<Vec3d> branchPoints : trail.branches) {
                     hasRibbonGeometry |= this.drawLightningRibbon(
                        lightningRibbonBuffer, targetPosition, branchPoints, 0.009F * widthScale, withAlpha(trailColorArgb, (int)(ribbonAlpha * 0.8F))
                     );
                  }
               }
            }

            for (TargetEsp.LightningTrail trail : this.lightningTrails) {
               float trailOpacity = this.getLightningOpacity(trail, currentTimeMillis) * this.fadeProgress;
               if (!(trailOpacity <= 0.02F)) {
                  int ribbonAlpha = (int)(trailOpacity * 245.0F);
                  hasRibbonGeometry |= this.drawLightningRibbon(
                     lightningRibbonBuffer, targetPosition, trail.points, 0.005F * widthScale, withAlpha(highlightColorArgb, ribbonAlpha)
                  );

                  for (List<Vec3d> branchPoints : trail.branches) {
                     hasRibbonGeometry |= this.drawLightningRibbon(
                        lightningRibbonBuffer, targetPosition, branchPoints, 0.003F * widthScale, withAlpha(highlightColorArgb, (int)(ribbonAlpha * 0.85F))
                     );
                  }
               }
            }

            DynamicTransforms.submitDraw(lightningRibbonBuffer, DrawMode.TRIANGLES, WorldRenderPipelines.Ill111IlI11l, null);
            GpuTextureView lightningTextureView = this.getTextureView(BLOOM_TEXTURE);
            if (lightningTextureView == null) {
               lightningTextureView = this.getTextureView(GLOW_TEXTURE);
            }

            if (lightningTextureView != null) {
               BufferBuilder lightningGlowBuffer = createTexturedBuffer();

               for (TargetEsp.LightningTrail trail : this.lightningTrails) {
                  float trailOpacity = this.getLightningOpacity(trail, currentTimeMillis) * this.fadeProgress;
                  if (!(trailOpacity <= 0.03F)) {
                     int glowAlpha = (int)(trailOpacity * 45.0F);
                     if (glowAlpha > 0) {
                        for (int vertexIndex = 0; vertexIndex < trail.points.size(); vertexIndex += 3) {
                           this.drawTexturedBillboard(
                              lightningGlowBuffer,
                              targetPosition.add(trail.points.get(vertexIndex)),
                              0.14F * widthScale,
                              withAlpha(trailColorArgb, glowAlpha)
                           );
                        }
                     }

                     int pointAlpha = (int)(trailOpacity * 120.0F);
                     if (pointAlpha > 0) {
                        for (Vec3d pointOffset : trail.points) {
                           this.drawTexturedBillboard(lightningGlowBuffer, targetPosition.add(pointOffset), 0.045F * widthScale, withAlpha(highlightColorArgb, pointAlpha));
                        }
                     }
                  }
               }

               drawTexturedBuffer(lightningGlowBuffer, lightningTextureView, WorldRenderPipelines.I1lIIII1III1lIlIl1l);
            }
         }
      }
   }

   private void drawTexturedBillboard(BufferBuilder buffer, Vec3d position, float size, int colorArgb) {
      this.drawBillboard(
         buffer, position, size, (colorArgb >> 16 & 0xFF) / 255.0F, (colorArgb >> 8 & 0xFF) / 255.0F, (colorArgb & 0xFF) / 255.0F, (colorArgb >>> 24 & 0xFF) / 255.0F
      );
   }

   private TargetEsp.LightningTrail createLightningTrail(LivingEntity target) {
      ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
      float halfTargetWidth = this.getEntityWidth(target) * 0.5F;
      float targetHeight = this.getEntityHeight(target);
      double startAngleRadians = threadLocalRandom.nextDouble() * Math.PI * 2.0;
      double startHeight = 0.15 + threadLocalRandom.nextDouble() * Math.max(0.2, targetHeight - 0.4);
      Vec3d startPoint = new Vec3d(Math.cos(startAngleRadians) * halfTargetWidth * 0.7, startHeight, Math.sin(startAngleRadians) * halfTargetWidth * 0.7);
      double endAngleRadians = threadLocalRandom.nextDouble() * Math.PI * 2.0;
      double elevationRadians = Math.toRadians((threadLocalRandom.nextDouble() - 0.5) * 150.0);
      Vec3d branchDirection = new Vec3d(Math.cos(elevationRadians) * Math.cos(endAngleRadians), Math.sin(elevationRadians) * 0.8 + 0.25, Math.cos(elevationRadians) * Math.sin(endAngleRadians)).normalize();
      double mainSegmentLength = 0.35 + threadLocalRandom.nextDouble() * 0.45;
      Vec3d mainEndpoint = startPoint.add(branchDirection.multiply(mainSegmentLength));
      List<Vec3d> mainPath = this.subdivideLightningPath(List.of(startPoint, mainEndpoint), 0.09, 3);
      List<List<Vec3d>> branches = new ArrayList<>();
      int branchCount = 1 + threadLocalRandom.nextInt(3);

      for (int branchIndex = 0; branchIndex < branchCount; branchIndex++) {
         int branchAnchorIndex = 1 + threadLocalRandom.nextInt(Math.max(1, mainPath.size() - 2));
         Vec3d branchAnchor = mainPath.get(branchAnchorIndex);
         Vec3d branchDirectionOffset = this.randomPerpendicular(branchDirection);
         if (threadLocalRandom.nextBoolean()) {
            branchDirectionOffset = branchDirectionOffset.multiply(-1.0);
         }

         double branchLength = 0.12 + threadLocalRandom.nextDouble() * 0.18;
         Vec3d branchEndpoint = branchAnchor.add(branchDirectionOffset.multiply(branchLength));
         branches.add(this.subdivideLightningPath(List.of(branchAnchor, branchEndpoint), 0.05, 2));
      }

      return new TargetEsp.LightningTrail(mainPath, branches, 140L + threadLocalRandom.nextLong(160L), threadLocalRandom.nextFloat() * 6.28F);
   }

   private List<Vec3d> subdivideLightningPath(List<Vec3d> points, double initialDisplacement, int subdivisionPasses) {
      List<Vec3d> subdividedPoints = new ArrayList<>(points);
      double displacementMagnitude = initialDisplacement;

      for (int passIndex = 0; passIndex < subdivisionPasses; passIndex++) {
         List<Vec3d> nextPassPoints = new ArrayList<>(subdividedPoints.size() * 2);

         for (int segmentIndex = 0; segmentIndex < subdividedPoints.size() - 1; segmentIndex++) {
            Vec3d segmentStart = subdividedPoints.get(segmentIndex);
            Vec3d segmentEnd = subdividedPoints.get(segmentIndex + 1);
            nextPassPoints.add(segmentStart);
            Vec3d segmentDirection = segmentEnd.subtract(segmentStart);
            if (segmentDirection.lengthSquared() < 1.0E-7) {
               nextPassPoints.add(segmentStart.add(segmentEnd).multiply(0.5));
            } else {
               Vec3d perpendicularDirection = this.randomPerpendicular(segmentDirection.normalize());
               double midpointDisplacement = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0 * displacementMagnitude;
               nextPassPoints.add(segmentStart.add(segmentEnd).multiply(0.5).add(perpendicularDirection.multiply(midpointDisplacement)));
            }
         }

         nextPassPoints.add(subdividedPoints.get(subdividedPoints.size() - 1));
         subdividedPoints = nextPassPoints;
         displacementMagnitude *= 0.5;
      }

      return subdividedPoints;
   }

   private Vec3d randomPerpendicular(Vec3d direction) {
      Vec3d referenceAxis = Math.abs(direction.y) < 0.9 ? new Vec3d(0.0, 1.0, 0.0) : new Vec3d(1.0, 0.0, 0.0);
      Vec3d firstPerpendicular = direction.crossProduct(referenceAxis).normalize();
      Vec3d secondPerpendicular = direction.crossProduct(firstPerpendicular).normalize();
      double randomAngleRadians = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
      return firstPerpendicular.multiply(Math.cos(randomAngleRadians)).add(secondPerpendicular.multiply(Math.sin(randomAngleRadians)));
   }

   private boolean drawLightningRibbon(BufferBuilder buffer, Vec3d origin, List<Vec3d> pathPoints, float width, int colorArgb) {
      int alphaByte = colorArgb >>> 24 & 0xFF;
      if (alphaByte > 0 && pathPoints.size() >= 2) {
         float red = (colorArgb >> 16 & 0xFF) / 255.0F;
         float green = (colorArgb >> 8 & 0xFF) / 255.0F;
         float blue = (colorArgb & 0xFF) / 255.0F;
         float alpha = alphaByte / 255.0F;
         float halfWidth = width * 0.5F;
         Vec3d cameraPosition = this.cameraPosition;
         int pointCount = pathPoints.size();
         Vec3d[] leftVertices = new Vec3d[pointCount];
         Vec3d[] rightVertices = new Vec3d[pointCount];
         Vec3d[] segmentNormals = new Vec3d[pointCount - 1];

         for (int segmentIndex = 0; segmentIndex < pointCount - 1; segmentIndex++) {
            Vec3d segmentStart = origin.add(pathPoints.get(segmentIndex));
            Vec3d segmentEnd = origin.add(pathPoints.get(segmentIndex + 1));
            Vec3d segmentDirection = segmentEnd.subtract(segmentStart);
            if (segmentDirection.length() < 1.0E-6) {
               segmentNormals[segmentIndex] = new Vec3d(0.0, 1.0, 0.0);
            } else {
               Vec3d segmentToCamera = segmentStart.add(segmentEnd).multiply(0.5).subtract(cameraPosition);
               Vec3d normalCandidate = segmentDirection.crossProduct(segmentToCamera);
               double normalLength = normalCandidate.length();
               segmentNormals[segmentIndex] = !(normalLength < 1.0E-6) && Double.isFinite(normalLength) ? normalCandidate.multiply(1.0 / normalLength) : new Vec3d(0.0, 1.0, 0.0);
            }
         }

         for (int pointIndex = 0; pointIndex < pointCount; pointIndex++) {
            Vec3d pointPosition = origin.add(pathPoints.get(pointIndex));
            Vec3d pointNormal;
            if (pointIndex == 0) {
               pointNormal = segmentNormals[0];
            } else if (pointIndex == pointCount - 1) {
               pointNormal = segmentNormals[pointCount - 2];
            } else {
               pointNormal = segmentNormals[pointIndex - 1].add(segmentNormals[pointIndex]);
               double averagedNormalLength = pointNormal.length();
               pointNormal = averagedNormalLength > 1.0E-5 && Double.isFinite(averagedNormalLength) ? pointNormal.multiply(1.0 / averagedNormalLength) : segmentNormals[pointIndex];
            }

            Vec3d halfWidthOffset = pointNormal.multiply(halfWidth);
            leftVertices[pointIndex] = pointPosition.subtract(halfWidthOffset).subtract(cameraPosition);
            rightVertices[pointIndex] = pointPosition.add(halfWidthOffset).subtract(cameraPosition);
         }

         for (int segmentIndex = 0; segmentIndex < pointCount - 1; segmentIndex++) {
            emitTriangle(buffer, leftVertices[segmentIndex], rightVertices[segmentIndex], rightVertices[segmentIndex + 1], red, green, blue, alpha);
            emitTriangle(buffer, leftVertices[segmentIndex], rightVertices[segmentIndex + 1], leftVertices[segmentIndex + 1], red, green, blue, alpha);
         }

         return true;
      } else {
         return false;
      }
   }

   private static void emitTriangle(
      BufferBuilder buffer, Vec3d firstVertex, Vec3d secondVertex, Vec3d thirdVertex, float red, float green, float blue, float alpha
   ) {
      buffer.vertex((float)firstVertex.x, (float)firstVertex.y, (float)firstVertex.z).color(red, green, blue, alpha);
      buffer.vertex((float)secondVertex.x, (float)secondVertex.y, (float)secondVertex.z).color(red, green, blue, alpha);
      buffer.vertex((float)thirdVertex.x, (float)thirdVertex.y, (float)thirdVertex.z).color(red, green, blue, alpha);
   }

   private float getLightningOpacity(TargetEsp.LightningTrail trail, long currentTimeMillis) {
      float ageProgress = MathHelper.clamp((float)(currentTimeMillis - trail.createdAtMillis) / (float)trail.durationMillis, 0.0F, 1.0F);
      float fadeProgress = ageProgress < 0.18F ? ageProgress / 0.18F : 1.0F - (ageProgress - 0.18F) / 0.82F;
      float flickerAmount = 0.78F + 0.22F * (float)Math.sin((float)currentTimeMillis * 0.045F + trail.phase);
      return MathHelper.clamp(fadeProgress, 0.0F, 1.0F) * flickerAmount;
   }

   private float getHitFlashAmount(LivingEntity target) {
      if (!this.colorOnHitSetting.getValue()) {
         return 0.0F;
      }

      float hurtFlashAmount = 0.0F;
      if (target != null && target.hurtTime > 0) {
         float partialTicks = DaamkyWorldRender.getTickDelta();
         hurtFlashAmount = MathHelper.clamp(target.hurtTime - partialTicks, 0.0F, 10.0F) / 10.0F;
      }

      float attackFlashAmount = 0.0F;
      long timeSinceAttackMillis = System.currentTimeMillis() - this.lastAttackMillis;
      if (timeSinceAttackMillis < 420L && (target == null || target == this.lastAttackedTarget || target == this.currentTarget)) {
         attackFlashAmount = 1.0F - (float)timeSinceAttackMillis / 420.0F;
      }

      float combinedFlashAmount = Math.max(hurtFlashAmount, attackFlashAmount);
      return MathHelper.clamp(combinedFlashAmount * combinedFlashAmount * (3.0F - 2.0F * combinedFlashAmount), 0.0F, 1.0F);
   }

   private static int blendArgb(int startColor, int endColor, float progress) {
      progress = MathHelper.clamp(progress, 0.0F, 1.0F);
      int startRed = startColor >> 16 & 0xFF;
      int startGreen = startColor >> 8 & 0xFF;
      int startBlue = startColor & 0xFF;
      int startAlpha = startColor >>> 24 & 0xFF;
      int endRed = endColor >> 16 & 0xFF;
      int endGreen = endColor >> 8 & 0xFF;
      int endBlue = endColor & 0xFF;
      int endAlpha = endColor >>> 24 & 0xFF;
      int red = (int)(startRed + (endRed - startRed) * progress);
      int green = (int)(startGreen + (endGreen - startGreen) * progress);
      int blue = (int)(startBlue + (endBlue - startBlue) * progress);
      int alpha = (int)(startAlpha + (endAlpha - startAlpha) * progress);
      return alpha << 24 | red << 16 | green << 8 | blue;
   }

   private static int withAlpha(int rgb, int alpha) {
      alpha = Math.max(0, Math.min(255, alpha));
      return alpha << 24 | rgb & 16777215;
   }

   private static float approach(float current, float target, float maxStep) {
      if (current < target) {
         return Math.min(current + maxStep, target);
      } else {
         return current > target ? Math.max(current - maxStep, target) : current;
      }
   }

   private Vec3d getTargetRenderPosition(float partialTicks) {
      LivingEntity livingEntity = this.currentTarget;
      return livingEntity != null && livingEntity.isAlive() ? DaamkyWorldRender.getInterpolatedPosition(livingEntity, partialTicks) : this.lastTargetPosition;
   }

   private float getHurtFlashAmount(LivingEntity target) {
      if (this.colorOnHitSetting.getValue() && target != null) {
         float hurtProgress = Math.clamp(target.hurtTime / 10.0F, 0.0F, 1.0F);
         return hurtProgress * hurtProgress * (3.0F - 2.0F * hurtProgress);
      } else {
         return 0.0F;
      }
   }

   private GpuTextureView getTextureView(Identifier textureId) {
      GpuTextureView cachedTextureView = TEXTURE_CACHE.get(textureId);
      if (cachedTextureView != null) {
         return cachedTextureView;
      }

      NativeImage nativeImage = null;

      try {
         Optional<Resource> resourceOptional = CLIENT.getResourceManager().getResource(textureId);
         if (resourceOptional.isPresent()) {
            try (InputStream inputStream = resourceOptional.get().getInputStream()) {
               nativeImage = NativeImage.read(inputStream);
            }
         }
      } catch (Exception resourceReadException) {
      }

      if (nativeImage == null) {
         if (CUBE_TEXTURE.equals(textureId)) {
            return null;
         }

         nativeImage = createRadialGlowTexture(128);
      } else if (!CUBE_TEXTURE.equals(textureId)) {
         int imageWidth = nativeImage.getWidth();
         int imageHeight = nativeImage.getHeight();
         float centerX = imageWidth / 2.0F;
         float centerY = imageHeight / 2.0F;

         for (int pixelY = 0; pixelY < imageHeight; pixelY++) {
            for (int pixelX = 0; pixelX < imageWidth; pixelX++) {
               float normalizedX = (pixelX + 0.5F - centerX) / centerX;
               float normalizedY = (pixelY + 0.5F - centerY) / centerY;
               float normalizedDistance = (float)Math.sqrt(normalizedX * normalizedX + normalizedY * normalizedY);
               if (normalizedDistance >= 1.0F) {
                  nativeImage.setColorArgb(pixelX, pixelY, 0);
               } else {
                  int pixelColor = nativeImage.getColorArgb(pixelX, pixelY);
                  int originalAlpha = pixelColor >>> 24 & 0xFF;
                  float edgeFade = 1.0F - normalizedDistance;
                  float smoothedEdgeFade = edgeFade * edgeFade * (3.0F - 2.0F * edgeFade);
                  int fadedAlpha = (int)(originalAlpha * smoothedEdgeFade);
                  nativeImage.setColorArgb(pixelX, pixelY, fadedAlpha << 24 | pixelColor & 16777215);
               }
            }
         }
      }

      try {
         NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(textureId::toString, nativeImage);
         GpuTextureView createdTextureView = nativeImageBackedTexture.getGlTextureView();
         if (createdTextureView != null) {
            TEXTURE_CACHE.put(textureId, createdTextureView);
         }

         return createdTextureView;
      } catch (Exception textureCreationException) {
         return null;
      }
   }

   private static NativeImage createRadialGlowTexture(int size) {
      NativeImage nativeImage = new NativeImage(size, size, true);
      float radius = size / 2.0F;

      for (int pixelY = 0; pixelY < size; pixelY++) {
         for (int pixelX = 0; pixelX < size; pixelX++) {
            float normalizedX = (pixelX + 0.5F - radius) / radius;
            float normalizedY = (pixelY + 0.5F - radius) / radius;
            float normalizedDistance = (float)Math.sqrt(normalizedX * normalizedX + normalizedY * normalizedY);
            if (normalizedDistance >= 1.0F) {
               nativeImage.setColorArgb(pixelX, pixelY, 0);
            } else {
               float edgeFade = 1.0F - normalizedDistance;
               float smoothedEdgeFade = edgeFade * edgeFade * (3.0F - 2.0F * edgeFade);
               int alpha = (int)(smoothedEdgeFade * smoothedEdgeFade * 255.0F);
               nativeImage.setColorArgb(pixelX, pixelY, alpha << 24 | 16777215);
            }
         }
      }

      return nativeImage;
   }

   private static final class LightningTrail {
      final List<Vec3d> points;
      final List<List<Vec3d>> branches;
      final long createdAtMillis;
      final long durationMillis;
      final float phase;

      LightningTrail(List<Vec3d> points, List<List<Vec3d>> branches, long durationMillis, float phase) {
         this.points = points;
         this.branches = branches;
         this.createdAtMillis = System.currentTimeMillis();
         this.durationMillis = durationMillis;
         this.phase = phase;
      }
   }

   private record PositionSample(Vec3d position, long timestampMillis) {

      private PositionSample(Vec3d position, long timestampMillis) {
         this.position = position;
         this.timestampMillis = timestampMillis;
      }
   }

   private static final class GhostParticle {
      Vec3d position;
      Vec3d velocity;
      final long createdAtMillis;
      final long durationMillis;
      final float size;

      GhostParticle(Vec3d position, Vec3d velocity, long createdAtMillis, long durationMillis, float size) {
         this.position = position;
         this.velocity = velocity;
         this.createdAtMillis = createdAtMillis;
         this.durationMillis = durationMillis;
         this.size = size;
      }
   }
}

