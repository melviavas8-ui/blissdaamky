package dev.daamky;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

public final class Blur {
   private static final MsdfFont themeHeadingFont = Medium.loadFont("brokke_bold");
   private static final float UNUSED_FLOAT_CONSTANT_5_0_A = 5.0F;
   private static final float UNUSED_FLOAT_CONSTANT_11_5 = 11.5F;
   private static final float UNUSED_FLOAT_CONSTANT_7_5 = 7.5F;
   private static final float UNUSED_FLOAT_CONSTANT_15_0_A = 15.0F;
   private static final float UNUSED_FLOAT_CONSTANT_2_5 = 2.5F;
   private static final float UNUSED_FLOAT_CONSTANT_5_6 = 5.6F;
   private static final float UNUSED_FLOAT_CONSTANT_3_0 = 3.0F;
   private static final float UNUSED_FLOAT_CONSTANT_4_0_A = 4.0F;
   private static final float UNUSED_FLOAT_CONSTANT_1_6 = 1.6F;
   private static final float UNUSED_FLOAT_CONSTANT_4_0_B = 4.0F;
   private static final float UNUSED_FLOAT_CONSTANT_8_0 = 8.0F;
   private static final float UNUSED_FLOAT_CONSTANT_17_0 = 17.0F;
   private static final float UNUSED_FLOAT_CONSTANT_4_25 = 4.25F;
   private static final float UNUSED_FLOAT_CONSTANT_15_0_B = 15.0F;
   private static final float UNUSED_FLOAT_CONSTANT_0_5 = 0.5F;
   private static final float UNUSED_FLOAT_CONSTANT_6_0_A = 6.0F;
   private static final float UNUSED_FLOAT_CONSTANT_5_0_B = 5.0F;
   private static final float UNUSED_FLOAT_CONSTANT_5_0_C = 5.0F;
   private static final float UNUSED_FLOAT_CONSTANT_13_0 = 13.0F;
   private static final float UNUSED_FLOAT_CONSTANT_7_0 = 7.0F;
   private static final float UNUSED_FLOAT_CONSTANT_46_0 = 46.0F;
   private static final float UNUSED_FLOAT_CONSTANT_1_5 = 1.5F;
   private static final float UNUSED_FLOAT_CONSTANT_2_0 = 2.0F;
   private static final float UNUSED_FLOAT_CONSTANT_6_0_B = 6.0F;
   private static final float UNUSED_FLOAT_CONSTANT_30_0 = 30.0F;
   private static final float UNUSED_FLOAT_CONSTANT_5_4 = 5.4F;
   private static final float UNUSED_FLOAT_CONSTANT_4_8 = 4.8F;
   private static final float UNUSED_FLOAT_CONSTANT_4_6 = 4.6F;
   private final List<List<Blur.BlurOption>> optionGroups = createOptionGroups();
   private final List<Blur.BlurOption> visibleOptions = new ArrayList<>();
   private final float[] optionVerticalOffsets;
   private final int[] optionGroupIndices;
   private final float[] optionVisibilityAmounts;
   private float totalOptionListHeight;
   private final float[] themeHoverAmounts = new float[Accent.getThemePresets().size()];
   private final float[] themeSelectionAmounts = new float[Accent.getThemePresets().size()];
   private final float[] optionHoverAmounts;
   private final float[] toggleStateAmounts;
   private final float[] sliderDragSelectionAmounts;
   private final float[] optionClickPulseAmounts;
   private Blur.SliderOption draggedSliderOption;
   private float sliderDragLeft;
   private float sliderDragWidth;
   private float frameDeltaSeconds = 0.016F;
   private long lastFrameTimeMs = 0L;
   private long panelAnimationStartedAtMs = 0L;
   private float optionScrollOffset = 0.0F;
   private float targetOptionScrollOffset = 0.0F;
   private float maxOptionScrollOffset = 0.0F;
   private float themeScrollOffset = 0.0F;
   private float targetThemeScrollOffset = 0.0F;
   private float themePanelLeft;
   private float themePanelTop;
   private float themePanelWidth;
   private float themePanelHeight;
   private float optionPanelLeft;
   private float optionPanelTop;
   private float optionPanelWidth;
   private float optionPanelHeight;

   public Blur() {
      List<Integer> groupIndices = new ArrayList<>();

      for (int groupIndex = 0; groupIndex < this.optionGroups.size(); groupIndex++) {
         for (Blur.BlurOption option : this.optionGroups.get(groupIndex)) {
            this.visibleOptions.add(option);
            groupIndices.add(groupIndex);
         }
      }

      this.optionVerticalOffsets = new float[this.visibleOptions.size()];
      this.optionGroupIndices = new int[this.visibleOptions.size()];
      this.optionVisibilityAmounts = new float[this.visibleOptions.size()];

      for (int optionIndex = 0; optionIndex < this.visibleOptions.size(); optionIndex++) {
         this.optionGroupIndices[optionIndex] = groupIndices.get(optionIndex);
         this.optionVisibilityAmounts[optionIndex] = isOptionVisible(this.visibleOptions.get(optionIndex)) ? 1.0F : 0.0F;
      }

      this.rebuildOptionLayout();
      this.optionHoverAmounts = new float[this.visibleOptions.size()];
      this.toggleStateAmounts = new float[this.visibleOptions.size()];
      this.sliderDragSelectionAmounts = new float[this.visibleOptions.size()];
      this.optionClickPulseAmounts = new float[this.visibleOptions.size()];

      for (int optionIndex = 0; optionIndex < this.visibleOptions.size(); optionIndex++) {
         Blur.BlurOption option = this.visibleOptions.get(optionIndex);
         if (option instanceof Blur.ToggleOption toggleOption) {
            this.toggleStateAmounts[optionIndex] = toggleOption.enabledSupplier().getAsBoolean() ? 1.0F : 0.0F;
         }
      }
   }

   private static List<List<Blur.BlurOption>> createOptionGroups() {
      Accent accent = Accent.getInstance();
      List<List<Blur.BlurOption>> optionGroups = new ArrayList<>();
      optionGroups.add(
         List.of(
            new Blur.ToggleOption(
               "Glass",
               accent::isGlassStyle,
               () -> accent.setGuiStyle(accent.isGlassStyle() ? Accent.GuiStyle.MINIMAL : Accent.GuiStyle.GLASS)
            ),
            new Blur.SliderOption(
               "Blur",
               () -> (accent.getBlurRadius() - 1.0F) / 5.0F,
               sliderValue -> accent.setBlurRadius((float)Math.round(1.0F + sliderValue * 5.0F)),
               () -> String.format(Locale.ROOT, "%.0f", accent.getBlurRadius())
            ),
            new Blur.SliderOption("Dim", accent::getOpacity, accent::setOpacity, () -> formatPercent(accent.getOpacity()))
         )
      );
      optionGroups.add(
         List.of(
            new Blur.ToggleOption("Accent Background", accent::isAccentBackgroundEnabled, () -> accent.setAccentBackgroundEnabled(!accent.isAccentBackgroundEnabled())),
            new Blur.ToggleOption("Auto Background", accent::isAutoBackgroundEnabled, () -> accent.setAutoBackgroundEnabled(!accent.isAutoBackgroundEnabled())),
            new Blur.ToggleOption("Black Background", accent::isBlackBackgroundEnabled, () -> accent.setBlackBackgroundEnabled(!accent.isBlackBackgroundEnabled())),
            new Blur.SliderOption(
               "Tint",
               () -> accent.getTintIntensity() / 2.0F,
               sliderValue -> accent.setTintIntensity(sliderValue * 2.0F),
               () -> formatPercent(accent.getTintIntensity() / 2.0F),
               accent::isAccentBackgroundEnabled
            ),
            new Blur.SliderOption("Outline", accent::getOutlineOpacity, accent::setOutlineOpacity, () -> formatPercent(accent.getOutlineOpacity()))
         )
      );
      optionGroups.add(List.of(new Blur.ButtonOption("Random Theme", accent::randomizeAccent), new Blur.ButtonOption("Reset", accent::resetToDefaults)));
      return optionGroups;
   }

   private static boolean isOptionVisible(Blur.BlurOption option) {
      return !(option instanceof Blur.SliderOption sliderOption && !sliderOption.visibleWhen().getAsBoolean());
   }

   private void rebuildOptionLayout() {
      float verticalOffset = 0.0F;
      int previousGroupIndex = -1;

      for (int optionIndex = 0; optionIndex < this.visibleOptions.size(); optionIndex++) {
         if (previousGroupIndex != -1 && this.optionGroupIndices[optionIndex] != previousGroupIndex) {
            verticalOffset += 6.0F;
         }

         previousGroupIndex = this.optionGroupIndices[optionIndex];
         this.optionVerticalOffsets[optionIndex] = verticalOffset;
         verticalOffset += 15.5F * this.optionVisibilityAmounts[optionIndex];
      }

      this.totalOptionListHeight = Math.max(0.0F, verticalOffset - 0.5F);
   }

   private static String formatPercent(float value) {
      return Math.round(value * 100.0F) + "%";
   }

   public void updateAnimationState(float deltaSeconds) {
      this.frameDeltaSeconds = deltaSeconds;
      this.optionScrollOffset = this.optionScrollOffset
         + (this.targetOptionScrollOffset - this.optionScrollOffset) * (1.0F - (float)Math.exp(-6.5F * deltaSeconds));
      if (Math.abs(this.targetOptionScrollOffset - this.optionScrollOffset) < 0.05F) {
         this.optionScrollOffset = this.targetOptionScrollOffset;
      }

      this.targetThemeScrollOffset = Math.clamp(this.targetThemeScrollOffset, 0.0F, this.getThemeListMaxScrollOffset());
      this.themeScrollOffset = this.themeScrollOffset
         + (this.targetThemeScrollOffset - this.themeScrollOffset) * (1.0F - (float)Math.exp(-6.5F * deltaSeconds));
      float visibilityBlend = 1.0F - (float)Math.exp(-10.0F * deltaSeconds);
      float interactionBlend = 1.0F - (float)Math.exp(-9.0F * deltaSeconds);

      for (int optionIndex = 0; optionIndex < this.visibleOptions.size(); optionIndex++) {
         float targetVisibility = isOptionVisible(this.visibleOptions.get(optionIndex)) ? 1.0F : 0.0F;
         this.optionVisibilityAmounts[optionIndex] = this.optionVisibilityAmounts[optionIndex] + (targetVisibility - this.optionVisibilityAmounts[optionIndex]) * visibilityBlend;
         if (Math.abs(targetVisibility - this.optionVisibilityAmounts[optionIndex]) < 0.002F) {
            this.optionVisibilityAmounts[optionIndex] = targetVisibility;
         }

         if (this.visibleOptions.get(optionIndex) == this.draggedSliderOption && targetVisibility == 0.0F) {
            this.draggedSliderOption = null;
         }

         Blur.BlurOption option = this.visibleOptions.get(optionIndex);
         if (option instanceof Blur.ToggleOption toggleOption) {
            float targetToggleState = toggleOption.enabledSupplier().getAsBoolean() ? 1.0F : 0.0F;
            this.toggleStateAmounts[optionIndex] = this.toggleStateAmounts[optionIndex] + (targetToggleState - this.toggleStateAmounts[optionIndex]) * visibilityBlend;
         }

         this.sliderDragSelectionAmounts[optionIndex] = this.sliderDragSelectionAmounts[optionIndex]
            + ((this.visibleOptions.get(optionIndex) == this.draggedSliderOption ? 1.0F : 0.0F) - this.sliderDragSelectionAmounts[optionIndex]) * interactionBlend;
         this.optionClickPulseAmounts[optionIndex] = this.optionClickPulseAmounts[optionIndex] + (0.0F - this.optionClickPulseAmounts[optionIndex]) * (1.0F - (float)Math.exp(-11.0F * deltaSeconds));
      }

      this.rebuildOptionLayout();
   }

   public boolean hasFocusedInput() {
      return false;
   }

   public void render(
      DrawContext drawContext,
      float mouseX,
      float mouseY,
      float themePanelLeft,
      float themePanelTop,
      float themePanelWidth,
      float themePanelHeight,
      float optionPanelLeft,
      float optionPanelTop,
      float optionPanelWidth,
      float optionPanelHeight,
      float panelAlpha
   ) {
      if (!(panelAlpha <= 0.0F)) {
         long currentTimeMs = System.currentTimeMillis();
         if (currentTimeMs - this.lastFrameTimeMs > 250L) {
            this.panelAnimationStartedAtMs = currentTimeMs;
         }

         this.lastFrameTimeMs = currentTimeMs;
         this.themePanelLeft = themePanelLeft;
         this.themePanelTop = themePanelTop;
         this.themePanelWidth = themePanelWidth;
         this.themePanelHeight = themePanelHeight;
         this.optionPanelLeft = optionPanelLeft;
         this.optionPanelTop = optionPanelTop;
         this.optionPanelWidth = optionPanelWidth;
         this.optionPanelHeight = optionPanelHeight;
         if (this.draggedSliderOption != null) {
            this.draggedSliderOption.valueConsumer().accept(Math.clamp((mouseX - this.sliderDragLeft) / this.sliderDragWidth, 0.0F, 1.0F));
         }

         this.drawThemeList(drawContext, mouseX, mouseY, currentTimeMs, panelAlpha);
         this.drawOptionList(drawContext, mouseX, mouseY, currentTimeMs, panelAlpha);
      }
   }

   private float getThemeListTop() {
      return this.themePanelTop + 11.5F + 7.5F - this.themeScrollOffset;
   }

   private float getThemeListMaxScrollOffset() {
      float contentHeight = Accent.getThemePresets().size() * 17.5F - 2.5F;
      float visibleHeight = this.themePanelHeight - 11.5F - 7.5F;
      return Math.max(0.0F, contentHeight - visibleHeight);
   }

   private int getSelectedThemeIndex() {
      List<Accent.ThemePreset> themes = Accent.getThemePresets();

      for (int themeIndex = 0; themeIndex < themes.size(); themeIndex++) {
         if (Accent.getInstance().matchesTheme(themes.get(themeIndex))) {
            return themeIndex;
         }
      }

      return -1;
   }

   private void drawThemeList(DrawContext drawContext, float mouseX, float mouseY, long currentTimeMs, float panelAlpha) {
      float headingY = this.themePanelTop + 5.75F;
      drawContext.enableScissor(
         PixelRounding.II1Il11l111II11IIl(this.themePanelLeft),
         PixelRounding.II1Il11l111II11IIl(this.themePanelTop),
         PixelRounding.l1I1IlllIlI(this.themePanelLeft + this.themePanelWidth),
         PixelRounding.l1I1IlllIlI(this.themePanelTop + this.themePanelHeight)
      );

      try {
         GuiTextBuilder.createAt(Medium.ICON_FONT, "J", this.themePanelLeft + 5.0F + 2.8F, headingY, 5.6F)
            .centered()
            .color(Accent.getAccentColorWithAlpha((int)(235.0F * panelAlpha)))
            .render(drawContext);
         GuiTextBuilder.createAt(themeHeadingFont, "Themes", this.themePanelLeft + 5.0F + 5.6F + 3.0F, headingY, 5.4F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(245.0F * panelAlpha)))
            .render(drawContext);
         int selectedThemeIndex = this.getSelectedThemeIndex();
         List<Accent.ThemePreset> themes = Accent.getThemePresets();
         float listTop = this.getThemeListTop();
         float clipTop = this.themePanelTop + 11.5F + 7.5F - 2.5F;
         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(this.themePanelLeft),
            PixelRounding.II1Il11l111II11IIl(clipTop),
            PixelRounding.l1I1IlllIlI(this.themePanelLeft + this.themePanelWidth),
            PixelRounding.l1I1IlllIlI(this.themePanelTop + this.themePanelHeight)
         );

         for (int themeIndex = 0; themeIndex < themes.size(); themeIndex++) {
            Accent.ThemePreset theme = themes.get(themeIndex);
            float rowTop = listTop + themeIndex * 17.5F;
            if (!(rowTop + 15.0F < clipTop) && !(rowTop > this.themePanelTop + this.themePanelHeight)) {
               float hoverAmount = this.updateHoverAnimation(
                  this.themeHoverAmounts, themeIndex, containsPoint(mouseX, mouseY, this.themePanelLeft, rowTop, this.themePanelWidth, 15.0F)
               );
               float selectionAmount = this.updateHoverAnimation(this.themeSelectionAmounts, themeIndex, themeIndex == selectedThemeIndex);
               float entranceAmount = easeOutCubic((float)(currentTimeMs - this.panelAnimationStartedAtMs - themeIndex * 30L) / 220.0F);
               if (!(entranceAmount <= 0.002F)) {
                  float rowAlpha = panelAlpha * entranceAmount;
                  float slideOffset = (1.0F - entranceAmount) * 7.0F;
                  float labelCenterY = rowTop + 7.5F;
                  float textEmphasis = Math.max(hoverAmount, selectionAmount);
                  float backgroundEmphasis = Math.max(selectionAmount, hoverAmount * 0.5F);
                  if (backgroundEmphasis > 0.01F) {
                     RoundedGradientRectBuilder.at(this.themePanelLeft + 1.0F + slideOffset, rowTop, this.themePanelWidth - 2.0F, 15.0F)
                        .cornerRadius(3.5F)
                        .color(Accent.getTextColorWithAlpha((int)((selectionAmount > 0.01F ? 12 : 8) * backgroundEmphasis * rowAlpha)))
                        .render(drawContext);
                  }

                  GuiTextBuilder.createAt(
                        Medium.DEFAULT_FONT, theme.name(), this.themePanelLeft + 5.0F + slideOffset, labelCenterY, 5.0F
                     )
                     .baselinePosition(TextBaselinePosition.CENTER)
                     .color(
                        ColorUtils.interpolateColor(
                           Accent.getDeepTextColor((int)(210.0F * rowAlpha)).toArgb(), Accent.getTextColorWithAlpha((int)(255.0F * rowAlpha)).toArgb(), textEmphasis
                        )
                     )
                     .render(drawContext);
                  RgbaColor[] swatchColors = new RgbaColor[]{
                     theme.backgroundColor(),
                     theme.textColor(),
                     theme.accentColor()
                  };
                  float swatchLeft = this.themePanelLeft + this.themePanelWidth - 4.0F - swatchColors.length * 4.0F - (swatchColors.length - 1) * 1.6F + slideOffset;

                  for (RgbaColor swatchColor : swatchColors) {
                     RoundedGradientRectBuilder.at(swatchLeft, labelCenterY - 2.0F, 4.0F, 4.0F)
                        .cornerRadius(2.0F)
                        .color(swatchColor.withAlpha((int)(255.0F * rowAlpha)))
                        .render(drawContext);
                     RoundedRectBuilder.createAt(swatchLeft, labelCenterY - 2.0F, 4.0F, 4.0F)
                        .cornerRadius(2.0F)
                        .borderWidth(0.5F)
                        .color(Accent.getTextColorWithAlpha((int)(40.0F * rowAlpha)))
                        .render(drawContext);
                     swatchLeft += 5.6F;
                  }

                  if (selectionAmount > 0.01F) {
                     float outlineWidth = swatchColors.length * 4.0F + (swatchColors.length - 1) * 1.6F + 3.0F;
                     float outlineLeft = this.themePanelLeft + this.themePanelWidth - 4.0F - outlineWidth + 1.5F + slideOffset;
                     RoundedRectBuilder.createAt(outlineLeft, labelCenterY - 2.0F - 1.5F, outlineWidth, 7.0F)
                        .cornerRadius(3.5F)
                        .borderWidth(0.7F)
                        .color(Accent.getAccentColorWithAlpha((int)(200.0F * selectionAmount * rowAlpha)))
                        .render(drawContext);
                  }
               }
            }
         }

         drawContext.disableScissor();
      } finally {
         drawContext.disableScissor();
      }
   }

   private float getOptionContentLeft() {
      return this.optionPanelLeft + 4.0F;
   }

   private float getOptionContentWidth() {
      return this.optionPanelWidth - 4.0F - 8.0F;
   }

   private float getOptionListTop() {
      return this.optionPanelTop + 17.0F;
   }

   private float getOptionListVisibleHeight() {
      return this.optionPanelHeight - 17.0F;
   }

   private void drawOptionList(DrawContext drawContext, float mouseX, float mouseY, long currentTimeMs, float panelAlpha) {
      float panelRight = this.optionPanelLeft + this.optionPanelWidth - 6.0F;
      drawContext.enableScissor(
         PixelRounding.II1Il11l111II11IIl(this.optionPanelLeft),
         PixelRounding.II1Il11l111II11IIl(this.optionPanelTop),
         PixelRounding.l1I1IlllIlI(panelRight),
         PixelRounding.l1I1IlllIlI(this.optionPanelTop + this.optionPanelHeight)
      );

      try {
         this.drawOptionListHeader(drawContext, panelAlpha);
         float contentLeft = this.getOptionContentLeft();
         float contentWidth = this.getOptionContentWidth();
         float optionListTop = this.getOptionListTop();
         float listHeight = this.getOptionListVisibleHeight();
         if (!(listHeight <= 2.0F)) {
            this.maxOptionScrollOffset = Math.max(0.0F, this.totalOptionListHeight - listHeight);
            this.targetOptionScrollOffset = Math.clamp(this.targetOptionScrollOffset, 0.0F, this.maxOptionScrollOffset);
            this.optionScrollOffset = Math.clamp(this.optionScrollOffset, 0.0F, this.maxOptionScrollOffset);
            boolean pointerInsideList = mouseX >= contentLeft && mouseX <= contentLeft + contentWidth && mouseY >= optionListTop && mouseY <= optionListTop + listHeight;
            drawContext.enableScissor(
               PixelRounding.II1Il11l111II11IIl(contentLeft - 1.0F),
               PixelRounding.II1Il11l111II11IIl(optionListTop),
               PixelRounding.l1I1IlllIlI(contentLeft + contentWidth + 1.0F),
               PixelRounding.l1I1IlllIlI(optionListTop + listHeight)
            );

            try {
               for (int optionIndex = 0; optionIndex < this.visibleOptions.size(); optionIndex++) {
                  Blur.BlurOption option = this.visibleOptions.get(optionIndex);
                  float visibilityAmount = this.optionVisibilityAmounts[optionIndex];
                  float rowTop = optionListTop + this.optionVerticalOffsets[optionIndex] - this.optionScrollOffset;
                  boolean isHovered = visibilityAmount > 0.5F && pointerInsideList && mouseY >= rowTop && mouseY <= rowTop + 15.0F;
                  float hoverAmount = this.updateHoverAnimation(this.optionHoverAmounts, optionIndex, isHovered);
                  if (!(visibilityAmount <= 0.01F) && !(rowTop + 15.0F < optionListTop) && !(rowTop > optionListTop + listHeight)) {
                     float entranceAmount = easeOutCubic((float)(currentTimeMs - this.panelAnimationStartedAtMs - optionIndex * 30L) / 280.0F);
                     if (!(entranceAmount <= 0.0F)) {
                        float rowAlpha = panelAlpha * entranceAmount * visibilityAmount;
                        drawContext.getMatrices().pushMatrix();
                        drawContext.getMatrices().translate((1.0F - entranceAmount) * 10.0F, 0.0F);

                        try {
                           float clickPulse = this.optionClickPulseAmounts[optionIndex];
                           float highlightAmount = 7.0F * hoverAmount + 10.0F * clickPulse;
                           if (highlightAmount > 0.1F) {
                              float highlightExpansion = clickPulse * 1.2F;
                              RoundedGradientRectBuilder.at(contentLeft - highlightExpansion, rowTop - highlightExpansion * 0.5F, contentWidth + highlightExpansion * 2.0F, 15.0F + highlightExpansion)
                                 .cornerRadius(3.5F)
                                 .color(Accent.getTextColorWithAlpha((int)(highlightAmount * rowAlpha)))
                                 .render(drawContext);
                           }

                           float labelCenterY = rowTop + 7.5F;
                           this.drawOptionControlPreview(drawContext, option, optionIndex, contentLeft + 4.0F, labelCenterY, hoverAmount, rowAlpha);
                           GuiTextBuilder.createAt(
                                 Medium.DEFAULT_FONT, option.label(), contentLeft + 4.0F + 5.0F + 5.0F, labelCenterY, 4.8F
                              )
                              .baselinePosition(TextBaselinePosition.CENTER)
                              .color(
                                 ColorUtils.interpolateColor(
                                    Accent.getDarkerTextColor((int)(235.0F * rowAlpha)).toArgb(),
                                    Accent.getTextColorWithAlpha((int)(255.0F * rowAlpha)).toArgb(),
                                    hoverAmount
                                 )
                              )
                              .render(drawContext);
                           float controlRight = contentLeft + contentWidth - 4.0F;
                           if (option instanceof Blur.ToggleOption) {
                              this.drawToggleSwitch(drawContext, this.toggleStateAmounts[optionIndex], controlRight, labelCenterY, rowAlpha);
                           } else if (option instanceof Blur.SliderOption sliderOption) {
                              this.drawSliderOption(drawContext, sliderOption, this.sliderDragSelectionAmounts[optionIndex], controlRight, labelCenterY, hoverAmount, rowAlpha);
                           }
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

   private void drawOptionListHeader(DrawContext drawContext, float panelAlpha) {
      float headingY = this.optionPanelTop + 4.25F;
      float titleLeft = this.optionPanelLeft + 4.0F + 4.0F;
      GuiTextBuilder.createAt(Medium.ICON_FONT, "J", titleLeft + 2.5F, headingY, 5.6F)
         .centered()
         .color(Accent.getAccentColorWithAlpha((int)(240.0F * panelAlpha)))
         .render(drawContext);
      int selectedThemeIndex = this.getSelectedThemeIndex();
      String themeName = selectedThemeIndex >= 0 ? Accent.getThemePresets().get(selectedThemeIndex).name() : "Custom Theme";
      GuiTextBuilder.createAt(themeHeadingFont, themeName, titleLeft + 5.0F + 5.0F, headingY, 5.4F)
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(Accent.getTextColorWithAlpha((int)(250.0F * panelAlpha)))
         .render(drawContext);
   }

   private void drawOptionControlPreview(DrawContext drawContext, Blur.BlurOption option, int optionIndex, float left, float centerY, float hoverAmount, float panelAlpha) {
      RgbaColor l1I1IlllIlI4 = Accent.getTextColorWithAlpha((int)((150.0F + 80.0F * hoverAmount) * panelAlpha));
      float controlTop = centerY - 2.5F;
      if (option instanceof Blur.ToggleOption) {
         float toggleAmount = this.toggleStateAmounts[optionIndex];
         RoundedRectBuilder.createAt(left, controlTop, 5.0F, 5.0F)
            .cornerRadius(1.6F)
            .borderWidth(0.7F)
            .color(
               ColorUtils.interpolateColor(l1I1IlllIlI4.toArgb(), Accent.getAccentColorWithAlpha((int)(255.0F * panelAlpha)).toArgb(), toggleAmount)
            )
            .render(drawContext);
         if (toggleAmount > 0.01F) {
            float checkSize = 3.0F * toggleAmount;
            RoundedGradientRectBuilder.at(left + (5.0F - checkSize) * 0.5F, centerY - checkSize * 0.5F, checkSize, checkSize)
               .cornerRadius(Math.max(0.3F, checkSize * 0.3F))
               .color(Accent.getAccentColorWithAlpha((int)(255.0F * toggleAmount * panelAlpha)))
               .render(drawContext);
         }
      } else if (option instanceof Blur.SliderOption) {
         float dragSelectionAmount = this.sliderDragSelectionAmounts[optionIndex];

         for (int tickIndex = 0; tickIndex < 3; tickIndex++) {
            float tickWidth = tickIndex == 1 ? 5.0F : 3.25F;
            RoundedGradientRectBuilder.at(left, controlTop + 0.5F + tickIndex * 1.75F, tickWidth, 0.7F)
               .cornerRadius(0.35F)
               .color(
                  ColorUtils.interpolateColor(l1I1IlllIlI4.toArgb(), Accent.getAccentColorWithAlpha((int)(255.0F * panelAlpha)).toArgb(), dragSelectionAmount)
               )
               .render(drawContext);
         }
      } else {
         float clickPulse = this.optionClickPulseAmounts[optionIndex];
         RoundedRectBuilder.createAt(left, controlTop, 5.0F, 5.0F)
            .cornerRadius(2.5F)
            .borderWidth(0.7F)
            .color(
               ColorUtils.interpolateColor(l1I1IlllIlI4.toArgb(), Accent.getAccentColorWithAlpha((int)(255.0F * panelAlpha)).toArgb(), clickPulse)
            )
            .render(drawContext);
         float pulseSize = 1.6F + clickPulse;
         RoundedGradientRectBuilder.at(left + (5.0F - pulseSize) * 0.5F, centerY - pulseSize * 0.5F, pulseSize, pulseSize)
            .cornerRadius(pulseSize * 0.5F)
            .color(
               ColorUtils.interpolateColor(l1I1IlllIlI4.toArgb(), Accent.getAccentColorWithAlpha((int)(255.0F * panelAlpha)).toArgb(), clickPulse)
            )
            .render(drawContext);
      }
   }

   private void drawToggleSwitch(DrawContext drawContext, float toggleAmount, float labelRight, float centerY, float panelAlpha) {
      float offLabelWidth = Medium.DEFAULT_FONT.measureWidth("OFF", 4.6F);
      if (toggleAmount > 0.01F) {
         GuiTextBuilder.createAt(
               Medium.DEFAULT_FONT, "ON", labelRight - Medium.DEFAULT_FONT.measureWidth("ON", 4.6F), centerY + (1.0F - toggleAmount) * 3.0F, 4.6F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(250.0F * toggleAmount * panelAlpha)))
            .render(drawContext);
      }

      if (toggleAmount < 0.99F) {
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "OFF", labelRight - offLabelWidth, centerY - toggleAmount * 3.0F, 4.6F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(210.0F * (1.0F - toggleAmount) * panelAlpha)))
            .render(drawContext);
      }

      float switchLeft = labelRight - offLabelWidth - 4.0F - 13.0F;
      float switchTop = centerY - 3.5F;
      RoundedGradientRectBuilder.at(switchLeft, switchTop, 13.0F, 7.0F)
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
      float knobLeft = switchLeft + knobInset;
      float knobRight = switchLeft + 13.0F - knobInset - knobSize;
      RoundedGradientRectBuilder.at(knobLeft + (knobRight - knobLeft) * toggleAmount, switchTop + knobInset, knobSize, knobSize)
         .cornerRadius(knobSize * 0.5F)
         .color(RgbaColor.rgba(255, 255, 255, (int)(255.0F * panelAlpha)))
         .render(drawContext);
   }

   private float getSliderTrackWidth() {
      return Math.min(46.0F, this.getOptionContentWidth() * 0.32F);
   }

   private float getSliderTrackLeft() {
      return this.getOptionContentLeft() + this.getOptionContentWidth() - 4.0F - 30.0F - 4.0F - this.getSliderTrackWidth();
   }

   private void drawSliderOption(DrawContext drawContext, Blur.SliderOption sliderOption, float dragSelectionAmount, float labelRight, float centerY, float hoverAmount, float panelAlpha) {
      String formattedValue = sliderOption.formattedValueSupplier().get();
      GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT, formattedValue, labelRight - Medium.DEFAULT_FONT.measureWidth(formattedValue, 4.6F), centerY, 4.6F
         )
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(
            ColorUtils.interpolateColor(
               Accent.getTextColorWithAlpha((int)(245.0F * panelAlpha)).toArgb(), Accent.getLighterAccentColor((int)(255.0F * panelAlpha)).toArgb(), dragSelectionAmount
            )
         )
         .render(drawContext);
      float trackWidth = this.getSliderTrackWidth();
      float trackLeft = this.getSliderTrackLeft();
      float trackHeight = 1.5F + dragSelectionAmount * 0.6F;
      float trackTop = centerY - trackHeight * 0.5F;
      RoundedGradientRectBuilder.at(trackLeft, trackTop, trackWidth, trackHeight)
         .cornerRadius(trackHeight * 0.5F)
         .color(Accent.getTextColorWithAlpha((int)((30.0F + 12.0F * hoverAmount) * panelAlpha)))
         .render(drawContext);
      float normalizedValue = Math.clamp(sliderOption.valueSupplier().get(), 0.0F, 1.0F);
      float thumbX = trackLeft + trackWidth * normalizedValue;
      RoundedGradientRectBuilder.at(trackLeft, trackTop, Math.max(trackHeight, thumbX - trackLeft), trackHeight)
         .cornerRadius(trackHeight * 0.5F)
         .horizontalGradient(Accent.getDarkerAccentColor((int)(230.0F * panelAlpha)), Accent.getAccentColorWithAlpha((int)(240.0F * panelAlpha)))
         .render(drawContext);
      float thumbEmphasis = Math.max(hoverAmount * 0.5F, dragSelectionAmount);
      if (thumbEmphasis > 0.01F) {
         float glowRadius = 3.0F + 1.5F * dragSelectionAmount;
         RoundedGradientRectBuilder.at(thumbX - glowRadius, centerY - glowRadius, glowRadius * 2.0F, glowRadius * 2.0F)
            .cornerRadius(glowRadius)
            .color(Accent.getAccentColorWithAlpha((int)(55.0F * thumbEmphasis * panelAlpha)))
            .render(drawContext);
      }

      float thumbWidth = 2.0F + dragSelectionAmount;
      float thumbHeight = 6.0F + hoverAmount * 0.6F + dragSelectionAmount;
      RoundedGradientRectBuilder.at(thumbX - thumbWidth * 0.5F, centerY - thumbHeight * 0.5F, thumbWidth, thumbHeight)
         .cornerRadius(thumbWidth * 0.5F)
         .color(
            ColorUtils.interpolateColor(
               Accent.getLighterAccentColor((int)(255.0F * panelAlpha)).toArgb(),
               RgbaColor.rgba(255, 255, 255, (int)(255.0F * panelAlpha)).toArgb(),
               dragSelectionAmount
            )
         )
         .render(drawContext);
   }

   private float updateHoverAnimation(float[] hoverAmounts, int index, boolean isHovered) {
      float targetAmount = isHovered ? 1.0F : 0.0F;
      float currentAmount = hoverAmounts[index];
      currentAmount += (targetAmount - currentAmount) * (1.0F - (float)Math.exp(-6.0F * this.frameDeltaSeconds));
      if (targetAmount == 0.0F && currentAmount < 0.001F) {
         currentAmount = 0.0F;
      } else if (targetAmount == 1.0F && currentAmount > 0.999F) {
         currentAmount = 1.0F;
      }

      hoverAmounts[index] = currentAmount;
      return currentAmount * currentAmount * (3.0F - 2.0F * currentAmount);
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

      List<Accent.ThemePreset> themes = Accent.getThemePresets();
      float themeListTop = this.getThemeListTop();

      for (int themeIndex = 0; themeIndex < themes.size(); themeIndex++) {
         float themeRowTop = themeListTop + themeIndex * 17.5F;
         if (!(themeRowTop + 7.5F < this.themePanelTop + 11.5F + 7.5F)
            && !(themeRowTop + 7.5F > this.themePanelTop + this.themePanelHeight)
            && containsPoint(mouseX, mouseY, this.themePanelLeft, themeRowTop, this.themePanelWidth, 15.0F)) {
            Accent.getInstance().applyTheme(themes.get(themeIndex));
            return true;
         }
      }

      float optionContentLeft = this.getOptionContentLeft();
      float optionContentWidth = this.getOptionContentWidth();
      float optionListTop = this.getOptionListTop();
      float visibleHeight = this.getOptionListVisibleHeight();
      if (!(mouseX < optionContentLeft) && !(mouseX > optionContentLeft + optionContentWidth) && !(mouseY < optionListTop) && !(mouseY > optionListTop + visibleHeight)) {
         for (int optionIndex = 0; optionIndex < this.visibleOptions.size(); optionIndex++) {
            if (!(this.optionVisibilityAmounts[optionIndex] < 0.5F)) {
               float optionRowTop = optionListTop + this.optionVerticalOffsets[optionIndex] - this.optionScrollOffset;
               if (!(mouseY < optionRowTop) && !(mouseY > optionRowTop + 15.0F)) {
                  Blur.BlurOption option = this.visibleOptions.get(optionIndex);
                  this.optionClickPulseAmounts[optionIndex] = 1.0F;
                  if (option instanceof Blur.ToggleOption toggleOption) {
                     toggleOption.toggleAction().run();
                  } else if (option instanceof Blur.ButtonOption buttonOption) {
                     buttonOption.action().run();
                  } else if (option instanceof Blur.SliderOption sliderOption) {
                     float trackLeft = this.getSliderTrackLeft();
                     float trackWidth = this.getSliderTrackWidth();
                     if (mouseX >= trackLeft - 3.0F && mouseX <= trackLeft + trackWidth + 3.0F) {
                        this.draggedSliderOption = sliderOption;
                        this.sliderDragLeft = trackLeft;
                        this.sliderDragWidth = trackWidth;
                        sliderOption.valueConsumer().accept(Math.clamp((mouseX - trackLeft) / trackWidth, 0.0F, 1.0F));
                     }
                  }

                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean keyPressed(KeyInput keyInput) {
      return false;
   }

   public boolean charTyped(CharInput charInput) {
      return false;
   }

   public void mouseDragged(float mouseX, float mouseY) {
      if (this.draggedSliderOption != null) {
         this.draggedSliderOption.valueConsumer().accept(Math.clamp((mouseX - this.sliderDragLeft) / this.sliderDragWidth, 0.0F, 1.0F));
      }
   }

   public void mouseReleased(int button) {
      if (button == 0) {
         this.draggedSliderOption = null;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
      if (mouseX >= this.themePanelLeft
         && mouseX <= this.themePanelLeft + this.themePanelWidth
         && mouseY >= this.themePanelTop
         && mouseY <= this.themePanelTop + this.themePanelHeight) {
         this.targetThemeScrollOffset = Math.clamp(this.targetThemeScrollOffset - (float)scrollAmount * 16.0F, 0.0F, this.getThemeListMaxScrollOffset());
         return true;
      } else if (mouseX >= this.optionPanelLeft
         && mouseX <= this.optionPanelLeft + this.optionPanelWidth
         && mouseY >= this.optionPanelTop
         && mouseY <= this.optionPanelTop + this.optionPanelHeight) {
         this.targetOptionScrollOffset = Math.clamp(this.targetOptionScrollOffset - (float)scrollAmount * 16.0F, 0.0F, this.maxOptionScrollOffset);
         return true;
      } else {
         return false;
      }
   }

   private record ButtonOption(String label, Runnable action) implements Blur.BlurOption {

      private ButtonOption(String label, Runnable action) {
         this.label = label;
         this.action = action;
      }
   }

   private record SliderOption(String label, Supplier<Float> valueSupplier, Consumer<Float> valueConsumer, Supplier<String> formattedValueSupplier, BooleanSupplier visibleWhen) implements Blur.BlurOption {

      SliderOption(String label, Supplier<Float> supplier, Consumer<Float> consumer, Supplier<String> supplier2) {
         this(label, supplier, consumer, supplier2, () -> true);
      }

      private SliderOption(String label, Supplier<Float> valueSupplier, Consumer<Float> valueConsumer, Supplier<String> formattedValueSupplier, BooleanSupplier visibleWhen) {
         this.label = label;
         this.valueSupplier = valueSupplier;
         this.valueConsumer = valueConsumer;
         this.formattedValueSupplier = formattedValueSupplier;
         this.visibleWhen = visibleWhen;
      }
   }

   private record ToggleOption(String label, BooleanSupplier enabledSupplier, Runnable toggleAction) implements Blur.BlurOption {

      private ToggleOption(String label, BooleanSupplier enabledSupplier, Runnable toggleAction) {
         this.label = label;
         this.enabledSupplier = enabledSupplier;
         this.toggleAction = toggleAction;
      }
   }

   private sealed interface BlurOption permits Blur.ToggleOption, Blur.SliderOption, Blur.ButtonOption {
      String label();
   }
}

