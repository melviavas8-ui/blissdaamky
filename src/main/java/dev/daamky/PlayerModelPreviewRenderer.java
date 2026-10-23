package dev.daamky;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public final class PlayerModelPreviewRenderer {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final long ANIMATION_EPOCH_MILLIS = System.currentTimeMillis();
   private static final Map<Model, float[]> modelBoundsCache = new HashMap<>();
// https://t.me/pasterplace

   private PlayerModelPreviewRenderer() {
   }
// https://t.me/pasterplace

   public static void renderModelPreview(DrawContext drawContext, Model model, float previewX, float previewY, float previewSize, float opacity) {
      ModelGeometry modelGeometry = model.getGeometry();
      if (modelGeometry != null && !(opacity <= 0.01F)) {
         GpuTextureView gpuTextureView = getModelTextureView(model);
         if (gpuTextureView != null) {
            float elapsedSeconds = (float)(System.currentTimeMillis() - ANIMATION_EPOCH_MILLIS) / 1000.0F;
             float[] modelBounds = modelBoundsCache.computeIfAbsent(model, cachedModel -> calculateModelBounds(modelGeometry));
            if (model.getAnimations() != null) {
               model.getAnimations().apply(modelGeometry, elapsedSeconds);
            }

            Matrix4f previewTransform = new Matrix4f()
               .rotateX((float)Math.toRadians(-18.0))
               .rotateY((float)Math.toRadians(elapsedSeconds * 45.0F))
                .translate(-modelBounds[0], -modelBounds[1], -modelBounds[2]);
             ArrayList<PlayerModelMeshData> meshDataList = new ArrayList<>();
            Matrix4fStack transformStack = new Matrix4fStack(64);
            transformStack.set(previewTransform);

            for (Object rootBone : modelGeometry.rootBones) {
               collectMeshData((ModelBone)rootBone, transformStack, meshDataList);
            }

            if (!meshDataList.isEmpty()) {
                meshDataList.sort((left, right) -> Float.compare(left.shade, right.shade));
                float modelScale = previewSize * 0.5F / Math.max(1.0E-4F, modelBounds[3]);
               float previewCenterX = previewX + previewSize * 0.5F;
               float previewCenterY = previewY + previewSize * 0.5F;
               int previewAlpha = Math.clamp((int)(opacity * 255.0F), 0, 255);
               float[] screenPositions = new float[meshDataList.size() * 8];
               float[] screenTextureCoordinates = new float[meshDataList.size() * 8];
               int[] packedVertexColors = new int[meshDataList.size()];
// https://t.me/pasterplace

               for (int quadIndex = 0; quadIndex < meshDataList.size(); quadIndex++) {
                   PlayerModelMeshData meshData = meshDataList.get(quadIndex);

                  for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                     screenPositions[quadIndex * 8 + cornerIndex * 2] = previewCenterX + ((PlayerModelPreviewRenderer.PlayerModelMeshData)meshData).positions[cornerIndex * 2] * modelScale;
                     screenPositions[quadIndex * 8 + cornerIndex * 2 + 1] = previewCenterY
                        - ((PlayerModelPreviewRenderer.PlayerModelMeshData)meshData).positions[cornerIndex * 2 + 1] * modelScale;
                  }

                  System.arraycopy(((PlayerModelPreviewRenderer.PlayerModelMeshData)meshData).textureCoordinates, 0, screenTextureCoordinates, quadIndex * 8, 8);
                  int vertexAlpha = Math.clamp((int)(((PlayerModelPreviewRenderer.PlayerModelMeshData)meshData).alpha * 255.0F), 0, 255);
                  packedVertexColors[quadIndex] = previewAlpha << 24 | vertexAlpha << 16 | vertexAlpha << 8 | vertexAlpha;
               }

               Matrix3x2f uiMatrices = new Matrix3x2f(drawContext.getMatrices());
               ScreenRect screenRect = drawContext.scissorStack.peekLast();
               GuiRenderStateBatcher.flush();
               drawContext.state
                  .addSimpleElement(
                     new PlayerModelPreviewRenderer.PlayerModelRenderState(
                        uiMatrices,
                        screenPositions,
                        screenTextureCoordinates,
                        packedVertexColors,
                        TextureSetup.of(gpuTextureView, RenderSystem.getSamplerCache().get(FilterMode.NEAREST)),
                        screenRect,
                        previewX,
                        previewY,
                        previewSize
                     )
                  );
            }
         }
      }
   }
// https://t.me/pasterplace

   private static @Nullable GpuTextureView getModelTextureView(Model model) {
      try {
         AbstractTexture modelTexture = MinecraftClient.getInstance().getTextureManager().getTexture(model.getTextureIdentifier());
         return modelTexture != null ? modelTexture.getGlTextureView() : null;
      } catch (Throwable textureLookupFailure) {
         return null;
      }
   }
// https://t.me/pasterplace

   private static float[] calculateModelBounds(ModelGeometry modelGeometry) {
      float minX = Float.MAX_VALUE;
      float minY = Float.MAX_VALUE;
      float minZ = Float.MAX_VALUE;
      float maxX = -Float.MAX_VALUE;
      float maxY = -Float.MAX_VALUE;
      float maxZ = -Float.MAX_VALUE;
       ArrayList<Vector3f> transformedVertices = new ArrayList<>();
      Matrix4fStack transformStack = new Matrix4fStack(64);

      for (Object rootBone : modelGeometry.rootBones) {
         collectTransformedVertices((ModelBone)rootBone, transformStack, transformedVertices);
      }

       for (Vector3f worldVertex : transformedVertices) {
          minX = Math.min(minX, worldVertex.x);
          minY = Math.min(minY, worldVertex.y);
          minZ = Math.min(minZ, worldVertex.z);
          maxX = Math.max(maxX, worldVertex.x);
          maxY = Math.max(maxY, worldVertex.y);
          maxZ = Math.max(maxZ, worldVertex.z);
      }
// https://t.me/pasterplace

      if (transformedVertices.isEmpty()) {
         return new float[]{0.0F, 0.0F, 0.0F, 1.0F};
      }

      float centerX = (minX + maxX) * 0.5F;
      float centerY = (minY + maxY) * 0.5F;
      float centerZ = (minZ + maxZ) * 0.5F;
      float boundingRadius = 0.0F;

       for (Vector3f boundsVertex : transformedVertices) {
          boundingRadius = Math.max(boundingRadius, boundsVertex.distance(centerX, centerY, centerZ));
      }

      return new float[]{centerX, centerY, centerZ, boundingRadius};
   }
// https://t.me/pasterplace

   static void applyBoneTransform(ModelBone bone, Matrix4fStack transformStack) {
      transformStack.translate(-bone.Ill111IlI11l / 16.0F, bone.I1l11IIlIIIll1l1lI1II / 16.0F, bone.I1I1lI111lII1 / 16.0F);
      transformStack.translate(bone.l1lll11l1l / 16.0F, bone.lIIl11l111lIIl1ll / 16.0F, bone.l1IIl11lI / 16.0F);
      transformStack.rotateZ(bone.floatValue).rotateY(bone.l11lIlIIll1III1).rotateX(bone.I1lIIII1III1lIlIl1l);
      transformStack.scale(bone.lIll1Il1lll111l11l1I1llllI, bone.lIIIlI1III1, bone.IIl1IlII1III1IIl1IIl);
      transformStack.translate(-bone.l1lll11l1l / 16.0F, -bone.lIIl11l111lIIl1ll / 16.0F, -bone.l1IIl11lI / 16.0F);
   }
// https://t.me/pasterplace

   static void applyPoseTransform(ModelBonePose pose, Matrix4fStack transformStack) {
      transformStack.translate(pose.lIIl11l111lIIl1ll / 16.0F, pose.l1IIl11lI / 16.0F, pose.I1lIIII1III1lIlIl1l / 16.0F);
      transformStack.rotateZ(pose.Ill111IlI11l).rotateY(pose.floatValue).rotateX(pose.l11lIlIIll1III1);
      transformStack.translate(-pose.lIIl11l111lIIl1ll / 16.0F, -pose.l1IIl11lI / 16.0F, -pose.I1lIIII1III1lIlIl1l / 16.0F);
   }
// https://t.me/pasterplace

   static void collectTransformedVertices(ModelBone bone, Matrix4fStack transformStack, List<Vector3f> transformedVertices) {
      transformStack.pushMatrix();
      applyBoneTransform(bone, transformStack);

      for (Object pose : bone.IllI1l1l1) {
         transformStack.pushMatrix();
         applyPoseTransform((ModelBonePose)pose, transformStack);

         for (ModelQuad quad : ((ModelBonePose)pose).II1Il11l111II11IIl) {
            if (quad != null) {
               float[] vertexPositions = quad.positions();

               for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                  transformedVertices.add(transformStack.transformPosition(vertexPositions[vertexIndex * 3], vertexPositions[vertexIndex * 3 + 1], vertexPositions[vertexIndex * 3 + 2], new Vector3f()));
               }
            }
         }
// https://t.me/pasterplace

         transformStack.popMatrix();
      }

      for (Object childBone : bone.l1I1IlllIlI) {
         collectTransformedVertices((ModelBone)childBone, transformStack, transformedVertices);
      }

      transformStack.popMatrix();
   }
// https://t.me/pasterplace

   private static void collectMeshData(ModelBone bone, Matrix4fStack transformStack, List<PlayerModelPreviewRenderer.PlayerModelMeshData> meshData) {
      transformStack.pushMatrix();
      applyBoneTransform(bone, transformStack);

      for (Object pose : bone.IllI1l1l1) {
         transformStack.pushMatrix();
         applyPoseTransform((ModelBonePose)pose, transformStack);

         for (ModelQuad quad : ((ModelBonePose)pose).II1Il11l111II11IIl) {
            if (quad != null) {
               float[] vertexPositions = quad.positions();
               float[] screenPositions = new float[8];
               float averageDepth = 0.0F;

               for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                  Vector3f transformedVertex = transformStack.transformPosition(vertexPositions[vertexIndex * 3], vertexPositions[vertexIndex * 3 + 1], vertexPositions[vertexIndex * 3 + 2], new Vector3f());
                  screenPositions[vertexIndex * 2] = transformedVertex.x;
                  screenPositions[vertexIndex * 2 + 1] = transformedVertex.y;
                  averageDepth += transformedVertex.z;
               }

               Vector3f transformedNormal = transformStack.transformDirection(quad.normalX(), quad.normalY(), quad.normalZ(), new Vector3f()).normalize();
               float shadeFactor = 0.55F + 0.45F * Math.abs(transformedNormal.z) + 0.12F * Math.max(0.0F, transformedNormal.y);
               meshData.add(new PlayerModelPreviewRenderer.PlayerModelMeshData(screenPositions, (float[])quad.textureCoordinates().clone(), averageDepth * 0.25F, Math.min(1.0F, shadeFactor)));
            }
         }

         transformStack.popMatrix();
      }
// https://t.me/pasterplace

      for (Object childBone : bone.l1I1IlllIlI) {
         collectMeshData((ModelBone)childBone, transformStack, meshData);
      }

      transformStack.popMatrix();
   }
// https://t.me/pasterplace

   private record PlayerModelMeshData(float[] positions, float[] textureCoordinates, float shade, float alpha) {

      private PlayerModelMeshData(float[] positions, float[] textureCoordinates, float shade, float alpha) {
         this.positions = positions;
         this.textureCoordinates = textureCoordinates;
         this.shade = shade;
         this.alpha = alpha;
      }
   }
// https://t.me/pasterplace

   private static final class PlayerModelRenderState implements SimpleGuiElementRenderState {
      private final Matrix3x2f matrices;
      private final float[] vertexPositions;
      private final float[] textureCoordinates;
      private final int[] vertexColors;
      private final TextureSetup textureSetup;
      private final @Nullable ScreenRect scissorArea;
      private final @Nullable ScreenRect bounds;

      PlayerModelRenderState(
         Matrix3x2f uiMatrices,
         float[] vertexPositions,
         float[] textureCoordinates,
         int[] vertexColors,
         TextureSetup textureSetup,
         @Nullable ScreenRect scissorArea,
         float previewX,
         float previewY,
         float previewSize
      ) {
         this.matrices = uiMatrices;
         this.vertexPositions = vertexPositions;
         this.textureCoordinates = textureCoordinates;
         this.vertexColors = vertexColors;
         this.textureSetup = textureSetup;
         this.scissorArea = scissorArea;
         ScreenRect previewBounds = new ScreenRect(
               (int)Math.floor(previewX - 2.0F), (int)Math.floor(previewY - 2.0F), (int)Math.ceil(previewSize + 4.0F), (int)Math.ceil(previewSize + 4.0F)
            )
            .transformEachVertex(uiMatrices);
         this.bounds = scissorArea != null ? scissorArea.intersection(previewBounds) : previewBounds;
      }

      public void setupVertices(VertexConsumer vertexConsumer) {
         for (int quadIndex = 0; quadIndex < this.vertexColors.length; quadIndex++) {
            for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
               int vertexOffset = quadIndex * 8 + cornerIndex * 2;
               vertexConsumer.vertex(this.matrices, this.vertexPositions[vertexOffset], this.vertexPositions[vertexOffset + 1])
                  .texture(this.textureCoordinates[vertexOffset], this.textureCoordinates[vertexOffset + 1])
                  .color(this.vertexColors[quadIndex]);
            }
         }
      }

      public RenderPipeline pipeline() {
         return RenderPipelines.GUI_TEXTURED;
      }

      public TextureSetup textureSetup() {
         return this.textureSetup;
      }

      public @Nullable ScreenRect scissorArea() {
         return this.scissorArea;
      }

      public @Nullable ScreenRect bounds() {
         return this.bounds;
      }
   }
}

