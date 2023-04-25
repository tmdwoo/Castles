package castles.castles.gui;

import castles.castles.Castle;
import castles.castles.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

import static castles.castles.Utils.*;
import static castles.castles.gui.GuiUtils.createGuiItem;
import static castles.castles.localization.Phrase.formatComponent;

public class CoreGuiHandler implements Listener {

    private final int[] rampartUpgradeCosts = {16, 32, 128, 512, 1024};
    private final int[] coreUpgradeCosts = {16, 32, 128, 512, 1024};

    private ItemStack getRampartUpgradeItem(Player opener, Castle castle) {
        int level = castle.levels.get("rampart");
        int obsidian = opener.getInventory().all(Material.OBSIDIAN).values().stream().mapToInt(ItemStack::getAmount).sum();
        if (Config.getGlobal().STATE.equals("WAR")) return createGuiItem(Material.DIAMOND, 1, Component.text("Cannot Repair or Upgrade Rampart During War", NamedTextColor.RED));
        if (castle.rampartHealth < castle.getRampartMaxHealth()) return createGuiItem(Material.DIAMOND, 1, Component.text("Repair Rampart", NamedTextColor.BLUE),
                formatComponent(Component.text(String.format("Repair Cost: %d {0}", ((int) Math.round(((double) rampartUpgradeCosts[level - 1] / 2) * ((double) castle.rampartHealth / castle.getRampartMaxHealth())))), NamedTextColor.WHITE), Component.text("Obsidian", TextColor.color(0x57299B))),
                formatComponent(Component.text(String.format("You have %d {0}", obsidian), NamedTextColor.GRAY), Component.text("Obsidian", TextColor.color(0x57299B))));
        if (level == 5) return createGuiItem(Material.DIAMOND, 1, Component.text("Max Level Reached", NamedTextColor.RED));
        return createGuiItem(Material.DIAMOND, 1, Component.text("Upgrade Rampart", NamedTextColor.GREEN),
                formatComponent(Component.text(String.format("Upgrade Cost: %d {0}", rampartUpgradeCosts[level - 1]), NamedTextColor.WHITE), Component.text("Obsidian", TextColor.color(0x57299B))),
                formatComponent(Component.text(String.format("You have %d {0}", obsidian), NamedTextColor.GRAY), Component.text("Obsidian", TextColor.color(0x57299B))));
    }

    private ItemStack getCoreUpgradeItem(Player opener, Castle castle) {
        int level = castle.levels.get("core");
        int diamond = opener.getInventory().all(Material.DIAMOND).values().stream().mapToInt(ItemStack::getAmount).sum();
        if (Config.getGlobal().STATE.equals("WAR")) return createGuiItem(Material.BLAZE_SPAWN_EGG, 1, Component.text("Cannot Upgrade Core During War", NamedTextColor.RED));
        if (level == 5) return createGuiItem(Material.BLAZE_SPAWN_EGG, 1, Component.text("Max Level Reached", NamedTextColor.RED));
        return createGuiItem(Material.BLAZE_SPAWN_EGG, 1, Component.text("Upgrade Core", NamedTextColor.GREEN),
                formatComponent(Component.text(String.format("Upgrade Cost: %d {0}", coreUpgradeCosts[level - 1]), NamedTextColor.WHITE), Component.text("Diamond", NamedTextColor.AQUA)),
                formatComponent(Component.text(String.format("You have %d {0}", diamond), NamedTextColor.GRAY), Component.text("Diamond", NamedTextColor.AQUA)));
    }

    private ItemStack getExpandCastleItem(Castle castle) {
        ChunkPos mainChunk = castle.chunks.get(0);
        for (int i = -4; i < 5; i++) for (int j = -4; j < 5; j++) {
            if (i == 0 && j == 0) continue;
            ChunkPos chunk = new ChunkPos(mainChunk.getWorld(), mainChunk.getX() + i, mainChunk.getZ() + j);
            if (castle.chunks.contains(chunk)) continue;
            else return createGuiItem(Material.EMERALD, 1, Component.text("Expand Castle", NamedTextColor.GREEN));
        }
        return createGuiItem(Material.EMERALD, 1, Component.text("Max Castle Size Reached", NamedTextColor.RED));
    }

    private ItemStack getCastleStatsItem(Castle castle) {
        return createGuiItem(Material.REDSTONE_TORCH, 0,
                Component.text("| Castle Stats |", NamedTextColor.GOLD),
                Component.text("Rampart Health: ", NamedTextColor.GRAY), Component.text(String.format("%d / %d", castle.rampartHealth, castle.getRampartMaxHealth()), NamedTextColor.WHITE),
                Component.text("Core Health: ", NamedTextColor.GRAY), Component.text(String.format("%d / %d", (int) castle.coreHealth, (int) castle.getCoreMaxHealth()), NamedTextColor.WHITE),
                Component.text("Rampart Level: ", NamedTextColor.GRAY), Component.text(String.valueOf(castle.levels.get("rampart")), NamedTextColor.WHITE),
                Component.text("Core Level: ", NamedTextColor.GRAY), Component.text(String.valueOf(castle.levels.get("core")), NamedTextColor.WHITE),
                Component.text("Castle Size: ", NamedTextColor.GRAY), Component.text(String.valueOf(castle.chunks.size()), NamedTextColor.WHITE)
        );
    }

    private Inventory getCoreGui(Player opener, Castle castle) {
        Inventory inv = Bukkit.createInventory(null, 9, castle.getComponent());

        inv.setItem(1, getRampartUpgradeItem(opener, castle));
        inv.setItem(3, getCoreUpgradeItem(opener, castle));
        inv.setItem(5, getExpandCastleItem(castle));
        inv.setItem(7, getCastleStatsItem(castle));
        return inv;
    }

    private Inventory getExpandCastleGui(Player opener, Castle castle) {
        Inventory inv = Bukkit.createInventory(null, 81, castle.getComponent().append(Component.text(" Expand", NamedTextColor.DARK_GRAY)));
        ChunkPos mainChunk = castle.chunks.get(0);
        for (int i = -4; i < 5; i++) for (int j = -4; j < 5; j++) {
            ChunkPos chunk = new ChunkPos(mainChunk.getWorld(), mainChunk.getX() + i, mainChunk.getZ() + j);
            if (castle.chunks.contains(chunk)) inv.setItem((i + 4) * 9 + (j + 4), createGuiItem(Material.WHITE_STAINED_GLASS_PANE, 0, castle.getComponent(opener)));
            else {
                boolean adjacent = false;
                for (ChunkPos adj : chunk.getAdjacent()) {
                    if (adj.getX() >= mainChunk.getX() - 4 && adj.getX() <= mainChunk.getX() + 4 && adj.getZ() >= mainChunk.getZ() - 4 && adj.getZ() <= mainChunk.getZ() + 4) {
                        if (castle.chunks.contains(adj)) {
                            adjacent = true;
                            break;
                        }
                    }
                }
                if (adjacent) inv.setItem((i + 4) * 9 + (j + 4), createGuiItem(Material.LIME_STAINED_GLASS_PANE, 0,Component.text("Expand Castle", NamedTextColor.GREEN),
                        formatComponent(Component.text("Cost: 10 {0}", NamedTextColor.GRAY), Component.text("Blood Point", NamedTextColor.RED))));
                else inv.setItem((i + 4) * 9 + (j + 4), createGuiItem(Material.RED_STAINED_GLASS_PANE, 0, Component.text("Cannot Expand Here", NamedTextColor.RED),
                        Component.text("Must be adjacent to existing castle", NamedTextColor.GRAY)));
            }
        }
        return inv;
    }

    @EventHandler
    public void onCoreRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getPersistentDataContainer().has(castlesKey)) {
            Entity core = event.getRightClicked();
            Castle castle = getCastleByName(core.getPersistentDataContainer().get(castlesKey, PersistentDataType.STRING));
            Player player = event.getPlayer();
            if (!player.hasPermission("castles.openGUI")) return;
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
            if (team == null || !Objects.equals(team, castle.getOwner())) return;
            player.openInventory(getCoreGui(player, castle));
        }
    }

    @EventHandler
    public void onClickCoreGui(InventoryClickEvent event) {
        Castle castle = getCastleByComponent(event.getView().title());
        if (castle == null) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        if (event.getSlot() == 1) {
            int level = castle.levels.get("rampart");
            int obsidian = player.getInventory().all(Material.OBSIDIAN).values().stream().mapToInt(ItemStack::getAmount).sum();
            if (Config.getGlobal().STATE.equals("WAR")) {
                player.sendMessage(Component.text("Cannot Repair or Upgrade Rampart During War", NamedTextColor.RED));
                event.getInventory().close();
                return;
            }
            if (castle.rampartHealth < castle.getRampartMaxHealth()) {
                if (obsidian < ((int) Math.round(((double) rampartUpgradeCosts[level - 1] / 2) * ((double) castle.rampartHealth / castle.getRampartMaxHealth())))) {
                    player.sendMessage(Component.text("You do not have enough obsidian to repair the rampart", NamedTextColor.RED));
                    event.getInventory().close();
                    return;
                }
                player.getInventory().removeItem(new ItemStack(Material.OBSIDIAN, (int) Math.round(((double) rampartUpgradeCosts[level - 1] / 2) * ((double) castle.rampartHealth / castle.getRampartMaxHealth()))));
                castle.setRampartHealth(castle.getRampartMaxHealth());
                player.sendMessage(Component.text("Rampart Repaired", NamedTextColor.GREEN));
                event.getInventory().setItem(1, getRampartUpgradeItem(player, castle));
                event.getInventory().setItem(7, getCastleStatsItem(castle));
                return;
            }
            if (level == 5) {
                player.sendMessage(Component.text("Max Level Reached", NamedTextColor.RED));
                event.getInventory().close();
                return;
            }
            if (obsidian < rampartUpgradeCosts[level]) {
                player.sendMessage(Component.text("You do not have enough obsidian to upgrade the rampart", NamedTextColor.RED));
                event.getInventory().close();
                return;
            }
            player.getInventory().removeItem(new ItemStack(Material.OBSIDIAN, rampartUpgradeCosts[level]));
            castle.setRampartLevel(level + 1);
            player.sendMessage(Component.text("Rampart Upgraded", NamedTextColor.GREEN));
            event.getInventory().setItem(1, getRampartUpgradeItem(player, castle));
            event.getInventory().setItem(7, getCastleStatsItem(castle));
            return;
        }
        if (event.getSlot() == 3) {
            int level = castle.levels.get("core");
            int diamond = player.getInventory().all(Material.DIAMOND).values().stream().mapToInt(ItemStack::getAmount).sum();
            if (Config.getGlobal().STATE.equals("WAR")) {
                player.sendMessage(Component.text("Cannot Upgrade Core During War", NamedTextColor.RED));
                event.getInventory().close();
                return;
            }
            if (level == 5) {
                player.sendMessage(Component.text("Max Level Reached", NamedTextColor.RED));
                event.getInventory().close();
                return;
            }
            if (diamond < coreUpgradeCosts[level]) {
                player.sendMessage(Component.text("You do not have enough diamonds to upgrade the core", NamedTextColor.RED));
                event.getInventory().close();
                return;
            }
            player.getInventory().removeItem(new ItemStack(Material.DIAMOND, coreUpgradeCosts[level]));
            castle.setCoreLevel(level + 1);
            player.sendMessage(Component.text("Core Upgraded", NamedTextColor.GREEN));
            event.getInventory().setItem(3, getCoreUpgradeItem(player, castle));
            event.getInventory().setItem(7, getCastleStatsItem(castle));
            return;
        }
        if (event.getSlot() == 5) {
            ChunkPos mainChunk = castle.chunks.get(0);
            for (int i = -4; i < 5; i++) for (int j = -4; j < 5; j++) {
                if (i == 0 && j == 0) continue;
                ChunkPos chunk = new ChunkPos(mainChunk.getWorld(), mainChunk.getX() + i, mainChunk.getZ() + j);
                if (castle.chunks.contains(chunk)) continue;
                else {
                    player.openInventory(getExpandCastleGui(player, castle));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onClickExpandCastleGui(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title =((TextComponent) event.getView().title()).content();
        if (!title.endsWith("Expand") || title.split(" ").length != 2) return;
        Castle castle = getCastleByName(title.split(" ")[0]);
        if (castle == null) return;
        event.setCancelled(true);
        int slot = event.getSlot();
        int chunkX = slot % 9 - 4, chunkZ = slot / 9 - 4;
        ItemStack item = event.getCurrentItem();
        if (item.getType() != Material.LIME_STAINED_GLASS_PANE) return;
        ChunkPos chunk = new ChunkPos(castle.chunks.get(0).getWorld(), castle.chunks.get(0).getX() + chunkX, castle.chunks.get(0).getZ() + chunkZ);
        if (castle.chunks.contains(chunk)) {
            player.sendMessage(Component.text("Cannot Expand Castle Here", NamedTextColor.RED));
            player.closeInventory();
            return;
        }
        for (ChunkPos c : chunk.getAdjacent()) {
            if (castle.chunks.contains(c)) {
                player.sendMessage(Component.text("Cannot Expand Castle Here", NamedTextColor.RED));
                player.closeInventory();
                return;
            }
        }
        castle.expand(chunk);
        player.sendMessage(Component.text("Castle Expanded", NamedTextColor.GREEN));
        return;
    }
}
