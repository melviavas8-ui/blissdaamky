package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventInput;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.core.event.impl.EventWorldChange;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class FreeCamera extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting speedSetting = new FloatSetting("Скорость", 1.0F, 0.1F, 5.0F, 0.05F);
   private final BooleanSetting freezePlayerSetting = new BooleanSetting("Стоять на месте", true);
   private Vec3d position;
   private Vec3d previousPosition;
   private float yaw;
   private float pitch;
// https://t.me/pasterplace

   public FreeCamera() {
      super("FreeCamera", "Свободная камера сквозь блоки, игрок остаётся на месте", ModuleCategory.PLAYER);
      this.addSettings(this.speedSetting, this.freezePlayerSetting);
   }
// https://t.me/pasterplace

   public static FreeCamera getEnabledInstance() {
      FreeCamera module = (FreeCamera)ModuleRegistry.getInstance().getModuleByClass(FreeCamera.class);
      return module != null && module.isEnabled() && module.position != null ? module : null;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      if (CLIENT.player != null) {
         this.position = CLIENT.player.getEyePos();
         this.previousPosition = this.position;
         this.yaw = CLIENT.player.getYaw();
         this.pitch = CLIENT.player.getPitch();
      } else {
         this.setEnabled(false);
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.position = null;
      this.previousPosition = null;
   }
// https://t.me/pasterplace

   public float getYaw() {
      return this.yaw;
   }
// https://t.me/pasterplace

   public float getPitch() {
      return this.pitch;
   }
// https://t.me/pasterplace

   public Vec3d getCameraPosition(float tickDelta) {
      return this.previousPosition.lerp(this.position, tickDelta);
   }
// https://t.me/pasterplace

   public void changeLookDirection(double deltaYaw, double deltaPitch) {
      this.yaw += (float)deltaYaw * 0.15F;
      this.pitch = MathHelper.clamp(this.pitch + (float)deltaPitch * 0.15F, -90.0F, 90.0F);
   }
// https://t.me/pasterplace

   @EventTarget(priority = 4)
   public void onInput(EventInput eventInput) {
      if (this.position != null && this.freezePlayerSetting.getValue()) {
         eventInput.setForward(0.0F);
         eventInput.setStrafe(0.0F);
         eventInput.setJump(false);
         eventInput.setSneak(false);
         eventInput.setSprint(false);
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld() && this.position != null) {
         this.previousPosition = this.position;
         if (CLIENT.currentScreen == null) {
            float forwardInput = (!CLIENT.options.forwardKey.isPressed() ? 0.0F : 1.0F)
               - (!CLIENT.options.backKey.isPressed() ? 0.0F : 1.0F);
            float strafeInput = (!CLIENT.options.leftKey.isPressed() ? 0.0F : 1.0F)
               - (!CLIENT.options.rightKey.isPressed() ? 0.0F : 1.0F);
            float verticalInput = (!CLIENT.options.jumpKey.isPressed() ? 0.0F : 1.0F)
               - (!CLIENT.options.sneakKey.isPressed() ? 0.0F : 1.0F);
            double movementSpeed = this.speedSetting.getValue().floatValue() * (!CLIENT.options.sprintKey.isPressed() ? 1.0 : 2.0);
            float yawRadians;
            double sinYaw = MathHelper.sin(yawRadians = this.yaw * (float) (Math.PI / 180.0));
            double cosYaw = MathHelper.cos(yawRadians);
            Vec3d movementDirection = new Vec3d(strafeInput * cosYaw - forwardInput * sinYaw, verticalInput, forwardInput * cosYaw + strafeInput * sinYaw);
            if (movementDirection.lengthSquared() > 1.0E-4) {
               this.position = this.position.add(movementDirection.normalize().multiply(movementSpeed));
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange eventWorldChange) {
      this.setEnabled(false);
   }
}

