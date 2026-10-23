package dev.daamky;

import dev.daamky.core.event.impl.EventAttack;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

public final class CombatUtils {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private CombatUtils() {
   }
// https://t.me/pasterplace

   public static void attackEntity(Entity entity) {
      MinecraftClient client = MinecraftClient.getInstance();
      ClientPlayerEntity clientPlayerEntity = client.player;
      if (clientPlayerEntity != null && client.interactionManager != null && entity != null) {
         ItemStack mainHandStack = clientPlayerEntity.getMainHandStack();
         EventAttack.consumeCancelled();
         client.interactionManager.attackEntity(clientPlayerEntity, entity);
         if (!EventAttack.consumeCancelled()) {
            clientPlayerEntity.swingHand(Hand.MAIN_HAND);
         }
      }
   }
// https://t.me/pasterplace

   public static float getAttackCooldownProgress() {
      ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
      return clientPlayerEntity == null ? 0.0F : clientPlayerEntity.getAttackCooldownProgress(0.5F);
   }
// https://t.me/pasterplace

   public static boolean isInCobweb(LivingEntity livingEntity) {
      Box box = livingEntity.getBoundingBox().expand(0.001);

      for (Object blockPosition : BlockPos.iterate(
         MathHelper.floor(box.minX),
         MathHelper.floor(box.minY),
         MathHelper.floor(box.minZ),
         MathHelper.floor(box.maxX),
         MathHelper.floor(box.maxY),
         MathHelper.floor(box.maxZ)
      )) {
         if (livingEntity.getEntityWorld().getBlockState((BlockPos)blockPosition).isOf(Blocks.COBWEB)) {
            return true;
         }
      }
// https://t.me/pasterplace

      return false;
   }
// https://t.me/pasterplace

   public static boolean isInInvalidCriticalState(ClientPlayerEntity livingEntity) {
      return livingEntity.isTouchingWater()
         || livingEntity.isInLava()
         || livingEntity.isClimbing()
         || livingEntity.hasVehicle()
         || livingEntity.getAbilities().flying
         || livingEntity.isGliding()
         || livingEntity.hasStatusEffect(StatusEffects.LEVITATION)
         || livingEntity.hasStatusEffect(StatusEffects.SLOW_FALLING)
         || livingEntity.hasStatusEffect(StatusEffects.BLINDNESS)
         || isInInvalidCriticalState(livingEntity);
   }
// https://t.me/pasterplace

   public static boolean isFallingForCriticalHit(ClientPlayerEntity livingEntity) {
      return !isInInvalidCriticalState(livingEntity) && !livingEntity.isOnGround() && livingEntity.fallDistance > 0.0;
   }
// https://t.me/pasterplace

   public static boolean hasMeleeWeapon(PlayerEntity playerEntity) {
      ItemStack mainHandStack = playerEntity.getMainHandStack();
      return mainHandStack.isIn(ItemTags.SWORDS)
         || mainHandStack.isIn(ItemTags.AXES)
         || mainHandStack.isOf(Items.MACE)
         || mainHandStack.isOf(Items.TRIDENT);
   }
// https://t.me/pasterplace

   public static float getEffectiveHealth(LivingEntity livingEntity) {
      return livingEntity.getHealth() + livingEntity.getAbsorptionAmount();
   }
}

