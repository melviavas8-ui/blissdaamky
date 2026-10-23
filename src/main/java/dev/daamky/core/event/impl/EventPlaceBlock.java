package dev.daamky.core.event.impl;

import dev.daamky.core.event.Event;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public final class EventPlaceBlock extends Event {
    private static final String p92534598 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final Block block;
   private final BlockPos pos;
// https://t.me/pasterplace

   public EventPlaceBlock(Block block, BlockPos position) {
      this.block = block;
      this.pos = position;
   }
// https://t.me/pasterplace

   public Block block() {
      return this.block;
   }
// https://t.me/pasterplace

   public BlockPos pos() {
      return this.pos;
   }
}

