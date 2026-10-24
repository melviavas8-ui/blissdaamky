package dev.daamky;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;

public final class Friend {
   private static final List<String> ROOT_COMMANDS = List.of("help", "friend", "bind", "toggle", "config", "rct", "tp", "gps", "macros", "neuro");

   private Friend() {
   }

   public static CompletableFuture<Suggestions> suggestCommands(String input, int cursorPosition) {
      String inputPrefix = input.substring(0, Math.min(cursorPosition, input.length()));
      int lastSpaceIndex = inputPrefix.lastIndexOf(32) + 1;
      SuggestionsBuilder suggestionsBuilder = new SuggestionsBuilder(inputPrefix, lastSpaceIndex);
      String currentToken = inputPrefix.substring(lastSpaceIndex).toLowerCase(Locale.ROOT);
      String[] textArray = inputPrefix.substring(1).split(" ", -1);
      int argumentIndex = textArray.length - 1;

      for (String suggestion : suggestCommandArguments(textArray, argumentIndex)) {
         if (suggestion.toLowerCase(Locale.ROOT).startsWith(argumentIndex == 0 ? currentToken.substring(Math.min(currentToken.length(), ".".length())) : currentToken)) {
            suggestionsBuilder.suggest((String)(argumentIndex == 0 ? "." + suggestion : suggestion));
         }
      }

      return suggestionsBuilder.buildFuture();
   }

   private static List<String> suggestCommandArguments(String[] textArray, int argumentIndex) {
      if (argumentIndex == 0) {
         return ROOT_COMMANDS;
      }

      String commandName = textArray[0].toLowerCase(Locale.ROOT);
      String subcommandArgument = textArray.length > 1 ? textArray[1].toLowerCase(Locale.ROOT) : "";

      return switch (commandName) {
         case "friend", "f" -> argumentIndex == 1
            ? List.of("add", "remove", "list", "clear")
            : (
               argumentIndex == 2 && subcommandArgument.equals("add")
                  ? getOnlinePlayerNames()
                  : (argumentIndex != 2 || !subcommandArgument.equals("remove") && !subcommandArgument.equals("del") ? List.of() : new ArrayList(FriendManager.getInstance().getFriendNames()))
            );
         case "tp", "teleport" -> {
            if (argumentIndex == 1) {
               ArrayList arrayList = new ArrayList<>(getOnlinePlayerNames());
               arrayList.add("stop");
               yield arrayList;
            } else {
               yield List.of();
            }
         }
         case "gps", "way" -> argumentIndex == 1
            ? List.of("list", "clear", "remove", "here")
            : (
               argumentIndex != 2 || !subcommandArgument.equals("remove") && !subcommandArgument.equals("del")
                  ? List.of()
                  : WaypointManager.getInstance().waypoints().stream().map(WaypointManager.Waypoint::name).toList()
            );
         case "macros", "macro" -> argumentIndex == 1
            ? List.of("add", "remove", "run", "list", "clear")
            : (
               argumentIndex != 2 || !subcommandArgument.equals("remove") && !subcommandArgument.equals("del") && !subcommandArgument.equals("run")
                  ? List.of()
                  : Key.getInstance().getBindings().stream().map(Key.KeyBindingEntry::name).toList()
            );
         case "neuro" -> argumentIndex == 1 ? List.of("record", "stop", "train", "tune", "play", "list") : List.of();
         case "toggle", "t" -> argumentIndex == 1 ? getModuleNames() : List.of();
         case "bind", "b" -> {
            if (argumentIndex == 1) {
               ArrayList arrayList2 = new ArrayList<>(List.of("list", "clear"));
               arrayList2.addAll(getModuleNames());
               yield arrayList2;
            } else {
               yield argumentIndex == 2 && subcommandArgument.equals("clear") ? getModuleNames() : List.of();
            }
         }
         case "config", "cfg" -> argumentIndex == 1 ? List.of("save", "load") : List.of();
         default -> List.of();
      };
   }

   private static List<String> getOnlinePlayerNames() {
      MinecraftClient client = MinecraftClient.getInstance();
       LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
      if (client.getNetworkHandler() != null) {
          for (net.minecraft.client.network.PlayerListEntry playerEntry : client.getNetworkHandler().getPlayerList()) {
             String playerName = playerEntry.getProfile().name();
            if (playerName != null && (client.player == null || !playerName.equals(client.player.getGameProfile().name()))) {
               linkedHashSet.add(playerName);
            }
         }
      }

      ArrayList arrayList = new ArrayList(linkedHashSet);
      arrayList.sort(String.CASE_INSENSITIVE_ORDER);
      return arrayList;
   }

   private static List<String> getModuleNames() {
      ArrayList arrayList = new ArrayList();

      for (Object module : ModuleRegistry.getInstance().getModules()) {
         arrayList.add(((ModuleStructure)module).getName());
      }

      return arrayList;
   }
}

