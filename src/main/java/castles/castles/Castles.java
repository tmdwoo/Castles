package castles.castles;

import castles.castles.command.BloodPointsCommand;
import castles.castles.command.CastlesCommand;
import castles.castles.config.Config;
import castles.castles.gui.CoreGuiHandler;
import castles.castles.handler.*;
import castles.castles.item.ItemHandler;
import castles.castles.scheduler.Schedules;
import castles.castles.tabcompletion.BloodPointsTabCompletion;
import castles.castles.tabcompletion.CastlesTabCompletion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static castles.castles.Utils.*;

public final class Castles extends JavaPlugin {
    public static Castles plugin;
    public static List<Castle> castles;
    public static HashMap<Team, String> teamToEntry = new HashMap<>();
    public static Map<UUID, List<UUID>> killerVictims;
    public static HashMap<Player, BukkitTask> teleportWarmup = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new CoreAttackHandler(), this);
        getServer().getPluginManager().registerEvents(new CastleRampartHandler(), this);
        getServer().getPluginManager().registerEvents(new CastleChunkHandler(), this);
        getServer().getPluginManager().registerEvents(new CastleProtectionHandler(), this);
        getServer().getPluginManager().registerEvents(new CastleMiscellaneousHandler(), this);
        getServer().getPluginManager().registerEvents(new BloodPointHandler(), this);
        getServer().getPluginManager().registerEvents(new TeleportHandler(), this);
        getServer().getPluginManager().registerEvents(new CoreGuiHandler(), this);
        getServer().getPluginManager().registerEvents(new ItemHandler(), this);
        this.getCommand("castles").setExecutor(new CastlesCommand());
        this.getCommand("bloodpoints").setExecutor(new BloodPointsCommand());
        this.getCommand("castles").setTabCompleter(new CastlesTabCompletion());
        this.getCommand("bloodpoints").setTabCompleter(new BloodPointsTabCompletion());
        loadTeamEntry();
        loadVictims();
        loadCastles();
        try {
            Config.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getLogger().info("Castles has been enabled!");
        Schedules.run();
    }

    @Override
    public void onDisable() {
        saveCastles();
        saveVictims();
        try {
            Config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getLogger().info("Castles has been disabled!");
    }
}
