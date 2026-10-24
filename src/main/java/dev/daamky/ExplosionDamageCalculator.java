package dev.daamky;

import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class ExplosionDamageCalculator {
   private ExplosionDamageCalculator() {
   }

   private static double calculateExposure(Vec3d explosionPosition, LivingEntity targetEntity, Box targetBounds, BlockPos forcedObsidianBlock, Set<BlockPos> ignoredBlocks) {
      double xSampleSpacing = 1.0 / ((targetBounds.maxX - targetBounds.minX) * 2.0 + 1.0);
      double ySampleSpacing = 1.0 / ((targetBounds.maxY - targetBounds.minY) * 2.0 + 1.0);
      double zSampleSpacing = 1.0 / ((targetBounds.maxZ - targetBounds.minZ) * 2.0 + 1.0);
      double xSampleOffset = (1.0 - Math.floor(1.0 / xSampleSpacing) * xSampleSpacing) / 2.0;
      double zSampleOffset = (1.0 - Math.floor(1.0 / zSampleSpacing) * zSampleSpacing) / 2.0;
      int visibleSampleCount = 0;
      int totalSampleCount = 0;

      for (double xSampleFraction = 0.0; xSampleFraction <= 1.0; xSampleFraction += xSampleSpacing) {
         for (double ySampleFraction = 0.0; ySampleFraction <= 1.0; ySampleFraction += ySampleSpacing) {
            for (double zSampleFraction = 0.0; zSampleFraction <= 1.0; zSampleFraction += zSampleSpacing) {
               Vec3d samplePosition = new Vec3d(
                  MathHelper.lerp(xSampleFraction, targetBounds.minX, targetBounds.maxX) + xSampleOffset,
                  MathHelper.lerp(ySampleFraction, targetBounds.minY, targetBounds.maxY),
                  MathHelper.lerp(zSampleFraction, targetBounds.minZ, targetBounds.maxZ) + zSampleOffset
               );
               if (isVisibleRay(samplePosition, explosionPosition, targetEntity, forcedObsidianBlock, ignoredBlocks)) {
                  visibleSampleCount++;
               }

               totalSampleCount++;
            }
         }
      }

      return totalSampleCount == 0 ? 0.0 : (double)visibleSampleCount / totalSampleCount;
   }

   private static boolean isVisibleRay(Vec3d rayStart, Vec3d rayEnd, LivingEntity targetEntity, BlockPos forcedObsidianBlock, Set<BlockPos> ignoredBlocks) {
      if (forcedObsidianBlock == null && ignoredBlocks.isEmpty()) {
         BlockHitResult blockHitResult = targetEntity.getEntityWorld()
            .raycast(new RaycastContext(rayStart, rayEnd, ShapeType.COLLIDER, FluidHandling.NONE, targetEntity));
         return blockHitResult.getType() == Type.MISS;
      } else {
         return (Boolean)BlockView.raycast(rayStart, rayEnd, targetEntity.getEntityWorld(), (world, currentBlockPos) -> {
            boolean isForcedObsidianBlock = currentBlockPos.equals(forcedObsidianBlock);
            if (!isForcedObsidianBlock && ignoredBlocks.contains(currentBlockPos)) {
               return null;
            }

            BlockState blockState = isForcedObsidianBlock ? Blocks.OBSIDIAN.getDefaultState() : world.getBlockState(currentBlockPos);
            VoxelShape voxelShape = blockState.getCollisionShape(world, currentBlockPos, ShapeContext.of(targetEntity));
            return world.raycastBlock(rayStart, rayEnd, currentBlockPos, voxelShape, blockState) == null ? null : Boolean.FALSE;
         }, world -> Boolean.TRUE);
      }
   }

   public static float calculateDamage(Vec3d explosionPosition, LivingEntity targetEntity, Box targetBounds, BlockPos forcedObsidianBlock, BlockPos ignoredBlock) {
      return calculateDamage(explosionPosition, targetEntity, targetBounds, forcedObsidianBlock, ignoredBlock == null ? Set.of() : Set.of(ignoredBlock));
   }

   public static float calculateDamage(Vec3d explosionPosition, LivingEntity targetEntity, Box targetBounds, BlockPos forcedObsidianBlock, Set<BlockPos> ignoredBlocks) {
      Set effectiveIgnoredBlocks = ignoredBlocks == null ? Set.of() : ignoredBlocks;
      Vec3d impactPosition = new Vec3d((targetBounds.minX + targetBounds.maxX) * 0.5, targetBounds.minY, (targetBounds.minZ + targetBounds.maxZ) * 0.5);
      double normalizedDistance = impactPosition.distanceTo(explosionPosition) / 12.0;
      if (normalizedDistance > 1.0) {
         return 0.0F;
      }

      double exposureAdjustedImpact = (1.0 - normalizedDistance) * calculateExposure(explosionPosition, targetEntity, targetBounds, forcedObsidianBlock, effectiveIgnoredBlocks);
      float rawDamage = (float)((exposureAdjustedImpact * exposureAdjustedImpact + exposureAdjustedImpact) * 42.0 + 1.0);
      if (targetEntity instanceof PlayerEntity) {
         Difficulty difficulty = targetEntity.getEntityWorld().getDifficulty();

         rawDamage = switch (difficulty) {
            case PEACEFUL -> 0.0F;
            case EASY -> Math.min(rawDamage * 0.5F + 1.0F, rawDamage);
            case HARD -> rawDamage * 1.5F;
            case NORMAL -> rawDamage;
            default -> throw new MatchException(null, null);
         };
      }

      float armor = targetEntity.getArmor();
      float armorToughness = (float)targetEntity.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS);
      float effectiveArmor = MathHelper.clamp(armor - rawDamage / (2.0F + armorToughness / 4.0F), armor * 0.2F, 20.0F);
      float damageAfterArmor = rawDamage * (1.0F - effectiveArmor / 25.0F);
      int totalProtection = 0;

      for (EquipmentSlot armorSlot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
         ItemStack armorStack = targetEntity.getEquippedStack(armorSlot);
         totalProtection += getEnchantmentLevel(targetEntity, armorStack, Enchantments.PROTECTION);
         totalProtection += getEnchantmentLevel(targetEntity, armorStack, Enchantments.BLAST_PROTECTION) * 2;
      }

      totalProtection = MathHelper.clamp(totalProtection, 0, 20);
      damageAfterArmor *= 1.0F - totalProtection / 25.0F;
      StatusEffectInstance statusEffectInstance = targetEntity.getStatusEffect(StatusEffects.RESISTANCE);
      if (statusEffectInstance != null) {
         float resistanceReduction = MathHelper.clamp((statusEffectInstance.getAmplifier() + 1) * 0.2F, 0.0F, 1.0F);
         damageAfterArmor *= 1.0F - resistanceReduction;
      }

      return Math.max(0.0F, damageAfterArmor);
   }

   public static float calculateDamage(Vec3d explosionPosition, LivingEntity targetEntity, Box targetBounds, BlockPos ignoredBlock) {
      return calculateDamage(explosionPosition, targetEntity, targetBounds, null, ignoredBlock == null ? Set.of() : Set.of(ignoredBlock));
   }

   public static float calculateDamage(Vec3d explosionPosition, LivingEntity targetEntity, Box targetBounds) {
      return calculateDamage(explosionPosition, targetEntity, targetBounds, null, Set.of());
   }

   public static float calculateDamage(Vec3d explosionPosition, LivingEntity targetEntity) {
      return calculateDamage(explosionPosition, targetEntity, targetEntity.getBoundingBox(), null, Set.of());
   }

   private static int getEnchantmentLevel(LivingEntity targetEntity, ItemStack armorStack, RegistryKey<Enchantment> enchantmentKey) {
      if (armorStack.isEmpty()) {
         return 0;
      }

      try {
         return targetEntity.getRegistryManager()
            .getOrThrow(RegistryKeys.ENCHANTMENT)
            .getOptional(enchantmentKey)
            .map(reference -> EnchantmentHelper.getLevel(reference, armorStack))
            .orElse(0);
      } catch (RuntimeException exception) {
         return 0;
      }
   }
}

