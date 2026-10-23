package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventTick;
import dev.daamky.core.event.impl.EventWorldChange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientTickEndC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.WorldChunk;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class TpKeyFinder extends ModuleStructure {
    private static final String sourceAttributionUrl = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final double UNUSED_MOVE_DISTANCE_9_5 = 9.5;
   private static final double SEARCH_RANGE = 50.0;
   private static final double SEARCH_STEP_BLOCKS = 96.0;
   private static final int UNUSED_INTEGER_CONSTANT_2_A = 2;
   private static final double UNUSED_DISTANCE_CONSTANT_16 = 16.0;
   private static final int UNUSED_TICK_CONSTANT_20_A = 20;
   private static final int UNUSED_TICK_CONSTANT_6_A = 6;
   private static final int UNUSED_INTEGER_CONSTANT_20 = 20;
   private static final double UNUSED_HEIGHT_CONSTANT_190 = 190.0;
   private static final int UNUSED_TICK_CONSTANT_6_B = 6;
   private static final double UNUSED_DISTANCE_CONSTANT_4 = 4.0;
   private static final long UNUSED_TIMEOUT_1500_MS = 1500L;
   private static final long UNUSED_TIMEOUT_5000_MS = 5000L;
   private static final int UNUSED_INTEGER_CONSTANT_0 = 0;
   private static final int UNUSED_TICK_CONSTANT_20_B = 20;
   private static final int UNUSED_INTEGER_CONSTANT_4 = 4;
   private static final int UNUSED_INTEGER_CONSTANT_2_B = 2;
   private static final int UNUSED_INTEGER_CONSTANT_1_A = 1;
   private static final int UNUSED_TICK_CONSTANT_9 = 9;
   private static final int UNUSED_TICK_LIMIT_100 = 100;
   private static final int UNUSED_INTEGER_CONSTANT_2_C = 2;
   private static final int UNUSED_TICK_COUNT_10 = 10;
   private static final int UNUSED_INTEGER_CONSTANT_1_B = 1;
   private static final float UNUSED_DELAY_SECONDS_1_5 = 1.5F;
   private static final int UNUSED_HEIGHT_CONSTANT_320 = 320;
   private final MultiToggleSetting targetTypes = new MultiToggleSetting("Цели").addOption("Вагонетки", true).addOption("Данжи", true);
   private final MultiToggleSetting lootCategories = new MultiToggleSetting("Что лутать")
      .addOption("Зачарованное золотое яблоко", true)
      .addOption("Золотое яблоко", true)
      .addOption("Ключ карты", true);
   private final BooleanSetting depositLootToEnderChest = new BooleanSetting("Deposit To /ec", true);
   private final BooleanSetting returnToStartOnDisable = new BooleanSetting("Return On Disable", true);
   private final BooleanSetting showChatNotifications = new BooleanSetting("Chat Notify", true);
   private final Set<String> visitedContainerKeys = new HashSet<>();
   private final Set<Long> unscannableChestPositions = new HashSet<>();
   private final Set<Long> deferredChestPositions = new HashSet<>();
   private final Map<Long, Integer> chunkBottomCheckTicks = new HashMap<>();
   private TpKeyFinder.ContainerScanState scanState = TpKeyFinder.ContainerScanState.IDLE;
   private TpKeyFinder.ContainerActionResult currentContainerType;
   private BlockPos currentContainerPosition;
   private ChestMinecartEntity currentChestMinecart;
   private Vec3d containerStandPosition;
   private Vec3d nextSearchPosition;
   private Vec3d searchCursorPosition;
   private Vec3d initialPlayerPosition;
   private double searchHeightY;
   private int travelCooldownTicks;
   private int travelFailureTicks;
   private int containerScanWaitTicks;
   private float initialPlayerYaw;
   private int stateTransitionDelayTicks;
   private int stateMachineTicks;
   private int serverRollbackPauseTicks;
   private int enderChestReturnDelayTicks;
   private int containerRetryCooldownTicks;
   private int containerOpenAttemptCount;
   private int openedContainerLootDelayTicks;
   private int wantedItemCountBeforeContainer;
   private long containerOpenStartedAtMs;
   private long enderChestMenuLastActionAtMs;
   private volatile boolean enabled;
   private volatile boolean serverDeniedContainerAccess;
   private boolean containerInteractionAttempted;
   private int searchOriginChunkX;
   private int searchOriginChunkZ;
   private int spiralOffsetX;
   private int spiralOffsetZ;
   private int spiralStepX;
   private int spiralStepZ;
   private int spiralSegmentLength;
   private int spiralSegmentProgress;
   private int spiralTurnsInSegment;
   private double searchDirectionX;
   private double searchDirectionZ;
   private double searchEdgeDirectionX;
   private double searchEdgeDirectionZ;
   private double worldBorderFallbackDirectionX;
   private double worldBorderFallbackDirectionZ;
   private int worldBorderFallbackStepsRemaining;
   private int worldBorderDirectionSign = 1;
// https://t.me/pasterplace

   public TpKeyFinder() {
      super("TpKeyFinder", "Ищет вагонетки и сундуки в данжах под картой, проверяет приваты и складывает ключи в /ec", ModuleCategory.PLAYER);
      this.addSettings(this.targetTypes, this.lootCategories, this.depositLootToEnderChest, this.returnToStartOnDisable, this.showChatNotifications);
      if (ClientInitializer.getEventBus() != null) {
         ClientInitializer.getEventBus().register(new TpKeyFinder.ContainerKeyTracker());
      }
   }
// https://t.me/pasterplace

   @Override
   protected void onEnable() {
      if (!this.hasActiveClientWorld()) {
         this.setEnabled(false);
      } else {
         ClientPlayerEntity clientPlayerEntity = CLIENT.player;
         if (!clientPlayerEntity.getAbilities().allowFlying && !clientPlayerEntity.isCreative()) {
            ClientChatLogger.l1I1IlllIlI("Для использования нужно взлететь на /fly.");
            this.setEnabled(false);
         } else {
            if (!clientPlayerEntity.getAbilities().flying) {
               clientPlayerEntity.getAbilities().flying = true;
               clientPlayerEntity.sendAbilitiesUpdate();
            }

            this.loadVisitedContainers();
            this.unscannableChestPositions.clear();
            this.deferredChestPositions.clear();
            this.chunkBottomCheckTicks.clear();
            this.initialPlayerPosition = clientPlayerEntity.getEntityPos();
            this.initialPlayerYaw = clientPlayerEntity.getYaw();
            this.searchCursorPosition = clientPlayerEntity.getEntityPos();
            this.clearCurrentContainerTarget();
            this.searchHeightY = CLIENT.world.getBottomY() - 2.0;
            if (clientPlayerEntity.getY() - this.searchHeightY > 190.0) {
               ClientChatLogger.l1I1IlllIlI(String.format(Locale.ROOT, "Слишком высоко: клип под бедрок работает до Y=%.0f.", this.searchHeightY + 190.0));
               this.setEnabled(false);
            } else {
               this.searchOriginChunkX = ChunkSectionPos.getSectionCoord(clientPlayerEntity.getBlockX());
               this.searchOriginChunkZ = ChunkSectionPos.getSectionCoord(clientPlayerEntity.getBlockZ());
               this.resetSearchProgress();
               this.initializeSearchDirectionFromYaw(clientPlayerEntity);
               this.enabled = false;
               this.serverDeniedContainerAccess = false;
               this.serverRollbackPauseTicks = 0;
               this.enderChestReturnDelayTicks = 0;
               this.stateMachineTicks = 0;
               this.stateTransitionDelayTicks = 0;
               if (this.showChatNotifications.getValue()) {
                  if (this.targetTypes.isOptionEnabled("Данжи")) {
                     ClientChatLogger.l1I1IlllIlI("Осторожнее! Лут по \"Данжи\" может кикаться.");
                  }

                  ClientChatLogger.II1Il11l111II11IIl(
                     String.format(
                        Locale.ROOT,
                        "TeleportKeyCard включен. Загружено проверенных: %d. Поиск вагонеток (Y=%.1f)...",
                        this.visitedContainerKeys.size(),
                        this.searchHeightY
                     )
                  );
               }

               this.beginSearchDescent();
            }
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      RotationPriorityManager.getInstance().clearRotationRequest(this);
      ClientPlayerEntity clientPlayerEntity = CLIENT.player;
      if (clientPlayerEntity != null && this.scanState != TpKeyFinder.ContainerScanState.IDLE) {
         clientPlayerEntity.closeHandledScreen();
         if (this.returnToStartOnDisable.getValue()
            && this.initialPlayerPosition != null
            && this.searchCursorPosition != null
            && CLIENT.world != null
            && this.searchCursorPosition.y < CLIENT.world.getBottomY()) {
            Vec3d initialPlayerPositionToRestore = this.initialPlayerPosition;
            if (!this.isSearchAreaReady(CLIENT.world, initialPlayerPositionToRestore.x, initialPlayerPositionToRestore.z, 0)) {
               int surfaceYForReturn = CLIENT.world
                  .getTopY(
                     Type.MOTION_BLOCKING,
                     (int)Math.floor(this.searchCursorPosition.x),
                     (int)Math.floor(this.searchCursorPosition.z)
                  );
               initialPlayerPositionToRestore = new Vec3d(this.searchCursorPosition.x, surfaceYForReturn, this.searchCursorPosition.z);
               ClientChatLogger.l1I1IlllIlI("Точка старта в непрогруженном чанке, поднимаемся на поверхность здесь.");
            }

            this.setSearchCursorIfLoaded(initialPlayerPositionToRestore);
            clientPlayerEntity.setPosition(initialPlayerPositionToRestore);
         }
      }

      this.scanState = TpKeyFinder.ContainerScanState.IDLE;
      this.clearCurrentContainerTarget();
      this.unscannableChestPositions.clear();
      if (this.showChatNotifications.getValue()) {
         ClientChatLogger.II1Il11l111II11IIl("TeleportKeyCard остановлен.");
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onWorldChange(EventWorldChange eventWorldChange) {
      if (this.isEnabled()) {
         this.scanState = TpKeyFinder.ContainerScanState.IDLE;
         this.setEnabled(false);
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onPacket(EventPacket eventPacket) {
      if (this.scanState != TpKeyFinder.ContainerScanState.IDLE) {
         if (eventPacket.isReceive()) {
            if (eventPacket.packet() instanceof PlayerPositionLookS2CPacket) {
               this.enabled = true;
               return;
            }

            String serverMessage = this.extractPacketMessageText(eventPacket.packet());
            if (serverMessage != null
               && this.isAccessDeniedMessage(serverMessage.toLowerCase(Locale.ROOT))
               && (this.scanState == TpKeyFinder.ContainerScanState.OPENING_CONTAINER || this.scanState == TpKeyFinder.ContainerScanState.LOOTING_CONTAINER)) {
               this.serverDeniedContainerAccess = true;
            }
         }
      }
   }
// https://t.me/pasterplace

   @EventTarget
   public void onTick(EventTick eventTick) {
      if (this.scanState != TpKeyFinder.ContainerScanState.IDLE && this.hasActiveClientWorld()) {
         ClientPlayerEntity clientPlayerEntity = CLIENT.player;
         ClientWorld clientWorld = CLIENT.world;
         if (clientPlayerEntity.isDead()) {
            this.setEnabled(false);
         } else {
            if (this.enabled) {
               this.enabled = false;
               this.searchCursorPosition = clientPlayerEntity.getEntityPos();
               this.serverRollbackPauseTicks = Math.round(30.0F);
               clientPlayerEntity.closeHandledScreen();
               this.clearCurrentContainerTarget();
               if (this.showChatNotifications.getValue()) {
                  ClientChatLogger.l1I1IlllIlI(String.format(Locale.ROOT, "Сервер откатил позицию (rollback). Пауза %.1f сек...", 1.5F));
               }
            }

            if (this.serverRollbackPauseTicks > 0) {
               if (--this.serverRollbackPauseTicks == 0) {
                  if (this.showChatNotifications.getValue()) {
                     ClientChatLogger.II1Il11l111II11IIl("Пауза отката завершена. Продолжаем полет.");
                  }

                  this.beginSearchDescent();
               }

               this.holdPlayerAtSearchCursor(clientPlayerEntity);
            } else {
               if (this.serverDeniedContainerAccess) {
                  this.serverDeniedContainerAccess = false;
                  if (this.showChatNotifications.getValue()) {
                     ClientChatLogger.l1I1IlllIlI("Сервер отклонил доступ (приват/регион)! Пропускаем цель.");
                  }

                  clientPlayerEntity.closeHandledScreen();
                  this.markContainerLooted();
                  this.markNearbyContainersVisited(this.currentContainerPosition);
                  this.resumeDescentAfterEnderChest();
               }
// https://t.me/pasterplace

               if (this.enderChestReturnDelayTicks > 0) {
                  if (--this.enderChestReturnDelayTicks == 0) {
                     this.beginSearchDescent();
                  }

                  this.holdPlayerAtSearchCursor(clientPlayerEntity);
               } else {
                  if (this.containerRetryCooldownTicks > 0) {
                     this.containerRetryCooldownTicks--;
                  }

                  if (this.travelCooldownTicks > 0) {
                     this.travelCooldownTicks--;
                  }
// https://t.me/pasterplace

                  if (this.stateTransitionDelayTicks > 0) {
                     this.stateTransitionDelayTicks--;
                     this.holdPlayerAtSearchCursor(clientPlayerEntity);
                  } else {
                     if (++this.stateMachineTicks >= 1) {
                        this.stateMachineTicks = 0;
                        switch (this.scanState) {
                           case DESCENDING_TO_SEARCH_HEIGHT:
                              this.transitionToStateAtPosition(
                                 new Vec3d(this.searchCursorPosition.x, this.searchHeightY, this.searchCursorPosition.z),
                                 TpKeyFinder.ContainerScanState.SCANNING_FOR_CONTAINERS
                              );
                              if (this.scanState == TpKeyFinder.ContainerScanState.SCANNING_FOR_CONTAINERS && this.showChatNotifications.getValue()) {
                                 ClientChatLogger.II1Il11l111II11IIl("Спуск завершен. Поиск целей под картой на 1 тике...");
                              }
                              break;
                           case SCANNING_FOR_CONTAINERS:
                              this.scanForContainerTargets(clientWorld);
                              break;
                           case TRAVELING_TO_CONTAINER:
                              this.travelToCurrentContainer(clientPlayerEntity, clientWorld);
                              break;
                           case POSITIONING_AT_CONTAINER:
                              this.prepareCurrentContainerInteraction();
                              break;
                           case OPENING_CONTAINER:
                              this.openCurrentContainer(clientPlayerEntity, clientWorld);
                              break;
                           case LOOTING_CONTAINER:
                              this.lootCurrentContainer(clientPlayerEntity);
                              break;
                           case REQUESTING_ENDER_CHEST:
                              this.requestEnderChest();
                              break;
                           case OPENING_ENDER_MENU:
                              this.openEnderChestMenu(clientPlayerEntity);
                              break;
                           case LOOTING_ENDER_CHEST:
                              this.lootEnderChest(clientPlayerEntity);
                        }
                     }
// https://t.me/pasterplace

                     if (this.scanState != TpKeyFinder.ContainerScanState.IDLE) {
                        this.holdPlayerAtSearchCursor(clientPlayerEntity);
                     }
                  }
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private void requestDownwardFlightRotation(ClientPlayerEntity clientPlayerEntity) {
      RotationPriorityManager.getInstance()
         .requestRotation(new Rotation(this.initialPlayerYaw, 90.0F), FocusMode.FREE, 180.0F, 20, false, this);
   }
// https://t.me/pasterplace

   private void retryCurrentContainerTravel(ClientWorld clientWorld) {
      if (this.scanState == TpKeyFinder.ContainerScanState.TRAVELING_TO_CONTAINER && CLIENT.player != null) {
         this.travelToCurrentContainer(CLIENT.player, clientWorld);
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void holdPlayerAtSearchCursor(ClientPlayerEntity clientPlayerEntity) {
      clientPlayerEntity.setPosition(this.searchCursorPosition);
      this.requestDownwardFlightRotation(clientPlayerEntity);
      clientPlayerEntity.setVelocity(Vec3d.ZERO);
      clientPlayerEntity.fallDistance = 0.0;
   }
// https://t.me/pasterplace

   private void beginSearchDescent() {
      this.scanState = TpKeyFinder.ContainerScanState.DESCENDING_TO_SEARCH_HEIGHT;
   }
// https://t.me/pasterplace

   private void scanForContainerTargets(ClientWorld clientWorld) {
      if (this.isSearchAreaReady(clientWorld, this.searchCursorPosition.x, this.searchCursorPosition.z, 0)
         || ++this.containerScanWaitTicks > 100) {
         this.containerScanWaitTicks = 0;
         ChestMinecartEntity nearestMinecart = this.targetTypes.isOptionEnabled("Вагонетки") ? this.findNearestUnvisitedMinecart(clientWorld) : null;
         BlockPos nearestDungeonChest = this.targetTypes.isOptionEnabled("Данжи") ? this.findNearestDungeonChest(clientWorld) : null;
         if (nearestMinecart != null && nearestDungeonChest != null) {
            double minecartDistanceSquared = this.horizontalDistanceSquared(this.searchCursorPosition, nearestMinecart.getEntityPos());
            double chestDistanceSquared = this.horizontalDistanceSquared(this.searchCursorPosition, Vec3d.ofCenter(nearestDungeonChest));
            if (minecartDistanceSquared <= chestDistanceSquared) {
               this.selectMinecartTarget(clientWorld, nearestMinecart);
            } else {
               this.selectStructureChestTarget(clientWorld, nearestDungeonChest);
            }

            this.retryCurrentContainerTravel(clientWorld);
         } else if (nearestMinecart != null) {
            this.selectMinecartTarget(clientWorld, nearestMinecart);
            this.retryCurrentContainerTravel(clientWorld);
         } else if (nearestDungeonChest != null) {
            this.selectStructureChestTarget(clientWorld, nearestDungeonChest);
            this.retryCurrentContainerTravel(clientWorld);
         } else {
            if (this.nextSearchPosition == null || this.horizontalDistance(this.searchCursorPosition, this.nextSearchPosition) < 0.001) {
               this.nextSearchPosition = this.getNextSearchPosition();
               if (this.nextSearchPosition == null) {
                  this.searchOriginChunkX = ChunkSectionPos.getSectionCoordFloored(this.searchCursorPosition.x);
                  this.searchOriginChunkZ = ChunkSectionPos.getSectionCoordFloored(this.searchCursorPosition.z);
                  this.unscannableChestPositions.clear();
                  this.resetSearchProgress();
                  return;
               }
            }

            this.advanceSearchCursorToward(clientWorld, this.nextSearchPosition);
         }
      }
   }
// https://t.me/pasterplace

   private void travelToCurrentContainer(ClientPlayerEntity clientPlayerEntity, ClientWorld clientWorld) {
      if (this.currentContainerType == TpKeyFinder.ContainerActionResult.CHEST_MINECART) {
         ChestMinecartEntity chestMinecartEntity = this.findCurrentMinecart();
         if (chestMinecartEntity == null) {
            this.clearCurrentContainerTarget();
            this.scanState = TpKeyFinder.ContainerScanState.SCANNING_FOR_CONTAINERS;
            return;
         }

         this.currentChestMinecart = chestMinecartEntity;
         this.currentContainerPosition = chestMinecartEntity.getBlockPos().toImmutable();
         Vec3d safeMinecartPosition = this.findSafeStandingPositionAtMinecart(clientPlayerEntity, clientWorld, chestMinecartEntity);
         if (safeMinecartPosition != null) {
            this.containerStandPosition = safeMinecartPosition;
         }
      } else if (this.currentContainerPosition != null) {
         Vec3d safeChestPosition = this.findSafeStandingPositionAtChest(clientPlayerEntity, clientWorld, this.currentContainerPosition);
         if (safeChestPosition != null) {
            this.containerStandPosition = safeChestPosition;
         }
      }

      if (this.containerStandPosition == null) {
         this.markContainerLooted();
         this.clearCurrentContainerTarget();
         this.scanState = TpKeyFinder.ContainerScanState.SCANNING_FOR_CONTAINERS;
      } else {
         Vec3d containerTravelPosition = new Vec3d(this.containerStandPosition.x, this.searchHeightY, this.containerStandPosition.z);
         if (this.horizontalDistance(this.searchCursorPosition, containerTravelPosition) <= 50.0 && this.travelCooldownTicks == 0) {
            if (this.containerStandPosition.y - this.searchHeightY > 190.0) {
               this.markContainerLooted();
               this.clearCurrentContainerTarget();
               this.scanState = TpKeyFinder.ContainerScanState.SCANNING_FOR_CONTAINERS;
            } else {
               this.scanState = TpKeyFinder.ContainerScanState.POSITIONING_AT_CONTAINER;
               this.prepareCurrentContainerInteraction();
            }
         } else {
            this.advanceSearchCursorToward(clientWorld, containerTravelPosition);
         }
      }
   }
// https://t.me/pasterplace

   private void advanceSearchCursorToward(ClientWorld clientWorld, Vec3d targetPosition) {
      double deltaX = targetPosition.x - this.searchCursorPosition.x;
      double deltaZ = targetPosition.z - this.searchCursorPosition.z;
      double horizontalDistance = Math.hypot(deltaX, deltaZ);
      if (!(horizontalDistance < 0.001)) {
         if (this.travelCooldownTicks <= 0) {
            Vec3d nextLoadedPosition = null;

            for (double remainingDistance = horizontalDistance; remainingDistance > 0.0; remainingDistance -= 4.0) {
               double travelFraction = remainingDistance / horizontalDistance;
               Vec3d candidatePosition = new Vec3d(
                  this.searchCursorPosition.x + deltaX * travelFraction,
                  this.searchHeightY,
                  this.searchCursorPosition.z + deltaZ * travelFraction
               );
               if (this.isSearchAreaReady(clientWorld, candidatePosition.x, candidatePosition.z, 1)) {
                  nextLoadedPosition = candidatePosition;
                  break;
               }
            }

            if (nextLoadedPosition == null) {
               if (++this.travelFailureTicks > 100) {
                  this.travelFailureTicks = 0;
                  if (this.scanState == TpKeyFinder.ContainerScanState.SCANNING_FOR_CONTAINERS) {
                     this.nextSearchPosition = null;
                  } else {
                     this.markContainerLooted();
                     this.clearCurrentContainerTarget();
                     this.scanState = TpKeyFinder.ContainerScanState.SCANNING_FOR_CONTAINERS;
                  }
               }
            } else {
               this.travelFailureTicks = 0;
               this.searchCursorPosition = nextLoadedPosition;
               this.travelCooldownTicks = 6;
            }
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void prepareCurrentContainerInteraction() {
      if (CLIENT.world != null
         && !this.isSearchAreaReady(CLIENT.world, this.containerStandPosition.x, this.containerStandPosition.z, 1)) {
         if (++this.travelFailureTicks > 100) {
            this.travelFailureTicks = 0;
            this.markContainerLooted();
            this.clearCurrentContainerTarget();
            this.scanState = TpKeyFinder.ContainerScanState.SCANNING_FOR_CONTAINERS;
         }
      } else {
         this.travelFailureTicks = 0;
         this.transitionToStateAtPosition(this.containerStandPosition, TpKeyFinder.ContainerScanState.OPENING_CONTAINER);
         if (this.scanState == TpKeyFinder.ContainerScanState.OPENING_CONTAINER) {
            this.stateTransitionDelayTicks = 1;
         }
// https://t.me/pasterplace

         if (this.scanState == TpKeyFinder.ContainerScanState.OPENING_CONTAINER) {
            this.containerOpenStartedAtMs = System.currentTimeMillis();
            this.containerOpenAttemptCount = 0;
            this.containerRetryCooldownTicks = 0;
            this.wantedItemCountBeforeContainer = this.countWantedItemsInInventory();
            if (this.showChatNotifications.getValue()) {
               ClientChatLogger.II1Il11l111II11IIl("Прибыли к цели. Открываем...");
            }
         }
      }
   }
// https://t.me/pasterplace

   private void openCurrentContainer(ClientPlayerEntity clientPlayerEntity, ClientWorld clientWorld) {
      if (clientPlayerEntity.currentScreenHandler instanceof GenericContainerScreenHandler) {
         this.openedContainerLootDelayTicks = 1;
         this.scanState = TpKeyFinder.ContainerScanState.LOOTING_CONTAINER;
      } else if (System.currentTimeMillis() - this.containerOpenStartedAtMs > 1500L) {
         this.markContainerLooted();
         this.markNearbyContainersVisited(this.currentContainerPosition);
         this.resumeDescentAfterEnderChest();
         if (this.showChatNotifications.getValue()) {
            ClientChatLogger.l1I1IlllIlI("Не удалось открыть цель (таймаут / запривачено). Пропускаем.");
         }
      } else if (this.containerRetryCooldownTicks <= 0 && this.containerOpenAttemptCount < 2) {
         this.containerInteractionAttempted = true;
         if (this.currentContainerType == TpKeyFinder.ContainerActionResult.CHEST_MINECART) {
            ChestMinecartEntity chestMinecartEntity = this.findCurrentMinecart();
            if (chestMinecartEntity == null) {
               this.markContainerLooted();
               this.resumeDescentAfterEnderChest();
               return;
            }

            if (this.isContainerInProtectedRegion(clientWorld, chestMinecartEntity.getBlockPos())) {
               this.markContainerLooted();
               this.markNearbyContainersVisited(chestMinecartEntity.getBlockPos());
               this.resumeDescentAfterEnderChest();
               if (this.showChatNotifications.getValue()) {
                  ClientChatLogger.l1I1IlllIlI("Вагонетка находится в чужом привате! Пропускаем.");
               }

               return;
            }

            Box box = chestMinecartEntity.getBoundingBox();
            Vec3d minecartInteractionPosition = new Vec3d(box.getCenter().x, box.maxY, box.getCenter().z);
            this.runContainerInteraction(
               clientPlayerEntity,
               () -> {
                  if (!CLIENT.interactionManager
                     .interactEntityAtLocation(clientPlayerEntity, chestMinecartEntity, new EntityHitResult(chestMinecartEntity, minecartInteractionPosition), Hand.MAIN_HAND)
                     .isAccepted()) {
                     CLIENT.interactionManager.interactEntity(clientPlayerEntity, chestMinecartEntity, Hand.MAIN_HAND);
                  }

                  if (this.containerOpenAttemptCount > 0) {
                     clientPlayerEntity.swingHand(Hand.MAIN_HAND);
                  }
               }
            );
         } else {
            if (this.currentContainerPosition == null) {
               this.resumeDescentAfterEnderChest();
               return;
            }

            if (this.isContainerInProtectedRegion(clientWorld, this.currentContainerPosition)) {
               this.markContainerLooted();
               this.markNearbyContainersVisited(this.currentContainerPosition);
               this.resumeDescentAfterEnderChest();
               if (this.showChatNotifications.getValue()) {
                  ClientChatLogger.l1I1IlllIlI("Сундук находится в чужом привате! Пропускаем.");
               }

               return;
            }

            BlockPos chestPosition = this.currentContainerPosition;
            Vec3d chestHitPosition = new Vec3d(chestPosition.getX() + 0.5, chestPosition.getY() + 0.875, chestPosition.getZ() + 0.5);
            Direction direction = Direction.UP;
            this.runContainerInteraction(clientPlayerEntity, () -> {
               CLIENT.interactionManager.interactBlock(clientPlayerEntity, Hand.MAIN_HAND, new BlockHitResult(chestHitPosition, direction, chestPosition, false));
               if (this.containerOpenAttemptCount > 0) {
                  clientPlayerEntity.swingHand(Hand.MAIN_HAND);
               }
            });
         }

         if (this.containerOpenAttemptCount == 0) {
            this.containerOpenAttemptCount = 1;
            this.containerRetryCooldownTicks = 9;
         } else {
            this.markContainerLooted();
            this.markNearbyContainersVisited(this.currentContainerPosition);
            this.clearCurrentContainerTarget();
            this.searchCursorPosition = new Vec3d(
               this.searchCursorPosition.x, this.searchHeightY, this.searchCursorPosition.z
            );
            this.scanState = TpKeyFinder.ContainerScanState.SCANNING_FOR_CONTAINERS;
            this.stateTransitionDelayTicks = 2;
            if (this.showChatNotifications.getValue()) {
               ClientChatLogger.l1I1IlllIlI("Цель не открылась (приват). Пропускаем.");
            }
         }
      }
   }
// https://t.me/pasterplace

   private void lootCurrentContainer(ClientPlayerEntity clientPlayerEntity) {
      ScreenHandler currentScreenHandler = clientPlayerEntity.currentScreenHandler;
      if (!(currentScreenHandler instanceof GenericContainerScreenHandler)) {
         this.finishCurrentContainerLooting(clientPlayerEntity);
      } else {
         GenericContainerScreenHandler containerScreenHandler = (GenericContainerScreenHandler)currentScreenHandler;
         if (this.openedContainerLootDelayTicks > 0) {
            this.openedContainerLootDelayTicks--;
         } else if (System.currentTimeMillis() - this.containerOpenStartedAtMs > 6500L) {
            this.finishCurrentContainerLooting(clientPlayerEntity);
         } else {
            int containerSlotCount = containerScreenHandler.getRows() * 9;
            boolean foundWantedItem = false;

            for (int slotIndex = 0; slotIndex < containerSlotCount && slotIndex < currentScreenHandler.slots.size(); slotIndex++) {
               ItemStack stack = currentScreenHandler.getSlot(slotIndex).getStack();
               if (this.isWantedLootItem(stack)) {
                  String itemDisplayName = stack.getName().getString();
                  int itemCount = stack.getCount();
                  CLIENT.interactionManager.clickSlot(currentScreenHandler.syncId, slotIndex, 0, SlotActionType.QUICK_MOVE, clientPlayerEntity);
                  foundWantedItem = true;
                  if (this.showChatNotifications.getValue()) {
                     ClientChatLogger.II1Il11l111II11IIl("Забрали: " + itemDisplayName + " (x" + itemCount + ")!");
                  }
               }
            }

            if (!foundWantedItem) {
               this.finishCurrentContainerLooting(clientPlayerEntity);
            }
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void finishCurrentContainerLooting(ClientPlayerEntity clientPlayerEntity) {
      this.markContainerLooted();
      clientPlayerEntity.closeHandledScreen();
      if (this.showChatNotifications.getValue()) {
         ClientChatLogger.II1Il11l111II11IIl("Цель проверена.");
      }

      if (this.depositLootToEnderChest.getValue() && this.countWantedItemsInInventory() > this.wantedItemCountBeforeContainer) {
         this.clearCurrentContainerTarget();
         this.enderChestMenuLastActionAtMs = System.currentTimeMillis();
         this.scanState = TpKeyFinder.ContainerScanState.REQUESTING_ENDER_CHEST;
         if (this.showChatNotifications.getValue()) {
            ClientChatLogger.II1Il11l111II11IIl("Открываем Эндер-сундук (/ec)...");
         }
      } else {
         this.resumeDescentAfterEnderChest();
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void requestEnderChest() {
      if (CLIENT.getNetworkHandler() != null) {
         CLIENT.getNetworkHandler().sendChatCommand("ec");
         this.enderChestMenuLastActionAtMs = System.currentTimeMillis();
         this.scanState = TpKeyFinder.ContainerScanState.OPENING_ENDER_MENU;
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void openEnderChestMenu(ClientPlayerEntity clientPlayerEntity) {
      if (System.currentTimeMillis() - this.enderChestMenuLastActionAtMs > 5000L) {
         clientPlayerEntity.closeHandledScreen();
         this.beginEnderChestReturnDelay();
      } else {
         ScreenHandler currentScreenHandler = clientPlayerEntity.currentScreenHandler;
         if (currentScreenHandler instanceof GenericContainerScreenHandler) {
            GenericContainerScreenHandler menuScreenHandler = (GenericContainerScreenHandler)currentScreenHandler;
            if (System.currentTimeMillis() - this.enderChestMenuLastActionAtMs >= 100L) {
               int menuSlotCount = menuScreenHandler.getRows() * 9;

               for (int slotIndex = 0; slotIndex < menuSlotCount && slotIndex < currentScreenHandler.slots.size(); slotIndex++) {
                  ItemStack stack = currentScreenHandler.getSlot(slotIndex).getStack();
                  if (!stack.isEmpty()) {
                     String normalizedItemName = stack.getName().getString().toLowerCase(Locale.ROOT);
                     if (normalizedItemName.contains("открыть") || normalizedItemName.contains("open")) {
                        CLIENT.interactionManager.clickSlot(currentScreenHandler.syncId, slotIndex, 0, SlotActionType.PICKUP, clientPlayerEntity);
                        this.enderChestMenuLastActionAtMs = System.currentTimeMillis();
                        return;
                     }
                  }
               }
// https://t.me/pasterplace

               this.enderChestMenuLastActionAtMs = System.currentTimeMillis();
               this.scanState = TpKeyFinder.ContainerScanState.LOOTING_ENDER_CHEST;
            }
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void lootEnderChest(ClientPlayerEntity clientPlayerEntity) {
      ScreenHandler currentScreenHandler = clientPlayerEntity.currentScreenHandler;
      if (currentScreenHandler instanceof GenericContainerScreenHandler) {
         GenericContainerScreenHandler enderChestScreenHandler = (GenericContainerScreenHandler)currentScreenHandler;
         if (System.currentTimeMillis() - this.enderChestMenuLastActionAtMs <= 5000L) {
            for (int enderChestSlotIndex = enderChestScreenHandler.getRows() * 9; enderChestSlotIndex < currentScreenHandler.slots.size(); enderChestSlotIndex++) {
               if (this.isWantedLootItem(currentScreenHandler.getSlot(enderChestSlotIndex).getStack())) {
                  CLIENT.interactionManager.clickSlot(currentScreenHandler.syncId, enderChestSlotIndex, 0, SlotActionType.QUICK_MOVE, clientPlayerEntity);
                  return;
               }
            }

            clientPlayerEntity.closeHandledScreen();
            if (this.showChatNotifications.getValue()) {
               ClientChatLogger.II1Il11l111II11IIl("Все найденные предметы сложены в Эндер-сундук!");
            }

            this.beginEnderChestReturnDelay();
            return;
         }
      }

      clientPlayerEntity.closeHandledScreen();
      this.beginEnderChestReturnDelay();
   }
// https://t.me/pasterplace

   private void beginEnderChestReturnDelay() {
      this.resumeDescentAfterEnderChest();
      this.enderChestReturnDelayTicks = 20;
   }
// https://t.me/pasterplace

   private void resumeDescentAfterEnderChest() {
      this.clearCurrentContainerTarget();
      this.enderChestReturnDelayTicks = 0;
      this.scanState = TpKeyFinder.ContainerScanState.DESCENDING_TO_SEARCH_HEIGHT;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void clearCurrentContainerTarget() {
      this.currentContainerType = null;
      this.currentContainerPosition = null;
      this.currentChestMinecart = null;
      this.containerStandPosition = null;
      this.containerOpenAttemptCount = 0;
      this.containerRetryCooldownTicks = 0;
      this.openedContainerLootDelayTicks = 0;
      this.containerInteractionAttempted = false;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void selectMinecartTarget(ClientWorld clientWorld, ChestMinecartEntity chestMinecartEntity) {
      this.currentContainerType = TpKeyFinder.ContainerActionResult.CHEST_MINECART;
      this.currentChestMinecart = chestMinecartEntity;
      this.currentContainerPosition = chestMinecartEntity.getBlockPos().toImmutable();
      this.containerStandPosition = this.findSafeStandingPositionAtMinecart(CLIENT.player, clientWorld, chestMinecartEntity);
      if (this.containerStandPosition != null) {
         this.scanState = TpKeyFinder.ContainerScanState.TRAVELING_TO_CONTAINER;
         if (this.showChatNotifications.getValue()) {
            ClientChatLogger.II1Il11l111II11IIl("Найдена вагонетка на " + this.currentContainerPosition.toShortString() + ". Летим к ней...");
         }
      } else {
         this.markContainerLooted();
         this.clearCurrentContainerTarget();
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void selectStructureChestTarget(ClientWorld clientWorld, BlockPos chestPosition) {
      this.currentContainerType = TpKeyFinder.ContainerActionResult.STRUCTURE_CHEST;
      this.currentContainerPosition = chestPosition.toImmutable();
      this.containerStandPosition = this.findSafeStandingPositionAtChest(CLIENT.player, clientWorld, chestPosition);
      if (this.containerStandPosition != null) {
         this.scanState = TpKeyFinder.ContainerScanState.TRAVELING_TO_CONTAINER;
         if (this.showChatNotifications.getValue()) {
            ClientChatLogger.II1Il11l111II11IIl(
               String.format(
                  Locale.ROOT,
                  "Найден сундук в данже на [%d, %d, %d]. Летим к нему...",
                  chestPosition.getX(),
                  chestPosition.getY(),
                  chestPosition.getZ()
               )
            );
         }
      } else {
         this.markContainerLooted();
         this.clearCurrentContainerTarget();
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private ChestMinecartEntity findNearestUnvisitedMinecart(ClientWorld clientWorld) {
       ChestMinecartEntity nearestMinecart = null;
      double nearestDistanceSquared = Double.MAX_VALUE;

      for (Entity entity : clientWorld.getEntities()) {
         if (entity instanceof ChestMinecartEntity) {
            ChestMinecartEntity chestMinecart = (ChestMinecartEntity)entity;
            if (chestMinecart.isAlive()) {
               BlockPos minecartPosition = chestMinecart.getBlockPos();
               String minecartKey = this.minecartPositionKey(minecartPosition);
               if (!this.visitedContainerKeys.contains(minecartKey)) {
                  if (!this.isContainerInProtectedRegion(clientWorld, minecartPosition)) {
                     double candidateDistanceSquared;
                     if ((candidateDistanceSquared = this.horizontalDistanceSquared(this.searchCursorPosition, chestMinecart.getEntityPos())) < nearestDistanceSquared) {
                        nearestDistanceSquared = candidateDistanceSquared;
                        nearestMinecart = chestMinecart;
                     }
                  } else {
                     this.visitedContainerKeys.add(minecartKey);
                     this.saveVisitedContainers();
                  }
               }
            }
         }
      }

       return nearestMinecart;
   }
// https://t.me/pasterplace

   private ChestMinecartEntity findCurrentMinecart() {
      if (this.currentChestMinecart != null && this.currentChestMinecart.isAlive()) {
         return this.currentChestMinecart;
      }

      if (this.currentContainerPosition != null && CLIENT.world != null) {
         for (Entity entity : CLIENT.world.getEntities()) {
            if (entity instanceof ChestMinecartEntity) {
               ChestMinecartEntity chestMinecart = (ChestMinecartEntity)entity;
               if (chestMinecart.isAlive() && chestMinecart.getBlockPos().getSquaredDistance(this.currentContainerPosition) <= 4.0) {
                  return (ChestMinecartEntity)chestMinecart;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }
// https://t.me/pasterplace

   private BlockPos findNearestDungeonChest(ClientWorld clientWorld) {
      int centerChunkX = ChunkSectionPos.getSectionCoordFloored(this.searchCursorPosition.x);
      int centerChunkZ = ChunkSectionPos.getSectionCoordFloored(this.searchCursorPosition.z);
      BlockPos nearestChestPosition = null;
      double nearestDistanceSquared = Double.MAX_VALUE;

      for (int chunkX = centerChunkX - 4; chunkX <= centerChunkX + 4; chunkX++) {
         for (int chunkZ = centerChunkZ - 4; chunkZ <= centerChunkZ + 4; chunkZ++) {
            if (clientWorld.isChunkLoaded(chunkX, chunkZ)) {
               WorldChunk worldChunk = clientWorld.getChunk(chunkX, chunkZ);

               for (BlockPos chestPosition : worldChunk.getBlockEntities().keySet()) {
                  String chestKey = this.chestPositionKey(chestPosition);
                  long packedChestPosition = chestPosition.asLong();
                  if (!this.visitedContainerKeys.contains(chestKey) && !this.deferredChestPositions.contains(packedChestPosition)) {
                     BlockState blockState = worldChunk.getBlockState(chestPosition);
                     if ((blockState.isOf(Blocks.CHEST) || blockState.isOf(Blocks.TRAPPED_CHEST))
                        && this.areNearbyChunksLoaded(clientWorld, chestPosition)) {
                        if (!this.isDungeonChest(clientWorld, chestPosition)) {
                           this.deferredChestPositions.add(packedChestPosition);
                        } else if (this.isContainerInProtectedRegion(clientWorld, (BlockPos)chestPosition)) {
                           this.visitedContainerKeys.add(chestKey);
                           this.saveVisitedContainers();
                        } else {
                           double candidateDistanceSquared = this.horizontalDistanceSquared(this.searchCursorPosition, Vec3d.ofCenter(chestPosition));
                           if (candidateDistanceSquared < nearestDistanceSquared) {
                              nearestDistanceSquared = candidateDistanceSquared;
                              nearestChestPosition = chestPosition.toImmutable();
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return nearestChestPosition;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private boolean areNearbyChunksLoaded(ClientWorld clientWorld, BlockPos chestPosition) {
      int firstChunkX = ChunkSectionPos.getSectionCoord(chestPosition.getX() - 5);
      int lastChunkX = ChunkSectionPos.getSectionCoord(chestPosition.getX() + 5);
      int firstChunkZ = ChunkSectionPos.getSectionCoord(chestPosition.getZ() - 5);
      int lastChunkZ = ChunkSectionPos.getSectionCoord(chestPosition.getZ() + 5);

      for (int chunkX = firstChunkX; chunkX <= lastChunkX; chunkX++) {
         for (int chunkZ = firstChunkZ; chunkZ <= lastChunkZ; chunkZ++) {
            if (!clientWorld.isChunkLoaded(chunkX, chunkZ)) {
               return false;
            }
         }
      }

      return true;
   }
// https://t.me/pasterplace

   private boolean isDungeonChest(ClientWorld clientWorld, BlockPos chestPosition) {
      BlockState blockState = clientWorld.getBlockState(chestPosition);
      if (!blockState.isOf(Blocks.CHEST) && !blockState.isOf(Blocks.TRAPPED_CHEST)) {
         return false;
      }

      if (chestPosition.getY() > 320) {
         return false;
      }

      BlockPos spaceAboveChest = chestPosition.up();
      if (!clientWorld.getBlockState(spaceAboveChest).getCollisionShape(clientWorld, spaceAboveChest).isEmpty()) {
         return false;
      }

      for (int offsetX = -5; offsetX <= 5; offsetX++) {
         for (int offsetY = -3; offsetY <= 3; offsetY++) {
            for (int offsetZ = -5; offsetZ <= 5; offsetZ++) {
               if (clientWorld.getBlockState(chestPosition.add(offsetX, offsetY, offsetZ)).isOf(Blocks.SPAWNER)) {
                  return true;
               }
            }
         }
      }
// https://t.me/pasterplace

      int dungeonBlockCount = 0;

      for (int offsetX = -2; offsetX <= 2; offsetX++) {
         for (int offsetY = -1; offsetY <= 2; offsetY++) {
            for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
               BlockState blockState2 = clientWorld.getBlockState(chestPosition.add(offsetX, offsetY, offsetZ));
               if (blockState2.isOf(Blocks.STONE_BRICKS)
                  || blockState2.isOf(Blocks.MOSSY_STONE_BRICKS)
                  || blockState2.isOf(Blocks.CRACKED_STONE_BRICKS)
                  || blockState2.isOf(Blocks.CHISELED_STONE_BRICKS)
                  || blockState2.isOf(Blocks.TUFF_BRICKS)
                  || blockState2.isOf(Blocks.DEEPSLATE_BRICKS)
                  || blockState2.isOf(Blocks.COBWEB)
                  || blockState2.isOf(Blocks.RAIL)
                  || blockState2.isOf(Blocks.OAK_PLANKS)
                  || blockState2.isOf(Blocks.DARK_OAK_PLANKS)
                  || blockState2.isOf(Blocks.OAK_FENCE)
                  || blockState2.isOf(Blocks.DARK_OAK_FENCE)
                  || blockState2.isOf(Blocks.MOSSY_COBBLESTONE)
                  || blockState2.isOf(Blocks.COBBLESTONE)) {
                  if (++dungeonBlockCount >= 3) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }
// https://t.me/pasterplace

   private boolean isContainerInProtectedRegion(ClientWorld clientWorld, BlockPos containerPosition) {
      for (int box = -6; box <= 6; box++) {
         for (int offsetY = -4; offsetY <= 4; offsetY++) {
            for (int offsetZ = -6; offsetZ <= 6; offsetZ++) {
               BlockState blockState = clientWorld.getBlockState(containerPosition.add(box, offsetY, offsetZ));
               if (blockState.isOf(Blocks.ANCIENT_DEBRIS)
                  || blockState.isOf(Blocks.CRYING_OBSIDIAN)
                  || blockState.isOf(Blocks.RESPAWN_ANCHOR)
                  || blockState.isOf(Blocks.BEACON)
                  || blockState.isOf(Blocks.EMERALD_ORE)
                  || blockState.isOf(Blocks.DEEPSLATE_EMERALD_ORE)
                  || blockState.isOf(Blocks.EMERALD_BLOCK)
                  || blockState.isOf(Blocks.NETHERITE_BLOCK)) {
                  return true;
               }
            }
         }
      }

      Box box = new Box(containerPosition).expand(8.0);

      for (Entity nearbyEntity : clientWorld.getOtherEntities((Entity)null, box, entity -> entity instanceof ArmorStandEntity || entity instanceof TextDisplayEntity)) {
         String nearbyLabel = "";
         if (nearbyEntity.hasCustomName() && nearbyEntity.getCustomName() != null) {
            nearbyLabel = nearbyEntity.getCustomName().getString().toLowerCase(Locale.ROOT);
         } else if (nearbyEntity instanceof TextDisplayEntity) {
            TextDisplayEntity textDisplay = (TextDisplayEntity)nearbyEntity;
            if (textDisplay.getText() != null) {
               nearbyLabel = textDisplay.getText().getString().toLowerCase(Locale.ROOT);
            }
         }

         if (this.isProtectionRelatedText(nearbyLabel)) {
            return true;
         }
      }

      for (int offsetX = -6; offsetX <= 6; offsetX++) {
         for (int offsetY = -4; offsetY <= 4; offsetY++) {
            for (int offsetZ = -6; offsetZ <= 6; offsetZ++) {
               BlockEntity blockEntity = clientWorld.getBlockEntity(containerPosition.add(offsetX, offsetY, offsetZ));
               if (blockEntity instanceof SignBlockEntity) {
                  SignBlockEntity blockEntity2 = (SignBlockEntity)blockEntity;
                  if (this.containsProtectionText(blockEntity2)) {
                     return true;
                  }
               }
            }
         }
      }

      return this.hasNearbyPlayerBaseStructures(clientWorld, containerPosition, 4);
   }
// https://t.me/pasterplace

   private boolean hasNearbyPlayerBaseStructures(ClientWorld clientWorld, BlockPos centerPosition, int radiusBlocks) {
      for (int offsetX = -radiusBlocks; offsetX <= radiusBlocks; offsetX++) {
         for (int offsetY = -radiusBlocks; offsetY <= radiusBlocks; offsetY++) {
            for (int offsetZ = -radiusBlocks; offsetZ <= radiusBlocks; offsetZ++) {
               BlockState blockState = clientWorld.getBlockState(centerPosition.add(offsetX, offsetY, offsetZ));
               if (blockState.isOf(Blocks.CRAFTING_TABLE)
                  || blockState.isOf(Blocks.FURNACE)
                  || blockState.isOf(Blocks.BLAST_FURNACE)
                  || blockState.isOf(Blocks.SMOKER)
                  || blockState.isOf(Blocks.BARREL)
                  || blockState.isOf(Blocks.HOPPER)
                  || blockState.isOf(Blocks.ANVIL)
                  || blockState.isOf(Blocks.CHIPPED_ANVIL)
                  || blockState.isOf(Blocks.DAMAGED_ANVIL)
                  || blockState.isOf(Blocks.BREWING_STAND)
                  || blockState.isOf(Blocks.ENCHANTING_TABLE)
                  || blockState.getBlock() instanceof DoorBlock
                  || blockState.getBlock() instanceof BedBlock) {
                  return true;
               }
            }
         }
      }

      return false;
   }
// https://t.me/pasterplace

   private boolean containsProtectionText(SignBlockEntity signBlockEntity) {
      for (boolean frontSide : new boolean[]{true, false}) {
         for (Text lineText : signBlockEntity.getText(frontSide).getMessages(false)) {
            if (this.isProtectionRelatedText(lineText.getString())) {
               return true;
            }
         }
      }

      return false;
   }
// https://t.me/pasterplace

   private boolean isProtectionRelatedText(String candidateText) {
      if (candidateText != null && !candidateText.isBlank()) {
         String normalizedText = candidateText.toLowerCase(Locale.ROOT);
         return normalizedText.contains("приват")
            || normalizedText.contains("запривач")
            || normalizedText.contains("private")
            || normalizedText.contains("замок")
            || normalizedText.contains("lock")
            || normalizedText.contains("регион")
            || normalizedText.contains("rg")
            || normalizedText.contains("владелец")
            || normalizedText.contains("участник")
            || normalizedText.contains("holyworld")
            || normalizedText.contains("reallyworld")
            || normalizedText.contains("до конца")
            || normalizedText.contains("сетка");
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   private boolean isAccessDeniedMessage(String messageText) {
      return messageText.contains("приват")
         || messageText.contains("запривач")
         || messageText.contains("регион")
         || messageText.contains("чужой")
         || messageText.contains("запрещен")
         || messageText.contains("нельзя")
         || messageText.contains("не можете")
         || messageText.contains("нет прав")
         || messageText.contains("нет доступа")
         || messageText.contains("private")
         || messageText.contains("region")
         || messageText.contains("protected")
         || messageText.contains("denied")
         || messageText.contains("cannot open")
         || messageText.contains("locked")
         || messageText.contains("no permission")
         || messageText.contains("not allowed");
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private String extractPacketMessageText(Object packet) {
      if (!(packet instanceof GameMessageS2CPacket gameMessagePacket)) {
         if (!(packet instanceof ProfilelessChatMessageS2CPacket profilelessChatPacket)) {
            if (!(packet instanceof ChatMessageS2CPacket signedChatPacket)) {
               return null;
            } else {
               return signedChatPacket.unsignedContent() == null ? signedChatPacket.body().content() : signedChatPacket.unsignedContent().getString();
            }
         } else {
            return profilelessChatPacket.message().getString();
         }
      } else {
         return gameMessagePacket.content().getString();
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private Vec3d findSafeStandingPositionAtChest(ClientPlayerEntity clientPlayerEntity, ClientWorld clientWorld, BlockPos chestPosition) {
      EntityDimensions entityDimensions = clientPlayerEntity.getDimensions(EntityPose.STANDING);
      Vec3d chestCenter = Vec3d.ofCenter(chestPosition);
      Vec3d standingPositionAboveChest = new Vec3d(chestCenter.x, chestPosition.getY() + 1.0, chestCenter.z);
      if (clientWorld.isSpaceEmpty(clientPlayerEntity, entityDimensions.getBoxAt(standingPositionAboveChest))) {
         return standingPositionAboveChest;
      }

      for (Direction direction : net.minecraft.util.math.Direction.Type.HORIZONTAL) {
         for (double heightOffset = 0.0; heightOffset <= 1.0; heightOffset++) {
            Vec3d adjacentStandingPosition = new Vec3d(chestCenter.x + direction.getOffsetX(), chestPosition.getY() + heightOffset, chestCenter.z + direction.getOffsetZ());
            if (clientWorld.isSpaceEmpty(clientPlayerEntity, entityDimensions.getBoxAt(adjacentStandingPosition))) {
               return adjacentStandingPosition;
            }
         }
      }

      return null;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private Vec3d findSafeStandingPositionAtMinecart(ClientPlayerEntity clientPlayerEntity, ClientWorld clientWorld, ChestMinecartEntity chestMinecartEntity) {
      EntityDimensions entityDimensions = clientPlayerEntity.getDimensions(EntityPose.STANDING);
      Vec3d minecartPosition = chestMinecartEntity.getEntityPos();
      Vec3d minecartStandingPosition = new Vec3d(minecartPosition.x, chestMinecartEntity.getBoundingBox().maxY, minecartPosition.z);
      if (clientWorld.isSpaceEmpty(clientPlayerEntity, entityDimensions.getBoxAt(minecartStandingPosition))) {
         return minecartStandingPosition;
      }

      for (Direction direction : net.minecraft.util.math.Direction.Type.HORIZONTAL) {
         Vec3d adjacentStandingPosition = new Vec3d(minecartPosition.x + direction.getOffsetX() * 1.2, Math.floor(minecartPosition.y), minecartPosition.z + direction.getOffsetZ() * 1.2);
         if (clientWorld.isSpaceEmpty(clientPlayerEntity, entityDimensions.getBoxAt(adjacentStandingPosition))) {
            return adjacentStandingPosition;
         }
      }

      Vec3d fallbackStandingPosition = new Vec3d(minecartPosition.x, Math.floor(minecartPosition.y) + 1.0, minecartPosition.z);
      return !clientWorld.isSpaceEmpty(clientPlayerEntity, entityDimensions.getBoxAt(fallbackStandingPosition)) ? null : fallbackStandingPosition;
   }
// https://t.me/pasterplace

   private void requestDownwardInteractionRotation(ClientPlayerEntity clientPlayerEntity) {
      this.requestDownwardFlightRotation(clientPlayerEntity);
      this.containerInteractionAttempted = true;
   }
// https://t.me/pasterplace

   private void runContainerInteraction(ClientPlayerEntity clientPlayerEntity, Runnable runnable) {
      runnable.run();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void markContainerLooted() {
      if (this.currentContainerPosition != null) {
         String containerKey = this.currentContainerType != TpKeyFinder.ContainerActionResult.CHEST_MINECART ? this.chestPositionKey(this.currentContainerPosition) : this.minecartPositionKey(this.currentContainerPosition);
         if (this.visitedContainerKeys.add(containerKey)) {
            if (this.currentContainerType == TpKeyFinder.ContainerActionResult.STRUCTURE_CHEST && CLIENT.world != null) {
               this.markAdjacentChestVisited(CLIENT.world, this.currentContainerPosition);
            }
// https://t.me/pasterplace

            this.saveVisitedContainers();
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void markAdjacentChestVisited(ClientWorld clientWorld, BlockPos chestPosition) {
      BlockState blockState = clientWorld.getBlockState(chestPosition);
      if (blockState.contains(ChestBlock.CHEST_TYPE) && blockState.contains(ChestBlock.FACING)) {
         ChestType comparable = blockState.get(ChestBlock.CHEST_TYPE);
         if (comparable != ChestType.SINGLE) {
            Direction comparable2 = blockState.get(ChestBlock.FACING);
            Direction partnerChestDirection = comparable != ChestType.LEFT ? comparable2.rotateYCounterclockwise() : comparable2.rotateYClockwise();
            this.visitedContainerKeys.add(this.chestPositionKey(chestPosition.offset(partnerChestDirection)));
         }
      }
   }
// https://t.me/pasterplace

   private boolean isWantedLootItem(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }

      if (this.lootCategories.isOptionEnabled("Зачарованное золотое яблоко") && stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
         return true;
      }

      if (this.lootCategories.isOptionEnabled("Золотое яблоко") && stack.isOf(Items.GOLDEN_APPLE)) {
         return true;
      }

      if (this.lootCategories.isOptionEnabled("Ключ карты")) {
         String normalizedItemName = stack.getName().getString().toLowerCase(Locale.ROOT);
         if (stack.isOf(Items.NAME_TAG) || stack.isOf(Items.TRIPWIRE_HOOK) || stack.isOf(Items.PAPER)) {
            return normalizedItemName.contains("ключ") || normalizedItemName.contains("key") || normalizedItemName.contains("карта");
         }
      }

      return false;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private int countWantedItemsInInventory() {
      if (CLIENT.player != null) {
         int wantedItemCount = 0;

         for (int inventorySlotIndex = 0; inventorySlotIndex < CLIENT.player.getInventory().size(); inventorySlotIndex++) {
            ItemStack stack = CLIENT.player.getInventory().getStack(inventorySlotIndex);
            if (this.isWantedLootItem(stack)) {
               wantedItemCount += stack.getCount();
            }
         }

         return wantedItemCount;
      } else {
         return 0;
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void resetSearchProgress() {
      this.spiralOffsetX = 0;
      this.spiralOffsetZ = 0;
      this.spiralStepX = 1;
      this.spiralStepZ = 0;
      this.spiralSegmentLength = 1;
      this.spiralSegmentProgress = 0;
      this.spiralTurnsInSegment = 0;
      this.searchEdgeDirectionX = 0.0;
      this.searchEdgeDirectionZ = 0.0;
      this.worldBorderFallbackStepsRemaining = 0;
      this.worldBorderDirectionSign = 1;
      this.nextSearchPosition = null;
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void initializeSearchDirectionFromYaw(ClientPlayerEntity clientPlayerEntity) {
      float yawRadians;
      double yawDirectionX = -Math.sin(yawRadians = (float)Math.toRadians(clientPlayerEntity.getYaw()));
      double yawDirectionZ = Math.cos(yawRadians);
      double directionLength;
      if (!((directionLength = Math.hypot(yawDirectionX, yawDirectionZ)) < 1.0E-6)) {
         this.searchDirectionX = yawDirectionX / directionLength;
         this.searchDirectionZ = yawDirectionZ / directionLength;
      } else {
         this.searchDirectionX = 1.0;
         this.searchDirectionZ = 0.0;
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void advanceSearchSpiral() {
      this.spiralOffsetX = this.spiralOffsetX + this.spiralStepX * 6;
      this.spiralOffsetZ = this.spiralOffsetZ + this.spiralStepZ * 6;
      if (++this.spiralSegmentProgress >= this.spiralSegmentLength) {
         this.spiralSegmentProgress = 0;
         int previousStepX = this.spiralStepX;
         this.spiralStepX = -this.spiralStepZ;
         this.spiralStepZ = previousStepX;
         if (++this.spiralTurnsInSegment - 2 == 0) {
            this.spiralSegmentLength++;
            this.searchEdgeDirectionX = this.searchEdgeDirectionX + this.searchDirectionX * 4.0;
            this.searchEdgeDirectionZ = this.searchEdgeDirectionZ + this.searchDirectionZ * 4.0;
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private Vec3d getNextSearchPosition() {
      WorldBorder worldBorder = CLIENT.world.getWorldBorder();

      for (int directionAttempt = 0; directionAttempt < 4; directionAttempt++) {
         double directionX = this.worldBorderFallbackStepsRemaining <= 0 ? this.searchDirectionX : this.worldBorderFallbackDirectionX;
         double directionZ = this.worldBorderFallbackStepsRemaining <= 0 ? this.searchDirectionZ : this.worldBorderFallbackDirectionZ;
         double nextX = this.searchCursorPosition.x + directionX * 96.0;
         double nextZ = this.searchCursorPosition.z + directionZ * 96.0;
         if (worldBorder.contains(nextX, nextZ)) {
            if (this.worldBorderFallbackStepsRemaining > 0 && --this.worldBorderFallbackStepsRemaining == 0) {
               this.searchDirectionX = -this.searchDirectionX;
               this.searchDirectionZ = -this.searchDirectionZ;
            }

            return new Vec3d(nextX, this.searchHeightY, nextZ);
         }

         if (this.worldBorderFallbackStepsRemaining <= 0) {
            this.worldBorderFallbackDirectionX = -this.searchDirectionZ * this.worldBorderDirectionSign;
            this.worldBorderFallbackDirectionZ = this.searchDirectionX * this.worldBorderDirectionSign;
            this.worldBorderDirectionSign = -this.worldBorderDirectionSign;
            this.worldBorderFallbackStepsRemaining = 2;
            if (this.showChatNotifications.getValue()) {
               ClientChatLogger.II1Il11l111II11IIl("Край мира, меняем направление.");
            }
         } else {
            this.worldBorderFallbackDirectionX = -this.worldBorderFallbackDirectionX;
            this.worldBorderFallbackDirectionZ = -this.worldBorderFallbackDirectionZ;
         }
      }

      return null;
   }
// https://t.me/pasterplace

   private boolean hasChunkBottomBlocksLoaded(ClientWorld clientWorld, int chunkX, int chunkZ) {
      WorldChunk worldChunk = clientWorld.getChunk(chunkX, chunkZ);
      if (worldChunk.isEmpty()) {
         return false;
      }

      int blockOriginX = ChunkSectionPos.getBlockCoord(chunkX);
      int blockOriginZ = ChunkSectionPos.getBlockCoord(chunkZ);
      Mutable mutable = new Mutable();
// https://t.me/pasterplace

      for (int sampleCorner = 0; sampleCorner < 4; sampleCorner++) {
         mutable.set(blockOriginX + (sampleCorner & 1) * 15, clientWorld.getBottomY(), blockOriginZ + (sampleCorner >> 1) * 15);
         if (!worldChunk.getBlockState(mutable).isAir()) {
            return true;
         }
      }

      return false;
   }
// https://t.me/pasterplace

   private boolean isSearchAreaReady(ClientWorld clientWorld, double searchX, double searchZ, int chunkRadius) {
      int searchChunkX = ChunkSectionPos.getSectionCoordFloored(searchX);
      int searchChunkZ = ChunkSectionPos.getSectionCoordFloored(searchZ);
      if (CLIENT.player != null) {
         int maximumViewDistance = Math.max(2, CLIENT.options.getClampedViewDistance() - 2);
         int playerChunkX = ChunkSectionPos.getSectionCoordFloored(CLIENT.player.getX());
         int playerChunkZ = ChunkSectionPos.getSectionCoordFloored(CLIENT.player.getZ());
         if (Math.abs(searchChunkX - playerChunkX) + chunkRadius > maximumViewDistance || Math.abs(searchChunkZ - playerChunkZ) + chunkRadius > maximumViewDistance) {
            return false;
         }
      }

      int playerTickAge = CLIENT.player == null ? 0 : CLIENT.player.age;
      boolean allChunksReady = true;
// https://t.me/pasterplace

      for (int chunkX = searchChunkX - chunkRadius; chunkX <= searchChunkX + chunkRadius; chunkX++) {
         for (int chunkZ = searchChunkZ - chunkRadius; chunkZ <= searchChunkZ + chunkRadius; chunkZ++) {
            long packedChunkPosition = ChunkPos.toLong(chunkX, chunkZ);
            if (clientWorld.isChunkLoaded(chunkX, chunkZ) && this.hasChunkBottomBlocksLoaded(clientWorld, chunkX, chunkZ)) {
               Integer previousCheckTick = this.chunkBottomCheckTicks.putIfAbsent(packedChunkPosition, playerTickAge);
               if (playerTickAge - (previousCheckTick == null ? playerTickAge : previousCheckTick) < 10) {
                  allChunksReady = false;
               }
            } else {
               this.chunkBottomCheckTicks.remove(packedChunkPosition);
               allChunksReady = false;
            }
         }
      }

      if (this.chunkBottomCheckTicks.size() > 8192) {
         this.chunkBottomCheckTicks.clear();
      }

      return allChunksReady;
   }
// https://t.me/pasterplace

   private void markNearbyContainersVisited(BlockPos referencePosition) {
      ClientWorld clientWorld = CLIENT.world;
      if (referencePosition != null && clientWorld != null) {
         double visitRadiusSquared = 256.0;
         int chunkRadius = (int)Math.ceil(1.0);
         int centerChunkX = ChunkSectionPos.getSectionCoord(referencePosition.getX());
         int centerChunkZ = ChunkSectionPos.getSectionCoord(referencePosition.getZ());
         int newlyVisitedCount = 0;

         for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
               if (clientWorld.isChunkLoaded(chunkX, chunkZ)) {
               for (BlockPos chestPosition : clientWorld.getChunk(chunkX, chunkZ).getBlockEntities().keySet()) {
                  BlockState blockState = clientWorld.getBlockState(chestPosition);
                     if ((blockState.isOf(Blocks.CHEST) || blockState.isOf(Blocks.TRAPPED_CHEST))
                        && chestPosition.getSquaredDistance(referencePosition) <= visitRadiusSquared
                     && this.visitedContainerKeys.add(this.chestPositionKey(chestPosition))) {
                        newlyVisitedCount++;
                     }
                  }
               }
            }
         }

         for (Entity entity : clientWorld.getEntities()) {
            if (entity instanceof ChestMinecartEntity) {
               ChestMinecartEntity chestMinecart = (ChestMinecartEntity)entity;
               if (chestMinecart.getBlockPos().getSquaredDistance(referencePosition) <= visitRadiusSquared && this.visitedContainerKeys.add(this.minecartPositionKey(chestMinecart.getBlockPos()))) {
                  newlyVisitedCount++;
               }
            }
         }

         if (newlyVisitedCount > 0) {
            this.saveVisitedContainers();
            if (this.showChatNotifications.getValue()) {
               ClientChatLogger.II1Il11l111II11IIl("Рядом с приватом пропущено целей: " + newlyVisitedCount);
            }
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
    private void transitionToStateAtPosition(Vec3d targetPosition, TpKeyFinder.ContainerScanState nextScanState) {
      if (this.setSearchCursorIfLoaded(targetPosition)) {
          this.scanState = nextScanState;
          if (nextScanState != TpKeyFinder.ContainerScanState.IDLE) {
            this.stateTransitionDelayTicks = 2;
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private boolean setSearchCursorIfLoaded(Vec3d searchPosition) {
      if (CLIENT.world != null && !this.isSearchAreaReady(CLIENT.world, searchPosition.x, searchPosition.z, 0)) {
         return false;
      }

      this.searchCursorPosition = searchPosition;
      return true;
   }
// https://t.me/pasterplace

   private static int calculateMovementPacketCount(double distance) {
      return Math.max(1, (int)Math.ceil(distance / 9.5));
   }
// https://t.me/pasterplace

   private static void sendPositionUpdatePackets(Vec3d position) {
      PacketSender.l1I1IlllIlI(new PositionAndOnGround(position.x, position.y, position.z, false, false));
      PacketSender.l1I1IlllIlI(ClientTickEndC2SPacket.INSTANCE);
   }
// https://t.me/pasterplace

   private double horizontalDistanceSquared(Vec3d firstPosition, Vec3d secondPosition) {
      double deltaX = firstPosition.x - secondPosition.x;
      double deltaZ = firstPosition.z - secondPosition.z;
      return deltaX * deltaX + deltaZ * deltaZ;
   }
// https://t.me/pasterplace

   private double horizontalDistance(Vec3d firstPosition, Vec3d secondPosition) {
      return Math.hypot(firstPosition.x - secondPosition.x, firstPosition.z - secondPosition.z);
   }
// https://t.me/pasterplace

   private String chestPositionKey(BlockPos chestPosition) {
      return "chest:" + chestPosition.getX() + ":" + chestPosition.getY() + ":" + chestPosition.getZ();
   }
// https://t.me/pasterplace

   private String minecartPositionKey(BlockPos minecartPosition) {
      return "minecart:" + minecartPosition.getX() + ":" + minecartPosition.getY() + ":" + minecartPosition.getZ();
   }
// https://t.me/pasterplace

   private Path visitedContainerFile() {
      return FabricLoader.getInstance().getGameDir().resolve("daamky").resolve("temp").resolve("tpkeyfinder.txt");
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void loadVisitedContainers() {
      this.visitedContainerKeys.clear();
      Path path = this.visitedContainerFile();
      if (Files.exists(path)) {
         try {
            for (String storedKey : Files.readAllLines(path, StandardCharsets.UTF_8)) {
               String containerKey = storedKey.trim();
               if (!containerKey.isEmpty()) {
                  this.visitedContainerKeys.add(containerKey);
               }
            }
         } catch (IOException ignoredReadException) {
         }
      }
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.BALANCED)
   private void saveVisitedContainers() {
      Path path = this.visitedContainerFile();

      try {
         Files.createDirectories(path.getParent());
          List<String> visitedKeys = this.visitedContainerKeys.stream().sorted().toList();
          Files.write(path, visitedKeys, StandardCharsets.UTF_8);
      } catch (IOException ignoredWriteException) {
      }
   }
// https://t.me/pasterplace

   private static final class PackedBlockPosition {
      private static long packChunkCoordinates(int chunkX, int chunkZ) {
         return chunkX & 4294967295L | (chunkZ & 4294967295L) << 32;
      }
   }
// https://t.me/pasterplace

   private enum ContainerScanState {
      IDLE,
      DESCENDING_TO_SEARCH_HEIGHT,
      SCANNING_FOR_CONTAINERS,
      TRAVELING_TO_CONTAINER,
      POSITIONING_AT_CONTAINER,
      OPENING_CONTAINER,
      LOOTING_CONTAINER,
      REQUESTING_ENDER_CHEST,
      OPENING_ENDER_MENU,
      LOOTING_ENDER_CHEST;

      // $VF: synthetic method
      private static TpKeyFinder.ContainerScanState[] createContainerScanStateValues() {
         return new TpKeyFinder.ContainerScanState[]{
            IDLE,
            DESCENDING_TO_SEARCH_HEIGHT,
            SCANNING_FOR_CONTAINERS,
            TRAVELING_TO_CONTAINER,
            POSITIONING_AT_CONTAINER,
            OPENING_CONTAINER,
            LOOTING_CONTAINER,
            REQUESTING_ENDER_CHEST,
            OPENING_ENDER_MENU,
            LOOTING_ENDER_CHEST
         };
      }
   }
// https://t.me/pasterplace

   private enum ContainerActionResult {
      CHEST_MINECART,
      STRUCTURE_CHEST;

      // $VF: synthetic method
      private static TpKeyFinder.ContainerActionResult[] createContainerActionResultValues() {
         return new TpKeyFinder.ContainerActionResult[]{CHEST_MINECART, STRUCTURE_CHEST};
      }
   }
// https://t.me/pasterplace

   public final class ContainerKeyTracker {
      private boolean previousContainerScreenOpen;
      private String containerUnderCrosshairKey;
      private BlockPos containerUnderCrosshairPosition;

      @EventTarget
      public void onTick(EventTick eventTick) {
         if (TpKeyFinder.CLIENT.player != null && TpKeyFinder.CLIENT.world != null) {
            boolean isContainerScreenOpen = TpKeyFinder.CLIENT.player.currentScreenHandler instanceof GenericContainerScreenHandler;
            if (!isContainerScreenOpen) {
               this.captureContainerUnderCrosshair();
            } else if (!this.previousContainerScreenOpen && this.containerUnderCrosshairKey != null && TpKeyFinder.this.scanState == TpKeyFinder.ContainerScanState.IDLE) {
               if (!TpKeyFinder.this.isEnabled()) {
                  TpKeyFinder.this.loadVisitedContainers();
               }

               if (TpKeyFinder.this.visitedContainerKeys.add(this.containerUnderCrosshairKey)) {
                  if (this.containerUnderCrosshairPosition != null) {
                     TpKeyFinder.this.markAdjacentChestVisited(TpKeyFinder.CLIENT.world, this.containerUnderCrosshairPosition);
                  }

                  TpKeyFinder.this.saveVisitedContainers();
               }
            }

            this.previousContainerScreenOpen = isContainerScreenOpen;
         } else {
            this.previousContainerScreenOpen = false;
            this.containerUnderCrosshairKey = null;
            this.containerUnderCrosshairPosition = null;
         }
      }
// https://t.me/pasterplace

      @Allow2Generation(level = GenerationLevel.BALANCED)
      private void captureContainerUnderCrosshair() {
         this.containerUnderCrosshairKey = null;
         this.containerUnderCrosshairPosition = null;
         HitResult hitResult = TpKeyFinder.CLIENT.crosshairTarget;
         if (hitResult instanceof BlockHitResult) {
            BlockHitResult hitResult2 = (BlockHitResult)hitResult;
            if (TpKeyFinder.CLIENT.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
               BlockState blockState = TpKeyFinder.CLIENT.world.getBlockState(hitResult2.getBlockPos());
               if (blockState.isOf(Blocks.CHEST) || blockState.isOf(Blocks.TRAPPED_CHEST)) {
                  this.containerUnderCrosshairPosition = hitResult2.getBlockPos().toImmutable();
                  this.containerUnderCrosshairKey = TpKeyFinder.this.chestPositionKey(this.containerUnderCrosshairPosition);
               }

               return;
            }
         }

         hitResult = TpKeyFinder.CLIENT.crosshairTarget;
         if (hitResult instanceof EntityHitResult) {
            EntityHitResult hitResult4 = (EntityHitResult)hitResult;
            Entity entity = hitResult4.getEntity();
            if (entity instanceof ChestMinecartEntity) {
               ChestMinecartEntity entity2 = (ChestMinecartEntity)entity;
               this.containerUnderCrosshairKey = TpKeyFinder.this.minecartPositionKey(entity2.getBlockPos());
            }
         }
      }
   }
}

