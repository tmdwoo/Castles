package castles.castles.tabcompletion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static castles.castles.Utils.combineArgs;
import static castles.castles.Utils.lowerStrings;

public class BloodPointsTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] rawArgs) {
        String[] args = lowerStrings(combineArgs(rawArgs));
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("castles.bloodpoints")) {
            return null;
        }
        if (args.length == 1) {
            for (String team : sender.getServer().getScoreboardManager().getMainScoreboard().getTeams().stream().map(Team::getName).toArray(String[]::new)) {
                if (team.toLowerCase().startsWith(args[0])) {
                    completions.add(team);
                }
            }
        } else if (args.length == 2) {
            if ("add".startsWith(args[1])) {
                completions.add("add");
            }
            if ("set".startsWith(args[1])) {
                completions.add("set");
            }
        }
        return completions;
    }
}
