package castles.castles.gui;

import castles.castles.Castle;
import castles.castles.config.Config;
import castles.castles.scheduler.Scheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static castles.castles.Utils.*;
import static castles.castles.gui.GuiUtils.createGuiItem;
import static castles.castles.item.ItemHandler.returnCore;
import static castles.castles.localization.Phrase.*;

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

    private ItemStack getExpandCastleItem() {
        return createGuiItem(Material.EMERALD, 1, Component.text("Expand Castle", NamedTextColor.GREEN));
    }

    private ItemStack getCastleDestroyItem() {
        return createGuiItem(Material.TNT, 0, Component.text("Destroy Castle", NamedTextColor.RED),
                formatComponent(Component.text("Cost: 30 {0}", NamedTextColor.WHITE), Component.text("Blood Point", NamedTextColor.RED)));
    }

    private Inventory getCoreGui(Player opener, Castle castle) {
        Inventory inv = Bukkit.createInventory(null, 9, castle.getComponent());

        inv.setItem(1, getRampartUpgradeItem(opener, castle));
        inv.setItem(3, getCoreUpgradeItem(opener, castle));
        inv.setItem(5, getExpandCastleItem());
        inv.setItem(7, getCastleDestroyItem());
        return inv;
    }

    private Inventory getExpandCastleGui(Player opener, Castle castle) {
        return getExpandCastleGui(opener, castle, castle.chunks.get(0).getX(), castle.chunks.get(0).getZ());
    }

    private Inventory getExpandCastleGui(Player opener, Castle castle, int x, int z) {
        Inventory inv = Bukkit.createInventory(null, 45, castle.getComponent().append(Component.text(" Expand", NamedTextColor.DARK_GRAY)));
        ChunkPos mainChunk = new ChunkPos(castle.chunks.get(0).getWorld(), x, z);
        for (int i = -2; i < 3; i++) for (int j = -4; j < 5; j++) {
            if (i == -2 && j == 0) {
                inv.setItem(4, createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, 1,
                        Component.text("⬆", NamedTextColor.WHITE)));
            } else if (i == 0 && j == 4) {
                inv.setItem(26, createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, 2,
                        Component.text("➡", NamedTextColor.WHITE)));
            } else if (i == 2 && j == 0) {
                inv.setItem(40, createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, 3,
                        Component.text("⬇", NamedTextColor.WHITE)));
            } else if (i == 0 && j == -4) {
                inv.setItem(18, createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, 4,
                        Component.text("⬅", NamedTextColor.WHITE)));
            } else if (i == -2 || i == 2 || j == -4 || j == 4) {
                inv.setItem((i + 2) * 9 + (j + 4), createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, 0,
                        Component.text("", NamedTextColor.WHITE)));
            } else {
                ChunkPos chunk = new ChunkPos(mainChunk.getWorld(), x + j, z + i);
                if (castle.chunks.contains(chunk)) {
                    inv.setItem((i + 2) * 9 + (j + 4), createGuiItem(Material.WHITE_STAINED_GLASS_PANE, 0,
                            castle.getComponent(opener),
                            Component.text(String.format("Chunk %d, %d", chunk.getX(), chunk.getZ()), NamedTextColor.GRAY),
                            Component.text("Claimed Chunk", NamedTextColor.GRAY)));
                }
                else if (getCastleByChunk(chunk) != null) {
                    Castle other = getCastleByChunk(chunk);
                    inv.setItem((i + 2) * 9 + (j + 4), createGuiItem(Material.RED_STAINED_GLASS_PANE, 0,
                            other.getComponent(opener),
                            Component.text(String.format("Chunk %d, %d", chunk.getX(), chunk.getZ()), NamedTextColor.GRAY),
                            Component.text("Already claimed by ", NamedTextColor.GRAY).append(other.getComponent(opener))));
                }
                else {
                    boolean adjacent = false;
                    for (ChunkPos adj : chunk.getAdjacent()) {
                        if (adj.getX() >= mainChunk.getX() - 4 && adj.getX() <= mainChunk.getX() + 4 && adj.getZ() >= mainChunk.getZ() - 1 && adj.getZ() <= mainChunk.getZ() + 1) {
                            if (castle.chunks.contains(adj)) {
                                adjacent = true;
                                break;
                            }
                        }
                    }
                    if (adjacent)
                        inv.setItem((i + 2) * 9 + (j + 4), createGuiItem(Material.LIME_STAINED_GLASS_PANE, 0,
                                Component.text("Expand Castle", NamedTextColor.GREEN),
                                Component.text(String.format("Chunk %d, %d", chunk.getX(), chunk.getZ()), NamedTextColor.GRAY),
                                formatComponent(Component.text("Cost: 20 {0}", NamedTextColor.WHITE), Component.text("Blood Point", NamedTextColor.RED))));
                    else
                        inv.setItem((i + 2) * 9 + (j + 4), createGuiItem(Material.RED_STAINED_GLASS_PANE, 0,
                                Component.text("Cannot Expand Here", NamedTextColor.RED),
                                Component.text(String.format("Chunk %d, %d", chunk.getX(), chunk.getZ()), NamedTextColor.GRAY),
                                Component.text("Must be adjacent to this castle", NamedTextColor.GRAY)));
                }
            }
        }
        return inv;
    }

    @EventHandler
    public void onCoreRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getPersistentDataContainer().has(castlesKey)) {
            Entity core = event.getRightClicked();
            Castle castle = getCastleByName(core.getPersistentDataContainer().get(castlesKey, PersistentDataType.STRING));
            if (castle == null) return;
            Player player = event.getPlayer();
            if (!player.hasPermission("castles.GUI")) return;
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
            if (team == null || !Objects.equals(team, castle.getOwner())) return;
            player.openInventory(getCoreGui(player, castle));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickCoreGui(InventoryClickEvent event) {
        Castle castle = getCastleByName(ChatColor.stripColor(event.getView().getTitle()));
        if (castle == null) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        if (slot == 1) {
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
            return;
        }
        if (slot == 3) {
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
            return;
        }
        if (slot == 5) {
            if (player.hasPermission("castles.GUI.expand")) player.openInventory(getExpandCastleGui(player, castle));
            else {
                player.sendMessage(Component.text("You do not have permission to expand castle", NamedTextColor.RED));
                return;
            }
        }
        if (slot == 7) {
            if (!player.hasPermission("castles.GUI.destroy")) {
                player.sendMessage(Component.text("You do not have permission to destroy castle", NamedTextColor.RED));
                return;
            }
            if (Config.getGlobal().STATE.equals("WAR")) {
                player.sendMessage(Component.text("Cannot Destroy Castle During War", NamedTextColor.RED));
                event.getInventory().close();
                return;
            }
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
            Team owner = castle.getOwner();
            if (owner == null || !owner.equals(team)) {
                player.sendMessage(Component.text(GUI_CASTLE_DESTROY_OWNER_ONLY.getPhrase(player), NamedTextColor.RED));
                return;
            }
            if (getScore(team) < 30) {
                player.sendMessage(Component.text(BP_NOT_ENOUGH.getPhrase(player), NamedTextColor.RED));
                return;
            }
            if (isPlayerInChatInteraction(player)) {
                return;
            }
            player.sendMessage(formatComponent(Component.text("Type {0} in chat in 15 seconds", NamedTextColor.GRAY), castle.getComponent()));
            BukkitTask destroyCastle = Scheduler.scheduleSyncDelayedTask(() -> {
                player.sendMessage(Component.text("Destroying castle cancelled due to timeout", NamedTextColor.RED));
                player.getPersistentDataContainer().remove(destroyCastleKey)    ;
            }, 20 * 15);
            String key = String.join("\"", String.valueOf(destroyCastle.getTaskId()), castle.name);
            player.getPersistentDataContainer().set(destroyCastleKey, PersistentDataType.STRING, key);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConfirmDestroy(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!player.getPersistentDataContainer().has(destroyCastleKey, PersistentDataType.STRING)) return;
        String[] key = player.getPersistentDataContainer().get(destroyCastleKey, PersistentDataType.STRING).split("\"");
        player.getPersistentDataContainer().remove(destroyCastleKey);
        if (key.length != 2) return;
        int taskId = Integer.parseInt(key[0]);
        BukkitTask destroyCastle = Bukkit.getScheduler().getPendingTasks().stream().filter(task -> task.getTaskId() == taskId).findFirst().orElse(null);
        if (destroyCastle == null) return;
        destroyCastle.cancel();
        event.setCancelled(true);
        String messageCastle = event.getMessage();
        String castleName = key[1];
        if (!messageCastle.equals(castleName)) {
            player.sendMessage(Component.text(GUI_CASTLE_DESTROY_NAME_NOT_MATCH.getPhrase(player), NamedTextColor.RED));
            return;
        }
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        Castle castle = getCastleByName(castleName);
        if (castle == null) return;
        Team owner = castle.getOwner();
        if (owner == null || !owner.equals(team)) {
            player.sendMessage(Component.text(GUI_CASTLE_DESTROY_OWNER_ONLY.getPhrase(player), NamedTextColor.RED));
            return;
        }
        if (getScore(team) < 30) {
            player.sendMessage(Component.text(BP_NOT_ENOUGH.getPhrase(player), NamedTextColor.RED));
            return;
        }
        player.sendMessage(formatComponent(Component.text(GUI_CASTLE_DESTROY.getPhrase(player)), castle.getComponent(player)));
        Scheduler.scheduleSyncDelayedTask(() -> {
            castle.destroy();
            setScore(castle.getOwner(), getScore(castle.getOwner()) - 30);
            returnCore(player);
            player.closeInventory();
            player.playSound(castle.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
        }, 0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickExpandCastleGui(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        List<String> title = Arrays.asList(ChatColor.stripColor(event.getView().getTitle()).split(" "));
        if (title.size() == 1 || !title .get(title.size() - 1).equals("Expand")) return;
        String castleName = String.join(" ", title.subList(0, title.size() - 1));
        Castle castle = getCastleByName(castleName);
        if (castle == null) return;
        event.setCancelled(true);
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        Team owner = castle.getOwner();
        if (owner == null || !owner.equals(team)) {
            player.sendMessage(Component.text(GUI_CASTLE_EXPAND_OWNER_ONLY.getPhrase(player), NamedTextColor.RED));
            return;
        }
        int slot = event.getSlot();
        ItemStack item = event.getCurrentItem();
        Inventory inventory = event.getInventory();
        String middleChunk = ((TextComponent) inventory.getItem(22).getItemMeta().lore().get(0)).content();
        Pattern pattern = Pattern.compile("Chunk (-\\d+|\\d+), (-\\d+|\\d+)");
        Matcher middleMatcher = pattern.matcher(middleChunk);
        if (!middleMatcher.find()) return;
        int chunkMX = Integer.parseInt(middleMatcher.group(1));
        int chunkMZ = Integer.parseInt(middleMatcher.group(2));
        if (slot == 4) {
            player.closeInventory();
            player.openInventory(getExpandCastleGui(player, castle, chunkMX, chunkMZ - 1));
            return;
        } else if (slot == 26) {
            player.closeInventory();
            player.openInventory(getExpandCastleGui(player, castle, chunkMX + 1, chunkMZ));
            return;
        } else if (slot == 40) {
            player.closeInventory();
            player.openInventory(getExpandCastleGui(player, castle, chunkMX, chunkMZ + 1));
            return;
        } else if (slot == 18) {
            player.closeInventory();
            player.openInventory(getExpandCastleGui(player, castle, chunkMX - 1, chunkMZ));
            return;
        } else if (item.getType().equals(Material.PURPLE_STAINED_GLASS_PANE)) return;
        String chunkText = ((TextComponent) item.getItemMeta().lore().get(0)).content();
        Matcher matcher = pattern.matcher(chunkText);
        if (!matcher.find()) return;
        int chunkX = Integer.parseInt(matcher.group(1));
        int chunkZ = Integer.parseInt(matcher.group(2));
        ChunkPos chunk = new ChunkPos(castle.chunks.get(0).getWorld(), chunkX, chunkZ);
        if (getScore(owner) < 20) {
            player.sendMessage(Component.text(BP_NOT_ENOUGH.getPhrase(player), NamedTextColor.RED));
            player.closeInventory();
            return;
        }
        if (castle.chunks.contains(chunk)) {
            player.sendMessage(Component.text(ALREADY_PART_OF_THE_CASTLE.getPhrase(player), NamedTextColor.RED));
            return;
        }
        if (getCastleByChunk(chunk) != null) {
            player.sendMessage(Component.text(ALREADY_PART_OF_ANOTHER_CASTLE.getPhrase(player), NamedTextColor.RED));
            return;
        }
        for (ChunkPos c : chunk.getAdjacent()) {
            if (castle.chunks.contains(c)) {
                setScore(castle.getOwner(), getScore(owner) - 20);
                castle.expand(chunk);
                player.sendMessage(formatComponent(Component.text(String.format(CASTLES_EXPAND.getPhrase(player), chunk.getX(), chunk.getZ())), castle.getComponent(player)));
                player.closeInventory();
                player.openInventory(getExpandCastleGui(player, castle, chunkMX, chunkMZ));
                return;
            }
        }
        player.sendMessage(Component.text(NOT_ADJACENT.getPhrase(player), NamedTextColor.RED));
        player.closeInventory();
    }
}
