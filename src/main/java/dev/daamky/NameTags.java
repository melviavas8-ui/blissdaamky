package dev.daamky;

import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventHudLayer;
import dev.daamky.core.event.impl.EventPacket;
import dev.daamky.core.event.impl.EventUpdate;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import project.melvi.metadata.Allow2Generation;
import project.melvi.metadata.Allow2Generation.GenerationLevel;

public final class NameTags extends ModuleStructure {
    private static final String TELEGRAM_CHANNEL_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static NameTags INSTANCE;
    private static final MsdfFont NAME_TAG_FONT = Medium.DEFAULT_FONT;
    private static final float UNUSED_FLOAT_CONSTANT_5_8 = 5.8F;
    private static final float UNUSED_FLOAT_CONSTANT_8_0_A = 8.0F;
    private static final float UNUSED_FLOAT_CONSTANT_8_0_B = 8.0F;
    private static final float UNUSED_FLOAT_CONSTANT_4_8 = 4.8F;
    private static final float UNUSED_FLOAT_CONSTANT_7_0 = 7.0F;
    private static final float UNUSED_FLOAT_CONSTANT_3_5_A = 3.5F;
    private static final float UNUSED_FLOAT_CONSTANT_2_0_A = 2.0F;
    private static final float UNUSED_FLOAT_CONSTANT_3_0_A = 3.0F;
    private static final float UNUSED_FLOAT_CONSTANT_1_5 = 1.5F;
    private static final float UNUSED_FLOAT_CONSTANT_3_5_B = 3.5F;
    private static final float UNUSED_FLOAT_CONSTANT_130_0 = 130.0F;
    private static final float UNUSED_FLOAT_CONSTANT_2_0_B = 2.0F;
    private static final float UNUSED_FLOAT_CONSTANT_4_0_A = 4.0F;
    private static final float UNUSED_FLOAT_CONSTANT_3_0_B = 3.0F;
    private static final float UNUSED_FLOAT_CONSTANT_10_5 = 10.5F;
    private static final float UNUSED_FLOAT_CONSTANT_1_0 = 1.0F;
    private static final float UNUSED_FLOAT_CONSTANT_0_68 = 0.68F;
    private static final float UNUSED_FLOAT_CONSTANT_3_5_C = 3.5F;
    private static final int UNUSED_INT_CONSTANT_NEGATIVE_9706598 = -9706598;
    private static final float UNUSED_FLOAT_CONSTANT_6_0 = 6.0F;
    private static final float UNUSED_FLOAT_CONSTANT_5_0 = 5.0F;
    private static final float UNUSED_FLOAT_CONSTANT_4_0_B = 4.0F;
    private static final float UNUSED_FLOAT_CONSTANT_2_0_C = 2.0F;
    private static final float UNUSED_FLOAT_CONSTANT_4_0_C = 4.0F;
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
        EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };
    private final dev.daamky.BooleanSetting playersSetting = new dev.daamky.BooleanSetting("Игроки", true);
    private final dev.daamky.BooleanSetting mobsSetting = new dev.daamky.BooleanSetting("Мобы", true);
    private final dev.daamky.BooleanSetting itemsSetting = new dev.daamky.BooleanSetting("Предметы", true);
    private final dev.daamky.BooleanSetting drawItemNamesSetting = new dev.daamky.BooleanSetting("Рисовать айтем", true);
    private final dev.daamky.BooleanSetting groupNearbyItemsSetting = new dev.daamky.BooleanSetting("Группировать", true);
    private final FloatSetting nearbyItemGroupDistanceSetting = new FloatSetting("Дистанция группы", 3.0F, 1.0F, 8.0F, 0.5F);
    private final FloatSetting maxNearbyItemCountSetting = new FloatSetting("Макс. предметов", 10.0F, 3.0F, 25.0F, 1.0F);
    private final dev.daamky.BooleanSetting tntFuseTimerSetting = new dev.daamky.BooleanSetting("ТНТ таймер", true);
    private final dev.daamky.BooleanSetting showPlayerHandsSetting = new dev.daamky.BooleanSetting("Руки игрока", false);
    private final dev.daamky.BooleanSetting showPotionStatusSetting = new dev.daamky.BooleanSetting("Писать зелья", false);
    private final MultiToggleSetting potionStatusEffectsSetting = new MultiToggleSetting(
    "Зелья",
    new dev.daamky.BooleanSetting("Сила", true),
    new dev.daamky.BooleanSetting("Скорость", true),
    new dev.daamky.BooleanSetting("Черепашка", true),
    new dev.daamky.BooleanSetting("Спешка", true)
    );
    private final FloatSetting nameTagScaleSetting = new FloatSetting("Размер", 1.0F, 0.2F, 2.0F, 0.05F);
    private final PotionStatusTracker potionStatusTracker = new PotionStatusTracker();
    private final Map<Entity, NameTags.PotionStatusLayout> potionStatusLayoutCache = new IdentityHashMap<>();
    private final NameTags.NearbyItemsCache nearbyItemGroupCache = new NameTags.NearbyItemsCache();
    private static final NameTags.PotionStatusLayout EMPTY_POTION_STATUS_LAYOUT = new NameTags.PotionStatusLayout(List.of(), 0.0F, 0.0F);
    private static final Map<UUID, NameTags.CachedFormattedName> formattedNameCache = new ConcurrentHashMap<>();
    private static final Pattern LEGACY_COLOR_CODE_PATTERN = Pattern.compile("(?i)[§&][0-9a-z]");
    private static final Pattern PRIVATE_USE_CHARACTER_PATTERN = Pattern.compile("[\\uE000-\\uF8FF]");
    private static final Pattern REPLACEMENT_CHARACTER_PATTERN = Pattern.compile("[\\?\\uFFFD]+");
    private static final Pattern DECORATIVE_PREFIX_PATTERN = Pattern.compile("^[\\u25C6\\u25C7\\u2605\\u2726\\u2727\\u2022\\*\\-\\.\\[\\]\\(\\)\\s]+");
    private static final Pattern LEADING_COLOR_CODE_PATTERN = Pattern.compile("(?i)^[a-f0-9б6]\\s+");
    private static final Pattern TRAILING_COLOR_CODE_PATTERN = Pattern.compile("(?i)\\s+[a-f0-9б6]$");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern ITEM_DECORATION_PATTERN = Pattern.compile("[⭐★\\[\\]\\|:•\\*\\-\\.]+");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern STRENGTH_IV_PATTERN = Pattern.compile("(?i).*(?:^|[^a-zа-я0-9])(iv|4|40%|\\+4)(?:$|[^a-zа-я0-9]).*");
    private static final Pattern STRENGTH_III_PATTERN = Pattern.compile("(?i).*(?:^|[^a-zа-я0-9])(iii|3|30%|\\+3)(?:$|[^a-zа-я0-9]).*");
    private static final Pattern STRENGTH_II_PATTERN = Pattern.compile("(?i).*(?:^|[^a-zа-я0-9])(ii|2|20%|\\+2)(?:$|[^a-zа-я0-9]).*");
    private static final Pattern STRENGTH_I_PATTERN = Pattern.compile("(?i).*(?:^|[^a-zа-я0-9])(i|1|10%|\\+1)(?:$|[^a-zа-я0-9]).*");
    private static final Pattern PLAYER_TEXTURE_URL_PATTERN = Pattern.compile("textures\\.minecraft\\.net/texture/([a-zA-Z0-9]+)");
    private static final Pattern BASE64_PROFILE_PROPERTY_PATTERN = Pattern.compile("(eyJ0ZXh0[A-Za-z0-9+/=]+)");
    private static final Pattern SHARPNESS_LEVEL_PATTERN = Pattern.compile("(?i)(?:острота|sharpness|остр[\\.\\s])\\s*([ivxlcdm\\d]+)");
    private static final Map<ItemStack, String> itemDisplayNameCache = new WeakHashMap<>();
    private static final Map<String, String> specialOrbNameCache = new ConcurrentHashMap<>();
// https://t.me/pasterplace

    public NameTags() {
        super("NameTags", "Показывает ники и здоровье через стены", ModuleCategory.RENDER);

        this.nearbyItemGroupDistanceSetting.visibleWhen(this.groupNearbyItemsSetting::getValue);
        this.maxNearbyItemCountSetting.visibleWhen(this.groupNearbyItemsSetting::getValue);
        this.showPotionStatusSetting.visibleWhen(this.playersSetting::getValue);
        this.potionStatusEffectsSetting.visibleWhen(() -> this.playersSetting.getValue() && this.showPotionStatusSetting.getValue());
        this.addSettings(
        this.playersSetting,
        this.mobsSetting,
        this.itemsSetting,
        this.drawItemNamesSetting,
        this.groupNearbyItemsSetting,
        this.nearbyItemGroupDistanceSetting,
        this.maxNearbyItemCountSetting,
        this.tntFuseTimerSetting,
        this.showPlayerHandsSetting,
        this.showPotionStatusSetting,
        this.potionStatusEffectsSetting,
        this.nameTagScaleSetting
        );
    }
// https://t.me/pasterplace

    public static boolean isNameTagsActive() {
        return INSTANCE != null && INSTANCE.isEnabled();
    }
// https://t.me/pasterplace

    public static boolean shouldRenderNameTag(Entity entity) {
        return isNameTagsActive() && INSTANCE.shouldRenderEntity(entity);
    }
// https://t.me/pasterplace

    @Allow2Generation(level = GenerationLevel.FAST)
    @Override
    protected void onDisable() {
        this.potionStatusTracker.clear();
        this.nearbyItemGroupCache.clear();
        this.potionStatusLayoutCache.clear();
    }
// https://t.me/pasterplace

    private boolean shouldRenderEntity(Entity entity) {
        if (entity == null || !entity.isAlive() || entity instanceof ArmorStandEntity) {
            return false;
        } else if (entity instanceof PlayerEntity) {
            return this.playersSetting.getValue();
        } else if (entity instanceof ItemEntity) {
            return this.itemsSetting.getValue();
        } else if (entity instanceof TntEntity) {
            return this.tntFuseTimerSetting.getValue();
        } else {
            return !(entity instanceof AnimalEntity) && !(entity instanceof MobEntity) ? false : this.mobsSetting.getValue();
        }
    }
// https://t.me/pasterplace

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void onUpdate(EventUpdate eventUpdate) {
        if (this.showPotionStatusSetting.getValue() && this.playersSetting.getValue()) {
            this.potionStatusTracker.update(CLIENT.world);
        } else {
            this.potionStatusTracker.clear();
        }
    }
// https://t.me/pasterplace

    @EventTarget
    @Allow2Generation(level = GenerationLevel.FAST)
    public void onPacket(EventPacket eventPacket) {
        if (eventPacket.isReceive() && this.showPotionStatusSetting.getValue() && this.playersSetting.getValue()) {
            Packet packet = eventPacket.packet();
            if (!(packet instanceof EntityStatusS2CPacket)) {
                packet = eventPacket.packet();
                if (packet instanceof WorldEventS2CPacket) {
                    WorldEventS2CPacket packet4 = (WorldEventS2CPacket)packet;
                    if (packet4.getEventId() == 2002 || packet4.getEventId() == 2007) {
                        CLIENT.execute(() -> {
                            if (this.isEnabled() && CLIENT.world != null) {
                                this.potionStatusTracker.recordPotionImpact(Vec3d.ofCenter(packet4.getPos()), packet4.getData());
                            }
                        });
                    }
                }
            } else {
                EntityStatusS2CPacket packet2 = (EntityStatusS2CPacket)packet;
                CLIENT.execute(() -> {
                    if (this.isEnabled() && CLIENT.world != null) {
                        Entity entity = packet2.getEntity(CLIENT.world);
                        if (entity instanceof PlayerEntity) {
                            PlayerEntity entity2 = (PlayerEntity)entity;
                            byte var4x = packet2.getStatus();
                            if (var4x == 9) {
                                this.potionStatusTracker.refreshPlayer(entity2);
                            } else if (var4x == 3 || var4x == 35) {
                                this.potionStatusTracker.forgetPlayer(entity2);
                            }
                        }
                    }
                });
            }
        }
    }
// https://t.me/pasterplace

    @EventTarget(priority = 0)
    public void onHudLayer(EventHudLayer eventHudLayer) {
        this.renderNameTags(eventHudLayer.graphics());
    }
// https://t.me/pasterplace

    private void renderNameTags(DrawContext drawContext) {
        this.potionStatusLayoutCache.clear();
        if (CLIENT.player != null && CLIENT.world != null && !CLIENT.options.hudHidden) {
            float scaledViewportHeight = GuiEventRenderer.getScreenHeight();
            float nameTagScale = this.nameTagScaleSetting.getValue();
            ArrayList<Entity> arrayList = new ArrayList<>();

            for (Entity entity : CLIENT.world.getEntities()) {
                if (this.shouldRenderEntity(entity)) {
                    arrayList.add(entity);
                }
            }

            boolean groupNearbyItemsEnabled = this.itemsSetting.getValue() && this.groupNearbyItemsSetting.getValue();
            if (groupNearbyItemsEnabled) {
                this.nearbyItemGroupCache
                .updateGroups(arrayList, this.nearbyItemGroupDistanceSetting.getValue(), Math.round(this.maxNearbyItemCountSetting.getValue()), this.drawItemNamesSetting.getValue());
            } else {
                this.nearbyItemGroupCache.clear();
            }

            ArrayList<NameTags.EntityRenderMetrics> arrayList2 = new ArrayList<>();

            for (Entity entity : arrayList) {
                if (entity != CLIENT.player || !CLIENT.options.getPerspective().isFirstPerson()) {
                    if (entity instanceof ItemEntity) {
                        ItemEntity itemEntity = (ItemEntity)entity;
                        if (groupNearbyItemsEnabled && this.nearbyItemGroupCache.isGroupedItem(itemEntity)) {
                            continue;
                        }
                    }

                    Vec3d projectedEntityPosition = WorldToScreenProjection.getInterpolatedPosition((Entity)entity);
                    WorldToScreenProjection.ScreenPoint entity4 = WorldToScreenProjection.project(projectedEntityPosition.add(0.0, entity.getHeight() + 0.15, 0.0));
                    WorldToScreenProjection.ScreenPoint entity3 = WorldToScreenProjection.project(projectedEntityPosition);
                    boolean headPointVisible = entity4 != null && entity4.inFront();
                    boolean basePointVisible = entity3 != null && entity3.inFront();
                    if ((headPointVisible || basePointVisible)
                    && (!basePointVisible || !(entity3.y() < 0.0F))
                    && (!headPointVisible || !(entity4.y() > scaledViewportHeight) || !basePointVisible || !(entity3.y() > scaledViewportHeight))) {
                        float tagAnchorX = headPointVisible ? entity4.x() : entity3.x();
                        float tagAnchorY = headPointVisible ? entity4.y() - 6.0F : 15.0F;
                        if (tagAnchorY < 15.0F) {
                            tagAnchorY = 15.0F;
                        }

                        if (entity instanceof PlayerEntity) {
                            PlayerEntity player = (PlayerEntity)entity;
                            tagAnchorY = this.getPlayerTagY(player, tagAnchorY, nameTagScale);
                        }

                        arrayList2.add(
                        new NameTags.EntityRenderMetrics(
                        (Entity)entity, tagAnchorX, tagAnchorY, basePointVisible ? entity3.x() : 0.0F, basePointVisible ? entity3.y() : 0.0F, basePointVisible, nameTagScale
                        )
                        );
                    }
                }
            }

            if (groupNearbyItemsEnabled) {
                this.nearbyItemGroupCache.renderGroups(drawContext, nameTagScale, scaledViewportHeight, false);
            }

            for (NameTags.EntityRenderMetrics entityMetrics : arrayList2) {
                this.renderEntityTag(drawContext, entityMetrics, true);
            }

            for (NameTags.EntityRenderMetrics entityMetrics : arrayList2) {
                this.renderEntityTag(drawContext, entityMetrics, false);
            }

            GuiEventRenderer.flushAndStartRootLayer(drawContext);
            if (groupNearbyItemsEnabled) {
                this.nearbyItemGroupCache.renderGroups(drawContext, nameTagScale, scaledViewportHeight, true);
            }

            for (NameTags.EntityRenderMetrics entityMetrics : arrayList2) {
                Entity entity = entityMetrics.entity();
                if (entity instanceof PlayerEntity) {
                    PlayerEntity entity2 = (PlayerEntity)entity;
                    this.renderEquipmentItems(
                    drawContext,
                    entity2,
                    entityMetrics.width(),
                    this.computePotionLayoutY(entity2, entityMetrics),
                    entityMetrics.scale()
                    );
                    if (this.showPlayerHandsSetting.getValue() && entityMetrics.isVisible()) {
                        this.renderHeldItemIcons(
                        drawContext,
                        entity2,
                        entityMetrics.left(),
                        entityMetrics.top(),
                        entityMetrics.scale()
                        );
                    }
                } else {
                    entity = entityMetrics.entity();
                    if (entity instanceof ItemEntity) {
                        ItemEntity entity4 = (ItemEntity)entity;
                        if (this.drawItemNamesSetting.getValue()) {
                            this.renderItemIconTag(
                            drawContext,
                            entity4,
                            entityMetrics.width(),
                            entityMetrics.height(),
                            entityMetrics.scale()
                            );
                        }
                    }
                }
            }
        }
    }
// https://t.me/pasterplace

    private void renderEntityTag(DrawContext drawContext, NameTags.EntityRenderMetrics metrics, boolean backgroundPass) {
        Entity entity = metrics.entity();
        if (entity instanceof PlayerEntity) {
            PlayerEntity entity2 = (PlayerEntity)entity;
            this.renderPlayerNameTag(drawContext, entity2, metrics.width(), metrics.height(), metrics.scale(), backgroundPass);
            this.renderPotionStatusLines(drawContext, entity2, metrics.width(), metrics.height(), metrics.scale(), backgroundPass);
            if (backgroundPass) {
                this.renderEquipmentSlotBackplates(drawContext, entity2, metrics.width(), this.computePotionLayoutY(entity2, metrics), metrics.scale());
            }

            if (this.showPlayerHandsSetting.getValue() && metrics.isVisible()) {
                this.renderHeldItemNames(drawContext, entity2, metrics.left(), metrics.top(), metrics.scale(), backgroundPass);
            }
        } else if (entity instanceof ItemEntity) {
            ItemEntity entity3 = (ItemEntity)entity;
            this.renderItemTag(drawContext, entity3, metrics.width(), metrics.height(), metrics.scale(), backgroundPass);
        } else if (entity instanceof TntEntity) {
            TntEntity entity4 = (TntEntity)entity;
            this.renderTntTag(drawContext, entity4, metrics.width(), metrics.height(), metrics.scale(), backgroundPass);
        } else if (entity instanceof LivingEntity) {
            LivingEntity entity5 = (LivingEntity)entity;
            this.renderLivingEntityTag(drawContext, entity5, metrics.width(), metrics.height(), metrics.scale(), backgroundPass);
        }
    }
// https://t.me/pasterplace

    private float computePotionLayoutY(PlayerEntity playerEntity, NameTags.EntityRenderMetrics metrics) {
        float tagHeaderHeight = 12.8F;
        float potionStatusY = metrics.height() - tagHeaderHeight * metrics.scale() - 1.5F - 10.5F * metrics.scale();
        if (potionStatusY < 2.0F) {
            potionStatusY = metrics.height() + this.getPotionStatusLayout(playerEntity).height() * metrics.scale() + 2.0F + 2.0F * metrics.scale();
        }

        return potionStatusY;
    }
// https://t.me/pasterplace

    private static float getScaledLineHeight(float fontSize) {
        return NAME_TAG_FONT.metrics().lineHeight() * fontSize;
    }
// https://t.me/pasterplace

    private static float measureTextWidth(String charSequence, float fontSize) {
        return NAME_TAG_FONT.measureWidth(charSequence, fontSize);
    }
// https://t.me/pasterplace

    private static void drawText(
    DrawContext drawContext, MsdfFont font, String displayText, float x, float y, float fontSize, int colorArgb
    ) {
        GuiTextBuilder.createAt(font, displayText, x, y, fontSize).color(colorArgb).render(drawContext);
    }
// https://t.me/pasterplace

    private static void pushScaleTransform(DrawContext drawContext, float pivotX, float pivotY, float scale) {
        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().translate(pivotX, pivotY);
        drawContext.getMatrices().scale(scale);
        drawContext.getMatrices().translate(-pivotX, -pivotY);
    }
// https://t.me/pasterplace

    private static void popScaleTransform(DrawContext drawContext) {
        drawContext.getMatrices().popMatrix();
    }
// https://t.me/pasterplace

    private static void drawBrokkeIcon(DrawContext drawContext, float x, float y, float width, float height, float cornerRadius, float opacity) {
        if (!(opacity <= 0.01F) && !(width <= 0.0F) && !(height <= 0.0F)) {
            BrokkeIcons.drawRoundedPanelBackground(drawContext, x, y, width, height, cornerRadius, opacity);
        }
    }
// https://t.me/pasterplace

    private static void drawAvatarFallback(DrawContext drawContext, float x, float y, float size) {
        RoundedGradientRectBuilder.at(x, y, size, size)
        .cornerRadius(size * 0.5F)
        .color(Accent.getAccentColorWithAlpha(110))
        .render(drawContext);
    }
// https://t.me/pasterplace

    private static int getPrimaryAccentArgb() {
        return Accent.getAccentColorWithAlpha(255).toArgb();
    }
// https://t.me/pasterplace

    private static int getMutedLabelArgb() {
        return Accent.getTextColorWithAlpha(250).toArgb();
    }
// https://t.me/pasterplace

    private static int getSubtleLabelColor() {
        return Accent.getDarkerTextColor(220).toArgb();
    }
// https://t.me/pasterplace

    private static void drawItemStack(DrawContext drawContext, ItemStack stack, float x, float y, float scale) {
        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().translate(x, y);
        drawContext.getMatrices().scale(scale / 16.0F);
        drawContext.getMatrices().translate(-8.0F, -8.0F);

        try {
            GuiEventRenderer.flushBatchedElements();
            drawContext.drawItem(stack, 0, 0);
        } catch (Exception ignoredRenderException) {
        }

        drawContext.getMatrices().popMatrix();
    }
// https://t.me/pasterplace

    private void renderPlayerNameTag(DrawContext drawContext, PlayerEntity player, float tagCenterX, float tagY, float scale, boolean backgroundPass) {
        String playerName = player.getName()
        .getString()
        .replaceAll("(?i)[§&][0-9a-fk-orx]", "")
        .replaceAll("[\\p{Cntrl}\\p{Co}\\p{Cn}�§]", "")
        .replaceAll("^\\?(?:[0-9a-fk-or]\\?)*[0-9a-fk-or]", "")
        .trim();
        boolean isFriend = FriendManager.getInstance().isFriend(playerName);
        float maxHealth = Math.max(1.0F, player.getMaxHealth());
        float absorptionHealth = player.getAbsorptionAmount();
        String healthText = String.valueOf((int)Math.ceil(player.getHealth() + absorptionHealth));
        NameTags.FormattedName formattedRank = this.getCachedFormattedName(player);
        String rankText = formattedRank != null ? formattedRank.text() : null;
        float rankTextWidth = rankText != null ? measureTextWidth(rankText, 5.8F) + 3.0F : 0.0F;
        float playerNameWidth = measureTextWidth(playerName, 5.8F);
        float healthTextWidth = measureTextWidth(healthText, 5.8F);
        float portraitColumnWidth = 11.5F;
        float panelGap = 2.0F;
        float namePanelWidth = 4.0F + portraitColumnWidth + rankTextWidth + playerNameWidth + 4.0F;
        float healthBarWidth = 5.0F;
        float healthPanelWidth = 8.0F + healthTextWidth + 2.5F + healthBarWidth;
        String friendLabel = "Друг";
        float friendBadgeWidth = isFriend ? 8.0F + measureTextWidth(friendLabel, 5.8F) : 0.0F;
        float totalTagWidth = namePanelWidth + panelGap + healthPanelWidth + (isFriend ? panelGap + friendBadgeWidth : 0.0F);
        float tagHeight = 12.8F;
        float tagLeftX = tagCenterX - totalTagWidth / 2.0F;
        float tagTopY = tagY - tagHeight;
        float healthPanelX = tagLeftX + namePanelWidth + panelGap;
        pushScaleTransform(drawContext, tagCenterX, tagY, scale);
        if (backgroundPass) {
            drawBrokkeIcon(drawContext, tagLeftX, tagTopY, namePanelWidth, tagHeight, 3.5F, 1.0F);
            drawBrokkeIcon(drawContext, healthPanelX, tagTopY, healthPanelWidth, tagHeight, 3.5F, 1.0F);
            if (isFriend) {
                drawBrokkeIcon(drawContext, healthPanelX + healthPanelWidth + panelGap, tagTopY, friendBadgeWidth, tagHeight, 3.5F, 1.0F);
            }

            popScaleTransform(drawContext);
        } else {
            float tagCenterY = tagTopY + tagHeight / 2.0F;
            if (isFriend) {
                float friendBadgeX = healthPanelX + healthPanelWidth + panelGap;
                RoundedGradientRectBuilder.at(tagLeftX, tagTopY, namePanelWidth, tagHeight)
                .cornerRadius(3.5F)
                .color(RgbaColor.fromArgb(577495962))
                .render(drawContext);
                RoundedGradientRectBuilder.at(friendBadgeX, tagTopY, friendBadgeWidth, tagHeight)
                .cornerRadius(3.5F)
                .color(RgbaColor.fromArgb(1080812442))
                .render(drawContext);
                RoundedGradientRectBuilder.at(tagLeftX + 1.5F, tagTopY + tagHeight - 1.2F, namePanelWidth - 3.0F, 0.8F)
                .cornerRadius(0.4F)
                .color(RgbaColor.fromArgb(-9706598))
                .render(drawContext);
                drawText(drawContext, NAME_TAG_FONT, friendLabel, friendBadgeX + 4.0F, tagCenterY - getScaledLineHeight(5.8F) / 2.0F, 5.8F, -9706598);
            }

            float avatarX = tagLeftX + 4.0F - 0.5F;
            float avatarY = tagCenterY - 4.0F;
            if (player instanceof AbstractClientPlayerEntity clientPlayer) {
                PlayerSkinQuadBuilder.at(avatarX, avatarY, 8.0F)
                .playerSkin(clientPlayer)
                .cornerRadius(2.5F)
                .overlay(RgbaColor.rgb(255, 70, 70).toArgb(), player.hurtTime > 0 ? 0.45F : 0.0F)
                .render(drawContext);
            } else {
                RoundedGradientRectBuilder.at(avatarX, avatarY, 8.0F, 8.0F)
                .cornerRadius(2.5F)
                .color(Accent.getAccentColorWithAlpha(40))
                .render(drawContext);
            }

            float labelY = tagCenterY - getScaledLineHeight(5.8F) / 2.0F;
            float rankTextX = tagLeftX + 4.0F + portraitColumnWidth;
            if (rankText != null) {
                drawText(drawContext, NAME_TAG_FONT, rankText, rankTextX, labelY, 5.8F, formattedRank.colorArgb());
                rankTextX += rankTextWidth;
            }

            drawText(drawContext, NAME_TAG_FONT, playerName, rankTextX, labelY, 5.8F, isFriend ? -9706598 : getMutedLabelArgb());
            float healthProgress = Math.clamp(player.getHealth() / maxHealth, 0.0F, 1.0F);
            int healthBarColor = getPrimaryAccentArgb();
            BrokkeIcons.drawCenteredText(drawContext, healthText, healthPanelX + 4.0F, tagCenterY, 5.8F, RgbaColor.fromArgb(healthBarColor));
            AnimatedListHudElement.drawProgressRing(drawContext, healthPanelX + healthPanelWidth - 4.0F - healthBarWidth / 2.0F, tagCenterY, healthBarWidth / 2.0F - 0.4F, 0.85F, 18, healthProgress, 1.0F);
            if (absorptionHealth > 0.0F) {
                AnimatedListHudElement.drawProgressRing(
                drawContext,
                healthPanelX + healthPanelWidth - 4.0F - healthBarWidth / 2.0F,
                tagCenterY,
                healthBarWidth / 2.0F - 0.4F,
                0.85F,
                18,
                Math.clamp(absorptionHealth / maxHealth, 0.0F, 1.0F),
                RgbaColor.rgb(255, 200, 90),
                1.0F
                );
            }

            popScaleTransform(drawContext);
        }
    }
// https://t.me/pasterplace

    private NameTags.PotionStatusLayout getPotionStatusLayout(PlayerEntity playerEntity) {
        if (!this.showPotionStatusSetting.getValue()) {
            return EMPTY_POTION_STATUS_LAYOUT;
        }

        Object cachedLayout = this.potionStatusLayoutCache.get(playerEntity);
        if (cachedLayout != null) {
            return (NameTags.PotionStatusLayout)cachedLayout;
        }

        ArrayList statusSegments = new ArrayList(4);
        if (this.potionStatusEffectsSetting.isOptionEnabled("Сила")) {
            this.appendPotionStatusSegment(statusSegments, this.potionStatusTracker.statusFor(playerEntity, StatusEffects.STRENGTH), "Сила", true, -36752);
        }

        if (this.potionStatusEffectsSetting.isOptionEnabled("Скорость")) {
            this.appendPotionStatusSegment(
            statusSegments, this.potionStatusTracker.statusFor(playerEntity, StatusEffects.SPEED), "Скорость", true, -9382401
            );
        }

        PotionStatus resistanceStatus = this.potionStatusTracker.statusFor(playerEntity, StatusEffects.RESISTANCE);
        PotionStatus slownessStatus = this.potionStatusTracker.statusFor(playerEntity, StatusEffects.SLOWNESS);
        if (this.potionStatusEffectsSetting.isOptionEnabled("Черепашка")
        && resistanceStatus != null
        && slownessStatus != null
        && resistanceStatus.amplifier() >= 2
        && slownessStatus.amplifier() >= 3) {
            boolean infinite = resistanceStatus.infinite() && slownessStatus.infinite();
            int duration = resistanceStatus.infinite() ? slownessStatus.remainingTicks() : (slownessStatus.infinite() ? resistanceStatus.remainingTicks() : Math.min(resistanceStatus.remainingTicks(), slownessStatus.remainingTicks()));
            this.appendPotionStatusSegment(
            statusSegments,
            new PotionStatus(0, duration, infinite, resistanceStatus.approximate() || slownessStatus.approximate()),
            "Черепах",
            false,
            -7842624
            );
        }

        if (this.potionStatusEffectsSetting.isOptionEnabled("Спешка")) {
            this.appendPotionStatusSegment(statusSegments, this.potionStatusTracker.statusFor(playerEntity, StatusEffects.HASTE), "Спешка", true, -12696);
        }

        if (statusSegments.isEmpty()) {
            this.potionStatusLayoutCache.put(playerEntity, EMPTY_POTION_STATUS_LAYOUT);
            return EMPTY_POTION_STATUS_LAYOUT;
        }

        ArrayList layoutRows = new ArrayList(2);
        ArrayList currentRowSegments = new ArrayList(3);
        float currentRowWidth = 0.0F;
        float segmentSeparatorWidth = measureTextWidth(" | ", 4.8F);

        for (Object statusSegment : statusSegments) {
            float candidateRowWidth = currentRowWidth + (currentRowSegments.isEmpty() ? 0.0F : segmentSeparatorWidth) + ((NameTags.ItemNameSegment)statusSegment).width();
            if (!currentRowSegments.isEmpty() && candidateRowWidth > 130.0F) {
                layoutRows.add(new NameTags.NameTagLayoutRow(List.copyOf(currentRowSegments), currentRowWidth));
                currentRowSegments.clear();
                currentRowWidth = 0.0F;
            }

            currentRowWidth += (currentRowSegments.isEmpty() ? 0.0F : segmentSeparatorWidth) + ((NameTags.ItemNameSegment)statusSegment).width();
            currentRowSegments.add(statusSegment);
        }

        if (!currentRowSegments.isEmpty()) {
            layoutRows.add(new NameTags.NameTagLayoutRow(List.copyOf(currentRowSegments), currentRowWidth));
        }

        float rowHeight = getScaledLineHeight(4.8F) + 1.5F;
        float widestRowWidth = 0.0F;

        for (Object layoutRow : layoutRows) {
            widestRowWidth = Math.max(widestRowWidth, ((NameTags.NameTagLayoutRow)layoutRow).yOffset());
        }

        widestRowWidth += 7.0F;
        float layoutHeight = 4.0F + layoutRows.size() * rowHeight - 1.5F;
        NameTags.PotionStatusLayout potionLayout = new NameTags.PotionStatusLayout(List.copyOf(layoutRows), widestRowWidth, layoutHeight);
        this.potionStatusLayoutCache.put(playerEntity, potionLayout);
        return potionLayout;
    }
// https://t.me/pasterplace

    public void renderPlayerTagPreview(DrawContext drawContext, PlayerEntity playerEntity, float tagCenterX, float tagY, float previewScale) {
        float scale = this.nameTagScaleSetting.getValue() * previewScale;
        this.potionStatusLayoutCache.clear();
        NameTags.EntityRenderMetrics metrics = new NameTags.EntityRenderMetrics(
        playerEntity, tagCenterX, this.getPlayerTagY(playerEntity, tagY, scale), 0.0F, 0.0F, false, scale
        );
        this.renderEntityTag(drawContext, metrics, true);
        this.renderEntityTag(drawContext, metrics, false);
        GuiEventRenderer.flushAndStartRootLayer(drawContext);
        this.renderEquipmentItems(drawContext, playerEntity, metrics.width(), this.computePotionLayoutY(playerEntity, metrics), metrics.scale());
    }
// https://t.me/pasterplace

    private float getPlayerTagY(PlayerEntity playerEntity, float baseTagY, float scale) {
        NameTags.PotionStatusLayout potionLayout = this.getPotionStatusLayout(playerEntity);
        return potionLayout.rows().isEmpty() ? baseTagY : Math.max(15.0F, baseTagY - (potionLayout.height() + 2.0F) * scale);
    }
// https://t.me/pasterplace

    private void renderPotionStatusLines(DrawContext drawContext, PlayerEntity playerEntity, float tagCenterX, float tagY, float scale, boolean backgroundPass) {
        NameTags.PotionStatusLayout potionLayout = this.getPotionStatusLayout(playerEntity);
        if (!potionLayout.rows().isEmpty()) {
            float lineAdvance = getScaledLineHeight(4.8F) + 1.5F;
            float firstRowY = tagY + 2.0F * scale;
            float rowCenterX = tagCenterX - potionLayout.width() / 2.0F;
            pushScaleTransform(drawContext, tagCenterX, firstRowY, scale);
            if (backgroundPass) {
                drawBrokkeIcon(drawContext, rowCenterX, firstRowY, potionLayout.width(), potionLayout.height(), 3.5F, 1.0F);
                popScaleTransform(drawContext);
            } else {
                float separatorWidth = measureTextWidth(" | ", 4.8F);

                for (int rowIndex = 0; rowIndex < potionLayout.rows().size(); rowIndex++) {
                    Object layoutRow = potionLayout.rows().get(rowIndex);
                    float segmentX = tagCenterX - ((NameTags.NameTagLayoutRow)layoutRow).yOffset() / 2.0F;
                    float rowY = firstRowY + 2.0F + rowIndex * lineAdvance;

                    for (int segmentIndex = 0; segmentIndex < ((NameTags.NameTagLayoutRow)layoutRow).segments().size(); segmentIndex++) {
                        if (segmentIndex > 0) {
                            drawText(drawContext, NAME_TAG_FONT, " | ", segmentX, rowY, 4.8F, getSubtleLabelColor());
                            segmentX += separatorWidth;
                        }

                        Object segment = ((NameTags.NameTagLayoutRow)layoutRow).segments().get(segmentIndex);
                        drawText(
                        drawContext,
                        NAME_TAG_FONT,
                        ((NameTags.ItemNameSegment)segment).text(),
                        segmentX,
                        rowY,
                        4.8F,
                        ((NameTags.ItemNameSegment)segment).colorArgb()
                        );
                        segmentX += ((NameTags.ItemNameSegment)segment).width();
                    }
                }

                popScaleTransform(drawContext);
            }
        }
    }
// https://t.me/pasterplace

    private void appendPotionStatusSegment(
    List<NameTags.ItemNameSegment> items, PotionStatus status, String effectName, boolean showAmplifier, int textColorArgb
    ) {
        if (status != null && (status.infinite() || status.remainingTicks() > 0)) {
            String durationText;
            if (status.infinite()) {
                durationText = "∞";
            } else {
                int durationSeconds = status.remainingTicks() / 20 + (status.remainingTicks() % 20 == 0 ? 0 : 1);
                durationText = durationSeconds / 60 + ":" + (durationSeconds % 60 < 10 ? "0" : "") + durationSeconds % 60;
            }

            String displayText = effectName + (showAmplifier ? " " + toRomanNumeral(status.amplifier() + 1) : "") + " " + (status.approximate() ? "~" : "") + durationText;
            items.add(new NameTags.ItemNameSegment(displayText, textColorArgb, measureTextWidth(displayText, 4.8F)));
        }
    }
// https://t.me/pasterplace

    private void renderLivingEntityTag(DrawContext drawContext, LivingEntity livingEntity, float tagCenterX, float tagY, float scale, boolean backgroundPass) {
        String displayName = livingEntity.getDisplayName().getString();
        String healthText = "[ " + (int)Math.ceil(livingEntity.getHealth() + livingEntity.getAbsorptionAmount()) + " ]";
        this.renderTwoLineTag(drawContext, displayName, healthText, getPrimaryAccentArgb(), tagCenterX, tagY, scale, backgroundPass);
    }
// https://t.me/pasterplace

    private void renderTntTag(DrawContext drawContext, TntEntity tntEntity, float tagCenterX, float tagY, float scale, boolean backgroundPass) {
        String displayName = tntEntity.getDisplayName().getString();
        float fuseSeconds = Math.max(0.0F, (tntEntity.getFuse() - WorldToScreenProjection.getTickDelta()) / 20.0F);
        String fuseTimeText = String.format(Locale.US, "%.1f", fuseSeconds) + "с";
        int fuseTimeColor = fuseSeconds < 1.0F ? -48060 : (fuseSeconds < 2.5F ? -22016 : -11141291);
        this.renderTwoLineTag(drawContext, displayName, fuseTimeText, fuseTimeColor, tagCenterX, tagY, scale, backgroundPass);
    }
// https://t.me/pasterplace

    private void renderTwoLineTag(DrawContext drawContext, String title, String subtitle, int subtitleColorArgb, float tagCenterX, float tagY, float scale, boolean backgroundPass) {
        float titleWidth = measureTextWidth(title, 5.8F);
        float subtitleWidth = measureTextWidth(subtitle, 5.8F);
        float badgeGap = 2.0F;
        float tagWidth = 4.0F + titleWidth + 3.5F + badgeGap + 3.5F + subtitleWidth + 4.0F;
        float tagHeight = 11.8F;
        float tagLeftX = tagCenterX - tagWidth / 2.0F;
        float tagTopY = tagY - tagHeight;
        pushScaleTransform(drawContext, tagCenterX, tagY, scale);
        if (backgroundPass) {
            drawBrokkeIcon(drawContext, tagLeftX, tagTopY, tagWidth, tagHeight, 3.5F, 1.0F);
            popScaleTransform(drawContext);
        } else {
            float textY = tagTopY + (tagHeight - getScaledLineHeight(5.8F)) / 2.0F;
            float titleX = tagLeftX + 4.0F;
            float badgeX = titleX + titleWidth + 3.5F;
            drawAvatarFallback(drawContext, badgeX, tagTopY + (tagHeight - 2.0F) / 2.0F, 2.0F);
            drawText(drawContext, NAME_TAG_FONT, title, titleX, textY, 5.8F, getMutedLabelArgb());
            drawText(drawContext, NAME_TAG_FONT, subtitle, badgeX + badgeGap + 3.5F, textY, 5.8F, subtitleColorArgb);
            popScaleTransform(drawContext);
        }
    }
// https://t.me/pasterplace

    private void renderItemTag(DrawContext drawContext, ItemEntity itemEntity, float tagCenterX, float tagY, float scale, boolean backgroundPass) {
        ItemStack droppedItemStack = itemEntity.getStack();
        String itemDisplayName = getCachedItemDisplayName(droppedItemStack);
        int itemCount = droppedItemStack.getCount();
        String countText = itemCount > 1 ? String.valueOf(itemCount) : "";
        boolean showItemName = this.drawItemNamesSetting.getValue();
        boolean showCount = !countText.isEmpty();
        boolean showBadge = showCount || showItemName;
        float nameColumnWidth = showItemName ? 11.0F : 0.0F;
        float countTextWidth = showCount ? measureTextWidth(countText, 5.8F) + 3.0F : 0.0F;
        float badgeGap = showBadge ? 6.0F : 0.0F;
        float itemNameWidth = measureTextWidth(itemDisplayName, 5.8F);
        float tagWidth = 4.0F + nameColumnWidth + countTextWidth + badgeGap + itemNameWidth + 4.0F;
        float tagHeight = 11.8F;
        float tagLeftX = tagCenterX - tagWidth / 2.0F;
        float tagTopY = tagY - tagHeight;
        pushScaleTransform(drawContext, tagCenterX, tagY, scale);
        if (backgroundPass) {
            drawBrokkeIcon(drawContext, tagLeftX, tagTopY, tagWidth, tagHeight, 3.5F, 1.0F);
            popScaleTransform(drawContext);
        } else {
            float textY = tagTopY + (tagHeight - getScaledLineHeight(5.8F)) / 2.0F;
            float textX = tagLeftX + 4.0F + nameColumnWidth;
            if (showBadge) {
                drawAvatarFallback(drawContext, textX + (showCount ? countTextWidth : 0.0F), tagTopY + (tagHeight - 2.0F) / 2.0F, 2.0F);
            }

            if (showCount) {
                drawText(drawContext, NAME_TAG_FONT, countText, textX, textY, 5.8F, getPrimaryAccentArgb());
            }

            textX += (showCount ? countTextWidth : 0.0F) + badgeGap;
            drawText(drawContext, NAME_TAG_FONT, itemDisplayName, textX, textY, 5.8F, getMutedLabelArgb());
            popScaleTransform(drawContext);
        }
    }
// https://t.me/pasterplace

    private void renderItemIconTag(DrawContext drawContext, ItemEntity itemEntity, float tagCenterX, float tagY, float scale) {
        ItemStack droppedItemStack = itemEntity.getStack();
        if (!droppedItemStack.isEmpty()) {
            String itemDisplayName = getCachedItemDisplayName(droppedItemStack);
            int itemCount = droppedItemStack.getCount();
            String countText = itemCount > 1 ? String.valueOf(itemCount) : "";
            float itemNameWidth = measureTextWidth(itemDisplayName, 5.8F);
            float countColumnWidth = countText.isEmpty() ? 0.0F : measureTextWidth(countText, 5.8F) + 3.0F;
            float tagWidth = 12.0F + countColumnWidth + 3.0F + 3.0F + 3.0F + itemNameWidth + 4.0F;
            float tagHeight = 11.8F;
            float tagLeftX = tagCenterX - tagWidth / 2.0F;
            float tagTopY = tagY - tagHeight;
            pushScaleTransform(drawContext, tagCenterX, tagY, scale);
            drawItemStack(drawContext, droppedItemStack, tagLeftX + 4.0F + 4.0F, tagTopY + tagHeight / 2.0F, 8.0F);
            popScaleTransform(drawContext);
        }
    }
// https://t.me/pasterplace

    private static List<ItemStack> getPlayerEquipmentItems(PlayerEntity playerEntity) {
        ArrayList equipmentItems = new ArrayList(6);
        ItemStack offHandStack = playerEntity.getOffHandStack();
        if (!offHandStack.isEmpty()) {
            equipmentItems.add(offHandStack);
        }

        for (EquipmentSlot equipmentSlot : ARMOR_SLOTS) {
            ItemStack armorStack = playerEntity.getEquippedStack(equipmentSlot);
            if (!armorStack.isEmpty()) {
                equipmentItems.add(armorStack);
            }
        }

        ItemStack mainHandStack = playerEntity.getMainHandStack();
        if (!mainHandStack.isEmpty()) {
            equipmentItems.add(mainHandStack);
        }

        return equipmentItems;
    }
// https://t.me/pasterplace

    private void renderEquipmentSlotBackplates(DrawContext drawContext, PlayerEntity playerEntity, float centerX, float topY, float scale) {
        int equipmentCount = getPlayerEquipmentItems(playerEntity).size();
        if (equipmentCount != 0) {
            float slotSize = 10.5F * scale;
            float slotGap = 1.0F * scale;
            float firstSlotX = centerX - (equipmentCount * slotSize + (equipmentCount - 1) * slotGap) / 2.0F;

            for (int slotIndex = 0; slotIndex < equipmentCount; slotIndex++) {
                drawBrokkeIcon(drawContext, firstSlotX + slotIndex * (slotSize + slotGap), topY, slotSize, slotSize, 2.5F * scale, 1.0F);
            }
        }
    }
// https://t.me/pasterplace

    private void renderEquipmentItems(DrawContext drawContext, PlayerEntity playerEntity, float centerX, float topY, float scale) {
        List items = getPlayerEquipmentItems(playerEntity);
        int equipmentCount = items.size();
        if (equipmentCount != 0) {
            float slotSize = 10.5F * scale;
            float slotGap = 1.0F * scale;
            float firstSlotX = centerX - (equipmentCount * slotSize + (equipmentCount - 1) * slotGap) / 2.0F;

            for (int slotIndex = 0; slotIndex < equipmentCount; slotIndex++) {
                Object equipmentStack = (ItemStack)items.get(equipmentCount - 1 - slotIndex);
                float slotCenterX = firstSlotX + slotIndex * (slotSize + slotGap);
                drawItemStack(drawContext, (ItemStack)equipmentStack, slotCenterX + slotSize / 2.0F, topY + slotSize / 2.0F, 7.14F * scale);
            }
        }
    }
// https://t.me/pasterplace

    private List<ItemStack> getHeldItems(PlayerEntity playerEntity) {
        ArrayList heldItems = new ArrayList(2);
        if (!playerEntity.getMainHandStack().isEmpty()) {
            heldItems.add(playerEntity.getMainHandStack());
        }

        if (!playerEntity.getOffHandStack().isEmpty()) {
            heldItems.add(playerEntity.getOffHandStack());
        }

        return heldItems;
    }
// https://t.me/pasterplace

    private void renderHeldItemNames(DrawContext drawContext, PlayerEntity playerEntity, float centerX, float baseY, float scale, boolean backgroundPass) {
        List heldItems = this.getHeldItems(playerEntity);
        if (!heldItems.isEmpty()) {
            ArrayList itemNames = new ArrayList();
            float widestNameWidth = 0.0F;

            for (Object heldStack : heldItems) {
                String itemName = getCachedItemDisplayName((ItemStack)heldStack);
                itemNames.add(itemName);
                widestNameWidth = Math.max(widestNameWidth, 10.0F + measureTextWidth(itemName, 4.8F));
            }

            float rowHeight = Math.max(7.0F, getScaledLineHeight(4.8F));
            float panelHeight = itemNames.size() * rowHeight + (itemNames.size() - 1) * 1.5F;
            float panelWidth = 7.0F + widestNameWidth;
            float panelContentHeight = 4.0F + panelHeight;
            float panelLeftX = centerX - panelWidth / 2.0F;
            float panelTopY = baseY + 12.0F;
            pushScaleTransform(drawContext, centerX, panelTopY, scale);
            if (backgroundPass) {
                drawBrokkeIcon(drawContext, panelLeftX, panelTopY, panelWidth, panelContentHeight, 3.5F, 1.0F);
                popScaleTransform(drawContext);
            } else {
                for (int rowIndex = 0; rowIndex < itemNames.size(); rowIndex++) {
                    Object itemName = (String)itemNames.get(rowIndex);
                    float itemNameWidth = measureTextWidth((String)itemName, 4.8F);
                    float itemNameX = panelLeftX + (panelWidth - (10.0F + itemNameWidth)) / 2.0F;
                    float rowY = panelTopY + 2.0F + rowIndex * (rowHeight + 1.5F);
                    drawText(
                    drawContext, NAME_TAG_FONT, (String)itemName, itemNameX + 7.0F + 3.0F, rowY + (rowHeight - getScaledLineHeight(4.8F)) / 2.0F, 4.8F, getMutedLabelArgb()
                    );
                }

                popScaleTransform(drawContext);
            }
        }
    }
// https://t.me/pasterplace

    private void renderHeldItemIcons(DrawContext drawContext, PlayerEntity playerEntity, float centerX, float baseY, float scale) {
        List heldItems = this.getHeldItems(playerEntity);
        if (!heldItems.isEmpty()) {
            ArrayList itemNames = new ArrayList();
            float widestNameWidth = 0.0F;

            for (Object heldStack : heldItems) {
                String itemName = getCachedItemDisplayName((ItemStack)heldStack);
                itemNames.add(itemName);
                widestNameWidth = Math.max(widestNameWidth, 10.0F + measureTextWidth(itemName, 4.8F));
            }

            float rowHeight = Math.max(7.0F, getScaledLineHeight(4.8F));
            float panelWidth = 7.0F + widestNameWidth;
            float panelLeftX = centerX - panelWidth / 2.0F;
            float panelTopY = baseY + 12.0F;
            pushScaleTransform(drawContext, centerX, panelTopY, scale);

            for (int itemIndex = 0; itemIndex < heldItems.size(); itemIndex++) {
                float itemNameWidth = measureTextWidth((String)itemNames.get(itemIndex), 4.8F);
                float itemX = panelLeftX + (panelWidth - (10.0F + itemNameWidth)) / 2.0F;
                float itemY = panelTopY + 2.0F + itemIndex * (rowHeight + 1.5F);
                drawItemStack(drawContext, (ItemStack)heldItems.get(itemIndex), itemX + 3.5F, itemY + rowHeight / 2.0F, 7.0F);
            }

            popScaleTransform(drawContext);
        }
    }
// https://t.me/pasterplace

    private NameTags.FormattedName getCachedFormattedName(PlayerEntity playerEntity) {
        UUID playerUuid = playerEntity.getUuid();
        long nowMillis = System.currentTimeMillis();
        Object cachedNameEntry = formattedNameCache.get(playerUuid);
        if (cachedNameEntry != null && nowMillis - ((NameTags.CachedFormattedName)cachedNameEntry).cachedAtMillis() < 1200L) {
            return ((NameTags.CachedFormattedName)cachedNameEntry).formattedName();
        }

        NameTags.FormattedName formattedName = this.resolveFormattedPlayerRank(playerEntity);
        formattedNameCache.put(playerUuid, new NameTags.CachedFormattedName(formattedName, nowMillis));
        return formattedName;
    }
// https://t.me/pasterplace

    private NameTags.FormattedName resolveFormattedPlayerRank(PlayerEntity player) {
        Team team = player.getScoreboardTeam();
        PlayerListEntry playerListEntry = CLIENT.getNetworkHandler() != null ? CLIENT.getNetworkHandler().getPlayerListEntry(player.getUuid()) : null;
        if (team == null && playerListEntry != null) {
            team = playerListEntry.getScoreboardTeam();
        }

        Cerberus.PlayerRank parsedRank = Cerberus.getPlayerRank(team != null ? team.getPrefix() : null);
        if (parsedRank == null && playerListEntry != null) {
            parsedRank = Cerberus.getPlayerRank(playerListEntry.getDisplayName());
        }

        if (parsedRank == null) {
            parsedRank = Cerberus.getPlayerRank(player.getDisplayName());
        }

        if (parsedRank != null) {
            String rankName = parsedRank.rankName().toUpperCase();
            return !rankName.equals("PLAYER") && !rankName.equals("ИГРОК") ? new NameTags.FormattedName(rankName, parsedRank.colorArgb()) : null;
        }

        String rankPrefix = null;
        Integer rankColor = null;
        if (team != null) {
            Text teamPrefix = team.getPrefix();
            if (teamPrefix != null && !teamPrefix.getString().isBlank()) {
                rankPrefix = teamPrefix.getString();
                rankColor = getTextFormattingColor(teamPrefix);
            }

            if (rankColor == null && team.getColor().getColorValue() != null) {
                rankColor = team.getColor().getColorValue();
            }
        }

        if ((rankPrefix == null || rankPrefix.isBlank()) && playerListEntry != null && playerListEntry.getDisplayName() != null) {
            Text listDisplayName = playerListEntry.getDisplayName();
            String listDisplayText = listDisplayName.getString();
            int playerNameOffset = listDisplayText.indexOf(player.getName().getString());
            if (playerNameOffset > 0) {
                rankPrefix = listDisplayText.substring(0, playerNameOffset);
                rankColor = getTextFormattingColor(listDisplayName);
            }
        }

        if (rankPrefix != null && !rankPrefix.isBlank()) {
            if (rankColor == null) {
                rankColor = getLegacyFormattingColor(rankPrefix);
            }

            String normalizedRankName = normalizeRankText(rankPrefix);
            if (!normalizedRankName.isEmpty() && !normalizedRankName.equalsIgnoreCase("player") && !normalizedRankName.equalsIgnoreCase("игрок")) {
                if (rankColor == null || rankColor == 16777215) {
                    rankColor = getRankColor(normalizedRankName);
                }

                return new NameTags.FormattedName(normalizedRankName, 0xFF000000 | rankColor & 16777215);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
// https://t.me/pasterplace

    private static Integer getTextFormattingColor(Text componentText) {
        Optional optional = componentText.visit(
        (textStyle, visitedText) -> visitedText != null && !visitedText.isBlank() && textStyle.getColor() != null
        ? Optional.of(textStyle.getColor().getRgb())
        : Optional.empty(),
        Style.EMPTY
        );
        return (Integer)optional.orElse(null);
    }
// https://t.me/pasterplace

    private static Integer getLegacyFormattingColor(String formattedText) {
        if (formattedText == null) {
            return null;
        }

        for (int formatCodeIndex = 0; formatCodeIndex < formattedText.length() - 1; formatCodeIndex++) {
            if (formattedText.charAt(formatCodeIndex) == 167 || formattedText.charAt(formatCodeIndex) == '&') {
                int formatColorArgb = getFormattingCodeArgb(Character.toLowerCase(formattedText.charAt(formatCodeIndex + 1)));
                if (formatColorArgb != -1 && formatColorArgb != 0) {
                    return formatColorArgb & 16777215;
                }
            }
        }

        return null;
    }
// https://t.me/pasterplace

    private static String normalizeRankText(String rankText) {
        if (rankText == null) {
            return "";
        }

        rankText = normalizeUnicodeLookalikes(rankText);
        rankText = LEGACY_COLOR_CODE_PATTERN.matcher(rankText).replaceAll("");
        rankText = PRIVATE_USE_CHARACTER_PATTERN.matcher(rankText).replaceAll("");
        rankText = ITEM_DECORATION_PATTERN.matcher(rankText).replaceAll(" ");
        rankText = WHITESPACE_PATTERN.matcher(rankText.trim()).replaceAll(" ");
        return rankText.toUpperCase();
    }
// https://t.me/pasterplace

    private static int getRankColor(String rankName) {
        String normalizedRankName = rankName.toUpperCase();
        if (normalizedRankName.contains("СТАЖ") || normalizedRankName.contains("STAZ") || normalizedRankName.contains("HELP") || normalizedRankName.contains("ХЕЛП")) {
            return -16718337;
        } else if (normalizedRankName.contains("ETERNITY") || normalizedRankName.contains("ЭТЕРНИТИ")) {
            return -43521;
        } else if (normalizedRankName.contains("STINGER") || normalizedRankName.contains("СТИНГЕР")) {
            return -37312;
        } else if (normalizedRankName.contains("DRAGON") || normalizedRankName.contains("ДРАГОН")) {
            return -4560696;
        } else if (normalizedRankName.contains("KRAKEN") || normalizedRankName.contains("КРАКЕН")) {
            return -16718218;
        } else if (normalizedRankName.contains("WITHER") || normalizedRankName.contains("ВИЗЕР")) {
            return -28416;
        } else if (normalizedRankName.contains("GHAST") || normalizedRankName.contains("ГАСТ")) {
            return -16718337;
        } else if (normalizedRankName.contains("MUSTANG") || normalizedRankName.contains("МУСТАНГ")) {
            return -10752;
        } else if (normalizedRankName.contains("GRIEF") || normalizedRankName.contains("ГРИФ") || normalizedRankName.contains("GRIF")) {
            return -59580;
        } else if (normalizedRankName.contains("MAGMA") || normalizedRankName.contains("МАГМА")) {
            return -35072;
        } else if (normalizedRankName.contains("TITAN") || normalizedRankName.contains("ТИТАН")) {
            return -13395457;
        } else if (normalizedRankName.contains("HERO") || normalizedRankName.contains("ГЕРОЙ")) {
            return -11141291;
        } else if (normalizedRankName.contains("VIP") || normalizedRankName.contains("ВИП")) {
            return -11141291;
        } else if (normalizedRankName.contains("PREMIUM") || normalizedRankName.contains("ПРЕМИУМ")) {
            return -16718337;
        } else if (normalizedRankName.contains("DELUXE") || normalizedRankName.contains("ДЕЛЮКС")) {
            return -43521;
        } else if (normalizedRankName.contains("SPONSOR") || normalizedRankName.contains("СПОНСОР")) {
            return -10752;
        } else {
            return !normalizedRankName.contains("ADMIN") && !normalizedRankName.contains("АДМИН") && !normalizedRankName.contains("MODER") && !normalizedRankName.contains("МОДЕР") ? -1 : -52429;
        }
    }
// https://t.me/pasterplace

    private static String toRomanNumeral(int level) {
        return switch (level) {
        case 1 -> "I";
        case 2 -> "II";
        case 3 -> "III";
        case 4 -> "IV";
            default -> String.valueOf(level);
        };
    }
// https://t.me/pasterplace

    private static String getCachedItemDisplayName(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            Object cachedDisplayName = itemDisplayNameCache.get(stack);
            if (cachedDisplayName != null) {
                return (String)cachedDisplayName;
            }

            String displayName = getFriendlyItemDisplayName(stack);
            itemDisplayNameCache.put(stack, displayName);
            return displayName;
        } else {
            return "";
        }
    }
// https://t.me/pasterplace

    private static String getDefaultItemName(ItemStack stack) {
        return stack.getItem().getDefaultStack().getName().getString();
    }
// https://t.me/pasterplace

    private static String getCustomItemName(ItemStack stack) {
        Text customNameComponent = (Text)stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customNameComponent == null) {
            customNameComponent = (Text)stack.get(DataComponentTypes.ITEM_NAME);
        }

        String customName = customNameComponent != null ? customNameComponent.getString() : "";
        if (customName.isEmpty()) {
            String fallbackName = stack.getName().getString();
            if (!fallbackName.equalsIgnoreCase(getDefaultItemName(stack))) {
                customName = fallbackName;
            }
        }

        return customName;
    }
// https://t.me/pasterplace

    private static List<String> getItemLoreLines(ItemStack stack) {
        ArrayList loreLines = new ArrayList();
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {
            for (Text line : lore.lines()) {
                String loreLine = sanitizeDisplayText(line.getString());
                if (!loreLine.isEmpty()) {
                    loreLines.add(loreLine);
                }
            }
        }

        return loreLines;
    }
// https://t.me/pasterplace

    private static String getItemCustomDataString(ItemStack stack) {
        Object customDataComponent = (NbtComponent)stack.get(DataComponentTypes.CUSTOM_DATA);
        return customDataComponent != null ? customDataComponent.toString() : null;
    }
// https://t.me/pasterplace

    private static String getFriendlyItemDisplayName(ItemStack stack) {
        if (stack.isOf(Items.PLAYER_HEAD)) {
            return getSpecialOrbDisplayName(stack);
        }

        if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
            String specialItemName = inferSpecialItemName(stack);
            return specialItemName != null ? specialItemName : "Талисман";
        }

        String itemName = null;
        String cleanCustomName = sanitizeDisplayText(getCustomItemName(stack));
        if (!cleanCustomName.isEmpty() && !cleanCustomName.equalsIgnoreCase(getDefaultItemName(stack))) {
            itemName = cleanCustomName;
        }

        if (itemName == null) {
            itemName = getDefaultItemName(stack);
        }

        if (isSwordItem(stack)) {
            int sharpnessLevel = getStackSharpnessLevel(stack);
            if (sharpnessLevel > 0) {
                String normalizedItemName = itemName.toLowerCase();
                if (!normalizedItemName.contains("остр") && !normalizedItemName.contains("sharpness")) {
                    itemName = itemName + " (Остр " + sharpnessLevel + ")";
                }
            }
        }

        return itemName;
    }
// https://t.me/pasterplace

    private static String inferSpecialItemName(ItemStack stack) {
        String cleanCustomName = sanitizeDisplayText(getCustomItemName(stack));
        List<String> loreLines = getItemLoreLines(stack);
        String detectedTalismanName = findTalismanName(cleanCustomName);
        if (detectedTalismanName != null) {
            return detectedTalismanName;
        }

        for (Object loreLine : loreLines) {
            detectedTalismanName = findTalismanName((String)loreLine);
            if (detectedTalismanName != null) {
                return detectedTalismanName;
            }
        }

        String customDataText = getItemCustomDataString(stack);
        String componentDataText = "";

        try {
            componentDataText = stack.getComponents().toString();
        } catch (Exception ignoredComponentReadException) {
        }

        String combinedMetadataText = (cleanCustomName + " " + String.join(" ", loreLines) + " " + (customDataText != null ? customDataText : "") + " " + componentDataText).toLowerCase(Locale.ROOT);
        String metadataTalismanName = findTalismanName(combinedMetadataText);
        if (metadataTalismanName != null) {
            return metadataTalismanName;
        }

        Identifier itemModelId = (Identifier)stack.get(DataComponentTypes.ITEM_MODEL);
        if (itemModelId != null) {
            String itemModelPath = itemModelId.getPath().toLowerCase(Locale.ROOT);
            String modelPathTalismanName = findTalismanName(itemModelPath);
            if (modelPathTalismanName != null) {
                return modelPathTalismanName;
            }

            int modelPathId = parseFirstInteger(itemModelPath);
            if (modelPathId > 0 && getTalismanNameFromModelId(modelPathId) != null) {
                return getTalismanNameFromModelId(modelPathId);
            }
        }

        int customModelDataId = getCustomModelDataId(stack);
        if (customModelDataId > 0 && getTalismanNameFromModelId(customModelDataId) != null) {
            return getTalismanNameFromModelId(customModelDataId);
        }

        String loreTalismanName = inferTalismanNameFromLore(loreLines);
        if (loreTalismanName != null) {
            return loreTalismanName;
        }

        Object attributeModifiers = (AttributeModifiersComponent)stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            String modifierSummary = attributeModifiers.toString().toLowerCase(Locale.ROOT);
            boolean hasHealthModifier = modifierSummary.contains("max_health") || modifierSummary.contains("health");
            boolean hasDamageModifier = modifierSummary.contains("attack_damage") || modifierSummary.contains("damage");
            boolean hasSpeedModifier = modifierSummary.contains("movement_speed") || modifierSummary.contains("speed");
            boolean hasArmorModifier = modifierSummary.contains("armor");
            if (hasHealthModifier && (hasDamageModifier || hasSpeedModifier || hasArmorModifier)) {
                return "Талисман INFINITY";
            }

            if (hasDamageModifier && hasSpeedModifier && hasArmorModifier) {
                return "Талисман ETERNITY";
            }

            if (hasDamageModifier && hasSpeedModifier) {
                return "Талисман STINGER";
            }
        }
// https://t.me/pasterplace

        return null;
    }
// https://t.me/pasterplace

    private static String inferTalismanNameFromLore(List<String> loreLines) {
        if (loreLines.isEmpty()) {
            return null;
        }

        boolean hasHealthBonus = false;
        boolean hasArmorBonus = false;
        boolean hasDamageBonus = false;
        boolean hasSpeedBonus = false;

        for (Object item : loreLines) {
            String loreLine = (String)item;
            String normalizedLoreLine = loreLine.toLowerCase();
            if (normalizedLoreLine.contains("здоровь") || normalizedLoreLine.contains("health")) {
                hasHealthBonus = true;
            }

            if (normalizedLoreLine.contains("броня") || normalizedLoreLine.contains("защит") || normalizedLoreLine.contains("armor")) {
                hasArmorBonus = true;
            }

            if (normalizedLoreLine.contains("урон") || normalizedLoreLine.contains("damage")) {
                hasDamageBonus = true;
            }

            if (normalizedLoreLine.contains("скорост") || normalizedLoreLine.contains("speed")) {
                hasSpeedBonus = true;
            }
        }

        if (hasHealthBonus && hasArmorBonus && hasDamageBonus && hasSpeedBonus) {
            return "Талисман INFINITY";
        }
// https://t.me/pasterplace

        if (hasDamageBonus && hasArmorBonus && hasSpeedBonus) {
            for (String loreLine : loreLines) {
                String normalizedLoreLine = loreLine.toLowerCase();
                if (normalizedLoreLine.contains("скорость i") && !normalizedLoreLine.contains("скорость ii")) {
                    return "Талисман STINGER";
                }
            }

            return "Талисман ETERNITY";
        } else if (hasHealthBonus && hasArmorBonus) {
            return "Талисман Титана";
        } else if (hasSpeedBonus && hasDamageBonus) {
            return "Талисман STINGER";
        } else {
            return hasSpeedBonus && hasArmorBonus ? "Талисман Сатиры" : null;
        }
    }
// https://t.me/pasterplace

    private static String getTalismanNameFromModelId(int modelId) {
        return switch (modelId) {
        case 1 -> "Талисман Мрака";
        case 2 -> "Талисман Вихря";
        case 3 -> "Талисман Демона";
        case 4 -> "Талисман Раздора";
        case 5 -> "Талисман Ярости";
        case 6 -> "Талисман Крушителя";
        case 7 -> "Талисман Карателя";
        case 8 -> "Талисман Тирана";
        case 9 -> "Талисман Гринча";
        case 10 -> "Талисман Ареса";
        case 11 -> "Талисман Хаоса";
        case 12 -> "Талисман Титана";
        case 13 -> "Талисман Гидры";
        case 14 -> "Талисман Бестии";
        case 15 -> "Талисман Икара";
        case 16 -> "Талисман Сатиры";
        case 17 -> "Талисман Эриды";
        case 18 -> "Талисман Мороза";
        case 19 -> "Талисман Гармонии";
        case 20 -> "Талисман Дедала";
        case 21 -> "Талисман Ехидна";
        case 22 -> "Талисман Грани";
        case 23 -> "Талисман Тритона";
        case 24 -> "Талисман Феникс";
        case 25 -> "Талисман INFINITY";
        case 26 -> "Талисман ETERNITY";
        case 27 -> "Талисман STINGER";
            default -> null;
        };
    }
// https://t.me/pasterplace

    private static String findTalismanName(String candidateText) {
        if (candidateText != null && !candidateText.isBlank()) {
            String lowercaseText = candidateText.toLowerCase(Locale.ROOT);
            if (lowercaseText.contains("infinity") || lowercaseText.contains("инфинити")) {
                return "Талисман INFINITY";
            } else if (lowercaseText.contains("eternity") || lowercaseText.contains("этернити") || lowercaseText.contains("вечност")) {
                return "Талисман ETERNITY";
            } else if (lowercaseText.contains("stinger") || lowercaseText.contains("стингер")) {
                return "Талисман STINGER";
            } else if (lowercaseText.contains("карател") || lowercaseText.contains("karatel")) {
                return "Талисман Карателя";
            } else if (lowercaseText.contains("крушител") || lowercaseText.contains("krush")) {
                return "Талисман Крушителя";
            } else if (lowercaseText.contains("ярост") || lowercaseText.contains("yarost") || lowercaseText.contains("daamky")) {
                return "Талисман Ярости";
            } else if (lowercaseText.contains("раздор") || lowercaseText.contains("razdor")) {
                return "Талисман Раздора";
            } else if (lowercaseText.contains("демон") || lowercaseText.contains("demon")) {
                return "Талисман Демона";
            } else if (lowercaseText.contains("вихр") || lowercaseText.contains("vihr") || lowercaseText.contains("vortex")) {
                return "Талисман Вихря";
            } else if (lowercaseText.contains("мрак") || lowercaseText.contains("mrak")) {
                return "Талисман Мрака";
            } else if (lowercaseText.contains("тиран") || lowercaseText.contains("tiran")) {
                return "Талисман Тирана";
            } else if (lowercaseText.contains("гринч") || lowercaseText.contains("grinch")) {
                return "Талисман Гринча";
            } else if (lowercaseText.contains("арес") || lowercaseText.contains("ares")) {
                return "Талисман Ареса";
            } else if (lowercaseText.contains("хаос") || lowercaseText.contains("haos") || lowercaseText.contains("chaos")) {
                return "Талисман Хаоса";
            } else if (lowercaseText.contains("титан") || lowercaseText.contains("titan")) {
                return "Талисман Титана";
            } else if (lowercaseText.contains("гидр") || lowercaseText.contains("gidr") || lowercaseText.contains("hydra")) {
                return "Талисман Гидры";
            } else if (lowercaseText.contains("бести") || lowercaseText.contains("besti")) {
                return "Талисман Бестии";
            } else if (lowercaseText.contains("икар") || lowercaseText.contains("ikar")) {
                return "Талисман Икара";
            } else if (lowercaseText.contains("сатир") || lowercaseText.contains("satir")) {
                return "Талисман Сатиры";
            } else if (lowercaseText.contains("эрид") || lowercaseText.contains("erid")) {
                return "Талисман Эриды";
            } else if (lowercaseText.contains("мороз") || lowercaseText.contains("moroz")) {
                return "Талисман Мороза";
            } else if (lowercaseText.contains("гармон") || lowercaseText.contains("garmon")) {
                return "Талисман Гармонии";
            } else if (lowercaseText.contains("дедал") || lowercaseText.contains("dedal")) {
                return "Талисман Дедала";
            } else if (lowercaseText.contains("ехидн") || lowercaseText.contains("ehidn")) {
                return "Талисман Ехидна";
            } else if (lowercaseText.contains("грани") || lowercaseText.contains("grani")) {
                return "Талисман Грани";
            } else if (lowercaseText.contains("тритон") || lowercaseText.contains("triton")) {
                return "Талисман Тритона";
            } else if (lowercaseText.contains("феникс") || lowercaseText.contains("fenix") || lowercaseText.contains("phoenix")) {
                return "Талисман Феникс";
            } else if (lowercaseText.contains("мифическ") || lowercaseText.contains("mythical")) {
                return "Мифический талисман";
            } else if (lowercaseText.contains("легендарн") || lowercaseText.contains("legendary")) {
                return "Легендарный талисман";
            } else {
                return !lowercaseText.contains("эпическ") && !lowercaseText.contains("epic") ? null : "Эпический талисман";
            }
        } else {
            return null;
        }
    }
// https://t.me/pasterplace

    private static int getCustomModelDataId(ItemStack stack) {
        CustomModelDataComponent customModelData = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (customModelData != null) {
            if (!customModelData.floats().isEmpty()) {
                return customModelData.floats().getFirst().intValue();
            }

            if (!customModelData.strings().isEmpty()) {
                return parseFirstInteger(customModelData.strings().getFirst());
            }

            if (!customModelData.colors().isEmpty()) {
                return customModelData.colors().getFirst();
            }
        }

        return 0;
    }
// https://t.me/pasterplace

    private static int parseFirstInteger(String sourceText) {
        if (sourceText == null) {
            return 0;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(sourceText);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (Exception ignoredNumberFormatException) {
            }
        }

        return 0;
    }
// https://t.me/pasterplace

    private static String extractPlayerTextureHash(String profileData) {
        if (profileData != null && !profileData.isEmpty()) {
            Matcher matcher = PLAYER_TEXTURE_URL_PATTERN.matcher(profileData);
            if (matcher.find()) {
                return matcher.group(1);
            }

            Matcher matcher2 = BASE64_PROFILE_PROPERTY_PATTERN.matcher(profileData);
            if (matcher2.find()) {
                try {
                    String decodedProfileJson = new String(Base64.getDecoder().decode(matcher2.group(1)), StandardCharsets.UTF_8);
                    Matcher matcher3 = PLAYER_TEXTURE_URL_PATTERN.matcher(decodedProfileJson);
                    if (matcher3.find()) {
                        return matcher3.group(1);
                    }
                } catch (Exception ignoredBase64DecodeException) {
                }

                return matcher2.group(1);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
// https://t.me/pasterplace

    private static String getPlayerHeadTextureHash(ItemStack playerHeadStack) {
        ProfileComponent profileComponent = (ProfileComponent)playerHeadStack.get(DataComponentTypes.PROFILE);
        String textureHash = profileComponent != null ? extractPlayerTextureHash(profileComponent.getGameProfile().toString()) : null;
        if (textureHash == null) {
            try {
                textureHash = extractPlayerTextureHash(playerHeadStack.getComponents().toString());
            } catch (Exception ignoredComponentReadException) {
            }
        }

        if (textureHash == null) {
            textureHash = extractPlayerTextureHash(getItemCustomDataString(playerHeadStack));
        }
// https://t.me/pasterplace

        return textureHash;
    }
// https://t.me/pasterplace

    private static boolean isCerberusItem(ItemStack stack, List<String> loreLines, String customDataText) {
        String cerberusMarker = "ᴄᴇʀʙᴇʀ";
        String defaultNameLowercase = stack.getName().getString().toLowerCase();
        if (!defaultNameLowercase.contains("cerber") && !defaultNameLowercase.contains("цербер") && !defaultNameLowercase.contains(cerberusMarker)) {
            if (customDataText != null) {
                String customDataLowercase = customDataText.toLowerCase();
                if (customDataLowercase.contains("cerber") || customDataLowercase.contains("цербер") || customDataLowercase.contains(cerberusMarker)) {
                    return true;
                }
            }

            for (String loreLine : loreLines) {
                String loreLineLowercase = loreLine.toLowerCase();
                if (loreLineLowercase.contains("cerber") || loreLineLowercase.contains("цербер")) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }
// https://t.me/pasterplace

    private static int parseRomanOrNumericStrength(String levelText) {
        if (STRENGTH_IV_PATTERN.matcher(levelText).matches()) {
            return 4;
        } else if (STRENGTH_III_PATTERN.matcher(levelText).matches()) {
            return 3;
        } else {
            return STRENGTH_II_PATTERN.matcher(levelText).matches() ? 2 : 1;
        }
    }
// https://t.me/pasterplace

    private static int parseSerializedEnchantmentLevel(String serializedData, String enchantmentId) {
        if (serializedData == null) {
            return 0;
        }

        int enchantmentIdOffset = serializedData.indexOf(enchantmentId);
        if (enchantmentIdOffset == -1) {
            return 0;
        }

        int levelMarkerIndex = serializedData.indexOf("\"lvl\":", enchantmentIdOffset);
        if (levelMarkerIndex == -1) {
            levelMarkerIndex = serializedData.indexOf("lvl:", enchantmentIdOffset);
        }

        if (levelMarkerIndex != -1 && levelMarkerIndex - enchantmentIdOffset < 60) {
            int levelDigitsStart = levelMarkerIndex + (serializedData.charAt(levelMarkerIndex) == '"' ? 6 : 4);

            while (levelDigitsStart < serializedData.length() && !Character.isDigit(serializedData.charAt(levelDigitsStart))) {
                levelDigitsStart++;
            }

            int levelDigitsEnd = levelDigitsStart;

            while (levelDigitsEnd < serializedData.length() && Character.isDigit(serializedData.charAt(levelDigitsEnd))) {
                levelDigitsEnd++;
            }

            if (levelDigitsEnd > levelDigitsStart) {
                try {
                    return Integer.parseInt(serializedData.substring(levelDigitsStart, levelDigitsEnd));
                } catch (Exception ignoredNumberFormatException) {
                }
            }
        }

        return 0;
    }
// https://t.me/pasterplace

    private static int parseAttributeTier(String serializedAttributes, String attributeKey) {
        int attributeKeyOffset = serializedAttributes.indexOf(attributeKey);
        if (attributeKeyOffset == -1) {
            return 0;
        }

        int amountMarkerIndex = serializedAttributes.indexOf("amount=", attributeKeyOffset);
        if (amountMarkerIndex == -1) {
            return 0;
        }

        int amountDigitsStart = amountMarkerIndex + 7;
        int amountDigitsEnd = amountDigitsStart;

        while (amountDigitsEnd < serializedAttributes.length() && (Character.isDigit(serializedAttributes.charAt(amountDigitsEnd)) || serializedAttributes.charAt(amountDigitsEnd) == '.')) {
            amountDigitsEnd++;
        }

        if (amountDigitsEnd <= amountDigitsStart) {
            return 0;
        }

        try {
            double attributeAmount = Double.parseDouble(serializedAttributes.substring(amountDigitsStart, amountDigitsEnd));
            if (attributeKey.contains("speed")) {
                if (attributeAmount >= 0.25) {
                    return 3;
                }

                if (attributeAmount >= 0.15) {
                    return 2;
                }

                if (attributeAmount >= 0.05) {
                    return 1;
                }
            } else {
                if (attributeAmount >= 3.5) {
                    return 4;
                }

                if (attributeAmount >= 2.5) {
                    return 3;
                }
// https://t.me/pasterplace

                if (attributeAmount >= 1.5) {
                    return 2;
                }

                if (attributeAmount >= 0.5) {
                    return 1;
                }
            }
        } catch (Exception ignoredNumberFormatException) {
        }

        return 0;
    }
// https://t.me/pasterplace

    private static String getSpecialOrbDisplayName(ItemStack stack) {
        String textureHash = getPlayerHeadTextureHash(stack);
        String cleanItemName = sanitizeDisplayText(getCustomItemName(stack));
        List<String> loreLines = getItemLoreLines(stack);
        String customDataText = getItemCustomDataString(stack);
        int damageLevel = 0;
        int armorLevel = 0;
        int speedLevel = 0;
        boolean isCerberusOrb = false;
        ArrayList modifierCategories = new ArrayList();
        if (isCerberusItem(stack, loreLines, customDataText) || cleanItemName.toLowerCase().contains("цербер") || cleanItemName.toLowerCase().contains("cerber")) {
            isCerberusOrb = true;
            damageLevel = 4;
        }

        for (String loreLine : loreLines) {
            String lowercaseLoreLine = loreLine.toLowerCase();
            if (lowercaseLoreLine.contains("цербер") || lowercaseLoreLine.contains("cerber")) {
                isCerberusOrb = true;
                damageLevel = 4;
                addUniqueValue(modifierCategories, "damage");
            } else if (lowercaseLoreLine.contains("урон") || lowercaseLoreLine.contains("damage") || lowercaseLoreLine.contains("сила") || lowercaseLoreLine.contains("силы") || lowercaseLoreLine.contains("strength")) {
                int parsedStrength = parseRomanOrNumericStrength(lowercaseLoreLine);
                if (parsedStrength >= 4) {
                    isCerberusOrb = true;
                    damageLevel = 4;
                } else {
                    damageLevel = parsedStrength;
                }

                addUniqueValue(modifierCategories, "damage");
            } else if (lowercaseLoreLine.contains("броня")
            || lowercaseLoreLine.contains("брони")
            || lowercaseLoreLine.contains("защит")
            || lowercaseLoreLine.contains("armor")
            || lowercaseLoreLine.contains("protection")
            || lowercaseLoreLine.contains("сопротивлен")) {
                armorLevel = parseRomanOrNumericStrength(lowercaseLoreLine);
                addUniqueValue(modifierCategories, "armor");
            } else if (lowercaseLoreLine.contains("скорост") || lowercaseLoreLine.contains("speed")) {
                speedLevel = parseRomanOrNumericStrength(lowercaseLoreLine);
                addUniqueValue(modifierCategories, "speed");
            }
        }

        if (customDataText != null) {
            String lowercaseCustomData = customDataText.toLowerCase();
            if (!isCerberusOrb && (lowercaseCustomData.contains("cerber") || lowercaseCustomData.contains("цербер"))) {
                isCerberusOrb = true;
                damageLevel = 4;
                addUniqueValue(modifierCategories, "damage");
            }
// https://t.me/pasterplace

            if (damageLevel == 0) {
                int serializedDamageLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "hms-damage");
                if (serializedDamageLevel == 0) {
                    serializedDamageLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "damage");
                }

                if (serializedDamageLevel == 0) {
                    serializedDamageLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "sdamage");
                }

                if (serializedDamageLevel >= 4) {
                    isCerberusOrb = true;
                    damageLevel = 4;
                } else if (serializedDamageLevel > 0) {
                    damageLevel = serializedDamageLevel;
                }

                if (damageLevel > 0) {
                    addUniqueValue(modifierCategories, "damage");
                }
            }

            if (armorLevel == 0) {
                int serializedArmorLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "hms-armor");
                if (serializedArmorLevel == 0) {
                    serializedArmorLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "hms-protection");
                }

                if (serializedArmorLevel == 0) {
                    serializedArmorLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "armor");
                }

                if (serializedArmorLevel == 0) {
                    serializedArmorLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "sarmor");
                }

                if (serializedArmorLevel > 0) {
                    armorLevel = serializedArmorLevel;
                    addUniqueValue(modifierCategories, "armor");
                }
            }

            if (speedLevel == 0) {
                int serializedSpeedLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "hms-speed");
                if (serializedSpeedLevel == 0) {
                    serializedSpeedLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "speed");
                }

                if (serializedSpeedLevel == 0) {
                    serializedSpeedLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "sspeed");
                }

                if (serializedSpeedLevel > 0) {
                    speedLevel = serializedSpeedLevel;
                    addUniqueValue(modifierCategories, "speed");
                }
            }
        }

        Object attributeModifiers = (AttributeModifiersComponent)stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            String modifierSummary = attributeModifiers.toString().toLowerCase();
            if (damageLevel == 0 && modifierSummary.contains("damage")) {
                int attributeDamageLevel = parseAttributeTier(modifierSummary, "attack_damage");
                if (attributeDamageLevel >= 4) {
                    isCerberusOrb = true;
                    damageLevel = 4;
                } else if (attributeDamageLevel > 0) {
                    damageLevel = attributeDamageLevel;
                }

                if (damageLevel > 0) {
                    addUniqueValue(modifierCategories, "damage");
                }
            }

            if (armorLevel == 0 && modifierSummary.contains("armor")) {
                int attributeArmorLevel = parseAttributeTier(modifierSummary, "armor");
                if (attributeArmorLevel > 0) {
                    armorLevel = attributeArmorLevel;
                    addUniqueValue(modifierCategories, "armor");
                }
            }

            if (speedLevel == 0 && modifierSummary.contains("speed")) {
                int attributeSpeedLevel = parseAttributeTier(modifierSummary, "movement_speed");
                if (attributeSpeedLevel > 0) {
                    speedLevel = attributeSpeedLevel;
                    addUniqueValue(modifierCategories, "speed");
                }
            }
        }

        String orbDisplayName = null;
        if (!modifierCategories.isEmpty()) {
            if (modifierCategories.size() == 1 && isCerberusOrb) {
                orbDisplayName = "Сфера Цербера";
            } else {
                ArrayList modifierDescriptions = new ArrayList();

                for (Object modifierCategory : modifierCategories) {
                    if ("armor".equals(modifierCategory) && armorLevel > 0) {
                        modifierDescriptions.add("Броня " + toRomanNumeral(armorLevel));
                    } else if ("damage".equals(modifierCategory) && damageLevel > 0) {
                        modifierDescriptions.add(!isCerberusOrb && damageLevel < 4 ? "Урон " + toRomanNumeral(damageLevel) : "Цербер");
                    } else if ("speed".equals(modifierCategory) && speedLevel > 0) {
                        modifierDescriptions.add("Скорость " + toRomanNumeral(speedLevel));
                    }
                }

                if (!modifierDescriptions.isEmpty()) {
                    orbDisplayName = "Сфера на " + String.join(" ", modifierDescriptions);
                }
            }
        }

        if (orbDisplayName != null) {
            if (textureHash != null) {
                specialOrbNameCache.put(textureHash, orbDisplayName);
            }

            return orbDisplayName;
        } else {
            return textureHash != null && specialOrbNameCache.containsKey(textureHash) ? specialOrbNameCache.get(textureHash) : "Сфера";
        }
    }
// https://t.me/pasterplace

    private static void addUniqueValue(List<String> uniqueValues, String candidateValue) {
        if (!uniqueValues.contains(candidateValue)) {
            uniqueValues.add(candidateValue);
        }
    }
// https://t.me/pasterplace

    private static boolean isSwordItem(ItemStack stack) {
        if (stack.isIn(ItemTags.SWORDS)) {
            return true;
        }

        String itemRegistryPath = Registries.ITEM.getId(stack.getItem()).getPath();
        return itemRegistryPath.endsWith("_sword");
    }
// https://t.me/pasterplace

    private static int parseRomanNumeral(String romanNumeral) {
        short totalValue = 0;
        short previousNumeralValue = 0;

        for (int characterIndex = romanNumeral.length() - 1; characterIndex >= 0; characterIndex--) {
            short currentNumeralValue = switch (romanNumeral.charAt(characterIndex)) {
            case 'C' -> 100;
            case 'D' -> 500;
                default -> 0;
            case 'I' -> 1;
            case 'L' -> 50;
            case 'M' -> 1000;
            case 'V' -> 5;
            case 'X' -> 10;
            };
            if (currentNumeralValue == 0) {
                return 0;
            }

            if (currentNumeralValue < previousNumeralValue) {
                totalValue -= currentNumeralValue;
            } else {
                totalValue += currentNumeralValue;
                previousNumeralValue = currentNumeralValue;
            }
        }

        return totalValue;
    }
// https://t.me/pasterplace

    private static int parseNumericLevel(String tooltipText) {
        if (tooltipText != null && !tooltipText.isBlank()) {
            Matcher matcher = SHARPNESS_LEVEL_PATTERN.matcher(sanitizeDisplayText(tooltipText));
            if (matcher.find()) {
                String normalizedLevelText = matcher.group(1).trim().toUpperCase();
                if (normalizedLevelText.matches("\\d+")) {
                    try {
                        return Integer.parseInt(normalizedLevelText);
                    } catch (Exception ignoredNumberFormatException) {
                        return 0;
                    }
                } else {
                    return parseRomanNumeral(normalizedLevelText);
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
// https://t.me/pasterplace

    private static int getSharpnessLevel(ItemEnchantmentsComponent itemEnchantmentsComponent) {
        if (itemEnchantmentsComponent == null) {
            return 0;
        }
// https://t.me/pasterplace

        for (RegistryEntry<Enchantment> enchantmentEntry : itemEnchantmentsComponent.getEnchantments()) {
            if (enchantmentEntry.matchesKey(Enchantments.SHARPNESS)) {
                int enchantmentLevel = itemEnchantmentsComponent.getLevel((RegistryEntry)enchantmentEntry);
                if (enchantmentLevel > 0) {
                    return enchantmentLevel;
                }
            }
        }

        return 0;
    }
// https://t.me/pasterplace

    private static int getStackSharpnessLevel(ItemStack stack) {
        int sharpnessLevel = getSharpnessLevel((ItemEnchantmentsComponent)stack.get(DataComponentTypes.ENCHANTMENTS));
        if (sharpnessLevel > 0) {
            return sharpnessLevel;
        }

        sharpnessLevel = getSharpnessLevel((ItemEnchantmentsComponent)stack.get(DataComponentTypes.STORED_ENCHANTMENTS));
        if (sharpnessLevel > 0) {
            return sharpnessLevel;
        }

        LoreComponent loreComponent = (LoreComponent)stack.get(DataComponentTypes.LORE);
        if (loreComponent != null) {
            for (Text loreLine : loreComponent.lines()) {
                int loreSharpnessLevel = parseNumericLevel(loreLine.getString());
                if (loreSharpnessLevel > 0) {
                    return loreSharpnessLevel;
                }
            }
        }

        Text customName = (Text)stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName != null) {
            int customNameSharpnessLevel = parseNumericLevel(customName.getString());
            if (customNameSharpnessLevel > 0) {
                return customNameSharpnessLevel;
            }
        }

        String customDataText = getItemCustomDataString(stack);
        if (customDataText != null) {
            String lowercaseCustomData = customDataText.toLowerCase();
            int customDataSharpnessLevel = parseSerializedEnchantmentLevel(lowercaseCustomData, "sharpness");
            return customDataSharpnessLevel > 0 ? customDataSharpnessLevel : parseSerializedEnchantmentLevel(lowercaseCustomData, "острота");
        } else {
            return 0;
        }
    }
// https://t.me/pasterplace

    private static String normalizeUnicodeLookalikes(String sourceText) {
        if (sourceText == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (int utf16Index = 0; utf16Index < sourceText.length(); utf16Index++) {
            int codePoint = sourceText.codePointAt(utf16Index);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                utf16Index++;
                if (codePoint >= 119808 && codePoint <= 120483) {
                    int letterIndex = (codePoint - 119808) % 52;
                    stringBuilder.append(letterIndex < 26 ? (char)(65 + letterIndex) : (char)(97 + letterIndex - 26));
                } else if (codePoint >= 120782 && codePoint <= 120831) {
                    stringBuilder.append((char)(48 + (codePoint - 120782) % 10));
                }
            } else {
                char sourceCharacter = (char)codePoint;
                switch (sourceCharacter) {
                case '¦':
                case 'ı':
                case 'ǀ':
                case 'ɪ':
                case 'І':
                case 'Ї':
                case 'і':
                case 'ї':
                case 'ᶦ':
                case 'ℐ':
                case 'ℑ':
                case 'ℒ':
                    stringBuilder.append('I');
                    break;
                case 'Ø':
                case 'ø':
                case 'ᴏ':
                case 'ᵒ':
                    stringBuilder.append('O');
                    break;
                case 'Ŀ':
                case 'ŀ':
                case 'Ł':
                case 'ł':
                case 'ʟ':
                case 'ˡ':
                    stringBuilder.append('L');
                    break;
                case 'Ş':
                case 'ş':
                case 'ˢ':
                case 'Ѕ':
                case 'ѕ':
                case 'ꜱ':
                    stringBuilder.append('S');
                    break;
                case 'Ɓ':
                case 'Ƃ':
                case 'ʙ':
                case 'ᵇ':
                    stringBuilder.append('B');
                    break;
                case 'Ɖ':
                case 'Ɗ':
                case 'ԁ':
                case 'ᴅ':
                case 'ᵈ':
                    stringBuilder.append('D');
                    break;
                case 'Ǝ':
                case 'ǝ':
                case 'ᴇ':
                case 'ᵉ':
                case 'ℇ':
                case 'ℰ':
                    stringBuilder.append('E');
                    break;
                case 'Ƒ':
                case 'ƒ':
                case 'Ϝ':
                case 'ϝ':
                case 'Ғ':
                case 'ғ':
                case 'ᶠ':
                case 'ℱ':
                case 'Ⅎ':
                case 'ꜰ':
                case 'Ꞙ':
                case 'ꞙ':
                case 'ꟻ':
                    stringBuilder.append('F');
                    break;
                case 'Ɠ':
                case 'ɢ':
                case 'Ԍ':
                case 'ԍ':
                case 'ᵍ':
                    stringBuilder.append('G');
                    break;
                case 'ǫ':
                case 'ϙ':
                case 'ℚ':
                    stringBuilder.append('Q');
                    break;
                case 'ɐ':
                case 'ᴀ':
                case 'ᵃ':
                case 'Ɐ':
                    stringBuilder.append('A');
                    break;
                case 'ɴ':
                case 'ᵑ':
                case 'ℕ':
                    stringBuilder.append('N');
                    break;
                case 'ʀ':
                case 'ʁ':
                case 'ʳ':
                case 'ℛ':
                case 'ℜ':
                case 'ℝ':
                    stringBuilder.append('R');
                    break;
                case 'ʏ':
                case 'ʸ':
                    stringBuilder.append('Y');
                    break;
                case 'ʜ':
                case 'Һ':
                case 'һ':
                case 'ℋ':
                case 'ℌ':
                case 'ℍ':
                    stringBuilder.append('H');
                    break;
                case 'ʲ':
                case 'Ј':
                case 'ј':
                case 'ᴊ':
                    stringBuilder.append('J');
                    break;
                case 'ʷ':
                case 'ᴡ':
                    stringBuilder.append('W');
                    break;
                case 'ˣ':
                    stringBuilder.append('X');
                    break;
                case 'Қ':
                case 'қ':
                case 'ᴋ':
                case 'ᵏ':
                    stringBuilder.append('K');
                    break;
                case 'ᴄ':
                case 'ᶜ':
                case 'ℂ':
                case 'ℭ':
                    stringBuilder.append('C');
                    break;
                case 'ᴍ':
                case 'ᵐ':
                case 'ℳ':
                    stringBuilder.append('M');
                    break;
                case 'ᴘ':
                case 'ᵖ':
                case 'ℙ':
                    stringBuilder.append('P');
                    break;
                case 'ᴙ':
                    stringBuilder.append('Я');
                    break;
                case 'ᴛ':
                case 'ᵗ':
                    stringBuilder.append('T');
                    break;
                case 'ᴜ':
                case 'ᵘ':
                    stringBuilder.append('U');
                    break;
                case 'ᴠ':
                case 'ᵛ':
                    stringBuilder.append('V');
                    break;
                case 'ᴢ':
                case 'ℤ':
                case 'ℨ':
                    stringBuilder.append('Z');
                    break;
                case 'ᴦ':
                    stringBuilder.append('Г');
                    break;
                case 'ᴧ':
                case 'ᴫ':
                    stringBuilder.append('Л');
                    break;
                case 'ᴨ':
                    stringBuilder.append('П');
                    break;
                case 'ᴩ':
                    stringBuilder.append('Р');
                    break;
                default:
                    if (sourceCharacter >= 'Ａ' && sourceCharacter <= 'Ｚ') {
                        stringBuilder.append((char)(65 + (sourceCharacter - 'Ａ')));
                    } else if (sourceCharacter >= 'ａ' && sourceCharacter <= 'ｚ') {
                        stringBuilder.append((char)(97 + (sourceCharacter - 'ａ')));
                    } else if (sourceCharacter >= '０' && sourceCharacter <= '９') {
                        stringBuilder.append((char)(48 + (sourceCharacter - '０')));
                    } else {
                        stringBuilder.append(sourceCharacter);
                    }
                }
            }
        }

        return stringBuilder.toString();
    }
// https://t.me/pasterplace

    private static String sanitizeDisplayText(String sourceText) {
        if (sourceText == null) {
            return "";
        }

        String sanitizedText = normalizeUnicodeLookalikes(sourceText);
        sanitizedText = LEGACY_COLOR_CODE_PATTERN.matcher(sanitizedText).replaceAll("");
        sanitizedText = PRIVATE_USE_CHARACTER_PATTERN.matcher(sanitizedText).replaceAll("");
        sanitizedText = REPLACEMENT_CHARACTER_PATTERN.matcher(sanitizedText).replaceAll(" ");
        sanitizedText = DECORATIVE_PREFIX_PATTERN.matcher(sanitizedText).replaceAll("");
        sanitizedText = LEADING_COLOR_CODE_PATTERN.matcher(sanitizedText).replaceAll("");
        sanitizedText = TRAILING_COLOR_CODE_PATTERN.matcher(sanitizedText).replaceAll("");
        return WHITESPACE_PATTERN.matcher(sanitizedText.trim()).replaceAll(" ");
    }
// https://t.me/pasterplace

    private static int getFormattingCodeArgb(char formatCode) {
        return switch (formatCode) {
        case '0' -> -16777216;
        case '1' -> -16777046;
        case '2' -> -16733696;
        case '3' -> -16733526;
        case '4' -> -5636096;
        case '5' -> -5635926;
        case '6' -> -22016;
        case '7' -> -5592406;
        case '8' -> -11184811;
        case '9' -> -11184641;
            default -> -1;
        case 'a' -> -11141291;
        case 'b' -> -11141121;
        case 'c' -> -43691;
        case 'd' -> -43521;
        case 'e' -> -171;
        case 'f' -> -1;
        case 'r' -> 0;
        };
    }
// https://t.me/pasterplace

    private record EntityRenderMetrics(Entity entity, float width, float height, float left, float top, boolean isVisible, float scale) {

        private EntityRenderMetrics(Entity entity, float width, float height, float left, float top, boolean isVisible, float scale) {
            this.entity = entity;
            this.width = width;
            this.height = height;
            this.left = left;
            this.top = top;
            this.isVisible = isVisible;
            this.scale = scale;
        }
    }
// https://t.me/pasterplace

    private record NameTagLayoutRow(List<NameTags.ItemNameSegment> segments, float yOffset) {

        private NameTagLayoutRow(List<NameTags.ItemNameSegment> segments, float yOffset) {
            this.segments = segments;
            this.yOffset = yOffset;
        }
    }
// https://t.me/pasterplace

    private record CachedFormattedName(NameTags.FormattedName formattedName, long cachedAtMillis) {

        private CachedFormattedName(NameTags.FormattedName formattedName, long cachedAtMillis) {
            this.formattedName = formattedName;
            this.cachedAtMillis = cachedAtMillis;
        }
    }
// https://t.me/pasterplace

    private record FormattedName(String text, int colorArgb) {

        private FormattedName(String text, int colorArgb) {
            this.text = text;
            this.colorArgb = colorArgb;
        }
    }
// https://t.me/pasterplace

    private record FormattedTextSegment(String text, int colorArgb) {

        private FormattedTextSegment(String text, int colorArgb) {
            this.text = text;
            this.colorArgb = colorArgb;
        }
    }
// https://t.me/pasterplace

    private static final class NearbyItemsCache {
        private final Map<Long, NameTags.NearbyItemGroupState> groupsById = new HashMap<>();
        private final Set<Integer> groupedItemEntityIds = new HashSet<>();
        private final List<NameTags.NearbyItemGroupState> visibleGroups = new ArrayList<>();
        private long nextGroupId;
        private ClientWorld lastWorld;
        private long lastWorldTime = Long.MIN_VALUE;
        private float lastGroupingDistance;
        private int lastMaxGroupSize;
        private boolean enabled;

        private void clear() {
            this.groupsById.clear();
            this.groupedItemEntityIds.clear();
            this.visibleGroups.clear();
            this.lastWorldTime = Long.MIN_VALUE;
            this.lastWorld = null;
        }

        private boolean isGroupedItem(ItemEntity itemEntity) {
            return this.groupedItemEntityIds.contains(itemEntity.getId());
        }

        private void updateGroups(List<Entity> worldEntities, float groupingDistance, int maxGroupsPerCluster, boolean showItemIcons) {
            if (NameTags.CLIENT.world == null) {
                this.clear();
            } else {
                if (this.lastWorld != NameTags.CLIENT.world) {
                    this.clear();
                    this.lastWorld = NameTags.CLIENT.world;
                }

                long worldTime = NameTags.CLIENT.world.getTime();
                if (worldTime == this.lastWorldTime && groupingDistance == this.lastGroupingDistance && maxGroupsPerCluster == this.lastMaxGroupSize && showItemIcons == this.enabled) {
                    long nowMillis = System.currentTimeMillis();

                    for (NameTags.NearbyItemGroupState groupState : this.visibleGroups) {
                        double projectedXSum = 0.0;
                        double projectedYSum = 0.0;
                        double projectedZSum = 0.0;
                        int projectedItemCount = 0;

                        for (int itemEntityId : ((NameTags.NearbyItemGroupState)groupState).itemEntityIds) {
                            Entity entity = NameTags.CLIENT.world.getEntityById(itemEntityId);
                            if (entity instanceof ItemEntity) {
                                ItemEntity entity2 = (ItemEntity)entity;
                                if (entity2.isAlive() && !entity2.getStack().isEmpty()) {
                                    Vec3d projectedPosition = WorldToScreenProjection.getInterpolatedPosition(entity);
                                    projectedXSum += projectedPosition.x;
                                    projectedYSum += projectedPosition.y;
                                    projectedZSum += projectedPosition.z;
                                    projectedItemCount++;
                                }
                            }
                        }

                        if (projectedItemCount > 0) {
                            ((NameTags.NearbyItemGroupState)groupState).currentX = projectedXSum / projectedItemCount;
                            ((NameTags.NearbyItemGroupState)groupState).currentY = projectedYSum / projectedItemCount;
                            ((NameTags.NearbyItemGroupState)groupState).currentZ = projectedZSum / projectedItemCount;
                        }

                        this.animateGroupState((NameTags.NearbyItemGroupState)groupState, nowMillis);
                    }
                } else {
                    this.lastWorldTime = worldTime;
                    this.lastGroupingDistance = groupingDistance;
                    this.lastMaxGroupSize = maxGroupsPerCluster;
                    this.enabled = showItemIcons;
                    ArrayList<ItemEntity> itemEntities = new ArrayList<>();

                    for (Entity entity : worldEntities) {
                        if (entity instanceof ItemEntity) {
                            ItemEntity itemEntity = (ItemEntity)entity;
                            if (itemEntity.isAlive() && !itemEntity.getStack().isEmpty()) {
                                itemEntities.add(itemEntity);
                            }
                        }
                    }

                    if (itemEntities.size() > 150 && NameTags.CLIENT.player != null) {
                        Vec3d localPlayerPosition = NameTags.CLIENT.player.getEntityPos();
                        itemEntities.sort((itemEntity, itemEntity2) -> Double.compare(itemEntity.squaredDistanceTo(localPlayerPosition), itemEntity2.squaredDistanceTo(localPlayerPosition)));
                        itemEntities = new ArrayList(itemEntities.subList(0, 150));
                    }

                    this.groupedItemEntityIds.clear();
                    this.visibleGroups.clear();

                    for (NameTags.NearbyItemGroupState groupState : this.groupsById.values()) {
                        groupState.active = false;
                    }

                    if (itemEntities.isEmpty()) {
                        this.groupsById.entrySet().removeIf(groupEntry -> System.currentTimeMillis() - groupEntry.getValue().lastUpdateMillis > 400L);
                    } else {
                        float groupingDistanceSquared = groupingDistance * groupingDistance;
                        int itemEntityCount = itemEntities.size();
                        boolean[] visitedItemEntities = new boolean[itemEntityCount];

                        for (int clusterStartIndex = 0; clusterStartIndex < itemEntityCount; clusterStartIndex++) {
                            if (!visitedItemEntities[clusterStartIndex]) {
                                ArrayList<ItemEntity> connectedCluster = new ArrayList<>();
                                ArrayDeque<Integer> pendingClusterIndices = new ArrayDeque<>();
                                pendingClusterIndices.add(clusterStartIndex);
                                visitedItemEntities[clusterStartIndex] = true;

                                while (!pendingClusterIndices.isEmpty()) {
                                    ItemEntity clusterItemEntity = itemEntities.get(pendingClusterIndices.removeFirst());
                                    connectedCluster.add(clusterItemEntity);

                                    for (int neighborIndex = 0; neighborIndex < itemEntityCount; neighborIndex++) {
                                        if (!visitedItemEntities[neighborIndex] && clusterItemEntity.squaredDistanceTo((Entity)itemEntities.get(neighborIndex)) <= groupingDistanceSquared) {
                                            visitedItemEntities[neighborIndex] = true;
                                            pendingClusterIndices.add(neighborIndex);
                                        }
                                    }
                                }

                                if (connectedCluster.size() >= 2) {
                                    HashSet<Integer> clusterEntityIds = new HashSet<>();
                                    double clusterProjectedXSum = 0.0;
                                    double clusterProjectedYSum = 0.0;
                                    double clusterProjectedZSum = 0.0;

                                    for (ItemEntity clusterItemEntity : connectedCluster) {
                                        clusterEntityIds.add(clusterItemEntity.getId());
                                        this.groupedItemEntityIds.add(clusterItemEntity.getId());
                                        Vec3d projectedPosition = WorldToScreenProjection.getInterpolatedPosition(clusterItemEntity);
                                        clusterProjectedXSum += projectedPosition.x;
                                        clusterProjectedYSum += projectedPosition.y;
                                        clusterProjectedZSum += projectedPosition.z;
                                    }

                                    double clusterX = clusterProjectedXSum / connectedCluster.size();
                                    double clusterY = clusterProjectedYSum / connectedCluster.size();
                                    double clusterZ = clusterProjectedZSum / connectedCluster.size();
                                    NameTags.NearbyItemGroupState groupState = this.findBestMatchingGroup(clusterEntityIds);
                                    if (groupState == null) {
                                        groupState = new NameTags.NearbyItemGroupState(++this.nextGroupId, clusterX, clusterY, clusterZ);
                                        this.groupsById.put(groupState.id, groupState);
                                    }
// https://t.me/pasterplace

                                    groupState.currentX = clusterX;
                                    groupState.currentY = clusterY;
                                    groupState.currentZ = clusterZ;
                                    groupState.itemEntityIds.clear();
                                    groupState.itemEntityIds.addAll(clusterEntityIds);
                                    groupState.active = true;
                                    groupState.showItemIcons = showItemIcons;
                                    List<NameTags.NearbyItemGroup> sortedItemGroups = this.buildItemGroups(connectedCluster);
                                    int totalItemCount = 0;

                                    for (NameTags.NearbyItemGroup itemGroup : sortedItemGroups) {
                                        totalItemCount += itemGroup.count;
                                    }

                                    ArrayList<NameTags.NearbyItemGroup> visibleItemGroups = new ArrayList<>(sortedItemGroups.subList(0, Math.min(sortedItemGroups.size(), maxGroupsPerCluster)));
                                    int visibleItemCount = 0;

                                    for (NameTags.NearbyItemGroup itemGroup : visibleItemGroups) {
                                        visibleItemCount += itemGroup.count;
                                    }

                                    int overflowItemCount = totalItemCount - visibleItemCount;
                                    groupState.overflowCountLabel = "+" + (overflowItemCount > 0 ? overflowItemCount : totalItemCount);
                                    groupState.itemGroups = visibleItemGroups;
                                    float overflowLabelWidth = NameTags.measureTextWidth(groupState.overflowCountLabel, 5.8F);
                                    float textLineHeight = NameTags.getScaledLineHeight(5.8F);
                                    float itemRowHeight = Math.max(8.0F, NameTags.getScaledLineHeight(5.8F)) + 2.0F;
                                    float widestItemRowWidth = 0.0F;
                                    float itemIconColumnWidth = showItemIcons ? 12.0F : 0.0F;

                                    for (NameTags.NearbyItemGroup itemGroup : visibleItemGroups) {
                                        widestItemRowWidth = Math.max(widestItemRowWidth, itemIconColumnWidth + itemGroup.textWidth);
                                    }

                                    groupState.targetWidth = 6.0F + Math.max(overflowLabelWidth, widestItemRowWidth) + 6.0F;
                                    groupState.targetHeight = 5.0F
                                    + textLineHeight
                                    + 4.0F
                                    + visibleItemGroups.size() * itemRowHeight
                                    + (visibleItemGroups.size() > 1 ? (visibleItemGroups.size() - 1) * 2.0F : 0.0F)
                                    + 5.0F;
                                    this.animateGroupState(groupState, System.currentTimeMillis());
                                    this.visibleGroups.add(groupState);
                                }
                            }
                        }

                        this.groupsById
                        .entrySet()
                        .removeIf(staleGroupEntry -> !staleGroupEntry.getValue().active && System.currentTimeMillis() - staleGroupEntry.getValue().lastUpdateMillis > 400L);
                    }
                }
            }
        }

        private void animateGroupState(NameTags.NearbyItemGroupState groupState, long nowMillis) {
            float elapsedSeconds = Math.clamp((float)(nowMillis - groupState.lastUpdateMillis) / 1000.0F, 0.0F, 0.1F);
            groupState.lastUpdateMillis = nowMillis;
            float positionLerpFactor = 1.0F - (float)Math.exp(-15.0F * elapsedSeconds);
            groupState.targetX = groupState.targetX + (groupState.currentX - groupState.targetX) * positionLerpFactor;
            groupState.targetY = groupState.targetY + (groupState.currentY - groupState.targetY) * positionLerpFactor;
            groupState.targetZ = groupState.targetZ + (groupState.currentZ - groupState.targetZ) * positionLerpFactor;
            float sizeLerpFactor = 1.0F - (float)Math.exp(-13.0F * elapsedSeconds);
            if (groupState.currentWidth < 0.0F) {
                groupState.currentWidth = groupState.targetWidth;
                groupState.currentHeight = groupState.targetHeight;
            } else {
                groupState.currentWidth = groupState.currentWidth + (groupState.targetWidth - groupState.currentWidth) * sizeLerpFactor;
                groupState.currentHeight = groupState.currentHeight + (groupState.targetHeight - groupState.currentHeight) * sizeLerpFactor;
            }

            groupState.appearanceProgress = Math.min(1.0F, groupState.appearanceProgress + elapsedSeconds * 4.5F);

            for (int groupIndex = 0; groupIndex < groupState.itemGroups.size(); groupIndex++) {
                groupState.itemFadeProgressByIndex.put(groupIndex, Math.min(1.0F, groupState.itemFadeProgressByIndex.getOrDefault(groupIndex, 0.0F) + elapsedSeconds * 6.0F));
            }
        }

        private NameTags.NearbyItemGroupState findBestMatchingGroup(Set<Integer> candidateEntityIds) {
            NameTags.NearbyItemGroupState bestMatch = null;
            int largestOverlap = 0;

            for (NameTags.NearbyItemGroupState existingGroup : this.groupsById.values()) {
                int overlapCount = 0;

                for (int candidateEntityId : candidateEntityIds) {
                    if (existingGroup.itemEntityIds.contains(candidateEntityId)) {
                        overlapCount++;
                    }
                }

                if (overlapCount > largestOverlap) {
                    largestOverlap = overlapCount;
                    bestMatch = existingGroup;
                }
            }

            return bestMatch;
        }
// https://t.me/pasterplace

        private List<NameTags.NearbyItemGroup> buildItemGroups(List<ItemEntity> itemEntities) {
            LinkedHashMap<String, NameTags.NearbyItemGroup> groupsByItemIdentity = new LinkedHashMap<>();

            for (ItemEntity itemEntity : itemEntities) {
                ItemStack stack = itemEntity.getStack();
                if (!stack.isEmpty()) {
                    String itemIdentityKey = Registries.ITEM.getId(stack.getItem()) + ":" + stack.getName().getString();
                    NameTags.NearbyItemGroup itemGroup = groupsByItemIdentity.get(itemIdentityKey);
                    if (itemGroup != null) {
                        itemGroup.count = itemGroup.count + stack.getCount();
                    } else {
                        groupsByItemIdentity.put(
                        itemIdentityKey,
                        new NameTags.NearbyItemGroup(stack.copy(), stack.getCount(), this.formatItemNameSegments(stack), this.getItemPriority(stack))
                        );
                    }
                }
            }

            ArrayList<NameTags.NearbyItemGroup> itemGroups = new ArrayList<>(groupsByItemIdentity.values());

            for (NameTags.NearbyItemGroup itemGroup : itemGroups) {
                float nameSegmentsWidth = 0.0F;

                for (NameTags.FormattedTextSegment nameSegment : itemGroup.nameSegments) {
                    nameSegmentsWidth += NameTags.measureTextWidth(nameSegment.text(), 5.8F);
                }

                itemGroup.textWidth = nameSegmentsWidth + NameTags.measureTextWidth(" x" + itemGroup.count, 5.8F);
            }

            itemGroups.sort((firstGroup, secondGroup) -> {
                int entityCountComparison = Integer.compare(secondGroup.entityCount, firstGroup.entityCount);
                return entityCountComparison != 0 ? entityCountComparison : Integer.compare(secondGroup.count, firstGroup.count);
            });
            return itemGroups;
        }

        private int getItemPriority(ItemStack stack) {
            if (stack.isOf(Items.PLAYER_HEAD) || stack.isOf(Items.TOTEM_OF_UNDYING)) {
                return 900;
            } else if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE) || stack.isOf(Items.END_CRYSTAL) || stack.isOf(Items.RESPAWN_ANCHOR)) {
                return 800;
            } else if (stack.getItem() instanceof SmithingTemplateItem) {
                return 750;
            } else if (stack.isOf(Items.ENCHANTED_BOOK)) {
                return 700;
            } else if (stack.isOf(Items.NETHERITE_SWORD)
            || stack.isOf(Items.NETHERITE_AXE)
            || stack.isOf(Items.NETHERITE_PICKAXE)
            || stack.isOf(Items.NETHERITE_HELMET)
            || stack.isOf(Items.NETHERITE_CHESTPLATE)
            || stack.isOf(Items.NETHERITE_LEGGINGS)
            || stack.isOf(Items.NETHERITE_BOOTS)
            || stack.isOf(Items.NETHERITE_INGOT)
            || stack.isOf(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) {
                return 650;
            } else if (stack.isOf(Items.TIPPED_ARROW) || stack.isOf(Items.SPECTRAL_ARROW)) {
                return 600;
            } else if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                return 550;
            } else if (stack.isOf(Items.DIAMOND_SWORD)
            || stack.isOf(Items.DIAMOND_HELMET)
            || stack.isOf(Items.DIAMOND_CHESTPLATE)
            || stack.isOf(Items.DIAMOND_LEGGINGS)
            || stack.isOf(Items.DIAMOND_BOOTS)
            || stack.isOf(Items.DIAMOND)
            || stack.isOf(Items.DIAMOND_BLOCK)) {
                return 500;
            } else if (stack.isOf(Items.GOLDEN_APPLE)
            || stack.isOf(Items.EXPERIENCE_BOTTLE)
            || stack.isOf(Items.BOW)
            || stack.isOf(Items.CROSSBOW)) {
                return 400;
            } else if (stack.isOf(Items.ENDER_PEARL) || stack.isOf(Items.CHORUS_FRUIT) || stack.isOf(Items.OBSIDIAN)) {
                return 350;
            } else if (stack.isOf(Items.GOLD_INGOT)
            || stack.isOf(Items.RAW_GOLD)
            || stack.isOf(Items.IRON_INGOT)
            || stack.isOf(Items.RAW_IRON)
            || stack.isOf(Items.EMERALD)) {
                return 300;
            } else {
                return !stack.isOf(Items.BUCKET) && !stack.isOf(Items.WATER_BUCKET) && !stack.isOf(Items.LAVA_BUCKET)
                ? 100
                : 250;
            }
        }

        private List<NameTags.FormattedTextSegment> formatItemNameSegments(ItemStack stack) {
            ArrayList sourceNameSegments = new ArrayList();
            Object customNameComponent = (Text)stack.get(DataComponentTypes.CUSTOM_NAME);
            if (customNameComponent != null) {
                this.appendTextSegments((Text)customNameComponent, sourceNameSegments, -1);
            } else {
                Object itemNameComponent = (Text)stack.get(DataComponentTypes.ITEM_NAME);
                if (itemNameComponent != null) {
                    this.appendTextSegments((Text)itemNameComponent, sourceNameSegments, -1);
                } else {
                    int defaultColorArgb = -1;
                    TextColor textColor = TextColor.fromFormatting(stack.getRarity().getFormatting());
                    if (textColor != null) {
                        defaultColorArgb = 0xFF000000 | textColor.getRgb();
                    } else if (stack.getItem() instanceof SmithingTemplateItem) {
                        defaultColorArgb = -171;
                    } else if (stack.isOf(Items.ENCHANTED_BOOK)) {
                        defaultColorArgb = -11141121;
                    }

                    this.appendTextSegments(stack.getName(), sourceNameSegments, defaultColorArgb);
                }
            }

            if (sourceNameSegments.isEmpty()) {
                sourceNameSegments.add(new NameTags.FormattedTextSegment(stack.getName().getString(), -1));
            }

            ArrayList formattedSegments = new ArrayList();

            for (Object sourceSegment : sourceNameSegments) {
                this.splitSpecialSymbols((NameTags.FormattedTextSegment)sourceSegment, formattedSegments);
            }

            return formattedSegments;
        }

        private void appendTextSegments(Text componentText, List<NameTags.FormattedTextSegment> segments, int defaultColorArgb) {
            componentText.visit((textStyle, formattedText) -> {
                if (formattedText != null && !formattedText.isEmpty()) {
                    TextColor textColor = textStyle.getColor();
                    this.appendLegacyFormattedText(formattedText, segments, textColor != null ? 0xFF000000 | textColor.getRgb() : defaultColorArgb);
                }

                return Optional.empty();
            }, Style.EMPTY);
        }
// https://t.me/pasterplace

        private void appendLegacyFormattedText(String rawText, List<NameTags.FormattedTextSegment> segments, int defaultColorArgb) {
            int currentColorArgb = defaultColorArgb;
            StringBuilder stringBuilder = new StringBuilder();

            for (int characterIndex = 0; characterIndex < rawText.length(); characterIndex++) {
                char currentCharacter = rawText.charAt(characterIndex);
                if (currentCharacter == 167 && characterIndex + 1 < rawText.length()) {
                    int formattingColorArgb = NameTags.getFormattingCodeArgb(Character.toLowerCase(rawText.charAt(characterIndex + 1)));
                    if (formattingColorArgb != -1) {
                        if (!stringBuilder.isEmpty()) {
                            segments.add(new NameTags.FormattedTextSegment(stringBuilder.toString(), currentColorArgb));
                            stringBuilder.setLength(0);
                        }

                        currentColorArgb = formattingColorArgb == 0 ? defaultColorArgb : formattingColorArgb;
                        characterIndex++;
                        continue;
                    }
                }

                stringBuilder.append(currentCharacter);
            }
// https://t.me/pasterplace

            if (!stringBuilder.isEmpty()) {
                segments.add(new NameTags.FormattedTextSegment(stringBuilder.toString(), currentColorArgb));
            }
        }

        private void splitSpecialSymbols(NameTags.FormattedTextSegment sourceSegment, List<NameTags.FormattedTextSegment> targetSegments) {
            String segmentText = sourceSegment.text();
            int segmentColorArgb = sourceSegment.colorArgb();
            if (!segmentText.contains("★") && !segmentText.contains("⚡")) {
                targetSegments.add(sourceSegment);
            } else {
                int segmentStartIndex = 0;

                for (int characterIndex = 0; characterIndex < segmentText.length(); characterIndex++) {
                    if (segmentText.startsWith("[★]", characterIndex)) {
                        if (characterIndex > segmentStartIndex) {
                            targetSegments.add(new NameTags.FormattedTextSegment(segmentText.substring(segmentStartIndex, characterIndex), segmentColorArgb));
                        }

                        targetSegments.add(new NameTags.FormattedTextSegment("[★]", -11141121));
                        characterIndex += 2;
                        segmentStartIndex = characterIndex + 1;
                    } else if (segmentText.charAt(characterIndex) == 9733) {
                        if (characterIndex > segmentStartIndex) {
                            targetSegments.add(new NameTags.FormattedTextSegment(segmentText.substring(segmentStartIndex, characterIndex), segmentColorArgb));
                        }

                        targetSegments.add(new NameTags.FormattedTextSegment("★", -11141121));
                        segmentStartIndex = characterIndex + 1;
                    } else if (segmentText.charAt(characterIndex) == 9889) {
                        if (characterIndex > segmentStartIndex) {
                            targetSegments.add(new NameTags.FormattedTextSegment(segmentText.substring(segmentStartIndex, characterIndex), segmentColorArgb));
                        }

                        targetSegments.add(new NameTags.FormattedTextSegment("⚡", -171));
                        segmentStartIndex = characterIndex + 1;
                    }
                }

                if (segmentStartIndex < segmentText.length()) {
                    targetSegments.add(new NameTags.FormattedTextSegment(segmentText.substring(segmentStartIndex), segmentColorArgb));
                }
            }
        }

        private void renderGroups(DrawContext drawContext, float baseScale, float viewportHeight, boolean drawIconsOnly) {
            for (Object groupState : this.visibleGroups) {
                float appearanceAlpha = (float)Math.sin(((NameTags.NearbyItemGroupState)groupState).appearanceProgress * Math.PI * 0.5);
                if (!(appearanceAlpha <= 0.01F)) {
                    WorldToScreenProjection.ScreenPoint screenPosition = WorldToScreenProjection.project(
                    ((NameTags.NearbyItemGroupState)groupState).targetX,
                    ((NameTags.NearbyItemGroupState)groupState).targetY + 0.35,
                    ((NameTags.NearbyItemGroupState)groupState).targetZ
                    );
                    if (screenPosition != null && screenPosition.inFront() && !(screenPosition.y() < 0.0F) && !(screenPosition.y() > viewportHeight)) {
                        float groupCenterX = screenPosition.x();
                        float groupTopY = Math.max(15.0F, screenPosition.y() - 6.0F);
                        float cardWidth = ((NameTags.NearbyItemGroupState)groupState).currentWidth;
                        float cardHeight = ((NameTags.NearbyItemGroupState)groupState).currentHeight;
                        float cardLeftX = groupCenterX - cardWidth / 2.0F;
                        float cardTopY = groupTopY - cardHeight;
                        float groupScale = baseScale * (0.85F + 0.15F * appearanceAlpha);
                        float fontLineHeight = NameTags.getScaledLineHeight(5.8F);
                        float itemRowHeight = Math.max(8.0F, NameTags.getScaledLineHeight(5.8F)) + 2.0F;
                        float firstItemRowY = cardTopY + 5.0F + fontLineHeight + 4.0F;
                        NameTags.pushScaleTransform(drawContext, groupCenterX, groupTopY, groupScale);
                        if (drawIconsOnly) {
                            if (((NameTags.NearbyItemGroupState)groupState).showItemIcons) {
                                for (int itemIconIndex = 0; itemIconIndex < ((NameTags.NearbyItemGroupState)groupState).itemGroups.size(); itemIconIndex++) {
                                    float itemIconY = firstItemRowY + itemIconIndex * (itemRowHeight + 2.0F);
                                    NameTags.drawItemStack(
                                    drawContext,
                                    ((NameTags.NearbyItemGroupState)groupState).itemGroups.get(itemIconIndex).itemStack,
                                    cardLeftX + 6.0F + 4.0F,
                                    itemIconY + itemRowHeight / 2.0F,
                                    8.0F
                                    );
                                }
                            }

                            NameTags.popScaleTransform(drawContext);
                        } else {
                            NameTags.drawBrokkeIcon(drawContext, cardLeftX, cardTopY, cardWidth, cardHeight, 4.5F, Math.clamp(180.0F * appearanceAlpha / 175.0F, 0.0F, 1.0F));
                            NameTags.drawText(
                            drawContext,
                            NameTags.NAME_TAG_FONT,
                            ((NameTags.NearbyItemGroupState)groupState).overflowCountLabel,
                            cardLeftX + 6.0F,
                            cardTopY + 5.0F,
                            5.8F,
                            Accent.getDarkerTextColor((int)(220.0F * appearanceAlpha)).toArgb()
                            );

                            for (int itemGroupIndex = 0; itemGroupIndex < ((NameTags.NearbyItemGroupState)groupState).itemGroups.size(); itemGroupIndex++) {
                                Object itemGroup = ((NameTags.NearbyItemGroupState)groupState).itemGroups.get(itemGroupIndex);
                                int rowAlpha = (int)(255.0F * appearanceAlpha * ((NameTags.NearbyItemGroupState)groupState).itemFadeProgressByIndex.getOrDefault(itemGroupIndex, 1.0F));
                                if (rowAlpha > 0) {
                                    float rowY = firstItemRowY + itemGroupIndex * (itemRowHeight + 2.0F);
                                    float textY = rowY + (itemRowHeight - NameTags.getScaledLineHeight(5.8F)) / 2.0F;
                                    float textX = cardLeftX + 6.0F + (((NameTags.NearbyItemGroupState)groupState).showItemIcons ? 12.0F : 0.0F);

                                    for (Object formattedSegment : ((NameTags.NearbyItemGroup)itemGroup).nameSegments) {
                                        NameTags.drawText(
                                        drawContext,
                                        NameTags.NAME_TAG_FONT,
                                        ((NameTags.FormattedTextSegment)formattedSegment).text(),
                                        textX,
                                        textY,
                                        5.8F,
                                        rowAlpha << 24 | ((NameTags.FormattedTextSegment)formattedSegment).colorArgb() & 16777215
                                        );
                                        textX += NameTags.measureTextWidth(((NameTags.FormattedTextSegment)formattedSegment).text(), 5.8F);
                                    }

                                    NameTags.drawText(
                                    drawContext, NameTags.NAME_TAG_FONT, " x" + ((NameTags.NearbyItemGroup)itemGroup).count, textX, textY, 5.8F, rowAlpha << 24 | 16777215
                                    );
                                }
                            }

                            NameTags.popScaleTransform(drawContext);
                        }
                    }
                }
            }
        }
    }
// https://t.me/pasterplace

    private record PotionStatusLayout(List<NameTags.NameTagLayoutRow> rows, float width, float height) {

        private PotionStatusLayout(List<NameTags.NameTagLayoutRow> rows, float width, float height) {
            this.rows = rows;
            this.width = width;
            this.height = height;
        }
    }
// https://t.me/pasterplace

    private static final class NearbyItemGroup {
        private final ItemStack itemStack;
        private int count;
        private final List<NameTags.FormattedTextSegment> nameSegments;
        private float textWidth;
        private final int entityCount;

        private NearbyItemGroup(ItemStack stack, int stackCount, List<NameTags.FormattedTextSegment> nameSegments, int entityCount) {
            this.itemStack = stack;
            this.count = stackCount;
            this.nameSegments = nameSegments;
            this.entityCount = entityCount;
        }
    }
// https://t.me/pasterplace

    // $VF: Couldn't be decompiled
    // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
    // java.lang.NullPointerException
    //   at java.base/java.util.Objects.requireNonNull(Objects.java:233)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.rename(VarDefinitionHelper.java:2035)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.iterateClashingExprent(VarDefinitionHelper.java:1822)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.iterateClashingExprent(VarDefinitionHelper.java:2029)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.iterateClashingNames(VarDefinitionHelper.java:1619)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.iterateClashingNames(VarDefinitionHelper.java:1739)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.iterateClashingNames(VarDefinitionHelper.java:1739)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.iterateClashingNames(VarDefinitionHelper.java:1739)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.iterateClashingNames(VarDefinitionHelper.java:1739)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarDefinitionHelper.remapClashingNames(VarDefinitionHelper.java:1584)
    //   at org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor.rerunClashing(VarProcessor.java:99)
    //   at org.jetbrains.java.decompiler.main.ClassWriter.invokeProcessors(ClassWriter.java:145)
    //   at org.jetbrains.java.decompiler.main.ClassWriter.writeClass(ClassWriter.java:379)
    //   at org.jetbrains.java.decompiler.main.ClassWriter.writeClass(ClassWriter.java:520)
    //   at org.jetbrains.java.decompiler.main.ClassesProcessor.writeClass(ClassesProcessor.java:521)
    //   at org.jetbrains.java.decompiler.main.Fernflower.getClassContent(Fernflower.java:200)
    //   at org.jetbrains.java.decompiler.struct.ContextUnit.lambda$save$3(ContextUnit.java:221)
// https://t.me/pasterplace

    private record ItemNameSegment(String text, int colorArgb, float width) {

        private ItemNameSegment(String text, int colorArgb, float width) {
            this.text = text;
            this.colorArgb = colorArgb;
            this.width = width;
        }
    }
// https://t.me/pasterplace

    private static final class NearbyItemGroupState {
        private final long id;
        private double currentX;
        private double currentY;
        private double currentZ;
        private double targetX;
        private double targetY;
        private double targetZ;
        private float targetWidth;
        private float targetHeight;
        private float currentWidth = -1.0F;
        private float currentHeight = -1.0F;
        private float appearanceProgress;
        private long lastUpdateMillis = System.currentTimeMillis();
        private final Set<Integer> itemEntityIds = new HashSet<>();
        private final Map<Integer, Float> itemFadeProgressByIndex = new HashMap<>();
        private List<NameTags.NearbyItemGroup> itemGroups = new ArrayList<>();
        private String overflowCountLabel = "+0";
        private boolean active;
        private boolean showItemIcons = true;

        private NearbyItemGroupState(long groupId, double initialX, double initialY, double initialZ) {
            this.id = groupId;
            this.currentX = initialX;
            this.currentY = initialY;
            this.currentZ = initialZ;
            this.targetX = initialX;
            this.targetY = initialY;
            this.targetZ = initialZ;
        }
    }

    private record PotionStatus(int amplifier, int remainingTicks, boolean infinite, boolean approximate) {
    }

    private static final class PotionStatusTracker {
        private final Map<UUID, Map<RegistryEntry<StatusEffect>, PotionStatus>> effectsByPlayer = new HashMap<>();
        private final ArrayDeque<PotionImpact> recentImpacts = new ArrayDeque<>();

        void clear() {
            this.effectsByPlayer.clear();
            this.recentImpacts.clear();
        }

        void update(ClientWorld world) {
            Set<UUID> presentPlayers = new HashSet<>();
            for (PlayerEntity player : world.getPlayers()) {
                presentPlayers.add(player.getUuid());
                this.refreshPlayer(player);
            }
            this.effectsByPlayer.keySet().retainAll(presentPlayers);
            long cutoff = System.currentTimeMillis() - 5_000L;
            while (!this.recentImpacts.isEmpty() && this.recentImpacts.peekFirst().recordedAt() < cutoff) {
                this.recentImpacts.removeFirst();
            }
        }

        void recordPotionImpact(Vec3d position, int color) {
            this.recentImpacts.addLast(new PotionImpact(position, color, System.currentTimeMillis()));
            while (this.recentImpacts.size() > 32) {
                this.recentImpacts.removeFirst();
            }
        }

        void refreshPlayer(PlayerEntity player) {
            Map<RegistryEntry<StatusEffect>, PotionStatus> statuses = new HashMap<>();
            Collection<StatusEffectInstance> activeEffects = player.getStatusEffects();
            for (StatusEffectInstance instance : activeEffects) {
                statuses.put(instance.getEffectType(), new PotionStatus(
                    instance.getAmplifier(), instance.getDuration(), instance.isInfinite(), false));
            }
            this.effectsByPlayer.put(player.getUuid(), statuses);
        }

        void forgetPlayer(PlayerEntity player) {
            this.effectsByPlayer.remove(player.getUuid());
        }

        PotionStatus statusFor(PlayerEntity player, RegistryEntry<StatusEffect> effectType) {
            StatusEffectInstance instance = player.getStatusEffect(effectType);
            if (instance == null) {
                Map<RegistryEntry<StatusEffect>, PotionStatus> statuses = this.effectsByPlayer.get(player.getUuid());
                return statuses == null ? null : statuses.get(effectType);
            }

            PotionStatus status = new PotionStatus(
                instance.getAmplifier(), instance.getDuration(), instance.isInfinite(), false);
            this.effectsByPlayer.computeIfAbsent(player.getUuid(), ignored -> new HashMap<>()).put(effectType, status);
            return status;
        }
    }

    private record PotionImpact(Vec3d position, int color, long recordedAt) {
    }
}

