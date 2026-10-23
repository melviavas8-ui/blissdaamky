/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  project.melvi.metadata.Allow2Generation
 *  project.melvi.metadata.Allow2Generation$GenerationLevel
 */
package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.daamky.Accent;
import dev.daamky.DynamicTransforms;
import dev.daamky.WorldRenderPipelines;
import dev.daamky.DaamkyWorldRender;
import dev.daamky.MultiOptionSetting;
import dev.daamky.FloatSetting;
import dev.daamky.ModuleCategory;
import dev.daamky.ModuleStructure;
import dev.daamky.GpuTextureViewCache;
import dev.daamky.Trails;
import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventRender3D;
import dev.daamky.core.event.impl.EventUpdate;
import dev.daamky.FriendManager;
import java.awt.Color;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import project.melvi.metadata.Allow2Generation;

public final class Trails
extends ModuleStructure {
    private static final Identifier BLOOM_TEXTURE = Trails.createTextureIdentifier("bloom");
    private static final Identifier CORE_TEXTURE = Trails.createTextureIdentifier("core");
    private static final Identifier DASH_BLOOM_TEXTURE = Trails.createTextureIdentifier("dashbloom");
    private static final Identifier GLOW_TEXTURE = Trails.createTextureIdentifier("glow");
    private static final int[] GROUP_FRAME_COUNTS = new int[]{11, 23, 32, 16, 32};
    private final MultiOptionSetting trailModeSetting = new MultiOptionSetting("\u0420\u0435\u0436\u0438\u043c", "\u0414\u0438\u043c\u0430", "\u0414\u0438\u043c\u0430", "\u0420\u044b\u0432\u043e\u043a", "\u042d\u043d\u0435\u0440\u0433\u0438\u044f", "\u041f\u0440\u0438\u0437\u0440\u0430\u043a\u0438");
    private final FloatSetting mistLifetimeSetting = new FloatSetting("\u0412\u0440\u0435\u043c\u044f \u0436\u0438\u0437\u043d\u0438", 1.1f, 0.35f, 2.5f, 0.05f);
    private final FloatSetting mistDensitySetting = new FloatSetting("\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c", 12.0f, 4.0f, 20.0f, 0.5f);
    private final FloatSetting mistScaleSetting = new FloatSetting("\u041c\u0430\u0441\u0448\u0442\u0430\u0431", 1.0f, 0.55f, 1.8f, 0.05f);
    private final dev.daamky.BooleanSetting mistThemeColorSetting = new dev.daamky.BooleanSetting("\u0426\u0432\u0435\u0442 \u0442\u0435\u043c\u044b", true);
    private final dev.daamky.BooleanSetting drawSelfTrailSetting = new dev.daamky.BooleanSetting("\u0421\u0435\u0431\u0435", true);
    private final dev.daamky.BooleanSetting firstPersonTrailSetting = new dev.daamky.BooleanSetting("\u0412 \u043f\u0435\u0440\u0432\u043e\u043c \u043b\u0438\u0446\u0435", true);
    private final dev.daamky.BooleanSetting drawOtherPlayersSetting = new dev.daamky.BooleanSetting("\u0418\u0433\u0440\u043e\u043a\u0438", false);
    private final dev.daamky.BooleanSetting drawFriendTrailsSetting = new dev.daamky.BooleanSetting("\u0414\u0440\u0443\u0437\u044c\u044f", true);
    private final MultiOptionSetting trailColorModeSetting = new MultiOptionSetting("\u0420\u0435\u0436\u0438\u043c \u0446\u0432\u0435\u0442\u0430", "\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u0430\u044f \u043f\u0430\u043b\u0438\u0442\u0440\u0430", "\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u0430\u044f \u043f\u0430\u043b\u0438\u0442\u0440\u0430", "\u0421\u0432\u043e\u0439", "\u041a\u043b\u0438\u0435\u043d\u0442", "\u0420\u0430\u0434\u0443\u0433\u0430");
    private final FloatSetting maximumPlayerDistanceSetting = new FloatSetting("\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u0430\u044f \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", 25.0f, 15.0f, 100.0f, 1.0f);
    private final dev.daamky.BooleanSetting smoothMovementSetting = new dev.daamky.BooleanSetting("\u0421\u0433\u043b\u0430\u0436\u0438\u0432\u0430\u043d\u0438\u0435 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f", false);
    private final dev.daamky.BooleanSetting renderSegmentsSetting = new dev.daamky.BooleanSetting("\u0421\u0435\u0433\u043c\u0435\u043d\u0442\u044b", false);
    private final dev.daamky.BooleanSetting renderPointsSetting = new dev.daamky.BooleanSetting("\u0422\u043e\u0447\u043a\u0438", true);
    private final dev.daamky.BooleanSetting enableLightingSetting = new dev.daamky.BooleanSetting("\u041e\u0441\u0432\u0435\u0449\u0435\u043d\u0438\u0435", true);
    private final FloatSetting trailDistanceSetting = new FloatSetting("\u0414\u043b\u0438\u043d\u0430 \u0441\u043b\u0435\u0434\u0430", 0.75f, 0.5f, 1.5f, 0.05f);
    private final MultiOptionSetting renderQualitySetting = new MultiOptionSetting("\u041a\u0430\u0447\u0435\u0441\u0442\u0432\u043e \u043e\u0442\u0440\u0438\u0441\u043e\u0432\u043a\u0438", "\u041a\u0430\u0447\u0435\u0441\u0442\u0432\u043e", "\u041f\u0440\u043e\u0438\u0437\u0432\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c", "\u041a\u0430\u0447\u0435\u0441\u0442\u0432\u043e", "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u043a\u0430\u0447\u0435\u0441\u0442\u0432\u043e");
    private final FloatSetting energyTrailLengthSetting = new FloatSetting("\u0414\u043b\u0438\u043d\u0430", 1.9f, 0.35f, 2.0f, 0.05f);
    private final FloatSetting energyTrailWidthSetting = new FloatSetting("\u0428\u0438\u0440\u0438\u043d\u0430", 0.38f, 0.08f, 0.6f, 0.02f);
    private final dev.daamky.BooleanSetting energyThemeColorSetting = new dev.daamky.BooleanSetting("\u0410\u0432\u0442\u043e \u0446\u0432\u0435\u0442 \u043f\u043e\u0434 \u0442\u0435\u043c\u0443", true);
    private final FloatSetting ghostTrailLengthSetting = new FloatSetting("\u0414\u043b\u0438\u043d\u0430 \u0448\u043b\u0435\u0439\u0444\u0430", 25.0f, 10.0f, 50.0f, 1.0f);
    private final FloatSetting ghostParticleSizeSetting = new FloatSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0447\u0430\u0441\u0442\u0438\u0446", 0.6f, 0.3f, 1.2f, 0.05f);
    private final FloatSetting ghostAnimationSpeedSetting = new FloatSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u043e\u0432", 3.0f, 1.0f, 10.0f, 0.5f);
    private final Random random = new Random(1234567891L);
    private final List<TrailInterpolationState> energyTrailParticles = new ArrayList<TrailInterpolationState>();
    private final List<PlayerTrail> playerTrails = new ArrayList<PlayerTrail>();
    private final Map<Integer, Vec3d> previousPlayerPositions = new HashMap<Integer, Vec3d>();
    private final List<TrailTextureDescriptor> dashTrailTextures = new ArrayList<TrailTextureDescriptor>();
    private final List<List<TrailTextureDescriptor>> groupedDashTrailTextures = new ArrayList<List<TrailTextureDescriptor>>();
    private final List<Trails.TimedPosition> ghostTrailPositions = new ArrayList<Trails.TimedPosition>();
    private final TrailHistory trailHistory = new TrailHistory();
    private boolean texturesInitialized;
    private long lastGhostTrailSampleMillis;
    private Vec3d lastGhostTrailPosition;
    private ClientWorld lastClientWorld;

    public Trails() {
        super("Trails", "\u0428\u043b\u0435\u0439\u0444 \u0440\u044b\u0432\u043a\u0430", ModuleCategory.RENDER);
        this.mistLifetimeSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0414\u0438\u043c\u0430"));
        this.mistDensitySetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0414\u0438\u043c\u0430"));
        this.mistScaleSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0414\u0438\u043c\u0430"));
        this.mistThemeColorSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0414\u0438\u043c\u0430"));
        this.drawSelfTrailSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.firstPersonTrailSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a") && (Boolean)this.drawSelfTrailSetting.getValue());
        this.drawOtherPlayersSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.drawFriendTrailsSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.trailColorModeSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.maximumPlayerDistanceSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a") && ((Boolean)this.drawOtherPlayersSetting.getValue() || (Boolean)this.drawFriendTrailsSetting.getValue()));
        this.smoothMovementSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.renderSegmentsSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.renderPointsSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.enableLightingSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.trailDistanceSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.renderQualitySetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a"));
        this.energyTrailLengthSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u042d\u043d\u0435\u0440\u0433\u0438\u044f"));
        this.energyTrailWidthSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u042d\u043d\u0435\u0440\u0433\u0438\u044f"));
        this.energyThemeColorSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u042d\u043d\u0435\u0440\u0433\u0438\u044f"));
        this.ghostTrailLengthSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u041f\u0440\u0438\u0437\u0440\u0430\u043a\u0438"));
        this.ghostParticleSizeSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u041f\u0440\u0438\u0437\u0440\u0430\u043a\u0438"));
        this.ghostAnimationSpeedSetting.visibleWhen(() -> this.trailModeSetting.isSelected("\u041f\u0440\u0438\u0437\u0440\u0430\u043a\u0438"));
        this.addSettings(this.trailModeSetting, this.mistLifetimeSetting, this.mistDensitySetting, this.mistScaleSetting, this.mistThemeColorSetting, this.drawSelfTrailSetting, this.firstPersonTrailSetting, this.drawOtherPlayersSetting, this.drawFriendTrailsSetting, this.trailColorModeSetting, this.maximumPlayerDistanceSetting, this.smoothMovementSetting, this.renderSegmentsSetting, this.renderPointsSetting, this.enableLightingSetting, this.trailDistanceSetting, this.renderQualitySetting, this.energyTrailLengthSetting, this.energyTrailWidthSetting, this.energyThemeColorSetting, this.ghostTrailLengthSetting, this.ghostParticleSizeSetting, this.ghostAnimationSpeedSetting);
    }

    private static Identifier createTextureIdentifier(String textureName) {
        return Identifier.of("daamky", "textures/trails/" + textureName + ".png");
    }

    private boolean usesCustomTrailColor() {
        return this.trailModeSetting.isSelected("\u0414\u0438\u043c\u0430") && !(Boolean)this.mistThemeColorSetting.getValue() || this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a") && this.trailColorModeSetting.isSelected("\u0421\u0432\u043e\u0439");
    }

    @Override
    public String getDisplayInfo() {
        return (String)this.trailModeSetting.getValue();
    }

    @Override
    @Allow2Generation(level=Allow2Generation.GenerationLevel.FAST)
    protected void onDisable() {
        this.clearTrailState();
    }

    private void clearTrailState() {
        this.trailHistory.clear();
        this.energyTrailParticles.clear();
        this.playerTrails.clear();
        this.previousPlayerPositions.clear();
        this.ghostTrailPositions.clear();
        this.lastGhostTrailSampleMillis = 0L;
        this.lastGhostTrailPosition = null;
        this.lastClientWorld = null;
    }

    private static int getDefaultTrailColor() {
        return Accent.getAccentColorWithAlpha(255).toArgb();
    }

    private static int packRgba(int n, int n2, int n3, int n4) {
        return MathHelper.clamp(n4, 0, 255) << 24 | MathHelper.clamp(n, 0, 255) << 16 | MathHelper.clamp(n2, 0, 255) << 8 | MathHelper.clamp(n3, 0, 255);
    }

    private static int withAlpha(int n, float f) {
        return n & 0xFFFFFF | MathHelper.clamp(Math.round(f * 255.0f), 0, 255) << 24;
    }

    private static int blendColors(int n, int n2, float f) {
        f = MathHelper.clamp(f, 0.0f, 1.0f);
        int n3 = n >> 16 & 0xFF;
        int n4 = n >> 8 & 0xFF;
        int n5 = n & 0xFF;
        int n6 = n >>> 24;
        int n7 = n2 >> 16 & 0xFF;
        int n8 = n2 >> 8 & 0xFF;
        int n9 = n2 & 0xFF;
        int n10 = n2 >>> 24;
        return Trails.packRgba((int)((float)n3 + (float)(n7 - n3) * f), (int)((float)n4 + (float)(n8 - n4) * f), (int)((float)n5 + (float)(n9 - n5) * f), (int)((float)n6 + (float)(n10 - n6) * f));
    }

    private static int scaleColorBrightness(int n, float f) {
        f = MathHelper.clamp(f, 0.0f, 1.0f);
        return Trails.packRgba((int)((float)(n >> 16 & 0xFF) * f), (int)((float)(n >> 8 & 0xFF) * f), (int)((float)(n & 0xFF) * f), n >>> 24);
    }

    private float getDashSubdivisionFactor() {
        return switch ((String)this.renderQualitySetting.getValue()) {
            case "\u041f\u0440\u043e\u0438\u0437\u0432\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c" -> 0.3333333f;
            case "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u043a\u0430\u0447\u0435\u0441\u0442\u0432\u043e" -> 5.0f;
            default -> 1.0f;
        };
    }

    private float getDashAnimationRate() {
        return switch ((String)this.renderQualitySetting.getValue()) {
            case "\u041f\u0440\u043e\u0438\u0437\u0432\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c" -> 1.1f;
            case "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u043a\u0430\u0447\u0435\u0441\u0442\u0432\u043e" -> 0.333333f;
            default -> 1.0f;
        };
    }

    private float getSparkleSpawnChance() {
        return switch ((String)this.renderQualitySetting.getValue()) {
            case "\u041f\u0440\u043e\u0438\u0437\u0432\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c" -> 0.07f;
            case "\u041c\u0430\u043a\u0441\u0438\u043c\u0430\u043b\u044c\u043d\u043e\u0435 \u043a\u0430\u0447\u0435\u0441\u0442\u0432\u043e" -> 0.0175f;
            default -> 0.035f;
        };
    }

    private int getConfiguredTrailColor() {
        return switch ((String)this.trailColorModeSetting.getValue()) {
            case "\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u0430\u044f \u043f\u0430\u043b\u0438\u0442\u0440\u0430" -> Color.getHSBColor((float)this.random.nextInt(361) / 360.0f, 1.0f, 1.0f).getRGB() | 0xFF000000;
            case "\u0420\u0430\u0434\u0443\u0433\u0430" -> Color.getHSBColor((float)(System.currentTimeMillis() % 1000L) / 1000.0f, 0.8f, 1.0f).getRGB() | 0xFF000000;
            default -> Trails.getDefaultTrailColor();
        };
    }

    private int getTrailLifetimeMillis() {
        return (int)((float)(550 + this.random.nextInt(300)) * ((Float)this.trailDistanceSetting.getValue()).floatValue());
    }

    private boolean shouldTrackPlayer(PlayerEntity playerEntity) {
        if (playerEntity == Trails.CLIENT.player) {
            return (Boolean)this.drawSelfTrailSetting.getValue() != false && ((Boolean)this.firstPersonTrailSetting.getValue() != false || !Trails.CLIENT.options.getPerspective().isFirstPerson());
        }
        if (Trails.CLIENT.player.distanceTo(playerEntity) > ((Float)this.maximumPlayerDistanceSetting.getValue()).floatValue()) {
            return false;
        }
        return FriendManager.getInstance().isFriend(playerEntity) ? (Boolean)this.drawFriendTrailsSetting.getValue() : (Boolean)this.drawOtherPlayersSetting.getValue();
    }

    private void loadTrailTextures() {
        int n;
        if (this.texturesInitialized) {
            return;
        }
        this.texturesInitialized = true;
        this.dashTrailTextures.clear();
        this.groupedDashTrailTextures.clear();
        for (n = 1; n <= 21; ++n) {
            this.dashTrailTextures.add(this.loadTextureDescriptor(Identifier.of("daamky", "textures/trails/dash/dashcubic" + n + ".png")));
        }
        for (n = 0; n < GROUP_FRAME_COUNTS.length; ++n) {
            ArrayList<TrailTextureDescriptor> arrayList = new ArrayList<TrailTextureDescriptor>();
            for (int i = 1; i <= GROUP_FRAME_COUNTS[n]; ++i) {
                arrayList.add(this.loadTextureDescriptor(Identifier.of("daamky", "textures/trails/group" + (n + 1) + "/dashcubic" + i + ".png")));
            }
            this.groupedDashTrailTextures.add(arrayList);
        }
    }

    private TrailTextureDescriptor loadTextureDescriptor(Identifier identifier) {
        int n;
        int n2;
        block14: {
            n2 = 48;
            n = 48;
            try {
                Optional<Resource> optional = CLIENT.getResourceManager().getResource(identifier);
                if (!optional.isPresent()) break block14;
                try (InputStream inputStream = optional.get().getInputStream();
                     NativeImage nativeImage = NativeImage.read(inputStream);){
                    n2 = nativeImage.getWidth();
                    n = nativeImage.getHeight();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return new TrailTextureDescriptor(identifier, n2, n);
    }

    @EventTarget
    public void onUpdate(EventUpdate eventUpdate) {
        if (Trails.CLIENT.player == null || Trails.CLIENT.world == null) {
            return;
        }
        if (!this.trailModeSetting.isSelected("\u041f\u0440\u0438\u0437\u0440\u0430\u043a\u0438")) {
            this.trailHistory.clear();
        }
        if (!this.trailModeSetting.isSelected("\u0420\u044b\u0432\u043e\u043a")) {
            return;
        }
        this.loadTrailTextures();
        for (PlayerEntity playerEntity : Trails.CLIENT.world.getPlayers()) {
            if (!this.shouldTrackPlayer(playerEntity)) continue;
            Vec3d currentPosition = playerEntity.getEntityPos();
            Vec3d previousPosition = this.previousPlayerPositions.put(playerEntity.getId(), currentPosition);
            if (previousPosition == null) continue;
            double d = currentPosition.x - previousPosition.x;
            double d2 = currentPosition.y - previousPosition.y;
            double d3 = currentPosition.z - previousPosition.z;
            if (Math.sqrt(d * d + d3 * d3) < 0.08) continue;
            float f = this.getDashSubdivisionFactor();
            double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
            int n2 = MathHelper.clamp((int)(d4 / 0.08 * (double)f), 1, (int)(16.0f * f));
            boolean bl = (Boolean)this.renderSegmentsSetting.getValue() != false || (Boolean)this.renderPointsSetting.getValue() != false;
            for (int i = 0; i < n2; ++i) {
                while (this.playerTrails.size() >= 220) {
                    this.playerTrails.removeFirst();
                }
                this.playerTrails.add(new PlayerTrail(playerEntity, previousPosition, currentPosition, (float)i / (float)n2, this.getTrailLifetimeMillis(), bl));
            }
        }
        this.previousPlayerPositions.keySet().removeIf(n -> Trails.CLIENT.world.getEntityById((int)n) == null);
        for (PlayerTrail iI1Il11l111II11IIl2 : this.playerTrails) {
            if (!(iI1Il11l111II11IIl2.progress() >= 1.0f) || iI1Il11l111II11IIl2.targetOpacity == 0.0f) continue;
            iI1Il11l111II11IIl2.targetOpacity = 0.0f;
        }
        this.playerTrails.removeIf(iI1Il11l111II11IIl -> iI1Il11l111II11IIl.progress() >= 1.0f && iI1Il11l111II11IIl.targetOpacity == 0.0f && iI1Il11l111II11IIl.opacity < 0.02f);
        boolean bl = (Boolean)this.smoothMovementSetting.getValue();
        PlayerTrail previousParticle = null;
        for (PlayerTrail iI1Il11l111II11IIl3 : this.playerTrails) {
            if (!(iI1Il11l111II11IIl3.opacity > 0.05f)) continue;
            if (previousParticle != null) {
                previousParticle.updateTowardNextTrailPoint(bl ? iI1Il11l111II11IIl3 : null);
            }
            previousParticle = iI1Il11l111II11IIl3;
        }
        if (previousParticle != null) {
            previousParticle.updateTowardNextTrailPoint(null);
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D eventRender3D) {
        if (Trails.CLIENT.player == null || Trails.CLIENT.world == null) {
            this.clearTrailState();
            return;
        }
        if (this.lastClientWorld != null && this.lastClientWorld != Trails.CLIENT.world) {
            this.clearTrailState();
        }
        this.lastClientWorld = Trails.CLIENT.world;
        float f = eventRender3D.partialTicks();
        switch ((String)this.trailModeSetting.getValue()) {
            case "\u041f\u0440\u0438\u0437\u0440\u0430\u043a\u0438": {
                this.ghostTrailPositions.clear();
                this.energyTrailParticles.clear();
                this.playerTrails.clear();
                this.lastGhostTrailPosition = null;
                this.renderGhostTrail(f);
                break;
            }
            case "\u0420\u044b\u0432\u043e\u043a": {
                this.ghostTrailPositions.clear();
                this.renderPlayerTrails(f);
                break;
            }
            case "\u042d\u043d\u0435\u0440\u0433\u0438\u044f": {
                this.energyTrailParticles.clear();
                this.lastGhostTrailPosition = null;
                this.renderEnergyRibbon(f);
                break;
            }
            default: {
                this.ghostTrailPositions.clear();
                this.renderEnergyMist(f);
            }
        }
    }

    private Vec3d getInterpolatedPlayerPosition(float f) {
        return new Vec3d(MathHelper.lerp((double)f, Trails.CLIENT.player.lastX, Trails.CLIENT.player.getX()), MathHelper.lerp((double)f, Trails.CLIENT.player.lastY, Trails.CLIENT.player.getY()), MathHelper.lerp((double)f, Trails.CLIENT.player.lastZ, Trails.CLIENT.player.getZ()));
    }

    private static void writeBillboardQuad(BufferBuilder bufferBuilder, Vec3d centerPosition, Vec3d widthAxis, Vec3d heightAxis, Vec3d depthAxis, double d, double d2, double d3, int n, int n2, int n3, int n4) {
        double d4 = d2 * 0.5;
        double d5 = d3 * 0.5;
        Vec3d offsetCenter = centerPosition.add(depthAxis.multiply(d));
        Vec3d topLeft = offsetCenter.add(widthAxis.multiply(-d4)).add(heightAxis.multiply(d5));
        Vec3d topRight = offsetCenter.add(widthAxis.multiply(d4)).add(heightAxis.multiply(d5));
        Vec3d bottomRight = offsetCenter.add(widthAxis.multiply(d4)).add(heightAxis.multiply(-d5));
        Vec3d bottomLeft = offsetCenter.add(widthAxis.multiply(-d4)).add(heightAxis.multiply(-d5));
        bufferBuilder.vertex((float)topLeft.x, (float)topLeft.y, (float)topLeft.z).texture(0.0f, 0.0f).color(n);
        bufferBuilder.vertex((float)topRight.x, (float)topRight.y, (float)topRight.z).texture(1.0f, 0.0f).color(n2);
        bufferBuilder.vertex((float)bottomRight.x, (float)bottomRight.y, (float)bottomRight.z).texture(1.0f, 1.0f).color(n3);
        bufferBuilder.vertex((float)bottomLeft.x, (float)bottomLeft.y, (float)bottomLeft.z).texture(0.0f, 1.0f).color(n4);
    }

    private static void writeRotatedTextureQuad(BufferBuilder bufferBuilder, double d, double d2, double d3, float f, float f2, float f3, float f4, int n) {
        Matrix4f matrix4f = new Matrix4f().translation((float)d, (float)d2, (float)d3).rotateY(-f3 * ((float)Math.PI / 180)).rotateX(f4 * ((float)Math.PI / 180));
        float f5 = f * 0.5f;
        float f6 = f2 * 0.5f;
        Vector3f vector3f = new Vector3f();
        matrix4f.transformPosition(-f5, -f6, 0.0f, vector3f);
        bufferBuilder.vertex(vector3f.x, vector3f.y, vector3f.z).texture(0.0f, 0.0f).color(n);
        matrix4f.transformPosition(-f5, f6, 0.0f, vector3f);
        bufferBuilder.vertex(vector3f.x, vector3f.y, vector3f.z).texture(0.0f, 1.0f).color(n);
        matrix4f.transformPosition(f5, f6, 0.0f, vector3f);
        bufferBuilder.vertex(vector3f.x, vector3f.y, vector3f.z).texture(1.0f, 1.0f).color(n);
        matrix4f.transformPosition(f5, -f6, 0.0f, vector3f);
        bufferBuilder.vertex(vector3f.x, vector3f.y, vector3f.z).texture(1.0f, 0.0f).color(n);
    }

    private static BufferBuilder createPositionTextureColorBuffer() {
        return DynamicTransforms.createBufferBuilder(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    private static void drawTextureBuffer(BufferBuilder bufferBuilder, Identifier identifier, RenderPipeline renderPipeline) {
        GpuTextureView gpuTextureView = GpuTextureViewCache.II1Il11l111II11IIl(identifier);
        if (gpuTextureView == null) {
            DynamicTransforms.submitDraw(bufferBuilder, VertexFormat.DrawMode.QUADS, renderPipeline, GpuTextureViewCache.II1Il11l111II11IIl(GLOW_TEXTURE));
            return;
        }
        DynamicTransforms.submitDraw(bufferBuilder, VertexFormat.DrawMode.QUADS, renderPipeline, gpuTextureView);
    }

    private Vec3d[] getCameraPlaneBasis() {
        Vector3f[] vector3fArray = DaamkyWorldRender.getCameraPlaneBasis();
        Vec3d cameraRight = new Vec3d(vector3fArray[0].x, vector3fArray[0].y, vector3fArray[0].z).normalize();
        Vec3d cameraUp = new Vec3d(vector3fArray[1].x, vector3fArray[1].y, vector3fArray[1].z).normalize();
        return new Vec3d[]{cameraRight, cameraUp, cameraRight.crossProduct(cameraUp).normalize()};
    }

    private void renderEnergyMist(float f) {
        if (Trails.CLIENT.options.getPerspective().isFirstPerson()) {
            return;
        }
        this.addEnergyTrailPoint(this.getInterpolatedPlayerPosition(f));
        long l = System.currentTimeMillis();
        this.energyTrailParticles.removeIf(l1I1IlllIlI2 -> l1I1IlllIlI2.isExpired(l));
        if (this.energyTrailParticles.isEmpty()) {
            return;
        }
        Vec3d cameraPosition = DaamkyWorldRender.getCameraPosition();
        Vec3d[] vec3dArray = this.getCameraPlaneBasis();
        Vec3d cameraRight = vec3dArray[0].multiply(-1.0);
        Vec3d cameraUp = vec3dArray[1];
        Vec3d cameraNormal = vec3dArray[2];
        int n = Trails.getDefaultTrailColor();
        int n2 = n >> 16 & 0xFF;
        int n3 = n >> 8 & 0xFF;
        int n4 = n & 0xFF;
        BufferBuilder bufferBuilder = Trails.createPositionTextureColorBuffer();
        BufferBuilder bufferBuilder2 = Trails.createPositionTextureColorBuffer();
        for (TrailInterpolationState l1I1IlllIlI3 : this.energyTrailParticles) {
            float f2 = l1I1IlllIlI3.progress(l);
            float f3 = l1I1IlllIlI3.fadeAlpha(l);
            int n5 = MathHelper.clamp((int)(255.0f * f3), 0, 255);
            if (n5 <= 4 || f3 <= 0.02f) continue;
            Vec3d particleRelativePosition = l1I1IlllIlI3.position.subtract(cameraPosition);
            float f4 = (float)l1I1IlllIlI3.direction.dotProduct(cameraRight);
            float f5 = (float)l1I1IlllIlI3.direction.dotProduct(cameraUp);
            double d = Math.toRadians((float)Math.toDegrees(Math.atan2(f5, f4)) + l1I1IlllIlI3.rotationDegrees);
            double d2 = Math.cos(d);
            double d3 = Math.sin(d);
            Vec3d rotatedCameraRight = cameraRight.multiply(d2).add(cameraUp.multiply(-d3));
            Vec3d rotatedCameraUp = cameraRight.multiply(d3).add(cameraUp.multiply(d2));
            float f6 = 1.0f - f2 * 0.2f;
            double d4 = l1I1IlllIlI3.startWidth * f6;
            double d5 = l1I1IlllIlI3.endWidth * (1.05f - f2 * 0.12f);
            int n6 = Trails.packRgba(n2, n3, n4, (int)((float)n5 * 0.28f));
            double d6 = d4 * (1.55 - (double)f2 * 0.2);
            Trails.writeBillboardQuad(bufferBuilder, particleRelativePosition, rotatedCameraRight, rotatedCameraUp, cameraNormal, 0.0, d6, d6, n6, n6, n6, n6);
            double d7 = Math.max(d4, d5) * 1.15;
            int n7 = Trails.packRgba(n2, n3, n4, (int)((float)n5 * 0.95f));
            int n8 = Trails.packRgba(255, 255, 255, (int)((float)n5 * 0.65f));
            Trails.writeBillboardQuad(bufferBuilder2, particleRelativePosition, rotatedCameraRight, rotatedCameraUp, cameraNormal, 0.002, d7, d7, n7, n7, n7, n7);
            Trails.writeBillboardQuad(bufferBuilder2, particleRelativePosition, rotatedCameraRight, rotatedCameraUp, cameraNormal, 0.004, d7 * 0.72, d7 * 0.72, n8, n8, n8, n8);
        }
        Trails.drawTextureBuffer(bufferBuilder, BLOOM_TEXTURE, WorldRenderPipelines.I1l11IIlIIIll1l1lI1II);
        Trails.drawTextureBuffer(bufferBuilder2, CORE_TEXTURE, WorldRenderPipelines.I1l11IIlIIIll1l1lI1II);
    }

    private void addEnergyTrailPoint(Vec3d playerPosition) {
        if (Trails.CLIENT.player.isRiding() || this.lastGhostTrailPosition == null || this.lastGhostTrailPosition.squaredDistanceTo(playerPosition) > 4.0) {
            this.lastGhostTrailPosition = playerPosition;
            return;
        }
        Vec3d movementDelta = playerPosition.subtract(this.lastGhostTrailPosition);
        double d = movementDelta.length();
        if (d < 0.12) {
            return;
        }
        Vec3d movementDirection = movementDelta.multiply(1.0 / d);
        Vec3d backwardDirection = movementDirection.multiply(-1.0);
        Vec3d horizontalNormal = new Vec3d(-movementDirection.z, 0.0, movementDirection.x);
        horizontalNormal = horizontalNormal.lengthSquared() < 1.0E-4 ? new Vec3d(1.0, 0.0, 0.0) : horizontalNormal.normalize();
        float f = Trails.CLIENT.player.getHeight() - (Trails.CLIENT.player.isSneaking() ? 0.15f : 0.0f);
        double d2 = Math.max(0.06, 0.12 * (12.0 / Math.max(4.0, (double)((Float)this.mistDensitySetting.getValue()).floatValue())));
        int n = MathHelper.clamp((int)Math.floor(d / d2), 1, 12);
        for (int i = 1; i <= n; ++i) {
            Vec3d samplePosition = this.lastGhostTrailPosition.add(movementDelta.multiply((double)i / (double)n)).add(0.0, (double)f * 0.2, 0.0);
            this.spawnEnergyTrailParticles(samplePosition, backwardDirection, horizontalNormal, f, d2);
        }
        this.lastGhostTrailPosition = playerPosition;
    }

    private void spawnEnergyTrailParticles(Vec3d spawnPosition, Vec3d trailDirection, Vec3d lateralDirection, float f, double d) {
        while (this.energyTrailParticles.size() >= 160) {
            this.energyTrailParticles.removeFirst();
        }
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        long l = Math.max(350L, (long)(((Float)this.mistLifetimeSetting.getValue()).floatValue() * 1000.0f));
        float f2 = ((Float)this.mistScaleSetting.getValue()).floatValue();
        float f3 = MathHelper.clamp((float)(d * 8.0), 0.0f, 1.0f);
        int n = 2 + threadLocalRandom.nextInt(2);
        for (int i = 0; i < n; ++i) {
            if (this.energyTrailParticles.size() >= 160) {
                this.energyTrailParticles.removeFirst();
            }
            double d2 = (threadLocalRandom.nextDouble() - 0.1) * (double)f * 0.7;
            double d3 = (threadLocalRandom.nextDouble() - 0.5) * (0.22 + (double)f3 * 0.12);
            double d4 = threadLocalRandom.nextDouble() * (0.14 + (double)f3 * 0.12);
            Vec3d particlePosition = spawnPosition.add(0.0, d2, 0.0).add(lateralDirection.multiply(d3)).add(trailDirection.multiply(d4));
            Vec3d particleDirection = trailDirection.add(lateralDirection.multiply((threadLocalRandom.nextDouble() - 0.5) * 0.3)).add(0.0, (threadLocalRandom.nextDouble() - 0.5) * 0.14, 0.0);
            particleDirection = particleDirection.lengthSquared() < 1.0E-4 ? trailDirection : particleDirection.normalize();
            float f4 = f2 * (0.42f + threadLocalRandom.nextFloat() * 0.14f + f3 * 0.08f);
            float f5 = f4 * (0.9f + threadLocalRandom.nextFloat() * 0.1f);
            float f6 = (float)((threadLocalRandom.nextDouble() - 0.5) * 12.0);
            this.energyTrailParticles.add(new TrailInterpolationState(particlePosition, particleDirection, f4, f5, f6, l));
        }
    }

    private void renderEnergyRibbon(float f) {
        int[] nArray;
        if (Trails.CLIENT.options.getPerspective().isFirstPerson()) {
            this.ghostTrailPositions.clear();
            this.lastGhostTrailSampleMillis = 0L;
            return;
        }
        long l = System.currentTimeMillis();
        Vec3d playerPosition = this.getInterpolatedPlayerPosition(f);
        Vec3d playerVelocity = Trails.CLIENT.player.getVelocity();
        double d = Math.sqrt(playerVelocity.x * playerVelocity.x + playerVelocity.z * playerVelocity.z);
        if (d > 0.008 && l - this.lastGhostTrailSampleMillis >= 8L) {
            if (!this.ghostTrailPositions.isEmpty() && this.ghostTrailPositions.getLast().position.squaredDistanceTo(playerPosition) > 1.0) {
                this.ghostTrailPositions.clear();
            }
            if (this.ghostTrailPositions.isEmpty()) {
                this.ghostTrailPositions.add(new Trails.TimedPosition(new Vec3d(Trails.CLIENT.player.lastX, Trails.CLIENT.player.lastY, Trails.CLIENT.player.lastZ), l - 16L));
            }
            if (this.ghostTrailPositions.getLast().position.squaredDistanceTo(playerPosition) > 0.0025) {
                this.ghostTrailPositions.add(new Trails.TimedPosition(playerPosition, l));
                if (this.ghostTrailPositions.size() > 220) {
                    this.ghostTrailPositions.removeFirst();
                }
            } else {
                this.ghostTrailPositions.set(this.ghostTrailPositions.size() - 1, new Trails.TimedPosition(playerPosition, l));
            }
            this.lastGhostTrailSampleMillis = l;
        }
        long l2 = Math.round(((Float)this.energyTrailLengthSetting.getValue()).floatValue() * 1000.0f);
        this.ghostTrailPositions.removeIf(illI1l1l1 -> l - illI1l1l1.timestampMillis > l2);
        if (this.ghostTrailPositions.size() < 2) {
            return;
        }
        if (((Boolean)this.energyThemeColorSetting.getValue()).booleanValue()) {
            int[] nArray2 = new int[3];
            nArray2[0] = Trails.getDefaultTrailColor();
            nArray2[1] = Accent.getLighterAccentColor(255).toArgb();
            nArray = nArray2;
            nArray2[2] = Accent.getDarkerAccentColor(255).toArgb();
        } else {
            int[] nArray3 = new int[3];
            nArray3[0] = -2342657;
            nArray3[1] = -10644225;
            nArray = nArray3;
            nArray3[2] = -44927;
        }
        int[] nArray4 = nArray;
        Vec3d cameraPosition = DaamkyWorldRender.getCameraPosition();
        BufferBuilder bufferBuilder = DynamicTransforms.createBufferBuilder(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        BufferBuilder bufferBuilder2 = DynamicTransforms.createBufferBuilder(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        this.renderEnergyRibbonMesh(bufferBuilder, bufferBuilder2, cameraPosition, l, l2, nArray4);
        DynamicTransforms.submitDraw(bufferBuilder2, VertexFormat.DrawMode.TRIANGLES, WorldRenderPipelines.Ill111IlI11l, null);
        DynamicTransforms.submitDraw(bufferBuilder, VertexFormat.DrawMode.TRIANGLES, WorldRenderPipelines.Ill111IlI11l, null);
    }

    private float getTimedPositionOpacity(Trails.TimedPosition illI1l1l1, long l, long l2) {
        return 1.0f - MathHelper.clamp((float)(l - illI1l1l1.timestampMillis) / (float)l2, 0.0f, 1.0f);
    }

    private Vec3d getSmoothedTrailPosition(int n) {
        Vec3d currentPosition = this.ghostTrailPositions.get((int)n).position;
        Vec3d previousPosition = this.ghostTrailPositions.get((int)Math.max((int)0, (int)(n - 1))).position;
        Vec3d nextPosition = this.ghostTrailPositions.get((int)Math.min((int)(this.ghostTrailPositions.size() - 1), (int)(n + 1))).position;
        return previousPosition.multiply(0.2).add(currentPosition.multiply(0.6)).add(nextPosition.multiply(0.2));
    }

    private static Vec3d getPathTangent(Vec3d[] vec3dArray, int n) {
        Vec3d previousPoint = vec3dArray[Math.max(0, n - 1)];
        Vec3d nextPoint = vec3dArray[Math.min(vec3dArray.length - 1, n + 1)];
        Vec3d pathTangent = nextPoint.subtract(previousPoint);
        return pathTangent.lengthSquared() < 1.0E-8 ? new Vec3d(0.0, 0.0, 1.0) : pathTangent.normalize();
    }

    private static Vec3d getPerpendicularVector(Vec3d tangent) {
        Vec3d referenceAxis = Math.abs(tangent.y) < 0.92 ? new Vec3d(0.0, 1.0, 0.0) : new Vec3d(1.0, 0.0, 0.0);
        Vec3d perpendicularVector = tangent.crossProduct(referenceAxis);
        return perpendicularVector.lengthSquared() < 1.0E-8 ? new Vec3d(1.0, 0.0, 0.0) : perpendicularVector.normalize();
    }

    private static Vec3d[] smoothTangentVectors(Vec3d[] vec3dArray) {
        Vec3d[] vec3dArray2 = new Vec3d[vec3dArray.length + 1];
        vec3dArray2[0] = vec3dArray[0];
        vec3dArray2[vec3dArray2.length - 1] = vec3dArray[vec3dArray.length - 1];
        for (int i = 1; i < vec3dArray2.length - 1; ++i) {
            Vec3d combinedTangent = vec3dArray[i - 1].add(vec3dArray[i]);
            if (combinedTangent.lengthSquared() < 1.0E-6) {
                vec3dArray2[i] = vec3dArray[i];
                continue;
            }
            Vec3d smoothedTangent = combinedTangent.normalize();
            double d = Math.abs(smoothedTangent.dotProduct(vec3dArray[i]));
            vec3dArray2[i] = smoothedTangent.multiply(Math.min(1.22, 1.0 / Math.max(0.5, d)));
        }
        return vec3dArray2;
    }

    private Vec3d[] buildStableNormals(Vec3d[] vec3dArray) {
        Vec3d[] vec3dArray2 = new Vec3d[vec3dArray.length];
        Vec3d previousNormal = Trails.getPerpendicularVector(Trails.getPathTangent(vec3dArray, 0));
        for (int i = 0; i < vec3dArray.length; ++i) {
            Vec3d pathTangent = Trails.getPathTangent(vec3dArray, i);
            Vec3d projectedNormal = previousNormal.subtract(pathTangent.multiply(previousNormal.dotProduct(pathTangent)));
            if (projectedNormal.lengthSquared() < 1.0E-8) {
                projectedNormal = Trails.getPerpendicularVector(pathTangent);
            }
            if ((projectedNormal = projectedNormal.normalize()).dotProduct(previousNormal) < 0.0) {
                projectedNormal = projectedNormal.negate();
            }
            vec3dArray2[i] = projectedNormal;
            previousNormal = projectedNormal;
        }
        return vec3dArray2;
    }

    private static float smoothstep(float f, float f2, float f3) {
        float f4 = MathHelper.clamp((f3 - f) / (f2 - f), 0.0f, 1.0f);
        return f4 * f4 * (3.0f - 2.0f * f4);
    }

    private static int sampleColorGradient(int[] nArray, float f) {
        f -= (float)Math.floor(f);
        float f2 = f * 3.0f;
        int n = Math.min(2, (int)Math.floor(f2));
        float f3 = f2 - (float)n;
        return Trails.blendColors(nArray[n], nArray[(n + 1) % 3], f3);
    }

    private static int calculateGlowColor(int[] nArray, float f, float f2, float f3, float f4, float f5) {
        float f6 = (float)Math.pow(Math.max(1.0f - Trails.smoothstep(0.72f, 1.02f, f), 0.0f), 0.5);
        int n = Trails.sampleColorGradient(nArray, f2 + f * 0.34f + f3 * 0.04f);
        float f7 = 0.389f;
        float f8 = f6 * (0.45f + 0.7f * f7);
        float[] fArray = new float[]{(float)(n >> 16 & 0xFF) / 255.0f * f8, (float)(n >> 8 & 0xFF) / 255.0f * f8, (float)(n & 0xFF) / 255.0f * f8};
        for (int i = 0; i < 3; ++i) {
            fArray[i] = 1.0f - (float)Math.exp(-fArray[i] * 2.6f);
        }
        float f9 = fArray[0] * 0.2126f + fArray[1] * 0.7152f + fArray[2] * 0.0722f;
        for (int i = 0; i < 3; ++i) {
            fArray[i] = MathHelper.clamp(f9 + (fArray[i] - f9) * 2.0f, 0.0f, 1.0f);
        }
        float f10 = Math.max(fArray[0], Math.max(fArray[1], fArray[2]));
        float f11 = MathHelper.clamp(f10, 0.0f, 1.0f) * f4 * f5;
        return Trails.packRgba((int)(Math.min(fArray[0], 0.98f) * 255.0f), (int)(Math.min(fArray[1], 0.98f) * 255.0f), (int)(Math.min(fArray[2], 0.98f) * 255.0f), (int)(f11 * 255.0f));
    }

    private void renderEnergyRibbonMesh(BufferBuilder bufferBuilder, BufferBuilder bufferBuilder2, Vec3d cameraPosition, long l, long l2, int[] nArray) {
        int n = this.ghostTrailPositions.size();
        Vec3d[] vec3dArray = new Vec3d[n];
        for (int i = 0; i < n; ++i) {
            vec3dArray[i] = this.getSmoothedTrailPosition(i).add(0.0, 0.9, 0.0);
        }
        Vec3d[] vec3dArray2 = new Vec3d[n - 1];
        Vec3d previousNormal = null;
        for (int i = 0; i < vec3dArray2.length; ++i) {
            Vec3d segmentDirection = vec3dArray[i + 1].subtract(vec3dArray[i]);
            if (segmentDirection.lengthSquared() < 1.0E-8) {
                vec3dArray2[i] = previousNormal == null ? new Vec3d(1.0, 0.0, 0.0) : previousNormal;
                continue;
            }
            Vec3d segmentNormal = Trails.getPerpendicularVector(segmentDirection.normalize());
            if (previousNormal != null && segmentNormal.dotProduct(previousNormal) < 0.0) {
                segmentNormal = segmentNormal.negate();
            }
            vec3dArray2[i] = segmentNormal;
            previousNormal = segmentNormal;
        }
        Vec3d[] vec3dArray3 = Trails.smoothTangentVectors(vec3dArray2);
        int n2 = 4;
        int n3 = 7;
        float f = (float)(l % 1000000L) * 0.001f;
        float f2 = ((Float)this.energyTrailWidthSetting.getValue()).floatValue();
        float f3 = 0.024f + f2 * 0.06f;
        for (int i = 0; i < n2; ++i) {
            float f4;
            float f5;
            float f6;
            Vec3d lateralOffsetDirection;
            Vec3d tangent;
            Vec3d[] vec3dArray4 = new Vec3d[n];
            for (int j = 0; j < n; ++j) {
                float f7 = 1.0f - this.getTimedPositionOpacity(this.ghostTrailPositions.get(j), l, l2);
                tangent = Trails.getPathTangent(vec3dArray, j);
                Vec3d ribbonNormal = vec3dArray3[j].normalize();
                lateralOffsetDirection = ribbonNormal.crossProduct(tangent);
                if (lateralOffsetDirection.lengthSquared() < 1.0E-8) {
                    lateralOffsetDirection = new Vec3d(0.0, 1.0, 0.0);
                }
                lateralOffsetDirection = lateralOffsetDirection.normalize();
                float f8 = i;
                float f9 = f7 * 2.0f / 1.9f;
                f6 = f8 * 1.7f * 3.0f;
                f5 = (2.0f + f8 * 0.35f) * 3.0f;
                float f10 = 1.4f + f8 * 1.2f;
                f4 = f * 0.5f;
                float f11 = f9 * f5 + f4 * f10 + f6;
                float f12 = f9 * f5 * 1.1f - f4 * f10 * 0.7f + f6 * 1.7f;
                float f13 = (float)Math.pow(Math.max(1.0f - Trails.smoothstep(0.58f, 1.05f, f9), 0.0f), 0.5);
                float f14 = 0.10778f * f13 * 1.1f;
                float f15 = MathHelper.sin(f11) * 0.6f + MathHelper.sin(f12) * 0.4f;
                float f16 = f2 * 2.0f * 1.9f;
                float f17 = (float)i * ((float)Math.PI * 2) / (float)n2;
                float f18 = f4 * 2.65f + f9 * ((float)Math.PI * 2) * 1.45f + f17;
                float f19 = f4 * 1.8f - f9 * ((float)Math.PI * 2) * 0.72f + f17 * 2.0f;
                float f20 = f2 * 0.46f * f13;
                float f21 = MathHelper.sin(f19) * f2 * 0.14f * f13;
                float f22 = MathHelper.sin(f18) * f20 + f21 + f15 * f14 * f16 * 0.42f;
                float f23 = MathHelper.cos(f18) * f20 * 0.68f + MathHelper.cos(f19) * f2 * 0.08f * f13;
                vec3dArray4[j] = vec3dArray[j].add(ribbonNormal.multiply(f22)).add(lateralOffsetDirection.multiply(f23));
            }
            Vec3d[] vec3dArray5 = this.buildStableNormals(vec3dArray4);
            Vec3d[][] vec3dArray6 = new Vec3d[n][n3];
            float[] alphaByPoint = new float[n];
            for (int j = 0; j < n; ++j) {
                Vec3d ribbonNormal = vec3dArray5[j];
                lateralOffsetDirection = Trails.getPathTangent(vec3dArray4, j);
                Vec3d radialTangent = ribbonNormal.crossProduct(lateralOffsetDirection);
                if (radialTangent.lengthSquared() < 1.0E-8) {
                    radialTangent = new Vec3d(0.0, 1.0, 0.0);
                }
                radialTangent = radialTangent.normalize();
                alphaByPoint[j] = f6 = this.getTimedPositionOpacity(this.ghostTrailPositions.get(j), l, l2);
                f5 = f3 * (0.28f + f6 * 0.72f);
                for (int k = 0; k < n3; ++k) {
                    f4 = (float)Math.PI * 2 * (float)k / (float)n3;
                    Vec3d radialOffset = ribbonNormal.multiply(MathHelper.cos(f4)).add(radialTangent.multiply(MathHelper.sin(f4)));
                    vec3dArray6[j][k] = vec3dArray4[j].add(radialOffset.multiply(f5));
                }
            }
            float f24 = (float)i / (float)n2;
            this.writeRibbonSection(bufferBuilder, vec3dArray6, vec3dArray4, alphaByPoint, cameraPosition, nArray, f24, f, 1.0f, 1.0f);
            this.writeRibbonSection(bufferBuilder2, vec3dArray6, vec3dArray4, alphaByPoint, cameraPosition, nArray, f24, f, 3.2f, 0.22f);
        }
    }

    private void writeRibbonSection(BufferBuilder bufferBuilder, Vec3d[][] vec3dArray, Vec3d[] vec3dArray2, float[] fArray, Vec3d cameraPosition, int[] nArray, float f, float f2, float f3, float f4) {
        int n = vec3dArray.length;
        int n2 = vec3dArray[0].length;
        for (int i = 1; i < n; ++i) {
            float f5 = fArray[i - 1];
            float f6 = fArray[i];
            float f7 = 1.0f - f5;
            float f8 = 1.0f - f6;
            int n3 = Trails.calculateGlowColor(nArray, f7, f, f2, f5 * 0.86f, f4);
            int n4 = Trails.calculateGlowColor(nArray, f8, f, f2, f6 * 0.86f, f4);
            for (int j = 0; j < n2; ++j) {
                int n5 = (j + 1) % n2;
                Vec3d sectionStartInnerVertex = Trails.interpolateVector(vec3dArray[i - 1][j], vec3dArray2[i - 1], f3).subtract(cameraPosition);
                Vec3d sectionStartOuterVertex = Trails.interpolateVector(vec3dArray[i - 1][n5], vec3dArray2[i - 1], f3).subtract(cameraPosition);
                Vec3d sectionEndInnerVertex = Trails.interpolateVector(vec3dArray[i][j], vec3dArray2[i], f3).subtract(cameraPosition);
                Vec3d sectionEndOuterVertex = Trails.interpolateVector(vec3dArray[i][n5], vec3dArray2[i], f3).subtract(cameraPosition);
                Trails.writeColoredVertex(bufferBuilder, sectionStartInnerVertex, n3);
                Trails.writeColoredVertex(bufferBuilder, sectionStartOuterVertex, n3);
                Trails.writeColoredVertex(bufferBuilder, sectionEndOuterVertex, n4);
                Trails.writeColoredVertex(bufferBuilder, sectionStartInnerVertex, n3);
                Trails.writeColoredVertex(bufferBuilder, sectionEndOuterVertex, n4);
                Trails.writeColoredVertex(bufferBuilder, sectionEndInnerVertex, n4);
            }
        }
    }

    private static Vec3d interpolateVector(Vec3d endVector, Vec3d startVector, float f) {
        return f == 1.0f ? endVector : startVector.add(endVector.subtract(startVector).multiply(f));
    }

    private static void writeColoredVertex(BufferBuilder bufferBuilder, Vec3d vertexPosition, int n) {
        bufferBuilder.vertex((float)vertexPosition.x, (float)vertexPosition.y, (float)vertexPosition.z).color(n);
    }

    private int getEnergyPulseColor(int n, int n2, double d) {
        double d2 = (d * 100.0 + (double)n) % 360.0;
        float f = (float)((d2 >= 180.0 ? 360.0 - d2 : d2) / 180.0);
        int n3 = Trails.getDefaultTrailColor();
        int n4 = Trails.blendColors(n3, Trails.scaleColorBrightness(n3, 0.25f), f);
        return Trails.packRgba(n4 >> 16 & 0xFF, n4 >> 8 & 0xFF, n4 & 0xFF, n2);
    }

    private void renderGhostTrail(float f) {
        if (Trails.CLIENT.options.getPerspective().isFirstPerson()) {
            this.trailHistory.clear();
            return;
        }
        int n = ((Float)this.ghostTrailLengthSetting.getValue()).intValue() * 30;
        double d = (double)((float)Trails.CLIENT.world.getTime() + f) / 20.0;
        this.trailHistory.appendPosition(this.getInterpolatedPlayerPosition(f), d, n);
        TrailPointWindow l1I1IlllIlI2 = this.trailHistory.createWindow(n);
        if (l1I1IlllIlI2.points().isEmpty()) {
            return;
        }
        Vec3d cameraPosition = DaamkyWorldRender.getCameraPosition();
        Vec3d[] vec3dArray = this.getCameraPlaneBasis();
        Vec3d cameraRight = vec3dArray[0];
        Vec3d cameraUp = vec3dArray[1];
        BufferBuilder bufferBuilder = Trails.createPositionTextureColorBuffer();
        double d2 = d * 2.0 * (double)((Float)this.ghostAnimationSpeedSetting.getValue()).floatValue();
        float f2 = 0.65f;
        int n2 = l1I1IlllIlI2.points().size();
        float f3 = (float)Math.min(1.0, 40.0 / l1I1IlllIlI2.visiblePointCount());
        float f4 = ((Float)this.ghostParticleSizeSetting.getValue()).floatValue() / 0.6f * 1.25f;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < n2; ++j) {
                float f5;
                TrailPoint iI1Il11l111II11IIl = l1I1IlllIlI2.points().get(j);
                float f6 = (float)MathHelper.clamp((d - iI1Il11l111II11IIl.timestampSeconds()) / 0.8, 0.0, 1.0);
                float f7 = 1.0f - (float)j / (float)l1I1IlllIlI2.visiblePointCount();
                int n3 = (int)(220.0f * f7 * (f5 = 1.0f - f6 * f6) * f3);
                if (n3 <= 5) continue;
                double d3 = d2 - (double)j * 0.015;
                double d4 = (Math.sin(d3) + 1.0) * 0.5;
                double d5 = Math.toRadians((double)(i * 90) + d3 * 50.0 % 360.0);
                Vec3d sparklePosition = iI1Il11l111II11IIl.position().subtract(cameraPosition).add(Math.cos(d5) * (double)f2, i % 2 == 0 ? 0.1 + 1.7 * d4 : 1.8 - 1.7 * d4, Math.sin(d5) * (double)f2);
                float f8 = (0.08f + 0.16f * f7) * f5 * f4;
                float f9 = (0.3f + 0.56f * f7) * f5 * f4;
                sparklePosition = sparklePosition.add(-f8 / 12.0f, -f8 / 12.0f, 0.0).add(cameraRight.multiply(f8 / 12.0f)).add(cameraUp.multiply(f8 / 2.0f));
                int n4 = Math.round((float)n3 * 0.05f);
                Trails.writeBillboardQuad(bufferBuilder, sparklePosition, cameraRight, cameraUp, Vec3d.ZERO, 0.0, f9, f9, this.getEnergyPulseColor(j * 2, n4, d), this.getEnergyPulseColor(j * 4, n4, d), this.getEnergyPulseColor(j * 8, n4, d), this.getEnergyPulseColor(j * 16, n4, d));
                Trails.writeBillboardQuad(bufferBuilder, sparklePosition, cameraRight, cameraUp, Vec3d.ZERO, 0.0, f8, f8, this.getEnergyPulseColor(j * 2, n3, d), this.getEnergyPulseColor(j * 4, n3, d), this.getEnergyPulseColor(j * 8, n3, d), this.getEnergyPulseColor(j * 16, n3, d));
            }
            f2 = -f2;
        }
        Trails.drawTextureBuffer(bufferBuilder, GLOW_TEXTURE, WorldRenderPipelines.I1I1lI111lII1);
    }

    private void renderPlayerTrails(float f) {
        int n;
        float f2;
        float f3;
        float f4;
        double d;
        double d2;
        if (this.playerTrails.isEmpty()) {
            return;
        }
        this.loadTrailTextures();
        Vec3d cameraPosition = DaamkyWorldRender.getCameraPosition();
        float f5 = DaamkyWorldRender.getCameraYaw();
        float f6 = DaamkyWorldRender.getCameraPitch();
        float f7 = this.getDashAnimationRate();
        float f8 = (Boolean)this.enableLightingSetting.getValue() != false ? 1.0f : 0.0f;
        boolean bl = (Boolean)this.renderSegmentsSetting.getValue() != false || (Boolean)this.renderPointsSetting.getValue() != false;
        ArrayList<PlayerTrail> arrayList = new ArrayList<PlayerTrail>();
        for (PlayerTrail playerTrail : this.playerTrails) {
            playerTrail.updateOpacity();
            if (!(playerTrail.opacity >= 0.05f) || playerTrail.textureSequence.currentTexture() == null) continue;
            arrayList.add(playerTrail);
        }
        if (arrayList.isEmpty()) {
            return;
        }
        Map<Identifier, List<PlayerTrail>> hashMap = new HashMap<>();
        for (PlayerTrail iI1Il11l111II11IIl : arrayList) {
            hashMap.computeIfAbsent(iI1Il11l111II11IIl.textureSequence.currentTexture().textureId(), identifier -> new ArrayList()).add(iI1Il11l111II11IIl);
        }
        for (Map.Entry<Identifier, List<PlayerTrail>> textureBatch : hashMap.entrySet()) {
            BufferBuilder bufferBuilder = Trails.createPositionTextureColorBuffer();
            for (PlayerTrail iI1Il11l111II11IIl : textureBatch.getValue()) {
                TrailTextureDescriptor l1IIl11lI = iI1Il11l111II11IIl.textureSequence.currentTexture();
                float f9 = iI1Il11l111II11IIl.opacity;
                d2 = iI1Il11l111II11IIl.getInterpolatedX(f) - cameraPosition.x;
                d = iI1Il11l111II11IIl.getInterpolatedY(f) - cameraPosition.y;
                double d3 = iI1Il11l111II11IIl.getInterpolatedZ(f) - cameraPosition.z;
                f4 = 0.033f * f9 * f7 * 0.1f;
                int n2 = Trails.withAlpha(Trails.scaleColorBrightness(Trails.blendColors(iI1Il11l111II11IIl.trailColor, -1, 0.45f), 1.0f), f9);
                Trails.writeRotatedTextureQuad(bufferBuilder, d2, d, d3, (float)l1IIl11lI.frameCount() * f4, (float)l1IIl11lI.framesPerSecond() * f4, iI1Il11l111II11IIl.cameraAngles[0], iI1Il11l111II11IIl.cameraAngles[1], n2);
            }
            Trails.drawTextureBuffer(bufferBuilder, (Identifier)textureBatch.getKey(), WorldRenderPipelines.I1l11IIlIIIll1l1lI1II);
        }
        BufferBuilder bufferBuilder = Trails.createPositionTextureColorBuffer();
        for (PlayerTrail iI1Il11l111II11IIl : arrayList) {
            TrailTextureDescriptor texture = iI1Il11l111II11IIl.textureSequence.currentTexture();
            float f10 = iI1Il11l111II11IIl.opacity;
            double d4 = iI1Il11l111II11IIl.getInterpolatedX(f) - cameraPosition.x;
            d2 = iI1Il11l111II11IIl.getInterpolatedY(f) - cameraPosition.y;
            d = iI1Il11l111II11IIl.getInterpolatedZ(f) - cameraPosition.z;
            float f11 = 0.033f * f10 * f7 * 0.1f;
            f3 = texture.frameCount() * f11;
            f4 = texture.framesPerSecond() * f11;
            float f12 = (float)Math.sqrt(f3 * f3 + f4 * f4);
            f2 = 1.0f - iI1Il11l111II11IIl.progress();
            n = Trails.blendColors(iI1Il11l111II11IIl.trailColor, -1, 0.15f);
            Trails.writeRotatedTextureQuad(bufferBuilder, d4, d2, d, f12 / 1.75f * 2.0f, f12 / 1.75f * 2.0f, f5, f6, Trails.withAlpha(n, 0.21568628f * f10));
            if (!(f8 > 0.0f)) continue;
            float f13 = f10 * f8;
            float f14 = f12 * (1.0f + 6.0f * f2 * f13);
            int n3 = Trails.withAlpha(Trails.scaleColorBrightness(n, f13 / 4.0f), Math.min(f13 / 2.0f / f7, 1.0f) * 0.43137255f);
            Trails.writeRotatedTextureQuad(bufferBuilder, d4, d2, d, f14, f14, f5, f6, n3);
        }
        Trails.drawTextureBuffer(bufferBuilder, DASH_BLOOM_TEXTURE, WorldRenderPipelines.I1l11IIlIIIll1l1lI1II);
        if (!bl) {
            return;
        }
        BufferBuilder bufferBuilder2 = Trails.createPositionTextureColorBuffer();
        float f15 = this.getSparkleSpawnChance();
        for (PlayerTrail iI1Il11l111II11IIl : arrayList) {
            if (iI1Il11l111II11IIl.sparkles.isEmpty()) continue;
            float f16 = iI1Il11l111II11IIl.opacity;
            double d5 = iI1Il11l111II11IIl.getInterpolatedX(f) - cameraPosition.x;
            double d6 = iI1Il11l111II11IIl.getInterpolatedY(f) - cameraPosition.y;
            double d7 = iI1Il11l111II11IIl.getInterpolatedZ(f) - cameraPosition.z;
            f3 = f15 * (0.8f + f16);
            for (TrailSparkle lIIl11l111lIIl1ll2 : iI1Il11l111II11IIl.sparkles) {
                f2 = Trails.easeInOut((float)(lIIl11l111lIIl1ll2.fade() * (double)f16));
                if (f2 < 0.02f) continue;
                n = Trails.blendColors(iI1Il11l111II11IIl.trailColor, Trails.withAlpha(-1, (float)(iI1Il11l111II11IIl.trailColor >>> 24) / 255.0f), f2);
                n = Trails.withAlpha(n, (float)(n >>> 24) / 255.0f * f2 / 1.33333f);
                Trails.writeRotatedTextureQuad(bufferBuilder2, d5 + lIIl11l111lIIl1ll2.getInterpolatedX(f), d6 + lIIl11l111lIIl1ll2.getInterpolatedY(f), d7 + lIIl11l111lIIl1ll2.getInterpolatedZ(f), f3, f3, f5, f6, n);
                Trails.writeRotatedTextureQuad(bufferBuilder2, d5 - lIIl11l111lIIl1ll2.getInterpolatedX(f), d6 - lIIl11l111lIIl1ll2.getInterpolatedY(f), d7 - lIIl11l111lIIl1ll2.getInterpolatedZ(f), f3, f3, f5, f6, n);
            }
        }
        Trails.drawTextureBuffer(bufferBuilder2, DASH_BLOOM_TEXTURE, WorldRenderPipelines.I1l11IIlIIIll1l1lI1II);
    }

    private static float easeInOut(float f) {
        return (f = MathHelper.clamp(f, 0.0f, 1.0f)) < 0.5f ? 2.0f * f * f : 1.0f - (float)Math.pow(-2.0f * f + 2.0f, 2.0) / 2.0f;
    }

    private static float angularDistance(float f, float f2) {
        float f3 = Math.abs(f - f2) % 360.0f;
        return f3 > 180.0f ? 360.0f - f3 : f3;
    }

    private static final class TrailHistory {
        private final List<TrailPoint> points = new ArrayList<>();
        private TrailPoint latestPoint;
        private double samplingRemainder;

        private TrailHistory() {
        }

        private void clear() {
            this.points.clear();
            this.latestPoint = null;
            this.samplingRemainder = 0.0;
        }

        private void appendPosition(Vec3d position, double d, int n) {
            if (this.latestPoint != null && (d < this.latestPoint.timestampSeconds() || d - this.latestPoint.timestampSeconds() >= 0.8 || position.squaredDistanceTo(this.latestPoint.position()) > 256.0)) {
                this.clear();
            }
            TrailPoint iI1Il11l111II11IIl = new TrailPoint(position, d);
            if (this.latestPoint == null) {
                this.latestPoint = iI1Il11l111II11IIl;
                this.points.add(iI1Il11l111II11IIl);
                return;
            }
            double d2 = Math.max(position.distanceTo(this.latestPoint.position()) / 0.03, (d - this.latestPoint.timestampSeconds()) / 0.05);
            if (d2 > 0.0) {
                double d3 = this.samplingRemainder + d2;
                int n2 = (int)Math.floor(d3);
                for (int i = 1; i <= n2; ++i) {
                    this.points.add(this.latestPoint.II1Il11l111II11IIl(iI1Il11l111II11IIl, ((double)i - this.samplingRemainder) / d2));
                }
                this.samplingRemainder = d3 - (double)n2;
            }
            this.latestPoint = iI1Il11l111II11IIl;
            int n3 = 0;
            while (n3 + 1 < this.points.size() && this.points.get(n3 + 1).timestampSeconds() <= d - 0.8) {
                ++n3;
            }
            if ((n3 = Math.max(n3, this.points.size() - n - 1)) > 0) {
                this.points.subList(0, n3).clear();
            }
        }

        private TrailPointWindow createWindow(int n) {
            if (this.latestPoint == null) {
                return new TrailPointWindow(List.of(), 0.0);
            }
            double d = 0.0;
            if (this.points.size() > 1 && this.points.getFirst().timestampSeconds() < this.latestPoint.timestampSeconds() - 0.8) {
                TrailPoint iI1Il11l111II11IIl = this.points.getFirst();
                TrailPoint iI1Il11l111II11IIl2 = this.points.get(1);
                d = Math.clamp((this.latestPoint.timestampSeconds() - 0.8 - iI1Il11l111II11IIl.timestampSeconds()) / (iI1Il11l111II11IIl2.timestampSeconds() - iI1Il11l111II11IIl.timestampSeconds()), 0.0, 1.0);
            }
            double d2 = Math.min((double)(n - 1), (double)(this.points.size() - 1) + this.samplingRemainder - d);
            int n2 = Math.min(this.points.size(), (int)d2 + 1);
            ArrayList<TrailPoint> arrayList = new ArrayList<>(n2);
            arrayList.add(this.latestPoint);
            for (int i = 1; i < n2; ++i) {
                int n3 = this.points.size() - 1 - i;
                arrayList.add(this.points.get(n3).II1Il11l111II11IIl(this.points.get(n3 + 1), this.samplingRemainder));
            }
            return new TrailPointWindow(arrayList, d2 + 1.0);
        }
    }

    private record TrailTextureDescriptor(Identifier textureId, int frameCount, int framesPerSecond) {
    }

    private final class PlayerTrail {
        private final PlayerEntity player;
        private final int entityId;
        private final int trailColor;
        private final TrailPatternSelector textureSequence;
        private final long startedAtMillis = System.currentTimeMillis();
        private final int lifetimeMillis;
        private final boolean generateSparkles;
        private final float[] cameraAngles = new float[2];
        private final List<TrailSparkle> sparkles = new ArrayList<TrailSparkle>();
        private double motionX;
        private double motionY;
        private double motionZ;
        private double currentX;
        private double currentY;
        private double currentZ;
        private double previousX;
        private double previousY;
        private double previousZ;
        private float opacity;
        private float targetOpacity = 1.0f;

        private PlayerTrail(PlayerEntity playerEntity, Vec3d startPosition, Vec3d endPosition, float f, int n, boolean bl) {
            this.player = playerEntity;
            this.entityId = playerEntity.getId();
            this.trailColor = Trails.this.getConfiguredTrailColor();
            this.textureSequence = new TrailPatternSelector(Trails.this);
            this.lifetimeMillis = Math.max(200, n);
            this.generateSparkles = bl;
            this.motionX = endPosition.x - startPosition.x;
            this.motionY = endPosition.y - startPosition.y;
            this.motionZ = endPosition.z - startPosition.z;
            double d = playerEntity.getHeight();
            this.currentX = startPosition.x - this.motionX * (double)f + (-0.0875 + 0.175 * Math.random());
            this.currentY = startPosition.y - this.motionY * (double)f + (d / 3.0 + d / 4.0 * Math.random() * 0.7);
            this.currentZ = startPosition.z - this.motionZ * (double)f + (-0.0875 + 0.175 * Math.random());
            this.previousX = this.currentX;
            this.previousY = this.currentY;
            this.previousZ = this.currentZ;
            this.motionX *= 0.04;
            this.motionY *= 0.04;
            this.motionZ *= 0.04;
            this.updateOrientationAndSparkles();
        }

        private float progress() {
            return Math.min((float)(System.currentTimeMillis() - this.startedAtMillis) / (float)this.lifetimeMillis, 1.0f);
        }

        private void updateOpacity() {
            float f = 0.035f;
            if (this.opacity < this.targetOpacity) {
                this.opacity = Math.min(this.targetOpacity, this.opacity + f);
            } else if (this.opacity > this.targetOpacity) {
                this.opacity = Math.max(this.targetOpacity, this.opacity - f);
            }
        }

        private int getHeadingDegrees() {
            int n = (int)Math.toDegrees(Math.atan2(this.motionZ, this.motionX) - 90.0);
            return n < 0 ? n + 360 : n;
        }

        private void updateOrientationAndSparkles() {
            float f = DaamkyWorldRender.getCameraPitch();
            if (Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ) < 5.0E-4) {
                this.cameraAngles[0] = (float)(360.0 * Math.random());
                this.cameraAngles[1] = f;
            } else {
                float f2 = this.getHeadingDegrees();
                float f3 = this.player == null ? 0.0f : (this.player.lastYaw - this.player.getYaw()) * 3.0f;
                this.cameraAngles[0] = f2 - 45.0f - 15.0f - f3;
                float f4 = Trails.angularDistance(f2 + 26.3f, this.player != null ? this.player.getYaw() : f2);
                this.cameraAngles[1] = f4 >= 10.0f && f4 <= 160.0f ? f : -90.0f;
            }
        }

        private void updateTowardNextTrailPoint(PlayerTrail iI1Il11l111II11IIl) {
            if (iI1Il11l111II11IIl != null && iI1Il11l111II11IIl.entityId != this.entityId) {
                iI1Il11l111II11IIl = null;
            }
            this.previousX = this.currentX;
            this.previousY = this.currentY;
            this.previousZ = this.currentZ;
            this.motionX = (iI1Il11l111II11IIl != null ? iI1Il11l111II11IIl.motionX : this.motionX) / 1.05;
            this.motionY = (iI1Il11l111II11IIl != null ? iI1Il11l111II11IIl.motionY : this.motionY) / 1.05;
            this.motionZ = (iI1Il11l111II11IIl != null ? iI1Il11l111II11IIl.motionZ : this.motionZ) / 1.05;
            this.currentX += 5.0 * this.motionX;
            this.currentY += 5.0 * this.motionY / (this.motionY < 0.0 ? 1.0 : 3.5);
            this.currentZ += 5.0 * this.motionZ;
            this.updateOrientationAndSparkles();
            if (this.generateSparkles) {
                if (this.progress() < 0.3f && Trails.this.random.nextInt(12) > 5) {
                    int n = (Boolean)Trails.this.renderSegmentsSetting.getValue() != false ? 1 : 2;
                    for (int i = 0; i < n; ++i) {
                        this.sparkles.add(new TrailSparkle(Trails.this.getDashAnimationRate()));
                    }
                }
                this.sparkles.forEach(TrailSparkle::tick);
                this.sparkles.removeIf(TrailSparkle::isExpired);
            } else {
                this.sparkles.clear();
            }
        }

        private double getInterpolatedX(float f) {
            return this.previousX + (this.currentX - this.previousX) * (double)f;
        }

        private double getInterpolatedY(float f) {
            return this.previousY + (this.currentY - this.previousY) * (double)f;
        }

        private double getInterpolatedZ(float f) {
            return this.previousZ + (this.currentZ - this.previousZ) * (double)f;
        }
    }

    private static final class TrailInterpolationState {
        private final Vec3d position;
        private final Vec3d direction;
        private final float startWidth;
        private final float endWidth;
        private final float rotationDegrees;
        private final long createdAtMillis = System.currentTimeMillis();
        private final long lifetimeMillis;

        private TrailInterpolationState(Vec3d position, Vec3d direction, float f, float f2, float f3, long l) {
            this.position = position;
            this.direction = direction;
            this.startWidth = f;
            this.endWidth = f2;
            this.rotationDegrees = f3;
            this.lifetimeMillis = l;
        }

        private boolean isExpired(long l) {
            return l - this.createdAtMillis >= this.lifetimeMillis;
        }

        private float progress(long l) {
            return MathHelper.clamp((float)(l - this.createdAtMillis) / (float)this.lifetimeMillis, 0.0f, 1.0f);
        }

        private float fadeAlpha(long l) {
            float f = 1.0f - this.progress(l);
            return MathHelper.clamp((float)Math.pow(Math.sin((double)f * Math.PI * 0.5), 0.85), 0.0f, 1.0f);
        }
    }

    private record TimedPosition(Vec3d position, long timestampMillis) {
    }

    private final class TrailPatternSelector {
        private final List<TrailTextureDescriptor> textureFrames;
        private final boolean animated;
        private final long startedAtMillis = System.currentTimeMillis();
        private final long animationDurationMillis;

        private TrailPatternSelector(Trails trails) {
            boolean bl = this.animated = !trails.dashTrailTextures.isEmpty() && trails.random.nextInt(100) > 40 && !trails.groupedDashTrailTextures.isEmpty();
            if (this.animated) {
                this.textureFrames = trails.groupedDashTrailTextures.get(trails.random.nextInt(trails.groupedDashTrailTextures.size()));
                this.animationDurationMillis = trails.getTrailLifetimeMillis();
            } else {
                this.textureFrames = trails.dashTrailTextures.isEmpty() ? List.of() : List.of(trails.dashTrailTextures.get(trails.random.nextInt(trails.dashTrailTextures.size())));
                this.animationDurationMillis = 1L;
            }
        }

        private TrailTextureDescriptor currentTexture() {
            if (this.textureFrames.isEmpty()) {
                return null;
            }
            if (!this.animated) {
                return this.textureFrames.getFirst();
            }
            long l = Math.max(this.animationDurationMillis, 1L);
            float f = (float)((System.currentTimeMillis() - this.startedAtMillis) % l) / (float)l;
            return this.textureFrames.get(MathHelper.clamp((int)(f * (float)this.textureFrames.size()), 0, this.textureFrames.size() - 1));
        }
    }

    private static final class TrailSparkle {
        private double currentX;
        private double currentY;
        private double currentZ;
        private double previousX;
        private double previousY;
        private double previousZ;
        private final double travelDistance;
        private final float yawDegrees;
        private final float pitchDegrees;
        private final long createdAtMillis = System.currentTimeMillis();

        private TrailSparkle(float f) {
            this.yawDegrees = (float)Math.random() * 360.0f;
            this.pitchDegrees = -90.0f + (float)Math.random() * 180.0f;
            this.travelDistance = Math.random() / 50.0 * (double)f;
        }

        private void tick() {
            float f = (float)Math.toRadians(this.yawDegrees);
            this.previousX = this.currentX;
            this.previousY = this.currentY;
            this.previousZ = this.currentZ;
            this.currentX += (double)MathHelper.sin(f) * this.travelDistance;
            this.currentY += (double)MathHelper.cos((float)Math.toRadians(this.pitchDegrees - 90.0f)) * this.travelDistance;
            this.currentZ += (double)MathHelper.cos(f) * this.travelDistance;
        }

        private double progress() {
            return MathHelper.clamp((double)(System.currentTimeMillis() - this.createdAtMillis) / 1000.0, 0.0, 1.0);
        }

        private boolean isExpired() {
            return this.progress() >= 1.0;
        }

        private double fade() {
            return 1.0 - this.progress();
        }

        private double getInterpolatedX(float f) {
            return this.previousX + (this.currentX - this.previousX) * (double)f;
        }

        private double getInterpolatedY(float f) {
            return this.previousY + (this.currentY - this.previousY) * (double)f;
        }

        private double getInterpolatedZ(float f) {
            return this.previousZ + (this.currentZ - this.previousZ) * (double)f;
        }
    }
}

