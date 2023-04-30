package castles.castles.handler;

import castles.castles.Castle;
import castles.castles.config.Config;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.block.PlayerShearBlockEvent;
import io.papermc.paper.event.entity.EntityDyeEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PlayerNameEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static castles.castles.Utils.*;

public class CastleProtectionHandler implements Listener {

    boolean isCastleProtected(@Nullable Castle castle) {
        if (castle == null) {
            return false;
        }
        Team t = castle.getOwner();
        if (t == null) {
            return castle.rampartHealth > 0 || Objects.equals(Config.getGlobal().STATE, "PEACEFUL");
        } else {
            return castle.rampartHealth > 0 || Objects.equals(Config.getGlobal().STATE, "PEACEFUL") || Objects.equals(Config.getGlobal().STATE, "PREPARATION") || (Objects.equals(Config.getGlobal().STATE, "WAR") && castle.protectionTime > 0);
        }
    }

    boolean isPlayerProtected(@NotNull Player player){
        Castle castle = getCastleByLocation(player.getLocation());
        if (castle != null){
            Team team = castle.getOwner();
            if (team == null || !team.hasPlayer(player)){
                return isCastleProtected(castle);
            }
        }
        return false;
    }

    boolean isFlag(@NotNull Location location, @Nullable Castle castle) {
        if (castle != null) {
            return castle.flags.get("wools").contains(location.serialize()) || castle.flags.get("fences").contains(location.serialize()) || castle.flags.get("blanks").contains(location.serialize());
        }
        return false;
    }

    boolean isFlag(@NotNull Location location){
        Castle castle = getCastleByLocation(location);
        return isFlag(location, castle);
    }

    boolean isLocationProtected(@Nullable Location location){
        if (location == null){
            return false;
        }
        Castle castle = getCastleByLocation(location);
        if (castle != null){
            if (isFlag(location, castle)) return true;
            return isCastleProtected(castle);
        }
        return false;
    }

    boolean isPlayerCrasher(@Nullable Player player, @Nullable Location location){
        if (player == null || location == null){
            return false;
        }
        Castle castle = getCastleByLocation(location);
        if (isFlag(location, castle)) return true;
        if (player.hasPermission("castles.bypass.protection")){
            return false;
        }
        if (castle != null){
            Team team = castle.getOwner();
            if (team != null && team.hasPlayer(player)){
                return false;
            }
            return isCastleProtected(castle);
        }
        return false;
    }

    boolean isOriginOutsideCastle(@NotNull Location target, @Nullable Location origin){
        Castle targetCastle = getCastleByLocation(target);
        Castle originCastle = getCastleByLocation(origin);
        if (isFlag(target, targetCastle)) return true;
        if (originCastle != null){
            return targetCastle != originCastle;
        }
        return true;
    }

    HashMap<ChunkPos, int[]> getChunkDistance(ChunkPos chunk, @NotNull ArrayList<ChunkPos> castleChunks){
        HashMap<ChunkPos, int[]> chunks = new HashMap<>();
        World world = Bukkit.getWorld(chunk.world);
        ChunkPos westEnd = chunk.clone(), eastEnd = chunk.clone();
        int westDistance = 0, eastDistance = 0;
        do {
            westDistance--;
            westEnd.x--;
        } while (castleChunks.contains(westEnd));
        do {
            eastDistance++;
            eastEnd.x++;
        } while (castleChunks.contains(eastEnd));
        chunks.put(westEnd, new int[]{westDistance, 0});
        chunks.put(eastEnd, new int[]{eastDistance, 0});
        for (int i = westEnd.x + 1; i < eastEnd.x; i++){
            ChunkPos NorthEnd = new ChunkPos(world, i, chunk.z), SouthEnd = new ChunkPos(world, i, chunk.z);
            int northDistance = 0, southDistance = 0;
            do {
                northDistance--;
                NorthEnd.z--;
            } while (castleChunks.contains(NorthEnd));
            do {
                southDistance++;
                SouthEnd.z++;
            } while (castleChunks.contains(SouthEnd));
            chunks.put(NorthEnd, new int[]{i - chunk.x, northDistance});
            chunks.put(SouthEnd, new int[]{i - chunk.x, southDistance});
        }
        return chunks;
    }


    Vector getShortestVectorToBorder(Location location, @NotNull ArrayList<ChunkPos> castleChunks){
        Vector vector = new Vector();
        ChunkPos locationChunk = new ChunkPos(location);
        HashMap<ChunkPos, int[]> chunks = getChunkDistance(locationChunk, castleChunks);
        double minDistance = Double.MAX_VALUE;
        double xChunkInnerCoord = (CHUNK_SIZE + location.getX() % CHUNK_SIZE) % CHUNK_SIZE;
        double zChunkInnerCoord = (CHUNK_SIZE + location.getZ() % CHUNK_SIZE) % CHUNK_SIZE;
        for (ChunkPos chunk : chunks.keySet()){
            int[] distance = chunks.get(chunk);
            double xDistance = distance[0] * CHUNK_SIZE + (distance[0] < 0 ? CHUNK_SIZE - xChunkInnerCoord - 0.5 : distance[0] > 0 ? -xChunkInnerCoord + 0.5 : 0);
            double zDistance = distance[1] * CHUNK_SIZE + (distance[1] < 0 ? CHUNK_SIZE - zChunkInnerCoord - 0.5 : distance[1] > 0 ? -zChunkInnerCoord + 0.5 : 0);
            double distanceSquared = Math.pow(xDistance, 2) + Math.pow(zDistance, 2);
            if (distanceSquared < minDistance){
                minDistance = distanceSquared;
                vector = new Vector(xDistance, 0, zDistance);
            }
        }
        return vector;
    }

    public void exilePlayer(@NotNull Player player){
        exilePlayer(player, player.getLocation());
    }
    public void exilePlayer(@NotNull Player player, @NotNull Location crashLocation){
        ArrayList<ChunkPos> chunks = new ArrayList<>();
        Location location = crashLocation.clone();
        while (isPlayerCrasher(player, location)){
            Castle castle = getCastleByLocation(location);
            chunks.addAll(castle.chunks);
            Vector vector = getShortestVectorToBorder(crashLocation, chunks);
            location = crashLocation.add(vector);
        }
        player.teleport(getSafeDestination(location));
    }

    // BlockEvent
    @EventHandler
    public void onBeaconEffect(BeaconEffectEvent e){
        if (isPlayerProtected(e.getPlayer())){
            if (badEffects.contains(e.getEffect().getType())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent e){
        if (isLocationProtected(e.getBlock().getLocation())){
            if (e.getIgnitingBlock() != null && isOriginOutsideCastle(e.getBlock().getLocation(), e.getIgnitingBlock().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getBlock().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e){
        if (isLocationProtected(e.getBlock().getLocation().add(e.getVelocity()))){
            if (isOriginOutsideCastle(e.getBlock().getLocation().add(e.getVelocity()), e.getBlock().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e){
        List<Block> protectedBlocks = new ArrayList<>();
        for (Block b : e.blockList()){
            if (isLocationProtected(b.getLocation())){
                if (isOriginOutsideCastle(b.getLocation(), e.getBlock().getLocation())){
                    protectedBlocks.add(b);
                }
            }
        }
        e.blockList().removeAll(protectedBlocks);
    }

    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent e){
        for (BlockState b : e.getBlocks()){
            if (isLocationProtected(b.getLocation())){
                if (isOriginOutsideCastle(b.getLocation(), e.getBlock().getLocation())){
                    e.getBlocks().remove(b);
                }
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent e){
        if (isLocationProtected(e.getToBlock().getLocation())){
            if (isOriginOutsideCastle(e.getToBlock().getLocation(), e.getBlock().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent e){
        if (isLocationProtected(e.getBlock().getLocation())){
            if (isOriginOutsideCastle(e.getBlock().getLocation(), e.getSource().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e){
        if (isLocationProtected(e.getBlock().getLocation())){
            if (e.getIgnitingBlock() != null && isOriginOutsideCastle(e.getBlock().getLocation(), e.getIgnitingBlock().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent e){
        if (isLocationProtected(e.getBlock().getLocation())){
            if (isOriginOutsideCastle(e.getBlock().getLocation(), e.getSourceBlock().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent e){
        for (Block b : e.getBlocks()){
            if (isLocationProtected(b.getLocation())){
                if (isOriginOutsideCastle(b.getLocation(), e.getBlock().getLocation())){
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent e){
        for (Block b : e.getBlocks()){
            if (isLocationProtected(b.getLocation())){
                if (isOriginOutsideCastle(b.getLocation(), e.getBlock().getLocation())){
                    e.getBlocks().remove(b);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getBlockPlaced().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockReceiveGameEvent(BlockReceiveGameEvent e){
        if (isLocationProtected(e.getBlock().getLocation())){
            if (e.getEntity() instanceof Player){
                if (isPlayerCrasher((Player) e.getEntity(), e.getBlock().getLocation())){
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockShearEntity(BlockShearEntityEvent e){
        if (isLocationProtected(e.getEntity().getLocation())){
            if (isOriginOutsideCastle(e.getEntity().getLocation(), e.getBlock().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    // entity event
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player){
            if (isPlayerCrasher((Player) e.getDamager(), e.getEntity().getLocation())){
                e.setCancelled(true);
            }
        } else if (e.getDamager() instanceof Projectile){
            ProjectileSource source = ((Projectile) e.getDamager()).getShooter();
            if (source instanceof Player){
                if (isPlayerCrasher((Player) source, e.getEntity().getLocation())){
                    e.setCancelled(true);
                }
            }
        } else if (e.getDamager() instanceof TNTPrimed) {
            if (((TNTPrimed) e.getDamager()).getSource() instanceof Player) {
                if (isPlayerCrasher((Player) ((TNTPrimed) e.getDamager()).getSource(), e.getEntity().getLocation())) {
                    e.setCancelled(true);
                }
            } else if (isOriginOutsideCastle(e.getEntity().getLocation(), e.getDamager().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent e) {
        if (e.getDismounted() instanceof Player) {
            if (isPlayerCrasher((Player) e.getDismounted(), e.getEntity().getLocation())) {
                exilePlayer((Player) e.getDismounted());
            }
        }
    }

    @EventHandler
    public void onEntityDye(EntityDyeEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getEntity().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e){
        List<Block> protectedBlocks = new ArrayList<>();
        Location origin = e.getLocation();
        if (e.getEntity() instanceof TNTPrimed){
            origin = e.getEntity().getOrigin();
        }
        for (Block b : e.blockList()){
            if (isLocationProtected(b.getLocation())){
                if (isOriginOutsideCastle(b.getLocation(), origin)){
                    protectedBlocks.add(b);
                }
            }
        }
        e.blockList().removeAll(protectedBlocks);
    }

    @EventHandler
    public void onEntityMount(EntityMountEvent e){
        if (e.getMount() instanceof Player){
            if (isPlayerCrasher((Player) e.getMount(), e.getEntity().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityMove(EntityMoveEvent e){
        if (isLocationProtected(e.getTo())){
            List<Entity> passengers = e.getEntity().getPassengers();
            if (passengers.size() > 0){
                for (Entity p : passengers){
                    if (p instanceof Player){
                        if (isPlayerCrasher((Player) p, e.getTo())){
                            e.getEntity().removePassenger(p);
                            exilePlayer((Player) p);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityPlace(EntityPlaceEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getEntity().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent e){
        if (isPlayerCrasher((Player) e.getOwner(), e.getEntity().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e){
        if (e.getTarget() instanceof Player){
            if (isPlayerCrasher((Player) e.getTarget(), e.getEntity().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent e){
        if (isLocationProtected(e.getTo())){
            List<Entity> passengers = e.getEntity().getPassengers();
            if (passengers.size() > 0){
                for (Entity p : passengers){
                    if (p instanceof Player){
                        if (isPlayerCrasher((Player) p, e.getTo())){
                            e.getEntity().removePassenger(p);
                            exilePlayer((Player) p);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e){
        if (e.getEntity().getShooter() instanceof Player){
            if (isPlayerCrasher((Player) e.getEntity().getShooter(), e.getEntity().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    // player event
    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getBed().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getBed().getLocation())){
            exilePlayer(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getBlockClicked().getRelative(e.getBlockFace()).getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getBlockClicked().getRelative(e.getBlockFace()).getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getItemDrop().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerEggThrow(PlayerEggThrowEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getEgg().getLocation())){
            e.setHatching(false);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getHook().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerHarvestBlock(PlayerHarvestBlockEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getHarvestedBlock().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getRightClicked().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemFrameChange(PlayerItemFrameChangeEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getItemFrame().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getTo())){
            if (isPlayerCrasher(e.getPlayer(), e.getFrom())){
                exilePlayer(e.getPlayer());
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerNameEntity(PlayerNameEntityEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getEntity().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupExperience(PlayerPickupExperienceEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getExperienceOrb().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent e){
        if (e.getEntity() instanceof Player){
            if (isPlayerCrasher((Player) e.getEntity(), e.getItem().getLocation())){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerPickupArrow(PlayerPickupArrowEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getArrow().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerShearEntity(PlayerShearEntityEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getEntity().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerShearBlock(PlayerShearBlockEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getBlock().getLocation())){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e){
        if (isPlayerCrasher(e.getPlayer(), e.getTo())){
            e.setCancelled(true);
            exilePlayer(e.getPlayer(), e.getTo());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (e.getClickedBlock() != null){
            if (isPlayerCrasher(e.getPlayer(), e.getClickedBlock().getLocation())){
                if (e.getPlayer().getPose() != Pose.SNEAKING && e.getClickedBlock().getType().isInteractable() && e.getAction() == Action.RIGHT_CLICK_BLOCK){
                    e.setCancelled(true);
                }
            }
        }
    }

    // vehicle event
    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent e) {
        if (e.getAttacker() instanceof Player) {
            if (isPlayerCrasher((Player) e.getAttacker(), e.getVehicle().getLocation())) {
                e.setCancelled(true);
            }
        } else if (e.getAttacker() instanceof Projectile) {
            if (((Projectile) e.getAttacker()).getShooter() instanceof Player) {
                if (isPlayerCrasher((Player) ((Projectile) e.getAttacker()).getShooter(), e.getVehicle().getLocation())) {
                    e.setCancelled(true);
                }
            }
        } else if (e.getAttacker() instanceof TNTPrimed) {
            if (((TNTPrimed) e.getAttacker()).getSource() instanceof Player) {
                if (isPlayerCrasher((Player) ((TNTPrimed) e.getAttacker()).getSource(), e.getVehicle().getLocation())) {
                    e.setCancelled(true);
                }
            } else if (isOriginOutsideCastle(e.getVehicle().getLocation(), e.getAttacker().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent e) {
        if (e.getAttacker() instanceof Player) {
            if (isPlayerCrasher((Player) e.getAttacker(), e.getVehicle().getLocation())) {
                e.setCancelled(true);
            }
        } else if (e.getAttacker() instanceof Projectile) {
            if (((Projectile) e.getAttacker()).getShooter() instanceof Player) {
                if (isPlayerCrasher((Player) ((Projectile) e.getAttacker()).getShooter(), e.getVehicle().getLocation())) {
                    e.setCancelled(true);
                }
            }
        } else if (e.getAttacker() instanceof TNTPrimed) {
            if (((TNTPrimed) e.getAttacker()).getSource() instanceof Player) {
                if (isPlayerCrasher((Player) ((TNTPrimed) e.getAttacker()).getSource(), e.getVehicle().getLocation())) {
                    e.setCancelled(true);
                }
            } else if (isOriginOutsideCastle(e.getVehicle().getLocation(), e.getAttacker().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e) {
        if (e.getEntered() instanceof Player) {
            if (isPlayerCrasher((Player) e.getEntered(), e.getVehicle().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent e) {
        if (e.getExited() instanceof Player) {
            if (isPlayerCrasher((Player) e.getExited(), e.getVehicle().getLocation())) {
                exilePlayer((Player) e.getExited());
            }
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent e) {
        if (isLocationProtected(e.getTo())) {
            List<Entity> passengers = e.getVehicle().getPassengers();
            for (Entity p : passengers) {
                if (p instanceof Player) {
                    if (isPlayerCrasher((Player) p, e.getTo())) {
                        e.getVehicle().removePassenger(p);
                        exilePlayer((Player) p);
                    }
                }
            }
        }
    }
}
