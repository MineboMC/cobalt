package net.minebo.cobalt.service.npc.listener;

import net.minebo.cobalt.service.npc.NpcService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class NpcBukkitListener implements Listener {

    private final NpcService service;

    public NpcBukkitListener(NpcService service) {
        this.service = service;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        service.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.handleQuit(event.getPlayer());
    }

    @EventHandler
    public void onWorld(PlayerChangedWorldEvent event) {
        service.handleWorld(event.getPlayer());
    }
}