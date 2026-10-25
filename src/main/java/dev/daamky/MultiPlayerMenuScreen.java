package dev.daamky;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.network.ServerInfo.Status;
import net.minecraft.client.option.ServerList;
import net.minecraft.network.NetworkingBackend;
import net.minecraft.util.Identifier;

public final class MultiPlayerMenuScreen extends MenuListScreen {
   private static final ExecutorService PING_EXECUTOR = Executors.newFixedThreadPool(3, runnable -> {
      Thread thread = new Thread(runnable, "daamky-server-ping");
      thread.setDaemon(true);
      return thread;
   });
   private final SearchTextField nameInput = new SearchTextField("Server name", 32, text -> {});
   private final SearchTextField addressInput = new SearchTextField("Server address", 64, text -> {});
   private final MultiplayerServerListPinger pinger = new MultiplayerServerListPinger();
   private final List<ServerInfo> visibleServers = new ArrayList<>();
   private ServerList serverList;

   public MultiPlayerMenuScreen(Screen previousScreen) {
      super("Multiplayer", previousScreen);
   }

   @Override
   protected void init() {
      super.init();
      this.reloadServers();
   }

   @Override
   public void removed() {
      try {
         this.pinger.cancel();
      } catch (Throwable cancelFailure) {
      }

      super.removed();
   }

   private void reloadServers() {
      MinecraftClient client = MinecraftClient.getInstance();
      this.serverList = new ServerList(client);
      this.serverList.loadFile();
      this.applyFilter();
      this.pingAll();
   }

   private void applyFilter() {
      String filter = this.searchInput.getText().trim().toLowerCase(Locale.ROOT);
      this.visibleServers.clear();

      for (int index = 0; index < this.serverList.size(); index++) {
         ServerInfo info = this.serverList.get(index);
         if (filter.isEmpty() || info.name.toLowerCase(Locale.ROOT).contains(filter) || info.address.toLowerCase(Locale.ROOT).contains(filter)) {
            this.visibleServers.add(info);
         }
      }

      this.clampSelection();
   }

   private void pingAll() {
      for (int index = 0; index < this.serverList.size(); index++) {
         this.pingServer(this.serverList.get(index));
      }
   }

   private void pingServer(ServerInfo info) {
      info.setStatus(Status.PINGING);
      PING_EXECUTOR.submit(() -> {
         try {
            this.pinger.add(info, () -> {}, () -> {}, NetworkingBackend.remote(MinecraftClient.getInstance().options.shouldUseNativeTransport()));
         } catch (Exception pingFailure) {
            info.setStatus(Status.UNREACHABLE);
         }
      });
   }

   @Override
   protected void onFrame() {
      try {
         this.pinger.tick();
      } catch (Throwable tickFailure) {
      }

      BotServerIcon.tick();
   }

   @Override
   protected int getEntryCount() {
      return this.visibleServers.size();
   }

   @Override
   protected String getCountText() {
      return this.serverList == null ? "0 Servers" : this.serverList.size() + " Servers";
   }

   @Override
   protected String getEmptyText() {
      return "No servers yet";
   }

   private static String describeStatus(ServerInfo info) {
      Status status = info.getStatus();
      if (status == Status.PINGING || status == Status.INITIAL) {
         return "Pinging...";
      } else if (status == Status.UNREACHABLE) {
         return "Offline";
      } else if (status == Status.INCOMPATIBLE) {
         return "Incompatible version";
      } else {
         String players = info.playerCountLabel == null ? "" : info.playerCountLabel.getString();
         return info.ping + " ms" + (players.isEmpty() ? "" : "   |   " + players);
      }
   }

   private static RgbaColor statusColor(ServerInfo info, int alpha) {
      Status status = info.getStatus();
      if (status == Status.PINGING || status == Status.INITIAL) {
         return RgbaColor.rgba(180, 180, 180, alpha);
      } else if (status == Status.UNREACHABLE || status == Status.INCOMPATIBLE) {
         return RgbaColor.rgba(235, 64, 64, alpha);
      } else if (info.ping > 250L) {
         return RgbaColor.rgba(250, 205, 60, alpha);
      } else {
         return RgbaColor.rgba(80, 220, 120, alpha);
      }
   }

   @Override
   protected void drawEntry(DrawContext drawContext, int index, float cardLeft, float cardTop, float cardWidth, float opacity) {
      ServerInfo info = this.visibleServers.get(index);
      Identifier icon = BotServerIcon.getServerIcon(info.address);
      if (icon != null) {
         TextureQuadBuilder.at(icon, cardLeft + 4.0F, cardTop + 4.0F, 18.0F, 18.0F).cornerRadius(3.0F).opacity(opacity).render(drawContext);
      } else {
         RoundedGradientRectBuilder.at(cardLeft + 4.0F, cardTop + 4.0F, 18.0F, 18.0F)
            .cornerRadius(3.0F)
            .color(RgbaColor.rgba(255, 255, 255, (int)(24.0F * opacity)))
            .render(drawContext);
      }

      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         Medium.DEFAULT_FONT.truncateToWidth(info.name, 5.2F, cardWidth - 44.0F),
         cardLeft + 27.0F,
         cardTop + 10.0F,
         5.2F,
         HorizontalTextAlignment.LEFT,
         Accent.getTextColorWithAlpha((int)(240.0F * opacity))
      );
      this.drawText(
         drawContext,
         Medium.DEFAULT_FONT,
         Medium.DEFAULT_FONT.truncateToWidth(describeStatus(info), 4.0F, cardWidth - 44.0F),
         cardLeft + 27.0F,
         cardTop + 18.0F,
         4.0F,
         HorizontalTextAlignment.LEFT,
         Accent.getDeepTextColor((int)(150.0F * opacity))
      );
      RoundedGradientRectBuilder.at(cardLeft + cardWidth - 9.0F, cardTop + ROW_HEIGHT * 0.5F - 1.5F, 3.0F, 3.0F)
         .cornerRadius(1.5F)
         .color(statusColor(info, (int)(255.0F * opacity)))
         .render(drawContext);
   }

   @Override
   protected void drawSidePanel(DrawContext drawContext, float left, float top, float blurRadius, float opacity, long timeMillis) {
      this.drawPanelHeader(drawContext, "Server panel", left, top, SIDE_PANEL_WIDTH, blurRadius, opacity);
      float boxTop = top + 18.0F;
      this.drawGlassBox(drawContext, left, boxTop, SIDE_PANEL_WIDTH, 120.0F, blurRadius, opacity);
      this.drawInput(drawContext, this.nameInput, left + 5.0F, boxTop + 5.0F, 82.0F, 14.0F, timeMillis, opacity);
      this.drawInput(drawContext, this.addressInput, left + 5.0F, boxTop + 22.0F, 82.0F, 14.0F, timeMillis, opacity);
      this.drawActionButton(drawContext, "join", "Join", null, left + 5.0F, boxTop + 40.0F, 39.0F, blurRadius, opacity);
      this.drawActionButton(drawContext, "direct", "Direct", null, left + 48.0F, boxTop + 40.0F, 39.0F, blurRadius, opacity);
      this.drawActionButton(drawContext, "add", "Add", null, left + 5.0F, boxTop + 57.0F, 39.0F, blurRadius, opacity);
      this.drawActionButton(drawContext, "save", "Save", null, left + 48.0F, boxTop + 57.0F, 39.0F, blurRadius, opacity);
      this.drawActionButton(drawContext, "delete", "Delete", null, left + 5.0F, boxTop + 74.0F, 39.0F, blurRadius, opacity);
      this.drawActionButton(drawContext, "refresh", "Refresh", null, left + 48.0F, boxTop + 74.0F, 39.0F, blurRadius, opacity);
      this.drawActionButton(drawContext, "vanilla", "Vanilla list", null, left + 5.0F, boxTop + 91.0F, 82.0F, blurRadius, opacity);
   }

   @Override
   protected void onFilterChanged(String filterLowercase) {
      this.applyFilter();
   }

   @Override
   protected void onSelect(int index) {
      if (index >= 0 && index < this.visibleServers.size()) {
         ServerInfo info = this.visibleServers.get(index);
         this.nameInput.setText(info.name);
         this.addressInput.setText(info.address);
      }
   }

   @Override
   protected void onActivate(int index) {
      if (index >= 0 && index < this.visibleServers.size()) {
         this.joinServer(this.visibleServers.get(index));
      }
   }

   private void joinServer(ServerInfo info) {
      MinecraftClient client = MinecraftClient.getInstance();
      ConnectScreen.connect(this, client, ServerAddress.parse(info.address.trim()), info, false, null);
   }

   private ServerInfo getSelectedServer() {
      return this.selectedIndex >= 0 && this.selectedIndex < this.visibleServers.size() ? this.visibleServers.get(this.selectedIndex) : null;
   }

   @Override
   protected void onAction(String actionId) {
      String address = this.addressInput.getText().trim();
      String name = this.nameInput.getText().trim();
      ServerInfo selected = this.getSelectedServer();
      switch (actionId) {
         case "join":
            if (selected != null) {
               this.joinServer(selected);
            } else if (!address.isEmpty()) {
               this.joinServer(new ServerInfo(name.isEmpty() ? address : name, address, ServerType.OTHER));
            } else {
               this.showStatus("Select a server first");
            }
            break;
         case "direct":
            if (address.isEmpty()) {
               this.showStatus("Enter an address");
            } else {
               this.joinServer(new ServerInfo(name.isEmpty() ? address : name, address, ServerType.OTHER));
            }
            break;
         case "add":
            if (address.isEmpty()) {
               this.showStatus("Enter an address");
            } else {
               ServerInfo added = new ServerInfo(name.isEmpty() ? address : name, address, ServerType.OTHER);
               this.serverList.add(added, false);
               this.serverList.saveFile();
               this.applyFilter();
               this.pingServer(added);
               this.showStatus("Added " + added.name);
            }
            break;
         case "save":
            if (selected == null) {
               this.showStatus("Select a server first");
            } else if (address.isEmpty()) {
               this.showStatus("Enter an address");
            } else {
               selected.name = name.isEmpty() ? address : name;
               selected.address = address;
               this.serverList.saveFile();
               this.applyFilter();
               this.pingServer(selected);
               this.showStatus("Saved");
            }
            break;
         case "delete":
            if (selected == null) {
               this.showStatus("Select a server first");
            } else {
               this.serverList.remove(selected);
               this.serverList.saveFile();
               this.selectedIndex = -1;
               this.applyFilter();
               this.showStatus("Deleted " + selected.name);
            }
            break;
         case "refresh":
            this.reloadServers();
            this.showStatus("Refreshing...");
            break;
         case "vanilla":
            this.transitionTo(new MultiplayerScreen(this));
            break;
         default:
            break;
      }
   }
}
