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

import java.util.Collections;
import java.util.List;

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

        Location spawnLoc = LocationUtil.getForwardOffset(eye, direction, 0.5); // 1.5 blocks in front of eyes

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
            // Use a larger projectile hitbox (radius 1.5 = 3x3x3 box)
            final double PROJECTILE_RADIUS_X = 1.5, PROJECTILE_RADIUS_Y = 1.5, PROJECTILE_RADIUS_Z = 1.5;
            // Player hitbox for self-hit prevention
            final double PLAYER_WIDTH = 0.6, PLAYER_HEIGHT = 1.8, PLAYER_DEPTH = 0.6;

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
                BoundingBox projBB = BoundingBox.of(currPos, PROJECTILE_RADIUS_X, PROJECTILE_RADIUS_Y, PROJECTILE_RADIUS_Z);

                // Self-hit bounding box (optional)
                BoundingBox shooterBB = BoundingBox.of(shooter.getLocation(), PLAYER_WIDTH, PLAYER_HEIGHT, PLAYER_DEPTH);

                // Block collision with expanded projectile hitbox (3x3x3 area)
                int px = currPos.getBlockX();
                int py = currPos.getBlockY();
                int pz = currPos.getBlockZ();

                outer: for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Block block = currPos.getWorld().getBlockAt(px + dx, py + dy, pz + dz);
                            BlockData data = block.getBlockData();
                            Material mat = data.getMaterial();
                            if (mat.isAir()) continue;

                            // Try Paper collision shapes
                            try {
                                List<BoundingBox> shapes = block.getCollisionShape() != null
                                        ? block.getCollisionShape().getBoundingBoxes().stream().toList() : java.util.Collections.emptyList();

                                if (!shapes.isEmpty()) {
                                    for (BoundingBox shapeBB : shapes) {
                                        BoundingBox worldBB = shapeBB.shift(block.getX(), block.getY(), block.getZ());
                                        if (worldBB.overlaps(projBB)) {
                                            // Don't hit the shooter if bounding boxes overlap
                                            if (shooterBB.overlaps(projBB)) continue outer;
                                            Bukkit.getPluginManager().callEvent(
                                                    new BlockProjectileHitEvent(projectile, shooter, null, currPos)
                                            );
                                            projectile.remove();
                                            cancel();
                                            return;
                                        }
                                    }
                                    continue;
                                }
                            } catch (Throwable ignore) {
                                // API fallback
                            }

                            // Full block occlusion fallback
                            if (data.isOccluding() && mat.isSolid()) {
                                BoundingBox blockBB = BoundingBox.of(block);
                                if (blockBB.overlaps(projBB)) {
                                    if (shooterBB.overlaps(projBB)) continue;
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

                // Entity collision (expand hitbox!)
                if (ticks > 2) {
                    // Use a "big projectile box" for area hit similar to getNearbyEntities(1.5, 1.5, 1.5)
                    for (Entity entity : projectile.getWorld().getNearbyEntities(currPos, PROJECTILE_RADIUS_X, PROJECTILE_RADIUS_Y, PROJECTILE_RADIUS_Z)) {
                        if (!(entity instanceof Player playerHit)) continue;
                        if (playerHit.getUniqueId().equals(shooter.getUniqueId())) continue; // No self-hit
                        // Optionally, check player game state, in spawn, etc here
                        // e.g.if (profile1.getProfileState() == ProfileState.SPAWN) continue;

                        // Optionally: use player's bounding box instead of entity center for a more realistic hit
                        BoundingBox playerBB = BoundingBox.of(playerHit.getLocation(), PLAYER_WIDTH, PLAYER_HEIGHT, PLAYER_DEPTH);
                        if (!projBB.overlaps(playerBB)) continue; // Must actually overlap!

                        // Hit!
                        Location hitLoc = playerHit.getLocation().clone();
                        Bukkit.getPluginManager().callEvent(
                                new BlockProjectileHitEvent(projectile, shooter, playerHit, hitLoc)
                        );
                        projectile.remove();
                        cancel();
                        return;
                    }
                }

                // Check for landing/standing on ground
                if (projectile.isOnGround()) {
                    if (!shooterBB.overlaps(projBB)) {
                        Bukkit.getPluginManager().callEvent(
                                new BlockProjectileHitEvent(projectile, shooter, null, projectile.getLocation())
                        );
                    }
                    projectile.remove();
                    cancel();
                    return;
                }

                prevPos = currPos.clone();
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