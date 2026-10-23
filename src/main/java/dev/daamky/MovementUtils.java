package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class MovementUtils {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private MovementUtils() {
   }
// https://t.me/pasterplace

   public static boolean isMoving() {
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      if (clientPlayerEntity == null) {
         return false;
      }

      PlayerInput playerInput = clientPlayerEntity.input.playerInput;
      return playerInput.forward() != playerInput.backward() || playerInput.left() != playerInput.right();
   }
// https://t.me/pasterplace

   public static float getMovementDirectionRadians() {
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      if (clientPlayerEntity == null) {
         return 0.0F;
      }

      PlayerInput playerInput = clientPlayerEntity.input.playerInput;
      float forwardInput = playerInput.forward() == playerInput.backward() ? 0.0F : (playerInput.forward() ? 1.0F : -1.0F);
      float strafeInput = playerInput.left() == playerInput.right() ? 0.0F : (playerInput.left() ? 1.0F : -1.0F);
      float yawDegrees = clientPlayerEntity.getYaw();
      if (forwardInput < 0.0F) {
         yawDegrees += 180.0F;
      }

      float directionMultiplier = forwardInput < 0.0F ? -0.5F : (forwardInput > 0.0F ? 0.5F : 1.0F);
      if (strafeInput > 0.0F) {
         yawDegrees -= 90.0F * directionMultiplier;
      } else if (strafeInput < 0.0F) {
         yawDegrees += 90.0F * directionMultiplier;
      }

      return yawDegrees * (float) (Math.PI / 180.0);
   }
// https://t.me/pasterplace

   public static double getHorizontalSpeed() {
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      if (clientPlayerEntity == null) {
         return 0.0;
      }

      Vec3d velocity = clientPlayerEntity.getVelocity();
      return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
   }
// https://t.me/pasterplace

   public static void setHorizontalSpeed(double horizontalSpeed) {
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      if (clientPlayerEntity != null) {
         Vec3d velocity = clientPlayerEntity.getVelocity();
         if (!isMoving()) {
            clientPlayerEntity.setVelocity(0.0, velocity.y, 0.0);
         } else {
            float directionRadians = getMovementDirectionRadians();
            clientPlayerEntity.setVelocity(-MathHelper.sin(directionRadians) * horizontalSpeed, velocity.y, MathHelper.cos(directionRadians) * horizontalSpeed);
         }
      }
   }
// https://t.me/pasterplace

   public static double getBaseMovementSpeed() {
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      double baseSpeed = 0.2873;
      if (clientPlayerEntity != null && clientPlayerEntity.hasStatusEffect(StatusEffects.SPEED)) {
         int speedAmplifier = clientPlayerEntity.getStatusEffect(StatusEffects.SPEED).getAmplifier();
         baseSpeed = 0.2873 * (1.0 + 0.2 * (speedAmplifier + 1));
      }
// https://t.me/pasterplace

      return baseSpeed;
   }
}

