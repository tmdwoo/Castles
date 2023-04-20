package castles.castles.handler;

import castles.castles.Castle;
import castles.castles.Castles;
import castles.castles.scheduler.Scheduler;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.Iterator;

import static castles.castles.Utils.getCastleByLocation;

public class CastleMiscellaneousHandler implements Listener {
    // protect flag
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Castle castle = getCastleByLocation(event.getBlock().getLocation());
        if (castle != null) {
            if (castle.flags.get("wools").contains(event.getBlock().getLocation().serialize()) || castle.flags.get("fences").contains(event.getBlock().getLocation().serialize())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            Castle castle = getCastleByLocation(block.getLocation());
            if (castle != null) {
                if (castle.flags.get("wools").contains(block.getLocation().serialize()) || castle.flags.get("fences").contains(block.getLocation().serialize())) {
                    iterator.remove();
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> iterator = event.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            Castle castle = getCastleByLocation(block.getLocation());
            if (castle != null) {
                if (castle.flags.get("wools").contains(block.getLocation().serialize()) || castle.flags.get("fences").contains(block.getLocation().serialize())) {
                    iterator.remove();
                }
            }
        }
    }

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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.isCancelled()) {
            Castle toCastle = getCastleByLocation(event.getTo());
            Castle fromCastle = getCastleByLocation(event.getFrom());
            updateBossBar(fromCastle, toCastle, event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.isCancelled()) {
            Castle toCastle = getCastleByLocation(event.getTo());
            Castle fromCastle = getCastleByLocation(event.getFrom());
            updateBossBar(fromCastle, toCastle, event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Castle toCastle = getCastleByLocation(event.getPlayer().getLocation());
        updateBossBar(null, toCastle, event.getPlayer());
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
            Scheduler.scheduleSyncDelayedTask(() -> {
                for (Castle castle : Castles.castles) {
                    castle.setOwner(castle.getOwner());
                }
            }, 1);
        }
    }

    @EventHandler
    public void onTeamChangePlayer(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args[0].equals("/team") && args.length >= 3) {
            Scheduler.scheduleSyncDelayedTask(() -> {
                for (Castle castle : Castles.castles) {
                    castle.setOwner(castle.getOwner());
                }
            }, 1);
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