package dev.daamky;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

public final class Plus extends MenuBackground {
    private static final String sourceAttributionUrl = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   private static final float leftPanelWidth = 92.0F;
   private static final float mainPanelWidth = 188.0F;
   private static final float cardCornerRadius = 8.0F;
   private static final float searchFieldHeight = 15.0F;
   private static final float accountRowHeight = 26.0F;
   private static final float panelGap = 4.0F;
   private static final float accountListHeight = 120.0F;
   private static final SimpleDateFormat accountCreatedAtFormat = new SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.ROOT);
   private static final SimpleDateFormat lastLoginFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ROOT);
   private final Screen previousScreen;
   private final Accounts accountsStore = Accounts.getInstance();
   private final SearchTextField nicknameInput = new SearchTextField("Enter nickname", 16, this::addAccountFromInput);
   private final SearchTextField accountFilterInput = new SearchTextField("Search", 16, text -> {});
   private float accountListScrollOffset;
   private float targetScrollOffset;
   private final Map<String, float[]> accountCardPositions = new HashMap<>();
   private final Map<String, Long> accountEntryStartTimes = new HashMap<>();
   private final List<Plus.AccountRowAnimation> removingAccountAnimations = new ArrayList<>();
   private String statusMessage = "";
   private long statusMessageStartedAtMillis;
// https://t.me/pasterplace

   public Plus(Screen screen) {
      super("Accounts");
      this.previousScreen = screen;
   }
// https://t.me/pasterplace

   @Override
   protected float getScreenScale() {
      return 1.1F;
   }
// https://t.me/pasterplace

   @Override
   public void close() {
      this.transitionTo(this.previousScreen);
   }
// https://t.me/pasterplace

   private float getAccountPanelLeft() {
      return (this.viewportWidth - 288.0F) * 0.5F;
   }
// https://t.me/pasterplace

   private float getAccountPanelTop() {
      return this.viewportHeight * 0.5F - 62.0F;
   }
// https://t.me/pasterplace

   private float getMenuBlurRadius() {
      return Math.min(Accent.getInstance().getCornerRadius() * 0.5F, 5.0F);
   }
// https://t.me/pasterplace

   private List<Accounts.AccountEntry> getFilteredAccounts() {
      String searchQuery = this.accountFilterInput.getText().trim().toLowerCase(Locale.ROOT);
      List<Accounts.AccountEntry> accounts = new ArrayList<>();
      for (Accounts.AccountEntry account : this.accountsStore.getAccounts()) {
         if (searchQuery.isEmpty() || account.username().toLowerCase(Locale.ROOT).contains(searchQuery)) {
            accounts.add(account);
         }
      }
      return accounts;
   }
// https://t.me/pasterplace

   private void addAccountFromInput(String username) {
      if (!this.accountsStore.isValidUsername(username.trim())) {
         this.showStatusMessage("Nickname must be 3-16 chars: A-Z, 0-9, _");
      } else {
         Accounts.AccountEntry addedAccount = this.accountsStore.getOrCreateAccount(username);
         this.nicknameInput.setText("");
         this.showStatusMessage("Added " + addedAccount.username());
      }
   }
// https://t.me/pasterplace

   private void showStatusMessage(String message) {
      this.statusMessage = message;
      this.statusMessageStartedAtMillis = System.currentTimeMillis();
   }
// https://t.me/pasterplace

   @Override
   protected void drawContent(DrawContext drawContext, float contentAlpha, long currentTimeMillis) {
      this.accountListScrollOffset = this.accountListScrollOffset + (this.targetScrollOffset - this.accountListScrollOffset) * (1.0F - (float)Math.exp(-this.frameDeltaSeconds * 16.0F));
      float menuAlpha = 1.0F - this.getTransitionProgress();
      float panelLeft = this.getAccountPanelLeft();
      float panelTop = this.getAccountPanelTop();
      float blurRadius = this.getMenuBlurRadius();
      float centerX = this.viewportWidth * 0.5F;
      float headerProgress = this.easeOutCubic(0.0F, 550.0F);
      float headerY = panelTop - 26.0F - (1.0F - headerProgress) * 10.0F;
      this.pushPivotScale(drawContext, centerX, headerY, 0.9F + 0.1F * this.easeOutBack(0.0F, 650.0F));
      this.drawText(
         drawContext, BOLD_FONT, "ACCOUNTS", centerX, headerY, 24.0F, HorizontalTextAlignment.CENTER, Accent.getTextColorWithAlpha((int)(245.0F * headerProgress * menuAlpha))
      );
      this.popTransformMatrix(drawContext);
      float mainPanelWidth = 304.0F;
      float mainPanelHeight = 154.0F;
      float panelVisibility = this.easeOutCubic(60.0F, 450.0F);
      this.pushPivotScale(drawContext, centerX, panelTop - 8.0F + mainPanelHeight * 0.5F, 0.94F + 0.06F * this.easeOutBack(60.0F, 600.0F) - this.getTransitionProgress() * 0.03F);
      this.drawAccentPanel(drawContext, panelLeft - 8.0F, panelTop - 8.0F, mainPanelWidth, mainPanelHeight, panelVisibility * menuAlpha);
      float addPanelProgress = this.easeOutCubic(160.0F, 450.0F);
      this.drawAddAccountPanel(drawContext, panelLeft - (1.0F - addPanelProgress) * 10.0F, panelTop, blurRadius, addPanelProgress * menuAlpha, currentTimeMillis);
      float sessionPanelProgress = this.easeOutCubic(240.0F, 450.0F);
      this.drawActiveSessionPanel(drawContext, panelLeft - (1.0F - sessionPanelProgress) * 10.0F, panelTop + 78.0F, blurRadius, sessionPanelProgress * menuAlpha);
      float accountPanelProgress = this.easeOutCubic(200.0F, 450.0F);
      this.drawAccountListPanel(drawContext, panelLeft + 92.0F + 8.0F + (1.0F - accountPanelProgress) * 10.0F, panelTop, blurRadius, accountPanelProgress * menuAlpha, currentTimeMillis);
      this.popTransformMatrix(drawContext);
      float hintVisibility = this.easeOutCubic(380.0F, 450.0F) * menuAlpha;
      float hintY = panelTop + 15.0F + 6.0F + 120.0F + 18.0F + (1.0F - hintVisibility) * 6.0F;
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         "CTRL + V: Create an account with a copied nickname",
         centerX,
         hintY,
         4.4F,
         HorizontalTextAlignment.CENTER,
         Accent.getDeepTextColor((int)(170.0F * hintVisibility))
      );
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         "CTRL + Z: Restore deleted account",
         centerX,
         hintY + 7.0F,
         4.4F,
         HorizontalTextAlignment.CENTER,
         Accent.getDeepTextColor((int)(170.0F * hintVisibility))
      );
      long statusElapsedMillis = currentTimeMillis - this.statusMessageStartedAtMillis;
      if (!this.statusMessage.isEmpty() && statusElapsedMillis < 2500L) {
         float statusEntryProgress = Math.min(1.0F, (float)statusElapsedMillis / 220.0F);
         statusEntryProgress = 1.0F - (1.0F - statusEntryProgress) * (1.0F - statusEntryProgress) * (1.0F - statusEntryProgress);
         float statusExitProgress = Math.min(1.0F, (float)(2500L - statusElapsedMillis) / 350.0F);
         float statusAlpha = contentAlpha * statusEntryProgress * statusExitProgress;
         float statusY = hintY + 20.0F + (1.0F - statusEntryProgress) * 6.0F;
         float statusWidth = Medium.DEFAULT_FONT.measureWidth(this.statusMessage, 4.8F) + 14.0F;
         this.pushPivotScale(drawContext, centerX, statusY, 0.9F + 0.1F * statusEntryProgress);
         this.drawGlassPanel(drawContext, centerX - statusWidth * 0.5F, statusY - 6.0F, statusWidth, 12.0F, 6.0F, statusAlpha, 0.4F);
         this.drawText(
            drawContext,
            Medium.DEFAULT_FONT,
            this.statusMessage,
            centerX,
            statusY,
            4.8F,
            HorizontalTextAlignment.CENTER,
            Accent.getAccentColorWithAlpha((int)(235.0F * statusAlpha))
         );
         this.popTransformMatrix(drawContext);
      }

      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         "Esc - back",
         centerX,
         this.viewportHeight - 10.0F,
         4.2F,
         HorizontalTextAlignment.CENTER,
         Accent.getDeepTextColor((int)(80.0F * hintVisibility))
      );
   }
// https://t.me/pasterplace

   private void drawPanelHeader(DrawContext drawContext, String panelTitle, String panelIcon, float panelLeft, float panelTop, float panelWidth, float panelOpacity, float contentOpacity) {
      this.drawGlassPanel(drawContext, panelLeft, panelTop, panelWidth, 15.0F, panelOpacity, contentOpacity, 0.0F);
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         panelTitle,
         panelLeft + 6.0F,
         panelTop + 7.5F,
         5.0F,
         HorizontalTextAlignment.LEFT,
         Accent.getTextColorWithAlpha((int)(230.0F * contentOpacity))
      );
      this.drawCenteredText(drawContext, panelIcon, panelLeft + panelWidth - 8.0F, panelTop + 7.5F, 5.4F, Accent.getDarkerTextColor((int)(200.0F * contentOpacity)));
   }
// https://t.me/pasterplace

   private void drawAddAccountPanel(DrawContext drawContext, float panelLeft, float panelTop, float panelOpacity, float contentOpacity, long currentTimeMillis) {
      this.drawPanelHeader(drawContext, "Account panel", "✿", panelLeft, panelTop, 92.0F, panelOpacity, contentOpacity);
      float buttonRowTop = panelTop + 15.0F + 3.0F;
      this.drawGlassPanel(drawContext, panelLeft, buttonRowTop, 92.0F, 56.0F, panelOpacity, contentOpacity, 0.0F);
      this.nicknameInput.render(drawContext, panelLeft + 5.0F, buttonRowTop + 5.0F, 82.0F, 14.0F, currentTimeMillis, contentOpacity);
      float addButtonHover = this.getHoverProgress("plus", this.isMouseWithin(panelLeft + 92.0F - 17.0F, buttonRowTop + 5.0F, 12.0F, 14.0F));
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         "+",
         panelLeft + 92.0F - 11.0F,
         buttonRowTop + 12.0F,
         7.0F,
         HorizontalTextAlignment.CENTER,
         Accent.getDeepTextColor((int)((140.0F + 115.0F * addButtonHover) * contentOpacity))
      );
      this.drawActionButton(drawContext, "add", "Add", "D", panelLeft + 5.0F, buttonRowTop + 22.0F, panelOpacity, contentOpacity);
      this.drawActionButton(drawContext, "gen", "Generate", "E", panelLeft + 5.0F, buttonRowTop + 39.0F, panelOpacity, contentOpacity);
   }
// https://t.me/pasterplace

   private void drawActionButton(DrawContext drawContext, String actionId, String buttonLabel, String shortcutLabel, float buttonLeft, float buttonTop, float panelOpacity, float contentOpacity) {
      float buttonWidth = 82.0F;
      float buttonHover = this.getHoverProgress(actionId, this.isMouseWithin(buttonLeft, buttonTop, buttonWidth, 14.0F));
      this.pushPivotScale(drawContext, buttonLeft + buttonWidth * 0.5F, buttonTop + 7.0F, (1.0F + 0.025F * buttonHover) * this.getTextChangeScale(actionId));
      this.drawGlassPanel(drawContext, buttonLeft, buttonTop, buttonWidth, 14.0F, panelOpacity, contentOpacity, buttonHover);
      if (buttonHover > 0.01F) {
         RoundedGradientRectBuilder.at(buttonLeft + 1.5F, buttonTop + 7.0F - 3.0F * buttonHover, 1.0F, 6.0F * buttonHover)
            .cornerRadius(0.5F)
            .color(Accent.getAccentColorWithAlpha((int)(255.0F * contentOpacity * buttonHover)))
            .render(drawContext);
      }
// https://t.me/pasterplace

      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         buttonLabel,
         buttonLeft + 6.0F + 1.5F * buttonHover,
         buttonTop + 7.0F,
         4.8F,
         HorizontalTextAlignment.LEFT,
         Accent.getDarkerTextColor((int)((190.0F + 65.0F * buttonHover) * contentOpacity))
      );
      this.pushPivotScale(drawContext, buttonLeft + buttonWidth - 7.0F, buttonTop + 7.0F, (1.0F + 0.12F * buttonHover) * (2.0F - this.getTextChangeScale(actionId)));
      this.drawCenteredText(
         drawContext,
         shortcutLabel,
         buttonLeft + buttonWidth - 7.0F,
         buttonTop + 7.0F,
         5.0F,
         Accent.getDarkerTextColor((int)((170.0F + 85.0F * buttonHover) * contentOpacity)).mix(Accent.getAccentColorWithAlpha((int)(255.0F * contentOpacity)), buttonHover * 0.6F)
      );
      this.popTransformMatrix(drawContext);
      this.popTransformMatrix(drawContext);
   }
// https://t.me/pasterplace

   private void drawActiveSessionPanel(DrawContext drawContext, float panelLeft, float panelTop, float panelOpacity, float contentOpacity) {
      this.drawPanelHeader(drawContext, "Active session", "D", panelLeft, panelTop, 92.0F, panelOpacity, contentOpacity);
      float sessionPanelTop = panelTop + 15.0F + 3.0F;
      this.drawGlassPanel(drawContext, panelLeft, sessionPanelTop, 92.0F, 26.0F, panelOpacity, contentOpacity, 0.0F);
      MinecraftClient client = MinecraftClient.getInstance();
      PlayerSkinQuadBuilder.at(panelLeft + 5.0F, sessionPanelTop + 4.0F, 18.0F)
         .localPlayerSkin()
         .cornerRadius(3.0F)
         .opacity(contentOpacity)
         .render(drawContext);
      String username = client.getSession().getUsername();
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         Medium.DEFAULT_FONT.truncateToWidth(username, 5.2F, 60.0F),
         panelLeft + 28.0F,
         sessionPanelTop + 10.0F,
         5.2F,
         HorizontalTextAlignment.LEFT,
         Accent.getTextColorWithAlpha((int)(240.0F * contentOpacity))
      );
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         lastLoginFormat.format(new Date()),
         panelLeft + 28.0F,
         sessionPanelTop + 17.5F,
         4.2F,
         HorizontalTextAlignment.LEFT,
         Accent.getDeepTextColor((int)(150.0F * contentOpacity))
      );
   }
// https://t.me/pasterplace

   private void drawAccountListPanel(DrawContext drawContext, float panelLeft, float panelTop, float blurRadius, float contentOpacity, long currentTimeMillis) {
      float leftPanelWidth = 92.0F;
      this.accountFilterInput.render(drawContext, panelLeft, panelTop, leftPanelWidth, 15.0F, currentTimeMillis, contentOpacity);
      this.drawCenteredText(drawContext, "M", panelLeft + leftPanelWidth - 7.0F, panelTop + 7.5F, 4.6F, Accent.getDeepTextColor((int)(160.0F * contentOpacity)));
      float searchButtonHover = panelLeft + leftPanelWidth + 4.0F;
      this.drawGlassPanel(drawContext, searchButtonHover, panelTop, 188.0F - leftPanelWidth - 4.0F, 15.0F, blurRadius, contentOpacity, 0.0F);
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         this.accountsStore.getAccountCount() + " Accounts in list",
         panelLeft + 188.0F - 6.0F,
         panelTop + 7.5F,
         4.6F,
         HorizontalTextAlignment.RIGHT,
         Accent.getDeepTextColor((int)(140.0F * contentOpacity))
      );
      float accountListTop = panelTop + 15.0F + 3.0F;
      this.drawGlassPanel(drawContext, panelLeft, accountListTop, 188.0F, 120.0F, blurRadius, contentOpacity, 0.0F);
      List<Accounts.AccountEntry> items = this.getFilteredAccounts();
      this.drawRemovingAccountAnimations(drawContext, accountListTop, blurRadius, contentOpacity);
      if (items.isEmpty() && this.removingAccountAnimations.isEmpty()) {
         this.drawCenteredText(drawContext, "G", panelLeft + 94.0F, accountListTop + 60.0F - 6.0F, 14.0F, Accent.getDeepTextColor((int)(70.0F * contentOpacity)));
         this.drawText(
            drawContext,
            Medium.DEFAULT_FONT,
            "No accounts yet",
            panelLeft + 94.0F,
            accountListTop + 60.0F + 8.0F,
            4.8F,
            HorizontalTextAlignment.CENTER,
            Accent.getDeepTextColor((int)(120.0F * contentOpacity))
         );
      } else {
         float scrollContentHeight = this.getAccountListContentHeight(items.size());
         this.targetScrollOffset = Math.clamp(this.targetScrollOffset, 0.0F, scrollContentHeight);
         float accountCardWidth = 86.0F;
         drawContext.enableScissor(
            PixelRounding.II1Il11l111II11IIl(panelLeft),
            PixelRounding.II1Il11l111II11IIl(accountListTop + 1.0F),
            PixelRounding.l1I1IlllIlI(panelLeft + 188.0F),
            PixelRounding.l1I1IlllIlI(accountListTop + 120.0F - 1.0F)
         );

         try {
            float positionLerpFactor = 1.0F - (float)Math.exp(-this.frameDeltaSeconds * 14.0F);
            boolean empty = this.accountEntryStartTimes.isEmpty();

            for (int accountIndex = 0; accountIndex < items.size(); accountIndex++) {
               Accounts.AccountEntry account = items.get(accountIndex);
               float cardColumnOffsetX = accountIndex % 2 * (accountCardWidth + 4.0F);
               float cardRowOffsetY = accountIndex / 2 * 30.0F;
               long accountEntryTimeMillis = currentTimeMillis + (empty ? 220L + accountIndex * 40L : 0L);
               this.accountEntryStartTimes.putIfAbsent(account.username(), accountEntryTimeMillis);
               float[] position = this.accountCardPositions
                  .computeIfAbsent(account.username(), username -> new float[]{cardColumnOffsetX, cardRowOffsetY + 10.0F});
               position[0] += (cardColumnOffsetX - position[0]) * positionLerpFactor;
               position[1] += (cardRowOffsetY - position[1]) * positionLerpFactor;
               float appearanceProgress = Math.clamp((float)(currentTimeMillis - this.accountEntryStartTimes.get(account.username())) / 380.0F, 0.0F, 1.0F);
               appearanceProgress = 1.0F - (1.0F - appearanceProgress) * (1.0F - appearanceProgress) * (1.0F - appearanceProgress);
               float cardLeft = panelLeft + 6.0F + position[0];
               float cardTop = accountListTop + 6.0F + position[1] - this.accountListScrollOffset + (1.0F - appearanceProgress) * 8.0F;
               if (!(cardTop + 26.0F < accountListTop) && !(cardTop > accountListTop + 120.0F)) {
                  this.pushPivotScale(drawContext, cardLeft + accountCardWidth * 0.5F, cardTop + 13.0F, 0.92F + 0.08F * appearanceProgress);
                  this.drawAccountCard(drawContext, account, cardLeft, cardTop, accountCardWidth, blurRadius, contentOpacity * appearanceProgress, accountListTop);
                  this.popTransformMatrix(drawContext);
               }
            }
         } finally {
            drawContext.disableScissor();
         }

         if (scrollContentHeight > 0.0F) {
            float scrollbarTrackHeight = 112.0F;
            float scrollbarThumbHeight = Math.max(12.0F, scrollbarTrackHeight * 120.0F / (120.0F + scrollContentHeight));
            float scrollbarThumbTop = accountListTop + 4.0F + (scrollbarTrackHeight - scrollbarThumbHeight) * (this.accountListScrollOffset / scrollContentHeight);
            RoundedGradientRectBuilder.at(panelLeft + 188.0F - 3.0F, scrollbarThumbTop, 1.2F, scrollbarThumbHeight)
               .cornerRadius(0.6F)
               .color(Accent.getAccentColorWithAlpha((int)(120.0F * contentOpacity)))
               .render(drawContext);
         }
      }
   }
// https://t.me/pasterplace

   private void drawAccountCard(
      DrawContext drawContext, Accounts.AccountEntry account, float cardLeft, float cardTop, float cardWidth, float cornerRadius, float cardOpacity, float listTop
   ) {
      boolean cardVisible = this.mouseY >= listTop && this.mouseY <= listTop + 120.0F;
      String cardAnimationKey = "card:" + account.username();
      float hoverProgress = this.getHoverProgress(cardAnimationKey, cardVisible && this.isMouseWithin(cardLeft, cardTop, cardWidth, 26.0F));
      float activeProgress = this.getHoverProgress(cardAnimationKey + ":active", this.accountsStore.isCurrentAccount(account));
      this.pushPivotScale(drawContext, cardLeft + cardWidth * 0.5F, cardTop + 13.0F, (1.0F + 0.025F * hoverProgress) * this.getTextChangeScale(cardAnimationKey));
      this.drawGlassPanel(drawContext, cardLeft, cardTop, cardWidth, 26.0F, cornerRadius, cardOpacity, Math.max(hoverProgress, activeProgress * 0.6F));
      if (activeProgress > 0.01F) {
         RoundedGradientRectBuilder.at(cardLeft, cardTop, cardWidth, 26.0F)
            .cornerRadius(cornerRadius)
            .color(Accent.getAccentColorWithAlpha((int)(22.0F * activeProgress * cardOpacity)))
            .render(drawContext);
         RoundedGradientRectBuilder.at(cardLeft + 1.5F, cardTop + 13.0F - 6.0F * activeProgress, 1.2F, 12.0F * activeProgress)
            .cornerRadius(0.6F)
            .color(Accent.getAccentColorWithAlpha((int)(255.0F * activeProgress * cardOpacity)))
            .render(drawContext);
      }
// https://t.me/pasterplace

      PlayerSkinQuadBuilder.at(cardLeft + 4.0F, cardTop + 4.0F, 18.0F)
         .size(account.lastLogin())
         .cornerRadius(3.0F)
         .opacity(cardOpacity)
         .render(drawContext);
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         Medium.DEFAULT_FONT.truncateToWidth(account.username(), 5.2F, cardWidth - 40.0F),
         cardLeft + 27.0F,
         cardTop + 10.0F,
         5.2F,
         HorizontalTextAlignment.LEFT,
         Accent.getTextColorWithAlpha((int)(240.0F * cardOpacity))
      );
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         accountCreatedAtFormat.format(new Date(account.createdAt())),
         cardLeft + 27.0F,
         cardTop + 17.5F,
         4.0F,
         HorizontalTextAlignment.LEFT,
         Accent.getDeepTextColor((int)(150.0F * cardOpacity))
      );
      float actionIconX = cardLeft + cardWidth - 9.0F;
      float pinHoverProgress = this.getHoverProgress(cardAnimationKey + ":pin", cardVisible && this.isMouseWithin(actionIconX - 3.5F, cardTop + 3.0F, 7.0F, 9.0F));
      RgbaColor pinColor = account.pinned()
         ? RgbaColor.rgba(250, 205, 60, (int)(255.0F * cardOpacity))
         : Accent.getDeepTextColor((int)((110.0F + 120.0F * pinHoverProgress) * cardOpacity));
      RoundedGradientRectBuilder.at(actionIconX - 2.0F, cardTop + 5.0F, 4.0F, 3.4F).cornerRadius(1.0F).color(pinColor).render(drawContext);
      RoundedGradientRectBuilder.at(actionIconX - 3.0F, cardTop + 8.0F, 6.0F, 0.9F).cornerRadius(0.4F).color(pinColor).render(drawContext);
      RoundedGradientRectBuilder.at(actionIconX - 0.35F, cardTop + 8.6F, 0.7F, 2.6F).cornerRadius(0.3F).color(pinColor).render(drawContext);
      float deleteHoverProgress = this.getHoverProgress(cardAnimationKey + ":del", cardVisible && this.isMouseWithin(actionIconX - 3.5F, cardTop + 14.0F, 7.0F, 9.0F));
      RgbaColor deleteColor = Accent.getDeepTextColor((int)((110.0F + 60.0F * deleteHoverProgress) * cardOpacity))
         .mix(RgbaColor.rgba(240, 70, 70, (int)(255.0F * cardOpacity)), deleteHoverProgress);
      RoundedGradientRectBuilder.at(actionIconX - 2.6F, cardTop + 15.6F, 5.2F, 0.8F)
         .cornerRadius(0.4F)
         .color(deleteColor)
         .render(drawContext);
      RoundedGradientRectBuilder.at(actionIconX - 0.9F, cardTop + 14.8F, 1.8F, 0.8F)
         .cornerRadius(0.4F)
         .color(deleteColor)
         .render(drawContext);
      RoundedRectBuilder.createAt(actionIconX - 2.0F, cardTop + 16.8F, 4.0F, 4.6F)
         .cornerRadii(0.0F, 0.0F, 0.8F, 0.8F)
         .borderWidth(0.7F)
         .color(deleteColor)
         .render(drawContext);
      this.popTransformMatrix(drawContext);
   }
// https://t.me/pasterplace

   private void drawRemovingAccountAnimations(DrawContext drawContext, float accountListTop, float cornerRadius, float contentOpacity) {
      Iterator<Plus.AccountRowAnimation> removalAnimationIterator = this.removingAccountAnimations.iterator();

      while (removalAnimationIterator.hasNext()) {
         Plus.AccountRowAnimation removalAnimation = removalAnimationIterator.next();
         float removalProgress = (float)(this.frameTimeMillis - removalAnimation.updatedAtMillis()) / 240.0F;
         if (removalProgress >= 1.0F) {
            removalAnimationIterator.remove();
         } else {
            float easedRemovalProgress = removalProgress * removalProgress;
            float cardLeft = removalAnimation.cardLeft();
            float cardTop = removalAnimation.cardTop() - this.accountListScrollOffset;
            if (!(cardTop + 26.0F < accountListTop) && !(cardTop > accountListTop + 120.0F)) {
               this.pushPivotScale(drawContext, cardLeft + removalAnimation.alpha() * 0.5F, cardTop + 13.0F, 1.0F - 0.15F * easedRemovalProgress);
               RoundedGradientRectBuilder.at(cardLeft, cardTop, removalAnimation.alpha(), 26.0F)
                  .cornerRadius(cornerRadius)
                  .color(RgbaColor.rgba(240, 70, 70, (int)(40.0F * (1.0F - easedRemovalProgress) * contentOpacity)))
                  .render(drawContext);
               PlayerSkinQuadBuilder.at(cardLeft + 4.0F, cardTop + 4.0F, 18.0F)
                  .size(removalAnimation.account().lastLogin())
                  .cornerRadius(3.0F)
                  .opacity((1.0F - easedRemovalProgress) * contentOpacity)
                  .render(drawContext);
               this.drawText(
                  drawContext,
                  Medium.DEFAULT_FONT,
                  removalAnimation.account().username(),
                  cardLeft + 27.0F,
                  cardTop + 10.0F,
                  5.2F,
                  HorizontalTextAlignment.LEFT,
                  Accent.getTextColorWithAlpha((int)(240.0F * (1.0F - easedRemovalProgress) * contentOpacity))
               );
               this.popTransformMatrix(drawContext);
            }
         }
      }
   }
// https://t.me/pasterplace

   private float getAccountListContentHeight(int accountCount) {
      int rowCount = (accountCount + 1) / 2;
      float contentHeight = 6.0F + rowCount * 30.0F + 2.0F;
      return Math.max(0.0F, contentHeight - 120.0F);
   }
// https://t.me/pasterplace

   @Override
   protected boolean handleContentClick(float mouseX, float mouseY, int mouseButton) {
      float panelLeft = this.getAccountPanelLeft();
      float panelTop = this.getAccountPanelTop();
      float addPanelTop = panelTop + 15.0F + 3.0F;
      this.nicknameInput.setFocused(mouseX >= panelLeft + 5.0F && mouseX <= panelLeft + 92.0F - 5.0F && mouseY >= addPanelTop + 5.0F && mouseY <= addPanelTop + 19.0F);
      float searchPanelLeft = panelLeft + 92.0F + 8.0F;
      float searchPanelWidth = 92.0F;
      this.accountFilterInput.setFocused(mouseX >= searchPanelLeft && mouseX <= searchPanelLeft + searchPanelWidth && mouseY >= panelTop && mouseY <= panelTop + 15.0F);
      if (mouseButton != 0) {
         return true;
      }

      if (this.nicknameInput.isFocused() && mouseX >= panelLeft + 92.0F - 17.0F) {
         this.addAccountFromInput(this.nicknameInput.getText());
         return true;
      }

      if (mouseX >= panelLeft + 5.0F && mouseX <= panelLeft + 92.0F - 5.0F) {
         if (mouseY >= addPanelTop + 22.0F && mouseY <= addPanelTop + 36.0F) {
            this.markTextChanged("add");
            this.addAccountFromInput(this.nicknameInput.getText());
            return true;
         }

         if (mouseY >= addPanelTop + 39.0F && mouseY <= addPanelTop + 53.0F) {
            this.markTextChanged("gen");
            Accounts.AccountEntry account = this.accountsStore.createRandomAccount();
            if (account != null) {
               this.showStatusMessage("Generated " + account.username());
            }

            return true;
         }
      }

      float accountListTop = panelTop + 15.0F + 3.0F;
      if (!(mouseY < accountListTop) && !(mouseY > accountListTop + 120.0F) && !(mouseX < searchPanelLeft) && !(mouseX > searchPanelLeft + 188.0F)) {
         List<Accounts.AccountEntry> items = this.getFilteredAccounts();
         float accountCardWidth = 86.0F;

         for (int accountIndex = 0; accountIndex < items.size(); accountIndex++) {
            float cardLeft = searchPanelLeft + 6.0F + accountIndex % 2 * (accountCardWidth + 4.0F);
            float cardTop = accountListTop + 6.0F + accountIndex / 2 * 30.0F - this.accountListScrollOffset;
            if (!(mouseX < cardLeft) && !(mouseX > cardLeft + accountCardWidth) && !(mouseY < cardTop) && !(mouseY > cardTop + 26.0F)) {
               Accounts.AccountEntry account = items.get(accountIndex);
               float actionIconX = cardLeft + accountCardWidth - 9.0F;
               if (mouseX >= actionIconX - 3.5F && mouseX <= actionIconX + 3.5F) {
                  if (mouseY >= cardTop + 3.0F && mouseY <= cardTop + 12.0F) {
                     this.accountsStore.togglePinned(account);
                     return true;
                  }

                  if (mouseY >= cardTop + 14.0F && mouseY <= cardTop + 23.0F) {
                     this.removingAccountAnimations
                        .add(new Plus.AccountRowAnimation(account, cardLeft, cardTop + this.accountListScrollOffset, accountCardWidth, this.frameTimeMillis));
                     this.accountCardPositions.remove(account.username());
                     this.accountEntryStartTimes.remove(account.username());
                     this.accountsStore.removeAccount(account);
                     this.showStatusMessage("Removed " + account.username() + " (CTRL + Z to restore)");
                     return true;
                  }
               }

               this.markTextChanged("card:" + account.username());
               this.accountsStore.login(account);
               this.showStatusMessage("Logged in as " + account.username());
               return true;
            }
         }

         return true;
      } else {
         return true;
      }
   }
// https://t.me/pasterplace

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalScrollAmount, double verticalScrollAmount) {
      this.targetScrollOffset -= (float)verticalScrollAmount * 20.0F;
      return true;
   }
// https://t.me/pasterplace

   public boolean keyPressed(KeyInput keyInput) {
      int keyCode = keyInput.getKeycode();
      if (keyCode == 256 && !this.nicknameInput.isFocused() && !this.accountFilterInput.isFocused()) {
         this.close();
         return true;
      } else if (this.nicknameInput.isFocused()) {
         return this.nicknameInput.keyPressed(keyInput);
      } else if (this.accountFilterInput.isFocused()) {
         return this.accountFilterInput.keyPressed(keyInput);
      } else if (keyInput.hasCtrl() && keyCode == 86) {
         String clipboardNickname = MinecraftClient.getInstance().keyboard.getClipboard();
         this.addAccountFromInput(clipboardNickname == null ? "" : clipboardNickname.trim());
         return true;
      } else if (keyInput.hasCtrl() && keyCode == 90) {
         this.showStatusMessage(this.accountsStore.restoreLastRemovedAccount() ? "Account restored" : "Nothing to restore");
         return true;
      } else {
         return super.keyPressed(keyInput);
      }
   }
// https://t.me/pasterplace

   public boolean charTyped(CharInput charInput) {
      if (this.nicknameInput.isFocused()) {
         return this.nicknameInput.charTyped(charInput);
      } else if (this.accountFilterInput.isFocused()) {
         this.targetScrollOffset = 0.0F;
         return this.accountFilterInput.charTyped(charInput);
      } else {
         return super.charTyped(charInput);
      }
   }
// https://t.me/pasterplace

   private record AccountRowAnimation(Accounts.AccountEntry account, float cardLeft, float cardTop, float alpha, long updatedAtMillis) {

      private AccountRowAnimation(Accounts.AccountEntry account, float cardLeft, float cardTop, float alpha, long updatedAtMillis) {
         this.account = account;
         this.cardLeft = cardLeft;
         this.cardTop = cardTop;
         this.alpha = alpha;
         this.updatedAtMillis = updatedAtMillis;
      }
   }
}

