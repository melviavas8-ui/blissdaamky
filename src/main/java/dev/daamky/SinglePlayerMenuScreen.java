package dev.daamky;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;

public final class SinglePlayerMenuScreen extends MenuListScreen {
   private static final SimpleDateFormat LAST_PLAYED_FORMAT = new SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.ROOT);
   private final List<LevelSummary> allWorlds = new ArrayList<>();
   private final List<LevelSummary> visibleWorlds = new ArrayList<>();
   private volatile List<LevelSummary> pendingWorlds;
   private boolean loading;

   public SinglePlayerMenuScreen(Screen previousScreen) {
      super("Singleplayer", previousScreen);
   }

   @Override
   protected void init() {
      super.init();
      this.loadWorlds();
   }

   private void loadWorlds() {
      MinecraftClient client = MinecraftClient.getInstance();
      this.loading = true;

      try {
         LevelStorage storage = client.getLevelStorage();
         storage.loadSummaries(storage.getLevelList())
            .thenAccept(summaries -> this.pendingWorlds = summaries)
            .exceptionally(failure -> {
               this.pendingWorlds = new ArrayList<>();
               return null;
            });
      } catch (Exception failure) {
         this.pendingWorlds = new ArrayList<>();
      }
   }

   private void applyFilter() {
      String filter = this.searchInput.getText().trim().toLowerCase(Locale.ROOT);
      this.visibleWorlds.clear();

      for (LevelSummary world : this.allWorlds) {
         if (filter.isEmpty()
            || world.getDisplayName().toLowerCase(Locale.ROOT).contains(filter)
            || world.getName().toLowerCase(Locale.ROOT).contains(filter)) {
            this.visibleWorlds.add(world);
         }
      }

      this.clampSelection();
   }

   @Override
   protected void onFrame() {
      List<LevelSummary> loaded = this.pendingWorlds;
      if (loaded != null) {
         this.pendingWorlds = null;
         this.allWorlds.clear();
         this.allWorlds.addAll(loaded);
         this.allWorlds.sort(Comparator.comparingLong(LevelSummary::getLastPlayed).reversed());
         this.loading = false;
         this.applyFilter();
         if (this.selectedIndex < 0 && !this.visibleWorlds.isEmpty()) {
            this.selectedIndex = 0;
         }
      }
   }

   @Override
   protected int getEntryCount() {
      return this.visibleWorlds.size();
   }

   @Override
   protected String getCountText() {
      return this.allWorlds.size() + " Worlds";
   }

   @Override
   protected String getEmptyText() {
      return this.loading ? "Loading worlds..." : "No worlds found";
   }

   private static String describeMode(LevelSummary world) {
      if (world.isHardcore()) {
         return "Hardcore";
      }

      String mode = world.getGameMode().name().toLowerCase(Locale.ROOT);
      return Character.toUpperCase(mode.charAt(0)) + mode.substring(1);
   }

   @Override
   protected void drawEntry(DrawContext drawContext, int index, float cardLeft, float cardTop, float cardWidth, float opacity) {
      LevelSummary world = this.visibleWorlds.get(index);
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         Medium.DEFAULT_FONT.truncateToWidth(world.getDisplayName(), 5.2F, cardWidth - 16.0F),
         cardLeft + 8.0F,
         cardTop + 10.0F,
         5.2F,
         HorizontalTextAlignment.LEFT,
         Accent.getTextColorWithAlpha((int)(240.0F * opacity))
      );
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         describeMode(world) + "   |   " + LAST_PLAYED_FORMAT.format(new Date(world.getLastPlayed())),
         cardLeft + 8.0F,
         cardTop + 18.0F,
         4.0F,
         HorizontalTextAlignment.LEFT,
         Accent.getDeepTextColor((int)(150.0F * opacity))
      );
   }

   @Override
   protected void drawSidePanel(DrawContext drawContext, float left, float top, float blurRadius, float opacity, long timeMillis) {
      this.drawPanelHeader(drawContext, "World panel", left, top, SIDE_PANEL_WIDTH, blurRadius, opacity);
      float buttonsTop = top + 18.0F;
      this.drawGlassBox(drawContext, left, buttonsTop, SIDE_PANEL_WIDTH, 56.0F, blurRadius, opacity);
      this.drawActionButton(drawContext, "play", "Play", "Enter", left + 5.0F, buttonsTop + 5.0F, 82.0F, blurRadius, opacity);
      this.drawActionButton(drawContext, "refresh", "Refresh", "F5", left + 5.0F, buttonsTop + 22.0F, 82.0F, blurRadius, opacity);
      this.drawActionButton(drawContext, "vanilla", "Create / Edit", null, left + 5.0F, buttonsTop + 39.0F, 82.0F, blurRadius, opacity);
      float infoTop = top + 78.0F;
      this.drawGlassBox(drawContext, left, infoTop, SIDE_PANEL_WIDTH, 60.0F, blurRadius, opacity);
      if (this.selectedIndex < 0 || this.selectedIndex >= this.visibleWorlds.size()) {
         this.drawText(
            drawContext,
            Medium.DEFAULT_FONT,
            "Select a world",
            left + SIDE_PANEL_WIDTH * 0.5F,
            infoTop + 30.0F,
            4.6F,
            HorizontalTextAlignment.CENTER,
            Accent.getDeepTextColor((int)(120.0F * opacity))
         );
         return;
      }

      LevelSummary world = this.visibleWorlds.get(this.selectedIndex);
      float textWidth = SIDE_PANEL_WIDTH - 12.0F;
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         Medium.DEFAULT_FONT.truncateToWidth(world.getDisplayName(), 5.2F, textWidth),
         left + 6.0F,
         infoTop + 10.0F,
         5.2F,
         HorizontalTextAlignment.LEFT,
         Accent.getTextColorWithAlpha((int)(240.0F * opacity))
      );
      String[] lines = new String[]{
         "Mode: " + describeMode(world),
         "Played: " + LAST_PLAYED_FORMAT.format(new Date(world.getLastPlayed())),
         "Folder: " + world.getName()
      };

      for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
         this.drawText(
            drawContext,
            Medium.DEFAULT_FONT,
            Medium.DEFAULT_FONT.truncateToWidth(lines[lineIndex], 4.2F, textWidth),
            left + 6.0F,
            infoTop + 22.0F + lineIndex * 9.0F,
            4.2F,
            HorizontalTextAlignment.LEFT,
            Accent.getDeepTextColor((int)(170.0F * opacity))
         );
      }
   }

   @Override
   protected void onFilterChanged(String filterLowercase) {
      this.applyFilter();
   }

   @Override
   protected void onSelect(int index) {
   }

   @Override
   protected void onActivate(int index) {
      this.playWorld(index);
   }

   private void playWorld(int index) {
      if (index < 0 || index >= this.visibleWorlds.size()) {
         this.showStatus("Select a world first");
         return;
      }

      MinecraftClient client = MinecraftClient.getInstance();
      LevelSummary world = this.visibleWorlds.get(index);
      client.createIntegratedServerLoader().start(world.getName(), () -> client.setScreen(this));
   }

   @Override
   protected void onAction(String actionId) {
      switch (actionId) {
         case "play":
            this.playWorld(this.selectedIndex);
            break;
         case "refresh":
            this.loadWorlds();
            this.showStatus("Refreshing...");
            break;
         case "vanilla":
            this.transitionTo(new SelectWorldScreen(this));
            break;
         default:
            break;
      }
   }
}
