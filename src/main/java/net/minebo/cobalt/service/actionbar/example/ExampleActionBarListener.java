package net.minebo.cobalt.service.actionbar.example;

import net.minebo.cobalt.service.actionbar.ActionBarService;
import net.minebo.cobalt.util.Coloring;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.concurrent.TimeUnit;

/** Shows a temporary kill message for 5 seconds before falling back to the provider's text. */
public final class ExampleActionBarListener implements Listener {

    private final ActionBarService service;

    public ExampleActionBarListener(ActionBarService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        service.sendTemporary(killer, Coloring.toComponent("<green>You killed <white>" + event.getEntity().getName() + "<green>!"), 5, TimeUnit.SECONDS);
    }
}
