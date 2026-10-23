package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class WingsAnimationController {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final long ANIMATION_OPEN_DURATION_MILLIS = 380L;
    private static final long ANIMATION_HOLD_DURATION_MILLIS = 2600L;
    private static final long ANIMATION_CLOSE_DURATION_MILLIS = 380L;
    private static long animationStartTimeMillis = -1L;
    private static boolean firstPersonAnimationActive;
// https://t.me/pasterplace

    private WingsAnimationController() {
    }
// https://t.me/pasterplace

    public static void tickAnimationInput() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity clientPlayerEntity = client.player;
        if (clientPlayerEntity != null && client.currentScreen == null) {
            if (!canAnimateWings(clientPlayerEntity)) {
                if (animationStartTimeMillis >= 0L && getAnimationProgress() > 0.0F) {
                    animationStartTimeMillis = -1L;
                }
            } else {
                boolean swapKeyPressed = false;
// https://t.me/pasterplace

                while (client.options.swapHandsKey.wasPressed()) {
                    swapKeyPressed = true;
                }

                if (swapKeyPressed) {
                    long currentTimeMillis = System.currentTimeMillis();
                    long elapsedMillis = animationStartTimeMillis < 0L ? Long.MAX_VALUE : currentTimeMillis - animationStartTimeMillis;
                    if (elapsedMillis >= 380L && elapsedMillis < 2980L) {
                        animationStartTimeMillis = currentTimeMillis - 380L - 2600L;
                    } else if (elapsedMillis >= 2980L) {
                        animationStartTimeMillis = currentTimeMillis;
                    }
                }
            }
        }
    }
// https://t.me/pasterplace

    public static boolean canAnimateWings(ClientPlayerEntity clientPlayerEntity) {
        return Cape.getInstance().getEquippedModel(Wings.WATCHES) != null
        && clientPlayerEntity.getMainHandStack().isEmpty()
        && clientPlayerEntity.getOffHandStack().isEmpty()
        && !clientPlayerEntity.isInvisible();
    }
// https://t.me/pasterplace

    public static float getAnimationProgress() {
        if (animationStartTimeMillis < 0L) {
            return 0.0F;
        }

        long elapsedMillis = System.currentTimeMillis() - animationStartTimeMillis;
        float animationProgress;
        if (elapsedMillis < 380L) {
            animationProgress = (float)elapsedMillis / 380.0F;
        } else if (elapsedMillis < 2980L) {
            animationProgress = 1.0F;
        } else {
            if (elapsedMillis >= 3360L) {
                animationStartTimeMillis = -1L;
                return 0.0F;
            }

            animationProgress = 1.0F - (float)(elapsedMillis - 380L - 2600L) / 380.0F;
        }

        return animationProgress * animationProgress * (3.0F - 2.0F * animationProgress);
    }
// https://t.me/pasterplace

    public static boolean isFirstPersonAnimationActive() {
        return firstPersonAnimationActive;
    }
// https://t.me/pasterplace

    public static void setFirstPersonAnimationActive(boolean firstPersonAnimationActive) {
        firstPersonAnimationActive = firstPersonAnimationActive;
    }
}

