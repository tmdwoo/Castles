package castles.castles.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;

import static castles.castles.Castles.plugin;
import static castles.castles.Utils.getTeamComponent;
import static castles.castles.localization.Phrase.formatComponent;

public class ChatHandler implements Listener {
    public static final NamespacedKey teamChatKey = new NamespacedKey(plugin, "TEAM_CHAT");
    private Component getPlayerWhisperComponent(Player player) {
        Team team = player.getScoreboard().getPlayerTeam(player);
        TextColor color = team != null ? team.hasColor() ? team.color() : NamedTextColor.WHITE : NamedTextColor.WHITE;
        ClickEvent clickEvent = ClickEvent.suggestCommand("/tell " + player.getName() + " ");
        return Component.text(player.getName(), color).clickEvent(clickEvent);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        event.setCancelled(true);
        Player player = event.getPlayer();
        Team team = player.getScoreboard().getPlayerTeam(player);
        if (team == null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(formatComponent(Component.text("<{0}> ", NamedTextColor.WHITE), player.displayName())
                        .append(event.message()));
            }
            return;
        }
        if (!player.getPersistentDataContainer().has(teamChatKey, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().set(teamChatKey, PersistentDataType.BYTE, (byte) 0);
        }
        TextColor color = team.hasColor() ? team.color() : NamedTextColor.WHITE;
        if (player.getPersistentDataContainer().get(teamChatKey, PersistentDataType.BYTE) == (byte) 1) {
            Component message = formatComponent(Component.text("[{0}] ", color), getTeamComponent(team))
                    .append(formatComponent(Component.text("<{0}> ", NamedTextColor.WHITE), player.displayName().color(color)))
                    .append(event.message());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (team.hasPlayer(p) || p.hasPermission("castles.eavesdrop.teamchat")) {
                    p.sendMessage(message);
                }
            }
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(formatComponent(Component.text("<{0}> ", NamedTextColor.WHITE), player.displayName().color(color))
                        .append(event.message()));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Team team = player.getScoreboard().getPlayerTeam(player);
        if (team != null) return;
        if (!player.getPersistentDataContainer().has(teamChatKey, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().set(teamChatKey, PersistentDataType.BYTE, (byte) 0);
        }
        if (player.getPersistentDataContainer().get(teamChatKey, PersistentDataType.BYTE) == (byte) 1) {
            player.sendMessage(Component.text("You are in team chat mode", NamedTextColor.GRAY));
        } else {
            player.sendMessage(Component.text("You are in global chat mode", NamedTextColor.GRAY));
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Team team = player.getScoreboard().getPlayerTeam(player);
        if (team == null) return;
        String command = event.getMessage();
        if (command.startsWith("/teammsg") && command.split(" ").length > 1) {
            event.setCancelled(true);
            String message = command.substring(9);
            TextColor color = team.hasColor() ? team.color() : NamedTextColor.WHITE;
            Component msg = formatComponent(Component.text("[{0}] ", color), getTeamComponent(team))
                    .append(formatComponent(Component.text("<{0}> ", NamedTextColor.WHITE), player.displayName().color(color)))
                    .append(Component.text(message));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (team.hasPlayer(p) || p.hasPermission("castles.eavesdrop.teamchat")) {
                    p.sendMessage(msg);
                }
            }
        } else if ((command.startsWith("/w") || command.startsWith("/msg") || command.startsWith("/tell")) && command.split(" ").length > 2) {
            String label = command.split(" ")[0];
            String targetName = command.split(" ")[1];
            Player target = Bukkit.getPlayerExact(targetName);
            if (target == null) return;
            String message = command.substring(label.length() + targetName.length() + 2);
            if (message.length() == 0) return;
            event.setCancelled(true);
            if (player.equals(target)) {
                player.sendMessage(formatComponent(Component.text("<{0}> ", NamedTextColor.GRAY), getPlayerWhisperComponent(player))
                        .append(Component.text(message, TextColor.color(0xD3D3D3), TextDecoration.ITALIC)));
            }
            else {
                player.sendMessage(formatComponent(Component.text("-> <{0}> ", NamedTextColor.GRAY), getPlayerWhisperComponent(target))
                        .append(Component.text(message, TextColor.color(0xD3D3D3), TextDecoration.ITALIC)));
                target.sendMessage(formatComponent(Component.text("<- <{0}> ", NamedTextColor.GRAY), getPlayerWhisperComponent(player))
                        .append(Component.text(message, TextColor.color(0xD3D3D3), TextDecoration.ITALIC)));
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("castles.eavesdrop.whisper") && !p.equals(player) && !p.equals(target)) {
                    p.sendMessage(formatComponent(Component.text("<{0}> -> <{1}> ", NamedTextColor.GRAY), getPlayerWhisperComponent(player), getPlayerWhisperComponent(target))
                            .append(Component.text(message, TextColor.color(0xD3D3D3), TextDecoration.ITALIC)));
                }
            }
        }
    }
}
