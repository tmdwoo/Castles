package castles.castles.handler;

import castles.castles.Castle;
import castles.castles.Castles;
import castles.castles.scheduler.Scheduler;
import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

import static castles.castles.Utils.getCastleByLocation;
import static castles.castles.Utils.getNearestTeamCastle;

public class CastleMiscellaneousHandler implements Listener {
    // boss bar
    void updateBossBar(Castle fromCastle, Castle toCastle, Player player) {
        if (toCastle != null && fromCastle != null) {
            if (toCastle != fromCastle) {
                toCastle.getBossBar().addPlayer(player);
                fromCastle.getBossBar().removePlayer(player);
            }
        } else if (toCastle != null) {
            toCastle.getBossBar().addPlayer(player);
        } else if (fromCastle != null) {
            fromCastle.getBossBar().removePlayer(player);
        }
    }

    void updateSpawnPoint(Castle fromCastle, Castle toCastle, Player player) {
        if (toCastle == null) return;
        if (Objects.equals(fromCastle, toCastle)) return;
        Team team = player.getScoreboard().getPlayerTeam(player);
        if (team == null) return;
        if (Objects.equals(team, toCastle.getOwner())) {
            player.setBedSpawnLocation(toCastle.getLocation());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.isCancelled()) {
            Castle toCastle = getCastleByLocation(event.getTo());
            Castle fromCastle = getCastleByLocation(event.getFrom());
            updateBossBar(fromCastle, toCastle, event.getPlayer());
            updateSpawnPoint(fromCastle, toCastle, event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        if (event.getCause().equals(PlayerSetSpawnEvent.Cause.BED) || event.getCause().equals(PlayerSetSpawnEvent.Cause.RESPAWN_ANCHOR) || event.getCause().equals(PlayerSetSpawnEvent.Cause.PLAYER_RESPAWN)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.isCancelled()) {
            Castle toCastle = getCastleByLocation(event.getTo());
            Castle fromCastle = getCastleByLocation(event.getFrom());
            updateBossBar(fromCastle, toCastle, event.getPlayer());
            updateSpawnPoint(fromCastle, toCastle, event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Castle toCastle = getCastleByLocation(event.getPlayer().getLocation());
        updateBossBar(null, toCastle, event.getPlayer());
        Castle spawnCastle = getCastleByLocation(event.getPlayer().getBedSpawnLocation());
        Team team = event.getPlayer().getScoreboard().getPlayerTeam(event.getPlayer());
        if (team != null && (spawnCastle == null || !Objects.equals(team, spawnCastle.getOwner()))) {
            Castle nearestCastle = getNearestTeamCastle(event.getPlayer());
            event.getPlayer().setBedSpawnLocation(nearestCastle == null ? null : nearestCastle.getLocation(), true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Castle fromCastle = getCastleByLocation(event.getPlayer().getLocation());
        updateBossBar(fromCastle, null, event.getPlayer());
    }

    @EventHandler
    public void onTeamChangeConsole(ServerCommandEvent event) {
        String[] args = event.getCommand().split(" ");
        if (args[0].equals("team") && args.length >= 3) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(args[2]);
            if (args.length == 5 && args[1].equals("modify")) {
                if (team == null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    for (Castle castle : Castles.castles) {
                        castle.setOwner(castle.getOwner());
                    }
                }, 1);
            } else if (args.length == 3 && args[1].equals("remove")) {
                if (team == null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    for (Castle castle : Castles.castles) {
                        castle.setOwner(castle.getOwner());
                    }
                }, 1);
            } else if ((args.length == 3 || args.length == 4) && args[1].equals("add")) {
                if (team != null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    for (Castle castle : Castles.castles) {
                        castle.setOwner(castle.getOwner());
                    }
                }, 1);
            }
        }
    }

    @EventHandler
    public void onTeamChangePlayer(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args[0].equals("/team") && args.length >= 3) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(args[2]);
            if (args.length == 5 && args[1].equals("modify")) {
                if (team == null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    for (Castle castle : Castles.castles) {
                        castle.setOwner(castle.getOwner());
                    }
                }, 1);
            } else if (args.length == 3 && args[1].equals("remove")) {
                if (team == null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    for (Castle castle : Castles.castles) {
                        castle.setOwner(castle.getOwner());
                    }
                }, 1);
            } else if ((args.length == 3 || args.length == 4) && args[1].equals("add")) {
                if (team != null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    for (Castle castle : Castles.castles) {
                        castle.setOwner(castle.getOwner());
                    }
                }, 1);
            }
        }
    }

    // Show Border by particle
    @EventHandler
    public void showBorders(PlayerMoveEvent event) {
        Castle toCastle = getCastleByLocation(event.getTo());
        Castle fromCastle = getCastleByLocation(event.getFrom());
        if (toCastle != null && fromCastle != null) {
            if (toCastle != fromCastle) {
                toCastle.showBorder(event.getPlayer());
            }
        } else if (toCastle != null) {
            toCastle.showBorder(event.getPlayer());
        }
    }
}