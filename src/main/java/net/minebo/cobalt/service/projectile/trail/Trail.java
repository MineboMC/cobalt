package net.minebo.cobalt.service.projectile.trail;

import net.minebo.cobalt.service.projectile.ActiveProjectile;
import org.bukkit.Location;

@FunctionalInterface
public interface Trail {

    /**
     * Draws the segment the projectile travelled this tick.
     *
     * @param distance total distance the projectile had travelled when it was at {@code from},
     *                 so spacing and helix phase stay continuous across ticks
     */
    void render(ActiveProjectile projectile, Location from, Location to, double distance);

    default void impact(ActiveProjectile projectile, Location location) {
    }
}
