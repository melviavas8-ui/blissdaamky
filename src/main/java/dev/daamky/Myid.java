package dev.daamky;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

public final class Myid {
    private static final String SOURCE_PROMOTION_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final AvatarTexture CHECK_ICON = Avatar.create("check");
    private static final AvatarTexture DOUBLE_CHECK_ICON = Avatar.create("check_double");
    private static final float PANEL_WIDTH = 112.0F;
    private static final float TAB_BAR_HEIGHT = 22.0F;
    private static final float EMOJI_ACTION_ROW_HEIGHT = 21.0F;
    private static final float SMALL_ICON_SIZE = 16.0F;
    private static final float SMALL_TEXT_SIZE = 4.6F;
    private static final float MEDIUM_TEXT_SIZE = 6.6F;
    private static final float TIMESTAMP_TEXT_SIZE = 4.5F;
    private static final float BADGE_TEXT_SIZE = 3.2F;
    private static final float EMPTY_STATE_WIDTH = 136.0F;
    private static final float PROFILE_CARD_HEIGHT = 104.0F;
    private static final float MESSAGE_PREVIEW_GAP = 14.0F;
    private static final String[] TAB_LABELS = new String[]{"Чаты", "Заявки", "Профиль"};
    private static final int[] TEXT_COLOR_PALETTE = new int[]{-30080, -18611, -3722, -8271996, -11549705, -6982195, -1023342, -11684180};
    private final GlobalsClient socialState = GlobalsClient.getInstance();
    private final SearchTextField chatSearchField = new SearchTextField("Поиск...", 32, text -> {});
    private final SearchTextField friendSearchField = new SearchTextField("ID или имя друга", 24, this::sendFriendRequest);
    private final SearchTextField biographyField = new SearchTextField("О себе", 70, text -> this.saveBiography());
    private final SimpleDateFormat messageTimeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat compactDateFormat = new SimpleDateFormat("dd.MM");
    private final Map<String, float[]> buttonHoverAnimations = new HashMap<>();
    private final Map<Long, Myid.EmojiLineRenderData> messageLayoutCache = new HashMap<>();
    private int selectedTabIndex;
    private float tabIndicatorX = -1.0F;
    private float animationFrameDelta = 0.016F;
    private String messageDraft = "";
    private boolean showMessageCursor;
    private boolean emojiPickerOpen;
    private float emojiPickerAnimation;
    private int selectedEmojiCategoryIndex;
    private float emojiGridScrollOffset;
    private boolean friendDetailsOpen;
    private float friendDetailsAnimation;
    private boolean confirmFriendRemoval;
    private String toastMessage = "";
    private long toastExpirationTime;
    private float friendListScrollOffset;
    private float friendListScrollTarget;
    private float friendListMaxScroll;
    private float animatedMessageScrollOffset;
    private float messageScrollOffset;
    private float messageMaxScroll;
    private float messageContentWidth;
    private String activeConversationId;
    private int lastChatRevision = -1;
    private long firstVisibleMessageId = -1L;
    private float previousMessageContentHeight;
    private float l1IIlIIlI1IIII1l1I1lI;
    private Chat contextMenuChat;
    private float contextMenuX;
    private float contextMenuY;
    private float panelX;
    private float panelY;
    private float panelWidth;
    private float panelHeight;
    private final List<Myid.ChatOverlayAction> pendingClickActions = new ArrayList<>();
    private final List<Myid.ChatOverlayAction> contextMenuActions = new ArrayList<>();
// https://t.me/pasterplace

    public void updateAnimations(float frameDelta) {
        this.animationFrameDelta = frameDelta;
        float interpolationFactor = 1.0F - (float)Math.exp(-12.0F * frameDelta);
        this.friendListScrollTarget = Math.clamp(this.friendListScrollTarget, 0.0F, this.friendListMaxScroll);
        this.friendListScrollOffset = this.friendListScrollOffset + (this.friendListScrollTarget - this.friendListScrollOffset) * interpolationFactor;
        this.messageScrollOffset = Math.clamp(this.messageScrollOffset, 0.0F, Math.max(0.0F, this.messageMaxScroll));
        this.animatedMessageScrollOffset = this.animatedMessageScrollOffset + (this.messageScrollOffset - this.animatedMessageScrollOffset) * interpolationFactor;
        this.emojiPickerAnimation = this.emojiPickerAnimation + ((this.emojiPickerOpen ? 1.0F : 0.0F) - this.emojiPickerAnimation) * (1.0F - (float)Math.exp(-16.0F * frameDelta));
        this.friendDetailsAnimation = this.friendDetailsAnimation
        + ((this.friendDetailsOpen ? 1.0F : 0.0F) - this.friendDetailsAnimation) * (1.0F - (float)Math.exp(-14.0F * frameDelta));
    }
// https://t.me/pasterplace

    public void render(
    DrawContext drawContext, float mouseX, float mouseY, float panelOriginX, float panelOriginY, float panelRenderWidth, float panelRenderHeight, float opacity
    ) {
        if (!(opacity <= 0.0F)) {
            this.socialState.start();
            this.socialState.markSelectedChatReadIfUnread();
            this.panelX = panelOriginX;
            this.panelY = panelOriginY;
            this.panelWidth = panelRenderWidth;
            this.panelHeight = panelRenderHeight;
            this.pendingClickActions.clear();
            this.contextMenuActions.clear();
            long currentTimeMillis = System.currentTimeMillis();
            this.renderNavigationTabs(drawContext, mouseX, mouseY, currentTimeMillis, opacity);
            RoundedGradientRectBuilder.at(panelOriginX + 112.0F, panelOriginY, 0.75F, panelRenderHeight)
            .cornerRadius(0.0F)
            .color(Accent.getTextColorWithAlpha((int)(12.0F * opacity)))
            .render(drawContext);
            float contentOriginX = panelOriginX + 112.0F + 0.75F;
            float contentWidth = panelRenderWidth - 112.0F - 0.75F;
            drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(contentOriginX),
            PixelRounding.II1Il11l111II11IIl(panelOriginY),
            PixelRounding.l1I1IlllIlI(contentOriginX + contentWidth),
            PixelRounding.l1I1IlllIlI(panelOriginY + panelRenderHeight)
            );

            try {
                if (this.selectedTabIndex == 2) {
                    this.renderProfileTab(drawContext, mouseX, mouseY, contentOriginX, contentWidth, currentTimeMillis, opacity);
                } else if (this.socialState.getSelectedChatId() == null) {
                    this.renderEmptyStateIcon(drawContext, contentOriginX, contentWidth, opacity);
                } else {
                    this.renderConversationHeader(drawContext, mouseX, mouseY, contentOriginX, contentWidth, currentTimeMillis, opacity);
                }

                this.renderConnectionState(drawContext, contentOriginX, contentWidth, opacity);
            } finally {
                drawContext.disableScissor();
            }

            if (this.contextMenuChat != null) {
                this.renderContextMenu(drawContext, mouseX, mouseY, opacity);
            }
        }
    }
// https://t.me/pasterplace

    private void renderNavigationTabs(DrawContext drawContext, float mouseX, float mouseY, long currentTimeMillis, float opacity) {
        float tabBarX = this.panelX + 5.0F;
        float tabBarWidth = 102.0F;
        float tabBarY = this.panelY + 4.0F;
        float tabHeight = 12.0F;
        float tabWidth = tabBarWidth / TAB_LABELS.length;
        RoundedGradientRectBuilder.at(tabBarX, tabBarY, tabBarWidth, tabHeight)
        .cornerRadius(tabHeight * 0.5F)
        .color(Accent.getTextColorWithAlpha((int)(8.0F * opacity)))
        .render(drawContext);
        float selectedTabIndicatorX = tabBarX + tabWidth * this.selectedTabIndex;
        this.tabIndicatorX = this.tabIndicatorX < 0.0F
        ? selectedTabIndicatorX
        : this.tabIndicatorX + (selectedTabIndicatorX - this.tabIndicatorX) * (1.0F - (float)Math.exp(-14.0F * this.animationFrameDelta));
        RoundedGradientRectBuilder.at(this.tabIndicatorX + 1.0F, tabBarY + 1.0F, tabWidth - 2.0F, tabHeight - 2.0F)
        .cornerRadius((tabHeight - 2.0F) * 0.5F)
        .color(Accent.getAccentColorWithAlpha((int)(90.0F * opacity)))
        .render(drawContext);

        for (int tabIndex = 0; tabIndex < TAB_LABELS.length; tabIndex++) {
            float tabOriginX = tabBarX + tabWidth * tabIndex;
            float tabHoverProgress = this.animateHover("tab" + tabIndex, containsPoint(mouseX, mouseY, tabOriginX, tabBarY, tabWidth, tabHeight) || this.selectedTabIndex == tabIndex, 10.0F);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, TAB_LABELS[tabIndex], tabOriginX + tabWidth * 0.5F, tabBarY + tabHeight * 0.5F, 4.3F)
            .centered()
            .color(
            ColorUtils.interpolateColor(
            Accent.getDeepTextColor((int)(200.0F * opacity)).toArgb(), Accent.getTextColorWithAlpha((int)(255.0F * opacity)).toArgb(), tabHoverProgress
            )
            )
            .render(drawContext);
            if (tabIndex == 1 && !this.socialState.getIncomingFriendRequests().isEmpty()) {
                this.renderUnreadCountBadge(drawContext, tabOriginX + tabWidth - 3.5F, tabBarY + 1.5F, this.socialState.getIncomingFriendRequests().size(), opacity);
            }
// https://t.me/pasterplace

            int capturedTabIndex = tabIndex;
            this.pendingClickActions.add(new Myid.ChatOverlayAction(tabOriginX, tabBarY, tabWidth, tabHeight, () -> this.selectTab(capturedTabIndex), null));
        }

        float searchFieldY = this.panelY + 20.0F;
        float searchFieldHeight = 12.0F;
        if (this.selectedTabIndex == 1) {
            float searchFieldInset = searchFieldHeight;
            this.friendSearchField.render(drawContext, this.panelX + 5.0F, searchFieldY, 99.0F - searchFieldInset, searchFieldHeight, currentTimeMillis, opacity);
            float addFriendButtonX = this.panelX + 112.0F - 5.0F - searchFieldInset;
            float addFriendButtonHover = this.animateHover("add", containsPoint(mouseX, mouseY, addFriendButtonX, searchFieldY, searchFieldInset, searchFieldHeight), 8.0F);
            RoundedGradientRectBuilder.at(addFriendButtonX, searchFieldY, searchFieldInset, searchFieldHeight)
            .cornerRadius(Accent.getInstance().getCornerRadius() * 0.5F)
            .color(
            ColorUtils.interpolateColor(
            Accent.getAccentColorWithAlpha((int)(70.0F * opacity)).toArgb(), Accent.getAccentColorWithAlpha((int)(160.0F * opacity)).toArgb(), addFriendButtonHover
            )
            )
            .render(drawContext);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "+", addFriendButtonX + searchFieldInset * 0.5F, searchFieldY + searchFieldHeight * 0.5F, 7.0F)
            .centered()
            .color(Accent.getTextColorWithAlpha((int)(255.0F * opacity)))
            .render(drawContext);
            this.pendingClickActions
            .add(new Myid.ChatOverlayAction(addFriendButtonX, searchFieldY, searchFieldInset, searchFieldHeight, () -> this.sendFriendRequest(this.friendSearchField.getText()), null));
        } else {
            this.chatSearchField.render(drawContext, this.panelX + 5.0F, searchFieldY, 102.0F, searchFieldHeight, currentTimeMillis, opacity);
            if (this.chatSearchField.getText().isEmpty() && !this.chatSearchField.isFocused()) {
                GuiTextBuilder.createAt(Medium.ICON_FONT, "M", this.panelX + 112.0F - 11.0F, searchFieldY + searchFieldHeight * 0.5F, 4.6F)
                .centered()
                .color(Accent.getDeepTextColor((int)(150.0F * opacity)))
                .render(drawContext);
            }
        }

        float contentOriginY = this.panelY + 36.0F;
        float contentHeight = this.panelY + this.panelHeight - contentOriginY - 2.0F;
        drawContext.enableScissor(
        PixelRounding.II1Il11l111II11IIl(this.panelX),
        PixelRounding.II1Il11l111II11IIl(contentOriginY),
        PixelRounding.l1I1IlllIlI(this.panelX + 112.0F),
        PixelRounding.l1I1IlllIlI(contentOriginY + contentHeight)
        );

        try {
            float visibleContentHeight = this.selectedTabIndex == 1
            ? this.renderFriendRequests(drawContext, mouseX, mouseY, contentOriginY, contentHeight, opacity)
            : this.renderConversationList(drawContext, mouseX, mouseY, contentOriginY, contentHeight, currentTimeMillis, opacity);
            this.friendListMaxScroll = Math.max(0.0F, visibleContentHeight - contentHeight);
        } finally {
            drawContext.disableScissor();
        }

        if (this.toastMessage.length() > 0 && currentTimeMillis < this.toastExpirationTime) {
            float toastWidth = Math.min(102.0F, Medium.DEFAULT_FONT.measureWidth(this.toastMessage, 4.2F) + 10.0F);
            float toastY = this.panelY + this.panelHeight - 16.0F;
            RoundedGradientRectBuilder.at(this.panelX + (112.0F - toastWidth) * 0.5F, toastY, toastWidth, 11.0F)
            .cornerRadius(5.5F)
            .color(RgbaColor.rgba(20, 20, 24, (int)(225.0F * opacity)))
            .render(drawContext);
            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT,
            Medium.DEFAULT_FONT.truncateToWidth(this.toastMessage, 4.2F, toastWidth - 6.0F),
            this.panelX + 56.0F,
            toastY + 5.5F,
            4.2F
            )
            .centered()
            .color(Accent.getTextColorWithAlpha((int)(240.0F * opacity)))
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    private float renderConversationList(DrawContext drawContext, float mouseX, float mouseY, float contentOriginY, float contentHeight, long currentTimeMillis, float opacity) {
        ArrayList conversationIds = new ArrayList();
        String searchQuery = this.chatSearchField.getText().trim().toLowerCase(Locale.ROOT);
        if (searchQuery.isEmpty() || "общий чат".contains(searchQuery) || "global".contains(searchQuery)) {
            conversationIds.add("global");
        }

        List<LastSeen> contactsByRecentMessage = new ArrayList<>(this.socialState.getFriends());
        contactsByRecentMessage.sort((lastSeen, lastSeen2) -> {
            Chat chat = this.socialState.getLatestMessage(lastSeen.userId);
            Chat chat2 = this.socialState.getLatestMessage(lastSeen2.userId);
            long firstChatTimestamp = chat == null ? 0L : chat.getTimestamp();
            long secondChatTimestamp = chat2 == null ? 0L : chat2.getTimestamp();
            if (firstChatTimestamp != secondChatTimestamp) {
                return Long.compare(secondChatTimestamp, firstChatTimestamp);
            } else if (lastSeen.isOnline != lastSeen2.isOnline) {
                return lastSeen.isOnline ? -1 : 1;
            } else {
                return lastSeen.username.compareToIgnoreCase(lastSeen2.username);
            }
        });

        for (LastSeen contact : contactsByRecentMessage) {
            if (searchQuery.isEmpty()
            || contact.username.toLowerCase(Locale.ROOT).contains(searchQuery)
            || contact.displayNickname.toLowerCase(Locale.ROOT).contains(searchQuery)) {
                conversationIds.add(contact.userId);
            }
        }

        float rowY = contentOriginY - this.friendListScrollOffset;

        for (Object conversationId : conversationIds) {
            if (rowY + 22.0F >= contentOriginY && rowY <= contentOriginY + contentHeight) {
                this.renderConversationEntry(drawContext, mouseX, mouseY, (String)conversationId, rowY, contentOriginY, contentHeight, currentTimeMillis, opacity);
            }
// https://t.me/pasterplace

            rowY += 23.0F;
        }

        if (this.socialState.getFriends().isEmpty() && searchQuery.isEmpty()) {
            float emptyStateTextY = rowY + 10.0F;
// https://t.me/pasterplace

            for (String emptyStateHint : new String[]{"Пока нет друзей", "Добавьте их во вкладке", "«Заявки» по ID или имени"}) {
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, emptyStateHint, this.panelX + 56.0F, emptyStateTextY, 4.2F)
                .centered()
                .color(Accent.getDeepTextColor((int)(160.0F * opacity)))
                .render(drawContext);
                emptyStateTextY += 6.5F;
            }
        }

        return conversationIds.size() * 23.0F + 4.0F;
    }
// https://t.me/pasterplace

    private void renderConversationEntry(
    DrawContext drawContext, float mouseX, float mouseY, String conversationId, float rowY, float contentOriginY, float contentHeight, long currentTimeMillis, float opacity
    ) {

        boolean isGlobalConversation = "global".equals(conversationId);
        LastSeen contact = isGlobalConversation ? null : this.socialState.findFriendByName(conversationId);
        if (isGlobalConversation || contact != null) {
            boolean isSelected = conversationId.equals(this.socialState.getSelectedChatId()) && this.selectedTabIndex != 2;
            boolean isHovered = containsPoint(mouseX, mouseY, this.panelX + 3.0F, rowY, 106.0F, 22.0F)
            && mouseY >= contentOriginY
            && mouseY <= contentOriginY + contentHeight;
            float hoverProgress = this.animateHover("row" + conversationId, isHovered, 12.0F);
            float selectionProgress = this.animateHover("sel" + conversationId, isSelected, 12.0F);
            RoundedGradientRectBuilder.at(this.panelX + 3.0F, rowY, 106.0F, 22.0F)
            .cornerRadius(Accent.getInstance().getCornerRadius() * 0.55F)
            .color(
            selectionProgress > 0.01F
            ? Accent.getAccentColorWithAlpha((int)((20.0F + 60.0F * selectionProgress) * opacity))
            : Accent.getTextColorWithAlpha((int)(8.0F * hoverProgress * opacity))
            )
            .render(drawContext);
            float avatarSize = 15.0F;
            float avatarX = this.panelX + 6.5F;
            float avatarY = rowY + (22.0F - avatarSize) * 0.5F;
            this.renderFallbackAvatar(drawContext, contact, avatarX, avatarY, avatarSize, opacity);
            if (contact != null && contact.isOnline) {
                this.renderOnlineIndicator(drawContext, avatarX + avatarSize - 3.6F, avatarY + avatarSize - 3.6F, opacity);
            }
// https://t.me/pasterplace

            float textOriginX = avatarX + avatarSize + 4.0F;
            float trailingTextX = this.panelX + 112.0F - 6.0F;
            Chat chat = this.socialState.getLatestMessage(conversationId);
            String latestMessageTimeText = chat == null ? "" : this.formatMessageTime(chat.getTimestamp());
            float latestMessageTimeWidth = Medium.DEFAULT_FONT.measureWidth(latestMessageTimeText, 3.7F);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, latestMessageTimeText, trailingTextX - latestMessageTimeWidth, rowY + 6.5F, 3.7F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(170.0F * opacity)))
            .render(drawContext);
            String conversationTitle = isGlobalConversation ? "Общий чат" : contact.username;
            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(conversationTitle, 4.8F, trailingTextX - latestMessageTimeWidth - 3.0F - textOriginX), textOriginX, rowY + 6.5F, 4.8F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(250.0F * opacity)))
            .render(drawContext);
            int unreadCount = this.socialState.getUnreadCount(conversationId);
            float previewMaxWidth = trailingTextX - textOriginX - (unreadCount > 0 ? 10.0F : 0.0F);
            List typingUserNames = this.socialState.getTypingPlayerNames(isGlobalConversation ? "global" : conversationId);
            float previewY = rowY + 15.0F;
            if (!typingUserNames.isEmpty()) {
                String typingStatusText = isGlobalConversation ? (String)typingUserNames.get(0) + " печатает" : "печатает";
                GuiTextBuilder.createAt(
                Medium.DEFAULT_FONT,
                Medium.DEFAULT_FONT.truncateToWidth(typingStatusText + loadingDots(currentTimeMillis), 4.1F, previewMaxWidth),
                textOriginX,
                previewY,
                4.1F
                )
                .baselinePosition(TextBaselinePosition.CENTER)
                .color(Accent.getAccentColorWithAlpha((int)(230.0F * opacity)))
                .render(drawContext);
            } else if (chat != null) {
                LastSeen currentUserPresence = this.socialState.getCurrentUser();
                String senderPrefix = currentUserPresence != null && currentUserPresence.userId.equals(chat.getSenderId())
                ? "Вы: "
                : (isGlobalConversation ? chat.getSenderName() + ": " : "");
                float senderPrefixWidth = Medium.DEFAULT_FONT.measureWidth(senderPrefix, 4.1F);
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(senderPrefix, 4.1F, previewMaxWidth), textOriginX, previewY, 4.1F)
                .baselinePosition(TextBaselinePosition.CENTER)
                .color(Accent.getAccentColorWithAlpha((int)(200.0F * opacity)))
                .render(drawContext);
                String messagePreviewText = EmojiTextRenderer.truncateText(chat.getMessage().replace('\n', ' '), 4.1F, Math.max(4.0F, previewMaxWidth - senderPrefixWidth));
                EmojiTextRenderer.renderLine(
                drawContext,
                EmojiTextRenderer.layoutLines(messagePreviewText, 4.1F, Float.MAX_VALUE).get(0),
                textOriginX + senderPrefixWidth,
                previewY,
                4.1F,
                Accent.getDeepTextColor((int)(190.0F * opacity)).toArgb(),
                opacity
                );
            } else {
                String presenceText = isGlobalConversation
                ? this.socialState.getGlobalOnlineCount() + " в сети"
                : (contact.isOnline ? "в сети" : this.formatLastSeen(contact.lastSeenAt));
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, presenceText, textOriginX, previewY, 4.1F)
                .baselinePosition(TextBaselinePosition.CENTER)
                .color(Accent.getDeepTextColor((int)(170.0F * opacity)))
                .render(drawContext);
            }

            if (unreadCount > 0) {
                this.renderUnreadCountBadge(drawContext, trailingTextX - 3.0F, previewY - 3.5F, unreadCount, opacity);
            }

            if (mouseY >= contentOriginY && mouseY <= contentOriginY + contentHeight) {
                this.pendingClickActions
                .add(new Myid.ChatOverlayAction(this.panelX + 3.0F, rowY, 106.0F, 22.0F, () -> this.selectConversation(conversationId), null));
            }
        }
    }
// https://t.me/pasterplace

    private float renderFriendRequests(DrawContext drawContext, float mouseX, float mouseY, float viewportTop, float viewportHeight, float opacity) {
        float contentY = viewportTop - this.friendListScrollOffset;
        float contentStartY = contentY;
        contentY = this.renderFriendList(
        drawContext, mouseX, mouseY, "Входящие", this.socialState.getIncomingFriendRequests(), true, contentY, viewportTop, viewportHeight, opacity
        );
        contentY += 4.0F;
        contentY = this.renderFriendList(drawContext, mouseX, mouseY, "Исходящие", this.socialState.getOutgoingFriendRequests(), false, contentY, viewportTop, viewportHeight, opacity);
        contentY += 6.0F;
        LastSeen currentUserPresence = this.socialState.getCurrentUser();
        if (currentUserPresence != null) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Ваш ID для друзей:", this.panelX + 56.0F, contentY + 3.0F, 4.0F)
            .centered()
            .color(Accent.getDeepTextColor((int)(160.0F * opacity)))
            .render(drawContext);
            contentY += 8.0F;
            float currentUserIdButtonWidth = Medium.DEFAULT_FONT.measureWidth("#" + currentUserPresence.getFormattedUserId(), 5.2F) + 12.0F;
            float currentUserIdButtonX = this.panelX + (112.0F - currentUserIdButtonWidth) * 0.5F;
            float hoverProgress = this.animateHover("myid", containsPoint(mouseX, mouseY, currentUserIdButtonX, contentY, currentUserIdButtonWidth, 11.0F), 10.0F);
            RoundedGradientRectBuilder.at(currentUserIdButtonX, contentY, currentUserIdButtonWidth, 11.0F)
            .cornerRadius(5.5F)
            .color(Accent.getAccentColorWithAlpha((int)((40.0F + 60.0F * hoverProgress) * opacity)))
            .render(drawContext);
            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT, "#" + currentUserPresence.getFormattedUserId(), this.panelX + 56.0F, contentY + 5.5F, 5.2F
            )
            .centered()
            .color(Accent.getTextColorWithAlpha((int)(255.0F * opacity)))
            .render(drawContext);
            this.pendingClickActions
            .add(new Myid.ChatOverlayAction(currentUserIdButtonX, contentY, currentUserIdButtonWidth, 11.0F, () -> this.copyToClipboardWithToast(currentUserPresence.getFormattedUserId(), "ID скопирован"), null));
            contentY += 14.0F;
        }

        return contentY - contentStartY;
    }
// https://t.me/pasterplace

    private float renderFriendList(
    DrawContext drawContext,
    float mouseX,
    float mouseY,
    String sectionTitle,
    List<LastSeen> contacts,
    boolean isIncomingRequests,
    float rowY,
    float viewportTop,
    float viewportHeight,
    float opacity
    ) {
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, sectionTitle + " · " + contacts.size(), this.panelX + 7.0F, rowY + 4.5F, 4.2F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(170.0F * opacity)))
        .render(drawContext);
        rowY += 10.0F;
        if (contacts.isEmpty()) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Пусто", this.panelX + 7.0F, rowY + 3.5F, 4.0F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(110.0F * opacity)))
            .render(drawContext);
            return rowY + 9.0F;
        }

        for (LastSeen contact : contacts) {
            RoundedGradientRectBuilder.at(this.panelX + 3.0F, rowY, 106.0F, 18.0F)
            .cornerRadius(Accent.getInstance().getCornerRadius() * 0.5F)
            .color(Accent.getTextColorWithAlpha((int)(7.0F * opacity)))
            .render(drawContext);
            this.renderFallbackAvatar(drawContext, (LastSeen)contact, this.panelX + 6.0F, rowY + 3.0F, 12.0F, opacity);
            float actionColumnRightX = this.panelX + 112.0F - 6.0F;
            float contactNameMaxWidth = actionColumnRightX - (isIncomingRequests ? 24.0F : 12.0F) - (this.panelX + 21.0F);
            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT,
            Medium.DEFAULT_FONT.truncateToWidth(contact.username, 4.6F, contactNameMaxWidth),
            this.panelX + 21.0F,
            rowY + 6.0F,
            4.6F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(245.0F * opacity)))
            .render(drawContext);
            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT, "#" + contact.getFormattedUserId(), this.panelX + 21.0F, rowY + 12.5F, 3.6F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(160.0F * opacity)))
            .render(drawContext);
            boolean mouseWithinViewport = mouseY >= viewportTop && mouseY <= viewportTop + viewportHeight;
            float actionCenterY = rowY + 9.0F;
            if (isIncomingRequests) {
                this.renderIconAction(drawContext, mouseX, mouseY, "acc" + contact.userId, actionColumnRightX - 17.0F, actionCenterY, true, opacity);
                this.renderCheckmark(drawContext, actionColumnRightX - 17.0F - 2.0F, actionCenterY + 0.2F, 1.8F, Accent.getTextColorWithAlpha((int)(255.0F * opacity)).toArgb());
                if (mouseWithinViewport) {
                    this.pendingClickActions
                    .add(
                    new Myid.ChatOverlayAction(
                    actionColumnRightX - 22.0F,
                    actionCenterY - 5.0F,
                    10.0F,
                    10.0F,
                    () -> this.socialState.updateFriendRelationship("accept", contact.userId, this::showToast),
                    null
                    )
                    );
                }
            }

            this.renderIconAction(drawContext, mouseX, mouseY, "dec" + contact.userId, actionColumnRightX - 5.0F, actionCenterY, false, opacity);
            this.renderCross(drawContext, actionColumnRightX - 5.0F, actionCenterY, 1.9F, Accent.getTextColorWithAlpha((int)(230.0F * opacity)).toArgb());
            if (mouseWithinViewport) {
                this.pendingClickActions
                .add(
                new Myid.ChatOverlayAction(
                actionColumnRightX - 10.0F,
                actionCenterY - 5.0F,
                10.0F,
                10.0F,
                () -> this.socialState.updateFriendRelationship("decline", contact.userId, this::showToast),
                null
                )
                );
            }

            rowY += 20.0F;
        }

        return rowY;
    }
// https://t.me/pasterplace

    private void renderEmptyStateIcon(DrawContext drawContext, float contentOriginX, float contentWidth, float opacity) {
        float centerX = contentOriginX + contentWidth * 0.5F;
        float centerY = this.panelY + this.panelHeight * 0.5F;
        RoundedGradientRectBuilder.at(centerX - 14.0F, centerY - 26.0F, 28.0F, 28.0F)
        .cornerRadius(14.0F)
        .color(Accent.getAccentColorWithAlpha((int)(40.0F * opacity)))
        .render(drawContext);
        GuiTextBuilder.createAt(Medium.ICON_FONT, "G", centerX, centerY - 12.0F, 12.0F)
        .centered()
        .color(Accent.getAccentColorWithAlpha((int)(230.0F * opacity)))
        .render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Globals", centerX, centerY + 8.0F, 6.4F)
        .centered()
        .color(Accent.getTextColorWithAlpha((int)(250.0F * opacity)))
        .render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Выберите чат, чтобы начать общение", centerX, centerY + 16.0F, 4.4F)
        .centered()
        .color(Accent.getDeepTextColor((int)(180.0F * opacity)))
        .render(drawContext);
        LastSeen currentUserPresence = this.socialState.getCurrentUser();
        if (currentUserPresence != null) {
            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT, "Вы: " + currentUserPresence.username + "  ·  #" + currentUserPresence.getFormattedUserId(), centerX, centerY + 24.0F, 4.0F
            )
            .centered()
            .color(Accent.getDeepTextColor((int)(140.0F * opacity)))
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    private void renderConnectionState(DrawContext drawContext, float contentOriginX, float contentWidth, float opacity) {
        if (this.socialState.getConnectionState() != GlobalsClient.ConnectionState.CONNECTED) {
            String connectionStatus = this.socialState.getConnectionStatusMessage();
            float statusPillWidth = Math.min(contentWidth - 20.0F, Medium.DEFAULT_FONT.measureWidth(connectionStatus, 4.2F) + 16.0F);
            float statusPillX = contentOriginX + (contentWidth - statusPillWidth) * 0.5F;
            float statusPillY = this.panelY + this.panelHeight - 16.0F - 22.0F;
            RoundedGradientRectBuilder.at(statusPillX, statusPillY, statusPillWidth, 11.0F)
            .cornerRadius(5.5F)
            .color(RgbaColor.rgba(200, 60, 60, (int)(200.0F * opacity)))
            .render(drawContext);
            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(connectionStatus, 4.2F, statusPillWidth - 8.0F), statusPillX + statusPillWidth * 0.5F, statusPillY + 5.5F, 4.2F
            )
            .centered()
            .color(RgbaColor.rgba(255, 255, 255, (int)(250.0F * opacity)))
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    private void renderConversationHeader(DrawContext drawContext, float mouseX, float mouseY, float contentOriginX, float contentWidth, long currentTimeMillis, float opacity) {
        String conversationId = this.socialState.getSelectedChatId();

        boolean isGlobalConversation = "global".equals(conversationId);
        LastSeen contact = isGlobalConversation ? null : this.socialState.findFriendByName(conversationId);
        if (!isGlobalConversation && contact == null) {
            this.renderEmptyStateIcon(drawContext, contentOriginX, contentWidth, opacity);
        } else {
            float headerY = this.panelY;
            float hoverProgress = this.animateHover("header", containsPoint(mouseX, mouseY, contentOriginX, headerY, contentWidth, 21.0F), 10.0F);
            RoundedGradientRectBuilder.at(contentOriginX, headerY, contentWidth, 21.0F)
            .cornerRadius(0.0F)
            .color(Accent.getTextColorWithAlpha((int)((5.0F + 5.0F * hoverProgress) * opacity)))
            .render(drawContext);
            RoundedGradientRectBuilder.at(contentOriginX, headerY + 21.0F, contentWidth, 0.6F)
            .cornerRadius(0.0F)
            .color(Accent.getTextColorWithAlpha((int)(12.0F * opacity)))
            .render(drawContext);
            float avatarSize = 14.0F;
            this.renderFallbackAvatar(drawContext, contact, contentOriginX + 7.0F, headerY + (21.0F - avatarSize) * 0.5F, avatarSize, opacity);
            float titleX = contentOriginX + 7.0F + avatarSize + 5.0F;
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, isGlobalConversation ? "Общий чат" : contact.username, titleX, headerY + 7.0F, 5.2F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(255.0F * opacity)))
            .render(drawContext);
            List<String> typingUserNames = this.socialState.getTypingPlayerNames(isGlobalConversation ? "global" : conversationId);
            boolean isStatusHighlighted = false;
            String presenceText;
            if (!typingUserNames.isEmpty()) {
                presenceText = (isGlobalConversation ? String.join(", ", typingUserNames) + " печатает" : "печатает") + loadingDots(currentTimeMillis);
                isStatusHighlighted = true;
            } else if (isGlobalConversation) {
                presenceText = this.socialState.getGlobalOnlineCount() + " в сети · " + this.socialState.getGlobalMemberCount() + " участников";
            } else if (contact.isOnline) {
                presenceText = contact.serverName.isEmpty() ? "в сети" : "в сети · " + contact.serverName;
                isStatusHighlighted = true;
            } else {
                presenceText = this.formatLastSeen(contact.lastSeenAt);
            }

            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT,
            Medium.DEFAULT_FONT.truncateToWidth(presenceText, 4.0F, contentWidth - (titleX - contentOriginX) - 20.0F),
            titleX,
            headerY + 14.2F,
            4.0F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(isStatusHighlighted ? Accent.getAccentColorWithAlpha((int)(230.0F * opacity)) : Accent.getDeepTextColor((int)(180.0F * opacity)))
            .render(drawContext);
            if (!isGlobalConversation) {
                float infoButtonX = contentOriginX + contentWidth - 12.0F;
                float infoHoverProgress = this.animateHover("info", containsPoint(mouseX, mouseY, infoButtonX - 5.0F, headerY + 5.0F, 10.0F, 10.0F) || this.friendDetailsOpen, 10.0F);
// https://t.me/pasterplace

                for (int infoDotIndex = -1; infoDotIndex <= 1; infoDotIndex++) {
                    RoundedGradientRectBuilder.at(infoButtonX - 0.8F, headerY + 10.5F + infoDotIndex * 2.6F - 0.8F, 1.6F, 1.6F)
                    .cornerRadius(0.8F)
                    .color(
                    ColorUtils.interpolateColor(
                    Accent.getDeepTextColor((int)(200.0F * opacity)).toArgb(), Accent.getAccentColorWithAlpha((int)(255.0F * opacity)).toArgb(), infoHoverProgress
                    )
                    )
                    .render(drawContext);
                }

                this.pendingClickActions.add(new Myid.ChatOverlayAction(contentOriginX, headerY, contentWidth, 21.0F, () -> {
                    this.friendDetailsOpen = !this.friendDetailsOpen;
                    this.confirmFriendRemoval = false;
                }, null));
            }

            float messagesY = headerY + 21.0F + 0.6F;
            float messagesHeight = this.panelY + this.panelHeight - 16.0F - 6.0F - messagesY;
            this.renderConversationMessages(drawContext, mouseX, mouseY, conversationId, isGlobalConversation, contact, contentOriginX, contentWidth, messagesY, messagesHeight, opacity);
            this.renderMessageComposer(drawContext, mouseX, mouseY, conversationId, contentOriginX, contentWidth, currentTimeMillis, opacity);
            if (this.emojiPickerAnimation > 0.01F) {
                this.renderEmojiPicker(drawContext, mouseX, mouseY, contentOriginX, contentWidth, opacity * this.emojiPickerAnimation);
            }

            if (this.friendDetailsAnimation > 0.01F && contact != null) {
                this.renderFriendCard(drawContext, mouseX, mouseY, contact, contentOriginX, contentWidth, messagesY, opacity * this.friendDetailsAnimation);
            }
        }
    }
// https://t.me/pasterplace

    private void renderConversationMessages(
    DrawContext drawContext,
    float mouseX,
    float mouseY,
    String conversationId,
    boolean isGlobalConversation,
    LastSeen contact,
    float contentOriginX,
    float contentWidth,
    float messagesY,
    float messagesHeight,
    float opacity
    ) {
        GlobalsClient.ChatSessionState chatSessionState = this.socialState.getOrCreateChatSession(conversationId);
        float messageTextMaxWidth = Math.max(60.0F, contentWidth * 0.68F);
        if (this.messageContentWidth != messageTextMaxWidth || !conversationId.equals(this.activeConversationId)) {
            this.messageLayoutCache.clear();
            this.messageContentWidth = messageTextMaxWidth;
            if (!conversationId.equals(this.activeConversationId)) {
                this.animatedMessageScrollOffset = 0.0F;
                this.messageScrollOffset = 0.0F;
                this.lastChatRevision = -1;
            }

            this.activeConversationId = conversationId;
        }

        List chatMessages = chatSessionState.messages;
        LastSeen currentUser = this.socialState.getCurrentUser();
        String currentUserId = currentUser == null ? "" : currentUser.userId;
        float totalMessageContentHeight = 6.0F;
        float[] messageRowHeights = new float[chatMessages.size()];
        boolean[] needsDateHeader = new boolean[chatMessages.size()];
        boolean[] messageGroupingFlags = new boolean[chatMessages.size()];

        for (int messageIndex = 0; messageIndex < chatMessages.size(); messageIndex++) {
            Object currentMessage = (Chat)chatMessages.get(messageIndex);
            Chat previousMessage = messageIndex > 0 ? (Chat)chatMessages.get(messageIndex - 1) : null;
            needsDateHeader[messageIndex] = previousMessage == null || !isSameDay(previousMessage.getTimestamp(), ((Chat)currentMessage).getTimestamp());
            boolean canGroupWithPrevious = previousMessage != null
            && !needsDateHeader[messageIndex]
            && previousMessage.getSenderId().equals(((Chat)currentMessage).getSenderId())
            && ((Chat)currentMessage).getTimestamp() - previousMessage.getTimestamp() < 120000L;
            messageGroupingFlags[messageIndex] = canGroupWithPrevious;
            boolean showSenderName = isGlobalConversation && !currentUserId.equals(((Chat)currentMessage).getSenderId()) && !canGroupWithPrevious;
            Object messageLayoutData = this.messageLayoutCache
            .computeIfAbsent(((Chat)currentMessage).getMessageId() * 2L + (showSenderName ? 1L : 0L), chatEntryKey -> this.layoutConversationMessage((Chat)currentMessage, messageTextMaxWidth, showSenderName));
            messageRowHeights[messageIndex] = ((Myid.EmojiLineRenderData)messageLayoutData).height + (canGroupWithPrevious ? 1.5F : 4.0F) + (needsDateHeader[messageIndex] ? 16.0F : 0.0F);
            totalMessageContentHeight += messageRowHeights[messageIndex];
        }

        long oldestMessageId = chatMessages.isEmpty() ? -1L : ((Chat)chatMessages.get(0)).getMessageId();
        if (this.lastChatRevision != chatSessionState.revision) {
            if (this.lastChatRevision >= 0 && oldestMessageId == this.firstVisibleMessageId && this.messageScrollOffset > 2.0F && totalMessageContentHeight > this.previousMessageContentHeight) {
                this.messageScrollOffset = this.messageScrollOffset + (totalMessageContentHeight - this.previousMessageContentHeight);
                this.animatedMessageScrollOffset = this.animatedMessageScrollOffset + (totalMessageContentHeight - this.previousMessageContentHeight);
            }

            this.lastChatRevision = chatSessionState.revision;
            this.firstVisibleMessageId = oldestMessageId;
            this.previousMessageContentHeight = totalMessageContentHeight;
        }

        this.messageMaxScroll = totalMessageContentHeight - messagesHeight;
        if (this.messageMaxScroll > 0.0F && this.messageScrollOffset >= this.messageMaxScroll - 12.0F) {
            this.socialState.loadOlderChatMessages(conversationId);
        }

        drawContext.enableScissor(
        PixelRounding.II1Il11l111II11IIl(contentOriginX),
        PixelRounding.II1Il11l111II11IIl(messagesY),
        PixelRounding.l1I1IlllIlI(contentOriginX + contentWidth),
        PixelRounding.l1I1IlllIlI(messagesY + messagesHeight)
        );

        try {
            if (!chatSessionState.historyLoaded) {
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Загрузка...", contentOriginX + contentWidth * 0.5F, messagesY + messagesHeight * 0.5F, 4.6F)
                .centered()
                .color(Accent.getDeepTextColor((int)(170.0F * opacity)))
                .render(drawContext);
                return;
            }

            if (chatMessages.isEmpty()) {
                float emptyStateCenterX = contentOriginX + contentWidth * 0.5F;
                float emptyStateCenterY = messagesY + messagesHeight * 0.5F;
                RoundedGradientRectBuilder.at(emptyStateCenterX - 50.0F, emptyStateCenterY - 14.0F, 100.0F, 28.0F)
                .cornerRadius(8.0F)
                .color(Accent.getTextColorWithAlpha((int)(8.0F * opacity)))
                .render(drawContext);
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Здесь пока пусто", emptyStateCenterX, emptyStateCenterY - 4.0F, 4.8F)
                .centered()
                .color(Accent.getTextColorWithAlpha((int)(230.0F * opacity)))
                .render(drawContext);
                Emoji.EmojiEntry emptyStateEmoji = Emoji.findEmojiEntry("ð");
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Напишите первое сообщение", emptyStateCenterX, emptyStateCenterY + 5.0F, 4.0F)
                .centered()
                .color(Accent.getDeepTextColor((int)(170.0F * opacity)))
                .render(drawContext);
                if (emptyStateEmoji != null) {
                    EmojiTextRenderer.renderEmoji(drawContext, emptyStateEmoji, emptyStateCenterX + 44.0F, emptyStateCenterY - 10.0F, 9.0F, opacity);
                }

                return;
            }

            float messageListY = messagesY + messagesHeight - totalMessageContentHeight + this.animatedMessageScrollOffset + 4.0F;
            long lastReadMessageId = this.socialState.getLastReadMessageId(conversationId);

            for (int messageIndex = 0; messageIndex < chatMessages.size(); messageIndex++) {
                Object currentMessage = (Chat)chatMessages.get(messageIndex);
                float messageTopY = messageListY;
                messageListY += messageRowHeights[messageIndex];
                if (!(messageListY < messagesY - 4.0F) && !(messageTopY > messagesY + messagesHeight + 4.0F)) {
                    float messageRenderY = messageTopY;
                    if (needsDateHeader[messageIndex]) {
                        String dateHeaderText = this.formatDateHeader(((Chat)currentMessage).getTimestamp());
                        float dateHeaderWidth = Medium.DEFAULT_FONT.measureWidth(dateHeaderText, 4.0F) + 10.0F;
                        RoundedGradientRectBuilder.at(contentOriginX + (contentWidth - dateHeaderWidth) * 0.5F, messageRenderY + 3.0F, dateHeaderWidth, 9.0F)
                        .cornerRadius(4.5F)
                        .color(RgbaColor.rgba(0, 0, 0, (int)(70.0F * opacity)))
                        .render(drawContext);
                        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, dateHeaderText, contentOriginX + contentWidth * 0.5F, messageRenderY + 7.5F, 4.0F)
                        .centered()
                        .color(Accent.getTextColorWithAlpha((int)(220.0F * opacity)))
                        .render(drawContext);
                        messageRenderY += 16.0F;
                    }

                    boolean sameSenderAsPrevious = messageGroupingFlags[messageIndex];
                    boolean showSenderName = isGlobalConversation && !currentUserId.equals(((Chat)currentMessage).getSenderId()) && !sameSenderAsPrevious;
                    messageRenderY += sameSenderAsPrevious ? 1.5F : 4.0F;
                    this.renderConversationMessage(
                    drawContext,
                    mouseX,
                    mouseY,
                    (Chat)currentMessage,
                    this.messageLayoutCache.get(((Chat)currentMessage).getMessageId() * 2L + (showSenderName ? 1L : 0L)),
                    currentUserId.equals(((Chat)currentMessage).getSenderId()),
                    isGlobalConversation,
                    contentOriginX,
                    contentWidth,
                    messageRenderY,
                    lastReadMessageId,
                    messagesY,
                    messagesHeight,
                    opacity
                    );
                }
            }
        } finally {
            drawContext.disableScissor();
        }

        if (this.animatedMessageScrollOffset > 30.0F) {
            float scrollToBottomButtonX = contentOriginX + contentWidth - 15.0F;
            float scrollToBottomButtonY = messagesY + messagesHeight - 15.0F;
            float scrollButtonHoverProgress = this.animateHover("down", containsPoint(mouseX, mouseY, scrollToBottomButtonX - 6.0F, scrollToBottomButtonY - 6.0F, 12.0F, 12.0F), 10.0F);
            RoundedGradientRectBuilder.at(scrollToBottomButtonX - 6.0F, scrollToBottomButtonY - 6.0F, 12.0F, 12.0F)
            .cornerRadius(6.0F)
            .color(
            ColorUtils.interpolateColor(
            RgbaColor.rgba(30, 30, 36, (int)(220.0F * opacity)).toArgb(),
            Accent.getAccentColorWithAlpha((int)(200.0F * opacity)).toArgb(),
            scrollButtonHoverProgress
            )
            )
            .render(drawContext);
            drawThickLine(
            drawContext, scrollToBottomButtonX - 2.6F, scrollToBottomButtonY - 1.0F, scrollToBottomButtonX, scrollToBottomButtonY + 1.6F, 1.0F, Accent.getTextColorWithAlpha((int)(240.0F * opacity)).toArgb()
            );
            drawThickLine(
            drawContext, scrollToBottomButtonX, scrollToBottomButtonY + 1.6F, scrollToBottomButtonX + 2.6F, scrollToBottomButtonY - 1.0F, 1.0F, Accent.getTextColorWithAlpha((int)(240.0F * opacity)).toArgb()
            );
            this.pendingClickActions.add(new Myid.ChatOverlayAction(scrollToBottomButtonX - 6.0F, scrollToBottomButtonY - 6.0F, 12.0F, 12.0F, () -> this.messageScrollOffset = 0.0F, null));
        }
    }
// https://t.me/pasterplace

    private Myid.EmojiLineRenderData layoutConversationMessage(Chat chat, float availableContentWidth, boolean showSenderName) {
        boolean containsOnlyEmoji = EmojiTextRenderer.isEmojiOnlyText(chat.getMessage());
        if (containsOnlyEmoji) {
            float emojiFontSize = 9.0F;
            List emojiLines = EmojiTextRenderer.layoutLines(chat.getMessage().strip(), emojiFontSize, availableContentWidth);
            float maxEmojiLineWidth = 0.0F;

            for (Object emojiLineLayout : emojiLines) {
                maxEmojiLineWidth = Math.max(maxEmojiLineWidth, ((EmojiTextRenderer.EmojiLineLayout)emojiLineLayout).width());
            }

            return new Myid.EmojiLineRenderData(emojiLines, maxEmojiLineWidth + 4.0F, emojiFontSize * 1.45F + 9.0F + (showSenderName ? 7.0F : 0.0F), true, showSenderName);
        } else {
            List messageLines = EmojiTextRenderer.layoutLines(chat.getMessage(), 4.6F, availableContentWidth - 9.0F);
            float maxMessageLineWidth = 0.0F;

            for (Object messageLineLayout : messageLines) {
                maxMessageLineWidth = Math.max(maxMessageLineWidth, ((EmojiTextRenderer.EmojiLineLayout)messageLineLayout).width());
            }

            float timestampWidth = Medium.DEFAULT_FONT.measureWidth("00:00", 3.4F) + 11.5F;
            float lastLineWidth = ((EmojiTextRenderer.EmojiLineLayout)messageLines.get(messageLines.size() - 1)).width();
            boolean timestampFitsOnLastLine = lastLineWidth + timestampWidth > availableContentWidth - 9.0F;
            float messageContentWidth = Math.max(maxMessageLineWidth, timestampFitsOnLastLine ? timestampWidth : lastLineWidth + timestampWidth);
            if (showSenderName) {
                messageContentWidth = Math.max(messageContentWidth, Medium.DEFAULT_FONT.measureWidth(chat.getSenderName(), 4.2F));
            }

            float bubbleWidth = messageContentWidth + 9.0F;
            float bubbleHeight = messageLines.size() * 6.6F + 6.4F + (timestampFitsOnLastLine ? 5.0F : 0.0F) + (showSenderName ? 7.0F : 0.0F);
            return new Myid.EmojiLineRenderData(messageLines, bubbleWidth, bubbleHeight, false, showSenderName);
        }
    }
// https://t.me/pasterplace

    private void renderConversationMessage(
    DrawContext drawContext,
    float mouseX,
    float mouseY,
    Chat chat,
    Myid.EmojiLineRenderData messageLayout,
    boolean isOutgoing,
    boolean isGlobalConversation,
    float contentOriginX,
    float contentWidth,
    float messageY,
    long lastReadMessageId,
    float viewportTop,
    float viewportHeight,
    float opacity
    ) {
        float senderAvatarSlotWidth = isGlobalConversation && !isOutgoing ? 13.0F : 0.0F;
        float bubbleX = isOutgoing ? contentOriginX + contentWidth - 8.0F - messageLayout.width : contentOriginX + 7.0F + senderAvatarSlotWidth;
        float bubbleCornerRadius = 6.0F;
        if (isGlobalConversation && !isOutgoing && messageLayout.centered) {
            LastSeen lastSeen = this.socialState.findFriendByName(chat.getSenderId());
            if (lastSeen != null) {
                PlayerSkinQuadBuilder.at(contentOriginX + 6.0F, messageY, 10.0F)
                .skinProfile(lastSeen.getGameProfile())
                .fullyRounded()
                .opacity(opacity)
                .render(drawContext);
            } else {
                RoundedGradientRectBuilder.at(contentOriginX + 6.0F, messageY, 10.0F, 10.0F)
                .cornerRadius(5.0F)
                .color(getTextColor(chat.getSenderId(), opacity * 0.85F))
                .render(drawContext);
                GuiTextBuilder.createAt(
                Medium.DEFAULT_FONT, chat.getSenderName().substring(0, 1).toUpperCase(Locale.ROOT), contentOriginX + 11.0F, messageY + 5.0F, 5.0F
                )
                .centered()
                .color(RgbaColor.rgba(255, 255, 255, (int)(250.0F * opacity)))
                .render(drawContext);
            }
        }
// https://t.me/pasterplace

        boolean isPointerOverBubble = containsPoint(mouseX, mouseY, bubbleX, messageY, messageLayout.width, messageLayout.height)
        && mouseY >= viewportTop
        && mouseY <= viewportTop + viewportHeight;
        if (!messageLayout.hasOverflow) {
            int bubbleBackgroundColor = isOutgoing
            ? Accent.getAccentColorWithAlpha((int)(150.0F * opacity)).toArgb()
            : RgbaColor.rgba(255, 255, 255, (int)(20.0F * opacity)).toArgb();
            if (isPointerOverBubble || chat == this.contextMenuChat) {
                bubbleBackgroundColor = isOutgoing
                ? Accent.getAccentColorWithAlpha((int)(185.0F * opacity)).toArgb()
                : RgbaColor.rgba(255, 255, 255, (int)(30.0F * opacity)).toArgb();
            }

            RoundedGradientRectBuilder.at(bubbleX, messageY, messageLayout.width, messageLayout.height)
            .cornerRadii(bubbleCornerRadius, bubbleCornerRadius, isOutgoing ? 2.0F : bubbleCornerRadius, isOutgoing ? bubbleCornerRadius : 2.0F)
            .color(bubbleBackgroundColor)
            .render(drawContext);
        }

        float messageTextY = messageY + 3.2F;
        if (messageLayout.centered) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, chat.getSenderName(), bubbleX + (messageLayout.hasOverflow ? 2.0F : 4.5F), messageTextY + 2.6F, 4.2F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(getTextColor(chat.getSenderId(), opacity))
            .render(drawContext);
            messageTextY += 7.0F;
        }

        if (messageLayout.hasOverflow) {
            float emojiFontSize = 9.0F;
            EmojiTextRenderer.renderLine(
            drawContext, messageLayout.lines.get(0), bubbleX + 2.0F, messageTextY + emojiFontSize * 1.45F * 0.5F, emojiFontSize, -1, opacity
            );
            String messageTimeText = this.messageTimeFormat.format(new Date(chat.getTimestamp()));
            float timestampBadgeWidth = Medium.DEFAULT_FONT.measureWidth(messageTimeText, 3.4F) + 6.0F + (isOutgoing ? 8.5F : 0.0F);
            float timestampBadgeY = messageY + messageLayout.height - 7.5F;
            RoundedGradientRectBuilder.at(bubbleX + messageLayout.width - timestampBadgeWidth, timestampBadgeY, timestampBadgeWidth, 7.0F)
            .cornerRadius(3.5F)
            .color(RgbaColor.rgba(0, 0, 0, (int)(80.0F * opacity)))
            .render(drawContext);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, messageTimeText, bubbleX + messageLayout.width - timestampBadgeWidth + 3.0F, timestampBadgeY + 3.5F, 3.4F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(RgbaColor.rgba(255, 255, 255, (int)(230.0F * opacity)))
            .render(drawContext);
            if (isOutgoing) {
                this.renderStatusGlyph(
                drawContext,
                bubbleX + messageLayout.width - 2.5F,
                timestampBadgeY + 3.5F,
                !isGlobalConversation && lastReadMessageId >= chat.getMessageId(),
                RgbaColor.rgba(255, 255, 255, (int)(230.0F * opacity)).toArgb()
                );
            }
        } else {
            int messageTextColor = isOutgoing
            ? RgbaColor.rgba(255, 255, 255, (int)(250.0F * opacity)).toArgb()
            : Accent.getTextColorWithAlpha((int)(245.0F * opacity)).toArgb();

            for (Object messageLineLayout : messageLayout.lines) {
                EmojiTextRenderer.renderLine(
                drawContext, (EmojiTextRenderer.EmojiLineLayout)messageLineLayout, bubbleX + 4.5F, messageTextY + 3.3F, 4.6F, messageTextColor, opacity
                );
                messageTextY += 6.6F;
            }

            String messageTimeText = this.messageTimeFormat.format(new Date(chat.getTimestamp()));
            float messageTimeX = bubbleX + messageLayout.width - 4.5F - Medium.DEFAULT_FONT.measureWidth(messageTimeText, 3.4F) - (isOutgoing ? 8.5F : 0.0F);
            float messageTimeY = messageY + messageLayout.height - 3.2F - 1.6F;
            int messageStatusColor = isOutgoing
            ? RgbaColor.rgba(255, 255, 255, (int)(190.0F * opacity)).toArgb()
            : Accent.getDeepTextColor((int)(170.0F * opacity)).toArgb();
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, messageTimeText, messageTimeX, messageTimeY, 3.4F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(messageStatusColor)
            .render(drawContext);
            if (isOutgoing) {
                this.renderStatusGlyph(drawContext, bubbleX + messageLayout.width - 4.5F + 0.6F, messageTimeY, !isGlobalConversation && lastReadMessageId >= chat.getMessageId(), messageStatusColor);
            }
        }

        if (isPointerOverBubble) {
            this.pendingClickActions.add(new Myid.ChatOverlayAction(bubbleX, messageY, messageLayout.width, messageLayout.height, null, chat));
        }
    }
// https://t.me/pasterplace

    private void renderMessageComposer(DrawContext drawContext, float mouseX, float mouseY, String conversationId, float contentOriginX, float contentWidth, long currentTimeMillis, float opacity) {
        float composerY = this.panelY + this.panelHeight - 16.0F - 3.0F;
        float composerX = contentOriginX + 5.0F;
        float composerWidth = contentWidth - 10.0F;
        RoundedGradientRectBuilder.at(composerX, composerY, composerWidth, 16.0F)
        .cornerRadius(8.0F)
        .color(RgbaColor.rgba(0, 0, 0, (int)(60.0F * opacity)))
        .render(drawContext);
        RoundedRectBuilder.createAt(composerX, composerY, composerWidth, 16.0F)
        .cornerRadius(8.0F)
        .borderWidth(0.6F)
        .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)((this.showMessageCursor ? 120 : 30) * opacity)))
        .alpha(opacity)
        .render(drawContext);
        float emojiButtonX = composerX + 2.5F;
        float emojiButtonHoverProgress = this.animateHover("emojiBtn", containsPoint(mouseX, mouseY, emojiButtonX, composerY + 2.0F, 12.0F, 12.0F) || this.emojiPickerOpen, 10.0F);
        Emoji.EmojiEntry emojiButtonIcon = Emoji.findEmojiEntry("ð");
        if (emojiButtonIcon != null) {
            EmojiTextRenderer.renderEmoji(drawContext, emojiButtonIcon, emojiButtonX + 1.5F - emojiButtonHoverProgress * 0.5F, composerY + 8.0F, 9.0F + emojiButtonHoverProgress, opacity * (0.7F + 0.3F * emojiButtonHoverProgress));
        }

        this.pendingClickActions.add(new Myid.ChatOverlayAction(emojiButtonX, composerY, 13.0F, 16.0F, () -> {
            this.emojiPickerOpen = !this.emojiPickerOpen;
            this.showMessageCursor = true;
        }, null));
        float sendButtonX = composerX + composerWidth - 8.0F - 1.0F;
        float sendButtonY = composerY + 8.0F;
        boolean hasMessageDraft = !this.messageDraft.isBlank();
        float sendHoverProgress = this.animateHover("send", containsPoint(mouseX, mouseY, sendButtonX - 6.0F, sendButtonY - 6.0F, 12.0F, 12.0F) && hasMessageDraft, 10.0F);
        float sendActiveProgress = this.animateHover("sendOn", hasMessageDraft, 12.0F);
        RoundedGradientRectBuilder.at(sendButtonX - 6.0F, sendButtonY - 6.0F, 12.0F, 12.0F)
        .cornerRadius(6.0F)
        .color(
        ColorUtils.interpolateColor(
        Accent.getTextColorWithAlpha((int)(14.0F * opacity)).toArgb(),
        Accent.getAccentColorWithAlpha((int)((170.0F + 70.0F * sendHoverProgress) * opacity)).toArgb(),
        sendActiveProgress
        )
        )
        .render(drawContext);
        int sendArrowColor = RgbaColor.rgba(255, 255, 255, (int)((150.0F + 105.0F * sendActiveProgress) * opacity)).toArgb();
        float sendArrowOffset = sendHoverProgress * 0.6F;
        drawCircularProgress(drawContext, sendButtonX - 2.4F + sendArrowOffset, sendButtonY, 5.4F, 5.6F, sendArrowColor);
        RoundedGradientRectBuilder.at(sendButtonX - 2.4F + sendArrowOffset, sendButtonY - 0.45F, 2.6F, 0.9F)
        .cornerRadius(0.45F)
        .color(
        ColorUtils.interpolateColor(
        Accent.getTextColorWithAlpha((int)(14.0F * opacity)).toArgb(),
        Accent.getAccentColorWithAlpha((int)((170.0F + 70.0F * sendHoverProgress) * opacity)).toArgb(),
        sendActiveProgress
        )
        )
        .render(drawContext);
        this.pendingClickActions.add(new Myid.ChatOverlayAction(sendButtonX - 7.0F, composerY, 14.0F, 16.0F, () -> this.sendMessage(conversationId), null));
        float draftTextX = emojiButtonX + 14.0F;
        float draftTextWidth = sendButtonX - 9.0F - draftTextX;
        this.pendingClickActions.add(new Myid.ChatOverlayAction(draftTextX, composerY, draftTextWidth, 16.0F, () -> this.showMessageCursor = true, null));
        float draftTextBaselineY = composerY + 8.0F;
        if (this.messageDraft.isEmpty()) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Сообщение", draftTextX, draftTextBaselineY, 4.6F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(150.0F * opacity)))
            .render(drawContext);
        }

        String visibleDraftText = this.messageDraft.replace('\n', ' ');

        while (!visibleDraftText.isEmpty() && EmojiTextRenderer.measureTextWidth(visibleDraftText, 4.6F) > draftTextWidth - 2.0F) {
            visibleDraftText = visibleDraftText.substring(Math.max(1, Character.charCount(visibleDraftText.codePointAt(0))));
        }

        drawContext.enableScissor(
        PixelRounding.II1Il11l111II11IIl(draftTextX),
        PixelRounding.II1Il11l111II11IIl(composerY),
        PixelRounding.l1I1IlllIlI(draftTextX + draftTextWidth),
        PixelRounding.l1I1IlllIlI(composerY + 16.0F)
        );

        try {
            List draftTextLines = EmojiTextRenderer.layoutLines(visibleDraftText, 4.6F, Float.MAX_VALUE);
            EmojiTextRenderer.renderLine(
            drawContext,
            (EmojiTextRenderer.EmojiLineLayout)draftTextLines.get(0),
            draftTextX,
            draftTextBaselineY,
            4.6F,
            Accent.getTextColorWithAlpha((int)(245.0F * opacity)).toArgb(),
            opacity
            );
            if (this.showMessageCursor && currentTimeMillis / 500L % 2L == 0L) {
                float cursorX = draftTextX + ((EmojiTextRenderer.EmojiLineLayout)draftTextLines.get(0)).width() + 0.6F;
                RoundedGradientRectBuilder.at(cursorX, draftTextBaselineY - 3.0F, 0.6F, 6.0F)
                .cornerRadius(0.0F)
                .color(Accent.getAccentColorWithAlpha((int)(255.0F * opacity)))
                .render(drawContext);
            }
        } finally {
            drawContext.disableScissor();
        }

        if (this.messageDraft.length() > 800) {
            String draftLengthLabel = this.messageDraft.length() + "/1000";
            GuiTextBuilder.createAt(
            Medium.DEFAULT_FONT, draftLengthLabel, sendButtonX - 9.0F - Medium.DEFAULT_FONT.measureWidth(draftLengthLabel, 3.4F), composerY - 4.0F, 3.4F
            )
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(180.0F * opacity)))
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    private void renderEmojiPicker(DrawContext drawContext, float mouseX, float mouseY, float contentOriginX, float contentWidth, float opacity) {
        float pickerWidth = Math.min(136.0F, contentWidth - 10.0F);
        float pickerX = contentOriginX + 5.0F;
        float pickerY = this.panelY + this.panelHeight - 16.0F - 7.0F - 104.0F + (1.0F - opacity) * 8.0F;
        RoundedGradientRectBuilder.at(pickerX, pickerY, pickerWidth, 104.0F)
        .cornerRadius(7.0F)
        .color(RgbaColor.rgba(22, 22, 28, (int)(245.0F * opacity)))
        .render(drawContext);
        RoundedRectBuilder.createAt(pickerX, pickerY, pickerWidth, 104.0F)
        .cornerRadius(7.0F)
        .borderWidth(0.6F)
        .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)(60.0F * opacity)))
        .alpha(opacity)
        .render(drawContext);
        this.pendingClickActions.add(new Myid.ChatOverlayAction(pickerX, pickerY, pickerWidth, 104.0F, () -> {}, null));
        ArrayList emojiCategories = new ArrayList<>(Emoji.getEmojiGroups().keySet());
        float categoryWidth = (pickerWidth - 8.0F) / emojiCategories.size();

        for (int categoryIndex = 0; categoryIndex < emojiCategories.size(); categoryIndex++) {
            float categoryX = pickerX + 4.0F + categoryWidth * categoryIndex;
            boolean isSelectedCategory = categoryIndex == this.selectedEmojiCategoryIndex;
            float categoryHoverProgress = this.animateHover("ecat" + categoryIndex, isSelectedCategory || containsPoint(mouseX, mouseY, categoryX, pickerY + 3.0F, categoryWidth, 11.0F), 12.0F);
            if (categoryHoverProgress > 0.01F) {
                RoundedGradientRectBuilder.at(categoryX + 1.0F, pickerY + 3.0F, categoryWidth - 2.0F, 11.0F)
                .cornerRadius(4.0F)
                .color(Accent.getAccentColorWithAlpha((int)((isSelectedCategory ? 70 : 25) * categoryHoverProgress * opacity)))
                .render(drawContext);
            }

            Emoji.EmojiEntry categoryIcon = Emoji.findEmojiEntry((String)emojiCategories.get(categoryIndex));
            if (categoryIcon != null) {
                EmojiTextRenderer.renderEmoji(drawContext, categoryIcon, categoryX + categoryWidth * 0.5F - 3.8F, pickerY + 8.5F, 7.6F, opacity);
            }

            int capturedCategoryIndex = categoryIndex;
            this.pendingClickActions.add(new Myid.ChatOverlayAction(categoryX, pickerY + 3.0F, categoryWidth, 11.0F, () -> {
                this.selectedEmojiCategoryIndex = capturedCategoryIndex;
                this.emojiGridScrollOffset = 0.0F;
            }, null));
        }
// https://t.me/pasterplace

        RoundedGradientRectBuilder.at(pickerX + 4.0F, pickerY + 16.0F, pickerWidth - 8.0F, 0.6F)
        .cornerRadius(0.0F)
        .color(Accent.getTextColorWithAlpha((int)(14.0F * opacity)))
        .render(drawContext);
        List<Emoji.EmojiEntry> categoryEmojis = Emoji.getEmojiGroups().get(emojiCategories.get(this.selectedEmojiCategoryIndex));
        int gridColumnCount = Math.max(1, (int)((pickerWidth - 8.0F) / 14.0F));
        float gridOriginX = pickerX + (pickerWidth - gridColumnCount * 14.0F) * 0.5F;
        float gridOriginY = pickerY + 18.0F;
        float emojiGridHeight = 83.0F;
        int gridRowCount = (categoryEmojis.size() + gridColumnCount - 1) / gridColumnCount;
        float maxGridScrollOffset = Math.max(0.0F, gridRowCount * 14.0F - emojiGridHeight);
        this.emojiGridScrollOffset = Math.clamp(this.emojiGridScrollOffset, 0.0F, maxGridScrollOffset);
        drawContext.enableScissor(
        PixelRounding.II1Il11l111II11IIl(pickerX),
        PixelRounding.II1Il11l111II11IIl(gridOriginY),
        PixelRounding.l1I1IlllIlI(pickerX + pickerWidth),
        PixelRounding.l1I1IlllIlI(gridOriginY + emojiGridHeight)
        );

        try {
            for (int emojiIndex = 0; emojiIndex < categoryEmojis.size(); emojiIndex++) {
                float emojiX = gridOriginX + emojiIndex % gridColumnCount * 14.0F;
                float emojiY = gridOriginY + emojiIndex / gridColumnCount * 14.0F - this.emojiGridScrollOffset;
                if (!(emojiY + 14.0F < gridOriginY) && !(emojiY > gridOriginY + emojiGridHeight)) {
                    Emoji.EmojiEntry emojiEntry = categoryEmojis.get(emojiIndex);
                    boolean isHovered = containsPoint(mouseX, mouseY, emojiX, emojiY, 14.0F, 14.0F) && mouseY >= gridOriginY && mouseY <= gridOriginY + emojiGridHeight;
                    float emojiHoverProgress = this.animateHover("e" + emojiEntry.emojiText(), isHovered, 16.0F);
                    if (emojiHoverProgress > 0.01F) {
                        RoundedGradientRectBuilder.at(emojiX + 0.5F, emojiY + 0.5F, 13.0F, 13.0F)
                        .cornerRadius(4.0F)
                        .color(Accent.getTextColorWithAlpha((int)(22.0F * emojiHoverProgress * opacity)))
                        .render(drawContext);
                    }

                    float emojiDisplaySize = 10.0F + 1.6F * emojiHoverProgress;
                    EmojiTextRenderer.renderEmoji(drawContext, (Emoji.EmojiEntry)emojiEntry, emojiX + (14.0F - emojiDisplaySize) * 0.5F, emojiY + 7.0F, emojiDisplaySize, opacity);
                    if (isHovered) {
                        this.pendingClickActions
                        .add(
                        new Myid.ChatOverlayAction(
                        emojiX, emojiY, 14.0F, 14.0F, () -> this.appendMessageText(emojiEntry.shortcode()), null
                        )
                        );
                    }
                }
            }
        } finally {
            drawContext.disableScissor();
        }
    }
// https://t.me/pasterplace

    private void renderFriendCard(
    DrawContext drawContext, float mouseX, float mouseY, LastSeen contact, float contentOriginX, float contentWidth, float cardY, float opacity
    ) {
        float cardWidth = Math.min(150.0F, contentWidth - 16.0F);
        float cardX = contentOriginX + contentWidth - cardWidth - 6.0F + (1.0F - opacity) * 12.0F;
        float cardTopY = cardY + 4.0F;
        List<String> cosmetics = new ArrayList<>();

        for (Model cosmeticModel : Cape.getInstance().parseSelection(contact.cosmeticsData).values()) {
            cosmetics.add(cosmeticModel.getDisplayName());
        }
// https://t.me/pasterplace

        List<String[]> profileRows = new ArrayList<>();
        List<Runnable> profileRowActions = new ArrayList<>();
        if (!contact.bio.isEmpty()) {
            profileRows.add(new String[]{contact.bio, "О себе"});
            profileRowActions.add(() -> {});
        }

        if (!contact.displayNickname.isEmpty()) {
            profileRows.add(new String[]{contact.displayNickname, "Ник"});
            profileRowActions.add(() -> this.copyToClipboardWithToast(contact.displayNickname, "Ник скопирован"));
        }

        profileRows.add(new String[]{"#" + contact.getFormattedUserId(), "ID"});
        profileRowActions.add(() -> this.copyToClipboardWithToast(contact.getFormattedUserId(), "ID скопирован"));
        if (!contact.serverName.isEmpty()) {
            profileRows.add(new String[]{contact.serverName, "Сервер"});
            profileRowActions.add(() -> this.copyToClipboardWithToast(contact.serverName, "Адрес скопирован"));
        }

        if (!cosmetics.isEmpty()) {
            profileRows.add(new String[]{String.join(", ", cosmetics), "Косметика"});
            profileRowActions.add(() -> {});
        }

        float profileHeaderHeight = 52.0F;
        float profileRowHeight = 13.0F;
        float cardHeight = profileHeaderHeight + 4.0F + profileRows.size() * profileRowHeight + 4.0F + 15.0F;
        float cornerRadius = Accent.getInstance().getCornerRadius();
        KawaseBlurBuilder.create(cardX, cardTopY, cardWidth, cardHeight)
        .cornerRadius(cornerRadius)
        .blurMode(KawaseGaussianBlurMode.KAWASE)
        .blurPasses((int)Accent.getInstance().getBlurRadius())
        .color(Accent.getTintedBackgroundColor((int)(170.0F * opacity)))
        .opacity(opacity)
        .render(drawContext);
        RoundedRectBuilder.createAt(cardX, cardTopY, cardWidth, cardHeight)
        .cornerRadius(cornerRadius)
        .borderWidth(0.5F)
        .color(Accent.getTextColorWithAlpha((int)(18.0F * opacity)))
        .alpha(opacity)
        .render(drawContext);
        this.pendingClickActions.add(new Myid.ChatOverlayAction(cardX, cardTopY, cardWidth, cardHeight, () -> {}, null));
        PlayerSkinQuadBuilder.at(cardX + cardWidth * 0.5F - 12.0F, cardTopY + 7.0F, 24.0F)
        .skinProfile(contact.getGameProfile())
        .fullyRounded()
        .opacity(opacity)
        .render(drawContext);
        if (contact.isOnline) {
            this.renderOnlineIndicator(drawContext, cardX + cardWidth * 0.5F + 6.5F, cardTopY + 25.0F, opacity);
        }

        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, contact.username, cardX + cardWidth * 0.5F, cardTopY + 38.0F, 5.4F)
        .centered()
        .color(Accent.getTextColorWithAlpha((int)(255.0F * opacity)))
        .render(drawContext);
        GuiTextBuilder.createAt(
        Medium.DEFAULT_FONT, contact.isOnline ? "в сети" : this.formatLastSeen(contact.lastSeenAt), cardX + cardWidth * 0.5F, cardTopY + 45.0F, 3.8F
        )
        .centered()
        .color(contact.isOnline ? Accent.getAccentColorWithAlpha((int)(230.0F * opacity)) : Accent.getDeepTextColor((int)(160.0F * opacity)))
        .render(drawContext);
        float dividerY = cardTopY + profileHeaderHeight;
        RoundedGradientRectBuilder.at(cardX, dividerY, cardWidth, 0.5F)
        .color(Accent.getTextColorWithAlpha((int)(14.0F * opacity)))
        .render(drawContext);
        float profileRowY = dividerY + 4.0F;

        for (int rowIndex = 0; rowIndex < profileRows.size(); rowIndex++) {
            profileRowY = this.renderFriendActionRow(
            drawContext,
            mouseX,
            mouseY,
            ((String[])profileRows.get(rowIndex))[1],
            ((String[])profileRows.get(rowIndex))[0],
            cardX,
            cardWidth,
            profileRowY,
            (Runnable)profileRowActions.get(rowIndex),
            opacity
            );
        }
// https://t.me/pasterplace

        RoundedGradientRectBuilder.at(cardX, profileRowY + 2.0F, cardWidth, 0.5F)
        .color(Accent.getTextColorWithAlpha((int)(14.0F * opacity)))
        .render(drawContext);
        float removeFriendButtonY = profileRowY + 4.0F;
        float removeFriendButtonHeight = cardHeight - (removeFriendButtonY - cardTopY) - 2.0F;
        float removeHoverProgress = this.animateHover("remove", containsPoint(mouseX, mouseY, cardX + 4.0F, removeFriendButtonY, cardWidth - 8.0F, removeFriendButtonHeight), 10.0F);
        if (removeHoverProgress > 0.01F || this.confirmFriendRemoval) {
            RoundedGradientRectBuilder.at(cardX + 4.0F, removeFriendButtonY, cardWidth - 8.0F, removeFriendButtonHeight)
            .cornerRadius(cornerRadius * 0.5F)
            .color(RgbaColor.rgba(220, 70, 70, (int)((this.confirmFriendRemoval ? 40.0F : 22.0F * removeHoverProgress) * opacity)))
            .render(drawContext);
        }

        GuiTextBuilder.createAt(
        Medium.DEFAULT_FONT, this.confirmFriendRemoval ? "ТОЧНО УДАЛИТЬ?" : "УДАЛИТЬ ИЗ ДРУЗЕЙ", cardX + 9.0F, removeFriendButtonY + removeFriendButtonHeight * 0.5F, 4.0F
        )
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(RgbaColor.rgba(235, 90, 90, (int)(240.0F * opacity)))
        .render(drawContext);
        this.pendingClickActions.add(new Myid.ChatOverlayAction(cardX + 4.0F, removeFriendButtonY, cardWidth - 8.0F, removeFriendButtonHeight, () -> {
            if (!this.confirmFriendRemoval) {
                this.confirmFriendRemoval = true;
            } else {
                this.confirmFriendRemoval = false;
                this.friendDetailsOpen = false;
                this.socialState.updateFriendRelationship("remove", contact.userId, this::showToast);
                this.socialState.selectChat(null);
            }
        }, null));
    }
// https://t.me/pasterplace

    private float renderFriendActionRow(
    DrawContext drawContext,
    float mouseX,
    float mouseY,
    String actionLabel,
    String valueText,
    float cardX,
    float cardWidth,
    float rowY,
    Runnable action,
    float opacity
    ) {
        float actionRowHeight = 12.0F;
        boolean isHovered = containsPoint(mouseX, mouseY, cardX + 4.0F, rowY, cardWidth - 8.0F, actionRowHeight);
        float hoverProgress = this.animateHover("il" + actionLabel, isHovered, 12.0F);
        if (hoverProgress > 0.01F) {
            RoundedGradientRectBuilder.at(cardX + 4.0F, rowY, cardWidth - 8.0F, actionRowHeight)
            .cornerRadius(Accent.getInstance().getCornerRadius() * 0.5F)
            .color(Accent.getTextColorWithAlpha((int)(10.0F * hoverProgress * opacity)))
            .render(drawContext);
        }
// https://t.me/pasterplace

        GuiTextBuilder.createAt(
        Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(valueText, 4.4F, cardWidth - 18.0F), cardX + 9.0F, rowY + 4.0F, 4.4F
        )
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getTextColorWithAlpha((int)(240.0F * opacity)))
        .render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, actionLabel, cardX + 9.0F, rowY + 9.0F, 3.4F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(150.0F * opacity)))
        .render(drawContext);
        this.pendingClickActions.add(new Myid.ChatOverlayAction(cardX + 4.0F, rowY, cardWidth - 8.0F, actionRowHeight, action, null));
        return rowY + 13.0F;
    }
// https://t.me/pasterplace

    private void renderProfileTab(DrawContext drawContext, float mouseX, float mouseY, float contentOriginX, float contentWidth, long currentTimeMillis, float opacity) {
        LastSeen currentUserPresence = this.socialState.getCurrentUser();
        float profileCardX = contentOriginX + 6.0F;
        float profileCardWidth = contentWidth - 12.0F;
        float profileCardY = this.panelY + 6.0F;
        float profileCardHeight = 54.0F;
        RoundedGradientRectBuilder.at(profileCardX, profileCardY, profileCardWidth, profileCardHeight)
        .cornerRadius(7.0F)
        .color(Accent.getTextColorWithAlpha((int)(6.0F * opacity)))
        .render(drawContext);
        float avatarSize = 22.0F;
        float avatarX = profileCardX + 8.0F;
        float avatarY = profileCardY + 8.0F;
        GuestAvatarRenderer.II1Il11l111II11IIl(drawContext, avatarX, avatarY, avatarSize, avatarSize * 0.5F, opacity);
        float profileTextX = avatarX + avatarSize + 7.0F;
        boolean isConnected = this.socialState.getConnectionState() == GlobalsClient.ConnectionState.CONNECTED;
        CloudConfigClient.UserIdentity userIdentity = CloudConfigClient.getInstance().getAuthenticatedUser();
        String profileName = Guest.l1I1IlllIlI();
        if (profileName.equals("Guest") && currentUserPresence == null) {
            profileName = "Загрузка...";
        }

        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, profileName, profileTextX, avatarY + 7.0F, 6.0F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getTextColorWithAlpha((int)(255.0F * opacity)))
        .render(drawContext);
        float userIdLabelX = profileTextX + Medium.DEFAULT_FONT.measureWidth(profileName, 6.0F) + 3.5F;
        RoundedGradientRectBuilder.at(userIdLabelX, avatarY + 5.8F, 2.4F, 2.4F)
        .cornerRadius(1.2F)
        .color(isConnected ? RgbaColor.rgba(76, 217, 100, (int)(255.0F * opacity)) : Accent.getDeepTextColor((int)(120.0F * opacity)))
        .render(drawContext);
        if (userIdentity != null || currentUserPresence != null) {
            String userId = userIdentity != null ? String.valueOf(userIdentity.userId()) : currentUserPresence.getFormattedUserId();
            String userIdLabel = userIdentity != null ? "ID " + userId : "#" + userId;
            float userIdLabelY = avatarY + 16.0F;
            float userIdLabelWidth = Medium.DEFAULT_FONT.measureWidth(userIdLabel, 3.8F);
            float copyHoverProgress = this.animateHover("pid", containsPoint(mouseX, mouseY, profileTextX, userIdLabelY - 3.5F, userIdLabelWidth, 7.0F), 10.0F);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, copyHoverProgress > 0.3F ? "скопировать" : userIdLabel, profileTextX, userIdLabelY, 3.8F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)((150.0F + 80.0F * copyHoverProgress) * opacity)))
            .render(drawContext);
            this.pendingClickActions.add(new Myid.ChatOverlayAction(profileTextX, userIdLabelY - 3.5F, userIdLabelWidth, 7.0F, () -> this.copyToClipboardWithToast(userId, "ID скопирован"), null));
        }

        String biographyPreview = currentUserPresence != null && !currentUserPresence.bio.isEmpty() ? currentUserPresence.bio : "Расскажите о себе";
        GuiTextBuilder.createAt(
        Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(biographyPreview, 4.0F, profileCardWidth - 16.0F), profileCardX + 8.0F, profileCardY + 38.0F, 4.0F
        )
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(190.0F * opacity)))
        .render(drawContext);
        String accountCreatedDate = currentUserPresence != null && currentUserPresence.accountCreatedAt > 0L ? new SimpleDateFormat("dd.MM.yy").format(new Date(currentUserPresence.accountCreatedAt)) : "—";
        String profileStatsText = this.socialState.getFriends().size()
        + " друзей  ·  "
        + this.socialState.getIncomingFriendRequests().size()
        + " заявок  ·  "
        + this.socialState.getGlobalOnlineCount()
        + " в сети  ·  с "
        + accountCreatedDate;
        GuiTextBuilder.createAt(
        Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(profileStatsText, 3.6F, profileCardWidth - 16.0F), profileCardX + 8.0F, profileCardY + 47.0F, 3.6F
        )
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(130.0F * opacity)))
        .render(drawContext);
        float cosmeticsTitleY = profileCardY + profileCardHeight + 8.0F;
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Косметика", profileCardX + 2.0F, cosmeticsTitleY, 4.2F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(170.0F * opacity)))
        .render(drawContext);
        float cosmeticsTextX = profileCardX + 2.0F + Medium.DEFAULT_FONT.measureWidth("Косметика", 4.2F) + 5.0F;
        if (Cape.getInstance().getEquippedModelCount() == 0) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "не выбрана", cosmeticsTextX, cosmeticsTitleY, 4.0F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getDeepTextColor((int)(120.0F * opacity)))
            .render(drawContext);
        }

        for (Object cosmeticModel : Cape.getInstance().getEquippedModels()) {
            String cosmeticName = ((Model)cosmeticModel).getDisplayName();
            float cosmeticChipWidth = Medium.DEFAULT_FONT.measureWidth(cosmeticName, 3.8F) + 8.0F;
            if (cosmeticsTextX + cosmeticChipWidth > profileCardX + profileCardWidth) {
                break;
            }

            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, cosmeticName, cosmeticsTextX + 4.0F, cosmeticsTitleY, 3.8F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(Accent.getTextColorWithAlpha((int)(235.0F * opacity)))
            .render(drawContext);
            cosmeticsTextX += cosmeticChipWidth + 3.0F;
        }

        cosmeticsTitleY += 9.0F;
        float biographyFieldWidth = profileCardWidth - 44.0F;
        this.l1IIlIIlI1IIII1l1I1lI = cosmeticsTitleY + 5.0F;
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "О себе", profileCardX + 2.0F, cosmeticsTitleY, 4.0F)
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(170.0F * opacity)))
        .render(drawContext);
        this.biographyField.render(drawContext, profileCardX, this.l1IIlIIlI1IIII1l1I1lI, biographyFieldWidth, 12.0F, currentTimeMillis, opacity);
        this.renderProfileAction(
        drawContext, mouseX, mouseY, "saveBio", "Сохранить", profileCardX + biographyFieldWidth + 4.0F, this.l1IIlIIlI1IIII1l1I1lI, profileCardWidth - biographyFieldWidth - 4.0F, this::saveBiography, opacity
        );
        float connectionStatusY = this.panelY + this.panelHeight - 12.0F;

        int connectionStatusColor = switch (this.socialState.getConnectionState()) {
        case CONNECTED -> RgbaColor.rgba(76, 217, 100, (int)(255.0F * opacity)).toArgb();
        case RECONNECTING -> RgbaColor.rgba(255, 196, 70, (int)(255.0F * opacity)).toArgb();
            default -> RgbaColor.rgba(240, 90, 90, (int)(255.0F * opacity)).toArgb();
        };
        String connectionStatusText = this.socialState.getConnectionStatusMessage();
        float connectionStatusPillWidth = Math.min(profileCardWidth, Medium.DEFAULT_FONT.measureWidth(connectionStatusText, 3.8F) + 14.0F);
        float connectionStatusPillX = contentOriginX + (contentWidth - connectionStatusPillWidth) * 0.5F;
        RoundedGradientRectBuilder.at(connectionStatusPillX + 4.0F, connectionStatusY + 3.0F, 2.0F, 2.0F).cornerRadius(1.0F).color(connectionStatusColor).render(drawContext);
        GuiTextBuilder.createAt(
        Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(connectionStatusText, 3.8F, connectionStatusPillWidth - 12.0F), connectionStatusPillX + 9.0F, connectionStatusY + 4.0F, 3.8F
        )
        .baselinePosition(TextBaselinePosition.CENTER)
        .color(Accent.getDeepTextColor((int)(180.0F * opacity)))
        .render(drawContext);
    }
// https://t.me/pasterplace

    private void renderContextMenu(DrawContext drawContext, float mouseX, float mouseY, float opacity) {
        LastSeen currentUserPresence = this.socialState.getCurrentUser();
        boolean isOwnMessage = currentUserPresence != null && currentUserPresence.userId.equals(this.contextMenuChat.getSenderId());
        ArrayList menuOptions = new ArrayList<>(List.of("Копировать"));
        if (isOwnMessage) {
            menuOptions.add("Удалить");
        }

        float menuWidth = 52.0F;
        float menuHeight = menuOptions.size() * 11.0F + 4.0F;
        float menuX = Math.min(this.contextMenuX, this.panelX + this.panelWidth - menuWidth - 2.0F);
        float menuY = Math.min(this.contextMenuY, this.panelY + this.panelHeight - menuHeight - 2.0F);
        RoundedGradientRectBuilder.at(menuX, menuY, menuWidth, menuHeight)
        .cornerRadius(5.0F)
        .color(RgbaColor.rgba(24, 24, 30, (int)(248.0F * opacity)))
        .render(drawContext);
        RoundedRectBuilder.createAt(menuX, menuY, menuWidth, menuHeight)
        .cornerRadius(5.0F)
        .borderWidth(0.6F)
        .color(Accent.applyOutlineOpacity(Accent.getAccentColorWithAlpha(255), (int)(60.0F * opacity)))
        .alpha(opacity)
        .render(drawContext);
        Chat chat = this.contextMenuChat;
        String conversationId = this.socialState.getSelectedChatId();

        for (int optionIndex = 0; optionIndex < menuOptions.size(); optionIndex++) {
            float optionY = menuY + 2.0F + optionIndex * 11.0F;
            boolean isDeleteOption = ((String)menuOptions.get(optionIndex)).equals("Удалить");
            float hoverProgress = this.animateHover("ctx" + optionIndex, containsPoint(mouseX, mouseY, menuX + 2.0F, optionY, menuWidth - 4.0F, 11.0F), 14.0F);
            if (hoverProgress > 0.01F) {
                RoundedGradientRectBuilder.at(menuX + 2.0F, optionY, menuWidth - 4.0F, 11.0F)
                .cornerRadius(3.5F)
                .color(
                isDeleteOption
                ? RgbaColor.rgba(220, 70, 70, (int)(90.0F * hoverProgress * opacity))
                : Accent.getAccentColorWithAlpha((int)(70.0F * hoverProgress * opacity))
                )
                .render(drawContext);
            }

            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, (String)menuOptions.get(optionIndex), menuX + 6.0F, optionY + 5.5F, 4.4F)
            .baselinePosition(TextBaselinePosition.CENTER)
            .color(
            isDeleteOption ? RgbaColor.rgba(255, 120, 120, (int)(250.0F * opacity)) : Accent.getTextColorWithAlpha((int)(245.0F * opacity))
            )
            .render(drawContext);
            Runnable optionAction = isDeleteOption
            ? () -> this.socialState.deleteChatMessage(conversationId, chat.getMessageId(), this::showToast)
            : () -> this.copyToClipboardWithToast(chat.getMessage(), "Текст скопирован");
            this.contextMenuActions.add(new Myid.ChatOverlayAction(menuX + 2.0F, optionY, menuWidth - 4.0F, 11.0F, optionAction, null));
        }
    }
// https://t.me/pasterplace

    public boolean mouseClicked(float mouseX, float mouseY, int button) {
        if (!containsPoint(mouseX, mouseY, this.panelX, this.panelY, this.panelWidth, this.panelHeight)) {
            return false;
        }

        this.chatSearchField.setFocused(false);
        this.friendSearchField.setFocused(false);
        this.biographyField.setFocused(false);
        boolean wasEmojiPickerOpen = this.emojiPickerOpen;
        if (button == 1) {
            this.contextMenuChat = null;

            for (int actionIndex = this.pendingClickActions.size() - 1; actionIndex >= 0; actionIndex--) {
                Object clickAction = this.pendingClickActions.get(actionIndex);
                if (((Myid.ChatOverlayAction)clickAction).chat != null
                && containsPoint(
                mouseX,
                mouseY,
                ((Myid.ChatOverlayAction)clickAction).left,
                ((Myid.ChatOverlayAction)clickAction).top,
                ((Myid.ChatOverlayAction)clickAction).width,
                ((Myid.ChatOverlayAction)clickAction).height
                )) {
                    this.contextMenuChat = ((Myid.ChatOverlayAction)clickAction).chat;
                    this.contextMenuX = mouseX;
                    this.contextMenuY = mouseY;
                    return true;
                }
            }

            return true;
        } else {
            if (button != 0) {
                return false;
            }

            if (this.contextMenuChat != null) {
                for (Object contextMenuAction : this.contextMenuActions) {
                    if (containsPoint(
                    mouseX,
                    mouseY,
                    ((Myid.ChatOverlayAction)contextMenuAction).left,
                    ((Myid.ChatOverlayAction)contextMenuAction).top,
                    ((Myid.ChatOverlayAction)contextMenuAction).width,
                    ((Myid.ChatOverlayAction)contextMenuAction).height
                    )) {
                        ((Myid.ChatOverlayAction)contextMenuAction).action.run();
                    }
                }

                this.contextMenuChat = null;
                return true;
            } else {
                for (int actionIndex = this.pendingClickActions.size() - 1; actionIndex >= 0; actionIndex--) {
                    Object clickAction = this.pendingClickActions.get(actionIndex);
                    if (((Myid.ChatOverlayAction)clickAction).action != null
                    && containsPoint(
                    mouseX,
                    mouseY,
                    ((Myid.ChatOverlayAction)clickAction).left,
                    ((Myid.ChatOverlayAction)clickAction).top,
                    ((Myid.ChatOverlayAction)clickAction).width,
                    ((Myid.ChatOverlayAction)clickAction).height
                    )) {
                        ((Myid.ChatOverlayAction)clickAction).action.run();
                        if (wasEmojiPickerOpen && this.emojiPickerOpen && !this.isPointInContextMenu(mouseX, mouseY)) {
                            this.emojiPickerOpen = false;
                        }

                        return true;
                    }
                }

                this.contextMenuChat = null;
                if (this.emojiPickerOpen && !this.isPointInContextMenu(mouseX, mouseY)) {
                    this.emojiPickerOpen = false;
                }

                if (this.friendDetailsOpen) {
                    this.friendDetailsOpen = false;
                    this.confirmFriendRemoval = false;
                }

                if (this.focusSearchFieldAt(mouseX, mouseY)) {
                    return true;
                }

                this.showMessageCursor = false;
                return true;
            }
        }
    }
// https://t.me/pasterplace

    private boolean isPointInContextMenu(float mouseX, float mouseY) {
        float emojiPickerX = this.panelX + 112.0F + 0.75F;
        float contentWidth = this.panelWidth - 112.0F - 0.75F;
        float emojiPickerWidth = Math.min(136.0F, contentWidth - 10.0F);
        float emojiPickerY = this.panelY + this.panelHeight - 16.0F - 7.0F - 104.0F;
        return containsPoint(mouseX, mouseY, emojiPickerX + 5.0F, emojiPickerY, emojiPickerWidth, 104.0F)
        || containsPoint(mouseX, mouseY, emojiPickerX + 5.0F, this.panelY + this.panelHeight - 16.0F - 3.0F, 16.0F, 16.0F);
    }
// https://t.me/pasterplace

    private boolean focusSearchFieldAt(float mouseX, float mouseY) {
        float searchFieldY = this.panelY + 20.0F;
        if (this.selectedTabIndex == 1 && containsPoint(mouseX, mouseY, this.panelX + 5.0F, searchFieldY, 87.0F, 12.0F)) {
            this.friendSearchField.setFocused(true);
            this.showMessageCursor = false;
            return true;
        }

        if (this.selectedTabIndex != 1 && containsPoint(mouseX, mouseY, this.panelX + 5.0F, searchFieldY, 102.0F, 12.0F)) {
            this.chatSearchField.setFocused(true);
            this.showMessageCursor = false;
            return true;
        }

        if (this.selectedTabIndex == 2) {
            float profileContentOriginX = this.panelX + 112.0F + 0.75F;
            float profileContentWidth = this.panelWidth - 112.0F - 0.75F;
            float biographyFieldWidth = profileContentWidth - 12.0F - 44.0F;
            if (containsPoint(mouseX, mouseY, profileContentOriginX + 6.0F, this.l1IIlIIlI1IIII1l1I1lI, biographyFieldWidth, 12.0F)) {
                this.biographyField.setFocused(true);
                this.showMessageCursor = false;
                return true;
            }
        }

        return false;
    }
// https://t.me/pasterplace

    public boolean mouseScrolled(float mouseX, float mouseY, double scrollAmount) {
        if (!containsPoint(mouseX, mouseY, this.panelX, this.panelY, this.panelWidth, this.panelHeight)) {
            return false;
        } else {
            this.contextMenuChat = null;
            if (mouseX < this.panelX + 112.0F) {
                this.friendListScrollTarget = Math.clamp(this.friendListScrollTarget - (float)scrollAmount * 18.0F, 0.0F, this.friendListMaxScroll);
                return true;
            } else if (this.emojiPickerOpen && this.isPointInContextMenu(mouseX, mouseY)) {
                this.emojiGridScrollOffset -= (float)scrollAmount * 14.0F;
                return true;
            } else {
                this.messageScrollOffset = Math.clamp(this.messageScrollOffset + (float)scrollAmount * 22.0F, 0.0F, Math.max(0.0F, this.messageMaxScroll));
                return true;
            }
        }
    }
// https://t.me/pasterplace

    public boolean keyPressed(KeyInput keyInput) {
        SearchTextField l1I1IIl111Il1l1l = this.getFocusedTextField();
        if (l1I1IIl111Il1l1l != null) {
            if (keyInput.getKeycode() == 256) {
                l1I1IIl111Il1l1l.setFocused(false);
                return true;
            } else {
                return l1I1IIl111Il1l1l.keyPressed(keyInput);
            }
        } else {
            int keyCode = keyInput.getKeycode();
            if (keyCode == 256) {
                if (this.contextMenuChat != null) {
                    this.contextMenuChat = null;
                    return true;
                } else if (this.emojiPickerOpen) {
                    this.emojiPickerOpen = false;
                    return true;
                } else if (this.friendDetailsOpen) {
                    this.friendDetailsOpen = false;
                    return true;
                } else if (this.showMessageCursor) {
                    this.showMessageCursor = false;
                    return true;
                } else {
                    return false;
                }
            } else if (this.showMessageCursor && this.socialState.getSelectedChatId() != null && this.selectedTabIndex != 2) {
                String conversationId = this.socialState.getSelectedChatId();
                if (keyCode != 257 && keyCode != 335) {
                    if (keyCode == 259) {
                        if (keyInput.hasCtrl()) {
                            String draftWithoutTrailingWhitespace = this.messageDraft.stripTrailing();
                            int lastWordStart = draftWithoutTrailingWhitespace.lastIndexOf(32);
                            this.messageDraft = lastWordStart < 0 ? "" : draftWithoutTrailingWhitespace.substring(0, lastWordStart + 1);
                        } else if (!this.messageDraft.isEmpty()) {
                            this.messageDraft = removeLastEmoji(this.messageDraft);
                        }

                        return true;
                    } else if (keyInput.hasCtrl() && keyCode == 86) {
                        String clipboardText = MinecraftClient.getInstance().keyboard.getClipboard();
                        if (clipboardText != null) {
                            this.appendMessageText(clipboardText.replace("\r", ""));
                        }

                        return true;
                    } else {
                        return true;
                    }
                } else {
                    if (keyInput.hasShift()) {
                        this.appendMessageText("\n");
                    } else {
                        this.sendMessage(conversationId);
                    }

                    return true;
                }
            } else {
                return false;
            }
        }
    }
// https://t.me/pasterplace

    public boolean charTyped(CharInput charInput) {
        SearchTextField l1I1IIl111Il1l1l = this.getFocusedTextField();
        if (l1I1IIl111Il1l1l != null) {
            return l1I1IIl111Il1l1l.charTyped(charInput);
        }

        if (this.showMessageCursor && this.socialState.getSelectedChatId() != null && this.selectedTabIndex != 2) {
            int codePoint = charInput.codepoint();
            if (codePoint >= 32 && codePoint != 127) {
                this.appendMessageText(charInput.asString());
            }

            return true;
        } else {
            return false;
        }
    }
// https://t.me/pasterplace

    public boolean hasFocusedInput() {
        return this.showMessageCursor || this.getFocusedTextField() != null;
    }
// https://t.me/pasterplace

    public void clearFocusedInput() {
        this.showMessageCursor = false;
        this.chatSearchField.setFocused(false);
        this.friendSearchField.setFocused(false);
        this.biographyField.setFocused(false);
        this.emojiPickerOpen = false;
        this.contextMenuChat = null;
    }
// https://t.me/pasterplace

    private SearchTextField getFocusedTextField() {
        for (SearchTextField textField : new SearchTextField[]{this.chatSearchField, this.friendSearchField, this.biographyField}) {
            if (textField.isFocused()) {
                return textField;
            }
        }

        return null;
    }
// https://t.me/pasterplace

    private void appendMessageText(String appendedText) {
        String updatedDraft = this.messageDraft + appendedText;
        if (updatedDraft.length() > 1000) {
            updatedDraft = updatedDraft.substring(0, 1000);
        }

        this.messageDraft = updatedDraft;
        String conversationId = this.socialState.getSelectedChatId();
        if (conversationId != null) {
            this.socialState.sendTypingIndicator(conversationId);
        }
    }
// https://t.me/pasterplace

    private static String removeLastEmoji(String draft) {
        for (int suffixLength = Math.min(5, draft.length()); suffixLength >= 1; suffixLength--) {
            int suffixStartIndex = draft.length() - suffixLength;
            if (Emoji.matchEmojiLength(draft, suffixStartIndex) == suffixLength) {
                return draft.substring(0, suffixStartIndex);
            }
        }

        String draftWithoutLastCodePoint = draft.substring(0, draft.offsetByCodePoints(draft.length(), -1));

        while (draftWithoutLastCodePoint.endsWith("️") || draftWithoutLastCodePoint.endsWith("‍")) {
            draftWithoutLastCodePoint = draftWithoutLastCodePoint.substring(0, draftWithoutLastCodePoint.length() - 1);
        }

        return draftWithoutLastCodePoint;
    }
// https://t.me/pasterplace

    private void sendMessage(String conversationId) {
        String messageText = this.messageDraft.strip();
        if (!messageText.isEmpty()) {
            this.messageDraft = "";
            this.messageScrollOffset = 0.0F;
            this.emojiPickerOpen = false;
            this.socialState.sendChatMessage(conversationId, messageText, errorMessage -> {
                this.showToast(errorMessage);
                if (this.messageDraft.isEmpty()) {
                    this.messageDraft = messageText;
                }
            });
        }
    }
// https://t.me/pasterplace

    private void selectTab(int tabIndex) {
        this.selectedTabIndex = tabIndex;
        this.friendListScrollTarget = 0.0F;
        this.contextMenuChat = null;
        if (tabIndex == 2) {
            LastSeen lastSeen = this.socialState.getCurrentUser();
            if (lastSeen != null) {
                this.biographyField.setText(lastSeen.bio);
            }

            this.emojiPickerOpen = false;
            this.friendDetailsOpen = false;
        }
    }
// https://t.me/pasterplace

    private void selectConversation(String conversationId) {
        if (this.selectedTabIndex == 2) {
            this.selectedTabIndex = 0;
        }

        if (!conversationId.equals(this.socialState.getSelectedChatId())) {
            this.friendDetailsOpen = false;
            this.emojiPickerOpen = false;
            this.confirmFriendRemoval = false;
        }

        this.socialState.selectChat(conversationId);
        this.showMessageCursor = true;
    }
// https://t.me/pasterplace

    private void sendFriendRequest(String friendQuery) {
        String normalizedQuery = friendQuery.trim();
        if (normalizedQuery.isEmpty()) {
            this.showToast("Введите ID или имя");
        } else {
            this.socialState.sendFriendRequest(normalizedQuery, responseMessage -> {
                this.friendSearchField.setText("");

                this.showToast("Заявка отправлена");
            }, this::showToast);
        }
    }
// https://t.me/pasterplace

    private void saveBiography() {
        this.socialState
        .updateProfile(null, this.biographyField.getText().trim(), () -> this.showToast("Профиль обновлён"), this::showToast);
    }
// https://t.me/pasterplace

    private void showToast(String toastMessage) {
        this.toastMessage = toastMessage;
        this.toastExpirationTime = System.currentTimeMillis() + 2600L;
    }
// https://t.me/pasterplace

    private void copyToClipboardWithToast(String clipboardText, String toastMessage) {
        if (clipboardText != null && !clipboardText.isEmpty()) {
            MinecraftClient.getInstance().keyboard.setClipboard(clipboardText);
            this.showToast(toastMessage);
        }
    }
// https://t.me/pasterplace

    private void renderFallbackAvatar(DrawContext drawContext, LastSeen lastSeen, float x, float y, float avatarSize, float opacity) {
        if (lastSeen == null) {
            RoundedGradientRectBuilder.at(x, y, avatarSize, avatarSize)
            .cornerRadius(avatarSize * 0.5F)
            .verticalGradient(Accent.getAccentColorWithAlpha((int)(230.0F * opacity)), Accent.getAccentColorWithAlpha((int)(120.0F * opacity)))
            .render(drawContext);
            GuiTextBuilder.createAt(Medium.ICON_FONT, "G", x + avatarSize * 0.5F, y + avatarSize * 0.5F, avatarSize * 0.52F)
            .centered()
            .color(RgbaColor.rgba(255, 255, 255, (int)(250.0F * opacity)))
            .render(drawContext);
        } else {
            if (lastSeen.isOnline && avatarSize >= 12.0F) {
                RoundedRectBuilder.createAt(x - 1.2F, y - 1.2F, avatarSize + 2.4F, avatarSize + 2.4F)
                .cornerRadius((avatarSize + 2.4F) * 0.5F)
                .borderWidth(0.8F)
                .color(RgbaColor.rgba(76, 217, 100, (int)(200.0F * opacity)))
                .alpha(opacity)
                .render(drawContext);
            }

            RoundedGradientRectBuilder.at(x, y, avatarSize, avatarSize)
            .cornerRadius(avatarSize * 0.5F)
            .verticalGradient(
            getTextColor(lastSeen.userId, opacity * 0.9F), getTextColor(lastSeen.userId, opacity * 0.45F)
            )
            .render(drawContext);
            PlayerSkinQuadBuilder.at(x, y, avatarSize)
            .skinProfile(lastSeen.getGameProfile())
            .fullyRounded()
            .opacity(opacity)
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    private void renderOnlineIndicator(DrawContext drawContext, float centerX, float centerY, float opacity) {
        RoundedGradientRectBuilder.at(centerX - 0.8F, centerY - 0.8F, 5.2F, 5.2F)
        .cornerRadius(2.6F)
        .color(RgbaColor.rgba(18, 18, 22, (int)(255.0F * opacity)))
        .render(drawContext);
        RoundedGradientRectBuilder.at(centerX, centerY, 3.6F, 3.6F)
        .cornerRadius(1.8F)
        .color(RgbaColor.rgba(76, 217, 100, (int)(255.0F * opacity)))
        .render(drawContext);
    }
// https://t.me/pasterplace

    private void renderUnreadCountBadge(DrawContext drawContext, float badgeRightX, float badgeY, int unreadCount, float opacity) {
        String badgeText = unreadCount > 99 ? "99+" : String.valueOf(unreadCount);
        float badgeWidth = Math.max(7.0F, Medium.DEFAULT_FONT.measureWidth(badgeText, 3.6F) + 4.0F);
        RoundedGradientRectBuilder.at(badgeRightX - badgeWidth, badgeY, badgeWidth, 7.0F)
        .cornerRadius(3.5F)
        .color(Accent.getAccentColorWithAlpha((int)(255.0F * opacity)))
        .render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, badgeText, badgeRightX - badgeWidth * 0.5F, badgeY + 3.5F, 3.6F)
        .centered()
        .color(RgbaColor.rgba(255, 255, 255, (int)(255.0F * opacity)))
        .render(drawContext);
    }
// https://t.me/pasterplace

    private void renderIconAction(
    DrawContext drawContext, float mouseX, float mouseY, String actionKey, float iconCenterX, float iconCenterY, boolean isPositiveAction, float opacity
    ) {
        float hoverProgress = this.animateHover(actionKey, containsPoint(mouseX, mouseY, iconCenterX - 5.0F, iconCenterY - 5.0F, 10.0F, 10.0F), 12.0F);
        int iconColor = isPositiveAction
        ? Accent.getAccentColorWithAlpha((int)((110.0F + 110.0F * hoverProgress) * opacity)).toArgb()
        : Accent.getTextColorWithAlpha((int)((14.0F + 20.0F * hoverProgress) * opacity)).toArgb();
        RoundedGradientRectBuilder.at(iconCenterX - 4.5F, iconCenterY - 4.5F, 9.0F, 9.0F).cornerRadius(4.5F).color(iconColor).render(drawContext);
    }
// https://t.me/pasterplace

    private void renderProfileAction(
    DrawContext drawContext,
    float mouseX,
    float mouseY,
    String actionKey,
    String actionLabel,
    float actionX,
    float actionY,
    float actionWidth,
    Runnable action,
    float opacity
    ) {
        float hoverProgress = this.animateHover(actionKey, containsPoint(mouseX, mouseY, actionX, actionY, actionWidth, 12.0F), 10.0F);
        RoundedGradientRectBuilder.at(actionX, actionY, actionWidth, 12.0F)
        .cornerRadius(Accent.getInstance().getCornerRadius() * 0.5F)
        .color(
        ColorUtils.interpolateColor(
        Accent.getAccentColorWithAlpha((int)(80.0F * opacity)).toArgb(), Accent.getAccentColorWithAlpha((int)(170.0F * opacity)).toArgb(), hoverProgress
        )
        )
        .render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, actionLabel, actionX + actionWidth * 0.5F, actionY + 6.0F, 4.2F)
        .centered()
        .color(Accent.getTextColorWithAlpha((int)(255.0F * opacity)))
        .render(drawContext);
        this.pendingClickActions.add(new Myid.ChatOverlayAction(actionX, actionY, actionWidth, 12.0F, action, null));
    }
// https://t.me/pasterplace

    private void renderStatusGlyph(DrawContext drawContext, float centerX, float centerY, boolean isRead, int baseColor) {
        float glyphHeight = 3.6F;
        float glyphWidth = isRead ? glyphHeight * 30.0F / 16.0F : glyphHeight * 20.0F / 16.0F;
        int colorAlpha = baseColor >>> 24 & 0xFF;
        int glyphColor = isRead ? baseColor : baseColor & 16777215 | (int)(colorAlpha * 0.75F) << 24;
        TextureQuadBuilder.at(isRead ? DOUBLE_CHECK_ICON : CHECK_ICON, centerX - glyphWidth, centerY - glyphHeight * 0.5F, glyphWidth, glyphHeight)
        .tint(glyphColor)
        .render(drawContext);
    }
// https://t.me/pasterplace

    private void renderCheckmark(DrawContext drawContext, float centerX, float centerY, float size, int color) {
        this.renderCheckmark(drawContext, centerX, centerY, size, 0.65F, color);
    }
// https://t.me/pasterplace

    private void renderCheckmark(DrawContext drawContext, float centerX, float centerY, float size, float strokeWidth, int color) {
        drawThickLine(drawContext, centerX - size, centerY, centerX - size * 0.25F, centerY + size * 0.8F, strokeWidth, color);
        drawThickLine(drawContext, centerX - size * 0.25F, centerY + size * 0.8F, centerX + size * 1.1F, centerY - size * 0.9F, strokeWidth, color);
    }
// https://t.me/pasterplace

    private void renderCross(DrawContext drawContext, float centerX, float centerY, float size, int color) {
        drawThickLine(drawContext, centerX - size, centerY - size, centerX + size, centerY + size, 0.75F, color);
        drawThickLine(drawContext, centerX - size, centerY + size, centerX + size, centerY - size, 0.75F, color);
    }
// https://t.me/pasterplace

    private static void drawThickLine(DrawContext drawContext, float startX, float startY, float endX, float endY, float thickness, int color) {
        float deltaX = endX - startX;
        float deltaY = endY - startY;
        float lineLength = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY) + thickness;
        drawContext.getMatrices().pushMatrix();
        drawContext.getMatrices().translate((startX + endX) * 0.5F, (startY + endY) * 0.5F);
        drawContext.getMatrices().rotate((float)Math.atan2(deltaY, deltaX));
        RoundedGradientRectBuilder.at(-lineLength * 0.5F, -thickness * 0.5F, lineLength, thickness)
        .cornerRadius(thickness * 0.5F)
        .color(color)
        .render(drawContext);
        drawContext.getMatrices().popMatrix();
    }
// https://t.me/pasterplace

    private static void drawCircularProgress(DrawContext drawContext, float startX, float centerY, float width, float height, int color) {
        byte segmentCount = 12;
        float segmentWidth = width / segmentCount;

        for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
            float segmentProgress = (segmentIndex + 0.5F) / segmentCount;
            float segmentHeight = height * (1.0F - segmentProgress);
            RoundedGradientRectBuilder.at(startX + segmentIndex * segmentWidth, centerY - segmentHeight * 0.5F, segmentWidth + 0.15F, segmentHeight)
            .cornerRadius(0.0F)
            .color(color)
            .render(drawContext);
        }
    }
// https://t.me/pasterplace

    private float animateHover(String animationKey, boolean isHovered, float animationSpeed) {
        float[] hoverProgressState = this.buttonHoverAnimations.computeIfAbsent(animationKey, hoverKey -> new float[1]);
        hoverProgressState[0] += ((isHovered ? 1.0F : 0.0F) - hoverProgressState[0]) * (1.0F - (float)Math.exp(-animationSpeed * this.animationFrameDelta));
        return hoverProgressState[0];
    }
// https://t.me/pasterplace

    private static int getTextColor(String playerName, float opacity) {
        int paletteColor = TEXT_COLOR_PALETTE[Math.floorMod(playerName.hashCode(), TEXT_COLOR_PALETTE.length)];
        return (int)(255.0F * opacity) << 24 | paletteColor & 16777215;
    }
// https://t.me/pasterplace

    private static String loadingDots(long currentTimeMillis) {
        return ".".repeat((int)(currentTimeMillis / 400L % 3L) + 1);
    }
// https://t.me/pasterplace

    private String formatMessageTime(long timestampMillis) {
        return isSameDay(timestampMillis, System.currentTimeMillis())
        ? this.messageTimeFormat.format(new Date(timestampMillis))
        : this.compactDateFormat.format(new Date(timestampMillis));
    }
// https://t.me/pasterplace

    private String formatDateHeader(long timestampMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        if (isSameDay(timestampMillis, currentTimeMillis)) {
            return "Сегодня";
        } else {
            return isSameDay(timestampMillis, currentTimeMillis - 86400000L) ? "Вчера" : this.fullDateFormat.format(new Date(timestampMillis));
        }
    }
// https://t.me/pasterplace

    private String formatLastSeen(long lastSeenTimeMillis) {
        if (lastSeenTimeMillis <= 0L) {
            return "был(а) давно";
        } else {
            long elapsedMillis = System.currentTimeMillis() - lastSeenTimeMillis;
            if (elapsedMillis < 60000L) {
                return "был(а) только что";
            } else if (elapsedMillis < 3600000L) {
                return "был(а) " + elapsedMillis / 60000L + " мин назад";
            } else if (isSameDay(lastSeenTimeMillis, System.currentTimeMillis())) {
                return "был(а) в " + this.messageTimeFormat.format(new Date(lastSeenTimeMillis));
            } else {
                return isSameDay(lastSeenTimeMillis, System.currentTimeMillis() - 86400000L)
                ? "был(а) вчера в " + this.messageTimeFormat.format(new Date(lastSeenTimeMillis))
                : "был(а) " + this.compactDateFormat.format(new Date(lastSeenTimeMillis));
            }
        }
    }
// https://t.me/pasterplace

    private static boolean isSameDay(long firstTimestampMillis, long secondTimestampMillis) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(firstTimestampMillis);
        Calendar instance2 = Calendar.getInstance();
        instance2.setTimeInMillis(secondTimestampMillis);
        return instance.get(1) == instance2.get(1) && instance.get(6) == instance2.get(6);
    }
// https://t.me/pasterplace

    private static boolean containsPoint(float mouseX, float mouseY, float left, float top, float width, float height) {
        return mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
    }
// https://t.me/pasterplace

    private record ChatOverlayAction(float left, float top, float width, float height, Runnable action, Chat chat) {

        private ChatOverlayAction(float left, float top, float width, float height, Runnable action, Chat chat) {
            this.left = left;
            this.top = top;
            this.width = width;
            this.height = height;
            this.action = action;
            this.chat = chat;
        }
    }
// https://t.me/pasterplace

    private record EmojiLineRenderData(List<EmojiTextRenderer.EmojiLineLayout> lines, float width, float height, boolean hasOverflow, boolean centered) {

        private EmojiLineRenderData(List<EmojiTextRenderer.EmojiLineLayout> lines, float width, float height, boolean hasOverflow, boolean centered) {
            this.lines = lines;
            this.width = width;
            this.height = height;
            this.hasOverflow = hasOverflow;
            this.centered = centered;
        }
    }
}

