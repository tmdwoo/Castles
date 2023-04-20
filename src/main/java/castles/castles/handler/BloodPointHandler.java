package castles.castles.handler;

import castles.castles.config.Config;
import castles.castles.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static castles.castles.Castles.killerVictims;
import static castles.castles.Castles.teamToEntry;
import static castles.castles.Utils.*;

public class BloodPointHandler implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof Player)) return;
        Player killer = ((Player) victim).getKiller();
        if (killer == null) return;
        Team killerTeam = killer.getScoreboard().getPlayerTeam(killer);
        if (killerTeam == null) return;
        List<UUID> killerVictim = killerVictims.get(killer.getUniqueId());
        if (Collections.frequency(killerVictim, victim.getUniqueId()) >= Config.getGlobal().BP_MAX_PER_VICTIM) return;
        killerVictim.add(victim.getUniqueId());
        addScore(killerTeam, Config.getGlobal().BP_PER_KILL);
    }

    @EventHandler
    public void onTeamChangeConsole(ServerCommandEvent event) {
        String[] args = event.getCommand().split(" ");
        if (args[0].equals("team") && args.length >= 3) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(args[2]);
            if (args.length == 5 && args[1].equals("modify")) {
                if (team == null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    String newEntry = getDisplayName(team);
                    String oldEntry = teamToEntry.get(team);
                    if (newEntry.equals(oldEntry)) return;
                    int score = getBloodPointsObjective().getScore(oldEntry).getScore();
                    getBloodPointsObjective().getScore(newEntry).setScore(score);
                    getBloodPointsObjective().getScore(oldEntry).resetScore();
                    teamToEntry.put(team, newEntry);
                }, 1);
            } else if (args.length == 3 && args[1].equals("remove")) {
                if (team == null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    String entry = teamToEntry.get(team);
                    getBloodPointsObjective().getScore(entry).resetScore();
                    teamToEntry.remove(team);
                }, 1);
            } else if ((args.length == 3 || args.length == 4) && args[1].equals("add")) {
                if (team != null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    Team newTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(args[2]);
                    if (newTeam == null) return;
                    String entry = getDisplayName(newTeam);
                    getBloodPointsObjective().getScore(entry).setScore(0);
                    teamToEntry.put(newTeam, entry);
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
                    String newEntry = getDisplayName(team);
                    String oldEntry = teamToEntry.get(team);
                    if (newEntry.equals(oldEntry)) return;
                    int score = getBloodPointsObjective().getScore(oldEntry).getScore();
                    getBloodPointsObjective().getScore(newEntry).setScore(score);
                    getBloodPointsObjective().getScore(oldEntry).resetScore();
                    teamToEntry.put(team, newEntry);
                }, 1);
            } else if (args.length == 3 && args[1].equals("remove")) {
                if (team == null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    String entry = teamToEntry.get(team);
                    getBloodPointsObjective().getScore(entry).resetScore();
                    teamToEntry.remove(team);
                }, 1);
            } else if ((args.length == 3 || args.length == 4) && args[1].equals("add")) {
                if (team != null) return;
                Scheduler.scheduleAsyncDelayedTask(() -> {
                    Team newTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(args[2]);
                    if (newTeam == null) return;
                    String entry = getDisplayName(newTeam);
                    getBloodPointsObjective().getScore(entry).setScore(0);
                    teamToEntry.put(newTeam, entry);
                }, 1);
            }
        }
    }
}
