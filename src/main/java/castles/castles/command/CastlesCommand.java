package castles.castles.command;

import castles.castles.Castle;
import castles.castles.Castles;
import castles.castles.config.Config;
import castles.castles.localization.Phrase;
import castles.castles.scheduler.Scheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static castles.castles.Castles.teleportWarmup;
import static castles.castles.Utils.*;
import static castles.castles.item.Items.getItemCore;
import static castles.castles.localization.Phrase.*;
import static castles.castles.scheduler.Schedules.cooldownKey;
import static java.lang.Math.round;


public class CastlesCommand implements CommandExecutor {
    public void error(@NotNull CommandSender sender, @NotNull String label, String[] args, @Range(from = 0, to = Integer.MAX_VALUE) Integer error, Phrase type){
        ArrayList<String> labelArgs = new ArrayList<>();
        labelArgs.add(label);
        labelArgs.addAll(Arrays.asList(args));
        int position = label.length();
        position += args.length == 0 ? 0 : 1;
        for (int i = 0; i < error; i++) {
            position += args[i].length() + 1;
        }
        String command = String.format("%s", String.join(" ", labelArgs));
        if (position > command.length()){
            position = command.length();
        }
        String commandCorrect = command.substring(0, position);
        String commandError = command.substring(position);
        commandCorrect = commandCorrect.length() > 10 ? "..." + commandCorrect.substring(commandCorrect.length() - 10) : commandCorrect;
        Component errorMessage = Component.text(
                        type.getPhrase(sender), NamedTextColor.RED)
                .appendNewline()
                .append((Component.text(commandCorrect, NamedTextColor.GRAY)))
                .append(Component.text(commandError, NamedTextColor.RED, TextDecoration.UNDERLINED))
                .append(Component.text(ERROR_HERE.getPhrase(sender), NamedTextColor.RED, TextDecoration.ITALIC));
        sender.sendMessage(errorMessage);
    }

    public Location parseBlockLocation(CommandSender sender, String[] args, int index) {
        if (args.length < index + 3) {
            return null;
        }
        Pattern pattern = Pattern.compile("~(-\\d+|\\d+|)");
        if (sender instanceof Player) {
            if (pattern.matcher(args[index]).matches()) {
                args[index] = String.valueOf(((Entity) sender).getLocation().getBlockX() + args.length > 1 ? Integer.parseInt(args[index].substring(1)) : 0);
            }
            if (pattern.matcher(args[index + 1]).matches()) {
                args[index + 1] = String.valueOf(((Entity) sender).getLocation().getBlockY() + args.length > 1 ? Integer.parseInt(args[index + 1].substring(1)) : 0);
            }
            if (pattern.matcher(args[index + 2]).matches()) {
                args[index + 2] = String.valueOf(((Entity) sender).getLocation().getBlockZ() + args.length > 1 ? Integer.parseInt(args[index + 2].substring(1)) : 0);
            }
        }
        if (isInteger(args[index]) && isInteger(args[index + 1]) && isInteger(args[index + 2])) {
            return new Location(
                    sender instanceof Player ? ((Entity) sender).getWorld() : Bukkit.getWorlds().get(0),
                    Integer.parseInt(args[index]) + 0.5,
                    Integer.parseInt(args[index + 1]),
                    Integer.parseInt(args[index + 2]) + 0.5
            );
        } else {
            return null;
        }
    }

    private void list(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.list")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length > index) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        sender.sendMessage(Component.text(CASTLES_LIST_TITLE.getPhrase(sender)).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        List<Castle> castles;
        if (sender instanceof Player && !sender.hasPermission("castles.castles.list.all")) {
            Team team = ((Player) sender).getScoreboard().getPlayerTeam((OfflinePlayer) sender);
            if (team == null) {
                sender.sendMessage(Component.text(NO_CASTLES.getPhrase(sender)).color(NamedTextColor.GRAY));
                return;
            } else {
                castles = new ArrayList<>();
                for (Castle castle : Castles.castles) {
                    if (castle.getOwner().equals(team)) {
                        castles.add(castle);
                    }
                }
            }
        } else {
            castles = Castles.castles;
        }
        if (castles.size() == 0) {
            sender.sendMessage(Component.text(NO_CASTLES.getPhrase(sender)).color(NamedTextColor.GRAY));
            return;
        }
        ArrayList<Component> castleNames = new ArrayList<>();
        for (Castle castle : castles) {
            castleNames.add(castle.getComponent(sender));
        }
        sender.sendMessage(Component.join(Component.text(", "), castleNames));
    }

    private void reload(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.reload")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length > index) {
            error(sender, label, args, index + 1, INCORRECT_ARGUMENT);
            return;
        }
        saveVictims();
        saveCastles();
        Castles.castles.clear();
        loadCastles();
        loadVictims();
        try {
            Config.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sender.sendMessage(Component.text("Castles reloaded!", NamedTextColor.GREEN));
    }

    private void state(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.state")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length < index) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length == index) {
            sender.sendMessage(formatComponent(Component.text(CURRENT_STATE.getPhrase(sender)), Component.text(stringToPhrase(Config.getGlobal().STATE).getPhrase(sender), getStateColor(Config.getGlobal().STATE))));
            return;
        }
        if (args.length == index + 1 && !sender.hasPermission("castles.castles.state.change")) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        switch (args[index]) {
            case "peaceful":
                Config.getGlobal().STATE = "PEACEFUL";
                Config.getGlobal().setValue("STATE", "PEACEFUL");
                break;
            case "war":
                Config.getGlobal().STATE = "WAR";
                Config.getGlobal().setValue("STATE", "WAR");
                break;
            case "preparation":
                Config.getGlobal().STATE = "PREPARATION";
                Config.getGlobal().setValue("STATE", "PREPARATION");
                break;
            default:
                error(sender, label, args, index, INCORRECT_ARGUMENT);
                return;
        }
        try {
            Config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sender.sendMessage(formatComponent(Component.text(STATE_CHANGED.getPhrase(sender)), Component.text(stringToPhrase(Config.getGlobal().STATE).getPhrase(sender), getStateColor(Config.getGlobal().STATE))));
    }

    private void create(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.create")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 4) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        String name = strip(args[index], "\"", 1);
        if (getCastleByName(name) != null) {
            sender.sendMessage(Component.text(NAME_DUPLICATE.getPhrase(sender), NamedTextColor.RED));
            return;
        } else if (name.contains("\"")) {
            sender.sendMessage(Component.text(NAME_DOUBLE_QUOTE.getPhrase(sender), NamedTextColor.RED));
            return;
        }
        Location location;
        Team team = sender instanceof Player ? ((Player) sender).getScoreboard().getPlayerTeam((OfflinePlayer) sender) : null;
        if (args.length == index + 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text(REQUIRE_ENTITY.getPhrase(sender), NamedTextColor.RED));
                return;
            }
            Player player = (Player) sender;
            location = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, round(player.getLocation().getY()), player.getLocation().getBlockZ() + 0.5);
        } else {
            location = parseBlockLocation(sender, args, index + 1);
            if (location == null) {
                if(!isInteger(args[index + 1])) {
                    error(sender, label, args, index + 1, EXPECTED_INTEGER);
                    return;
                } else if(args.length >= index + 3 && !isInteger(args[index + 2])) {
                    error(sender, label, args, index + 2, EXPECTED_INTEGER);
                    return;
                } else if(args.length >= index + 4 && !isInteger(args[index + 3])) {
                    error(sender, label, args, index + 3, EXPECTED_INTEGER);
                    return;
                } else {
                    error(sender, label, args, index + 1, INCORRECT_ARGUMENT);
                    return;
                }
            }
        }
        if (getCastleByLocation(location) != null) {
            sender.sendMessage(Component.text(CASTLE_ALREADY_IN_CHUNK.getPhrase(sender), NamedTextColor.RED));
            return;
        }
        WorldEnv worldEnv = getWorldEnv(location.getWorld());
        if (location.getY() < worldEnv.getMinY() || location.getY() > worldEnv.getMaxY()) {
            sender.sendMessage(Component.text(String.format(Y_COORD_OUT_OF_RANGE.getPhrase(sender), worldEnv.getMinY(), worldEnv.getMaxY()), NamedTextColor.RED));
            return;
        }
        if (getMod(location.getX(), 16) < 1.5 || getMod(location.getX(), 16) > 14.5 || getMod(location.getZ(), 16) < 1.5 || getMod(location.getZ(), 16) > 14.5) {
            sender.sendMessage(Component.text(CORE_ON_EDGE.getPhrase(sender), NamedTextColor.RED));
            return;
        }

        Castle castle = new Castle(name, location, team);
        sender.sendMessage(formatComponent(Component.text(CASTLES_CREATE.getPhrase(sender)), castle.getComponent(sender)));
    }

    private void remove(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.remove")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        String name = strip(args[index], "\"", 1);
        Castle castle = getCastleByName(name);
        if (castle == null) {
            sender.sendMessage(Component.text(String.format(UNKNOWN_CASTLE.getPhrase(sender), name), NamedTextColor.RED));
            return;
        }
        sender.sendMessage(formatComponent(Component.text(CASTLES_REMOVE.getPhrase(sender)), castle.getComponent(sender)));
        castle.destroy();
    }

    private void expand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.expand")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length < index + 1) {
            error(sender, label, args, args.length, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 4) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        Location location;
        ChunkPos chunkPos;
        String name = strip(args[index], "\"", 1);
        Castle castle = getCastleByName(name);
        if (castle == null) {
            sender.sendMessage(Component.text(String.format(UNKNOWN_CASTLE.getPhrase(sender), args[index]), NamedTextColor.RED));
            return;
        }
        if (args.length == index + 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text(REQUIRE_ENTITY.getPhrase(sender), NamedTextColor.RED));
                return;
            }
            Player player = (Player) sender;
            location = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, round(player.getLocation().getY()), player.getLocation().getBlockZ() + 0.5);
        } else {
            location = parseBlockLocation(sender, args, index + 1);
            if (location == null) {
                if(!isInteger(args[index + 1])) {
                    error(sender, label, args, index + 1, EXPECTED_INTEGER);
                    return;
                } else if(args.length >= index + 3 && !isInteger(args[index + 2])) {
                    error(sender, label, args, index + 2, EXPECTED_INTEGER);
                    return;
                } else if(args.length >= index + 4 && !isInteger(args[index + 3])) {
                    error(sender, label, args, index + 3, EXPECTED_INTEGER);
                    return;
                } else {
                    error(sender, label, args, index + 1, INCORRECT_ARGUMENT);
                    return;
                }
            }
            location = new Location(location.getWorld(), location.getBlockX() * 16 + 8, round(location.getY()), location.getBlockZ() * 16 + 8);
        }
        chunkPos = new ChunkPos(location.getChunk());
        if (getCastleByLocation(location) != null) {
            if (getCastleByLocation(location).equals(castle)) {
                sender.sendMessage(Component.text(ALREADY_PART_OF_THE_CASTLE.getPhrase(sender), NamedTextColor.RED));
            } else {
                sender.sendMessage(Component.text(ALREADY_PART_OF_ANOTHER_CASTLE.getPhrase(sender), NamedTextColor.RED));
            }
            return;
        }
        if (location.getWorld().getUID() != castle.chunks.get(0).getWorld().getUID()) {
            sender.sendMessage(Component.text(DIFFERENT_WORLD.getPhrase(sender), NamedTextColor.RED));
            return;
        }
        for (ChunkPos castleChunk : castle.chunks) {
            if (castleChunk.isAdjacent(chunkPos)) {
                castle.expand(chunkPos);
                return;
            }
        }
    }

    private void teleport(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text(REQUIRE_ENTITY.getPhrase(sender), NamedTextColor.RED));
            return;
        }
        if (!(sender.hasPermission("castles.castles.teleport.own") || sender.hasPermission("castles.castles.teleport.any"))) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        String name = strip(args[index], "\"", 1);
        Castle castle = getCastleByName(name);
        if (castle == null) {
            sender.sendMessage(Component.text(String.format(UNKNOWN_CASTLE.getPhrase(sender), name), NamedTextColor.RED));
            return;
        }
        Team team = ((Player) sender).getScoreboard().getPlayerTeam((OfflinePlayer) sender);
        if (sender.hasPermission("castles.castles.teleport.any")) {
            ((Player) sender).teleport(Location.deserialize(castle.location));
            sender.sendMessage(formatComponent(Component.text(CASTLES_TELEPORT.getPhrase(sender)), castle.getComponent(sender)));
            return;
        }
        if (sender.hasPermission("castles.castles.teleport.own")) {
            if (team == null || !team.equals(castle.getOwner())) {
                sender.sendMessage(Component.text(TELEPORT_PERMISSION_OWNER_ONLY.getPhrase(sender), NamedTextColor.RED));
                return;
            }
            PersistentDataContainer container = ((Player) sender).getPersistentDataContainer();
            if (container.has(cooldownKey, PersistentDataType.INTEGER) && container.get(cooldownKey, PersistentDataType.INTEGER) > 0) {
                sender.sendMessage(Component.text(String.format(TELEPORT_COOLDOWN_REMAINING.getPhrase(sender), container.get(cooldownKey, PersistentDataType.INTEGER)), NamedTextColor.RED));
                return;
            }
            if (teleportWarmup.containsKey(sender)) {
                teleportWarmup.get(sender).cancel();
                sender.sendMessage(Component.text(TELEPORT_CANCELLED.getPhrase(sender), NamedTextColor.RED));
            }
            sender.sendMessage(formatComponent(Component.text(String.format(TELEPORT_WARMUP.getPhrase(sender), Config.getGlobal().TELEPORT_WARMUP)), castle.getComponent(sender)));
            teleportWarmup.put((Player) sender, Scheduler.scheduleSyncDelayedTask(() -> {
                ((Player) sender).teleport(Location.deserialize(castle.location));
                sender.sendMessage(formatComponent(Component.text(CASTLES_TELEPORT.getPhrase(sender)), castle.getComponent(sender)));
                container.set(cooldownKey, PersistentDataType.INTEGER, Config.getGlobal().TELEPORT_COOLDOWN);
            }, Config.getGlobal().TELEPORT_WARMUP * 20));
        }
    }

    private void modify(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.modify")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length < index + 2) {
            error(sender, label, args, args.length, UNKNOWN_COMMAND);
            return;
        }
        String name = strip(args[index], "\"", 1);
        Castle castle = getCastleByName(name);
        if (castle == null) {
            sender.sendMessage(Component.text(String.format(UNKNOWN_CASTLE.getPhrase(sender), name), NamedTextColor.RED));
            return;
        }
        String subCommand = args[index + 1];
        switch (subCommand) {
            case "location": {
                modifyLocation(sender, label, args, index + 2, castle);
                break;
            }
            case "name": {
                modifyName(sender, label, args, index + 2, castle);
                break;
            }
            case "owner": {
                modifyOwner(sender, label, args, index + 2, castle);
                break;
            }
            case "protect": {
                modifyProtect(sender, label, args, index + 2, castle);
                break;
            }
            default: {
                error(sender, label, args, index + 1, UNKNOWN_COMMAND);
                break;
            }
        }
    }

    private void modifyLocation(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, Castle castle) {
        if (args.length < index) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 3) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        Location location;
        if (args.length == index) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text(REQUIRE_ENTITY.getPhrase(sender), NamedTextColor.RED));
                return;
            }
            Player player = (Player) sender;
            location = new Location(player.getWorld(), player.getLocation().getBlockX() + 0.5, round(player.getLocation().getY()), player.getLocation().getBlockZ() + 0.5);
        } else {
            location = parseBlockLocation(sender, args, index);
            if (location == null) {
                if(!isInteger(args[index])) {
                    error(sender, label, args, index, EXPECTED_INTEGER);
                    return;
                } else if(args.length >= index + 2 && !isInteger(args[index + 1])) {
                    error(sender, label, args, index + 1, EXPECTED_INTEGER);
                    return;
                } else if(args.length >= index + 3 && !isInteger(args[index + 2])) {
                    error(sender, label, args, index + 2, EXPECTED_INTEGER);
                    return;
                } else {
                    error(sender, label, args, index, INCORRECT_ARGUMENT);
                    return;
                }
            }
        }
        if (getCastleByLocation(location) != castle) {
            sender.sendMessage(Component.text(OUT_OF_CASTLE.getPhrase(sender), NamedTextColor.RED));
            return;
        }
        WorldEnv worldEnv = getWorldEnv(location.getWorld());
        if (location.getY() < worldEnv.getMinY() || location.getY() > worldEnv.getMaxY()) {
            sender.sendMessage(Component.text(String.format(Y_COORD_OUT_OF_RANGE.getPhrase(sender), worldEnv.getMinY(), worldEnv.getMaxY()), NamedTextColor.RED));
            return;
        }
        if (getMod(location.getX(), 16) < 1.5 || getMod(location.getX(), 16) > 14.5 || getMod(location.getZ(), 16) < 1.5 || getMod(location.getZ(), 16) > 14.5) {
            sender.sendMessage(Component.text(CORE_ON_EDGE.getPhrase(sender), NamedTextColor.RED));
            return;
        }
        castle.setLocation(location);
        sender.sendMessage(formatComponent(Component.text(CASTLES_MODIFY_LOCATION.getPhrase(sender)), castle.getComponent(sender), getLocationComponent(location)));
    }

    private void modifyName(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, Castle castle) {
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        String name = strip(args[index], "\"", 1);
        if (getCastleByName(name) != null) {
            sender.sendMessage(Component.text(NAME_DUPLICATE.getPhrase(sender), NamedTextColor.RED));
            return;
        } else if (name.contains("\"")) {
            sender.sendMessage(Component.text(NAME_DOUBLE_QUOTE.getPhrase(sender), NamedTextColor.RED));
            return;
        }
        castle.setName(name);
        sender.sendMessage(formatComponent(Component.text(CASTLES_MODIFY_NAME.getPhrase(sender)), castle.getComponent(sender)));
    }

    private void modifyOwner(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, Castle castle) {
        Team team;
        if (args.length < index) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        } else if (args.length == index) {
            team = null;
        } else {
            team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(args[3]);
            if (team == null) {
                sender.sendMessage(Component.text(String.format(UNKNOWN_TEAM.getPhrase(sender), args[3]), NamedTextColor.RED));
                return;
            }
        }
        castle.setOwner(team);
        if (team == null) {
            sender.sendMessage(formatComponent(Component.text(CASTLES_MODIFY_OWNER_NONE.getPhrase(sender)), castle.getComponent(sender)));
        } else {
            sender.sendMessage(formatComponent(Component.text(CASTLES_MODIFY_OWNER.getPhrase(sender)), castle.getComponent(sender), getTeamComponent(team)));
        }
    }

    private void modifyProtect(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, Castle castle) {
        if (args.length < index + 2) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 2) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        switch (args[index]) {
            case "set": {
                AbstractMap.SimpleEntry<Double, String> timeUnit = parseIntegerAndUnit(args[index + 1]);
                if (timeUnit == null) {
                    error(sender, label, args, index + 1, EXPECTED_FLOAT);
                    return;
                }
                Integer time = timeUnitToSeconds(timeUnit.getKey(), timeUnit.getValue());
                if (time == null) {
                    sender.sendMessage(Component.text(INVALID_UNIT.getPhrase(sender), NamedTextColor.RED));
                    return;
                } else if (time < 0) {
                    sender.sendMessage(Component.text(NEGATIVE_TIME.getPhrase(sender), NamedTextColor.RED));
                    return;
                }
                castle.protectionTime = time;
                break;
            }
            case "add": {
                AbstractMap.SimpleEntry<Double, String> timeUnit = parseIntegerAndUnit(args[index + 1]);
                if (timeUnit == null) {
                    error(sender, label, args, index + 1, EXPECTED_FLOAT);
                    return;
                }
                Integer time = timeUnitToSeconds(timeUnit.getKey(), timeUnit.getValue());
                if (time == null) {
                    sender.sendMessage(Component.text(INVALID_UNIT.getPhrase(sender), NamedTextColor.RED));
                    return;
                }
                if (castle.protectionTime + time > 0) {
                    castle.protectionTime += time;
                } else {
                    castle.protectionTime = 0;
                }
                break;
            }
            default: {
                error(sender, label, args, index, INCORRECT_ARGUMENT);
                return;
            }
        }
        if (castle.protectionTime > 0) {
            sender.sendMessage(formatComponent(Component.text(String.format(CASTLES_MODIFY_PROTECTION.getPhrase(sender), secondsToTimeString(castle.protectionTime))), castle.getComponent(sender)));
        } else {
            sender.sendMessage(formatComponent(Component.text(CASTLES_MODIFY_PROTECTION_NONE.getPhrase(sender)), castle.getComponent(sender)));
        }
    }

    private void core(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.core")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length < index + 2) {
            error(sender, label, args, args.length, UNKNOWN_COMMAND);
            return;
        }
        String name = strip(args[index], "\"", 1);
        Castle castle = getCastleByName(name);
        if (castle == null) {
            sender.sendMessage(Component.text(String.format(UNKNOWN_CASTLE.getPhrase(sender), args[index]), NamedTextColor.RED));
            return;
        }
        switch (args[index + 1]) {
            case "level": {
                coreLevel(sender, label, args, index + 2, castle);
                break;
            }
            case "health": {
                coreHealth(sender, label, args, index + 2, castle);
                break;
            }
            default: {
                error(sender, label, args, index + 1, INCORRECT_ARGUMENT);
                break;
            }
        }
    }

    private void coreLevel(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, Castle castle) {
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        int level;
        if (!isInteger(args[index])) {
            error(sender, label, args, index, EXPECTED_INTEGER);
            return;
        } else {
            level = Integer.parseInt(args[index]);
        }
        if (level < 1 || level > 5) {
            sender.sendMessage(Component.text(String.format(LEVEL_OUT_OF_RANGE.getPhrase(sender), 1, 5), NamedTextColor.RED));
            return;
        }
        castle.setCoreLevel(level);
        sender.sendMessage(formatComponent(Component.text(String.format(CASTLES_CORE_LEVEL.getPhrase(sender), level)), castle.getComponent(sender)));
    }

    private void coreHealth(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, Castle castle) {
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        double health;
        if (!isInteger(args[index])) {
            error(sender, label, args, index, EXPECTED_FLOAT);
            return;
        } else {
            health = Integer.parseInt(args[index]);
        }
        if (health <= 0) {
            sender.sendMessage(Component.text(NOT_POSITIVE_HEALTH.getPhrase(sender), NamedTextColor.RED));
            return;
        } else if (health > castle.getCoreMaxHealth()) {
            sender.sendMessage(Component.text(String.format(HEALTH_EXCEEDS_MAX.getPhrase(sender),
                    castle.getCoreMaxHealth()), NamedTextColor.RED));
            return;
        }
        castle.setCoreHealth(health);
        sender.sendMessage(formatComponent(Component.text(String.format(CASTLES_CORE_HEALTH.getPhrase(sender), health)), castle.getComponent(sender)));
    }

    private void rampart (@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.rampart")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length < index + 2) {
            error(sender, label, args, args.length, UNKNOWN_COMMAND);
            return;
        }
        String name = strip(args[index], "\"", 1);
        Castle castle = getCastleByName(name);
        if (castle == null) {
            sender.sendMessage(Component.text(String.format(UNKNOWN_CASTLE.getPhrase(sender), args[index]), NamedTextColor.RED));
            return;
        }
        switch (args[index + 1]) {
            case "level": {
                rampartLevel(sender, label, args, index + 2, castle);
                break;
            }
            case "health": {
                rampartHealth(sender, label, args, index + 2, castle);
                break;
            }
            default: {
                error(sender, label, args, index + 1, INCORRECT_ARGUMENT);
                break;
            }
        }
    }

    private void rampartLevel(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, Castle castle) {
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        int level;
        if (!isInteger(args[index])) {
            error(sender, label, args, index, EXPECTED_INTEGER);
            return;
        } else {
            level = Integer.parseInt(args[index]);
        }
        if (level < 1 || level > 5) {
            sender.sendMessage(Component.text(String.format(LEVEL_OUT_OF_RANGE.getPhrase(sender),1 , 5), NamedTextColor.RED));
            return;
        }
        castle.setRampartLevel(level);
        sender.sendMessage(formatComponent(Component.text(String.format(CASTLES_RAMPART_LEVEL.getPhrase(sender), level)), castle.getComponent(sender)));
    }

    private void rampartHealth(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, Castle castle) {
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        int health;
        if (!isInteger(args[index])) {
            error(sender, label, args, index, EXPECTED_INTEGER);
            return;
        } else {
            health = Integer.parseInt(args[index]);
        }
        if (health < 0) {
            sender.sendMessage(Component.text(NOT_POSITIVE_HEALTH.getPhrase(sender), NamedTextColor.RED));
            return;
        } else if (health > castle.getRampartMaxHealth()) {
            sender.sendMessage(Component.text(String.format(HEALTH_EXCEEDS_MAX.getPhrase(sender),
                    castle.getRampartMaxHealth()), NamedTextColor.RED));
            return;
        }
        castle.setRampartHealth(health);
        sender.sendMessage(formatComponent(Component.text(String.format(CASTLES_RAMPART_HEALTH.getPhrase(sender), health)), castle.getComponent(sender)));
    }

    private void item(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index) {
        if (sender instanceof Player && !sender.hasPermission("castles.castles.item")) {
            error(sender, label, args, index - 1, INCORRECT_ARGUMENT);
            return;
        }
        if (args.length < index) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text(REQUIRE_PLAYER.getPhrase(sender), NamedTextColor.RED));
            return;
        }
        Player player = (Player) sender;
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        ItemStack core = getItemCore();
        if (!player.hasPermission("castles.castles.item.ignoreBP")) {
            if (team == null || getScore(team) < 50) {
                sender.sendMessage(Component.text(BP_NOT_ENOUGH.getPhrase(sender), NamedTextColor.RED));
                return;
            } else {
                setScore(team, getScore(team) - 50);
            }
        }
        if (player.getInventory().contains(core)) {
            ((Player) sender).getInventory().addItem(core);
        } else if (player.getInventory().firstEmpty() == -1) {
            ((Player) sender).getWorld().dropItemNaturally(((Player) sender).getLocation(), core);
        } else {
            ((Player) sender).getInventory().addItem(core);
        }
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] rawArgs) {
        String[] args = combineArgs(rawArgs);
        if (args.length > 0 && ((sender instanceof Player && sender.hasPermission("castles.castles")) || sender instanceof ConsoleCommandSender)) {
            switch (args[0]) {
                case "list": {
                    list(sender, label, args, 1);
                    break;
                }
                case "reload": {
                    reload(sender, label, args, 1);
                    break;
                }
                case "state": {
                    state(sender, label, args, 1);
                    break;
                }
                case "create": {
                    create(sender, label, args, 1);
                    break;
                }
                case "expand": {
                    expand(sender, label, args, 1);
                    break;
                }
                case "remove": {
                    remove(sender, label, args, 1);
                    break;
                }
                case "teleport":
                case "tp": {
                    teleport(sender, label, args, 1);
                    break;
                }
                case "modify": {
                    modify(sender, label, args, 1);
                    break;
                }
                case "core": {
                    core(sender, label, args, 1);
                    break;
                }
                case "rampart": {
                    rampart(sender, label, args, 1);
                    break;
                }
                case "item": {
                    item(sender, label, args, 1);
                    break;
                }
                default: {
                    error(sender, label, args, 0, UNKNOWN_COMMAND);
                }
            }
        } else {
            error(sender, label, args, 0, UNKNOWN_COMMAND);
        }
        return true;
    }
}