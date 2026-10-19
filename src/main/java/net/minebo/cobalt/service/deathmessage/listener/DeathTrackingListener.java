package net.minebo.cobalt.service.deathmessage.listener;

import net.minebo.cobalt.service.deathmessage.DeathMessageService;
import net.minebo.cobalt.service.deathmessage.tracker.DamageResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class DeathTrackingListener implements Listener {

    private final DeathMessageService service;

    public DeathTrackingListener(DeathMessageService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        if (event.getFinalDamage() <= 0.0D) {
            return;
        }
        service.recordDamage(victim.getUniqueId(), DamageResolver.resolve(event));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent event) {
        service.handleDeath(event);
    }
}