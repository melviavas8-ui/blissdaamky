package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventRender3D;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.mixin.client.MultiPlayerGameModeAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class CrystalAura extends ModuleStructure {
    private static final String sourceAttributionUrl = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final int CRYSTAL_SWITCH_DELAY = 15;
   private static final int CRYSTAL_TARGET_LIMIT = 6;
   private static final String excludeSelfLabel = "Себя";
   private static final String excludeFriendsLabel = "Друзей";
   private static final String protectedResourcesLabel = "Ресурсы";
   private static final String playersTargetLabel = "Игроки";
   private static final String unarmoredTargetsLabel = "Голые";
   private final FloatSetting targetRange = new FloatSetting("Дистанция до цели", 8.0F, 3.0F, 12.0F, 0.5F);
   private final FloatSetting placementRange = new FloatSetting("Дистанция постановки", 5.5F, 2.0F, 6.0F, 0.1F);
   private final FloatSetting minimumSelfDistance = new FloatSetting("Мин. дистанция до себя", 1.5F, 0.0F, 6.0F, 0.1F);
   private final FloatSetting targetPredictionTicks = new FloatSetting("Предикт", 1.5F, 0.0F, 4.0F, 0.1F);
   private final FloatSetting minimumTargetDamage = new FloatSetting("Мин. урон по цели", 4.0F, 0.0F, 20.0F, 0.5F);
   private final dev.daamky.BooleanSetting placeObsidian = new dev.daamky.BooleanSetting("Ставить обсидиан", true);
   private final dev.daamky.BooleanSetting mineForCrystalPlacement = new dev.daamky.BooleanSetting("Подкапывать", true);
   private final dev.daamky.BooleanSetting enableSmartSafety = new dev.daamky.BooleanSetting("Умный сейф", true);
   private final FloatSetting maximumSelfDamage = new FloatSetting("Макс. урон себе", 6.0F, 0.0F, 20.0F, 0.5F);
   private final FloatSetting minimumHealthAfterExplosion = new FloatSetting("Мин. HP после взрыва", 6.0F, 0.0F, 20.0F, 0.5F);
   private final dev.daamky.BooleanSetting requireFavorableTrade = new dev.daamky.BooleanSetting("Только выгодный размен", true);
   private final FloatSetting minimumDamageAdvantage = new FloatSetting("Мин. выгода (цель−я)", 1.0F, 0.0F, 12.0F, 0.5F);
   private final dev.daamky.BooleanSetting renderPlacementMarkers = new dev.daamky.BooleanSetting("Рендер", true);
   private final MultiToggleSetting protectedEntities = new MultiToggleSetting("Не взрывать")
      .addOption("Себя", false)
      .addOption("Друзей", true)
      .addOption("Ресурсы", true);
   private final MultiToggleSetting targetCategories = new MultiToggleSetting("Цели").addOption("Игроки", true).addOption("Голые", true);
   private final ConcurrentLinkedQueue<CrystalAura.BlockUpdateMarker> placementRenderMarkers = new ConcurrentLinkedQueue<>();
   private final CrystalAura.ItemSlotSwapState itemSlotSwapState = new CrystalAura.ItemSlotSwapState();
   private final Map<Long, Boolean> explosionSafetyCache = new HashMap<>();
   private int swappedInventorySlot = -1;
   private int trackedCrystalTicksRemaining;
   private int blockMiningTicksRemaining;
   private BlockPos pendingObsidianPosition;
   private Box trackedCrystalSearchBox;
   private BlockPos blockToMineForCrystal;
   private boolean targetInsideCobweb;
   private int itemSwapCooldownTicks;
   private int pendingObsidianTicksRemaining;
   private BlockPos lastPlacementPosition;
   private LivingEntity currentTarget;
   private int trackedCrystalEntityId = -1;
   private Vec3d trackedCrystalPosition;
   private int swappedHotbarSlot = -1;
   private int originalSelectedSlot = -1;
   private CrystalAura.CrystalAttackCandidate pendingAttack;
// https://t.me/pasterplace

   public CrystalAura() {
      super("CrystalAura", "Умная постановка и взрыв кристаллов", ModuleCategory.COMBAT);
      this.maximumSelfDamage.visibleWhen(this.enableSmartSafety::getValue);
      this.minimumHealthAfterExplosion.visibleWhen(this.enableSmartSafety::getValue);
      this.minimumDamageAdvantage.visibleWhen(this.requireFavorableTrade::getValue);
      this.addSettings(
         this.targetRange,
         this.placementRange,
         this.minimumSelfDistance,
         this.targetPredictionTicks,
         this.minimumTargetDamage,
         this.placeObsidian,
         this.mineForCrystalPlacement,
         this.enableSmartSafety,
         this.maximumSelfDamage,
         this.minimumHealthAfterExplosion,
         this.requireFavorableTrade,
         this.minimumDamageAdvantage,
         this.renderPlacementMarkers,
         this.protectedEntities,
         this.targetCategories
      );
   }
// https://t.me/pasterplace

   private boolean isSolidBuildBlock(BlockPos position) {
      return CLIENT.world.isInBuildLimit(position)
         && !CLIENT.world.getBlockState(position).isAir()
         && !CLIENT.world.getBlockState(position).isReplaceable();
   }
// https://t.me/pasterplace

   private void tickInventorySwap() {
      if (!this.itemSlotSwapState.hasPendingSwap()) {
         this.itemSwapCooldownTicks = 0;
      } else if (++this.itemSwapCooldownTicks >= 20) {
         this.itemSlotSwapState.restoreItemsAndSelectedSlot();
         this.itemSwapCooldownTicks = 0;
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.restoreInventoryState();
      this.itemSlotSwapState.restoreItemsAndSelectedSlot();
      this.resetCombatState();
      this.currentTarget = null;
      this.placementRenderMarkers.clear();
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }
// https://t.me/pasterplace

   private void resetCombatState() {
      this.pendingAttack = null;
      this.clearTrackedCrystalSpawn();
      this.clearPlacementState();
      this.explosionSafetyCache.clear();
      this.clearLegacyInventorySwapSlots();
      this.lastPlacementPosition = null;
      this.pendingObsidianPosition = null;
      this.pendingObsidianTicksRemaining = 0;
      this.targetInsideCobweb = false;
      this.itemSwapCooldownTicks = 0;
   }
// https://t.me/pasterplace

   private void beginObsidianPlacement(BlockPos obsidianPosition, CrystalAura.CrystalPlacementHit l1lll11l1l) {
      if (this.queueBlockInteraction(Items.OBSIDIAN, l1lll11l1l.hitResult())) {
         this.pendingObsidianPosition = obsidianPosition;
         this.pendingObsidianTicksRemaining = 8;
         this.lastPlacementPosition = obsidianPosition;
         this.addPlacementRenderMarker(obsidianPosition);
      }
   }
// https://t.me/pasterplace

   private BlockPos findExistingCrystalBase(LivingEntity livingEntity) {
      CrystalAura.CrystalPlacementCandidate placementCandidate = this.findBestPlacement(livingEntity);
      return placementCandidate != null && placementCandidate.mode() == CrystalAura.CrystalAuraMode.CRYSTAL_ON_BASE ? placementCandidate.basePosition() : null;
   }
// https://t.me/pasterplace

   private void attackCrystalInTrackedArea() {
      if (this.trackedCrystalSearchBox != null && CLIENT.world != null) {
         for (EndCrystalEntity crystal : CLIENT.world.getEntitiesByClass(EndCrystalEntity.class, this.trackedCrystalSearchBox.expand(0.75), endCrystalEntity -> true)) {
            if (this.isEntitySafeForPlacement(crystal)) {
               this.queueCrystalAttack(crystal);
               return;
            }
         }
      }
   }
// https://t.me/pasterplace

   private boolean isCrystalBaseUsable(BlockPos basePosition, LivingEntity livingEntity) {
      BlockState blockState = CLIENT.world.getBlockState(basePosition);
      if (!blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.BEDROCK)) {
         return false;
      }

      BlockPos crystalSpawnPosition = basePosition.up();
      BlockState blockState2 = CLIENT.world.getBlockState(crystalSpawnPosition);
      if (!blockState2.isAir() && !blockState2.isReplaceable()) {
         return false;
      }

      Box box = new Box(
         crystalSpawnPosition.getX(),
         crystalSpawnPosition.getY(),
         crystalSpawnPosition.getZ(),
         crystalSpawnPosition.getX() + 1.0,
         crystalSpawnPosition.getY() + 2.0,
         crystalSpawnPosition.getZ() + 1.0
      );
      return CLIENT.world
         .getOtherEntities(
            livingEntity, box, entity -> entity instanceof LivingEntity && entity != CLIENT.player && !(entity instanceof ArmorStandEntity)
         )
         .isEmpty();
   }
// https://t.me/pasterplace

   private boolean isBetterPlacementCandidate(CrystalAura.CrystalPlacementCandidate candidate, CrystalAura.CrystalPlacementCandidate currentBest) {
      if (Math.abs(candidate.placementScore() - currentBest.placementScore()) > 0.35F) {
         return candidate.placementScore() > currentBest.placementScore();
      } else if (Math.abs(candidate.netDamage() - currentBest.netDamage()) > 0.25F) {
         return candidate.netDamage() > currentBest.netDamage();
      } else {
         return Math.abs(candidate.targetDamage() - currentBest.targetDamage()) > 0.25F
            ? candidate.targetDamage() > currentBest.targetDamage()
            : candidate.mode().ordinal() < currentBest.mode().ordinal();
      }
   }
// https://t.me/pasterplace

   private void clearTrackedCrystalSpawn() {
      this.trackedCrystalSearchBox = null;
      this.trackedCrystalEntityId = -1;
      this.trackedCrystalPosition = null;
      this.trackedCrystalTicksRemaining = 0;
   }
// https://t.me/pasterplace

   private int findInventorySlot(Item item) {
      for (int slotIndex = 9; slotIndex < 36; slotIndex++) {
         if (CLIENT.player.getInventory().getStack(slotIndex).isOf(item)) {
            return slotIndex;
         }
      }

      return -1;
   }
// https://t.me/pasterplace

   private void trackCrystalSpawnNearBase(BlockPos basePosition) {
      BlockPos crystalSpawnPosition = basePosition.up();
      this.trackedCrystalSearchBox = new Box(crystalSpawnPosition).expand(1.0);
      this.trackedCrystalEntityId = -1;
      this.trackedCrystalPosition = Vec3d.ofCenter(crystalSpawnPosition);
      this.trackedCrystalTicksRemaining = 12;
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onPacket(EventPacket eventPacket) {
      if (eventPacket.isReceive()) {
         Packet packet = eventPacket.packet();
         if (packet instanceof EntitySpawnS2CPacket) {
            EntitySpawnS2CPacket packet2 = (EntitySpawnS2CPacket)packet;
            if (packet2.getEntityType() == EntityType.END_CRYSTAL && CLIENT.world != null) {
               CLIENT.execute(() -> this.handleCrystalSpawnPacket(packet2));
            }
         }
      }
   }
// https://t.me/pasterplace

   private void handleCrystalSpawnPacket(EntitySpawnS2CPacket entitySpawnS2CPacket) {
      if (this.isEnabled() && CLIENT.player != null && CLIENT.world != null && CLIENT.interactionManager != null) {
         double spawnX = entitySpawnS2CPacket.getX();
         double spawnY = entitySpawnS2CPacket.getY();
         double spawnZ = entitySpawnS2CPacket.getZ();
         boolean insideTrackedSearchBox = this.trackedCrystalSearchBox != null && this.trackedCrystalSearchBox.expand(1.0).contains(spawnX, spawnY, spawnZ);
         boolean nearCurrentTarget = this.currentTarget != null && this.currentTarget.squaredDistanceTo(spawnX, spawnY, spawnZ) <= 16.0;
         if (insideTrackedSearchBox || nearCurrentTarget) {
            this.trackedCrystalEntityId = entitySpawnS2CPacket.getEntityId();
            this.trackedCrystalPosition = new Vec3d(spawnX, spawnY, spawnZ);
            this.trackedCrystalTicksRemaining = 12;
            if (this.trackedCrystalSearchBox == null) {
               this.trackedCrystalSearchBox = new Box(spawnX - 0.6, spawnY - 0.4, spawnZ - 0.6, spawnX + 0.6, spawnY + 1.6, spawnZ + 0.6);
            }

            this.attackTrackedCrystalIfReady();
            Entity entity = CLIENT.world.getEntityById(entitySpawnS2CPacket.getEntityId());
            if (entity instanceof EndCrystalEntity) {
               Entity entity2 = (EndCrystalEntity)entity;
               if (this.isEntitySafeForPlacement(entity2)) {
                  this.isEntitySafeForPlacement(entity2);
               }
            }
         }

         Entity entity3 = CLIENT.world.getEntityById(entitySpawnS2CPacket.getEntityId());
         if (entity3 instanceof EndCrystalEntity) {
            Entity entity4 = (EndCrystalEntity)entity3;
            this.isProtectedResourceNearEntity(entity4);
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      if (CLIENT.player != null
         && CLIENT.world != null
         && CLIENT.interactionManager != null
         && this.pendingAttack != null) {
         this.requestRotation(rotationBetweenPoints(this.pendingAttack.position(), CLIENT.player.getEyePos()));
      } else {
         if (CLIENT.player != null && CLIENT.world != null && CLIENT.interactionManager != null) {
            this.explosionSafetyCache.clear();
            if (!CLIENT.player.isSpectator()) {
               this.attackTrackedCrystalIfReady();
               this.attackCrystalInTrackedArea();
               this.attackNearbyCrystals();
               LivingEntity livingEntity = this.findTarget();
               if (livingEntity == null) {
                  this.lastPlacementPosition = null;
                  this.targetInsideCobweb = false;
                  this.clearPlacementState();
                  this.pendingObsidianPosition = null;
                  this.tickInventorySwap();
               } else {
                  this.currentTarget = livingEntity;
                  this.targetInsideCobweb = CrystalAura.containsCobweb((Entity)livingEntity);
                  this.itemSwapCooldownTicks = 0;
                  this.attackCrystalsNearTarget(livingEntity);
                  if (this.pendingObsidianPosition != null) {
                     if (CLIENT.world.getBlockState(this.pendingObsidianPosition).isOf(Blocks.OBSIDIAN)
                        && this.isCrystalBaseUsable(this.pendingObsidianPosition, livingEntity)) {
                        BlockPos confirmedObsidianPosition = this.pendingObsidianPosition;
                        this.pendingObsidianPosition = null;
                        this.pendingObsidianTicksRemaining = 0;
                        this.tryPlaceCrystalAt(confirmedObsidianPosition, false);
                        return;
                     }

                     if (--this.pendingObsidianTicksRemaining > 0) {
                        return;
                     }

                     this.pendingObsidianPosition = null;
                  }

                  if (this.blockToMineForCrystal != null) {
                     this.mineBlockForCrystalPlacement(livingEntity);
                  } else if (CrystalAura.ItemSlotSwapState.containsItem(Items.END_CRYSTAL)) {
                     CrystalAura.CrystalPlacementCandidate placementCandidate = this.findBestPlacement(livingEntity);
                     if (placementCandidate != null) {
                        switch (placementCandidate.mode()) {
                           case CRYSTAL_ON_BASE:
                              this.tryPlaceCrystalAt(placementCandidate.basePosition(), true);
                              return;
                           case OBSIDIAN_REPLACEMENT:
                              if (this.placeObsidian.getValue()
                                 && CrystalAura.ItemSlotSwapState.containsItem(Items.OBSIDIAN)
                                 && placementCandidate.placementHit() != null) {
                                 this.beginObsidianPlacement(placementCandidate.basePosition(), placementCandidate.placementHit());
                                 return;
                              }
                              break;
                           case OBSIDIAN_SUPPORT:
                              if (this.mineForCrystalPlacement.getValue() && CrystalAura.ItemSlotSwapState.containsItem(Items.OBSIDIAN)) {
                                 this.blockToMineForCrystal = placementCandidate.basePosition();
                                 this.blockMiningTicksRemaining = 50;
                                 this.mineBlockForCrystalPlacement(livingEntity);
                                 return;
                              }
                        }
                     }
                  }
               }
            } else {
               this.itemSlotSwapState.restoreItemsAndSelectedSlot();
            }
         } else {
            this.clearLegacyInventorySwapSlots();
            this.itemSlotSwapState.clearState();
         }
      }
   }
// https://t.me/pasterplace

   private void restoreInventoryState() {
      if (CLIENT.player != null && CLIENT.interactionManager != null) {
         if (this.swappedInventorySlot != -1) {
            if (isInventoryScreenOpen()) {
               swapInventorySlots(this.swappedInventorySlot, this.swappedHotbarSlot);
            }

            this.swappedInventorySlot = -1;
            this.swappedHotbarSlot = -1;
         }

         if (this.originalSelectedSlot != -1) {
            selectHotbarSlot(this.originalSelectedSlot);
            this.originalSelectedSlot = -1;
         }
      } else {
         this.clearLegacyInventorySwapSlots();
      }
   }
// https://t.me/pasterplace

   private boolean isClearOfNearbyFriends(Vec3d explosionPosition) {
      if (CLIENT.world == null) {
         return true;
      }

      double minimumFriendDistance = Math.max(2.0, this.minimumSelfDistance.getValue().floatValue());

      for (PlayerEntity friend : CLIENT.world.getPlayers()) {
         if (friend != CLIENT.player
            && friend.isAlive()
            && FriendManager.getInstance().isFriend(friend)
            && friend.getEntityPos().distanceTo(explosionPosition) < minimumFriendDistance) {
            return false;
         }
      }

      return true;
   }
// https://t.me/pasterplace

   private int findHotbarSlot(Item item) {
      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         if (CLIENT.player.getInventory().getStack(slotIndex).isOf(item)) {
            return slotIndex;
         }
      }

      return -1;
   }
// https://t.me/pasterplace

   private int findBestMiningToolSlot(BlockState blockState) {
      int bestToolSlot = -1;
      float bestMiningSpeed = 1.0F;
// https://t.me/pasterplace

      for (int slotIndex = 0; slotIndex < 36; slotIndex++) {
         ItemStack candidateStack = CLIENT.player.getInventory().getStack(slotIndex);
         if (!candidateStack.isEmpty()) {
            float miningSpeed = candidateStack.getMiningSpeedMultiplier(blockState);
            if (miningSpeed > bestMiningSpeed) {
               bestMiningSpeed = miningSpeed;
               bestToolSlot = slotIndex;
            }
         }
      }

      return bestToolSlot;
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onRender3D(EventRender3D eventRender3D) {
      if (this.renderPlacementMarkers.getValue() && CLIENT.player != null && CLIENT.world != null) {
         for (CrystalAura.BlockUpdateMarker marker : this.placementRenderMarkers) {
            if (System.currentTimeMillis() - marker.updatedAtMillis() <= 1500L) {
               renderPlacementBox(new Box(marker.blockPosition()), 2029998200, 1.5F);
            } else {
               this.placementRenderMarkers.remove(marker);
            }
         }

         if (this.lastPlacementPosition != null) {
            renderPlacementBox(new Box(this.lastPlacementPosition), -1258346416, 2.0F);
         }

         if (this.blockToMineForCrystal != null) {
            renderPlacementBox(new Box(this.blockToMineForCrystal), -1761629662, 1.5F);
         }
      }
   }
// https://t.me/pasterplace

   private static void renderPlacementBox(Box box, int argbColor, float outlineWidth) {
      RgbaColor l1I1IlllIlI4 = RgbaColor.fromArgb(argbColor);
      DaamkyWorldRender.drawFilledBox(box, l1I1IlllIlI4.withAlpha((int)(l1I1IlllIlI4.alpha() * 0.25F)), true);
      DaamkyWorldRender.drawBoxOutline(box, l1I1IlllIlI4, outlineWidth, true);
   }
// https://t.me/pasterplace

   private Vec3d getTargetPositionWithPrediction(LivingEntity livingEntity) {
      if (this.targetInsideCobweb) {
         return livingEntity.getEntityPos();
      }
// https://t.me/pasterplace

      Vec3d targetVelocity = livingEntity.getVelocity();
      double horizontalSpeed = Math.sqrt(targetVelocity.x * targetVelocity.x + targetVelocity.z * targetVelocity.z);
      double predictionTicks = horizontalSpeed > 0.03 ? this.targetPredictionTicks.getValue().floatValue() : 0.0;
      return livingEntity.getEntityPos().add(targetVelocity.x * predictionTicks, 0.0, targetVelocity.z * predictionTicks);
   }
// https://t.me/pasterplace

   private void clearLegacyInventorySwapSlots() {
      this.originalSelectedSlot = -1;
      this.swappedInventorySlot = -1;
      this.swappedHotbarSlot = -1;
   }
// https://t.me/pasterplace

   private float calculateCrystalDamageScore(EndCrystalEntity endCrystalEntity, LivingEntity livingEntity) {
      float targetDamage = calculateExplosionDamage(endCrystalEntity.getEntityPos(), livingEntity);
      if (targetDamage < this.minimumTargetDamage.getValue()) {
         return Float.NEGATIVE_INFINITY;
      }

      float selfDamage = CLIENT.player != null && !CLIENT.player.isCreative()
         ? calculateExplosionDamage(endCrystalEntity.getEntityPos(), CLIENT.player)
         : 0.0F;
      return this.requireFavorableTrade.getValue() && targetDamage - selfDamage < this.minimumDamageAdvantage.getValue()
         ? Float.NEGATIVE_INFINITY
         : this.calculatePlacementScore(targetDamage, selfDamage, livingEntity);
   }
// https://t.me/pasterplace

   private LivingEntity findTarget() {
      Aura aura = (Aura)ModuleRegistry.getInstance().getModuleByClass(Aura.class);
      if (aura != null && aura.isEnabled()) {
         LivingEntity livingEntity = aura.getTarget();
         if (livingEntity != null && livingEntity.isAlive() && this.isValidTarget(livingEntity)) {
            return livingEntity;
         }
      }

      if (this.currentTarget != null && this.currentTarget.isAlive() && this.isTargetAllowedByArmorFilter(this.currentTarget)) {
         return this.currentTarget;
      }

      double maximumTargetRange = this.targetRange.getValue().floatValue();
      double maximumTargetDistanceSquared = maximumTargetRange * maximumTargetRange;
      LivingEntity nearestTarget = null;
      double nearestTargetDistanceSquared = Double.MAX_VALUE;

      for (PlayerEntity playerCandidate : CLIENT.world.getPlayers()) {
         LivingEntity livingEntity2 = this.findCloserTargetCandidate((PlayerEntity)playerCandidate, maximumTargetDistanceSquared, nearestTargetDistanceSquared);
         if (livingEntity2 != null) {
            nearestTargetDistanceSquared = CLIENT.player.squaredDistanceTo(livingEntity2);
            nearestTarget = livingEntity2;
         }
      }
// https://t.me/pasterplace

      return nearestTarget;
   }
// https://t.me/pasterplace

   private BlockPos getTargetGroundBlock(LivingEntity livingEntity) {
      Vec3d predictedTargetPosition = this.getTargetPositionWithPrediction(livingEntity);
      return BlockPos.ofFloored(predictedTargetPosition.x, livingEntity.getY() - 0.05, predictedTargetPosition.z);
   }
// https://t.me/pasterplace

   private boolean isPlacementSafeFromSelfAndFriends(BlockPos basePosition, LivingEntity livingEntity) {
      Vec3d crystalSpawnPosition = Vec3d.ofCenter(basePosition.up());
      if (this.protectedEntities.isOptionEnabled("Себя")) {
         double selfDistance = CLIENT.player.getEyePos().distanceTo(crystalSpawnPosition);
         if (selfDistance < this.minimumSelfDistance.getValue().floatValue()) {
            return false;
         }
      }

      return livingEntity == null || !this.protectedEntities.isOptionEnabled("Друзей") || this.isClearOfNearbyFriends(crystalSpawnPosition);
   }
// https://t.me/pasterplace

   private void mineBlockForCrystalPlacement(LivingEntity livingEntity) {
      if (this.blockToMineForCrystal == null) {
         this.clearPlacementState();
      } else if (--this.blockMiningTicksRemaining <= 0) {
         this.clearPlacementState();
      } else if (this.findExistingCrystalBase(livingEntity) != null) {
         this.clearPlacementState();
      } else {
         BlockState blockState = CLIENT.world.getBlockState(this.blockToMineForCrystal);
         if (!blockState.isAir() && !blockState.isReplaceable()) {
            this.requestRotation(rotationBetweenPoints(Vec3d.ofCenter(this.blockToMineForCrystal), CLIENT.player.getEyePos()));
            int bestToolSlot = this.findBestMiningToolSlot(blockState);
            if (bestToolSlot != -1) {
               this.itemSlotSwapState.selectInventoryItem(bestToolSlot);
            }

            CLIENT.interactionManager.updateBlockBreakingProgress(this.blockToMineForCrystal, Direction.UP);
            CLIENT.player.swingHand(Hand.MAIN_HAND);
         } else {
            BlockPos minedBlockPosition = this.blockToMineForCrystal;
            this.clearPlacementState();
            CrystalAura.CrystalPlacementHit l1lll11l1l = this.findReachablePlacementFace(minedBlockPosition);
            if (l1lll11l1l != null && this.isExplosionPointSafe(getCrystalSpawnPosition(minedBlockPosition), livingEntity)) {
               this.itemSlotSwapState.restoreItemsAndSelectedSlot();
               if (this.queueBlockInteraction(Items.OBSIDIAN, l1lll11l1l.hitResult())) {
                  this.pendingObsidianPosition = minedBlockPosition;
                  this.pendingObsidianTicksRemaining = 8;
                  this.lastPlacementPosition = minedBlockPosition;
                  this.addPlacementRenderMarker(minedBlockPosition);
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private CrystalAura.CrystalPlacementCandidate evaluatePlacementCandidate(BlockPos candidateBasePosition, LivingEntity livingEntity) {
      if (this.isWithinCrystalPlacementRange(candidateBasePosition) && this.isPlacementSafeFromSelfAndFriends(candidateBasePosition, livingEntity)) {
         BlockState blockState = CLIENT.world.getBlockState(candidateBasePosition);
         Vec3d explosionPosition = getCrystalSpawnPosition(candidateBasePosition);
         if (!blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.BEDROCK)) {
            if (blockState.isReplaceable()) {
               if (this.placeObsidian.getValue() && CrystalAura.ItemSlotSwapState.containsItem(Items.OBSIDIAN)) {
                  if (!this.canCreateObsidianSupport(candidateBasePosition, livingEntity)) {
                     return null;
                  }

                  CrystalAura.CrystalPlacementHit l1lll11l1l = this.findReachablePlacementFace(candidateBasePosition);
                  return l1lll11l1l != null && this.isExplosionPointSafe(explosionPosition, livingEntity)
                     ? this.createPlacementCandidate(CrystalAura.CrystalAuraMode.OBSIDIAN_REPLACEMENT, candidateBasePosition, explosionPosition, livingEntity, l1lll11l1l, 0.15F)
                     : null;
               }

               return null;
            }

            if (this.mineForCrystalPlacement.getValue() && CrystalAura.ItemSlotSwapState.containsItem(Items.OBSIDIAN)) {
               if (!blockState.isOf(Blocks.OBSIDIAN) && !blockState.isOf(Blocks.BEDROCK) && !blockState.isAir()) {
                  BlockState blockState2 = CLIENT.world.getBlockState(candidateBasePosition.up());
                  if (!blockState2.isAir() && !blockState2.isReplaceable()) {
                     return null;
                  }

                  boolean adjacentSolidBlockFound = false;

                  for (Direction direction : Direction.values()) {
                     if (this.isSolidBuildBlock(candidateBasePosition.offset(direction))) {
                        adjacentSolidBlockFound = true;
                        break;
                     }
                  }

                  if (!adjacentSolidBlockFound) {
                     return null;
                  }

                  return !this.isExplosionPointSafe(explosionPosition, livingEntity)
                     ? null
                     : this.createPlacementCandidate(CrystalAura.CrystalAuraMode.OBSIDIAN_SUPPORT, candidateBasePosition, explosionPosition, livingEntity, null, 0.75F);
               }

               return null;
            }

            return null;
         }

         if (this.isCrystalBaseUsable(candidateBasePosition, livingEntity) && !this.containsCrystalAtBase(candidateBasePosition)) {
            return !this.isExplosionPointSafe(explosionPosition, livingEntity)
               ? null
               : this.createPlacementCandidate(CrystalAura.CrystalAuraMode.CRYSTAL_ON_BASE, candidateBasePosition, explosionPosition, livingEntity, null, 0.0F);
         }
      }
// https://t.me/pasterplace

      return null;
   }
// https://t.me/pasterplace

   private void addPlacementRenderMarker(BlockPos markedPosition) {
      if (this.renderPlacementMarkers.getValue()) {
         this.placementRenderMarkers.add(new CrystalAura.BlockUpdateMarker(markedPosition, System.currentTimeMillis()));

         while (this.placementRenderMarkers.size() > 24) {
            this.placementRenderMarkers.poll();
         }
      }
   }
// https://t.me/pasterplace

   private void attackCrystalsNearTarget(LivingEntity livingEntity) {
      if (livingEntity != null && CLIENT.world != null) {
         EndCrystalEntity bestCrystal = null;
         float bestDamageScore = Float.NEGATIVE_INFINITY;

         for (EndCrystalEntity crystal : CLIENT.world
            .getEntitiesByClass(EndCrystalEntity.class, livingEntity.getBoundingBox().expand(4.5), crystal -> true)) {
            float damageScore = this.calculateCrystalDamageScore(crystal, livingEntity);
            if (damageScore > bestDamageScore) {
               bestDamageScore = damageScore;
               bestCrystal = crystal;
            }
         }

         if (bestCrystal != null) {
            this.queueCrystalAttack(bestCrystal);
         }
      }
   }
// https://t.me/pasterplace

   private void tryPlaceCrystalAt(BlockPos basePosition, boolean skipExplosionSafetyCheck) {
      if (this.isCrystalBaseUsable(basePosition, this.currentTarget)) {
         if (this.containsCrystalAtBase(basePosition)) {
            this.attackCrystalInTrackedArea();
         } else if (this.isWithinCrystalPlacementRange(basePosition)
            && this.isPlacementSafeFromSelfAndFriends(basePosition, this.currentTarget)
            && (skipExplosionSafetyCheck || this.isExplosionPointSafe(getCrystalSpawnPosition(basePosition), this.currentTarget))) {
            Vec3d crystalSpawnPosition = new Vec3d(basePosition.getX() + 0.5, basePosition.getY() + 1.0, basePosition.getZ() + 0.5);
            if (this.queueBlockInteraction(Items.END_CRYSTAL, new BlockHitResult(crystalSpawnPosition, Direction.UP, basePosition, false))) {
               this.trackCrystalSpawnNearBase(basePosition);
               this.lastPlacementPosition = basePosition;
               this.addPlacementRenderMarker(basePosition);
               this.attackCrystalInTrackedArea();
               this.attackCrystalsNearTarget(this.currentTarget);
            }
         }
      }
   }
// https://t.me/pasterplace

   private boolean canCreateObsidianSupport(BlockPos supportPosition, LivingEntity livingEntity) {
      BlockState blockState = CLIENT.world.getBlockState(supportPosition.up());
      if (!blockState.isAir() && !blockState.isReplaceable()) {
         return false;
      }

      Box box = new Box(
         supportPosition.getX(),
         supportPosition.getY() + 1.0,
         supportPosition.getZ(),
         supportPosition.getX() + 1.0,
         supportPosition.getY() + 3.0,
         supportPosition.getZ() + 1.0
      );
      return CLIENT.world
         .getOtherEntities(
            livingEntity, box, entity -> entity instanceof LivingEntity && entity != CLIENT.player && !(entity instanceof ArmorStandEntity)
         )
         .isEmpty();
   }
// https://t.me/pasterplace

   private void queueCrystalAttack(EndCrystalEntity endCrystalEntity) {
      if (this.pendingAttack == null && CLIENT.player != null) {
         Vec3d aimPosition = CLIENT.player.getEyePos().add(getEntityLookVector(endCrystalEntity));
         this.pendingAttack = new CrystalAura.CrystalAttackCandidate(endCrystalEntity, null, null, aimPosition, CLIENT.player.age);
         this.requestRotation(rotationBetweenPoints(aimPosition, CLIENT.player.getEyePos()));
      }

      this.clearTrackedCrystalSpawn();
   }
// https://t.me/pasterplace

   private boolean queueBlockInteraction(Item item, BlockHitResult blockHitResult) {
      if (this.pendingAttack == null && CLIENT.player != null && CrystalAura.ItemSlotSwapState.containsItem(item)) {
         Vec3d interactionPosition = blockHitResult.getPos();
         this.pendingAttack = new CrystalAura.CrystalAttackCandidate(null, item, blockHitResult, interactionPosition, CLIENT.player.age);
         this.requestRotation(rotationBetweenPoints(interactionPosition, CLIENT.player.getEyePos()));
         return true;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onTick(EventTick eventTick) {
      if (CLIENT.player != null && CLIENT.world != null && CLIENT.interactionManager != null) {
         CrystalAura.CrystalAttackCandidate attackCandidate = this.pendingAttack;
         if (attackCandidate != null && CLIENT.player.age - attackCandidate.ageTicks() > 6) {
            this.pendingAttack = null;
            attackCandidate = null;
         }

         if (attackCandidate != null && !CLIENT.player.isUsingItem() && this.executePendingAttack(attackCandidate)) {
            this.pendingAttack = null;
         } else {
            this.restoreInventoryState();
         }
      }
   }
// https://t.me/pasterplace

   private boolean executePendingAttack(CrystalAura.CrystalAttackCandidate attackCandidate) {
      Rotation lIll1Il1lll111l11l1I1llllI2 = RotationPriorityManager.getInstance().getLastMotionRotation();
      Vec3d playerEyePosition = CLIENT.player.getEyePos();
      Vec3d viewDirection = Vec3d.fromPolar(lIll1Il1lll111l11l1I1llllI2.getPitch(), lIll1Il1lll111l11l1I1llllI2.getYaw());
      if (attackCandidate.crystal() != null) {
         EndCrystalEntity endCrystalEntity = attackCandidate.crystal();
         if (endCrystalEntity.isAlive() && !endCrystalEntity.isRemoved()) {
            double entityInteractionRange = CLIENT.player.getEntityInteractionRange();
            if (endCrystalEntity.getBoundingBox().raycast(playerEyePosition, playerEyePosition.add(viewDirection.multiply(entityInteractionRange))).isEmpty()) {
               return false;
            }

            CLIENT.interactionManager.attackEntity(CLIENT.player, endCrystalEntity);
            CLIENT.player.swingHand(Hand.MAIN_HAND);
            return true;
         } else {
            this.pendingAttack = null;
            return false;
         }
      } else {
         BlockHitResult blockHitResult = attackCandidate.blockHit();
         double blockInteractionRange = CLIENT.player.getBlockInteractionRange();
         BlockHitResult blockHitResult2 = CLIENT.world
            .raycast(
               new RaycastContext(
                  playerEyePosition, playerEyePosition.add(viewDirection.multiply(blockInteractionRange)), ShapeType.OUTLINE, FluidHandling.NONE, CLIENT.player
               )
            );
         if (blockHitResult2.getType() != Type.BLOCK || !blockHitResult2.getBlockPos().equals(blockHitResult.getBlockPos())) {
            return false;
         } else if (attackCandidate.heldItem() != Items.END_CRYSTAL && blockHitResult2.getSide() != blockHitResult.getSide()) {
            return false;
         } else if (!this.ensureItemSelected(attackCandidate.heldItem())) {
            this.pendingAttack = null;
            return false;
         } else {
            CLIENT.interactionManager.interactBlock(CLIENT.player, Hand.MAIN_HAND, blockHitResult2);
            CLIENT.player.swingHand(Hand.MAIN_HAND);
            return true;
         }
      }
   }
// https://t.me/pasterplace

   private boolean isExplosionPointSafe(Vec3d explosionPosition, LivingEntity livingEntity) {
      if (!this.enableSmartSafety.getValue()) {
         return true;
      }

      if (CLIENT.player == null || CLIENT.world == null) {
         return false;
      }

      if (CLIENT.player.isCreative()) {
         return true;
      }

      long cacheKey = BlockPos.ofFloored(explosionPosition).asLong();
      Boolean visible = this.explosionSafetyCache.get(cacheKey);
      if (visible != null) {
         return visible;
      }

      boolean explosionIsSafe = this.isExplosionDamageTradeAcceptable(explosionPosition, livingEntity);
      if (this.explosionSafetyCache.size() < 64) {
         this.explosionSafetyCache.put(cacheKey, explosionIsSafe);
      }

      return explosionIsSafe;
   }
// https://t.me/pasterplace

   private CrystalAura.CrystalPlacementHit findReachablePlacementFace(BlockPos targetBlock) {
      CrystalAura.CrystalPlacementHit bestPlacementHit = null;
      double bestDistanceSquared = Double.MAX_VALUE;
      Vec3d playerEyePosition = CLIENT.player.getEyePos();
      Direction[] searchDirections = new Direction[]{
         Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP
      };
// https://t.me/pasterplace

      for (Direction faceDirection : searchDirections) {
         BlockPos neighborBlock = targetBlock.offset(faceDirection.getOpposite());
         if (this.isSolidBuildBlock(neighborBlock)) {
            Vec3d hitPosition = Vec3d.ofCenter(neighborBlock).add(Vec3d.of(faceDirection.getVector()).multiply(0.5));
            if (!(playerEyePosition.subtract(hitPosition).dotProduct(Vec3d.of(faceDirection.getVector())) <= 0.0)) {
               double distanceSquared = playerEyePosition.squaredDistanceTo(hitPosition);
               if (distanceSquared < bestDistanceSquared) {
                  bestDistanceSquared = distanceSquared;
                  bestPlacementHit = new CrystalAura.CrystalPlacementHit(new BlockHitResult(hitPosition, faceDirection, neighborBlock, false), hitPosition);
               }
            }
         }
      }

      return bestPlacementHit;
   }
// https://t.me/pasterplace

   private boolean isEntitySafeForPlacement(Entity entity) {
      if (entity == null || CLIENT.player == null) {
         return false;
      }

      if (this.isProtectedResourceNearEntity(entity)) {
         return false;
      }

      if (this.protectedEntities.isOptionEnabled("Себя")) {
         double selfDistance = CLIENT.player.getEyePos().distanceTo(entity.getBoundingBox().getCenter());
         if (selfDistance < this.minimumSelfDistance.getValue().floatValue()) {
            return false;
         }
      }

      return this.protectedEntities.isOptionEnabled("Друзей") && !this.isClearOfNearbyFriends(entity.getBoundingBox().getCenter())
         ? false
         : this.isExplosionPointSafe(entity.getEntityPos(), this.currentTarget);
   }
// https://t.me/pasterplace

   private boolean isValidTarget(LivingEntity entity) {
      return entity instanceof PlayerEntity playerTarget && playerTarget != CLIENT.player && playerTarget.isAlive()
         ? !FriendManager.getInstance().isFriend(playerTarget) && !AntiBot.II1Il11l111II11IIl(playerTarget)
         : false;
   }
// https://t.me/pasterplace

   private static boolean isInventoryScreenOpen() {
      return CLIENT.player != null && CLIENT.player.currentScreenHandler == CLIENT.player.playerScreenHandler;
   }
// https://t.me/pasterplace

   private void attackTrackedCrystalIfReady() {
      if (this.trackedCrystalTicksRemaining > 0 && CLIENT.world != null) {
         this.trackedCrystalTicksRemaining--;
         if ((this.trackedCrystalEntityId == -1 ? null : CLIENT.world.getEntityById(this.trackedCrystalEntityId)) instanceof EndCrystalEntity trackedCrystal
            && this.isEntitySafeForPlacement((Entity)trackedCrystal)) {
            this.queueCrystalAttack(trackedCrystal);
         } else if (this.trackedCrystalPosition != null) {
            Box box = new Box(
               this.trackedCrystalPosition.x - 1.2,
               this.trackedCrystalPosition.y - 0.8,
               this.trackedCrystalPosition.z - 1.2,
               this.trackedCrystalPosition.x + 1.2,
               this.trackedCrystalPosition.y + 2.0,
               this.trackedCrystalPosition.z + 1.2
            );

            for (EndCrystalEntity crystal : CLIENT.world.getEntitiesByClass(EndCrystalEntity.class, box, endCrystalEntity -> true)) {
               if (this.isEntitySafeForPlacement(crystal)) {
                  this.queueCrystalAttack(crystal);
                  return;
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private boolean isTargetAllowedByArmorFilter(LivingEntity entity) {
      if (!this.isValidTarget(entity)) {
         return false;
      }

      PlayerEntity playerTarget = (PlayerEntity)entity;
      boolean isUnarmored = true;

      for (EquipmentSlot equipmentSlot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
         if (!playerTarget.getEquippedStack(equipmentSlot).isEmpty()) {
            isUnarmored = false;
            break;
         }
      }

      boolean includeArmoredPlayers = this.targetCategories.isOptionEnabled("Игроки");
      boolean includeUnarmoredPlayers = this.targetCategories.isOptionEnabled("Голые");
      return includeArmoredPlayers || includeUnarmoredPlayers && isUnarmored;
   }
// https://t.me/pasterplace

   private boolean ensureItemSelected(Item item) {
      if (CLIENT.player != null && CLIENT.interactionManager != null) {
         if (CLIENT.player.getMainHandStack().isOf(item)) {
            return true;
         }

         int previousHotbarSlot = CLIENT.player.getInventory().getSelectedSlot();
         int itemHotbarSlot = this.findHotbarSlot(item);
         if (itemHotbarSlot != -1) {
            CLIENT.player.getInventory().setSelectedSlot(itemHotbarSlot);
            syncSelectedHotbarSlot();
            if (this.originalSelectedSlot == -1) {
               this.originalSelectedSlot = previousHotbarSlot;
            }

            return true;
         }

         int itemInventorySlot = this.findInventorySlot(item);
         if (itemInventorySlot != -1 && isInventoryScreenOpen()) {
            int temporaryHotbarSlot = this.findAvailableHotbarSlot();
            CLIENT.player.getInventory().setSelectedSlot(temporaryHotbarSlot);
            syncSelectedHotbarSlot();
            swapInventorySlots(itemInventorySlot, temporaryHotbarSlot);
            this.swappedInventorySlot = itemInventorySlot;
            this.swappedHotbarSlot = temporaryHotbarSlot;
            if (this.originalSelectedSlot == -1) {
               this.originalSelectedSlot = previousHotbarSlot;
            }

            return true;
         }
      }

      return false;
   }
// https://t.me/pasterplace

   private void trackNearbyCrystal(EndCrystalEntity endCrystalEntity) {
      boolean insideTrackedSearchBox = this.trackedCrystalSearchBox != null
         && this.trackedCrystalSearchBox.expand(1.0).contains(endCrystalEntity.getX(), endCrystalEntity.getY(), endCrystalEntity.getZ());
      boolean nearCurrentTarget = this.currentTarget != null && endCrystalEntity.squaredDistanceTo(this.currentTarget) <= 16.0;
      if ((insideTrackedSearchBox || nearCurrentTarget) && this.isEntitySafeForPlacement((Entity)endCrystalEntity)) {
         this.trackedCrystalEntityId = endCrystalEntity.getId();
         this.trackedCrystalPosition = endCrystalEntity.getEntityPos();
         this.trackedCrystalTicksRemaining = 12;
         this.queueCrystalAttack(endCrystalEntity);
      }
   }
// https://t.me/pasterplace

   private boolean containsCrystalAtBase(BlockPos basePosition) {
      Box box = new Box(
         basePosition.getX(),
         basePosition.getY() + 1.0,
         basePosition.getZ(),
         basePosition.getX() + 1.0,
         basePosition.getY() + 2.0,
         basePosition.getZ() + 1.0
      );
      return !CLIENT.world.getEntitiesByClass(EndCrystalEntity.class, box, endCrystalEntity -> true).isEmpty();
   }
// https://t.me/pasterplace

   private boolean isExplosionDamageTradeAcceptable(Vec3d explosionPosition, LivingEntity livingEntity) {
      float targetDamage = calculateExplosionDamage(explosionPosition, CLIENT.player);
      if (targetDamage > this.maximumSelfDamage.getValue()) {
         return false;
      } else if (getHealthAfterDamage(CLIENT.player, targetDamage) < this.minimumHealthAfterExplosion.getValue()) {
         return false;
      } else if (this.requireFavorableTrade.getValue() && livingEntity != null && livingEntity != CLIENT.player) {
         float selfDamage = calculateExplosionDamage(explosionPosition, livingEntity);
         return selfDamage - targetDamage >= this.minimumDamageAdvantage.getValue();
      } else {
         return true;
      }
   }
// https://t.me/pasterplace

   private int findAvailableHotbarSlot() {
      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         if (CLIENT.player.getInventory().getStack(slotIndex).isEmpty()) {
            return slotIndex;
         }
      }

      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         Item item = CLIENT.player.getInventory().getStack(slotIndex).getItem();
         if (item != Items.END_CRYSTAL && item != Items.OBSIDIAN && item != Items.COBWEB && item != Items.TOTEM_OF_UNDYING && item != Items.ENDER_PEARL) {
            return slotIndex;
         }
      }

      return CLIENT.player.getInventory().getSelectedSlot();
   }
// https://t.me/pasterplace

   private BlockPos getTargetFootBlock(LivingEntity livingEntity) {
      return BlockPos.ofFloored(livingEntity.getX(), livingEntity.getY() - 0.05, livingEntity.getZ());
   }
// https://t.me/pasterplace

   private CrystalAura.CrystalPlacementCandidate createPlacementCandidate(
      CrystalAura.CrystalAuraMode lIIl11l111lIIl1ll,
      BlockPos basePosition,
      Vec3d explosionPosition,
      LivingEntity livingEntity,
      CrystalAura.CrystalPlacementHit l1lll11l1l,
      float placementPenalty
   ) {
      float targetDamage = calculateExplosionDamage(explosionPosition, livingEntity);
      if (targetDamage < this.minimumTargetDamage.getValue()) {
         return null;
      }

      float selfDamage = CLIENT.player != null && !CLIENT.player.isCreative()
         ? calculateExplosionDamage(explosionPosition, CLIENT.player)
         : 0.0F;
      float netDamage = targetDamage - selfDamage;
      if (this.requireFavorableTrade.getValue() && netDamage < this.minimumDamageAdvantage.getValue()) {
         return null;
      }
// https://t.me/pasterplace

      float placementScore = this.calculatePlacementScore(targetDamage, selfDamage, livingEntity) - placementPenalty;
      return new CrystalAura.CrystalPlacementCandidate(lIIl11l111lIIl1ll, basePosition, targetDamage, selfDamage, netDamage, placementScore, l1lll11l1l);
   }
// https://t.me/pasterplace

   private void attackNearbyCrystals() {
      if (CLIENT.world != null && CLIENT.player != null) {
         double searchRange = this.placementRange.getValue().floatValue();
         EndCrystalEntity bestCrystal = null;
         float bestCrystalScore = Float.NEGATIVE_INFINITY;

         for (EndCrystalEntity crystal : CLIENT.world
            .getEntitiesByClass(EndCrystalEntity.class, CLIENT.player.getBoundingBox().expand(searchRange), crystal -> true)) {
            if (this.trackedCrystalSearchBox != null || this.currentTarget != null && !(crystal.squaredDistanceTo(this.currentTarget) > 16.0)) {
               float crystalScore = this.currentTarget != null
                  ? this.calculateCrystalDamageScore(crystal, this.currentTarget)
                  : (float)(100.0 - crystal.squaredDistanceTo(CLIENT.player));
               if (crystalScore > bestCrystalScore) {
                  bestCrystalScore = crystalScore;
                  bestCrystal = crystal;
               }
            }
         }

         if (bestCrystal != null) {
            this.queueCrystalAttack(bestCrystal);
         }
      }
   }
// https://t.me/pasterplace

   private LivingEntity findCloserTargetCandidate(PlayerEntity entity, double maximumDistanceSquared, double nearestDistanceSquared) {
      if (!this.isTargetAllowedByArmorFilter(entity)) {
         return null;
      }

      double candidateDistanceSquared = CLIENT.player.squaredDistanceTo(entity);
      return !(candidateDistanceSquared > maximumDistanceSquared) && !(candidateDistanceSquared >= nearestDistanceSquared) ? entity : null;
   }
// https://t.me/pasterplace

   private CrystalAura.CrystalPlacementCandidate findBestPlacement(LivingEntity livingEntity) {
      BlockPos targetFootBlock = this.getTargetFootBlock(livingEntity);
      BlockPos predictedTargetBlock = this.getTargetGroundBlock(livingEntity);
      CrystalAura.CrystalPlacementCandidate bestCandidate = null;

      for (BlockPos searchOrigin : new BlockPos[]{predictedTargetBlock, targetFootBlock}) {
         for (int xOffset = -4; xOffset <= 4; xOffset++) {
            for (int yOffset = -4; yOffset <= 4; yOffset++) {
               for (int zOffset = -2; zOffset <= 1; zOffset++) {
                  BlockPos candidatePosition = searchOrigin.add(xOffset, zOffset, yOffset);
                  if (!candidatePosition.equals(targetFootBlock)) {
                     CrystalAura.CrystalPlacementCandidate candidate = this.evaluatePlacementCandidate(candidatePosition, livingEntity);
                     if (candidate != null && (bestCandidate == null || this.isBetterPlacementCandidate(candidate, bestCandidate))) {
                        bestCandidate = candidate;
                     }
                  }
               }
            }
         }
      }

      return bestCandidate;
   }
// https://t.me/pasterplace

   private boolean isWithinCrystalPlacementRange(BlockPos basePosition) {
      return CLIENT.player.getEyePos().distanceTo(Vec3d.ofCenter(basePosition)) <= this.placementRange.getValue().floatValue();
   }
// https://t.me/pasterplace

   private float calculatePlacementScore(float targetDamage, float selfDamage, LivingEntity livingEntity) {
      float score = targetDamage * 3.0F - selfDamage * 2.5F;
      score += targetDamage - selfDamage;
      float targetRemainingHealth = livingEntity.getHealth() + livingEntity.getAbsorptionAmount();
      if (targetDamage >= targetRemainingHealth) {
         score += 100.0F;
         if (selfDamage <= this.maximumSelfDamage.getValue() * 0.5F) {
            score += 25.0F;
         }
      }

      return score;
   }
// https://t.me/pasterplace

   private void clearPlacementState() {
      this.blockToMineForCrystal = null;
      this.blockMiningTicksRemaining = 0;
   }
// https://t.me/pasterplace

   private boolean isProtectedResourceNearEntity(Entity entity) {
      return this.protectedEntities.isOptionEnabled("Ресурсы")
         && CLIENT.world != null
         && CLIENT.world
            .getEntitiesByClass(ItemEntity.class, entity.getBoundingBox().expand(5.0), itemEntity -> true)
            .stream()
            .map(itemEntity -> itemEntity.getStack().getItem())
            .anyMatch(
               item -> item == Items.TOTEM_OF_UNDYING
                  || item == Items.ENCHANTED_GOLDEN_APPLE
                  || item == Items.NETHERITE_HELMET
                  || item == Items.NETHERITE_CHESTPLATE
                  || item == Items.NETHERITE_LEGGINGS
                  || item == Items.NETHERITE_BOOTS
                  || item == Items.NETHERITE_SWORD
                  || item == Items.DIAMOND_SWORD
                  || item == Items.ELYTRA
                  || item == Items.TRIDENT
            );
   }
// https://t.me/pasterplace

   private void requestRotation(Rotation lIll1Il1lll111l11l1I1llllI2) {
      RotationPriorityManager.getInstance().requestRotation(lIll1Il1lll111l11l1I1llllI2, FocusMode.FREE, 40.0F, 15, false, this);
   }
// https://t.me/pasterplace

   private static Rotation rotationBetweenPoints(Vec3d origin, Vec3d target) {
      Vec3d direction = origin.subtract(target);
      float yaw = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(direction.y, Math.hypot(direction.x, direction.z))));
      return new Rotation(yaw, MathHelper.clamp(pitch, -90.0F, 90.0F));
   }
// https://t.me/pasterplace

   private static Vec3d getEntityLookVector(Entity entity) {
      Vec3d playerEyePosition = CLIENT.player.getEyePos();
      double clampedTargetY = MathHelper.lerp(
         MathHelper.clamp(playerEyePosition.distanceTo(entity.getEntityPos().add(0.0, entity.getEyeHeight(entity.getPose()), 0.0)) / 3.0, 0.0, 1.0),
         entity.getBoundingBox().minY,
         MathHelper.clamp(playerEyePosition.y, entity.getBoundingBox().minY, entity.getBoundingBox().maxY)
      );
      return new Vec3d(entity.getX(), clampedTargetY, entity.getZ()).subtract(playerEyePosition);
   }
// https://t.me/pasterplace

   private static boolean containsCobweb(Entity entity) {
      if (entity != null && CLIENT.world != null) {
         Box box = entity.getBoundingBox();
         double minX = box.minX + 0.001;
         double minZ = box.minZ + 0.001;
         double maxX = box.maxX - 0.001;
         double maxZ = box.maxZ - 0.001;
         double entityHeight = box.maxY - box.minY;
         double sampleBandHeight = entityHeight * 0.119;
         double lowerSampleY = box.minY + sampleBandHeight / 2.0;
         double upperSampleY = box.maxY - sampleBandHeight / 2.0;
         double xSampleStep = (maxX - minX) / 4.0;
         double zSampleStep = (maxZ - minZ) / 4.0;
         double ySampleStep = (upperSampleY - lowerSampleY) / 8.0;

         for (double sampleX = minX; sampleX <= maxX + 1.0E-4; sampleX += xSampleStep) {
            for (double sampleY = lowerSampleY; sampleY <= upperSampleY + 1.0E-4; sampleY += ySampleStep) {
               for (double sampleZ = minZ; sampleZ <= maxZ + 1.0E-4; sampleZ += zSampleStep) {
                  if (CLIENT.world.getBlockState(BlockPos.ofFloored(sampleX, sampleY, sampleZ)).isOf(Blocks.COBWEB)) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   private static void swapInventorySlots(int inventorySlot, int hotbarButton) {
      if (CLIENT.player != null && CLIENT.interactionManager != null) {
         CLIENT.interactionManager
            .clickSlot(CLIENT.player.playerScreenHandler.syncId, inventorySlot, hotbarButton, SlotActionType.SWAP, CLIENT.player);
      }
   }
// https://t.me/pasterplace

   private static void selectHotbarSlot(int hotbarSlot) {
      if (CLIENT.player != null && hotbarSlot >= 0 && hotbarSlot <= 8 && CLIENT.player.getInventory().getSelectedSlot() != hotbarSlot) {
         CLIENT.player.getInventory().setSelectedSlot(hotbarSlot);
         syncSelectedHotbarSlot();
      }
   }
// https://t.me/pasterplace

   private static void syncSelectedHotbarSlot() {
      if (CLIENT.interactionManager != null) {
         ((MultiPlayerGameModeAccessor)CLIENT.interactionManager).daamky$syncSelectedSlot();
      }
   }
// https://t.me/pasterplace

   private static Vec3d getCrystalSpawnPosition(BlockPos basePosition) {
      return new Vec3d(basePosition.getX() + 0.5, basePosition.getY() + 1.0, basePosition.getZ() + 0.5);
   }
// https://t.me/pasterplace

   private static float getHealthAfterDamage(LivingEntity livingEntity, float damage) {
      return livingEntity == null ? 0.0F : livingEntity.getHealth() + livingEntity.getAbsorptionAmount() - damage;
   }
// https://t.me/pasterplace

   private static float calculateExplosionDamage(Vec3d explosionPosition, LivingEntity livingEntity) {
      if (explosionPosition != null && livingEntity != null && CLIENT.world != null) {
         double distanceRatio = Math.sqrt(livingEntity.squaredDistanceTo(explosionPosition)) / 12.0;
         if (distanceRatio > 1.0) {
            return 0.0F;
         }
// https://t.me/pasterplace

         float explosionExposure = calculateExplosionExposure(explosionPosition, livingEntity);
         if (explosionExposure <= 0.0F) {
            return 0.0F;
         }
// https://t.me/pasterplace

         double exposureAdjustedImpact = (1.0 - distanceRatio) * explosionExposure;
         float explosionDamage = (float)((exposureAdjustedImpact * exposureAdjustedImpact + exposureAdjustedImpact) / 2.0 * 7.0 * 12.0 + 1.0);
         explosionDamage = applyDifficultyDamageMultiplier(explosionDamage);
         explosionDamage = applyArmorDamageReduction(explosionDamage, livingEntity);
         explosionDamage *= getArmorDamageMultiplier(livingEntity);
         explosionDamage *= getResistanceDamageMultiplier(livingEntity);
         return Math.max(0.0F, explosionDamage);
      } else {
         return 0.0F;
      }
   }
// https://t.me/pasterplace

   private static float calculateExplosionExposure(Vec3d explosionPosition, LivingEntity livingEntity) {
      Box box = livingEntity.getBoundingBox();
      double xSampleStep = 1.0 / (box.getLengthX() * 2.0 + 1.0);
      double ySampleStep = 1.0 / (box.getLengthY() * 2.0 + 1.0);
      double zSampleStep = 1.0 / (box.getLengthZ() * 2.0 + 1.0);
      if (!(xSampleStep <= 0.0) && !(ySampleStep <= 0.0) && !(zSampleStep <= 0.0)) {
         double xSampleOffset = (1.0 - Math.floor(1.0 / xSampleStep) * xSampleStep) / 2.0;
         double zSampleOffset = (1.0 - Math.floor(1.0 / zSampleStep) * zSampleStep) / 2.0;
         int visibleSampleCount = 0;
         int totalSampleCount = 0;

         for (double sampleX = 0.0; sampleX <= 1.0; sampleX += xSampleStep) {
            for (double sampleY = 0.0; sampleY <= 1.0; sampleY += ySampleStep) {
               for (double sampleZ = 0.0; sampleZ <= 1.0; sampleZ += zSampleStep) {
                  Vec3d entitySamplePosition = new Vec3d(
                     MathHelper.lerp(sampleX, box.minX, box.maxX) + xSampleOffset,
                     MathHelper.lerp(sampleY, box.minY, box.maxY),
                     MathHelper.lerp(sampleZ, box.minZ, box.maxZ) + zSampleOffset
                  );
                  BlockHitResult blockHitResult = CLIENT.world
                     .raycast(new RaycastContext(entitySamplePosition, explosionPosition, ShapeType.COLLIDER, FluidHandling.NONE, livingEntity));
                  if (blockHitResult.getType() == Type.MISS) {
                     visibleSampleCount++;
                  }

                  totalSampleCount++;
               }
            }
         }

         return totalSampleCount == 0 ? 0.0F : (float)visibleSampleCount / totalSampleCount;
      } else {
         return 0.0F;
      }
   }
// https://t.me/pasterplace

   private static float getArmorDamageMultiplier(LivingEntity livingEntity) {
      if (!(livingEntity instanceof PlayerEntity playerTarget)) {
         return 1.0F;
      } else {
         float protectionPoints = 0.0F;

         for (EquipmentSlot armorSlot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armorStack = playerTarget.getEquippedStack(armorSlot);
            protectionPoints += getEnchantmentLevel(armorStack, "protection");
            protectionPoints += getEnchantmentLevel(armorStack, "blast_protection") * 2.0F;
         }

         protectionPoints = Math.min(protectionPoints, 20.0F);
         return 1.0F - protectionPoints / 25.0F;
      }
   }
// https://t.me/pasterplace

   private static int getEnchantmentLevel(ItemStack enchantedStack, String enchantmentId) {
      if (enchantedStack != null && !enchantedStack.isEmpty()) {
         ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.getEnchantments(enchantedStack);

         for (RegistryEntry<Enchantment> enchantmentEntry : itemEnchantmentsComponent.getEnchantments()) {
            String currentEnchantmentId = enchantmentEntry.getKey().map(registryKey -> registryKey.getValue().getPath()).orElse("");
            if (enchantmentId.equals(currentEnchantmentId)) {
               return itemEnchantmentsComponent.getLevel(enchantmentEntry);
            }
         }

         return 0;
      } else {
         return 0;
      }
   }
// https://t.me/pasterplace

   private static float getResistanceDamageMultiplier(LivingEntity livingEntity) {
      StatusEffectInstance statusEffectInstance = livingEntity.getStatusEffect(StatusEffects.RESISTANCE);
      if (statusEffectInstance == null) {
         return 1.0F;
      }

      int resistanceReductionPoints = 5 * (statusEffectInstance.getAmplifier() + 1);
      return Math.max(0.0F, (25.0F - resistanceReductionPoints) / 25.0F);
   }
// https://t.me/pasterplace

   private static float applyArmorDamageReduction(float damage, LivingEntity livingEntity) {
      float armor = livingEntity.getArmor();
      float armorToughness = (float)livingEntity.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS);
      float armorDivisor = 2.0F + armorToughness / 4.0F;
      float effectiveArmor = MathHelper.clamp(armor - damage / armorDivisor, armor * 0.2F, 20.0F);
      return damage * (1.0F - effectiveArmor / 25.0F);
   }
// https://t.me/pasterplace

   private static float applyDifficultyDamageMultiplier(float baseDamage) {
      return switch (CLIENT.world.getDifficulty()) {
         case PEACEFUL -> 0.0F;
         case EASY -> Math.min(baseDamage / 2.0F + 1.0F, baseDamage);
         case HARD -> baseDamage * 3.0F / 2.0F;
         default -> baseDamage;
      };
   }
// https://t.me/pasterplace

   private record CrystalPlacementCandidate(CrystalAura.CrystalAuraMode mode, BlockPos basePosition, float targetDamage, float selfDamage, float netDamage, float placementScore, CrystalAura.CrystalPlacementHit placementHit) {

   }
// https://t.me/pasterplace

   private record CrystalAttackCandidate(EndCrystalEntity crystal, Item heldItem, BlockHitResult blockHit, Vec3d position, int ageTicks) {

      private CrystalAttackCandidate(EndCrystalEntity crystal, Item heldItem, BlockHitResult blockHit, Vec3d position, int ageTicks) {
         this.crystal = crystal;
         this.heldItem = heldItem;
         this.blockHit = blockHit;
         this.position = position;
         this.ageTicks = ageTicks;
      }
   }
// https://t.me/pasterplace

   private record BlockUpdateMarker(BlockPos blockPosition, long updatedAtMillis) {

      private BlockUpdateMarker(BlockPos blockPosition, long updatedAtMillis) {
         this.blockPosition = blockPosition;
         this.updatedAtMillis = updatedAtMillis;
      }
   }
// https://t.me/pasterplace

   private record CrystalPlacementHit(BlockHitResult hitResult, Vec3d position) {

      private CrystalPlacementHit(BlockHitResult hitResult, Vec3d position) {
         this.hitResult = hitResult;
         this.position = position;
      }
   }
// https://t.me/pasterplace

   private enum CrystalAuraMode {
      CRYSTAL_ON_BASE,
      OBSIDIAN_REPLACEMENT,
      OBSIDIAN_SUPPORT;

      // $VF: synthetic method
      private static CrystalAura.CrystalAuraMode[] createModeValues() {
         return new CrystalAura.CrystalAuraMode[]{CRYSTAL_ON_BASE, OBSIDIAN_REPLACEMENT, OBSIDIAN_SUPPORT};
      }
   }
// https://t.me/pasterplace

   private static final class ItemSlotSwapState {
      private int swappedInventorySlot = -1;
      private int originalSelectedSlot = -1;
      private int swapHotbarSlot = -1;
      private int lastSelectedHotbarSlot = -1;

      private void restoreSwappedInventoryItem() {
         if (this.swappedInventorySlot != -1) {
            if (CrystalAura.CLIENT.player != null && CrystalAura.isInventoryScreenOpen()) {
               CrystalAura.swapInventorySlots(this.swappedInventorySlot, this.swapHotbarSlot);
            }

            this.swappedInventorySlot = -1;
            this.swapHotbarSlot = -1;
         }
      }

      boolean hasPendingSwap() {
         return this.originalSelectedSlot != -1 || this.swappedInventorySlot != -1;
      }

      static int findInventorySlot(Item item) {
         if (CrystalAura.CLIENT.player != null && item != null) {
            for (int slotIndex = 0; slotIndex < 36; slotIndex++) {
               if (CrystalAura.CLIENT.player.getInventory().getStack(slotIndex).isOf(item)) {
                  return slotIndex;
               }
            }
// https://t.me/pasterplace

            return -1;
         } else {
            return -1;
         }
      }

      static boolean containsItem(Item item) {
         return findInventorySlot(item) != -1;
      }

      private int findReplaceableHotbarSlot() {
         for (int emptyHotbarSlot = 0; emptyHotbarSlot < 9; emptyHotbarSlot++) {
            if (CrystalAura.CLIENT.player.getInventory().getStack(emptyHotbarSlot).isEmpty()) {
               return emptyHotbarSlot;
            }
         }

         for (int replaceableHotbarSlot = 0; replaceableHotbarSlot < 9; replaceableHotbarSlot++) {
            if (!isProtectedHotbarItem(CrystalAura.CLIENT.player.getInventory().getStack(replaceableHotbarSlot))) {
               return replaceableHotbarSlot;
            }
         }

         return CrystalAura.CLIENT.player.getInventory().getSelectedSlot();
      }

      void clearState() {
         this.originalSelectedSlot = -1;
         this.swappedInventorySlot = -1;
         this.swapHotbarSlot = -1;
         this.lastSelectedHotbarSlot = -1;
      }

      void restoreItemsAndSelectedSlot() {
         this.restoreSwappedInventoryItem();
         if (this.originalSelectedSlot != -1) {
            if (CrystalAura.CLIENT.player != null && CrystalAura.CLIENT.player.getInventory().getSelectedSlot() == this.lastSelectedHotbarSlot
               )
             {
// https://t.me/pasterplace
               CrystalAura.selectHotbarSlot(this.originalSelectedSlot);
            }

            this.originalSelectedSlot = -1;
         }

         this.lastSelectedHotbarSlot = -1;
      }

      private static boolean isProtectedHotbarItem(ItemStack candidateStack) {
         if (candidateStack.isEmpty()) {
            return false;
         }

         Item item = candidateStack.getItem();
         return item != Items.END_CRYSTAL
               && item != Items.OBSIDIAN
               && item != Items.COBWEB
               && item != Items.TOTEM_OF_UNDYING
               && item != Items.ENDER_PEARL
               && item != Items.ENCHANTED_GOLDEN_APPLE
               && item != Items.EXPERIENCE_BOTTLE
            ? candidateStack.isIn(ItemTags.SWORDS)
               || item instanceof AxeItem
               || candidateStack.isIn(ItemTags.PICKAXES)
               || candidateStack.isIn(ItemTags.SHOVELS)
               || candidateStack.isIn(ItemTags.HOES)
            : true;
      }

      private void selectHotbarSlotAndTrack(int hotbarSlot) {
         CrystalAura.selectHotbarSlot(hotbarSlot);
         this.lastSelectedHotbarSlot = hotbarSlot;
      }
// https://t.me/pasterplace

      private void rememberOriginalSelectedSlot() {
         if (this.originalSelectedSlot == -1 && CrystalAura.CLIENT.player != null) {
            this.originalSelectedSlot = CrystalAura.CLIENT.player.getInventory().getSelectedSlot();
         }
      }

      boolean selectInventoryItem(int requestedSlot) {
         if (CrystalAura.CLIENT.player == null || requestedSlot < 0 || requestedSlot > 35) {
            return false;
         }

         if (requestedSlot < 9) {
            if (this.swapHotbarSlot != requestedSlot) {
               this.restoreSwappedInventoryItem();
            }

            this.rememberOriginalSelectedSlot();
            this.selectHotbarSlotAndTrack(requestedSlot);
            return true;
         } else if (!CrystalAura.isInventoryScreenOpen()) {
            return false;
         } else if (this.swappedInventorySlot == requestedSlot) {
            this.selectHotbarSlotAndTrack(this.swapHotbarSlot);
            return true;
         } else {
            this.restoreSwappedInventoryItem();
            this.rememberOriginalSelectedSlot();
            int replacementHotbarSlot = this.findReplaceableHotbarSlot();
            this.selectHotbarSlotAndTrack(replacementHotbarSlot);
            CrystalAura.swapInventorySlots(requestedSlot, replacementHotbarSlot);
            this.swappedInventorySlot = requestedSlot;
            this.swapHotbarSlot = replacementHotbarSlot;
            return true;
         }
      }
   }
}

