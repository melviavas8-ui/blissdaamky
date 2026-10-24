package dev.daamky;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.MatrixStack.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.util.Arm;
import org.joml.Quaternionf;

public final class PlayerCosmeticsFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
   private static final long INITIALIZATION_TIME_MILLIS = System.currentTimeMillis();

   public PlayerCosmeticsFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> featureRendererContext) {
      super(featureRendererContext);
   }

   @Override
   public void render(
      MatrixStack matrices,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int packedLight,
      PlayerEntityRenderState playerEntityRenderState,
      float limbAngle,
      float limbDistance
   ) {
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player != null && client.world != null && !playerEntityRenderState.invisible) {
          Collection<Model> cosmeticModels = getPlayerModels(client, playerEntityRenderState.id);
         if (!cosmeticModels.isEmpty()) {
             PlayerEntityModel entityModel = (PlayerEntityModel)this.getContextModel();
            float elapsedSeconds = (float)(System.currentTimeMillis() - INITIALIZATION_TIME_MILLIS) / 1000.0F;

             for (Model cosmeticModel : cosmeticModels) {
                if (cosmeticModel.getCosmeticType() != Wings.PETS) {
                   ModelGeometry geometry = cosmeticModel.getGeometry();
                  if (geometry != null) {
                     matrices.push();
                      if (cosmeticModel.getCosmeticType() == Wings.WATCHES) {
                        boolean isLeftArm = playerEntityRenderState.mainArm != Arm.LEFT;
                        (isLeftArm ? entityModel.leftArm : entityModel.rightArm).applyTransform(matrices);
                        renderCosmeticModel(
                           matrices,
                           orderedRenderCommandQueue,
                           packedLight,
                            cosmeticModel,
                           geometry,
                           isLeftArm,
                            playerEntityRenderState.skinTextures.model() == PlayerSkinType.SLIM,
                           elapsedSeconds
                        );
                        matrices.pop();
                     } else {
                         if (cosmeticModel.getCosmeticType().isAttachedToBody()) {
                           entityModel.body.applyTransform(matrices);
                        } else {
                           entityModel.head.applyTransform(matrices);
                        }

                        matrices.multiply(new Quaternionf().rotationZ((float) Math.PI));
                         float backOffset = cosmeticModel.getCosmeticType() == Wings.WINGS
                           ? -0.7F
                            : (cosmeticModel.getCosmeticType() == Wings.BACK ? -0.35F : 0.0F);
                         matrices.translate(cosmeticModel.getOffsetX(), cosmeticModel.getOffsetY() + backOffset, cosmeticModel.getOffsetZ());
                         if (cosmeticModel.getYaw() != 0.0F) {
                            matrices.multiply(new Quaternionf().rotationY((float)Math.toRadians(cosmeticModel.getYaw())));
                        }

                         if (cosmeticModel.getPitch() != 0.0F) {
                            matrices.multiply(new Quaternionf().rotationX((float)Math.toRadians(cosmeticModel.getPitch())));
                        }

                         if (cosmeticModel.getRoll() != 0.0F) {
                            matrices.multiply(new Quaternionf().rotationZ((float)Math.toRadians(cosmeticModel.getRoll())));
                        }

                        matrices.scale(
                            cosmeticModel.getScale(), cosmeticModel.getScale(), cosmeticModel.getScale()
                        );
                         Animations animations = cosmeticModel.getAnimations();
                         orderedRenderCommandQueue.submitCustom(matrices, RenderLayers.entityCutoutNoCull(cosmeticModel.getTextureIdentifier()), (transformEntry, vertices) -> {
                           if (animations != null) {
                              animations.apply(geometry, elapsedSeconds);
                           }

                           MatrixStack cosmeticMatrices = new MatrixStack();
                           cosmeticMatrices.peek().copy(transformEntry);

                            for (ModelBone rootBone : geometry.rootBones) {
                               renderBone(rootBone, cosmeticMatrices, vertices, packedLight);
                           }
                        });
                        matrices.pop();
                     }
                  }
               }
            }
         }
      }
   }

   public static void renderWatch(
      MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, int packedLight, ModelPart armModelPart, boolean isLeftArm, boolean isSlimSkin
   ) {
      Model model = Cape.getInstance().getEquippedModel(Wings.WATCHES);
      ModelGeometry watchGeometry = model != null ? model.getGeometry() : null;
      if (watchGeometry != null) {
         matrices.push();
         armModelPart.applyTransform(matrices);
         renderCosmeticModel(
            matrices, orderedRenderCommandQueue, packedLight, model, watchGeometry, isLeftArm, isSlimSkin, (float)(System.currentTimeMillis() - INITIALIZATION_TIME_MILLIS) / 1000.0F
         );
         matrices.pop();
      }
   }

   private static void renderCosmeticModel(
      MatrixStack matrices,
      OrderedRenderCommandQueue orderedRenderCommandQueue,
      int packedLight,
      Model cosmeticModel,
      ModelGeometry geometry,
      boolean isLeftArm,
      boolean isSlimSkin,
      float elapsedSeconds
   ) {
      if (!isLeftArm) {
         matrices.scale(-1.0F, 1.0F, 1.0F);
      }

      if (isSlimSkin) {
         matrices.translate(-0.016875F, 0.0F, 0.0F);
         matrices.scale(0.767F, 1.0F, 1.0F);
      }

      matrices.multiply(new Quaternionf().rotationZ((float) Math.PI));
      matrices.scale(cosmeticModel.getScale(), cosmeticModel.getScale(), cosmeticModel.getScale());
      orderedRenderCommandQueue.submitCustom(matrices, RenderLayers.entityCutoutNoCull(cosmeticModel.getTextureIdentifier()), (transformEntry, vertices) -> {
         LocalTime localTime = LocalTime.now();
         float secondsInMinute = localTime.getSecond() + localTime.getNano() / 1.0E9F;
         float minutesInHour = localTime.getMinute() + secondsInMinute / 60.0F;
         float hoursInHalfDay = localTime.getHour() % 12 + minutesInHour / 60.0F;
         setBoneRotation(geometry, "second", secondsInMinute / 60.0F);
         setBoneRotation(geometry, "minute", minutesInHour / 60.0F);
         setBoneRotation(geometry, "hour", hoursInHalfDay / 12.0F);
         MatrixStack matricesx = new MatrixStack();
         matricesx.peek().copy(transformEntry);

         for (Object rootBone : geometry.rootBones) {
            renderBone((ModelBone)rootBone, matricesx, vertices, packedLight);
         }
      });
   }

   private static void setBoneRotation(ModelGeometry geometry, String boneName, float rotationProgress) {
      Object bone = geometry.bonesByName.get(boneName);
      if (bone != null) {
         ((ModelBone)bone).I1lIIII1III1lIlIl1l = rotationProgress * (float) (Math.PI * 2);
      }
   }

   static Collection<Model> getPlayerModels(MinecraftClient client, int playerEntityId) {
      if (playerEntityId == client.player.getId()) {
         return Cape.getInstance().getEquippedModels();
      }

      Entity entity = client.world.getEntityById(playerEntityId);
      if (entity instanceof PlayerEntity) {
         PlayerEntity entity2 = (PlayerEntity)entity;
         GlobalsClient.PlayerCosmetics playerCosmetics = GlobalsClient.getInstance().getCosmeticsForPlayer(entity2.getGameProfile().name());
         if (playerCosmetics != null) {
            return playerCosmetics.cosmetics();
         }
      }

      return List.of();
   }

   private static void renderBone(ModelBone bone, MatrixStack matrices, VertexConsumer vertices, int packedLight) {
      matrices.push();
      matrices.translate(-bone.Ill111IlI11l / 16.0F, bone.I1l11IIlIIIll1l1lI1II / 16.0F, bone.I1I1lI111lII1 / 16.0F);
      matrices.translate(bone.l1lll11l1l / 16.0F, bone.lIIl11l111lIIl1ll / 16.0F, bone.l1IIl11lI / 16.0F);
      applyBoneRotation(matrices, bone.I1lIIII1III1lIlIl1l, bone.l11lIlIIll1III1, bone.floatValue);
      matrices.scale(bone.lIll1Il1lll111l11l1I1llllI, bone.lIIIlI1III1, bone.IIl1IlII1III1IIl1IIl);
      matrices.translate(-bone.l1lll11l1l / 16.0F, -bone.lIIl11l111lIIl1ll / 16.0F, -bone.l1IIl11lI / 16.0F);

      for (Object childPose : bone.IllI1l1l1) {
         renderBonePose((ModelBonePose)childPose, matrices, vertices, packedLight);
      }

      for (Object childBone : bone.l1I1IlllIlI) {
         renderBone((ModelBone)childBone, matrices, vertices, packedLight);
      }

      matrices.pop();
   }

   private static void renderBonePose(ModelBonePose bonePose, MatrixStack matrices, VertexConsumer vertices, int packedLight) {
      matrices.push();
      matrices.translate(bonePose.lIIl11l111lIIl1ll / 16.0F, bonePose.l1IIl11lI / 16.0F, bonePose.I1lIIII1III1lIlIl1l / 16.0F);
      applyBoneRotation(matrices, bonePose.l11lIlIIll1III1, bonePose.floatValue, bonePose.Ill111IlI11l);
      matrices.translate(-bonePose.lIIl11l111lIIl1ll / 16.0F, -bonePose.l1IIl11lI / 16.0F, -bonePose.I1lIIII1III1lIlIl1l / 16.0F);
      Entry matrixEntry = matrices.peek();

      for (ModelQuad quad : bonePose.II1Il11l111II11IIl) {
         if (quad != null) {
            float normalX = quad.normalX();
            float normalY = quad.normalY();
            float normalZ = quad.normalZ();
            if ((bonePose.IllI1l1l1 == 0.0F || bonePose.l1lll11l1l == 0.0F) && normalX < 0.0F) {
               normalX = -normalX;
            }

            if ((bonePose.l1I1IlllIlI == 0.0F || bonePose.l1lll11l1l == 0.0F) && normalY < 0.0F) {
               normalY = -normalY;
            }

            if ((bonePose.l1I1IlllIlI == 0.0F || bonePose.IllI1l1l1 == 0.0F) && normalZ < 0.0F) {
               normalZ = -normalZ;
            }

            float[] vertexPositions = quad.positions();
            float[] textureCoordinates = quad.textureCoordinates();

            for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
               vertices.vertex(matrixEntry, vertexPositions[vertexIndex * 3], vertexPositions[vertexIndex * 3 + 1], vertexPositions[vertexIndex * 3 + 2])
                  .color(-1)
                  .texture(textureCoordinates[vertexIndex * 2], textureCoordinates[vertexIndex * 2 + 1])
                  .overlay(OverlayTexture.DEFAULT_UV)
                  .light(packedLight)
                  .normal(matrixEntry, normalX, normalY, normalZ);
            }
         }
      }

      matrices.pop();
   }

   private static void applyBoneRotation(MatrixStack matrices, float pitch, float yaw, float roll) {
      if (roll != 0.0F) {
         matrices.multiply(new Quaternionf().rotationZ(roll));
      }

      if (yaw != 0.0F) {
         matrices.multiply(new Quaternionf().rotationY(yaw));
      }

      if (pitch != 0.0F) {
         matrices.multiply(new Quaternionf().rotationX(pitch));
      }
   }

}

