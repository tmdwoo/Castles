package castles.castles.gui;

import castles.castles.Castle;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

import static castles.castles.Utils.castlesKey;
import static castles.castles.Utils.getCastleByName;
import static castles.castles.gui.GuiUtils.createGuiItem;

public class CoreGuiHandler implements Listener {

    public Inventory CoreGui(Castle castle) {
        Inventory inv = Bukkit.createInventory(null, 9, castle.getComponent());

        inv.setItem(1, createGuiItem(Material.DIAMOND, 1, Component.text("Upgrade Rampart"), Component.text("Cost: 1 Diamond")));
        return inv;
    }

    @EventHandler
    public void onCoreRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() != null && event.getRightClicked().getPersistentDataContainer().has(castlesKey)) {
            Entity core = event.getRightClicked();
            Castle castle = getCastleByName(core.getPersistentDataContainer().get(castlesKey, PersistentDataType.STRING));
            Player player = event.getPlayer();
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
            if (team == null || !Objects.equals(team, castle.getOwner())) return;
            player.openInventory(CoreGui(castle));
        }
    }
}
