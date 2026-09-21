package net.minebo.cobalt.service.scheduler.listener;

import net.minebo.cobalt.service.scheduler.SchedulerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class SchedulerListener implements Listener {

    private final SchedulerService service;

    public SchedulerListener(SchedulerService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        service.handleQuit(event.getPlayer());
    }
}