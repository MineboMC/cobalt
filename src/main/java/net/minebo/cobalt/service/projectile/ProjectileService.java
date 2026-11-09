package net.minebo.cobalt.service.projectile;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.CService;
import net.minebo.cobalt.service.projectile.trail.Trail;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class ProjectileService extends CService {

    private final Map<String, ProjectileType> types = new LinkedHashMap<>();
    private final Map<String, Trail> trails = new LinkedHashMap<>();
    private final Map<UUID, ActiveProjectile> active = new LinkedHashMap<>();
    private NamespacedKey projectileKey;
    private BukkitTask task;

    @Override
    public String getName() {
        return "Projectiles";
    }

    @Override
    public void onEnable() {
        projectileKey = new NamespacedKey(Cobalt.getInstance(), "projectile");
        task = Bukkit.getScheduler().runTaskTimer(Cobalt.getInstance(), () -> {
            for (ActiveProjectile projectile : List.copyOf(active.values())) {
                projectile.tick();
            }
        }, 1L, 1L);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
        }
        for (ActiveProjectile projectile : List.copyOf(active.values())) {
            projectile.remove();
        }
        active.clear();
    }

    public void registerType(String id, ProjectileType type) { types.put(id.toLowerCase(), type); }
    public void unregisterType(String id) { types.remove(id.toLowerCase()); }
    public ProjectileType getType(String id) { return types.get(id.toLowerCase()); }
    public Map<String, ProjectileType> types() { return Collections.unmodifiableMap(types); }

    public void registerTrail(String id, Trail trail) { trails.put(id.toLowerCase(), trail); }
    public void unregisterTrail(String id) { trails.remove(id.toLowerCase()); }
    public Trail getTrail(String id) { return trails.get(id.toLowerCase()); }
    public Map<String, Trail> trails() { return Collections.unmodifiableMap(trails); }

    public ActiveProjectile launch(String typeId, LivingEntity shooter) {
        ProjectileType type = getType(typeId);
        return type == null ? null : launch(type, shooter);
    }

    public ActiveProjectile launch(ProjectileType type, LivingEntity shooter) {
        Location eye = shooter.getEyeLocation();
        Vector direction = eye.getDirection();
        Location origin = eye.clone()
                .add(direction.clone().multiply(type.forwardOffset))
                .add(0, type.verticalOffset, 0);
        return launch(type, origin, direction.multiply(type.speed), shooter);
    }

    public ActiveProjectile launch(ProjectileType type, Location origin, Vector velocity, Entity shooter) {
        Vector launchVelocity = applySpread(velocity, type.spread);
        Location spawn = origin.clone();
        if (launchVelocity.lengthSquared() > 1.0E-6) {
            spawn.setDirection(launchVelocity);
        }

        Display display = spawnDisplay(type, spawn);
        ActiveProjectile projectile = new ActiveProjectile(this, type, shooter, origin, launchVelocity, display);
        display.getPersistentDataContainer().set(projectileKey, PersistentDataType.STRING, projectile.id().toString());
        active.put(projectile.id(), projectile);

        projectile.playSound(type.launchSound, origin);
        if (type.onLaunch != null) {
            type.onLaunch.accept(projectile);
        }
        return projectile;
    }

    public Collection<ActiveProjectile> active() {
        return Collections.unmodifiableCollection(active.values());
    }

    public ActiveProjectile byId(UUID id) {
        return active.get(id);
    }

    public ActiveProjectile byEntity(Entity entity) {
        String id = entity.getPersistentDataContainer().get(projectileKey, PersistentDataType.STRING);
        return id == null ? null : active.get(UUID.fromString(id));
    }

    void untrack(ActiveProjectile projectile) {
        active.remove(projectile.id());
    }

    private Display spawnDisplay(ProjectileType type, Location location) {
        Consumer<Display> setup = display -> {
            display.setPersistent(false);
            display.setTeleportDuration(1);
            display.setTransformation(ActiveProjectile.transformation(type, 0));
            if (type.glowColor != null) {
                display.setGlowing(true);
                display.setGlowColorOverride(type.glowColor);
            }
        };

        if (type.isBlock()) {
            return location.getWorld().spawn(location, BlockDisplay.class, display -> {
                display.setBlock(type.block);
                setup.accept(display);
            });
        }
        return location.getWorld().spawn(location, ItemDisplay.class, display -> {
            display.setItemStack(type.item);
            setup.accept(display);
        });
    }

    private static Vector applySpread(Vector velocity, double degrees) {
        Vector result = velocity.clone();
        if (degrees <= 0 || result.lengthSquared() < 1.0E-6) {
            return result;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Vector dir = result.clone().normalize();
        Vector axis = Math.abs(dir.getY()) > 0.99 ? new Vector(1, 0, 0) : new Vector(0, 1, 0);
        Vector perpendicular = dir.clone().crossProduct(axis).normalize()
                .rotateAroundAxis(dir, random.nextDouble() * Math.PI * 2);
        return result.rotateAroundAxis(perpendicular, Math.toRadians(random.nextDouble() * degrees));
    }
}
