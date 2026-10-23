package dev.daamky;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BotManagementScreen extends Screen {
    private static final String TELEGRAM_ATTRIBUTION_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float LEFT_PANE_WIDTH = 240.0F;
   private static final float RIGHT_PANE_WIDTH = 240.0F;
   private static final float PANEL_WIDTH = 480.0F;
   private static final float PANEL_HEIGHT = 320.0F;
   private static final float PANEL_CORNER_RADIUS = 10.0F;
   private static final long TRANSITION_DURATION_MS = 180L;
   private static final int PANEL_SURFACE_COLOR = RgbaColor.rgba(14, 14, 16, 122).toArgb();
   private static final int HEADER_SURFACE_COLOR = RgbaColor.rgba(14, 14, 16, 184).toArgb();
   private static final float BASE_PADDING = 4.0F;
   private static final float CONTENT_WIDTH = 232.0F;
   private static final float CONTROL_HEIGHT = 23.0F;
   private static final float CONTROL_CORNER_RADIUS = 6.0F;
   private static final int BUTTON_SURFACE_COLOR = RgbaColor.rgba(14, 14, 16, 122).toArgb();
   private static final float SESSION_ROW_HEIGHT = 23.0F;
   private static final int SUBTLE_SURFACE_COLOR = RgbaColor.rgba(14, 14, 16, 61).toArgb();
   private static final float SMALL_GAP = 4.0F;
   private static final float SEARCH_FIELD_WIDTH = 114.0F;
   private static final float CONTROL_GAP = 4.0F;
   private static final float HEADER_TOP_OFFSET = 31.0F;
   private static final float SESSION_ROW_WIDTH = 227.0F;
   private static final float SESSION_LIST_TOP_OFFSET = 58.0F;
   private static final float MATRIX_SCALE_FACTOR = 2.0F;
   private static final float SESSION_ROW_STEP = 25.0F;
   private static final float SESSION_LIST_VIEWPORT_HEIGHT = 231.0F;
   private static final float SESSION_ROW_ADVANCE = 25.0F;
   private static final float SCROLLBAR_WIDTH = 4.0F;
   private static final float HAIRLINE_WIDTH = 1.0F;
   private static final float MIN_SCROLLBAR_HANDLE_HEIGHT = 16.0F;
   private static final int SCROLLBAR_TRACK_COLOR = RgbaColor.rgba(255, 255, 255, 5).toArgb();
   private static final int SCROLLBAR_HANDLE_COLOR = RgbaColor.rgba(255, 255, 255, 10).toArgb();
   private static final float SESSION_ACTION_WIDTH = 64.0F;
   private static final float SERVER_ICON_SIZE = 7.0F;
   private static final float BORDER_THICKNESS = 4.0F;
   private static final int SESSION_ROW_SURFACE_COLOR = RgbaColor.rgba(14, 14, 16, 61).toArgb();
   private static final float AVATAR_TEXTURE_SIZE = 15.0F;
   private static final float DETAIL_PANEL_WIDTH = 232.0F;
   private static final float SESSION_ACTION_AREA_WIDTH = 157.0F;
   private static final float ACTION_BUTTON_HEIGHT = 23.0F;
   private static final float DETAIL_PANEL_X_OFFSET = 319.0F;
   private static final float CHAT_INPUT_GAP = 12.0F;
   private static final float DETAIL_CONTENT_TOP_OFFSET = 39.0F;
   private static final float DETAIL_CONTENT_WIDTH = 232.0F;
   private static final float PICTURE_IN_PICTURE_HEIGHT = 130.5F;
   private static final float DETAIL_CONTENT_X_OFFSET = 244.0F;
   private static final float DETAIL_SECTION_GAP = 4.0F;
   private static final float DISCONNECT_HISTORY_HEIGHT = 107.5F;
   private static final float MESSAGE_ACTION_GAP = 4.0F;
   private static final float SCROLLBAR_THICKNESS = 1.0F;
   private static final float SEND_BUTTON_LABEL_SIZE = 8.0F;
   private static final float DISCONNECT_HISTORY_SCROLL_STEP = 14.0F;
   private static final long DISCONNECT_FADE_DURATION_MS = 260L;
   private static final float HISTORY_TEXT_GAP = 4.0F;
   private static final float MESSAGE_INPUT_HEIGHT = 23.0F;
   private static final float SEND_BUTTON_WIDTH = 48.0F;
   private static final int PRIMARY_TEXT_COLOR = RgbaColor.rgba(255, 255, 255, 184).toArgb();
   private static final int SECONDARY_TEXT_COLOR = RgbaColor.rgba(255, 255, 255, 122).toArgb();
   private static final int OPAQUE_WHITE = -1;
   private static final float SMALL_TEXT_SIZE = 6.0F;
   private static final float DETAIL_TEXT_SIZE = 5.0F;
   private static final float ROLE_TEXT_SIZE = 5.0F;
   private static final float ICON_LABEL_SIZE = 7.0F;
   private static final int DISCONNECT_ACTION_COLOR = RgbaColor.rgba(255, 99, 99, 255).toArgb();
   private static final float ROLE_BADGE_ICON_SIZE = 11.0F;
   private static final float ROLE_BADGE_VERTICAL_PADDING = 4.0F;
   private static final float SESSION_LABEL_TEXT_SIZE = 6.0F;
   private static final int ROLE_BADGE_SURFACE_COLOR = RgbaColor.rgba(166, 178, 255, 46).toArgb();
   private static final float ROLE_BADGE_MAX_WIDTH = 26.0F;
   private static final float ROLE_BADGE_MIN_WIDTH = 44.0F;
   private static final int REMOVE_SESSION_COLOR = RgbaColor.rgba(255, 64, 64, 255).toArgb();
   private static final float ROLE_BADGE_HORIZONTAL_PADDING = 4.0F;
   private static final float ROLE_BADGE_CORNER_RADIUS = 3.0F;
   private static final int ROLE_BADGE_TEXT_COLOR = RgbaColor.rgba(166, 178, 255, 255).toArgb();
   private static final String BRAND_GLYPH = "G";
   private final long screenOpenedAt = System.currentTimeMillis();
   private long screenClosingStartedAt = -1L;
   private final Map<String, BotManagementScreen.SessionRowAnimation> rowAnimationsByUsername = new HashMap<>();
   private final Map<BotSessionLogEntry, long[]> disconnectAnimationStartTimes = new IdentityHashMap<>();
   private final Map<String, List<String>> wrappedDisconnectLinesByMessage = new HashMap<>();
   private BotSessionInfo lastRenderedBotSession;
   private SavedBotSession selectedBotSession;
   private float disconnectHistoryScrollOffset;
   private float sessionListScrollOffset;
   private final SearchTextField chatMessageField = new SearchTextField("Сообщение...", 256, text -> this.sendChatMessage());
   private final SearchTextField serverAddressField = new SearchTextField("Подключиться к...", 128, text -> this.connectSelectedSession());
   private final SearchTextField newSessionUsernameField = new SearchTextField("Добавить...", 16, text -> this.addSavedSession());
   private final SearchTextField sessionSearchField = new SearchTextField("Поиск...", 32, text -> {});
   private final SearchTextField roleEditField = new SearchTextField("Роль...", 16, text -> this.saveRoleEdit());
   private String editingRoleUsername;
   private float connectButtonWidth;
   private float manageButtonWidth;
   private float mouseX;
   private float mouseY;
   private static final Map<String, String> serverHostLabelCache = new HashMap<>();
// https://t.me/pasterplace

   public BotManagementScreen() {
      super(Text.literal("Bots"));
   }
// https://t.me/pasterplace

   protected void init() {
      super.init();
      this.connectButtonWidth = Medium.DEFAULT_FONT.measureWidth("Подключить", 6.0F) + 30.0F;
      this.manageButtonWidth = Medium.DEFAULT_FONT.measureWidth("Управлять", 6.0F) + 18.0F;
      if (this.selectedBotSession == null) {
         List<SavedBotSession> savedSessions = SavedBots.getSessionRepository().getAllSessions();
         if (!savedSessions.isEmpty()) {
            this.selectedBotSession = savedSessions.get(0);
         }
      }
   }
// https://t.me/pasterplace

   private float getOpenAnimationProgress() {
      long nowMillis = System.currentTimeMillis();
      float progress;
      if (this.screenClosingStartedAt >= 0L) {
         progress = 1.0F - Math.clamp((float)(nowMillis - this.screenClosingStartedAt) / 180.0F, 0.0F, 1.0F);
      } else {
         progress = Math.clamp((float)(nowMillis - this.screenOpenedAt) / 180.0F, 0.0F, 1.0F);
      }

      return 1.0F - (float)Math.pow(1.0F - progress, 3.0);
   }
// https://t.me/pasterplace

   private boolean isTransitionActive() {
      return this.screenClosingStartedAt >= 0L || System.currentTimeMillis() - this.screenOpenedAt < 180L;
   }
// https://t.me/pasterplace

   private static float getViewportWidth() {
      return GuiEventRenderer.getScreenWidth();
   }
// https://t.me/pasterplace

   private static float getViewportHeight() {
      return GuiEventRenderer.getScreenHeight();
   }
// https://t.me/pasterplace

   private static float getPanelLeft() {
      return (getViewportWidth() - 480.0F) / 2.0F;
   }
// https://t.me/pasterplace

   private static float getPanelTop() {
      return (getViewportHeight() - 320.0F) / 2.0F;
   }
// https://t.me/pasterplace

   private static float scaleMouseCoordinate(double windowCoordinate) {
      return (float)windowCoordinate * MinecraftClient.getInstance().getWindow().getScaleFactor() / 2.0F;
   }
// https://t.me/pasterplace

   public void render(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
      MinecraftClient client = MinecraftClient.getInstance();
      int windowScaleFactor = client.getWindow().getScaleFactor();
      if (windowScaleFactor > 0) {
         float openProgress = this.getOpenAnimationProgress();
         if (this.screenClosingStartedAt >= 0L && openProgress <= 0.001F) {
            client.setScreen(null);
         } else {
            this.mouseX = scaleMouseCoordinate((double)mouseX);
            this.mouseY = scaleMouseCoordinate((double)mouseY);
            float panelLeft = getPanelLeft();
            float panelTop = getPanelTop();
            float transformCenterX = panelLeft + 240.0F;
            float transformCenterY = panelTop + 160.0F;
            float contentScale = 0.94F + 0.06F * openProgress;
            drawContext.getMatrices().pushMatrix();
            drawContext.getMatrices().scale(2.0F / windowScaleFactor);
            drawContext.getMatrices().translate(transformCenterX, transformCenterY);
            drawContext.getMatrices().scale(contentScale, contentScale);
            drawContext.getMatrices().translate(-transformCenterX, -transformCenterY);
            GuiRenderStateBatcher.beginBatching(drawContext);

            try {
               this.renderPanelContents(drawContext, panelLeft, panelTop, openProgress);
            } finally {
               GuiRenderStateBatcher.endBatching();
               drawContext.getMatrices().popMatrix();
            }
         }
      }
   }
// https://t.me/pasterplace

   private static int applyAlpha(int colorArgb, float alphaMultiplier) {
      int scaledAlpha = Math.round((colorArgb >>> 24 & 0xFF) * Math.clamp(alphaMultiplier, 0.0F, 1.0F));
      return scaledAlpha << 24 | colorArgb & 16777215;
   }
// https://t.me/pasterplace

   private static void drawRoundedRect(DrawContext drawContext, float x, float y, float width, float height, float cornerRadius, int colorArgb) {
      RoundedGradientRectBuilder.at(x, y, width, height).cornerRadius(cornerRadius).color(colorArgb).render(drawContext);
   }
// https://t.me/pasterplace

   private static void drawCenteredText(DrawContext drawContext, String label, float x, float y, float width, float fontSize, int colorArgb) {
      GuiTextBuilder.createAt(Medium.DEFAULT_FONT, label, x, y + width / 2.0F, fontSize)
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(colorArgb)
         .render(drawContext);
   }
// https://t.me/pasterplace

   private static void drawCenteredSmallText(DrawContext drawContext, String label, float x, float y, float width, float fontSize, int colorArgb) {
      GuiTextBuilder.createAt(Medium.ICON_FONT, label, x, y + width / 2.0F, fontSize)
         .baselinePosition(TextBaselinePosition.CENTER)
         .color(colorArgb)
         .render(drawContext);
   }
// https://t.me/pasterplace

   private static float measureTextWidth(String textToMeasure, float fontSize) {
      return Medium.DEFAULT_FONT.measureWidth(textToMeasure, fontSize);
   }
// https://t.me/pasterplace

   private void renderPanelContents(DrawContext drawContext, float panelX, float panelY, float animationAlpha) {
      BotServerIcon.tick();
      RoundedOutlineBuilder.at(panelX, panelY + 16.0F, 480.0F, 320.0F)
         .cornerRadius(10.0F)
         .color(RgbaColor.rgba(12, 12, 12, (int)(20.0F * animationAlpha)))
         .render(drawContext);
      KawaseBlurBuilder.create(panelX, panelY, 480.0F, 320.0F)
         .cornerRadius(10.0F)
         .blurMode(KawaseGaussianBlurMode.KAWASE)
         .blurPasses((int)Accent.getInstance().getBlurRadius())
         .opacity(animationAlpha)
         .render(drawContext);
      RoundedGradientRectBuilder.at(panelX, panelY, 240.0F, 320.0F)
         .cornerRadii(10.0F, 0.0F, 0.0F, 10.0F)
         .color(applyAlpha(PANEL_SURFACE_COLOR, animationAlpha))
         .render(drawContext);
      RoundedGradientRectBuilder.at(panelX + 240.0F, panelY, 240.0F, 320.0F)
         .cornerRadii(0.0F, 10.0F, 10.0F, 0.0F)
         .color(applyAlpha(HEADER_SURFACE_COLOR, animationAlpha))
         .render(drawContext);
      drawRoundedRect(drawContext, panelX + 4.0F, panelY + 4.0F, 232.0F, 23.0F, 6.0F, applyAlpha(BUTTON_SURFACE_COLOR, animationAlpha));
      float newSessionInputY = panelY + 31.0F;
      float newSessionInputX = panelX + 4.0F;
      long frameTimeMillis = System.currentTimeMillis();
      this.newSessionUsernameField.render(drawContext, newSessionInputX, newSessionInputY, 89.0F, 23.0F, frameTimeMillis, animationAlpha);
      float addSessionButtonX = newSessionInputX + 114.0F - 23.0F;
      drawRoundedRect(drawContext, addSessionButtonX, newSessionInputY, 23.0F, 23.0F, 6.0F, applyAlpha(SUBTLE_SURFACE_COLOR, animationAlpha));
      float addSymbolWidth = measureTextWidth("+", 9.0F);
      drawCenteredText(drawContext, "+", addSessionButtonX + (23.0F - addSymbolWidth) / 2.0F, newSessionInputY, 23.0F, 9.0F, applyAlpha(PRIMARY_TEXT_COLOR, animationAlpha));
      float searchFieldX = newSessionInputX + 114.0F + 4.0F;
      this.sessionSearchField.render(drawContext, searchFieldX, newSessionInputY, 114.0F, 23.0F, frameTimeMillis, animationAlpha);
      List<SavedBotSession> savedSessions = SavedBots.getSessionRepository().getAllSessions();
      this.rowAnimationsByUsername.keySet().removeIf(username -> savedSessions.stream().noneMatch(session -> session.getUsername().equals(username)));
      List<SavedBotSession> filteredSessions = this.filterSessions(savedSessions);
      float sessionListTop = panelY + 58.0F;
      float sessionListBottom = sessionListTop + 231.0F;
      this.sessionListScrollOffset = Math.max(0.0F, Math.min(this.sessionListScrollOffset, getSessionListMaxScroll(filteredSessions.size())));
      drawContext.enableScissor(
         PixelRounding.II1Il11l111II11IIl(panelX + 4.0F),
         PixelRounding.II1Il11l111II11IIl(sessionListTop),
         PixelRounding.l1I1IlllIlI(panelX + 4.0F + 227.0F),
         PixelRounding.l1I1IlllIlI(sessionListBottom)
      );

      try {
         for (int sessionRowIndex = 0; sessionRowIndex < filteredSessions.size(); sessionRowIndex++) {
            this.renderSavedBotRow(drawContext, filteredSessions.get(sessionRowIndex), sessionRowIndex, panelX, sessionListTop, sessionListBottom, animationAlpha, frameTimeMillis);
         }
      } finally {
         drawContext.disableScissor();
      }

      float scrollbarX = panelX + 4.0F + 227.0F + 4.0F;
      drawRoundedRect(drawContext, scrollbarX, sessionListTop, 1.0F, 231.0F, 0.5F, applyAlpha(SCROLLBAR_TRACK_COLOR, animationAlpha));
      float sessionListContentHeight = getSessionListContentHeight(filteredSessions.size());
      float scrollbarThumbHeight = sessionListContentHeight <= 231.0F ? 231.0F : Math.max(16.0F, 53361.0F / sessionListContentHeight);
      float maxSessionListScroll = getSessionListMaxScroll(filteredSessions.size());
      float scrollbarThumbY = sessionListTop + (maxSessionListScroll > 0.0F ? this.sessionListScrollOffset / maxSessionListScroll : 0.0F) * (231.0F - scrollbarThumbHeight);
      drawRoundedRect(drawContext, scrollbarX, scrollbarThumbY, 1.0F, scrollbarThumbHeight, 0.5F, applyAlpha(SCROLLBAR_HANDLE_COLOR, animationAlpha));
      float transferActionY = panelY + 58.0F + 231.0F + 4.0F;
      float transferActionX = panelX + 4.0F;
      drawRoundedRect(drawContext, transferActionX, transferActionY, 232.0F, 23.0F, 6.0F, applyAlpha(SUBTLE_SURFACE_COLOR, animationAlpha));
      String transferAllLabel = "Перевести все монеты" + (SavedBots.getPayoutRecipient().isEmpty() ? "" : " → " + SavedBots.getPayoutRecipient());
      drawCenteredText(
         drawContext, transferAllLabel, transferActionX + (232.0F - measureTextWidth(transferAllLabel, 6.0F)) / 2.0F, transferActionY, 23.0F, 6.0F, applyAlpha(PRIMARY_TEXT_COLOR, animationAlpha)
      );
      boolean selectedSessionOnline = this.selectedBotSession != null && this.selectedBotSession.isOnline();
      float rightPanelButtonX = panelX + 319.0F;
      float rightPanelButtonY = panelY + 4.0F;
      drawRoundedRect(drawContext, panelX + 240.0F + 4.0F, panelY + 4.0F, 232.0F, 23.0F, 6.0F, applyAlpha(SESSION_ROW_SURFACE_COLOR, animationAlpha));
      drawRoundedRect(drawContext, panelX + 240.0F + 4.0F, panelY + 4.0F, 23.0F, 23.0F, 6.0F, applyAlpha(SESSION_ROW_SURFACE_COLOR, animationAlpha));
      if (this.selectedBotSession != null) {
         Identifier identifier = BotAvatars.getAvatarTexture(this.selectedBotSession.getUsername());
         if (identifier != null) {
            float avatarX = panelX + 240.0F + 4.0F + 4.0F;
            float avatarY = panelY + 4.0F + 4.0F;
            TextureQuadBuilder.at(identifier, avatarX, avatarY, 15.0F, 15.0F).cornerRadius(3.0F).opacity(animationAlpha).render(drawContext);
         }
// https://t.me/pasterplace

         float selectedUsernameX = panelX + 240.0F + 4.0F + 23.0F + 8.0F;
         drawCenteredText(drawContext, this.selectedBotSession.getUsername(), selectedUsernameX, panelY + 4.0F, 23.0F, 6.0F, applyAlpha(-1, animationAlpha));
      }

      float botLabelWidth = measureTextWidth("Bot", 6.0F);
      float brandGlyphWidth = Medium.ICON_FONT.measureWidth("G", 7.0F);
      float brandGlyphX = panelX + 4.0F + (232.0F - brandGlyphWidth - 3.0F - botLabelWidth) / 2.0F;
      drawCenteredSmallText(drawContext, "G", brandGlyphX, panelY + 4.0F, 23.0F, 7.0F, applyAlpha(ROLE_BADGE_TEXT_COLOR, animationAlpha));
      drawCenteredText(drawContext, "Bot", brandGlyphX + brandGlyphWidth + 3.0F, panelY + 4.0F, 23.0F, 6.0F, applyAlpha(-1, animationAlpha));
      if (selectedSessionOnline) {
         this.serverAddressField.setFocused(false);
         float manageButtonX = rightPanelButtonX + 157.0F - this.manageButtonWidth;
         drawRoundedRect(drawContext, manageButtonX, rightPanelButtonY, this.manageButtonWidth, 23.0F, 6.0F, applyAlpha(BUTTON_SURFACE_COLOR, animationAlpha));
         String manageButtonLabel = "Управлять";
         drawCenteredText(
            drawContext,
            manageButtonLabel,
            manageButtonX + (this.manageButtonWidth - measureTextWidth(manageButtonLabel, 6.0F)) / 2.0F,
            rightPanelButtonY,
            23.0F,
            6.0F,
            applyAlpha(PRIMARY_TEXT_COLOR, animationAlpha)
         );
      } else {
         float connectButtonX = rightPanelButtonX + 157.0F - this.connectButtonWidth;
         this.serverAddressField.render(drawContext, rightPanelButtonX + 15.0F, rightPanelButtonY, 157.0F - this.connectButtonWidth - 17.0F, 23.0F, frameTimeMillis, animationAlpha);
         drawRoundedRect(drawContext, connectButtonX, rightPanelButtonY, this.connectButtonWidth, 23.0F, 6.0F, applyAlpha(BUTTON_SURFACE_COLOR, animationAlpha));
         String connectButtonLabel = "Подключить";
         drawCenteredText(
            drawContext,
            connectButtonLabel,
            connectButtonX + (this.connectButtonWidth - measureTextWidth(connectButtonLabel, 6.0F)) / 2.0F,
            rightPanelButtonY,
            23.0F,
            6.0F,
            applyAlpha(PRIMARY_TEXT_COLOR, animationAlpha)
         );
      }
// https://t.me/pasterplace

      float detailsPanelX = panelX + 244.0F;
      float disconnectHistoryTop = panelY + 39.0F + 130.5F + 4.0F;
      float chatInputY = disconnectHistoryTop + 107.5F + 4.0F;
      if (selectedSessionOnline) {
         if (this.selectedBotSession.hasActivePlayerSession() && BotPictureInPictureRenderer.INSTANCE.hasPictureInPicture(this.selectedBotSession)) {
            TextureQuadBuilder.at(BotPictureInPictureRenderer.TEXTURE_ID, panelX + 244.0F, panelY + 39.0F, 232.0F, 130.5F)
               .cornerRadius(6.0F)
               .setUnusedFlagTwo(true)
               .opacity(animationAlpha)
               .render(drawContext);
            GuiEventRenderer.flushAndStartRootLayer(drawContext);
            BotPictureInPictureRenderer.INSTANCE.renderScreenPreview(this.selectedBotSession, drawContext, panelX + 244.0F, panelY + 39.0F, 232.0F, 130.5F);
            GuiEventRenderer.flushAndStartRootLayer(drawContext);
         } else {
            drawRoundedRect(drawContext, panelX + 244.0F, panelY + 39.0F, 232.0F, 130.5F, 6.0F, applyAlpha(SESSION_ROW_SURFACE_COLOR, animationAlpha));
         }

         drawRoundedRect(drawContext, detailsPanelX, disconnectHistoryTop, 232.0F, 107.5F, 6.0F, applyAlpha(SESSION_ROW_SURFACE_COLOR, animationAlpha));
      } else {
         float offlinePanelHeight = 242.0F;
         drawRoundedRect(drawContext, detailsPanelX, panelY + 39.0F, 232.0F, offlinePanelHeight, 6.0F, applyAlpha(SESSION_ROW_SURFACE_COLOR, animationAlpha));
         String offlineMessage = "Нет подключения";
         drawCenteredText(
            drawContext,
            offlineMessage,
            detailsPanelX + (232.0F - measureTextWidth(offlineMessage, 6.0F)) / 2.0F,
            panelY + 39.0F,
            offlinePanelHeight,
            6.0F,
            applyAlpha(PRIMARY_TEXT_COLOR, animationAlpha)
         );
      }

      if (selectedSessionOnline) {
         this.chatMessageField.render(drawContext, detailsPanelX, chatInputY, 182.0F, 23.0F, frameTimeMillis, animationAlpha);
         float sendButtonX = detailsPanelX + 232.0F - 48.0F;
         drawRoundedRect(drawContext, sendButtonX, chatInputY, 48.0F, 23.0F, 6.0F, applyAlpha(BUTTON_SURFACE_COLOR, animationAlpha));
         String sendButtonLabel = "Отправить";
         drawCenteredText(
            drawContext, sendButtonLabel, sendButtonX + (48.0F - measureTextWidth(sendButtonLabel, 6.0F)) / 2.0F, chatInputY, 23.0F, 6.0F, applyAlpha(PRIMARY_TEXT_COLOR, animationAlpha)
         );
         this.renderDisconnectHistory(drawContext, detailsPanelX, disconnectHistoryTop, animationAlpha);
      } else {
         this.chatMessageField.setFocused(false);
      }
   }
// https://t.me/pasterplace

   private void renderSavedBotRow(
      DrawContext drawContext, SavedBotSession session, int rowIndex, float listX, float clipTop, float clipBottom, float animationAlpha, long nowMillis
   ) {
      Object animationState = this.rowAnimationsByUsername.computeIfAbsent(session.getUsername(), username -> new BotManagementScreen.SessionRowAnimation(nowMillis));
      float unanimatedRowY = clipTop + rowIndex * 25.0F - this.sessionListScrollOffset;
      boolean hoveredRow = this.mouseY >= clipTop
         && this.mouseY <= clipBottom
         && this.mouseX >= listX + 4.0F
         && this.mouseX <= listX + 4.0F + 227.0F
         && this.mouseY >= unanimatedRowY
         && this.mouseY <= unanimatedRowY + 23.0F;
      float rowFadeProgress = ((BotManagementScreen.SessionRowAnimation)animationState).getFadeInProgress(nowMillis);
      float disconnectHoverProgress = ((BotManagementScreen.SessionRowAnimation)animationState).disconnectButtonHover.approach(hoveredRow ? 1.0F : 0.0F, 0.15F);
      float rowOpacity = animationAlpha * rowFadeProgress;
      float rowY = unanimatedRowY + (1.0F - rowFadeProgress) * 6.0F;
      float rowX = listX + 4.0F;
      drawRoundedRect(drawContext, rowX, rowY, 227.0F, 23.0F, 6.0F, applyAlpha(SESSION_ROW_SURFACE_COLOR, rowOpacity));
      drawRoundedRect(drawContext, rowX, rowY, 23.0F, 23.0F, 6.0F, applyAlpha(SESSION_ROW_SURFACE_COLOR, rowOpacity));
      float disconnectButtonX = rowX + 227.0F - 64.0F;
      drawRoundedRect(drawContext, disconnectButtonX, rowY, 64.0F, 23.0F, 6.0F, applyAlpha(SESSION_ROW_SURFACE_COLOR, rowOpacity));
      if (session.isOnline()) {
         boolean hoveredDisconnectButton = this.mouseX >= disconnectButtonX
            && this.mouseX <= disconnectButtonX + 64.0F
            && this.mouseY >= rowY
            && this.mouseY <= rowY + 23.0F
            && this.mouseY >= clipTop
            && this.mouseY <= clipBottom;
         float disconnectAnimationProgress = ((BotManagementScreen.SessionRowAnimation)animationState).serverAddressHover.approach(hoveredDisconnectButton ? 1.0F : 0.0F, 0.2F);
         float disconnectButtonHeight = 23.0F;
         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(Math.max(rowX, listX + 4.0F)),
            PixelRounding.II1Il11l111II11IIl(Math.max(rowY, clipTop)),
            PixelRounding.l1I1IlllIlI(rowX + 227.0F),
            PixelRounding.l1I1IlllIlI(Math.min(rowY + 23.0F, clipBottom))
         );

         try {
            if (disconnectAnimationProgress < 0.999F) {
               String serverAddress = session.getServerAddress();
               String serverDisplayName = getServerHostLabel(serverAddress);
               Identifier identifier = BotServerIcon.getServerIcon(serverAddress);
               float serverDisplayNameWidth = measureTextWidth(serverDisplayName, 6.0F);
               float serverDisplayWidth = serverDisplayNameWidth + (identifier != null ? 11.0F : 0.0F);
               float serverDisplayX = disconnectButtonX + (64.0F - serverDisplayWidth) / 2.0F;
               float serverDisplayY = rowY - disconnectAnimationProgress * disconnectButtonHeight;
               float serverDisplayOpacity = rowOpacity * (1.0F - disconnectAnimationProgress);
               if (identifier != null) {
                  float serverIconY = serverDisplayY + 8.0F;
                  TextureQuadBuilder.at(identifier, serverDisplayX, serverIconY, 7.0F, 7.0F)
                     .cornerRadius(2.0F)
                     .opacity(serverDisplayOpacity)
                     .render(drawContext);
                  serverDisplayX += 11.0F;
               }

               drawCenteredText(drawContext, serverDisplayName, serverDisplayX, serverDisplayY, 23.0F, 6.0F, applyAlpha(-1, serverDisplayOpacity));
            }

            if (disconnectAnimationProgress > 0.001F) {
               String disconnectButtonLabel = "Отключить";
               float disconnectButtonLabelY = rowY + (1.0F - disconnectAnimationProgress) * disconnectButtonHeight;
               drawCenteredText(
                  drawContext,
                  disconnectButtonLabel,
                  disconnectButtonX + (64.0F - measureTextWidth(disconnectButtonLabel, 6.0F)) / 2.0F,
                  disconnectButtonLabelY,
                  23.0F,
                  6.0F,
                  applyAlpha(DISCONNECT_ACTION_COLOR, rowOpacity * disconnectAnimationProgress)
               );
            }
         } finally {
            drawContext.disableScissor();
            drawContext.enableScissor(
               PixelRounding.II1Il11l111II11IIl(listX + 4.0F),
               PixelRounding.II1Il11l111II11IIl(clipTop),
               PixelRounding.l1I1IlllIlI(listX + 4.0F + 227.0F),
               PixelRounding.l1I1IlllIlI(clipBottom)
            );
         }
      } else {
         String offlineLabel = "Offline";
         drawCenteredText(
            drawContext, offlineLabel, disconnectButtonX + (64.0F - measureTextWidth(offlineLabel, 6.0F)) / 2.0F, rowY, 23.0F, 6.0F, applyAlpha(PRIMARY_TEXT_COLOR, rowOpacity)
         );
      }

      boolean hoveredRemoveButton = this.mouseX >= rowX
         && this.mouseX <= rowX + 23.0F
         && this.mouseY >= rowY
         && this.mouseY <= rowY + 23.0F
         && this.mouseY >= clipTop
         && this.mouseY <= clipBottom;
      float removeHoverProgress = ((BotManagementScreen.SessionRowAnimation)animationState).removeButtonHover.approach(hoveredRemoveButton ? 1.0F : 0.0F, 0.16F);
      Identifier avatarTexture = BotAvatars.getAvatarTexture(session.getUsername());
      if (avatarTexture != null) {
         float avatarX = rowX + 4.0F;
         float avatarY = rowY + 4.0F;
         TextureQuadBuilder.at(avatarTexture, avatarX, avatarY, 15.0F, 15.0F)
            .cornerRadius(3.0F)
            .overlay(-15592940, 0.62F * removeHoverProgress)
            .opacity(rowOpacity)
            .render(drawContext);
      }

      if (removeHoverProgress > 0.001F) {
         String removeButtonLabel = "×";
         drawCenteredText(
            drawContext, removeButtonLabel, rowX + (23.0F - measureTextWidth(removeButtonLabel, 10.0F)) / 2.0F, rowY, 23.0F, 10.0F, applyAlpha(REMOVE_SESSION_COLOR, rowOpacity * removeHoverProgress)
         );
      }

      float usernameX = rowX + 23.0F + 6.0F + disconnectHoverProgress * 2.0F;
      String roleLabel = SavedBots.getRoleForUsername(session.getUsername());
      boolean editingRole = session.getUsername().equals(this.editingRoleUsername);
      String formattedBalance = formatSessionBalance(session);
      boolean hasBalance = !formattedBalance.isEmpty();
      float balanceTextWidth = hasBalance ? measureTextWidth(formattedBalance, 5.0F) : 0.0F;
      float balanceGap = hasBalance ? balanceTextWidth + 6.0F : 0.0F;
      float roleBadgeWidth = 0.0F;
      if (editingRole) {
         float roleEditorWidth = measureTextWidth(this.roleEditField.getText().isEmpty() ? "Role" : this.roleEditField.getText(), 5.0F);
         roleBadgeWidth = Math.max(44.0F, roleEditorWidth + 8.0F);
      } else if (!roleLabel.isEmpty()) {
         roleBadgeWidth = measureTextWidth(roleLabel, 5.0F) + 8.0F;
      }
// https://t.me/pasterplace

      float roleBadgeGap = roleBadgeWidth > 0.0F ? roleBadgeWidth + 6.0F : 0.0F;
      boolean showBalance = hasBalance;
      float availableLabelWidth = disconnectButtonX - 6.0F - balanceGap - roleBadgeGap - usernameX;
      if (showBalance && availableLabelWidth < 26.0F) {
         showBalance = false;
         availableLabelWidth = disconnectButtonX - 6.0F - roleBadgeGap - usernameX;
      }

      String displayUsername = truncateTextToWidth(session.getUsername(), 6.0F, Math.max(0.0F, availableLabelWidth));
      float usernameTextWidth = measureTextWidth(displayUsername, 6.0F);
      drawCenteredText(drawContext, displayUsername, usernameX, rowY, 23.0F, 6.0F, applyAlpha(-1, rowOpacity));
      float balanceX = disconnectButtonX - 6.0F - balanceTextWidth;
      if (showBalance && balanceX > usernameX + usernameTextWidth + 6.0F) {
         drawCenteredText(drawContext, formattedBalance, balanceX, rowY, 23.0F, 5.0F, applyAlpha(Accent.getAccentColorWithAlpha(255).toArgb(), rowOpacity));
      }

      float roleLabelX = usernameX + usernameTextWidth + 6.0F;
      float identityRightX = showBalance && balanceX > roleLabelX ? balanceX - 6.0F : disconnectButtonX - 6.0F;
      float roleLabelWidth = identityRightX - roleLabelX;
      if (editingRole && roleLabelWidth > 12.0F) {
         float roleEditorY = rowY + 4.0F;
         this.roleEditField.render(drawContext, roleLabelX, roleEditorY, roleLabelWidth, 15.0F, nowMillis, rowOpacity);
      } else if (roleLabelWidth > 12.0F && !roleLabel.isEmpty()) {
         String roleBadgeText = truncateTextToWidth(roleLabel, 5.0F, roleLabelWidth - 8.0F);
         if (!roleBadgeText.isEmpty()) {
            float renderedRoleBadgeWidth = measureTextWidth(roleBadgeText, 5.0F) + 8.0F;
            float roleBadgeY = rowY + 6.0F;
            drawRoundedRect(drawContext, roleLabelX, roleBadgeY, renderedRoleBadgeWidth, 11.0F, 3.0F, applyAlpha(ROLE_BADGE_SURFACE_COLOR, rowOpacity));
            drawCenteredText(drawContext, roleBadgeText, roleLabelX + 4.0F, roleBadgeY, 11.0F, 5.0F, applyAlpha(ROLE_BADGE_TEXT_COLOR, rowOpacity));
         }
      }
   }
// https://t.me/pasterplace

   private static String formatSessionBalance(SavedBotSession disconnect) {
      if (disconnect instanceof BotSession disconnectRecord && disconnect.isOnline()) {
         long balance = SavedBots.getScoreboardBalance(disconnectRecord);
         return balance < 0L ? "--" : String.format(Locale.ROOT, "%,d", balance).replace(',', ' ');
      } else {
         return "";
      }
   }
// https://t.me/pasterplace

   private void renderDisconnectHistory(DrawContext drawContext, float panelX, float panelY, float animationAlpha) {
      List<BotSessionLogEntry> disconnectEntries = this.selectedBotSession.getSessionHistory();
      long nowMillis = System.currentTimeMillis();
      if (this.selectedBotSession != this.lastRenderedBotSession) {
         this.lastRenderedBotSession = this.selectedBotSession;
         this.disconnectAnimationStartTimes.clear();
         this.wrappedDisconnectLinesByMessage.clear();

         for (BotSessionLogEntry disconnectEntry : disconnectEntries) {
            this.disconnectAnimationStartTimes.put(disconnectEntry, new long[]{0L});
         }
      }

      Iterator<BotSessionLogEntry> entryIterator = this.disconnectAnimationStartTimes.keySet().iterator();

      while (entryIterator.hasNext()) {
         BotSessionLogEntry animatedEntry = entryIterator.next();
         boolean entryStillPresent = false;

         for (BotSessionLogEntry candidateEntry : disconnectEntries) {
            if (candidateEntry == animatedEntry) {
               entryStillPresent = true;
               break;
            }
         }

         if (!entryStillPresent) {
            entryIterator.remove();
         }
      }

      if (!disconnectEntries.isEmpty()) {
         float historyLineHeight = Medium.DEFAULT_FONT.lineHeight(6.0F) + 1.0F;
         float timestampWidth = measureTextWidth("00:00:00", 5.0F);
         float timestampX = panelX + 8.0F + timestampWidth + 8.0F;
         float messageWidth = panelX + 232.0F - 8.0F - timestampX;
         List<DisconnectHistoryRow> historyRows = new ArrayList<>(disconnectEntries.size());

         for (BotSessionLogEntry historyEntry : disconnectEntries) {
            long[] firstSeenTimestampHolder = this.disconnectAnimationStartTimes.computeIfAbsent(historyEntry, logEntry -> new long[]{nowMillis});
            float rawEntryProgress = firstSeenTimestampHolder[0] == 0L ? 1.0F : Math.clamp((float)(nowMillis - firstSeenTimestampHolder[0]) / 260.0F, 0.0F, 1.0F);
            float entryFadeProgress = 1.0F - (float)Math.pow(1.0F - rawEntryProgress, 3.0);
            List<String> wrappedMessageLines = this.wrappedDisconnectLinesByMessage.computeIfAbsent(historyEntry.message(), message -> wrapText(message, messageWidth));

            for (int messageLineIndex = 0; messageLineIndex < wrappedMessageLines.size(); messageLineIndex++) {
               historyRows.add(
                  new DisconnectHistoryRow(messageLineIndex == 0 ? historyEntry.serverName() : null, wrappedMessageLines.get(messageLineIndex), entryFadeProgress)
               );
            }
         }
// https://t.me/pasterplace

         float historyViewportHeight = 99.5F;
         float totalAnimatedHeight = 0.0F;

         for (DisconnectHistoryRow historyRow : historyRows) {
            totalAnimatedHeight += historyLineHeight * historyRow.animationProgress();
         }

         float maxHistoryScroll = Math.max(0.0F, totalAnimatedHeight - historyViewportHeight);
         this.disconnectHistoryScrollOffset = Math.max(0.0F, Math.min(this.disconnectHistoryScrollOffset, maxHistoryScroll));
         float rowY = panelY + 107.5F - 4.0F - totalAnimatedHeight + this.disconnectHistoryScrollOffset;
         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(panelX),
            PixelRounding.II1Il11l111II11IIl(panelY),
            PixelRounding.l1I1IlllIlI(panelX + 232.0F),
            PixelRounding.l1I1IlllIlI(panelY + 107.5F)
         );

         try {
            for (DisconnectHistoryRow historyEntry : historyRows) {
               float rowOpacity = historyEntry.animationProgress();
               if (rowY + historyLineHeight > panelY && rowY < panelY + 107.5F) {
                  float rowTextAlpha = animationAlpha * rowOpacity;
                  if (historyEntry.groupName() != null) {
                     drawCenteredText(
                        drawContext,
                        historyEntry.groupName(),
                        panelX + 8.0F,
                        rowY,
                        historyLineHeight,
                        5.0F,
                        applyAlpha(SECONDARY_TEXT_COLOR, rowTextAlpha)
                     );
                  }

                  drawCenteredText(drawContext, historyEntry.displayText(), timestampX, rowY, historyLineHeight, 6.0F, applyAlpha(-1, rowTextAlpha));
               }
// https://t.me/pasterplace

               rowY += historyLineHeight * rowOpacity;
            }
         } finally {
            drawContext.disableScissor();
         }
      }
   }
// https://t.me/pasterplace

   private static String truncateTextToWidth(String textToTruncate, float fontSize, float maxWidth) {
      if (textToTruncate != null && !textToTruncate.isEmpty() && !(maxWidth <= 0.0F)) {
         if (measureTextWidth(textToTruncate, fontSize) <= maxWidth) {
            return textToTruncate;
         }

         String ellipsis = "...";
         float ellipsisWidth = measureTextWidth(ellipsis, fontSize);
         if (ellipsisWidth >= maxWidth) {
            return "";
         }

         String truncatedText = textToTruncate;

         while (!truncatedText.isEmpty() && measureTextWidth(truncatedText, fontSize) + ellipsisWidth > maxWidth) {
            truncatedText = truncatedText.substring(0, truncatedText.length() - 1);
         }

         return truncatedText.isEmpty() ? "" : truncatedText + ellipsis;
      } else {
         return "";
      }
   }
// https://t.me/pasterplace

   private static float getSessionListContentHeight(int sessionCount) {
      return sessionCount <= 0 ? 0.0F : sessionCount * 25.0F - 2.0F;
   }
// https://t.me/pasterplace

   private static float getSessionListMaxScroll(int sessionCount) {
      return Math.max(0.0F, getSessionListContentHeight(sessionCount) - 231.0F);
   }
// https://t.me/pasterplace

   private List<SavedBotSession> filterSessions(List<SavedBotSession> sessions) {
      String query = this.sessionSearchField.getText().trim();
      if (query.isEmpty()) {
         return sessions;
      }

      String lowercaseQuery = query.toLowerCase(Locale.ROOT);
      List<SavedBotSession> filteredSessions = new ArrayList<>();

      for (SavedBotSession session : sessions) {
         if (session.getUsername().toLowerCase(Locale.ROOT).contains(lowercaseQuery)) {
            filteredSessions.add(session);
         }
      }

      return filteredSessions;
   }
// https://t.me/pasterplace

   private List<SavedBotSession> getFilteredSessions() {
      return this.filterSessions(SavedBots.getSessionRepository().getAllSessions());
   }
// https://t.me/pasterplace

   private static String getServerHostLabel(String serverAddress) {
      return serverAddress != null && !"No Server".equalsIgnoreCase(serverAddress) ? serverHostLabelCache.computeIfAbsent(serverAddress, BotManagementScreen::formatServerHost) : "";
   }
// https://t.me/pasterplace

   private static String formatServerHost(String serverAddress) {
      int portSeparatorIndex = serverAddress.indexOf(58);
      String hostname = portSeparatorIndex > 0 ? serverAddress.substring(0, portSeparatorIndex) : serverAddress;
      String[] hostnameSegments = hostname.split("\\.");
      if (hostnameSegments.length < 2) {
         return hostname;
      }

      for (String ipSegment : hostnameSegments) {
         if (!ipSegment.chars().allMatch(Character::isDigit)) {
            return hostnameSegments[hostnameSegments.length - 2];
         }
      }

      return hostname;
   }
// https://t.me/pasterplace

   private static List<String> wrapText(String textToWrap, float maxLineWidth) {
      List<String> wrappedLines = new ArrayList<>();
      StringBuilder stringBuilder = new StringBuilder();

      for (String word : textToWrap.split(" ")) {
         if (stringBuilder.isEmpty()) {
            stringBuilder.append(word);
         } else if (measureTextWidth(stringBuilder + " " + word, 6.0F) <= maxLineWidth) {
            stringBuilder.append(' ').append(word);
         } else {
            wrappedLines.add(stringBuilder.toString());
            stringBuilder.setLength(0);
            stringBuilder.append(word);
         }

         while (stringBuilder.length() > 1 && measureTextWidth(stringBuilder.toString(), 6.0F) > maxLineWidth) {
            int splitPosition = stringBuilder.length();

            while (splitPosition > 1 && measureTextWidth(stringBuilder.substring(0, splitPosition), 6.0F) > maxLineWidth) {
               splitPosition--;
            }

            wrappedLines.add(stringBuilder.substring(0, splitPosition));
            stringBuilder.delete(0, splitPosition);
         }
      }

      if (!stringBuilder.isEmpty()) {
         wrappedLines.add(stringBuilder.toString());
      }

      return wrappedLines;
   }
// https://t.me/pasterplace

   private void sendChatMessage() {
      String message = this.chatMessageField.getText();
      if (this.selectedBotSession != null && !message.isBlank()) {
         this.selectedBotSession.sendChatMessage(message);
      }

      this.chatMessageField.setText("");
   }
// https://t.me/pasterplace

   private void clearSearchFocus() {
      this.chatMessageField.setFocused(false);
      this.serverAddressField.setFocused(false);
      this.newSessionUsernameField.setFocused(false);
      this.sessionSearchField.setFocused(false);
   }
// https://t.me/pasterplace

   private void beginRoleEdit(SavedBotSession i1I1lI1Illl1I1Il2) {
      this.saveRoleEdit();
      this.editingRoleUsername = i1I1lI1Illl1I1Il2.getUsername();
      this.roleEditField.setText(SavedBots.getRoleForUsername(this.editingRoleUsername));
      this.clearSearchFocus();
      this.roleEditField.setFocused(true);
   }
// https://t.me/pasterplace

   private void saveRoleEdit() {
      if (this.editingRoleUsername != null && SavedBots.findSession(this.editingRoleUsername) != null) {
         SavedBots.setRoleForUsername(this.editingRoleUsername, this.roleEditField.getText().trim());
      }

      this.cancelRoleEdit();
   }
// https://t.me/pasterplace

   private void cancelRoleEdit() {
      this.editingRoleUsername = null;
      this.roleEditField.setText("");
      this.roleEditField.setFocused(false);
   }
// https://t.me/pasterplace

   private void addSavedSession() {
      String username = this.newSessionUsernameField.getText().trim();
      if (!username.isBlank() && SavedBots.getSessionRepository().addOfflineSession(username)) {
         this.newSessionUsernameField.setText("");
         this.selectedBotSession = SavedBots.getSessionRepository().findSession(username);
         this.sessionListScrollOffset = 0.0F;
      }
   }
// https://t.me/pasterplace

   private void connectSelectedSession() {
      String serverAddress = this.serverAddressField.getText().trim();
      if (serverAddress.isBlank()) {
         ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
         if (serverInfo != null && serverInfo.address != null) {
            serverAddress = serverInfo.address.trim();
         }
      }

      if (this.selectedBotSession != null && !serverAddress.isBlank()) {
         String serverHost = serverAddress;
         int serverPort = 25565;
         int portSeparatorIndex = serverAddress.indexOf(58);
         if (portSeparatorIndex > 0) {
            serverHost = serverAddress.substring(0, portSeparatorIndex).trim();

            try {
               serverPort = Integer.parseInt(serverAddress.substring(portSeparatorIndex + 1).trim());
            } catch (NumberFormatException invalidPort) {
            }
         }

         this.selectedBotSession = SavedBots.getSessionRepository().createSession(this.selectedBotSession.getUsername(), serverHost, serverPort);
      }

      this.serverAddressField.setText("");
      this.serverAddressField.setFocused(false);
   }
// https://t.me/pasterplace

   private static boolean containsPoint(float mouseX, float mouseY, float left, float top, float width, float height) {
      return mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
   }
// https://t.me/pasterplace

   public boolean mouseClicked(Click click, boolean doubleClick) {
      if (this.isTransitionActive()) {
         return true;
      }

      float mouseX = scaleMouseCoordinate(click.x());
      float mouseY = scaleMouseCoordinate(click.y());
      float panelLeft = getPanelLeft();
      float panelTop = getPanelTop();
      float sessionListTop = panelTop + 58.0F;
      float sessionListBottom = sessionListTop + 231.0F;
      if (click.button() == 1) {
         List<SavedBotSession> filteredSessions = this.getFilteredSessions();
// https://t.me/pasterplace

         for (int rightClickRowIndex = 0; rightClickRowIndex < filteredSessions.size(); rightClickRowIndex++) {
            float rightClickRowY = sessionListTop + rightClickRowIndex * 25.0F - this.sessionListScrollOffset;
            if (mouseY >= sessionListTop && mouseY <= sessionListBottom && containsPoint(mouseX, mouseY, panelLeft + 4.0F, rightClickRowY, 227.0F, 23.0F)) {
               this.beginRoleEdit(filteredSessions.get(rightClickRowIndex));
               return true;
            }
         }

         this.saveRoleEdit();
         return true;
      } else {
         if (click.button() != 0) {
            return super.mouseClicked(click, doubleClick);
         }

         if (this.editingRoleUsername != null) {
            this.saveRoleEdit();
         }

         float inputRowY = panelTop + 31.0F;
         float leftControlsX = panelLeft + 4.0F;
         float addSessionButtonX = leftControlsX + 114.0F - 23.0F;
         if (containsPoint(mouseX, mouseY, addSessionButtonX, inputRowY, 23.0F, 23.0F)) {
            this.addSavedSession();
            return true;
         }

         if (containsPoint(mouseX, mouseY, leftControlsX, inputRowY, 91.0F, 23.0F)) {
            this.clearSearchFocus();
            this.newSessionUsernameField.setFocused(true);
            return true;
         }

         float searchInputX = leftControlsX + 114.0F + 4.0F;
         if (containsPoint(mouseX, mouseY, searchInputX, inputRowY, 114.0F, 23.0F)) {
            this.clearSearchFocus();
            this.sessionSearchField.setFocused(true);
            return true;
         }

         float transferButtonX = panelTop + 58.0F + 231.0F + 4.0F;
         if (containsPoint(mouseX, mouseY, panelLeft + 4.0F, transferButtonX, 232.0F, 23.0F)) {
            SavedBots.resetPayoutTracking();
            return true;
         }

         List<SavedBotSession> filteredSessions = this.getFilteredSessions();

         for (int sessionRowIndex = 0; sessionRowIndex < filteredSessions.size(); sessionRowIndex++) {
            float sessionRowY = sessionListTop + sessionRowIndex * 25.0F - this.sessionListScrollOffset;
            if (mouseY >= sessionListTop && mouseY <= sessionListBottom && containsPoint(mouseX, mouseY, panelLeft + 4.0F, sessionRowY, 227.0F, 23.0F)) {
               SavedBotSession clickedSession = filteredSessions.get(sessionRowIndex);
               if (mouseX <= panelLeft + 4.0F + 23.0F) {
                  String removedUsername = clickedSession.getUsername();
                  SavedBots.getSessionRepository().removeSession(removedUsername);
                  this.rowAnimationsByUsername.remove(removedUsername);
                  if (this.selectedBotSession == clickedSession) {
                  List<SavedBotSession> savedSessions = SavedBots.getSessionRepository().getAllSessions();
                  this.selectedBotSession = savedSessions.isEmpty() ? null : savedSessions.get(0);
                     this.disconnectHistoryScrollOffset = 0.0F;
                  }

                  return true;
               }

               if (clickedSession.isOnline() && mouseX >= panelLeft + 4.0F + 227.0F - 64.0F) {
                  clickedSession.disconnect("disconnect");
                  return true;
               }

               this.selectedBotSession = clickedSession;
               this.disconnectHistoryScrollOffset = 0.0F;
               return true;
            }
         }
// https://t.me/pasterplace

         float rightPanelButtonX = panelLeft + 319.0F;
         float rightPanelButtonY = panelTop + 4.0F;
         boolean rightPanelButtonHit = containsPoint(mouseX, mouseY, rightPanelButtonX, rightPanelButtonY, 157.0F, 23.0F);
         if (this.selectedBotSession != null && this.selectedBotSession.isOnline()) {
            float manageButtonX = rightPanelButtonX + 157.0F - this.manageButtonWidth;
            if (rightPanelButtonHit && mouseX >= manageButtonX) {
               String selectedUsername = this.selectedBotSession.getUsername();
               BotPictureInPictureRenderer.INSTANCE.closePictureInPicture();
               MinecraftClient.getInstance().setScreen(null);
               SavedBots.getServiceAccess().switchToSession(selectedUsername);
               return true;
            }
         } else {
            float connectButtonX = rightPanelButtonX + 157.0F - this.connectButtonWidth;
            if (rightPanelButtonHit && mouseX >= connectButtonX) {
               this.connectSelectedSession();
               return true;
            }

            if (rightPanelButtonHit && mouseX >= rightPanelButtonX + 15.0F) {
               this.clearSearchFocus();
               this.serverAddressField.setFocused(true);
               return true;
            }
         }

         if (this.selectedBotSession != null && this.selectedBotSession.isOnline()) {
            float detailsPanelX = panelLeft + 244.0F;
            float chatInputY = panelTop + 39.0F + 130.5F + 4.0F + 107.5F + 4.0F;
            float sendButtonX = detailsPanelX + 232.0F - 48.0F;
            if (containsPoint(mouseX, mouseY, sendButtonX, chatInputY, 48.0F, 23.0F)) {
               this.sendChatMessage();
               return true;
            }

            if (containsPoint(mouseX, mouseY, detailsPanelX, chatInputY, 232.0F, 23.0F)) {
               this.clearSearchFocus();
               this.chatMessageField.setFocused(true);
               return true;
            }
         }

         this.clearSearchFocus();
         return super.mouseClicked(click, doubleClick);
      }
   }
// https://t.me/pasterplace

   private SearchTextField getFocusedTextField() {
      if (this.roleEditField.isFocused()) {
         return this.roleEditField;
      } else if (this.chatMessageField.isFocused()) {
         return this.chatMessageField;
      } else if (this.serverAddressField.isFocused()) {
         return this.serverAddressField;
      } else if (this.newSessionUsernameField.isFocused()) {
         return this.newSessionUsernameField;
      } else {
         return this.sessionSearchField.isFocused() ? this.sessionSearchField : null;
      }
   }
// https://t.me/pasterplace

   public boolean keyPressed(KeyInput keyInput) {
      if (this.isTransitionActive()) {
         return true;
      }

      SearchTextField l1I1IIl111Il1l1l = this.getFocusedTextField();
      if (l1I1IIl111Il1l1l != null) {
         if (l1I1IIl111Il1l1l == this.roleEditField && keyInput.getKeycode() == 256) {
            this.cancelRoleEdit();
            return true;
         } else {
            return l1I1IIl111Il1l1l.keyPressed(keyInput);
         }
      } else if (keyInput.getKeycode() == 256) {
         this.close();
         return true;
      } else {
         return super.keyPressed(keyInput);
      }
   }
// https://t.me/pasterplace

   public boolean charTyped(CharInput charInput) {
      if (this.isTransitionActive()) {
         return true;
      }

      SearchTextField l1I1IIl111Il1l1l = this.getFocusedTextField();
      return l1I1IIl111Il1l1l != null ? l1I1IIl111Il1l1l.charTyped(charInput) : super.charTyped(charInput);
   }
// https://t.me/pasterplace

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalScroll, double verticalScroll) {
      if (this.isTransitionActive()) {
         return true;
      } else {
         float scaledMouseX = scaleMouseCoordinate(mouseX);
         float scaledMouseY = scaleMouseCoordinate(mouseY);
         float panelLeft = getPanelLeft();
         float panelTop = getPanelTop();
         float pictureInPictureX = panelLeft + 244.0F;
         float pictureInPictureY = panelTop + 39.0F + 130.5F + 4.0F;
         if (containsPoint(scaledMouseX, scaledMouseY, pictureInPictureX, pictureInPictureY, 232.0F, 107.5F)) {
            this.disconnectHistoryScrollOffset = Math.max(0.0F, this.disconnectHistoryScrollOffset + (float)verticalScroll * 14.0F);
            return true;
         } else {
            float sessionListTop = panelTop + 58.0F;
            if (containsPoint(scaledMouseX, scaledMouseY, panelLeft + 4.0F, sessionListTop, 227.0F, 231.0F)) {
               float maxSessionListScroll = getSessionListMaxScroll(this.getFilteredSessions().size());
               this.sessionListScrollOffset = Math.max(0.0F, Math.min(maxSessionListScroll, this.sessionListScrollOffset - (float)verticalScroll * 25.0F));
               return true;
            } else {
               return super.mouseScrolled(mouseX, mouseY, horizontalScroll, verticalScroll);
            }
         }
      }
   }
// https://t.me/pasterplace

   public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
   }
// https://t.me/pasterplace

   public boolean shouldPause() {
      return false;
   }
// https://t.me/pasterplace

   public boolean shouldCloseOnEsc() {
      return false;
   }
// https://t.me/pasterplace

   public BotSessionInfo getSelectedSessionInfo() {
      return this.selectedBotSession;
   }
// https://t.me/pasterplace

   public void close() {
      if (this.screenClosingStartedAt < 0L) {
         this.screenClosingStartedAt = System.currentTimeMillis();
         BotPictureInPictureRenderer.INSTANCE.closePictureInPicture();
      }
   }
// https://t.me/pasterplace

   public void removed() {
      BotPictureInPictureRenderer.INSTANCE.closePictureInPicture();
      super.removed();
   }
// https://t.me/pasterplace

   private record DisconnectHistoryRow(String groupName, String displayText, float animationProgress) {

      private DisconnectHistoryRow(String groupName, String displayText, float animationProgress) {
         this.groupName = groupName;
         this.displayText = displayText;
         this.animationProgress = animationProgress;
      }
   }
// https://t.me/pasterplace

   private static final class SmoothedValue {
      float currentValue;

      float approach(float targetValue, float interpolationSpeed) {
         this.currentValue = this.currentValue + (targetValue - this.currentValue) * Math.min(1.0F, interpolationSpeed * 2.5F);
         if (Math.abs(targetValue - this.currentValue) < 0.001F) {
            this.currentValue = targetValue;
         }
// https://t.me/pasterplace

         return this.currentValue;
      }
   }
// https://t.me/pasterplace

   private static final class SessionRowAnimation {
      final long createdAtMillis;
      final BotManagementScreen.SmoothedValue disconnectButtonHover = new BotManagementScreen.SmoothedValue();
      final BotManagementScreen.SmoothedValue removeButtonHover = new BotManagementScreen.SmoothedValue();
      final BotManagementScreen.SmoothedValue serverAddressHover = new BotManagementScreen.SmoothedValue();

      SessionRowAnimation(long createdAtMillis) {
         this.createdAtMillis = createdAtMillis;
      }
// https://t.me/pasterplace

      float getFadeInProgress(long nowMillis) {
         float linearProgress = Math.clamp((float)(nowMillis - this.createdAtMillis) / 220.0F, 0.0F, 1.0F);
         return 1.0F - (float)Math.pow(1.0F - linearProgress, 3.0);
      }
   }
}

