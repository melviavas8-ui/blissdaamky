package dev.daamky;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public final class NoInteract extends ModuleStructure {
    private static final String p605bc121 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final BooleanSetting l1I1IlllIlI = new BooleanSetting("Только с оружием/едой", false);
// https://t.me/pasterplace

   public NoInteract() {
      super("NoInteract", "Не открывает сундуки, верстаки и др. при нажатии ПКМ", ModuleCategory.PLAYER);
      this.addSetting(this.l1I1IlllIlI);
   }
// https://t.me/pasterplace

   public static boolean II1Il11l111II11IIl(BlockPos targetBlockPos) {
      NoInteract module = (NoInteract)ModuleRegistry.getInstance().getModuleByClass(NoInteract.class);
      MinecraftClient client = MinecraftClient.getInstance();
      if (module != null && module.isEnabled() && client.world != null && client.player != null && !client.player.isSneaking()
         )
       {
// https://t.me/pasterplace
         if (module.l1I1IlllIlI.getValue()) {
            ItemStack heldStack = client.player.getMainHandStack();
            boolean heldItemAllowsInteraction = CombatUtils.isInInvalidCriticalState(client.player) || heldStack.contains(DataComponentTypes.FOOD);
            if (!heldItemAllowsInteraction) {
               return false;
            }
         }
// https://t.me/pasterplace

         BlockState blockState = client.world.getBlockState(targetBlockPos);
         return blockState.createScreenHandlerFactory(client.world, targetBlockPos) != null;
      } else {
         return false;
      }
   }
}

