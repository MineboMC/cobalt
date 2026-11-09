package net.minebo.cobalt.service.projectile;

import net.minebo.cobalt.service.projectile.event.CobaltProjectileHitEvent;
import net.minebo.cobalt.service.projectile.trail.Trail;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ActiveProjectile {

    private static final int SHOOTER_GRACE_TICKS = 5;
    private static final int MAX_HITS_PER_TICK = 8;

    private final UUID id = UUID.randomUUID();
    private final ProjectileService service;
    private final ProjectileType type;
    private final Entity shooter;
    private final World world;
    private final Display display;
    private final Map<String, Object> metadata = new HashMap<>();
    private final Set<UUID> hitEntities = new HashSet<>();

    private Vector position;
    private Vector velocity;
    private int ticksLived;
    private double distanceTravelled;
    private int pierceLeft;
    private int bouncesLeft;
    private float spinAngle;
    private boolean removed;

    ActiveProjectile(ProjectileService service, ProjectileType type, Entity shooter, Location origin, Vector velocity, Display display) {
        this.service = service;
        this.type = type;
        this.shooter = shooter;
        this.world = origin.getWorld();
        this.position = origin.toVector();
        this.velocity = velocity.clone();
        this.display = display;
        this.pierceLeft = type.pierce;
        this.bouncesLeft = type.bounces;
    }

    public UUID id() { return id; }
    public ProjectileType type() { return type; }
    public Entity shooter() { return shooter; }
    public World world() { return world; }
    public Display display() { return display; }
    public Location location() { return position.toLocation(world); }
    public Vector velocity() { return velocity.clone(); }
    public int ticksLived() { return ticksLived; }
    public double distanceTravelled() { return distanceTravelled; }
    public boolean isRemoved() { return removed; }

    public void setVelocity(Vector velocity) { this.velocity = velocity.clone(); }

    public Map<String, Object> metadata() { return metadata; }

    @SuppressWarnings("unchecked")
    public <T> T metadata(String key) { return (T) metadata.get(key); }

    public void remove() {
        if (removed) {
            return;
        }
        removed = true;
        if (display.isValid()) {
            display.remove();
        }
        service.untrack(this);
    }

    void tick() {
        if (removed) {
            return;
        }
        if (!display.isValid() || !world.isChunkLoaded(position.getBlockX() >> 4, position.getBlockZ() >> 4)) {
            remove();
            return;
        }

        ticksLived++;
        if (type.onTick != null) {
            type.onTick.accept(this);
            if (removed) {
                return;
            }
        }

        Vector from = position.clone();
        double length = velocity.length();
        if (length > 1.0E-6) {
            move(from, velocity.clone().multiply(1.0 / length), length);
            if (removed) {
                return;
            }
        }

        velocity.multiply(type.drag).setY(velocity.getY() - type.gravity);
        updateDisplay();

        if (ticksLived >= type.maxLifetime || (type.maxDistance > 0 && distanceTravelled >= type.maxDistance)) {
            expire();
        }
    }

    private void move(Vector from, Vector dir, double length) {
        for (int i = 0; i < MAX_HITS_PER_TICK; i++) {
            RayTraceResult blockHit = type.collideWithBlocks
                    ? world.rayTraceBlocks(from.toLocation(world), dir, length, type.fluidCollision, type.ignorePassableBlocks)
                    : null;
            RayTraceResult entityHit = world.rayTraceEntities(from.toLocation(world), dir, length, type.hitboxRadius, this::canHit);

            RayTraceResult nearest = nearest(from, blockHit, entityHit);
            if (nearest == null) {
                advanceTo(from.clone().add(dir.clone().multiply(length)));
                return;
            }

            Vector hitPos = nearest.getHitPosition();
            if (nearest == entityHit) {
                handleEntityHit(entityHit.getHitEntity(), hitPos, dir);
                if (removed) {
                    return;
                }
                // Pierced or cancelled: the entity is now excluded, trace the same segment again.
                continue;
            }

            advanceTo(hitPos);
            handleBlockHit(blockHit, dir);
            return;
        }
        advanceTo(from.clone().add(dir.clone().multiply(length)));
    }

    private void handleEntityHit(Entity entity, Vector hitPos, Vector dir) {
        hitEntities.add(entity.getUniqueId());
        ProjectileHit hit = ProjectileHit.entity(entity, hitPos.toLocation(world));
        if (!callHitEvent(hit)) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            if (type.damage > 0) {
                if (type.ignoreNoDamageTicks) {
                    living.setNoDamageTicks(0);
                }
                if (shooter != null) {
                    living.damage(type.damage, shooter);
                } else {
                    living.damage(type.damage);
                }
            }
            if (type.knockback != 0) {
                Vector push = dir.clone().setY(0);
                if (push.lengthSquared() > 1.0E-6) {
                    living.setVelocity(living.getVelocity().add(push.normalize().multiply(type.knockback).setY(0.1 * Math.abs(type.knockback))));
                }
            }
        }

        if (type.onHitEntity != null) {
            type.onHitEntity.accept(this, hit);
        }

        if (pierceLeft > 0) {
            pierceLeft--;
            return;
        }
        advanceTo(hitPos);
        impact(hit.location());
        remove();
    }

    private void handleBlockHit(RayTraceResult result, Vector dir) {
        ProjectileHit hit = ProjectileHit.block(result.getHitBlock(), result.getHitBlockFace(), result.getHitPosition().toLocation(world));
        if (!callHitEvent(hit)) {
            remove();
            return;
        }

        if (type.onHitBlock != null) {
            type.onHitBlock.accept(this, hit);
            if (removed) {
                return;
            }
        }

        if (bouncesLeft > 0 && hit.face() != null) {
            bouncesLeft--;
            Vector normal = hit.face().getDirection();
            double speed = velocity.length() * type.bounciness;
            Vector reflected = dir.clone().subtract(normal.clone().multiply(2 * dir.dot(normal))).normalize();
            velocity = reflected.multiply(speed);
            // Nudge off the surface so the next trace doesn't start inside the block.
            position = position.clone().add(normal.multiply(0.05));
            playSound(type.hitSound, hit.location());
            return;
        }

        impact(hit.location());
        remove();
    }

    private void expire() {
        if (type.onExpire != null) {
            type.onExpire.accept(this);
        }
        remove();
    }

    private boolean callHitEvent(ProjectileHit hit) {
        CobaltProjectileHitEvent event = new CobaltProjectileHitEvent(this, hit);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private void impact(Location location) {
        playSound(type.hitSound, location);
        for (Trail trail : type.trails) {
            trail.impact(this, location);
        }
    }

    void playSound(org.bukkit.Sound sound, Location location) {
        if (sound != null) {
            world.playSound(location, sound, type.soundVolume, type.soundPitch);
        }
    }

    private void advanceTo(Vector to) {
        double segment = to.distance(position);
        if (segment > 1.0E-6) {
            Location fromLoc = position.toLocation(world);
            Location toLoc = to.toLocation(world);
            for (Trail trail : type.trails) {
                trail.render(this, fromLoc, toLoc, distanceTravelled);
            }
        }
        distanceTravelled += segment;
        position = to.clone();
    }

    private boolean canHit(Entity entity) {
        if (entity == display || hitEntities.contains(entity.getUniqueId())) {
            return false;
        }
        if (shooter != null && entity.getUniqueId().equals(shooter.getUniqueId())) {
            if (!type.canHitShooter || ticksLived <= SHOOTER_GRACE_TICKS) {
                return false;
            }
        }
        return type.entityFilter.test(entity);
    }

    private static RayTraceResult nearest(Vector from, RayTraceResult a, RayTraceResult b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.getHitPosition().distanceSquared(from) <= b.getHitPosition().distanceSquared(from) ? a : b;
    }

    private void updateDisplay() {
        Location target = position.toLocation(world);
        if (type.faceVelocity && velocity.lengthSquared() > 1.0E-6) {
            target.setDirection(velocity);
        } else {
            target.setYaw(display.getYaw());
            target.setPitch(display.getPitch());
        }
        display.teleport(target);

        if (type.spinDegrees != 0) {
            spinAngle += (float) Math.toRadians(type.spinDegrees);
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(1);
            display.setTransformation(transformation(type, spinAngle));
        }
    }

    /** Model transform: scaled, rotated about its centre (block displays are corner-anchored). */
    static Transformation transformation(ProjectileType type, float angle) {
        Quaternionf rotation = new Quaternionf(new AxisAngle4f(angle,
                (float) type.spinAxis.getX(), (float) type.spinAxis.getY(), (float) type.spinAxis.getZ()));
        Vector3f translation = new Vector3f();
        if (type.isBlock()) {
            float half = -0.5F * type.scale;
            translation = rotation.transform(new Vector3f(half, half, half));
        }
        return new Transformation(translation, rotation, new Vector3f(type.scale), new Quaternionf());
    }
}
