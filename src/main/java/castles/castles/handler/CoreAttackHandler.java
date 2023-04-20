package castles.castles.handler;

import castles.castles.Castle;
import castles.castles.config.Config;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static castles.castles.Utils.castlesKey;
import static castles.castles.Utils.getCastleByName;
import static org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD;
import static org.bukkit.entity.EntityType.EVOKER_FANGS;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

public class CoreAttackHandler implements Listener {

    public final List<DamageCause> damageExceptionList = Arrays.asList(CONTACT, SUFFOCATION, FALL, DRAGON_BREATH, FIRE, FALLING_BLOCK, MELTING, LAVA, DROWNING, VOID, SUICIDE, STARVATION, FLY_INTO_WALL, HOT_FLOOR, CRAMMING, DRYOUT, FREEZE);

    boolean isCastleProtected(@NotNull Castle castle) {
        Team t = castle.getOwner();
        if (t == null) {
            return castle.rampartHealth > 0 || Objects.equals(Config.getGlobal().STATE, "PEACEFUL");
        } else {
            return castle.rampartHealth > 0 || Objects.equals(Config.getGlobal().STATE, "PEACEFUL") || Objects.equals(Config.getGlobal().STATE, "PREPARATION") || (Objects.equals(Config.getGlobal().STATE, "WAR") && castle.protectionTime > 0);
        }
    }

    boolean isDamageable(@Nullable Castle castle) {
        if (castle == null) {
            return false;
        }
        return !isCastleProtected(castle);
    }

    boolean isValidDamager(@Nullable Castle castle, @Nullable Entity entity) {
        if (!isDamageable(castle)) {
            return false;
        }
        if (entity == null) {
            return false;
        }
        if (entity.getType().equals(AREA_EFFECT_CLOUD) || entity.getType().equals(EVOKER_FANGS)) {
            return false;
        }
        assert castle != null;
        Team t = castle.getOwner();
        if (t == null) {
            return true;
        } else {
            return !t.hasEntity(entity);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(castlesKey)) {
            Entity core = event.getEntity();
            Castle castle = getCastleByName(core.getPersistentDataContainer().get(castlesKey, PersistentDataType.STRING));
            if (event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
                if (isValidDamager(castle, ((EntityDamageByEntityEvent) event).getDamager())) {
                    assert castle != null;
                    castle.damageCore(event.getFinalDamage(), ((EntityDamageByEntityEvent) event).getDamager());
                } else {
                    event.setCancelled(true);
                }
            } else if (!damageExceptionList.contains(event.getCause()) && isDamageable(castle)) {
                ((LivingEntity) core).setHealth(1024);
            } else {
                event.setCancelled(true);
            }
        }
    }
}
