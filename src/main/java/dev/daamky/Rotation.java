package dev.daamky;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record Rotation(float yaw, float pitch) {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
// https://t.me/pasterplace

   public Rotation(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }
// https://t.me/pasterplace

   public static Rotation of(float yaw, float pitch) {
      return new Rotation(yaw, MathHelper.clamp(pitch, -90.0F, 90.0F));
   }
// https://t.me/pasterplace

   public Rotation normalized() {
      return new Rotation(MathHelper.wrapDegrees(this.yaw), MathHelper.clamp(this.pitch, -90.0F, 90.0F));
   }
// https://t.me/pasterplace

   public float yawDeltaTo(Rotation lIll1Il1lll111l11l1I1llllI2) {
      return MathHelper.wrapDegrees(lIll1Il1lll111l11l1I1llllI2.yaw - this.yaw);
   }
// https://t.me/pasterplace

   public float pitchDeltaTo(Rotation lIll1Il1lll111l11l1I1llllI2) {
      return lIll1Il1lll111l11l1I1llllI2.pitch - this.pitch;
   }
// https://t.me/pasterplace

   public float angularDistanceTo(Rotation lIll1Il1lll111l11l1I1llllI2) {
      float yawDelta = this.yawDeltaTo(lIll1Il1lll111l11l1I1llllI2);
      float pitchDelta = this.pitchDeltaTo(lIll1Il1lll111l11l1I1llllI2);
      return (float)Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
   }
// https://t.me/pasterplace

   public Vec3d directionVector() {
      float pitchRadians = this.pitch * (float) (Math.PI / 180.0);
      float negativeYawRadians = -this.yaw * (float) (Math.PI / 180.0);
      float cosYaw = (float)Math.cos(negativeYawRadians);
      float sinYaw = (float)Math.sin(negativeYawRadians);
      float cosPitch = (float)Math.cos(pitchRadians);
      float sinPitch = (float)Math.sin(pitchRadians);
      return new Vec3d(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
   }
// https://t.me/pasterplace

   public float getYaw() {
      return this.yaw;
   }
// https://t.me/pasterplace

   public float getPitch() {
      return this.pitch;
   }
}

