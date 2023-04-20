package castles.castles.command;

import castles.castles.localization.Phrase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Arrays;

import static castles.castles.Utils.*;
import static castles.castles.localization.Phrase.*;

public class BloodPointsCommand implements CommandExecutor {
    public void error(@NotNull CommandSender sender, @NotNull String label, String[] args, @Range(from = 0, to = Integer.MAX_VALUE) Integer error, Phrase type) {
        ArrayList<String> labelArgs = new ArrayList<>();
        labelArgs.add(label);
        labelArgs.addAll(Arrays.asList(args));
        int position = label.length();
        position += args.length == 0 ? 0 : 1;
        for (int i = 0; i < error; i++) {
            position += args[i].length() + 1;
        }
        String command = String.format("%s", String.join(" ", labelArgs));
        if (position > command.length()) {
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

    private void add(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, @NotNull Team team) {
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        int points;
        if (!isInteger(args[index])) {
            error(sender, label, args, index, EXPECTED_INTEGER);
            return;
        } else {
            points = Integer.parseInt(args[index]);
        }
        addScore(team, points);
    }

    private void set(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int index, @NotNull Team team) {
        if (args.length < index + 1) {
            error(sender, label, args, index, UNKNOWN_COMMAND);
            return;
        } else if (args.length > index + 1) {
            error(sender, label, args, index, INCORRECT_ARGUMENT);
            return;
        }
        int points;
        if (!isInteger(args[index])) {
            error(sender, label, args, index, EXPECTED_INTEGER);
            return;
        } else {
            points = Integer.parseInt(args[index]);
        }
        if (points < 0) {
            sender.sendMessage(Component.text(NEGATIVE_BLOODPOINTS.getPhrase(sender), NamedTextColor.RED));
            return;
        }
        setScore(team, points);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] rawArgs) {
        String[] args = combineArgs(rawArgs);
        if (args.length > 0 && ((sender instanceof Player && sender.hasPermission("castles.bloodpoints")) || sender instanceof ConsoleCommandSender)) {
            if (args.length < 3) {
                error(sender, label, args, args.length, UNKNOWN_COMMAND);
                return true;
            } else if (args.length > 3) {
                error(sender, label, args, 3, INCORRECT_ARGUMENT);
                return true;
            }
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(args[0]);
            if (team == null) {
                sender.sendMessage(Component.text(String.format(UNKNOWN_TEAM.getPhrase(sender), args[0]), NamedTextColor.RED));
                return true;
            }
            switch (args[1]) {
                case "add": {
                    add(sender, label, args, 2, team);
                    break;
                }
                case "set": {
                    set(sender, label, args, 2, team);
                    break;
                }
                default: {
                    error(sender, label, args, 1, UNKNOWN_COMMAND);
                    break;
                }
            }

        } else {
            error(sender, label, args, 0, UNKNOWN_COMMAND);
        }
        return true;
    }
}
