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
    private static final String pfd12c17d = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
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

