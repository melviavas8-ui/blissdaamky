package dev.daamky;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

public final class Bots {
    private static final String sourceAttributionUrl = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float LAYOUT_FLOAT_5_0 = 5.0F;
   private static final float LAYOUT_FLOAT_11_5 = 11.5F;
   private static final float LAYOUT_FLOAT_7_5 = 7.5F;
   private static final float LAYOUT_FLOAT_13_0_A = 13.0F;
   private static final float LAYOUT_FLOAT_12_0 = 12.0F;
   private static final float LAYOUT_FLOAT_15_0 = 15.0F;
   private static final float LAYOUT_FLOAT_2_5 = 2.5F;
   private static final float LAYOUT_FLOAT_9_0 = 9.0F;
   private static final float LAYOUT_FLOAT_5_6 = 5.6F;
   private static final float LAYOUT_FLOAT_5_4_A = 5.4F;
   private static final float LAYOUT_FLOAT_5_4_B = 5.4F;
   private static final float LAYOUT_FLOAT_4_2 = 4.2F;
   private static final float LAYOUT_FLOAT_4_0_A = 4.0F;
   private static final float LAYOUT_FLOAT_8_0 = 8.0F;
   private static final float LAYOUT_FLOAT_13_0_B = 13.0F;
   private static final float LAYOUT_FLOAT_7_0_A = 7.0F;
   private static final float LAYOUT_FLOAT_7_0_B = 7.0F;
   private static final float LAYOUT_FLOAT_11_0_A = 11.0F;
   private static final float LAYOUT_FLOAT_4_0_B = 4.0F;
   private final SearchTextField botNameSearchField = new SearchTextField("Ник бота...", 16, this::addBotFromSearch);
   private final SearchTextField chatMessageField = new SearchTextField("Сообщение или /команда", 256, this::sendChatMessage);
   private final Map<String, float[]> rowAnimationStates = new HashMap<>();
   private final float[] buttonHoverAnimations = new float[10];
   private BotSession selectedBot;
   private boolean sendMessagesToAllBots;
   private float autoReconnectToggleAnimation;
   private float broadcastChatToggleAnimation;
   private String toastMessage = "";
   private long toastShownAtMs;
   private float frameDeltaSeconds = 0.016F;
   private long lastFrameTimeMs;
   private long panelAnimationStartedAtMs;
   private float botListScrollOffset;
   private float targetBotListScrollOffset;
   private float maxBotListScrollOffset;
   private float chatHistoryScrollOffset;
   private boolean modulesTabSelected;
   private float moduleListScrollOffset;
   private float maxModuleListScrollOffset;
   private static final float LAYOUT_FLOAT_11_0_B = 11.0F;
   private float maxChatHistoryScrollOffset;
   private float panelLeft;
   private float panelTop;
   private float panelWidth;
   private float panelHeight;
   private float detailsPanelLeft;
   private float detailsPanelTop;
   private float detailsPanelHeight;
   private float detailsPanelWidth;
// https://t.me/pasterplace

   public void updateAnimationState(float deltaSeconds) {
      this.frameDeltaSeconds = deltaSeconds;
      this.targetBotListScrollOffset = Math.clamp(this.targetBotListScrollOffset, 0.0F, this.maxBotListScrollOffset);
      this.botListScrollOffset = this.botListScrollOffset + (this.targetBotListScrollOffset - this.botListScrollOffset) * (1.0F - (float)Math.exp(-6.5F * deltaSeconds));
      if (Math.abs(this.targetBotListScrollOffset - this.botListScrollOffset) < 0.05F) {
         this.botListScrollOffset = this.targetBotListScrollOffset;
      }

      float animationSmoothingFactor = 1.0F - (float)Math.exp(-10.0F * deltaSeconds);
      this.autoReconnectToggleAnimation = this.autoReconnectToggleAnimation
         + ((this.selectedBot != null && this.selectedBot.isAutoReconnectEnabled() ? 1.0F : 0.0F) - this.autoReconnectToggleAnimation) * animationSmoothingFactor;
      this.broadcastChatToggleAnimation = this.broadcastChatToggleAnimation + ((this.sendMessagesToAllBots ? 1.0F : 0.0F) - this.broadcastChatToggleAnimation) * animationSmoothingFactor;

      for (Map.Entry<String, float[]> animationEntry : this.rowAnimationStates.entrySet()) {
         float[] animationValues = animationEntry.getValue();
         boolean isSelectedBot = this.selectedBot != null && this.selectedBot.getUsername().equals(animationEntry.getKey());
         animationValues[1] += ((isSelectedBot ? 1.0F : 0.0F) - animationValues[1]) * animationSmoothingFactor;
      }

      if (this.selectedBot != null && !SavedBots.getSessionRegistry().getSessions().contains(this.selectedBot)) {
         this.selectedBot = null;
      }
   }
// https://t.me/pasterplace

   public void render(
      DrawContext drawContext,
      float mouseX,
      float mouseY,
      float panelLeft,
      float panelTop,
      float panelWidth,
      float panelHeight,
      float detailsPanelLeft,
      float detailsPanelTop,
      float detailsPanelWidth,
      float detailsPanelHeight,
      float panelAlpha
   ) {
      if (!(panelAlpha <= 0.0F)) {
         long nowMs = System.currentTimeMillis();
         if (nowMs - this.lastFrameTimeMs > 250L) {
            this.panelAnimationStartedAtMs = nowMs;
         }

         this.lastFrameTimeMs = nowMs;
         this.panelLeft = panelLeft;
         this.panelTop = panelTop;
         this.panelWidth = panelWidth;
         this.panelHeight = panelHeight;
         this.detailsPanelLeft = detailsPanelLeft;
         this.detailsPanelTop = detailsPanelTop;
         this.detailsPanelHeight = detailsPanelWidth;
         this.detailsPanelWidth = detailsPanelHeight;
         this.drawBotList(drawContext, mouseX, mouseY, nowMs, panelAlpha);
         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(detailsPanelLeft),
            PixelRounding.II1Il11l111II11IIl(detailsPanelTop),
            PixelRounding.l1I1IlllIlI(detailsPanelLeft + detailsPanelWidth - 6.0F),
            PixelRounding.l1I1IlllIlI(detailsPanelTop + detailsPanelHeight)
         );

         try {
            this.drawDetailsPanel(drawContext, mouseX, mouseY, nowMs, panelAlpha);
         } finally {
            drawContext.disableScissor();
         }
      }
   }
// https://t.me/pasterplace

   private float getSearchFieldTop() {
      return this.panelTop + 11.5F + 7.5F;
   }
// https://t.me/pasterplace

   private float getBulkActionsTop() {
      return this.getSearchFieldTop() + 13.0F + 4.0F;
   }
// https://t.me/pasterplace

   private float getBotListTop() {
      return this.getBulkActionsTop() + 12.0F + 9.0F;
   }
// https://t.me/pasterplace

   private float getBotNameSearchWidth() {
      return this.panelWidth - 10.0F - 13.0F - 4.0F;
   }
// https://t.me/pasterplace

   private float getBulkActionButtonWidth() {
      return (this.panelWidth - 10.0F - 4.0F) * 0.5F;
   }
// https://t.me/pasterplace

   private float getBotListVisibleHeight() {
      return this.panelTop + this.panelHeight - this.getBotListTop();
   }
// https://t.me/pasterplace

   private void drawBotList(DrawContext drawContext, float mouseX, float mouseY, long nowMs, float panelAlpha) {
      SavedBots.SavedBotSessionRegistry botRegistry = SavedBots.getSessionRegistry();
      List<BotSession> botSessions = botRegistry.getSessions();
      float headerCenterY = this.panelTop + 5.75F;
      drawContext.enableScissor(
         PixelRounding.II1Il11l111II11IIl(this.panelLeft),
         PixelRounding.II1Il11l111II11IIl(this.panelTop),
         PixelRounding.l1I1IlllIlI(this.panelLeft + this.panelWidth),
         PixelRounding.l1I1IlllIlI(this.panelTop + this.panelHeight)
      );

      try {
         GuiTextBuilder.createAt(Medium.ICON_FONT, "G", this.panelLeft + 5.0F + 2.8F, headerCenterY, 5.6F)
            .centered()
            .color(Accent.getAccentColorWithAlpha((int)(235.0F * panelAlpha)))
            .render(drawContext);
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Bots", this.panelLeft + 5.0F + 5.6F + 3.0F, headerCenterY, 5.4F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(245.0F * panelAlpha)))
            .render(drawContext);
         String botCountLabel = botRegistry.countOnlineSessions() + "/" + botSessions.size();
         float botCountTextWidth = Medium.DEFAULT_FONT.measureWidth(botCountLabel, 4.4F);
         float botCountBadgeWidth = botCountTextWidth + 6.0F;
         float botCountBadgeLeft = this.panelLeft + this.panelWidth - 5.0F - botCountBadgeWidth;
         RoundedGradientRectBuilder.at(botCountBadgeLeft, headerCenterY - 3.5F, botCountBadgeWidth, 7.0F)
            .cornerRadius(3.5F)
            .color(Accent.getAccentColorWithAlpha((int)(60.0F * panelAlpha)))
            .render(drawContext);
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, botCountLabel, botCountBadgeLeft + botCountBadgeWidth * 0.5F, headerCenterY, 4.4F)
            .centered()
            .color(Accent.getTextColorWithAlpha((int)(255.0F * panelAlpha)))
            .render(drawContext);
         float searchFieldLeft = this.panelLeft + 5.0F;
         float searchFieldHeight = 13.0F;
         float searchFieldWidth = this.getBotNameSearchWidth();
         this.botNameSearchField.render(drawContext, searchFieldLeft, this.getSearchFieldTop(), searchFieldWidth, 13.0F, nowMs, panelAlpha);
         float addBotButtonLeft = searchFieldLeft + searchFieldWidth + 4.0F;
         boolean addBotButtonHovered = containsPoint(mouseX, mouseY, addBotButtonLeft, this.getSearchFieldTop(), searchFieldHeight, 13.0F);
         float addBotHoverAmount = this.updateHoverAnimation(this.buttonHoverAnimations, 0, addBotButtonHovered);
         RoundedGradientRectBuilder.at(addBotButtonLeft, this.getSearchFieldTop(), searchFieldHeight, 13.0F)
            .cornerRadius(Accent.getInstance().getCornerRadius() * 0.5F)
            .color(
               ColorUtils.interpolateColor(
                  Accent.getAccentColorWithAlpha((int)(60.0F * panelAlpha)).toArgb(), Accent.getAccentColorWithAlpha((int)(150.0F * panelAlpha)).toArgb(), addBotHoverAmount
               )
            )
            .render(drawContext);
         GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "+", addBotButtonLeft + searchFieldHeight * 0.5F, this.getSearchFieldTop() + 6.5F, 7.0F)
            .centered()
            .color(Accent.getTextColorWithAlpha((int)(255.0F * panelAlpha)))
            .render(drawContext);
         float bulkActionButtonWidth = this.getBulkActionButtonWidth();
         this.drawActionButton(drawContext, mouseX, mouseY, 1, "Зайти всеми", searchFieldLeft, this.getBulkActionsTop(), bulkActionButtonWidth, true, panelAlpha);
         this.drawActionButton(drawContext, mouseX, mouseY, 2, "Выйти всеми", searchFieldLeft + bulkActionButtonWidth + 4.0F, this.getBulkActionsTop(), bulkActionButtonWidth, false, panelAlpha);
         float sectionDividerY = this.getBotListTop() - 4.5F;
         RoundedGradientRectBuilder.at(this.panelLeft + 5.0F, sectionDividerY - 0.375F, this.panelWidth - 10.0F, 0.75F)
            .cornerRadius(0.0F)
            .color(Accent.getTextColorWithAlpha((int)(12.0F * panelAlpha)))
            .render(drawContext);
      } finally {
         drawContext.disableScissor();
      }
// https://t.me/pasterplace

      float botListTop = this.getBotListTop();
      float botListHeight = this.getBotListVisibleHeight();
      this.maxBotListScrollOffset = Math.max(0.0F, botSessions.size() * 17.5F - 2.5F - botListHeight);
      if (botSessions.isEmpty()) {
         GuiTextBuilder.createAt(
               Medium.DEFAULT_FONT, "Добавьте бота по нику", this.panelLeft + this.panelWidth * 0.5F, botListTop + 12.0F, 4.6F
            )
            .centered()
            .color(Accent.getDeepTextColor((int)(200.0F * panelAlpha)))
            .render(drawContext);
      } else {
         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(this.panelLeft),
            PixelRounding.II1Il11l111II11IIl(botListTop),
            PixelRounding.l1I1IlllIlI(this.panelLeft + this.panelWidth),
            PixelRounding.l1I1IlllIlI(botListTop + botListHeight)
         );

         try {
            for (int botIndex = 0; botIndex < botSessions.size(); botIndex++) {
               BotSession botSession = botSessions.get(botIndex);
               float botRowTop = botListTop + botIndex * 17.5F - this.botListScrollOffset;
               if (!(botRowTop + 15.0F < botListTop) && !(botRowTop > botListTop + botListHeight)) {
                  float[] animation = this.rowAnimationStates.computeIfAbsent(botSession.getUsername(), username -> new float[]{0.0F, 0.0F});
                  boolean botRowHovered = containsPoint(mouseX, mouseY, this.panelLeft + 5.0F, botRowTop, this.panelWidth - 10.0F, 15.0F)
                     && mouseY >= botListTop
                     && mouseY <= botListTop + botListHeight;
                  animation[0] += ((botRowHovered ? 1.0F : 0.0F) - animation[0]) * (1.0F - (float)Math.exp(-6.0F * this.frameDeltaSeconds));
                  float hoverAnimation = smoothstep(animation[0]);
                  float selectionAnimation = smoothstep(animation[1]);
                  float rowEntranceProgress = easeOutCubic((float)(nowMs - this.panelAnimationStartedAtMs - Math.min(botIndex, 12) * 25L) / 260.0F);
                  if (!(rowEntranceProgress <= 0.002F)) {
                     float rowAlpha = panelAlpha * rowEntranceProgress;
                     float rowSlideOffset = (1.0F - rowEntranceProgress) * 7.0F;
                     float botRowCenterY = botRowTop + 7.5F;
                     float rowHighlightAmount = Math.max(hoverAnimation, selectionAnimation);
                     RoundedGradientRectBuilder.at(this.panelLeft + 5.0F, botRowTop, this.panelWidth - 10.0F, 15.0F)
                        .cornerRadius(Accent.getInstance().getCornerRadius() * 0.5F)
                        .color(Accent.getAccentColorWithAlpha((int)((selectionAnimation * 34.0F + hoverAnimation * 12.0F) * rowAlpha)))
                        .render(drawContext);
                     float statusDotSize = 3.4F;
                     RoundedGradientRectBuilder.at(this.panelLeft + 5.0F + 5.0F + rowSlideOffset, botRowCenterY - statusDotSize * 0.5F, statusDotSize, statusDotSize)
                        .cornerRadius(statusDotSize * 0.5F)
                        .color(connectionStatusColor(botSession.getConnectionStatus(), rowAlpha))
                        .render(drawContext);
                     String statusLabel = botSession.getConnectionStatus().getDisplayName();
                     float statusLabelWidth = Medium.DEFAULT_FONT.measureWidth(statusLabel, 4.2F);
                     float usernameLeft = this.panelLeft + 5.0F + 12.0F + hoverAnimation * 2.0F + rowSlideOffset;
                     float statusLabelLeft = this.panelLeft + this.panelWidth - 5.0F - 5.0F - statusLabelWidth + rowSlideOffset;
                     float usernameMaxWidth = statusLabelLeft - 4.0F - usernameLeft;
                     GuiTextBuilder.createAt(
                           Medium.DEFAULT_FONT,
                            Medium.DEFAULT_FONT.truncateToWidth(botSession.getUsername(), 5.4F, usernameMaxWidth),
                           usernameLeft,
                           botRowCenterY,
                           5.4F
                        )
                        .baselinePosition(TextBaselinePosition.CENTER)
                        .color(
                           ColorUtils.interpolateColor(
                              RgbaColor.rgba(140, 145, 160, (int)(175.0F * rowAlpha)).toArgb(),
                              RgbaColor.rgba(255, 255, 255, (int)(255.0F * rowAlpha)).toArgb(),
                              rowHighlightAmount
                           )
                        )
                        .render(drawContext);
                     GuiTextBuilder.createAt(Medium.DEFAULT_FONT, statusLabel, statusLabelLeft, botRowCenterY, 4.2F)
                        .baselinePosition(TextBaselinePosition.CENTER)
                        .color(Accent.getDeepTextColor((int)(200.0F * rowAlpha)))
                        .render(drawContext);
                  }
               }
            }
         } finally {
            drawContext.disableScissor();
         }
      }
   }
// https://t.me/pasterplace

   private float getDetailsPanelLeft() {
      return this.detailsPanelLeft + 4.0F;
   }
// https://t.me/pasterplace

   private float getDetailsPanelHeight() {
      return this.detailsPanelHeight - 4.0F - 8.0F;
   }
// https://t.me/pasterplace

   private float getConnectionActionsTop() {
      return this.detailsPanelTop + 11.5F + 7.5F + 0.5F;
   }
// https://t.me/pasterplace

   private float getBotManagementButtonTop() {
      return this.detailsPanelTop + (this.getBulkActionsTop() - this.panelTop);
   }
// https://t.me/pasterplace

   private float getSettingsRowsTop() {
      return this.detailsPanelTop + (this.getBotListTop() - this.panelTop);
   }
// https://t.me/pasterplace

   private float getSettingRowCenterY(int settingIndex) {
      return this.getSettingsRowsTop() + 11.0F * (settingIndex + 0.5F);
   }
// https://t.me/pasterplace

   private float getDetailsTabsTop() {
      return this.getSettingsRowsTop() + 33.0F + 4.5F;
   }
// https://t.me/pasterplace

   private float getDetailsTabWidth() {
      return 46.0F;
   }
// https://t.me/pasterplace

   private float getModuleListTop() {
      return this.getDetailsTabsTop() + 12.0F + 4.0F;
   }
// https://t.me/pasterplace

   private float getChatInputTop() {
      return this.detailsPanelTop + this.detailsPanelWidth - 13.0F - 2.0F;
   }
// https://t.me/pasterplace

   private float getChatHistoryHeight() {
      return this.getChatInputTop() - 4.0F - this.getModuleListTop();
   }
// https://t.me/pasterplace

   private void drawDetailsPanel(DrawContext drawContext, float mouseX, float mouseY, long nowMs, float panelAlpha) {
      float entranceProgress = easeOutCubic((float)(nowMs - this.panelAnimationStartedAtMs - 60L) / 320.0F);
      float detailsAlpha = panelAlpha * entranceProgress;
      if (!(detailsAlpha <= 0.002F)) {
         float detailsLeft = this.getDetailsPanelLeft();
         float detailsHeight = this.getDetailsPanelHeight();
         if (this.selectedBot == null) {
            float emptyStateCenterX = this.detailsPanelLeft + (this.detailsPanelHeight - 6.0F) * 0.5F;
            float emptyStateCenterY = this.detailsPanelTop + this.detailsPanelWidth * 0.5F;
            GuiTextBuilder.createAt(Medium.ICON_FONT, "G", emptyStateCenterX, emptyStateCenterY - 12.0F, 18.0F)
               .centered()
               .color(Accent.getAccentColorWithAlpha((int)(120.0F * detailsAlpha)))
               .render(drawContext);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Выберите бота", emptyStateCenterX, emptyStateCenterY + 6.0F, 6.8F)
               .centered()
               .color(Accent.getDeepTextColor((int)(200.0F * detailsAlpha)))
               .render(drawContext);
            String currentServerAddress = SavedBots.getSessionRegistry().getCurrentServerAddress();
            GuiTextBuilder.createAt(
                  Medium.DEFAULT_FONT, currentServerAddress == null ? "Сначала зайдите на сервер" : "Сервер: " + currentServerAddress, emptyStateCenterX, emptyStateCenterY + 15.0F, 4.6F
               )
               .centered()
               .color(Accent.getDarkerTextColor((int)(170.0F * detailsAlpha)))
               .render(drawContext);
            this.drawToast(drawContext, emptyStateCenterX, this.detailsPanelTop + this.detailsPanelWidth - 8.0F, nowMs, detailsAlpha);
         } else {
            BotSession disconnect = this.selectedBot;
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, disconnect.getUsername(), detailsLeft, this.detailsPanelTop + 4.5F, 7.0F)
               .baselinePosition(TextBaselinePosition.CENTER)
               .color(Accent.getTextColorWithAlpha((int)(250.0F * detailsAlpha)))
               .render(drawContext);
            String connectionSummary = disconnect.getConnectionStatus().getDisplayName();
            if (!"No Server".equals(disconnect.getServerAddress())) {
               connectionSummary = connectionSummary + "  |  " + disconnect.getServerAddress();
            }

            if (disconnect.isOfflineOrDisconnected() && !disconnect.getLastDisconnectReason().isEmpty()) {
               connectionSummary = connectionSummary + "  |  " + disconnect.getLastDisconnectReason();
            }

            RoundedGradientRectBuilder.at(detailsLeft + 0.3F, this.detailsPanelTop + 12.5F - 1.7F, 3.4F, 3.4F)
               .cornerRadius(1.7F)
               .color(connectionStatusColor(disconnect.getConnectionStatus(), detailsAlpha))
               .render(drawContext);
            GuiTextBuilder.createAt(
                  Medium.DEFAULT_FONT,
                  Medium.DEFAULT_FONT.truncateToWidth(connectionSummary, 4.4F, detailsHeight - 6.0F),
                  detailsLeft + 6.0F,
                  this.detailsPanelTop + 12.5F,
                  4.4F
               )
               .baselinePosition(TextBaselinePosition.CENTER)
               .color(Accent.getDeepTextColor((int)(210.0F * detailsAlpha)))
               .render(drawContext);
            float actionButtonWidth = (detailsHeight - 8.0F) / 3.0F;
            boolean isConnected = disconnect.isOfflineOrDisconnected();
            this.drawActionButton(drawContext, mouseX, mouseY, 3, isConnected ? "Подключить" : "Отключить", detailsLeft, this.getConnectionActionsTop(), actionButtonWidth, isConnected, detailsAlpha);
            this.drawActionButton(drawContext, mouseX, mouseY, 4, "Переподкл.", detailsLeft + actionButtonWidth + 4.0F, this.getConnectionActionsTop(), actionButtonWidth, false, detailsAlpha);
            this.drawActionButton(drawContext, mouseX, mouseY, 5, "Удалить", detailsLeft + (actionButtonWidth + 4.0F) * 2.0F, this.getConnectionActionsTop(), actionButtonWidth, false, detailsAlpha);
            this.drawActionButton(drawContext, mouseX, mouseY, 6, "Управление ботом", detailsLeft, this.getBotManagementButtonTop(), detailsHeight, true, detailsAlpha);
            RoundedGradientRectBuilder.at(detailsLeft, this.getSettingsRowsTop() - 4.5F - 0.375F, detailsHeight, 0.75F)
               .cornerRadius(0.0F)
               .color(Accent.getTextColorWithAlpha((int)(12.0F * detailsAlpha)))
               .render(drawContext);
            this.drawToggleRow(
               drawContext, "Авто-переподключение", 0, smoothstep(Math.clamp(this.autoReconnectToggleAnimation, 0.0F, 1.0F)), detailsLeft, detailsHeight, detailsAlpha
            );
            this.drawToggleRow(drawContext, "Писать всеми ботами", 1, smoothstep(Math.clamp(this.broadcastChatToggleAnimation, 0.0F, 1.0F)), detailsLeft, detailsHeight, detailsAlpha);
            this.drawToggleRow(drawContext, "Панель бота на экране", 2, SavedBots.getSessionRegistry().isPanelOpen() ? 1.0F : 0.0F, detailsLeft, detailsHeight, detailsAlpha);
            float tabsTop = this.getDetailsTabsTop();
            this.drawActionButton(drawContext, mouseX, mouseY, 7, "Чат", detailsLeft, tabsTop, this.getDetailsTabWidth(), !this.modulesTabSelected, detailsAlpha);
            String modulesTabLabel = "Модули (" + countEnabledModules(disconnect) + ")";
            this.drawActionButton(
               drawContext, mouseX, mouseY, 8, modulesTabLabel, detailsLeft + this.getDetailsTabWidth() + 4.0F, tabsTop, this.getDetailsTabWidth(), this.modulesTabSelected, detailsAlpha
            );
            float contentLeft = detailsLeft + this.getDetailsTabWidth() * 2.0F + 4.0F;
            float contentCenterX = contentLeft + (detailsLeft + detailsHeight - contentLeft) * 0.5F;
            float contentTop = tabsTop + 6.0F;
            float moduleListTop = this.getModuleListTop();
            float moduleListHeight = this.getChatHistoryHeight();
            RoundedGradientRectBuilder.at(detailsLeft, moduleListTop, detailsHeight, moduleListHeight)
               .cornerRadius(Accent.getInstance().getCornerRadius() * 0.5F)
               .color(RgbaColor.rgba(0, 0, 0, (int)(55.0F * detailsAlpha)))
               .render(drawContext);
            RoundedRectBuilder.createAt(detailsLeft, moduleListTop, detailsHeight, moduleListHeight)
               .cornerRadius(Accent.getInstance().getCornerRadius() * 0.5F)
               .borderWidth(0.6F)
               .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)(30.0F * detailsAlpha)))
               .alpha(detailsAlpha)
               .render(drawContext);
            if (this.modulesTabSelected) {
               this.drawModuleList(drawContext, mouseX, mouseY, disconnect, detailsLeft, moduleListTop, detailsHeight, moduleListHeight, detailsAlpha);
               this.chatMessageField.render(drawContext, detailsLeft, this.getChatInputTop(), detailsHeight, 13.0F, nowMs, detailsAlpha);
               this.drawToast(drawContext, contentCenterX, contentTop, nowMs, detailsAlpha);
            } else {
                List<String> chatLines = disconnect.getFormattedSessionHistory();
                int visibleChatLineCount = Math.max(1, (int)((moduleListHeight - 4.0F) / 7.0F));
                this.maxChatHistoryScrollOffset = Math.max(0, chatLines.size() - visibleChatLineCount);
               this.chatHistoryScrollOffset = Math.clamp(this.chatHistoryScrollOffset, 0.0F, this.maxChatHistoryScrollOffset);
                int chatEndIndex = chatLines.size() - (int)this.chatHistoryScrollOffset;
               int firstVisibleChatLineIndex = Math.max(0, chatEndIndex - visibleChatLineCount);
               drawContext.enableScissor(
                  PixelRounding.II1Il11l111II11IIl(detailsLeft),
                  PixelRounding.II1Il11l111II11IIl(moduleListTop),
                  PixelRounding.l1I1IlllIlI(detailsLeft + detailsHeight),
                  PixelRounding.l1I1IlllIlI(moduleListTop + moduleListHeight)
               );

               try {
                  if (chatLines.isEmpty()) {
                     GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Чат пуст", detailsLeft + detailsHeight * 0.5F, moduleListTop + moduleListHeight * 0.5F, 4.6F)
                        .centered()
                        .color(Accent.getDeepTextColor((int)(160.0F * detailsAlpha)))
                        .render(drawContext);
                  }

                  float firstChatLineY = moduleListTop + 2.0F + 3.5F + (visibleChatLineCount - (chatEndIndex - firstVisibleChatLineIndex)) * 7.0F;

                  for (int chatLineIndex = firstVisibleChatLineIndex; chatLineIndex < chatEndIndex; chatLineIndex++) {
                     String chatLine = chatLines.get(chatLineIndex);
                     boolean isPlayerChatLine = chatLine.contains("] > ");
                     GuiTextBuilder.createAt(
                           Medium.DEFAULT_FONT,
                            Medium.DEFAULT_FONT.truncateToWidth(chatLine, 4.2F, detailsHeight - 6.0F),
                           detailsLeft + 3.0F,
                           firstChatLineY,
                           4.2F
                        )
                        .baselinePosition(TextBaselinePosition.CENTER)
                        .color(isPlayerChatLine ? Accent.getLighterAccentColor((int)(240.0F * detailsAlpha)) : Accent.getDarkerTextColor((int)(225.0F * detailsAlpha)))
                        .render(drawContext);
                     firstChatLineY += 7.0F;
                  }
               } finally {
                  drawContext.disableScissor();
               }
// https://t.me/pasterplace

               this.chatMessageField.render(drawContext, detailsLeft, this.getChatInputTop(), detailsHeight, 13.0F, nowMs, detailsAlpha);
               this.drawToast(drawContext, contentCenterX, contentTop, nowMs, detailsAlpha);
            }
         }
      }
   }
// https://t.me/pasterplace

   private void drawToggleRow(DrawContext drawContext, String label, int settingIndex, float toggleProgress, float left, float panelWidth, float alpha) {
      float rowCenterY = this.getSettingRowCenterY(settingIndex);
      GuiTextBuilder.createAt(Medium.DEFAULT_FONT, label, left + 1.0F, rowCenterY, 4.8F)
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(Accent.getDarkerTextColor((int)(230.0F * alpha)))
         .render(drawContext);
      this.drawToggleSwitch(drawContext, toggleProgress, left + panelWidth - 13.0F, rowCenterY, alpha);
   }
// https://t.me/pasterplace

   private void drawModuleList(
      DrawContext drawContext, float mouseX, float mouseY, BotSession botSession, float left, float top, float width, float height, float alpha
   ) {
      List<BotModuleEntry> moduleEntries = botSession.getModuleProfile().II1Il11l111II11IIl();
      this.maxModuleListScrollOffset = Math.max(0.0F, moduleEntries.size() * 11.0F + 4.0F - height);
      this.moduleListScrollOffset = Math.clamp(this.moduleListScrollOffset, 0.0F, this.maxModuleListScrollOffset);
      drawContext.enableScissor(
         PixelRounding.II1Il11l111II11IIl(left),
         PixelRounding.II1Il11l111II11IIl(top),
         PixelRounding.l1I1IlllIlI(left + width),
         PixelRounding.l1I1IlllIlI(top + height)
      );

      try {
          for (int moduleIndex = 0; moduleIndex < moduleEntries.size(); moduleIndex++) {
             BotModuleEntry moduleEntry = moduleEntries.get(moduleIndex);
            float moduleRowTop = top + 2.0F + moduleIndex * 11.0F - this.moduleListScrollOffset;
            if (!(moduleRowTop + 11.0F < top) && !(moduleRowTop > top + height)) {
               float moduleRowCenterY = moduleRowTop + 5.5F;
                boolean moduleEnabled = moduleEntry.IllI1l1l1();
               if (containsPoint(mouseX, mouseY, left, moduleRowTop, width, 11.0F)) {
                  RoundedGradientRectBuilder.at(left + 1.0F, moduleRowTop, width - 2.0F, 11.0F)
                     .cornerRadius(2.0F)
                     .color(Accent.getAccentColorWithAlpha((int)(18.0F * alpha)))
                     .render(drawContext);
               }

                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, moduleEntry.II1Il11l111II11IIl(), left + 4.0F, moduleRowCenterY, 4.6F)
                  .baselinePosition(TextBaselinePosition.CENTER)
                  .color(Accent.getTextColorWithAlpha((int)(235.0F * alpha)))
                  .render(drawContext);
                String moduleDescription = Medium.DEFAULT_FONT.truncateToWidth(moduleEntry.l1I1IlllIlI(), 3.8F, width * 0.55F);
               float descriptionWidth = Medium.DEFAULT_FONT.measureWidth(moduleDescription, 3.8F);
               GuiTextBuilder.createAt(Medium.DEFAULT_FONT, moduleDescription, left + width - 13.0F - 8.0F - descriptionWidth, moduleRowCenterY, 3.8F)
                  .baselinePosition(TextBaselinePosition.CENTER)
                  .color(Accent.getDeepTextColor((int)(150.0F * alpha)))
                  .render(drawContext);
               this.drawToggleSwitch(drawContext, moduleEnabled ? 1.0F : 0.0F, left + width - 13.0F - 4.0F, moduleRowCenterY, alpha);
            }
         }
      } finally {
         drawContext.disableScissor();
      }
   }
// https://t.me/pasterplace

   private static int countEnabledModules(BotSession disconnect) {
      int enabledModuleCount = 0;

      for (BotModuleEntry moduleEntry : disconnect.getModuleProfile().II1Il11l111II11IIl()) {
         if (moduleEntry.IllI1l1l1()) {
            enabledModuleCount++;
         }
      }

      return enabledModuleCount;
   }
// https://t.me/pasterplace

   private void drawToast(DrawContext drawContext, float toastLeft, float toastTop, long nowMs, float panelAlpha) {
      if (!this.toastMessage.isEmpty()) {
         float toastProgress = (float)(nowMs - this.toastShownAtMs) / 2500.0F;
         if (toastProgress >= 1.0F) {
            this.toastMessage = "";
         } else {
            float toastAlpha = toastProgress < 0.8F ? 1.0F : 1.0F - (toastProgress - 0.8F) / 0.2F;
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, this.toastMessage, toastLeft, toastTop, 4.4F)
               .centered()
               .color(Accent.getLighterAccentColor((int)(230.0F * panelAlpha * toastAlpha)))
               .render(drawContext);
         }
      }
   }
// https://t.me/pasterplace

   private void showToast(String message) {
      this.toastMessage = message;
      this.toastShownAtMs = System.currentTimeMillis();
   }
// https://t.me/pasterplace

   private void drawActionButton(
      DrawContext drawContext,
      float mouseX,
      float mouseY,
      int actionIndex,
      String label,
      float left,
      float top,
      float width,
      boolean selected,
      float alpha
   ) {
      float hoverProgress = this.updateHoverAnimation(this.buttonHoverAnimations, actionIndex, containsPoint(mouseX, mouseY, left, top, width, 12.0F));
      int backgroundColor = selected
         ? Accent.getAccentColorWithAlpha((int)(95.0F * alpha)).toArgb()
         : Accent.getTextColorWithAlpha((int)(14.0F * alpha)).toArgb();
      int labelColor = selected
         ? Accent.getAccentColorWithAlpha((int)(160.0F * alpha)).toArgb()
         : Accent.getAccentColorWithAlpha((int)(55.0F * alpha)).toArgb();
      RoundedGradientRectBuilder.at(left, top, width, 12.0F)
         .cornerRadius(Accent.getInstance().getCornerRadius() * 0.5F)
         .color(ColorUtils.interpolateColor(backgroundColor, labelColor, hoverProgress))
         .render(drawContext);
      GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT,
            Medium.DEFAULT_FONT.truncateToWidth(label, 4.6F, width - 4.0F),
            left + width * 0.5F,
            top + 6.0F,
            4.6F
         )
         .centered()
         .color(
            ColorUtils.interpolateColor(
               Accent.getDarkerTextColor((int)(220.0F * alpha)).toArgb(),
               Accent.getTextColorWithAlpha((int)(255.0F * alpha)).toArgb(),
               Math.max(hoverProgress, selected ? 1.0F : 0.0F)
            )
         )
         .render(drawContext);
   }
// https://t.me/pasterplace

   private static int connectionStatusColor(BotSession.BotConnectionStatus status, float alpha) {
      return switch (status) {
         case ONLINE -> RgbaColor.rgba(90, 220, 120, (int)(255.0F * alpha)).toArgb();
         case OFFLINE, DISCONNECTED -> RgbaColor.rgba(120, 125, 140, (int)(220.0F * alpha)).toArgb();
         default -> RgbaColor.rgba(240, 200, 80, (int)(255.0F * alpha)).toArgb();
      };
   }
// https://t.me/pasterplace

   private void drawToggleSwitch(DrawContext drawContext, float value, float left, float centerY, float alpha) {
      float trackTop = centerY - 3.5F;
      RoundedGradientRectBuilder.at(left, trackTop, 13.0F, 7.0F)
         .cornerRadius(3.5F)
         .color(
            ColorUtils.interpolateColor(
               RgbaColor.rgba(0, 0, 0, (int)(80.0F * alpha)).toArgb(),
               Accent.getAccentColorWithAlpha((int)(218.0F * alpha)).toArgb(),
               value
            )
         )
         .render(drawContext);
      float knobInset = 1.1F;
      float knobSize = 7.0F - knobInset * 2.0F;
      float trackLeft = left + knobInset;
      float trackRight = left + 13.0F - knobInset - knobSize;
      RoundedGradientRectBuilder.at(trackLeft + (trackRight - trackLeft) * value, trackTop + knobInset, knobSize, knobSize)
         .cornerRadius(knobSize * 0.5F)
         .color(RgbaColor.rgba(255, 255, 255, (int)(255.0F * alpha)))
         .render(drawContext);
   }
// https://t.me/pasterplace

   public boolean mouseClicked(float mouseX, float mouseY, int mouseButton) {
      if (mouseButton != 0) {
         return false;
      }

      SavedBots.SavedBotSessionRegistry botRegistry = SavedBots.getSessionRegistry();
      float botListLeft = this.panelLeft + 5.0F;
      float searchFieldWidth = this.getBotNameSearchWidth();
      this.botNameSearchField.setFocused(containsPoint(mouseX, mouseY, botListLeft, this.getSearchFieldTop(), searchFieldWidth, 13.0F));
      this.chatMessageField
         .setFocused(this.selectedBot != null && containsPoint(mouseX, mouseY, this.getDetailsPanelLeft(), this.getChatInputTop(), this.getDetailsPanelHeight(), 13.0F));
      if (!this.botNameSearchField.isFocused() && !this.chatMessageField.isFocused()) {
         if (containsPoint(mouseX, mouseY, botListLeft + searchFieldWidth + 4.0F, this.getSearchFieldTop(), 13.0F, 13.0F)) {
            this.addBotFromSearch(this.botNameSearchField.getText());
            return true;
         }

         float bulkActionButtonWidth = this.getBulkActionButtonWidth();
         if (containsPoint(mouseX, mouseY, botListLeft, this.getBulkActionsTop(), bulkActionButtonWidth, 12.0F)) {
            if (botRegistry.getCurrentServerAddress() == null) {
               this.showToast("Сначала зайдите на сервер");
            } else {
               this.showToast("Подключаю: " + botRegistry.countOfflineSessionsAssignedToCurrentServer());
            }

            return true;
         } else {
            if (containsPoint(mouseX, mouseY, botListLeft + bulkActionButtonWidth + 4.0F, this.getBulkActionsTop(), bulkActionButtonWidth, 12.0F)) {
               botRegistry.disconnectAllSessions();
               this.showToast("Все боты отключены");
               return true;
            }

            float botListTop = this.getBotListTop();
            if (mouseY >= botListTop
               && mouseY <= botListTop + this.getBotListVisibleHeight()
               && mouseX >= this.panelLeft
               && mouseX <= this.panelLeft + this.panelWidth) {
                List<BotSession> botSessions = botRegistry.getSessions();
               int botIndex = (int)((mouseY - botListTop + this.botListScrollOffset) / 17.5F);
                if (botIndex >= 0 && botIndex < botSessions.size()) {
                   BotSession clickedBot = botSessions.get(botIndex);
                   this.selectedBot = this.selectedBot == clickedBot ? null : clickedBot;
                  this.chatHistoryScrollOffset = 0.0F;
                  return true;
               }
            }

            if (this.selectedBot == null) {
               return false;
            }

            float detailsPanelLeft = this.getDetailsPanelLeft();
            float detailsPanelHeight = this.getDetailsPanelHeight();
            float actionButtonWidth = (detailsPanelHeight - 8.0F) / 3.0F;
            if (containsPoint(mouseX, mouseY, detailsPanelLeft, this.getConnectionActionsTop(), actionButtonWidth, 12.0F)) {
               if (this.selectedBot.isOfflineOrDisconnected()) {
                  if (!botRegistry.assignCurrentServerToSession(this.selectedBot)) {
                     this.showToast("Сначала зайдите на сервер");
                  }
               } else {
                  this.selectedBot.disconnectFromServer();
               }

               return true;
            } else if (containsPoint(mouseX, mouseY, detailsPanelLeft + actionButtonWidth + 4.0F, this.getConnectionActionsTop(), actionButtonWidth, 12.0F)) {
               this.selectedBot.disconnectFromServer();
               if (!botRegistry.assignCurrentServerToSession(this.selectedBot)) {
                  this.showToast("Сначала зайдите на сервер");
               }

               return true;
            } else if (containsPoint(mouseX, mouseY, detailsPanelLeft, this.getBotManagementButtonTop(), detailsPanelHeight, 12.0F)) {
               if (this.selectedBot.getConnectionStatus() != BotSession.BotConnectionStatus.ONLINE) {
                  this.showToast("Бот должен быть в игре");
               } else {
                   String selectedBotUsername = this.selectedBot.getUsername();
                  Search.getInstance().close();
                  MinecraftClient.getInstance().setScreen(null);
                   SavedBots.switchToSession(selectedBotUsername);
               }

               return true;
            } else {
               if (containsPoint(mouseX, mouseY, detailsPanelLeft + (actionButtonWidth + 4.0F) * 2.0F, this.getConnectionActionsTop(), actionButtonWidth, 12.0F)) {
                  botRegistry.removeSession(this.selectedBot);
                  this.showToast("Бот " + this.selectedBot.getUsername() + " удалён");
                  this.selectedBot = null;
                  return true;
               }
// https://t.me/pasterplace

               if (containsPoint(mouseX, mouseY, detailsPanelLeft, this.getDetailsTabsTop(), this.getDetailsTabWidth(), 12.0F)) {
                  this.modulesTabSelected = false;
                  return true;
               }

               if (containsPoint(mouseX, mouseY, detailsPanelLeft + this.getDetailsTabWidth() + 4.0F, this.getDetailsTabsTop(), this.getDetailsTabWidth(), 12.0F)) {
                  this.modulesTabSelected = true;
                  return true;
               }

               if (this.modulesTabSelected && containsPoint(mouseX, mouseY, detailsPanelLeft, this.getModuleListTop(), detailsPanelHeight, this.getChatHistoryHeight())) {
                   List<BotModuleEntry> moduleEntries = this.selectedBot.getModuleProfile().II1Il11l111II11IIl();
                  int moduleIndex = (int)((mouseY - this.getModuleListTop() - 2.0F + this.moduleListScrollOffset) / 11.0F);
                   if (moduleIndex >= 0 && moduleIndex < moduleEntries.size()) {
                      moduleEntries.get(moduleIndex).l1lll11l1l();
                  }
// https://t.me/pasterplace

                  return true;
               } else if (containsPoint(mouseX, mouseY, detailsPanelLeft, this.getSettingsRowsTop(), detailsPanelHeight, 11.0F)) {
                  this.selectedBot.setAutoReconnectEnabled(!this.selectedBot.isAutoReconnectEnabled());
                  return true;
               } else if (containsPoint(mouseX, mouseY, detailsPanelLeft, this.getSettingsRowsTop() + 11.0F, detailsPanelHeight, 11.0F)) {
                  this.sendMessagesToAllBots = !this.sendMessagesToAllBots;
                  return true;
               } else if (containsPoint(mouseX, mouseY, detailsPanelLeft, this.getSettingsRowsTop() + 22.0F, detailsPanelHeight, 11.0F)) {
                  SavedBots.getSessionRegistry().setPanelOpen(!SavedBots.getSessionRegistry().isPanelOpen());
                  return true;
               } else {
                  return false;
               }
            }
         }
      } else {
         return true;
      }
   }
// https://t.me/pasterplace

   public boolean mouseScrolled(float mouseX, float mouseY, double scrollAmount) {
      if (containsPoint(mouseX, mouseY, this.panelLeft, this.getBotListTop(), this.panelWidth, this.getBotListVisibleHeight())) {
         this.targetBotListScrollOffset = Math.clamp(this.targetBotListScrollOffset - (float)scrollAmount * 18.0F, 0.0F, this.maxBotListScrollOffset);
         return true;
      }

      if (this.selectedBot != null && containsPoint(mouseX, mouseY, this.getDetailsPanelLeft(), this.getModuleListTop(), this.getDetailsPanelHeight(), this.getChatHistoryHeight())) {
         if (this.modulesTabSelected) {
            this.moduleListScrollOffset = Math.clamp(this.moduleListScrollOffset - (float)scrollAmount * 11.0F, 0.0F, this.maxModuleListScrollOffset);
         } else {
            this.chatHistoryScrollOffset = Math.clamp(this.chatHistoryScrollOffset + (float)scrollAmount * 2.0F, 0.0F, this.maxChatHistoryScrollOffset);
         }

         return true;
      } else {
         return false;
      }
   }
// https://t.me/pasterplace

   public boolean keyPressed(KeyInput keyInput) {
      if (this.botNameSearchField.isFocused()) {
         return this.botNameSearchField.keyPressed(keyInput);
      } else {
         return this.chatMessageField.isFocused() ? this.chatMessageField.keyPressed(keyInput) : false;
      }
   }
// https://t.me/pasterplace

   public boolean charTyped(CharInput charInput) {
      if (this.botNameSearchField.isFocused()) {
         return this.botNameSearchField.charTyped(charInput);
      } else {
         return this.chatMessageField.isFocused() ? this.chatMessageField.charTyped(charInput) : false;
      }
   }
// https://t.me/pasterplace

   public boolean hasFocusedInput() {
      return this.botNameSearchField.isFocused() || this.chatMessageField.isFocused();
   }
// https://t.me/pasterplace

   public void clearInputFocus() {
      this.botNameSearchField.setFocused(false);
      this.chatMessageField.setFocused(false);
   }
// https://t.me/pasterplace

   private void addBotFromSearch(String enteredUsername) {
      String username = enteredUsername.trim();
      if (!SavedBots.isValidUsername(username)) {
         this.showToast("Ник: 3-16 символов, латиница, цифры, _");
      } else {
         BotSession disconnect = SavedBots.getSessionRegistry().createOfflineSessionIfAbsent(username);
         if (disconnect == null) {
            this.showToast("Такой бот уже есть");
         } else {
            this.botNameSearchField.setText("");
            this.selectedBot = disconnect;
            this.showToast("Бот " + username + " добавлен");
         }
      }
   }
// https://t.me/pasterplace

   private void sendChatMessage(String enteredMessage) {
      String message = enteredMessage.trim();
      if (!message.isEmpty()) {
         if (this.sendMessagesToAllBots) {
            int sentBotCount = SavedBots.getSessionRegistry().sendChatToAllSessions(message);
            this.showToast(sentBotCount == 0 ? "Нет ботов онлайн" : "Отправлено ботам: " + sentBotCount);
            if (sentBotCount > 0) {
               this.chatMessageField.setText("");
            }
         } else if (this.selectedBot != null && this.selectedBot.sendChatMessage(message)) {
            this.chatHistoryScrollOffset = 0.0F;
            this.chatMessageField.setText("");
         } else {
            this.showToast("Бот не в игре");
         }
      }
   }
// https://t.me/pasterplace

   private float updateHoverAnimation(float[] animationValues, int animationIndex, boolean hovered) {
      float targetValue = hovered ? 1.0F : 0.0F;
      float currentValue = animationValues[animationIndex];
      currentValue += (targetValue - currentValue) * (1.0F - (float)Math.exp(-6.0F * this.frameDeltaSeconds));
      if (targetValue == 0.0F && currentValue < 0.001F) {
         currentValue = 0.0F;
      } else if (targetValue == 1.0F && currentValue > 0.999F) {
         currentValue = 1.0F;
      }

      animationValues[animationIndex] = currentValue;
      return smoothstep(currentValue);
   }
// https://t.me/pasterplace

   private static float smoothstep(float value) {
      return value * value * (3.0F - 2.0F * value);
   }
// https://t.me/pasterplace

   private static float easeOutCubic(float value) {
      return 1.0F - (float)Math.pow(1.0F - Math.clamp(value, 0.0F, 1.0F), 3.0);
   }
// https://t.me/pasterplace

   private static boolean containsPoint(float mouseX, float mouseY, float left, float top, float width, float height) {
      return mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
   }
}

