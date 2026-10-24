package dev.daamky.mixin.render;

import dev.daamky.NameTags;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class NameTagsHideMixin {
   @Inject(
      method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
      at = @At("TAIL")
   )
   private void daamky$hideVanillaNameTag(PlayerLikeEntity entity, PlayerEntityRenderState renderState, float tickDelta, CallbackInfo callbackInfo) {
      if (entity instanceof PlayerEntity && NameTags.isNameTagsActive()) {
         renderState.displayName = null;
         renderState.playerName = null;
      }
   }
}

