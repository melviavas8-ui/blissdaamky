/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package dev.daamky;

import com.google.gson.JsonObject;
import dev.daamky.Accent;
import dev.daamky.TextBaselinePosition;
import dev.daamky.SearchTextField;
import dev.daamky.Medium;
import dev.daamky.PixelRounding;
import dev.daamky.GuiTextBuilder;
import dev.daamky.NotificationScheduler;
import dev.daamky.ColorUtils;
import dev.daamky.RoundedGradientRectBuilder;
import dev.daamky.ClientConfigManager;
import dev.daamky.CloudConfigClient;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

public final class ClaudeConfig {
    private static final float PANEL_PADDING = 5.0f;
    private static final float HEADER_TOP_SPACING = 11.5f;
    private static final float TITLE_BAR_HEIGHT = 7.5f;
    private static final float CONTROL_HEIGHT = 13.0f;
    private static final float ACTION_BUTTON_HEIGHT = 12.0f;
    private static final float CONFIG_ROW_HEIGHT = 15.0f;
    private static final float HEADER_VERTICAL_ADJUSTMENT = 2.5f;
    private static final float DETAIL_SECTION_GAP = 9.0f;
    private final SearchTextField nameSearchField = new SearchTextField("\u0418\u043c\u044f \u043a\u043e\u043d\u0444\u0438\u0433\u0430...", 32, this::saveCloudConfig);
    private final float[] actionHoverProgress = new float[8];
    private final SimpleDateFormat updatedAtFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private List<CloudConfigClient.CloudConfigEntry> configs = new ArrayList<CloudConfigClient.CloudConfigEntry>();
    private float[] configHoverProgress = new float[0];
    private String selectedConfigDisplayName;
    private boolean loading;
    private boolean initialized;
    private String statusMessage = "";
    private float animationDeltaSeconds = 0.016f;
    private float scrollOffset;
    private float targetScrollOffset;
    private float maxScrollOffset;
    private float x;
    private float y;
    private float width;
    private float contentHeight;
    private float detailsX;
    private float detailsY;
    private float detailsWidth;
    private static final float REFRESH_BUTTON_HEIGHT = 8.5f;
    private static final float SMALL_TEXT_SIZE = 4.2f;
    private static final float ACTION_ROW_HEIGHT = 15.0f;
    private static final float DETAIL_ROW_HEIGHT = 8.0f;
    private static final long DELETE_CONFIRMATION_WINDOW_MILLIS = 3000L;
    private static final int DEFAULT_ACTION_COLOR = 14704735;
    private String confirmationDisplayName;
    private long confirmationRequestedAtMillis;

    public void tickAnimation(float f) {
        this.animationDeltaSeconds = f;
        this.targetScrollOffset = Math.clamp(this.targetScrollOffset, 0.0f, this.maxScrollOffset);
        this.scrollOffset += (this.targetScrollOffset - this.scrollOffset) * (1.0f - (float)Math.exp(-6.5f * f));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(DrawContext drawContext, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11) {
        if (f11 <= 0.0f) {
            return;
        }
        if (!this.initialized) {
            this.initialized = true;
            this.refreshCloudConfigs();
        }
        this.x = f3;
        this.y = f4;
        this.width = f5;
        this.contentHeight = f6;
        this.detailsX = f7;
        this.detailsY = f8;
        this.detailsWidth = f9;
        long l = System.currentTimeMillis();
        this.renderConfigList(drawContext, f, f2, l, f11);
        drawContext.enableScissor(PixelRounding.II1Il11l111II11IIl(f7), PixelRounding.II1Il11l111II11IIl(f8), PixelRounding.l1I1IlllIlI(f7 + f9 - 6.0f), PixelRounding.l1I1IlllIlI(f8 + f10));
        try {
            this.renderConfigDetails(drawContext, f, f2, f11);
        }
        finally {
            drawContext.disableScissor();
        }
    }

    private float getConfigListTop() {
        return this.y + 11.5f + 7.5f;
    }

    private float getDetailsTop() {
        return this.getConfigListTop() + 13.0f + 9.0f;
    }

    private float getCreateButtonX() {
        return this.x + this.width - 5.0f - 13.0f;
    }

    private float getRefreshButtonWidth() {
        return Medium.DEFAULT_FONT.measureWidth("\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c", 4.2f) + 8.0f;
    }

    private float getRefreshButtonX() {
        return this.x + this.width - 5.0f - this.getRefreshButtonWidth();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderConfigList(DrawContext drawContext, float f, float f2, long l, float f3) {
        float f4 = this.y + 5.75f;
        GuiTextBuilder.createAt(Medium.ICON_FONT, "F", this.x + 5.0f + 2.8f, f4, 5.6f).centered().color(Accent.getAccentColorWithAlpha((int)(235.0f * f3))).render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "Claude config", this.x + 5.0f + 8.6f, f4, 5.4f).baselinePosition(TextBaselinePosition.CENTER).color(Accent.getTextColorWithAlpha((int)(245.0f * f3))).render(drawContext);
        float f5 = this.getActionHoverProgress(0, ClaudeConfig.containsPoint(f, f2, this.getRefreshButtonX(), f4 - 4.25f, this.getRefreshButtonWidth(), 8.5f));
        RoundedGradientRectBuilder.at(this.getRefreshButtonX(), f4 - 4.25f, this.getRefreshButtonWidth(), 8.5f).cornerRadius(4.25f).color(ColorUtils.interpolateColor(Accent.getTextColorWithAlpha((int)(10.0f * f3)).toArgb(), Accent.getAccentColorWithAlpha((int)(70.0f * f3)).toArgb(), f5)).render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, this.loading ? "..." : "\u041e\u0431\u043d\u043e\u0432\u0438\u0442\u044c", this.getRefreshButtonX() + this.getRefreshButtonWidth() * 0.5f, f4, 4.2f).centered().color(ColorUtils.interpolateColor(Accent.colorWithMutedAccent((int)(210.0f * f3)).toArgb(), Accent.getTextColorWithAlpha((int)(255.0f * f3)).toArgb(), f5)).render(drawContext);
        float f6 = this.x + 5.0f;
        float f7 = this.getCreateButtonX() - 3.0f - f6;
        this.nameSearchField.render(drawContext, f6, this.getConfigListTop(), f7, 13.0f, l, f3);
        float f8 = this.getActionHoverProgress(1, ClaudeConfig.containsPoint(f, f2, this.getCreateButtonX(), this.getConfigListTop(), 13.0f, 13.0f));
        RoundedGradientRectBuilder.at(this.getCreateButtonX(), this.getConfigListTop(), 13.0f, 13.0f).cornerRadius(Accent.getInstance().getCornerRadius() * 0.5f).color(ColorUtils.interpolateColor(Accent.getAccentColorWithAlpha((int)(60.0f * f3)).toArgb(), Accent.getAccentColorWithAlpha((int)(150.0f * f3)).toArgb(), f8)).render(drawContext);
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, "+", this.getCreateButtonX() + 6.5f, this.getConfigListTop() + 6.5f, 7.0f).centered().color(Accent.getTextColorWithAlpha((int)(255.0f * f3))).render(drawContext);
        float f9 = this.getDetailsTop() - 4.5f;
        RoundedGradientRectBuilder.at(this.x + 5.0f, f9 - 0.375f, this.width - 10.0f, 0.75f).cornerRadius(0.0f).color(Accent.getTextColorWithAlpha((int)(12.0f * f3))).render(drawContext);
        float f10 = this.getDetailsTop();
        float f11 = this.y + this.contentHeight - f10;
        this.maxScrollOffset = Math.max(0.0f, (float)this.configs.size() * 17.5f - 2.5f - f11);
        if (this.configs.isEmpty()) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, this.loading ? "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430..." : "\u041d\u0435\u0442 \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432", this.x + this.width * 0.5f, f10 + Math.min(f11 * 0.35f, 24.0f), 4.6f).centered().color(Accent.colorWithMutedAccent((int)(170.0f * f3))).render(drawContext);
            return;
        }
        drawContext.enableScissor(PixelRounding.II1Il11l111II11IIl(this.x), PixelRounding.II1Il11l111II11IIl(f10), PixelRounding.l1I1IlllIlI(this.x + this.width), PixelRounding.l1I1IlllIlI(f10 + f11));
        try {
            float f12 = f10 - this.scrollOffset;
            for (int i = 0; i < this.configs.size(); ++i) {
                CloudConfigClient.CloudConfigEntry l1I1IlllIlI2 = this.configs.get(i);
                boolean bl = l1I1IlllIlI2.displayName().equals(this.selectedConfigDisplayName);
                boolean bl2 = ClaudeConfig.containsPoint(f, f2, this.x + 5.0f, f12, this.width - 10.0f, 15.0f) && f2 >= f10 && f2 <= f10 + f11;
                int n = i;
                this.configHoverProgress[n] = this.configHoverProgress[n] + ((bl2 || bl ? 1.0f : 0.0f) - this.configHoverProgress[i]) * (1.0f - (float)Math.exp(-10.0f * this.animationDeltaSeconds));
                RoundedGradientRectBuilder.at(this.x + 5.0f, f12, this.width - 10.0f, 15.0f).cornerRadius(Accent.getInstance().getCornerRadius() * 0.5f).color(bl ? Accent.getAccentColorWithAlpha((int)(55.0f * f3)) : Accent.getTextColorWithAlpha((int)((5.0f + 8.0f * this.configHoverProgress[i]) * f3))).render(drawContext);
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(l1I1IlllIlI2.name(), 5.0f, this.width - 10.0f - 30.0f), this.x + 5.0f + 4.0f, f12 + 7.5f, 5.0f).baselinePosition(TextBaselinePosition.CENTER).color(Accent.getTextColorWithAlpha((int)(245.0f * f3))).render(drawContext);
                String configBadgeText = l1I1IlllIlI2.isUserConfig() ? "\u041a\u0443\u043f\u043b\u0435\u043d" : (l1I1IlllIlI2.isSharedConfig() ? "\u041e\u0442 \u0434\u0440\u0443\u0433\u0430" : ClaudeConfig.formatConfigSize(l1I1IlllIlI2.size()));
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, configBadgeText, this.x + this.width - 5.0f - 4.0f - Medium.DEFAULT_FONT.measureWidth(configBadgeText, 4.0f), f12 + 7.5f, 4.0f).baselinePosition(TextBaselinePosition.CENTER).color(!l1I1IlllIlI2.isPublicConfig() ? Accent.getAccentColorWithAlpha((int)(220.0f * f3)) : Accent.colorWithMutedAccent((int)(170.0f * f3))).render(drawContext);
                f12 += 17.5f;
            }
        }
        finally {
            drawContext.disableScissor();
        }
    }

    private float getDetailsPanelX() {
        return this.detailsX + 6.0f;
    }

    private float getDetailsPanelWidth() {
        return this.detailsWidth - 14.0f;
    }

    private float getDetailsContentTop() {
        return this.detailsY + 18.0f;
    }

    private float getActionListHeight(CloudConfigClient.CloudConfigEntry l1I1IlllIlI2) {
        return (float)this.getConfigDetailRows(l1I1IlllIlI2).length * 8.0f + 6.0f;
    }

    private List<ActionBounds> buildActionBounds(CloudConfigClient.CloudConfigEntry l1I1IlllIlI2) {
        int n;
        float f = this.getDetailsPanelX();
        float f2 = this.getDetailsPanelWidth();
        float f3 = this.getDetailsContentTop() + this.getActionListHeight(l1I1IlllIlI2) + 7.0f;
        ArrayList<ActionBounds> arrayList = new ArrayList<ActionBounds>();
        ArrayList<Integer> arrayList2 = new ArrayList<Integer>();
        boolean bl = false;
        int[] nArray = ClaudeConfig.getActionIds(l1I1IlllIlI2);
        int n2 = nArray.length;
        for (n = 0; n < n2; ++n) {
            int n3 = nArray[n];
            if (n3 == 2 || n3 == 7) {
                arrayList.add(new ActionBounds(n3, f, f3, f2, 15.0f));
                f3 += 20.0f;
                continue;
            }
            if (n3 == 4) {
                bl = true;
                continue;
            }
            arrayList2.add(n3);
        }
        float f4 = (f2 - 3.0f) * 0.5f;
        for (n2 = 0; n2 < arrayList2.size(); ++n2) {
            n = n2 == arrayList2.size() - 1 && n2 % 2 == 0 ? 1 : 0;
            float f5 = n != 0 || n2 % 2 == 0 ? f : f + f4 + 3.0f;
            arrayList.add(new ActionBounds((Integer)arrayList2.get(n2), f5, f3, n != 0 ? f2 : f4, 12.0f));
            if (n == 0 && n2 % 2 != 1) continue;
            f3 += 15.0f;
        }
        if (bl) {
            float f6 = arrayList2.isEmpty() ? 0.0f : 6.0f;
            arrayList.add(new ActionBounds(4, f, f3 += f6, f2, 12.0f));
        }
        return arrayList;
    }

    private boolean isDeleteConfirmationPending(CloudConfigClient.CloudConfigEntry l1I1IlllIlI2) {
        return l1I1IlllIlI2 != null && l1I1IlllIlI2.displayName().equals(this.confirmationDisplayName) && System.currentTimeMillis() - this.confirmationRequestedAtMillis < 3000L;
    }

    private void renderConfigDetails(DrawContext drawContext, float f, float f2, float f3) {
        float f4 = this.getDetailsPanelX();
        float f5 = this.getDetailsPanelWidth();
        CloudConfigClient.CloudConfigEntry l1I1IlllIlI2 = this.getSelectedConfig();
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, l1I1IlllIlI2 != null ? Medium.DEFAULT_FONT.truncateToWidth(l1I1IlllIlI2.name(), 6.5f, f5) : "\u041e\u0431\u043b\u0430\u0447\u043d\u044b\u0435 \u043a\u043e\u043d\u0444\u0438\u0433\u0438", f4, this.detailsY + 8.0f, 6.5f).baselinePosition(TextBaselinePosition.CENTER).color(Accent.getTextColorWithAlpha((int)(250.0f * f3))).render(drawContext);
        String[][] stringArray = this.getConfigDetailRows(l1I1IlllIlI2);
        float f6 = this.getDetailsContentTop();
        RoundedGradientRectBuilder.at(f4, f6, f5, this.getActionListHeight(l1I1IlllIlI2)).cornerRadius(Accent.getInstance().getCornerRadius() * 0.5f).color(Accent.getTextColorWithAlpha((int)(8.0f * f3))).render(drawContext);
        float f7 = f6 + 3.0f + 4.0f;
        for (String[] stringArray2 : stringArray) {
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, stringArray2[0], f4 + 4.0f, f7, 4.4f).baselinePosition(TextBaselinePosition.CENTER).color(Accent.colorWithMutedAccent((int)(180.0f * f3))).render(drawContext);
            if (stringArray2.length > 1) {
                String configValueText = Medium.DEFAULT_FONT.truncateToWidth(stringArray2[1], 4.4f, f5 * 0.6f);
                GuiTextBuilder.createAt(Medium.DEFAULT_FONT, configValueText, f4 + f5 - 4.0f - Medium.DEFAULT_FONT.measureWidth(configValueText, 4.4f), f7, 4.4f).baselinePosition(TextBaselinePosition.CENTER).color(Accent.getTextColorWithAlpha((int)(235.0f * f3))).render(drawContext);
            }
            f7 += 8.0f;
        }
        float f8 = f6 + this.getActionListHeight(l1I1IlllIlI2);
        for (ActionBounds iI1Il11l111II11IIl : this.buildActionBounds(l1I1IlllIlI2)) {
            this.renderActionButton(drawContext, f, f2, iI1Il11l111II11IIl, ClaudeConfig.getActionLabel(iI1Il11l111II11IIl.actionId(), l1I1IlllIlI2, this.isDeleteConfirmationPending(l1I1IlllIlI2)), f3);
            f8 = iI1Il11l111II11IIl.y() + iI1Il11l111II11IIl.height();
        }
        if (!this.statusMessage.isEmpty()) {
            float f9 = f8 + 7.0f;
            RoundedGradientRectBuilder.at(f4, f9, f5, 11.0f).cornerRadius(Accent.getInstance().getCornerRadius() * 0.5f).color(Accent.getAccentColorWithAlpha((int)(28.0f * f3))).render(drawContext);
            GuiTextBuilder.createAt(Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(this.statusMessage, 4.4f, f5 - 8.0f), f4 + 4.0f, f9 + 5.5f, 4.4f).baselinePosition(TextBaselinePosition.CENTER).color(Accent.getAccentColorWithAlpha((int)(235.0f * f3))).render(drawContext);
        }
    }

    private String[][] getConfigDetailRows(CloudConfigClient.CloudConfigEntry l1I1IlllIlI2) {
        String[][] stringArray;
        if (l1I1IlllIlI2 == null) {
            return new String[][]{{"\u0412\u0432\u0435\u0434\u0438 \u0438\u043c\u044f \u0441\u043b\u0435\u0432\u0430 \u0438 \u043d\u0430\u0436\u043c\u0438 +"}, {"\u0438\u043b\u0438 \u0432\u0441\u0442\u0430\u0432\u044c \u043a\u043e\u0434 \u0434\u0440\u0443\u0433\u0430 \u0432 \u043f\u043e\u043b\u0435"}, {"\u0438 \u043d\u0430\u0436\u043c\u0438 \u00ab\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043f\u043e \u043a\u043e\u0434\u0443\u00bb"}};
        }
        String formattedUpdatedAt = l1I1IlllIlI2.updatedAt() > 0L ? this.updatedAtFormatter.format(new Date(l1I1IlllIlI2.updatedAt())) : "\u2014";
        switch (l1I1IlllIlI2.endpoint()) {
            default: {
                throw new MatchException(null, null);
            }
            case CloudConfigClient.ConfigEndpoint.USER: {
                String[][] stringArrayArray = new String[3][];
                stringArrayArray[0] = new String[]{"\u0422\u0438\u043f", "\u041a\u0443\u043f\u043b\u0435\u043d"};
                stringArrayArray[1] = new String[]{"\u0410\u0432\u0442\u043e\u0440", l1I1IlllIlI2.author().isEmpty() ? "\u2014" : l1I1IlllIlI2.author()};
                stringArray = stringArrayArray;
                stringArrayArray[2] = new String[]{"\u041e\u0431\u043d\u043e\u0432\u043b\u0451\u043d", formattedUpdatedAt};
                break;
            }
            case CloudConfigClient.ConfigEndpoint.SHARED: {
                String[][] stringArrayArray = new String[3][];
                stringArrayArray[0] = new String[]{"\u0422\u0438\u043f", "\u041e\u0442 \u0434\u0440\u0443\u0433\u0430"};
                stringArrayArray[1] = new String[]{"\u0410\u0432\u0442\u043e\u0440", l1I1IlllIlI2.author().isEmpty() ? "\u2014" : l1I1IlllIlI2.author()};
                stringArray = stringArrayArray;
                stringArrayArray[2] = new String[]{"\u041e\u0431\u043d\u043e\u0432\u043b\u0451\u043d", formattedUpdatedAt};
                break;
            }
            case CloudConfigClient.ConfigEndpoint.PUBLIC: {
                String[][] stringArray2 = new String[2][];
                stringArray2[0] = new String[]{"\u0420\u0430\u0437\u043c\u0435\u0440", ClaudeConfig.formatConfigSize(l1I1IlllIlI2.size())};
                stringArray = stringArray2;
                stringArray2[1] = new String[]{"\u0418\u0437\u043c\u0435\u043d\u0451\u043d", formattedUpdatedAt};
            }
        }
        return stringArray;
    }

    private static int[] getActionIds(CloudConfigClient.CloudConfigEntry l1I1IlllIlI2) {
        int[] nArray;
        if (l1I1IlllIlI2 == null) {
            return new int[]{7};
        }
        switch (l1I1IlllIlI2.endpoint()) {
            default: {
                throw new MatchException(null, null);
            }
            case CloudConfigClient.ConfigEndpoint.USER: {
                int[] nArray2 = new int[1];
                nArray = nArray2;
                nArray2[0] = 2;
                break;
            }
            case CloudConfigClient.ConfigEndpoint.SHARED: {
                int[] nArray3 = new int[2];
                nArray3[0] = 2;
                nArray = nArray3;
                nArray3[1] = 4;
                break;
            }
            case CloudConfigClient.ConfigEndpoint.PUBLIC: {
                int[] nArray4 = new int[5];
                nArray4[0] = 2;
                nArray4[1] = 3;
                nArray4[2] = 5;
                nArray4[3] = 6;
                nArray = nArray4;
                nArray4[4] = 4;
            }
        }
        return nArray;
    }

    private static String getActionLabel(int n, CloudConfigClient.CloudConfigEntry l1I1IlllIlI2, boolean bl) {
        return switch (n) {
            case 2 -> "\u041f\u0440\u0438\u043c\u0435\u043d\u0438\u0442\u044c \u043a\u043e\u043d\u0444\u0438\u0433";
            case 3 -> "\u041f\u0435\u0440\u0435\u0437\u0430\u043f\u0438\u0441\u0430\u0442\u044c";
            case 4 -> {
                if (bl) {
                    yield "\u041d\u0430\u0436\u043c\u0438 \u0435\u0449\u0451 \u0440\u0430\u0437 \u0434\u043b\u044f \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0436\u0434\u0435\u043d\u0438\u044f";
                }
                if (l1I1IlllIlI2 != null && l1I1IlllIlI2.isSharedConfig()) {
                    yield "\u0423\u0431\u0440\u0430\u0442\u044c \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430";
                }
                yield "\u0423\u0434\u0430\u043b\u0438\u0442\u044c";
            }
            case 5 -> "\u0421\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u0442\u044c \u043a\u043e\u0434";
            case 6 -> "\u0417\u0430\u043a\u0440\u044b\u0442\u044c \u0434\u043e\u0441\u0442\u0443\u043f";
            default -> "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043f\u043e \u043a\u043e\u0434\u0443";
        };
    }

    public boolean mouseClicked(float f, float f2, int n) {
        if (n != 0) {
            return false;
        }
        this.nameSearchField.setFocused(ClaudeConfig.containsPoint(f, f2, this.x + 5.0f, this.getConfigListTop(), this.getCreateButtonX() - 3.0f - this.x - 5.0f, 13.0f));
        if (this.nameSearchField.isFocused()) {
            return true;
        }
        if (ClaudeConfig.containsPoint(f, f2, this.getCreateButtonX(), this.getConfigListTop(), 13.0f, 13.0f)) {
            this.saveCloudConfig(this.nameSearchField.getText());
            return true;
        }
        float f3 = this.y + 5.75f;
        if (ClaudeConfig.containsPoint(f, f2, this.getRefreshButtonX(), f3 - 4.25f, this.getRefreshButtonWidth(), 8.5f)) {
            this.refreshCloudConfigs();
            return true;
        }
        float f4 = this.getDetailsTop();
        if (f2 >= f4 && f2 <= this.y + this.contentHeight) {
            float f5 = f4 - this.scrollOffset;
            for (CloudConfigClient.CloudConfigEntry record : this.configs) {
                if (ClaudeConfig.containsPoint(f, f2, this.x + 5.0f, f5, this.width - 10.0f, 15.0f)) {
                    this.selectedConfigDisplayName = record.displayName();
                    if (record.isPublicConfig()) {
                        this.nameSearchField.setText(record.name());
                    }
                    return true;
                }
                f5 += 17.5f;
            }
        }
        CloudConfigClient.CloudConfigEntry l1I1IlllIlI2 = this.getSelectedConfig();
        for (ActionBounds iI1Il11l111II11IIl : this.buildActionBounds(l1I1IlllIlI2)) {
            if (!ClaudeConfig.containsPoint(f, f2, iI1Il11l111II11IIl.x(), iI1Il11l111II11IIl.y(), iI1Il11l111II11IIl.width(), iI1Il11l111II11IIl.height())) continue;
            switch (iI1Il11l111II11IIl.actionId()) {
                case 2: {
                    this.loadCloudConfig(l1I1IlllIlI2);
                    break;
                }
                case 3: {
                    this.saveCloudConfig(l1I1IlllIlI2.name());
                    break;
                }
                case 4: {
                    if (this.isDeleteConfirmationPending(l1I1IlllIlI2)) {
                        this.confirmationDisplayName = null;
                        this.deleteCloudConfig(l1I1IlllIlI2);
                        break;
                    }
                    this.confirmationDisplayName = l1I1IlllIlI2.displayName();
                    this.confirmationRequestedAtMillis = System.currentTimeMillis();
                    break;
                }
                case 5: {
                    this.createShareCode(l1I1IlllIlI2.name());
                    break;
                }
                case 6: {
                    this.revokeShareCode(l1I1IlllIlI2.name());
                    break;
                }
                default: {
                    this.redeemShareCode(this.nameSearchField.getText());
                }
            }
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(float f, float f2, double d) {
        if (ClaudeConfig.containsPoint(f, f2, this.x, this.getDetailsTop(), this.width, this.y + this.contentHeight - this.getDetailsTop())) {
            this.targetScrollOffset = Math.clamp(this.targetScrollOffset - (float)d * 20.0f, 0.0f, this.maxScrollOffset);
            return true;
        }
        return false;
    }

    public boolean keyPressed(KeyInput keyInput) {
        return this.nameSearchField.isFocused() && this.nameSearchField.keyPressed(keyInput);
    }

    public boolean charTyped(CharInput charInput) {
        return this.nameSearchField.isFocused() && this.nameSearchField.charTyped(charInput);
    }

    public void clearSearchFocus() {
        this.nameSearchField.setFocused(false);
    }

    private void refreshCloudConfigs() {
        CloudConfigClient updatedAt = CloudConfigClient.getInstance();
        updatedAt.fetchCurrentUser();
        CompletableFuture<List<CloudConfigClient.CloudConfigEntry>> userConfigs = updatedAt.listUserConfigs().exceptionally(throwable -> List.of());
        CompletableFuture<List<CloudConfigClient.CloudConfigEntry>> sharedConfigs = updatedAt.listSharedConfigs().exceptionally(throwable -> List.of());
        CompletableFuture<List<CloudConfigClient.CloudConfigEntry>> userAndShared = userConfigs.thenCombine(sharedConfigs, (user, shared) -> {
            ArrayList<CloudConfigClient.CloudConfigEntry> combined = new ArrayList<>(user);
            combined.addAll(shared);
            return combined;
        });
        CompletableFuture<List<CloudConfigClient.CloudConfigEntry>> allConfigs = updatedAt.listPublicConfigs().thenCombine(userAndShared, (publicConfigs, otherConfigs) -> {
            ArrayList<CloudConfigClient.CloudConfigEntry> combined = new ArrayList<>(otherConfigs);
            publicConfigs.sort((first, second) -> Long.compare(second.updatedAt(), first.updatedAt()));
            combined.addAll(publicConfigs);
            return combined;
        });
        this.runAsyncCloudOperation(allConfigs, configs -> {
            this.configs = configs;
            this.configHoverProgress = new float[configs.size()];
            if (this.selectedConfigDisplayName != null && this.getSelectedConfig() == null) {
                this.selectedConfigDisplayName = null;
            }
            this.statusMessage = "";
        });
    }

    private void saveCloudConfig(String configName) {
        String normalizedConfigName = configName.trim();
        if (!CloudConfigClient.CONFIG_NAME_PATTERN.matcher(normalizedConfigName).matches()) {
            this.statusMessage = "\u0418\u043c\u044f: 1-32 \u0441\u0438\u043c\u0432\u043e\u043b\u0430, \u0431\u0443\u043a\u0432\u044b, \u0446\u0438\u0444\u0440\u044b, \u043f\u0440\u043e\u0431\u0435\u043b, _ . -";
            return;
        }
        this.runAsyncCloudOperation(CloudConfigClient.getInstance().updateCloudConfig(normalizedConfigName, ClientConfigManager.getInstance().createConfigJson()), ignored -> {
            this.selectedConfigDisplayName = normalizedConfigName;
            NotificationScheduler.II1Il11l111II11IIl("Claude config", "\u041a\u043e\u043d\u0444\u0438\u0433 \u00ab" + normalizedConfigName + "\u00bb \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d \u0432 \u043e\u0431\u043b\u0430\u043a\u043e", 2500L);
            this.refreshCloudConfigs();
        });
    }

    private void loadCloudConfig(CloudConfigClient.CloudConfigEntry l1I1IlllIlI2) {
        String configName = l1I1IlllIlI2.name();
        CloudConfigClient updatedAt = CloudConfigClient.getInstance();
        CompletableFuture<JsonObject> completableFuture = switch (l1I1IlllIlI2.endpoint()) {
            default -> throw new MatchException(null, null);
            case CloudConfigClient.ConfigEndpoint.USER -> updatedAt.fetchUserConfig(l1I1IlllIlI2.id());
            case CloudConfigClient.ConfigEndpoint.SHARED -> updatedAt.fetchSharedConfig(l1I1IlllIlI2.id());
            case CloudConfigClient.ConfigEndpoint.PUBLIC -> updatedAt.fetchPublicConfig(configName);
        };
        this.runAsyncCloudOperation(completableFuture, jsonObject -> {
            ClientConfigManager.getInstance().applyConfig((JsonObject)jsonObject);
            ClientConfigManager.getInstance().saveConfig();
            NotificationScheduler.II1Il11l111II11IIl("Claude config", "\u041a\u043e\u043d\u0444\u0438\u0433 \u00ab" + configName + "\u00bb \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d", 2500L);
        });
    }

    private void deleteCloudConfig(CloudConfigClient.CloudConfigEntry l1I1IlllIlI2) {
        String configName = l1I1IlllIlI2.name();
        CloudConfigClient updatedAt = CloudConfigClient.getInstance();
        this.runAsyncCloudOperation(l1I1IlllIlI2.isSharedConfig() ? updatedAt.deleteSharedConfig(l1I1IlllIlI2.id()) : updatedAt.deleteCloudConfig(configName), ignored -> {
            this.selectedConfigDisplayName = null;
            NotificationScheduler.II1Il11l111II11IIl("Claude config", "\u041a\u043e\u043d\u0444\u0438\u0433 \u00ab" + configName + "\u00bb \u0443\u0434\u0430\u043b\u0451\u043d", 2500L);
            this.refreshCloudConfigs();
        });
    }

    private void createShareCode(String configName) {
        this.runAsyncCloudOperation(CloudConfigClient.getInstance().createShareCode(configName), shareCode -> {
            MinecraftClient.getInstance().keyboard.setClipboard((String)shareCode);
            this.statusMessage = "\u041a\u043e\u0434 " + shareCode + " \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d";
            NotificationScheduler.II1Il11l111II11IIl("Claude config", "\u041a\u043e\u0434 \u0434\u043e\u0441\u0442\u0443\u043f\u0430 \u043a \u00ab" + configName + "\u00bb \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d: " + shareCode, 4000L);
        });
    }

    private void revokeShareCode(String shareCode) {
        this.runAsyncCloudOperation(CloudConfigClient.getInstance().revokeShareCode(shareCode), ignored -> {
            this.statusMessage = "\u0414\u043e\u0441\u0442\u0443\u043f \u0437\u0430\u043a\u0440\u044b\u0442";
            NotificationScheduler.II1Il11l111II11IIl("Claude config", "\u0414\u043e\u0441\u0442\u0443\u043f \u043a \u00ab" + shareCode + "\u00bb \u0437\u0430\u043a\u0440\u044b\u0442", 2500L);
        });
    }

    private void redeemShareCode(String shareCode) {
        String normalizedShareCode = shareCode.trim();
        if (normalizedShareCode.isEmpty()) {
            this.statusMessage = "\u0412\u0441\u0442\u0430\u0432\u044c \u043a\u043e\u0434 \u0434\u0440\u0443\u0433\u0430 \u0432 \u043f\u043e\u043b\u0435 \u0441\u043b\u0435\u0432\u0430";
            return;
        }
        this.runAsyncCloudOperation(CloudConfigClient.getInstance().redeemShareCode(normalizedShareCode), ignored -> {
            this.nameSearchField.setText("");
            NotificationScheduler.II1Il11l111II11IIl("Claude config", "\u041a\u043e\u043d\u0444\u0438\u0433 \u0434\u0440\u0443\u0433\u0430 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d", 2500L);
            this.refreshCloudConfigs();
        });
    }

    private <T> void runAsyncCloudOperation(CompletableFuture<T> completableFuture, Consumer<T> consumer) {
        if (this.loading) {
            return;
        }
        this.loading = true;
        this.statusMessage = "";
        completableFuture.whenComplete((result, throwable) -> MinecraftClient.getInstance().execute(() -> {
            this.loading = false;
            if (throwable == null) {
                consumer.accept(result);
                return;
            }
            Throwable throwable2 = throwable instanceof CompletionException && throwable.getCause() != null ? throwable.getCause() : throwable;
            this.statusMessage = throwable2 instanceof CloudConfigClient.CloudApiException ? throwable2.getMessage() : "\u0421\u0435\u0440\u0432\u0435\u0440 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d";
        }));
    }

    private CloudConfigClient.CloudConfigEntry getSelectedConfig() {
        for (CloudConfigClient.CloudConfigEntry l1I1IlllIlI2 : this.configs) {
            if (!l1I1IlllIlI2.displayName().equals(this.selectedConfigDisplayName)) continue;
            return l1I1IlllIlI2;
        }
        return null;
    }

    private void renderActionButton(DrawContext drawContext, float f, float f2, ActionBounds iI1Il11l111II11IIl, String actionLabel, float f3) {
        int n;
        int n2;
        int n3;
        float f4 = iI1Il11l111II11IIl.x();
        float f5 = iI1Il11l111II11IIl.y();
        float f6 = iI1Il11l111II11IIl.width();
        float f7 = iI1Il11l111II11IIl.height();
        boolean bl = iI1Il11l111II11IIl.actionId() == 2 || iI1Il11l111II11IIl.actionId() == 7;
        boolean bl2 = iI1Il11l111II11IIl.actionId() == 4;
        boolean bl3 = bl2 && this.isDeleteConfirmationPending(this.getSelectedConfig());
        float f8 = this.getActionHoverProgress(iI1Il11l111II11IIl.actionId(), ClaudeConfig.containsPoint(f, f2, f4, f5, f6, f7));
        if (bl) {
            n3 = Accent.getAccentColorWithAlpha((int)(110.0f * f3)).toArgb();
            n2 = Accent.getAccentColorWithAlpha((int)(185.0f * f3)).toArgb();
            n = Accent.getTextColorWithAlpha((int)(255.0f * f3)).toArgb();
        } else if (bl2) {
            n3 = (int)((float)(bl3 ? 120 : 22) * f3) << 24 | 0xE0605F;
            n2 = (int)((float)(bl3 ? 170 : 75) * f3) << 24 | 0xE0605F;
            n = bl3 ? Accent.getTextColorWithAlpha((int)(255.0f * f3)).toArgb() : (int)(235.0f * f3) << 24 | 0xE0605F;
        } else {
            n3 = Accent.getTextColorWithAlpha((int)(12.0f * f3)).toArgb();
            n2 = Accent.getAccentColorWithAlpha((int)(60.0f * f3)).toArgb();
            n = Accent.getDarkerTextColor((int)(225.0f * f3)).toArgb();
        }
        RoundedGradientRectBuilder.at(f4, f5, f6, f7).cornerRadius(Accent.getInstance().getCornerRadius() * 0.5f).color(ColorUtils.interpolateColor(n3, n2, f8)).render(drawContext);
        float f9 = bl ? 5.0f : 4.5f;
        GuiTextBuilder.createAt(Medium.DEFAULT_FONT, Medium.DEFAULT_FONT.truncateToWidth(actionLabel, f9, f6 - 4.0f), f4 + f6 * 0.5f, f5 + f7 * 0.5f, f9).centered().color(ColorUtils.interpolateColor(n, Accent.getTextColorWithAlpha((int)(255.0f * f3)).toArgb(), f8)).render(drawContext);
    }

    private float getActionHoverProgress(int n, boolean bl) {
        float f = bl ? 1.0f : 0.0f;
        int n2 = n;
        this.actionHoverProgress[n2] = this.actionHoverProgress[n2] + (f - this.actionHoverProgress[n]) * (1.0f - (float)Math.exp(-6.0f * this.animationDeltaSeconds));
        return this.actionHoverProgress[n];
    }

    private static String formatConfigSize(long l) {
        return l < 1024L ? l + " B" : String.format("%.1f KB", (double)l / 1024.0);
    }

    private static boolean containsPoint(float f, float f2, float f3, float f4, float f5, float f6) {
        return f >= f3 && f <= f3 + f5 && f2 >= f4 && f2 <= f4 + f6;
    }

    private record ActionBounds(int actionId, float x, float y, float width, float height) {
    }
}
