package net.minebo.cobalt.projectile;

import net.minebo.cobalt.util.LocationUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class BlockProjectile implements Listener {

    JavaPlugin plugin;
    Material blockMaterial;
    double velocityMod;
    String metadataKey = null;

    public BlockProjectile(JavaPlugin plugin, Material blockMaterial, double velocityMod) {
        this.plugin = plugin;
        this.blockMaterial = blockMaterial;
        this.velocityMod = velocityMod;
    }

    public BlockProjectile withMetadata(String key) {
        this.metadataKey = key;
        return this;
    }

    public FallingBlock shoot(Player shooter) {
        Location eye = shooter.getEyeLocation();
        Vector direction = eye.getDirection();

        Location spawnLoc = LocationUtil.getForwardOffset(eye, direction, 1.5); // 1.5 blocks in front of eyes

        return worldShoot(shooter, blockMaterial.createBlockData(), velocityMod, direction, spawnLoc);
    }

    private FallingBlock worldShoot(Player shooter, BlockData blockData, double velocityMod, Vector direction, Location spawnLocation) {
        World world = shooter.getWorld();
        FallingBlock blockProjectile = world.spawnFallingBlock(spawnLocation, blockData);
        blockProjectile.setDropItem(false);
        blockProjectile.setVelocity(direction.normalize().multiply(velocityMod));
        // Set metadata if provided
        if (metadataKey != null) {
            blockProjectile.setMetadata(metadataKey, new FixedMetadataValue(plugin, true));
        }
        trackProjectile(blockProjectile, shooter);
        return blockProjectile;
    }

    private void trackProjectile(FallingBlock projectile, Player shooter) {
        new BukkitRunnable() {
            int ticks = 0;
            Location prevPos = projectile.getLocation().clone();
            @Override
            public void run() {
                if (!projectile.isValid()) {
                    cancel();
                    return;
                }

                Vector velocity = projectile.getVelocity();
                if (velocity.lengthSquared() == 0) return;
                ticks++;

                Location currPos = projectile.getLocation();
                // The bounding box for a FallingBlock. Adjust size if needed for visuals.
                BoundingBox projBB = BoundingBox.of(currPos, 0.25, 0.25, 0.25);

                // Scan all blocks in a 3x3x3 area around the projectile
                int px = currPos.getBlockX();
                int py = currPos.getBlockY();
                int pz = currPos.getBlockZ();

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Block block = currPos.getWorld().getBlockAt(px+dx, py+dy, pz+dz);
                            BlockData data = block.getBlockData();
                            Material mat = data.getMaterial();
                            if (mat.isAir()) continue;

                            // Full block collision (handled by vanilla, but let's check anyway)
                            if (data.isOccluding() && mat.isSolid()) {
                                BoundingBox blockBB = BoundingBox.of(block);
                                if (blockBB.overlaps(projBB)) {
                                    // Collision!
                                    Bukkit.getPluginManager().callEvent(
                                            new BlockProjectileHitEvent(projectile, shooter, null, currPos)
                                    );
                                    projectile.remove();
                                    cancel();
                                    return;
                                }
                                continue;
                            }

                            // Check partial/collision shapes (stairs, hoppers, slabs etc.)
                            try {
                                // Paper API: block.getCollisionShape() returns a list of BoundingBox objects.
                                if (block.getCollisionShape() != null && !block.getCollisionShape().getBoundingBoxes().isEmpty()) {
                                    for (BoundingBox shapeBB : block.getCollisionShape().getBoundingBoxes()) {
                                        BoundingBox worldBB = shapeBB.shift(block.getX(), block.getY(), block.getZ());
                                        if (worldBB.overlaps(projBB)) {
                                            Bukkit.getPluginManager().callEvent(
                                                    new BlockProjectileHitEvent(projectile, shooter, null, currPos)
                                            );
                                            projectile.remove();
                                            cancel();
                                            return;
                                        }
                                    }
                                } else {
                                    // Fallback: treat as full block if no special shape information
                                    BoundingBox blockBB = BoundingBox.of(block);
                                    if (blockBB.overlaps(projBB)) {
                                        Bukkit.getPluginManager().callEvent(
                                                new BlockProjectileHitEvent(projectile, shooter, null, currPos)
                                        );
                                        projectile.remove();
                                        cancel();
                                        return;
                                    }
                                }
                            } catch (Throwable ignore) {
                                // Old Spigot fallback: treat as full block
                                BoundingBox blockBB = BoundingBox.of(block);
                                if (blockBB.overlaps(projBB)) {
                                    Bukkit.getPluginManager().callEvent(
                                            new BlockProjectileHitEvent(projectile, shooter, null, currPos)
                                    );
                                    projectile.remove();
                                    cancel();
                                    return;
                                }
                            }
                        }
                    }
                }

                // Entity raytracing/collision
                if (ticks > 2) {
                    RayTraceResult entityHit = currPos.getWorld().rayTraceEntities(
                            prevPos, velocity.normalize(), velocity.length(),
                            entity -> entity instanceof Player && !entity.equals(shooter)
                    );
                    if (entityHit != null && entityHit.getHitEntity() != null) {
                        projectile.teleport(entityHit.getHitPosition().toLocation(projectile.getWorld()));
                        Bukkit.getPluginManager().callEvent(
                                new BlockProjectileHitEvent(projectile, shooter, entityHit.getHitEntity(), entityHit.getHitPosition().toLocation(projectile.getWorld()))
                        );
                        projectile.remove();
                        cancel();
                        return;
                    }
                }

                // Standing/landing fallback
                if (projectile.isOnGround()) {
                    Bukkit.getPluginManager().callEvent(
                            new BlockProjectileHitEvent(projectile, shooter, null, projectile.getLocation())
                    );
                    projectile.remove();
                    cancel();
                    return;
                }
                prevPos = projectile.getLocation().clone();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public static class BlockProjectileHitEvent extends Event {
        private static final HandlerList HANDLERS = new HandlerList();
        private final FallingBlock projectile;
        private final Player shooter;
        private final Entity hitEntity; // null if block/ground
        private final Location hitLocation;

        public BlockProjectileHitEvent(FallingBlock projectile, Player shooter, Entity hitEntity, Location hitLocation) {
            this.projectile = projectile;
            this.shooter = shooter;
            this.hitEntity = hitEntity;
            this.hitLocation = hitLocation;
        }

        public FallingBlock getProjectile() { return projectile; }
        public Player getShooter() { return shooter; }
        public Entity getHitEntity() { return hitEntity; }
        public Location getHitLocation() { return hitLocation; }

        @Override public HandlerList getHandlers() { return HANDLERS; }
        public static HandlerList getHandlerList() { return HANDLERS; }
    }
}