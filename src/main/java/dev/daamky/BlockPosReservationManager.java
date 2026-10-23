package dev.daamky;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.CartographyTableBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.block.StonecutterBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.util.math.BlockPos;

public final class BlockPosReservationManager {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final BlockPosReservationManager INSTANCE = new BlockPosReservationManager();
    private static final List<Class<? extends Block>> INTERACTABLE_BLOCK_TYPES = List.of(
      BlockWithEntity.class,
      DoorBlock.class,
      TrapdoorBlock.class,
      FenceGateBlock.class,
      ButtonBlock.class,
      LeverBlock.class,
      NoteBlock.class,
      JukeboxBlock.class,
      CraftingTableBlock.class,
      AbstractFurnaceBlock.class,
      AnvilBlock.class,
      BarrelBlock.class,
      ChestBlock.class,
      EnderChestBlock.class,
      ShulkerBoxBlock.class,
      EnchantingTableBlock.class,
      BrewingStandBlock.class,
      LoomBlock.class,
      StonecutterBlock.class,
      CartographyTableBlock.class,
      SmithingTableBlock.class,
      GrindstoneBlock.class,
      BeaconBlock.class,
      LecternBlock.class
   );
    private final Map<BlockPos, Object> reservations = new HashMap<>();
// https://t.me/pasterplace

   private BlockPosReservationManager() {
   }
// https://t.me/pasterplace

   public static BlockPosReservationManager getInstance() {
      return INSTANCE;
   }
// https://t.me/pasterplace

   public void reservePositions(Object owner, Collection<BlockPos> items) {
      if (owner != null) {
         this.releaseReservations(owner);
         if (items != null) {
             for (BlockPos reservedPosition : items) {
               if (reservedPosition != null) {
                  this.reservations.put(reservedPosition.toImmutable(), owner);
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   public void releaseReservations(Object owner) {
      if (owner != null) {
         this.reservations.entrySet().removeIf(reservationEntry -> reservationEntry.getValue() == owner);
      }
   }
// https://t.me/pasterplace

   public boolean isReservedByOther(Object owner, BlockPos position) {
      Object reservationOwner = position == null ? null : this.reservations.get(position);
      return reservationOwner != null && reservationOwner != owner;
   }
// https://t.me/pasterplace

   public static boolean isInteractableBlock(BlockState blockState) {
       for (Class<? extends Block> blockClass : INTERACTABLE_BLOCK_TYPES) {
         if (blockClass.isInstance(blockState.getBlock())) {
            return true;
         }
      }
// https://t.me/pasterplace

      return false;
   }
}

