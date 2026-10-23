package dev.daamky;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;

public final class ModuleSettingsPanel {
    private static final String TELEGRAM_CHANNEL_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float UNUSED_FLOAT_CONSTANT_7_0_A = 7.0F;
   private static final float UNUSED_FLOAT_CONSTANT_5_0_A = 5.0F;
   private static final float UNUSED_FLOAT_CONSTANT_5_0_B = 5.0F;
   private static final float UNUSED_FLOAT_CONSTANT_6_5_A = 6.5F;
   private static final float UNUSED_FLOAT_CONSTANT_5_0_E = 5.0F;
   private static final float UNUSED_FLOAT_CONSTANT_5_6_A = 5.6F;
   private static final float UNUSED_FLOAT_CONSTANT_4_3 = 4.3F;
   private static final float UNUSED_FLOAT_CONSTANT_5_6_B = 5.6F;
   private static final int UNUSED_INT_CONSTANT_2 = 2;
   private static final float UNUSED_FLOAT_CONSTANT_4_4 = 4.4F;
   private static final float UNUSED_FLOAT_CONSTANT_3_8 = 3.8F;
   private static final float UNUSED_FLOAT_CONSTANT_13_0 = 13.0F;
   private static final float UNUSED_FLOAT_CONSTANT_7_0_B = 7.0F;
   private static final MsdfFont MODULE_TITLE_FONT = Medium.loadFont("brokke_bold");
   private static final float UNUSED_FLOAT_CONSTANT_5_0_C = 5.0F;
   private static final float UNUSED_FLOAT_CONSTANT_5_0_D = 5.0F;
   private static final float UNUSED_FLOAT_CONSTANT_8_5 = 8.5F;
   private static final float UNUSED_FLOAT_CONSTANT_6_5_B = 6.5F;
   private static final float UNUSED_FLOAT_CONSTANT_7_0_C = 7.0F;
   private static final float UNUSED_FLOAT_CONSTANT_1_5 = 1.5F;
   private static final float UNUSED_FLOAT_CONSTANT_4_0 = 4.0F;
   private static final float UNUSED_FLOAT_CONSTANT_9_0 = 9.0F;
   private static final float UNUSED_FLOAT_CONSTANT_9_5 = 9.5F;
   private static final float UNUSED_FLOAT_CONSTANT_2_0 = 2.0F;
   private static final int[] OPTION_COLOR_GRADIENT_ARGB = new int[]{-65536, -256, -16711936, -16711681, -16776961, -65281, -65536};
   private final Map<ModuleStructure, float[]> moduleAnimationStates = new HashMap<>();
   private final List<ModuleSettingsPanel.ModuleRowLayout> moduleRows = new ArrayList<>();
   private ModuleCategory activeCategory;
   private String searchText = "";
   private float animationTime;
   private float frameDeltaSeconds = 0.016F;
   private float scrollOffset;
   private float targetScrollOffset;
   private float maxScrollOffset;
   private float contentLeft;
   private float contentTop;
   private float contentWidth;
   private float contentHeight;
   private ModuleStructure selectedModule;
   private IntSetting keybindCaptureSetting;
   private FloatSetting activeFloatSetting;
   private ColorSetting activeColorSetting;
   private float settingControlStartX;
   private float settingControlWidth;
   private Setting<?> activeOptionSetting;
   private float optionPopupAnimationProgress;
   private float optionPopupLeft;
   private float optionPopupTop;
   private float optionPopupWidth;
   private boolean optionPopupOpensUpward;
   private Setting<?> optionPopupSetting;
   private float selectedOptionIndicatorTop;
   private float optionHoverProgress;
// https://t.me/pasterplace

   public void updateAnimation(float deltaSeconds) {
      this.frameDeltaSeconds = deltaSeconds;
      this.animationTime += deltaSeconds;
      this.scrollOffset = this.scrollOffset + (this.targetScrollOffset - this.scrollOffset) * (1.0F - (float)Math.exp(-6.5F * deltaSeconds));
      if (Math.abs(this.targetScrollOffset - this.scrollOffset) < 0.05F) {
         this.scrollOffset = this.targetScrollOffset;
      }

      float targetOptionPopupProgress = this.activeOptionSetting != null ? 1.0F : 0.0F;
      this.optionPopupAnimationProgress = this.optionPopupAnimationProgress + (targetOptionPopupProgress - this.optionPopupAnimationProgress) * (1.0F - (float)Math.exp(-(targetOptionPopupProgress > this.optionPopupAnimationProgress ? 13.0F : 18.0F) * deltaSeconds));

      for (Map.Entry<ModuleStructure, float[]> animationEntry : this.moduleAnimationStates.entrySet()) {
         float[] value = animationEntry.getValue();
         float targetExpansionProgress = value[2] > 0.5F ? 1.0F : 0.0F;
         value[0] += (targetExpansionProgress - value[0]) * (1.0F - (float)Math.exp(-9.0F * deltaSeconds));
         if (Math.abs(targetExpansionProgress - value[0]) < 0.001F) {
            value[0] = targetExpansionProgress;
         }
      }
   }
// https://t.me/pasterplace

   private float[] getModuleAnimationState(ModuleStructure moduleStructure) {
      return this.moduleAnimationStates.computeIfAbsent(moduleStructure, moduleStructurex -> new float[]{1.0F, 0.0F, 1.0F});
   }
// https://t.me/pasterplace

   public boolean isModuleExpanded(ModuleStructure moduleStructure) {
      float[] animationState = this.moduleAnimationStates.get(moduleStructure);
      return animationState != null && animationState[2] > 0.5F;
   }
// https://t.me/pasterplace

   public boolean hasSelectedModule() {
      return this.selectedModule != null;
   }
// https://t.me/pasterplace

   public void clearSelectedModule() {
      this.selectedModule = null;
   }
// https://t.me/pasterplace

   public void setSelectedModuleKeybind(int keybind) {
      if (this.selectedModule != null) {
         this.selectedModule.setKeybind(keybind);
         this.selectedModule = null;
      }
   }
// https://t.me/pasterplace

   public boolean isKeybindCaptureActive() {
      return this.keybindCaptureSetting != null;
   }
// https://t.me/pasterplace

   public void setKeybindCapture(int keyCode) {
      if (this.keybindCaptureSetting != null) {
         boolean clearKeybind = keyCode == 256 || keyCode == 261 || keyCode == 259;
         this.keybindCaptureSetting.setValue(Integer.valueOf(clearKeybind ? 0 : keyCode));
         this.keybindCaptureSetting = null;
      }
   }
// https://t.me/pasterplace

   public boolean cancelOptionSelection() {
      if (this.activeOptionSetting == null) {
         return false;
      }

      this.activeOptionSetting = null;
      return true;
   }
// https://t.me/pasterplace

   public boolean onMouseClicked(float mouseX, float mouseY, int mouseButton) {
      return this.activeOptionSetting == null ? false : this.handleMouseClick(mouseX, mouseY, mouseButton);
   }
// https://t.me/pasterplace

   public void clearSelection() {
      this.selectedModule = null;
      this.keybindCaptureSetting = null;
      this.activeOptionSetting = null;
      this.activeFloatSetting = null;
      this.activeColorSetting = null;
   }
// https://t.me/pasterplace

   private static List<ModuleStructure> findModules(ModuleCategory moduleCategory, String searchText) {
      ArrayList<ModuleStructure> matchingModules = new ArrayList<>();
      boolean hasSearchQuery = searchText != null && !searchText.isBlank();
      String normalizedSearchQuery = hasSearchQuery ? searchText.trim().toLowerCase(Locale.ROOT) : "";

      for (ModuleStructure moduleStructure : ModuleRegistry.getInstance().getModules()) {
         if (hasSearchQuery
            ? moduleStructure.getName().toLowerCase(Locale.ROOT).contains(normalizedSearchQuery)
            : moduleStructure.getCategory() == moduleCategory) {
            matchingModules.add(moduleStructure);
         }
      }

      matchingModules.sort(Comparator.comparing(module -> module.getName(), String.CASE_INSENSITIVE_ORDER));
      return matchingModules;
   }
// https://t.me/pasterplace

   private static List<String> wrapText(String textToWrap, float fontSize, float maxLineWidth) {
      ArrayList<String> wrappedLines = new ArrayList<>();
      if (textToWrap != null && !textToWrap.isBlank()) {
         StringBuilder currentLine = new StringBuilder();

         for (String word : textToWrap.trim().split("\\s+")) {
            String candidateLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (!(Medium.DEFAULT_FONT.measureWidth(candidateLine, fontSize) <= maxLineWidth) && !currentLine.isEmpty()) {
               wrappedLines.add(currentLine.toString());
               currentLine.setLength(0);
               currentLine.append(word);
            } else {
               currentLine.setLength(0);
               currentLine.append(candidateLine);
            }
         }

         if (!currentLine.isEmpty()) {
            wrappedLines.add(currentLine.toString());
         }

         if (wrappedLines.size() > 2) {
            ArrayList<String> limitedLines = new ArrayList<>(wrappedLines.subList(0, 2));
            limitedLines.set(1, Medium.DEFAULT_FONT.truncateToWidth(limitedLines.get(1) + " " + wrappedLines.get(2), fontSize, maxLineWidth));
            return limitedLines;
         } else {
            return wrappedLines;
         }
      } else {
         return wrappedLines;
      }
   }
// https://t.me/pasterplace

   private static List<Setting<?>> getVisibleSettings(ModuleStructure moduleStructure) {
      ArrayList<Setting<?>> visibleSettings = new ArrayList<>();

      for (Object setting : moduleStructure.getSettings()) {
         if (((Setting<?>)setting).isVisible()
            && (
               setting instanceof BooleanSetting
                  || setting instanceof FloatSetting
                  || setting instanceof MultiOptionSetting
                  || setting instanceof MultiToggleSetting
                  || setting instanceof IntSetting
                  || setting instanceof ColorSetting
            )) {
            visibleSettings.add((Setting<?>)setting);
         }
      }

      return visibleSettings;
   }
// https://t.me/pasterplace

   private static float getSettingLabelHeight(Setting<?> setting) {
      return setting.getDescription().isEmpty() ? 6.5F : 12.1F;
   }
// https://t.me/pasterplace

   private static float getSettingRowHeight(Setting<?> setting) {
      float labelHeight = getSettingLabelHeight(setting);
      if (setting instanceof FloatSetting) {
         return labelHeight + 7.5F;
      } else {
         return setting instanceof ColorSetting ? labelHeight + 7.0F : Math.max(labelHeight, 9.0F);
      }
   }
// https://t.me/pasterplace

   private static float getExpandedSettingsHeight(ModuleStructure moduleStructure) {
      float settingsHeight = 0.0F;
      List items = getVisibleSettings(moduleStructure);

      for (int settingIndex = 0; settingIndex < items.size(); settingIndex++) {
         settingsHeight += getSettingRowHeight((Setting<?>)items.get(settingIndex)) + (settingIndex > 0 ? 7.0F : 0.0F);
      }
// https://t.me/pasterplace

      return settingsHeight;
   }
// https://t.me/pasterplace

   private void rebuildModuleRows(float contentLeft, float contentTop, float contentWidth, ModuleCategory moduleCategory, String searchText) {
      this.moduleRows.clear();
      float columnWidth = (contentWidth - 14.0F - 5.0F) * 0.5F;
      float[] columnHeights = new float[]{0.0F, 0.0F};

      for (ModuleStructure moduleStructure : findModules(moduleCategory, searchText)) {
         ModuleSettingsPanel.ModuleRowLayout rowLayout = new ModuleSettingsPanel.ModuleRowLayout(moduleStructure);
         rowLayout.width = columnWidth;
         float descriptionWidth = columnWidth - 13.0F - 13.0F - 4.0F;
         rowLayout.descriptionLines = wrapText(moduleStructure.getDescription(), 4.3F, descriptionWidth);
         rowLayout.headerHeight = 18.6F + (rowLayout.descriptionLines.isEmpty() ? 0.0F : 2.5F + rowLayout.descriptionLines.size() * 5.6F - 1.0F);
         rowLayout.settingsHeight = getExpandedSettingsHeight(moduleStructure);
         int columnIndex = columnHeights[0] <= columnHeights[1] ? 0 : 1;
         rowLayout.left = contentLeft + 7.0F + columnIndex * (columnWidth + 5.0F);
         rowLayout.top = columnHeights[columnIndex];
         columnHeights[columnIndex] += rowLayout.getRenderedHeight(easeOutQuart(this.getModuleAnimationState(moduleStructure)[0])) + 5.0F;
         this.moduleRows.add(rowLayout);
      }

      float totalRowsHeight = Math.max(columnHeights[0], columnHeights[1]) + 7.0F;
      this.maxScrollOffset = Math.max(0.0F, totalRowsHeight - this.contentHeight + 7.0F);
      this.targetScrollOffset = Math.clamp(this.targetScrollOffset, 0.0F, this.maxScrollOffset);
      this.scrollOffset = Math.clamp(this.scrollOffset, 0.0F, this.maxScrollOffset);
   }
// https://t.me/pasterplace

   private static float easeOutQuart(float progress) {
      progress = Math.clamp(progress, 0.0F, 1.0F);
      return 1.0F - (float)Math.pow(1.0F - progress, 4.0);
   }
// https://t.me/pasterplace

   private float interpolateValue(float currentValue, float targetValue, float speed) {
      return currentValue + (targetValue - currentValue) * (1.0F - (float)Math.exp(-speed * this.frameDeltaSeconds));
   }
// https://t.me/pasterplace

   public void renderModuleList(
      DrawContext drawContext,
      float mouseX,
      float mouseY,
      float contentLeft,
      float contentTop,
      float contentWidth,
      float contentHeight,
      ModuleCategory moduleCategory,
      String searchQuery,
      float opacity
   ) {
      if (!(opacity <= 0.0F)) {
         String normalizedSearchQuery = searchQuery == null ? "" : searchQuery;
         if (moduleCategory != this.activeCategory || !normalizedSearchQuery.equals(this.searchText)) {
            this.activeCategory = moduleCategory;
            this.searchText = normalizedSearchQuery;
            this.animationTime = 0.0F;
            this.scrollOffset = 0.0F;
            this.targetScrollOffset = 0.0F;
            this.activeOptionSetting = null;
            this.optionPopupAnimationProgress = 0.0F;
            this.selectedModule = null;
         }
// https://t.me/pasterplace

         this.contentLeft = contentLeft;
         this.contentTop = contentTop;
         this.contentWidth = contentWidth;
         this.contentHeight = contentHeight;
         this.rebuildModuleRows(contentLeft, contentTop, contentWidth, moduleCategory, normalizedSearchQuery);
         if (this.activeFloatSetting != null) {
            updateFloatSettingFromMouse(this.activeFloatSetting, (mouseX - this.settingControlStartX) / this.settingControlWidth);
         }

         if (this.activeColorSetting != null) {
            this.updateColorSettingFromMouse(this.activeColorSetting, mouseX);
         }

         boolean mouseWithinContent = mouseX >= contentLeft && mouseX <= contentLeft + contentWidth && mouseY >= contentTop && mouseY <= contentTop + contentHeight;
         boolean optionPopupOpen = this.activeOptionSetting != null;
         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(contentLeft),
            PixelRounding.II1Il11l111II11IIl(contentTop),
            PixelRounding.l1I1IlllIlI(contentLeft + contentWidth),
            PixelRounding.l1I1IlllIlI(contentTop + contentHeight)
         );
// https://t.me/pasterplace

         try {
            if (this.moduleRows.isEmpty()) {
               GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Здесь пока ничего нет", contentLeft + contentWidth * 0.5F, contentTop + 30.0F, 5.0F)
                  .centered()
                  .color(Accent.getDeepTextColor((int)(170.0F * opacity)))
                  .render(drawContext);
            }

            for (int rowIndex = 0; rowIndex < this.moduleRows.size(); rowIndex++) {
               Object rowLayout = this.moduleRows.get(rowIndex);
               float[] moduleAnimationState = this.getModuleAnimationState(((ModuleSettingsPanel.ModuleRowLayout)rowLayout).module);
               float expansionProgress = easeOutQuart(moduleAnimationState[0]);
               float rowTop = contentTop + 7.0F + ((ModuleSettingsPanel.ModuleRowLayout)rowLayout).top - this.scrollOffset;
               float rowHeight = ((ModuleSettingsPanel.ModuleRowLayout)rowLayout).headerHeight
                  + (((ModuleSettingsPanel.ModuleRowLayout)rowLayout).settingsHeight > 0.0F ? (((ModuleSettingsPanel.ModuleRowLayout)rowLayout).settingsHeight + 3.25F) * expansionProgress : 0.0F);
               if (!(rowTop + rowHeight < contentTop) && !(rowTop > contentTop + contentHeight)) {
                  float rowEntranceProgress = easeOutQuart((this.animationTime - rowIndex * 0.035F) / 0.45F);
                  if (!(rowEntranceProgress <= 0.003F)) {
                     float rowOpacity = opacity * rowEntranceProgress;
                     rowTop += (1.0F - rowEntranceProgress) * 6.0F;
                     boolean rowHovered = mouseWithinContent
                        && !optionPopupOpen
                        && mouseX >= ((ModuleSettingsPanel.ModuleRowLayout)rowLayout).left
                        && mouseX <= ((ModuleSettingsPanel.ModuleRowLayout)rowLayout).left + ((ModuleSettingsPanel.ModuleRowLayout)rowLayout).width
                        && mouseY >= rowTop
                        && mouseY <= rowTop + rowHeight;
                     moduleAnimationState[1] = this.interpolateValue(moduleAnimationState[1], rowHovered ? 1.0F : 0.0F, 9.0F);
                     this.renderModuleRow(drawContext, (ModuleSettingsPanel.ModuleRowLayout)rowLayout, rowTop, rowHeight, expansionProgress, moduleAnimationState[1], rowOpacity, mouseX, mouseY, mouseWithinContent && !optionPopupOpen);
                  }
               }
            }
         } finally {
            drawContext.disableScissor();
         }

         if (this.optionPopupAnimationProgress > 0.01F) {
            GuiEventRenderer.flushAndStartRootLayer(drawContext);
            this.renderOptionPopup(drawContext, mouseX, mouseY, opacity);
         }
      }
   }
// https://t.me/pasterplace

   private void renderModuleRow(
      DrawContext drawContext,
      ModuleSettingsPanel.ModuleRowLayout rowLayout,
      float rowTop,
      float rowHeight,
      float expansionProgress,
      float hoverProgress,
      float opacity,
      float mouseX,
      float mouseY,
      boolean pointerEventsEnabled
   ) {
      ModuleStructure moduleStructure = rowLayout.module;
      float moduleAnimationProgress = moduleStructure.getAnimationProgress();
      RoundedGradientRectBuilder.at(rowLayout.left, rowTop, rowLayout.width, rowHeight)
         .cornerRadius(5.0F)
         .color(Accent.getTextColorWithAlpha((int)((9.0F + 6.0F * hoverProgress + 4.0F * expansionProgress) * opacity)))
         .render(drawContext);
      float rowHeaderRight = rowLayout.left + rowLayout.width - 6.5F;
      float textBaselineY = rowTop + 6.5F + 2.8F;
      float moduleTitleRight = rowHeaderRight - 13.0F;
      this.renderBooleanToggle(drawContext, moduleTitleRight, textBaselineY - 3.5F, moduleAnimationProgress, opacity);
      boolean isSelectedModule = moduleStructure == this.selectedModule;
      int moduleKeybind = moduleStructure.getKeybind();
      float keybindBadgeRight = moduleTitleRight - 4.0F;
      if (isSelectedModule || moduleKeybind != 0) {
         String keybindLabel = isSelectedModule ? "..." : dev.daamky.KeybindManager.getDisplayName(moduleKeybind);
         float keybindLabelWidth = Medium.DEFAULT_FONT.measureWidth(keybindLabel, 3.8F);
         float keybindBadgeLeft = keybindBadgeRight - keybindLabelWidth - 4.0F;
         RoundedGradientRectBuilder.at(keybindBadgeLeft, textBaselineY - 3.5F, keybindLabelWidth + 4.0F, 7.0F)
            .cornerRadius(2.0F)
            .color(isSelectedModule ? Accent.getAccentColorWithAlpha((int)(60.0F * opacity)) : Accent.getTextColorWithAlpha((int)(12.0F * opacity)))
            .render(drawContext);
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, keybindLabel, keybindBadgeLeft + 2.0F, textBaselineY, 3.8F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(isSelectedModule ? Accent.getAccentColorWithAlpha((int)(255.0F * opacity)) : Accent.getDeepTextColor((int)(220.0F * opacity)))
            .render(drawContext);
         keybindBadgeRight = keybindBadgeLeft - 3.0F;
      }

      float moduleTitleLeft = rowLayout.left + 6.5F;
      GuiTextBuilder.createAt(
            MODULE_TITLE_FONT, MODULE_TITLE_FONT.truncateToWidth(moduleStructure.getName(), 5.6F, Math.max(8.0F, keybindBadgeRight - moduleTitleLeft)), moduleTitleLeft, textBaselineY, 5.6F
         )
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(
            ColorUtils.interpolateColor(
               Accent.getDeepTextColor((int)(230.0F * opacity)).toArgb(),
               Accent.getTextColorWithAlpha((int)(255.0F * opacity)).toArgb(),
               Math.max(moduleAnimationProgress, hoverProgress * 0.4F)
            )
         )
         .render(drawContext);
      float descriptionLineY = rowTop + 6.5F + 5.6F + 2.5F + 2.8F - 0.5F;

      for (Object descriptionLine : rowLayout.descriptionLines) {
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, (String)descriptionLine, moduleTitleLeft, descriptionLineY, 4.3F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(190.0F * opacity)))
            .render(drawContext);
         descriptionLineY += 5.6F;
      }

      if (!(expansionProgress <= 0.01F) && !(rowLayout.settingsHeight <= 0.0F)) {
         float settingsTop = rowTop + rowLayout.headerHeight;
         float settingsViewportHeight = rowHeight - rowLayout.headerHeight;
         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(rowLayout.left),
            PixelRounding.II1Il11l111II11IIl(settingsTop),
            PixelRounding.l1I1IlllIlI(rowLayout.left + rowLayout.width),
            PixelRounding.l1I1IlllIlI(settingsTop + settingsViewportHeight)
         );

         try {
            float settingsOpacity = opacity * easeOutQuart((expansionProgress - 0.15F) / 0.85F);
            float settingRowTop = settingsTop;
            float settingsLeft = rowLayout.left + 6.5F;
            float settingsWidth = rowLayout.width - 13.0F;

            for (Object setting : getVisibleSettings(moduleStructure)) {
               float settingRowHeight = getSettingRowHeight((Setting<?>)setting);
               boolean settingHovered = pointerEventsEnabled && mouseX >= settingsLeft && mouseX <= settingsLeft + settingsWidth && mouseY >= settingRowTop && mouseY <= settingRowTop + settingRowHeight;
               this.renderSettingControl(drawContext, (Setting<?>)setting, settingsLeft, settingRowTop, settingsWidth, settingHovered, settingsOpacity);
               settingRowTop += settingRowHeight + 7.0F;
            }
         } finally {
            drawContext.disableScissor();
         }
      }
   }
// https://t.me/pasterplace

   private static String getSettingHint(Setting<?> setting) {
      String settingName = setting.getName().toLowerCase(Locale.ROOT);
      if (setting instanceof IntSetting || settingName.contains("бинд") || settingName.contains("клавиш") || settingName.contains("bind") || settingName.contains("key")) {
         return "Q";
      } else if (settingName.contains("задерж")
         || settingName.contains("интервал")
         || settingName.contains("время")
         || settingName.contains("длительн")
         || settingName.contains("таймаут")
         || settingName.contains("ожидан")
         || settingName.contains("delay")
         || settingName.contains("duration")) {
         return "P";
      } else if (settingName.contains("зель") || settingName.contains("potion")) {
         return "O";
      } else if (settingName.contains("цели") || settingName.contains("игрок") || settingName.contains("друз") || settingName.contains("friend")) {
         return "G";
      } else if (settingName.contains("поиск")) {
         return "M";
      } else {
         return setting instanceof MultiToggleSetting ? "E" : null;
      }
   }
// https://t.me/pasterplace

   private void renderSettingMarker(DrawContext drawContext, Setting<?> setting, float markerLeft, float markerCenterY, float opacity) {
      float toggleProgress = setting instanceof BooleanSetting booleanSetting ? booleanSetting.getToggleProgress() : 0.0F;
      int markerColorArgb = ColorUtils.interpolateColor(
         Accent.getDeepTextColor((int)(210.0F * opacity)).toArgb(), Accent.getAccentColorWithAlpha((int)(255.0F * opacity)).toArgb(), toggleProgress
      );
      float markerLeftOffset = markerLeft + 2.5F;
      String settingHint = getSettingHint(setting);
      if (settingHint != null) {
         GuiTextBuilder.createAt(Medium.ICON_FONT, settingHint, markerLeftOffset, markerCenterY, 4.6F).centered().color(markerColorArgb).render(drawContext);
      } else {
         if (setting instanceof ColorSetting colorSetting) {
            RoundedGradientRectBuilder.at(markerLeftOffset - 2.2F, markerCenterY - 2.2F, 4.4F, 4.4F)
               .cornerRadius(2.2F)
               .color(colorSetting.getRgbaColor().withAlpha((int)(255.0F * opacity)))
               .render(drawContext);
         } else if (setting instanceof FloatSetting) {
            RoundedGradientRectBuilder.at(markerLeftOffset - 2.4F, markerCenterY - 0.35F, 4.8F, 0.7F)
               .cornerRadius(0.35F)
               .color(markerColorArgb)
               .render(drawContext);
            RoundedGradientRectBuilder.at(markerLeftOffset - 1.4F, markerCenterY - 1.1F, 2.2F, 2.2F)
               .cornerRadius(1.1F)
               .color(markerColorArgb)
               .render(drawContext);
         } else if (setting instanceof BooleanSetting) {
            RoundedGradientRectBuilder.at(markerLeftOffset - 2.4F, markerCenterY - 1.3F, 4.8F, 2.6F)
               .cornerRadius(1.3F)
               .color(markerColorArgb)
               .render(drawContext);
            float toggleThumbX = markerLeftOffset - 1.1F + 2.2F * toggleProgress;
            RoundedGradientRectBuilder.at(toggleThumbX - 0.8F, markerCenterY - 0.8F, 1.6F, 1.6F)
               .cornerRadius(0.8F)
               .color(Accent.getTintedBackgroundColor((int)(255.0F * opacity)))
               .render(drawContext);
         } else {
            for (int markerDotOffset = -1; markerDotOffset <= 1; markerDotOffset++) {
               RoundedGradientRectBuilder.at(markerLeftOffset - 2.3F, markerCenterY + markerDotOffset * 1.5F - 0.3F, 0.6F, 0.6F)
                  .cornerRadius(0.3F)
                  .color(markerColorArgb)
                  .render(drawContext);
               RoundedGradientRectBuilder.at(markerLeftOffset - 1.2F, markerCenterY + markerDotOffset * 1.5F - 0.3F, 3.5F, 0.6F)
                  .cornerRadius(0.3F)
                  .color(markerColorArgb)
                  .render(drawContext);
            }
         }
      }
   }
// https://t.me/pasterplace

   private void renderSettingLabel(DrawContext drawContext, Setting<?> setting, float rowLeft, float rowTop, float rowWidth, boolean isHovered, float opacity) {
      this.renderSettingMarker(drawContext, setting, rowLeft, rowTop + 3.1F, opacity);
      GuiTextBuilder.createAt(
            MODULE_TITLE_FONT,
            MODULE_TITLE_FONT.truncateToWidth(setting.getName(), 5.0F, Math.max(8.0F, rowWidth - 8.5F)),
            rowLeft + 8.5F,
            rowTop + 3.1F,
            5.0F
         )
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(isHovered ? Accent.getTextColorWithAlpha((int)(255.0F * opacity)) : Accent.getTextColorWithAlpha((int)(238.0F * opacity)))
         .render(drawContext);
      String description = setting.getDescription();
      if (!description.isEmpty()) {
         GuiTextBuilder.createAt(
               Medium.DEFAULT_FONT,
               Medium.DEFAULT_FONT.truncateToWidth(description, 4.3F, Math.max(8.0F, rowWidth)),
               rowLeft,
               rowTop + 6.5F + 2.5800002F,
               4.3F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(190.0F * opacity)))
            .render(drawContext);
      }
   }
// https://t.me/pasterplace

   private static float drawSettingValue(DrawContext drawContext, String valueText, float valueCenterX, float baselineY, int colorArgb) {
      boolean hasDegreeSymbol = valueText.endsWith("°");
      String valueWithoutDegreeSymbol = hasDegreeSymbol ? valueText.substring(0, valueText.length() - 1).stripTrailing() : valueText;
      float degreeSymbolFontSize = 2.7280002F;
      float degreeSymbolWidth = hasDegreeSymbol ? Medium.DEFAULT_FONT.measureWidth("o", degreeSymbolFontSize) + 0.5F : 0.0F;
      float valueTextWidth = Medium.DEFAULT_FONT.measureWidth(valueWithoutDegreeSymbol, 4.4F);
      GuiTextBuilder.createAt(Medium.DEFAULT_FONT, valueWithoutDegreeSymbol, valueCenterX - degreeSymbolWidth - valueTextWidth, baselineY, 4.4F)
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(colorArgb)
         .render(drawContext);
      if (hasDegreeSymbol) {
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "o", valueCenterX - degreeSymbolWidth + 0.5F, baselineY - 1.32F, degreeSymbolFontSize)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(colorArgb)
            .render(drawContext);
      }

      return valueTextWidth + degreeSymbolWidth;
   }
// https://t.me/pasterplace

   private void renderSettingControl(DrawContext drawContext, Setting<?> setting, float rowLeft, float rowTop, float rowWidth, boolean isHovered, float opacity) {
      float labelHeight = getSettingLabelHeight(setting);
      float controlCenterY = rowTop + Math.max(labelHeight, 9.0F) * 0.5F;
      float controlRight = rowLeft + rowWidth;
      if (setting instanceof FloatSetting floatSetting) {
         float formattedValueWidth = drawSettingValue(
            drawContext,
            floatSetting.formatValue(),
            controlRight,
            rowTop + 3.1F,
            floatSetting == this.activeFloatSetting
               ? Accent.getAccentColorWithAlpha((int)(255.0F * opacity)).toArgb()
               : Accent.getTextColorWithAlpha((int)(230.0F * opacity)).toArgb()
         );
         this.renderSettingLabel(drawContext, setting, rowLeft, rowTop, rowWidth - formattedValueWidth - 6.0F, isHovered, opacity);
         float sliderTrackY = rowTop + labelHeight + 5.0F;
         float sliderLeft = rowLeft;
         float sliderWidth = rowWidth;
         RoundedGradientRectBuilder.at(sliderLeft, sliderTrackY - 0.75F, sliderWidth, 1.5F)
            .cornerRadius(0.75F)
            .color(Accent.getTextColorWithAlpha((int)((isHovered ? 30 : 22) * opacity)))
            .render(drawContext);
         float sliderProgress = Math.clamp(floatSetting.sliderProgress(), 0.0F, 1.0F);
         float sliderProgressX = sliderLeft + sliderWidth * sliderProgress;
         RoundedGradientRectBuilder.at(sliderLeft, sliderTrackY - 0.75F, Math.max(1.5F, sliderProgressX - sliderLeft), 1.5F)
            .cornerRadius(0.75F)
            .color(Accent.getAccentColorWithAlpha((int)(240.0F * opacity)))
            .render(drawContext);
         float sliderThumbSize = 4.0F + (floatSetting == this.activeFloatSetting ? 0.8F : 0.0F);
         float sliderThumbCenterX = Math.clamp(sliderProgressX, sliderLeft + sliderThumbSize * 0.5F, sliderLeft + sliderWidth - sliderThumbSize * 0.5F);
         RoundedGradientRectBuilder.at(sliderThumbCenterX - sliderThumbSize * 0.5F, sliderTrackY - sliderThumbSize * 0.5F, sliderThumbSize, sliderThumbSize)
            .cornerRadius(sliderThumbSize * 0.5F)
            .color(RgbaColor.rgba(255, 255, 255, (int)(255.0F * opacity)))
            .render(drawContext);
      } else if (setting instanceof ColorSetting colorSetting) {
         float colorSwatchSize = 7.0F;
         RoundedGradientRectBuilder.at(controlRight - colorSwatchSize, rowTop + labelHeight * 0.5F - colorSwatchSize * 0.5F, colorSwatchSize, colorSwatchSize)
            .cornerRadius(colorSwatchSize * 0.5F)
            .color(colorSetting.getRgbaColor().withAlpha((int)(255.0F * opacity)))
            .render(drawContext);
         this.renderSettingLabel(drawContext, setting, rowLeft, rowTop, rowWidth - colorSwatchSize - 6.0F, isHovered, opacity);
         float paletteY = rowTop + labelHeight + 3.0F;
         float paletteHeight = 3.5F;
         float paletteLeft = rowLeft;
         float paletteWidth = rowWidth;
         float paletteSegmentWidth = paletteWidth / (OPTION_COLOR_GRADIENT_ARGB.length - 1);

         for (int segmentIndex = 0; segmentIndex < OPTION_COLOR_GRADIENT_ARGB.length - 1; segmentIndex++) {
            RoundedGradientRectBuilder paletteSegmentBuilder = RoundedGradientRectBuilder.at(
               paletteLeft + segmentIndex * paletteSegmentWidth, paletteY, paletteSegmentWidth + (segmentIndex < OPTION_COLOR_GRADIENT_ARGB.length - 2 ? 0.3F : 0.0F), paletteHeight
            );
            if (segmentIndex == 0) {
               paletteSegmentBuilder.leftCornerRadius(paletteHeight * 0.5F);
            }

            if (segmentIndex == OPTION_COLOR_GRADIENT_ARGB.length - 2) {
               paletteSegmentBuilder.rightCornerRadius(paletteHeight * 0.5F);
            }

            paletteSegmentBuilder.horizontalGradient(
                  RgbaColor.fromArgb(OPTION_COLOR_GRADIENT_ARGB[segmentIndex]).withAlpha((int)(255.0F * opacity)),
                  RgbaColor.fromArgb(OPTION_COLOR_GRADIENT_ARGB[segmentIndex + 1]).withAlpha((int)(255.0F * opacity))
               )
               .render(drawContext);
         }

         float paletteMarkerX = paletteLeft + paletteWidth * colorSetting.getHue();
         RoundedGradientRectBuilder.at(paletteMarkerX - 1.0F, paletteY - 1.0F, 2.0F, paletteHeight + 2.0F)
            .cornerRadius(1.0F)
            .color(RgbaColor.rgba(255, 255, 255, (int)(245.0F * opacity)))
            .render(drawContext);
      } else {
         float controlWidth;
         if (setting instanceof BooleanSetting booleanSetting) {
            controlWidth = 13.0F;
            this.renderBooleanToggle(drawContext, controlRight - 13.0F, controlCenterY - 3.5F, booleanSetting.getToggleProgress(), opacity);
         } else if (setting instanceof IntSetting keybindSetting) {
            boolean isCapturingKeybind = keybindSetting == this.keybindCaptureSetting;
            String keybindLabel = isCapturingKeybind ? "..." : (keybindSetting.getValue() == 0 ? "None" : dev.daamky.KeybindManager.getDisplayName(keybindSetting.getValue()));
            controlWidth = Medium.DEFAULT_FONT.measureWidth(keybindLabel, 4.4F) + 8.0F;
            RoundedGradientRectBuilder.at(controlRight - controlWidth, controlCenterY - 4.5F, controlWidth, 9.0F)
               .cornerRadius(2.5F)
               .color(isCapturingKeybind ? Accent.getAccentColorWithAlpha((int)(60.0F * opacity)) : Accent.getTextColorWithAlpha((int)(12.0F * opacity)))
               .render(drawContext);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, keybindLabel, controlRight - controlWidth * 0.5F, controlCenterY, 4.4F)
               .centered()
               .color(isCapturingKeybind ? Accent.getAccentColorWithAlpha((int)(255.0F * opacity)) : Accent.getTextColorWithAlpha((int)(235.0F * opacity)))
               .render(drawContext);
         } else {
            String optionSummary = getOptionSummary(setting);
            controlWidth = getOptionPopupWidth(setting);
            float popupLeft = controlRight - controlWidth;
            boolean isOptionSelected = setting == this.activeOptionSetting;
            RoundedGradientRectBuilder.at(popupLeft, controlCenterY - 4.5F, controlWidth, 9.0F)
               .cornerRadius(2.5F)
               .color(Accent.getTextColorWithAlpha((int)((isOptionSelected ? 22 : (isHovered ? 17 : 12)) * opacity)))
               .render(drawContext);
            GuiTextBuilder.createAt(
                  Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(optionSummary, 4.4F, controlWidth - 11.0F), popupLeft + 4.0F, controlCenterY, 4.4F
               )
               .baselinePosition(TextBaselinePosition.CENTER)
               .color(Accent.getTextColorWithAlpha((int)(240.0F * opacity)))
               .render(drawContext);
            renderDropdownArrow(
               drawContext, controlRight - 4.0F, controlCenterY, isOptionSelected ? 1.0F + 2.0F * this.optionPopupAnimationProgress : 1.0F, Accent.getDeepTextColor((int)(210.0F * opacity)).toArgb()
            );
         }

         this.renderSettingLabel(drawContext, setting, rowLeft, controlCenterY - labelHeight * 0.5F, rowWidth - controlWidth - 6.0F, isHovered, opacity);
      }
   }
// https://t.me/pasterplace

   private static String getOptionSummary(Setting<?> setting) {
      if (setting instanceof MultiOptionSetting multiOptionSetting) {
         return multiOptionSetting.getValue();
      } else {
         MultiToggleSetting multiToggleSetting = (MultiToggleSetting)setting;
         return "Выбрано " + multiToggleSetting.getEnabledCount() + "/" + multiToggleSetting.getOptions().size();
      }
   }
// https://t.me/pasterplace

   private static float getOptionPopupWidth(Setting<?> setting) {
      float widestOptionLabelWidth = Medium.DEFAULT_FONT.measureWidth(getOptionSummary(setting), 4.4F);
      if (setting instanceof MultiOptionSetting multiOptionSetting) {
         for (Object optionLabel : multiOptionSetting.getOptions()) {
            widestOptionLabelWidth = Math.max(widestOptionLabelWidth, Medium.DEFAULT_FONT.measureWidth((CharSequence)optionLabel, 4.4F));
         }
      }

      return Math.min(70.0F, widestOptionLabelWidth + 13.0F);
   }
// https://t.me/pasterplace

   private static List<String> getOptionLabels(Setting<?> setting) {
      if (setting instanceof MultiOptionSetting multiOptionSetting) {
         return multiOptionSetting.getOptions();
      } else {
         ArrayList<String> optionLabels = new ArrayList<>();

         for (Object booleanSetting : ((MultiToggleSetting)setting).getOptions()) {
            optionLabels.add(((BooleanSetting)booleanSetting).getName());
         }

         return optionLabels;
      }
   }
// https://t.me/pasterplace

   private static boolean isOptionSelected(Setting<?> setting, int optionIndex) {
      return setting instanceof MultiOptionSetting multiOptionSetting
         ? multiOptionSetting.getSelectedIndex() == optionIndex
         : ((MultiToggleSetting)setting).getOptions().get(optionIndex).getValue();
   }
// https://t.me/pasterplace

   private void renderBooleanToggle(DrawContext drawContext, float toggleLeft, float toggleTop, float toggleProgress, float opacity) {
      RoundedGradientRectBuilder.at(toggleLeft, toggleTop, 13.0F, 7.0F)
         .cornerRadius(3.5F)
         .color(
            ColorUtils.interpolateColor(
               RgbaColor.rgba(0, 0, 0, (int)(90.0F * opacity)).toArgb(),
               Accent.getAccentColorWithAlpha((int)(225.0F * opacity)).toArgb(),
               toggleProgress
            )
         )
         .render(drawContext);
      float thumbInset = 1.1F;
      float thumbSize = 7.0F - thumbInset * 2.0F;
      float thumbLeft = toggleLeft + thumbInset;
      float thumbRight = toggleLeft + 13.0F - thumbInset - thumbSize;
      RoundedGradientRectBuilder.at(thumbLeft + (thumbRight - thumbLeft) * toggleProgress, toggleTop + thumbInset, thumbSize, thumbSize)
         .cornerRadius(thumbSize * 0.5F)
         .color(RgbaColor.rgba(255, 255, 255, (int)((200.0F + 55.0F * toggleProgress) * opacity)))
         .render(drawContext);
   }
// https://t.me/pasterplace

   private static void renderDropdownArrow(DrawContext drawContext, float centerX, float centerY, float rotationProgress, int colorArgb) {
      float angleRadians = (float)((Math.PI / 2) * rotationProgress);
      float arrowWidth = 1.9F;
      float dotSize = 0.85F;
      byte segmentCount = 5;

      for (byte directionSign = -1; directionSign <= 1; directionSign += 2) {
         for (int segmentIndex = 0; segmentIndex <= segmentCount; segmentIndex++) {
            float segmentProgress = (float)segmentIndex / segmentCount;
            float localX = -arrowWidth * 0.5F + arrowWidth * segmentProgress;
            float localY = directionSign * arrowWidth * (1.0F - segmentProgress);
            float rotatedX = (float)(localX * Math.cos(angleRadians) - localY * Math.sin(angleRadians));
            float rotatedY = (float)(localX * Math.sin(angleRadians) + localY * Math.cos(angleRadians));
            RoundedGradientRectBuilder.at(centerX + rotatedX - dotSize * 0.5F, centerY + rotatedY - dotSize * 0.5F, dotSize, dotSize)
               .cornerRadius(dotSize * 0.5F)
               .color(colorArgb)
               .render(drawContext);
         }
      }
   }
// https://t.me/pasterplace

   private static void renderSelectedCheckmark(DrawContext drawContext, float centerX, float centerY, int colorArgb) {
      float[][] checkmarkPoints = new float[][]{{-1.7F, 0.1F}, {-0.5F, 1.3F}, {1.8F, -1.2F}};
      float dotSize = 0.85F;

      for (int segmentIndex = 0; segmentIndex < 2; segmentIndex++) {
         float[] segmentStart = checkmarkPoints[segmentIndex];
         float[] segmentEnd = checkmarkPoints[segmentIndex + 1];
         int segmentSteps = segmentIndex == 0 ? 4 : 7;

         for (int stepIndex = 0; stepIndex <= segmentSteps; stepIndex++) {
            float stepProgress = (float)stepIndex / segmentSteps;
            float dotX = centerX + segmentStart[0] + (segmentEnd[0] - segmentStart[0]) * stepProgress;
            float dotY = centerY + segmentStart[1] + (segmentEnd[1] - segmentStart[1]) * stepProgress;
            RoundedGradientRectBuilder.at(dotX - dotSize * 0.5F, dotY - dotSize * 0.5F, dotSize, dotSize)
               .cornerRadius(dotSize * 0.5F)
               .color(colorArgb)
               .render(drawContext);
         }
      }
   }
// https://t.me/pasterplace

   private void renderOptionPopup(DrawContext drawContext, float mouseX, float mouseY, float opacity) {
      if (this.optionPopupSetting != null) {
         float popupProgress = easeOutQuart(this.optionPopupAnimationProgress);
         float popupOpacity = opacity * popupProgress;
         if (!(popupOpacity <= 0.004F)) {
            List optionLabels = getOptionLabels(this.optionPopupSetting);
            float popupHeight = 4.0F + optionLabels.size() * 9.5F;
            float popupTop = this.optionPopupTop + (this.optionPopupOpensUpward ? 1.0F : -1.0F) * (1.0F - popupProgress) * 3.0F;
            boolean hasActiveOptionSetting = this.activeOptionSetting != null;
            drawContext.getMatrices().pushMatrix();
            float transformOriginX = this.optionPopupLeft + this.optionPopupWidth;
            float transformOriginY = this.optionPopupOpensUpward ? popupTop + popupHeight : popupTop;
            float popupScale = 0.95F + 0.05F * popupProgress;
            drawContext.getMatrices().translate(transformOriginX, transformOriginY);
            drawContext.getMatrices().scale(popupScale, popupScale);
            drawContext.getMatrices().translate(-transformOriginX, -transformOriginY);
// https://t.me/pasterplace

            try {
               Accent accent = Accent.getInstance();
               KawaseBlurBuilder.create(this.optionPopupLeft, popupTop, this.optionPopupWidth, popupHeight)
                  .cornerRadius(5.0F)
                  .blurMode(KawaseGaussianBlurMode.KAWASE)
                  .blurPasses((int)Math.max(6.0F, accent.getBlurRadius()))
                  .color(Accent.getTintedBackgroundColor((int)(190.0F * popupOpacity)))
                  .opacity(popupOpacity)
                  .render(drawContext);
               RoundedGradientRectBuilder.at(this.optionPopupLeft, popupTop, this.optionPopupWidth, popupHeight)
                  .cornerRadius(5.0F)
                  .color(Accent.getAccentBackgroundColor((int)(34.0F * popupOpacity)))
                  .render(drawContext);
               RoundedGradientRectBuilder.at(this.optionPopupLeft, popupTop, this.optionPopupWidth, popupHeight)
                  .cornerRadius(5.0F)
                  .color(Accent.getTextColorWithAlpha((int)(12.0F * popupOpacity)))
                  .render(drawContext);
               RoundedRectBuilder.createAt(this.optionPopupLeft, popupTop, this.optionPopupWidth, popupHeight)
                  .cornerRadius(5.0F)
                  .borderWidth(0.7F)
                  .color(Accent.getTextColorWithAlpha((int)(16.0F * popupOpacity)))
                  .render(drawContext);
               int hoveredOptionIndex = -1;
               if (hasActiveOptionSetting && mouseX >= this.optionPopupLeft && mouseX <= this.optionPopupLeft + this.optionPopupWidth) {
                  int hoveredOptionCandidate = (int)Math.floor((mouseY - popupTop - 2.0F) / 9.5F);
                  if (hoveredOptionCandidate >= 0 && hoveredOptionCandidate < optionLabels.size()) {
                     hoveredOptionIndex = hoveredOptionCandidate;
                  }
               }

               if (hoveredOptionIndex >= 0) {
                  float hoveredOptionTop = popupTop + 2.0F + hoveredOptionIndex * 9.5F;
                  this.selectedOptionIndicatorTop = this.optionHoverProgress < 0.02F ? hoveredOptionTop : this.interpolateValue(this.selectedOptionIndicatorTop, hoveredOptionTop, 20.0F);
               }

               this.optionHoverProgress = this.interpolateValue(this.optionHoverProgress, hoveredOptionIndex >= 0 ? 1.0F : 0.0F, 16.0F);
               if (this.optionHoverProgress > 0.01F) {
                  RoundedGradientRectBuilder.at(this.optionPopupLeft + 2.0F, this.selectedOptionIndicatorTop, this.optionPopupWidth - 4.0F, 9.5F)
                     .cornerRadius(3.0F)
                     .color(Accent.getTextColorWithAlpha((int)(14.0F * this.optionHoverProgress * popupOpacity)))
                     .render(drawContext);
               }

               for (int optionIndex = 0; optionIndex < optionLabels.size(); optionIndex++) {
                  float optionTop = popupTop + 2.0F + optionIndex * 9.5F;
                  float optionAnimationProgress = easeOutQuart((this.optionPopupAnimationProgress - optionIndex * 0.035F) / 0.6F);
                  float optionOpacity = popupOpacity * optionAnimationProgress;
                  boolean isSelected = isOptionSelected(this.optionPopupSetting, optionIndex);
                  float optionCenterY = optionTop + 4.75F;
                  float optionTextLeft = this.optionPopupLeft + 6.0F + (1.0F - optionAnimationProgress) * 2.0F;
                  GuiTextBuilder.createAt(
                        Medium.DEFAULT_FONT,
                        Medium.DEFAULT_FONT.truncateToWidth((String)optionLabels.get(optionIndex), 4.4F, this.optionPopupWidth - 20.0F),
                        optionTextLeft,
                        optionCenterY,
                        4.4F
                     )
                     .baselinePosition(TextBaselinePosition.CENTER)
                     .color(
                        isSelected
                           ? Accent.getTextColorWithAlpha((int)(250.0F * optionOpacity))
                           : (optionIndex == hoveredOptionIndex ? Accent.getTextColorWithAlpha((int)(235.0F * optionOpacity)) : Accent.getDeepTextColor((int)(210.0F * optionOpacity)))
                     )
                     .render(drawContext);
                  if (isSelected) {
                     renderSelectedCheckmark(
                        drawContext,
                        this.optionPopupLeft + this.optionPopupWidth - 7.0F,
                        optionCenterY,
                        Accent.getAccentColorWithAlpha((int)(255.0F * optionOpacity)).toArgb()
                     );
                  }
               }
            } finally {
               drawContext.getMatrices().popMatrix();
            }
         }
      }
   }
// https://t.me/pasterplace

   private ModuleSettingsPanel.ModuleRowLayout getModuleRowAt(float mouseX, float mouseY) {
      for (Object moduleRowObject : this.moduleRows) {
         float moduleRowTop = this.contentTop + 7.0F + ((ModuleSettingsPanel.ModuleRowLayout)moduleRowObject).top - this.scrollOffset;
         float moduleRowHeight = ((ModuleSettingsPanel.ModuleRowLayout)moduleRowObject).getRenderedHeight(
            easeOutQuart(this.getModuleAnimationState(((ModuleSettingsPanel.ModuleRowLayout)moduleRowObject).module)[0])
         );
         if (mouseX >= ((ModuleSettingsPanel.ModuleRowLayout)moduleRowObject).left
            && mouseX <= ((ModuleSettingsPanel.ModuleRowLayout)moduleRowObject).left + ((ModuleSettingsPanel.ModuleRowLayout)moduleRowObject).width
            && mouseY >= moduleRowTop
            && mouseY <= moduleRowTop + moduleRowHeight) {
            return (ModuleSettingsPanel.ModuleRowLayout)moduleRowObject;
         }
      }
// https://t.me/pasterplace

      return null;
   }
// https://t.me/pasterplace

   public boolean handleMouseClick(float mouseX, float mouseY, int button) {
      if (this.activeOptionSetting != null) {
         List optionLabels = getOptionLabels(this.activeOptionSetting);
         float popupHeight = 4.0F + optionLabels.size() * 9.5F;
         if (mouseX >= this.optionPopupLeft
            && mouseX <= this.optionPopupLeft + this.optionPopupWidth
            && mouseY >= this.optionPopupTop
            && mouseY <= this.optionPopupTop + popupHeight) {
            int optionIndex = (int)((mouseY - this.optionPopupTop - 2.0F) / 9.5F);
            if (optionIndex >= 0 && optionIndex < optionLabels.size() && button == 0) {
               if (this.activeOptionSetting instanceof MultiOptionSetting multiOptionSetting) {
                  multiOptionSetting.setSelectedIndex(optionIndex);
                  this.activeOptionSetting = null;
               } else {
                  ((MultiToggleSetting)this.activeOptionSetting).getOptions().get(optionIndex).toggle();
               }
            }

            return true;
         } else {
            this.activeOptionSetting = null;
            return true;
         }
      } else {
         if (this.keybindCaptureSetting != null && button >= 2) {
            this.keybindCaptureSetting.setValue(Integer.valueOf(dev.daamky.KeybindManager.encodeMouseButton(button)));
            this.keybindCaptureSetting = null;
            return true;
         }

         if (!(mouseX < this.contentLeft)
            && !(mouseX > this.contentLeft + this.contentWidth)
            && !(mouseY < this.contentTop)
            && !(mouseY > this.contentTop + this.contentHeight)) {
            ModuleSettingsPanel.ModuleRowLayout moduleRow = this.getModuleRowAt(mouseX, mouseY);
            if (moduleRow == null) {
               return false;
            }

            ModuleStructure moduleStructure = moduleRow.module;
            float[] moduleAnimationState = this.getModuleAnimationState(moduleStructure);
            float moduleRowTop = this.contentTop + 7.0F + moduleRow.top - this.scrollOffset;
            if (button == 2) {
               this.selectedModule = this.selectedModule == moduleStructure ? null : moduleStructure;
               return true;
            }

            if (mouseY <= moduleRowTop + moduleRow.headerHeight) {
               if (button == 0) {
                  moduleStructure.toggle();
               }

               return true;
            } else {
               if (moduleAnimationState[0] < 0.9F) {
                  return true;
               }
// https://t.me/pasterplace

               float settingRowTop = moduleRowTop + moduleRow.headerHeight;
               float settingsLeft = moduleRow.left + 6.5F;
               float settingsWidth = moduleRow.width - 13.0F;
               boolean isPrimaryClick = button == 0;

               for (Object settingObject : getVisibleSettings(moduleStructure)) {
                  float settingRowHeight = getSettingRowHeight((Setting<?>)settingObject);
                  if (mouseY >= settingRowTop && mouseY <= settingRowTop + settingRowHeight) {
                     this.handleSettingClick((Setting<?>)settingObject, settingsLeft, settingRowTop, settingsWidth, mouseX, mouseY, isPrimaryClick);
                     return true;
                  }

                  settingRowTop += settingRowHeight + 7.0F;
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }
// https://t.me/pasterplace

   private void handleSettingClick(
      Setting<?> setting, float controlLeft, float rowTop, float controlWidth, float mouseX, float mouseY, boolean isPrimaryClick
   ) {
      float controlRight = controlLeft + controlWidth;
      if (setting instanceof FloatSetting floatSetting) {
         if (!isPrimaryClick) {
            return;
         }
// https://t.me/pasterplace

         this.activeFloatSetting = floatSetting;
         this.settingControlStartX = controlLeft;
         this.settingControlWidth = controlWidth;
         updateFloatSettingFromMouse(floatSetting, (mouseX - this.settingControlStartX) / this.settingControlWidth);
      } else if (setting instanceof BooleanSetting booleanSetting) {
         if (isPrimaryClick) {
            booleanSetting.toggle();
         }
      } else if (!(setting instanceof MultiOptionSetting) && !(setting instanceof MultiToggleSetting)) {
         if (setting instanceof IntSetting keybindSetting) {
            if (isPrimaryClick) {
               this.keybindCaptureSetting = this.keybindCaptureSetting == keybindSetting ? null : keybindSetting;
            } else {
               keybindSetting.setValue(Integer.valueOf(0));
               this.keybindCaptureSetting = null;
            }
         } else if (setting instanceof ColorSetting colorSetting) {
            if (!isPrimaryClick) {
               return;
            }

            if (mouseY >= rowTop + getSettingLabelHeight(setting)) {
               this.activeColorSetting = colorSetting;
               this.settingControlStartX = controlLeft;
               this.settingControlWidth = controlWidth;
               this.updateColorSettingFromMouse(colorSetting, mouseX);
            }
         }
      } else {
         if (!isPrimaryClick && setting instanceof MultiOptionSetting multiOptionSetting) {
            int optionCount = multiOptionSetting.getOptions().size();
            multiOptionSetting.setSelectedIndex((multiOptionSetting.getSelectedIndex() + optionCount - 1) % optionCount);
            return;
         }

         float minimumPopupWidth = getOptionPopupWidth(setting);
         List optionLabels = getOptionLabels(setting);
         float widestOptionLabelWidth = 0.0F;

         for (Object optionLabel : optionLabels) {
            widestOptionLabelWidth = Math.max(widestOptionLabelWidth, Medium.DEFAULT_FONT.measureWidth((CharSequence)optionLabel, 4.4F));
         }

         this.optionPopupWidth = Math.max(minimumPopupWidth, widestOptionLabelWidth + 22.0F);
         this.optionPopupLeft = controlRight - this.optionPopupWidth;
         float rowCenterY = rowTop + Math.max(getSettingLabelHeight(setting), 9.0F) * 0.5F;
         float popupBelowTop = rowCenterY + 4.5F + 2.0F;
         float optionPopupHeight = 4.0F + optionLabels.size() * 9.5F;
         this.optionPopupOpensUpward = popupBelowTop + optionPopupHeight > this.contentTop + this.contentHeight;
         this.optionPopupTop = this.optionPopupOpensUpward ? rowCenterY - 4.5F - 2.0F - optionPopupHeight : popupBelowTop;
         this.optionHoverProgress = 0.0F;
         this.activeOptionSetting = setting;
         this.optionPopupSetting = setting;
         this.optionPopupAnimationProgress = 0.0F;
      }
   }
// https://t.me/pasterplace

   private void updateColorSettingFromMouse(ColorSetting colorSetting, float mouseX) {
      float hueProgress = Math.clamp((mouseX - this.settingControlStartX) / this.settingControlWidth, 0.0F, 0.999F);
      colorSetting.setHsb(hueProgress, Math.max(0.35F, colorSetting.getSaturation()), Math.max(0.35F, colorSetting.getBrightness()));
   }
// https://t.me/pasterplace

   private static void updateFloatSettingFromMouse(FloatSetting floatSetting, float sliderProgress) {
      float previousValue = floatSetting.getValue();
      floatSetting.setSliderProgress(sliderProgress);
      if (floatSetting.getValue() != previousValue) {
         ClientSounds.playSliderSound(
            (floatSetting.getValue() - floatSetting.minimum())
               / (floatSetting.maximum() - floatSetting.minimum())
         );
      }
   }
// https://t.me/pasterplace

   public void handleMouseDrag(float mouseX, float mouseY) {
      if (this.activeFloatSetting != null) {
         updateFloatSettingFromMouse(this.activeFloatSetting, (mouseX - this.settingControlStartX) / this.settingControlWidth);
      }

      if (this.activeColorSetting != null) {
         this.updateColorSettingFromMouse(this.activeColorSetting, mouseX);
      }
   }
// https://t.me/pasterplace

   public void handleMouseRelease(int mouseButton) {
      if (mouseButton == 0) {
         this.activeFloatSetting = null;
         this.activeColorSetting = null;
      }
   }
// https://t.me/pasterplace

   public boolean handleMouseScroll(double mouseX, double mouseY, double scrollAmount) {
      if (mouseX >= this.contentLeft
         && mouseX <= this.contentLeft + this.contentWidth
         && mouseY >= this.contentTop
         && mouseY <= this.contentTop + this.contentHeight) {
         this.activeOptionSetting = null;
         this.targetScrollOffset = Math.clamp(this.targetScrollOffset - (float)scrollAmount * 18.0F, 0.0F, this.maxScrollOffset);
         return true;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   private static final class ModuleRowLayout {
      final ModuleStructure module;
      float left;
      float top;
      float width;
      float headerHeight;
      float settingsHeight;
      List<String> descriptionLines;

      ModuleRowLayout(ModuleStructure moduleStructure) {
         this.module = moduleStructure;
      }

      float getRenderedHeight(float expansionProgress) {
         return this.headerHeight + (this.settingsHeight > 0.0F ? (this.settingsHeight + 3.25F) * expansionProgress : 0.0F);
      }
   }
}

