package castles.castles;

import castles.castles.config.Config;
import castles.castles.scheduler.Scheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.Duration;
import java.util.*;

import static castles.castles.Castles.plugin;
import static castles.castles.Utils.*;
import static castles.castles.localization.Phrase.*;
import static castles.castles.scheduler.CorePattern.registerCorePattern;
import static castles.castles.scheduler.Schedules.corePatterns;
import static java.lang.Math.max;

public class Castle implements Serializable {
    public String name;
    public String owner;
    public long createdTime;
    public Map<String, Object> location;
    public ArrayList<ChunkPos> chunks = new ArrayList<>();
    public UUID coreUUID;
    public HashMap<String, List<Map<String, Object>>> flags;
    int rampartHeight;
    public HashMap<Map<String, Object>, String> rampart = new HashMap<>();
    public List<Map<String, Object>> crackedRampart = new ArrayList<>();
    public HashMap<Map<String, Object>, String> beforeRampart = new HashMap<>();
    public Integer protectionTime = 3600;
    public double coreHealth;
    public int lastHit = 0;
    public int rampartHealth;
    public HashMap<String, Integer> levels = new HashMap<>();

    public Castle(String name, Location location) {
        this(name, location, null);
    }

    public Castle(@NotNull String name, @NotNull Location location, @Nullable Team owner) {
        if (getCastleByName(name) != null) {
            throw new IllegalArgumentException("A castle with that name already exists");
        } else if (getCastleByLocation(location) != null) {
            throw new IllegalArgumentException("A castle already exists in this chunk");
        } else if (location.getY() < getWorldEnv(location.getWorld()).getMinY() || location.getY() > getWorldEnv(location.getWorld()).getMaxY()) {
            throw new IllegalArgumentException("The castle must be above or below the world's height limit");
        } else if (getMod(location.getX(), 16) < 1.5 || getMod(location.getX(), 16) > 14.5 || getMod(location.getZ(), 16) < 1.5 || getMod(location.getZ(), 16) > 14.5) {
            throw new IllegalArgumentException("The castle cannot be placed on the edge of a chunk");
        } else {
            this.name = name;
            this.createdTime = System.currentTimeMillis();
            this.owner = owner == null ? null : owner.getName();
            this.location = location.serialize();
            location.getChunk().getPersistentDataContainer().set(Utils.castlesKey, PersistentDataType.STRING, name);
            flags = buildFlag(location, owner);
            chunks.add(new ChunkPos(location));
            levels.put("core", 1);
            levels.put("rampart", 1);
            coreHealth = getCoreMaxHealth();
            rampartHealth = getRampartMaxHealth();
            getBossBar().setProgress(1);
            setCore();
            setRampart();
            Castles.castles.add(this);
            corePatterns.put(this, registerCorePattern(this));
        }
    }

    public @NotNull BossBar getBossBar() {
        NamespacedKey bossBarKey = new NamespacedKey(plugin, Long.toString(createdTime));
        BossBar bossBar = Bukkit.getBossBar(bossBarKey);
        if (bossBar == null) {
            Bukkit.createBossBar(bossBarKey, name, BarColor.WHITE, BarStyle.SOLID);
            bossBar = Bukkit.getBossBar(bossBarKey);
            bossBar.setProgress(coreHealth / getCoreMaxHealth());
        }
        return bossBar;
    }

    private LivingEntity setCore() {
        for (Entity preCore : chunks.get(0).getChunk().getEntities()) {
            if (preCore.getPersistentDataContainer().has(Utils.castlesKey, PersistentDataType.STRING) && preCore.getPersistentDataContainer().get(Utils.castlesKey, PersistentDataType.STRING).equals(name)) {
                preCore.remove();
            }
        }
        LivingEntity entity = (LivingEntity) getLocation().getWorld().spawnEntity(getLocation(), EntityType.BLAZE);
        entity.teleport(getLocation());
        Team owner = this.getOwner();
        Component coreName = Component.text(this.name, owner != null ? (owner.hasColor() ? owner.color() : null) : null);
        entity.customName(coreName);
        entity.setAI(false);
        entity.setCanPickupItems(false);
        entity.setCollidable(false);
        entity.setCustomNameVisible(false);
        entity.setGravity(false);
        entity.setPersistent(true);
        entity.setSilent(true);
        entity.setRemoveWhenFarAway(false);
        entity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(1024);
        entity.setHealth(1024);
        entity.getPersistentDataContainer().set(Utils.castlesKey, PersistentDataType.STRING, name);
        coreUUID = entity.getUniqueId();
        return entity;
    }

    public void setCoreLevel(int level) {
        if (level < 1 || level > 5) {
            throw new IllegalArgumentException("The level must be between 1 and 5");
        }
        double oldMaxHealth = getCoreMaxHealth();
        levels.put("core", level);
        double newMaxHealth = getCoreMaxHealth();
        setCoreHealth(coreHealth / oldMaxHealth * newMaxHealth);
    }

    public void setCoreHealth(double health) {
        if (health < 0) {
            throw new IllegalArgumentException("The health cannot be negative");
        } else if (health > getCoreMaxHealth()) {
            throw new IllegalArgumentException(String.format("The health cannot be greater than max health (%s)", getCoreMaxHealth()));
        }
        coreHealth = health;
        getBossBar().setProgress(coreHealth / getCoreMaxHealth());
    }

    public double getCoreMaxHealth() {
        return 100 * Math.pow(levels.get("core"), 2);
    }

    public void damageCore(double damage) {
        damageCore(damage, null);
    }

    public void damageCore(double damage, Entity damager) {
        if (damage < 0) {
            throw new IllegalArgumentException("The damage cannot be negative");
        }
        if (lastHit == 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(formatComponent(Component.text(CASTLE_CORE_ATTACKED.getPhrase(player), NamedTextColor.RED), getComponent(player)));
            }
        }
        lastHit = 400;
        if (damage >= coreHealth) {
            setCoreHealth(0);
            if (damager instanceof Player) {
                Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam((Player) damager);
                if (!Objects.equals(owner, team == null ? null : team.getName())) {
                    occupy(team, (Player) damager);
                }
            }
        } else {
            setCoreHealth(coreHealth - damage);
        }
        for (Player player : getPlayersInCastle()) {player.sendActionBar(formatComponent(Component.text(String.format(CASTLE_CORE_HEALTH.getPhrase(player), (int) coreHealth, (int) getCoreMaxHealth()), NamedTextColor.RED), getComponent(player)));}
        if (damager instanceof Player) {
            Player player = (Player) damager;
            player.sendActionBar(formatComponent(Component.text(String.format(CASTLE_CORE_HEALTH.getPhrase(player), (int) coreHealth, (int) getCoreMaxHealth()), NamedTextColor.RED), getComponent(player)));
        }
        getCore().setHealth(1024);
    }

    public @NotNull LivingEntity getCore() {
        LivingEntity core = (LivingEntity) getLocation().getWorld().getEntity(coreUUID);
        if (core == null) {
            for (Entity entity : chunks.get(0).getChunk().getEntities()) {
                if (!entity.getPersistentDataContainer().has(Utils.castlesKey, PersistentDataType.STRING)) continue;
                if (!entity.getType().equals(EntityType.BLAZE)) continue;
                if (Objects.equals(entity.getPersistentDataContainer().get(Utils.castlesKey, PersistentDataType.STRING), name)) {
                    core = (LivingEntity) entity;
                    coreUUID = core.getUniqueId();
                    return core;
                }
            }
            core = setCore();
        }
        return core;
    }

    public void occupy(@Nullable Team team, @NotNull Player damager) {
        if (Objects.equals(owner, team == null ? null : team.getName())) {
            throw new IllegalArgumentException("The castle is already owned by this team");
        }
        TextColor color = team != null ? (team.hasColor() ? team.color() : null) : null;
        protectionTime = 3600;
        lastHit = 0;
        setCoreHealth(getCoreMaxHealth());
        setRampartHealth(getRampartMaxHealth());
        updateRampart();
        setOwner(team);
        if (team != null) {
            for (Player player : getPlayersInCastle()) {
                if (team.hasPlayer(player)) player.setBedSpawnLocation(getLocation(), true);
            }
        }
        getLocation().getWorld().spawnParticle(Particle.ELECTRIC_SPARK, getLocation(), 100, 0.5, 0.5, 0.5, 0.1);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (team == null) {
                player.sendMessage(formatComponent(Component.text(CASTLE_CORE_UNOCCUPIED.getPhrase(player)), getComponent(player)));
                player.showTitle(Title.title(formatComponent(Component.text(CASTLE_CORE_UNOCCUPIED.getPhrase(player)), getComponent(player)), Component.text("by ", NamedTextColor.GRAY).append(Component.text(damager.getName(), NamedTextColor.GRAY)), Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(5000), Duration.ofMillis(500))));
            } else {
                player.sendMessage(formatComponent(Component.text(CASTLE_CORE_OCCUPIED.getPhrase(player)), getComponent(player), getTeamComponent(team)));
                player.showTitle(Title.title(formatComponent(Component.text(CASTLE_CORE_OCCUPIED.getPhrase(player)), getComponent(player), getTeamComponent(team)), Component.text("by ", NamedTextColor.GRAY).append(Component.text(damager.getName(), color)), Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(5000), Duration.ofMillis(500))));
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 1, 1);
        }
        addScore(team, Config.getGlobal().BP_PER_OCCUPY);
    }

    public void expand(ChunkPos chunk) {
        if (getCastleByChunk(chunk) != null) {
            throw new IllegalArgumentException("A castle already exists in this chunk");
        }
        // check if chunk is adjacent to castle
        for (ChunkPos castleChunk : chunks) {
            if (castleChunk.isAdjacent(chunk)) {
                chunks.add(chunk);
                chunk.getChunk().getPersistentDataContainer().set(Utils.castlesKey, PersistentDataType.STRING, name);
                destroyRampart();
                buildRampart();
                return;
            }
        }
        throw new IllegalArgumentException("The chunk is not adjacent to the castle");
    }

    public void setName(String name) {
        this.name = name;
        // change persistent data container
        for (ChunkPos chunk : chunks) {
            chunk.getChunk().getPersistentDataContainer().set(Utils.castlesKey, PersistentDataType.STRING, name);
        }
        getBossBar().setTitle(name);
        setCore();
    }

    public void setOwner(Team owner) {
        for (OfflinePlayer player : getOwner().getPlayers()) {
            if (player.isOnline() && Objects.equals(getCastleByLocation(player.getBedSpawnLocation()), this)) {
                Castle nearestCastle = getNearestTeamCastle(player.getPlayer());
                ((Player) player).setBedSpawnLocation(nearestCastle == null ? null : nearestCastle.getLocation(), true);
            }
        }
        this.owner = owner == null ? null : owner.getName();
        Material wool = DyeColor2Wool.get(t2dColorMap.get(owner != null && owner.hasColor() ? owner.color() : NamedTextColor.WHITE));
        for (Map<String, Object> wools : flags.get("wools")) {
            Location woolLocation = Location.deserialize(wools);
            woolLocation.getBlock().setType(wool);
        }
        setCore();
    }

    public void setLocation(Location location) {
        if (!chunks.contains(new ChunkPos(location))) {
            throw new IllegalArgumentException("The castle must be in the chunk");
        } else if (location.getY() < getWorldEnv(location.getWorld()).getMinY() || location.getY() > getWorldEnv(location.getWorld()).getMaxY()) {
            throw new IllegalArgumentException("The castle must be above or below the world's height limit");
        } else if (getMod(location.getX(), 16) < 1.5 || getMod(location.getX(), 16) > 14.5 || getMod(location.getZ(), 16) < 1.5 || getMod(location.getZ(), 16) > 14.5) {
            throw new IllegalArgumentException("The castle cannot be placed on the edge of a chunk");
        }else {
            getLocation().getChunk().getPersistentDataContainer().remove(Utils.castlesKey);
            this.location = location.serialize();
            location.getChunk().getPersistentDataContainer().set(Utils.castlesKey, PersistentDataType.STRING, name);
            for (Map<String, Object> wools : flags.get("wools")) {
                Location woolLocation = Location.deserialize(wools);
                woolLocation.getBlock().setType(Material.AIR);
            }
            for (Map<String, Object> fence : flags.get("fences")) {
                Location bedLocation = Location.deserialize(fence);
                bedLocation.getBlock().setType(Material.AIR);
            }
            flags = buildFlag(location, this.getOwner());
            setCore();
        }
    }

    public void destroy() {
        for (ChunkPos chunk : chunks) {
            chunk.getChunk().getPersistentDataContainer().remove(Utils.castlesKey);
        }
        destroyRampart();
        NamespacedKey bossBarKey = new NamespacedKey(plugin, Long.toString(createdTime));
        BossBar bossBar = Bukkit.getBossBar(bossBarKey);
        for (Player player : bossBar.getPlayers()) {
            bossBar.removePlayer(player);
        }
        Bukkit.removeBossBar(bossBarKey);
        Entity core = getCore();
        if (core != null) {
            core.remove();
            Castles.castles.remove(this);
        }
        for (Map<String, Object> wools : flags.get("wools")) {
            Location woolLocation = Location.deserialize(wools);
            woolLocation.getBlock().setType(Material.AIR);
        }
        for (Map<String, Object> beds : flags.get("fences")) {
            Location bedLocation = Location.deserialize(beds);
            bedLocation.getBlock().setType(Material.AIR);
        }
        for (BukkitTask task : corePatterns.get(this)) {
            task.cancel();
        }
    }

    public void showBorder(Player player) {
        Set<Location> border = getBorderCoords(chunks);
        Particle particle = getOwner() == null ? Particle.SMOKE_NORMAL : getOwner().hasPlayer(player) ? Particle.VILLAGER_HAPPY : Particle.FLAME;
        for (Location location : border) {
            player.spawnParticle(particle, location, 1, 0, 0, 0, 0);
        }
    }

    public void setRampart() {
        ChunkPos chunk = chunks.get(0);
        int top = getWorldEnv(chunk.getWorld()).getMinY() + 12;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                top = max(top, chunk.getWorld().getHighestBlockYAt(chunk.getX() * 16 + x, chunk.getZ() * 16 + z, HeightMap.WORLD_SURFACE_WG) + 6);
            }
        }
        rampartHeight = top;
        buildRampart();
    }

    public void buildRampart() {
        HashMap<Map<String, Object>, String> coords = getRampartCoords(chunks, rampartHeight);
        for (Map.Entry<Map<String, Object>, String> entry : coords.entrySet()) {
            Location location = Location.deserialize(entry.getKey());
            Block block = location.getBlock();
            if (!nbtBlocks.contains(block.getType().name())) {
                beforeRampart.put(location.serialize(), block.getType().name());
                String mat = entry.getValue();
                Material material;
                switch (mat) {
                    case "BRICKS": {
                        material = Material.valueOf(RampartTypes.get(levels.get("rampart")).get(0));
                        block.setType(material);
                        break;
                    }
                    case "STAIRS": {
                        material = Material.valueOf(RampartTypes.get(levels.get("rampart")).get(1));
                        block.setType(material);
                        Stairs stair = (Stairs) block.getBlockData();
                        stair.setHalf(Bisected.Half.BOTTOM);
                        stair.setShape(Stairs.Shape.STRAIGHT);
                        if (getMod(location.getBlockX(), 16) == 7) stair.setFacing(BlockFace.WEST);
                        else if (getMod(location.getBlockX(), 16) == 8) stair.setFacing(BlockFace.EAST);
                        else if (getMod(location.getBlockZ(), 16) == 7) stair.setFacing(BlockFace.NORTH);
                        else if (getMod(location.getBlockZ(), 16) == 8) stair.setFacing(BlockFace.SOUTH);
                        block.setBlockData(stair);
                        break;
                    }
                    case "SLAB": {
                        material = Material.valueOf(RampartTypes.get(levels.get("rampart")).get(2));
                        block.setType(material);
                        Slab slab = (Slab) block.getBlockData();
                        slab.setType(Slab.Type.BOTTOM);
                        block.setBlockData(slab);
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unexpected value: " + mat);
                }
                rampart.put(location.serialize(), material.name());
            }
        }
        crackedRampart.clear();
    }

    public void crackRampart(double damage) {
        if (rampart.isEmpty()) {
            return;
        }
        Random random = new Random();
        double crack = damage * 1;
        double cracked = ((double) crackedRampart.size()) / ((double) rampart.size());
        int numToCrack = (int) ((crack - cracked) * rampart.size());
        List<Map<String, Object>> keys = new ArrayList<>(rampart.keySet());
        keys.removeAll(crackedRampart);
        if (numToCrack > 0) {
            while (numToCrack > 0) {
                int index = random.nextInt(keys.size());
                Map<String, Object> entry = keys.get(index);
                Location location = Location.deserialize(entry);
                String mat = rampart.get(entry);
                if (mat.endsWith("BRICKS")) {
                    location.getBlock().setType(Material.valueOf(RampartTypes.get(levels.get("rampart")).get(3)));
                } else {
                    location.getBlock().setType(Material.AIR);
                }
                crackedRampart.add(entry);
                keys.remove(index);
                numToCrack--;
            }
        } else if (numToCrack < 0) {
            numToCrack = -numToCrack;
            while (numToCrack > 0) {
                int index = random.nextInt(crackedRampart.size());
                Map<String, Object> entry = crackedRampart.get(index);
                String mat = rampart.get(entry);
                Location location = Location.deserialize(entry);
                location.getBlock().setType(Material.valueOf(mat));
                if (mat.endsWith("STAIRS")) {
                    Stairs stair = (Stairs) location.getBlock().getBlockData();
                    stair.setHalf(Bisected.Half.BOTTOM);
                    stair.setShape(Stairs.Shape.STRAIGHT);
                    if (getMod(location.getBlockX(), 16) == 7) stair.setFacing(BlockFace.WEST);
                    else if (getMod(location.getBlockX(), 16) == 8) stair.setFacing(BlockFace.EAST);
                    else if (getMod(location.getBlockZ(), 16) == 7) stair.setFacing(BlockFace.NORTH);
                    else if (getMod(location.getBlockZ(), 16) == 8) stair.setFacing(BlockFace.SOUTH);
                    location.getBlock().setBlockData(stair);
                } else if (mat.endsWith("SLAB")) {
                    Slab slab = (Slab) location.getBlock().getBlockData();
                    slab.setType(Slab.Type.BOTTOM);
                    location.getBlock().setBlockData(slab);
                }
                crackedRampart.remove(entry);
                numToCrack--;
            }
        }
    }

    public void destroyRampart() {
        for (Map.Entry<Map<String, Object>, String> entry : beforeRampart.entrySet()) {
            Location location = Location.deserialize(entry.getKey());
            Block block = location.getBlock();
            block.setType(Material.valueOf(entry.getValue()));
        }
        beforeRampart.clear();
        rampart.clear();
        crackedRampart.clear();
    }

    public void setRampartHealth(int health) {
        if (health < 0 || health > getRampartMaxHealth()) {
            throw new IllegalArgumentException("Health must be between 0 and max" + getRampartMaxHealth());
        }
        rampartHealth = health;
        updateRampart();
    }

    public void setRampartLevel(int level) {
        if (level < 1 || level > 5) {
            throw new IllegalArgumentException("Level must be between 1 and 5");
        }
        double oldMaxHealth = getRampartMaxHealth();
        levels.put("rampart", level);
        double newMaxHealth = getRampartMaxHealth();
        rampartHealth = (int) Math.round(((double) rampartHealth / oldMaxHealth) * newMaxHealth);
        destroyRampart();
        buildRampart();
        updateRampart();
    }

    public int getRampartMaxHealth() {
        return levels.get("rampart") * 200;
    }

    public void damageRampart() {
        damageRampart(null);
    }

    public void damageRampart(Entity damager) {
        if (rampartHealth > 0) {
            double healthRatio = ((double) rampartHealth) / ((double) getRampartMaxHealth());
            if (healthRatio == 1) for (Player player : Bukkit.getOnlinePlayers())
                player.sendMessage(formatComponent(Component.text(CASTLE_RAMPART_ATTACKED.getPhrase(player)), getComponent(player)));
            else if (healthRatio == 0.75) for (Player player : Bukkit.getOnlinePlayers())
                player.sendMessage(formatComponent(Component.text(String.format(CASTLE_RAMPART_PERCENT.getPhrase(player), (int) ((1 - healthRatio) * 100))), getComponent(player)));
            else if (healthRatio == 0.5) for (Player player : Bukkit.getOnlinePlayers())
                player.sendMessage(formatComponent(Component.text(String.format(CASTLE_RAMPART_PERCENT.getPhrase(player), (int) ((1 - healthRatio) * 100))), getComponent(player)));
            else if (healthRatio == 0.25) for (Player player : Bukkit.getOnlinePlayers())
                player.sendMessage(formatComponent(Component.text(String.format(CASTLE_RAMPART_PERCENT.getPhrase(player), (int) ((1 - healthRatio) * 100))), getComponent(player)));
            rampartHealth -= 1;
            if (rampartHealth <= 0) for (Player player : Bukkit.getOnlinePlayers())
                player.sendMessage(formatComponent(Component.text(CASTLE_RAMPART_DESTROYED.getPhrase(player)), getComponent(player)));
            updateRampart();
            for (Player player : getPlayersInCastle()) player.sendActionBar(formatComponent(Component.text(String.format(CASTLE_RAMPART_HEALTH.getPhrase(player), rampartHealth, getRampartMaxHealth())), getComponent(player)));
            if (damager instanceof LivingEntity) {
                ((LivingEntity) damager).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * levels.get("rampart"), levels.get("rampart") < 3 ? 0 : 1, false, false, false));
                ((LivingEntity) damager).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * levels.get("rampart"), levels.get("rampart") < 3 ? 0 : 1, false, false, false));
                ((LivingEntity) damager).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * levels.get("rampart"), 0, false, false, false));
                if (damager instanceof Player) {
                    damager.sendActionBar(formatComponent(Component.text(String.format(CASTLE_RAMPART_HEALTH.getPhrase(damager), rampartHealth, getRampartMaxHealth())), getComponent(damager)));
                }
            }
        }
    }

    public void updateRampart() {
        double maxHealth = getRampartMaxHealth();
        if (rampartHealth <= 0) {
            destroyRampart();
            for (Map<String, Object> entry : rampart.keySet()) {
                Location location = Location.deserialize(entry);
                location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 1);
                location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            }
        } else if (rampartHealth >= maxHealth) {
            destroyRampart();
            buildRampart();
        } else {
            if (rampart.size() == 0) buildRampart();
            crackRampart((maxHealth-rampartHealth)/maxHealth);
        }
    }

    public void update() {
        if (protectionTime > 0) {
            protectionTime--;
        }
        if (lastHit > 0) {
            lastHit--;
            if (lastHit == 0) {
                Team team = getOwner();
                if (team != null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (team.hasPlayer(player)) {
                            player.sendMessage(formatComponent(Component.text(CASTLE_CORE_RECOVERED.getPhrase(player)), getComponent(player)));
                        }
                    }
                }
                protectionTime += 600;
            } else if (lastHit <= 100) {
                if (lastHit == 100) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(formatComponent(Component.text(CASTLE_CORE_RECOVERING.getPhrase(player)), getComponent(player), getComponent(player)));
                    }
                }
                setCoreHealth(coreHealth + (getCoreMaxHealth() - coreHealth) / lastHit);
                for (Player player : getPlayersInCastle()) player.sendActionBar(formatComponent(Component.text(String.format(CASTLE_CORE_HEALTH.getPhrase(player), (int) coreHealth, (int) getCoreMaxHealth())), getComponent(player)));
            }
        }
    }

    public void shootArrow(LivingEntity entity) {
        Vector direction = entity.getLocation().subtract(getLocation()).toVector().normalize();
        Arrow arrow = getLocation().getWorld().spawnArrow(getLocation().add(0, 1, 0),  direction, 1, 12);
        arrow.setVelocity(direction.multiply(1.5));
        arrow.setLifetimeTicks(20 * 3);
        arrow.getPersistentDataContainer().set(castlesKey, PersistentDataType.STRING, name);
        arrow.setShooter(getCore());
    }

    public void shootShulkerBullet(LivingEntity entity) {
        ShulkerBullet bullet = getLocation().getWorld().spawn(getLocation().add(0, 1, 0), ShulkerBullet.class);
        bullet.setTarget(entity);
        bullet.getPersistentDataContainer().set(castlesKey, PersistentDataType.STRING, name);
        bullet.setShooter(getCore());
    }

    public void summonIceField(LivingEntity entity) {
        AreaEffectCloud cloud = getLocation().getWorld().spawn(entity.getLocation(), AreaEffectCloud.class);
        cloud.setRadius(5);
        cloud.setRadiusOnUse(-0.1f);
        cloud.setDuration(20 * 5);
        cloud.setParticle(Particle.BLOCK_CRACK, Material.ICE.createBlockData());
        cloud.setDurationOnUse(20 * 5);
        cloud.setReapplicationDelay(20 * 5);
        cloud.setBasePotionData(new PotionData(PotionType.SLOWNESS));
        cloud.getPersistentDataContainer().set(castlesKey, PersistentDataType.STRING, name);
        cloud.setSource(getCore());
    }

    public void summonToxicField(LivingEntity entity) {
        AreaEffectCloud cloud = getLocation().getWorld().spawn(entity.getLocation(), AreaEffectCloud.class);
        cloud.setRadius(5);
        cloud.setRadiusOnUse(-0.1f);
        cloud.setDuration(20 * 5);
        cloud.setParticle(Particle.BLOCK_CRACK, Material.WEATHERED_COPPER.createBlockData());
        cloud.setDurationOnUse(20 * 5);
        cloud.setReapplicationDelay(20 * 5);
        cloud.getPersistentDataContainer().set(castlesKey, PersistentDataType.STRING, name);
        cloud.setSource(getCore());
        Scheduler.scheduleSyncDelayedTask(() -> {
            cloud.setBasePotionData(new PotionData(PotionType.POISON));
        }, 20);
    }

    public void summonEvokerFangs(LivingEntity entity) {
        Vector direction = entity.getLocation().subtract(getLocation()).toVector().setY(0).normalize();
        Location location = getLocation();
        Scheduler.scheduleSyncRepeatingTask(() -> {
            EvokerFangs fang = location.getWorld().spawn(location.add(direction), EvokerFangs.class);
            fang.getPersistentDataContainer().set(castlesKey, PersistentDataType.STRING, name);
            fang.setOwner(getCore());
        }, 0, 2, 9);
    }

    public void summonVex(LivingEntity entity) {
        Location location = getLocation();
        Vex vex = location.getWorld().spawn(location, Vex.class);
        vex.setTarget(entity);
        vex.getPersistentDataContainer().set(castlesKey, PersistentDataType.STRING, name);
        getOwner().addEntity(vex);
        Scheduler.scheduleSyncDelayedTask(() -> {
            vex.remove();
        }, 20 * 60);
    }

    public List<Player> getPlayersInCastle() {
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getCastleByLocation(player.getLocation()) == this && player.getLocation().distance(getLocation()) <= 16) players.add(player);
        }
        return players;
    }

    public List<Monster> getMonstersInCastle() {
        List<Monster> monsters = new ArrayList<>();
        for (ChunkPos chunkPos : chunks) {
            for (Entity entity : chunkPos.getChunk().getEntities()) {
                if (entity instanceof Monster && !getCore().equals(entity) && !entity.getType().equals(EntityType.VEX) && getCore().getLocation().distance(entity.getLocation()) <= 16) monsters.add((Monster) entity);
            }
        }
        return monsters;
    }

    /**
     * @return the location of the core
     */
    public @NotNull Location getLocation() {
        return Location.deserialize(location);
    }

    /**
     * @return the owner of the castle
     */
    public @Nullable Team getOwner(){
        if (this.owner != null) {
            Team owner = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(this.owner);
            if (owner == null) {
                this.setOwner(null);
                return null;
            }
            return owner;
        }
        return null;
    }

    /**
     * @return the name of the castle
     */
    public @NotNull Component getComponent(CommandSender sender) {
        Team owner = this.getOwner();
        TextColor color = owner != null ? (owner.hasColor() ? owner.color() : null) : null;
        return Component.text(name, color)
                .hoverEvent(HoverEvent.showText(
                        formatComponent(Component.text(CASTLE_OWNER.getPhrase(sender), NamedTextColor.GOLD), owner != null ? owner.displayName().color(color) : Component.text("No One"))
                                .appendNewline()
                                .append(protectionTime == 0 ? Component.text(CASTLE_NOT_PROTECTED.getPhrase(sender)) : Component.text(String.format(CASTLE_PROTECTED.getPhrase(sender), secondsToTimeString(protectionTime)), TextColor.color(1 - Math.min(1, (float) protectionTime / 3600), Math.min(1, (float) protectionTime / 3600), 0.0F)))
                                .appendNewline()
                                .append(Component.text(String.format(CASTLE_RAMPART.getPhrase(sender), levels.get("rampart"), rampartHealth, getRampartMaxHealth()), NamedTextColor.GRAY))
                                .appendNewline()
                                .append(Component.text(String.format(CASTLE_CORE.getPhrase(sender), levels.get("core"), (int) coreHealth, (int) getCoreMaxHealth()), NamedTextColor.GRAY))))
                .clickEvent(ClickEvent.runCommand(String.format("/castles teleport \"%s\"", name)));
    }

    public @NotNull Component getComponent() {
        return getComponent(Bukkit.getConsoleSender());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Castle) {
            return ((Castle) obj).name.equals(this.name);
        }
        return false;
    }
}
