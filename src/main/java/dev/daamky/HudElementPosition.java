package dev.daamky;

import java.util.List;
import net.minecraft.client.MinecraftClient;

public abstract class HudElementPosition {
   protected static final MinecraftClient CLIENT = MinecraftClient.getInstance();
   private final String displayName;
   private final FloatSetting positionXSetting;
   private final FloatSetting positionYSetting;
   protected float elementWidth;
   protected float elementHeight;
   private float visibilityAlpha;
   private float hoverAlpha;

   protected HudElementPosition(String displayName, String settingsKey, float initialX, float initialY) {
      this.displayName = displayName;
      this.positionXSetting = new FloatSetting("hud_" + settingsKey + "_x", initialX, 0.0F, 4000.0F, 0.5F);
      this.positionYSetting = new FloatSetting("hud_" + settingsKey + "_y", initialY, 0.0F, 4000.0F, 0.5F);
      this.positionXSetting.visibleWhen(() -> false);
      this.positionYSetting.visibleWhen(() -> false);
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public List<FloatSetting> getPositionSettings() {
      return List.of(this.positionXSetting, this.positionYSetting);
   }

   public float getX() {
      return Math.clamp(this.positionXSetting.getValue(), 0.0F, Math.max(0.0F, GuiEventRenderer.getScreenWidth() - this.elementWidth));
   }

   public float getY() {
      return Math.clamp(this.positionYSetting.getValue(), 0.0F, Math.max(0.0F, GuiEventRenderer.getScreenHeight() - this.elementHeight));
   }

   public void setPosition(float x, float y) {
      this.positionXSetting.setValue(Float.valueOf(Math.round(x * 2.0F) / 2.0F));
      this.positionYSetting.setValue(Float.valueOf(Math.round(y * 2.0F) / 2.0F));
   }

   public boolean isHiddenByPosition() {
      return this.positionXSetting.getValue() >= 9000.0F || this.positionYSetting.getValue() >= 9000.0F;
   }

   public void clampPositionToScreen(float screenWidth, float screenHeight) {
      this.setPosition(
         Math.min(this.positionXSetting.getValue(), screenWidth - this.elementWidth - 7.0F), Math.min(this.positionYSetting.getValue(), screenHeight - this.elementHeight - 7.0F)
      );
   }

   public float getWidth() {
      return this.elementWidth;
   }

   public float getHeight() {
      return this.elementHeight;
   }

   public float getVisibilityAlpha() {
      return this.visibilityAlpha;
   }

   public float getHoverAlpha() {
      return this.hoverAlpha;
   }

   public boolean isDraggable() {
      return true;
   }

   public boolean handlePrimaryClick(float mouseX, float mouseY) {
      return false;
   }

   public boolean handleSecondaryClick(float mouseX, float mouseY) {
      return false;
   }

   public boolean containsPoint(float pointX, float pointY) {
      float elementX = this.getX();
      float elementY = this.getY();
      return pointX >= elementX && pointX <= elementX + this.elementWidth && pointY >= elementY && pointY <= elementY + this.elementHeight;
   }

   public boolean isRightHalfOfScreen() {
      return this.getX() + this.elementWidth * 0.5F > GuiEventRenderer.getScreenWidth() * 0.5F;
   }

   public void updateState(float tickDelta, boolean enabled, boolean pointerInside) {
      float targetVisibilityAlpha = enabled ? 1.0F : 0.0F;
      this.visibilityAlpha = interpolate(this.visibilityAlpha, targetVisibilityAlpha, 8.0F, tickDelta);
      if (Math.abs(targetVisibilityAlpha - this.visibilityAlpha) < 0.002F) {
         this.visibilityAlpha = targetVisibilityAlpha;
      }

      this.hoverAlpha = interpolate(this.hoverAlpha, pointerInside ? 1.0F : 0.0F, 10.0F, tickDelta);
      this.updateAnimation(tickDelta);
   }

   public void updateAnimation(float tickDelta) {
   }

   public abstract boolean isVisibleInCurrentScreen(boolean chatScreenOpen);

   public abstract void render(HudRenderContext renderContext, float visibilityAlpha);

   protected static float interpolate(float current, float target, float speed, float tickDelta) {
      return current + (target - current) * (1.0F - (float)Math.exp(-speed * tickDelta));
   }
}

