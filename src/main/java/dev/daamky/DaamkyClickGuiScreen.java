package dev.daamky;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class DaamkyClickGuiScreen extends Screen {
   public DaamkyClickGuiScreen() {
      super(Text.literal("Daamky"));
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   public boolean shouldPause() {
      return false;
   }

   public boolean keyPressed(KeyInput keyInput) {
      if (Search.getInstance().handleKeyInput(keyInput)) {
         return true;
      }

      if (keyInput.getKeycode() != 256 && keyInput.getKeycode() != 344) {
         return super.keyPressed(keyInput);
      }

      Search.getInstance().close();
      return true;
   }

   @Override
   public boolean charTyped(CharInput charInput) {
      return Search.getInstance().handleCharInput(charInput) ? true : super.charTyped(charInput);
   }

   @Override
   public boolean mouseClicked(Click click, boolean doubleClick) {
      return Search.getInstance().handleMouseClick(click) ? true : super.mouseClicked(click, doubleClick);
   }

   public boolean mouseReleased(Click click) {
      Search.getInstance().handleMouseRelease(click);
      return super.mouseReleased(click);
   }

   public boolean mouseDragged(Click click, double deltaX, double deltaY) {
      Search.getInstance().updateMousePosition(click.x(), click.y());
      return true;
   }

   @Override
   public void mouseMoved(double mouseX, double mouseY) {
      Search.getInstance().updateMousePosition(mouseX, mouseY);
      super.mouseMoved(mouseX, mouseY);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return Search.getInstance().handleMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount) ? true : super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   public void close() {
      Search.getInstance().close();
   }

   public void removed() {
      if (Search.getInstance().isOpen()) {
         Search.getInstance().close();
      }

      super.removed();
   }

   public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
   }

   public void renderInGameBackground(DrawContext drawContext) {
   }
}

