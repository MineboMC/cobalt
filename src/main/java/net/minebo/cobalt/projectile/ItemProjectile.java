package net.minebo.cobalt.projectile;

import lombok.Getter;
import net.minebo.cobalt.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ItemProjectile implements Listener {

    JavaPlugin plugin;
    Material material;
    double velocityMod;

    public ItemProjectile(JavaPlugin plugin, Material material, double velocityMod) {
        this.plugin = plugin;
        this.material = material;
        this.velocityMod = velocityMod;
    }

    /**
     * Shoot a projectile.
     * @param shooter The player who fires the projectile.
     */
    public void shoot(Player shooter) {
        Location start = shooter.getEyeLocation();
        worldShoot(shooter, new ItemBuilder(material).build(), velocityMod, start.getDirection(), start);
    }

    /**
     * Internal method to create, launch, and handle projectile tracking.
     */
    private void worldShoot(Player shooter, ItemStack stack, double velocityMod, Vector direction, Location spawnLocation) {
        World world = shooter.getWorld();
        spawnLocation.setY(spawnLocation.getY() - 0.4);
        Item itemProjectile = world.dropItem(spawnLocation, stack);
        itemProjectile.setPickupDelay(Integer.MAX_VALUE); // No pickup allowed
        itemProjectile.setVelocity(direction.normalize().multiply(velocityMod));
        trackProjectile(itemProjectile, shooter);
    }

    /**
     * Tracks the projectile each tick.
     * Uses Paper's rayTraceEntities for accurate hit detection.
     * Fires CustomItemProjectileHitEvent on hit.
     */
    private void trackProjectile(Item projectile, Player shooter) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!projectile.isValid()) {
                    cancel();
                    return;
                }

                Vector velocity = projectile.getVelocity();
                Location pos = projectile.getLocation();
                double distance = velocity.length();
                if (distance == 0) return;

                // Entity raytrace (already present)
                RayTraceResult entityHit = projectile.getWorld().rayTraceEntities(
                        pos, velocity.normalize(), distance,
                        entity -> entity instanceof Player && !entity.equals(shooter)
                );
                if (entityHit != null && entityHit.getHitEntity() != null) {
                    Bukkit.getPluginManager().callEvent(
                            new ItemProjectileHitEvent(projectile, shooter, entityHit.getHitEntity(), entityHit.getHitPosition().toLocation(projectile.getWorld()))
                    );
                    projectile.remove();
                    cancel();
                    return;
                }

                // NEW: Block raytrace, catches walls and ground BEFORE bouncing/skidding
                RayTraceResult blockHit = projectile.getWorld().rayTraceBlocks(
                        pos, velocity.normalize(), distance
                );
                if (blockHit != null) {
                    Bukkit.getPluginManager().callEvent(
                            new ItemProjectileHitEvent(projectile, shooter, null, blockHit.getHitPosition().toLocation(projectile.getWorld()))
                    );
                    projectile.remove();
                    cancel();
                    return;
                }

                // If no collision, original isOnGround check is now a fallback
                if (projectile.isOnGround()) {
                    Bukkit.getPluginManager().callEvent(
                            new ItemProjectileHitEvent(projectile, shooter, null, projectile.getLocation())
                    );
                    projectile.remove();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    /**
     * Example listener for the custom hit event.
     * Use this as a template for custom effects (damage, particles, etc).
     */
    @EventHandler
    public void onProjectileHit(ItemProjectileHitEvent event) {
        Player shooter = event.getShooter();
        Entity hit = event.getHitEntity();
        if (hit instanceof Player target) {
            target.damage(5.0, shooter);
            shooter.sendMessage("Hit " + target.getName());
        } else {
            shooter.sendMessage("Projectile landed at: " +
                    event.getHitLocation().getBlockX() + ", " +
                    event.getHitLocation().getBlockY() + ", " +
                    event.getHitLocation().getBlockZ()
            );
        }
    }

    /**
     * Custom event fired when our item projectile hits something.
     */
    @Getter
    public static class ItemProjectileHitEvent extends Event {
        private static final HandlerList HANDLERS = new HandlerList();
        private final Item projectile;
        private final Player shooter;
        private final Entity hitEntity;   // null if hit ground
        private final Location hitLocation;

        public ItemProjectileHitEvent(Item projectile, Player shooter, Entity hitEntity, Location hitLocation) {
            this.projectile = projectile;
            this.shooter = shooter;
            this.hitEntity = hitEntity;
            this.hitLocation = hitLocation;
        }

        @Override public HandlerList getHandlers() { return HANDLERS; }
        public static HandlerList getHandlerList() { return HANDLERS; }
    }

}
