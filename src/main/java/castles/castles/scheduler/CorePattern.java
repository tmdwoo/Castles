package castles.castles.scheduler;

import castles.castles.Castle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static castles.castles.scheduler.Scheduler.scheduleSyncRepeatingTask;

public class CorePattern {
    public static BukkitTask[] registerCorePattern(Castle castle) {
        AtomicReference<List<LivingEntity>> entities = new AtomicReference<>(new ArrayList<>());
        BukkitTask entityListUpdate = scheduleSyncRepeatingTask(() -> {
            List<LivingEntity> list = new ArrayList<>();
            list.addAll(castle.getMonstersInCastle());
            list.addAll(castle.getPlayersInCastle());
            list.removeIf(entity -> castle.getOwner() != null && (entity instanceof Player && (castle.getOwner().hasPlayer((Player) entity) || entity.hasPermission("castles.bypass.protection"))) || entity.isDead() || entity.isInvulnerable());
            entities.set(list);
        }, 0, 10, 0);

        BukkitTask arrow = scheduleSyncRepeatingTask(() -> {
            if (entities.get().isEmpty()) return;
            LivingEntity entity = entities.get().stream().min((o1, o2) -> {
                double distance1 = o1.getLocation().distance(castle.getLocation());
                double distance2 = o2.getLocation().distance(castle.getLocation());
                return Double.compare(distance1, distance2);
            }).orElse(null);
            if (entity == null) return;
            castle.shootArrow(entity);
        }, 0, 30, 0);

        BukkitTask shulkerBullet = scheduleSyncRepeatingTask(() -> {
            if (entities.get().isEmpty()) return;
            if (castle.levels.get("core") < 2) return;
            LivingEntity entity = entities.get().stream().max((o1, o2) -> {
                double distance1 = o1.getLocation().distance(castle.getLocation());
                double distance2 = o2.getLocation().distance(castle.getLocation());
                return Double.compare(distance1, distance2);
            }).orElse(null);
            if (entity == null) return;
            castle.shootShulkerBullet(entity);
        }, 0, 20 * 5, 0);

        BukkitTask evokerFangs = scheduleSyncRepeatingTask(() -> {
            if (entities.get().isEmpty()) return;
            if (castle.levels.get("core") < 3) return;
            if (castle.coreHealth / castle.getCoreMaxHealth() > 0.75) return;
            LivingEntity entity = entities.get().stream().min((o1, o2) -> {
                double distance1 = o1.getLocation().distance(castle.getLocation());
                double distance2 = o2.getLocation().distance(castle.getLocation());
                return Double.compare(distance1, distance2);
            }).orElse(null);
            if (entity == null) return;
            castle.summonEvokerFangs(entity);
        }, 50, 20 * 5, 0);

        BukkitTask iceField = scheduleSyncRepeatingTask(() -> {
            if (entities.get().isEmpty()) return;
            if (castle.levels.get("core") < 3) return;
            if (castle.coreHealth / castle.getCoreMaxHealth() > 0.9) return;
            LivingEntity entity = entities.get().stream().skip((int) (entities.get().size() * Math.random())).findFirst().orElse(null);
            if (entity == null) return;
            castle.summonIceField(entity);
        }, 0, 20 * 10, 0);

        BukkitTask toxicField = scheduleSyncRepeatingTask(() -> {
            if (entities.get().isEmpty()) return;
            if (castle.levels.get("core") < 4) return;
            if (castle.coreHealth / castle.getCoreMaxHealth() > 0.5) return;
            LivingEntity entity = entities.get().stream().skip((int) (entities.get().size() * Math.random())).findFirst().orElse(null);
            if (entity == null) return;
            castle.summonToxicField(entity);
        }, 0, 20 * 20, 0);

        BukkitTask vex1 = scheduleSyncRepeatingTask(() -> {
            if (entities.get().isEmpty()) return;
            if (castle.levels.get("core") < 5) return;
            LivingEntity entity = entities.get().stream().max((o1, o2) -> {
                double distance1 = o1.getLocation().distance(castle.getLocation());
                double distance2 = o2.getLocation().distance(castle.getLocation());
                return Double.compare(distance1, distance2);
            }).orElse(null);
            if (entity == null) return;
            castle.summonVex(entity);
        }, 0, 20 * 30, 0);

        BukkitTask vex2 = scheduleSyncRepeatingTask(() -> {
            if (entities.get().isEmpty()) return;
            if (castle.levels.get("core") < 5) return;
            if (castle.coreHealth / castle.getCoreMaxHealth() > 0.5) return;
            LivingEntity entity = entities.get().stream().min((o1, o2) -> {
                double distance1 = o1.getLocation().distance(castle.getLocation());
                double distance2 = o2.getLocation().distance(castle.getLocation());
                return Double.compare(distance1, distance2);
            }).orElse(null);
            if (entity == null) return;
            castle.summonVex(entity);
        }, 20 * 15, 20 * 30, 0);

        return new BukkitTask[]{entityListUpdate, arrow, shulkerBullet, evokerFangs, iceField, toxicField, vex1, vex2};
    }
}
