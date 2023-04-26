package castles.castles.item;

import castles.castles.Castle;
import castles.castles.Utils;
import castles.castles.config.Config;
import castles.castles.scheduler.Scheduler;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.Objects;

import static castles.castles.Castles.plugin;
import static castles.castles.Utils.*;
import static castles.castles.item.Items.getItemCore;
import static castles.castles.localization.Phrase.*;

public class ItemHandler implements Listener {
    private final NamespacedKey createCastleKey = new NamespacedKey(plugin, "createCastle");

    public void returnCore(Player player) {
        if (player.getInventory().contains(getItemCore())) {
            player.getInventory().addItem(getItemCore());
        } else if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(getItemCore());
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), getItemCore());
        }
    }

    private void createCastle(Location location, Player player) {
        /*
        TODO: create castle 함수
        CastleCommand.java에서 사용한 것과 같은 방식으로 성을 지을 수 있는지 검사
        persistent data가 has() true면 이미 성을 만들고 있으므로 에러와 함께 return
        createCastle 성 생성시 플레이어 persistent data에 int형으로 bukkit task id 저장 후 10초의 scheduler로 기다린 후 채팅이 없으면 생성 이벤트 취소
        10초 내로 player chat event가 생성될 경우 큰따옴표 여부 검사 후 없으면 생성
        */
        if (!location.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            player.sendMessage(Component.text("You can only build a castle in the overworld", NamedTextColor.RED));
            returnCore(player);
            return;
        }
        if (getCastleByLocation(location) != null) {
            player.sendMessage(Component.text(CASTLE_ALREADY_IN_CHUNK.getPhrase(player), NamedTextColor.RED));
            returnCore(player);
            return;
        }
        Utils.WorldEnv worldEnv = getWorldEnv(location.getWorld());
        if (location.getY() < worldEnv.getMinY() || location.getY() > worldEnv.getMaxY()) {
            player.sendMessage(Component.text(String.format(Y_COORD_OUT_OF_RANGE.getPhrase(player), worldEnv.getMinY(), worldEnv.getMaxY()), NamedTextColor.RED));
            returnCore(player);
            return;
        }
        if (getMod(location.getX(), 16) < 1.5 || getMod(location.getX(), 16) > 14.5 || getMod(location.getZ(), 16) < 1.5 || getMod(location.getZ(), 16) > 14.5) {
            player.sendMessage(Component.text(CORE_ON_EDGE.getPhrase(player), NamedTextColor.RED));
            returnCore(player);
            return;
        }
        if (player.getPersistentDataContainer().has(createCastleKey, PersistentDataType.STRING)) {
            player.sendMessage(Component.text("You are already creating a castle", NamedTextColor.RED));
            returnCore(player);
            return;
        }
        if (location.getX() < Config.getGlobal().MINIMUM_CASTLE_X || location.getX() > Config.getGlobal().MAXIMUM_CASTLE_X || location.getZ() < Config.getGlobal().MINIMUM_CASTLE_Z || location.getZ() > Config.getGlobal().MAXIMUM_CASTLE_Z) {
            player.sendMessage(Component.text(String.format(CASTLE_OUT_OF_RANGE.getPhrase(player), Config.getGlobal().MINIMUM_CASTLE_X, Config.getGlobal().MINIMUM_CASTLE_X, Config.getGlobal().MAXIMUM_CASTLE_X, Config.getGlobal().MAXIMUM_CASTLE_Z), NamedTextColor.RED));
            returnCore(player);
            return;
        }
        player.sendMessage(Component.text("Type the name of the castle in chat in 15 seconds", NamedTextColor.GRAY));
        BukkitTask createCastle = Scheduler.scheduleSyncDelayedTask(() -> {
            player.sendMessage(Component.text("Creating castle cancelled due to timeout", NamedTextColor.RED));
            player.getPersistentDataContainer().remove(createCastleKey);
            returnCore(player);
        }, 20 * 15);
        String key = String.join(",", String.valueOf(createCastle.getTaskId()), String.valueOf(location.getX()), String.valueOf(location.getY()), String.valueOf(location.getZ()));
        player.getPersistentDataContainer().set(createCastleKey, PersistentDataType.STRING, key);
    }

    @EventHandler
    public void onCoreUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!Objects.equals(event.getHand(), EquipmentSlot.HAND)) return;
        if (player.getInventory().getItemInMainHand().isSimilar(getItemCore())) {
            event.setCancelled(true);
            if (event.getClickedBlock() == null || !event.getClickedBlock().isSolid()) return;
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
            Location location = event.getClickedBlock().getLocation().add(event.getBlockFace().getDirection());
            createCastle(location, player);
        } else if (player.getInventory().getItemInOffHand().isSimilar(getItemCore())) {
            event.setCancelled(true);
            if (event.getClickedBlock() == null || !event.getClickedBlock().isSolid()) return;
            player.getInventory().getItemInOffHand().setAmount(player.getInventory().getItemInOffHand().getAmount() - 1);
            Location location = event.getClickedBlock().getLocation().add(event.getBlockFace().getDirection());
            createCastle(location, player);
        }
    }

    @EventHandler
    public void onNameCastle(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!player.getPersistentDataContainer().has(createCastleKey, PersistentDataType.STRING)) return;
        String[] key = player.getPersistentDataContainer().get(createCastleKey, PersistentDataType.STRING).split(",");
        player.getPersistentDataContainer().remove(createCastleKey);
        if (key.length != 4) return;
        int taskId = Integer.parseInt(key[0]);
        BukkitTask createCastle = Bukkit.getScheduler().getPendingTasks().stream().filter(task -> task.getTaskId() == taskId).findFirst().orElse(null);
        if (createCastle == null) return;
        createCastle.cancel();
        event.setCancelled(true);
        Location location = new Location(player.getWorld(), Double.parseDouble(key[1]) + 0.5, Double.parseDouble(key[2]), Double.parseDouble(key[3]) + 0.5);
        String name = ((TextComponent) event.message()).content();
        if (name.contains("\"")) {
            player.sendMessage(Component.text(NAME_DOUBLE_QUOTE.getPhrase(player), NamedTextColor.RED));
            returnCore(player);
            return;
        }
        if (getCastleByName(name) != null) {
            player.sendMessage(Component.text(NAME_DUPLICATE.getPhrase(player), NamedTextColor.RED));
            returnCore(player);
            return;
        }
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(player);
        Scheduler.scheduleSyncDelayedTask(() -> new Castle(name, location, team), 0);
        player.sendMessage(formatComponent(Component.text(CASTLES_CREATE.getPhrase(player)), getCastleByName(name).getComponent(player)));
    }
}