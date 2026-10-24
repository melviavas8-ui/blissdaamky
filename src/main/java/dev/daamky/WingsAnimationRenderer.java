/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.entity.player.PlayerEntity
 *  org.joml.Vector3f
 */
package dev.daamky;

import dev.daamky.ModelGeometry;
import dev.daamky.Model;
import dev.daamky.PlayerCosmeticsFeatureRenderer;
import dev.daamky.Wings;
import dev.daamky.core.event.EventTarget;
import dev.daamky.core.event.impl.EventRender3D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Vector3f;

public final class WingsAnimationRenderer {
    private static final WingsAnimationRenderer II1Il11l111II11IIl = new WingsAnimationRenderer();
    private static final long l1I1IlllIlI = System.currentTimeMillis();
    private static final float IllI1l1l1 = 0.95f;
    private static final Vector3f l1lll11l1l = new Vector3f(0.35f, 1.0f, 0.55f).normalize();
    private final Map<Integer, PetAnimationState> animationStates = new HashMap<>();

    private WingsAnimationRenderer() {
    }

    public static WingsAnimationRenderer II1Il11l111II11IIl() {
        return II1Il11l111II11IIl;
    }

    @EventTarget
    public void II1Il11l111II11IIl(EventRender3D eventRender3D) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            this.animationStates.clear();
            return;
        }
        float f = Math.clamp(eventRender3D.delta(), 0.0f, 0.05f);
        float f2 = eventRender3D.partialTicks();
        HashSet<Integer> hashSet = new HashSet<Integer>();
        for (PlayerEntity player : client.world.getPlayers()) {
            ModelGeometry lIll1Il1lll111l11l1I1llllI;
            if (player.isSpectator() || player.isInvisible()) continue;
            Model model = null;
            for (Model candidateModel : PlayerCosmeticsFeatureRenderer.getPlayerModels(client, player.getId())) {
                if (candidateModel.getCosmeticType() != Wings.PETS) continue;
                model = candidateModel;
            }
            if (model == null || (lIll1Il1lll111l11l1I1llllI = model.getGeometry()) == null) continue;
            hashSet.add(player.getId());
            this.animationStates.computeIfAbsent(player.getId(), ignored -> new PetAnimationState())
                .update(client, player, model, lIll1Il1lll111l11l1I1llllI, f2, f);
        }
        this.animationStates.keySet().retainAll(hashSet);
    }

    private static final class PetAnimationState {
        private float elapsedSeconds;

        void update(MinecraftClient client, PlayerEntity player, Model model,
                    ModelGeometry modelData, float partialTicks, float deltaSeconds) {
            this.elapsedSeconds += Math.clamp(deltaSeconds, 0.0f, 0.05f);
            Animations animations = model.getAnimations();
            if (animations != null) {
                float animationTime = this.elapsedSeconds + partialTicks * Math.clamp(deltaSeconds, 0.0f, 0.05f);
                animations.apply(modelData, animationTime);
            }
        }
    }
}


