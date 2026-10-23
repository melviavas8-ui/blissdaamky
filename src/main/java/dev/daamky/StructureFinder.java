package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventUpdate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class StructureFinder extends ModuleStructure {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final int MAX_CHUNKS_PER_UPDATE = 6;
   private static final int MAX_CANDIDATE_CHECKS_PER_UPDATE = 4000;
   private final FloatSetting searchRadius = new FloatSetting("Размер области", 16000.0F, 1000.0F, 60000.0F, 500.0F);
   private final BooleanSetting centerRouteOnPlayer = new BooleanSetting("Центр от игрока", false);
   private final FloatSetting flightRouteSpacing = new FloatSetting("Шаг ряда", 160.0F, 32.0F, 512.0F, 16.0F);
   private final FloatSetting flightAltitude = new FloatSetting("Высота полёта", 200.0F, 64.0F, 320.0F, 1.0F);
   private final FloatSetting flightSpeed = new FloatSetting("Скорость", 1.0F, 0.2F, 100.0F, 0.1F);
   private final FloatSetting minimumMatchPercent = new FloatSetting("Совпадение %", 70.0F, 10.0F, 100.0F, 1.0F);
   private final BooleanSetting searchEnabled = new BooleanSetting("Поиск", false);
   private final List<int[]> structureTemplate = new ArrayList<>();
   private final Map<Block, Integer> templateBlockCounts = new HashMap<>();
   private final Map<Block, List<int[]>> templatePositionsByBlock = new HashMap<>();
   private final Set<Long> loadedChunkIds = new HashSet<>();
   private final List<Long> rarestBlockAnchorPositions = new ArrayList<>();
   private final List<double[]> flightRoute = new ArrayList<>();
   private final Stopwatch flightCommandCooldown = new Stopwatch();
   private Block rarestTemplateBlock;
   private int flightRouteIndex;
   private int anchorPositionIndex;
   private boolean structureFound;
// https://t.me/pasterplace

   public StructureFinder() {
      super("StructureFinder", "Ищет сохранённую структуру полётом по миру", ModuleCategory.WORLD);
      this.addSettings(
         this.searchRadius, this.centerRouteOnPlayer, this.flightRouteSpacing, this.flightAltitude, this.flightSpeed, this.minimumMatchPercent, this.searchEnabled
      );
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onEnable() {
      this.loadStructureTemplate();
      this.resetSearchState();
   }
// https://t.me/pasterplace

   @Allow2Generation(level = GenerationLevel.FAST)
   @Override
   protected void onDisable() {
      this.searchEnabled.setValue(false);
      if (CLIENT.player != null) {
         CLIENT.player.setVelocity(Vec3d.ZERO);
      }
   }
// https://t.me/pasterplace

   @EventTarget
   @Allow2Generation(level = GenerationLevel.FAST)
   public void onUpdate(EventUpdate eventUpdate) {
      if (this.hasActiveClientWorld()) {
         if (this.searchEnabled.getValue()) {
            if (!this.structureTemplate.isEmpty()) {
               if (this.flightRoute.isEmpty()) {
                  this.buildFlightRoute();
               }

               this.loadNearbyChunks();
               this.scanStructureCandidates();
               if (!this.structureFound) {
                  this.advanceFlightRoute();
               }
            } else {
               ClientChatLogger.l1I1IlllIlI("Шаблон структуры не загружен");
               this.searchEnabled.setValue(false);
            }
         } else {
            if (!this.flightRoute.isEmpty()) {
               this.resetSearchState();
            }
         }
      }
   }
// https://t.me/pasterplace

   private void resetSearchState() {
      this.flightRoute.clear();
      this.loadedChunkIds.clear();
      this.rarestBlockAnchorPositions.clear();
      this.anchorPositionIndex = 0;
      this.flightRouteIndex = 0;
      this.structureFound = false;
   }
// https://t.me/pasterplace

   private void indexTemplateBlocksByFrequency() {
      this.templateBlockCounts.clear();
      this.templatePositionsByBlock.clear();
// https://t.me/pasterplace

      for (Object templatePosition : this.structureTemplate) {
         Object templateBlock = (Block)Registries.BLOCK.get((int)((Object[])templatePosition)[3]);
         this.templateBlockCounts.merge((Block)templateBlock, 1, Integer::sum);
         this.templatePositionsByBlock.computeIfAbsent((Block)templateBlock, block -> new ArrayList<>()).add((int[])templatePosition);
      }

      this.rarestTemplateBlock = null;
      int lowestBlockCount = Integer.MAX_VALUE;

      for (java.util.Map.Entry<?, ?> blockCountEntry : this.templateBlockCounts.entrySet()) {
         if ((Integer)blockCountEntry.getValue() < lowestBlockCount) {
            lowestBlockCount = (Integer)blockCountEntry.getValue();
            this.rarestTemplateBlock = (Block)blockCountEntry.getKey();
         }
      }
   }
// https://t.me/pasterplace

   private void loadStructureTemplate() {
      if (this.structureTemplate.isEmpty()) {
         try (InputStream templateInputStream = StructureFinder.class.getResourceAsStream("/assets/daamky/structure/structure.txt")) {
            if (templateInputStream != null) {
               BufferedReader templateReader = new BufferedReader(new InputStreamReader(templateInputStream, StandardCharsets.UTF_8));

               String templateLine;
               while ((templateLine = templateReader.readLine()) != null) {
                  String[] templateFields = templateLine.trim().split(" ");
                  if (templateFields.length == 4) {
                     Identifier blockIdentifier = Identifier.tryParse(templateFields[3]);
                     if (blockIdentifier != null) {
                         Block templateBlock = Registries.BLOCK.get(blockIdentifier);
                        this.structureTemplate
                           .add(
                              new int[]{
                                 Integer.parseInt(templateFields[0]),
                                 Integer.parseInt(templateFields[1]),
                                 Integer.parseInt(templateFields[2]),
                                 Registries.BLOCK.getRawId(templateBlock)
                              }
                           );
                     }
                  }
               }

               if (!this.structureTemplate.isEmpty()) {
                  this.indexTemplateBlocksByFrequency();
               }
            }
         } catch (IOException | NumberFormatException templateReadException) {
            this.structureTemplate.clear();
         }
      }
   }
// https://t.me/pasterplace

   private void buildFlightRoute() {
      double halfSearchRadius = this.searchRadius.getValue().floatValue() / 2.0;
      double routeCenterX = this.centerRouteOnPlayer.getValue() ? CLIENT.player.getX() : 0.0;
      double routeCenterZ = this.centerRouteOnPlayer.getValue() ? CLIENT.player.getZ() : 0.0;
      double routeSpacing = this.flightRouteSpacing.getValue().floatValue();
      boolean scanRowFromMinZ = true;

      for (double routeX = routeCenterX - halfSearchRadius; routeX <= routeCenterX + halfSearchRadius; routeX += routeSpacing) {
         this.flightRoute.add(new double[]{routeX, scanRowFromMinZ ? routeCenterZ - halfSearchRadius : routeCenterZ + halfSearchRadius});
         this.flightRoute.add(new double[]{routeX, scanRowFromMinZ ? routeCenterZ + halfSearchRadius : routeCenterZ - halfSearchRadius});
         scanRowFromMinZ = !scanRowFromMinZ;
      }

      double nearestRoutePointDistanceSquared = Double.MAX_VALUE;

      for (int routePointIndex = 0; routePointIndex < this.flightRoute.size(); routePointIndex++) {
          double[] routePoint = this.flightRoute.get(routePointIndex);
          double playerDistanceSquared = MathHelper.square(routePoint[0] - CLIENT.player.getX())
             + MathHelper.square(routePoint[1] - CLIENT.player.getZ());
         if (playerDistanceSquared < nearestRoutePointDistanceSquared) {
            nearestRoutePointDistanceSquared = playerDistanceSquared;
            this.flightRouteIndex = routePointIndex;
         }
      }

      ClientChatLogger.II1Il11l111II11IIl("Маршрут: " + this.flightRoute.size() + " точек");
   }
// https://t.me/pasterplace

   private void advanceFlightRoute() {
      if (this.flightRouteIndex >= this.flightRoute.size()) {
         ClientChatLogger.II1Il11l111II11IIl("Область пройдена, структура не найдена");
         this.searchEnabled.setValue(false);
      } else if (!CLIENT.player.getAbilities().flying) {
         this.activateFlight();
      } else {
          double[] routePoint = this.flightRoute.get(this.flightRouteIndex);
          double deltaX = routePoint[0] - CLIENT.player.getX();
          double deltaZ = routePoint[1] - CLIENT.player.getZ();
         double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         double flightSpeed = this.flightSpeed.getValue().floatValue();
         if (horizontalDistance < flightSpeed + 1.0) {
            this.flightRouteIndex++;
         } else {
            double altitudeDelta = this.flightAltitude.getValue().floatValue() - CLIENT.player.getY();
            double verticalVelocity = MathHelper.clamp(altitudeDelta * 0.3, -1.0, 1.0);
            double travelSpeed = Math.abs(altitudeDelta) > 8.0 ? flightSpeed * 0.3 : flightSpeed;
            CLIENT.player.setYaw((float)(MathHelper.atan2(deltaZ, deltaX) * 180.0F / (float)Math.PI) - 90.0F);
            CLIENT.player.setVelocity(deltaX / horizontalDistance * travelSpeed, verticalVelocity, deltaZ / horizontalDistance * travelSpeed);
         }
      }
   }
// https://t.me/pasterplace

   private void activateFlight() {
      PlayerAbilities playerAbilities = CLIENT.player.getAbilities();
      if (playerAbilities.allowFlying) {
         if (CLIENT.player.isOnGround()) {
            CLIENT.player.jump();
         } else {
            playerAbilities.flying = true;
            CLIENT.player.sendAbilitiesUpdate();
         }
      } else {
         if (this.flightCommandCooldown.hasElapsed(3000L) && CLIENT.getNetworkHandler() != null) {
            CLIENT.getNetworkHandler().sendChatCommand("fly");
            this.flightCommandCooldown.reset();
         }
      }
   }
// https://t.me/pasterplace

   private void loadNearbyChunks() {
      int viewDistance = CLIENT.options.getClampedViewDistance();
      ChunkPos chunkPos = CLIENT.player.getChunkPos();
      int chunksLoadedThisUpdate = 0;
// https://t.me/pasterplace

      for (int chunkOffsetX = -viewDistance; chunkOffsetX <= viewDistance && chunksLoadedThisUpdate < 6; chunkOffsetX++) {
         for (int chunkOffsetZ = -viewDistance; chunkOffsetZ <= viewDistance && chunksLoadedThisUpdate < 6; chunkOffsetZ++) {
            long chunkId = ChunkPos.toLong(chunkPos.x + chunkOffsetX, chunkPos.z + chunkOffsetZ);
            if (!this.loadedChunkIds.contains(chunkId)) {
               Chunk loadedChunk = CLIENT.world.getChunk(chunkPos.x + chunkOffsetX, chunkPos.z + chunkOffsetZ, ChunkStatus.FULL, false);
               if (loadedChunk instanceof WorldChunk) {
                  WorldChunk worldChunk = (WorldChunk)loadedChunk;
                  this.loadedChunkIds.add(chunkId);
                  this.collectRarestBlockPositions(worldChunk);
                  chunksLoadedThisUpdate++;
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private void collectRarestBlockPositions(WorldChunk worldChunk) {
      ChunkSection[] chunkSections = worldChunk.getSectionArray();
      int chunkStartX = worldChunk.getPos().getStartX();
      int chunkStartZ = worldChunk.getPos().getStartZ();

      for (int sectionIndex = 0; sectionIndex < chunkSections.length; sectionIndex++) {
         ChunkSection chunkSection = chunkSections[sectionIndex];
         if (!chunkSection.isEmpty() && chunkSection.getBlockStateContainer().hasAny(blockState -> blockState.getBlock() == this.rarestTemplateBlock)) {
            int sectionStartY = ChunkSectionPos.getBlockCoord(worldChunk.sectionIndexToCoord(sectionIndex));

            for (int localY = 0; localY < 16; localY++) {
               for (int localZ = 0; localZ < 16; localZ++) {
                  for (int localX = 0; localX < 16; localX++) {
                     if (chunkSection.getBlockState(localX, localY, localZ).getBlock() == this.rarestTemplateBlock) {
                        this.rarestBlockAnchorPositions.add(BlockPos.asLong(chunkStartX + localX, sectionStartY + localY, chunkStartZ + localZ));
                     }
                  }
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   private void scanStructureCandidates() {
      List<int[]> anchorTemplatePositions = this.templatePositionsByBlock.get(this.rarestTemplateBlock);
      if (anchorTemplatePositions != null) {
         int remainingBlockChecks = 4000;
         double requiredMatchingBlocks = this.minimumMatchPercent.getValue().floatValue() / 100.0 * this.structureTemplate.size();
         Mutable candidateBlockPos = new Mutable();

         while (this.anchorPositionIndex < this.rarestBlockAnchorPositions.size() && remainingBlockChecks > 0 && !this.structureFound) {
            long anchorBlockPosition = this.rarestBlockAnchorPositions.get(this.anchorPositionIndex++);
            int anchorWorldX = BlockPos.unpackLongX(anchorBlockPosition);
            int anchorWorldY = BlockPos.unpackLongY(anchorBlockPosition);
            int anchorWorldZ = BlockPos.unpackLongZ(anchorBlockPosition);

             for (int[] candidateAnchorOffsets : anchorTemplatePositions) {
               for (int quarterTurns = 0; quarterTurns < 4 && !this.structureFound; quarterTurns++) {
                int candidateOriginX = anchorWorldX - rotateTemplateOffsetX(candidateAnchorOffsets[0], candidateAnchorOffsets[2], quarterTurns);
                int candidateOriginY = anchorWorldY - candidateAnchorOffsets[1];
                int candidateOriginZ = anchorWorldZ - rotateTemplateOffsetZ(candidateAnchorOffsets[0], candidateAnchorOffsets[2], quarterTurns);
                  int matchedBlockCount = 0;
                  int mismatchedBlockCount = 0;
                  int maxAllowedMismatches = this.structureTemplate.size() - (int)Math.ceil(requiredMatchingBlocks);

                for (int[] templatePosition : this.structureTemplate) {
                     candidateBlockPos.set(
                      candidateOriginX + rotateTemplateOffsetX(templatePosition[0], templatePosition[2], quarterTurns),
                      candidateOriginY + templatePosition[1],
                      candidateOriginZ + rotateTemplateOffsetZ(templatePosition[0], templatePosition[2], quarterTurns)
                     );
                   if (Registries.BLOCK.getRawId(CLIENT.world.getBlockState(candidateBlockPos).getBlock()) == templatePosition[3]) {
                        matchedBlockCount++;
                     } else if (++mismatchedBlockCount > maxAllowedMismatches) {
                        break;
                     }
                  }

                  remainingBlockChecks -= matchedBlockCount + mismatchedBlockCount;
                  if (matchedBlockCount >= requiredMatchingBlocks) {
                     this.reportFoundStructure(candidateOriginX, candidateOriginY, candidateOriginZ, matchedBlockCount);
                  }
               }
            }
         }

         if (this.anchorPositionIndex >= this.rarestBlockAnchorPositions.size()) {
            this.rarestBlockAnchorPositions.clear();
            this.anchorPositionIndex = 0;
         }
      }
   }
// https://t.me/pasterplace

   private static int rotateTemplateOffsetX(int localX, int localZ, int quarterTurns) {
      return switch (quarterTurns) {
         case 1 -> -localZ;
         case 2 -> -localX;
         case 3 -> localZ;
         default -> localX;
      };
   }
// https://t.me/pasterplace

   private static int rotateTemplateOffsetZ(int localX, int localZ, int quarterTurns) {
      return switch (quarterTurns) {
         case 1 -> localX;
         case 2 -> -localZ;
         case 3 -> -localX;
         default -> localZ;
      };
   }
// https://t.me/pasterplace

   private void reportFoundStructure(int originX, int originY, int originZ, int matchedBlockCount) {
      this.structureFound = true;
      this.searchEnabled.setValue(false);
      CLIENT.player.setVelocity(Vec3d.ZERO);
      int matchPercent = Math.round(matchedBlockCount * 100.0F / this.structureTemplate.size());
      ClientChatLogger.II1Il11l111II11IIl("Структура найдена: " + originX + " " + originY + " " + originZ + " (" + matchPercent + "%)");
   }
}

