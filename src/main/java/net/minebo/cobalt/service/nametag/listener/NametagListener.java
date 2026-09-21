package net.minebo.cobalt.service.nametag.listener;

import net.minebo.cobalt.Cobalt;
import net.minebo.cobalt.service.nametag.NametagService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class NametagListener implements Listener {

    private final NametagService service;

    public NametagListener(NametagService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Cobalt.getInstance(), () -> {
            if (!player.isOnline()) {
                return;
            }
            service.refresh(player);
            service.refreshTarget(player);
        }, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        service.handleQuit(event.getPlayer());
    }
}