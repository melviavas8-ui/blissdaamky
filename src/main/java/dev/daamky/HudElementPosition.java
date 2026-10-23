package dev.daamky;

import java.util.List;
import net.minecraft.client.MinecraftClient;

public abstract class HudElementPosition {
    private static final String sourceAttributionUrl = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   protected static final MinecraftClient CLIENT = MinecraftClient.getInstance();
   private final String displayName;
   private final FloatSetting positionXSetting;
   private final FloatSetting positionYSetting;
   protected float elementWidth;
   protected float elementHeight;
   private float visibilityAlpha;
   private float hoverAlpha;
// https://t.me/pasterplace

   protected HudElementPosition(String displayName, String settingsKey, float initialX, float initialY) {
      this.displayName = displayName;
      this.positionXSetting = new FloatSetting("hud_" + settingsKey + "_x", initialX, 0.0F, 4000.0F, 0.5F);
      this.positionYSetting = new FloatSetting("hud_" + settingsKey + "_y", initialY, 0.0F, 4000.0F, 0.5F);
      this.positionXSetting.visibleWhen(() -> false);
      this.positionYSetting.visibleWhen(() -> false);
   }
// https://t.me/pasterplace

   public String getDisplayName() {
      return this.displayName;
   }
// https://t.me/pasterplace

   public List<FloatSetting> getPositionSettings() {
      return List.of(this.positionXSetting, this.positionYSetting);
   }
// https://t.me/pasterplace

   public float getX() {
      return Math.clamp(this.positionXSetting.getValue(), 0.0F, Math.max(0.0F, GuiEventRenderer.getScreenWidth() - this.elementWidth));
   }
// https://t.me/pasterplace

   public float getY() {
      return Math.clamp(this.positionYSetting.getValue(), 0.0F, Math.max(0.0F, GuiEventRenderer.getScreenHeight() - this.elementHeight));
   }
// https://t.me/pasterplace

   public void setPosition(float x, float y) {
      this.positionXSetting.setValue(Float.valueOf(Math.round(x * 2.0F) / 2.0F));
      this.positionYSetting.setValue(Float.valueOf(Math.round(y * 2.0F) / 2.0F));
   }
// https://t.me/pasterplace

   public boolean isHiddenByPosition() {
      return this.positionXSetting.getValue() >= 9000.0F || this.positionYSetting.getValue() >= 9000.0F;
   }
// https://t.me/pasterplace

   public void clampPositionToScreen(float screenWidth, float screenHeight) {
      this.setPosition(
         Math.min(this.positionXSetting.getValue(), screenWidth - this.elementWidth - 7.0F), Math.min(this.positionYSetting.getValue(), screenHeight - this.elementHeight - 7.0F)
      );
   }
// https://t.me/pasterplace

   public float getWidth() {
      return this.elementWidth;
   }
// https://t.me/pasterplace

   public float getHeight() {
      return this.elementHeight;
   }
// https://t.me/pasterplace

   public float getVisibilityAlpha() {
      return this.visibilityAlpha;
   }
// https://t.me/pasterplace

   public float getHoverAlpha() {
      return this.hoverAlpha;
   }
// https://t.me/pasterplace

   public boolean isDraggable() {
      return true;
   }
// https://t.me/pasterplace

   public boolean handlePrimaryClick(float mouseX, float mouseY) {
      return false;
   }
// https://t.me/pasterplace

   public boolean handleSecondaryClick(float mouseX, float mouseY) {
      return false;
   }
// https://t.me/pasterplace

   public boolean containsPoint(float pointX, float pointY) {
      float elementX = this.getX();
      float elementY = this.getY();
      return pointX >= elementX && pointX <= elementX + this.elementWidth && pointY >= elementY && pointY <= elementY + this.elementHeight;
   }
// https://t.me/pasterplace

   public boolean isRightHalfOfScreen() {
      return this.getX() + this.elementWidth * 0.5F > GuiEventRenderer.getScreenWidth() * 0.5F;
   }
// https://t.me/pasterplace

   public void updateState(float tickDelta, boolean enabled, boolean pointerInside) {
      float targetVisibilityAlpha = enabled ? 1.0F : 0.0F;
      this.visibilityAlpha = interpolate(this.visibilityAlpha, targetVisibilityAlpha, 8.0F, tickDelta);
      if (Math.abs(targetVisibilityAlpha - this.visibilityAlpha) < 0.002F) {
         this.visibilityAlpha = targetVisibilityAlpha;
      }
// https://t.me/pasterplace

      this.hoverAlpha = interpolate(this.hoverAlpha, pointerInside ? 1.0F : 0.0F, 10.0F, tickDelta);
      this.updateAnimation(tickDelta);
   }
// https://t.me/pasterplace

   public void updateAnimation(float tickDelta) {
   }
// https://t.me/pasterplace

   public abstract boolean isVisibleInCurrentScreen(boolean chatScreenOpen);
// https://t.me/pasterplace

   public abstract void render(HudRenderContext renderContext, float visibilityAlpha);
// https://t.me/pasterplace

   protected static float interpolate(float current, float target, float speed, float tickDelta) {
      return current + (target - current) * (1.0F - (float)Math.exp(-speed * tickDelta));
   }
}

