package net.minebo.cobalt.service.projectile;

import net.minebo.cobalt.service.projectile.trail.Trail;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ProjectileType {

    final ItemStack item;
    final BlockData block;
    final float scale;
    final float spinDegrees;
    final Vector spinAxis;
    final boolean faceVelocity;
    final Color glowColor;

    final double speed;
    final double gravity;
    final double drag;
    final double spread;
    final int maxLifetime;
    final double maxDistance;
    final double forwardOffset;
    final double verticalOffset;

    final double hitboxRadius;
    final boolean collideWithBlocks;
    final boolean ignorePassableBlocks;
    final FluidCollisionMode fluidCollision;
    final Predicate<Entity> entityFilter;
    final boolean canHitShooter;
    final int pierce;
    final int bounces;
    final double bounciness;

    final double damage;
    final double knockback;
    final boolean ignoreNoDamageTicks;

    final Sound launchSound;
    final Sound hitSound;
    final float soundVolume;
    final float soundPitch;

    final List<Trail> trails;

    final Consumer<ActiveProjectile> onLaunch;
    final Consumer<ActiveProjectile> onTick;
    final BiConsumer<ActiveProjectile, ProjectileHit> onHitEntity;
    final BiConsumer<ActiveProjectile, ProjectileHit> onHitBlock;
    final Consumer<ActiveProjectile> onExpire;

    private ProjectileType(Builder b) {
        this.item = b.item;
        this.block = b.block;
        this.scale = b.scale;
        this.spinDegrees = b.spinDegrees;
        this.spinAxis = b.spinAxis;
        this.faceVelocity = b.faceVelocity;
        this.glowColor = b.glowColor;
        this.speed = b.speed;
        this.gravity = b.gravity;
        this.drag = b.drag;
        this.spread = b.spread;
        this.maxLifetime = b.maxLifetime;
        this.maxDistance = b.maxDistance;
        this.forwardOffset = b.forwardOffset;
        this.verticalOffset = b.verticalOffset;
        this.hitboxRadius = b.hitboxRadius;
        this.collideWithBlocks = b.collideWithBlocks;
        this.ignorePassableBlocks = b.ignorePassableBlocks;
        this.fluidCollision = b.fluidCollision;
        this.entityFilter = b.entityFilter;
        this.canHitShooter = b.canHitShooter;
        this.pierce = b.pierce;
        this.bounces = b.bounces;
        this.bounciness = b.bounciness;
        this.damage = b.damage;
        this.knockback = b.knockback;
        this.ignoreNoDamageTicks = b.ignoreNoDamageTicks;
        this.launchSound = b.launchSound;
        this.hitSound = b.hitSound;
        this.soundVolume = b.soundVolume;
        this.soundPitch = b.soundPitch;
        this.trails = List.copyOf(b.trails);
        this.onLaunch = b.onLaunch;
        this.onTick = b.onTick;
        this.onHitEntity = b.onHitEntity;
        this.onHitBlock = b.onHitBlock;
        this.onExpire = b.onExpire;
    }

    public static Builder item(Material material) {
        return item(new ItemStack(material));
    }

    public static Builder item(ItemStack item) {
        Builder b = new Builder();
        b.item = item.clone();
        return b;
    }

    public static Builder block(Material material) {
        return block(material.createBlockData());
    }

    public static Builder block(BlockData block) {
        Builder b = new Builder();
        b.block = block.clone();
        return b;
    }

    public Builder toBuilder() {
        Builder b = new Builder();
        b.item = item;
        b.block = block;
        b.scale = scale;
        b.spinDegrees = spinDegrees;
        b.spinAxis = spinAxis;
        b.faceVelocity = faceVelocity;
        b.glowColor = glowColor;
        b.speed = speed;
        b.gravity = gravity;
        b.drag = drag;
        b.spread = spread;
        b.maxLifetime = maxLifetime;
        b.maxDistance = maxDistance;
        b.forwardOffset = forwardOffset;
        b.verticalOffset = verticalOffset;
        b.hitboxRadius = hitboxRadius;
        b.collideWithBlocks = collideWithBlocks;
        b.ignorePassableBlocks = ignorePassableBlocks;
        b.fluidCollision = fluidCollision;
        b.entityFilter = entityFilter;
        b.canHitShooter = canHitShooter;
        b.pierce = pierce;
        b.bounces = bounces;
        b.bounciness = bounciness;
        b.damage = damage;
        b.knockback = knockback;
        b.ignoreNoDamageTicks = ignoreNoDamageTicks;
        b.launchSound = launchSound;
        b.hitSound = hitSound;
        b.soundVolume = soundVolume;
        b.soundPitch = soundPitch;
        b.trails.addAll(trails);
        b.onLaunch = onLaunch;
        b.onTick = onTick;
        b.onHitEntity = onHitEntity;
        b.onHitBlock = onHitBlock;
        b.onExpire = onExpire;
        return b;
    }

    public boolean isBlock() { return block != null; }
    public ItemStack item() { return item == null ? null : item.clone(); }
    public BlockData block() { return block == null ? null : block.clone(); }
    public double speed() { return speed; }
    public double gravity() { return gravity; }
    public double drag() { return drag; }
    public double spread() { return spread; }
    public int maxLifetime() { return maxLifetime; }
    public double maxDistance() { return maxDistance; }
    public double hitboxRadius() { return hitboxRadius; }
    public int pierce() { return pierce; }
    public int bounces() { return bounces; }
    public double damage() { return damage; }
    public double knockback() { return knockback; }
    public List<Trail> trails() { return trails; }

    public static final class Builder {
        private ItemStack item;
        private BlockData block;
        private float scale = 0.5F;
        private float spinDegrees = 0;
        private Vector spinAxis = new Vector(1, 0, 0);
        private boolean faceVelocity = true;
        private Color glowColor;

        private double speed = 1.5;
        private double gravity = 0.03;
        private double drag = 0.99;
        private double spread = 0;
        private int maxLifetime = 100;
        private double maxDistance = 0;
        private double forwardOffset = 0.5;
        private double verticalOffset = -0.1;

        private double hitboxRadius = 0.25;
        private boolean collideWithBlocks = true;
        private boolean ignorePassableBlocks = true;
        private FluidCollisionMode fluidCollision = FluidCollisionMode.NEVER;
        private Predicate<Entity> entityFilter = entity -> entity instanceof LivingEntity;
        private boolean canHitShooter = false;
        private int pierce = 0;
        private int bounces = 0;
        private double bounciness = 0.6;

        private double damage = 0;
        private double knockback = 0;
        private boolean ignoreNoDamageTicks = false;

        private Sound launchSound;
        private Sound hitSound;
        private float soundVolume = 1.0F;
        private float soundPitch = 1.0F;

        private final List<Trail> trails = new ArrayList<>();

        private Consumer<ActiveProjectile> onLaunch;
        private Consumer<ActiveProjectile> onTick;
        private BiConsumer<ActiveProjectile, ProjectileHit> onHitEntity;
        private BiConsumer<ActiveProjectile, ProjectileHit> onHitBlock;
        private Consumer<ActiveProjectile> onExpire;

        private Builder() {
        }

        public Builder scale(float scale) { this.scale = scale; return this; }
        public Builder spin(float degreesPerTick) { this.spinDegrees = degreesPerTick; return this; }
        public Builder spin(float degreesPerTick, Vector axis) { this.spinDegrees = degreesPerTick; this.spinAxis = axis.clone().normalize(); return this; }
        public Builder faceVelocity(boolean face) { this.faceVelocity = face; return this; }
        public Builder glow(Color color) { this.glowColor = color; return this; }

        public Builder speed(double blocksPerTick) { this.speed = blocksPerTick; return this; }
        public Builder gravity(double gravity) { this.gravity = gravity; return this; }
        public Builder noGravity() { return gravity(0); }
        public Builder drag(double drag) { this.drag = drag; return this; }
        public Builder spread(double degrees) { this.spread = Math.max(0, degrees); return this; }
        public Builder lifetime(int ticks) { this.maxLifetime = Math.max(1, ticks); return this; }
        public Builder maxDistance(double blocks) { this.maxDistance = Math.max(0, blocks); return this; }
        public Builder spawnOffset(double forward, double vertical) { this.forwardOffset = forward; this.verticalOffset = vertical; return this; }

        public Builder hitbox(double radius) { this.hitboxRadius = Math.max(0, radius); return this; }
        public Builder collideWithBlocks(boolean collide) { this.collideWithBlocks = collide; return this; }
        public Builder ignorePassableBlocks(boolean ignore) { this.ignorePassableBlocks = ignore; return this; }
        public Builder fluidCollision(FluidCollisionMode mode) { this.fluidCollision = mode; return this; }
        public Builder hitEntities(Predicate<Entity> filter) { this.entityFilter = filter; return this; }
        public Builder hitPlayersOnly() { return hitEntities(entity -> entity instanceof Player); }
        public Builder hitNoEntities() { return hitEntities(entity -> false); }
        public Builder canHitShooter(boolean canHit) { this.canHitShooter = canHit; return this; }
        public Builder pierce(int entities) { this.pierce = Math.max(0, entities); return this; }
        public Builder bounces(int bounces, double bounciness) { this.bounces = Math.max(0, bounces); this.bounciness = bounciness; return this; }

        public Builder damage(double damage) { this.damage = Math.max(0, damage); return this; }
        public Builder knockback(double strength) { this.knockback = strength; return this; }
        public Builder ignoreNoDamageTicks(boolean ignore) { this.ignoreNoDamageTicks = ignore; return this; }

        public Builder launchSound(Sound sound) { this.launchSound = sound; return this; }
        public Builder hitSound(Sound sound) { this.hitSound = sound; return this; }
        public Builder soundVolume(float volume, float pitch) { this.soundVolume = volume; this.soundPitch = pitch; return this; }

        public Builder trail(Trail trail) { this.trails.add(trail); return this; }
        public Builder trails(Trail... trails) { this.trails.addAll(List.of(trails)); return this; }
        public Builder clearTrails() { this.trails.clear(); return this; }

        public Builder onLaunch(Consumer<ActiveProjectile> callback) { this.onLaunch = callback; return this; }
        public Builder onTick(Consumer<ActiveProjectile> callback) { this.onTick = callback; return this; }
        public Builder onHitEntity(BiConsumer<ActiveProjectile, ProjectileHit> callback) { this.onHitEntity = callback; return this; }
        public Builder onHitBlock(BiConsumer<ActiveProjectile, ProjectileHit> callback) { this.onHitBlock = callback; return this; }
        public Builder onExpire(Consumer<ActiveProjectile> callback) { this.onExpire = callback; return this; }

        public ProjectileType build() {
            return new ProjectileType(this);
        }
    }
}
