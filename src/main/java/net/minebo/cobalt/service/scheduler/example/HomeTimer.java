package net.minebo.cobalt.service.scheduler.example;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minebo.cobalt.service.scheduler.timer.ScheduledTimer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HomeTimer extends ScheduledTimer {

    private final Map<UUID, Location> starts = new ConcurrentHashMap<>();

    @Override
    public String getId() {
        return "home";
    }

    @Override
    public void onStart(Player player) {
        if (player == null) {
            return;
        }
        starts.put(player.getUniqueId(), player.getLocation());
        player.sendMessage(Component.text("Teleporting home in 10 seconds. Don't move.", NamedTextColor.YELLOW));
    }

    @Override
    public void onTick(Player player, long remainingMillis) {
    }

    @Override
    public void onComplete(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        starts.remove(player.getUniqueId());
        player.teleport(player.getWorld().getSpawnLocation());
        player.sendMessage(Component.text("Teleported home.", NamedTextColor.GREEN));
    }

    @Override
    public void onCancel(Player player) {
        if (player == null) {
            return;
        }
        starts.remove(player.getUniqueId());
        player.sendMessage(Component.text("Teleport cancelled.", NamedTextColor.RED));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!scheduler().isTimerActive(player, this)) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        scheduler().cancelTimer(player, this);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (scheduler().isTimerActive(player, this)) {
            scheduler().cancelTimer(player, this);
        }
    }
}