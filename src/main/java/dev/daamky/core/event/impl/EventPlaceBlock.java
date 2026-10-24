package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public final class EventPlaceBlock extends Event {
   private final Block block;
   private final BlockPos pos;

   public EventPlaceBlock(Block block, BlockPos position) {
      this.block = block;
      this.pos = position;
   }

   public Block block() {
      return this.block;
   }

   public BlockPos pos() {
      return this.pos;
   }
}

