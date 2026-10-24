package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoTool extends ModuleStructure {
   private final BooleanSetting l1I1IlllIlI = new BooleanSetting("Возвращать слот", true);
   private final BooleanSetting IllI1l1l1 = new BooleanSetting("Беречь прочность", true);
   private int l1lll11l1l = -1;

   public AutoTool() {
      super("AutoTool", "Берёт лучший инструмент для ломания блока", ModuleCategory.PLAYER);
      this.addSettings(this.l1I1IlllIlI, this.IllI1l1l1);
   }

   @EventTarget
   public void II1Il11l111II11IIl(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         boolean isBreakingBlock = CLIENT.options.attackKey.isPressed()
            && CLIENT.crosshairTarget != null
            && CLIENT.crosshairTarget.getType() == Type.BLOCK;
         if (!isBreakingBlock) {
            if (this.l1lll11l1l != -1 && this.l1I1IlllIlI.getValue()) {
               InventoryUtils.setSelectedHotbarSlot(this.l1lll11l1l);
            }

            this.l1lll11l1l = -1;
         } else {
            BlockState blockState = CLIENT.world.getBlockState(((BlockHitResult)CLIENT.crosshairTarget).getBlockPos());
            if (!blockState.isAir()) {
               int bestToolSlot = -1;
               float bestMiningSpeed = CLIENT.player.getMainHandStack().getMiningSpeedMultiplier(blockState);

               for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
                  ItemStack candidateTool = CLIENT.player.getInventory().getStack(hotbarSlot);
                  if (!this.IllI1l1l1.getValue() || !candidateTool.isDamageable() || candidateTool.getMaxDamage() - candidateTool.getDamage() > 3) {
                     float miningSpeed = candidateTool.getMiningSpeedMultiplier(blockState);
                     if (miningSpeed > bestMiningSpeed + 0.01F) {
                        bestMiningSpeed = miningSpeed;
                        bestToolSlot = hotbarSlot;
                     }
                  }
               }

               if (bestToolSlot != -1 && bestToolSlot != InventoryUtils.getSelectedHotbarSlot()) {
                  if (this.l1lll11l1l == -1) {
                     this.l1lll11l1l = InventoryUtils.getSelectedHotbarSlot();
                  }

                  InventoryUtils.setSelectedHotbarSlot(bestToolSlot);
               }
            }
         }
      }
   }

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.l1lll11l1l = -1;
   }
}

