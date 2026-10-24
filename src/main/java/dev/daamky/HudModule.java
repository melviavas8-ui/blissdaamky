package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventHudLayer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;

public final class HudModule extends ModuleStructure {
   private final MultiToggleSetting enabledElements = new MultiToggleSetting("Elements")
      .addOption("Watermark", true)
      .addOption("Target HUD", true)
      .addOption("Potions", true)
      .addOption("Keybinds", true)
      .addOption("Cooldowns", true)
      .addOption("Armor", true)
      .addOption("Inventory", false)
      .addOption("Staff", true)
      .addOption("Totems", false)
      .addOption("Notifications", true)
      .addOption("Hotbar", true)
      .addOption("Scoreboard", true);
   private final MultiToggleSetting watermarkInformation = new MultiToggleSetting("Watermark Info")
      .addOption("User", true)
      .addOption("Ping", true)
      .addOption("FPS", true);
   private final MultiOptionSetting watermarkStyle = new MultiOptionSetting("Watermark Style", "Theme", "Theme", "Rainbow", "Adaptive");
   private final MultiOptionSetting watermarkPosition = new MultiOptionSetting("Watermark Position", "Left", "Left", "Center", "Right");
   private final List<HudElementPosition> hudElements = new ArrayList<>();
   private HudElementPosition draggedElement;
   private float dragOffsetX;
   private float dragOffsetY;
   private boolean primaryMouseButtonDown;
   private boolean secondaryMouseButtonDown;
   private float animationTime;

   public HudModule() {
      super("HUD", "Displays interface elements on screen", ModuleCategory.RENDER);
      this.hudElements.add(new Hotbar());
      this.hudElements.add(new Scoreboard());
      this.hudElements.add(new Watermark(this.watermarkInformation, this.watermarkStyle, this.watermarkPosition));
      this.hudElements.add(new Potions());
      this.hudElements.add(new Keybinds());
      this.hudElements.add(new Cooldowns());
      this.hudElements.add(new Staff());
      this.hudElements.add(new Armor());
      this.hudElements.add(new Totems());
      this.hudElements.add(new Inventory());
      this.hudElements.add(new TargetHUD());
      this.hudElements.add(new Notifications(this));
      this.addSetting(this.enabledElements);
      this.watermarkInformation.visibleWhen(() -> this.enabledElements.isOptionEnabled("Watermark"));
      this.watermarkStyle.visibleWhen(() -> this.enabledElements.isOptionEnabled("Watermark"));
      this.addSetting(this.watermarkInformation);
      this.watermarkPosition.visibleWhen(() -> this.enabledElements.isOptionEnabled("Watermark"));
      this.addSetting(this.watermarkStyle);
      this.addSetting(this.watermarkPosition);

      for (Object hudElement : this.hudElements) {
         for (Object positionSetting : ((HudElementPosition)hudElement).getPositionSettings()) {
            this.addSetting((Setting<?>)positionSetting);
         }
      }

      this.setEnabled(true);
   }

   public static boolean isElementEnabled(String elementDisplayName) {
      HudModule module = (HudModule)ModuleRegistry.getInstance().getModuleByClass(HudModule.class);
      return module != null && module.isEnabled() && module.enabledElements.isOptionEnabled(elementDisplayName);
   }

   public static boolean isPointOverEditableElement(float pointX, float pointY) {
      HudModule module = (HudModule)ModuleRegistry.getInstance().getModuleByClass(HudModule.class);
      if (module == null || !module.isEnabled()) {
         return false;
      }

      if (module.draggedElement != null) {
         return true;
      }

      for (HudElementPosition hudElement : module.hudElements) {
         if (hudElement.isDraggable() && hudElement.getVisibilityAlpha() >= 0.5F && hudElement.containsPoint(pointX, pointY)) {
            return true;
         }
      }

      return false;
   }

   private static float getMouseX(MinecraftClient client) {
      Window window = client.getWindow();
      return (float)(client.mouse.getX() * window.getFramebufferWidth() / Math.max(1, window.getWidth()) / 2.0);
   }

   private static float getMouseY(MinecraftClient client) {
      Window window = client.getWindow();
      return (float)(client.mouse.getY() * window.getFramebufferHeight() / Math.max(1, window.getHeight()) / 2.0);
   }

   private static boolean isChatScreenOpen(MinecraftClient client) {
      return client.currentScreen instanceof ChatScreen;
   }

   @Override
   public void updateAnimation(float frameDelta) {
      super.updateAnimation(frameDelta);
      this.animationTime += frameDelta;
      MinecraftClient client = MinecraftClient.getInstance();
      boolean worldAvailable = client != null && client.player != null && client.world != null;
      boolean chatScreenOpen = worldAvailable && isChatScreenOpen(client);
      float mouseX = worldAvailable ? getMouseX(client) : -1.0F;
      float mouseY = worldAvailable ? getMouseY(client) : -1.0F;

      for (Object hudElement : this.hudElements) {
         boolean enabledBySetting = this.enabledElements.isOptionEnabled(((HudElementPosition)hudElement).getDisplayName());
         boolean shouldRender = this.isEnabled() && worldAvailable && enabledBySetting && ((HudElementPosition)hudElement).isVisibleInCurrentScreen(chatScreenOpen);
         boolean isHoveredOrDragged = chatScreenOpen
            && ((HudElementPosition)hudElement).isDraggable()
            && (hudElement == this.draggedElement || this.draggedElement == null && ((HudElementPosition)hudElement).containsPoint(mouseX, mouseY));
         ((HudElementPosition)hudElement).updateState(frameDelta, shouldRender, isHoveredOrDragged);
      }
   }

   private void handleEditorInput(MinecraftClient client) {
      boolean chatScreenOpen = isChatScreenOpen(client);
      boolean primaryButtonDown = chatScreenOpen && MouseButtonState.isPrimaryButtonDown();
      float mouseX = getMouseX(client);
      float mouseY = getMouseY(client);
      if (primaryButtonDown && !this.primaryMouseButtonDown) {
         this.draggedElement = null;
         boolean primaryClickHandled = false;

         for (Object hudElement : this.hudElements) {
            if (((HudElementPosition)hudElement).getVisibilityAlpha() >= 0.5F && ((HudElementPosition)hudElement).handlePrimaryClick(mouseX, mouseY)) {
               primaryClickHandled = true;
            }
         }

         for (int elementIndex = this.hudElements.size() - 1; elementIndex >= 0 && !primaryClickHandled; elementIndex--) {
            Object hudElement = this.hudElements.get(elementIndex);
            if (((HudElementPosition)hudElement).isDraggable()
               && !(((HudElementPosition)hudElement).getVisibilityAlpha() < 0.5F)
               && ((HudElementPosition)hudElement).containsPoint(mouseX, mouseY)) {
               this.draggedElement = (HudElementPosition)hudElement;
               this.dragOffsetX = mouseX - ((HudElementPosition)hudElement).getX();
               this.dragOffsetY = mouseY - ((HudElementPosition)hudElement).getY();
               break;
            }
         }
      }

      if (!primaryButtonDown) {
         this.draggedElement = null;
      }

      if (this.draggedElement != null) {
         float editorWidth = GuiEventRenderer.getScreenWidth();
         float editorHeight = GuiEventRenderer.getScreenHeight();
         float draggedX = Math.clamp(mouseX - this.dragOffsetX, 0.0F, Math.max(0.0F, editorWidth - this.draggedElement.getWidth()));
         float draggedY = Math.clamp(mouseY - this.dragOffsetY, 0.0F, Math.max(0.0F, editorHeight - this.draggedElement.getHeight()));
         this.draggedElement.setPosition(draggedX, draggedY);
      }

      this.primaryMouseButtonDown = primaryButtonDown;
      boolean secondaryButtonDown = chatScreenOpen && MouseButtonState.isSecondaryButtonDown();
      if (secondaryButtonDown && !this.secondaryMouseButtonDown) {
         for (Object hudElement : this.hudElements) {
            if (((HudElementPosition)hudElement).getVisibilityAlpha() >= 0.5F && ((HudElementPosition)hudElement).handleSecondaryClick(mouseX, mouseY)) {
               break;
            }
         }
      }

      this.secondaryMouseButtonDown = secondaryButtonDown;
   }

   @EventTarget(priority = 4)
   public void onHudLayerRender(EventHudLayer eventHudLayer) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client != null && client.options != null && client.player != null && client.world != null) {
         this.renderHudElements(client, eventHudLayer.graphics());
      }
   }

   private void renderHudElements(MinecraftClient client, DrawContext drawContext) {
      float hudWidth = GuiEventRenderer.getScreenWidth();
      float hudHeight = GuiEventRenderer.getScreenHeight();
      this.handleEditorInput(client);
      GuiEventRenderer.flushAndStartRootLayer(drawContext);
      boolean chatScreenOpen = isChatScreenOpen(client);
      ArrayList<Runnable> deferredRenderCallbacks = new ArrayList<>();
      HudRenderContext renderContext = new HudRenderContext(drawContext, client, chatScreenOpen, deferredRenderCallbacks, this.animationTime);

      for (Object hudElement : this.hudElements) {
         if (((HudElementPosition)hudElement).isHiddenByPosition() && ((HudElementPosition)hudElement).getWidth() > 0.0F) {
            ((HudElementPosition)hudElement).clampPositionToScreen(hudWidth, hudHeight);
         }

         float visibilityAlpha = ((HudElementPosition)hudElement).getVisibilityAlpha();
         if (!(visibilityAlpha <= 0.005F)) {
            ((HudElementPosition)hudElement).render(renderContext, visibilityAlpha);
         }
      }

      if (!deferredRenderCallbacks.isEmpty()) {
         GuiEventRenderer.flushAndStartRootLayer(drawContext);

         for (Runnable renderCallback : deferredRenderCallbacks) {
            renderCallback.run();
         }
      }
   }
}

