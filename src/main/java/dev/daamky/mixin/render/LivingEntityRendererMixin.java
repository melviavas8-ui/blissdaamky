package dev.daamky.mixin.render;

import dev.daamky.Optimizer;
import dev.daamky.RotationPriorityManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
   @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
   private void daamky$serverRotation(LivingEntity entity, LivingEntityRenderState renderState, float tickDelta, CallbackInfo callbackInfo) {
      if (entity != MinecraftClient.getInstance().player && Optimizer.shouldSimplifyEntityRender(renderState.squaredDistanceToCamera, entity instanceof PlayerEntity)) {
         daamky$simplify(renderState);
      }

      RotationPriorityManager rotationPriorityManager = RotationPriorityManager.getInstance();
      if (entity == MinecraftClient.getInstance().player && rotationPriorityManager.isBodyYawOverrideActive() && !entity.hasVehicle()) {
         float bodyYaw = rotationPriorityManager.getInterpolatedBodyYaw(tickDelta);
         renderState.bodyYaw = bodyYaw;
         renderState.relativeHeadYaw = MathHelper.wrapDegrees(rotationPriorityManager.getInterpolatedYaw(tickDelta) - bodyYaw);
         renderState.pitch = rotationPriorityManager.getInterpolatedPitch(tickDelta);
         if (renderState.flipUpsideDown) {
            renderState.pitch *= -1.0F;
            renderState.relativeHeadYaw *= -1.0F;
         }
      }
   }

   private static void daamky$simplify(LivingEntityRenderState renderState) {
      if (renderState instanceof BipedEntityRenderState bipedRenderState) {
         bipedRenderState.equippedHeadStack = ItemStack.EMPTY;
         bipedRenderState.equippedChestStack = ItemStack.EMPTY;
         bipedRenderState.equippedLegsStack = ItemStack.EMPTY;
         bipedRenderState.equippedFeetStack = ItemStack.EMPTY;
      }

      if (renderState instanceof ArmedEntityRenderState armedRenderState) {
         armedRenderState.rightHandItemState.clear();
         armedRenderState.leftHandItemState.clear();
      }

      if (renderState instanceof PlayerEntityRenderState playerRenderState) {
         playerRenderState.hatVisible = false;
         playerRenderState.jacketVisible = false;
         playerRenderState.leftPantsLegVisible = false;
         playerRenderState.rightPantsLegVisible = false;
         playerRenderState.leftSleeveVisible = false;
         playerRenderState.rightSleeveVisible = false;
         playerRenderState.capeVisible = false;
      }
   }
}

