package net.minebo.cobalt.service.projectile.event;

import net.minebo.cobalt.service.projectile.ActiveProjectile;
import net.minebo.cobalt.service.projectile.ProjectileHit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired before a Cobalt projectile applies a hit.
 * <p>
 * Cancelling suppresses the hit's damage, knockback, callbacks and impact effects. When an entity
 * hit is cancelled the projectile passes through that entity; when a block hit is cancelled the
 * projectile is removed silently.
 */
public final class CobaltProjectileHitEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final ActiveProjectile projectile;
    private final ProjectileHit hit;
    private boolean cancelled;

    public CobaltProjectileHitEvent(ActiveProjectile projectile, ProjectileHit hit) {
        this.projectile = projectile;
        this.hit = hit;
    }

    public ActiveProjectile getProjectile() { return projectile; }
    public ProjectileHit getHit() { return hit; }
    public Entity getShooter() { return projectile.shooter(); }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
