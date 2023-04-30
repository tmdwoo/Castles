package castles.castles.chat;

import castles.castles.localization.Phrase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Arrays;

import static castles.castles.localization.Phrase.*;

public class TeamChatCommand implements CommandExecutor {
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

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] rawArgs) {
        if (rawArgs.length > 1){
            error(sender, label, rawArgs, 1, UNKNOWN_COMMAND);
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text(REQUIRE_PLAYER.getPhrase(sender), NamedTextColor.RED));
            return true;
        }
        Player player = (Player) sender;
        Team team = player.getScoreboard().getPlayerTeam(player);
        if (team == null) {
            player.sendMessage(Component.text(TEAMCHAT_REQUIRE_TEAM.getPhrase(player), NamedTextColor.RED));
            return true;
        }
        if (!player.getPersistentDataContainer().has(ChatHandler.teamChatKey, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().set(ChatHandler.teamChatKey, PersistentDataType.BYTE, (byte) 0);
        }
        if (rawArgs.length == 1) {
            if (rawArgs[0].equals("on")) {
                player.getPersistentDataContainer().set(ChatHandler.teamChatKey, PersistentDataType.BYTE, (byte) 1);
                player.sendMessage(Component.text(TEAMCHAT_ENABLED.getPhrase(player), NamedTextColor.GREEN));
                return true;
            }
            if (rawArgs[0].equals("off")) {
                player.getPersistentDataContainer().set(ChatHandler.teamChatKey, PersistentDataType.BYTE, (byte) 0);
                player.sendMessage(Component.text(TEAMCHAT_DISABLED.getPhrase(player), NamedTextColor.RED));
                return true;
            }
            error(sender, label, rawArgs, 1, UNKNOWN_COMMAND);
            return true;
        }
        byte teamChat = player.getPersistentDataContainer().get(ChatHandler.teamChatKey, PersistentDataType.BYTE);
        if (teamChat == 0) {
            player.getPersistentDataContainer().set(ChatHandler.teamChatKey, PersistentDataType.BYTE, (byte) 1);
            player.sendMessage(Component.text(TEAMCHAT_ENABLED.getPhrase(player), NamedTextColor.GREEN));
        } else {
            player.getPersistentDataContainer().set(ChatHandler.teamChatKey, PersistentDataType.BYTE, (byte) 0);
            player.sendMessage(Component.text(TEAMCHAT_DISABLED.getPhrase(player), NamedTextColor.RED));
        }
        return true;
    }
}
