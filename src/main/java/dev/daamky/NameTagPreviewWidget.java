package dev.daamky;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public final class NameTagPreviewWidget {
   public static final float PREVIEW_WIDTH = 130.0F;
   private static final float MODEL_HEIGHT_DIVISOR = 3.4F;
   private static final float MODEL_SCALE = 1.05F;
   private float visibilityProgress = 0.0F;
   private long previewStartedAtMillis = 0L;
   private float modelYaw = 0.0F;
   private float modelPitch = 0.0F;
   private boolean isDragging = false;
   private float lastMouseX;
   private float lastMouseY;
   private float previewX;
   private float previewY;
   private float previewWidth;
   private float previewHeight;

   public void updateVisibility(float deltaSeconds, boolean visible) {
      float targetVisibility = visible ? 1.0F : 0.0F;
      if (visible && this.visibilityProgress < 0.001F) {
         this.previewStartedAtMillis = System.currentTimeMillis();
         this.modelYaw = 0.0F;
         this.modelPitch = 0.0F;
      }

      this.visibilityProgress = this.visibilityProgress + (targetVisibility - this.visibilityProgress) * (1.0F - (float)Math.exp(-10.0F * deltaSeconds));
      if (Math.abs(targetVisibility - this.visibilityProgress) < 0.001F) {
         this.visibilityProgress = targetVisibility;
      }

      if (!visible) {
         this.isDragging = false;
      }
   }

   public float getVisibilityProgress() {
      return this.visibilityProgress;
   }

   public boolean isVisible() {
      return this.visibilityProgress > 0.001F;
   }

   public boolean beginDrag(float mouseX, float mouseY, int mouseButton) {
      if (this.visibilityProgress < 0.5F || mouseButton != 0) {
         return false;
      } else if (mouseX >= this.previewX
         && mouseX <= this.previewX + this.previewWidth
         && mouseY >= this.previewY
         && mouseY <= this.previewY + this.previewHeight) {
         this.isDragging = true;
         this.lastMouseX = mouseX;
         this.lastMouseY = mouseY;
         return true;
      } else {
         return false;
      }
   }

   public void dragTo(float mouseX, float mouseY) {
      if (this.isDragging) {
         this.modelYaw = this.modelYaw + (mouseX - this.lastMouseX) * 2.2F;
         this.modelPitch = Math.clamp(this.modelPitch - (mouseY - this.lastMouseY) * 1.4F, -35.0F, 35.0F);
         this.lastMouseX = mouseX;
         this.lastMouseY = mouseY;
      }
   }

   public void endDrag(int mouseButton) {
      if (mouseButton == 0) {
         this.isDragging = false;
      }
   }

   public void renderPreview(DrawContext drawContext, String playerName, float previewX, float previewY, float previewWidth, float animationProgress) {
      float animatedOpacity = animationProgress * this.visibilityProgress;
      this.previewWidth = 0.0F;
      if (!(animatedOpacity < 0.01F)) {
         RoundedGradientRectBuilder.at(previewX, previewY, 0.75F, previewWidth)
            .cornerRadius(0.0F)
            .color(Accent.getTextColorWithAlpha((int)(12.0F * animatedOpacity)).toArgb())
            .render(drawContext);
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Превью", previewX + 8.0F, previewY + 10.25F, 5.0F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(240.0F * animatedOpacity)))
            .render(drawContext);
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, playerName, previewX + 130.0F - 8.0F, previewY + 10.25F, 4.4F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .alignment(HorizontalTextAlignment.RIGHT)
            .color(Accent.getDeepTextColor((int)(200.0F * animatedOpacity)))
            .render(drawContext);
         this.previewX = previewX + 2.0F;
         this.previewY = previewY + 20.0F;
         this.previewWidth = 124.0F;
         this.previewHeight = previewWidth - 32.0F;
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "ЛКМ: вращать", previewX + 65.0F, previewY + previewWidth - 7.0F, 3.8F)
            .centered()
            .color(Accent.getDeepTextColor((int)(140.0F * animatedOpacity)))
            .render(drawContext);
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player == null) {
            GuiTextBuilder.createAt(
                  Medium.DEFAULT_FONT,
                  "Зайдите в мир",
                  this.previewX + this.previewWidth * 0.5F,
                  this.previewY + this.previewHeight * 0.5F,
                  4.6F
               )
               .centered()
               .color(Accent.getDeepTextColor((int)(190.0F * animatedOpacity)))
               .render(drawContext);
         } else if (!(animationProgress < 0.5F) && !(this.visibilityProgress < 0.5F)) {
            float entranceProgress = Math.clamp((float)(System.currentTimeMillis() - this.previewStartedAtMillis) / 420.0F, 0.0F, 1.0F);
            float easedEntranceProgress = 1.0F - (float)Math.pow(1.0F - entranceProgress, 3.0);
            Vector2f transformedTopLeft = drawContext.getMatrices().transformPosition(this.previewX, this.previewY, new Vector2f());
            Vector2f transformedBottomRight = drawContext.getMatrices()
               .transformPosition(this.previewX + this.previewWidth, this.previewY + this.previewHeight, new Vector2f());
            int clipLeft = (int)Math.floor(transformedTopLeft.x);
            int clipTop = (int)Math.floor(transformedTopLeft.y);
            int clipRight = (int)Math.ceil(transformedBottomRight.x);
            int clipBottom = (int)Math.ceil(transformedBottomRight.y);
            if (clipRight - clipLeft >= 4 && clipBottom - clipTop >= 4) {
               float modelScale = Math.min((clipBottom - clipTop) / 3.4F, (clipRight - clipLeft) / 2.4F) * (0.7F + 0.3F * easedEntranceProgress);
               GuiEventRenderer.flushBatchedElements();
               drawContext.state
                  .addSpecialElement(
                     new EspPreviewRenderState(
                        this.modelYaw, this.modelPitch, 1.05F, clipLeft, clipTop, clipRight, clipBottom, modelScale, drawContext.scissorStack.peekLast()
                     )
                  );
               GuiEventRenderer.flushAndStartRootLayer(drawContext);
               NameTags nameTagsModule = (NameTags)ModuleRegistry.getInstance().getModuleByClass(NameTags.class);
               if (nameTagsModule != null && nameTagsModule.isEnabled() && DaamkyEspPreview.isPreviewActive() && easedEntranceProgress > 0.85F) {
                  Matrix3x2f inverseTransform = drawContext.getMatrices().invert(new Matrix3x2f());
                  Vector2f nameTagPosition = inverseTransform.transformPosition(
                     clipLeft + (clipRight - clipLeft) * DaamkyEspPreview.getProjectedHeadX(), clipTop + (clipBottom - clipTop) * DaamkyEspPreview.getProjectedHeadY(), new Vector2f()
                  );
                  nameTagsModule.renderPlayerTagPreview(drawContext, client.player, nameTagPosition.x, nameTagPosition.y - 6.0F, 0.8F);
               }
            }
         } else {
            this.previewStartedAtMillis = System.currentTimeMillis();
         }
      }
   }
}

