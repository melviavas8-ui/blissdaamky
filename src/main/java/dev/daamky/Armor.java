package dev.daamky;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public final class Armor extends HudElementPosition {
   private static final EquipmentSlot[] DISPLAYED_EQUIPMENT_SLOTS = new EquipmentSlot[]{
      EquipmentSlot.HEAD,
      EquipmentSlot.CHEST,
      EquipmentSlot.LEGS,
      EquipmentSlot.FEET,
      EquipmentSlot.MAINHAND,
      EquipmentSlot.OFFHAND
   };
   private static final float ITEM_ICON_SIZE = 11.0F;
   private static final float ITEM_SPACING = 3.0F;
   private static final float PANEL_INSET = 4.0F;
   private static final float DURABILITY_BAR_OFFSET = 1.5F;
   private static final float DURABILITY_BAR_HEIGHT = 1.2F;
   private final List<ItemStack> displayedItemStacks = new ArrayList<>();
   private float animatedElementWidth = 19.0F;

   public Armor() {
      super("Armor", "armor", 9999.0F, 9999.0F);
      this.elementWidth = this.animatedElementWidth;
      this.elementHeight = 21.7F;
   }

   @Override
   public void clampPositionToScreen(float screenWidth, float screenHeight) {
      this.setPosition(screenWidth * 0.5F - 55.0F, screenHeight - 78.0F);
   }

   @Override
   public void updateAnimation(float frameDelta) {
      this.displayedItemStacks.clear();
      if (CLIENT.player != null) {
         for (EquipmentSlot equipmentSlot : DISPLAYED_EQUIPMENT_SLOTS) {
            ItemStack equippedItem = CLIENT.player.getEquippedStack(equipmentSlot);
            if (!equippedItem.isEmpty()) {
               this.displayedItemStacks.add(equippedItem.copy());
            }
         }
      }

      float targetWidth = 8.0F + Math.max(1, this.displayedItemStacks.size()) * 14.0F - 3.0F;
      this.animatedElementWidth = interpolate(this.animatedElementWidth, targetWidth, 10.0F, frameDelta);
      this.elementWidth = this.animatedElementWidth;
   }

   @Override
   public boolean isVisibleInCurrentScreen(boolean editMode) {
      return editMode || !this.displayedItemStacks.isEmpty();
   }

   @Override
   public void render(HudRenderContext recordExtension3, float alpha) {
      DrawContext drawContext = recordExtension3.drawContext();
      float originX = this.getX();
      float originY = this.getY();
      BrokkeIcons.drawPanelBackground(drawContext, originX, originY, this.elementWidth, this.elementHeight, alpha);
      float itemStartX = originX + 4.0F;
      float itemEndX = originX + this.elementWidth - 4.0F + 0.5F;
      float itemY = originY + 4.0F;

      for (ItemStack displayedStack : this.displayedItemStacks) {
         if (itemStartX + 11.0F > itemEndX) {
            break;
         }

         float itemX = itemStartX;
         if (alpha > 0.4F) {
            recordExtension3.postRenderCallbacks().add(() -> BrokkeIcons.drawItemStack(drawContext, displayedStack, itemX, itemY, 11.0F));
         }

         if (displayedStack.isDamageable()) {
            float durabilityFraction = 1.0F - (float)displayedStack.getDamage() / displayedStack.getMaxDamage();
            BrokkeIcons.drawProgressBar(drawContext, itemStartX + 1.0F, itemY + 11.0F + 1.5F, 9.0F, 1.2F, durabilityFraction, BrokkeIcons.progressColor(durabilityFraction, alpha), alpha);
         }

         if (displayedStack.getCount() > 1) {
            String stackCount = String.valueOf(displayedStack.getCount());
            float countTextWidth = BrokkeIcons.measureTextWidth(stackCount, 3.6F);
            float countTextX = itemStartX + 11.0F - countTextWidth + 0.5F;
            recordExtension3.postRenderCallbacks()
               .add(() -> BrokkeIcons.drawCenteredText(drawContext, stackCount, countTextX, itemY + 11.0F - 1.2F, 3.6F, BrokkeIcons.lightGrayWithAlpha(originX)));
         }

         itemStartX += 14.0F;
      }
   }
}

