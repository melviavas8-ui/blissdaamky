/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.floats.FloatArrayList
 *  it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  project.melvi.metadata.Allow2Generation
 *  project.melvi.metadata.Allow2Generation$GenerationLevel
 */
package dev.daamky;

import dev.daamky.DaamkyWorldRender;
import dev.daamky.MultiOptionSetting;
import dev.daamky.MultiToggleSetting;
import dev.daamky.RgbaColor;
import dev.daamky.BooleanSetting;
import dev.daamky.FloatSetting;
import dev.daamky.ModuleCategory;
import dev.daamky.ModuleStructure;
import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventRender3D;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.AnimatedColorUtils;
import dev.daamky.FriendManager;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import project.melvi.metadata.Allow2Generation;

public final class ESP
extends ModuleStructure {
    private final MultiOptionSetting entityRenderMode = new MultiOptionSetting("\u0420\u0435\u0436\u0438\u043c", "\u041a\u043e\u0440\u043e\u0431\u043a\u0430", "\u041a\u043e\u0440\u043e\u0431\u043a\u0430", "\u0423\u0433\u043b\u044b", "\u0421\u043a\u0435\u043b\u0435\u0442", "\u041a\u043e\u043b\u043e\u043d\u043d\u0430");
    private final MultiToggleSetting entityTargetSettings = new MultiToggleSetting("\u0426\u0435\u043b\u0438", new BooleanSetting[0]).addOption("\u0418\u0433\u0440\u043e\u043a\u0438", true).addOption("\u0414\u0440\u0443\u0437\u044c\u044f", true).addOption("\u041c\u043e\u043d\u0441\u0442\u0440\u044b", false).addOption("\u0416\u0438\u0432\u043e\u0442\u043d\u044b\u0435", false).addOption("\u041d\u0435\u0432\u0438\u0434\u0438\u043c\u044b\u0435", true).addOption("\u0421\u0435\u0431\u044f", false);
    private final MultiOptionSetting entityColorMode = new MultiOptionSetting("\u0426\u0432\u0435\u0442", "\u0422\u0435\u043c\u0430", "\u0422\u0435\u043c\u0430", "\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435", "\u0411\u0435\u043b\u044b\u0439");
    private final BooleanSetting filledBoxes = new BooleanSetting("\u0417\u0430\u043b\u0438\u0432\u043a\u0430", true);
    private final BooleanSetting throughWalls = new BooleanSetting("\u0421\u043a\u0432\u043e\u0437\u044c \u0441\u0442\u0435\u043d\u044b", true);
    private final BooleanSetting healthBarEnabled = new BooleanSetting("\u041f\u043e\u043b\u043e\u0441\u043a\u0430 \u0437\u0434\u043e\u0440\u043e\u0432\u044c\u044f", true);
    private final FloatSetting fillAlpha = new FloatSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0437\u0430\u043b\u0438\u0432\u043a\u0438", 40.0f, 5.0f, 150.0f, 5.0f);
    private final FloatSetting outlineThickness = new FloatSetting("\u0422\u043e\u043b\u0449\u0438\u043d\u0430", 2.0f, 0.5f, 5.0f, 0.1f);
    private final BooleanSetting glowEnabled = new BooleanSetting("Glow", false);
    private final FloatSetting glowRadius = new FloatSetting("\u0420\u0430\u0434\u0438\u0443\u0441 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f", 2.0f, 0.5f, 5.0f, 0.1f);
    private final FloatSetting glowStrength = new FloatSetting("\u0421\u0438\u043b\u0430 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f", 1.5f, 0.2f, 4.0f, 0.1f);
    private final FloatSetting glowDistance = new FloatSetting("\u0414\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f", 48.0f, 8.0f, 256.0f, 4.0f, " \u0431\u043b");
    private final BooleanSetting fillGlowEnabled = new BooleanSetting("\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435 \u0437\u0430\u043b\u0438\u0432\u043a\u0438", false);
    private final FloatSetting entityRenderDistance = new FloatSetting("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", 128.0f, 16.0f, 256.0f, 4.0f, " \u0431\u043b");
    private final MultiToggleSetting objectTargetSettings = new MultiToggleSetting("\u041e\u0431\u044a\u0435\u043a\u0442\u044b", new BooleanSetting[0]).addOption("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", false).addOption("\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u0438", false).addOption("\u0421\u0442\u0440\u0435\u043b\u044b", false);
    private final MultiOptionSetting objectRenderMode = new MultiOptionSetting("\u0420\u0435\u0436\u0438\u043c \u043e\u0431\u044a\u0435\u043a\u0442\u043e\u0432", "\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435", "\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435", "\u041a\u043e\u0440\u043e\u0431\u043a\u0430", "\u041b\u0443\u0447");
    private final MultiOptionSetting objectColorMode = new MultiOptionSetting("\u0426\u0432\u0435\u0442 \u043e\u0431\u044a\u0435\u043a\u0442\u043e\u0432", "\u0420\u0435\u0434\u043a\u043e\u0441\u0442\u044c", "\u0420\u0435\u0434\u043a\u043e\u0441\u0442\u044c", "\u0422\u0435\u043c\u0430");
    private final BooleanSetting projectileTrailsEnabled = new BooleanSetting("\u0421\u043b\u0435\u0434\u044b \u0441\u043d\u0430\u0440\u044f\u0434\u043e\u0432", true);
    private final FloatSetting itemMarkerSize = new FloatSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u043e\u0431\u044a\u0435\u043a\u0442\u043e\u0432", 0.35f, 0.1f, 1.0f, 0.05f);
    private final FloatSetting objectRenderDistance = new FloatSetting("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f \u043e\u0431\u044a\u0435\u043a\u0442\u043e\u0432", 64.0f, 8.0f, 160.0f, 4.0f, " \u0431\u043b");
    private final Int2ObjectOpenHashMap<ArrayDeque<Vec3d>> projectileTrailsByEntityId = new Int2ObjectOpenHashMap();
    private final IntOpenHashSet trackedProjectileIds = new IntOpenHashSet();
    private final ItemRenderState itemRenderState = new ItemRenderState();
    private final Int2FloatOpenHashMap entityOpacityById = new Int2FloatOpenHashMap();
    private final IntOpenHashSet visibleEntityIds = new IntOpenHashSet();
    private final List<LivingEntity> visibleLivingEntities = new ArrayList<LivingEntity>();
    private final FloatArrayList visibleEntityOpacities = new FloatArrayList();
    private boolean renderingGlowBuffer;

    public ESP() {
        super("ESP", "\u041f\u043e\u0434\u0441\u0432\u0435\u0447\u0438\u0432\u0430\u0435\u0442 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439 \u0432 \u043c\u0438\u0440\u0435", ModuleCategory.RENDER);
        this.filledBoxes.visibleWhen(() -> this.entityRenderMode.isSelected("\u041a\u043e\u0440\u043e\u0431\u043a\u0430") || this.entityRenderMode.isSelected("\u041a\u043e\u043b\u043e\u043d\u043d\u0430"));
        this.fillAlpha.visibleWhen(() -> (Boolean)this.filledBoxes.getValue() != false && (this.entityRenderMode.isSelected("\u041a\u043e\u0440\u043e\u0431\u043a\u0430") || this.entityRenderMode.isSelected("\u041a\u043e\u043b\u043e\u043d\u043d\u0430")));
        this.healthBarEnabled.visibleWhen(() -> !this.entityRenderMode.isSelected("\u0421\u043a\u0435\u043b\u0435\u0442"));
        this.glowRadius.visibleWhen(this.glowEnabled::getValue);
        this.glowStrength.visibleWhen(this.glowEnabled::getValue);
        this.glowDistance.visibleWhen(this.glowEnabled::getValue);
        this.fillGlowEnabled.visibleWhen(() -> (Boolean)this.glowEnabled.getValue() != false && (Boolean)this.filledBoxes.getValue() != false && (this.entityRenderMode.isSelected("\u041a\u043e\u0440\u043e\u0431\u043a\u0430") || this.entityRenderMode.isSelected("\u041a\u043e\u043b\u043e\u043d\u043d\u0430")));
        this.objectRenderMode.visibleWhen(this::hasObjectEspTargets);
        this.objectColorMode.visibleWhen(this::hasObjectEspTargets);
        this.projectileTrailsEnabled.visibleWhen(() -> this.objectTargetSettings.isOptionEnabled("\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u0438") || this.objectTargetSettings.isOptionEnabled("\u0421\u0442\u0440\u0435\u043b\u044b"));
        this.itemMarkerSize.visibleWhen(this::hasObjectEspTargets);
        this.objectRenderDistance.visibleWhen(this::hasObjectEspTargets);
        this.addSettings(this.entityRenderMode, this.entityTargetSettings, this.entityColorMode, this.filledBoxes, this.fillAlpha, this.throughWalls, this.healthBarEnabled, this.outlineThickness, this.glowEnabled, this.glowRadius, this.glowStrength, this.glowDistance, this.fillGlowEnabled, this.entityRenderDistance, this.objectTargetSettings, this.objectRenderMode, this.objectColorMode, this.projectileTrailsEnabled, this.itemMarkerSize, this.objectRenderDistance);
    }

    public void renderEntity(LivingEntity livingEntity) {
        this.renderLivingEntity(livingEntity, 1.0f);
        if (((Boolean)this.glowEnabled.getValue()).booleanValue()) {
            DaamkyWorldRender.beginTexturedQuadBuffer(((Float)this.glowRadius.getValue()).floatValue(), ((Float)this.glowStrength.getValue()).floatValue(), (Boolean)this.throughWalls.getValue() != false || this.entityRenderMode.isSelected("\u0421\u043a\u0435\u043b\u0435\u0442"));
            this.renderingGlowBuffer = true;
            try {
                this.renderLivingEntity(livingEntity, 1.0f);
            }
            finally {
                this.renderingGlowBuffer = false;
                DaamkyWorldRender.flushTexturedQuadBuffer();
            }
        }
    }

    public String getEntityRenderMode() {
        return (String)this.entityRenderMode.getValue();
    }

    private boolean hasObjectEspTargets() {
        return this.objectTargetSettings.getEnabledCount() > 0;
    }

    public boolean isItemEspEnabled() {
        return this.objectTargetSettings.isOptionEnabled("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b");
    }

    public void renderDroppedItemModel(MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, double d, double d2, double d3) {
        if (!this.isItemEspEnabled() || ESP.CLIENT.world == null) {
            return;
        }
        ItemStack renderedItemStack = new ItemStack(Items.DIAMOND_SWORD);
        CLIENT.getItemModelManager().clearAndUpdate(this.itemRenderState, renderedItemStack, ItemDisplayContext.GROUND, ESP.CLIENT.world, null, 0);
        float f = (float)(System.currentTimeMillis() % 6280L) / 1000.0f;
        matrixStack.push();
        matrixStack.translate(d, d2 + Math.sin(f * 2.0f) * 0.08 + 0.25, d3);
        matrixStack.multiply((Quaternionfc)new Quaternionf().rotateY(f));
        this.itemRenderState.render(matrixStack, orderedRenderCommandQueue, 0xF000F0, OverlayTexture.DEFAULT_UV, 0);
        matrixStack.pop();
    }

    public void renderDroppedItemMarker(Vec3d itemPosition) {
        if (!this.isItemEspEnabled()) {
            return;
        }
        float f = (float)(System.currentTimeMillis() % 6280L) / 1000.0f;
        Vec3d floatingItemPosition = itemPosition.add(0.0, Math.sin(f * 2.0f) * 0.08 + 0.25, 0.0);
        Box box = new Box(itemPosition.x - 0.125, itemPosition.y, itemPosition.z - 0.125, itemPosition.x + 0.125, itemPosition.y + 0.25, itemPosition.z + 0.125);
        this.renderObjectMarker(itemPosition, floatingItemPosition, box, this.getObjectColor(Rarity.RARE, 0), (boolean)((Boolean)this.throughWalls.getValue()));
    }

    @EventTarget
    @Allow2Generation(level=Allow2Generation.GenerationLevel.FAST)
    public void updateProjectileTrails(EventUpdate eventUpdate) {
        if (ESP.CLIENT.world == null || !((Boolean)this.projectileTrailsEnabled.getValue()).booleanValue() || !this.objectTargetSettings.isOptionEnabled("\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u0438") && !this.objectTargetSettings.isOptionEnabled("\u0421\u0442\u0440\u0435\u043b\u044b")) {
            this.projectileTrailsByEntityId.clear();
            return;
        }
        this.trackedProjectileIds.clear();
        for (Entity entity : ESP.CLIENT.world.getEntities()) {
            if (!this.isTrackedProjectile(entity)) continue;
            this.trackedProjectileIds.add(entity.getId());
            ArrayDeque<Vec3d> arrayDeque = this.projectileTrailsByEntityId.computeIfAbsent(entity.getId(), n -> new ArrayDeque<>());
            arrayDeque.addLast(entity.getEntityPos());
            while (arrayDeque.size() > 40) {
                arrayDeque.removeFirst();
            }
        }
        this.projectileTrailsByEntityId.keySet().retainAll((IntCollection)this.trackedProjectileIds);
    }

    private boolean isTrackedProjectile(Entity entity) {
        return entity instanceof FireworkRocketEntity && this.objectTargetSettings.isOptionEnabled("\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u0438") || entity instanceof PersistentProjectileEntity && this.objectTargetSettings.isOptionEnabled("\u0421\u0442\u0440\u0435\u043b\u044b");
    }

    private void renderObjectOverlays() {
        if (!this.hasObjectEspTargets()) {
            return;
        }
        boolean bl = (Boolean)this.throughWalls.getValue();
        double d = ((Float)this.objectRenderDistance.getValue()).floatValue() * ((Float)this.objectRenderDistance.getValue()).floatValue();
        for (Entity entity : ESP.CLIENT.world.getEntities()) {
            if (entity.squaredDistanceTo(ESP.CLIENT.player) > d) continue;
            if (entity instanceof ItemEntity) {
                ItemEntity itemEntity = (ItemEntity)entity;
                if (this.objectTargetSettings.isOptionEnabled("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b")) {
                    Box box = DaamkyWorldRender.getInterpolatedBoundingBox(itemEntity).expand(0.05);
                    if (!DaamkyWorldRender.shouldRenderBox(box)) continue;
                    Vec3d cameraPosition = DaamkyWorldRender.getInterpolatedPosition(itemEntity);
                    float f = (float)Math.sin(((float)itemEntity.age + DaamkyWorldRender.getTickDelta()) / 10.0f) * 0.08f + 0.25f;
                    this.renderObjectMarker(cameraPosition, cameraPosition.add(0.0, f, 0.0), box, this.getObjectColor(itemEntity.getStack().getRarity(), itemEntity.getId()), bl);
                    continue;
                }
            }
            if (!this.isTrackedProjectile(entity)) continue;
            this.renderProjectile(entity, bl);
        }
    }

    private RgbaColor getObjectColor(Rarity rarity, int n) {
        if (this.objectColorMode.isSelected("\u0422\u0435\u043c\u0430")) {
            return AnimatedColorUtils.getAnimatedAccentColor((float)n * 0.5f, 255);
        }
        return switch (rarity) {
            case Rarity.UNCOMMON -> RgbaColor.rgb(255, 230, 90);
            case Rarity.RARE -> RgbaColor.rgb(90, 230, 255);
            case Rarity.EPIC -> RgbaColor.rgb(220, 100, 255);
            default -> RgbaColor.rgb(235, 235, 235);
        };
    }

    private void renderObjectMarker(Vec3d basePosition, Vec3d markerPosition, Box box, RgbaColor l1I1IlllIlI4, boolean bl) {
        float f = ((Float)this.itemMarkerSize.getValue()).floatValue();
        switch ((String)this.objectRenderMode.getValue()) {
            case "\u041a\u043e\u0440\u043e\u0431\u043a\u0430": {
                DaamkyWorldRender.drawFilledBox(box, l1I1IlllIlI4.withAlpha(40), bl);
                DaamkyWorldRender.drawBoxOutline(box, l1I1IlllIlI4.withAlpha(220), 1.5f, bl);
                break;
            }
            case "\u041b\u0443\u0447": {
                DaamkyWorldRender.drawGradientLine(basePosition, basePosition.add(0.0, 2.5, 0.0), l1I1IlllIlI4.withAlpha(230), l1I1IlllIlI4.withAlpha(0), 2.5f, bl);
                DaamkyWorldRender.drawBloomRing(markerPosition, f, l1I1IlllIlI4.withAlpha(160), bl);
                break;
            }
            default: {
                DaamkyWorldRender.drawBloomRing(markerPosition, f, l1I1IlllIlI4.withAlpha(200), bl);
                DaamkyWorldRender.drawBloomRing(markerPosition, f * 0.4f, RgbaColor.WHITE.withAlpha(140), bl);
            }
        }
    }

    private void renderProjectile(Entity entity, boolean bl) {
        RgbaColor l1I1IlllIlI4 = this.objectColorMode.isSelected("\u0422\u0435\u043c\u0430") ? AnimatedColorUtils.getAnimatedAccentColor((float)entity.getId(), 255) : (entity instanceof FireworkRocketEntity ? RgbaColor.rgb(255, 120, 60) : RgbaColor.rgb(230, 230, 230));
        Box box = DaamkyWorldRender.getInterpolatedBoundingBox(entity).expand(0.1);
        if (!DaamkyWorldRender.shouldRenderBox(box)) {
            return;
        }
        DaamkyWorldRender.drawBoxOutline(box, l1I1IlllIlI4.withAlpha(230), 1.6f, bl);
        DaamkyWorldRender.drawBloomRing(box.getCenter(), ((Float)this.itemMarkerSize.getValue()).floatValue() * 1.2f, l1I1IlllIlI4.withAlpha(170), bl);
        ArrayDeque<Vec3d> arrayDeque = this.projectileTrailsByEntityId.get(entity.getId());
        if (!((Boolean)this.projectileTrailsEnabled.getValue()).booleanValue() || arrayDeque == null || arrayDeque.size() < 2) {
            return;
        }
        int n = arrayDeque.size();
        int n2 = 0;
        Vec3d previousTrailPosition = null;
        for (Vec3d trailPosition : arrayDeque) {
            if (previousTrailPosition != null) {
                DaamkyWorldRender.drawLine(previousTrailPosition, trailPosition, l1I1IlllIlI4.withAlpha((int)(220.0f * ((float)n2 / (float)n))), 2.0f, bl);
            }
            previousTrailPosition = trailPosition;
            ++n2;
        }
        DaamkyWorldRender.drawLine(previousTrailPosition, DaamkyWorldRender.getInterpolatedPosition(entity), l1I1IlllIlI4.withAlpha(220), 2.0f, bl);
    }

    @Override
    @Allow2Generation(level=Allow2Generation.GenerationLevel.FAST)
    protected void onDisable() {
        this.projectileTrailsByEntityId.clear();
        this.entityOpacityById.clear();
        this.visibleLivingEntities.clear();
        this.visibleEntityOpacities.clear();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventTarget
    public void onRender3D(EventRender3D eventRender3D) {
        int n;
        Object livingEntityCandidate;
        if (ESP.CLIENT.world == null || ESP.CLIENT.player == null) {
            return;
        }
        this.renderObjectOverlays();
        float f = eventRender3D.delta();
        float f2 = 1.0f - (float)Math.exp(-8.0f * f);
        double d = ((Float)this.entityRenderDistance.getValue()).floatValue() * ((Float)this.entityRenderDistance.getValue()).floatValue();
        this.visibleEntityIds.clear();
        this.visibleLivingEntities.clear();
        this.visibleEntityOpacities.clear();
        for (Entity entity : ESP.CLIENT.world.getEntities()) {
            if (!(entity instanceof LivingEntity) || !this.shouldRenderLivingEntity((LivingEntity)(livingEntityCandidate = (LivingEntity)entity)) || entity.squaredDistanceTo(ESP.CLIENT.player) > d) continue;
            n = entity.getId();
            this.visibleEntityIds.add(n);
            float f3 = this.entityOpacityById.getOrDefault(n, 0.0f);
            f3 += (1.0f - f3) * f2;
            this.entityOpacityById.put(n, f3);
            if (!DaamkyWorldRender.shouldRenderBox(DaamkyWorldRender.getInterpolatedBoundingBox((Entity)livingEntityCandidate).expand(0.3))) continue;
            this.visibleLivingEntities.add((LivingEntity)livingEntityCandidate);
            this.visibleEntityOpacities.add(f3);
            this.renderLivingEntity((LivingEntity)livingEntityCandidate, f3);
        }
        this.entityOpacityById.keySet().retainAll((IntCollection)this.visibleEntityIds);
        if (!((Boolean)this.glowEnabled.getValue()).booleanValue() || this.visibleLivingEntities.isEmpty()) {
            return;
        }
        double d2 = ((Float)this.glowDistance.getValue()).floatValue() * ((Float)this.glowDistance.getValue()).floatValue();
        livingEntityCandidate = DaamkyWorldRender.getCameraPosition();
        DaamkyWorldRender.beginTexturedQuadBuffer(((Float)this.glowRadius.getValue()).floatValue(), ((Float)this.glowStrength.getValue()).floatValue(), (Boolean)this.throughWalls.getValue() != false || this.entityRenderMode.isSelected("\u0421\u043a\u0435\u043b\u0435\u0442"));
        this.renderingGlowBuffer = true;
        try {
            for (n = 0; n < this.visibleLivingEntities.size(); ++n) {
                LivingEntity livingEntity = this.visibleLivingEntities.get(n);
                if (livingEntity.squaredDistanceTo((Vec3d)livingEntityCandidate) > d2) continue;
                this.renderLivingEntity(livingEntity, this.visibleEntityOpacities.getFloat(n));
            }
        }
        finally {
            this.renderingGlowBuffer = false;
            DaamkyWorldRender.flushTexturedQuadBuffer();
        }
    }

    private boolean shouldRenderLivingEntity(LivingEntity livingEntity) {
        if (!livingEntity.isAlive()) {
            return false;
        }
        if (livingEntity == ESP.CLIENT.player) {
            return this.entityTargetSettings.isOptionEnabled("\u0421\u0435\u0431\u044f") && !ESP.CLIENT.options.getPerspective().isFirstPerson();
        }
        if (livingEntity.isInvisible() && !this.entityTargetSettings.isOptionEnabled("\u041d\u0435\u0432\u0438\u0434\u0438\u043c\u044b\u0435")) {
            return false;
        }
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)livingEntity;
            if (FriendManager.getInstance().isFriend(playerEntity)) {
                return this.entityTargetSettings.isOptionEnabled("\u0414\u0440\u0443\u0437\u044c\u044f");
            }
            return this.entityTargetSettings.isOptionEnabled("\u0418\u0433\u0440\u043e\u043a\u0438") && !playerEntity.isSpectator();
        }
        if (livingEntity instanceof HostileEntity) {
            return this.entityTargetSettings.isOptionEnabled("\u041c\u043e\u043d\u0441\u0442\u0440\u044b");
        }
        if (livingEntity instanceof AnimalEntity) {
            return this.entityTargetSettings.isOptionEnabled("\u0416\u0438\u0432\u043e\u0442\u043d\u044b\u0435");
        }
        return false;
    }

    private RgbaColor getLivingEntityColor(LivingEntity livingEntity, float f, int n) {
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)livingEntity;
            if (FriendManager.getInstance().isFriend(playerEntity)) {
                return RgbaColor.rgb(80, 255, 140).withAlpha(n);
            }
        }
        if (this.entityColorMode.isSelected("\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435")) {
            return AnimatedColorUtils.getHealthColor(livingEntity.getHealth() / Math.max(1.0f, livingEntity.getMaxHealth()), n);
        }
        if (this.entityColorMode.isSelected("\u0411\u0435\u043b\u044b\u0439")) {
            return RgbaColor.WHITE.withAlpha(n);
        }
        return AnimatedColorUtils.getAnimatedAccentColor(f + (float)livingEntity.getId() * 0.37f, n);
    }

    private void renderLivingEntity(LivingEntity livingEntity, float f) {
        boolean bl = (Boolean)this.throughWalls.getValue();
        float f2 = ((Float)this.outlineThickness.getValue()).floatValue();
        Box box = DaamkyWorldRender.getInterpolatedBoundingBox(livingEntity).expand(0.05);
        RgbaColor l1I1IlllIlI4 = this.getLivingEntityColor(livingEntity, 0.0f, AnimatedColorUtils.scaleAlpha(230.0f, f));
        switch ((String)this.entityRenderMode.getValue()) {
            case "\u0423\u0433\u043b\u044b": {
                this.renderCornerBox(box, livingEntity, f, f2, bl);
                break;
            }
            case "\u0421\u043a\u0435\u043b\u0435\u0442": {
                this.renderSkeleton(livingEntity, l1I1IlllIlI4, f2, bl);
                break;
            }
            case "\u041a\u043e\u043b\u043e\u043d\u043d\u0430": {
                this.renderCylinder(livingEntity, box, f, f2, bl);
                break;
            }
            default: {
                if (((Boolean)this.filledBoxes.getValue()).booleanValue() && (!this.renderingGlowBuffer || ((Boolean)this.fillGlowEnabled.getValue()).booleanValue())) {
                    DaamkyWorldRender.drawGradientBox(box, this.getLivingEntityColor(livingEntity, 1.5f, AnimatedColorUtils.scaleAlpha(((Float)this.fillAlpha.getValue()).floatValue() * 0.4f, f)), this.getLivingEntityColor(livingEntity, 0.0f, AnimatedColorUtils.scaleAlpha(((Float)this.fillAlpha.getValue()).floatValue(), f)), bl);
                }
                DaamkyWorldRender.drawBoxOutline(box, l1I1IlllIlI4, f2, bl);
            }
        }
        if (((Boolean)this.healthBarEnabled.getValue()).booleanValue() && !this.entityRenderMode.isSelected("\u0421\u043a\u0435\u043b\u0435\u0442") && !this.renderingGlowBuffer) {
            this.renderHealthBar(livingEntity, box, f, bl);
        }
    }

    private void renderCornerBox(Box box, LivingEntity livingEntity, float f, float f2, boolean bl) {
        double d = (box.maxX - box.minX) * 0.3;
        double d2 = (box.maxY - box.minY) * 0.22;
        double d3 = (box.maxZ - box.minZ) * 0.3;
        double[] dArray = new double[]{box.minX, box.maxX};
        double[] dArray2 = new double[]{box.minY, box.maxY};
        double[] dArray3 = new double[]{box.minZ, box.maxZ};
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                for (int k = 0; k < 2; ++k) {
                    double d4 = dArray[i];
                    double d5 = dArray2[j];
                    double d6 = dArray3[k];
                    int n = this.getLivingEntityColor(livingEntity, (float)j * 1.6f, AnimatedColorUtils.scaleAlpha(240.0f, f)).toArgb();
                    double d7 = i == 0 ? d : -d;
                    double d8 = j == 0 ? d2 : -d2;
                    double d9 = k == 0 ? d3 : -d3;
                    DaamkyWorldRender.drawLine(d4, d5, d6, d4 + d7, d5, d6, n, n, f2, bl);
                    DaamkyWorldRender.drawLine(d4, d5, d6, d4, d5 + d8, d6, n, n, f2, bl);
                    DaamkyWorldRender.drawLine(d4, d5, d6, d4, d5, d6 + d9, n, n, f2, bl);
                }
            }
        }
    }

    private void renderCylinder(LivingEntity livingEntity, Box box, float f, float f2, boolean bl) {
        Vec3d entityPosition = DaamkyWorldRender.getInterpolatedPosition(livingEntity);
        double d = Math.max(box.maxX - box.minX, box.maxZ - box.minZ) * 0.6;
        double d2 = box.maxY - box.minY;
        int n = 32;
        float f3 = AnimatedColorUtils.animationTime() * 1.5f;
        for (int i = 0; i < n; ++i) {
            RgbaColor l1I1IlllIlI4;
            double d3 = (double)i * Math.PI * 2.0 / (double)n + (double)f3;
            double d4 = (double)(i + 1) * Math.PI * 2.0 / (double)n + (double)f3;
            Vec3d lowerSegmentStart = entityPosition.add(Math.cos(d3) * d, 0.0, Math.sin(d3) * d);
            Vec3d lowerSegmentEnd = entityPosition.add(Math.cos(d4) * d, 0.0, Math.sin(d4) * d);
            Vec3d upperSegmentStart = lowerSegmentStart.add(0.0, d2, 0.0);
            Vec3d upperSegmentEnd = lowerSegmentEnd.add(0.0, d2, 0.0);
            if (((Boolean)this.filledBoxes.getValue()).booleanValue() && (!this.renderingGlowBuffer || ((Boolean)this.fillGlowEnabled.getValue()).booleanValue())) {
                l1I1IlllIlI4 = this.getLivingEntityColor(livingEntity, (float)i * 0.2f, AnimatedColorUtils.scaleAlpha(((Float)this.fillAlpha.getValue()).floatValue() * 1.6f, f));
                DaamkyWorldRender.drawGradientQuad(lowerSegmentStart, lowerSegmentEnd, upperSegmentEnd, upperSegmentStart, l1I1IlllIlI4, l1I1IlllIlI4, l1I1IlllIlI4.withAlpha(0), l1I1IlllIlI4.withAlpha(0), bl);
            }
            l1I1IlllIlI4 = this.getLivingEntityColor(livingEntity, (float)i * 0.2f, AnimatedColorUtils.scaleAlpha(230.0f, f));
            DaamkyWorldRender.drawLine(lowerSegmentStart, lowerSegmentEnd, l1I1IlllIlI4, f2, bl);
        }
    }

    private void renderHealthBar(LivingEntity livingEntity, Box box, float f, boolean bl) {
        float f2 = Math.clamp(livingEntity.getHealth() / Math.max(1.0f, livingEntity.getMaxHealth()), 0.0f, 1.0f);
        Vec3d healthBarCenter = box.getCenter();
        float f3 = DaamkyWorldRender.getCameraYaw() * ((float)Math.PI / 180);
        double d = -Math.cos(f3);
        double d2 = -Math.sin(f3);
        double d3 = Math.max(box.maxX - box.minX, box.maxZ - box.minZ) * 0.5 + 0.12;
        double d4 = healthBarCenter.x - d * d3;
        double d5 = healthBarCenter.z - d2 * d3;
        double d6 = box.minY;
        double d7 = box.maxY;
        double d8 = d6 + (d7 - d6) * (double)f2;
        DaamkyWorldRender.drawLine(d4, d6, d5, d4, d7, d5, RgbaColor.BLACK.withAlpha(AnimatedColorUtils.scaleAlpha(160.0f, f)).toArgb(), RgbaColor.BLACK.withAlpha(AnimatedColorUtils.scaleAlpha(160.0f, f)).toArgb(), 4.0f, bl);
        DaamkyWorldRender.drawLine(d4, d6, d5, d4, d8, d5, AnimatedColorUtils.getHealthColor(0.0f, AnimatedColorUtils.scaleAlpha(255.0f, f)).mix(AnimatedColorUtils.getHealthColor(f2, 255), 0.6f).withAlpha(AnimatedColorUtils.scaleAlpha(255.0f, f)).toArgb(), AnimatedColorUtils.getHealthColor(f2, AnimatedColorUtils.scaleAlpha(255.0f, f)).toArgb(), 2.2f, bl);
    }

    private void renderSkeleton(LivingEntity livingEntity, RgbaColor l1I1IlllIlI4, float f, boolean bl) {
        float f2 = DaamkyWorldRender.getTickDelta();
        Vec3d entityPosition = DaamkyWorldRender.getInterpolatedPosition(livingEntity);
        float f3 = livingEntity.getHeight() / 1.8f;
        float f4 = MathHelper.lerpAngleDegrees(f2, livingEntity.lastBodyYaw, livingEntity.bodyYaw) * ((float)Math.PI / 180);
        float f5 = MathHelper.lerpAngleDegrees(f2, livingEntity.lastHeadYaw, livingEntity.headYaw) * ((float)Math.PI / 180);
        float f6 = livingEntity.getLerpedPitch(f2) * ((float)Math.PI / 180);
        float f7 = livingEntity.limbAnimator.getAnimationProgress(f2);
        float f8 = Math.min(1.0f, livingEntity.limbAnimator.getAmplitude(f2));
        float f9 = MathHelper.cos(f7 * 0.6662f) * 1.1f * f8;
        boolean bl2 = livingEntity.isInSneakingPose();
        double d = -Math.sin(f4);
        double d2 = Math.cos(f4);
        double d3 = -Math.cos(f4);
        double d4 = -Math.sin(f4);
        double d5 = (bl2 ? 0.62 : 0.72) * (double)f3;
        double d6 = (bl2 ? 1.2 : 1.38) * (double)f3;
        double d7 = (bl2 ? 1.27 : 1.45) * (double)f3;
        double d8 = d5;
        double d9 = 0.62 * (double)f3;
        double d10 = 0.12 * (double)f3;
        double d11 = 0.3 * (double)f3;
        Vec3d bodyCenter = entityPosition.add(0.0, d5, 0.0);
        Vec3d headPosition = entityPosition.add(bl2 ? d * 0.18 * (double)f3 : 0.0, d7, bl2 ? d2 * 0.18 * (double)f3 : 0.0);
        Vec3d lookDirection = new Vec3d(-Math.sin(f5) * Math.cos(f6), -Math.sin(f6), Math.cos(f5) * Math.cos(f6));
        Vec3d headTopPosition = headPosition.add(0.0, 0.28 * (double)f3, 0.0).add(lookDirection.multiply(0.06 * (double)f3));
        Vec3d leftArmStart = bodyCenter.add(d3 * d10, 0.0, d4 * d10);
        Vec3d rightArmStart = bodyCenter.add(-d3 * d10, 0.0, -d4 * d10);
        Vec3d leftArmEnd = leftArmStart.add(d * Math.sin(f9) * d8, -Math.cos(f9) * d8, d2 * Math.sin(f9) * d8);
        Vec3d rightArmEnd = rightArmStart.add(d * Math.sin(-f9) * d8, -Math.cos(f9) * d8, d2 * Math.sin(-f9) * d8);
        Vec3d hipPosition = headPosition.add(0.0, -(d7 - d6), 0.0);
        Vec3d leftLegStart = hipPosition.add(d3 * d11, 0.0, d4 * d11);
        Vec3d rightLegStart = hipPosition.add(-d3 * d11, 0.0, -d4 * d11);
        float f10 = -f9 * 0.9f;
        Vec3d leftLegEnd = leftLegStart.add(d * Math.sin(f10) * d9, -Math.cos(f10) * d9, d2 * Math.sin(f10) * d9);
        Vec3d rightLegEnd = rightLegStart.add(d * Math.sin(-f10) * d9, -Math.cos(f10) * d9, d2 * Math.sin(-f10) * d9);
        RgbaColor l1I1IlllIlI42 = l1I1IlllIlI4;
        RgbaColor l1I1IlllIlI43 = AnimatedColorUtils.getAnimatedThemeColor(livingEntity.getId(), l1I1IlllIlI4.alpha());
        DaamkyWorldRender.drawGradientLine(bodyCenter, headPosition, l1I1IlllIlI43, l1I1IlllIlI42, f, bl);
        DaamkyWorldRender.drawLine(headPosition, headTopPosition, l1I1IlllIlI42, f, bl);
        DaamkyWorldRender.drawLine(leftArmStart, rightArmStart, l1I1IlllIlI43, f, bl);
        DaamkyWorldRender.drawLine(leftLegStart, rightLegStart, l1I1IlllIlI42, f, bl);
        DaamkyWorldRender.drawGradientLine(leftArmStart, leftArmEnd, l1I1IlllIlI43, l1I1IlllIlI42, f, bl);
        DaamkyWorldRender.drawGradientLine(rightArmStart, rightArmEnd, l1I1IlllIlI43, l1I1IlllIlI42, f, bl);
        DaamkyWorldRender.drawGradientLine(leftLegStart, leftLegEnd, l1I1IlllIlI42, l1I1IlllIlI43, f, bl);
        DaamkyWorldRender.drawGradientLine(rightLegStart, rightLegEnd, l1I1IlllIlI42, l1I1IlllIlI43, f, bl);
    }
}

