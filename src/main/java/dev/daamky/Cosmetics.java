package dev.daamky;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class Cosmetics {
   private static final float UNUSED_LAYOUT_CONSTANT_5_0 = 5.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_11_5 = 11.5F;
   private static final float UNUSED_LAYOUT_CONSTANT_7_5 = 7.5F;
   private static final float UNUSED_LAYOUT_CONSTANT_9_0 = 9.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_5_6 = 5.6F;
   private static final float UNUSED_LAYOUT_CONSTANT_3_0 = 3.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_22_0 = 22.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_2_5 = 2.5F;
   private static final float UNUSED_LAYOUT_CONSTANT_4_0 = 4.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_8_0 = 8.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_17_0 = 17.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_4_25 = 4.25F;
   private static final float UNUSED_LAYOUT_CONSTANT_15_0 = 15.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_0_5 = 0.5F;
   private static final float UNUSED_LAYOUT_CONSTANT_13_0 = 13.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_7_0 = 7.0F;
   private static final float UNUSED_LAYOUT_CONSTANT_5_4 = 5.4F;
   private static final float UNUSED_LAYOUT_CONSTANT_4_8 = 4.8F;
   private static final float UNUSED_LAYOUT_CONSTANT_4_2 = 4.2F;
   private static final MsdfFont cosmeticsHeadingFont = Medium.loadFont("brokke_bold");
   private static final Wings[] cosmeticCategories = new Wings[]{
      null, Wings.WINGS, Wings.BACK, Wings.PETS, Wings.HATS, Wings.WATCHES
   };
   private final float[] categoryHoverAmounts = new float[cosmeticCategories.length];
   private final float[] categorySelectionAmounts = new float[cosmeticCategories.length];
   private final Map<Model, float[]> modelAnimationStates = new HashMap<>();
   private int selectedCategoryIndex = 0;
   private boolean useFullBodyPreviewRotation;
   private float previewRotationBlend;
   private long modelEntryAnimationStartMs = 0L;
   private float frameDeltaSeconds = 0.016F;
   private long lastRenderTimeMs = 0L;
   private long categoryEntryAnimationStartMs = 0L;
   private float modelScrollOffset = 0.0F;
   private float targetModelScrollOffset = 0.0F;
   private float maxModelScrollOffset = 0.0F;
   private float categoryPanelLeft;
   private float categoryPanelTop;
   private float categoryPanelWidth;
   private float categoryPanelHeight;
   private float modelPanelLeft;
   private float modelPanelTop;
   private float modelPanelWidth;
   private float modelPanelHeight;

   public void updateAnimationState(float deltaSeconds) {
      this.frameDeltaSeconds = deltaSeconds;
      this.targetModelScrollOffset = Math.clamp(this.targetModelScrollOffset, 0.0F, this.maxModelScrollOffset);
      this.modelScrollOffset = this.modelScrollOffset + (this.targetModelScrollOffset - this.modelScrollOffset) * (1.0F - (float)Math.exp(-6.5F * deltaSeconds));
      if (Math.abs(this.targetModelScrollOffset - this.modelScrollOffset) < 0.05F) {
         this.modelScrollOffset = this.targetModelScrollOffset;
      }

      this.previewRotationBlend = this.previewRotationBlend + ((this.useFullBodyPreviewRotation ? 1.0F : 0.0F) - this.previewRotationBlend) * (1.0F - (float)Math.exp(-7.0F * deltaSeconds));
      float animationBlend = 1.0F - (float)Math.exp(-10.0F * deltaSeconds);
      Cape cosmeticManager = Cape.getInstance();

      for (Map.Entry<Model, float[]> modelAnimationEntry : this.modelAnimationStates.entrySet()) {
         float[] animationState = modelAnimationEntry.getValue();
         animationState[1] += ((cosmeticManager.isEquipped(modelAnimationEntry.getKey()) ? 1.0F : 0.0F) - animationState[1]) * animationBlend;
         animationState[2] += (0.0F - animationState[2]) * (1.0F - (float)Math.exp(-11.0F * deltaSeconds));
      }
   }

   public void render(
      DrawContext drawContext,
      float mouseX,
      float mouseY,
      float categoryPanelLeft,
      float categoryPanelTop,
      float categoryPanelWidth,
      float categoryPanelHeight,
      float modelPanelLeft,
      float modelPanelTop,
      float modelPanelWidth,
      float modelPanelHeight,
      float panelAlpha
   ) {
      if (!(panelAlpha <= 0.0F)) {
         long currentTimeMs = System.currentTimeMillis();
         if (currentTimeMs - this.lastRenderTimeMs > 250L) {
            this.categoryEntryAnimationStartMs = currentTimeMs;
            this.modelEntryAnimationStartMs = currentTimeMs;
         }

         this.lastRenderTimeMs = currentTimeMs;
         this.categoryPanelLeft = categoryPanelLeft;
         this.categoryPanelTop = categoryPanelTop;
         this.categoryPanelWidth = categoryPanelWidth;
         this.categoryPanelHeight = categoryPanelHeight;
         this.modelPanelLeft = modelPanelLeft;
         this.modelPanelTop = modelPanelTop;
         this.modelPanelWidth = modelPanelWidth;
         this.modelPanelHeight = modelPanelHeight;
         this.drawCategoryTabs(drawContext, mouseX, mouseY, currentTimeMs, panelAlpha);
         this.drawModelPanel(drawContext, mouseX, mouseY, currentTimeMs, panelAlpha);
      }
   }

   private float getCategoryListTop() {
      return this.categoryPanelTop + 11.5F + 7.5F;
   }

   private float getModelListTop() {
      return this.getCategoryListTop() + 22.0F + 9.0F;
   }

   private float getCategoryTileWidth() {
      return (this.categoryPanelWidth - 2.5F * (cosmeticCategories.length - 1)) / cosmeticCategories.length;
   }

   private float getCategoryTabLeft(int categoryIndex) {
      return this.categoryPanelLeft + categoryIndex * (this.getCategoryTileWidth() + 2.5F);
   }

   private static String getCategoryLabel(Wings wings) {
      return wings == null ? "Все" : wings.getDisplayName();
   }

   private void drawCategoryTabs(DrawContext drawContext, float mouseX, float mouseY, long currentTimeMs, float panelAlpha) {
      float headingY = this.categoryPanelTop + 5.75F;
      drawContext.enableScissor(
         PixelRounding.II1Il11l111II11IIl(this.categoryPanelLeft),
         PixelRounding.II1Il11l111II11IIl(this.categoryPanelTop),
         PixelRounding.l1I1IlllIlI(this.categoryPanelLeft + this.categoryPanelWidth),
         PixelRounding.l1I1IlllIlI(this.categoryPanelTop + this.categoryPanelHeight)
      );

      try {
         GuiTextBuilder.createAt(Medium.ICON_FONT, "H", this.categoryPanelLeft + 5.0F + 2.8F, headingY, 5.6F)
            .centered()
            .color(Accent.getAccentColorWithAlpha((int)(235.0F * panelAlpha)))
            .render(drawContext);
         GuiTextBuilder.createAt(cosmeticsHeadingFont, "Cosmetics", this.categoryPanelLeft + 5.0F + 5.6F + 3.0F, headingY, 5.4F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(245.0F * panelAlpha)))
            .render(drawContext);
         Cape cosmeticManager = Cape.getInstance();
         float categoryListTop = this.getCategoryListTop();
         float tabWidth = this.getCategoryTileWidth();

         for (int categoryIndex = 0; categoryIndex < cosmeticCategories.length; categoryIndex++) {
            float tabLeft = this.getCategoryTabLeft(categoryIndex);
            float hoverAmount = this.updateHoverAnimation(this.categoryHoverAmounts, categoryIndex, containsPoint(mouseX, mouseY, tabLeft, categoryListTop, tabWidth, 22.0F));
            float selectionAmount = this.updateHoverAnimation(this.categorySelectionAmounts, categoryIndex, categoryIndex == this.selectedCategoryIndex);
            float entranceAmount = easeOutCubic((float)(currentTimeMs - this.categoryEntryAnimationStartMs - categoryIndex * 30L) / 220.0F);
            if (!(entranceAmount <= 0.002F)) {
               float tabAlpha = panelAlpha * entranceAmount;
               float tabTop = categoryListTop + (1.0F - entranceAmount) * 5.0F;
               float emphasisAmount = Math.max(hoverAmount, selectionAmount);
               RoundedGradientRectBuilder.at(tabLeft, tabTop, tabWidth, 22.0F)
                  .cornerRadius(3.5F)
                  .color(
                     ColorUtils.interpolateColor(
                        Accent.getTextColorWithAlpha((int)((6.0F + 6.0F * hoverAmount) * tabAlpha)).toArgb(),
                        Accent.getAccentColorWithAlpha((int)(60.0F * tabAlpha)).toArgb(),
                        selectionAmount
                     )
                  )
                  .render(drawContext);
               if (selectionAmount > 0.01F) {
                  RoundedRectBuilder.createAt(tabLeft, tabTop, tabWidth, 22.0F)
                     .cornerRadius(3.5F)
                     .borderWidth(0.6F)
                     .color(Accent.getAccentColorWithAlpha((int)(150.0F * selectionAmount * tabAlpha)))
                     .render(drawContext);
               }

               float iconCenterX = tabLeft + tabWidth * 0.5F;
               int iconColor = ColorUtils.interpolateColor(
                  Accent.getDarkerTextColor((int)(170.0F * tabAlpha)).toArgb(), Accent.getAccentColorWithAlpha((int)(255.0F * tabAlpha)).toArgb(), emphasisAmount
               );
               drawCategoryIcon(drawContext, cosmeticCategories[categoryIndex], iconCenterX, tabTop + 8.0F, 1.0F + selectionAmount * 0.12F, iconColor);
               String categoryLabel = Medium.DEFAULT_FONT.truncateToWidth(getCategoryLabel(cosmeticCategories[categoryIndex]), 3.8F, tabWidth - 2.0F);
               GuiTextBuilder.createAt(Medium.DEFAULT_FONT, categoryLabel, iconCenterX, tabTop + 16.5F, 3.8F)
                  .centered()
                  .color(
                     ColorUtils.interpolateColor(
                        Accent.getDeepTextColor((int)(200.0F * tabAlpha)).toArgb(), Accent.getTextColorWithAlpha((int)(255.0F * tabAlpha)).toArgb(), emphasisAmount
                     )
                  )
                  .render(drawContext);
            }
         }

         String selectedModelCount = String.valueOf(cosmeticManager.getModelsByCategory(cosmeticCategories[this.selectedCategoryIndex]).size());
         float countTextWidth = Medium.DEFAULT_FONT.measureWidth(selectedModelCount, 4.2F);
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, selectedModelCount, this.categoryPanelLeft + this.categoryPanelWidth - 5.0F - countTextWidth, headingY, 4.2F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getAccentColorWithAlpha((int)(220.0F * panelAlpha)))
            .render(drawContext);
      } finally {
         drawContext.disableScissor();
      }

      float dividerY = this.getModelListTop() - 4.5F;
      RoundedGradientRectBuilder.at(this.categoryPanelLeft - 3.0F, dividerY - 0.375F, this.categoryPanelWidth + 6.0F, 0.75F)
         .cornerRadius(0.0F)
         .color(Accent.getTextColorWithAlpha((int)(12.0F * panelAlpha)))
         .render(drawContext);
   }

   private float getModelListLeft() {
      return this.modelPanelLeft + 4.0F;
   }

   private float getModelListWidth() {
      return this.modelPanelWidth - 4.0F - 8.0F;
   }

   private float getModelListContentTop() {
      return this.modelPanelTop + 17.0F;
   }

   private static void drawRotatedShape(DrawContext drawContext, float centerX, float centerY, float width, float height, float rotationDegrees, int color) {
      drawContext.getMatrices().pushMatrix();
      drawContext.getMatrices().translate(centerX, centerY);
      drawContext.getMatrices().rotate(rotationDegrees * (float) (Math.PI / 180.0));
      RoundedGradientRectBuilder.at(-width * 0.5F, -height * 0.5F, width, height)
         .cornerRadius(Math.min(width, height) * 0.5F)
         .color(color)
         .render(drawContext);
      drawContext.getMatrices().popMatrix();
   }

   private static void drawCircle(DrawContext drawContext, float centerX, float centerY, float diameter, int color) {
      RoundedGradientRectBuilder.at(centerX - diameter * 0.5F, centerY - diameter * 0.5F, diameter, diameter)
         .cornerRadius(diameter * 0.5F)
         .color(color)
         .render(drawContext);
   }

   private static void drawCategoryIcon(DrawContext drawContext, Wings wings, float iconX, float iconY, float scale, int iconColor) {
      drawContext.getMatrices().pushMatrix();
      drawContext.getMatrices().translate(iconX, iconY);
      drawContext.getMatrices().scale(scale);

      try {
         if (wings == null) {
            float diamondSize = 2.3F;
            float diamondGap = 0.8F;

            for (int columnIndex = 0; columnIndex < 2; columnIndex++) {
               for (int rowIndex = 0; rowIndex < 2; rowIndex++) {
                  RoundedGradientRectBuilder.at(-diamondSize - diamondGap * 0.5F + columnIndex * (diamondSize + diamondGap), -diamondSize - diamondGap * 0.5F + rowIndex * (diamondSize + diamondGap), diamondSize, diamondSize)
                     .cornerRadius(0.6F)
                     .color(iconColor)
                     .render(drawContext);
               }
            }
         } else if (wings == Wings.WINGS) {
            for (byte wingSide = -1; wingSide <= 1; wingSide += 2) {
               drawRotatedShape(drawContext, wingSide * 1.9F, -1.4F, 3.6F, 0.9F, wingSide * -28.0F, iconColor);
               drawRotatedShape(drawContext, wingSide * 2.0F, 0.0F, 3.2F, 0.9F, wingSide * -8.0F, iconColor);
               drawRotatedShape(drawContext, wingSide * 1.8F, 1.3F, 2.4F, 0.9F, wingSide * 14.0F, iconColor);
            }

            drawCircle(drawContext, 0.0F, 0.0F, 1.1F, iconColor);
         } else if (wings == Wings.BACK) {
            RoundedRectBuilder.createAt(-1.4F, -3.4F, 2.8F, 1.8F)
               .cornerRadius(0.9F)
               .borderWidth(0.6F)
               .color(iconColor)
               .render(drawContext);
            RoundedGradientRectBuilder.at(-2.6F, -2.0F, 5.2F, 5.4F).cornerRadius(1.3F).color(iconColor).render(drawContext);
            RoundedGradientRectBuilder.at(-1.5F, 0.6F, 3.0F, 0.6F).cornerRadius(0.3F).color(-2013265920).render(drawContext);
         } else if (wings == Wings.PETS) {
            RoundedGradientRectBuilder.at(-1.8F, 0.0F, 3.6F, 2.9F).cornerRadius(1.4F).color(iconColor).render(drawContext);
            drawCircle(drawContext, -2.4F, -0.9F, 1.4F, iconColor);
            drawCircle(drawContext, -0.8F, -2.4F, 1.4F, iconColor);
            drawCircle(drawContext, 0.8F, -2.4F, 1.4F, iconColor);
            drawCircle(drawContext, 2.4F, -0.9F, 1.4F, iconColor);
         } else if (wings == Wings.WATCHES) {
            RoundedGradientRectBuilder.at(-1.3F, -3.6F, 2.6F, 7.2F).cornerRadius(0.8F).color(iconColor).render(drawContext);
            RoundedRectBuilder.createAt(-2.6F, -2.6F, 5.2F, 5.2F)
               .cornerRadius(2.6F)
               .borderWidth(0.8F)
               .color(iconColor)
               .render(drawContext);
            RoundedGradientRectBuilder.at(-1.9F, -1.9F, 3.8F, 3.8F).cornerRadius(1.9F).color(-1442840576).render(drawContext);
            drawRotatedShape(drawContext, 0.0F, -0.7F, 0.5F, 1.6F, 0.0F, iconColor);
            drawRotatedShape(drawContext, 0.45F, 0.0F, 1.1F, 0.5F, 0.0F, iconColor);
         } else {
            RoundedGradientRectBuilder.at(-3.4F, 1.6F, 6.8F, 1.1F).cornerRadius(0.55F).color(iconColor).render(drawContext);
            RoundedGradientRectBuilder.at(-1.9F, -2.8F, 3.8F, 4.6F).cornerRadius(1.0F).color(iconColor).render(drawContext);
            RoundedGradientRectBuilder.at(-1.9F, 0.4F, 3.8F, 0.6F).cornerRadius(0.0F).color(1711276032).render(drawContext);
         }
      } finally {
         drawContext.getMatrices().popMatrix();
      }
   }

   private float getCategoryPanelLeft() {
      return this.categoryPanelLeft;
   }

   private float getCategoryPanelWidth() {
      return this.categoryPanelWidth;
   }

   private float getWingsListTopY() {
      return this.getModelListTop();
   }

   private float getVisibleModelListHeight() {
      return this.categoryPanelTop + this.categoryPanelHeight - this.getWingsListTopY();
   }

   private float getModelPreviewHeight() {
      return this.modelPanelTop + this.modelPanelHeight - this.getModelListContentTop() - 2.0F;
   }

   private List<Model> getModelsInSelectedCategory() {
      return Cape.getInstance().getModelsByCategory(cosmeticCategories[this.selectedCategoryIndex]);
   }

   private float[] getModelAnimationState(Model model) {
      return this.modelAnimationStates.computeIfAbsent(model, modelx -> new float[]{0.0F, Cape.getInstance().isEquipped(modelx) ? 1.0F : 0.0F, 0.0F});
   }

   private void drawModelPanel(DrawContext drawContext, float panelAlpha, float mouseY, long currentTimeMs, float panelOpacity) {
      float panelRight = this.modelPanelLeft + this.modelPanelWidth - 6.0F;
      drawContext.enableScissor(
         PixelRounding.II1Il11l111II11IIl(this.modelPanelLeft),
         PixelRounding.II1Il11l111II11IIl(this.modelPanelTop),
         PixelRounding.l1I1IlllIlI(panelRight),
         PixelRounding.l1I1IlllIlI(this.modelPanelTop + this.modelPanelHeight)
      );

      try {
         this.drawModelPanelHeader(drawContext, panelOpacity);
         this.drawModelPreview(drawContext, panelAlpha, mouseY, currentTimeMs, panelOpacity);
      } finally {
         drawContext.disableScissor();
      }

      this.drawModelList(drawContext, panelAlpha, mouseY, currentTimeMs, panelOpacity);
   }

   private void drawModelList(DrawContext drawContext, float mouseX, float mouseY, long currentTimeMs, float panelAlpha) {
      drawContext.enableScissor(
         PixelRounding.II1Il11l111II11IIl(this.categoryPanelLeft),
         PixelRounding.II1Il11l111II11IIl(this.categoryPanelTop),
         PixelRounding.l1I1IlllIlI(this.categoryPanelLeft + this.categoryPanelWidth),
         PixelRounding.l1I1IlllIlI(this.categoryPanelTop + this.categoryPanelHeight)
      );

      try {
         float listLeft = this.getCategoryPanelLeft();
         float listWidth = this.getCategoryPanelWidth();
         float listTop = this.getWingsListTopY();
         float visibleHeight = this.getVisibleModelListHeight();
         if (!(visibleHeight <= 2.0F)) {
            List<Model> models = this.getModelsInSelectedCategory();
            float contentHeight = Math.max(0.0F, models.size() * 15.5F - 0.5F);
            this.maxModelScrollOffset = Math.max(0.0F, contentHeight - visibleHeight);
            this.targetModelScrollOffset = Math.clamp(this.targetModelScrollOffset, 0.0F, this.maxModelScrollOffset);
            this.modelScrollOffset = Math.clamp(this.modelScrollOffset, 0.0F, this.maxModelScrollOffset);
            boolean pointerInsideList = mouseX >= listLeft && mouseX <= listLeft + listWidth && mouseY >= listTop && mouseY <= listTop + visibleHeight;
            drawContext.enableScissor(
               PixelRounding.II1Il11l111II11IIl(listLeft - 1.0F),
               PixelRounding.II1Il11l111II11IIl(listTop),
               PixelRounding.l1I1IlllIlI(listLeft + listWidth + 1.0F),
               PixelRounding.l1I1IlllIlI(listTop + visibleHeight)
            );

            try {
               for (int modelIndex = 0; modelIndex < models.size(); modelIndex++) {
                  Model model = models.get(modelIndex);
                  float[] animationState = this.getModelAnimationState(model);
                  float rowTop = listTop + modelIndex * 15.5F - this.modelScrollOffset;
                  boolean isHovered = pointerInsideList && mouseY >= rowTop && mouseY <= rowTop + 15.0F;
                  animationState[0] += ((isHovered ? 1.0F : 0.0F) - animationState[0]) * (1.0F - (float)Math.exp(-6.0F * this.frameDeltaSeconds));
                  if (!(rowTop + 15.0F < listTop) && !(rowTop > listTop + visibleHeight)) {
                     float entranceAmount = easeOutCubic((float)(currentTimeMs - this.modelEntryAnimationStartMs - Math.min(modelIndex, 12) * 25L) / 260.0F);
                     if (!(entranceAmount <= 0.0F)) {
                        float rowAlpha = panelAlpha * entranceAmount;
                        float hoverAmount = smoothStep(animationState[0]);
                        float selectionAmount = animationState[1];
                        float clickPulse = animationState[2];
                        drawContext.getMatrices().pushMatrix();
                        drawContext.getMatrices().translate((1.0F - entranceAmount) * 10.0F, 0.0F);

                        try {
                           float highlightAmount = 7.0F * hoverAmount + 10.0F * clickPulse + 5.0F * selectionAmount;
                           if (highlightAmount > 0.1F) {
                              float highlightExpansion = clickPulse * 1.2F;
                              RoundedGradientRectBuilder.at(listLeft - highlightExpansion, rowTop - highlightExpansion * 0.5F, listWidth + highlightExpansion * 2.0F, 15.0F + highlightExpansion)
                                 .cornerRadius(3.5F)
                                 .color(Accent.getTextColorWithAlpha((int)(highlightAmount * rowAlpha)))
                                 .render(drawContext);
                           }

                           float rowCenterY = rowTop + 7.5F;
                           float previewLeft = listLeft + 2.0F;
                           float previewSize = 12.0F + hoverAmount * 1.0F;
                           PlayerModelPreviewRenderer.renderModelPreview(
                              drawContext, model, previewLeft - (previewSize - 12.0F) * 0.5F, rowCenterY - previewSize * 0.5F, previewSize, rowAlpha
                           );
                           float modelNameLeft = previewLeft + 15.0F;
                           float actionColumnRight = listLeft + listWidth - 4.0F;
                           float modelNameRight = actionColumnRight - 13.0F;
                           float modelNameWidth = modelNameRight - modelNameLeft - 4.0F;
                           GuiTextBuilder.createAt(
                                 cosmeticsHeadingFont,
                                 cosmeticsHeadingFont.truncateToWidth(model.getDisplayName(), 4.8F, modelNameWidth),
                                 modelNameLeft,
                                 rowCenterY - (this.selectedCategoryIndex == 0 ? 2.4F : 0.0F),
                                 4.8F
                              )
                              .baselinePosition(TextBaselinePosition.CENTER)
                              .color(
                                 ColorUtils.interpolateColor(
                                    Accent.getDarkerTextColor((int)(235.0F * rowAlpha)).toArgb(),
                                    Accent.getTextColorWithAlpha((int)(255.0F * rowAlpha)).toArgb(),
                                    Math.max(hoverAmount, selectionAmount)
                                 )
                              )
                              .render(drawContext);
                           if (this.selectedCategoryIndex == 0) {
                              GuiTextBuilder.createAt(
                                    Medium.DEFAULT_FONT, model.getCosmeticType().getDisplayName(), modelNameLeft, rowCenterY + 3.0F, 3.6F
                                 )
                                 .baselinePosition(TextBaselinePosition.CENTER)
                                 .color(Accent.getDeepTextColor((int)(170.0F * rowAlpha)))
                                 .render(drawContext);
                           }

                           this.drawToggleSwitch(drawContext, selectionAmount, modelNameRight, rowCenterY, rowAlpha);
                        } finally {
                           drawContext.getMatrices().popMatrix();
                        }
                     }
                  }
               }

               return;
            } finally {
               drawContext.disableScissor();
            }
         }
      } finally {
         drawContext.disableScissor();
      }
   }

   private void drawModelPanelHeader(DrawContext drawContext, float panelAlpha) {
      float headingY = this.modelPanelTop + 4.25F;
      float titleLeft = this.modelPanelLeft + 4.0F + 4.0F;
      GuiTextBuilder.createAt(Medium.ICON_FONT, "H", titleLeft + 2.5F, headingY, 5.6F)
         .centered()
         .color(Accent.getAccentColorWithAlpha((int)(240.0F * panelAlpha)))
         .render(drawContext);
      GuiTextBuilder.createAt(cosmeticsHeadingFont, getCategoryLabel(cosmeticCategories[this.selectedCategoryIndex]), titleLeft + 10.0F, headingY, 5.4F)
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(Accent.getTextColorWithAlpha((int)(250.0F * panelAlpha)))
         .render(drawContext);
   }

   private void drawModelPreview(DrawContext drawContext, float mouseX, float mouseY, long currentTimeMs, float panelAlpha) {
      float previewLeft = this.getModelListLeft();
      float previewTop = this.getModelListContentTop();
      float previewWidth = this.getModelListWidth();
      float previewHeight = this.getModelPreviewHeight();
      float entranceAmount = easeOutCubic((float)(currentTimeMs - this.categoryEntryAnimationStartMs - 60L) / 320.0F);
      float previewAlpha = panelAlpha * entranceAmount;
      MinecraftClient client = MinecraftClient.getInstance();
      if (client.player == null) {
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Зайдите в мир для превью", previewLeft + previewWidth * 0.5F, previewTop + previewHeight * 0.5F, 4.6F)
            .centered()
            .color(Accent.getDeepTextColor((int)(190.0F * previewAlpha)))
            .render(drawContext);
      } else if (!(panelAlpha < 0.97F) && !(entranceAmount < 0.97F)) {
         Vector2f previewTopLeftScreen = drawContext.getMatrices().transformPosition(previewLeft + 1.0F, previewTop + 1.0F, new Vector2f());
         Vector2f previewBottomRightScreen = drawContext.getMatrices().transformPosition(previewLeft + previewWidth - 1.0F, previewTop + previewHeight - 1.0F, new Vector2f());
         Vector2f mousePositionScreen = drawContext.getMatrices().transformPosition(mouseX, mouseY, new Vector2f());
         int previewLeftPixel = (int)Math.floor(previewTopLeftScreen.x);
         int previewTopPixel = (int)Math.floor(previewTopLeftScreen.y);
         int previewRightPixel = (int)Math.ceil(previewBottomRightScreen.x);
         int previewBottomPixel = (int)Math.ceil(previewBottomRightScreen.y);
         int entityRenderSize = Math.max(8, (int)((previewBottomPixel - previewTopPixel) * 0.36F));
         GuiEventRenderer.flushBatchedElements();
         this.renderEntityPreview(drawContext, previewLeftPixel, previewTopPixel, previewRightPixel, previewBottomPixel, entityRenderSize, mousePositionScreen.x, mousePositionScreen.y, client.player);
      }
   }

   private void renderEntityPreview(
      DrawContext drawContext, int previewLeft, int previewTop, int previewRight, int previewBottom, int packedLight, float mouseX, float mouseY, LivingEntity livingEntity
   ) {
      float horizontalLookAngle = (float)Math.atan(((previewLeft + previewRight) / 2.0F - mouseX) / 40.0F);
      float verticalLookAngle = (float)Math.atan(((previewTop + previewBottom) / 2.0F - mouseY) / 40.0F);
      float bodyRotationBlend = this.previewRotationBlend * this.previewRotationBlend * (3.0F - 2.0F * this.previewRotationBlend);
      float headRotationBlend = 1.0F - bodyRotationBlend;
      Quaternionf modelRotation = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf headRotation = new Quaternionf().rotateX(verticalLookAngle * 20.0F * headRotationBlend * (float) (Math.PI / 180.0));
      modelRotation.mul(headRotation);
      var entityRenderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);
      EntityRenderState entityRenderState = entityRenderer.getAndUpdateRenderState(livingEntity, 1.0F);
      entityRenderState.shadowPieces.clear();
      entityRenderState.outlineColor = 0;
      if (entityRenderState instanceof LivingEntityRenderState livingRenderState) {
         livingRenderState.bodyYaw = 180.0F + horizontalLookAngle * 20.0F * headRotationBlend + bodyRotationBlend * 180.0F;
         livingRenderState.relativeHeadYaw = horizontalLookAngle * 20.0F * headRotationBlend;
         livingRenderState.pitch = livingRenderState.pose != EntityPose.GLIDING ? -verticalLookAngle * 20.0F * headRotationBlend : 0.0F;
         livingRenderState.width = livingRenderState.width / livingRenderState.baseScale;
         livingRenderState.height = livingRenderState.height / livingRenderState.baseScale;
         livingRenderState.baseScale = 1.0F;
      }

      Vector3f entityRenderOffset = new Vector3f(0.0F, entityRenderState.height / 2.0F + 0.0625F, 0.0F);
      GuiEventRenderer.flushBatchedElements();
      drawContext.addEntity(entityRenderState, packedLight, entityRenderOffset, modelRotation, headRotation, previewLeft, previewTop, previewRight, previewBottom);
   }

   private void drawToggleSwitch(DrawContext drawContext, float toggleAmount, float switchRight, float centerY, float panelAlpha) {
      float switchTop = centerY - 3.5F;
      RoundedGradientRectBuilder.at(switchRight, switchTop, 13.0F, 7.0F)
         .cornerRadius(3.5F)
         .color(
            ColorUtils.interpolateColor(
               RgbaColor.rgba(0, 0, 0, (int)(80.0F * panelAlpha)).toArgb(),
               Accent.getAccentColorWithAlpha((int)(218.0F * panelAlpha)).toArgb(),
               toggleAmount
            )
         )
         .render(drawContext);
      float knobInset = 1.1F;
      float knobSize = 7.0F - knobInset * 2.0F;
      float knobStart = switchRight + knobInset;
      float knobEnd = switchRight + 13.0F - knobInset - knobSize;
      RoundedGradientRectBuilder.at(knobStart + (knobEnd - knobStart) * toggleAmount, switchTop + knobInset, knobSize, knobSize)
         .cornerRadius(knobSize * 0.5F)
         .color(RgbaColor.rgba(255, 255, 255, (int)(255.0F * panelAlpha)))
         .render(drawContext);
   }

   private float updateHoverAnimation(float[] animationAmounts, int index, boolean isHovered) {
      float targetAmount = isHovered ? 1.0F : 0.0F;
      float currentAmount = animationAmounts[index];
      currentAmount += (targetAmount - currentAmount) * (1.0F - (float)Math.exp(-6.0F * this.frameDeltaSeconds));
      if (targetAmount == 0.0F && currentAmount < 0.001F) {
         currentAmount = 0.0F;
      } else if (targetAmount == 1.0F && currentAmount > 0.999F) {
         currentAmount = 1.0F;
      }

      animationAmounts[index] = currentAmount;
      return smoothStep(currentAmount);
   }

   private static float smoothStep(float value) {
      return value * value * (3.0F - 2.0F * value);
   }

   private static float easeOutCubic(float progress) {
      return 1.0F - (float)Math.pow(1.0F - Math.clamp(progress, 0.0F, 1.0F), 3.0);
   }

   private static boolean containsPoint(float mouseX, float mouseY, float left, float top, float width, float height) {
      return mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
   }

   public boolean mouseClicked(float mouseX, float mouseY, int button) {
      if (button != 0) {
         return false;
      }

      float categoryListTop = this.getCategoryListTop();

      for (int categoryIndex = 0; categoryIndex < cosmeticCategories.length; categoryIndex++) {
         if (containsPoint(mouseX, mouseY, this.getCategoryTabLeft(categoryIndex), categoryListTop, this.getCategoryTileWidth(), 22.0F)) {
            if (this.selectedCategoryIndex != categoryIndex) {
               this.selectedCategoryIndex = categoryIndex;
               this.modelEntryAnimationStartMs = System.currentTimeMillis();
               this.targetModelScrollOffset = 0.0F;
               this.modelScrollOffset = 0.0F;
            }

            return true;
         }
      }

      Cape cosmeticManager = Cape.getInstance();
      float categoryPanelLeft = this.getCategoryPanelLeft();
      float categoryPanelWidth = this.getCategoryPanelWidth();
      float modelListTop = this.getWingsListTopY();
      float visibleHeight = this.getVisibleModelListHeight();
      if (!(mouseX < categoryPanelLeft) && !(mouseX > categoryPanelLeft + categoryPanelWidth) && !(mouseY < modelListTop) && !(mouseY > modelListTop + visibleHeight)) {
         List<Model> models = this.getModelsInSelectedCategory();

         for (int modelIndex = 0; modelIndex < models.size(); modelIndex++) {
            float modelRowTop = modelListTop + modelIndex * 15.5F - this.modelScrollOffset;
            if (!(mouseY < modelRowTop) && !(mouseY > modelRowTop + 15.0F)) {
               Model model = models.get(modelIndex);
               this.getModelAnimationState(model)[2] = 1.0F;
               cosmeticManager.toggleEquippedModel(model);
               boolean usesWingOrBackPreview = model.getCosmeticType() == Wings.WINGS || model.getCosmeticType() == Wings.BACK;
               this.useFullBodyPreviewRotation = cosmeticManager.isEquipped(model) && usesWingOrBackPreview;
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
      if (mouseX >= this.getCategoryPanelLeft()
         && mouseX <= this.getCategoryPanelLeft() + this.getCategoryPanelWidth()
         && mouseY >= this.getWingsListTopY()
         && mouseY <= this.getWingsListTopY() + this.getVisibleModelListHeight()) {
         this.targetModelScrollOffset = Math.clamp(this.targetModelScrollOffset - (float)scrollAmount * 16.0F, 0.0F, this.maxModelScrollOffset);
         return true;
      } else {
         return false;
      }
   }
}

