package dev.daamky;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class DaamkyClickGuiScreen extends Screen {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public DaamkyClickGuiScreen() {
      super(Text.literal("Daamky"));
   }
// https://t.me/pasterplace

   public boolean shouldCloseOnEsc() {
      return false;
   }
// https://t.me/pasterplace

   public boolean shouldPause() {
      return false;
   }
// https://t.me/pasterplace

   public boolean keyPressed(KeyInput keyInput) {
      if (Search.getInstance().handleKeyInput(keyInput)) {
         return true;
      }

      if (keyInput.getKeycode() != 256 && keyInput.getKeycode() != 344) {
         return super.keyPressed(keyInput);
      }
// https://t.me/pasterplace

      Search.getInstance().close();
      return true;
   }
// https://t.me/pasterplace

   @Override
   public boolean charTyped(CharInput charInput) {
      return Search.getInstance().handleCharInput(charInput) ? true : super.charTyped(charInput);
   }
// https://t.me/pasterplace

   @Override
   public boolean mouseClicked(Click click, boolean doubleClick) {
      return Search.getInstance().handleMouseClick(click) ? true : super.mouseClicked(click, doubleClick);
   }
// https://t.me/pasterplace

   public boolean mouseReleased(Click click) {
      Search.getInstance().handleMouseRelease(click);
      return super.mouseReleased(click);
   }
// https://t.me/pasterplace

   public boolean mouseDragged(Click click, double deltaX, double deltaY) {
      Search.getInstance().updateMousePosition(click.x(), click.y());
      return true;
   }
// https://t.me/pasterplace

   @Override
   public void mouseMoved(double mouseX, double mouseY) {
      Search.getInstance().updateMousePosition(mouseX, mouseY);
      super.mouseMoved(mouseX, mouseY);
   }
// https://t.me/pasterplace

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return Search.getInstance().handleMouseScroll(mouseX, mouseY, horizontalAmount, verticalAmount) ? true : super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }
// https://t.me/pasterplace

   public void close() {
      Search.getInstance().close();
   }
// https://t.me/pasterplace

   public void removed() {
      if (Search.getInstance().isOpen()) {
         Search.getInstance().close();
      }

      super.removed();
   }
// https://t.me/pasterplace

   public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
   }
// https://t.me/pasterplace

   public void renderInGameBackground(DrawContext drawContext) {
   }
}

