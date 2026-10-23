package dev.daamky;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;

public final class Notifications extends HudElementPosition {
    private static final String TELEGRAM_CONTACT_URL = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
    private static final float NOTIFICATION_HEIGHT = 18.0F;
    private static final float NOTIFICATION_SPACING = 3.0F;
    private static final float DEFAULT_NOTIFICATION_DURATION = 2.2F;
    private static final int MAX_VISIBLE_NOTIFICATIONS = 6;
    private static final float HUD_ELEMENT_WIDTH = 110.0F;
    private static Notifications INSTANCE;
    private final ModuleStructure notificationsModule;
    private final List<Notifications.NotificationAnimationState> notificationAnimations = new ArrayList<>();
    private final Map<ModuleStructure, Boolean> previousModuleEnabledStates = new IdentityHashMap<>();
    private boolean moduleStatesInitialized;
// https://t.me/pasterplace

    public Notifications(ModuleStructure moduleStructure) {
        super("Notifications", "notifications", 9999.0F, 9999.0F);
        this.notificationsModule = moduleStructure;
        this.elementWidth = 110.0F;
        this.elementHeight = 18.0F;

    }
// https://t.me/pasterplace

    public static void showNotification(String title, String description, RgbaColor l1I1IlllIlI4, float durationSeconds) {
        if (INSTANCE != null && INSTANCE.notificationsModule.isEnabled()) {
            INSTANCE.enqueueNotification(new Notifications.NotificationAnimationState(title, description, true, l1I1IlllIlI4, durationSeconds));
        }
    }
// https://t.me/pasterplace

    @Override
    public void clampPositionToScreen(float screenWidth, float screenHeight) {
        this.setPosition((screenWidth - this.elementWidth) * 0.5F, screenHeight * 0.5F + 20.0F);
    }
// https://t.me/pasterplace

    public void showNotification(String title, String description, boolean enabled) {
        this.enqueueNotification(new Notifications.NotificationAnimationState(title, description, enabled, null, 2.2F));
    }
// https://t.me/pasterplace

    private void enqueueNotification(Notifications.NotificationAnimationState notification) {
        this.notificationAnimations.add(0, notification);
// https://t.me/pasterplace

        while (this.notificationAnimations.size() > 6) {
            this.notificationAnimations.remove(this.notificationAnimations.size() - 1);
        }
    }
// https://t.me/pasterplace

    @Override
    public void updateAnimation(float frameDeltaSeconds) {
        ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();
        if (moduleRegistry != null) {
            for (Object module : moduleRegistry.getModules()) {
                if (module != this.notificationsModule) {
                    Boolean previousEnabledState = this.previousModuleEnabledStates.put((ModuleStructure)module, ((ModuleStructure)module).isEnabled());
                    if (this.moduleStatesInitialized && previousEnabledState != null && previousEnabledState != ((ModuleStructure)module).isEnabled()) {
                        this.showNotification(
                        ((ModuleStructure)module).getName(),
                        ((ModuleStructure)module).isEnabled() ? "Enabled" : "Disabled",
                        ((ModuleStructure)module).isEnabled()
                        );
                    }
                }
            }

            this.moduleStatesInitialized = true;
        }

        float nextVerticalOffset = 0.0F;
        Iterator notificationIterator = this.notificationAnimations.iterator();

        while (notificationIterator.hasNext()) {
            Object notification = (Notifications.NotificationAnimationState)notificationIterator.next();
            ((Notifications.NotificationAnimationState)notification).elapsedSeconds += frameDeltaSeconds;
            boolean withinLifetime = ((Notifications.NotificationAnimationState)notification).elapsedSeconds < ((Notifications.NotificationAnimationState)notification).durationSeconds;
            ((Notifications.NotificationAnimationState)notification).visibilityProgress = interpolate(
            ((Notifications.NotificationAnimationState)notification).visibilityProgress, withinLifetime ? 1.0F : 0.0F, 10.0F, frameDeltaSeconds
            );
            if (!withinLifetime && ((Notifications.NotificationAnimationState)notification).visibilityProgress < 0.01F) {
                notificationIterator.remove();
            } else {
                ((Notifications.NotificationAnimationState)notification).verticalOffset = interpolate(
                ((Notifications.NotificationAnimationState)notification).verticalOffset, nextVerticalOffset, 12.0F, frameDeltaSeconds
                );
                nextVerticalOffset += 21.0F * ((Notifications.NotificationAnimationState)notification).visibilityProgress;
            }
        }
    }
// https://t.me/pasterplace

    @Override
    public boolean isVisibleInCurrentScreen(boolean screenAllowsHud) {
        return screenAllowsHud || !this.notificationAnimations.isEmpty();
    }
// https://t.me/pasterplace

    @Override
    public void render(HudRenderContext renderContext, float hudOpacity) {
        DrawContext drawContext = renderContext.drawContext();
        float centerX = this.getX() + this.elementWidth * 0.5F;
        float baseY = this.getY();
        if (this.notificationAnimations.isEmpty()) {
            renderNotificationCard(drawContext, "Notifications", "Enabled", true, null, centerX, baseY, 1.0F, hudOpacity);
        } else {
            for (Object notification : this.notificationAnimations) {
                float animatedOpacity = hudOpacity * ((Notifications.NotificationAnimationState)notification).visibilityProgress;
                if (!(animatedOpacity <= 0.01F)) {
                    float notificationY = baseY
                    + ((Notifications.NotificationAnimationState)notification).verticalOffset
                    - (1.0F - ((Notifications.NotificationAnimationState)notification).visibilityProgress) * 6.0F;
                    float remainingTimeProgress = 1.0F
                    - Math.clamp(((Notifications.NotificationAnimationState)notification).elapsedSeconds / ((Notifications.NotificationAnimationState)notification).durationSeconds, 0.0F, 1.0F);
                    renderNotificationCard(
                    drawContext,
                    ((Notifications.NotificationAnimationState)notification).title,
                    ((Notifications.NotificationAnimationState)notification).description,
                    ((Notifications.NotificationAnimationState)notification).enabled,
                    ((Notifications.NotificationAnimationState)notification).accentColor,
                    centerX,
                    notificationY,
                    remainingTimeProgress,
                    animatedOpacity
                    );
                }
            }
        }
    }
// https://t.me/pasterplace

    private static void renderNotificationCard(
    DrawContext drawContext, String title, String description, boolean enabled, RgbaColor accentColor, float centerX, float topY, float remainingTimeProgress, float opacity
    ) {
        float titleWidth = BrokkeIcons.measureTextWidth(title, 5.7F);
        float descriptionWidth = BrokkeIcons.measureTextWidth(description, 5.7F);
        float progressRingSize = 7.0F;
        float cardWidth = 13.0F + titleWidth + 9.0F + descriptionWidth + 6.0F + progressRingSize + 6.0F;
        float cardLeft = centerX - cardWidth * 0.5F;
        BrokkeIcons.drawPanelBackground(drawContext, cardLeft, topY, cardWidth, 18.0F, opacity);
        float textCenterY = topY + 9.0F;
        RgbaColor statusColor = accentColor != null
        ? accentColor.withAlpha((int)(255.0F * opacity))
        : (enabled ? BrokkeIcons.whiteWithAlpha(opacity) : RgbaColor.rgba(255, 120, 120, (int)(255.0F * opacity)));
        BrokkeIcons.drawColorSwatch(drawContext, cardLeft + 8.0F, textCenterY, 3.5F, statusColor);
        float textX = cardLeft + 13.0F;
        BrokkeIcons.drawCenteredText(drawContext, title, textX, textCenterY, 5.7F, BrokkeIcons.lightGrayWithAlpha(opacity));
        textX += titleWidth + 4.5F;
        BrokkeIcons.drawVerticalDivider(drawContext, textX, textCenterY, opacity);
        textX += 4.5F;
        BrokkeIcons.drawCenteredText(drawContext, description, textX, textCenterY, 5.7F, BrokkeIcons.mutedGrayWithAlpha(opacity));
        AnimatedListHudElement.drawProgressRing(drawContext, cardLeft + cardWidth - 6.0F - progressRingSize * 0.5F, textCenterY, progressRingSize * 0.5F - 0.4F, 1.0F, 24, remainingTimeProgress, opacity);
    }
// https://t.me/pasterplace

    private static final class NotificationAnimationState {
        final String title;
        final String description;
        final boolean enabled;
        final RgbaColor accentColor;
        final float durationSeconds;
        float elapsedSeconds;
        float visibilityProgress;
        float verticalOffset;

        NotificationAnimationState(String title, String description, boolean enabled, RgbaColor accentColor, float durationSeconds) {
            this.title = title;
            this.description = description;
            this.enabled = enabled;
            this.accentColor = accentColor;
            this.durationSeconds = durationSeconds;
        }
    }
}

