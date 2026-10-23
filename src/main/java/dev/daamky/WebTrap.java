package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.core.event.impl.EventUpdate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class WebTrap extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final FloatSetting targetDistance = new FloatSetting("Дистанция", 4.5F, 2.0F, 6.0F, 0.1F, "бл.");
   private final FloatSetting placementDelay = new FloatSetting("Задержка", 200.0F, 0.0F, 1000.0F, 10.0F, "мс");
   private final BooleanSetting placeAtHeadHeight = new BooleanSetting("Ставить в голову", false);
   private final BooleanSetting restorePreviousHotbarSlot = new BooleanSetting("Возвращать слот", true);
   private final Stopwatch placementStopwatch = new Stopwatch();
   private BlockPos placementTargetPosition;
   private Direction placementFace;
// https://t.me/pasterplace

   public WebTrap() {
      super("WebTrap", "Ставит паутину в ноги ближайшему противнику", ModuleCategory.COMBAT);
      this.addSettings(this.targetDistance, this.placementDelay, this.placeAtHeadHeight, this.restorePreviousHotbarSlot);
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      this.placementTargetPosition = null;
      if (this.hasActiveClientWorld() && InventoryUtils.findHotbarSlot(stack -> stack.isOf(Items.COBWEB)) != -1) {
         PlayerEntity playerEntity = this.findNearestTargetPlayer();
         if (playerEntity != null) {
            BlockPos playerBlockPosition = playerEntity.getBlockPos();
            if (!this.isPlacementSpot(playerBlockPosition) && this.placeAtHeadHeight.getValue()) {
               this.isPlacementSpot(playerBlockPosition.up());
            }

            if (this.placementTargetPosition != null) {
               Vec3d placementAimPosition = Vec3d.ofCenter(this.placementTargetPosition.offset(this.placementFace.getOpposite()))
                  .add(Vec3d.of(this.placementFace.getVector()).multiply(0.5));
               Rotation lIll1Il1lll111l11l1I1llllI2 = RotationUtils.lookAt(CLIENT.player.getEyePos(), placementAimPosition);
               RotationPriorityManager.getInstance().requestRotation(lIll1Il1lll111l11l1I1llllI2, FocusMode.FREE, 60.0F, 4, false, this);
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onTick(EventTick eventTick) {
      if (this.hasActiveClientWorld()
         && this.placementTargetPosition != null
         && !CLIENT.player.isUsingItem()
         && this.placementStopwatch.hasElapsed(this.placementDelay.getValue().longValue())) {
         int cobwebSlot;
         if ((cobwebSlot = InventoryUtils.findHotbarSlot(stack -> stack.isOf(Items.COBWEB))) != -1) {
            int previousHotbarSlot = InventoryUtils.getHotbarSlotToRestore();
            InventoryUtils.setSelectedHotbarSlot(cobwebSlot);
            BlockPos placementBlockPosition = this.placementTargetPosition.offset(this.placementFace.getOpposite());
            Vec3d blockHitPosition = Vec3d.ofCenter(placementBlockPosition).add(Vec3d.of(this.placementFace.getVector()).multiply(0.5));
            CLIENT.interactionManager
               .interactBlock(CLIENT.player, Hand.MAIN_HAND, new BlockHitResult(blockHitPosition, this.placementFace, placementBlockPosition, false));
            CLIENT.player.swingHand(Hand.MAIN_HAND);
            if (this.restorePreviousHotbarSlot.getValue()) {
               InventoryUtils.rememberHotbarSlotForRestore(previousHotbarSlot);
            }

            this.placementStopwatch.reset();
         }
      }
   }
// https://t.me/pasterplace

   private boolean isPlacementSpot(BlockPos placementPosition) {
      if (!CLIENT.world.getBlockState(placementPosition).isReplaceable()) {
         return false;
      }

      if (CLIENT.player.getEyePos().distanceTo(Vec3d.ofCenter(placementPosition)) > this.targetDistance.getValue().floatValue()) {
         return false;
      }

      for (Direction neighborDirection : Direction.values()) {
         BlockPos neighborPosition = placementPosition.offset(neighborDirection);
         if (!CLIENT.world.getBlockState(neighborPosition).isReplaceable()) {
            this.placementTargetPosition = placementPosition;
            this.placementFace = neighborDirection.getOpposite();
            return true;
         }
      }

      return false;
   }
// https://t.me/pasterplace

   private PlayerEntity findNearestTargetPlayer() {
      Object closestPlayer = null;
      double nearestDistance = this.targetDistance.getValue().floatValue() + 1.0;

       for (PlayerEntity candidatePlayer : CLIENT.world.getPlayers()) {
         if (candidatePlayer != CLIENT.player
            && candidatePlayer.isAlive()
            && !candidatePlayer.isSpectator()
            && !FriendManager.getInstance().isFriend((Entity)candidatePlayer)
            && !AntiBot.II1Il11l111II11IIl((Entity)candidatePlayer)) {
            double candidateDistance = candidatePlayer.distanceTo(CLIENT.player);
            if (candidateDistance < nearestDistance) {
               nearestDistance = candidateDistance;
               closestPlayer = candidatePlayer;
            }
         }
      }
// https://t.me/pasterplace

      return (PlayerEntity)closestPlayer;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.placementTargetPosition = null;
      RotationPriorityManager.getInstance().clearRotationRequest(this);
   }
}

