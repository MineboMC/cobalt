package net.minebo.cobalt.service.hologram.listener;

import net.minebo.cobalt.service.hologram.HologramService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class HologramListener implements Listener {
    private final HologramService service;
    public HologramListener(HologramService service) { this.service = service; }

    @EventHandler public void onJoin(PlayerJoinEvent event) { service.handleJoin(event.getPlayer()); }
    @EventHandler public void onQuit(PlayerQuitEvent event) { service.handleQuit(event.getPlayer()); }
    @EventHandler public void onWorld(PlayerChangedWorldEvent event) { service.handleWorld(event.getPlayer()); }
}