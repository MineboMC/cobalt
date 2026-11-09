package net.minebo.cobalt.service.projectile.trail;

import com.destroystokyo.paper.ParticleBuilder;
import net.minebo.cobalt.service.projectile.ActiveProjectile;
import net.minebo.cobalt.util.ParticleEffect;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public final class ParticleTrail implements Trail {

    private final Particle particle;
    private final Object data;
    private final int count;
    private final double offsetX, offsetY, offsetZ;
    private final double extra;
    private final double spacing;
    private final int interval;
    private final int delayTicks;
    private final TrailShape shape;
    private final double radius;
    private final double rotationPerBlock;
    private final int ringPoints;
    private final int viewRange;
    private final boolean force;
    private final Particle impactParticle;
    private final Object impactData;
    private final int impactCount;
    private final double impactSpread;
    private final double impactExtra;

    private ParticleTrail(Builder b) {
        this.particle = b.particle;
        this.data = b.data;
        this.count = b.count;
        this.offsetX = b.offsetX;
        this.offsetY = b.offsetY;
        this.offsetZ = b.offsetZ;
        this.extra = b.extra;
        this.spacing = b.spacing;
        this.interval = b.interval;
        this.delayTicks = b.delayTicks;
        this.shape = b.shape;
        this.radius = b.radius;
        this.rotationPerBlock = b.rotationPerBlock;
        this.ringPoints = b.ringPoints;
        this.viewRange = b.viewRange;
        this.force = b.force;
        this.impactParticle = b.impactParticle;
        this.impactData = b.impactData;
        this.impactCount = b.impactCount;
        this.impactSpread = b.impactSpread;
        this.impactExtra = b.impactExtra;
    }

    public static Builder of(Particle particle) {
        return new Builder(particle);
    }

    public static Builder of(ParticleEffect effect) {
        return new Builder(effect.getBukkitParticle());
    }

    public static Builder dust(Color color, float size) {
        return new Builder(Particle.DUST).data(new Particle.DustOptions(color, size));
    }

    public Builder toBuilder() {
        Builder b = new Builder(particle);
        b.data = data;
        b.count = count;
        b.offsetX = offsetX;
        b.offsetY = offsetY;
        b.offsetZ = offsetZ;
        b.extra = extra;
        b.spacing = spacing;
        b.interval = interval;
        b.delayTicks = delayTicks;
        b.shape = shape;
        b.radius = radius;
        b.rotationPerBlock = rotationPerBlock;
        b.ringPoints = ringPoints;
        b.viewRange = viewRange;
        b.force = force;
        b.impactParticle = impactParticle;
        b.impactData = impactData;
        b.impactCount = impactCount;
        b.impactSpread = impactSpread;
        b.impactExtra = impactExtra;
        return b;
    }

    @Override
    public void render(ActiveProjectile projectile, Location from, Location to, double distance) {
        int age = projectile.ticksLived();
        if (age < delayTicks || age % interval != 0) {
            return;
        }

        Vector start = from.toVector();
        Vector path = to.toVector().subtract(start);
        double length = path.length();
        if (length < 1.0E-6) {
            spawnShape(to, projectile.velocity(), distance);
            return;
        }
        Vector dir = path.clone().multiply(1.0 / length);

        if (spacing <= 0) {
            spawnShape(to, dir, distance + length);
            return;
        }

        // Points sit at fixed multiples of the spacing along the whole flight, so the trail
        // stays evenly spaced across ticks.
        double first = Math.ceil(distance / spacing) * spacing;
        for (double d = first; d < distance + length; d += spacing) {
            Location point = start.clone().add(dir.clone().multiply(d - distance)).toLocation(from.getWorld());
            spawnShape(point, dir, d);
        }
    }

    @Override
    public void impact(ActiveProjectile projectile, Location location) {
        if (impactParticle == null || impactCount <= 0) {
            return;
        }
        new ParticleBuilder(impactParticle)
                .location(location)
                .count(impactCount)
                .offset(impactSpread, impactSpread, impactSpread)
                .extra(impactExtra)
                .data(impactData)
                .receivers(viewRange, true)
                .force(force)
                .spawn();
    }

    private void spawnShape(Location center, Vector dir, double distance) {
        switch (shape) {
            case LINE -> spawn(center);
            case HELIX -> spawnAround(center, dir, distance * rotationPerBlock);
            case DOUBLE_HELIX -> {
                double angle = distance * rotationPerBlock;
                spawnAround(center, dir, angle);
                spawnAround(center, dir, angle + Math.PI);
            }
            case RING -> {
                for (int i = 0; i < ringPoints; i++) {
                    spawnAround(center, dir, (Math.PI * 2 * i) / ringPoints);
                }
            }
        }
    }

    private void spawnAround(Location center, Vector dir, double angle) {
        if (dir.lengthSquared() < 1.0E-6) {
            spawn(center);
            return;
        }
        dir = dir.clone().normalize();
        Vector u = perpendicular(dir);
        Vector v = dir.clone().crossProduct(u).normalize();
        Vector offset = u.multiply(Math.cos(angle) * radius).add(v.multiply(Math.sin(angle) * radius));
        spawn(center.clone().add(offset));
    }

    private void spawn(Location location) {
        new ParticleBuilder(particle)
                .location(location)
                .count(count)
                .offset(offsetX, offsetY, offsetZ)
                .extra(extra)
                .data(data)
                .receivers(viewRange, true)
                .force(force)
                .spawn();
    }

    private static Vector perpendicular(Vector dir) {
        Vector axis = Math.abs(dir.getY()) > 0.99 ? new Vector(1, 0, 0) : new Vector(0, 1, 0);
        return dir.clone().crossProduct(axis).normalize();
    }

    private static void validateData(Particle particle, Object data) {
        Class<?> type = particle.getDataType();
        if (type == Void.class) {
            return;
        }
        if (data == null || !type.isInstance(data)) {
            throw new IllegalArgumentException("Particle " + particle + " requires data of type " + type.getSimpleName());
        }
    }

    public static final class Builder {
        private final Particle particle;
        private Object data;
        private int count = 1;
        private double offsetX, offsetY, offsetZ;
        private double extra = 0;
        private double spacing = 0.25;
        private int interval = 1;
        private int delayTicks = 0;
        private TrailShape shape = TrailShape.LINE;
        private double radius = 0.3;
        private double rotationPerBlock = Math.PI / 2;
        private int ringPoints = 8;
        private int viewRange = 64;
        private boolean force = false;
        private Particle impactParticle;
        private Object impactData;
        private int impactCount;
        private double impactSpread = 0.25;
        private double impactExtra = 0.05;

        private Builder(Particle particle) {
            this.particle = particle;
        }

        public Builder data(Object data) { this.data = data; return this; }
        public Builder color(Color color) { return color(color, 1.0F); }
        public Builder color(Color color, float size) { return data(new Particle.DustOptions(color, size)); }
        public Builder transition(Color from, Color to, float size) { return data(new Particle.DustTransition(from, to, size)); }
        public Builder block(Material material) { return data(material.createBlockData()); }
        public Builder item(Material material) { return data(new ItemStack(material)); }

        public Builder count(int count) { this.count = Math.max(1, count); return this; }
        public Builder offset(double x, double y, double z) { this.offsetX = x; this.offsetY = y; this.offsetZ = z; return this; }
        public Builder offset(double all) { return offset(all, all, all); }
        public Builder extra(double extra) { this.extra = extra; return this; }
        public Builder speed(double speed) { return extra(speed); }
        public Builder spacing(double spacing) { this.spacing = Math.max(0, spacing); return this; }
        public Builder interval(int ticks) { this.interval = Math.max(1, ticks); return this; }
        public Builder delay(int ticks) { this.delayTicks = Math.max(0, ticks); return this; }
        public Builder shape(TrailShape shape) { this.shape = shape; return this; }
        public Builder radius(double radius) { this.radius = radius; return this; }
        public Builder rotation(double radiansPerBlock) { this.rotationPerBlock = radiansPerBlock; return this; }
        public Builder ringPoints(int points) { this.ringPoints = Math.max(1, points); return this; }
        public Builder viewRange(int blocks) { this.viewRange = blocks; return this; }
        public Builder force(boolean force) { this.force = force; return this; }

        public Builder impact(int count) { return impact(particle, data, count); }
        public Builder impact(Particle particle, int count) { return impact(particle, null, count); }
        public Builder impact(Particle particle, Object data, int count) {
            this.impactParticle = particle;
            this.impactData = data;
            this.impactCount = count;
            return this;
        }
        public Builder impactSpread(double spread) { this.impactSpread = spread; return this; }
        public Builder impactSpeed(double speed) { this.impactExtra = speed; return this; }

        public ParticleTrail build() {
            validateData(particle, data);
            if (impactParticle != null) {
                validateData(impactParticle, impactData);
            }
            return new ParticleTrail(this);
        }
    }
}
