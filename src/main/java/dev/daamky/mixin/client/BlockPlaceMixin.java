package dev.daamky.mixin.client;

import dev.daamky.ClientInitializer;
import dev.daamky.core.event.impl.EventPlaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockPlaceMixin {
    private static final String pd7f9f251 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "onPlaced", at = @At("HEAD"))
   private void daamky$onPlaced(World world, BlockPos placedBlockPos, BlockState blockState, @Nullable LivingEntity placingEntity, ItemStack placedItemStack, CallbackInfo callbackInfo) {
      if (world.isClient()
         && placingEntity == MinecraftClient.getInstance().player
         && blockState.isOf(Blocks.OBSIDIAN)
         && ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().post(new EventPlaceBlock(blockState.getBlock(), placedBlockPos.toImmutable()));
      }
   }
}

