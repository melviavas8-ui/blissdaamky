package dev.daamky;

import java.util.Arrays;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Type;

public final class ClientCommandDispatcher {
   public static final String COMMAND_PREFIX = ".";
   private static final ClientCommandDispatcher INSTANCE = new ClientCommandDispatcher();

   private ClientCommandDispatcher() {
   }

   public static ClientCommandDispatcher getInstance() {
      return INSTANCE;
   }

   public boolean dispatch(String commandText) {
      if (commandText != null && commandText.startsWith(".") && commandText.length() != 1) {
         String[] commandTokens = commandText.substring(".".length()).trim().split("\\s+");
         String commandName = commandTokens[0].toLowerCase(Locale.ROOT);
         String[] arguments = Arrays.copyOfRange(commandTokens, 1, commandTokens.length);

         try {
            switch (commandName) {
               case "help":
                  printHelp();
                  break;
               case "friend":
               case "f":
                  friendCommand(arguments);
                  break;
               case "bind":
               case "b":
                  bindCommand(arguments);
                  break;
               case "toggle":
               case "t":
                  toggleCommand(arguments);
                  break;
               case "config":
               case "cfg":
                  configCommand(arguments);
                  break;
               case "rct":
               case "reconnect":
                  Spooky.getInstance().startAnarchyReconnect(arguments);
                  break;
               case "tp":
               case "teleport":
                  teleportCommand(arguments);
                  break;
               case "gps":
               case "way":
                  waypointCommand(arguments);
                  break;
               case "macros":
               case "macro":
                  macroCommand(arguments);
                  break;
               case "neuro":
                  NeuroRotationController.getInstance().handleNeuroCommand(arguments);
                  break;
               case "bot":
               case "bots":
                  botCommand(arguments);
                  break;
               default:
                  ClientChatLogger.l1I1IlllIlI("Unknown command. Type .help");
            }
         } catch (RuntimeException commandException) {
            ClientChatLogger.l1I1IlllIlI("Error: " + commandException.getMessage());
         }

         return true;
      } else {
         return false;
      }
   }

   private static void botCommand(String[] arguments) {
      String subcommand = arguments.length == 0 ? "gui" : arguments[0].toLowerCase(Locale.ROOT);
      switch (subcommand) {
         case "gui": {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> client.setScreen(new BotManagementScreen()));
            break;
         }
         case "pay":
            if (arguments.length < 2) {
               ClientChatLogger.II1Il11l111II11IIl(".bot pay <nick> [keep]");
               return;
            }

            SavedBots.setPayoutRecipient(arguments[1]);
            if (arguments.length > 2) {
               SavedBots.setMinimumPayoutAmount(Long.parseLong(arguments[2]));
            }

            ClientChatLogger.II1Il11l111II11IIl("Получатель монет: " + arguments[1] + ", оставлять: " + SavedBots.getMinimumPayoutAmount());
            break;
         case "autopay": {
            boolean autoPayEnabled = !SavedBots.isBalancePayoutEnabled();
            SavedBots.setBalancePayoutEnabled(autoPayEnabled);
            ClientChatLogger.II1Il11l111II11IIl("AutoPay ботов: " + (autoPayEnabled ? "включен" : "выключен"));
            break;
         }
         default:
            ClientChatLogger.II1Il11l111II11IIl(".bot gui | pay <nick> [keep] | autopay");
      }
   }

   private static void printHelp() {
      ClientChatLogger.II1Il11l111II11IIl(".friend add/remove <name> | list | clear");
      ClientChatLogger.II1Il11l111II11IIl(".bind <module> <key> | clear <module> | list");
      ClientChatLogger.II1Il11l111II11IIl(".toggle <module>");
      ClientChatLogger.II1Il11l111II11IIl(".config save | load");
      ClientChatLogger.II1Il11l111II11IIl(".rct [number] — reconnect to anarchy (ReallyWorld / FunTime)");
      ClientChatLogger.II1Il11l111II11IIl(".gps <имя> <x> <y> <z> | <имя> <x> <z> | here <имя> | remove <имя> | list | clear");
      ClientChatLogger.II1Il11l111II11IIl(".macros add <имя> <клавиша> <текст> | remove <имя> | run <имя> | list | clear");
      ClientChatLogger.II1Il11l111II11IIl(".neuro record <имя> | stop | train <имя> [эпохи] | tune <модель> [запись] [эпохи] | play <имя> | list");
      ClientChatLogger.II1Il11l111II11IIl(".tp <nick> | <x> <z> | <x> <y> <z> | stop — teleport through the void (needs /fly)");
   }

   private static void teleportCommand(String[] arguments) {
      requireArgumentCount(arguments, 1);
      TeleportController teleportController = TeleportController.getInstance();
      if (arguments[0].equalsIgnoreCase("stop")) {
         teleportController.cancelTeleport();
      } else {
         MinecraftClient client = MinecraftClient.getInstance();
         if (client.player != null && client.world != null) {
            if (arguments.length >= 3) {
               teleportController.startTeleport(
                  client.player, client.world, Double.parseDouble(arguments[0]), Double.parseDouble(arguments[1]), Double.parseDouble(arguments[2])
               );
            } else if (arguments.length == 2) {
               teleportController.startTeleport(
                  client.player, client.world, Double.parseDouble(arguments[0]), client.player.getY(), Double.parseDouble(arguments[1])
               );
            } else {
                for (PlayerEntity targetPlayer : client.world.getPlayers()) {
                  if (targetPlayer != client.player && targetPlayer.getGameProfile().name().equalsIgnoreCase(arguments[0])) {
                     teleportController.startTeleport(client.player, client.world, targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
                     return;
                  }
               }

               ClientChatLogger.l1I1IlllIlI("Игрок " + arguments[0] + " не найден");
            }
         }
      }
   }

   private static void waypointCommand(String[] arguments) {
      WaypointManager waypointManager = WaypointManager.getInstance();
      if (arguments.length != 0 && !arguments[0].equalsIgnoreCase("list")) {
         switch (arguments[0].toLowerCase(Locale.ROOT)) {
            case "clear":
               waypointManager.clearWaypoints();
               ClientChatLogger.II1Il11l111II11IIl("Метки очищены");
               break;
            case "remove":
            case "del":
               requireArgumentCount(arguments, 2);
               if (waypointManager.removeWaypoint(arguments[1])) {
                  ClientChatLogger.II1Il11l111II11IIl("Метка " + arguments[1] + " удалена");
               } else {
                  ClientChatLogger.l1I1IlllIlI("Метка " + arguments[1] + " не найдена");
               }
               break;
            case "here":
               ClientPlayerEntity player = MinecraftClient.getInstance().player;
               if (player == null) {
                  return;
               }

               String hereWaypointName = arguments.length > 1 ? arguments[1] : "GPS";
                waypointManager.addWaypoint(
                  new WaypointManager.Waypoint(
                     hereWaypointName, player.getBlockX(), player.getBlockY(), player.getBlockZ()
                  )
               );
               ClientChatLogger.II1Il11l111II11IIl("Метка " + hereWaypointName + " поставлена");
               break;
            default:
                boolean hasExplicitName = !isInteger(arguments[0]);
                String waypointName = hasExplicitName ? arguments[0] : "GPS";
                int coordinateOffset = hasExplicitName ? 1 : 0;
               int coordinateCount = arguments.length - coordinateOffset;
               if (coordinateCount == 3) {
                  waypointManager.addWaypoint(
                     new WaypointManager.Waypoint(
                        waypointName, Integer.parseInt(arguments[coordinateOffset]), Integer.parseInt(arguments[coordinateOffset + 1]), Integer.parseInt(arguments[coordinateOffset + 2])
                     )
                  );
               } else {
                  if (coordinateCount != 2) {
                     ClientChatLogger.l1I1IlllIlI("Usage: .gps <имя> <x> <y> <z>");
                     return;
                  }

                  waypointManager.addWaypoint(
                     new WaypointManager.Waypoint(waypointName, Integer.parseInt(arguments[coordinateOffset]), 64, Integer.parseInt(arguments[coordinateOffset + 1]))
                  );
               }

               ClientChatLogger.II1Il11l111II11IIl("Метка " + waypointName + " поставлена");
         }
      } else if (waypointManager.waypoints().isEmpty()) {
         ClientChatLogger.II1Il11l111II11IIl("Меток нет");
      } else {
         for (WaypointManager.Waypoint waypoint : waypointManager.waypoints()) {
            ClientChatLogger.II1Il11l111II11IIl(
               waypoint.name()
                  + " → "
                  + waypoint.x()
                  + ", "
                  + waypoint.y()
                  + ", "
                  + waypoint.z()
            );
         }
      }
   }

   private static void macroCommand(String[] arguments) {
      Key keyManager = Key.getInstance();
      if (arguments.length != 0 && !arguments[0].equalsIgnoreCase("list")) {
         switch (arguments[0].toLowerCase(Locale.ROOT)) {
            case "add":
               requireArgumentCount(arguments, 4);
               int keyCode = parseKeyCode(arguments[2]);
               if (keyCode == 0) {
                  throw new IllegalArgumentException("Unknown key: " + arguments[2]);
               }

               String macroMessage = String.join(" ", Arrays.copyOfRange(arguments, 3, arguments.length));
               keyManager.registerBinding(new Key.KeyBindingEntry(arguments[1], keyCode, macroMessage));
               ClientChatLogger.II1Il11l111II11IIl("Макрос " + arguments[1] + " [" + KeybindManager.getDisplayName(keyCode) + "] → " + macroMessage);
               break;
            case "remove":
            case "del":
               requireArgumentCount(arguments, 2);
               if (!keyManager.removeBinding(arguments[1])) {
                  ClientChatLogger.l1I1IlllIlI("Макрос " + arguments[1] + " не найден");
                  return;
               }

               ClientChatLogger.II1Il11l111II11IIl("Макрос " + arguments[1] + " удалён");
               break;
            case "run":
               requireArgumentCount(arguments, 2);
               Key.KeyBindingEntry bindingEntry = keyManager.getBinding(arguments[1]);
               if (bindingEntry == null) {
                  ClientChatLogger.l1I1IlllIlI("Макрос " + arguments[1] + " не найден");
               } else {
                  Key.sendChatOrCommand(bindingEntry.message());
               }

               return;
            case "clear":
               keyManager.clearBindings();
               ClientChatLogger.II1Il11l111II11IIl("Макросы очищены");
               break;
            default:
               ClientChatLogger.l1I1IlllIlI("Usage: .macros add <имя> <клавиша> <текст> | remove <имя> | run <имя> | list | clear");
               return;
         }

         ClientConfigManager.getInstance().saveConfig();
      } else if (keyManager.getBindings().isEmpty()) {
         ClientChatLogger.II1Il11l111II11IIl("Макросов нет");
      } else {
         for (Object bindingEntry : keyManager.getBindings()) {
            ClientChatLogger.II1Il11l111II11IIl(
               ((Key.KeyBindingEntry)bindingEntry).name()
                  + " ["
                  + KeybindManager.getDisplayName(((Key.KeyBindingEntry)bindingEntry).keyCode())
                  + "] → "
                  + ((Key.KeyBindingEntry)bindingEntry).message()
            );
         }
      }
   }

   private static boolean isInteger(String value) {
      return value.matches("-?\\d+");
   }

   private static void friendCommand(String[] arguments) {
      FriendManager friendManager = FriendManager.getInstance();
      if (arguments.length != 0 && !arguments[0].equalsIgnoreCase("list")) {
         switch (arguments[0].toLowerCase(Locale.ROOT)) {
            case "add":
               requireArgumentCount(arguments, 2);
               friendManager.addFriend(arguments[1]);
               ClientChatLogger.II1Il11l111II11IIl(arguments[1] + " added to friends");
               break;
            case "remove":
            case "del":
               requireArgumentCount(arguments, 2);
               friendManager.removeFriend(arguments[1]);
               ClientChatLogger.II1Il11l111II11IIl(arguments[1] + " removed from friends");
               break;
            case "clear":
               friendManager.clearFriends();
               ClientChatLogger.II1Il11l111II11IIl("Friend list cleared");
               break;
            default:
               ClientChatLogger.l1I1IlllIlI("Usage: .friend add/remove <name> | list | clear");
         }

         ClientConfigManager.getInstance().saveConfig();
      } else {
         ClientChatLogger.II1Il11l111II11IIl(friendManager.getFriendNames().isEmpty() ? "Friend list is empty" : "Friends: " + String.join(", ", friendManager.getFriendNames()));
      }
   }

   private static void bindCommand(String[] arguments) {
      if (arguments.length != 0 && !arguments[0].equalsIgnoreCase("list")) {
         if (arguments[0].equalsIgnoreCase("clear")) {
            requireArgumentCount(arguments, 2);
            getModuleOrThrow(arguments[1]).setKeybind(0);
            ClientChatLogger.II1Il11l111II11IIl("Bind " + arguments[1] + " reset");
            ClientConfigManager.getInstance().saveConfig();
         } else {
            requireArgumentCount(arguments, 2);
            ModuleStructure module = getModuleOrThrow(arguments[0]);
            int keyCode = parseKeyCode(arguments[1]);
            module.setKeybind(keyCode);
            ClientChatLogger.II1Il11l111II11IIl(module.getName() + " → " + KeybindManager.getDisplayName(keyCode));
            ClientConfigManager.getInstance().saveConfig();
         }
      } else {
         for (Object module : ModuleRegistry.getInstance().getModules()) {
            if (((ModuleStructure)module).getKeybind() != 0) {
               ClientChatLogger.II1Il11l111II11IIl(
                  ((ModuleStructure)module).getName() + " → " + KeybindManager.getDisplayName(((ModuleStructure)module).getKeybind())
               );
            }
         }
      }
   }

   private static void toggleCommand(String[] arguments) {
      requireArgumentCount(arguments, 1);
      getModuleOrThrow(arguments[0]).toggle();
   }

   private static void configCommand(String[] arguments) {
      requireArgumentCount(arguments, 1);
      if (arguments[0].equalsIgnoreCase("save")) {
         ClientConfigManager.getInstance().saveConfig();
         ClientChatLogger.II1Il11l111II11IIl("Config saved");
      } else if (arguments[0].equalsIgnoreCase("load")) {
         ClientConfigManager.getInstance().loadConfig();
         ClientChatLogger.II1Il11l111II11IIl("Config loaded");
      } else {
         ClientChatLogger.l1I1IlllIlI("Usage: .config save | load");
      }
   }

   private static ModuleStructure getModuleOrThrow(String moduleName) {
      ModuleStructure module = ModuleRegistry.getInstance().getModuleByName(moduleName);
      if (module == null) {
         throw new IllegalArgumentException("Module " + moduleName + " not found");
      } else {
         return module;
      }
   }

   private static int parseKeyCode(String keyName) {
      String normalizedKeyName = keyName.toLowerCase(Locale.ROOT).replace('_', '.');
      if (normalizedKeyName.equals("none") || normalizedKeyName.equals("clear")) {
         return 0;
      }

      if (normalizedKeyName.matches("(mouse|m)[0-9]{1,2}")) {
         int mouseButtonNumber = Integer.parseInt(normalizedKeyName.replaceAll("[^0-9]", ""));
         if (mouseButtonNumber >= 1 && mouseButtonNumber <= 8) {
            return KeybindManager.encodeMouseButton(mouseButtonNumber - 1);
         } else {
            throw new IllegalArgumentException("Unknown mouse button: " + keyName);
         }
      } else {
         normalizedKeyName = switch (normalizedKeyName) {
            case "lshift" -> "left.shift";
            case "rshift" -> "right.shift";
            case "lctrl", "lcontrol" -> "left.control";
            case "rctrl", "rcontrol" -> "right.control";
            case "lalt" -> "left.alt";
            case "ralt" -> "right.alt";
            case "esc" -> "escape";
            case "del" -> "delete";
            case "ins" -> "insert";
            case "pgup" -> "page.up";
            case "pgdn", "pgdown" -> "page.down";
            case "capslock", "caps" -> "caps.lock";
            default -> normalizedKeyName;
         };

         net.minecraft.client.util.InputUtil.Key inputKey;
         try {
            inputKey = InputUtil.fromTranslationKey(normalizedKeyName.startsWith("key.") ? normalizedKeyName : "key.keyboard." + normalizedKeyName);
         } catch (RuntimeException invalidKeyException) {
            inputKey = null;
         }

         if (inputKey != null && inputKey.getCategory() == Type.KEYSYM && inputKey.getCode() > 0) {
            return inputKey.getCode();
         } else {
            throw new IllegalArgumentException("Unknown key: " + keyName);
         }
      }
   }

   private static void requireArgumentCount(String[] arguments, int minimumArgumentCount) {
      if (arguments.length < minimumArgumentCount) {
         throw new IllegalArgumentException("Not enough arguments");
      }
   }
}

