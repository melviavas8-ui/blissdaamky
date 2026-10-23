package dev.daamky;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class Totems extends HudElementPosition {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float HUD_PANEL_HEIGHT = 16.0F;
   private ItemStack totemIcon;
   private int totemCount;
   private boolean totemInOffhand;
   private float animatedPanelWidth = 40.0F;
// https://t.me/pasterplace

   public Totems() {
      super("Totems", "totems", 9999.0F, 9999.0F);
      this.elementWidth = this.animatedPanelWidth;
      this.elementHeight = 16.0F;
   }
// https://t.me/pasterplace

   @Override
   public void clampPositionToScreen(float screenWidth, float screenHeight) {
      this.setPosition(screenWidth * 0.5F + 60.0F, screenHeight - 78.0F);
   }
// https://t.me/pasterplace

   @Override
   public void updateAnimation(float frameDelta) {
      this.totemCount = 0;
      this.totemInOffhand = false;
      if (CLIENT.player != null) {
         for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
            ItemStack inventoryStack = CLIENT.player.getInventory().getStack(inventorySlot);
            if (inventoryStack.isOf(Items.TOTEM_OF_UNDYING)) {
               this.totemCount = this.totemCount + inventoryStack.getCount();
            }
         }

         ItemStack offhandStack = CLIENT.player.getEquippedStack(EquipmentSlot.OFFHAND);
         if (offhandStack.isOf(Items.TOTEM_OF_UNDYING)) {
            this.totemCount = this.totemCount + offhandStack.getCount();
            this.totemInOffhand = true;
         }
      }

      float targetWidth = 18.0F + BrokkeIcons.measureTextWidth(this.getDisplayCountText(), 5.7F) + 6.0F;
      this.animatedPanelWidth = interpolate(this.animatedPanelWidth, targetWidth, 10.0F, frameDelta);
      this.elementWidth = this.animatedPanelWidth;
   }
// https://t.me/pasterplace

   private String getDisplayCountText() {
      return this.totemCount + (this.totemInOffhand ? " held" : "");
   }
// https://t.me/pasterplace

   @Override
   public boolean isVisibleInCurrentScreen(boolean editMode) {
      return editMode || this.totemCount > 0;
   }
// https://t.me/pasterplace

   @Override
   public void render(HudRenderContext recordExtension3, float alpha) {
      DrawContext drawContext = recordExtension3.drawContext();
      float originX = this.getX();
      float originY = this.getY();
      BrokkeIcons.drawPanelBackground(drawContext, originX, originY, this.elementWidth, 16.0F, alpha);
      float iconX = originX + 5.0F;
      float iconY = originY + 8.0F - 4.5F;
      if (alpha > 0.4F) {
         recordExtension3.postRenderCallbacks().add(() -> BrokkeIcons.drawItemStack(drawContext, this.getTotemIcon(), iconX, iconY, 9.0F));
      }
// https://t.me/pasterplace

      RgbaColor textColor = this.totemCount == 0 ? RgbaColor.rgba(255, 120, 120, (int)(255.0F * alpha)) : BrokkeIcons.lightGrayWithAlpha(alpha);
      BrokkeIcons.drawCenteredText(drawContext, this.getDisplayCountText(), iconX + 13.0F, originY + 8.0F, 5.7F, textColor);
   }
// https://t.me/pasterplace

   private ItemStack getTotemIcon() {
      if (this.totemIcon == null) {
         this.totemIcon = new ItemStack(Items.TOTEM_OF_UNDYING);
      }

      return this.totemIcon;
   }
}

