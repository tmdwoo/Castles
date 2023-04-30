package castles.castles.chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static castles.castles.Utils.combineArgs;
import static castles.castles.Utils.lowerStrings;

public class TeamChatTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] rawArgs) {
        String[] args = lowerStrings(combineArgs(rawArgs));
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("castles.teamchat")) {
            return null;
        }
        if (args.length == 1) {
            if ("on".startsWith(args[0])) {
                completions.add("on");
            }
            if ("off".startsWith(args[0])) {
                completions.add("off");
            }
        }
        return completions;
    }
}
