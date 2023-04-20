package castles.castles.handler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import static castles.castles.Castles.teleportWarmup;
import static castles.castles.localization.Phrase.TELEPORT_CANCELLED;

public class TeleportHandler implements Listener {

    private boolean isSameLocation(Location loc1, Location loc2) {
        return loc1.getX() == loc2.getX() && loc1.getY() == loc2.getY() && loc1.getZ() == loc2.getZ();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (teleportWarmup.containsKey(player) && !isSameLocation(event.getFrom(), event.getTo())) {
            teleportWarmup.get(player).cancel();
            teleportWarmup.remove(player);
            player.sendMessage(Component.text(TELEPORT_CANCELLED.getPhrase(player), NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (teleportWarmup.containsKey(player) && !isSameLocation(event.getFrom(), event.getTo())) {
            teleportWarmup.get(player).cancel();
            teleportWarmup.remove(player);
            player.sendMessage(Component.text(TELEPORT_CANCELLED.getPhrase(player), NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (teleportWarmup.containsKey(player)){
            teleportWarmup.get(player).cancel();
            teleportWarmup.remove(player);
        }
    }
}
