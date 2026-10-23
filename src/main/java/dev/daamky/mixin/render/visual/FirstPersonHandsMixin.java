package dev.daamky.mixin.render.visual;

import dev.daamky.WingsAnimationController;
import dev.daamky.SwingAnimation;
import dev.daamky.ViewModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.SwingAnimationType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class FirstPersonHandsMixin {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Shadow
   private float equipProgressMainHand;
   @Shadow
   private float lastEquipProgressMainHand;
   @Unique
   private boolean daamky$plainHand;
   @Unique
   private float daamky$inverseArmHeight;
   @Unique
   private float daamky$equipAnim;
   @Unique
   private boolean daamky$positionApplied;
   @Unique
   private boolean daamky$mainHand;
// https://t.me/pasterplace

   @Shadow
   private void renderArmHoldingItem(MatrixStack matrices, OrderedRenderCommandQueue commandQueue, int light, float equipProgress, float swingProgress, Arm arm) {
      throw new AssertionError();
   }
// https://t.me/pasterplace

   @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
   private void daamky$renderOffhandInspectAnimation(
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack heldItemStack,
      float equipProgress,
      MatrixStack matrices,
      OrderedRenderCommandQueue commandQueue,
      int light,
      CallbackInfo callbackInfo
   ) {
      if (hand == Hand.OFF_HAND && heldItemStack.isEmpty() && !player.isInvisible()) {
         float inspectionProgress = WingsAnimationController.getAnimationProgress();
         if (!(inspectionProgress <= 0.0F)) {
            Arm oppositeArm = player.getMainArm().getOpposite();
            float armDirection = oppositeArm == Arm.RIGHT ? 1.0F : -1.0F;
            matrices.push();
            matrices.translate(-armDirection * 0.22F * inspectionProgress, 0.08F * inspectionProgress, -0.05F * inspectionProgress);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-armDirection * 12.0F * inspectionProgress));
            this.daamky$mainHand = false;
            this.daamky$positionApplied = false;
            WingsAnimationController.setFirstPersonAnimationActive(true);
// https://t.me/pasterplace

            try {
               this.renderArmHoldingItem(matrices, commandQueue, light, 1.0F - inspectionProgress, 0.0F, oppositeArm);
            } finally {
               WingsAnimationController.setFirstPersonAnimationActive(false);
               matrices.pop();
            }
         }
      }
   }
// https://t.me/pasterplace

   @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
   private void daamky$prepareHand(
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack heldItemStack,
      float equipProgress,
      MatrixStack matrices,
      OrderedRenderCommandQueue commandQueue,
      int light,
      CallbackInfo callbackInfo
   ) {
      SwingAnimation.prepareSwingAnimation();
      this.daamky$plainHand = false;
      this.daamky$positionApplied = false;
      this.daamky$mainHand = hand == Hand.MAIN_HAND;
      this.daamky$inverseArmHeight = equipProgress;
      this.daamky$equipAnim = MathHelper.lerp(tickDelta, this.lastEquipProgressMainHand, this.equipProgressMainHand);
      if (!heldItemStack.isEmpty() && !heldItemStack.contains(DataComponentTypes.MAP_ID) && !heldItemStack.isOf(Items.CROSSBOW)) {
         if (!player.isUsingItem() || player.getItemUseTimeLeft() <= 0 || player.getActiveHand() != hand) {
            if (!player.isUsingRiptide()) {
               if (heldItemStack.getSwingAnimation().type() == SwingAnimationType.WHACK) {
                  this.daamky$plainHand = true;
               }
            }
         }
      }
   }
// https://t.me/pasterplace

   @Unique
   private void daamky$applyPosition(MatrixStack matrices) {
      ViewModel viewModel = ViewModel.getInstanceIfActive();
      if (viewModel != null && !this.daamky$positionApplied) {
         viewModel.translateHand(matrices, this.daamky$mainHand);
         this.daamky$positionApplied = true;
      }
   }
// https://t.me/pasterplace

   @Inject(method = "applyEatOrDrinkTransformation", at = @At("HEAD"))
   private void daamky$eatPosition(MatrixStack matrices, float tickDelta, Arm arm, ItemStack heldItemStack, PlayerEntity player, CallbackInfo callbackInfo) {
      this.daamky$applyPosition(matrices);
   }
// https://t.me/pasterplace

   @Inject(method = "applyBrushTransformation", at = @At("HEAD"))
   private void daamky$brushPosition(MatrixStack matrices, float tickDelta, Arm arm, PlayerEntity player, CallbackInfo callbackInfo) {
      this.daamky$applyPosition(matrices);
   }
// https://t.me/pasterplace

   @Inject(method = "applyEquipOffset", at = @At("HEAD"))
   private void daamky$armPosition(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo callbackInfo) {
      this.daamky$applyPosition(matrices);
   }
// https://t.me/pasterplace

   @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
   private void daamky$swingArm(float swingProgress, MatrixStack matrices, int handSign, Arm arm, CallbackInfo callbackInfo) {
      this.daamky$applyPosition(matrices);
      if (this.daamky$plainHand) {
         SwingAnimation swingAnimation = SwingAnimation.getActiveInstance();
         if (swingAnimation != null) {
            matrices.translate(-handSign * 0.56F, 0.52F + this.daamky$inverseArmHeight * 0.6F, 0.72F);
            swingAnimation.applySwingTransform(matrices, arm, swingProgress, this.daamky$inverseArmHeight, this.daamky$equipAnim);
            callbackInfo.cancel();
         }
      }
   }
// https://t.me/pasterplace

   @Inject(method = "renderArmHoldingItem", at = @At("HEAD"))
   private void daamky$playerArm(MatrixStack matrices, OrderedRenderCommandQueue commandQueue, int light, float equipProgress, float swingProgress, Arm arm, CallbackInfo callbackInfo) {
      ViewModel viewModel = ViewModel.getInstanceIfActive();
      if (viewModel != null && !this.daamky$positionApplied) {
         viewModel.transformHand(matrices, this.daamky$mainHand);
         this.daamky$positionApplied = true;
      }
   }
// https://t.me/pasterplace

   @Inject(
      method = "renderFirstPersonItem",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;I)V"
      )
   )
   private void daamky$beforeItemSubmit(
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack heldItemStack,
      float equipProgress,
      MatrixStack matrices,
      OrderedRenderCommandQueue commandQueue,
      int light,
      CallbackInfo callbackInfo
   ) {
      ViewModel viewModel = ViewModel.getInstanceIfActive();
      if (viewModel != null) {
         boolean isMainHand = hand == Hand.MAIN_HAND;
         if (!this.daamky$positionApplied) {
            viewModel.translateHand(matrices, isMainHand);
         }

         viewModel.scaleHand(matrices, isMainHand);
      }
   }
}

