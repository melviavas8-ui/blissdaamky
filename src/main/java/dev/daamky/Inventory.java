package dev.daamky;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public final class Inventory extends HudElementPosition {
   private static final float SLOT_SIZE = 11.0F;
   private static final float SLOT_GAP = 1.5F;
   private static final float PANEL_INNER_PADDING = 5.0F;
   private static final float HEADER_HEIGHT = 15.0F;
   private final float[] slotAnimationProgress = new float[27];

   public Inventory() {
      super("Inventory", "inventory", 9999.0F, 9999.0F);
      this.elementWidth = 121.0F;
      this.elementHeight = 59.0F;
   }

   @Override
   public boolean isVisibleInCurrentScreen(boolean isEditorPreview) {
      return CLIENT.player != null;
   }

   @Override
   public void updateAnimation(float deltaTime) {
      if (CLIENT.player != null) {
         PlayerInventory playerInventory = CLIENT.player.getInventory();

         for (int inventorySlot = 0; inventorySlot < 27; inventorySlot++) {
            this.slotAnimationProgress[inventorySlot] = interpolate(
               this.slotAnimationProgress[inventorySlot], playerInventory.getStack(9 + inventorySlot).isEmpty() ? 0.0F : 1.0F, 12.0F, deltaTime
            );
         }
      }
   }

   @Override
   public void render(HudRenderContext renderContext, float opacity) {
      if (CLIENT.player != null) {
         DrawContext drawContext = renderContext.drawContext();
         float x = this.getX();
         float y = this.getY();
         BrokkeIcons.drawPanelBackground(drawContext, x, y, this.elementWidth, this.elementHeight, opacity);
         PlayerInventory playerInventory = CLIENT.player.getInventory();
         int occupiedSlotCount = 0;

         for (int inventorySlotIndex = 9; inventorySlotIndex < 36; inventorySlotIndex++) {
            if (!playerInventory.getStack(inventorySlotIndex).isEmpty()) {
               occupiedSlotCount++;
            }
         }

         float headerBaselineY = y + 7.5F;
         BrokkeIcons.drawCenteredText(drawContext, "E", x + 5.0F + 3.0F, headerBaselineY, 6.0F, opacity);
         BrokkeIcons.drawCenteredText(drawContext, "Inventory", x + 5.0F + 9.5F, headerBaselineY, 6.2F, BrokkeIcons.lightGrayWithAlpha(opacity));
         String occupiedSlotCountText = occupiedSlotCount + "/27";
         float countTextWidth = BrokkeIcons.measureTextWidth(occupiedSlotCountText, 4.8F);
         float countBadgeWidth = countTextWidth + 6.0F;
         float countBadgeX = x + this.elementWidth - 5.0F - countBadgeWidth;
         RoundedGradientRectBuilder.at(countBadgeX, headerBaselineY - 3.75F, countBadgeWidth, 7.5F)
            .cornerRadius(3.75F)
            .color(BrokkeIcons.whiteWithAlpha(opacity * 0.16F))
            .render(drawContext);
         BrokkeIcons.drawCenteredText(drawContext, occupiedSlotCountText, countBadgeX + 3.0F, headerBaselineY, 4.8F, BrokkeIcons.whiteWithAlpha(opacity));
         float firstSlotRowY = y + 15.0F + 3.0F;

         for (int slotIndex = 0; slotIndex < 27; slotIndex++) {
            int columnIndex = slotIndex % 9;
            int rowIndex = slotIndex / 9;
            float slotX = x + 5.0F + columnIndex * 12.5F;
            float slotY = firstSlotRowY + rowIndex * 12.5F;
            float slotAnimationProgress = this.slotAnimationProgress[slotIndex];
            RoundedGradientRectBuilder.at(slotX, slotY, 11.0F, 11.0F)
               .cornerRadius(2.5F)
               .color(BrokkeIcons.whiteWithAlpha(opacity * (0.06F + 0.12F * slotAnimationProgress)))
               .render(drawContext);
            ItemStack slotItem = playerInventory.getStack(9 + slotIndex);
            if (slotItem.isEmpty()) {
               BrokkeIcons.drawColorSwatch(drawContext, slotX + 5.5F, slotY + 5.5F, 1.2F, BrokkeIcons.whiteWithAlpha(opacity * 0.18F));
            } else {
               float renderedItemSize = 9.0F * (0.7F + 0.3F * slotAnimationProgress);
               float renderedItemX = slotX + (11.0F - renderedItemSize) * 0.5F;
               float renderedItemY = slotY + (11.0F - renderedItemSize) * 0.5F;
               ItemStack renderedItemCopy = slotItem.copy();
               if (opacity > 0.4F) {
                  renderContext.postRenderCallbacks().add(() -> BrokkeIcons.drawItemStack(drawContext, renderedItemCopy, renderedItemX, renderedItemY, renderedItemSize));
               }

               if (slotItem.getCount() > 1) {
                  String stackCountText = String.valueOf(slotItem.getCount());
                  float stackCountTextWidth = BrokkeIcons.measureTextWidth(stackCountText, 3.6F);
                  renderContext.postRenderCallbacks()
                     .add(
                        () -> BrokkeIcons.drawCenteredText(
                           drawContext, stackCountText, slotX + 11.0F - stackCountTextWidth - 0.6F, slotY + 11.0F - 2.2F, 3.6F, BrokkeIcons.lightGrayWithAlpha(y)
                        )
                     );
               }
            }
         }
      }
   }
}

