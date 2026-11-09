package net.minebo.cobalt.service.projectile.example;

import net.minebo.cobalt.service.projectile.ProjectileService;
import net.minebo.cobalt.service.projectile.ProjectileType;
import net.minebo.cobalt.service.projectile.trail.ParticleTrail;
import net.minebo.cobalt.service.projectile.trail.TrailShape;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

public final class ExampleProjectiles {

    private ExampleProjectiles() {
    }

    public static void registerAll(ProjectileService service) {
        ParticleTrail flame = ParticleTrail.of(Particle.FLAME)
                .shape(TrailShape.HELIX)
                .radius(0.3)
                .spacing(0.15)
                .impact(Particle.LAVA, 10)
                .build();
        ParticleTrail smoke = ParticleTrail.of(Particle.SMOKE)
                .spacing(0.3)
                .offset(0.05)
                .build();
        ParticleTrail laser = ParticleTrail.dust(Color.RED, 0.8F)
                .spacing(0.1)
                .impact(12)
                .impactSpread(0.15)
                .build();
        ParticleTrail sparkle = ParticleTrail.of(Particle.END_ROD)
                .shape(TrailShape.DOUBLE_HELIX)
                .radius(0.4)
                .rotation(Math.PI)
                .spacing(0.2)
                .build();
        ParticleTrail ring = ParticleTrail.of(Particle.ELECTRIC_SPARK)
                .shape(TrailShape.RING)
                .radius(0.5)
                .ringPoints(10)
                .spacing(1.0)
                .build();
        ParticleTrail rubble = ParticleTrail.of(Particle.BLOCK)
                .block(Material.COBBLESTONE)
                .spacing(0.5)
                .offset(0.1)
                .impact(30)
                .impactSpread(0.5)
                .build();

        service.registerTrail("flame", flame);
        service.registerTrail("smoke", smoke);
        service.registerTrail("laser", laser);
        service.registerTrail("sparkle", sparkle);
        service.registerTrail("ring", ring);
        service.registerTrail("rubble", rubble);

        service.registerType("fireball", ProjectileType.item(Material.FIRE_CHARGE)
                .speed(1.6)
                .gravity(0.01)
                .damage(5)
                .spin(25)
                .launchSound(Sound.ENTITY_BLAZE_SHOOT)
                .hitSound(Sound.ENTITY_GENERIC_EXPLODE)
                .soundVolume(0.6F, 1.2F)
                .trails(flame, smoke)
                .onHitEntity((projectile, hit) -> hit.entity().setFireTicks(60))
                .build());

        service.registerType("boulder", ProjectileType.block(Material.COBBLESTONE)
                .scale(0.8F)
                .speed(1.2)
                .gravity(0.06)
                .hitbox(0.6)
                .damage(8)
                .knockback(1.2)
                .spin(12)
                .hitSound(Sound.BLOCK_STONE_BREAK)
                .trail(rubble)
                .build());

        service.registerType("laser", ProjectileType.item(Material.REDSTONE)
                .scale(0.25F)
                .speed(3.0)
                .noGravity()
                .drag(1.0)
                .maxDistance(60)
                .pierce(2)
                .damage(3)
                .ignoreNoDamageTicks(true)
                .launchSound(Sound.BLOCK_BEACON_ACTIVATE)
                .soundVolume(0.5F, 2.0F)
                .trail(laser)
                .build());

        service.registerType("bouncer", ProjectileType.item(Material.SLIME_BALL)
                .speed(1.0)
                .gravity(0.05)
                .bounces(4, 0.7)
                .lifetime(200)
                .damage(2)
                .hitSound(Sound.BLOCK_SLIME_BLOCK_HIT)
                .trail(sparkle)
                .build());

        service.registerType("shotgun", ProjectileType.item(Material.IRON_NUGGET)
                .scale(0.3F)
                .speed(2.0)
                .spread(8)
                .maxDistance(25)
                .damage(1.5)
                .ignoreNoDamageTicks(true)
                .trail(ParticleTrail.of(Particle.CRIT).spacing(0.4).build())
                .build());

        service.registerType("orb", ProjectileType.block(Material.SEA_LANTERN)
                .scale(0.4F)
                .speed(0.6)
                .noGravity()
                .drag(1.0)
                .glow(Color.AQUA)
                .collideWithBlocks(false)
                .lifetime(80)
                .spin(10)
                .trail(ring)
                .onTick(projectile -> projectile.world().getNearbyEntities(projectile.location(), 3, 3, 3).stream()
                        .filter(entity -> entity instanceof LivingEntity && !entity.equals(projectile.shooter()))
                        .findFirst()
                        .ifPresent(target -> {
                            var toward = ((LivingEntity) target).getEyeLocation().toVector()
                                    .subtract(projectile.location().toVector()).normalize().multiply(0.6);
                            projectile.setVelocity(projectile.velocity().multiply(0.8).add(toward.multiply(0.2)));
                        }))
                .damage(4)
                .build());
    }
}
