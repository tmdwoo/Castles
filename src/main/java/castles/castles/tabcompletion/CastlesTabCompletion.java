package castles.castles.tabcompletion;

import castles.castles.Castle;
import castles.castles.Castles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static castles.castles.Utils.*;

public class CastlesTabCompletion implements TabCompleter {
    public final Map<String, ArrayList<String>> commands = Map.ofEntries(
            Map.entry("list", new ArrayList<>(List.of("list"))),
            Map.entry("reload", new ArrayList<>(List.of("reload"))),
            Map.entry("state", new ArrayList<>(List.of("state"))),
            Map.entry("create", new ArrayList<>(List.of("create"))),
            Map.entry("remove", new ArrayList<>(List.of("remove"))),
            Map.entry("expand", new ArrayList<>(List.of("expand"))),
            Map.entry("teleport", new ArrayList<>(Arrays.asList("teleport.any", "teleport.own"))),
            Map.entry("tp", new ArrayList<>(Arrays.asList("teleport.any", "teleport.own"))),
            Map.entry("modify", new ArrayList<>(List.of("modify"))),
            Map.entry("core", new ArrayList<>(List.of("core"))),
            Map.entry("rampart", new ArrayList<>(List.of("rampart"))),
            Map.entry("item", new ArrayList<>(List.of("item")))
            );

    public @Nullable String getCastleCompletion(Castle castle, String arg) {
        String name = castle.name;
        if (arg.startsWith("\"") || name.contains(" ")) {
            name = "\"" + name + "\"";
        }
        List<String> names = new ArrayList<>(List.of(name.split(" ")));
        if (name.toLowerCase().startsWith(arg)) {
            int spaceCount = countChar(arg, " ");
            if (spaceCount > 0) {
                names = names.subList(spaceCount, names.size());
                name = String.join(" ", names);
            }
            return name;
        }
        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] rawArgs) {
        String[] args = lowerStrings(combineArgs(rawArgs));
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            HashSet<String> completionSet = new HashSet<>();
            for (String c : commands.keySet()) {
                if (c.toLowerCase().startsWith(args[0])) {
                    for (String perm : commands.get(c)) {
                        if (sender.hasPermission("castles.castles." + perm)) {
                            completionSet.add(c);
                            break;
                        }
                    }
                }
            }
            completions.addAll(completionSet);
        } else if (args.length >= 2) {
            switch (args[0]) {
                case "state":
                    if (sender.hasPermission("castles.castles.state.change")) {
                        completions.add("peaceful");
                        completions.add("war");
                        completions.add("preparation");
                    }
                    break;
                case "create":
                    if (sender.hasPermission("castles.castles.create")) {
                        Location loc = ((Player) sender).getLocation();
                        if (args.length == 3) {
                            String arg;
                            if (args[2].equals("")) {
                                arg = String.valueOf(loc.getBlockX());
                            } else {
                                arg = args[2];
                            }
                            completions.add(arg);
                            completions.add(arg + " " + loc.getBlockY());
                            completions.add(arg + " " + loc.getBlockY() + " " + loc.getBlockZ());
                        } else if (args.length == 4) {
                            String arg;
                            if (args[3].equals("")) {
                                arg = String.valueOf(loc.getBlockY());
                            } else {
                                arg = args[3];
                            }
                            completions.add(arg);
                            completions.add(arg + " " + loc.getBlockZ());
                        } else if (args.length == 5) {
                            String arg;
                            if (args[4].equals("")) {
                                arg = String.valueOf(loc.getBlockZ());
                            } else {
                                arg = args[4];
                            }
                            completions.add(arg);
                        }
                    }
                    break;
                case "remove":
                    if (sender.hasPermission("castles.castles.remove")) {
                        if (args.length == 2) {
                            for (Castle castle : Castles.castles) {
                                String completion = getCastleCompletion(castle, args[1]);
                                if (completion != null) {
                                    completions.add(completion);
                                }
                            }
                        }
                    }
                    break;
                case "expand":
                    if (sender.hasPermission("castles.castles.expand")) {
                        if (args.length == 2) {
                            for (Castle castle : Castles.castles) {
                                String completion = getCastleCompletion(castle, args[1]);
                                if (completion != null) {
                                    completions.add(completion);
                                }
                            }
                        }
                        Location loc = ((Player) sender).getTargetBlock(null, 100).getLocation();
                        if (args.length == 3) {
                            String arg;
                            if (args[2].equals("")) {
                                arg = String.valueOf(loc.getBlockX());
                            } else {
                                arg = args[2];
                            }
                            completions.add(arg);
                            completions.add(arg + " " + loc.getBlockY());
                            completions.add(arg + " " + loc.getBlockY() + " " + loc.getBlockZ());
                        } else if (args.length == 4) {
                            String arg;
                            if (args[3].equals("")) {
                                arg = String.valueOf(loc.getBlockY());
                            } else {
                                arg = args[3];
                            }
                            completions.add(arg);
                            completions.add(arg + " " + loc.getBlockZ());
                        } else if (args.length == 5) {
                            String arg;
                            if (args[4].equals("")) {
                                arg = String.valueOf(loc.getBlockZ());
                            } else {
                                arg = args[4];
                            }
                            completions.add(arg);
                        }
                    }
                    break;
                case "teleport":
                case "tp":
                    if (sender.hasPermission("castles.castles.teleport.any") || sender.hasPermission("castles.castles.teleport.own")) {
                        for (Castle castle : Castles.castles) {
                            if (sender.hasPermission("castles.castles.teleport.any") || (sender.hasPermission("castles.castles.teleport.own") && castle.getOwner() != null && castle.getOwner().hasPlayer((Player) sender))) {
                                String completion = getCastleCompletion(castle, args[1]);
                                if (completion != null) {
                                    completions.add(completion);
                                }
                            }
                        }
                    }
                    break;
                case "modify":
                    if (sender.hasPermission("castles.castles.modify")) {
                        if (args.length == 2) {
                            for (Castle castle : Castles.castles) {
                                String completion = getCastleCompletion(castle, args[1]);
                                if (completion != null) {
                                    completions.add(completion);
                                }

                            }
                        } else if (args.length == 3) {
                            List<String> modifyCommands = new ArrayList<>(List.of("name", "owner", "protect", "location"));
                            for (String c : modifyCommands) {
                                if (c.toLowerCase().startsWith(args[2])) {
                                    completions.add(c);
                                }
                            }
                        } else {
                            switch (args[2]) {
                                case "name":
                                    break;
                                case "owner":
                                    if (args.length == 4) {
                                        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
                                            if (team.getName().toLowerCase().startsWith(args[3])) {
                                                completions.add(team.getName());
                                            }
                                        }
                                    }
                                    break;
                                case "protect":
                                    if (args.length == 4) {
                                        List<String> protectCommands = new ArrayList<>(List.of("set", "add"));
                                        for (String c : protectCommands) {
                                            if (c.toLowerCase().startsWith(args[3])) {
                                                completions.add(c);
                                            }
                                        }
                                    } else if (args.length == 5) {
                                        List<String> timeUnits = new ArrayList<>(List.of("s", "m", "h", "d"));
                                        AbstractMap.SimpleEntry<Double, String> timeUnit = parseIntegerAndUnit(args[4]);
                                        if (timeUnit != null) {
                                            if (timeUnit.getValue().equals("")) {
                                                for (String unit : timeUnits) {
                                                    completions.add(args[4] + unit);
                                                }
                                            }
                                        }
                                    }
                                    break;
                                case "location":
                                    Location loc = ((Player) sender).getTargetBlock(null, 5).getLocation();
                                    if (args.length == 4) {
                                        String arg;
                                        if (args[3].equals("")) {
                                            arg = String.valueOf(loc.getBlockX());
                                        } else {
                                            arg = args[3];
                                        }
                                        completions.add(arg);
                                        completions.add(arg + " " + loc.getBlockY());
                                        completions.add(arg + " " + loc.getBlockY() + " " + loc.getBlockZ());
                                    } else if (args.length == 5) {
                                        String arg;
                                        if (args[4].equals("")) {
                                            arg = String.valueOf(loc.getBlockY());
                                        } else {
                                            arg = args[4];
                                        }
                                        completions.add(arg);
                                        completions.add(arg + " " + loc.getBlockZ());
                                    } else if (args.length == 6) {
                                        String arg;
                                        if (args[5].equals("")) {
                                            arg = String.valueOf(loc.getBlockZ());
                                        } else {
                                            arg = args[5];
                                        }
                                        completions.add(arg);
                                    }
                            }
                        }
                    }
                    break;
                case "core":
                    if (sender.hasPermission("castles.castles.core")) {
                        if (args.length == 2) {
                            for (Castle castle : Castles.castles) {
                                String completion = getCastleCompletion(castle, args[1]);
                                if (completion != null) {
                                    completions.add(completion);
                                }
                            }
                        } else if (args.length == 3) {
                            List<String> coreCommands = new ArrayList<>(List.of("health", "level"));
                            for (String c : coreCommands) {
                                if (c.toLowerCase().startsWith(args[2])) {
                                    completions.add(c);
                                }
                            }
                        }
                    }
                    break;
                case "rampart":
                    if (sender.hasPermission("castles.castles.rampart")) {
                        if (args.length == 2) {
                            for (Castle castle : Castles.castles) {
                                String completion = getCastleCompletion(castle, args[1]);
                                if (completion != null) {
                                    completions.add(completion);
                                }
                            }
                        } else if (args.length == 3) {
                            List<String> rampartCommands = new ArrayList<>(List.of("health", "level"));
                            for (String c : rampartCommands) {
                                if (c.toLowerCase().startsWith(args[2])) {
                                    completions.add(c);
                                }
                            }
                        }
                    }
                    break;
            }
        }
        return completions;
    }
}
