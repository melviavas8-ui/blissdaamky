package dev.daamky;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class BlockRaycastUtils {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final MinecraftClient client = MinecraftClient.getInstance();
   private static final Direction[] BLOCK_FACE_ORDER = new Direction[]{
      Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN
   };
// https://t.me/pasterplace

   private BlockRaycastUtils() {
   }
// https://t.me/pasterplace

   private static boolean addCandidateBlock(
      LinkedHashSet<BlockPos> visitedBlocks, BlockPos candidateBlockPos, BlockPos excludedBlockPos, double reachDistance, Predicate<BlockPos> candidatePredicate
   ) {
      if (visitedBlocks.contains(candidateBlockPos)) {
         return true;
      } else if (visitedBlocks.size() < 6 && isValidRaycastCandidate(candidateBlockPos, excludedBlockPos, reachDistance, candidatePredicate)) {
         visitedBlocks.add(candidateBlockPos.toImmutable());
         return true;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   private static boolean canReachBlockFace(
      BlockRaycastUtils.RaycastBlockHit targetFaceHit,
      BlockPos virtualBlockPos,
      BlockPos excludedBlockPos,
      LinkedHashSet<BlockPos> visitedBlocks,
      double reachDistance,
      Predicate<BlockPos> candidatePredicate
   ) {
      Vec3d playerEyePosition = client.player.getEyePos();
      Vec3d raycastEndPosition = targetFaceHit.hitPosition().subtract(Vec3d.of(targetFaceHit.side().getVector()).multiply(0.001));
      int obstructionCount = 0;

      while (obstructionCount <= 6) {
         BlockHitResult blockHitResult = rayCastReplacingVisitedBlocks(playerEyePosition, raycastEndPosition, virtualBlockPos, visitedBlocks);
         if (blockHitResult != null && blockHitResult.getType() == Type.BLOCK) {
            BlockPos obstructionBlockPos = blockHitResult.getBlockPos();
            if (obstructionBlockPos.equals(targetFaceHit.hitBlockPos())) {
               return blockHitResult.getSide() == targetFaceHit.side();
            }

            if (!obstructionBlockPos.equals(virtualBlockPos) && addCandidateBlock(visitedBlocks, obstructionBlockPos, excludedBlockPos, reachDistance, candidatePredicate)) {
               obstructionCount++;
               continue;
            }

            return false;
         }
// https://t.me/pasterplace

         return false;
      }

      return false;
   }
// https://t.me/pasterplace

   private static BlockHitResult rayCastBlockPosition(BlockPos targetBlockPos) {
      BlockState blockState = client.world.getBlockState(targetBlockPos);
      VoxelShape voxelShape = blockState.getOutlineShape(client.world, targetBlockPos, ShapeContext.of(client.player));
      if (voxelShape.isEmpty()) {
         return null;
      }

      Vec3d blockShapeCenter = voxelShape.getBoundingBox().getCenter().add(targetBlockPos.getX(), targetBlockPos.getY(), targetBlockPos.getZ());
      return client.world
         .raycast(
            new RaycastContext(
               client.player.getEyePos(), blockShapeCenter, ShapeType.OUTLINE, FluidHandling.NONE, client.player
            )
         );
   }
// https://t.me/pasterplace

   public static BlockRaycastUtils.RaycastResult findBlockRaycastPath(BlockPos targetBlockPos, boolean placementMode, double reachDistance, boolean allowReservedBlocks, Predicate<BlockPos> candidatePredicate) {
      if (client.world != null
         && client.player != null
         && targetBlockPos != null
         && candidatePredicate != null
         && Double.isFinite(reachDistance)
         && !(reachDistance <= 0.0)) {
         targetBlockPos = targetBlockPos.toImmutable();
         BlockState blockState = client.world.getBlockState(targetBlockPos);
         if (!placementMode && !isObsidianOrBedrock(blockState)) {
            return null;
         }

         if (placementMode && isObsidianOrBedrock(blockState)) {
            return null;
         }

         BlockPos excludedBlockPos = placementMode ? null : targetBlockPos;
          Predicate<BlockPos> raycastCandidatePredicate = candidateBlockPos -> !candidateBlockPos.equals(excludedBlockPos) && candidatePredicate.test(candidateBlockPos);
          LinkedHashSet<BlockPos> visitedBlocks = new LinkedHashSet<>();
         if (placementMode && !blockState.isReplaceable() && !addCandidateBlock(visitedBlocks, targetBlockPos, null, reachDistance, raycastCandidatePredicate)) {
            return null;
         }

         if (!client.world.isAir(targetBlockPos.up())
            && !addCandidateBlock(visitedBlocks, targetBlockPos.up(), null, reachDistance, raycastCandidatePredicate)) {
            return null;
         }

         List targetFaceHits = getBlockFaceHits(targetBlockPos, new Box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), reachDistance);
         if (targetFaceHits.isEmpty()) {
            return null;
         }

         ArrayList supportFaceHits = new ArrayList();
         if (placementMode) {
            for (Direction supportDirection : BLOCK_FACE_ORDER) {
               BlockPos neighborBlockPos = targetBlockPos.offset(supportDirection.getOpposite());
               if (!visitedBlocks.contains(neighborBlockPos)) {
                  BlockState neighborBlockState = client.world.getBlockState(neighborBlockPos);
                  if (!neighborBlockState.isReplaceable()
                     && !neighborBlockState.getCollisionShape(client.world, neighborBlockPos).isEmpty()
                     && (allowReservedBlocks || !BlockPosReservationManager.isInteractableBlock(neighborBlockState))) {
                     VoxelShape voxelShape = neighborBlockState.getOutlineShape(
                        client.world, neighborBlockPos, ShapeContext.of(client.player)
                     );
                     if (!voxelShape.isEmpty()) {
                        BlockRaycastUtils.RaycastBlockHit neighborFaceHit = createBlockFaceHit(neighborBlockPos, supportDirection, voxelShape.getBoundingBox());
                        if (isHitWithinReachAndFacing(neighborFaceHit, reachDistance)) {
                           supportFaceHits.add(neighborFaceHit);
                        }
                     }
                  }
               }
            }

            if (supportFaceHits.isEmpty()) {
               return null;
            }
         } else {
            supportFaceHits.add(null);
         }

         BlockRaycastUtils.RaycastResult bestRaycastPath = null;

         for (Object supportFaceHit : supportFaceHits) {
            for (Object targetFaceHit : targetFaceHits) {
               BlockRaycastUtils.RaycastResult candidatePath = searchRaycastPath(
                  targetBlockPos, (BlockRaycastUtils.RaycastBlockHit)supportFaceHit, (BlockRaycastUtils.RaycastBlockHit)targetFaceHit, visitedBlocks, reachDistance, raycastCandidatePredicate
               );
               if (candidatePath != null && (bestRaycastPath == null || candidatePath.visitedBlocks().size() < bestRaycastPath.visitedBlocks().size())) {
                  bestRaycastPath = candidatePath;
                  if (candidatePath.visitedBlocks().size() == visitedBlocks.size()) {
                     return candidatePath;
                  }
               }
            }
         }
// https://t.me/pasterplace

         return bestRaycastPath;
      } else {
         return null;
      }
   }
// https://t.me/pasterplace

   private static boolean isHitWithinReachAndFacing(BlockRaycastUtils.RaycastBlockHit faceHit, double reachDistance) {
      Vec3d playerEyePosition = client.player.getEyePos();
      return playerEyePosition.squaredDistanceTo(faceHit.hitPosition()) <= reachDistance * reachDistance
         && playerEyePosition.subtract(faceHit.hitPosition()).dotProduct(Vec3d.of(faceHit.side().getVector())) > 0.001;
   }
// https://t.me/pasterplace

   private static List<BlockRaycastUtils.RaycastBlockHit> getBlockFaceHits(BlockPos targetBlockPos, Box blockShapeBounds, double reachDistance) {
      ArrayList faceHits = new ArrayList();

      for (Direction faceDirection : BLOCK_FACE_ORDER) {
         BlockRaycastUtils.RaycastBlockHit faceHit = createBlockFaceHit(targetBlockPos, faceDirection, blockShapeBounds);
         if (isHitWithinReachAndFacing(faceHit, reachDistance)) {
            faceHits.add(faceHit);
         }
      }

      return faceHits;
   }
// https://t.me/pasterplace

   private static BlockRaycastUtils.RaycastBlockHit createBlockFaceHit(BlockPos targetBlockPos, Direction direction, Box box) {
      double hitX = (box.minX + box.maxX) * 0.5;
      double hitY = (box.minY + box.maxY) * 0.5;
      double hitZ = (box.minZ + box.maxZ) * 0.5;
      switch (direction) {
         case DOWN:
            hitY = box.minY;
            break;
         case UP:
            hitY = box.maxY;
            break;
         case NORTH:
            hitZ = box.minZ;
            break;
         case SOUTH:
            hitZ = box.maxZ;
            break;
         case WEST:
            hitX = box.minX;
            break;
         case EAST:
            hitX = box.maxX;
      }

      return new BlockRaycastUtils.RaycastBlockHit(
         targetBlockPos.toImmutable(), direction, new Vec3d(targetBlockPos.getX() + hitX, targetBlockPos.getY() + hitY, targetBlockPos.getZ() + hitZ)
      );
   }
// https://t.me/pasterplace

   private static boolean isObsidianOrBedrock(BlockState blockState) {
      return blockState.isOf(Blocks.OBSIDIAN) || blockState.isOf(Blocks.BEDROCK);
   }
// https://t.me/pasterplace

   private static BlockHitResult rayCastReplacingVisitedBlocks(Vec3d rayStart, Vec3d rayEnd, BlockPos virtualObsidianPos, Set<BlockPos> visitedBlocks) {
      return (BlockHitResult)BlockView.raycast(rayStart, rayEnd, client.world, (clientWorld, raycastBlockPos) -> {
         boolean isVirtualObsidianPos = raycastBlockPos.equals(virtualObsidianPos);
         if (!isVirtualObsidianPos && visitedBlocks.contains(raycastBlockPos)) {
            return null;
         }

         BlockState raycastBlockState = isVirtualObsidianPos ? Blocks.OBSIDIAN.getDefaultState() : clientWorld.getBlockState(raycastBlockPos);
         VoxelShape voxelShape = raycastBlockState.getOutlineShape(clientWorld, raycastBlockPos, ShapeContext.of(client.player));
         return clientWorld.raycastBlock(rayStart, rayEnd, raycastBlockPos, voxelShape, raycastBlockState);
      }, clientWorld -> null);
   }
// https://t.me/pasterplace

   private static BlockRaycastUtils.RaycastResult searchRaycastPath(
      BlockPos targetBlockPos,
      BlockRaycastUtils.RaycastBlockHit supportFaceHit,
      BlockRaycastUtils.RaycastBlockHit targetFaceHit,
      Set<BlockPos> visitedBlocks,
      double reachDistance,
      Predicate<BlockPos> candidatePredicate
   ) {
      LinkedHashSet visitedBlockPositions = new LinkedHashSet(visitedBlocks);
      BlockPos excludedBlockPos = supportFaceHit == null ? null : supportFaceHit.hitBlockPos();

      for (int pathSearchStep = 0; pathSearchStep <= 6; pathSearchStep++) {
         if (supportFaceHit != null && !canReachBlockFace(supportFaceHit, null, excludedBlockPos, visitedBlockPositions, reachDistance, candidatePredicate)) {
            return null;
         }

         if (!canReachBlockFace(targetFaceHit, targetBlockPos, excludedBlockPos, visitedBlockPositions, reachDistance, candidatePredicate)) {
            return null;
         }

         if (visitedBlockPositions.isEmpty()) {
            return new BlockRaycastUtils.RaycastResult(visitedBlockPositions, null);
         }
// https://t.me/pasterplace

         BlockPos obstructionBlockPos = null;

         for (Object visitedBlockPos : visitedBlockPositions) {
            BlockHitResult raycastHitResult = rayCastBlockPosition((BlockPos)visitedBlockPos);
            if (raycastHitResult != null && raycastHitResult.getType() == Type.BLOCK) {
               BlockPos raycastHitBlockPos = raycastHitResult.getBlockPos();
               if (visitedBlockPositions.contains(raycastHitBlockPos)) {
                  return new BlockRaycastUtils.RaycastResult(visitedBlockPositions, raycastHitBlockPos);
               }

               if (obstructionBlockPos == null && isValidRaycastCandidate(raycastHitBlockPos, excludedBlockPos, reachDistance, candidatePredicate)) {
                  obstructionBlockPos = raycastHitBlockPos.toImmutable();
               }
            }
         }

         if (obstructionBlockPos == null || !addCandidateBlock(visitedBlockPositions, obstructionBlockPos, excludedBlockPos, reachDistance, candidatePredicate)) {
            return null;
         }
      }

      return null;
   }
// https://t.me/pasterplace

   private static boolean isValidRaycastCandidate(BlockPos candidateBlockPos, BlockPos excludedBlockPos, double reachDistance, Predicate<BlockPos> candidatePredicate) {
      if (candidateBlockPos.equals(excludedBlockPos)) {
         return false;
      }

      BlockState blockState = client.world.getBlockState(candidateBlockPos);
      return !blockState.isAir()
         && blockState.getFluidState().isEmpty()
         && blockState.getHardness(client.world, candidateBlockPos) >= 0.0F
         && client.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(candidateBlockPos)) <= reachDistance * reachDistance
         && candidatePredicate.test(candidateBlockPos);
   }
// https://t.me/pasterplace

   private record RaycastBlockHit(BlockPos hitBlockPos, Direction side, Vec3d hitPosition) {

      private RaycastBlockHit(BlockPos hitBlockPos, Direction side, Vec3d hitPosition) {
         this.hitBlockPos = hitBlockPos;
         this.side = side;
         this.hitPosition = hitPosition;
      }
   }
// https://t.me/pasterplace

    public record RaycastResult(Set<BlockPos> visitedBlocks, BlockPos hitBlock) {
// https://t.me/pasterplace

      public RaycastResult(Set<BlockPos> visitedBlocks, BlockPos hitBlock) {
          LinkedHashSet<BlockPos> immutableVisitedBlocks = new LinkedHashSet<>();

          for (BlockPos visitedBlockPos : visitedBlocks) {
            immutableVisitedBlocks.add(visitedBlockPos.toImmutable());
         }

         this.visitedBlocks = Collections.unmodifiableSet(immutableVisitedBlocks);
         this.hitBlock = hitBlock == null ? null : hitBlock.toImmutable();
      }
   }
}

