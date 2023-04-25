package castles.castles.handler;

import castles.castles.Castle;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;

import static castles.castles.Utils.castlesKey;
import static castles.castles.Utils.getCastleByName;

public class CorePatternHandler implements Listener {
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!entity.getPersistentDataContainer().has(castlesKey, PersistentDataType.STRING)) return;
        Castle castle = getCastleByName(entity.getPersistentDataContainer().get(castlesKey, PersistentDataType.STRING));
        if (entity instanceof AbstractVillager || entity instanceof Animals || entity instanceof Vex) {
            event.setCancelled(true);
        }
        if (castle == null) return;
        if (entity instanceof Player) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam((Player) entity);
            if (team != null && team.hasPlayer((Player) entity)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        Entity entity = event.getHitEntity();
        if (entity == null) return;
        Projectile projectile = event.getEntity();
        Entity shooter = (Entity) projectile.getShooter();
        Castle castle = null;
        if (projectile.getPersistentDataContainer().has(castlesKey, PersistentDataType.STRING)) {
            castle = getCastleByName(projectile.getPersistentDataContainer().get(castlesKey, PersistentDataType.STRING));
        } else if (shooter != null && shooter.getPersistentDataContainer().has(castlesKey, PersistentDataType.STRING)) {
            castle = getCastleByName(shooter.getPersistentDataContainer().get(castlesKey, PersistentDataType.STRING));
        }
        if (castle == null) return;
        if (entity instanceof AbstractVillager || entity instanceof Animals || entity instanceof Vex) {
            event.setCancelled(true);
        }
        if (entity instanceof Player) {
            Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam((Player) entity);
            if (team != null && team.hasPlayer((Player) entity)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityPotionEffectEvent(EntityPotionEffectEvent event) {
        if (event.getCause() == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD) {
            Entity entity = event.getEntity();
            if (entity instanceof Animals || entity instanceof AbstractVillager || entity instanceof Vex) {
                event.setCancelled(true);
            }
        }
    }
}
