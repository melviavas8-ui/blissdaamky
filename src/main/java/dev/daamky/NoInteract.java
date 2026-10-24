package dev.daamky;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public final class NoInteract extends ModuleStructure {
   private final BooleanSetting l1I1IlllIlI = new BooleanSetting("Только с оружием/едой", false);

   public NoInteract() {
      super("NoInteract", "Не открывает сундуки, верстаки и др. при нажатии ПКМ", ModuleCategory.PLAYER);
      this.addSetting(this.l1I1IlllIlI);
   }

   public static boolean II1Il11l111II11IIl(BlockPos targetBlockPos) {
      NoInteract module = (NoInteract)ModuleRegistry.getInstance().getModuleByClass(NoInteract.class);
      MinecraftClient client = MinecraftClient.getInstance();
      if (module != null && module.isEnabled() && client.world != null && client.player != null && !client.player.isSneaking()
         )
       {
         if (module.l1I1IlllIlI.getValue()) {
            ItemStack heldStack = client.player.getMainHandStack();
            boolean heldItemAllowsInteraction = CombatUtils.isInInvalidCriticalState(client.player) || heldStack.contains(DataComponentTypes.FOOD);
            if (!heldItemAllowsInteraction) {
               return false;
            }
         }

         BlockState blockState = client.world.getBlockState(targetBlockPos);
         return blockState.createScreenHandlerFactory(client.world, targetBlockPos) != null;
      } else {
         return false;
      }
   }
}

