package castles.castles.handler;

import castles.castles.Castle;
import castles.castles.config.Config;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static castles.castles.Utils.getCastleByLocation;

public class CastleRampartHandler implements Listener {
    Castle getCastleByRampart(Block block) {
        Castle castle = getCastleByLocation(block.getLocation());
        if (castle == null) {
            return null;
        }
        if (castle.rampart.containsKey(block.getLocation().serialize())) {
            return castle;
        }
        return null;
    }

    boolean isCastleRampartDamageable(@Nullable Castle castle) {
        if (castle == null) {
            return false;
        }
        Team t = castle.getOwner();
        if (t == null) {
            return !Objects.equals(Config.getGlobal().STATE, "PEACEFUL");
        } else {
            return !(Objects.equals(Config.getGlobal().STATE, "PEACEFUL") || Objects.equals(Config.getGlobal().STATE, "PREPARATION") || (Objects.equals(Config.getGlobal().STATE, "WAR") && castle.protectionTime > 0));
        }
    }

    @EventHandler()
    public void onBlockBreak(BlockBreakEvent event){
        Castle castle = getCastleByRampart(event.getBlock());
        if (castle == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (isCastleRampartDamageable(castle) && (castle.getOwner() == null || !castle.getOwner().hasPlayer(player))) {
            castle.damageRampart(player);
        }
    }

    @EventHandler()
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> protectedBlocks = new ArrayList<>();
        for (Block b : event.blockList()){
            Castle castle = getCastleByRampart(b);
            if (castle == null) {
                continue;
            }
            if (isCastleRampartDamageable(castle)) {
                castle.damageRampart();
                protectedBlocks.add(b);
            }
        }
        event.blockList().removeAll(protectedBlocks);
    }

    @EventHandler()
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> protectedBlocks = new ArrayList<>();
        for (Block b : event.blockList()){
            Castle castle = getCastleByRampart(b);
            if (castle == null) {
                continue;
            }
            if (isCastleRampartDamageable(castle)) {
                castle.damageRampart();
                protectedBlocks.add(b);
            }
        }
        event.blockList().removeAll(protectedBlocks);
    }
}
