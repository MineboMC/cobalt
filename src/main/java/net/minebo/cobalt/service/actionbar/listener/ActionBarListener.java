package net.minebo.cobalt.service.actionbar.listener;

import net.minebo.cobalt.service.actionbar.ActionBarService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ActionBarListener implements Listener {

    private final ActionBarService service;

    public ActionBarListener(ActionBarService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        service.handleQuit(event.getPlayer());
    }
}
