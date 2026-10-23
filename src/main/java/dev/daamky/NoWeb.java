package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.mixin.client.ClientLevelAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos.Mutable;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NoWeb extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private final MultiOptionSetting mode = new MultiOptionSetting("Режим", "Фиксированный", "Фиксированный", "Кастомный", "Игнор");
   private final FloatSetting customHorizontalSpeed = new FloatSetting("Горизонтальная скорость", 0.19175F, 0.01F, 1.5F, 0.001F);
   private final FloatSetting customVerticalSpeed = new FloatSetting("Вертикальная скорость", 0.995F, 0.01F, 1.5F, 0.001F);
   private final BooleanSetting reallyWorldBypassEnabled = new BooleanSetting("ReallyWorld", true);
   private final BooleanSetting jumpBypassEnabled = new BooleanSetting("JumpBypass", false);
   private final BooleanSetting ignoreBerryBushes = new BooleanSetting("Ягодные кусты", true);
   private final Mutable searchBlockPosition = new Mutable();
   private boolean sprintStoppedByJumpBypass;
// https://t.me/pasterplace

   public NoWeb() {
      super("NoWeb", "Убирает замедление в паутине", ModuleCategory.MOVEMENT);
      this.customHorizontalSpeed.visibleWhen(() -> this.mode.isSelected("Кастомный"));
      this.customVerticalSpeed.visibleWhen(() -> this.mode.isSelected("Кастомный"));
      this.reallyWorldBypassEnabled.visibleWhen(() -> this.mode.isSelected("Игнор"));
      this.jumpBypassEnabled.visibleWhen(() -> this.mode.isSelected("Игнор"));
      this.ignoreBerryBushes.visibleWhen(() -> this.mode.isSelected("Игнор"));
      this.addSettings(this.mode, this.customHorizontalSpeed, this.customVerticalSpeed, this.reallyWorldBypassEnabled, this.jumpBypassEnabled, this.ignoreBerryBushes);
   }
// https://t.me/pasterplace

   @Override
   public String getDisplayInfo() {
      return this.mode.getValue();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.sprintStoppedByJumpBypass = false;
   }
// https://t.me/pasterplace

   private static NoWeb getEnabledModule() {
      NoWeb module = (NoWeb)ModuleRegistry.getInstance().getModuleByClass(NoWeb.class);
      return module != null && module.isEnabled() ? module : null;
   }
// https://t.me/pasterplace

   public static boolean isIgnoreModeEnabled() {
      NoWeb noWeb = getEnabledModule();
      return noWeb != null && noWeb.mode.isSelected("Игнор");
   }
// https://t.me/pasterplace

   public static boolean shouldIgnoreBlock(BlockState blockState) {
      NoWeb noWeb = getEnabledModule();
      return noWeb != null && noWeb.mode.isSelected("Игнор")
         ? blockState.isOf(Blocks.COBWEB) || noWeb.ignoreBerryBushes.getValue() && blockState.isOf(Blocks.SWEET_BERRY_BUSH)
         : false;
   }
// https://t.me/pasterplace

   public static void sendStopDestroyBlockPacket(BlockPos targetBlockPosition) {
      NoWeb noWeb = getEnabledModule();
      MinecraftClient client = MinecraftClient.getInstance();
      if (noWeb != null
         && noWeb.mode.isSelected("Игнор")
         && noWeb.reallyWorldBypassEnabled.getValue()
         && client.player != null
         && client.world != null) {
         PendingUpdateManager pendingUpdateManager = ((ClientLevelAccessor)client.world).daamky$getBlockStatePredictionHandler().incrementSequence();

         try {
            client.player
               .networkHandler
               .sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, targetBlockPosition, Direction.UP, pendingUpdateManager.getSequence()));
         } catch (Throwable sendPacketException) {
            if (pendingUpdateManager != null) {
               try {
                  pendingUpdateManager.close();
               } catch (Throwable closeException) {
                  sendPacketException.addSuppressed(closeException);
               }
            }
// https://t.me/pasterplace

            throw sendPacketException;
         }

         if (pendingUpdateManager != null) {
            pendingUpdateManager.close();
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (this.mode.isSelected("Игнор")) {
            if (this.jumpBypassEnabled.getValue()) {
               if (this.isCobwebWithinRadius(2.0)) {
                  if (CLIENT.options.jumpKey.isPressed()) {
                     if (CLIENT.player.isSprinting()) {
                        CLIENT.player.setSprinting(false);
                        this.sprintStoppedByJumpBypass = true;
                     }
                  } else if (CLIENT.player.isOnGround() && this.sprintStoppedByJumpBypass) {
                     CLIENT.player.setSprinting(true);
                     this.sprintStoppedByJumpBypass = false;
                  }
               } else if (this.sprintStoppedByJumpBypass && CLIENT.player.isOnGround()) {
                  this.sprintStoppedByJumpBypass = false;
               }
            }
         } else if (this.isPlayerInsideCobweb()) {
            double verticalSpeed = this.mode.isSelected("Кастомный") ? this.customVerticalSpeed.getValue().floatValue() : 0.995;
            double horizontalSpeed = this.mode.isSelected("Кастомный") ? this.customHorizontalSpeed.getValue().floatValue() : 0.6;
            double verticalMotion = 0.0;
            if (CLIENT.options.jumpKey.isPressed()) {
               verticalMotion = verticalSpeed;
            } else if (CLIENT.options.sneakKey.isPressed()) {
               verticalMotion = -verticalSpeed;
            }

            CLIENT.player.setVelocity(0.0, verticalMotion, 0.0);
            MovementUtils.setHorizontalSpeed(horizontalSpeed);
         }
      }
   }
// https://t.me/pasterplace

   private boolean isCobwebWithinRadius(double searchRadius) {
      double playerX = CLIENT.player.getX();
      double playerY = CLIENT.player.getY();
      double playerZ = CLIENT.player.getZ();
      int playerBlockX = CLIENT.player.getBlockX();
      int playerBlockY = CLIENT.player.getBlockY();
      int playerBlockZ = CLIENT.player.getBlockZ();
      double searchRadiusSquared = searchRadius * searchRadius;

      for (int xOffset = -2; xOffset <= 2; xOffset++) {
         for (int yOffset = -2; yOffset <= 2; yOffset++) {
            for (int zOffset = -2; zOffset <= 2; zOffset++) {
               int blockX = playerBlockX + xOffset;
               int blockY = playerBlockY + yOffset;
               int blockZ = playerBlockZ + zOffset;
               this.searchBlockPosition.set(blockX, blockY, blockZ);
               if (CLIENT.world.getBlockState(this.searchBlockPosition).getBlock() instanceof CobwebBlock) {
                  double distanceX = playerX - (blockX + 0.5);
                  double distanceY = playerY - (blockY + 0.5);
                  double distanceZ = playerZ - (blockZ + 0.5);
                  if (distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ <= searchRadiusSquared) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }
// https://t.me/pasterplace

   private boolean isPlayerInsideCobweb() {
      Box box = CLIENT.player.getBoundingBox();
      BlockPos playerBlockPosition = CLIENT.player.getBlockPos();

      for (int xOffset = -1; xOffset <= 1; xOffset++) {
         for (int yOffset = -1; yOffset <= 1; yOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
               BlockPos neighborBlockPosition = playerBlockPosition.add(xOffset, yOffset, zOffset);
               if (box.intersects(new Box(neighborBlockPosition)) && CLIENT.world.getBlockState(neighborBlockPosition).isOf(Blocks.COBWEB)) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}

