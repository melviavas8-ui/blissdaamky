package dev.daamky;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.Settings;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.pathing.goals.GoalXZ;
import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventMotion;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.chunk.ChunkSection;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class AutoQuest extends ModuleStructure {
    private static final Pattern RTP_DELAY_MESSAGE_PATTERN = Pattern.compile("подождите\\s+(\\d+)\\s+сек");
    private static final Pattern QUEST_PROGRESS_PATTERN = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)");
    private static final Predicate<ItemStack> STONE_MATERIAL_PREDICATE = itemStack -> itemStack.isIn(ItemTags.STONE_CRAFTING_MATERIALS);
    private static final Predicate<ItemStack> LOG_ITEM_PREDICATE = itemStack -> itemStack.isIn(ItemTags.LOGS);
    private static final Predicate<ItemStack> SUPPORT_BLOCK_ITEM_PREDICATE = itemStack -> itemStack.isOf(Items.COBBLESTONE)
    || itemStack.isOf(Items.COBBLED_DEEPSLATE)
    || itemStack.isOf(Items.DIRT);
    private static final Item[] DISCARDABLE_ITEMS = new Item[]{
        Items.COBBLESTONE,
        Items.COBBLED_DEEPSLATE,
        Items.DEEPSLATE,
        Items.STONE,
        Items.DIRT,
        Items.GRAVEL,
        Items.SAND,
        Items.RED_SAND,
        Items.ANDESITE,
        Items.DIORITE,
        Items.GRANITE,
        Items.TUFF,
        Items.CALCITE,
        Items.FLINT,
        Items.NETHERRACK,
        Items.SANDSTONE,
        Items.RED_SANDSTONE,
        Items.CLAY_BALL,
        Items.ROTTEN_FLESH,
        Items.BONE,
        Items.STRING,
        Items.SPIDER_EYE,
        Items.GUNPOWDER,
        Items.ARROW,
        Items.MOSSY_COBBLESTONE,
        Items.SMOOTH_BASALT,
        Items.DRIPSTONE_BLOCK,
        Items.POINTED_DRIPSTONE,
        Items.MOSS_BLOCK,
        Items.GLOW_LICHEN
    };
    private static final Predicate<ItemStack> PLANK_ITEM_PREDICATE = itemStack -> itemStack.isIn(ItemTags.PLANKS);
    private static final Pattern GRIEF_NUMBER_PATTERN = Pattern.compile("(?:гриф|анарх)\\D*(\\d+)");
    private static final String BOT_PASSWORD = "gagaga";
    private final FloatSetting questActionDelaySetting = new FloatSetting("Задержка", 150.0F, 50.0F, 500.0F, 10.0F, "мс");
    private final FloatSetting randomTeleportWaitSetting = new FloatSetting("Ожидание RTP", 6000.0F, 1000.0F, 20000.0F, 500.0F, "мс");
    private final FloatSetting miningTimeoutSetting = new FloatSetting("Таймаут добычи", 300.0F, 30.0F, 900.0F, 10.0F, "сек");
    private final FloatSetting griefNumberSetting = new FloatSetting("Номер грифа", 0.0F, 0.0F, 100.0F, 1.0F);
    private final FloatSetting retreatHealthThresholdSetting = new FloatSetting("HP для отхода", 10.0F, 4.0F, 18.0F, 1.0F, "HP");
    private final Stopwatch actionCooldownTimer = new Stopwatch();
    private final Stopwatch stageWatchdogTimer = new Stopwatch();
    private final Stopwatch blockMiningProgressTimer = new Stopwatch();
    private final List<AutoQuest.BlockRequirement> blockRequirements = new ArrayList<>();
    private final Set<String> completedQuestEntries = new HashSet<>();
    private final Set<Integer> ignoredDroppedItemIds = new HashSet<>();
    private final Stopwatch droppedItemPathTimer = new Stopwatch();
    private final Stopwatch marketItemWaitTimer = new Stopwatch();
    private ItemEntity itemEntity;
    private AutoQuest.QuestStage currentStage = AutoQuest.QuestStage.RETURN_TO_SPAWN;
    private AutoQuest.QuestStage stageAfterObjective = AutoQuest.QuestStage.RETURN_TO_SPAWN;
    private Item questItem;
    private Predicate<ItemStack> questItemPredicate;
    private int requiredQuestItemCount;
    private AutoQuest.BlockRequirement currentBlockRequirement;
    private Vec3d randomTeleportStartPosition;
    private int questMenuAttempts;
    private boolean rtpPearlUsed;
    private Object lastObservedWorld;
    private volatile long rtpWaitUntilMillis;
    private BlockPos pendingBlockPlacement;
    private Direction pendingBlockPlacementFace;
    private BlockPos cachedFurnacePosition;
    private int stageRetryCount;
    private boolean requiresCrafting;
    private boolean breakObjective;
    private boolean requiresMobKill;
    private boolean requiresItemDelivery;
    private EntityType<?> targetMobType;
    private LivingEntity currentTarget;
    private int completedMobKills;
    private boolean hasAttackedCurrentTarget;
    private boolean attackQueued;
    private final Stopwatch combatPathTimer = new Stopwatch();
    private final Stopwatch baritoneRepathTimer = new Stopwatch();
    private final Stopwatch mobSpawnerSearchTimer = new Stopwatch();
    private final Set<BlockPos> visitedSpawnerPositions = new HashSet<>();
    private BlockPos currentSpawnerPosition;
    private int mobSearchAttemptCount;
    private static AutoQuest instance;
    private int minedBlockCount;
    private final Stopwatch discardIntervalTimer = new Stopwatch();
    private final Stopwatch stuckDetectionTimer = new Stopwatch();
    private Vec3d lastObservedPosition;
    private boolean requiresFenceCrafting;
    private Predicate<ItemStack> marketItemPredicate;
    private int marketTargetItemCount;
    private AutoQuest.QuestStage stageAfterMaterialsCollected = AutoQuest.QuestStage.RETURN_TO_SPAWN;
    private String marketSearchTerm;
    private String questName;
    private int observedMarketStackCount;
    private int marketQuantityRetryCount;
    private boolean marketSearchInProgress;
    private int marketNextPageAttempts;
    private Item fencePlankItem;
    private boolean requiresSpecialToolCrafting;
    private Item specialToolItem;
    private int specialToolRequiredCount;
    private Predicate<ItemStack> specialToolMaterialPredicate;
    private int[] specialToolRecipeSlots;
    private AutoQuest.QuestStage stageAfterToolCrafting = AutoQuest.QuestStage.RETURN_TO_SPAWN;
    private boolean requiresPvpBotQuest;
    private Item pvpRequiredToolItem;
    private boolean botCallPending;
    private volatile long pendingTeleportAcceptAtMillis;
    private volatile String trackedBotUsername;
    private final Stopwatch botCallRetryTimer = new Stopwatch();
    private final Stopwatch botConnectionTimer = new Stopwatch();
    private final Stopwatch botHubReturnTimer = new Stopwatch();
    private boolean botReturningToHub;
    private BotSession.BotConnectionStatus previousBotConnectionStatus;
    private int marketItemCountBeforePurchase = -1;
    private final Stopwatch craftingTransferTimer = new Stopwatch();
    private Predicate<ItemStack> queuedRequirementPredicate;
    private final ArrayDeque<AutoQuest.ItemRequirement> queuedItemRequirements = new ArrayDeque<>();
    private boolean requiresPotionBrewing;
    private boolean requiresEating;
    private boolean hungerRunStarted;
    private boolean shouldRunToExhaustHunger;
    private final Stopwatch hungerRunTimer = new Stopwatch();
    private BlockPos hungerPillarStartPosition;
    private boolean shouldPlaceLandingBlock;
    private int remainingHungerRunTicks;
    private int brewedQuestPotionCount;
    private RegistryEntry<Potion> questPotion;
    private Item potionBrewingIngredient;
    private boolean waterBottleFillPending;
    private BlockPos nearbyWaterSource;
    private int rtpRetryCount;
    private int postTeleportJumpTicks;
    private final Stopwatch rtpResultRetryTimer = new Stopwatch();
    private BlockPos brewingStandPosition;
    private final Stopwatch waterPathTimeoutTimer = new Stopwatch();
    private Rotation queuedWaterAimRotation;
    private final Stopwatch waterAimRotationTimer = new Stopwatch();
    private int queuedRequirementCount;
    private String queuedRequirementDescription;
    private BotSession questBot;
    private boolean retreatingForHealth;
    private final Stopwatch healingTimer = new Stopwatch();
    private final Stopwatch foodRecoveryTimer = new Stopwatch();
    private static final String[][] ITEM_NAME_ALIASES = new String[][]{
        {"зельеварка", "варочная стойка", "brewing stand"},
        {"бутылочка", "пузырёк", "стеклянный пузырёк", "пузырек", "glass bottle"},
        {"незерский нарост", "адский нарост", "nether wart"},
        {"сверкающий ломтик арбуза", "сверкающий арбуз", "блестящий ломтик арбуза", "glistering melon"},
        {"огненный порошок", "порошок ифрита", "blaze powder"},
        {"ведро воды"},
        {"сахар", "sugar"},
        {"сгусток магмы", "magma cream"},
        {"золотая морковь", "golden carrot"},
        {"кроличья лапка", "rabbit foot"},
        {"слеза гаста", "ghast tear"},
        {"иглобрюх", "pufferfish"},
        {"мембрана фантома", "phantom membrane"},
        {"паучий глаз", "spider eye"}
    };
    private int marketPurchaseAttempt;
    private int marketUnreceivedPurchaseRetries;
    private int marketScreenContentHash = -1;
    private boolean marketPurchaseDialogOpen;
    private long lastMarketInteractionAtMillis;
    private long marketScreenLastChangeAtMillis;
    private String currentMarketAlias;
    private int marketAliasIndex;
    private static final Predicate<ItemStack> WATER_POTION_PREDICATE = isPotion(net.minecraft.potion.Potions.WATER);

    public AutoQuest() {
        super("AutoQuest", "Выполняет квесты NPC на спавне: дерево, доски, железо", ModuleCategory.WORLD);
        this.addSettings(this.questActionDelaySetting, this.randomTeleportWaitSetting, this.miningTimeoutSetting, this.griefNumberSetting, this.retreatHealthThresholdSetting);

    }

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onEnable() {
        if (this.hasActiveClientWorld()) {
            if (InventoryUtils.findItemSlot(stack -> stack.isIn(ItemTags.AXES)) != -1) {
                if (InventoryUtils.findItemSlot(stack -> stack.isIn(ItemTags.PICKAXES) && stack.isSuitableFor(Blocks.IRON_ORE.getDefaultState()))
                != -1) {
                    this.questItem = null;
                    this.questMenuAttempts = 0;
                    this.blockRequirements.clear();
                    this.completedQuestEntries.clear();
                    this.currentBlockRequirement = null;
                    this.cachedFurnacePosition = null;
                    this.configureBaritone();
                    this.cancelBaritonePath();
                    this.lastObservedWorld = CLIENT.world;
                    this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
                } else {
                    ClientChatLogger.l1I1IlllIlI("Нужна каменная кирка или лучше.");
                    this.setEnabled(false);
                }
            } else {
                ClientChatLogger.l1I1IlllIlI("Нужен топор в инвентаре.");
                this.setEnabled(false);
            }
        } else {
            this.setEnabled(false);
        }
    }

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onDisable() {
        if (this.postTeleportJumpTicks > 0) {
            this.postTeleportJumpTicks = 0;
            CLIENT.options.jumpKey.setPressed(false);
        }

        if (this.currentStage == AutoQuest.QuestStage.EAT_QUEST_FOOD) {
            CLIENT.options.jumpKey.setPressed(false);
            this.hungerPillarStartPosition = null;
            this.shouldPlaceLandingBlock = false;
            CLIENT.options.useKey.setPressed(false);
            CLIENT.options.sprintKey.setPressed(false);
            CLIENT.options.forwardKey.setPressed(false);
        }

        this.cancelBaritonePath();
        BaritoneAPI.getSettings().allowBreak.reset();
        BaritoneAPI.getSettings().allowBreakAnyway.reset();
        BaritoneAPI.getSettings().blocksToAvoid.reset();
        BaritoneAPI.getSettings().blocksToDisallowBreaking.reset();
        BaritoneAPI.getSettings().minYLevelWhileMining.reset();
        BaritoneAPI.getSettings().exploreForBlocks.reset();
        BaritoneAPI.getSettings().acceptableThrowawayItems.reset();
        RotationPriorityManager.getInstance().clearRotationRequest(this);
        this.pendingBlockPlacement = null;
        SavedBots.removeSession(this.questBot);
        this.questBot = null;
    }

    @EventTarget
    public void onUpdate(EventUpdate eventUpdate) {
        if (this.hasActiveClientWorld() && CLIENT.getNetworkHandler() != null) {
            if (!CLIENT.player.isAlive()) {
                this.cancelBaritonePath();
                this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
            } else {
                if (CLIENT.world != this.lastObservedWorld) {
                    this.lastObservedWorld = CLIENT.world;
                    this.cancelBaritonePath();
                    if (this.currentStage == AutoQuest.QuestStage.MINE_REQUIRED_BLOCKS || this.currentStage == AutoQuest.QuestStage.HUNT && !this.retreatingForHealth) {
                        this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                        return;
                    }
                }

                if (this.stageWatchdogTimer.hasElapsed(this.getStageTimeoutMillis())) {
                    ClientChatLogger.l1I1IlllIlI("Этап " + this.currentStage.name() + " завис, повторяю.");
                    this.cancelBaritonePath();
                    RotationPriorityManager.getInstance().clearRotationRequest(this);
                    this.pendingBlockPlacement = null;
                    this.closeHandledScreen();
                    this.setStage(
                    this.currentStage != AutoQuest.QuestStage.MINE_REQUIRED_BLOCKS
                    && this.currentStage != AutoQuest.QuestStage.HUNT
                    && this.currentStage != AutoQuest.QuestStage.WAIT_FOR_RTP_RESULT
                    ? AutoQuest.QuestStage.RETURN_TO_SPAWN
                    : AutoQuest.QuestStage.WAIT_FOR_RTP
                    );
                } else {
                    if (this.currentStage == AutoQuest.QuestStage.CRAFT_QUEST_ITEM || this.currentStage == AutoQuest.QuestStage.CRAFT_FURNACE) {
                        RotationPriorityManager.getInstance()
                        .requestRotation(RotationPriorityManager.getInstance().getCurrentRotation(), FocusMode.FOCUSED, 60.0F, 1, false, this);
                    }

                    if (this.currentStage != AutoQuest.QuestStage.MINE_REQUIRED_BLOCKS && this.currentStage != AutoQuest.QuestStage.HUNT) {
                        this.lastObservedPosition = null;
                    } else {
                        boolean hasStableHuntDestination = this.currentStage == AutoQuest.QuestStage.HUNT && (this.retreatingForHealth || this.currentSpawnerPosition != null && this.canSeeSpawner());
                        if (!hasStableHuntDestination && this.lastObservedPosition != null && !(CLIENT.player.getEntityPos().squaredDistanceTo(this.lastObservedPosition) > 4.0)) {
                            if (this.stuckDetectionTimer.hasElapsed(20000L)) {
                                this.lastObservedPosition = null;
                                this.cancelBaritonePath();
                                this.currentBlockRequirement = null;
                                ClientChatLogger.l1I1IlllIlI("Застрял, ухожу телепортом.");
                                if (this.currentSpawnerPosition != null) {
                                    this.visitedSpawnerPositions.add(this.currentSpawnerPosition);
                                    this.currentSpawnerPosition = null;
                                }

                                this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                                return;
                            }
                        } else {
                            this.lastObservedPosition = CLIENT.player.getEntityPos();
                            this.stuckDetectionTimer.reset();
                        }
                    }

                    if (this.postTeleportJumpTicks > 0) {
                        CLIENT.options.jumpKey.setPressed(--this.postTeleportJumpTicks > 0);
                    }

                    switch (this.currentStage) {
                    case RETURN_TO_SPAWN:
                        this.returnToSpawn();
                        break;
                    case WAIT_FOR_SPAWN:
                        this.waitForSpawn();
                        break;
                    case OPEN_QUEST_MENU:
                        this.openQuestMenu();
                        break;
                    case SELECT_QUEST:
                        this.selectQuest();
                        break;
                    case CLAIM_QUEST_REWARD:
                        this.claimQuestReward();
                        break;
                    case WAIT_FOR_RTP:
                        this.requestRandomTeleport();
                        break;
                    case WAIT_FOR_RTP_RESULT:
                        this.waitForRandomTeleport();
                        break;
                    case MINE_REQUIRED_BLOCKS:
                        this.mineRequiredBlocks();
                        break;
                    case HUNT:
                        this.runHuntStep();
                        break;
                    case MOVE_REQUIRED_ITEMS:
                        this.moveRequiredItems();
                        break;
                    case PREPARE_CRAFTING_INVENTORY:
                        this.prepareCraftingInventory();
                        break;
                    case COLLECT_REQUIRED_ITEMS:
                        this.collectRequiredItems();
                        break;
                    case CRAFT_QUEST_ITEM:
                        this.craftQuestItem();
                        break;
                    case CRAFT_FURNACE:
                        this.openFurnace();
                        break;
                    case SMELT_QUEST_ITEMS:
                        this.smeltQuestItems();
                        break;
                    case COMPLETE_PVP_KILL:
                        this.completePvpQuest();
                        break;
                    case WAIT_FOR_BOT_KILL:
                        this.waitForPvpBotKill();
                        break;
                    case BREW_QUEST_POTIONS:
                        this.brewQuestPotions();
                        break;
                    case EAT_QUEST_FOOD:
                        this.processEatQuest();
                    }
                }
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private long getStageTimeoutMillis() {
        if (this.currentStage == AutoQuest.QuestStage.MINE_REQUIRED_BLOCKS || this.currentStage == AutoQuest.QuestStage.HUNT) {
            return this.miningTimeoutSetting.getValue().longValue() & 1000L;
        } else if (this.currentStage == AutoQuest.QuestStage.SMELT_QUEST_ITEMS) {
            return 60000L + (this.requiredQuestItemCount & 11000L);
        } else if (this.currentStage == AutoQuest.QuestStage.COMPLETE_PVP_KILL || this.currentStage == AutoQuest.QuestStage.WAIT_FOR_BOT_KILL) {
            return 900000L;
        } else {
            return this.currentStage != AutoQuest.QuestStage.BREW_QUEST_POTIONS ? this.currentStage.l1IIIIl1lII1IlIIIll1 : 900000L;
        }
    }

    @EventTarget
    public void onPacket(EventPacket eventPacket) {
        if (eventPacket.isReceive()) {
            Packet packet = eventPacket.packet();
            if (packet instanceof GameMessageS2CPacket) {
                GameMessageS2CPacket packet2 = (GameMessageS2CPacket)packet;
                if (!packet2.overlay()) {
                    String chatMessage = packet2.content().getString().toLowerCase(Locale.ROOT);
                    String botUsername = this.trackedBotUsername;
                    if (this.currentStage == AutoQuest.QuestStage.COMPLETE_PVP_KILL && this.botCallPending && botUsername != null && chatMessage.contains(botUsername)) {
                        this.pendingTeleportAcceptAtMillis = System.currentTimeMillis() + 300L;
                    }

                    if (chatMessage.contains("уже был куплен")) {
                        CLIENT.execute(() -> {
                            if (this.isEnabled() && this.currentStage == AutoQuest.QuestStage.COLLECT_REQUIRED_ITEMS) {
                                ClientChatLogger.II1Il11l111II11IIl("Лот уже купили, ищу заново.");
                                this.closeHandledScreen();
                                this.marketPurchaseDialogOpen = false;
                                this.marketSearchInProgress = false;
                                this.stageRetryCount = 0;
                                this.marketItemCountBeforePurchase = -1;
                                this.marketPurchaseAttempt = 0;
                                this.marketNextPageAttempts = 0;
                                this.marketQuantityRetryCount = 0;
                                this.observedMarketStackCount = 0;
                                this.actionCooldownTimer.reset();
                            }
                        });
                        return;
                    }

                    if (chatMessage.contains("hey! sorry, but you can't")) {
                        CLIENT.execute(() -> {
                            if (this.isEnabled()) {
                                if (this.currentStage == AutoQuest.QuestStage.MINE_REQUIRED_BLOCKS || this.currentStage == AutoQuest.QuestStage.HUNT) {
                                    this.cancelBaritonePath();
                                    ClientChatLogger.II1Il11l111II11IIl("Чужой регион, новый RTP.");
                                    this.currentBlockRequirement = null;
                                    if (this.currentSpawnerPosition != null) {
                                        this.visitedSpawnerPositions.add(this.currentSpawnerPosition);
                                        this.currentSpawnerPosition = null;
                                    }

                                    this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                                } else if (this.currentStage == AutoQuest.QuestStage.CRAFT_QUEST_ITEM || this.currentStage == AutoQuest.QuestStage.CRAFT_FURNACE) {
                                    this.pendingBlockPlacement = null;
                                    ClientChatLogger.II1Il11l111II11IIl("Здесь нельзя ставить, новый RTP.");
                                    this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                                }
                            }
                        });
                        return;
                    }

                    Matcher matcher = RTP_DELAY_MESSAGE_PATTERN.matcher(chatMessage);
                    if (!matcher.find()) {
                        return;
                    }

                    this.rtpWaitUntilMillis = System.currentTimeMillis() + Long.parseLong(matcher.group(1)) * 1000L + 1000L;
                    CLIENT.execute(() -> {
                        if (this.isEnabled() && this.currentStage == AutoQuest.QuestStage.WAIT_FOR_RTP_RESULT) {
                            this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                        }
                    });
                    return;
                }
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion eventMotion) {
        if (eventMotion.isPost() && this.hasActiveClientWorld()) {
            if (this.shouldPlaceLandingBlock && this.currentStage == AutoQuest.QuestStage.EAT_QUEST_FOOD && this.hungerPillarStartPosition != null) {
                this.placeBlockDuringFall();
            } else if (!this.attackQueued
            || this.currentTarget == null
            || this.currentStage != AutoQuest.QuestStage.HUNT && this.currentStage != AutoQuest.QuestStage.COMPLETE_PVP_KILL) {
                if (!this.waterBottleFillPending
                || this.currentStage != AutoQuest.QuestStage.BREW_QUEST_POTIONS
                || !RotationPriorityManager.getInstance().isFocusedBy(this)
                || !(RotationPriorityManager.getInstance().getLastMotionRotation().angularDistanceTo(this.queuedWaterAimRotation) < 2.0F) && !this.waterAimRotationTimer.hasElapsed(1500L)) {
                    if (this.pendingBlockPlacement != null
                    && (
                    this.currentStage == AutoQuest.QuestStage.CRAFT_QUEST_ITEM
                    || this.currentStage == AutoQuest.QuestStage.CRAFT_FURNACE
                    || this.currentStage == AutoQuest.QuestStage.BREW_QUEST_POTIONS
                    )) {
                        Vec3d blockHitPosition = Vec3d.ofCenter(this.pendingBlockPlacement)
                        .add(
                        this.pendingBlockPlacementFace.getOffsetX() * 0.5, this.pendingBlockPlacementFace.getOffsetY() * 0.5, this.pendingBlockPlacementFace.getOffsetZ() * 0.5
                        );
                        CLIENT.interactionManager
                        .interactBlock(CLIENT.player, Hand.MAIN_HAND, new BlockHitResult(blockHitPosition, this.pendingBlockPlacementFace, this.pendingBlockPlacement, false));
                        CLIENT.player.swingHand(Hand.MAIN_HAND);
                        this.pendingBlockPlacement = null;
                        this.actionCooldownTimer.reset();
                    }
                } else {
                    this.waterBottleFillPending = false;
                    float previousYaw = CLIENT.player.getYaw();
                    float previousPitch = CLIENT.player.getPitch();
                    CLIENT.player.setYaw(this.queuedWaterAimRotation.getYaw());
                    CLIENT.player.setPitch(this.queuedWaterAimRotation.getPitch());
                    CLIENT.interactionManager.interactItem(CLIENT.player, Hand.MAIN_HAND);
                    CLIENT.player.setYaw(previousYaw);
                    CLIENT.player.setPitch(previousPitch);
                    CLIENT.player.swingHand(Hand.MAIN_HAND);
                    this.actionCooldownTimer.reset();
                }
            } else {
                this.attackQueued = false;
                CLIENT.interactionManager.attackEntity(CLIENT.player, this.currentTarget);
                CLIENT.player.swingHand(Hand.MAIN_HAND);
                this.hasAttackedCurrentTarget = true;
            }
        }
    }

    private void setStage(AutoQuest.QuestStage nextStage) {
        if (this.currentStage == AutoQuest.QuestStage.EAT_QUEST_FOOD && nextStage != AutoQuest.QuestStage.EAT_QUEST_FOOD) {
            CLIENT.options.useKey.setPressed(false);
            CLIENT.options.sprintKey.setPressed(false);
            CLIENT.options.forwardKey.setPressed(false);
            if (this.hungerPillarStartPosition != null) {
                this.releaseQuestControls();
            }

            this.remainingHungerRunTicks = 0;
            this.cancelBaritonePath();
        }

        if (nextStage != AutoQuest.QuestStage.HUNT) {
            this.retreatingForHealth = false;
        }

        if (nextStage == AutoQuest.QuestStage.COLLECT_REQUIRED_ITEMS && this.currentStage != AutoQuest.QuestStage.COLLECT_REQUIRED_ITEMS) {
            this.marketPurchaseDialogOpen = false;
            this.marketItemCountBeforePurchase = -1;
            this.marketUnreceivedPurchaseRetries = 0;
            this.marketSearchInProgress = false;
            this.marketNextPageAttempts = 0;
        }

        this.currentStage = nextStage;
        this.actionCooldownTimer.reset();
        this.stageWatchdogTimer.reset();
        this.blockMiningProgressTimer.reset();
        this.stageRetryCount = 0;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void returnToSpawn() {
        this.closeHandledScreen();
        this.cancelBaritonePath();
        CLIENT.getNetworkHandler().sendChatCommand("spawn");
        this.setStage(AutoQuest.QuestStage.WAIT_FOR_SPAWN);
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void waitForSpawn() {
        if (this.actionCooldownTimer.hasElapsed(3000L)) {
            this.questMenuAttempts = 0;
            this.setStage(AutoQuest.QuestStage.OPEN_QUEST_MENU);
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void openQuestMenu() {
        if (!this.isHandledContainerOpen()) {
            CLIENT.getNetworkHandler().sendChatCommand("quest1");
            this.setStage(AutoQuest.QuestStage.SELECT_QUEST);
        } else {
            this.setStage(AutoQuest.QuestStage.SELECT_QUEST);
        }
    }

    private void selectQuest() {
        Screen screen = CLIENT.currentScreen;
        if (screen instanceof HandledScreen) {
            HandledScreen<?> screen2 = (HandledScreen<?>)screen;
            if (!(screen2.getScreenHandler() instanceof PlayerScreenHandler)) {
                RotationPriorityManager.getInstance().clearRotationRequest(this);
                if (!this.actionCooldownTimer.hasElapsed(500L)) {
                    return;
                }

                ScreenHandler questMenuHandler = screen2.getScreenHandler();
                int questSlotCount = questMenuHandler.slots.size() - 36;
                int selectedQuestSlot = -1;

                for (int questSlotIndex = 0; questSlotIndex < questSlotCount; questSlotIndex++) {
                    if (questSlotIndex % 9 != 8) {
                        ItemStack questStack = ((Slot)questMenuHandler.slots.get(questSlotIndex)).getStack();
                        String questLore = this.getItemLore(questStack);
                        if (!questStack.isEmpty() && !questStack.isOf(Items.BARRIER) && questLore.contains("не выполнено") && !questLore.contains("уже прошли")) {
                            String questEntryKey = questSlotIndex + "|" + questStack.getName().getString();
                            Matcher matcher = QUEST_PROGRESS_PATTERN.matcher(questLore);
                            if (matcher.find() && Integer.parseInt(matcher.group(1)) >= Integer.parseInt(matcher.group(2))) {
                                if (this.completedQuestEntries.add(questEntryKey)) {
                                    ClientChatLogger.II1Il11l111II11IIl("Квест " + questStack.getName().getString() + " выполнен, забираю.");
                                    InventoryUtils.clickSlot(questSlotIndex, 0, SlotActionType.PICKUP);
                                    this.setStage(AutoQuest.QuestStage.CLAIM_QUEST_REWARD);
                                    return;
                                }
                            } else if (!this.completedQuestEntries.contains(questEntryKey)) {
                                selectedQuestSlot = questSlotIndex;
                                break;
                            }
                        }
                    }
                }

                if (selectedQuestSlot == -1) {
                    ClientChatLogger.II1Il11l111II11IIl("Доступных квестов нет.");
                    this.closeHandledScreen();
                    this.setEnabled(false);
                    return;
                }

                ItemStack selectedQuestStack = ((Slot)questMenuHandler.slots.get(selectedQuestSlot)).getStack();
                Matcher matcher2 = QUEST_PROGRESS_PATTERN.matcher(this.getItemLore(selectedQuestStack));
                if (!matcher2.find()) {
                    ClientChatLogger.l1I1IlllIlI("Не удалось прочитать прогресс квеста.");
                    this.closeHandledScreen();
                    this.setEnabled(false);
                    return;
                }

                this.requiredQuestItemCount = Integer.parseInt(matcher2.group(2)) - Integer.parseInt(matcher2.group(1));
                this.questItem = selectedQuestStack.getItem();
                Item item = this.questItem;
                this.questItemPredicate = stack -> stack.isOf(item);
                this.requiresCrafting = this.getItemLore(selectedQuestStack).contains("скрафтить");
                this.breakObjective = this.getItemLore(selectedQuestStack).contains("сломать");
                this.requiresMobKill = this.getItemLore(selectedQuestStack).contains("убить");
                this.requiresItemDelivery = this.getItemLore(selectedQuestStack).contains("принести");
                this.requiresPotionBrewing = this.getItemLore(selectedQuestStack).contains("сварить");
                this.requiresEating = this.getItemLore(selectedQuestStack).contains("съесть");
                String questDisplayName = selectedQuestStack.getName().getString();
                this.questName = questDisplayName.toLowerCase(Locale.ROOT);
                if (!this.configureQuestRequirements()) {
                    ClientChatLogger.l1I1IlllIlI("Квест на " + questDisplayName + " не поддерживается.");
                    this.closeHandledScreen();
                    this.setEnabled(false);
                    return;
                }

                int currentQuestItemCount = this.countMatchingItems(this.questItemPredicate);
                if (this.breakObjective
                || this.requiresMobKill
                || this.requiresCrafting && this.requiresSpecialToolCrafting
                || this.requiresEating
                || currentQuestItemCount <= 0
                || currentQuestItemCount < this.requiredQuestItemCount && this.getNextQuestStage() != AutoQuest.QuestStage.RETURN_TO_SPAWN) {
                    this.closeHandledScreen();
                    ClientChatLogger.II1Il11l111II11IIl(String.format(Locale.ROOT, "Квест: %s x%d.", questDisplayName, this.requiredQuestItemCount));
                    if (this.marketItemPredicate != null && this.countMatchingItems(this.marketItemPredicate) < this.marketTargetItemCount) {
                        this.setStage(AutoQuest.QuestStage.COLLECT_REQUIRED_ITEMS);
                        return;
                    }

                    AutoQuest.BlockRequirement blockRequirement = this.getNextBlockRequirement();
                    if (blockRequirement != null) {
                        this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                        return;
                    }

                    this.setStage(
                    this.requiresPvpBotQuest && this.getMissingRequiredToolCount() == 0
                    ? AutoQuest.QuestStage.WAIT_FOR_BOT_KILL
                    : (
                    !this.requiresPotionBrewing && !this.requiresFenceCrafting && !this.requiresSpecialToolCrafting && !this.requiresMobKill
                    ? this.getNextQuestStage()
                    : AutoQuest.QuestStage.WAIT_FOR_RTP
                    )
                    );
                    return;
                }

                ClientChatLogger.II1Il11l111II11IIl(
                String.format(Locale.ROOT, "Сдаю %s: %d/%d.", questDisplayName, Math.min(currentQuestItemCount, this.requiredQuestItemCount), this.requiredQuestItemCount)
                );
                InventoryUtils.clickSlot(selectedQuestSlot, 0, SlotActionType.PICKUP);
                this.setStage(AutoQuest.QuestStage.CLAIM_QUEST_REWARD);
                return;
            }
        }

        if (this.actionCooldownTimer.hasElapsed(2000L)) {
            RotationPriorityManager.getInstance().clearRotationRequest(this);
            if (++this.questMenuAttempts > 3) {
                this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
            } else {
                this.setStage(AutoQuest.QuestStage.OPEN_QUEST_MENU);
            }
        }
    }

    private void collectRequiredItems() {
        if (this.marketItemPredicate == null || this.countMatchingItems(this.createMarketSearchPredicate()) >= this.marketTargetItemCount) {
            this.closeHandledScreen();
            if (this.queuedRequirementPredicate == null && !this.queuedItemRequirements.isEmpty()) {
                Object nextItemRequirement = this.queuedItemRequirements.poll();
                this.queuedRequirementPredicate = ((AutoQuest.ItemRequirement)nextItemRequirement).itemPredicate();
                this.queuedRequirementCount = ((AutoQuest.ItemRequirement)nextItemRequirement).requiredCount();
                this.queuedRequirementDescription = ((AutoQuest.ItemRequirement)nextItemRequirement).description();
            }

            if (this.queuedRequirementPredicate != null) {
                this.marketItemPredicate = this.queuedRequirementPredicate;
                this.marketTargetItemCount = this.queuedRequirementCount;
                this.marketSearchTerm = this.queuedRequirementDescription;
                this.queuedRequirementPredicate = null;
                this.marketItemCountBeforePurchase = -1;
                this.marketUnreceivedPurchaseRetries = 0;
                this.marketPurchaseAttempt = 0;
                this.marketPurchaseDialogOpen = false;
                this.marketSearchInProgress = false;
                this.marketNextPageAttempts = 0;
                this.stageRetryCount = 0;
                this.actionCooldownTimer.reset();
            } else {
                ClientChatLogger.II1Il11l111II11IIl("Покупка завершена.");
                this.setStage(this.stageAfterMaterialsCollected);
            }
        } else if (!this.isHandledContainerOpen()) {
            if (!this.marketSearchInProgress || this.actionCooldownTimer.hasElapsed(1500L)) {
                if (this.stageRetryCount++ > 3) {
                    if (!this.advanceMarketSearchAlias()) {
                        this.stopQuestWithMessage("Рынок не открылся.");
                    }
                } else {
                    this.marketSearchInProgress = true;
                    this.actionCooldownTimer.reset();
                    CLIENT.getNetworkHandler().sendChatCommand(this.marketSearchTerm == null ? "market" : "market search " + this.marketSearchTerm);
                }
            }
        } else if (this.actionCooldownTimer
        .hasElapsed(this.marketItemCountBeforePurchase != -1 ? 100L : Math.max(350L, this.questActionDelaySetting.getValue().longValue()))
        && this.marketItemWaitTimer.hasElapsed(this.marketItemCountBeforePurchase != -1 ? 100L : 1000L)) {
            this.actionCooldownTimer.reset();
            if (this.marketItemCountBeforePurchase != -1 && this.countMatchingItems(this.createMarketSearchPredicate()) > this.marketItemCountBeforePurchase) {
                this.closeHandledScreen();
                this.marketPurchaseDialogOpen = false;
                this.marketSearchInProgress = false;
                this.stageRetryCount = 0;
                this.marketItemCountBeforePurchase = -1;
                this.marketPurchaseAttempt = 0;
                this.marketNextPageAttempts = 0;
            } else {
                ScreenHandler marketScreenHandler = ((HandledScreen)CLIENT.currentScreen).getScreenHandler();
                int marketScreenHash = marketScreenHandler.syncId;

                for (int marketSlotIndex = 0; marketSlotIndex < marketScreenHandler.slots.size() - 36; marketSlotIndex++) {
                    ItemStack marketSlotStack = ((Slot)marketScreenHandler.slots.get(marketSlotIndex)).getStack();
                    marketScreenHash = marketScreenHash * 31 + (marketSlotStack.isEmpty() ? 0 : marketSlotStack.getName().getString().hashCode() + marketSlotStack.getCount());
                }

                if (marketScreenHash != this.marketScreenContentHash) {
                    this.marketScreenContentHash = marketScreenHash;
                    this.marketScreenLastChangeAtMillis = System.currentTimeMillis();
                }

                if (System.currentTimeMillis() - this.marketScreenLastChangeAtMillis > 25000L) {
                    ClientChatLogger.II1Il11l111II11IIl("Рынок завис, открываю заново.");
                    this.closeHandledScreen();
                    this.marketPurchaseDialogOpen = false;
                    this.marketSearchInProgress = false;
                    this.stageRetryCount = 0;
                    this.marketItemCountBeforePurchase = -1;
                    this.marketScreenContentHash = -1;
                    this.marketScreenLastChangeAtMillis = System.currentTimeMillis();
                    this.actionCooldownTimer.reset();
                } else {
                    int marketSlotCount = marketScreenHandler.slots.size() - 36;
                    int predicateMatchSlot = -1;
                    int localizedMatchSlot = -1;
                    byte selectedListingSlot = -1;
                    int populatedSlotCount = 0;
                    int buyMoreSlot = -1;
                    int buyLessSlot = -1;
                    int nextPageSlot = -1;

                    for (int marketSlotIndex = 0; marketSlotIndex < marketSlotCount; marketSlotIndex++) {
                        ItemStack marketListingStack = ((Slot)marketScreenHandler.slots.get(marketSlotIndex)).getStack();
                        if (!marketListingStack.isEmpty()) {
                            populatedSlotCount++;
                            String marketItemName = marketListingStack.getName().getString().toLowerCase(Locale.ROOT);
                            if (marketItemName.contains("купить больше")) {
                                buyMoreSlot = marketSlotIndex;
                            } else if (marketItemName.contains("купить меньше")) {
                                buyLessSlot = marketSlotIndex;
                            } else if (marketItemName.contains("следующая страница")) {
                                nextPageSlot = marketSlotIndex;
                            } else if (predicateMatchSlot == -1 && this.marketItemPredicate.test(marketListingStack)) {
                                predicateMatchSlot = marketSlotIndex;
                            } else if (localizedMatchSlot == -1 && this.matchesMarketListing(marketItemName, this.getItemLore(marketListingStack))) {
                                localizedMatchSlot = marketSlotIndex;
                            }
                        }
                    }

                    selectedListingSlot = (byte)(predicateMatchSlot != -1 ? predicateMatchSlot : localizedMatchSlot);
                    if (selectedListingSlot != -1 || System.currentTimeMillis() - this.marketScreenLastChangeAtMillis >= 15000L) {
                        if (selectedListingSlot != -1) {
                            ItemStack selectedListingStack = ((Slot)marketScreenHandler.slots.get(selectedListingSlot)).getStack();
                            String selectedListingLore = this.getItemLore(selectedListingStack);
                            if (!selectedListingLore.contains("перейти к покупке")) {
                                boolean purchaseConfirmationPending = buyMoreSlot == -1
                                && !selectedListingLore.contains("перейти к покупке")
                                && (this.marketPurchaseDialogOpen || selectedListingLore.contains("купить") || selectedListingLore.contains("итого") || selectedListingLore.contains("товар по"));
                                if (!purchaseConfirmationPending || selectedListingLore.contains("чтобы купить") && selectedListingLore.contains("итого")) {
                                    if (purchaseConfirmationPending) {
                                        this.marketPurchaseDialogOpen = false;
                                        if (selectedListingLore.contains("недостаточно средств")) {
                                            this.stopQuestWithMessage("Не хватает денег на рынке.");
                                        } else if (this.marketItemCountBeforePurchase == -1
                                        || this.countMatchingItems(this.createMarketSearchPredicate()) > this.marketItemCountBeforePurchase
                                        || this.marketItemWaitTimer.hasElapsed(150L)) {
                                            if (this.marketItemCountBeforePurchase != -1 && this.countMatchingItems(this.createMarketSearchPredicate()) <= this.marketItemCountBeforePurchase) {
                                                this.marketPurchaseAttempt++;
                                                if (this.marketPurchaseAttempt >= 30) {
                                                    if (++this.marketUnreceivedPurchaseRetries > 3) {
                                                        this.stopQuestWithMessage("Рынок не отдаёт покупку.");
                                                        return;
                                                    }

                                                    ClientChatLogger.II1Il11l111II11IIl("Покупка не пришла, открываю рынок заново.");
                                                    this.marketPurchaseDialogOpen = false;
                                                    this.closeHandledScreen();
                                                    this.marketSearchInProgress = false;
                                                    this.stageRetryCount = 0;
                                                    this.marketPurchaseAttempt = 0;
                                                    this.marketItemCountBeforePurchase = -1;
                                                    this.actionCooldownTimer.reset();
                                                    return;
                                                }
                                            } else {
                                                this.marketPurchaseAttempt = 0;
                                            }

                                            this.marketItemCountBeforePurchase = this.countMatchingItems(this.createMarketSearchPredicate());
                                            ClientChatLogger.II1Il11l111II11IIl(String.format(Locale.ROOT, "Покупаю %s на рынке.", selectedListingStack.getName().getString()));
                                            this.sendSlotClick(selectedListingSlot, this.marketPurchaseAttempt % 2 == 0 ? 0 : 1, SlotActionType.PICKUP);
                                            this.marketItemWaitTimer.reset();
                                        }
                                    } else if (selectedListingStack.getMaxCount() == 1) {
                                        if (this.marketItemCountBeforePurchase == -1
                                        || this.countMatchingItems(this.createMarketSearchPredicate()) > this.marketItemCountBeforePurchase
                                        || this.marketItemWaitTimer.hasElapsed(400L)) {
                                            if (selectedListingLore.contains("недостаточно средств")) {
                                                this.stopQuestWithMessage("Не хватает денег на рынке.");
                                            } else {
                                                this.marketItemCountBeforePurchase = this.countMatchingItems(this.createMarketSearchPredicate());
                                                ClientChatLogger.II1Il11l111II11IIl(
                                                String.format(Locale.ROOT, "Покупаю %s x1 на рынке.", selectedListingStack.getName().getString())
                                                );
                                                this.sendSlotClick(selectedListingSlot, 1, SlotActionType.PICKUP);
                                                this.marketItemWaitTimer.reset();
                                            }
                                        }
                                    } else if (buyMoreSlot == -1) {
                                        this.sendSlotClick(selectedListingSlot, 0, SlotActionType.PICKUP);
                                    } else if (!selectedListingLore.contains("недостаточно средств")) {
                                        int missingPurchaseCount = this.marketTargetItemCount - this.countMatchingItems(this.createMarketSearchPredicate());
                                        int marketStackCount = selectedListingStack.getCount();
                                        if (marketStackCount == this.observedMarketStackCount) {
                                            this.marketQuantityRetryCount++;
                                        } else {
                                            this.marketQuantityRetryCount = 0;
                                            this.observedMarketStackCount = marketStackCount;
                                        }

                                        if (marketStackCount < missingPurchaseCount && this.marketQuantityRetryCount < 3) {
                                            this.sendSlotClick(buyMoreSlot, 0, missingPurchaseCount - marketStackCount >= 16 ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
                                        } else if (marketStackCount > missingPurchaseCount && buyLessSlot != -1) {
                                            this.sendSlotClick(buyLessSlot, 0, marketStackCount - missingPurchaseCount >= 16 ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
                                        } else {
                                            ClientChatLogger.II1Il11l111II11IIl(
                                            String.format(Locale.ROOT, "Покупаю %s x%d на рынке.", selectedListingStack.getName().getString(), marketStackCount)
                                            );
                                            this.marketItemCountBeforePurchase = this.countMatchingItems(this.createMarketSearchPredicate());
                                            this.sendSlotClick(selectedListingSlot, 1, SlotActionType.PICKUP);
                                            this.marketItemWaitTimer.reset();
                                            this.marketQuantityRetryCount = 0;
                                            this.observedMarketStackCount = 0;
                                        }
                                    } else if (buyLessSlot != -1 && selectedListingStack.getCount() > 1) {
                                        this.sendSlotClick(buyLessSlot, 0, SlotActionType.PICKUP);
                                    } else {
                                        this.stopQuestWithMessage("Не хватает денег на рынке.");
                                    }
                                } else {
                                    if (System.currentTimeMillis() - Math.max(this.marketScreenLastChangeAtMillis, this.lastMarketInteractionAtMillis) > 15000L) {
                                        this.marketPurchaseDialogOpen = false;
                                        ClientChatLogger.II1Il11l111II11IIl("Цена лота не загрузилась, открываю рынок заново.");
                                        this.closeHandledScreen();
                                        this.marketSearchInProgress = false;
                                        this.stageRetryCount = 0;
                                        this.marketItemCountBeforePurchase = -1;
                                        this.actionCooldownTimer.reset();
                                    }
                                }
                            } else if (!this.marketPurchaseDialogOpen || System.currentTimeMillis() - this.lastMarketInteractionAtMillis >= 3000L) {
                                this.sendSlotClick(selectedListingSlot, 0, SlotActionType.PICKUP);
                                this.marketPurchaseDialogOpen = true;
                                this.lastMarketInteractionAtMillis = System.currentTimeMillis();
                                this.marketItemCountBeforePurchase = -1;
                                this.marketPurchaseAttempt = 0;
                            }
                        } else if (nextPageSlot != -1 && this.marketNextPageAttempts++ < 20) {
                            this.sendSlotClick(nextPageSlot, 0, SlotActionType.PICKUP);
                        } else {
                            if (!this.advanceMarketSearchAlias()) {
                                this.stopQuestWithMessage("Нужного предмета нет на рынке.");
                            }
                        }
                    }
                }
            }
        }
    }

    private Predicate<ItemStack> createMarketSearchPredicate() {
        Predicate requiredItemPredicate = this.marketItemPredicate;
        return stack -> !stack.isEmpty() && (requiredItemPredicate.test(stack) || this.matchesRequiredItemName(stack));
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean matchesRequiredItemName(ItemStack stack) {
        if (this.marketSearchTerm == null) {
            return false;
        }

        String itemName = stack.getName().getString().toLowerCase(Locale.ROOT);

        for (String alias : getItemNameAliases(this.marketSearchTerm)) {
            if (itemName.equals(alias)) {
                return true;
            }
        }

        return false;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean matchesMarketListing(String itemName, String itemLore) {
        if (this.marketSearchTerm == null || itemName.isEmpty()) {
            return false;
        }

        if (!itemLore.contains("купить") && !itemLore.contains("покупк")) {
            return false;
        }

        String[] textArray = getItemNameAliases(this.marketSearchTerm);
        if (!Arrays.asList(textArray).contains("бутылочка")) {
            for (String alias : textArray) {
                if (itemName.equals(alias) || itemName.startsWith(alias + " ")) {
                    return true;
                }
            }

            if (Arrays.asList(textArray).contains("ведро воды")) {
                return itemName.contains("ведро") && (itemName.contains("вод") || itemLore.contains("вод"));
            } else {
                return !Arrays.asList(textArray).contains("зельеварка") ? false : itemName.contains("зельевар") || itemName.contains("варочн");
            }
        } else {
            return !itemName.contains("вод")
            && !itemName.contains("опыт")
            && !itemName.contains("мед")
            && !itemName.contains("мёд")
            && !itemName.contains("зель")
            && !itemName.contains("туман")
            && !itemName.contains("оседающ")
            && !itemName.contains("взрывн")
            ? itemName.equals("бутылочка")
            || itemName.equals("пузырёк")
            || itemName.equals("пузырек")
            || itemName.equals("стеклянный пузырёк")
            || itemName.equals("стеклянная бутылочка")
            || itemName.equals("glass bottle")
            : false;
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private static String[] getItemNameAliases(String itemName) {
        for (String[] aliases : ITEM_NAME_ALIASES) {
            for (String alias : aliases) {
                if (alias.equals(itemName)) {
                    return aliases;
                }
            }
        }

        return new String[]{itemName};
    }

    private static String getCanonicalItemName(String itemName) {
        return getItemNameAliases(itemName)[0];
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean advanceMarketSearchAlias() {
        if (this.marketSearchTerm == null) {
            return false;
        }

        String[] textArray = getItemNameAliases(this.marketSearchTerm);
        if (this.currentMarketAlias == null || !Arrays.asList(textArray).contains(this.currentMarketAlias)) {
            this.currentMarketAlias = this.marketSearchTerm;
            this.marketAliasIndex = 0;
        }

        while (this.marketAliasIndex < textArray.length) {
            String alias = textArray[this.marketAliasIndex++];
            if (!alias.equals(this.marketSearchTerm)) {
                this.closeHandledScreen();
                this.marketSearchTerm = alias;
                this.marketSearchInProgress = false;
                this.marketNextPageAttempts = 0;
                this.stageRetryCount = 0;
                this.actionCooldownTimer.reset();
                ClientChatLogger.II1Il11l111II11IIl("Ищу на рынке как: " + alias);
                return true;
            }
        }

        this.currentMarketAlias = null;
        return false;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void sendSlotClick(int slotIndex, int mouseButton, SlotActionType slotActionType) {
        ScreenHandler containerHandler = CLIENT.player.currentScreenHandler;
        CLIENT.getNetworkHandler()
        .sendPacket(
        new ClickSlotC2SPacket(
        containerHandler.syncId,
        containerHandler.getRevision(),
        (short)slotIndex,
        (byte)mouseButton,
        slotActionType,
        new Int2ObjectOpenHashMap(),
        ItemStackHash.EMPTY
        )
        );
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void stopQuestWithMessage(String reason) {
        this.closeHandledScreen();
        this.marketItemPredicate = null;
        ClientChatLogger.l1I1IlllIlI(reason + " Выключаюсь.");
        this.setEnabled(false);
    }

    private boolean configureQuestRequirements() {
        this.blockRequirements.clear();
        this.requiresFenceCrafting = false;
        this.requiresSpecialToolCrafting = false;
        this.requiresPvpBotQuest = false;
        this.pvpRequiredToolItem = null;
        this.queuedRequirementPredicate = null;
        this.queuedItemRequirements.clear();
        this.marketItemPredicate = null;
        this.marketSearchTerm = null;
        if (this.requiresPotionBrewing) {
            return this.configurePotionQuest();
        }

        if (this.requiresEating) {
            if (!this.questItem.getComponents().contains(DataComponentTypes.FOOD)) {
                return false;
            }

            this.marketItemPredicate = this.questItemPredicate;
            this.marketTargetItemCount = this.requiredQuestItemCount;
            this.marketSearchTerm = this.questName;
            this.stageAfterMaterialsCollected = AutoQuest.QuestStage.EAT_QUEST_FOOD;
            this.stageAfterObjective = AutoQuest.QuestStage.EAT_QUEST_FOOD;
            int missingSupportBlocks = 64 - this.countMatchingItems(SUPPORT_BLOCK_ITEM_PREDICATE);
            if (missingSupportBlocks > 0) {
                this.queuedItemRequirements
                .add(
                new AutoQuest.ItemRequirement(
                stack -> stack.isOf(Items.COBBLESTONE),
                this.countMatchingItems(stack -> stack.isOf(Items.COBBLESTONE)) + missingSupportBlocks,
                "булыжник"
                )
                );
            }

            if (this.countMatchingItems(this.questItemPredicate) >= this.requiredQuestItemCount && !this.queuedItemRequirements.isEmpty()) {
                Object queuedRequirement = this.queuedItemRequirements.poll();
                this.marketItemPredicate = ((AutoQuest.ItemRequirement)queuedRequirement).itemPredicate();
                this.marketTargetItemCount = ((AutoQuest.ItemRequirement)queuedRequirement).requiredCount();
                this.marketSearchTerm = ((AutoQuest.ItemRequirement)queuedRequirement).description();
            }

            this.hungerRunStarted = false;
            this.shouldRunToExhaustHunger = false;
            return true;
        } else {
            String questItemPath = Registries.ITEM.getId(this.questItem).getPath();
            int currentQuestItemCount = this.countMatchingItems(this.questItemPredicate);
            int missingQuestItemCount = Math.max(0, this.requiredQuestItemCount - currentQuestItemCount);
            if (this.breakObjective) {
                Block block = Block.getBlockFromItem(this.questItem);
                if (block == Blocks.AIR) {
                    return false;
                }

                Item item = this.questItem;
                Item minedBlockItem = block == Blocks.DEEPSLATE ? Items.COBBLED_DEEPSLATE : (block == Blocks.STONE ? Items.COBBLESTONE : item);
                Predicate<ItemStack> minedBlockItemPredicate = stack -> stack.isOf(minedBlockItem) || stack.isOf(item);
                this.blockRequirements.add(new AutoQuest.BlockRequirement(questItemPath, minedBlockItemPredicate, new Block[]{block}, this.requiredQuestItemCount, true));
                this.minedBlockCount = 0;
                this.stageAfterObjective = AutoQuest.QuestStage.RETURN_TO_SPAWN;
                return true;
            } else if (!this.requiresCrafting || this.questItem != Items.GOLDEN_PICKAXE && this.questItem != Items.DIAMOND_PICKAXE) {
                if (questItemPath.endsWith("_fence") && !questItemPath.startsWith("nether")) {
                    String woodType = questItemPath.substring(0, questItemPath.length() - "_fence".length());
                    Object plankItem = (Item)Registries.ITEM.get(Identifier.ofVanilla(woodType + "_planks"));
                    Item logItem = plankItem == Items.AIR ? null : this.findLogForPlankItem(woodType + "_planks");
                    if (logItem == null) {
                        return false;
                    }

                    this.requiresFenceCrafting = true;
                    this.fencePlankItem = (Item)plankItem;
                    int requiredLogCount = Math.max(0, (this.getRequiredLogCount() - this.countMatchingItems(stack -> stack.isOf((Item)plankItem)) + 3) / 4);
                    this.marketItemPredicate = stack -> stack.isOf(logItem);
                    this.marketTargetItemCount = requiredLogCount + 1;
                    this.marketSearchTerm = new ItemStack(logItem).getName().getString().toLowerCase(Locale.ROOT);
                    this.stageAfterMaterialsCollected = AutoQuest.QuestStage.WAIT_FOR_RTP;
                    this.stageAfterObjective = AutoQuest.QuestStage.PREPARE_CRAFTING_INVENTORY;
                    return true;
                } else {
                    if (questItemPath.endsWith("_planks")
                    || questItemPath.endsWith("_log")
                    || questItemPath.endsWith("_stem")
                    || this.questItem == Items.COAL
                    || this.questItem == Items.RAW_IRON
                    || this.questItem == Items.COBBLESTONE
                    || this.questItem == Items.IRON_INGOT) {
                        this.marketItemPredicate = this.questItemPredicate;
                        this.marketTargetItemCount = this.requiredQuestItemCount;
                        this.marketSearchTerm = this.questName;
                        this.stageAfterMaterialsCollected = AutoQuest.QuestStage.RETURN_TO_SPAWN;
                        this.stageAfterObjective = AutoQuest.QuestStage.RETURN_TO_SPAWN;
                        return true;
                    }

                    if (this.requiresMobKill && this.questName.startsWith("игрок")) {
                        return this.configurePlayerKillQuest();
                    }

                    if (this.requiresMobKill) {
                        this.targetMobType = this.findEntityTypeByLocalizedName(this.questName);
                        if (this.targetMobType == null) {
                            return false;
                        }

                        this.completedMobKills = 0;
                        this.currentTarget = null;
                        this.stageAfterObjective = AutoQuest.QuestStage.RETURN_TO_SPAWN;
                        return true;
                    } else if (this.requiresItemDelivery) {
                        this.marketItemPredicate = this.questItemPredicate;
                        this.marketTargetItemCount = this.requiredQuestItemCount;
                        this.marketSearchTerm = this.questName;
                        this.stageAfterMaterialsCollected = AutoQuest.QuestStage.RETURN_TO_SPAWN;
                        this.stageAfterObjective = AutoQuest.QuestStage.RETURN_TO_SPAWN;
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                boolean diamondPickaxeQuest = this.questItem == Items.DIAMOND_PICKAXE;
                Predicate<ItemStack> toolItemPredicate = stack -> stack.isOf(questItem);
                this.setToolCraftingRequirements(this.questItem, currentQuestItemCount + this.requiredQuestItemCount, toolItemPredicate, new int[]{1, 2, 3}, AutoQuest.QuestStage.RETURN_TO_SPAWN);
                this.marketItemPredicate = toolItemPredicate;
                this.marketTargetItemCount = this.requiredQuestItemCount * 3;
                this.marketSearchTerm = diamondPickaxeQuest ? "алмаз" : "золотой слиток";
                if (this.countMatchingItems(PLANK_ITEM_PREDICATE) < this.getRequiredLogCount() && this.countMatchingItems(stack -> stack.isOf(Items.OAK_LOG)) < 2) {
                    this.queuedRequirementPredicate = toolItemPredicate;
                    this.queuedRequirementCount = this.marketTargetItemCount;
                    this.queuedRequirementDescription = this.marketSearchTerm;
                    this.marketItemPredicate = stack -> stack.isOf(Items.OAK_LOG);
                    this.marketTargetItemCount = 2;
                    this.marketSearchTerm = "дубовое бревно";
                }

                this.stageAfterMaterialsCollected = AutoQuest.QuestStage.WAIT_FOR_RTP;
                this.stageAfterObjective = AutoQuest.QuestStage.PREPARE_CRAFTING_INVENTORY;
                return true;
            }
        }
    }

    private int getRequiredLogsForFenceQuest() {
        return this.requiresFenceCrafting ? (Math.max(0, this.requiredQuestItemCount - this.countMatchingItems(this.questItemPredicate)) + 2) / 3 : 0;
    }

    private void setToolCraftingRequirements(Item item, int requiredToolCount, Predicate<ItemStack> materialPredicate, int[] recipeSlots, AutoQuest.QuestStage afterCraftStage) {
        this.requiresSpecialToolCrafting = true;
        this.specialToolItem = item;
        this.specialToolRequiredCount = requiredToolCount;
        this.specialToolMaterialPredicate = materialPredicate;
        this.specialToolRecipeSlots = recipeSlots;
        this.stageAfterToolCrafting = afterCraftStage;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean configurePlayerKillQuest() {
        int toolNameStart = this.questName.indexOf("с помощью ");
        this.pvpRequiredToolItem = toolNameStart >= 0 ? this.findItemByLocalizedName(this.questName.substring(toolNameStart + "с помощью ".length()).trim()) : null;
        if (this.pvpRequiredToolItem != null) {
            this.requiresPvpBotQuest = true;
            this.completedMobKills = 0;
            this.currentTarget = null;
            this.botCallPending = false;
            this.stageAfterObjective = AutoQuest.QuestStage.WAIT_FOR_BOT_KILL;
            Item item = this.pvpRequiredToolItem;
            if (this.countMatchingItems(stack -> stack.isOf(item)) <= 0) {
                if (this.pvpRequiredToolItem == Items.WOODEN_HOE) {
                    this.setToolCraftingRequirements(Items.WOODEN_HOE, 1, PLANK_ITEM_PREDICATE, new int[]{1, 2}, AutoQuest.QuestStage.WAIT_FOR_BOT_KILL);
                    int additionalOakLogsRequired;
                    if ((additionalOakLogsRequired = Math.max(0, (this.getRequiredLogCount() - this.countMatchingItems(PLANK_ITEM_PREDICATE) + 3) / 4 - this.countMatchingItems(LOG_ITEM_PREDICATE))) > 0) {
                        this.marketItemPredicate = stack -> stack.isOf(Items.OAK_LOG);
                        this.marketTargetItemCount = this.countMatchingItems(this.marketItemPredicate) + additionalOakLogsRequired;
                        this.marketSearchTerm = "дубовое бревно";
                    }

                    this.stageAfterMaterialsCollected = AutoQuest.QuestStage.WAIT_FOR_RTP;
                    this.stageAfterObjective = AutoQuest.QuestStage.PREPARE_CRAFTING_INVENTORY;
                    return true;
                } else {
                    ClientChatLogger.l1I1IlllIlI("Положите в инвентарь: " + new ItemStack(this.pvpRequiredToolItem).getName().getString());
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private Item findItemByLocalizedName(String localizedItemName) {
        for (Object registeredItem : Registries.ITEM) {
            if (new ItemStack((ItemConvertible)registeredItem).getName().getString().toLowerCase(Locale.ROOT).equals(localizedItemName)) {
                return (Item)registeredItem;
            }
        }

        return null;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private int getMissingRequiredToolCount() {
        if (this.requiresSpecialToolCrafting) {
            Item item = this.specialToolItem;
            return Math.max(0, this.specialToolRequiredCount - this.countMatchingItems(stack -> stack.isOf(item)));
        } else {
            return 0;
        }
    }

    private int getRequiredStickCount() {
        int requiredStickCount = this.getRequiredLogsForFenceQuest() * 2 + this.getMissingRequiredToolCount() * 2;
        return (Math.max(0, requiredStickCount - this.countMatchingItems(stack -> stack.isOf(Items.STICK))) + 3) / 4;
    }

    private boolean needsCraftingTable() {
        return this.findNearbyBlock(Blocks.CRAFTING_TABLE) == null && this.countMatchingItems(stack -> stack.isOf(Items.CRAFTING_TABLE)) == 0;
    }

    private int getRequiredLogCount() {
        int requiredLogCount = this.requiresSpecialToolCrafting && this.specialToolMaterialPredicate == PLANK_ITEM_PREDICATE ? this.getMissingRequiredToolCount() * this.specialToolRecipeSlots.length : 0;
        return this.getRequiredLogsForFenceQuest() * 4 + this.getRequiredStickCount() * 2 + requiredLogCount + (this.needsCraftingTable() ? 4 : 0);
    }

    private Predicate<ItemStack> getQuestItemPredicate() {
        Item item = this.fencePlankItem;
        return this.requiresFenceCrafting ? stack -> stack.isOf(item) : PLANK_ITEM_PREDICATE;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private Predicate<ItemStack> getMaterialPredicate() {
        if (this.requiresFenceCrafting) {
            Item item = this.findLogForPlankItem(Registries.ITEM.getId(this.fencePlankItem).getPath());
            return stack -> stack.isOf(item);
        } else {
            return LOG_ITEM_PREDICATE;
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void prepareCraftingInventory() {
        if (this.actionCooldownTimer.hasElapsed(this.questActionDelaySetting.getValue().longValue())) {
            if (this.isHandledContainerOpen()) {
                this.closeHandledScreen();
            } else {
                this.actionCooldownTimer.reset();
                PlayerScreenHandler playerScreenHandler = CLIENT.player.playerScreenHandler;
                Predicate questItemPredicate = this.getQuestItemPredicate();
                Predicate materialPredicate = this.getMaterialPredicate();
                LinkedHashMap<Integer, Predicate<ItemStack>> linkedHashMap = new LinkedHashMap<>();
                if (this.countMatchingItems(questItemPredicate) < this.getRequiredLogCount() && this.countMatchingItems(materialPredicate) > 0) {
                    linkedHashMap.put(1, materialPredicate);
                } else if (this.getRequiredStickCount() > 0) {
                    linkedHashMap.put(1, questItemPredicate);
                    linkedHashMap.put(3, questItemPredicate);
                }

                if (!this.maintainCraftingGrid(playerScreenHandler, 1, 4, 9, linkedHashMap)) {
                    if (linkedHashMap.isEmpty()) {
                        this.setStage(AutoQuest.QuestStage.CRAFT_QUEST_ITEM);
                    } else {
                        ClientChatLogger.l1I1IlllIlI("Не хватает дерева на крафт.");
                        this.currentBlockRequirement = null;
                        this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                    }
                }
            }
        }
    }

    private boolean maintainCraftingGrid(ScreenHandler handler, int firstCraftingSlot, int craftingSlotCount, int playerInventoryStartSlot, Map<Integer, Predicate<ItemStack>> craftingRequirements) {
        if (!handler.getCursorStack().isEmpty()) {
            for (int slotIndex = playerInventoryStartSlot; slotIndex < handler.slots.size(); slotIndex++) {
                if (handler.slots.get(slotIndex).getStack().isEmpty()) {
                    InventoryUtils.clickSlot(slotIndex, 0, SlotActionType.PICKUP);
                    return true;
                }
            }

            return true;
        } else {
            for (int craftingSlotIndex = playerInventoryStartSlot; craftingSlotIndex < firstCraftingSlot + craftingSlotCount; craftingSlotIndex++) {
                ItemStack slotStack = handler.slots.get(craftingSlotIndex).getStack();
                Predicate<ItemStack> slotRequirement = craftingRequirements.get(craftingSlotIndex);
                if (!slotStack.isEmpty() && (slotRequirement == null || !slotRequirement.test(slotStack))) {
                    InventoryUtils.clickSlot(craftingSlotIndex, 0, SlotActionType.QUICK_MOVE);
                    return true;
                }
            }

            if (craftingRequirements.isEmpty()) {
                return false;
            }

            for (Map.Entry<Integer, Predicate<ItemStack>> craftingRequirement : craftingRequirements.entrySet()) {
                if (handler.slots.get(craftingRequirement.getKey()).getStack().isEmpty()) {

                    int sourceInventorySlot = -1;

                    for (int playerInventorySlotIndex = playerInventoryStartSlot; playerInventorySlotIndex < handler.slots.size(); playerInventorySlotIndex++) {
                        if (craftingRequirement.getValue().test(handler.slots.get(playerInventorySlotIndex).getStack())) {
                            sourceInventorySlot = playerInventorySlotIndex;
                            break;
                        }
                    }

                    if (sourceInventorySlot == -1) {
                        return false;
                    }

                    InventoryUtils.clickSlot(sourceInventorySlot, 0, SlotActionType.PICKUP);

                    for (Map.Entry<Integer, Predicate<ItemStack>> matchingRequirement : craftingRequirements.entrySet()) {
                        if (matchingRequirement.getValue() == craftingRequirement.getValue() && handler.slots.get(matchingRequirement.getKey()).getStack().isEmpty()) {
                            InventoryUtils.clickSlot(matchingRequirement.getKey(), 1, SlotActionType.PICKUP);
                        }
                    }

                    InventoryUtils.clickSlot(sourceInventorySlot, 0, SlotActionType.PICKUP);
                    this.craftingTransferTimer.reset();
                    return true;
                }
            }

            if (!((Slot)handler.slots.get(0)).getStack().isEmpty()) {
                InventoryUtils.clickSlot(0, 0, SlotActionType.QUICK_MOVE);
                this.craftingTransferTimer.reset();
                return true;
            }

            if (this.craftingTransferTimer.hasElapsed(2000L)) {
                for (int playerSlotIndex = firstCraftingSlot; playerSlotIndex < firstCraftingSlot + craftingSlotCount; playerSlotIndex++) {
                    if (!((Slot)handler.slots.get(playerSlotIndex)).getStack().isEmpty()) {
                        InventoryUtils.clickSlot(playerSlotIndex, 0, SlotActionType.QUICK_MOVE);
                        return true;
                    }
                }

                this.craftingTransferTimer.reset();
            }

            return true;
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void craftFence(CraftingScreenHandler craftingHandler) {
        if (this.countMatchingItems(this.questItemPredicate) < this.requiredQuestItemCount) {
            Item item = this.fencePlankItem;
            Predicate<ItemStack> plankPredicate = stack -> stack.isOf(item);
            Predicate<ItemStack> stickPredicate = stack -> stack.isOf(Items.STICK);
            LinkedHashMap<Integer, Predicate<ItemStack>> linkedHashMap2 = new LinkedHashMap<>();
            linkedHashMap2.put(1, plankPredicate);
            linkedHashMap2.put(2, stickPredicate);
            linkedHashMap2.put(3, plankPredicate);
            linkedHashMap2.put(4, plankPredicate);
            linkedHashMap2.put(5, stickPredicate);
            linkedHashMap2.put(6, plankPredicate);
            if (!this.maintainCraftingGrid(craftingHandler, 1, 9, 10, linkedHashMap2)) {
                this.closeHandledScreen();
                this.setStage(AutoQuest.QuestStage.PREPARE_CRAFTING_INVENTORY);
            }
        } else {
            LinkedHashMap<Integer, Predicate<ItemStack>> linkedHashMap = new LinkedHashMap<>();
            if (!this.maintainCraftingGrid(craftingHandler, 1, 9, 10, linkedHashMap)) {
                this.closeHandledScreen();
                ClientChatLogger.II1Il11l111II11IIl("Заборы скрафчены.");
                this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void craftQuestTool(CraftingScreenHandler craftingHandler) {
        if (this.getMissingRequiredToolCount() == 0) {
            LinkedHashMap<Integer, Predicate<ItemStack>> linkedHashMap = new LinkedHashMap<>();
            if (!this.maintainCraftingGrid(craftingHandler, 1, 9, 10, linkedHashMap)) {
                this.closeHandledScreen();
                ClientChatLogger.II1Il11l111II11IIl("Инструмент скрафчен.");
                this.setStage(this.stageAfterToolCrafting);
            }
        } else {
            Predicate<ItemStack> linkedHashMap = stack -> stack.isOf(Items.STICK);
            LinkedHashMap<Integer, Predicate<ItemStack>> linkedHashMap2 = new LinkedHashMap<>();

            for (int recipeSlot : this.specialToolRecipeSlots) {
                linkedHashMap2.put(recipeSlot, this.specialToolMaterialPredicate);
            }

            linkedHashMap2.put(5, linkedHashMap);
            linkedHashMap2.put(8, linkedHashMap);
            if (!this.maintainCraftingGrid(craftingHandler, 1, 9, 10, linkedHashMap2)) {
                this.closeHandledScreen();
                if (this.countMatchingItems(this.specialToolMaterialPredicate) >= this.specialToolRecipeSlots.length) {
                    this.setStage(AutoQuest.QuestStage.PREPARE_CRAFTING_INVENTORY);
                } else {
                    ClientChatLogger.l1I1IlllIlI("Не хватает материала на инструмент.");
                    this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
                }
            }
        }
    }

    public static BotSession getActiveQuestBot() {
        AutoQuest autoQuest = instance;
        return autoQuest == null ? null : autoQuest.questBot;
    }

    private BotSession ensureQuestBotReady() {
        if (this.questBot == null) {
            this.questBot = SavedBots.createRandomSession();
            this.previousBotConnectionStatus = null;
            ClientChatLogger.II1Il11l111II11IIl("Создан бот " + this.questBot.getUsername() + ".");
        }

        BotSession disconnect = this.questBot;
        this.trackedBotUsername = disconnect.getUsername().toLowerCase(Locale.ROOT);
        BotSession.BotConnectionStatus observedConnectionStatus = disconnect.getConnectionStatus();
        if (observedConnectionStatus != this.previousBotConnectionStatus) {
            if (disconnect.isOfflineOrDisconnected() && this.previousBotConnectionStatus == BotSession.BotConnectionStatus.ONLINE) {
                String i1l11IIlIIIll1l1lI1II = disconnect.getLastDisconnectReason();
                ClientChatLogger.l1I1IlllIlI(
                "Бот отключился" + (i1l11IIlIIIll1l1lI1II != null && !i1l11IIlIIIll1l1lI1II.isBlank() ? ": " + i1l11IIlIIIll1l1lI1II : "") + ". Переподключаю."
                );
                this.botCallPending = false;
                this.currentTarget = null;
            }

            this.previousBotConnectionStatus = observedConnectionStatus;
            this.botConnectionTimer.reset();
        }

        this.stageWatchdogTimer.reset();
        int griefNumber = this.getConfiguredGriefNumber();
        if (disconnect.isOfflineOrDisconnected()) {
            if (this.actionCooldownTimer.hasElapsed(5000L)) {
                if (griefNumber <= 0) {
                    ClientChatLogger.l1I1IlllIlI("Не удалось узнать номер грифа, укажите его в настройке.");
                    this.setEnabled(false);
                    return null;
                }

                disconnect.configureTargetLobby(griefNumber, "gagaga");
                if (!SavedBots.configureSessionForCurrentServer(disconnect)) {
                    ClientChatLogger.l1I1IlllIlI("Основной аккаунт не на сервере.");
                    this.setEnabled(false);
                    return null;
                }

                ClientChatLogger.II1Il11l111II11IIl(String.format(Locale.ROOT, "Захожу ботом %s на гриф #%d.", disconnect.getUsername(), griefNumber));
                this.actionCooldownTimer.reset();
                this.botConnectionTimer.reset();
            }

            return null;
        } else if (observedConnectionStatus != BotSession.BotConnectionStatus.ONLINE) {
            if (this.botConnectionTimer.hasElapsed(60000L)) {
                ClientChatLogger.l1I1IlllIlI("Бот завис на входе, переподключаю.");
                disconnect.disconnectFromServer();
                this.actionCooldownTimer.reset();
            }

            return null;
        } else if (disconnect.getTargetLobbyNumber() > 0 && (griefNumber <= 0 || disconnect.getTargetLobbyNumber() == griefNumber)) {
            if (!disconnect.isConfiguredLobbyReady()) {
                if (this.botConnectionTimer.hasElapsed(120000L)) {
                    ClientChatLogger.l1I1IlllIlI("Бот не смог зайти на гриф (капча или меню), переподключаю.");
                    disconnect.disconnectFromServer();
                    this.actionCooldownTimer.reset();
                }

                return null;
            } else {
                this.botConnectionTimer.reset();
                return disconnect;
            }
        } else {
            disconnect.disconnectFromServer();
            this.actionCooldownTimer.reset();
            return null;
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void waitForPvpBotKill() {
        if (this.stageRetryCount++ == 0) {
            this.cancelBaritonePath();
            RotationPriorityManager.getInstance().clearRotationRequest(this);
            CLIENT.getNetworkHandler().sendChatCommand("spawn");
            ClientChatLogger.II1Il11l111II11IIl("Жду, пока бот зайдёт на гриф.");
        }

        if (!this.isHandledContainerOpen()) {
            BotSession disconnect = this.ensureQuestBotReady();
            if (disconnect != null) {
                ClientChatLogger.II1Il11l111II11IIl("Бот на грифе, иду на RTP.");
                this.botCallPending = false;
                this.botReturningToHub = false;
                this.currentTarget = null;
                this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
            }
        } else {
            this.closeHandledScreen();
        }
    }

    public static boolean isPvpQuestActive() {
        AutoQuest autoQuest = instance;
        return autoQuest != null
        && autoQuest.isEnabled()
        && autoQuest.requiresPvpBotQuest
        && (autoQuest.currentStage == AutoQuest.QuestStage.WAIT_FOR_BOT_KILL || autoQuest.currentStage == AutoQuest.QuestStage.COMPLETE_PVP_KILL);
    }

    private void completePvpQuest() {
        if (this.completedMobKills >= this.requiredQuestItemCount) {
            this.cancelBaritonePath();
            RotationPriorityManager.getInstance().clearRotationRequest(this);
            ClientChatLogger.II1Il11l111II11IIl("Все убийства сделаны, сдаю квест.");
            this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
        } else if (this.isHandledContainerOpen()) {
            this.closeHandledScreen();
        } else if (this.botReturningToHub) {
            BotSession disconnect = this.questBot;
            if (disconnect == null || this.findNearbyPlayer(disconnect.getUsername()) == null) {
                this.completedMobKills++;
                ClientChatLogger.II1Il11l111II11IIl(
                String.format(Locale.ROOT, "Бот вышел в PvP, убийство засчитано %d/%d.", this.completedMobKills, this.requiredQuestItemCount)
                );
                this.resetPvpTargetState();
            } else if (this.botHubReturnTimer.hasElapsed(8000L)) {
                disconnect.returnToServerSelector();
                this.botHubReturnTimer.reset();
            }
        } else {
            BotSession disconnect2 = this.ensureQuestBotReady();
            if (disconnect2 != null) {
                if (this.currentTarget != null && (this.currentTarget.isDead() || this.currentTarget.isRemoved())) {
                    if (this.hasAttackedCurrentTarget) {
                        this.completedMobKills++;
                        ClientChatLogger.II1Il11l111II11IIl(String.format(Locale.ROOT, "Убито %d/%d.", this.completedMobKills, this.requiredQuestItemCount));
                    }

                    this.resetPvpTargetState();
                }

                if (this.currentTarget == null) {
                    this.hasAttackedCurrentTarget = false;
                    this.currentTarget = this.findNearbyPlayer(disconnect2.getUsername());
                }

                if (this.currentTarget == null) {
                    if (Aceopxykhbtm.II1Il11l111II11IIl()) {
                        this.botCallPending = false;
                    } else if (!this.botCallPending) {
                        if (this.botCallRetryTimer.hasElapsed(3000L)) {
                            disconnect2.sendChatMessage("/call " + CLIENT.player.getName().getString());
                            this.botCallPending = true;
                            this.pendingTeleportAcceptAtMillis = System.currentTimeMillis() + 2500L;
                            this.botCallRetryTimer.reset();
                        }
                    } else if (this.pendingTeleportAcceptAtMillis != 0L && System.currentTimeMillis() >= this.pendingTeleportAcceptAtMillis) {
                        this.pendingTeleportAcceptAtMillis = 0L;
                        CLIENT.getNetworkHandler().sendChatCommand("tpaccept");
                    } else {
                        if (this.botCallRetryTimer.hasElapsed(15000L)) {
                            this.botCallPending = false;
                            this.botCallRetryTimer.reset();
                        }
                    }
                } else {
                    this.stageWatchdogTimer.reset();
                    if (this.hasAttackedCurrentTarget) {
                        this.cancelBaritonePath();
                        RotationPriorityManager.getInstance().clearRotationRequest(this);
                        disconnect2.returnToServerSelector();
                        this.botReturningToHub = true;
                        this.botHubReturnTimer.reset();
                        ClientChatLogger.II1Il11l111II11IIl("Ударил бота, он выходит в /hub.");
                    } else if (!(CLIENT.player.distanceTo(this.currentTarget) > 3.0F)) {
                        this.cancelBaritonePath();
                        RotationPriorityManager.getInstance()
                        .requestRotation(
                        RotationUtils.lookAt(CLIENT.player.getEyePos(), this.currentTarget.getBoundingBox().getCenter()),
                        FocusMode.FREE,
                        60.0F,
                        3,
                        false,
                        this
                        );
                        this.selectHotbarItem(this.pvpRequiredToolItem);
                        if (CLIENT.player.getAttackCooldownProgress(0.5F) >= 0.95F) {
                            this.attackQueued = true;
                        }
                    } else {
                        if (this.combatPathTimer.hasElapsed(1000L) || !this.getBaritone().getCustomGoalProcess().isActive()) {
                            BaritoneAPI.getSettings().allowBreak.value = false;
                            this.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalNear(this.currentTarget.getBlockPos(), 1));
                            this.combatPathTimer.reset();
                        }
                    }
                }
            }
        }
    }

    private static Predicate<ItemStack> isPotion(RegistryEntry<Potion> registryEntry) {
        return stack -> stack.isOf(Items.POTION) && getPotionContents(stack) != null && getPotionContents(stack).matches(registryEntry);
    }

    private static PotionContentsComponent getPotionContents(ItemStack stack) {
        return (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
    }

    private static Item getPotionIngredient(RegistryEntry<Potion> registryEntry) {
        if (registryEntry.matches(net.minecraft.potion.Potions.HEALING)) {
            return Items.GLISTERING_MELON_SLICE;
        } else if (registryEntry.matches(net.minecraft.potion.Potions.STRENGTH)) {
            return Items.BLAZE_POWDER;
        } else if (registryEntry.matches(net.minecraft.potion.Potions.SWIFTNESS)) {
            return Items.SUGAR;
        } else if (registryEntry.matches(net.minecraft.potion.Potions.FIRE_RESISTANCE)) {
            return Items.MAGMA_CREAM;
        } else if (registryEntry.matches(net.minecraft.potion.Potions.NIGHT_VISION)) {
            return Items.GOLDEN_CARROT;
        } else if (registryEntry.matches(net.minecraft.potion.Potions.LEAPING)) {
            return Items.RABBIT_FOOT;
        } else if (registryEntry.matches(net.minecraft.potion.Potions.REGENERATION)) {
            return Items.GHAST_TEAR;
        } else if (registryEntry.matches(net.minecraft.potion.Potions.WATER_BREATHING)) {
            return Items.PUFFERFISH;
        } else if (registryEntry.matches(net.minecraft.potion.Potions.SLOW_FALLING)) {
            return Items.PHANTOM_MEMBRANE;
        } else {
            return registryEntry.matches(net.minecraft.potion.Potions.POISON) ? Items.SPIDER_EYE : null;
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private static String getItemSearchName(Item item) {
        if (item != Items.GLASS_BOTTLE) {
            if (item != Items.WATER_BUCKET) {
                return item != Items.BREWING_STAND ? new ItemStack(item).getName().getString().toLowerCase(Locale.ROOT) : "зельеварка";
            } else {
                return "ведро воды";
            }
        } else {
            return "бутылочка";
        }
    }

    private void addItemRequirement(Item item, int requiredItemCount) {
        if (requiredItemCount > 0 && this.countMatchingItems(stack -> stack.isOf(item)) < requiredItemCount) {
            this.queuedItemRequirements.add(new AutoQuest.ItemRequirement(stack -> stack.isOf(item), requiredItemCount, getCanonicalItemName(getItemSearchName(item))));
        }
    }

    private boolean configurePotionQuest() {
        this.questPotion = null;

        for (Object potionEntry : Registries.POTION.streamEntries().toList()) {
            String potionName = PotionContentsComponent.createStack(Items.POTION, (RegistryEntry)potionEntry).getName().getString().toLowerCase(Locale.ROOT);
            if (potionName.equals(this.questName)) {
                this.questPotion = (RegistryEntry<Potion>)potionEntry;
                break;
            }
        }

        if (this.questPotion == null) {
            return false;
        }

        this.potionBrewingIngredient = getPotionIngredient(this.questPotion);
        if (this.potionBrewingIngredient == null) {
            return false;
        }

        this.questItemPredicate = isPotion(this.questPotion);
        this.brewedQuestPotionCount = 0;
        int remainingPotionCount = Math.max(0, this.requiredQuestItemCount - this.countMatchingItems(this.questItemPredicate));
        int potionBatchCount = (remainingPotionCount + 2) / 3;
        int totalPotionBottleCount = potionBatchCount * 3;
        if (this.countMatchingItems(stack -> stack.isOf(Items.BREWING_STAND)) == 0 && this.findNearbyBlock(Blocks.BREWING_STAND) == null) {
            this.addItemRequirement(Items.BLAZE_ROD, 1);
            if (this.countMatchingItems(STONE_MATERIAL_PREDICATE) < 3) {
                this.queuedItemRequirements.add(new AutoQuest.ItemRequirement(stack -> stack.isOf(Items.COBBLESTONE), 3, "булыжник"));
            }

            if (this.countMatchingItems(stack -> stack.isOf(Items.CRAFTING_TABLE)) == 0
            && this.countMatchingItems(PLANK_ITEM_PREDICATE) < 4
            && this.countMatchingItems(LOG_ITEM_PREDICATE) == 0) {
                this.queuedItemRequirements.add(new AutoQuest.ItemRequirement(stack -> stack.isOf(Items.OAK_LOG), 1, "дубовое бревно"));
            }
        }

        int missingWaterBottleCount = Math.max(0, totalPotionBottleCount - this.countMatchingItems(WATER_POTION_PREDICATE) - this.countMatchingItems(stack -> stack.isOf(Items.GLASS_BOTTLE)));
        if (missingWaterBottleCount > 0) {
            this.addItemRequirement(Items.GLASS_BOTTLE, missingWaterBottleCount);
        }

        this.addItemRequirement(Items.NETHER_WART, potionBatchCount);
        int blazePowderBatchCount = (potionBatchCount + 9) / 10;
        if (this.potionBrewingIngredient == Items.BLAZE_POWDER) {
            this.addItemRequirement(Items.BLAZE_POWDER, potionBatchCount + blazePowderBatchCount);
        } else {
            this.addItemRequirement(this.potionBrewingIngredient, potionBatchCount);
            this.addItemRequirement(Items.BLAZE_POWDER, blazePowderBatchCount);
        }

        if (this.queuedItemRequirements.isEmpty()) {
            ClientChatLogger.II1Il11l111II11IIl("Всё для варки уже есть.");
        } else {
            StringBuilder stringBuilder = new StringBuilder();

            for (Object queuedRequirement : this.queuedItemRequirements) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }

                stringBuilder.append(((AutoQuest.ItemRequirement)queuedRequirement).description())
                .append(" x")
                .append(((AutoQuest.ItemRequirement)queuedRequirement).requiredCount());
            }

            ClientChatLogger.II1Il11l111II11IIl("Докупаю: " + stringBuilder);
        }

        Object requirement = this.queuedItemRequirements.poll();
        if (requirement != null) {
            this.marketItemPredicate = ((AutoQuest.ItemRequirement)requirement).itemPredicate();
            this.marketTargetItemCount = ((AutoQuest.ItemRequirement)requirement).requiredCount();
            this.marketSearchTerm = ((AutoQuest.ItemRequirement)requirement).description();
        }

        this.stageAfterMaterialsCollected = AutoQuest.QuestStage.WAIT_FOR_RTP;
        this.stageAfterObjective = AutoQuest.QuestStage.WAIT_FOR_RTP;
        return true;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void aimAtAndFillBottle(Vec3d waterPoint) {
        RotationPriorityManager.getInstance().clearRotationRequest(this);
        this.selectHotbarItem(Items.GLASS_BOTTLE);
        if (CLIENT.player.getMainHandStack().isOf(Items.GLASS_BOTTLE)) {
            Rotation lIll1Il1lll111l11l1I1llllI2 = RotationUtils.lookAt(CLIENT.player.getEyePos(), waterPoint);
            int aimAligned = Math.abs(MathHelper.wrapDegrees(CLIENT.player.getYaw() - lIll1Il1lll111l11l1I1llllI2.getYaw())) < 1.0F
            && Math.abs(CLIENT.player.getPitch() - lIll1Il1lll111l11l1I1llllI2.getPitch()) < 1.0F
            ? 1
            : 0;
            CLIENT.player.setYaw(lIll1Il1lll111l11l1I1llllI2.getYaw());
            CLIENT.player.setPitch(lIll1Il1lll111l11l1I1llllI2.getPitch());
            if (aimAligned != 0) {
                CLIENT.interactionManager.interactItem(CLIENT.player, Hand.MAIN_HAND);
                CLIENT.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private Vec3d findWaterInteractionPoint() {
        BlockPos playerBlockPos = CLIENT.player.getBlockPos();
        Vec3d playerEyePosition = CLIENT.player.getEyePos();
        Vec3d nearestInteractionPoint = null;
        double nearestWaterDistanceSquared = Double.MAX_VALUE;

        for (BlockPos waterBlockPos : BlockPos.iterate(playerBlockPos.add(-4, -3, -4), playerBlockPos.add(4, 2, 4))) {
            if (CLIENT.world.getFluidState((BlockPos)waterBlockPos).isEqualAndStill(Fluids.WATER)) {
                float waterSurfaceHeight = CLIENT.world.getFluidState((BlockPos)waterBlockPos).getHeight(CLIENT.world, (BlockPos)waterBlockPos);

                for (Vec3d waterSamplePoint : new Vec3d[]{
                    new Vec3d(waterBlockPos.getX() + 0.5, waterBlockPos.getY() + waterSurfaceHeight - 0.05, waterBlockPos.getZ() + 0.5), Vec3d.ofCenter((Vec3i)waterBlockPos)
                }) {
                    double waterSampleDistanceSquared;
                    if (!((waterSampleDistanceSquared = playerEyePosition.squaredDistanceTo(waterSamplePoint)) > 18.0) && !(waterSampleDistanceSquared >= nearestWaterDistanceSquared)) {
                        Vec3d raycastEndpoint = playerEyePosition.add(waterSamplePoint.subtract(playerEyePosition).normalize().multiply(4.4));
                        BlockHitResult blockHitResult = CLIENT.world
                        .raycast(new RaycastContext(playerEyePosition, raycastEndpoint, ShapeType.OUTLINE, FluidHandling.SOURCE_ONLY, CLIENT.player));
                        if (blockHitResult.getType() == Type.BLOCK
                        && CLIENT.world.getFluidState(blockHitResult.getBlockPos()).isEqualAndStill(Fluids.WATER)) {
                            nearestInteractionPoint = blockHitResult.getPos();
                            nearestWaterDistanceSquared = waterSampleDistanceSquared;
                        }
                    }
                }
            }
        }

        return nearestInteractionPoint;
    }

    private void brewQuestPotions() {
        if (!this.waterBottleFillPending
        && this.pendingBlockPlacement == null
        && this.actionCooldownTimer.hasElapsed(Math.max(200L, this.questActionDelaySetting.getValue().longValue()))) {
            this.actionCooldownTimer.reset();
            if (this.countQuestPotions() >= this.requiredQuestItemCount) {
                this.closeHandledScreen();
                RotationPriorityManager.getInstance().clearRotationRequest(this);
                ClientChatLogger.II1Il11l111II11IIl("Зелья сварены.");
                this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
            } else {
                ScreenHandler currentScreenHandler = CLIENT.player.currentScreenHandler;
                if (currentScreenHandler instanceof BrewingStandScreenHandler) {
                    BrewingStandScreenHandler brewingHandler = (BrewingStandScreenHandler)currentScreenHandler;
                    this.updateBrewingStand(brewingHandler);
                } else if (this.isHandledContainerOpen()) {
                    this.closeHandledScreen();
                } else {
                    BlockPos nearbyBrewingStandPosition = this.brewingStandPosition != null
                    && CLIENT.world.getBlockState(this.brewingStandPosition).isOf(Blocks.BREWING_STAND)
                    ? this.brewingStandPosition
                    : this.findNearbyBlock(Blocks.BREWING_STAND);
                    if (nearbyBrewingStandPosition != null || this.countMatchingItems(stack -> stack.isOf(Items.BREWING_STAND)) != 0) {
                        int remainingBottlesToBrew = (Math.max(0, this.requiredQuestItemCount - this.countQuestPotions()) + 2) / 3 * 3;
                        int waterBottleCount = this.countMatchingItems(WATER_POTION_PREDICATE);
                        int bottleBatchCapacity = Math.min(Math.min(3, remainingBottlesToBrew), Math.max(1, this.countEmptyInventorySlots() + waterBottleCount));
                        boolean shouldFillWaterBottles = this.countMatchingItems(stack -> stack.isOf(Items.GLASS_BOTTLE)) > 0 && waterBottleCount < bottleBatchCapacity;
                        if (!shouldFillWaterBottles || this.countEmptyInventorySlots() != 0 || waterBottleCount != 0 || !this.discardUnneededQuestItem() && !this.discardFirstMatchingItem(this.questItemPredicate)) {
                            if (shouldFillWaterBottles && this.countEmptyInventorySlots() > 0) {
                                Vec3d waterInteractionPoint = this.findWaterInteractionPoint();
                                if (waterInteractionPoint == null
                                && this.nearbyWaterSource != null
                                && CLIENT.world.getFluidState(this.nearbyWaterSource).isEqualAndStill(Fluids.WATER)) {
                                    Vec3d nearbyWaterInteractionPoint = new Vec3d(
                                    this.nearbyWaterSource.getX() + 0.5, this.nearbyWaterSource.getY() + 0.85, this.nearbyWaterSource.getZ() + 0.5
                                    );
                                    if (CLIENT.player.getEyePos().squaredDistanceTo(nearbyWaterInteractionPoint) <= 18.0) {
                                        waterInteractionPoint = nearbyWaterInteractionPoint;
                                    }
                                }

                                if (waterInteractionPoint != null) {
                                    this.cancelBaritonePath();
                                    if (nearbyBrewingStandPosition == null && this.countMatchingItems(stack -> stack.isOf(Items.BREWING_STAND)) > 0) {
                                        RotationPriorityManager.getInstance().clearRotationRequest(this);
                                        ClientChatLogger.II1Il11l111II11IIl("Ставлю зельеварку у воды.");
                                        this.placeItemBlock(Items.BREWING_STAND);
                                    } else {
                                        this.aimAtAndFillBottle(waterInteractionPoint);
                                    }
                                } else {
                                    RotationPriorityManager.getInstance().clearRotationRequest(this);
                                    if (this.nearbyWaterSource == null || !CLIENT.world.getFluidState(this.nearbyWaterSource).isEqualAndStill(Fluids.WATER)) {
                                        this.nearbyWaterSource = this.findNearbyWaterSource();
                                        if (this.nearbyWaterSource == null) {
                                            ClientChatLogger.II1Il11l111II11IIl("Воды рядом нет, новый RTP.");
                                            this.brewingStandPosition = null;
                                            this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                                            return;
                                        }

                                        ClientChatLogger.II1Il11l111II11IIl(
                                        String.format(
                                        Locale.ROOT,
                                        "Иду к воде: %d %d %d.",
                                        this.nearbyWaterSource.getX(),
                                        this.nearbyWaterSource.getY(),
                                        this.nearbyWaterSource.getZ()
                                        )
                                        );
                                        this.waterPathTimeoutTimer.reset();
                                    }

                                    if (!this.getBaritone().getCustomGoalProcess().isActive()) {
                                        if (this.waterPathTimeoutTimer.hasElapsed(60000L)) {
                                            ClientChatLogger.II1Il11l111II11IIl("Не дошёл до воды, новый RTP.");
                                            this.nearbyWaterSource = null;
                                            this.brewingStandPosition = null;
                                            this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                                            return;
                                        }

                                        BaritoneAPI.getSettings().allowBreak.value = false;
                                        this.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalNear(this.nearbyWaterSource, 2));
                                    }

                                    this.stageWatchdogTimer.reset();
                                }
                            } else {
                                RotationPriorityManager.getInstance().clearRotationRequest(this);
                                if (nearbyBrewingStandPosition == null) {
                                    this.cancelBaritonePath();
                                    this.placeItemBlock(Items.BREWING_STAND);
                                } else {
                                    this.brewingStandPosition = nearbyBrewingStandPosition;
                                    if (CLIENT.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(nearbyBrewingStandPosition)) > 16.0) {
                                        if (!this.getBaritone().getCustomGoalProcess().isActive()) {
                                            BaritoneAPI.getSettings().allowBreak.value = false;
                                            this.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalNear(nearbyBrewingStandPosition, 2));
                                        }

                                        this.stageWatchdogTimer.reset();
                                    } else {
                                        this.cancelBaritonePath();
                                        this.scheduleBlockPlacement(nearbyBrewingStandPosition, this.getBlockInteractionFace(nearbyBrewingStandPosition));
                                    }
                                }
                            }
                        }
                    } else if (this.countMatchingItems(stack -> stack.isOf(Items.BLAZE_ROD)) > 0 && this.countMatchingItems(STONE_MATERIAL_PREDICATE) >= 3) {
                        ClientChatLogger.II1Il11l111II11IIl("Крафчу зельеварку.");
                        this.setStage(AutoQuest.QuestStage.CRAFT_QUEST_ITEM);
                    } else {
                        ClientChatLogger.l1I1IlllIlI("Не хватает огненного стержня или булыжника на зельеварку, докупаю.");
                        this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
                    }
                }
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private BlockPos findNearbyWaterSource() {
        BlockPos playerBlockPos = CLIENT.player.getBlockPos();
        BlockPos nearestWaterSource = null;
        double nearestWaterDistanceSquared = Double.MAX_VALUE;
        Mutable mutable = new Mutable();

        for (BlockPos waterBlockPos : BlockPos.iterate(playerBlockPos.add(-40, -10, -40), playerBlockPos.add(40, 6, 40))) {
            if (CLIENT.world.getFluidState((BlockPos)waterBlockPos).isEqualAndStill(Fluids.WATER)) {
                mutable.set(waterBlockPos.getX(), waterBlockPos.getY() + 1, waterBlockPos.getZ());
                double waterDistanceSquared;
                if (CLIENT.world.getBlockState(mutable).isAir() && (waterDistanceSquared = waterBlockPos.getSquaredDistance(playerBlockPos)) < nearestWaterDistanceSquared) {
                    nearestWaterDistanceSquared = waterDistanceSquared;
                    nearestWaterSource = waterBlockPos.toImmutable();
                }
            }
        }

        return nearestWaterSource;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void craftBrewingStand(CraftingScreenHandler craftingHandler) {
        if (this.countMatchingItems(stack -> stack.isOf(Items.BREWING_STAND)) <= 0) {
            LinkedHashMap<Integer, Predicate<ItemStack>> linkedHashMap2 = new LinkedHashMap<>();
            linkedHashMap2.put(2, stack -> stack.isOf(Items.BLAZE_ROD));
            linkedHashMap2.put(4, STONE_MATERIAL_PREDICATE);
            linkedHashMap2.put(5, STONE_MATERIAL_PREDICATE);
            linkedHashMap2.put(6, STONE_MATERIAL_PREDICATE);
            if (!this.maintainCraftingGrid(craftingHandler, 1, 9, 10, linkedHashMap2)) {
                this.closeHandledScreen();
                ClientChatLogger.l1I1IlllIlI("Не хватает материалов на зельеварку, докупаю.");
                this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
            }
        } else {
            LinkedHashMap<Integer, Predicate<ItemStack>> linkedHashMap = new LinkedHashMap<>();
            if (!this.maintainCraftingGrid(craftingHandler, 1, 9, 10, linkedHashMap)) {
                this.closeHandledScreen();
                ClientChatLogger.II1Il11l111II11IIl("Зельеварка скрафчена.");
                this.setStage(AutoQuest.QuestStage.BREW_QUEST_POTIONS);
            }
        }
    }

    private int countQuestPotions() {
        return Math.max(this.brewedQuestPotionCount, this.countMatchingItems(this.questItemPredicate));
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private int countEmptyInventorySlots() {
        int emptySlotCount = 0;

        for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
            if (CLIENT.player.getInventory().getStack(inventorySlot).isEmpty()) {
                emptySlotCount++;
            }
        }

        return emptySlotCount;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean discardFirstMatchingItem(Predicate<ItemStack> itemPredicate) {
        for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
            if (itemPredicate.test(CLIENT.player.getInventory().getStack(inventorySlot))) {
                InventoryUtils.clickSlot(InventoryUtils.toScreenHandlerSlot(inventorySlot), 1, SlotActionType.THROW);
                return true;
            }
        }

        return false;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean discardUnneededQuestItem() {
        for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
            ItemStack stack = CLIENT.player.getInventory().getStack(inventorySlot);
            if (!stack.isEmpty() && this.shouldDiscardItem(stack)) {
                InventoryUtils.clickSlot(InventoryUtils.toScreenHandlerSlot(inventorySlot), 1, SlotActionType.THROW);
                return true;
            }
        }

        return false;
    }

    private void updateBrewingStand(BrewingStandScreenHandler brewingStandScreenHandler) {
        for (int potionSlot = 0; potionSlot < 3; potionSlot++) {
            if (this.questItemPredicate.test(brewingStandScreenHandler.getSlot(potionSlot).getStack())) {
                InventoryUtils.clickSlot(potionSlot, 1, SlotActionType.THROW);
                this.brewedQuestPotionCount++;
                return;
            }
        }

        if (brewingStandScreenHandler.getBrewTime() <= 0 && brewingStandScreenHandler.getSlot(3).getStack().isEmpty()) {
            if (brewingStandScreenHandler.getFuel() <= 0 && brewingStandScreenHandler.getSlot(4).getStack().isEmpty()) {
                if (!this.insertBrewingIngredient(brewingStandScreenHandler, Items.BLAZE_POWDER, 4)) {
                    ClientChatLogger.l1I1IlllIlI("Нет огненного порошка, докупаю.");
                    this.closeHandledScreen();
                    this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
                }
            } else {
                RegistryEntry currentPotion = null;
                boolean hasEmptyPotionSlot = false;

                for (int potionSlot = 0; potionSlot < 3; potionSlot++) {
                    ItemStack potionStack = brewingStandScreenHandler.getSlot(potionSlot).getStack();
                    if (potionStack.isEmpty()) {
                        hasEmptyPotionSlot = true;
                    } else {
                        PotionContentsComponent potionContentsComponent = getPotionContents(potionStack);
                        if (potionContentsComponent == null
                        || potionContentsComponent.potion().isEmpty()
                        || currentPotion != null && !currentPotion.equals(potionContentsComponent.potion().get())) {
                            InventoryUtils.clickSlot(potionSlot, 0, SlotActionType.QUICK_MOVE);
                            return;
                        }

                        currentPotion = (RegistryEntry)potionContentsComponent.potion().get();
                    }
                }

                boolean hasWaterBottles = currentPotion == null || currentPotion.matches(net.minecraft.potion.Potions.WATER);
                if (hasEmptyPotionSlot && hasWaterBottles && this.countMatchingItems(WATER_POTION_PREDICATE) > 0) {
                    for (int inventorySlot = 5; inventorySlot < brewingStandScreenHandler.slots.size(); inventorySlot++) {
                        if (WATER_POTION_PREDICATE.test(brewingStandScreenHandler.getSlot(inventorySlot).getStack())) {
                            InventoryUtils.clickSlot(inventorySlot, 0, SlotActionType.QUICK_MOVE);
                            return;
                        }
                    }
                }

                if (currentPotion == null) {
                    this.closeHandledScreen();
                } else {
                    Item item;
                    if (currentPotion.matches(net.minecraft.potion.Potions.WATER)) {
                        item = Items.NETHER_WART;
                    } else {
                        if (!currentPotion.matches(net.minecraft.potion.Potions.AWKWARD)) {
                            for (int potionSlot = 0; potionSlot < 3; potionSlot++) {
                                if (!brewingStandScreenHandler.getSlot(potionSlot).getStack().isEmpty()) {
                                    InventoryUtils.clickSlot(potionSlot, 0, SlotActionType.QUICK_MOVE);
                                    return;
                                }
                            }

                            return;
                        }

                        item = this.potionBrewingIngredient;
                    }

                    if (!this.insertBrewingIngredient(brewingStandScreenHandler, item, 3)) {
                        ClientChatLogger.l1I1IlllIlI("Не хватает " + new ItemStack(item).getName().getString() + ", докупаю.");
                        this.closeHandledScreen();
                        this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
                    }
                }
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean insertBrewingIngredient(BrewingStandScreenHandler brewingStandScreenHandler, Item item, int brewingStandSlot) {
        for (int inventorySlot = 5; inventorySlot < brewingStandScreenHandler.slots.size(); inventorySlot++) {
            if (brewingStandScreenHandler.getSlot(inventorySlot).getStack().isOf(item)) {
                InventoryUtils.clickSlot(inventorySlot, 0, SlotActionType.PICKUP);
                InventoryUtils.clickSlot(brewingStandSlot, 1, SlotActionType.PICKUP);
                InventoryUtils.clickSlot(inventorySlot, 0, SlotActionType.PICKUP);
                return true;
            }
        }

        return false;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void resetPvpTargetState() {
        this.currentTarget = null;
        this.hasAttackedCurrentTarget = false;
        this.botReturningToHub = false;
        this.botCallPending = false;
        this.botCallRetryTimer.reset();
        RotationPriorityManager.getInstance().clearRotationRequest(this);
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private PlayerEntity findNearbyPlayer(String playerName) {
        for (PlayerEntity player : CLIENT.world.getPlayers()) {
            if (player != CLIENT.player
            && player.isAlive()
            && player.getName().getString().equalsIgnoreCase(playerName)
            && player.distanceTo(CLIENT.player) <= 32.0F) {
                return (PlayerEntity)player;
            }
        }

        return null;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private int getConfiguredGriefNumber() {
        int configuredGriefNumber;
        if ((configuredGriefNumber = this.griefNumberSetting.getValue().intValue()) <= 0) {
            ScoreboardObjective scoreboardObjective = CLIENT.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
            if (scoreboardObjective != null) {
                Matcher matcher = GRIEF_NUMBER_PATTERN.matcher(scoreboardObjective.getDisplayName().getString().toLowerCase(Locale.ROOT));
                return !matcher.find() ? 0 : Integer.parseInt(matcher.group(1));
            } else {
                return 0;
            }
        } else {
            return configuredGriefNumber;
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void selectHotbarItem(Item item) {
        int inventorySlot;
        if ((inventorySlot = InventoryUtils.findItemSlot(stack -> stack.isOf(item))) != -1) {
            if (inventorySlot >= 9) {
                InventoryUtils.swapInventorySlotWithHotbar(inventorySlot, 8, Priority.PriorityCategory.DEFAULT);
                inventorySlot = 8;
            }

            InventoryUtils.setSelectedHotbarSlot(inventorySlot);
        }
    }

    private void processEatQuest() {
        if (this.isHandledContainerOpen()) {
            this.closeHandledScreen();
        } else if (this.countMatchingItems(this.questItemPredicate) == 0) {
            CLIENT.options.useKey.setPressed(false);
            ClientChatLogger.II1Il11l111II11IIl("Еда съедена, проверяю квест.");
            this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
        } else if (!CLIENT.player.getHungerManager().isNotFull() && !CLIENT.player.isUsingItem()) {
            CLIENT.options.useKey.setPressed(false);
            if (!this.shouldRunToExhaustHunger) {
                ClientChatLogger.II1Il11l111II11IIl("Сытость полная, телепортируюсь бегать.");
                this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
            } else {
                if (!this.hungerRunStarted) {
                    this.hungerRunStarted = true;
                    ClientChatLogger.II1Il11l111II11IIl("Бегаю, трачу сытость.");
                }

                this.moveToSpendHunger();
                this.stageWatchdogTimer.reset();
            }
        } else {
            if (this.hungerRunStarted) {
                this.hungerRunStarted = false;
                if (this.hungerPillarStartPosition != null) {
                    this.releaseQuestControls();
                }

                this.remainingHungerRunTicks = 0;
                CLIENT.options.forwardKey.setPressed(false);
                this.cancelBaritonePath();
                CLIENT.options.sprintKey.setPressed(false);
            }

            if (!CLIENT.player.getMainHandStack().isOf(this.questItem)) {
                CLIENT.options.useKey.setPressed(false);
                this.selectHotbarItem(this.questItem);
            } else {
                CLIENT.options.useKey.setPressed(true);
                this.stageWatchdogTimer.reset();
            }
        }
    }

    private void moveToSpendHunger() {
        if (this.remainingHungerRunTicks > 0) {
            this.getBaritone().getInputOverrideHandler().clearAllKeys();
            CLIENT.options.sneakKey.setPressed(false);
            CLIENT.player.setSneaking(false);
            CLIENT.options.forwardKey.setPressed(--this.remainingHungerRunTicks > 0);
        } else if (this.hungerPillarStartPosition == null && CLIENT.player.getHealth() < CLIENT.player.getMaxHealth()) {
            CLIENT.options.sprintKey.setPressed(false);
            if (this.getBaritone().getPathingBehavior().isPathing()) {
                this.cancelBaritonePath();
            }
        } else {
            if (this.hungerPillarStartPosition != null || this.countMatchingItems(SUPPORT_BLOCK_ITEM_PREDICATE) >= 8) {
                if (this.hungerPillarStartPosition == null) {
                    if (CLIENT.player.isOnGround() && !CLIENT.player.isTouchingWater()) {
                        this.cancelBaritonePath();
                        CLIENT.options.sprintKey.setPressed(false);
                        CLIENT.options.sneakKey.setPressed(false);
                        this.hungerPillarStartPosition = CLIENT.player.getBlockPos();
                        this.hungerRunTimer.reset();
                        return;
                    }

                    return;
                }

                if (CLIENT.player.getY() >= this.hungerPillarStartPosition.getY() + 7.5 && CLIENT.player.isOnGround()) {
                    this.releaseQuestControls();
                    this.remainingHungerRunTicks = 15;
                    return;
                }

                if (!this.hungerRunTimer.hasElapsed(30000L) && this.countMatchingItems(SUPPORT_BLOCK_ITEM_PREDICATE) > 0) {
                    this.jumpToSpendHunger();
                    return;
                }

                this.releaseQuestControls();
            }

            CLIENT.options.sprintKey.setPressed(true);
            if (CLIENT.player.isOnGround() && this.postTeleportJumpTicks == 0) {
                this.postTeleportJumpTicks = 2;
            }

            if (!this.getBaritone().getPathingBehavior().isPathing() || this.hungerRunTimer.hasElapsed(20000L)) {
                double randomAngle = Math.random() * Math.PI * 2.0;
                double randomDistance = 60.0 + Math.random() * 40.0;
                int goalX = (int)(CLIENT.player.getX() + Math.cos(randomAngle) * randomDistance);
                int goalZ = (int)(CLIENT.player.getZ() + Math.sin(randomAngle) * randomDistance);
                BaritoneAPI.getSettings().allowBreak.value = false;
                this.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(goalX, goalZ));
                this.hungerRunTimer.reset();
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void jumpToSpendHunger() {
        CLIENT.options.forwardKey.setPressed(false);
        CLIENT.options.sprintKey.setPressed(false);
        this.postTeleportJumpTicks = 0;
        ItemStack mainHandStack = CLIENT.player.getMainHandStack();
        if (SUPPORT_BLOCK_ITEM_PREDICATE.test(mainHandStack)) {
            RotationPriorityManager.getInstance()
            .requestRotation(
            new Rotation(CLIENT.player.getYaw(), 90.0F), FocusMode.FOCUSED, 180.0F, 1, false, this
            );
            CLIENT.options.jumpKey.setPressed(true);
            this.shouldPlaceLandingBlock = true;
        } else {
            CLIENT.options.jumpKey.setPressed(false);
            int inventorySlot;
            if ((inventorySlot = InventoryUtils.findItemSlot(SUPPORT_BLOCK_ITEM_PREDICATE)) != -1) {
                this.selectHotbarItem(CLIENT.player.getInventory().getStack(inventorySlot).getItem());
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void placeBlockDuringFall() {
        this.shouldPlaceLandingBlock = false;
        if (!CLIENT.player.isOnGround() && SUPPORT_BLOCK_ITEM_PREDICATE.test(CLIENT.player.getMainHandStack())) {
            BlockPos playerFootPosition = BlockPos.ofFloored(
            CLIENT.player.getX(), CLIENT.player.getY() - 0.01, CLIENT.player.getZ()
            );
            BlockPos supportBlockPosition = playerFootPosition.down();
            if (CLIENT.world.getBlockState(playerFootPosition).isReplaceable()
            && !CLIENT.world.getBlockState(supportBlockPosition).isReplaceable()
            && !(CLIENT.player.getY() - playerFootPosition.getY() < 1.0)) {
                Vec3d blockPlacementHitPosition = Vec3d.ofCenter(supportBlockPosition).add(0.0, 0.5, 0.0);
                CLIENT.interactionManager
                .interactBlock(CLIENT.player, Hand.MAIN_HAND, new BlockHitResult(blockPlacementHitPosition, Direction.UP, supportBlockPosition, false));
                CLIENT.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    private void releaseQuestControls() {
        CLIENT.options.jumpKey.setPressed(false);
        this.shouldPlaceLandingBlock = false;
        this.hungerPillarStartPosition = null;
        RotationPriorityManager.getInstance().clearRotationRequest(this);
        this.cancelBaritonePath();
    }

    private AutoQuest.QuestStage getNextQuestStage() {
        return this.stageAfterObjective;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private Item findLogForPlankItem(String plankItemPath) {
        String woodType = plankItemPath.substring(0, plankItemPath.length() - 7);
        String logItemPath = !woodType.equals("crimson") && !woodType.equals("warped") ? woodType + "_log" : woodType + "_stem";
        Object registeredItem = (Item)Registries.ITEM.get(Identifier.ofVanilla(logItemPath));
        return (Item)(registeredItem != Items.AIR ? registeredItem : null);
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private AutoQuest.BlockRequirement getNextBlockRequirement() {
        for (Object blockRequirement : this.blockRequirements) {
            if (this.breakObjective) {
                return (AutoQuest.BlockRequirement)(this.minedBlockCount >= this.requiredQuestItemCount ? null : blockRequirement);
            }

            if (this.countMatchingItems(((AutoQuest.BlockRequirement)blockRequirement).itemPredicate) < ((AutoQuest.BlockRequirement)blockRequirement).requiredCount) {
                return (AutoQuest.BlockRequirement)blockRequirement;
            }
        }

        return null;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void claimQuestReward() {
        if (this.actionCooldownTimer.hasElapsed(1000L)) {
            this.closeHandledScreen();
            this.questMenuAttempts = 0;
            this.setStage(AutoQuest.QuestStage.OPEN_QUEST_MENU);
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void requestRandomTeleport() {
        this.closeHandledScreen();
        this.cancelBaritonePath();
        if (System.currentTimeMillis() >= this.rtpWaitUntilMillis) {
            this.randomTeleportStartPosition = CLIENT.player.getEntityPos();
            this.rtpPearlUsed = false;
            this.rtpRetryCount = 0;
            CLIENT.getNetworkHandler().sendChatCommand("rtp");
            this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP_RESULT);
        }
    }

    private void waitForRandomTeleport() {
        boolean teleportedToNewPosition = CLIENT.player.getEntityPos().squaredDistanceTo(this.randomTeleportStartPosition) > 10000.0;
        if (teleportedToNewPosition && this.isHandledContainerOpen() && this.actionCooldownTimer.hasElapsed(1000L)) {
            this.closeHandledScreen();
        } else {
            Screen screen = CLIENT.currentScreen;
            if (screen instanceof HandledScreen) {
                HandledScreen<?> screen2 = (HandledScreen<?>)screen;
                if (!(screen2.getScreenHandler() instanceof PlayerScreenHandler)) {
                    if (!this.rtpPearlUsed && this.actionCooldownTimer.hasElapsed(300L)) {
                        ScreenHandler teleportResultHandler = screen2.getScreenHandler();
                        int containerSlotCount = teleportResultHandler.slots.size() - 36;

                        for (int containerSlotIndex = 0; containerSlotIndex < containerSlotCount; containerSlotIndex++) {
                            if (((Slot)teleportResultHandler.slots.get(containerSlotIndex)).getStack().isOf(Items.ENDER_PEARL)) {
                                InventoryUtils.clickSlot(containerSlotIndex, 0, SlotActionType.PICKUP);
                                this.rtpPearlUsed = true;
                                this.actionCooldownTimer.reset();
                                break;
                            }
                        }
                    }

                    return;
                }
            }

            if (teleportedToNewPosition && this.actionCooldownTimer.hasElapsed(2500L)
            || this.actionCooldownTimer.hasElapsed(this.randomTeleportWaitSetting.getValue().longValue())) {
                if (!teleportedToNewPosition) {
                    this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                    return;
                }

                if (this.rtpRetryCount < 2) {
                    if (this.rtpRetryCount == 0 || this.rtpResultRetryTimer.hasElapsed(600L)) {
                        this.postTeleportJumpTicks = 4;
                        this.rtpRetryCount++;
                        this.rtpResultRetryTimer.reset();
                    }

                    return;
                }

                if (!this.rtpResultRetryTimer.hasElapsed(700L)) {
                    return;
                }

                if (this.requiresPvpBotQuest && this.getMissingRequiredToolCount() == 0) {
                    this.botCallPending = false;
                    this.setStage(AutoQuest.QuestStage.COMPLETE_PVP_KILL);
                    return;
                }

                if (this.requiresPotionBrewing) {
                    this.setStage(AutoQuest.QuestStage.BREW_QUEST_POTIONS);
                    return;
                }

                if (this.requiresEating) {
                    this.shouldRunToExhaustHunger = true;
                    this.setStage(AutoQuest.QuestStage.EAT_QUEST_FOOD);
                    return;
                }

                if (this.requiresMobKill && !this.requiresPvpBotQuest) {
                    this.currentTarget = null;
                    this.currentSpawnerPosition = null;
                    this.mobSearchAttemptCount = 0;
                    this.setStage(AutoQuest.QuestStage.HUNT);
                    return;
                }

                AutoQuest.BlockRequirement blockRequirement = this.getNextBlockRequirement();
                if (blockRequirement != null && !blockRequirement.blockOnly && !this.containsAnyBlockInLoadedChunks(blockRequirement.blocks)) {
                    ClientChatLogger.II1Il11l111II11IIl("Здесь нет: " + blockRequirement.description + ", ищу другой биом.");
                    this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                    return;
                }

                this.currentBlockRequirement = null;
                this.itemEntity = null;
                this.ignoredDroppedItemIds.clear();
                this.setStage(AutoQuest.QuestStage.MINE_REQUIRED_BLOCKS);
            }
        }
    }

    private void mineRequiredBlocks() {
        if (this.isPlayerNearby()) {
            this.cancelBaritonePath();
            this.currentBlockRequirement = null;
            CLIENT.getNetworkHandler().sendChatCommand("spawn");
            ClientChatLogger.II1Il11l111II11IIl("Рядом игрок, ухожу на спавн и делаю RTP.");
            this.rtpWaitUntilMillis = Math.max(this.rtpWaitUntilMillis, System.currentTimeMillis() + 2000L);
            this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
        } else if (this.isHandledContainerOpen()) {
            this.closeHandledScreen();
        } else {
            this.discardExcessInventoryItems();
            AutoQuest.BlockRequirement blockRequirement = this.getNextBlockRequirement();
            if (blockRequirement == null) {
                this.cancelBaritonePath();
                this.currentBlockRequirement = null;
                BaritoneAPI.getSettings().allowBreak.value = false;
                this.setStage(this.getNextQuestStage());
            } else if (blockRequirement != this.currentBlockRequirement) {
                this.currentBlockRequirement = blockRequirement;
                BaritoneAPI.getSettings().allowBreak.value = blockRequirement.blockOnly;
                int minimumMiningY = blockRequirement.blockOnly ? 0 : CLIENT.player.getBlockY() - 10;
                BaritoneAPI.getSettings().minYLevelWhileMining.value = minimumMiningY;
                BaritoneAPI.getSettings().allowBreakAnyway.value = new ArrayList<>(List.of(blockRequirement.blocks));
                this.getBaritone().getMineProcess().mine(0, blockRequirement.blocks);
                this.blockMiningProgressTimer.reset();
                ClientChatLogger.II1Il11l111II11IIl(
                String.format(
                Locale.ROOT,
                "Добываю %s: %d/%d.",
                this.currentBlockRequirement.description,
                this.countMatchingItems(this.currentBlockRequirement.itemPredicate),
                this.currentBlockRequirement.requiredCount
                )
                );
            } else if (this.recoverMiningTarget()) {
                this.blockMiningProgressTimer.reset();
            } else if (this.getBaritone().getMineProcess().isActive()) {
                this.blockMiningProgressTimer.reset();
            } else {
                if (this.blockMiningProgressTimer.hasElapsed(3000L)) {
                    ClientChatLogger.II1Il11l111II11IIl("Рядом нет: " + this.currentBlockRequirement.description + ", новый RTP.");
                    this.currentBlockRequirement = null;
                    this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                }
            }
        }
    }

    private void runHuntStep() {
        if (this.retreatingForHealth) {
            this.recoverHealthAtSpawn();
        } else if (CLIENT.player.getHealth() <= this.retreatHealthThresholdSetting.getValue()) {
            this.cancelBaritonePath();
            RotationPriorityManager.getInstance().clearRotationRequest(this);
            this.attackQueued = false;
            this.currentTarget = null;
            this.currentSpawnerPosition = null;
            this.retreatingForHealth = true;
            this.healingTimer.reset();
            this.foodRecoveryTimer.setElapsedMillis(60000L);
            CLIENT.getNetworkHandler().sendChatCommand("spawn");
            ClientChatLogger.II1Il11l111II11IIl("Мало HP, ухожу на спавн лечиться.");
        } else if (this.isPlayerNearby()) {
            this.cancelBaritonePath();
            CLIENT.getNetworkHandler().sendChatCommand("spawn");
            ClientChatLogger.II1Il11l111II11IIl("Рядом игрок, ухожу на спавн и делаю RTP.");
            this.rtpWaitUntilMillis = Math.max(this.rtpWaitUntilMillis, System.currentTimeMillis() + 2000L);
            this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
        } else if (this.isHandledContainerOpen()) {
            this.closeHandledScreen();
        } else {
            this.discardExcessInventoryItems();
            if (this.currentTarget != null && (this.currentTarget.isDead() || this.currentTarget.isRemoved())) {
                if (this.hasAttackedCurrentTarget && this.currentTarget.isDead()) {
                    this.completedMobKills++;
                    this.stageWatchdogTimer.reset();
                    ClientChatLogger.II1Il11l111II11IIl(String.format(Locale.ROOT, "Убито %d/%d.", this.completedMobKills, this.requiredQuestItemCount));
                }

                this.currentTarget = null;
            }

            if (this.completedMobKills >= this.requiredQuestItemCount) {
                this.cancelBaritonePath();
                this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
            } else {
                if (this.currentSpawnerPosition == null && this.mobSpawnerSearchTimer.hasElapsed(2000L)) {
                    this.mobSpawnerSearchTimer.reset();
                    this.currentSpawnerPosition = this.findNearestMobSpawner();
                    if (this.currentSpawnerPosition != null) {
                        this.cancelBaritonePath();
                        this.currentTarget = null;
                        ClientChatLogger.II1Il11l111II11IIl(
                        String.format(
                        Locale.ROOT,
                        "Нашел спавнер на %d %d %d, иду к нему.",
                        this.currentSpawnerPosition.getX(),
                        this.currentSpawnerPosition.getY(),
                        this.currentSpawnerPosition.getZ()
                        )
                        );
                    }
                }

                if (this.currentTarget == null) {
                    this.hasAttackedCurrentTarget = false;
                    this.currentTarget = this.findNearestTargetMob();
                    if (this.currentTarget == null) {
                        if (this.currentSpawnerPosition != null) {
                            this.navigateToMobSpawner();
                        } else {
                            this.searchForMobs();
                        }

                        return;
                    }

                    this.mobSearchAttemptCount = 0;
                    this.cancelBaritonePath();
                    this.combatPathTimer.setElapsedMillis(10000L);
                }

                this.blockMiningProgressTimer.reset();
                if (!(CLIENT.player.distanceTo(this.currentTarget) > 3.0F)) {
                    this.cancelBaritonePath();
                    RotationPriorityManager.getInstance()
                    .requestRotation(
                    RotationUtils.lookAt(CLIENT.player.getEyePos(), this.currentTarget.getBoundingBox().getCenter()),
                    FocusMode.FREE,
                    60.0F,
                    3,
                    false,
                    this
                    );
                    this.selectCombatWeapon();
                    if (CLIENT.player.getAttackCooldownProgress(0.5F) >= 0.95F) {
                        this.attackQueued = true;
                    }
                } else {
                    if (this.combatPathTimer.hasElapsed(1000L) || !this.getBaritone().getCustomGoalProcess().isActive()) {
                        BaritoneAPI.getSettings().allowBreak.value = false;
                        this.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalNear(this.currentTarget.getBlockPos(), 1));
                        this.combatPathTimer.reset();
                    }
                }
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void recoverHealthAtSpawn() {
        this.stageWatchdogTimer.reset();
        this.blockMiningProgressTimer.reset();
        if (!this.isHandledContainerOpen()) {
            if (!(CLIENT.player.getHealth() >= CLIENT.player.getMaxHealth() - 1.0F)
            && !this.healingTimer.hasElapsed(180000L)) {
                if (CLIENT.player.getHungerManager().getFoodLevel() < 18 && this.foodRecoveryTimer.hasElapsed(30000L)) {
                    this.foodRecoveryTimer.reset();
                    CLIENT.getNetworkHandler().sendChatCommand("feed");
                }
            } else {
                this.retreatingForHealth = false;
                ClientChatLogger.II1Il11l111II11IIl("Вылечился, возвращаюсь к фарму.");
                this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
            }
        } else {
            this.closeHandledScreen();
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void navigateToMobSpawner() {
        this.blockMiningProgressTimer.reset();
        if (CLIENT.world.getBlockEntity(this.currentSpawnerPosition) instanceof MobSpawnerBlockEntity) {
            if (this.canSeeSpawner()) {
                this.stageWatchdogTimer.reset();
                this.cancelBaritonePath();
            } else {
                if (!this.getBaritone().getCustomGoalProcess().isActive() || this.combatPathTimer.hasElapsed(5000L)) {
                    BaritoneAPI.getSettings().allowBreak.value = true;
                    BaritoneAPI.getSettings().blocksToDisallowBreaking.value = new ArrayList<>(List.of(Blocks.SPAWNER));
                    this.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalGetToBlock(this.currentSpawnerPosition));
                    this.combatPathTimer.reset();
                }
            }
        } else {
            this.currentSpawnerPosition = null;
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean canSeeSpawner() {
        Vec3d playerEyePosition = CLIENT.player.getEyePos();
        Vec3d spawnerCenter = Vec3d.ofCenter(this.currentSpawnerPosition);
        if (playerEyePosition.squaredDistanceTo(spawnerCenter) > 6.25) {
            return false;
        }

        BlockHitResult blockHitResult = CLIENT.world
        .raycast(new RaycastContext(playerEyePosition, spawnerCenter, ShapeType.COLLIDER, FluidHandling.NONE, CLIENT.player));
        return blockHitResult.getType() == Type.MISS || blockHitResult.getBlockPos().equals(this.currentSpawnerPosition);
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private BlockPos findNearestMobSpawner() {
        BlockPos playerBlockPos = CLIENT.player.getBlockPos();
        int playerSectionX = ChunkSectionPos.getSectionCoord(playerBlockPos.getX());
        int playerSectionZ = ChunkSectionPos.getSectionCoord(playerBlockPos.getZ());
        int viewDistance = (Integer)CLIENT.options.getViewDistance().getValue();
        BlockPos nearestSpawnerPosition = null;
        double nearestSpawnerDistanceSquared = Double.MAX_VALUE;

        for (int sectionX = playerSectionX - viewDistance; sectionX <= playerSectionX + viewDistance; sectionX++) {
            for (int sectionZ = playerSectionZ - viewDistance; sectionZ <= playerSectionZ + viewDistance; sectionZ++) {
                if (CLIENT.world.isChunkLoaded(sectionX, sectionZ)) {
                    for (BlockEntity blockEntity : CLIENT.world.getChunk(sectionX, sectionZ).getBlockEntities().values()) {
                        if (blockEntity instanceof MobSpawnerBlockEntity) {
                            MobSpawnerBlockEntity spawnerBlockEntity = (MobSpawnerBlockEntity)blockEntity;
                            if (!this.visitedSpawnerPositions.contains(blockEntity.getPos())) {
                                Entity entity = spawnerBlockEntity.getLogic().getRenderedEntity(CLIENT.world, blockEntity.getPos());
                                double spawnerDistanceSquared;
                                if (entity != null && entity.getType() == this.targetMobType && (spawnerDistanceSquared = blockEntity.getPos().getSquaredDistance(playerBlockPos)) < nearestSpawnerDistanceSquared
                                )
                                {
                                    nearestSpawnerDistanceSquared = spawnerDistanceSquared;
                                    nearestSpawnerPosition = blockEntity.getPos().toImmutable();
                                }
                            }
                        }
                    }
                }
            }
        }

        return nearestSpawnerPosition;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void searchForMobs() {
        if (!this.getBaritone().getCustomGoalProcess().isActive() || this.baritoneRepathTimer.hasElapsed(25000L)) {
            if (++this.mobSearchAttemptCount <= 6) {
                double randomAngle = Math.random() * Math.PI * 2.0;
                double randomDistance = 40.0 + Math.random() * 20.0;
                int searchGoalX = (int)(CLIENT.player.getX() + Math.cos(randomAngle) * randomDistance);
                int searchGoalZ = (int)(CLIENT.player.getZ() + Math.sin(randomAngle) * randomDistance);
                BaritoneAPI.getSettings().allowBreak.value = false;
                this.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(searchGoalX, searchGoalZ));
                this.baritoneRepathTimer.reset();
            } else {
                this.mobSearchAttemptCount = 0;
                ClientChatLogger.II1Il11l111II11IIl("Мобов не нашел, новый RTP.");
                this.cancelBaritonePath();
                this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private LivingEntity findNearestTargetMob() {
        Object nearestTarget = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (Entity entity : CLIENT.world
        .getOtherEntities(CLIENT.player, CLIENT.player.getBoundingBox().expand(64.0, 24.0, 64.0))) {
            if (entity.getType() == this.targetMobType && entity instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity)entity;
                double targetDistanceSquared;
                if (livingTarget.isAlive() && (targetDistanceSquared = livingTarget.squaredDistanceTo(CLIENT.player)) < nearestDistanceSquared) {
                    nearestDistanceSquared = targetDistanceSquared;
                    nearestTarget = livingTarget;
                }
            }
        }

        return (LivingEntity)nearestTarget;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void selectCombatWeapon() {
        int weaponInventorySlot;
        if ((weaponInventorySlot = InventoryUtils.findItemSlot(stack -> stack.isIn(ItemTags.SWORDS))) == -1) {
            weaponInventorySlot = InventoryUtils.findItemSlot(stack -> stack.isIn(ItemTags.AXES));
        }

        if (weaponInventorySlot != -1) {
            if (weaponInventorySlot >= 9) {
                InventoryUtils.swapInventorySlotWithHotbar(weaponInventorySlot, 8, Priority.PriorityCategory.DEFAULT);
                weaponInventorySlot = 8;
            }

            InventoryUtils.setSelectedHotbarSlot(weaponInventorySlot);
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private EntityType<?> findEntityTypeByLocalizedName(String localizedEntityName) {
        for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
            if (entityType.getName().getString().toLowerCase(Locale.ROOT).equals(localizedEntityName)) {
                return (EntityType<?>)entityType;
            }
        }

        return null;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    public static void onBlockMined(BlockState blockState) {
        AutoQuest autoQuest = instance;
        if (autoQuest != null
        && autoQuest.isEnabled()
        && autoQuest.breakObjective
        && autoQuest.currentStage == AutoQuest.QuestStage.MINE_REQUIRED_BLOCKS
        && !autoQuest.blockRequirements.isEmpty()) {
            for (Block minedBlock : autoQuest.blockRequirements.get(0).blocks) {
                if (blockState.isOf(minedBlock)) {
                    autoQuest.minedBlockCount++;
                    if (autoQuest.minedBlockCount - 32 == 0 || autoQuest.minedBlockCount == autoQuest.requiredQuestItemCount) {
                        ClientChatLogger.II1Il11l111II11IIl(
                        String.format(
                        Locale.ROOT, "Сломано %d/%d.", Math.min(autoQuest.minedBlockCount, autoQuest.requiredQuestItemCount), autoQuest.requiredQuestItemCount
                        )
                        );
                    }

                    return;
                }
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void discardExcessInventoryItems() {
        if (this.discardIntervalTimer.hasElapsed(150L) && CLIENT.player.playerScreenHandler.getCursorStack().isEmpty()) {
            for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
                ItemStack stack = CLIENT.player.getInventory().getStack(inventorySlot);
                if (!stack.isEmpty() && this.shouldDiscardItem(stack)) {
                    InventoryUtils.clickSlot(InventoryUtils.toScreenHandlerSlot(inventorySlot), 1, SlotActionType.THROW);
                    this.discardIntervalTimer.reset();
                    return;
                }
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean shouldDiscardItem(ItemStack stack) {
        if (!this.breakObjective && this.matchesBlockRequirement(stack)) {
            return false;
        }

        if (SUPPORT_BLOCK_ITEM_PREDICATE.test(stack) && this.countMatchingItems(SUPPORT_BLOCK_ITEM_PREDICATE) - stack.getCount() < 64) {
            return false;
        }

        for (Item discardableItem : DISCARDABLE_ITEMS) {
            if (stack.isOf(discardableItem)) {
                return true;
            }
        }

        return false;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean recoverMiningTarget() {
        if (this.breakObjective) {
            return false;
        }

        if (this.itemEntity != null && (this.itemEntity.isRemoved() || this.droppedItemPathTimer.hasElapsed(8000L))) {
            if (!this.itemEntity.isRemoved()) {
                this.ignoredDroppedItemIds.add(this.itemEntity.getId());
            }

            this.itemEntity = null;
            this.getBaritone().getPathingBehavior().cancelEverything();
            BaritoneAPI.getSettings().allowBreakAnyway.value = new ArrayList<>(List.of(this.currentBlockRequirement.blocks));
            this.getBaritone().getMineProcess().mine(0, this.currentBlockRequirement.blocks);
            return true;
        } else if (this.itemEntity != null) {
            if (!this.getBaritone().getCustomGoalProcess().isActive()) {
                this.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(this.itemEntity.getBlockPos()));
            }

            return true;
        } else {
            this.itemEntity = this.findNearestDroppedItem();
            if (this.itemEntity != null) {
                this.droppedItemPathTimer.reset();
                this.getBaritone().getMineProcess().cancel();
                this.getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(this.itemEntity.getBlockPos()));
                return true;
            } else {
                return false;
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private ItemEntity findNearestDroppedItem() {
        ItemEntity nearestItemEntity = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        double searchVerticalExpansion = this.currentBlockRequirement != null && !this.currentBlockRequirement.blockOnly ? 4.0 : 12.0;

        for (ItemEntity candidateItemEntity : CLIENT.world
        .getEntitiesByClass(
        ItemEntity.class,
        CLIENT.player.getBoundingBox().expand(24.0, searchVerticalExpansion, 24.0),
        itemEntity -> (itemEntity.isOnGround() || itemEntity.isTouchingWater())
        && !this.ignoredDroppedItemIds.contains(itemEntity.getId())
        && this.matchesBlockRequirement(itemEntity.getStack())
        )) {
            double itemDistanceSquared;
            if ((itemDistanceSquared = candidateItemEntity.squaredDistanceTo(CLIENT.player)) < nearestDistanceSquared) {
                nearestDistanceSquared = itemDistanceSquared;
                nearestItemEntity = candidateItemEntity;
            }
        }

        return (ItemEntity)nearestItemEntity;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean matchesBlockRequirement(ItemStack stack) {
        for (Object blockRequirement : this.blockRequirements) {
            if (((AutoQuest.BlockRequirement)blockRequirement).itemPredicate.test(stack)) {
                return true;
            }
        }

        return false;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean containsAnyBlockInLoadedChunks(Block[] blockArray) {
        Predicate<BlockState> blockPredicate = blockState -> {
            for (Block var5x : blockArray) {
                if (blockState.isOf(var5x)) {
                    return true;
                }
            }

            return false;
        };
        int playerSectionX = ChunkSectionPos.getSectionCoord(CLIENT.player.getBlockX());
        int playerSectionZ = ChunkSectionPos.getSectionCoord(CLIENT.player.getBlockZ());
        int viewDistance = (Integer)CLIENT.options.getViewDistance().getValue();

        for (int sectionX = playerSectionX - viewDistance; sectionX <= playerSectionX + viewDistance; sectionX++) {
            for (int sectionZ = playerSectionZ - viewDistance; sectionZ <= playerSectionZ + viewDistance; sectionZ++) {
                if (CLIENT.world.isChunkLoaded(sectionX, sectionZ)) {
                    for (ChunkSection chunkSection : CLIENT.world.getChunk(sectionX, sectionZ).getSectionArray()) {
                        if (!chunkSection.isEmpty() && chunkSection.getBlockStateContainer().hasAny(blockPredicate)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean isPlayerNearby() {
        for (PlayerEntity player : CLIENT.world.getPlayers()) {
            if (player != CLIENT.player
            && player.isAlive()
            && !player.isSpectator()
            && player.distanceTo(CLIENT.player) <= 20.0F) {
                return true;
            }
        }

        return false;
    }

    private IBaritone getBaritone() {
        return BaritoneAPI.getProvider().getPrimaryBaritone();
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void configureBaritone() {
        Settings settings = BaritoneAPI.getSettings();
        settings.allowBreak.value = false;
        settings.allowBreakAnyway.value = new ArrayList();
        settings.blocksToAvoid.value = new ArrayList<>(
        Registries.BLOCK
        .stream()
        .filter(
        block -> block.getDefaultState().isIn(BlockTags.DOORS)
        || block.getDefaultState().isIn(BlockTags.TRAPDOORS)
        || block.getDefaultState().isIn(BlockTags.FENCE_GATES)
        )
        .toList()
        );
        settings.allowPlace.value = true;
        settings.acceptableThrowawayItems.value = new ArrayList<>(List.of(Items.COBBLESTONE, Items.COBBLED_DEEPSLATE, Items.DIRT));
        settings.allowSprint.value = true;
        settings.allowInventory.value = true;
        settings.autoTool.value = true;
        settings.freeLook.value = true;
        settings.antiCheatCompatibility.value = true;
        settings.allowParkour.value = false;
        settings.mineScanDroppedItems.value = true;
        settings.exploreForBlocks.value = false;
    }

    private void cancelBaritonePath() {
        this.getBaritone().getPathingBehavior().cancelEverything();
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void moveRequiredItems() {
        if (this.actionCooldownTimer.hasElapsed(this.questActionDelaySetting.getValue().longValue())) {
            if (!this.isHandledContainerOpen()) {
                this.actionCooldownTimer.reset();
                Item item = this.findLogForPlankItem(Registries.ITEM.getId(this.questItem).getPath());
                LinkedHashMap<Integer, Predicate<ItemStack>> linkedHashMap = new LinkedHashMap<>();
                if (this.countMatchingItems(this.questItemPredicate) < this.requiredQuestItemCount
                && item != null
                && this.countMatchingItems(stack -> stack.isOf(item)) > 0) {
                    linkedHashMap.put(1, stack -> stack.isOf(item));
                }

                if (!this.maintainCraftingGrid(CLIENT.player.playerScreenHandler, 1, 4, 9, linkedHashMap)) {
                    this.setStage(
                    this.countMatchingItems(this.questItemPredicate) <= 0 ? AutoQuest.QuestStage.WAIT_FOR_RTP : AutoQuest.QuestStage.RETURN_TO_SPAWN
                    );
                }
            } else {
                this.closeHandledScreen();
            }
        }
    }

    private void craftQuestItem() {
        if (this.actionCooldownTimer.hasElapsed(this.questActionDelaySetting.getValue().longValue()) && this.pendingBlockPlacement == null) {
            this.actionCooldownTimer.reset();
            if (!this.requiresFenceCrafting
            && !this.requiresSpecialToolCrafting
            && !this.requiresPotionBrewing
            && this.countMatchingItems(stack -> stack.isOf(Items.FURNACE)) > 0) {
                this.setStage(AutoQuest.QuestStage.CRAFT_FURNACE);
            } else {
                Screen screen = CLIENT.currentScreen;
                if (screen instanceof HandledScreen) {
                    HandledScreen<?> screen2 = (HandledScreen<?>)screen;
                    ScreenHandler currentScreenHandler = screen2.getScreenHandler();
                    if (currentScreenHandler instanceof CraftingScreenHandler) {
                        CraftingScreenHandler craftingHandler = (CraftingScreenHandler)currentScreenHandler;
                        if (this.requiresPotionBrewing) {
                            this.craftBrewingStand(craftingHandler);
                        } else if (this.requiresSpecialToolCrafting) {
                            this.craftQuestTool(craftingHandler);
                        } else if (this.requiresFenceCrafting) {
                            this.craftFence(craftingHandler);
                        } else {
                            this.craftFurnace(craftingHandler);
                        }

                        return;
                    }
                }

                if (this.isHandledContainerOpen()) {
                    this.closeHandledScreen();
                } else {
                    BlockPos craftingTablePosition = this.findNearbyBlock(Blocks.CRAFTING_TABLE);
                    if (craftingTablePosition != null) {
                        if (this.stageRetryCount++ > 5) {
                            this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                        } else {
                            this.scheduleBlockPlacement(craftingTablePosition, this.getBlockInteractionFace(craftingTablePosition));
                        }
                    } else if (this.countMatchingItems(stack -> stack.isOf(Items.CRAFTING_TABLE)) > 0) {
                        this.placeItemBlock(Items.CRAFTING_TABLE);
                    } else {
                        PlayerScreenHandler playerScreenHandler = CLIENT.player.playerScreenHandler;
                        LinkedHashMap<Integer, Predicate<ItemStack>> linkedHashMap = new LinkedHashMap<>();
                        if (this.countMatchingItems(PLANK_ITEM_PREDICATE) >= 4) {
                            for (int craftingSlotIndex = 1; craftingSlotIndex <= 4; craftingSlotIndex++) {
                                linkedHashMap.put(craftingSlotIndex, PLANK_ITEM_PREDICATE);
                            }
                        } else {
                            if (this.countMatchingItems(LOG_ITEM_PREDICATE) <= 0 && !this.hasPlanksInCraftingGrid(playerScreenHandler)) {
                                ClientChatLogger.l1I1IlllIlI("Нет дерева для верстака.");
                                this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                                return;
                            }

                            linkedHashMap.put(1, LOG_ITEM_PREDICATE);
                        }

                        this.maintainCraftingGrid(playerScreenHandler, 1, 4, 9, linkedHashMap);
                    }
                }
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private boolean hasPlanksInCraftingGrid(PlayerScreenHandler playerScreenHandler) {
        for (int craftingSlotIndex = 1; craftingSlotIndex <= 4; craftingSlotIndex++) {
            if (LOG_ITEM_PREDICATE.test(((Slot)playerScreenHandler.slots.get(craftingSlotIndex)).getStack())) {
                return true;
            }
        }

        return false;
    }

    private void craftFurnace(CraftingScreenHandler craftingScreenHandler) {
        if (!craftingScreenHandler.getCursorStack().isEmpty()) {
            for (int playerInventorySlot = 10; playerInventorySlot < craftingScreenHandler.slots.size(); playerInventorySlot++) {
                if (((Slot)craftingScreenHandler.slots.get(playerInventorySlot)).getStack().isEmpty()) {
                    InventoryUtils.clickSlot(playerInventorySlot, 0, SlotActionType.PICKUP);
                    return;
                }
            }
        } else if (((Slot)craftingScreenHandler.slots.get(0)).getStack().isOf(Items.FURNACE)) {
            InventoryUtils.clickSlot(0, 0, SlotActionType.QUICK_MOVE);
        } else if (this.countMatchingItems(furnaceStack -> furnaceStack.isOf(Items.FURNACE)) > 0) {
            for (int craftingGridSlot = 1; craftingGridSlot <= 9; craftingGridSlot++) {
                if (!((Slot)craftingScreenHandler.slots.get(craftingGridSlot)).getStack().isEmpty()) {
                    InventoryUtils.clickSlot(craftingGridSlot, 0, SlotActionType.QUICK_MOVE);
                    return;
                }
            }

            this.closeHandledScreen();
            this.setStage(AutoQuest.QuestStage.CRAFT_FURNACE);
        } else {
            int cobblestoneInventorySlot = -1;

            for (int craftingGridSlot = 10; craftingGridSlot < craftingScreenHandler.slots.size(); craftingGridSlot++) {
                if (STONE_MATERIAL_PREDICATE.test(((Slot)craftingScreenHandler.slots.get(craftingGridSlot)).getStack())) {
                    cobblestoneInventorySlot = craftingGridSlot;
                    break;
                }
            }

            if (cobblestoneInventorySlot == -1) {
                ClientChatLogger.l1I1IlllIlI("Не хватает булыжника на печку.");
                this.closeHandledScreen();
                this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
            } else {
                InventoryUtils.clickSlot(cobblestoneInventorySlot, 0, SlotActionType.PICKUP);

                for (int craftingGridSlot = 1; craftingGridSlot <= 9; craftingGridSlot++) {
                    if (craftingGridSlot != 5 && ((Slot)craftingScreenHandler.slots.get(craftingGridSlot)).getStack().isEmpty()) {
                        InventoryUtils.clickSlot(craftingGridSlot, 1, SlotActionType.PICKUP);
                    }
                }

                InventoryUtils.clickSlot(cobblestoneInventorySlot, 0, SlotActionType.PICKUP);
            }
        }
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void openFurnace() {
        if (this.actionCooldownTimer.hasElapsed(this.questActionDelaySetting.getValue().longValue()) && this.pendingBlockPlacement == null) {
            this.actionCooldownTimer.reset();
            Screen screen = CLIENT.currentScreen;
            if (screen instanceof HandledScreen) {
                HandledScreen<?> screen2 = (HandledScreen<?>)screen;
                if (screen2.getScreenHandler() instanceof AbstractFurnaceScreenHandler) {
                    this.setStage(AutoQuest.QuestStage.SMELT_QUEST_ITEMS);
                    return;
                }
            }

            if (this.isHandledContainerOpen()) {
                this.closeHandledScreen();
            } else {
                BlockPos furnacePosition = this.cachedFurnacePosition != null && CLIENT.world.getBlockState(this.cachedFurnacePosition).isOf(Blocks.FURNACE)
                ? this.cachedFurnacePosition
                : this.findNearbyBlock(Blocks.FURNACE);
                if (furnacePosition != null) {
                    this.cachedFurnacePosition = furnacePosition;
                    if (this.stageRetryCount++ <= 5) {
                        this.scheduleBlockPlacement(furnacePosition, this.getBlockInteractionFace(furnacePosition));
                    } else {
                        this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
                    }
                } else if (this.countMatchingItems(stack -> stack.isOf(Items.FURNACE)) != 0) {
                    this.placeItemBlock(Items.FURNACE);
                } else {
                    this.setStage(AutoQuest.QuestStage.CRAFT_QUEST_ITEM);
                }
            }
        }
    }

    private void smeltQuestItems() {
        Screen screen = CLIENT.currentScreen;
        if (screen instanceof HandledScreen) {
            HandledScreen<?> screen2 = (HandledScreen<?>)screen;
            ScreenHandler currentScreenHandler = screen2.getScreenHandler();
            if (currentScreenHandler instanceof AbstractFurnaceScreenHandler) {
                ScreenHandler furnaceHandler = (AbstractFurnaceScreenHandler)currentScreenHandler;
                if (!this.actionCooldownTimer.hasElapsed(Math.max(250L, this.questActionDelaySetting.getValue().longValue()))) {
                    return;
                }

                this.actionCooldownTimer.reset();
                if (!furnaceHandler.getCursorStack().isEmpty()) {
                    return;
                }

                if (!((Slot)furnaceHandler.slots.get(2)).getStack().isEmpty()) {
                    InventoryUtils.clickSlot(2, 0, SlotActionType.QUICK_MOVE);
                    return;
                }

                int smeltedQuestItemCount = this.countMatchingItems(this.questItemPredicate);
                if (smeltedQuestItemCount >= this.requiredQuestItemCount) {
                    this.closeHandledScreen();
                    ClientChatLogger.II1Il11l111II11IIl("Переплавка готова.");
                    this.setStage(AutoQuest.QuestStage.RETURN_TO_SPAWN);
                    return;
                }

                for (int inventorySlot = 3; inventorySlot < furnaceHandler.slots.size(); inventorySlot++) {
                    ItemStack stack = ((Slot)furnaceHandler.slots.get(inventorySlot)).getStack();
                    if (stack.isOf(Items.RAW_IRON)
                    && ((Slot)furnaceHandler.slots.get(0)).getStack().getCount() < this.requiredQuestItemCount - smeltedQuestItemCount) {
                        InventoryUtils.clickSlot(inventorySlot, 0, SlotActionType.QUICK_MOVE);
                        return;
                    }

                    if (stack.isOf(Items.COAL) && ((Slot)furnaceHandler.slots.get(1)).getStack().isEmpty()) {
                        InventoryUtils.clickSlot(inventorySlot, 0, SlotActionType.QUICK_MOVE);
                        return;
                    }
                }

                if (((Slot)furnaceHandler.slots.get(0)).getStack().isEmpty()
                && this.countMatchingItems(itemStackx -> itemStackx.isOf(Items.RAW_IRON)) == 0) {
                    this.closeHandledScreen();
                    this.setStage(smeltedQuestItemCount > 0 ? AutoQuest.QuestStage.RETURN_TO_SPAWN : AutoQuest.QuestStage.WAIT_FOR_RTP);
                }

                return;
            }
        }

        this.setStage(AutoQuest.QuestStage.CRAFT_FURNACE);
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private BlockPos findNearbyBlock(Block block) {
        BlockPos playerBlockPos = CLIENT.player.getBlockPos();

        for (BlockPos blockPosition : BlockPos.iterate(playerBlockPos.add(-4, -2, -4), playerBlockPos.add(4, 3, 4))) {
            if (CLIENT.world.getBlockState((BlockPos)blockPosition).isOf(block)
            && CLIENT.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter((Vec3i)blockPosition)) <= 16.0) {
                return blockPosition.toImmutable();
            }
        }

        return null;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void placeItemBlock(Item item) {
        BlockPos playerBlockPos = CLIENT.player.getBlockPos();

        for (Object direction : net.minecraft.util.math.Direction.Type.HORIZONTAL) {
            BlockPos candidateBlockPosition = playerBlockPos.offset((Direction)direction);
            BlockState blockState = CLIENT.world.getBlockState(candidateBlockPosition.down());
            if (CLIENT.world.getBlockState(candidateBlockPosition).isReplaceable()
            && CLIENT.world.getBlockState(candidateBlockPosition).getFluidState().isEmpty()
            && !CLIENT.player.getBoundingBox().intersects(new Box(candidateBlockPosition))
            && !blockState.getCollisionShape(CLIENT.world, candidateBlockPosition.down()).isEmpty()) {
                int itemInventorySlot;
                if ((itemInventorySlot = InventoryUtils.findItemSlot(stack -> stack.isOf(item))) != -1) {
                    if (itemInventorySlot >= 9) {
                        InventoryUtils.swapInventorySlotWithHotbar(itemInventorySlot, 7, Priority.PriorityCategory.DEFAULT);
                        itemInventorySlot = 7;
                    }

                    InventoryUtils.setSelectedHotbarSlot(itemInventorySlot);
                    this.scheduleBlockPlacement(candidateBlockPosition.down(), Direction.UP);
                    return;
                }

                return;
            }
        }

        ClientChatLogger.l1I1IlllIlI("Нет места поставить блок, новый RTP.");
        this.setStage(AutoQuest.QuestStage.WAIT_FOR_RTP);
    }

    private Direction getBlockInteractionFace(BlockPos targetBlockPos) {
        Vec3d directionVector = CLIENT.player.getEyePos().subtract(Vec3d.ofCenter(targetBlockPos));
        return Direction.getFacing(directionVector.x, directionVector.y, directionVector.z);
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private void scheduleBlockPlacement(BlockPos targetBlockPos, Direction direction) {
        Vec3d blockClickPosition = Vec3d.ofCenter(targetBlockPos).add(direction.getOffsetX() * 0.5, direction.getOffsetY() * 0.5, direction.getOffsetZ() * 0.5);
        RotationPriorityManager.getInstance()
        .requestRotation(
        RotationUtils.lookAt(CLIENT.player.getEyePos(), blockClickPosition), FocusMode.FREE, 60.0F, 3, false, this
        );
        this.pendingBlockPlacement = targetBlockPos;
        this.pendingBlockPlacementFace = direction;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private int countMatchingItems(Predicate<ItemStack> itemPredicate) {
        if (itemPredicate == null) {
            return 0;
        }

        int matchingItemCount = InventoryUtils.countMatchingItems(itemPredicate);
        if (CLIENT.player == null) {
            return matchingItemCount;
        }

        PlayerScreenHandler playerScreenHandler = CLIENT.player.playerScreenHandler;

        for (int playerInventorySlotIndex = 1; playerInventorySlotIndex <= 4; playerInventorySlotIndex++) {
            ItemStack inventoryStack = ((Slot)playerScreenHandler.slots.get(playerInventorySlotIndex)).getStack();
            if (itemPredicate.test(inventoryStack)) {
                matchingItemCount += inventoryStack.getCount();
            }
        }

        ScreenHandler currentScreenHandler = CLIENT.player.currentScreenHandler;
        if (currentScreenHandler instanceof CraftingScreenHandler) {
            CraftingScreenHandler craftingHandler = (CraftingScreenHandler)currentScreenHandler;

            for (int craftingSlotIndex = 1; craftingSlotIndex <= 9; craftingSlotIndex++) {
                ItemStack craftingGridStack = ((Slot)craftingHandler.slots.get(craftingSlotIndex)).getStack();
                if (itemPredicate.test(craftingGridStack)) {
                    matchingItemCount += craftingGridStack.getCount();
                }
            }
        }

        ItemStack cursorStack = currentScreenHandler.getCursorStack();
        if (itemPredicate.test(cursorStack)) {
            matchingItemCount += cursorStack.getCount();
        }

        return matchingItemCount;
    }

    @Allow2Generation(level = GenerationLevel.BALANCED)
    private String getItemLore(ItemStack stack) {
        LoreComponent loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (Text line : loreComponent.lines()) {
            stringBuilder.append(line.getString()).append('\n');
        }

        return stringBuilder.toString().toLowerCase(Locale.ROOT);
    }

    private boolean isHandledContainerOpen() {
        Screen screen = CLIENT.currentScreen;
        if (screen instanceof HandledScreen) {
            HandledScreen<?> screen2 = (HandledScreen<?>)screen;
            if (!(screen2.getScreenHandler() instanceof PlayerScreenHandler)) {
                return true;
            }
        }

        return false;
    }

    private void closeHandledScreen() {
        if (CLIENT.currentScreen instanceof HandledScreen) {
            CLIENT.player.closeHandledScreen();
        }
    }

    private record ItemRequirement(Predicate<ItemStack> itemPredicate, int requiredCount, String description) {

        private ItemRequirement(Predicate<ItemStack> itemPredicate, int requiredCount, String description) {
            this.itemPredicate = itemPredicate;
            this.requiredCount = requiredCount;
            this.description = description;
        }
    }

    private enum QuestStage {
        RETURN_TO_SPAWN(10000L),
        WAIT_FOR_SPAWN(15000L),
        OPEN_QUEST_MENU(15000L),
        SELECT_QUEST(15000L),
        CLAIM_QUEST_REWARD(10000L),
        WAIT_FOR_RTP(90000L),
        WAIT_FOR_RTP_RESULT(30000L),
        MINE_REQUIRED_BLOCKS(0L),
        HUNT(0L),
        MOVE_REQUIRED_ITEMS(30000L),
        PREPARE_CRAFTING_INVENTORY(60000L),
        COLLECT_REQUIRED_ITEMS(60000L),
        CRAFT_QUEST_ITEM(30000L),
        CRAFT_FURNACE(20000L),
        SMELT_QUEST_ITEMS(0L),
        COMPLETE_PVP_KILL(0L),
        WAIT_FOR_BOT_KILL(0L),
        BREW_QUEST_POTIONS(0L),
        EAT_QUEST_FOOD(900000L);

        private final long l1IIIIl1lII1IlIIIll1;

        QuestStage(long timeoutMillis) {
            this.l1IIIIl1lII1IlIIIll1 = timeoutMillis;
        }

        // $VF: synthetic method
        private static AutoQuest.QuestStage[] II1Il11l111II11IIl() {
            return new AutoQuest.QuestStage[]{
                RETURN_TO_SPAWN,
                WAIT_FOR_SPAWN,
                OPEN_QUEST_MENU,
                SELECT_QUEST,
                CLAIM_QUEST_REWARD,
                WAIT_FOR_RTP,
                WAIT_FOR_RTP_RESULT,
                MINE_REQUIRED_BLOCKS,
                HUNT,
                MOVE_REQUIRED_ITEMS,
                PREPARE_CRAFTING_INVENTORY,
                COLLECT_REQUIRED_ITEMS,
                CRAFT_QUEST_ITEM,
                CRAFT_FURNACE,
                SMELT_QUEST_ITEMS,
                COMPLETE_PVP_KILL,
                WAIT_FOR_BOT_KILL,
                BREW_QUEST_POTIONS,
                EAT_QUEST_FOOD
            };
        }
    }

    private record BlockRequirement(String description, Predicate<ItemStack> itemPredicate, Block[] blocks, int requiredCount, boolean blockOnly) {

        private BlockRequirement(String description, Predicate<ItemStack> itemPredicate, Block[] blocks, int requiredCount, boolean blockOnly) {
            this.description = description;
            this.itemPredicate = itemPredicate;
            this.blocks = blocks;
            this.requiredCount = requiredCount;
            this.blockOnly = blockOnly;
        }
    }
}

