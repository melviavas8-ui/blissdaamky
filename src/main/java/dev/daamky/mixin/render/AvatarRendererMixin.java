package dev.daamky.mixin.render;

import dev.daamky.Cape;
import dev.daamky.WingsAnimationController;
import dev.daamky.PlayerCosmeticsFeatureRenderer;
import dev.daamky.Wings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class AvatarRendererMixin {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   @Inject(method = "<init>", at = @At("TAIL"))
   private void daamky$addCosmeticsLayer(Context rendererContext, boolean slimArms, CallbackInfo callbackInfo) {
      FeatureRendererContext featureRendererContext = (FeatureRendererContext)this;
      ((LivingEntityRendererInvoker)this).daamky$addLayer(new PlayerCosmeticsFeatureRenderer(featureRendererContext));
   }
// https://t.me/pasterplace

   @Inject(
      method = "renderArm",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;resetTransform()V", shift = Shift.AFTER, ordinal = 0)
   )
   private void daamky$animateWingArmPose(MatrixStack matrices, OrderedRenderCommandQueue commandQueue, int light, Identifier skinTexture, ModelPart armModelPart, boolean armFlag, CallbackInfo callbackInfo) {
      if (WingsAnimationController.isFirstPersonAnimationActive()) {
         PlayerEntityModel playerModel = (PlayerEntityModel)((LivingEntityRenderer)(Object)this).getModel();
         float wingAnimationProgress = WingsAnimationController.getAnimationProgress();
         float armSideSign = armModelPart == playerModel.leftArm ? 1.0F : -1.0F;
         armModelPart.yaw += armSideSign * 1.25F * wingAnimationProgress;
         armModelPart.roll += armSideSign * 0.15F * wingAnimationProgress;
      }
   }
// https://t.me/pasterplace

   @Inject(method = "renderArm", at = @At("TAIL"))
   private void daamky$renderWatchAccessory(MatrixStack matrices, OrderedRenderCommandQueue commandQueue, int light, Identifier skinTexture, ModelPart armModelPart, boolean armFlag, CallbackInfo callbackInfo) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && Cape.getInstance().getEquippedModel(Wings.WATCHES) != null) {
         PlayerEntityModel playerModel = (PlayerEntityModel)((LivingEntityRenderer)(Object)this).getModel();
         boolean leftArm = client.player.getMainArm() != Arm.LEFT;
         if (armModelPart == (leftArm ? playerModel.leftArm : playerModel.rightArm)) {
            PlayerCosmeticsFeatureRenderer.renderWatch(matrices, commandQueue, light, armModelPart, leftArm, client.player.getSkin().model() == PlayerSkinType.SLIM);
         }
      }
   }
}

