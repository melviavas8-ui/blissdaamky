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

   private BlockPosReservationManager() {
   }

   public static BlockPosReservationManager getInstance() {
      return INSTANCE;
   }

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

   public void releaseReservations(Object owner) {
      if (owner != null) {
         this.reservations.entrySet().removeIf(reservationEntry -> reservationEntry.getValue() == owner);
      }
   }

   public boolean isReservedByOther(Object owner, BlockPos position) {
      Object reservationOwner = position == null ? null : this.reservations.get(position);
      return reservationOwner != null && reservationOwner != owner;
   }

   public static boolean isInteractableBlock(BlockState blockState) {
       for (Class<? extends Block> blockClass : INTERACTABLE_BLOCK_TYPES) {
         if (blockClass.isInstance(blockState.getBlock())) {
            return true;
         }
      }

      return false;
   }
}

