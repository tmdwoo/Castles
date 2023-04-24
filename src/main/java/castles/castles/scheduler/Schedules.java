package castles.castles.scheduler;

import castles.castles.Castle;
import castles.castles.Castles;
import castles.castles.config.Config;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static castles.castles.Castles.killerVictims;
import static castles.castles.Castles.plugin;
import static castles.castles.Utils.*;
import static castles.castles.localization.Phrase.*;
import static castles.castles.scheduler.CorePattern.registerCorePattern;

public class Schedules {
    public static NamespacedKey cooldownKey = new NamespacedKey(plugin, "COOLDOWN");
    private static String nextState;
    private static LocalDateTime nextTime;
    private static LocalDateTime nextMidnight;
    public static Map<Castle, BukkitTask[]> corePatterns = new HashMap<>();


    private static void teleportCooldown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getPersistentDataContainer().has(cooldownKey, PersistentDataType.INTEGER)) {
                int cooldown = player.getPersistentDataContainer().get(cooldownKey, PersistentDataType.INTEGER);
                if (cooldown > 0) {
                    player.getPersistentDataContainer().set(cooldownKey, PersistentDataType.INTEGER, cooldown - 1);
                }
            } else {
                player.getPersistentDataContainer().set(cooldownKey, PersistentDataType.INTEGER, 0);
            }
        }
    }

    private static int getStateHour(double hour) {
        return (int) hour;
    }

    private static int getStateMinute(double hour) {
        return (int) (hour * 60 % 60);
    }

    private static int getStateSecond(double hour) {
        return (int) (hour * 3600 % 60);
    }

    private static void setNextState() {
        setNextState(LocalDateTime.now());
    }

    private static void setNextState(LocalDateTime standard) {
        double peacefulTime = Config.getGlobal().PEACEFUL_TIME;
        double preparationTime = Config.getGlobal().PREPARATION_TIME;
        double warTime = Config.getGlobal().WAR_TIME;
        LocalDateTime peaceful = LocalDateTime.of(standard.getYear(), standard.getMonth(), standard.getDayOfMonth(), getStateHour(peacefulTime), getStateMinute(peacefulTime), getStateSecond(peacefulTime));
        LocalDateTime preparation = LocalDateTime.of(standard.getYear(), standard.getMonth(), standard.getDayOfMonth(), getStateHour(preparationTime), getStateMinute(preparationTime), getStateSecond(preparationTime));
        LocalDateTime war = LocalDateTime.of(standard.getYear(), standard.getMonth(), standard.getDayOfMonth(), getStateHour(warTime), getStateMinute(warTime), getStateSecond(warTime));
        peaceful = peaceful.isBefore(standard) ? peaceful.plusDays(1) : peaceful;
        preparation = preparation.isBefore(standard) ? preparation.plusDays(1) : preparation;
        war = war.isBefore(standard) ? war.plusDays(1) : war;

        Duration durationToPeaceful = Duration.between(standard, peaceful);
        Duration durationToPreparation = Duration.between(standard, preparation);
        Duration durationToWar = Duration.between(standard, war);

        if (durationToPeaceful.compareTo(durationToPreparation) <= 0 && durationToPeaceful.compareTo(durationToWar) <= 0) {
            nextState = "PEACEFUL";
            nextTime = peaceful;
        } else if (durationToPreparation.compareTo(durationToPeaceful) <= 0 && durationToPreparation.compareTo(durationToWar) <= 0) {
            nextState = "PREPARATION";
            nextTime = preparation;
        } else {
            nextState = "WAR";
            nextTime = war;
        }
    }

    private static void updateState() {
        LocalDateTime now = LocalDateTime.now();
        if (nextTime.isBefore(now)) {
            Config.getGlobal().STATE = nextState;
            Config.getGlobal().setValue("STATE", nextState);
            try {
                Config.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(formatComponent(Component.text(STATE_CHANGED.getPhrase(player)), Component.text(stringToPhrase(nextState).getPhrase(player), getStateColor(nextState))));
            }
            setNextState(now);
        }
    }

    private static void updateCastles() {
        for (Castle c : Castles.castles) {
            c.update();
        }
    } 

    private static void midnightUpdate() {
        LocalDateTime now = LocalDateTime.now();
        if (nextMidnight == null) {
            nextMidnight = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0).plusDays(1);
        }
        if (nextMidnight.isBefore(now)) {
            killerVictims.clear();
            saveVictims();
            for (Castle castle : Castles.castles) {
                Team owner = castle.getOwner();
                if (owner != null) {
                    addScore(owner, Config.getGlobal().BP_PER_CASTLE);
                }
            }
            nextMidnight = nextMidnight.plusDays(1);
        }
    }

    public static void run() {
        setNextState();
        for (Castle castle : Castles.castles) {
            corePatterns.put(castle, registerCorePattern(castle));
        }
        Scheduler.scheduleSyncRepeatingTask(() -> {
            teleportCooldown();
            updateState();
            updateCastles();
            midnightUpdate();
        }, 0, 20, 0);
    }
}
