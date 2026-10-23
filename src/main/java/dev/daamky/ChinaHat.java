package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventRender3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class ChinaHat extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting hatStyle = new MultiOptionSetting("Стиль", "Градиент", "Градиент", "Сплошной", "Стекло");
   private final BooleanSetting renderForFriends = new BooleanSetting("Друзьям", true);
   private final BooleanSetting renderInFirstPerson = new BooleanSetting("От первого лица", false);
   private final FloatSetting baseRadius = new FloatSetting("Радиус", 0.65F, 0.3F, 1.2F, 0.05F);
   private final BooleanSetting glowEnabled = new BooleanSetting("Glow", false);
   private final FloatSetting glowRadius = new FloatSetting("Радиус свечения", 2.0F, 0.5F, 5.0F, 0.1F);
   private final FloatSetting glowStrength = new FloatSetting("Сила свечения", 1.5F, 0.2F, 4.0F, 0.1F);
   private final FloatSetting coneHeight = new FloatSetting("Высота", 0.3F, 0.1F, 0.8F, 0.05F);
// https://t.me/pasterplace

   public ChinaHat() {
      super("ChinaHat", "Китайская шляпа над головой", ModuleCategory.RENDER);
      this.glowRadius.visibleWhen(this.glowEnabled::getValue);
      this.glowStrength.visibleWhen(this.glowEnabled::getValue);
      this.addSettings(
         this.hatStyle, this.renderForFriends, this.renderInFirstPerson, this.baseRadius, this.coneHeight, this.glowEnabled, this.glowRadius, this.glowStrength
      );
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onRender3D(EventRender3D event) {
      if (CLIENT.world != null && CLIENT.player != null) {
         for (Object entity : CLIENT.world.getEntities()) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)entity;
               if (!player.isInvisible()
                  && (
                     player != CLIENT.player
                        ? this.renderForFriends.getValue() && FriendManager.getInstance().isFriend((Entity)player)
                        : !CLIENT.options.getPerspective().isFirstPerson() || this.renderInFirstPerson.getValue()
                  )) {
                  this.renderHat((PlayerEntity)player);
                  if (this.glowEnabled.getValue()) {
                     DaamkyWorldRender.beginTexturedQuadBuffer(this.glowRadius.getValue(), this.glowStrength.getValue());
                     this.renderHat((PlayerEntity)player);
                     DaamkyWorldRender.flushTexturedQuadBuffer();
                  }
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   public void renderForPlayer(PlayerEntity player) {
      this.renderHat(player);
      if (this.glowEnabled.getValue()) {
         DaamkyWorldRender.beginTexturedQuadBuffer(this.glowRadius.getValue(), this.glowStrength.getValue());
         this.renderHat(player);
         DaamkyWorldRender.flushTexturedQuadBuffer();
      }
   }
// https://t.me/pasterplace

   private void renderHat(PlayerEntity player) {
      float tickDelta = DaamkyWorldRender.getTickDelta();
      Vec3d playerPosition = DaamkyWorldRender.getInterpolatedPosition(player);
      float headYawRadians = MathHelper.lerpAngleDegrees(tickDelta, player.lastHeadYaw, player.headYaw) * (float) (Math.PI / 180.0);
      float pitchRadians = player.getLerpedPitch(tickDelta) * (float) (Math.PI / 180.0);
      double playerHeightOffset = player.getHeight() + (player.isInSneakingPose() ? -0.05 : 0.08);
      Vec3d hatCenter = playerPosition.add(0.0, playerHeightOffset, 0.0);
      double headPitchOffsetX = -Math.sin(headYawRadians) * Math.sin(pitchRadians) * 0.12;
      double headPitchOffsetZ = Math.cos(headYawRadians) * Math.sin(pitchRadians) * 0.12;
      Vec3d coneTipPosition = hatCenter.add(-headPitchOffsetX, this.coneHeight.getValue().floatValue(), -headPitchOffsetZ);
      double hatRadius = this.baseRadius.getValue().floatValue();
      float animationOffset = AnimatedColorUtils.animationTime() * 0.6F;
// https://t.me/pasterplace

      for (int segmentIndex = 0; segmentIndex < 48; segmentIndex++) {
         double segmentStartAngle = segmentIndex * Math.PI * 2.0 / 48.0;
         double segmentEndAngle = (segmentIndex + 1) * Math.PI * 2.0 / 48.0;
         Vec3d segmentStartPosition = hatCenter.add(Math.cos(segmentStartAngle) * hatRadius, this.getConeSurfaceHeight(segmentStartAngle, headYawRadians, pitchRadians), Math.sin(segmentStartAngle) * hatRadius);
         Vec3d segmentEndPosition = hatCenter.add(Math.cos(segmentEndAngle) * hatRadius, this.getConeSurfaceHeight(segmentEndAngle, headYawRadians, pitchRadians), Math.sin(segmentEndAngle) * hatRadius);
         RgbaColor segmentStartColor = this.getSegmentColor(segmentIndex, 48, animationOffset);
         RgbaColor segmentEndColor = this.getSegmentColor(segmentIndex + 1, 48, animationOffset);
         RgbaColor faceFillColor = this.hatStyle.isSelected("Сплошной")
            ? segmentStartColor.withAlpha(150)
            : RgbaColor.WHITE.mix(segmentStartColor, 0.5F).withAlpha(this.hatStyle.isSelected("Стекло") ? 60 : 170);
         int edgeAlpha = this.hatStyle.isSelected("Стекло") ? 70 : 150;
         DaamkyWorldRender.drawTriangle(coneTipPosition, segmentStartPosition, segmentEndPosition, faceFillColor, segmentStartColor.withAlpha(edgeAlpha), segmentEndColor.withAlpha(edgeAlpha), false);
         DaamkyWorldRender.drawGradientLine(segmentStartPosition, segmentEndPosition, segmentStartColor.withAlpha(240), segmentEndColor.withAlpha(240), 2.0F, false);
      }
   }
// https://t.me/pasterplace

   private double getConeSurfaceHeight(double angleRadians, float headYawRadians, float pitchRadians) {
      double yawProjection = -Math.sin(headYawRadians);
      double forwardDirectionCosine = Math.cos(headYawRadians);
      double surfaceHeightOffset = Math.cos(angleRadians) * yawProjection + Math.sin(angleRadians) * forwardDirectionCosine;
      return -surfaceHeightOffset * Math.sin(pitchRadians) * this.baseRadius.getValue().floatValue() * 0.35;
   }
// https://t.me/pasterplace

   private RgbaColor getSegmentColor(int segmentIndex, int segmentCount, float animationOffset) {
      float normalizedSegmentPosition = (float)segmentIndex / segmentCount;
      return AnimatedColorUtils.getAnimatedAccentColor((float)(normalizedSegmentPosition * Math.PI * 2.0) + animationOffset, 255);
   }
}

