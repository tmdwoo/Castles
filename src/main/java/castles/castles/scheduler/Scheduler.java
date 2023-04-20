package castles.castles.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static castles.castles.Castles.plugin;

public class Scheduler {

    private Scheduler() {
        throw new IllegalStateException("Scheduler class");
    }

    public static BukkitTask scheduleSyncDelayedTask(Runnable task, int delay) {
        if (delay == 0) {
            return Bukkit.getServer().getScheduler().runTask(plugin, task);
        }
        else {
            return Bukkit.getServer().getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    public static BukkitTask scheduleSyncRepeatingTask(Runnable task, int delay, int period, int times) {
        if (times == 0) {
            return Bukkit.getServer().getScheduler().runTaskTimer(plugin, task, delay, period);
        }
        else {
            return new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    if (i < times) {
                        task.run();
                        i++;
                    }
                    else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, delay, period);
        }
    }

    public static BukkitTask scheduleAsyncDelayedTask(Runnable task, int delay) {
        if (delay == 0) {
            return Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
        else {
            return Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
        }
    }

    public static void cancelTask(Object task) {
        if (task instanceof BukkitTask) {
            BukkitTask bukkitTask = (BukkitTask) task;
            bukkitTask.cancel();
        }
    }
}